package org.anair.stanchion.controller;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.anair.stanchion.controller.SpringBatchJobController;
import org.anair.stanchion.model.JobResponse;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobExecutionNotRunningException;
import org.springframework.batch.core.launch.JobInstanceAlreadyExistsException;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.launch.NoSuchJobExecutionException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;

import static org.easymock.EasyMock.*;

public class SpringBatchJobControllerTest {
	
	private SpringBatchJobController controller;
	private JobExplorer mockJobExplorer;
	private JobOperator mockJobOperator;
	private Job mockJob1;
	private Job mockJob2;
	
	
	@Before
	public void setUp() throws Exception {
		mockJobExplorer = createMock(JobExplorer.class);
		mockJobOperator = createMock(JobOperator.class);
		mockJob1 = createMock(Job.class);
		mockJob2 = createMock(Job.class);
		
		controller = new SpringBatchJobController();
		controller.setJobExplorer(mockJobExplorer);
		controller.setJobOperator(mockJobOperator);
		
		Map<String, Job> jobs = new HashMap<String, Job>();
		expect(mockJob1.getName()).andStubReturn("j1");
		expect(mockJob2.getName()).andStubReturn("j2");
		jobs.put("job1", mockJob1);
		jobs.put("job2", mockJob2);
		controller.setJobs(jobs);
	}

	@Test
	public void list() throws IOException {
		replay(mockJob1, mockJob2);
		List<JobResponse> list = controller.list();
		verify(mockJob1, mockJob2);
		
		assertNotNull(list);
		assertEquals(2, list.size());
	}

	@Test
	public void testExecutionsByJobName() throws NoSuchJobException {
		String JOB_NAME = "j1";
		Date now = new Date();
		
		JobInstance jobInstance1 = new JobInstance(1L, JOB_NAME);
		JobInstance jobInstance2 = new JobInstance(2L, JOB_NAME);
		
		JobExecution jobExecution1 = new JobExecution(1L);
		jobExecution1.setCreateTime(now);
		jobExecution1.setStatus(BatchStatus.COMPLETED);
		
		JobExecution jobExecution2 = new JobExecution(2L);
		jobExecution2.setCreateTime(DateUtils.addDays(now, -1));
		jobExecution2.setStatus(BatchStatus.FAILED);
		
		JobExecution jobExecution3 = new JobExecution(3L);
		jobExecution3.setCreateTime(DateUtils.addDays(now, -2));
		jobExecution3.setStatus(BatchStatus.STARTED);
		
		expect(mockJobExplorer.getJobInstances(JOB_NAME, 0, 5)).andReturn(Arrays.asList(jobInstance1, jobInstance2));
		expect(mockJobExplorer.getJobExecutions(jobInstance1)).andReturn(Arrays.asList(jobExecution1));
		expect(mockJobExplorer.getJobExecutions(jobInstance2)).andReturn(Arrays.asList(jobExecution2, jobExecution3));
		
		replay(mockJobExplorer);
		
		List<JobResponse> executionsByJobName = controller.executionsByJobName(JOB_NAME);
		verify(mockJobExplorer);
		
		assertNotNull(executionsByJobName);
		assertEquals(3, executionsByJobName.size());
		
		JobResponse jobResponse = executionsByJobName.get(0);
		assertEquals(1, jobResponse.getExecutionId().longValue());
		assertEquals(BatchStatus.COMPLETED.name(), jobResponse.getStatus());
		assertEquals(now, jobResponse.getStartTime());
		
		jobResponse = executionsByJobName.get(1);
		assertEquals(2, jobResponse.getExecutionId().longValue());
		assertEquals(BatchStatus.FAILED.name(), jobResponse.getStatus());
		assertEquals(DateUtils.addDays(now, -1), jobResponse.getStartTime());
		
		jobResponse = executionsByJobName.get(2);
		assertEquals(3, jobResponse.getExecutionId().longValue());
		assertEquals(BatchStatus.STARTED.name(), jobResponse.getStatus());
		assertEquals(DateUtils.addDays(now, -2), jobResponse.getStartTime());
	}

