package org.grouplens.lenskit.webapp;

import org.grouplens.common.dto.XmlDtoContentHandler;
import org.junit.Before;

public class TestRequestHandlersXml extends AbstractRequestHandlerTest {

	@Before
	public void init() throws Exception {
		acceptHeader = "application/xml; application/json q=0.8";
		contentType = "application/xml";
		contentHandler = new XmlDtoContentHandler();
		super.init();
	}
}
