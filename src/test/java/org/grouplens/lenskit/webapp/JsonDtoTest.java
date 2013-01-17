package org.grouplens.lenskit.webapp;

import org.grouplens.common.dto.JsonDtoContentHandler;
import org.junit.Before;

public class JsonDtoTest extends AbstractDtoTest {

	@Before
	public void setup() {
		contentHandler = new JsonDtoContentHandler();
	}
}
	
