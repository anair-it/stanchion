package org.anair.stanchion.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.anair.stanchion.model.LoggerInfo;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.Sets;

@RestController
@RequestMapping(value="/logging",produces=MediaType.APPLICATION_JSON_VALUE)
public class LoggingController {

	@RequestMapping("list")
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody List<LoggerInfo> getLoggerList() {
		return getLog4J2LoggerList();
	}

	@RequestMapping("update")
	@ResponseStatus(HttpStatus.OK)
	public void addLogger(@RequestBody final LoggerInfo loggerInfo) throws Exception {
		Validate.notNull(loggerInfo, "loggerInfo cannot be null");
		Validate.notEmpty(loggerInfo.getName(), "logger name cannot be blank or null");
		
		addLog4J2Logger(loggerInfo);
	}
	
	
	private void addLog4J2Logger(final LoggerInfo loggerInfo) throws Exception {
		final LoggerContext logContext = (LoggerContext) LogManager.getContext(false);
		
	    final Configuration config = logContext.getConfiguration();
	    
	    LoggerConfig loggerConfig = config.getLoggerConfig(loggerInfo.getName());
	    LoggerConfig specificConfig = loggerConfig;

	    // We need a specific configuration for this logger,
	    // otherwise we would change the level of all other loggers
	    // having the original configuration as parent as well
	    if (!loggerConfig.getName().equals(loggerInfo.getName())) {
	        specificConfig = new LoggerConfig(loggerInfo.getName(), Level.toLevel(loggerInfo.getLevel()), true);
	        specificConfig.setParent(loggerConfig);
	        config.addLogger(loggerInfo.getName(), specificConfig);
	    }
	    specificConfig.setLevel(Level.toLevel(loggerInfo.getLevel()));
	    logContext.updateLoggers();
	}
	
	private List<LoggerInfo> getLog4J2LoggerList() {
		LoggerContext logContext = (LoggerContext) LogManager.getContext(false);

		final Configuration config = logContext.getConfiguration();
		List<LoggerConfig> configuredloggers = new ArrayList<LoggerConfig>(config.getLoggers().values());
		
		final Set<LoggerInfo> rv = Sets.newHashSet();

		for (final LoggerConfig configuredLogger : configuredloggers) {
			final LoggerInfo li = new LoggerInfo();
			li.setName(configuredLogger.getName());
			li.setLevel(null == configuredLogger.getLevel() ? null : configuredLogger.getLevel().toString());
			rv.add(li);
		}
		
		List<org.apache.logging.log4j.Logger> loggers = new ArrayList<org.apache.logging.log4j.Logger>(logContext.getLoggers());
		
		for (final org.apache.logging.log4j.Logger logger : loggers) {
			final LoggerInfo li = new LoggerInfo();
			li.setName(logger.getName());
			li.setLevel(null == logger.getLevel() ? null : logger.getLevel().toString());
			rv.add(li);
		}
		
		List<LoggerInfo> sortedrv = new ArrayList<LoggerInfo>(rv);
		Collections.sort(sortedrv, new Comparator<LoggerInfo>() {
			@Override
			public int compare(LoggerInfo o1, LoggerInfo o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		
		
		return sortedrv;
	}
	
}
