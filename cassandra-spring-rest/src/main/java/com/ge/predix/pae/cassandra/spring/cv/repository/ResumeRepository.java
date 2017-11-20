package com.ge.predix.pae.cassandra.spring.cv.repository;

import org.springframework.data.cassandra.repository.TypedIdCassandraRepository;
import org.springframework.stereotype.Repository;

import com.ge.predix.pae.cassandra.spring.cv.model.Resume;

@Repository
public interface ResumeRepository extends
		TypedIdCassandraRepository<Resume, String> {

}
