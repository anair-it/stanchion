package org.anair.stanchion.healthcheck;


import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Session;

import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.connection.SingleConnectionFactory;

public class MQQCFHealthCheck implements HealthCheckJmxAware {
	
	private ConnectionFactory connectionFactory;
		
	public MQQCFHealthCheck(ConnectionFactory connectionFactory) {
		this.connectionFactory = connectionFactory;
	}

	@Override
	public HealthCheckResult check() {
		Connection connection = null;
		Session session = null;

		if(connectionFactory instanceof CachingConnectionFactory || connectionFactory instanceof SingleConnectionFactory){
			this.connectionFactory = ((SingleConnectionFactory)connectionFactory).getTargetConnectionFactory();
		}
		
		try {
			connection = this.connectionFactory.createConnection(" ", " ");
			session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
			if (session.createTextMessage() != null) {
				return HealthCheckResult.healthy();
			}
			return HealthCheckResult.healthy();
		} catch (Exception e) {
			return HealthCheckResult.unhealthy(e.getMessage());
		} finally {
			if (session != null) {
				try {
					session.close();
				} catch (Throwable e) {
					return HealthCheckResult.unhealthy(e.getMessage());
				}
			}
			if (connection != null) {
				try {
					connection.close();
				} catch (Throwable e) {
					return HealthCheckResult.unhealthy(e.getMessage());
				}
			}
		}
	}
	
	@Override
	public boolean isHealthy() {
		return check().isHealthy();
	}
}
