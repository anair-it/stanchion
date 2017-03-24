package org.anair.stanchion.healthcheck;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.curator.CuratorZookeeperClient;
import org.apache.curator.framework.CuratorFramework;
import org.junit.Before;
import org.junit.Test;

public class ZookeeperHealthCheckTest {

	private ZookeeperHealthCheck zookeeperHealthCheck;
	private CuratorZookeeperClient mockCuratorZookeeperClient;
	private CuratorFramework mockCuratorFramework;
	
	@Before
	public void setup(){
		mockCuratorFramework = createMock(CuratorFramework.class);
		mockCuratorZookeeperClient = createMock(CuratorZookeeperClient.class);
		zookeeperHealthCheck = new ZookeeperHealthCheck(mockCuratorFramework);
	}
	
	@Test
	public void test() throws Exception {
		expect(mockCuratorFramework.getZookeeperClient()).andStubReturn(mockCuratorZookeeperClient);
		expect(mockCuratorZookeeperClient.isConnected()).andReturn(true);
		expect(mockCuratorZookeeperClient.getCurrentConnectionString()).andReturn("zoo1,zoo2,zoo3").anyTimes();
		replay(mockCuratorZookeeperClient, mockCuratorFramework);
		assertTrue(zookeeperHealthCheck.check().isHealthy());
		verify(mockCuratorZookeeperClient, mockCuratorFramework);
		
		reset(mockCuratorZookeeperClient, mockCuratorFramework);
		
		expect(mockCuratorFramework.getZookeeperClient()).andStubReturn(mockCuratorZookeeperClient);
		expect(mockCuratorZookeeperClient.getCurrentConnectionString()).andReturn("zoo1,zoo2,zoo3").anyTimes();
		expect(mockCuratorZookeeperClient.isConnected()).andReturn(false);
		replay(mockCuratorZookeeperClient, mockCuratorFramework);
		assertFalse(zookeeperHealthCheck.check().isHealthy());
		verify(mockCuratorZookeeperClient, mockCuratorFramework);
	}

}
