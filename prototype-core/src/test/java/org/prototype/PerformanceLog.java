package org.prototype;

import org.prototype.annotation.Subscribe;
import org.prototype.business.PerformanceData;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * 性能日志.
 * @author lj
 *
 */
@Slf4j
@Subscribe(type = PerformanceData.TYPE)
public class PerformanceLog {

	private ObjectMapper mapper;
	
	public PerformanceLog(ObjectMapper mapper) {
		this.mapper = mapper;
	}

	public void onMessage(PerformanceData data) {
		try {
			log.info("Performance data : " + mapper.writeValueAsString(data));
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

}
