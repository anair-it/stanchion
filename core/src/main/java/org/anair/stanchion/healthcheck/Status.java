package org.anair.stanchion.healthcheck;

public enum Status {
	GOOD,BAD;
	
	public static Status resolveBooleanStatus(boolean status){
		return status?GOOD:BAD;
	}
}
