package com.ge.predix.pae.cassandra.spring.cv.dto;

import java.util.List;
import java.util.Map;

public class SolrDocsResponseDTO {

	private int numFound;
	private int start;
	private List<Map<String, String>> docs;

	/**
	 * @return the numFound
	 */
	public int getNumFound() {
		return numFound;
	}

	/**
	 * @param numFound
	 *            the numFound to set
	 */
	public void setNumFound(int numFound) {
		this.numFound = numFound;
	}

	/**
	 * @return the start
	 */
	public int getStart() {
		return start;
	}

	/**
	 * @param start
	 *            the start to set
	 */
	public void setStart(int start) {
		this.start = start;
	}

	/**
	 * @return the docs
	 */
	public List<Map<String, String>> getDocs() {
		return docs;
	}

	/**
	 * @param docs
	 *            the docs to set
	 */
	public void setDocs(List<Map<String, String>> docs) {
		this.docs = docs;
	}

}
