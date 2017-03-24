package org.anair.stanchion.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.anair.stanchion.model.JmsEndpointInfo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/manage/jms/spring",produces=MediaType.APPLICATION_JSON_VALUE)
public class SpringJmsEndpointController implements ApplicationContextAware{

	private static final Logger LOG = LoggerFactory.getLogger(SpringJmsEndpointController.class);
	private ApplicationContext applicationContext;
	private Map<String,DefaultMessageListenerContainer> messageListenerContainers;
	
	/**
	 * List Queues configured with Spring JMS Listeners.
	 * 
	 * @return JMSConsumers
	 * @throws Exception
	 */
	@RequestMapping(value = "list")
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody List<JmsEndpointInfo> getList() throws Exception{
		LOG.trace("Entering getList()");
		final List<JmsEndpointInfo> rv = new ArrayList<JmsEndpointInfo>();
		
		for(Map.Entry<String, DefaultMessageListenerContainer> entry: messageListenerContainers.entrySet()){	
			DefaultMessageListenerContainer messageListenerContainer = entry.getValue();
			JmsEndpointInfo info = new JmsEndpointInfo();
			info.setCleanName(entry.getKey());
			info.setName(entry.getKey());
			info.setState(messageListenerContainer.isRunning()?"Started":"Stopped");
			info.setMaxConcurrentConsumers(messageListenerContainer.getMaxConcurrentConsumers());
			info.setConcurrentConsumers(messageListenerContainer.getConcurrentConsumers());
			info.setQueueName(resolveJMSQueueName(messageListenerContainer.getDestination().toString()));
			rv.add(info);
		}

		return rv;
	}
	
	@RequestMapping(value = "/{listenerBeanName}/concurrent/{count}")
	@ResponseStatus(HttpStatus.OK)
	public void modifyConcurrentConsumers(@PathVariable(value = "listenerBeanName") String listenerBeanName, @PathVariable(value = "count") int count) throws Exception{
		DefaultMessageListenerContainer messageListenerContainer = messageListenerContainers.get(listenerBeanName);
		messageListenerContainer.setConcurrentConsumers(count);
	}
	
	@RequestMapping(value = "/{listenerBeanName}/maxconcurrent/{count}")
	@ResponseStatus(HttpStatus.OK)
	public void modifyMaxConcurrentConsumers(@PathVariable(value = "listenerBeanName") String listenerBeanName, @PathVariable(value = "count") int count) throws Exception{
		DefaultMessageListenerContainer messageListenerContainer = messageListenerContainers.get(listenerBeanName);
		messageListenerContainer.setMaxConcurrentConsumers(count);
	}
	
	@RequestMapping(value = "/{listenerBeanName}/start")
	@ResponseStatus(HttpStatus.OK)
	public void start(@PathVariable(value = "listenerBeanName") String listenerBeanName) throws Exception {
		DefaultMessageListenerContainer messageListenerContainer = messageListenerContainers.get(listenerBeanName);
		messageListenerContainer.start();
	}
	
	@RequestMapping(value = "/{listenerBeanName}/stop")
	@ResponseStatus(HttpStatus.OK)
	public void stop(@PathVariable(value = "listenerBeanName") String listenerBeanName) throws Exception {
		DefaultMessageListenerContainer messageListenerContainer = messageListenerContainers.get(listenerBeanName);
		messageListenerContainer.stop();
	}
	
	@RequestMapping(value = "/startall")
	@ResponseStatus(HttpStatus.OK)
	public void startAll() throws Exception {
		for(Map.Entry<String, DefaultMessageListenerContainer> entry: messageListenerContainers.entrySet()){	
			DefaultMessageListenerContainer messageListenerContainer = entry.getValue();
			messageListenerContainer.start();
		}
	}
	
	@RequestMapping(value = "/stopall")
	@ResponseStatus(HttpStatus.OK)
	public void stopAll() throws Exception {
		for(Map.Entry<String, DefaultMessageListenerContainer> entry: messageListenerContainers.entrySet()){	
			DefaultMessageListenerContainer messageListenerContainer = entry.getValue();
			messageListenerContainer.stop();
		}
	}
	
	private void findMessageListenerBeans(){
		setMessageListenerContainers(BeanFactoryUtils.beansOfTypeIncludingAncestors(applicationContext, DefaultMessageListenerContainer.class));
	}
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
		try{
			findMessageListenerBeans();
		} catch(NoSuchBeanDefinitionException e){}
		
	}

	public void setMessageListenerContainers(Map<String, DefaultMessageListenerContainer> messageListenerContainers) {
		this.messageListenerContainers = messageListenerContainers;
	}
	
	private String resolveJMSQueueName(String jmsQueueName){
		return StringUtils.substring(jmsQueueName, StringUtils.indexOf(jmsQueueName, ":///")+4);
	}
	
}