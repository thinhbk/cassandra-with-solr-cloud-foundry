package com.ge.predix.pae.cassandra.spring.cv.solr.repository;

import java.util.List;

import org.springframework.data.solr.repository.Query;
import org.springframework.data.solr.repository.SolrCrudRepository;
import org.springframework.stereotype.Repository;

import com.ge.predix.pae.cassandra.spring.cv.model.SolrResume;

@Repository
public interface ResumeSolrRepository extends
		SolrCrudRepository<SolrResume, String> {
	@Query("bio:*?0*")
	List<SolrResume> findByBioSolr(String name);
}
