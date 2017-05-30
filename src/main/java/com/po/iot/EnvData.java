package com.po.iot;

public class EnvData {
	private final long timestamp;
	private final double temperature;
	private final double humidity;
	private final long bubbleTimestamp;
	
	public EnvData(long timestamp, double temperature, double humidity, long bubbleTimestamp) {
		super();
		this.timestamp = timestamp;
		this.temperature = temperature;
		this.humidity = humidity;
		this.bubbleTimestamp = bubbleTimestamp;
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
	
	public long getBubbleTimestamp() {
		return bubbleTimestamp;
	}
}
