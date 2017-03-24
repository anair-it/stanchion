package org.anair.stanchion.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanException;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.anair.stanchion.model.CacheInfo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.hazelcast.core.HazelcastInstance;

@RestController
@RequestMapping(value = "/manage/cache",produces=MediaType.APPLICATION_JSON_VALUE)
public class HazelcastCacheController extends AbstractJmxAwareController {
	private static final Logger LOG = LoggerFactory.getLogger(HazelcastCacheController.class);
	
	private static final String CACHE_DOMAIN_NAME = "com.hazelcast";
	private static final String CACHE_SIZE_ATTRIBUTE = "size";
	private static final String CACHE_CONFIG_ATTRIBUTE = "config";
	private static final String CACHE_HITS_ATTRIBUTE = "localHits";
	private static final String CLEAR_CACHE_OPERATION = "clear";

	private HazelcastInstance hzInstance;

	
	@RequestMapping(value = "list")
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody List<CacheInfo> getCacheList() throws Exception{
		final List<CacheInfo> rv = new ArrayList<CacheInfo>();
		
		ObjectName objName = getCacheMbeanObjectName("IMap", "*");
		Set<ObjectName> mbeanCacheSet = clientJmxConnection.queryNames(objName, null);
		for(ObjectName mBeanObjName:mbeanCacheSet){
			Integer count = (Integer)clientJmxConnection.getAttribute(mBeanObjName, CACHE_SIZE_ATTRIBUTE);
			String config = (String)clientJmxConnection.getAttribute(mBeanObjName, CACHE_CONFIG_ATTRIBUTE);
			Long localHits = (Long)clientJmxConnection.getAttribute(mBeanObjName, CACHE_HITS_ATTRIBUTE);
			final CacheInfo cacheInfo = new CacheInfo(mBeanObjName.getKeyProperty("name"), count,config, localHits);
			rv.add(cacheInfo);
			LOG.debug("Retrieved cache for {}", cacheInfo);
		}

		return rv;
	}

	@RequestMapping(value = "/clear/{cacheName}")
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody void clearCache(@PathVariable(value = "cacheName") String cacheName) throws Exception{
		if (StringUtils.isBlank(cacheName)) {
			throw new RuntimeException("key cannot be null");
		}
		
		ObjectName objName = getCacheMbeanObjectName("IMap", cacheName.trim());
		bustCacheMbeanFunction(objName);
	}


	@RequestMapping("/clearall")
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody void clearAllCache() throws Exception{
		ObjectName objName = getCacheMbeanObjectName("IMap", "*");
		Set<ObjectName> mbeanCacheSet = clientJmxConnection.queryNames(objName, null);
		for(ObjectName mBeanObjName:mbeanCacheSet){
			bustCacheMbeanFunction(mBeanObjName);
		}
	}

	private void bustCacheMbeanFunction(ObjectName mBeanObjName)
			throws InstanceNotFoundException, IntrospectionException,
			ReflectionException, IOException, MBeanException {
		clientJmxConnection.invoke(mBeanObjName, CLEAR_CACHE_OPERATION, null, null);
		LOG.debug("Busted {} cache.", mBeanObjName.getCanonicalName());
	}

	private ObjectName getCacheMbeanObjectName(String type, String key)
			throws MalformedObjectNameException {
		Hashtable<String,String> jmxObjectNamePropertyMap = new Hashtable<String, String>();
		jmxObjectNamePropertyMap.put("instance", hzInstance.getName());
		jmxObjectNamePropertyMap.put("type", type);
		jmxObjectNamePropertyMap.put("name", key.trim());
		ObjectName objName = new ObjectName(CACHE_DOMAIN_NAME,jmxObjectNamePropertyMap);
		return objName;
	}

	@Override
	protected void findBeans(){
		this.hzInstance = BeanFactoryUtils.beanOfTypeIncludingAncestors(applicationContext, HazelcastInstance.class);
	}

}