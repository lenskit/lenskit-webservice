package org.grouplens.lenskit.webapp;

import java.net.URL;
import java.sql.SQLException;

import org.grouplens.lenskit.core.LenskitRecommenderEngine;
import org.grouplens.lenskit.core.LenskitRecommenderEngineFactory;
import org.junit.Before;

public class TestSessionData extends AbstractSessionTest {

	@Before
	public void init() throws SQLException {
		URL propertyFileUrl = ServerUtils.getFileUrl(this.getClass(), "recServer.properties");
		String filePath = propertyFileUrl.toString().substring("file:".length());
		Configuration config = new Configuration(filePath);	
		LenskitRecommenderEngineFactory recommenderEngineFactory = config.getLenskitRecommenderEngineFactory();
		LenskitRecommenderEngine engine = recommenderEngineFactory.create();
		session = new Session(engine.open());
	}
}