package org.anair.stanchion.model;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JobResponse implements Serializable {

	private static final long serialVersionUID = -4545969201001309283L;
	
	private String name;
	private String parameters;
	private long executionId;
	private String status;
	private Date startTime;
	private Date endTime;
	private String executionTime;
	private List<JobResponse> steps;
	private Map<String, Integer> count;
	private String stackTrace;
	private boolean open;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getParameters() {
		return parameters;
	}
	public void setParameters(String parameters) {
		this.parameters = parameters;
	}
	public Long getExecutionId() {
		return executionId;
	}
	public void setExecutionId(Long executionId) {
		this.executionId = executionId;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public Date getStartTime() {
		return startTime;
	}
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}
	public Date getEndTime() {
		return endTime;
	}
	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}
	public String getExecutionTime() {
		return executionTime;
	}
	public void setExecutionTime(String executionTime) {
		this.executionTime = executionTime;
	}
	public List<JobResponse> getSteps() {
		return steps;
	}
	public void setSteps(List<JobResponse> steps) {
		this.steps = steps;
	}
	public void setExecutionId(long executionId) {
		this.executionId = executionId;
	}
	public Map<String, Integer> getCount() {
		return count;
	}
	public void setCount(Map<String, Integer> count) {
		this.count = count;
	}
	public void addCount(String name, int count){
		if(this.count == null){
			this.count = new HashMap<String, Integer>();
		}
		this.count.put(name, count);
	}
	public String getStackTrace() {
		return stackTrace;
	}
	public void setStackTrace(String stackTrace) {
		this.stackTrace = stackTrace;
	}
	public boolean isOpen() {
		return open;
	}
	public void setOpen(boolean open) {
		this.open = open;
	}
}
