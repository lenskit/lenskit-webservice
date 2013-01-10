package org.grouplens.lenskit.webapp;

import java.net.URL;

import org.grouplens.common.dto.XmlDtoContentHandler;
import org.grouplens.lenskit.core.LenskitRecommenderEngine;
import org.grouplens.lenskit.core.LenskitRecommenderEngineFactory;
import org.grouplens.lenskit.webapp.handler.AddRatingRequestHandler;
import org.grouplens.lenskit.webapp.handler.DeleteRatingRequestHandler;
import org.grouplens.lenskit.webapp.handler.GetCurrentRatingsRequestHandler;
import org.grouplens.lenskit.webapp.handler.GetEventRequestHandler;
import org.grouplens.lenskit.webapp.handler.GetItemEventsRequestHandler;
import org.grouplens.lenskit.webapp.handler.GetItemMetadataRequestHandler;
import org.grouplens.lenskit.webapp.handler.GetItemRatingsRequestHandler;
import org.grouplens.lenskit.webapp.handler.GetItemStatisticsRequestHandler;
import org.grouplens.lenskit.webapp.handler.GetSystemStatisticsRequestHandler;
import org.grouplens.lenskit.webapp.handler.GetUserEventsRequestHandler;
import org.grouplens.lenskit.webapp.handler.GetUserMetadataRequestHandler;
import org.grouplens.lenskit.webapp.handler.GetUserPredictionsRequestHandler;
import org.grouplens.lenskit.webapp.handler.GetUserRatingsRequestHandler;
import org.grouplens.lenskit.webapp.handler.GetUserRecommendationsRequestHandler;
import org.grouplens.lenskit.webapp.handler.GetUserStatisticsRequestHandler;
import org.grouplens.lenskit.webapp.handler.RequestHandlerManager;
import org.junit.Before;

public class TestRequestHandlersXml extends AbstractRequestHandlerTest {

	@Before
	public void init() throws Exception {
		URL propertyFileUrl = ServerUtils.getFileUrl(this.getClass(), "recServer.properties");
		String filePath = propertyFileUrl.toString().substring("file:".length());
		Configuration config = new Configuration(filePath);
		LenskitRecommenderEngineFactory factory = config.getLenskitRecommenderEngineFactory();
		LenskitRecommenderEngine engine = factory.create();
		session = new Session(engine.open());
		manager = new RequestHandlerManager();
		manager.addHandler(new AddRatingRequestHandler());
		manager.addHandler(new DeleteRatingRequestHandler());
		manager.addHandler(new GetCurrentRatingsRequestHandler());
		manager.addHandler(new GetEventRequestHandler());
		manager.addHandler(new GetItemEventsRequestHandler());
		manager.addHandler(new GetItemMetadataRequestHandler());
		manager.addHandler(new GetItemRatingsRequestHandler());
		manager.addHandler(new GetItemStatisticsRequestHandler());
		manager.addHandler(new GetSystemStatisticsRequestHandler());
		manager.addHandler(new GetUserEventsRequestHandler());
		manager.addHandler(new GetUserMetadataRequestHandler());
		manager.addHandler(new GetUserPredictionsRequestHandler());
		manager.addHandler(new GetUserRatingsRequestHandler());
		manager.addHandler(new GetUserRecommendationsRequestHandler());
		manager.addHandler(new GetUserStatisticsRequestHandler());
		acceptHeader = "application/xml; application/json q=0.8";
		contentType = "application/xml";
		contentHandler = new XmlDtoContentHandler();
	}
}
