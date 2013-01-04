package org.grouplens.lenskit.webapp;

import java.net.URL;
import java.sql.SQLException;

import org.grouplens.lenskit.core.LenskitRecommenderEngine;
import org.grouplens.lenskit.core.LenskitRecommenderEngineFactory;
import org.grouplens.lenskit.data.event.SimpleNullRating;
import org.grouplens.lenskit.data.event.SimpleRating;
import org.junit.Before;

public class TestSessionSchema extends AbstractSessionTest {

	@Before
	public void init() throws SQLException {
		URL propertyFileUrl = ServerUtils.getFileUrl(this.getClass(), "recServerNoData.properties");
		String filePath = propertyFileUrl.toString().substring("file:".length());
		Configuration config = new Configuration(filePath);
		LenskitRecommenderEngineFactory factory = config.getLenskitRecommenderEngineFactory();
		LenskitRecommenderEngine engine = factory.create();
		session = new Session(engine.open());
		session.addRating(new SimpleRating(5, 23, 256, 4.0, 20000));
		session.addRating(new SimpleRating(10, 735, 256, 3.5, 48000));
		session.addRating(new SimpleRating(15, 23, 32, 4.4, 32000));
		session.addRating(new SimpleRating(25, 491, 2048, 5.0, 14000));
		session.addRating(new SimpleRating(35, 306, 256, 3.8, 23000));
		session.addRating(new SimpleRating(70, 938, 512, 4.7, 82000));
		session.addRating(new SimpleRating(75, 23, 1024, 5.0, 93000));
		session.addRating(new SimpleRating(95, 23, 8192, 3.8, 37000));
		session.addRating(new SimpleRating(45, 837, 256, 2.9, 83000));
		session.addRating(new SimpleRating(40, 294, 256, 1.5, 90000));
		session.addRating(new SimpleNullRating(85, 23, 256, 130000));
		session.addRating(new SimpleNullRating(80, 23, 8192, 150000));
	}
}