package com.po.iot;

public class EnvData {
	private final long timestamp;
	private final double temperature;
	private final double humidity;
	
	public EnvData(long timestamp, double temperature, double humidity) {
		super();
		this.timestamp = timestamp;
		this.temperature = temperature;
		this.humidity = humidity;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public double getTemperature() {
		return temperature;
	}

	public double getHumidity() {
		return humidity;
	}
}
