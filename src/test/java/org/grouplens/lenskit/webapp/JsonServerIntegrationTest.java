package org.grouplens.lenskit.webapp;

import org.grouplens.common.dto.JsonDtoContentHandler;
import org.junit.Before;

public class JsonServerIntegrationTest extends AbstractServerIntegrationTest {
	
	@Before
	public void setup() {
		acceptHeader = "application/json; application/xml q=0.8";
		contentType = "application/json";
		contentHandler = new JsonDtoContentHandler();
	}
}