package com.ge.predix.pae.cassandra.spring.cv.service;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.apache.lucene.queryparser.classic.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ge.predix.pae.cassandra.spring.configuration.CassandraConnectionFactory;
import com.ge.predix.pae.cassandra.spring.cv.dto.SolrResponseDTO;
import com.ge.predix.pae.cassandra.spring.cv.lucene.ResumeSearch;
import com.ge.predix.pae.cassandra.spring.cv.model.Resume;
import com.ge.predix.pae.cassandra.spring.cv.repository.ResumeRepository;
import com.ge.predix.pae.cassandra.spring.cv.solr.repository.ResumeSolrRepository;

/**
 * The Class CurriculumService.
 *
 * @author Thomas Thinh PHAM 502640065
 */
@Service
@Scope("prototype")
public class CurriculumService {

	/** The log. */
	private static Logger log = Logger.getLogger(CurriculumService.class);

	/** The repository. */
	@Autowired
	private ResumeRepository repository;

	/** The repository. */
	@Autowired
	private ResumeSolrRepository solrRepository;

	/** The search. */
	@Autowired
	private ResumeSearch search;

	/** The cassandra connection factory. */
	@Autowired
	private CassandraConnectionFactory cassandraConnectionFactory;

	/** The rest template. */
	@Autowired
	private RestTemplate restTemplate;

	/** The mapping jackson2 http message converter. */
	@Autowired
	private MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter;

	/**
	 * Save.
	 *
	 * @param cv
	 *            the cv
	 */
	public void save(Resume cv) {
		repository.save(cv);
		try {
			search.index(cv);
		} catch (IOException e) {
			log.warn("Error when index the resume", e);
		}
	}

	/**
	 * Search cv by bio.
	 *
	 * @param text
	 *            the text
	 * @return the list
	 */
	public List<Resume> searchCVByBio(String text) {
		try {
			List<String> ids = search.findByBio(text);
			return (List<Resume>) repository.findAll(ids);
		} catch (ParseException | IOException e) {
			log.warn("Error when search the resume by keyword", e);
		}
		return Collections.emptyList();
	}

	/**
	 * Search cv by bio.
	 *
	 * @param text
	 *            the text
	 * @return the list
	 */
	public String searchCVByBioUsingSolr(String text) {
		String authorization = String.format("%s:%s",
				cassandraConnectionFactory.getProperties().getUsername(),
				cassandraConnectionFactory.getProperties().getPassword());
		authorization = "Basic "
				+ Base64.getEncoder().encodeToString(authorization.getBytes());
		// log.info("Authorization header: " + authorization);
		MultiValueMap headers = new LinkedMultiValueMap();
		headers.add("Authorization", authorization);
		headers.add("Content-Type", "application/json");
		HttpEntity requestEntity = new HttpEntity(headers);
		String[] nodeIp = cassandraConnectionFactory.getProperties()
				.getContactPoints().split(",");
		String url = String.format("http://%s:8983/solr/%s.%s/select",
				nodeIp[nodeIp.length - 1], cassandraConnectionFactory
						.getProperties().getKeyspaceName(), "resume");
		restTemplate.getMessageConverters().add(
				mappingJackson2HttpMessageConverter);
		Map<String, String> pathParams = new HashMap<String, String>();
		pathParams.put("keyspace", cassandraConnectionFactory.getProperties()
				.getKeyspaceName());
		Map<String, String> uriParams = new HashMap<String, String>();
		uriParams.put("omitHeader", "on");
		// uriParams.put("indent", "on");
		uriParams.put("wt", "json");
		uriParams.put("rows", "100");
		String[] pattern = text.split(" ");
		String query = String.join(" AND ", Arrays.asList(pattern).stream()
				.map(item -> "bio:" + item).collect(Collectors.toList()));
		uriParams.put("q", query);
		log.info("Using RestTemplate to query Solr directly; Url for querying: "
				+ buildSafeUrl(url, pathParams, uriParams).toString());
		ResponseEntity<SolrResponseDTO> response = restTemplate.exchange(
				buildSafeUrl(url, pathParams, uriParams), HttpMethod.GET,
				requestEntity, SolrResponseDTO.class);
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			return objectMapper.writeValueAsString(response.getBody()
					.getResponse().getDocs());
		} catch (JsonProcessingException e) {
			log.warn("Error when calling to Cassandra solr server.", e);
			return null;
		}
		// return solrRepository.findByBioSolr(text);
	}

	/**
	 * Retrieve all.
	 *
	 * @return the list
	 */
	public List<Resume> retrieveAll() {
		try {
			return (List<Resume>) repository.findAll();
		} catch (Exception e) {
			log.warn("Error when getting all resume", e);
		}
		return Collections.emptyList();
	}

	/**
	 * Delete resume.
	 *
	 * @param nickName
	 *            the nick name
	 */
	public void deleteResume(String nickName) {
		repository.delete(nickName);
	}

	/**
	 * Builds the safe url.
	 *
	 * @param fromUrl
	 *            the from url
	 * @param pathParams
	 *            the path params
	 * @param uriParams
	 *            the uri params
	 * @return the uri
	 */
	private URI buildSafeUrl(String fromUrl, Map<String, String> pathParams,
			Map<String, String> uriParams) {

		// Query parameters
		UriComponentsBuilder builder = UriComponentsBuilder
				.fromUriString(fromUrl);
		for (String key : uriParams.keySet()) {
			builder = builder.queryParam(key, uriParams.get(key));
		}

		/**
		 * Console output: http://abc.com/solr/keyspace.table/select?q
		 * =name:*&rows=100
		 */
		return builder.buildAndExpand(pathParams).toUri();
	}
}
