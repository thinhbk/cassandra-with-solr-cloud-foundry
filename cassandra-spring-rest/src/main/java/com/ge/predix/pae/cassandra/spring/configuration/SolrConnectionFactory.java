package com.ge.predix.pae.cassandra.spring.configuration;

import java.util.Collections;

import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.solr.core.SolrOperations;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.convert.CustomConversions;
import org.springframework.data.solr.core.convert.MappingSolrConverter;
import org.springframework.data.solr.core.convert.SolrConverter;
import org.springframework.data.solr.core.mapping.SimpleSolrMappingContext;
import org.springframework.data.solr.repository.config.EnableSolrRepositories;
import org.springframework.data.solr.server.SolrClientFactory;
import org.springframework.data.solr.server.support.HttpSolrClientFactory;
import org.springframework.data.solr.server.support.HttpSolrClientFactoryBean;

/**
 * A factory for creating SolrConnection objects.
 * 
 * @author ThinhPL - 502640065
 */
@Configuration
@EnableSolrRepositories("com.ge.predix.pae.cassandra.spring.cv.solr.repository")
@EnableAutoConfiguration
public class SolrConnectionFactory {

	/** The log. */
	private static Logger log = Logger.getLogger(SolrConnectionFactory.class);

	/** The cassandra connection factory. */
	@Autowired
	private CassandraConnectionFactory cassandraConnectionFactory;

	/**
	 * Solr server factory bean.
	 *
	 * @return the http solr client factory bean
	 */
	@Bean
	public HttpSolrClientFactoryBean solrServerFactoryBean() {
		log.info("solrServerFactoryBean");
		HttpSolrClientFactoryBean factory = new HttpSolrClientFactoryBean();
		String[] nodeIp = cassandraConnectionFactory.getProperties()
				.getContactPoints().split(",");
		String url = String.format("http://%s:8983/solr/%s.resume",
				nodeIp[nodeIp.length - 1], cassandraConnectionFactory
						.getProperties().getKeyspaceName());
		factory.setUrl(url);
		log.info("Solr server url - " + url);
		return factory;
	}

	/**
	 * Credentials.
	 *
	 * @return the credentials
	 */
	@Bean
	public Credentials credentials() {
		return new UsernamePasswordCredentials(cassandraConnectionFactory
				.getProperties().getUsername(), cassandraConnectionFactory
				.getProperties().getPassword());
	}

	/**
	 * Solr client factory.
	 *
	 * @param solrClient
	 *            the solr client
	 * @param credentials
	 *            the credentials
	 * @return the solr client factory
	 */
	@Bean
	public SolrClientFactory solrClientFactory(SolrClient solrClient,
			Credentials credentials) {
		return new HttpSolrClientFactory(solrClient, "", credentials, "BASIC");

	}

	/**
	 * Solr template.
	 *
	 * @param solrClientFactory
	 *            the solr client factory
	 * @return the solr operations
	 * @throws Exception
	 *             the exception
	 */
	@Bean
	public SolrOperations solrTemplate(SolrClientFactory solrClientFactory)
			throws Exception {
		SolrTemplate solrTemplate = new SolrTemplate(solrClientFactory);
		return solrTemplate;
	}

	/**
	 * Solr client.
	 *
	 * @return the solr client
	 * @throws Exception
	 *             the exception
	 */
	@Bean
	public SolrClient solrClient() throws Exception {
		SolrClient client = solrServerFactoryBean().getObject();
		return client;
	}

	/**
	 * Solr converter.
	 *
	 * @return the solr converter
	 * @throws Exception
	 *             the exception
	 */
	@Bean
	public SolrConverter solrConverter() throws Exception {
		MappingSolrConverter mappingSolrConverter = new MappingSolrConverter(
				new SimpleSolrMappingContext());
		mappingSolrConverter.setCustomConversions(solrCustomConversions());
		return mappingSolrConverter;
	}

	/**
	 * Solr custom conversions.
	 *
	 * @return the custom conversions
	 */
	@Bean
	public CustomConversions solrCustomConversions() {
		return new CustomConversions(Collections.emptyList());
	}
}
