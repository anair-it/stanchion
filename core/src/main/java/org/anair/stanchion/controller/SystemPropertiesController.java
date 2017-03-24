package org.anair.stanchion.controller;

import java.util.List;
import java.util.Map.Entry;

import org.anair.stanchion.model.NameValue;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.Lists;

/**
 * system properties controller
 *
 */
@RestController
@RequestMapping(value="/sysprops",produces=MediaType.APPLICATION_JSON_VALUE)
public class SystemPropertiesController {

	protected static final String PASSWORD = "password";
	protected static final String PWD = "pwd";
	protected static final String OBFUSCATED = "********";

	@RequestMapping
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody List<NameValue> getSystemProperties() {

		List<NameValue> nameValues = Lists.newArrayList();
		for (final Entry<Object, Object> item : System.getProperties().entrySet()) {
			String propertyName = item.getKey().toString();
			if (StringUtils.containsIgnoreCase(propertyName, PASSWORD) || StringUtils.containsIgnoreCase(propertyName, PWD)) {
				nameValues.add(new NameValue(propertyName, OBFUSCATED));
			} else {
				nameValues.add(new NameValue(propertyName, item.getValue().toString()));
			}
		}

		return nameValues;
	}
}