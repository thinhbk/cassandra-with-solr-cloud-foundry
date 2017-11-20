package com.ge.predix.pae.cassandra.spring.configuration;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.boot.autoconfigure.cassandra.CassandraProperties;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;

/**
 * The Class CloudCassandraPropertiesManager.
 *
 * @author Thomas Thinh PHAM 502640065
 */
public class CloudCassandraPropertiesManager extends CassandraProperties {
	/** The Constant VCAP_SERVICES. */
	private static final String VCAP_SERVICES = "VCAP_SERVICES";

	/** The log. */
	private static Logger log = Logger
			.getLogger(CloudCassandraPropertiesManager.class);

	/** The cassandra config. */
	private CassandraConfig cassandraConfig;

	/** The solr url. */
	private String solrUrl;

	/**
	 * Instantiates a new cloud cassandra properties manager.
	 *
	 * @param cassandraConfig
	 *            the cassandra config
	 */
	public CloudCassandraPropertiesManager(CassandraConfig cassandraConfig) {
		super();
		this.cassandraConfig = cassandraConfig;
		if (hasCloudProperties()) {
			log.info("Active profile is cloud.");
			setProperties();
		} else {
			log.info("Active profile is dev.");
		}
	}

	/**
	 * Checks for cloud properties.
	 *
	 * @return true, if successful
	 */
	public boolean hasCloudProperties() {
		String vCapVariable = getVCapVariable();
		log.info(String.format("(VCAP) variable: %s", vCapVariable));
		return vCapVariable != null;
	}

	/**
	 * Gets the properties.
	 *
	 * @return the properties
	 */
	public Map<String, Object> getProperties() {
		Map<String, Object> jsonMap = getJsonMapFromEnvironment();
		return jsonMap;
	}

	/**
	 * Gets the json map from environment.
	 *
	 * @return the json map from environment
	 */
	private Map<String, Object> getJsonMapFromEnvironment() {
		JsonParser jsonParser = JsonParserFactory.getJsonParser();
		Map<String, Object> jsonMap;
		jsonMap = jsonParser.parseMap(getVCapVariable());
		return (Map<String, Object>) ((List<Map<String, Object>>) jsonMap
				.get("predix-columnar-store")).get(0).get("credentials");
	}

	/**
	 * Sets the properties.
	 */
	public void setProperties() {
		Map<String, Object> jsonMap = this.getProperties();
		ArrayList<Map<String, Object>> dataCenters = (ArrayList<Map<String, Object>>) jsonMap
				.get("datacenters");
		log.info("data Center (VCAP): " + dataCenters.toString());
		// Get the list of the ip of solr enabled DC are listed at
		// last, which will be used in the SolrConnectionFactory class
		Map<String, Object> solrCluster = dataCenters
				.stream()
				.filter(item -> item.get("dc_name") != null
						&& ((String) item.get("dc_name")).toLowerCase()
								.indexOf("solr") >= 0).findFirst().orElse(null);
		if (solrCluster != null) {
			this.solrUrl = String.join(",",
					((ArrayList<String>) solrCluster.get("nodes")));
		} else {
			this.solrUrl = "";
		}
		// Do the trick to assure that the ip that enables solr is listed at the
		// end of the cluster nodes
		dataCenters.sort(new Comparator<Map<String, Object>>() {
			@Override
			public int compare(Map<String, Object> arg0,
					Map<String, Object> arg1) {
				if (arg0.get("dc_name") != null
						&& ((String) arg0.get("dc_name")).toLowerCase()
								.indexOf("solr") >= 0) {
					return 1;
				}
				if (arg1.get("dc_name") != null
						&& ((String) arg1.get("dc_name")).toLowerCase()
								.indexOf("solr") >= 0) {
					return -1;
				}
				return 0;
			}
		});

		ArrayList<String> nodeIps = dataCenters.stream()
				.map(item -> (ArrayList<String>) item.get("nodes"))
				.reduce(new ArrayList<String>(), (item1, item2) -> {
					item1.addAll(item2);
					return item1;
				});
		log.info(String.format("Ip of cassandra (VCAP): %s",
				String.join(",", nodeIps)));
		this.setPort((Integer) jsonMap.get("port"));
		this.setContactPoints(String.join(",", nodeIps));
		this.setKeyspaceName(cassandraConfig.getKeyspace());
		this.setPassword((String) jsonMap.get("password"));
		this.setUsername((String) jsonMap.get("user_name"));
	}

	/**
	 * Gets the v cap variable.
	 *
	 * @return the v cap variable
	 */
	public String getVCapVariable() {
		return System.getenv(VCAP_SERVICES);
	}

	/**
	 * Gets the v cap variable.
	 *
	 * @return the v cap variable
	 */
	public String getSolrUrls() {
		return System.getenv(VCAP_SERVICES);
	}
}