package org.anair.stanchion.healthcheck;

import org.apache.curator.framework.CuratorFramework;

public class ZookeeperHealthCheck implements HealthCheckJmxAware {
	
	private CuratorFramework curatorFramework;
	
	public ZookeeperHealthCheck(CuratorFramework curatorFramework) {
		this.curatorFramework = curatorFramework;
	}

	@Override
	public HealthCheckResult check() {
		if(curatorFramework != null && curatorFramework.getZookeeperClient() != null){
			return curatorFramework.getZookeeperClient().isConnected()?
					HealthCheckResult.healthy(curatorFramework.getZookeeperClient().getCurrentConnectionString())
					: HealthCheckResult.unhealthy(curatorFramework.getZookeeperClient().getCurrentConnectionString());
		}
		return null;
	}

	@Override
	public boolean isHealthy() {
		return check().isHealthy();
	}
	
}
