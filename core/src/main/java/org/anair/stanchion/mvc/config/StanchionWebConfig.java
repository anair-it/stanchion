package org.anair.stanchion.mvc.config;

import org.anair.stanchion.service.ConfigurationServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@EnableWebMvc
@ComponentScan({ "org.anair.stanchion.controller" })
public class StanchionWebConfig {

	@Value("#{systemProperties['catalina.base']}")
	private String catalinaBase;
	
	@Bean
	public ConfigurationServiceImpl getConfigurationService() {
		return new ConfigurationServiceImpl();
	}
	
	@Bean
	public FileSystemResource getJmxremoteResource(){
		return new FileSystemResource(catalinaBase+"/conf/jmxremote.password");
	}
	
}
