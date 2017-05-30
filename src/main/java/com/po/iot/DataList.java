package com.po.iot;

import java.util.List;

public class DataList {
	public List<Object[]> data;
	
	public DataList(List<Object[]> data) {
		this.data = data;
	}
	
	public List<Object[]> getData() {
		return data;
	}
}
