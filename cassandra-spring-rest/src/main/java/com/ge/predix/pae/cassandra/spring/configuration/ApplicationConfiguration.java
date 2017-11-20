package com.ge.predix.pae.cassandra.spring.configuration;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.cassandra.CassandraProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.config.CassandraClusterFactoryBean;
import org.springframework.data.cassandra.config.CassandraEntityClassScanner;
import org.springframework.data.cassandra.config.CassandraSessionFactoryBean;
import org.springframework.data.cassandra.config.SchemaAction;
import org.springframework.data.cassandra.config.java.AbstractCassandraConfiguration;
import org.springframework.data.cassandra.convert.CassandraConverter;
import org.springframework.data.cassandra.convert.CustomConversions;
import org.springframework.data.cassandra.convert.MappingCassandraConverter;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.data.cassandra.mapping.BasicCassandraMappingContext;
import org.springframework.data.cassandra.mapping.CassandraMappingContext;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;

import com.datastax.driver.core.AuthProvider;
import com.datastax.driver.core.PlainTextAuthProvider;

/**
 * The Class ApplicationConfiguration.
 *
 * @author Thomas Thinh PHAM 502640065
 */
@Configuration
@EnableCassandraRepositories("com.ge.predix.pae.cassandra.spring.cv.repository")
@EnableAutoConfiguration
@EnableConfigurationProperties(CassandraProperties.class)
public class ApplicationConfiguration extends AbstractCassandraConfiguration {

	/** The cassandra connection factory. */
	@Autowired
	private CassandraConnectionFactory cassandraConnectionFactory;

	@Override
	protected String getKeyspaceName() {
		return cassandraConnectionFactory.getProperties().getKeyspaceName();
	}

	@Override
	protected String getContactPoints() {
		return cassandraConnectionFactory.getProperties().getContactPoints();
	}

	@Override
	protected int getPort() {
		return cassandraConnectionFactory.getProperties().getPort();
	}

	@Override
	protected AuthProvider getAuthProvider() {
		return new PlainTextAuthProvider(cassandraConnectionFactory
				.getProperties().getUsername(), cassandraConnectionFactory
				.getProperties().getPassword());
	}

	@Override
	public SchemaAction getSchemaAction() {
		return SchemaAction.RECREATE_DROP_UNUSED; // Please note that, in
													// production, you would
													// want a different schema
	}

	/**
	 * Operations.
	 *
	 * @return the cassandra operations
	 * @throws Exception
	 *             the exception
	 */
	@Bean
	public CassandraOperations operations() throws Exception {
		return new CassandraTemplate(session().getObject(),
				new MappingCassandraConverter(
						new BasicCassandraMappingContext()));
	}

	/**
	 * Mapping context.
	 *
	 * @return the cassandra mapping context
	 */
	@Bean
	public CassandraMappingContext mappingContext() {
		BasicCassandraMappingContext basicCassandraMappingContext = new BasicCassandraMappingContext();
		basicCassandraMappingContext
				.setCustomConversions(cassandraCustomConversions());
		try {
			basicCassandraMappingContext
					.setInitialEntitySet(CassandraEntityClassScanner
							.scan(("com.ge.predix.pae.cassandra.spring.cv.model")));
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return basicCassandraMappingContext;
	}

	/**
	 * Converter.
	 *
	 * @return the cassandra converter
	 */
	@Bean
	public CassandraConverter converter() {
		return new MappingCassandraConverter(mappingContext());
	}

	@Bean
	@Override
	public CassandraClusterFactoryBean cluster() {
		CassandraClusterFactoryBean cluster = new CassandraClusterFactoryBean();
		cluster.setContactPoints(cassandraConnectionFactory.getProperties()
				.getContactPoints());
		// cluster.setContactPoints("10.72.42.92");
		cluster.setPort(cassandraConnectionFactory.getProperties().getPort());
		PlainTextAuthProvider sap = new PlainTextAuthProvider(
				cassandraConnectionFactory.getProperties().getUsername(),
				cassandraConnectionFactory.getProperties().getPassword());
		cluster.setAuthProvider(sap);
		return cluster;
	}

	@Bean
	public CassandraSessionFactoryBean session() {

		CassandraSessionFactoryBean session = new CassandraSessionFactoryBean();
		session.setCluster(cluster().getObject());
		session.setKeyspaceName(cassandraConnectionFactory.getProperties()
				.getKeyspaceName());
		session.setConverter(converter());
		session.setSchemaAction(SchemaAction.CREATE_IF_NOT_EXISTS);

		return session;
	}

	/**
	 * Cassandra custom conversions.
	 *
	 * @return the custom conversions
	 */
	public CustomConversions cassandraCustomConversions() {
		return new CustomConversions(Collections.emptyList());
	}

}