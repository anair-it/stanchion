package org.anair.stanchion.controller;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.anair.stanchion.model.NameValue;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.ServletContextAware;

import com.google.common.collect.Lists;

/**
 * server info controller
 *
 */
@RestController
@RequestMapping(value="/server", produces = MediaType.APPLICATION_JSON_VALUE)
public class ServerInfoController implements ServletContextAware {

    protected static final String APPLICATION_NAME = "Application Name";
    protected static final String APPLICATION_VERSION = "Application Version";
    protected static final String UPTIME = "JVM Uptime";
    protected static final String SERVER_INFO = "Server Info";
    protected static final String LOCAL_NAME = "Local Name";
    protected static final String LOCAL_ADDR = "Local Addr";
    protected static final String LOCAL_PORT = "Local Port";
    protected static final String CONTEXT_PATH = "Context Path";

    private ServletContext ctx;

    @RequestMapping
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public List<NameValue> getSystemInfo(final HttpServletRequest request) {
        List<NameValue> nameValues = Lists.newArrayList();
        
        final ResourceBundle appProps = ResourceBundle.getBundle("application-version");
        nameValues.add(new NameValue(APPLICATION_NAME, appProps.getString("name")));
        nameValues.add(new NameValue(APPLICATION_VERSION, formatVersionText(appProps.getString("version"))));
        nameValues.add(new NameValue(UPTIME, calculateJvmUptimeHumanReadable()));
        nameValues.add(new NameValue(SERVER_INFO, this.ctx.getServerInfo()));
        nameValues.add(new NameValue(LOCAL_NAME, request.getLocalName()));
        nameValues.add(new NameValue(LOCAL_ADDR, request.getLocalAddr()));
        nameValues.add(new NameValue(LOCAL_PORT, Integer.toString(request.getLocalPort())));
        nameValues.add(new NameValue(CONTEXT_PATH, request.getContextPath()));

        return nameValues;
    }

    private String formatVersionText(String versionText) {
		if (StringUtils.isNotBlank(versionText)) {
			return versionText.replace("-${BUILD_NUMBER}", "");
		}
		return versionText;
	}

	private String calculateJvmUptimeHumanReadable() {
		RuntimeMXBean rb = ManagementFactory.getRuntimeMXBean();
    	String uptime = "";
    	long uptimeLong = TimeUnit.MILLISECONDS.toDays(rb.getUptime());
    	if(uptimeLong == 0L){
    		uptimeLong = TimeUnit.MILLISECONDS.toHours(rb.getUptime());
	    	if(uptimeLong == 0L){
	    		uptimeLong = TimeUnit.MILLISECONDS.toMinutes(rb.getUptime());
		    	if(uptimeLong == 0L){
		    		uptimeLong = TimeUnit.MILLISECONDS.toSeconds(rb.getUptime());
			    	if(uptimeLong == 0L){
			    		uptime = rb.getUptime() + " Millis";
			    	}else{
			    		uptime = String.valueOf(uptimeLong) + " Sec";
			    	}
		    	}else{
		    		uptime = String.valueOf(uptimeLong) + " Min";
		    	}
	    	}else{
	    		uptime = String.valueOf(uptimeLong) + " Hrs";
	    	}
    	}else{
    		uptime = String.valueOf(uptimeLong) + " Days";
    	}
		return uptime;
	}

	@Override
	public void setServletContext(ServletContext servletContext) {
		this.ctx = servletContext;
	}
}