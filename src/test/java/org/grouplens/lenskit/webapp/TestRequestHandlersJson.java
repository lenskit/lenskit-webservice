package org.grouplens.lenskit.webapp;

import org.grouplens.common.dto.JsonDtoContentHandler;
import org.junit.Before;

public class TestRequestHandlersJson extends AbstractRequestHandlerTest {
	
	@Before
	public void init() throws Exception {
		acceptHeader = "application/json; application/xml q=0.8";
		contentType = "application/json";
		contentHandler = new JsonDtoContentHandler();
		super.init();
	}
}