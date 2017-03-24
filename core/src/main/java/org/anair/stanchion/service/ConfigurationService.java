package org.anair.stanchion.service;

import java.util.List;

import org.anair.stanchion.model.NameValue;


public interface ConfigurationService {

	<T> T get(String key);
	
	boolean getBoolean(String key, boolean defaultValue);
	
	List<NameValue> getAll();
	
	void update(NameValue configValue);
	
	void add(NameValue configValue);
	
	void remove(NameValue configValue);

	String[] getStringArray(String key);
	
}
