package org.anair.stanchion.controller;

import static org.junit.Assert.*;

import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import static org.easymock.EasyMock.*;

import org.anair.stanchion.controller.ServerInfoController;
import org.anair.stanchion.model.NameValue;
import org.junit.Before;
import org.junit.Test;


public class ServerInfoControllerTest {

	private ServerInfoController controller;
	private HttpServletRequest mockRequest;
	private ServletContext mockServletContext;

	@Before
	public void setUp() throws Exception {
		this.controller = new ServerInfoController();
		this.mockRequest = createMock(HttpServletRequest.class);
		this.mockServletContext = createMock(ServletContext.class);
		this.controller.setServletContext(this.mockServletContext);
	}

	@Test
	public void getSystemInfo() {
		expect(this.mockServletContext.getServerInfo()).andReturn("context.serverInfo");
		expect(this.mockRequest.getLocalName()).andReturn("request.localName");
		expect(this.mockRequest.getLocalAddr()).andReturn("request.localAddr");
		expect(this.mockRequest.getLocalPort()).andReturn(Integer.valueOf(2));
		expect(this.mockRequest.getContextPath()).andReturn("request.contextPath");
		replay(this.mockRequest, this.mockServletContext);

		List<NameValue> rv = this.controller.getSystemInfo(this.mockRequest);

		assertEquals(8, rv.size());
		verify(this.mockRequest, this.mockServletContext);
	}
	
}