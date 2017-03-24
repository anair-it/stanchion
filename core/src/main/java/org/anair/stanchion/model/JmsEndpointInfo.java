package org.anair.stanchion.model;

import java.io.Serializable;

public class JmsEndpointInfo implements Serializable {

	private static final long serialVersionUID = 3296644100966868965L;
	
	private String camelContext;
	private String name;
	private String cleanName;
	private String queueName;
	private String state;
	private int concurrentConsumers;
	private int maxConcurrentConsumers;
	private String routeName;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getConcurrentConsumers() {
		return concurrentConsumers;
	}
	public void setConcurrentConsumers(int concurrentConsumers) {
		this.concurrentConsumers = concurrentConsumers;
	}
	public int getMaxConcurrentConsumers() {
		return maxConcurrentConsumers;
	}
	public void setMaxConcurrentConsumers(int maxConcurrentConsumers) {
		this.maxConcurrentConsumers = maxConcurrentConsumers;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	@Override
	public String toString() {
		return "JmsEndpointInfo [name=" + name + ", state=" + state + ", concurrentConsumers=" + concurrentConsumers
				+ ", maxConcurrentConsumers=" + maxConcurrentConsumers + "]";
	}
	public String getRouteName() {
		return routeName;
	}
	public void setRouteName(String routeName) {
		this.routeName = routeName;
	}
	public String getCamelContext() {
		return camelContext;
	}
	public void setCamelContext(String camelContext) {
		this.camelContext = camelContext;
	}
	public String getQueueName() {
		return queueName;
	}
	public void setQueueName(String queueName) {
		this.queueName = queueName;
	}
	public String getCleanName() {
		return cleanName;
	}
	public void setCleanName(String cleanName) {
		this.cleanName = cleanName;
	}
}
