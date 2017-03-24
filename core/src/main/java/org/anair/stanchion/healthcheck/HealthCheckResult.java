package org.anair.stanchion.healthcheck;

import java.io.Serializable;

public class HealthCheckResult implements Serializable{
	
	private static final long serialVersionUID = -5641681494062739280L;
	private final boolean healthy;
    private final String message;

    private HealthCheckResult(boolean isHealthy, String message) {
        this.healthy = isHealthy;
        this.message = message;
    }

    private HealthCheckResult(boolean isHealthy) {
    	this(isHealthy, null);
    }
    
    public static HealthCheckResult healthy() {
        return healthy(null);
    }

    public static HealthCheckResult healthy(String message) {
        return new HealthCheckResult(true, message);
    }


    public static HealthCheckResult unhealthy() {
        return unhealthy(null);
    }
    
    public static HealthCheckResult unhealthy(String message) {
        return new HealthCheckResult(false, message);
    }
    
    public boolean isHealthy() {
        return healthy;
    }

    public String getMessage() {
        return message;
    }

}
