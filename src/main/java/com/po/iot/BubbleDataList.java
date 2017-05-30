package com.po.iot;

import java.util.List;

public class BubbleDataList {
	public List<Object[]> timestamps;
	
	public BubbleDataList(List<Object[]> timestamps) {
		this.timestamps = timestamps;
	}
	
	public List<Object[]> getTimestamps() {
		return timestamps;
	}
}
