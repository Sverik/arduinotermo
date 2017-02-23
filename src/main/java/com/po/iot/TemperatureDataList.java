package com.po.iot;

import java.util.List;

public class TemperatureDataList {
	private final List<Object[]> temperatures;

	public TemperatureDataList(List<Object[]> temperatures) {
		this.temperatures = temperatures;
	}

	public List<Object[]> getTemperatures() {
		return temperatures;
	}
	
}
