package org.anair.stanchion.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.anair.stanchion.model.JobResponse;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.UnexpectedJobExecutionException;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobExecutionNotRunningException;
import org.springframework.batch.core.launch.JobInstanceAlreadyExistsException;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.launch.NoSuchJobExecutionException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * spring-batch ui controller
 *
 */
@RestController
@RequestMapping(value="/spring-batch",produces=MediaType.APPLICATION_JSON_VALUE)
public class SpringBatchJobController implements ApplicationContextAware{
	
	private JobExplorer jobExplorer;
	private JobOperator jobOperator;
	private ApplicationContext applicationContext;
	private Map<String, Job> jobs;
	
	
	@RequestMapping("list")
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody List<JobResponse> list() throws IOException {
		List<JobResponse> jobList = Lists.newArrayList();
		if(jobs != null && !jobs.isEmpty()){
			for(Map.Entry<String, Job> entry: jobs.entrySet()){
				JobResponse jobResponse = new JobResponse();
				jobResponse.setName(entry.getValue().getName());
				jobResponse.setOpen(false);
				jobList.add(jobResponse);
			}
		}
//Below lines is used to simulate multiple jobs in an app.		
//		for(String jobName: jobExplorer.getJobNames()){
//			JobResponse jobResponse = new JobResponse();
//			jobResponse.setName(jobName);
//			jobResponse.setOpen(false);
//			jobList.add(jobResponse);
//		}
		
		return jobList;
	}
	
	
	@RequestMapping("job/{jobName}/executions")
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody List<JobResponse> executionsByJobName(@PathVariable("jobName") String jobName) throws NoSuchJobException {
		List<JobInstance> jobInstances = jobExplorer.getJobInstances(jobName, 0, 5);
		List<JobResponse> jobExecutionsResponse = Lists.newArrayList();
		
		for (JobInstance jobInstance : jobInstances) {
			for (JobExecution jobExecution : jobExplorer.getJobExecutions(jobInstance)) {
				JobResponse jobResponse = new JobResponse();
				jobResponse.setExecutionId(jobExecution.getId());
				jobResponse.setStatus(jobExecution.getStatus().toString());
				jobResponse.setStartTime(jobExecution.getCreateTime());
				jobExecutionsResponse.add(jobResponse);
			}
		}
		
		return jobExecutionsResponse;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@RequestMapping("executions/{executionId}")
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody JobResponse executionByExecutionId(@PathVariable("executionId") Long executionId) throws NoSuchJobException {
		JobExecution jobExecution = jobExplorer.getJobExecution(executionId);

		JobResponse jobResponse = new JobResponse();
		jobResponse.setName(jobExecution.getJobInstance().getJobName());
		jobResponse.setExecutionId(jobExecution.getId());
		jobResponse.setStatus(jobExecution.getStatus().toString());
		jobResponse.setStartTime(jobExecution.getStartTime());
		jobResponse.setEndTime(jobExecution.getEndTime());
		jobResponse.setParameters(jobExecution.getJobParameters().toString());
		jobResponse.setExecutionTime(executionTimeHumanReadable(jobExecution.getEndTime().getTime() - jobExecution.getStartTime().getTime()));
		jobResponse.setOpen(false);
		if(! CollectionUtils.isEmpty(jobExecution.getFailureExceptions())){
			jobResponse.setStackTrace(jobExecution.getFailureExceptions().get(0).getMessage());	
		}
		
		List<JobResponse> stepExecutionsResponse = Lists.transform(new ArrayList(jobExecution.getStepExecutions())
				, new Function<StepExecution, JobResponse>() {

			@Override
			public JobResponse apply(StepExecution stepExecution) {
				JobResponse stepResponse = new JobResponse();
				stepResponse.setName(stepExecution.getStepName());
				stepResponse.setExecutionId(stepExecution.getId());
				stepResponse.setStatus(stepExecution.getStatus().toString());
				stepResponse.setStartTime(stepExecution.getStartTime());
				stepResponse.setEndTime(stepExecution.getEndTime());
				stepResponse.setExecutionTime(executionTimeHumanReadable(stepExecution.getEndTime().getTime() - stepExecution.getStartTime().getTime()));
				stepResponse.addCount("Read", stepExecution.getReadCount());
				stepResponse.addCount("Write", stepExecution.getWriteCount());
				stepResponse.addCount("Skip", stepExecution.getSkipCount());
				stepResponse.addCount("Read skip", stepExecution.getReadSkipCount());
				stepResponse.addCount("Write skip", stepExecution.getWriteSkipCount());
				stepResponse.addCount("Filter", stepExecution.getFilterCount());
				stepResponse.addCount("Process skip", stepExecution.getProcessSkipCount());
				stepResponse.addCount("Commit", stepExecution.getCommitCount());
				stepResponse.addCount("Rollback", stepExecution.getRollbackCount());
				return stepResponse;
			}
		});
		jobResponse.setSteps(stepExecutionsResponse);
		
		return jobResponse;
	}
	
	@RequestMapping("job/{jobName}/{parameters}/start")
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody String start(@PathVariable("jobName") final String jobName, @PathVariable("parameters") final String parameters) {
		Map<String,String> status = Maps.newHashMap();
		
		try {
			this.jobOperator.start(jobName, parameters);
		} catch (NoSuchJobException e) {
			status.put("info", e.getMessage());
			return e.getMessage();
		} catch (JobInstanceAlreadyExistsException e) {
			status.put("warn", e.getMessage());
			return e.getMessage();
		} catch (JobParametersInvalidException e) {
			status.put("warn", e.getMessage());
			return e.getMessage();
		} catch (UnexpectedJobExecutionException e) {
			status.put("danger", e.getMessage());
		}
		
		return status.get("danger");
	}
	
	
	@RequestMapping("executions/{executionId}/stop")
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody String stop(@PathVariable("executionId") Long executionId) {
		Map<String,String> status = Maps.newHashMap();
		
		try {
			this.jobOperator.stop(executionId);
		} catch (NoSuchJobExecutionException e) {
			status.put("info", e.getMessage());
		} catch (JobExecutionNotRunningException e) {
			status.put("info", e.getMessage());
		}
		
		return status.get("info");
	}
	
	@RequestMapping("executions/{executionId}/restart")
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody String restart(@PathVariable("executionId") Long executionId) {
		Map<String,String> status = Maps.newHashMap();
		
		try {
			this.jobOperator.restart(executionId);
		} catch (JobInstanceAlreadyCompleteException e) {
			status.put("info", e.getMessage());
			return e.getMessage();
		} catch (NoSuchJobExecutionException e) {
			status.put("info", e.getMessage());
			return e.getMessage();
		} catch (NoSuchJobException e) {
			status.put("info", e.getMessage());
			return e.getMessage();
		} catch (JobRestartException e) {
			status.put("danger", e.getMessage());
		} catch (JobParametersInvalidException e) {
			status.put("warn", e.getMessage());
			return e.getMessage();
		}
		
		return status.get("danger");
	}
	
	@RequestMapping("/executions/{executionId}/abandon")
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody String abandon(@PathVariable("executionId") Long executionId) {
		Map<String,String> status = Maps.newHashMap();

		try {
			jobOperator.abandon(executionId);
		} catch (NoSuchJobExecutionException e) {
			status.put("info", e.getMessage());
		} catch (JobExecutionAlreadyRunningException e) {
			status.put("info", e.getMessage());
		}
		
		return status.get("info");
	}
	
	private String executionTimeHumanReadable(long time){
		String diff = "";
    	long diffLong = TimeUnit.MILLISECONDS.toDays(time);
    	if(diffLong == 0){
    		diffLong = TimeUnit.MILLISECONDS.toHours(time);
	    	if(diffLong == 0){
	    		diffLong = TimeUnit.MILLISECONDS.toMinutes(time);
		    	if(diffLong == 0){
		    		diffLong = TimeUnit.MILLISECONDS.toSeconds(time);
			    	if(diffLong == 0){
			    		diff = time + " Millis";
			    	}else{
			    		diff = String.valueOf(diffLong) + " Sec";
			    	}
		    	}else{
		    		diff = String.valueOf(diffLong) + " Min";
		    	}
	    	}else{
	    		diff = String.valueOf(diffLong) + " Hrs";
	    	}
    	}else{
    		diff = String.valueOf(diffLong) + " Days";
    	}
		return diff;
	}
	
	public void setJobExplorer(JobExplorer jobExplorer) {
		this.jobExplorer = jobExplorer;
	}

	public void setJobOperator(JobOperator jobOperator) {
		this.jobOperator = jobOperator;
	}

	private void findJobOperatorBean(){
		setJobOperator(BeanFactoryUtils.beanOfTypeIncludingAncestors(applicationContext, JobOperator.class));
	}
	
	private void findJobExplorerBean(){
		setJobExplorer(BeanFactoryUtils.beanOfTypeIncludingAncestors(applicationContext, JobExplorer.class));
	}
	
	private void findJobBeans(){
		setJobs(BeanFactoryUtils.beansOfTypeIncludingAncestors(applicationContext, Job.class));
	}
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
		try{
			findJobOperatorBean();
			findJobExplorerBean();
			findJobBeans();
		} catch(NoSuchBeanDefinitionException e){}
	}


	public void setJobs(Map<String, Job> jobs) {
		this.jobs = jobs;
	}
	
}