	@Test
	public void testExecutionByExecutionId() throws NoSuchJobException {
		String JOB_NAME = "j1";
		Long EXECUTION_ID = 1L;
		Date now = new Date();
		
		JobInstance jobInstance = new JobInstance(1L, JOB_NAME);
		
		//Preparing 2 Job parameters
		JobParameter jobParam1 = new JobParameter("value1");
		JobParameter jobParam2 = new JobParameter(2L);
		Map<String, JobParameter> jobParamMap = new HashMap<String, JobParameter>();
		jobParamMap.put("var1", jobParam1);
		jobParamMap.put("var2", jobParam2);
		JobParameters jobParams = new JobParameters(jobParamMap);
		
		JobExecution jobExecution = new JobExecution(1L, jobParams);
		jobExecution.setJobInstance(jobInstance);
		jobExecution.setStartTime(now);
		jobExecution.setEndTime(DateUtils.addHours(now, 1));
		jobExecution.setStatus(BatchStatus.COMPLETED);
		
		
		//Preparing 2 job steps
		List<StepExecution> stepExecutions = new ArrayList<StepExecution>();
		
		StepExecution stepExe1 = new StepExecution("step1", jobExecution, 1L);
		stepExe1.setStartTime(now);
		stepExe1.setEndTime(DateUtils.addMinutes(now, 10));
		stepExe1.setStatus(BatchStatus.COMPLETED);
		stepExe1.setCommitCount(1);
		stepExe1.setFilterCount(0);
		stepExe1.setProcessSkipCount(0);
		stepExe1.setReadCount(5);
		stepExe1.setReadSkipCount(0);
		stepExe1.setRollbackCount(0);
		stepExe1.setWriteCount(1);
		stepExe1.setWriteSkipCount(0);
		stepExecutions.add(stepExe1);
		
		stepExe1 = new StepExecution("step2", jobExecution, 1L);
		stepExe1.setStartTime(now);
		stepExe1.setEndTime(DateUtils.addSeconds(now, 32));
		stepExe1.setStatus(BatchStatus.COMPLETED);
		stepExe1.setCommitCount(2);
		stepExe1.setFilterCount(2);
		stepExe1.setProcessSkipCount(0);
		stepExe1.setReadCount(10);
		stepExe1.setReadSkipCount(0);
		stepExe1.setRollbackCount(0);
		stepExe1.setWriteCount(10);
		stepExe1.setWriteSkipCount(0);
		stepExecutions.add(stepExe1);
		
		jobExecution.addStepExecutions(stepExecutions);
		
		expect(mockJobExplorer.getJobExecution(EXECUTION_ID)).andReturn(jobExecution);
		
		replay(mockJobExplorer);
		JobResponse jobResponse = controller.executionByExecutionId(EXECUTION_ID);
		verify(mockJobExplorer);
		assertEquals(2, jobResponse.getSteps().size());
		assertEquals("10 Min", jobResponse.getSteps().get(0).getExecutionTime());
		assertEquals("{Read=5, Write=1, Process skip=0, Commit=1, Read skip=0, Filter=0, Skip=0, Rollback=0, Write skip=0}", jobResponse.getSteps().get(0).getCount().toString());
		assertEquals("32 Sec", jobResponse.getSteps().get(1).getExecutionTime());
		assertEquals("{Read=10, Write=10, Process skip=0, Commit=2, Read skip=0, Filter=2, Skip=0, Rollback=0, Write skip=0}", jobResponse.getSteps().get(1).getCount().toString());
		
		assertEquals("{var2=2, var1=value1}", jobResponse.getParameters());
		assertEquals("1 Hrs", jobResponse.getExecutionTime());
		assertNull("No exception for a COMPLETED job", jobResponse.getStackTrace());
	}

	@Test
	public void start_success() throws NoSuchJobException, JobInstanceAlreadyExistsException, JobParametersInvalidException {
		String JOB_NAME = "j1";
		String RESPONSE_MSG = null;
		
		expect(mockJobOperator.start(JOB_NAME, "var1=value1,var2=2")).andStubReturn(1L);
		
		replay(mockJobOperator);
		String response = controller.start(JOB_NAME, "var1=value1,var2=2");
		verify(mockJobOperator);
		assertEquals(RESPONSE_MSG, response);
	}
	
	@Test
	public void start_NoSuchJobException() throws NoSuchJobException, JobInstanceAlreadyExistsException, JobParametersInvalidException {
		String JOB_NAME = "j1";
		String RESPONSE_MSG = "Job not found";
		
		expect(mockJobOperator.start(JOB_NAME, "var1=value1,var2=2")).andStubThrow(new NoSuchJobException(RESPONSE_MSG));
		
		replay(mockJobOperator);
		String response = controller.start(JOB_NAME, "var1=value1,var2=2");
		verify(mockJobOperator);
		assertEquals(RESPONSE_MSG, response);
	}
	
