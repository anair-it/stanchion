package org.anair.stanchion.controller;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import javax.management.MBeanServerConnection;

import org.anair.stanchion.jmx.JmxConnectorUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.jndi.JndiLocatorDelegate;

public abstract class AbstractJmxAwareController implements ApplicationContextAware {

	protected ApplicationContext applicationContext;
	protected MBeanServerConnection clientJmxConnection;
	private Resource jmxremoteResource;
	
	@Value("#{systemProperties['base.jmx.port'] ?: 6969}")
	private int jmxPort;
	protected JndiLocatorDelegate jndiLocator = JndiLocatorDelegate.createDefaultResourceRefLocator();
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
		try{
			findBeans();
			String matchingadminUseridPassword = fetchJmxRemoteResourceContent(applicationContext);
			
			this.clientJmxConnection = JmxConnectorUtil.connect(jmxPort, StringUtils.split(matchingadminUseridPassword, " "));
		} catch(NoSuchBeanDefinitionException | IOException e){}
	}

	private String fetchJmxRemoteResourceContent(ApplicationContext applicationContext) throws IOException {
		jmxremoteResource = BeanFactoryUtils.beanOfType(applicationContext, FileSystemResource.class);
		File jmxRemotePasswordFile = jmxremoteResource.getFile();
		List<String> contents = FileUtils.readLines(jmxRemotePasswordFile, Charset.defaultCharset());
		
		return contents.stream()
			.filter(content-> content.startsWith("admin"))
			.findAny()
			.orElse("admin springsource");
	}
	
	protected abstract void findBeans();

}