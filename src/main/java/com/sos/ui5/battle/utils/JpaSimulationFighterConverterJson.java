package com.sos.ui5.battle.utils;

import java.io.IOException;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sos.ui5.battle.sos.simulator.SimulationFighter;

@Converter(autoApply = true)
public class JpaSimulationFighterConverterJson implements AttributeConverter<SimulationFighter, String> {

	private final static ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public String convertToDatabaseColumn(SimulationFighter meta) {
		if (meta==null) {
			return "[]";
		}

		try {
			return objectMapper.writeValueAsString(meta);
		} catch (JsonProcessingException ex) {
			return null;
		}
	}

	@Override
	public SimulationFighter convertToEntityAttribute(String dbData) {
		if (dbData==null) {
			return null;
		}
		try {
			return objectMapper.readValue(dbData,  new TypeReference<SimulationFighter>() {
			});
		} catch (IOException ex) {
			return null;
		}
	}

}