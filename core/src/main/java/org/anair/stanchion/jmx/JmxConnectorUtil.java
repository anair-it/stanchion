package org.anair.stanchion.jmx;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;


public class JmxConnectorUtil {
	private static final Logger LOG = LoggerFactory.getLogger(JmxConnectorUtil.class);

	public static MBeanServerConnection connect(int port, String[] jmxCredentials)  {
		try {
			return jmxConnect(getHostname(), port, jmxCredentials);
		} catch (IOException e) {
			try {
				return jmxConnect("localhost", port, jmxCredentials);
			} catch (IOException e1) {
				LOG.error(e.getMessage());
			}
		}
		return null;
	}
	
	private static MBeanServerConnection jmxConnect(String host, int port, String[] jmxCredentials) throws IOException {
		String serviceUrl = "service:jmx:rmi:///jndi/rmi://"+host+":"+port+"/jmxrmi";
		Map<String, Object> environment = new HashMap<String, Object>(); 
		Properties props = new Properties();
		
		props.put(JMXConnector.CREDENTIALS, jmxCredentials);
		CollectionUtils.mergePropertiesIntoMap(props, environment);
		JMXConnector jmxConnector = null;
		try {
			jmxConnector = JMXConnectorFactory.connect(new JMXServiceURL(serviceUrl), environment);
			return jmxConnector.getMBeanServerConnection();
		} catch (MalformedURLException e) {
			LOG.error(e.getMessage());
		}
		return null;
	}
	
	private static String getHostname(){
		String hostname = "localhost";
		
		try {
			hostname = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
		}
		
		return hostname;
	}
}
