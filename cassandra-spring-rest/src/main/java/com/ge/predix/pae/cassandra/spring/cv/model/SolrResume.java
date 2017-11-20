package com.ge.predix.pae.cassandra.spring.cv.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.solr.client.solrj.beans.Field;
import org.springframework.data.annotation.Id;
import org.springframework.data.solr.core.mapping.Indexed;
import org.springframework.data.solr.core.mapping.SolrDocument;

@SolrDocument(solrCoreName = "resume")
public class SolrResume {

	@Id
	@Field
	@Indexed(name = "nickName", type = "string")
	private String nickName;

	@Field
	private String name;

	@Field
	private String country;

	@Field
	@Indexed(name = "bio", type = "string")
	private String bio;

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getBio() {
		return bio;
	}

	public void setBio(String bio) {
		this.bio = bio;
	}

	@Override
	public String toString() {

		return ToStringBuilder.reflectionToString(this,
				ToStringStyle.SHORT_PREFIX_STYLE);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof SolrResume) {
			SolrResume other = SolrResume.class.cast(obj);
			return new EqualsBuilder().append(other.nickName, nickName)
					.isEquals();
		}
		return false;
	}

	@Override
	public int hashCode() {

		return new HashCodeBuilder().append(nickName).toHashCode();
	}
}
