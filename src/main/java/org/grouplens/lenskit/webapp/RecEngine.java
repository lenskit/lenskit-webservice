package org.grouplens.lenskit.webapp;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.grouplens.lenskit.core.LenskitRecommenderEngineFactory;
import org.grouplens.lenskit.webapp.Configuration.ConfigurationException;
import org.grouplens.lenskit.webapp.ServerUtils.ParsedUrl;
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
import org.grouplens.lenskit.webapp.handler.RequestHandler;
import org.grouplens.lenskit.webapp.handler.RequestHandler.RequestMethod;
import org.grouplens.lenskit.webapp.handler.RequestHandlerManager;

public class RecEngine extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private RequestHandlerManager manager;
	private Session session;
	private Configuration configuration;

	/**
	 * web.xml parameter name when initializing the servlet
	 */
	public static final String REC_ENGINE_CONFIG_PARAMETER = "re-config-path";

	public static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>";

	private static final Object2IntOpenHashMap<Class<? extends Exception>> exceptionMapping;
	static {
		exceptionMapping = new Object2IntOpenHashMap<Class<? extends Exception>>();
		exceptionMapping.put(BadRequestException.class, HttpServletResponse.SC_BAD_REQUEST);
		exceptionMapping.put(ResourceNotFoundException.class, HttpServletResponse.SC_NOT_FOUND);
		exceptionMapping.put(ConflictException.class, HttpServletResponse.SC_CONFLICT);
		exceptionMapping.put(RequestContentTypeException.class, HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
		exceptionMapping.put(ResponseContentTypeException.class, HttpServletResponse.SC_NOT_ACCEPTABLE);
		exceptionMapping.put(UnauthorizedModificationException.class, HttpServletResponse.SC_FORBIDDEN);
		exceptionMapping.put(Exception.class, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);

		try {
			String configPath = config.getInitParameter(REC_ENGINE_CONFIG_PARAMETER);
			this.configuration = new Configuration(configPath);
			LenskitRecommenderEngineFactory factory = configuration.getLenskitRecommenderEngineFactory();
			session = new Session(factory.create().open());
		} catch (ConfigurationException ce) {
			throw new ServletException("Unable to load rec-engine configuration", ce);
		}

		manager = new RequestHandlerManager();
		manager.addHandler(new AddRatingRequestHandler());
		manager.addHandler(new DeleteRatingRequestHandler());
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
	}

	@Override 
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doRequest(RequestMethod.GET, request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doRequest(RequestMethod.POST, request, response);
	}

	@Override
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doRequest(RequestMethod.DELETE, request, response);
	}

	@Override
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doRequest(RequestMethod.PUT, request, response);
	}

	private void doRequest(RequestMethod method, HttpServletRequest request, HttpServletResponse response) {
		PrintWriter out = null;
		try {
			out = response.getWriter();
			ParsedUrl parsed = ServerUtils.parseUrl(request, manager.getDefinedResources());
			RequestHandler handler = manager.getHandler(method, parsed);
			if (handler == null) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				out.println("Invalid URL");
			} else {				
				response.setCharacterEncoding("UTF-8");
				handler.handle(session, parsed, request, response);
				response.flushBuffer();
			}
		} catch (Exception e) {
			response.setStatus(exceptionMapping.get(e.getClass()));
			if (out != null) {
				out.println(e.getMessage());
			}
		}
	}

	@Override
	public void destroy() {
		super.destroy();
		session.close();
	}
}