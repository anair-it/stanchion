package org.anair.stanchion.controller;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jms.Queue;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.anair.stanchion.model.JmsEndpointInfo;
import org.apache.camel.CamelContext;
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

@RestController
@RequestMapping(value = "/manage/jms/camel",produces=MediaType.APPLICATION_JSON_VALUE)
public class CamelJmsEndpointController extends AbstractJmxAwareController {

	private static final Logger LOG = LoggerFactory.getLogger(CamelJmsEndpointController.class);
	private static final String DOMAIN_NAME = "org.apache.camel";

	private Map<String,CamelContext> camelContextMap;
	
	/**
	 * For every <code>CamelContext</code> in the app, get all JMS consuming queues. Outbound queues are not listed.
	 * <p>
	 * <ul>
	 * <li>Fetch all Camel Contexts</li>
	 * <li>Fetch all Camel JMS Consumers for a Camel Context</li>
	 * <li>Find matching Camel endpoint used by the JMS Consumer</li>
	 * <li>Gather data from <code>JMSConsumer</code> and the endpoint and return back data to UI</li>
	 * </ul>
	 * 
	 * @return JMSConsumers
	 * @throws Exception
	 */
	@RequestMapping(value = "list")
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody List<JmsEndpointInfo> getList() throws Exception{
		LOG.trace("Entering getList()");
		final List<JmsEndpointInfo> rv = new ArrayList<JmsEndpointInfo>();
		
		for(Map.Entry<String, CamelContext> entry: camelContextMap.entrySet()){	
			ObjectName jmsConsumerObjName = getMbeanObjectName(entry.getKey(), "consumers", "*JmsConsumer*");
			Set<ObjectName> jmsConsumerMbeanSet = clientJmxConnection.queryNames(jmsConsumerObjName, null);
			for(ObjectName jmsConsumerMbean:jmsConsumerMbeanSet){
				String jmsConsumerEndpointUri = (String)clientJmxConnection.getAttribute(jmsConsumerMbean, "EndpointUri");
				String routeId = (String)clientJmxConnection.getAttribute(jmsConsumerMbean, "RouteId");
				String state = (String)clientJmxConnection.getAttribute(jmsConsumerMbean, "State");
				
				JmsEndpointInfo info = new JmsEndpointInfo();
				info.setCamelContext(entry.getKey());
				info.setRouteName(routeId);
				info.setState(state);
				resolveEndpointNameFromJmxObjectName(jmsConsumerEndpointUri, info);
				
				ObjectName objName = getMbeanObjectName(entry.getKey(), "endpoints", "*"+info.getCleanName()+"*");
				Set<ObjectName> mbeanSet = clientJmxConnection.queryNames(objName, null);
				
				for(ObjectName mBeanObjName:mbeanSet){
					String endpointUri = (String)clientJmxConnection.getAttribute(mBeanObjName, "EndpointUri");
					
					if(jmsConsumerEndpointUri.equals(endpointUri)){
						Integer concurrentConsumers = (Integer)clientJmxConnection.getAttribute(mBeanObjName, "ConcurrentConsumers");
						Integer maxConcurrentConsumers = (Integer)clientJmxConnection.getAttribute(mBeanObjName, "MaxConcurrentConsumers");
						
						info.setConcurrentConsumers(concurrentConsumers);
						info.setMaxConcurrentConsumers(maxConcurrentConsumers);
						
						Queue queue = jndiLocator.lookup(info.getName(), Queue.class);
						info.setQueueName(resolveJMSQueueName(queue.getQueueName()));
						
						rv.add(info);
						LOG.debug("Retrieved jms endpoint for {}", info);
						
						break;
					}
				}
			}
		}

		return rv;
	}
	
	@RequestMapping(value = "/{camelContext}/{endpointName}/concurrent/{count}")
	@ResponseStatus(HttpStatus.OK)
	public void modifyConcurrentConsumers(@PathVariable(value = "camelContext") String camelContext, @PathVariable(value = "endpointName") String endpointName, @PathVariable(value = "count") int count) throws Exception{
		modifyConsumers(camelContext, endpointName, count, "setConcurrentConsumers");
	}
	
	@RequestMapping(value = "/{camelContext}/{endpointName}/maxconcurrent/{count}")
	@ResponseStatus(HttpStatus.OK)
	public void modifyMaxConcurrentConsumers(@PathVariable(value = "camelContext") String camelContext, @PathVariable(value = "endpointName") String endpointName, @PathVariable(value = "count") int count) throws Exception{
		modifyConsumers(camelContext, endpointName, count, "setMaxConcurrentConsumers");
	}
	
	@RequestMapping(value = "/{camelContext}/{endpointName}/start")
	@ResponseStatus(HttpStatus.OK)
	public void start(@PathVariable(value = "camelContext") String camelContext, @PathVariable(value = "endpointName") String endpointName) throws Exception {
		stateChangeOperation(camelContext, endpointName, "start");
	}
	