	@Test
	public void start_JobInstanceAlreadyExistsException() throws NoSuchJobException, JobInstanceAlreadyExistsException, JobParametersInvalidException {
		String JOB_NAME = "j1";
		String RESPONSE_MSG = "Job Instance Already Exists";
		
		expect(mockJobOperator.start(JOB_NAME, "var1=value1,var2=2")).andStubThrow(new JobInstanceAlreadyExistsException(RESPONSE_MSG));
		
		replay(mockJobOperator);
		String response = controller.start(JOB_NAME, "var1=value1,var2=2");
		verify(mockJobOperator);
		assertEquals(RESPONSE_MSG, response);
	}
	
	@Test
	public void start_JobParametersInvalidException() throws NoSuchJobException, JobInstanceAlreadyExistsException, JobParametersInvalidException {
		String JOB_NAME = "j1";
		String RESPONSE_MSG = "Job Parameters Invalid";
		
		expect(mockJobOperator.start(JOB_NAME, "var1=value1,var2=2")).andStubThrow(new JobParametersInvalidException(RESPONSE_MSG));
		
		replay(mockJobOperator);
		String response = controller.start(JOB_NAME, "var1=value1,var2=2");
		verify(mockJobOperator);
		assertEquals(RESPONSE_MSG, response);
	}
	
	@Test
	public void stop_success() throws NoSuchJobExecutionException, JobExecutionNotRunningException { 
		Long EXECUTION_ID = 1L;
		String RESPONSE_MSG = null;
		
		expect(mockJobOperator.stop(EXECUTION_ID)).andStubReturn(true);
		
		replay(mockJobOperator);
		String response = controller.stop(EXECUTION_ID);
		verify(mockJobOperator);
		assertEquals(RESPONSE_MSG, response);
	}
	
	@Test
	public void stop_NoSuchJobExecutionException() throws NoSuchJobExecutionException, JobExecutionNotRunningException { 
		Long EXECUTION_ID = 1L;
		String RESPONSE_MSG = "No Such Job Execution";
		
		expect(mockJobOperator.stop(EXECUTION_ID)).andStubThrow(new NoSuchJobExecutionException(RESPONSE_MSG));
		
		replay(mockJobOperator);
		String response = controller.stop(EXECUTION_ID);
		verify(mockJobOperator);
		assertEquals(RESPONSE_MSG, response);
	}
	
	@Test
	public void stop_JobExecutionNotRunningException() throws NoSuchJobExecutionException, JobExecutionNotRunningException { 
		Long EXECUTION_ID = 1L;
		String RESPONSE_MSG = "Job Execution Not Running";
		
		expect(mockJobOperator.stop(EXECUTION_ID)).andStubThrow(new JobExecutionNotRunningException(RESPONSE_MSG));
		
		replay(mockJobOperator);
		String response = controller.stop(EXECUTION_ID);
		verify(mockJobOperator);
		assertEquals(RESPONSE_MSG, response);
	}
	
	@Test
	public void restart_success() throws JobInstanceAlreadyCompleteException, NoSuchJobExecutionException, NoSuchJobException, JobRestartException, JobParametersInvalidException { 
		Long EXECUTION_ID = 1L;
		String RESPONSE_MSG = null;
		
		expect(mockJobOperator.restart(EXECUTION_ID)).andStubReturn(1L);
		
		replay(mockJobOperator);
		String response = controller.restart(EXECUTION_ID);
		verify(mockJobOperator);
		assertEquals(RESPONSE_MSG, response);
	}
	
	@Test
	public void restart_JobInstanceAlreadyCompleteException() throws JobInstanceAlreadyCompleteException, NoSuchJobExecutionException, NoSuchJobException, JobRestartException, JobParametersInvalidException { 
		Long EXECUTION_ID = 1L;
		String RESPONSE_MSG = "Job Instance Already Complete";
		
		expect(mockJobOperator.restart(EXECUTION_ID)).andStubThrow(new JobInstanceAlreadyCompleteException(RESPONSE_MSG));
		
		replay(mockJobOperator);
		String response = controller.restart(EXECUTION_ID);
		verify(mockJobOperator);
		assertEquals(RESPONSE_MSG, response);
	}
	
