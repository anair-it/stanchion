package org.anair.stanchion.healthcheck;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:spring-h2.xml"})
public class JdbcHealthCheckTest {

	private JdbcHealthCheck check;
	@Autowired
	@Qualifier("h2DataSource")
	private DataSource dataSource;
	
	@Test
	public void testCheck() throws Exception {
		check = new JdbcHealthCheck(dataSource);
		assertTrue(check.isHealthy());
		
		check = new JdbcHealthCheck(null);
		assertFalse(check.isHealthy());
	}

}
