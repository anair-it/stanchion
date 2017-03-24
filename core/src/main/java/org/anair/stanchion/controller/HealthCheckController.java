package org.anair.stanchion.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.PostConstruct;
import javax.jms.ConnectionFactory;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.NotCompliantMBeanException;
import javax.sql.DataSource;

import org.anair.stanchion.healthcheck.HealthCheckJmxAware;
import org.anair.stanchion.healthcheck.HealthCheckResult;
import org.anair.stanchion.healthcheck.JdbcHealthCheck;
import org.anair.stanchion.healthcheck.MQQCFHealthCheck;
import org.anair.stanchion.healthcheck.Status;
import org.anair.stanchion.healthcheck.ZookeeperHealthCheck;
import org.anair.stanchion.jmx.JmxStanchionSupport;
import org.anair.stanchion.model.NameValue;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jmx.MBeanServerNotFoundException;
import org.springframework.jmx.support.JmxUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.Lists;

/**
 * health check controller
 * <p>
 * Check health connection to external resources like Database, MQ provider,
 * Zookeeper and register mbeans in JMX
 *
 */
@RestController
@RequestMapping(value = "/healthcheck", produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_PLAIN_VALUE })
public class HealthCheckController {
	private static final Logger LOG = LoggerFactory.getLogger(HealthCheckController.class);
	private ConcurrentMap<String, HealthCheckJmxAware> healthCheckMap;
	
	@Autowired
	private ApplicationContext applicationContext;

	private MBeanServer mBeanServer;

	@PostConstruct
	public void init() {
		this.healthCheckMap = new ConcurrentHashMap<String, HealthCheckJmxAware>();
		locateMBeanServer();

		jdbcHealthCheck();
		MQHealthCheck();
		zookeeperHealthCheck();
	}

	@RequestMapping
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody List<NameValue> healthCheck() throws IOException {
		init();

		List<NameValue> nameValues = Lists.newArrayList();
		healthCheckMap.forEach((registerName, healthCheck) -> {
			HealthCheckResult result = healthCheck.check();
			NameValue nameValue = new NameValue(registerName, Status.resolveBooleanStatus(result.isHealthy()).name());
			if (StringUtils.isNotBlank(result.getMessage())) {
				nameValue.setMessage(result.getMessage());
			}
			nameValues.add(nameValue);
		});

		return nameValues;
	}

	private void locateMBeanServer() {
		if (mBeanServer == null) {
			try {
				mBeanServer = JmxUtils.locateMBeanServer();
			} catch (MBeanServerNotFoundException e) {
				LOG.error("Could not find an MBeanServer. Please create an MBeanServer instance.");
			}
		}
	}

	private void jdbcHealthCheck() {
		Map<String, DataSource> dataSourceBeans = getDataSourceBeans();
		if (dataSourceBeans != null && !dataSourceBeans.isEmpty()) {
			dataSourceBeans.forEach((dataSourceBeanName, dataSourceBean) -> {
				JdbcHealthCheck jdbcHealthCheck = new JdbcHealthCheck(dataSourceBean);
				healthCheckMap.putIfAbsent(dataSourceBeanName, jdbcHealthCheck);
				try {
					registrHealthCheckMBean(dataSourceBeanName, jdbcHealthCheck);
				} catch (Exception e) {
					LOG.error(e.getMessage(), e);;
				}
			});
		}
	}

	private void zookeeperHealthCheck() {
		CuratorFramework  curatorFramework = findCuratorFrameworkBean();
		if(curatorFramework != null){
			ZookeeperHealthCheck zookeeperHealthCheck = new ZookeeperHealthCheck(curatorFramework);
			healthCheckMap.putIfAbsent("zookeeper", zookeeperHealthCheck);
			try {
				registrHealthCheckMBean("zookeeper", zookeeperHealthCheck);
			} catch (Exception e) {
				LOG.error(e.getMessage(), e);;
			}
		}
	}

	private void MQHealthCheck() {
		Map<String, ConnectionFactory> mqConnectionFactories = getConnectionFactoryBeans();
		if (mqConnectionFactories != null && !mqConnectionFactories.isEmpty()) {
			mqConnectionFactories.forEach((connectionFactoryName, connectionFactory) -> {
				MQQCFHealthCheck mqqcfHealthCheck = new MQQCFHealthCheck(connectionFactory);
				healthCheckMap.putIfAbsent(connectionFactoryName, mqqcfHealthCheck);
				try {
					registrHealthCheckMBean(connectionFactoryName, mqqcfHealthCheck);
				} catch (Exception e) {
					LOG.error(e.getMessage(), e);;
				}

			});
		}
	}
	
	private CuratorFramework findCuratorFrameworkBean() {
		try {
			return BeanFactoryUtils.beanOfTypeIncludingAncestors(applicationContext, CuratorFramework.class);
		}catch(NoSuchBeanDefinitionException e){}
		return null;
	}
	
	private Map<String, ConnectionFactory> getConnectionFactoryBeans() {
		try {
			return BeanFactoryUtils.beansOfTypeIncludingAncestors(applicationContext, ConnectionFactory.class);
		} catch (NoSuchBeanDefinitionException e) {
		}
		return null;
	}

	private Map<String, DataSource> getDataSourceBeans() {
		try {
			return BeanFactoryUtils.beansOfTypeIncludingAncestors(applicationContext, DataSource.class);
		} catch (NoSuchBeanDefinitionException e) {
		}
		return null;
	}

	private interface JmxHealthCheckMBean extends HealthCheckJmxAware {
		public boolean isHealthy();
	}

	private void registrHealthCheckMBean(String jmxBeanName, HealthCheckJmxAware jmxBean)
			throws NotCompliantMBeanException, InstanceAlreadyExistsException, MBeanRegistrationException, InstanceNotFoundException {
		
		JmxHealthCheck jmxHealthCheck = new JmxHealthCheck(jmxBean, jmxBeanName);
		if(mBeanServer.isRegistered(jmxHealthCheck.getObjectName())){
			mBeanServer.unregisterMBean(jmxHealthCheck.getObjectName());
		}
		mBeanServer.registerMBean(jmxHealthCheck, jmxHealthCheck.getObjectName());
	}

	private static class JmxHealthCheck extends JmxStanchionSupport<HealthCheckJmxAware> implements JmxHealthCheckMBean  {
		private final HealthCheckJmxAware healthCheckAware;

		private JmxHealthCheck(HealthCheckJmxAware healthCheckAware, String beanName) throws NotCompliantMBeanException {
			super(HealthCheckJmxAware.class, beanName, "health-check");
			this.healthCheckAware = healthCheckAware;
		}

		@Override
		public boolean isHealthy() {
			return healthCheckAware.isHealthy();
		}

		@Override
		public HealthCheckResult check() {
			return null;
		}
	}

}