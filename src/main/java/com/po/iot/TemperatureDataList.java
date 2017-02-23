package com.po.iot;

import java.util.List;

public class TemperatureDataList {
	private final List<long[]> temperatures;

	public TemperatureDataList(List<long[]> temperatures) {
		this.temperatures = temperatures;
	}

	public List<long[]> getTemperatures() {
		return temperatures;
	}
	
}
