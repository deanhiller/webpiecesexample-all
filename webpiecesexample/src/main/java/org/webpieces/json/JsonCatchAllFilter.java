package org.webpieces.json;

import javax.inject.Inject;

import org.webpieces.plugin.json.JacksonCatchAllFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonCatchAllFilter extends JacksonCatchAllFilter {

	@Inject
	public JsonCatchAllFilter(ObjectMapper mapper) {
		super(mapper);
	}
	

	
}
