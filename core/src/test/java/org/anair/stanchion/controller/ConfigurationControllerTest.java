package org.anair.stanchion.controller;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.anair.stanchion.controller.ConfigurationController;
import org.anair.stanchion.model.NameValue;
import org.anair.stanchion.service.ConfigurationService;
import org.junit.Before;
import org.junit.Test;
import static org.easymock.EasyMock.*;

public class ConfigurationControllerTest {

	private ConfigurationController c;
	private ConfigurationService mockConfigurationService;
	
	@Before
	public void setUp() throws Exception {
		mockConfigurationService = createMock(ConfigurationService.class);
		
		c = new ConfigurationController();
		c.setConfigurationService(mockConfigurationService);
	}

	@Test
	public void getConfigList() {
		List<NameValue> expected = Arrays.asList(new NameValue("k1", "val1"), new NameValue("k2", 123));
		
		expect(mockConfigurationService.getAll()).andReturn(Arrays.asList(new NameValue("k1", "val1"), new NameValue("k2", 123)));
		replay(mockConfigurationService);
		List<NameValue> configValues = c.getConfigList();
		verify(mockConfigurationService);
		
		assertEquals(2, configValues.size());
		assertEquals(expected, configValues);
	}
	
	@Test
	public void addConfig_success() {
		String KEY = "key";
		String VALUE = "value";
		
		mockConfigurationService.add(new NameValue(KEY, VALUE));
		replay(mockConfigurationService);
		c.addConfig(KEY, VALUE);
		verify(mockConfigurationService);
	}
	
	@Test(expected=Exception.class)
	public void addConfig_exception() {
		String KEY = "key";
		String VALUE = "value";
		
		mockConfigurationService.add(new NameValue(KEY, VALUE));
		expectLastCall().andThrow(new Exception("error"));
		replay(mockConfigurationService);
		c.addConfig(KEY, VALUE);
		verify(mockConfigurationService);
	}
	
	@Test
	public void updateConfig_success() {
		String KEY = "key";
		String VALUE = "value";
		
		mockConfigurationService.update(new NameValue(KEY, VALUE));
		replay(mockConfigurationService);
		c.updateConfig(KEY, VALUE);
		verify(mockConfigurationService);
	}
	
	@Test(expected=Exception.class)
	public void updateConfig_exception() {
		String KEY = "key";
		String VALUE = "value";
		
		mockConfigurationService.update(new NameValue(KEY, VALUE));
		expectLastCall().andThrow(new Exception("error"));
		replay(mockConfigurationService);
		c.updateConfig(KEY, VALUE);
		verify(mockConfigurationService);
	}
	
	@Test
	public void removeConfig_success() {
		String KEY = "key";
		String VALUE = null;
		
		mockConfigurationService.remove(new NameValue(KEY, VALUE));
		replay(mockConfigurationService);
		c.removeConfig(KEY);
		verify(mockConfigurationService);
	}
	
	@Test(expected=Exception.class)
	public void removeConfig_exception() {
		String KEY = "key";
		String VALUE = null;
		
		mockConfigurationService.remove(new NameValue(KEY, VALUE));
		expectLastCall().andThrow(new Exception("error"));
		replay(mockConfigurationService);
		c.removeConfig(KEY);
		verify(mockConfigurationService);
	}

}
