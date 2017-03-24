package org.anair.stanchion.service;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.anair.stanchion.model.NameValue;
import org.anair.stanchion.service.ConfigurationServiceImpl;
import org.apache.commons.configuration.Configuration;
import org.junit.Before;
import org.junit.Test;

public class ConfigurationServiceImplTest {
	
	private ConfigurationServiceImpl service;
	private Configuration mockConfiguration;
	
	@Before
	public void setUp() throws Exception {
		mockConfiguration = createMock(Configuration.class);
		
		service = new ConfigurationServiceImpl();
		service.setConfigValueDao(mockConfiguration);
	}

	@Test
	public void get() {
		String KEY = "key";
		String VALUE = "value";
		
		expect(mockConfiguration.getProperty(KEY)).andReturn(VALUE);
		replay(mockConfiguration);
		
		assertEquals(VALUE, service.get(KEY));
		verify(mockConfiguration);
	}
	
	@Test
	public void getBoolean() {
		String KEY = "key";
		boolean VALUE = true;
		
		expect(mockConfiguration.getBoolean(KEY, false)).andReturn(VALUE);
		replay(mockConfiguration);
		
		assertEquals(VALUE, service.getBoolean(KEY, false));
		verify(mockConfiguration);
	}
	
	@Test
	public void getStringArray() {
		String KEY = "key";
		String[] VALUE = new String[]{"val1", "val2"};
		
		expect(mockConfiguration.getStringArray(KEY)).andReturn(VALUE);
		replay(mockConfiguration);
		
		assertArrayEquals(VALUE, service.getStringArray(KEY));
		verify(mockConfiguration);
	}
	
	@Test
	public void getAll() {
		List<NameValue> expected = Arrays.asList(new NameValue("k1", "val1"), new NameValue("k2", 123));
		
		expect(mockConfiguration.getKeys()).andReturn(Arrays.asList("k1","k2").iterator());
		expect(mockConfiguration.getProperty("k1")).andReturn("val1");
		expect(mockConfiguration.getProperty("k2")).andReturn(123);
		replay(mockConfiguration);
		List<NameValue> configValues = service.getAll();
		verify(mockConfiguration);
		
		assertEquals(2, configValues.size());
		assertEquals(expected, configValues);
	}
	
	@Test
	public void add() {
		String KEY = "key";
		String VALUE = "value";
		
		expect(mockConfiguration.containsKey(KEY)).andReturn(true);
		mockConfiguration.clearProperty(KEY);
		mockConfiguration.addProperty(KEY, VALUE);
		replay(mockConfiguration);
		
		service.add(new NameValue(KEY, VALUE));
		verify(mockConfiguration);
	}
	
	@Test
	public void update() {
		String KEY = "key";
		String VALUE = "value";
		
		expect(mockConfiguration.containsKey(KEY)).andReturn(true);
		mockConfiguration.setProperty(KEY, VALUE);
		replay(mockConfiguration);
		
		service.update(new NameValue(KEY, VALUE));
		verify(mockConfiguration);
	}
	
	@Test
	public void remove() {
		String KEY = "key";
		String VALUE = "value";
		
		expect(mockConfiguration.containsKey(KEY)).andReturn(true);
		mockConfiguration.clearProperty(KEY);
		replay(mockConfiguration);
		
		service.remove(new NameValue(KEY, VALUE));
		verify(mockConfiguration);
	}

}
