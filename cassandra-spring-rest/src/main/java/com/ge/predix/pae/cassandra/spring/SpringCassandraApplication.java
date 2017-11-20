package com.ge.predix.pae.cassandra.spring;

import javax.servlet.MultipartConfigElement;

import org.apache.http.impl.client.HttpClients;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.cassandra.CassandraDataAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;

@SpringBootApplication(exclude = { CassandraDataAutoConfiguration.class })
public class SpringCassandraApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringCassandraApplication.class, args);
	}

	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurerAdapter() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/**").allowedOrigins("*")
						.allowedMethods("*");
			}
		};
	}

	@Bean
	MultipartConfigElement multipartConfigElement() {
		return new MultipartConfigElement("");
	}

	@Bean
	@Primary
	public MappingJackson2HttpMessageConverter jacksonConverter() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		mapper.setDateFormat(new ISO8601DateFormat());
		mapper.setVisibility(PropertyAccessor.ALL, Visibility.NONE);
		mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
		MappingJackson2HttpMessageConverter jacksonConverter = new MappingJackson2HttpMessageConverter();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
				false);
		jacksonConverter.setObjectMapper(mapper);

		return jacksonConverter;
	}

	// @Bean
	// public StringHttpMessageConverter stringMessageConverter() {
	// return new StringHttpMessageConverter();
	// }

	@Bean
	public ClientHttpRequestFactory clientHttpRequestFactory() {
		HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory();
		clientHttpRequestFactory.setConnectTimeout(120 * 1000);
		clientHttpRequestFactory.setReadTimeout(120 * 1000);
		clientHttpRequestFactory
				.setHttpClient(HttpClients
						.custom()
						.setRetryHandler(
								(exception, executionCount, context) -> {
									if (executionCount > 5) {
										// LOGGER.warn("Maximum tries reached
										// for client http
										// pool ");
										return false;
									}
									if (exception instanceof org.apache.http.NoHttpResponseException) {
										// LOGGER.warn("No response from server on "
										// +
										// executionCount + " call");
										return true;
									}
									return false;
								}).build());
		return clientHttpRequestFactory;
	}

	@Bean
	public RestTemplate restOperation() {
		RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory());
		return restTemplate;
	}
}
