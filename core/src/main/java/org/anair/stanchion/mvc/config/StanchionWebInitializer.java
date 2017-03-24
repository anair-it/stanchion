package org.anair.stanchion.mvc.config;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

import org.apache.commons.lang3.StringUtils;
import org.jolokia.http.AgentServlet;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

public class StanchionWebInitializer implements WebApplicationInitializer {

	private static final String STANCHION_SERVLET_NAME = "stanchion";
	private static final String STANCHION_SERVLET_URL = "/stanchion/secure/*";
	private static final String JOLOKIA_SERVLET_NAME = "jolokia-agent";
	private static final String JOLOKIA_SERVLET_URL = "/stanchion/hawtio/jolokia/*";
	
	@Override
	public void onStartup(ServletContext servletContext) throws ServletException {
		WebApplicationContext context = getContext();
		
        ServletRegistration.Dynamic stanchionDispatcher = servletContext.addServlet(STANCHION_SERVLET_NAME, new DispatcherServlet(context));
        stanchionDispatcher.setLoadOnStartup(1);
        stanchionDispatcher.addMapping(STANCHION_SERVLET_URL);
        
        String jolokiaPolicyType = StringUtils.defaultString(System.getProperty("jolokia.policy.type"), "rw");
        
        ServletRegistration.Dynamic jolokia = servletContext.addServlet(JOLOKIA_SERVLET_NAME, new AgentServlet());
        jolokia.setInitParameter("policyLocation", "classpath:/jolokia-"+jolokiaPolicyType+"-access.xml");
        jolokia.setLoadOnStartup(1);
        jolokia.addMapping(JOLOKIA_SERVLET_URL);
        
	}
	
	private AnnotationConfigWebApplicationContext getContext() {
        AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
        context.setConfigLocation("org.anair.stanchion.mvc.config");
        return context;
    }
}
