/*
 * Copyright (c) 2017 General Electric Company. All rights reserved.
 *
 * The copyright to the computer software herein is the property of
 * General Electric Company. The software may be used and/or copied only
 * with the written permission of General Electric Company or in accordance
 * with the terms and conditions stipulated in the agreement/contract
 * under which the software has been supplied.
 */
package com.ge.predix.pae.cassandra.spring.configuration;

import org.springframework.cloud.config.java.AbstractCloudConfig;
import org.springframework.cloud.config.java.ServiceScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * class Cloud Auto Config.
 *
 * @author Thomas Thinh PHAM 502640065
 */
@Configuration
@ServiceScan
@Profile("cloud")
public class CloudAutoConfig extends AbstractCloudConfig {

	/**
	 * Gets the data source.
	 *
	 * @return the data source
	 */
	/*
	 * @Bean
	 * 
	 * @Primary public DataSource getDataSource() { return
	 * connectionFactory().dataSource(); }
	 */

	/**
	 * Cassandra connection factory.
	 *
	 * @return the cassandra connection factory
	 */
	@Bean
	public CassandraConnectionFactory cassandraConnectionFactory() {
		return new CassandraConnectionFactory();
	}
	
	// @Bean
	// public WebSocketContainer webSocketContainer() {
	// return ContainerProvider.getWebSocketContainer();
	// }
}
