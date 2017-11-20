package com.ge.predix.pae.cassandra.spring.cv.service;

import java.util.Base64;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestOperations;

import com.ge.predix.pae.cassandra.spring.configuration.CassandraConnectionFactory;
import com.ge.predix.pae.cassandra.spring.cv.lucene.ResumeSearch;
import com.ge.predix.pae.cassandra.spring.cv.repository.ResumeRepository;

/**
 * The Class SolrAutoIndexingService.
 *
 * @author Thomas Thinh PHAM 502640065
 */
@Component
public class SolrAutoIndexingService implements
		ApplicationListener<ContextRefreshedEvent> {

	/** The log. */
	private static Logger log = Logger.getLogger(SolrAutoIndexingService.class);

	/** The repository. */
	@Autowired
	private ResumeRepository repository;

	/** The rest template. */
	@Autowired
	private RestOperations restTemplate;

	/** The search. */
	// @Autowired
	// private ResumeSearch search;

	/** The cassandra connection factory. */
	@Autowired
	private CassandraConnectionFactory cassandraConnectionFactory;

	@Override
	public void onApplicationEvent(ContextRefreshedEvent arg0) {
		// Try to index all existing data in Cassandra
		/*
		 log.info("Start indexing all data in Cassandra once application start-up.");
		repository.findAll().forEach(item -> {
			try {
				search.index(item);
			} catch (Exception e) {
			}
		});
		*/
		try {
			log.info("Start configuring the index in Cassandra Solr once application start-up.");
			String authorization = String.format("%s:%s",
					cassandraConnectionFactory.getProperties().getUsername(),
					cassandraConnectionFactory.getProperties().getPassword());
			authorization = "Basic "
					+ Base64.getEncoder().encodeToString(
							authorization.getBytes());
			// log.info("Authorization header: " + authorization);
			MultiValueMap headers = new LinkedMultiValueMap();
			headers.add("Authorization", authorization);
			headers.add("Content-Type", "application/json");
			HttpEntity requestEntity = new HttpEntity(headers);
			String[] nodeIp = cassandraConnectionFactory.getProperties()
					.getContactPoints().split(",");
			String url = String
					.format("http://%s:8983/solr/admin/cores?action=CREATE&name=%s.%s&generateResources=true&reindex=true",
							nodeIp[nodeIp.length - 1],
							cassandraConnectionFactory.getProperties()
									.getKeyspaceName(), "resume");
			log.info("Url for enabling Cassandar index: " + url);
			restTemplate.exchange(url, HttpMethod.GET, requestEntity,
					String.class);
		} catch (Exception e) {
			log.warn(
					"Error when calling to cassandar solr to enable indexing. ",
					e);
		}
	}
}
