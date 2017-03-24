package org.anair.stanchion.service;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.anair.stanchion.model.NameValue;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.DatabaseConfiguration;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.google.common.collect.Lists;

public class ConfigurationServiceImpl implements ConfigurationService, ApplicationContextAware {

	private Configuration configValueDao;
	private ApplicationContext applicationContext;
	
	@Override
	public List<NameValue> getAll() {
		List<NameValue> configValues = Lists.newArrayList();
		for(Iterator<String> iter = configValueDao.getKeys();iter.hasNext();){
			String key = iter.next();
			Object value =  (Object)get(key);
			configValues.add(new NameValue(key, value));
		}
		
		if(configValues != null && !configValues.isEmpty()){
			Collections.sort(configValues, new Comparator<NameValue>() {
				@Override
				public int compare(NameValue o1, NameValue o2) {
					return o1.getKey().compareTo(o2.getKey());
				}
			});
		}
		return configValues;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	@Cacheable("configValueCache")
	public <T> T get(String key) {
		return (T) configValueDao.getProperty(key);
	}
	
	@Override
	@Cacheable(value="configValueCache", key="#key")
	public boolean getBoolean(String key, boolean defaultValue) {
		return configValueDao.getBoolean(key, defaultValue);
	}
	
	@Override
	@Cacheable(value="configValueCache", key="#key")
	public String[] getStringArray(String key) {
		return configValueDao.getStringArray(key);
	}
	
	@Override
	@CacheEvict(value="configValueCache", key="#configValue.key")
	public void add(NameValue configValue) {
		if(configValueDao.containsKey(configValue.getKey())){
			configValueDao.clearProperty(configValue.getKey());
		}
		configValueDao.addProperty(configValue.getKey(), configValue.getValue());		
	}
	
	@Override
	@CacheEvict(value="configValueCache", key="#configValue.key")
	public void update(NameValue configValue) {
		if(configValueDao.containsKey(configValue.getKey())){
			configValueDao.setProperty(configValue.getKey(), configValue.getValue());
		}
	}

	@Override
	@CacheEvict(value="configValueCache", key="#configValue.key")
	public void remove(NameValue configValue) {
		if(configValueDao.containsKey(configValue.getKey())){
			configValueDao.clearProperty(configValue.getKey());
		}else{
			configValue.setMessage("Config key: " + configValue.getKey() + " does not exist.");
		}
	}
	
	
	public void setConfigValueDao(Configuration configValueDao) {
		this.configValueDao = configValueDao;
	}
	
	private void findConfigValueDaoBean(){
		setConfigValueDao(BeanFactoryUtils.beanOfTypeIncludingAncestors(applicationContext, DatabaseConfiguration.class));
	}
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
		try{
			findConfigValueDaoBean();
		} catch(NoSuchBeanDefinitionException e){}
	}

}
