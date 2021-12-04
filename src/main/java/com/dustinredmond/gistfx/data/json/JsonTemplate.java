package com.dustinredmond.gistfx.data.json;

import java.util.LinkedHashMap;
import java.util.Map;

class JsonTemplate {

	private Map<String, String> nameMap = new LinkedHashMap<>();

	public Map<String, String> getNameMap() {
		return nameMap;
	}

	public void setNameMap(Map<String, String> nameMap) {
		this.nameMap = nameMap;
	}
}
