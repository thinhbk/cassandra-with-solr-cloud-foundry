package com.ge.predix.pae.cassandra.spring.configuration;

import javax.annotation.PostConstruct;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.cassandra.CassandraProperties;

/**
 * A factory for creating CassandraConnection objects.
 * 
 * @author ThinhPL - 502640065
 */
public class CassandraConnectionFactory {
	/** The log. */
	private static Logger log = Logger
			.getLogger(CassandraConnectionFactory.class);

	/** The cassandra properties. */
	@Autowired
	CassandraProperties cassandraProperties;

	/** The cassandra config. */
	@Autowired
	private CassandraConfig cassandraConfig;

	/** The cloud config. */
	private CloudCassandraPropertiesManager cloudConfig;

	/** The active properties. */
	CassandraProperties activeProperties;

	/**
	 * Instantiates a new cassandra connection factory.
	 */
	@PostConstruct
	public void init() {
		cloudConfig = new CloudCassandraPropertiesManager(cassandraConfig);
	}

	/**
	 * Sets the active property.
	 */
	private void setActiveProperty() {
		if (cloudConfig.hasCloudProperties()) {
			log.info("Active profile is cloud.");
			activeProperties = cloudConfig;
		} else {
			activeProperties = cassandraProperties;
		}
	}

	/**
	 * Gets the properties.
	 *
	 * @return the properties
	 */
	public CassandraProperties getProperties() {
		if (activeProperties == null)
			setActiveProperty();
		return activeProperties;
	}

	/**
	 * Gets the Solr Urls.
	 *
	 * @return the String
	 */
	public String getSolrUrl() {
		if (activeProperties == null)
			setActiveProperty();
		return cloudConfig.getSolrUrls();
	}

}