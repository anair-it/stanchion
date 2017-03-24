package org.anair.stanchion.healthcheck;

public interface HealthCheckJmxAware {

	public HealthCheckResult check();
	
	public boolean isHealthy();
}