	@Test
	public void restart_NoSuchJobExecutionException() throws JobInstanceAlreadyCompleteException, NoSuchJobExecutionException, NoSuchJobException, JobRestartException, JobParametersInvalidException { 
		Long EXECUTION_ID = 1L;
		String RESPONSE_MSG = "No Such Job Execution";
		
		expect(mockJobOperator.restart(EXECUTION_ID)).andStubThrow(new NoSuchJobExecutionException(RESPONSE_MSG));
		
		replay(mockJobOperator);
		String response = controller.restart(EXECUTION_ID);
		verify(mockJobOperator);
		assertEquals(RESPONSE_MSG, response);
	}
	
	@Test
	public void restart_NoSuchJobException() throws JobInstanceAlreadyCompleteException, NoSuchJobExecutionException, NoSuchJobException, JobRestartException, JobParametersInvalidException { 
		Long EXECUTION_ID = 1L;
		String RESPONSE_MSG = "No Such Job";
		
		expect(mockJobOperator.restart(EXECUTION_ID)).andStubThrow(new NoSuchJobException(RESPONSE_MSG));
		
		replay(mockJobOperator);
		String response = controller.restart(EXECUTION_ID);
		verify(mockJobOperator);
		assertEquals(RESPONSE_MSG, response);
	}
	
	@Test
	public void restart_JobRestartException() throws JobInstanceAlreadyCompleteException, NoSuchJobExecutionException, NoSuchJobException, JobRestartException, JobParametersInvalidException { 
		Long EXECUTION_ID = 1L;
		String RESPONSE_MSG = "Job not Restarted";
		
		expect(mockJobOperator.restart(EXECUTION_ID)).andStubThrow(new JobRestartException(RESPONSE_MSG));
		
		replay(mockJobOperator);
		String response = controller.restart(EXECUTION_ID);
		verify(mockJobOperator);
		assertEquals(RESPONSE_MSG, response);
	}
	
	@Test
	public void restart_JobParametersInvalidException() throws JobInstanceAlreadyCompleteException, NoSuchJobExecutionException, NoSuchJobException, JobRestartException, JobParametersInvalidException { 
		Long EXECUTION_ID = 1L;
		String RESPONSE_MSG = "Job Parameters Invalid";
		
		expect(mockJobOperator.restart(EXECUTION_ID)).andStubThrow(new JobParametersInvalidException(RESPONSE_MSG));
		
		replay(mockJobOperator);
		String response = controller.restart(EXECUTION_ID);
		verify(mockJobOperator);
		assertEquals(RESPONSE_MSG, response);
	}

	@Test
	public void abandon_success() throws NoSuchJobExecutionException, JobExecutionAlreadyRunningException { 
		Long EXECUTION_ID = 1L;
		String RESPONSE_MSG = null;
		
		expect(mockJobOperator.abandon(EXECUTION_ID)).andStubReturn(new JobExecution(1L));
		
		replay(mockJobOperator);
		String response = controller.abandon(EXECUTION_ID);
		verify(mockJobOperator);
		assertEquals(RESPONSE_MSG, response);
	}
	
	@Test
	public void abandon_NoSuchJobExecutionException() throws NoSuchJobExecutionException, JobExecutionAlreadyRunningException { 
		Long EXECUTION_ID = 1L;
		String RESPONSE_MSG = "No Such Job Execution";
		
		expect(mockJobOperator.abandon(EXECUTION_ID)).andStubThrow(new NoSuchJobExecutionException(RESPONSE_MSG));
		
		replay(mockJobOperator);
		String response = controller.abandon(EXECUTION_ID);
		verify(mockJobOperator);
		assertEquals(RESPONSE_MSG, response);
	}
	
	@Test
	public void abandon_JobExecutionAlreadyRunningException() throws NoSuchJobExecutionException, JobExecutionAlreadyRunningException { 
		Long EXECUTION_ID = 1L;
		String RESPONSE_MSG = "Job Execution Already Running";
		
		expect(mockJobOperator.abandon(EXECUTION_ID)).andStubThrow(new JobExecutionAlreadyRunningException(RESPONSE_MSG));
		
		replay(mockJobOperator);
		String response = controller.abandon(EXECUTION_ID);
		verify(mockJobOperator);
		assertEquals(RESPONSE_MSG, response);
	}

}
