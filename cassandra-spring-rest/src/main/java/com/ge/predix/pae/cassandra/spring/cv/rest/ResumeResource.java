package com.ge.predix.pae.cassandra.spring.cv.rest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.ge.predix.pae.cassandra.spring.cv.model.Resume;
import com.ge.predix.pae.cassandra.spring.cv.model.SolrResume;
import com.ge.predix.pae.cassandra.spring.cv.service.CurriculumService;

@RestController
@RequestMapping(value = "/resume")
public class ResumeResource {

	@Autowired
	private CurriculumService service;

	@RequestMapping(method = RequestMethod.GET, value = "")
	public List<Resume> getAllResume() {
		return service.retrieveAll();
	}

	@RequestMapping(method = RequestMethod.GET, value = "/{content}", produces = "application/json;charset=utf-8")
	public String getCV(@PathVariable("content") String text) {
		return service.searchCVByBioUsingSolr(text);
	}

	@RequestMapping(method = RequestMethod.POST, value = "")
	public ResponseEntity<String> insertResume(@RequestBody Resume curriculum) {
		service.save(curriculum);
		return new ResponseEntity<String>("Sucess", HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.DELETE, value = "/{nickname}")
	public ResponseEntity<String> deleteResume(
			@PathVariable(value = "nickname") String nickName) {
		service.deleteResume(nickName);
		return new ResponseEntity<String>("Sucess", HttpStatus.OK);
	}
}