	@RequestMapping(value = "/{camelContext}/{endpointName}/stop")
	@ResponseStatus(HttpStatus.OK)
	public void stop(@PathVariable(value = "camelContext") String camelContext, @PathVariable(value = "endpointName") String endpointName) throws Exception {
		stateChangeOperation(camelContext, endpointName, "stop");
	}
	
	@RequestMapping(value = "/startall")
	@ResponseStatus(HttpStatus.OK)
	public void startAll() throws Exception {
		stateChangeOperation("start");
	}
	
	@RequestMapping(value = "/stopall")
	@ResponseStatus(HttpStatus.OK)
	public void stopAll() throws Exception {
		stateChangeOperation("stop");
	}
	
	private void stateChangeOperation(String camelContext, String endpointName, String operation) throws Exception {
		ObjectName jmsConsumerObjName = getMbeanObjectName(camelContext, "consumers", "*JmsConsumer*");
		Set<ObjectName> jmsConsumerMbeanSet = clientJmxConnection.queryNames(jmsConsumerObjName, null);
		for(ObjectName jmsConsumerMbean:jmsConsumerMbeanSet){
			String jmsConsumerEndpointUri = (String)clientJmxConnection.getAttribute(jmsConsumerMbean, "EndpointUri");
			
			if(jmsConsumerEndpointUri.contains(endpointName)){
				clientJmxConnection.invoke(jmsConsumerMbeanSet.iterator().next(), operation, null, null);
				LOG.debug("Stopped JmsConsumer for endpoint {}.", endpointName);
				break;
			}
		}
	}
	
	private void stateChangeOperation(String operation) throws Exception {
		for(Map.Entry<String, CamelContext> entry: camelContextMap.entrySet()){	
			ObjectName jmsConsumerObjName = getMbeanObjectName(entry.getKey(), "consumers", "*JmsConsumer*");
			Set<ObjectName> jmsConsumerMbeanSet = clientJmxConnection.queryNames(jmsConsumerObjName, null);
			for(ObjectName jmsConsumerMbean:jmsConsumerMbeanSet){
				String jmsConsumerEndpointUri = (String)clientJmxConnection.getAttribute(jmsConsumerMbean, "EndpointUri");
				clientJmxConnection.invoke(jmsConsumerMbeanSet.iterator().next(), operation, null, null);
				LOG.debug("Stopped JmsConsumer for endpoint {}.", jmsConsumerEndpointUri);
			}
		}
	}
	
	private void modifyConsumers(String camelContext, String endpointName, int count, String operation) throws Exception{
		ObjectName objName = getMbeanObjectName(camelContext, "endpoints", "*"+endpointName+"*");
		Set<ObjectName> mbeanSet = clientJmxConnection.queryNames(objName, null);
		if(mbeanSet.size() == 1){
			clientJmxConnection.invoke(mbeanSet.iterator().next(), operation, new Object[]{count}, new String[]{"int"});
			LOG.debug("Modified {} for operation {} with consumer count of {}.", endpointName, operation);
		}else{
			LOG.error("Illegal. Found 2 mbeans for name: {}", objName.toString());
		}
	}

	private ObjectName getMbeanObjectName(String context, String type, String key)
			throws MalformedObjectNameException {
		Hashtable<String,String> jmxObjectNamePropertyMap = new Hashtable<String, String>();
		jmxObjectNamePropertyMap.put("context", context);
		jmxObjectNamePropertyMap.put("type", type);
		jmxObjectNamePropertyMap.put("name", key.trim());
		ObjectName objName = new ObjectName(DOMAIN_NAME,jmxObjectNamePropertyMap);
		return objName;
	}
	
	@Override
	protected void findBeans(){
		this.camelContextMap = BeanFactoryUtils.beansOfTypeIncludingAncestors(applicationContext, CamelContext.class);
	}
	
	private void resolveEndpointNameFromJmxObjectName(String objectName, JmsEndpointInfo info){
		String endpointName = null;

		int questionMarkIndex = StringUtils.indexOf(objectName, "?");
		if(questionMarkIndex == -1){
			endpointName = StringUtils.substring(objectName, StringUtils.indexOf(objectName, "://")+3);
		}else{
			endpointName = StringUtils.substring(objectName, StringUtils.indexOf(objectName, "://")+3, questionMarkIndex);	
		}
		info.setName(endpointName);
		
		int slashIndex = StringUtils.indexOf(endpointName, "/");
		if(slashIndex > 0){
			endpointName = StringUtils.substring(endpointName, slashIndex+1);
		}
		info.setCleanName(endpointName);
	}
	
	private String resolveJMSQueueName(String jmsQueueName){
		return StringUtils.substring(jmsQueueName, StringUtils.indexOf(jmsQueueName, ":///")+4);
	}
	
}