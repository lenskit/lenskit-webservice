package org.grouplens.lenskit.webapp;

import org.grouplens.common.dto.XmlDtoContentHandler;
import org.junit.Before;

public class XmlServerIntegrationTest extends AbstractServerIntegrationTest {
	
	@Before
	public void setup() {
		acceptHeader = "application/xml; application/json q=0.8";
		contentType = "application/xml";
		contentHandler = new XmlDtoContentHandler();
	}
}