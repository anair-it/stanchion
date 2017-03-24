package org.anair.stanchion.jmx;

import javax.management.MBeanInfo;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.management.StandardMBean;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JmxStanchionSupport<T> extends StandardMBean {

	private static final Logger LOG = LoggerFactory.getLogger(JmxStanchionSupport.class);
	private static final String JMX_MBEAN_TYPE_NAME = "stanchion:type={beanType},name={beanName}";
	private static final String JMX_MBEAN_TYPE = "stanchion:type={beanType}";
	
	private String beanName;
	private ObjectName objectName;
	private String beanType;
	
	protected JmxStanchionSupport(Class<T> mbeanInterface, String beanName, String beanType)
			throws NotCompliantMBeanException {
		super(mbeanInterface);
		this.beanName = beanName;
		this.beanType = beanType;
		this.objectName = createObjectName();
	}

	/* (non-Javadoc)
	 * @see javax.management.StandardMBean#getDescription(javax.management.MBeanInfo)
	 */
	protected String getDescription(MBeanInfo info) {
        return "Stanchion "+ beanType + " bean: " + beanName;
    }
	
	private ObjectName createObjectName() {
        ObjectName objectName = null;
        try {
        	if(StringUtils.isBlank(beanName)){
        		objectName = new ObjectName(StringUtils.replaceEach(JMX_MBEAN_TYPE, new String[]{"{beanType}"}, new String[]{beanType}));
        	}else{
        		objectName = new ObjectName(StringUtils.replaceEach(JMX_MBEAN_TYPE_NAME, new String[]{"{beanType}", "{beanName}"}, new String[]{beanType, beanName}));
        	}
			LOG.info(objectName.getCanonicalName() + " MBean defined.");
		} catch (Exception e) {
			LOG.error("Error creating ObjectName. ", e);
		}
        return objectName;
    }
	
	public ObjectName getObjectName() {
		return objectName;
	}
}
