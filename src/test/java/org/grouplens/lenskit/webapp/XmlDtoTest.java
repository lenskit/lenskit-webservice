package org.grouplens.lenskit.webapp;

import org.grouplens.common.dto.XmlDtoContentHandler;
import org.junit.Before;

public class XmlDtoTest extends AbstractDtoTest {

	@Before
	public void setup() {
		contentHandler = new XmlDtoContentHandler();
		showOutput = false;
	}
}
