Stanchion - IT support console
---
Pluggable IT support console for tomcat/tcServer applications. Follow few simple steps and get a fully functional IT support UI in your webapp.

Features
--- 
1. __Server Info__: (Read-only) Display server name, host, port, JVM uptime etc
2. __Health check__: (Read-only) Identifies IBM MQ Connection Factories, DataSource(s) and Zookeeper connection in the app, perform a health check and display a color-coded GOOD/BAD status
3. __Server Properties__: (Read-only) Display JVM and tomcat properties 
4. __App properties__: (Read-only) Display environment properties externalized to app.properties file 
5. __Database-driven properties__: Create, update, delete and view database-driven configuration. Assuming a table called CONFIG_VALUES that has KEY column and VALUE column, add the following spring bean configuration in the classpath:
	`<bean class="org.apache.commons.configuration.DatabaseConfiguration"> <constructor-arg type="javax.sql.DataSource" ref="dataSource" /> <constructor-arg name="table" value="{SCHEMA}.CONFIG_VALUES" /> <constructor-arg name="keyColumn" value="KEY" /> <constructor-arg name="valueColumn" value="VALUE" /></bean>`
6. __Manage__:      
	a. __Logging__: Add,update,view log levels dynamically. Supports only Log4j 2.              
	b. __Cache__: View in-memory caches, size and ability to clear cache. Works ONLY with Hazelcast         
	c. __Camel Jms Consumer__: Manage camel based JMS Consumers                     
	d. __Spring Jms Consumer__: Manage spring based JMS Consumers          
7. __Hawtio__: Opens [Redhat open-source hawtio](http://hawt.io) JMX console. Hawtio also provides visualization and profiling capabilities for camel flows     
8. __Spring batch__: The home page lists all jobs in your application. Following capabilities are included:        
	a. Identify Spring batch jobs in classpath          
	b. Start a job       
	c.	See last 5 executions of a job. The jobs are listed with JOB-STATUS:CREATE-TIME as the header in descending order       
	d. See step detail of each execution. You may abandon, pause, restart jobs based on their current state        


---
Prerequisites
---
1. Java 8
2. Tomcat 7.x or Pivotal tcServer 3.x
3. Maven 3.x
4. Enable JMX for tomcat/tcServer


---
Getting started
----
Follow below mandatory steps: 
1. Download this project
2. Do a `mvn clean install` on the project
3. Add maven dependency to your web application pom:

		<dependency>
			<groupId>org.anair.stanchion</groupId>
			<artifactId>stanchion-web</artifactId>
			<version>1.0.0</version>
			<type>war</type>
			<scope>runtime</scope>
		</dependency> 
		<dependency>
			<groupId>org.anair.stanchion</groupId>
			<artifactId>stanchion-core</artifactId>
			<version>1.0.0</version>
			<type>jar</type>
		</dependency> 


4.Create __application-version.properties__ file in _src/main/resources_ with content:

	name=${pom.name}
	version=${pom.version}
           
5.Filter _src/main/resources_ folder to allow variable replacement for _application-version.properties_. Add the below resource segment to the pom:

	<build>
	...
		<resources>
			<resource>
				<directory>src/main/resources</directory>
				<filtering>true</filtering>
			</resource>
		</resources>
	...
	</build>	

6.By default, the application gets all JMX privileges. Add the jvm arg __-Djolokia.policy.type=ro__ to disable JMX write and execute privileges     
7. The support console web components is overlayed into your application
7.Hit url _http://{host name}:{port}/{context root}/stanchion/index.jsp_ and IT support console will be displayed

Health check
---
- Monitor health of datasource, MQ and zookeeper connection from the application
	- Hit the url _http://{host name}:{port}/{context root}/stanchion/secure/healthcheck_ and get back a json response with a list of database/mq connections and status
	- Configure monitoring tool to inspect JMX Mbeans to check resource health. Inspect all mbeans in __stanchion:type=health-check__ and check the boolean value of the exposed JMX attribute _Healthy_

Optional steps
---
- If there is a jar conflict with your application or do not need certain jars in the application, add dependency excludes to _stanchion-core_ dependency.

Notes
---
- If you do not have Spring batch job in the project, the UI show an info message. This is expected behavior.
- If you do not have a database-driven configuration setup, you will see an info message in the UI. This is expected behavior.
- For release notes, refer [CHANGELOG.md](CHANGELOG.md)

