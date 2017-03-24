package org.anair.stanchion.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.anair.stanchion.controller.SystemPropertiesController;
import org.anair.stanchion.model.NameValue;
import org.junit.Before;
import org.junit.Test;


public class SystemPropertiesControllerTest {

	private SystemPropertiesController controller;

	@Before
	public void setUp() throws Exception {
		this.controller = new SystemPropertiesController();
		System.getProperties().clear();
	}


	@Test
	public void getSystemProperties_value() {
		String key = "a";
		
		System.setProperty(key, "testValue");
		List<NameValue> rv = this.controller.getSystemProperties();

		assertNotNull(rv);
		assertEquals(System.getProperties().entrySet().size(), rv.size());
		
		assertEquals(key, rv.get(0).getKey());
		assertEquals("testValue", rv.get(0).getValue());

	}

	@Test
	public void getSystemProperties_passwordCheck() {

		String key = "aPaSsWoRdb";
		System.setProperty(key, "someValue");
		List<NameValue> rv = this.controller.getSystemProperties();
		assertEquals(key, rv.get(0).getKey());
		assertEquals(SystemPropertiesController.OBFUSCATED, rv.get(0).getValue());

		System.getProperties().clear();
		
		key = "aPWdb";
		System.setProperty(key, "someValue");
		rv = this.controller.getSystemProperties();

		assertEquals(key, rv.get(0).getKey());
		assertEquals(SystemPropertiesController.OBFUSCATED, rv.get(0).getValue());

	}
}