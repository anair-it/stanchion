package org.anair.stanchion.controller;

import java.util.List;

import org.anair.stanchion.model.NameValue;
import org.anair.stanchion.service.ConfigurationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.Lists;

@RestController
@RequestMapping(value="/config",produces=MediaType.APPLICATION_JSON_VALUE)
public class ConfigurationController {

	private static final Logger LOG = LoggerFactory.getLogger(ConfigurationController.class);
	
	@Autowired
	private ConfigurationService configurationService;
	
	@RequestMapping("list")
	public @ResponseBody List<NameValue> getConfigList() {
		List<NameValue> configValues = Lists.newArrayList();
		
		try{
			configValues = configurationService.getAll();
		}catch(NullPointerException e){
			
		}catch(Exception e){
			LOG.error(e.getMessage(), e);
		}
		return configValues;
	}
	
	@RequestMapping(value = "update/{key}/{value}", method=RequestMethod.POST)
	public @ResponseBody void updateConfig(@PathVariable String key, @PathVariable String value) {
		NameValue configValue = new NameValue(key,value);
		try{
			configurationService.update(configValue);
		}catch(Exception e){
			LOG.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}
	
	@RequestMapping(value = "add/{key}/{value}", method=RequestMethod.POST)
	public @ResponseBody void addConfig(@PathVariable String key, @PathVariable String value) {
		NameValue configValue = new NameValue(key,value);
		try{
			configurationService.add(configValue);
		}catch(Exception e){
			LOG.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}
	
	@RequestMapping(value = "remove/{key}", method=RequestMethod.POST)
	public @ResponseBody void removeConfig(@PathVariable String key) {
		NameValue configValue = new NameValue(key,null);
		try{
			configurationService.remove(configValue);
		}catch(Exception e){
			LOG.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	public void setConfigurationService(ConfigurationService configurationService) {
		this.configurationService = configurationService;
	}

}
