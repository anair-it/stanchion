package org.anair.stanchion.healthcheck;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;


public class JdbcHealthCheck implements HealthCheckJmxAware {

	private DataSource dataSource;
	private static final String ORACLE_VALIDATION_SQL = "SELECT 1 FROM DUAL";

	public JdbcHealthCheck(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	@Override
	public HealthCheckResult check() {
		Connection con = null;
		Statement st = null;
		ResultSet rs = null;
		
		try {
			con = dataSource.getConnection();
			st = con.createStatement();
			String dbProductName = con.getMetaData().getDatabaseProductName();
			if (dbProductName.equalsIgnoreCase("oracle")) {
				rs = st.executeQuery(ORACLE_VALIDATION_SQL);
			}
			return HealthCheckResult.healthy();
		} catch (Exception ex) {
			return HealthCheckResult.unhealthy(ex.getMessage());
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
				}
			}
			if (st != null) {
				try {
					st.close();
				} catch (SQLException e) {
				}
			}
			if (con != null) {
				try {
					con.close();
				} catch (SQLException e) {
				}
			}
		}
	}
	
	@Override
	public boolean isHealthy() {
		return check().isHealthy();
	}

}
