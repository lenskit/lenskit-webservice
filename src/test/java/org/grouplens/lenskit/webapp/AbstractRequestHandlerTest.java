package org.grouplens.lenskit.webapp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.grouplens.common.dto.Dto;
import org.grouplens.common.dto.DtoContainer;
import org.grouplens.common.dto.DtoContentHandler;
import org.grouplens.lenskit.core.LenskitRecommenderEngine;
import org.grouplens.lenskit.core.LenskitRecommenderEngineFactory;
import org.grouplens.lenskit.data.Event;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.webapp.ServerUtils.ParsedUrl;
import org.grouplens.lenskit.webapp.dto.EventDto;
import org.grouplens.lenskit.webapp.dto.ItemDto;
import org.grouplens.lenskit.webapp.dto.ItemEventsDto;
import org.grouplens.lenskit.webapp.dto.ItemRatingsDto;
import org.grouplens.lenskit.webapp.dto.ItemStatisticsDto;
import org.grouplens.lenskit.webapp.dto.PredictionDto;
import org.grouplens.lenskit.webapp.dto.PreferenceDto;
import org.grouplens.lenskit.webapp.dto.RatingDto;
import org.grouplens.lenskit.webapp.dto.SystemStatisticsDto;
import org.grouplens.lenskit.webapp.dto.UserDto;
import org.grouplens.lenskit.webapp.dto.UserEventsDto;
import org.grouplens.lenskit.webapp.dto.UserPredictionsDto;
import org.grouplens.lenskit.webapp.dto.UserPreferencesDto;
import org.grouplens.lenskit.webapp.dto.UserRatingsDto;
import org.grouplens.lenskit.webapp.dto.UserRecommendationsDto;
import org.grouplens.lenskit.webapp.dto.UserStatisticsDto;
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
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import com.mockrunner.mock.web.MockHttpServletRequest;
import com.mockrunner.mock.web.MockHttpServletResponse;

public abstract class AbstractRequestHandlerTest {

	protected Session session;
	protected RequestHandlerManager manager;
	protected String acceptHeader;
	protected String contentType;
	protected DtoContentHandler contentHandler;
	
	public static final double EPSILON = 1.0e-6;
	
	private MockHttpServletRequest request;
	private MockHttpServletResponse response;
	
	private static final String CONTEXT_PATH = "/lenskit";

	// Intended to be called by setup methods in subclasses
	public void init() throws Exception {
		String filePath = ServerUtils.getFilePath(this.getClass(), "recServer.properties");
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
		
		request = new MockHttpServletRequest();
		request.setContextPath(CONTEXT_PATH);
		request.setCharacterEncoding("UTF-8");
		request.setContentType(contentType);
		request.setHeader("Accept", acceptHeader);
		
		response = new MockHttpServletResponse();
	}
	
	@After
	public void cleanUp() {
		session.close();
	}

	@Test
	public void testAddRatingRequestHandler() throws Exception {
		request.setRequestURI(CONTEXT_PATH + "/users/938/events/ratings");
		request.setMethod("POST");
		DtoContainer<RatingDto> container = new DtoContainer<RatingDto>(RatingDto.class, false);
		container.set(new RatingDto("105", "938", "8192", (long) 35700, 4.5));
		String requestBody = contentHandler.toString(container);
		request.setBodyContent(requestBody);
		request.setContentLength(requestBody.length());
		ParsedUrl parsed = ServerUtils.parseUrl(request, manager.getDefinedResources());
		RequestHandler handler = manager.getHandler(RequestMethod.POST, parsed);
		handler.handle(session, parsed, request, response);
		response.flushBuffer();
		container.clear();
		contentHandler.fromString(response.getOutputStreamContent(), container);
		RatingDto actual = container.getSingle();
		RatingDto expected = new RatingDto("105", "938", "8192", (long) 35700, 4.5, session.getEventRevId(105));
		assertDtoEquals(expected, actual);
		assertEquals(MockHttpServletResponse.SC_CREATED, response.getStatusCode());
	}

	@Test
	public void testGetCurrentRatingsRequestHandler() throws Exception {
		request.setRequestURI(CONTEXT_PATH + "/users/23/currentRatings");
		request.setMethod("GET");
		ParsedUrl parsed = ServerUtils.parseUrl(request, manager.getDefinedResources());
		RequestHandler handler = manager.getHandler(RequestMethod.GET, parsed);
		handler.handle(session, parsed, request, response);
		assertEquals(MockHttpServletResponse.SC_OK, response.getStatusCode());
		response.flushBuffer();
		DtoContainer<RatingDto> result = new DtoContainer<RatingDto>(RatingDto.class, true);
		contentHandler.fromString(response.getOutputStreamContent(), result);
		List<RatingDto> ratingDtos = result.get();
		assertEquals(2, ratingDtos.size());
		Object2DoubleOpenHashMap<String> ratings = new Object2DoubleOpenHashMap<String>(2);
		for (RatingDto dto : ratingDtos) {
			ratings.put(dto.item_id, dto.value);
		}
		for (Map.Entry<Long, Double> e: session.getCurrentUserRatings(23).entrySet()) {
			assertEquals(ratings.get(Long.toString(e.getKey())), e.getValue(), EPSILON);
		}
	}

	@Test
	public void testGetEventRequestHandler() throws Exception {
		request.setRequestURI(CONTEXT_PATH + "/events/45");
		request.setMethod("GET");
		ParsedUrl parsed = ServerUtils.parseUrl(request, manager.getDefinedResources());
		RequestHandler handler = manager.getHandler(RequestMethod.GET, parsed);
		handler.handle(session, parsed, request, response);
		assertEquals(MockHttpServletResponse.SC_OK, response.getStatusCode());
		response.flushBuffer();
		DtoContainer<EventDto> result = new DtoContainer<EventDto>(EventDto.class, false);
		contentHandler.fromString(response.getOutputStreamContent(), result);
		EventDto actual = result.getSingle();
		Rating r = (Rating)session.getEvent(45);
		String eid = Long.toString(r.getId());
		String iid = Long.toString(r.getItemId());
		String uid = Long.toString(r.getUserId());
		String _rid = session.getEventRevId(45);
		long timestamp = r.getTimestamp();
		double value = r.getPreference().getValue();
		EventDto expected = new EventDto("rating", eid, uid, iid, timestamp, value, _rid);
		assertDtoEquals(expected, actual);
	}

	@Test
	public void testGetItemEventsRequestHandler() throws Exception {
		request.setRequestURI(CONTEXT_PATH + "/items/256/events");
		request.setMethod("GET");
		ParsedUrl parsed = ServerUtils.parseUrl(request, manager.getDefinedResources());
		RequestHandler handler = manager.getHandler(RequestMethod.GET, parsed);
		handler.handle(session, parsed, request, response);
		assertEquals(MockHttpServletResponse.SC_OK, response.getStatusCode());
		response.flushBuffer();
		DtoContainer<ItemEventsDto> container = new DtoContainer<ItemEventsDto>(ItemEventsDto.class, false);
		contentHandler.fromString(response.getOutputStreamContent(), container);
		ItemEventsDto actual = container.getSingle();
		ItemEventsDto expected = new ItemEventsDto("256", 6, 0);
		for (Event evt : session.getItemEvents(256)) {
			String eid = Long.toString(evt.getId());
			String uid = Long.toString(evt.getUserId());
			long timestamp = evt.getTimestamp();
			String _rid = session.getEventRevId(evt.getId());
			String type = "event";
			Double value = null;
			if (evt instanceof Rating) {
				Rating r = (Rating)evt;
				if (r.getPreference() != null) {
					value = r.getPreference().getValue();
					type = "rating";
				} else {
					type = "unrating";
				}
			}
			expected.addEvent(type, eid, uid, timestamp, value, _rid);
		}
		assertDtoEquals(expected, actual);
	}

	@Test
	public void testGetItemEventsRequestHandler2() throws Exception {
		request.setRequestURI(CONTEXT_PATH + "/items/256/events");
		request.setMethod("GET");
		request.setQueryString("user=23&user=735&user=306");
		ParsedUrl parsed = ServerUtils.parseUrl(request, manager.getDefinedResources());
		RequestHandler handler = manager.getHandler(RequestMethod.GET, parsed);
		handler.handle(session, parsed, request, response);
		assertEquals(MockHttpServletResponse.SC_OK, response.getStatusCode());
		response.flushBuffer();
		DtoContainer<ItemEventsDto> container = new DtoContainer<ItemEventsDto>(ItemEventsDto.class, false);
		contentHandler.fromString(response.getOutputStreamContent(), container);
		ItemEventsDto actual = container.getSingle();
		ItemEventsDto expected = new ItemEventsDto("256", 4, 0);
		LongOpenHashSet users = new LongOpenHashSet(new long[]{23, 735, 306});
		for (Event evt : session.getItemEvents(256, users)) {
			String eid = Long.toString(evt.getId());
			String uid = Long.toString(evt.getUserId());
			long timestamp = evt.getTimestamp();
			String _rid = session.getEventRevId(evt.getId());
			String type = "event";
			Double value = null;
			if (evt instanceof Rating) {
				Rating r = (Rating)evt;
				if (r.getPreference() != null) {
					value = r.getPreference().getValue();
					type = "rating";
				} else {
					type = "unrating";
				}
			}
			expected.addEvent(type, eid, uid, timestamp, value, _rid);
		}
		assertDtoEquals(expected, actual);
	}

	@Test
	public void testGetItemEventsRequestHandler3() throws Exception {
		request.setRequestURI(CONTEXT_PATH + "/items/256/events");
		request.setMethod("GET");
		request.setQueryString("user=23&user=735&user=306&null=false");
		ParsedUrl parsed = ServerUtils.parseUrl(request, manager.getDefinedResources());
		RequestHandler handler = manager.getHandler(RequestMethod.GET, parsed); 
		handler.handle(session, parsed, request, response);
		assertEquals(MockHttpServletResponse.SC_OK, response.getStatusCode());
		response.flushBuffer();
		DtoContainer<ItemEventsDto> container = new DtoContainer<ItemEventsDto>(ItemEventsDto.class, false);
		contentHandler.fromString(response.getOutputStreamContent(), container);
		ItemEventsDto actual = container.getSingle();
		ItemEventsDto expected = new ItemEventsDto("256", 3, 0);
		LongOpenHashSet users = new LongOpenHashSet(new long[]{23, 735, 306});
		for (Event evt : session.getItemEvents(256, users)) {
			String eid = Long.toString(evt.getId());
			String uid = Long.toString(evt.getUserId());
			long timestamp = evt.getTimestamp();
			String _rid = session.getEventRevId(evt.getId());
			String type = "event";
			Double value = null;
			if (evt instanceof Rating) {
				Rating r = (Rating)evt;
				if (r.getPreference() != null) {
					value = r.getPreference().getValue();
					type = "rating";
				} else {
					continue;
				}
			}
			expected.addEvent(type, eid, uid, timestamp, value, _rid);
		}
		assertDtoEquals(expected, actual);
	}

	@Test
	public void testGetItemMetadataRequestHandler() throws Exception {
		request.setRequestURI(CONTEXT_PATH + "/items/512");
		request.setMethod("GET");
		ParsedUrl parsed = ServerUtils.parseUrl(request, manager.getDefinedResources());
		RequestHandler handler = manager.getHandler(RequestMethod.GET, parsed);
		handler.handle(session, parsed, request, response);
		assertEquals(MockHttpServletResponse.SC_OK, response.getStatusCode());
		response.flushBuffer();
		DtoContainer<ItemDto> result= new DtoContainer<ItemDto>(ItemDto.class, false);
		contentHandler.fromString(response.getOutputStreamContent(), result);
		ItemDto actual = result.getSingle();
		ItemDto expected = new ItemDto("512");
		assertDtoEquals(expected, actual);
	}

	@Test
	public void testGetItemRatingsRequestHandler() throws Exception {
		request.setRequestURI(CONTEXT_PATH + "/items/256/events/ratings");
		request.setMethod("GET");
		ParsedUrl parsed = ServerUtils.parseUrl(request, manager.getDefinedResources());
		RequestHandler handler = manager.getHandler(RequestMethod.GET, parsed);
		handler.handle(session, parsed, request, response);
		assertEquals(MockHttpServletResponse.SC_OK, response.getStatusCode());
		response.flushBuffer();
		DtoContainer<ItemRatingsDto> result = new DtoContainer<ItemRatingsDto>(ItemRatingsDto.class, false);
		contentHandler.fromString(response.getOutputStreamContent(), result);
		ItemRatingsDto actual = result.getSingle();
		ItemRatingsDto expected = new ItemRatingsDto("256", 6, 0);
		for (Rating r : session.getItemRatings(256)) {
			String eid = Long.toString(r.getId());
			String uid = Long.toString(r.getUserId());
			long timestamp = r.getTimestamp();
			String _rid = session.getEventRevId(r.getId());
			Double value = null;
			if (r.getPreference() != null) {
				value = r.getPreference().getValue();
			}
			expected.addRating(eid, uid, timestamp, value, _rid);
		}
		assertDtoEquals(expected, actual);
	}

	@Test
	public void testGetItemRatingsRequestHandler2() throws Exception {
		request.setRequestURI(CONTEXT_PATH + "/items/256/events/ratings");
		request.setMethod("GET");
		request.setQueryString("user=23&user=735&user=306");
		ParsedUrl parsed = ServerUtils.parseUrl(request, manager.getDefinedResources());
		RequestHandler handler = manager.getHandler(RequestMethod.GET, parsed);
		handler.handle(session, parsed, request, response);
		assertEquals(MockHttpServletResponse.SC_OK, response.getStatusCode());
		response.flushBuffer();
		DtoContainer<ItemRatingsDto> container = new DtoContainer<ItemRatingsDto>(ItemRatingsDto.class, false);
		contentHandler.fromString(response.getOutputStreamContent(), container);
		ItemRatingsDto actual = container.getSingle();
		ItemRatingsDto expected = new ItemRatingsDto("256", 4, 0);
		LongOpenHashSet users = new LongOpenHashSet(new long[]{23, 735, 306});
		for (Rating r : session.getItemRatings(256, users)) {
			String eid = Long.toString(r.getId());
			String uid = Long.toString(r.getUserId());
			long timestamp = r.getTimestamp();
			String _rid = session.getEventRevId(r.getId());
			Double value = null;
			if (r.getPreference() != null) {
				value = r.getPreference().getValue();
			}
			expected.addRating(eid, uid, timestamp, value, _rid);
		}
		assertDtoEquals(expected, actual);
	}

	@Test
	public void testGetItemRatingsRequestHandler3() throws Exception {
		request.setRequestURI(CONTEXT_PATH + "/items/256/events/ratings");
		request.setMethod("GET");
		request.setQueryString("user=23&user=735&user=306&null=false");
		ParsedUrl parsed = ServerUtils.parseUrl(request, manager.getDefinedResources());
		RequestHandler handler = manager.getHandler(RequestMethod.GET, parsed);
		handler.handle(session, parsed, request, response);
		assertEquals(MockHttpServletResponse.SC_OK, response.getStatusCode());
		response.flushBuffer();
		DtoContainer<ItemRatingsDto> container = new DtoContainer<ItemRatingsDto>(ItemRatingsDto.class, false);
		contentHandler.fromString(response.getOutputStreamContent(), container);
		ItemRatingsDto actual = container.getSingle();
		ItemRatingsDto expected = new ItemRatingsDto("256", 3, 0);
		LongOpenHashSet users = new LongOpenHashSet(new long[]{23, 735, 306});
		for (Rating r : session.getItemRatings(256, users)) {
			String eid = Long.toString(r.getId());
			String uid = Long.toString(r.getUserId());
			long timestamp = r.getTimestamp();
			String _rid = session.getEventRevId(r.getId());
			Double value = null;
			if (r.getPreference() != null) {
				value = r.getPreference().getValue();
			} else {
				continue;
			}
			expected.addRating(eid, uid, timestamp, value, _rid);
		}
		assertDtoEquals(expected, actual);
	}

	@Test
	public void testGetItemStatisticsDto() throws Exception {
		request.setRequestURI(CONTEXT_PATH + "/items/256/statistics");
		request.setMethod("GET");
		ParsedUrl parsed = ServerUtils.parseUrl(request, manager.getDefinedResources());
		RequestHandler handler = manager.getHandler(RequestMethod.GET, parsed);
		handler.handle(session, parsed, request, response);
		assertEquals(MockHttpServletResponse.SC_OK, response.getStatusCode());
		response.flushBuffer();
		DtoContainer<ItemStatisticsDto> result= new DtoContainer<ItemStatisticsDto>(ItemStatisticsDto.class, false);
		contentHandler.fromString(response.getOutputStreamContent(), result);
		ItemStatisticsDto actual = result.getSingle();
		ItemStatisticsDto expected = new ItemStatisticsDto("256", 6, 4, 2.925);
		assertDtoEquals(expected, actual);
	}

	@Test
	public void testSystemStatisticsDto() throws Exception {
		request.setRequestURI(CONTEXT_PATH + "/statistics");
		request.setMethod("GET");
		ParsedUrl parsed = ServerUtils.parseUrl(request, manager.getDefinedResources());
		RequestHandler handler = manager.getHandler(RequestMethod.GET, parsed);
		handler.handle(session, parsed, request, response);
		assertEquals(MockHttpServletResponse.SC_OK, response.getStatusCode());
		response.flushBuffer();
		DtoContainer<SystemStatisticsDto> container = new DtoContainer<SystemStatisticsDto>(SystemStatisticsDto.class, false);
		contentHandler.fromString(response.getOutputStreamContent(), container);
		SystemStatisticsDto actual = container.getSingle();
		int userCount = session.getUserCount();
		int itemCount = session.getItemCount();
		int eventCount = session.getEventCount();
		SystemStatisticsDto expected = new SystemStatisticsDto(userCount, itemCount, eventCount);
		assertDtoEquals(actual, expected);
	}

	@Test
	public void testGetUserEventsRequestHandler() throws Exception {
		request.setRequestURI(CONTEXT_PATH + "/users/23/events");
		request.setMethod("GET");
		ParsedUrl parsed = ServerUtils.parseUrl(request, manager.getDefinedResources());
		RequestHandler handler = manager.getHandler(RequestMethod.GET, parsed);
		handler.handle(session, parsed, request, response);
		assertEquals(MockHttpServletResponse.SC_OK, response.getStatusCode());
		response.flushBuffer();
		DtoContainer<UserEventsDto> container = new DtoContainer<UserEventsDto>(UserEventsDto.class, false);
		contentHandler.fromString(response.getOutputStreamContent(), container);
		UserEventsDto actual = container.getSingle();
		UserEventsDto expected = new UserEventsDto("23", 6, 0);
		for (Event evt : session.getUserEvents(23)) {
			String eid = Long.toString(evt.getId());
			String iid = Long.toString(evt.getItemId());
			long timestamp = evt.getTimestamp();
			String _rid = session.getEventRevId(evt.getId());
			String type = "event";
			Double value = null;
			if (evt instanceof Rating) {
				Rating r = (Rating)evt;
				if (r.getPreference() != null) {
					value = r.getPreference().getValue();
					type = "rating";
				} else {
					type = "unrating";
				}
			}
			expected.addEvent(type, eid, iid, timestamp, value, _rid);
		}
		assertDtoEquals(actual, expected);
	}

	@Test
	public void testGetUserEventsRequestHandler2() throws Exception {
		request.setRequestURI(CONTEXT_PATH + "/users/23/events");
		request.setQueryString("item=256&item=1024");
		request.setMethod("GET");
		ParsedUrl parsed = ServerUtils.parseUrl(request, manager.getDefinedResources());
		RequestHandler handler = manager.getHandler(RequestMethod.GET, parsed);
		handler.handle(session, parsed, request, response);
		assertEquals(MockHttpServletResponse.SC_OK, response.getStatusCode());
		response.flushBuffer();
		DtoContainer<UserEventsDto> container = new DtoContainer<UserEventsDto>(UserEventsDto.class, false);
		contentHandler.fromString(response.getOutputStreamContent(), container);
		UserEventsDto actual = container.getSingle();
		UserEventsDto expected = new UserEventsDto("23", 3, 0);
		LongOpenHashSet items = new LongOpenHashSet(new long[]{256, 1024});
		for (Event evt : session.getUserEvents(23, items)) {
			String eid = Long.toString(evt.getId());
			String iid = Long.toString(evt.getItemId());
			long timestamp = evt.getTimestamp();
			String _rid = session.getEventRevId(evt.getId());
			String type = "event";
			Double value = null;
			if (evt instanceof Rating) {
				Rating r = (Rating)evt;
				if (r.getPreference() != null) {
					value = r.getPreference().getValue();
					type = "rating";
				} else {
					type = "unrating";
				}
			}
			expected.addEvent(type, eid, iid, timestamp, value, _rid);
		}
		assertDtoEquals(actual, expected);
	}

	@Test
	public void testGetUserEventsRequestHandler3() throws Exception {
		request.setRequestURI(CONTEXT_PATH + "/users/23/events");
		request.setQueryString("item=256&item=1024&null=false");
		request.setMethod("GET");
		ParsedUrl parsed = ServerUtils.parseUrl(request, manager.getDefinedResources());
		RequestHandler handler = manager.getHandler(RequestMethod.GET, parsed);
		handler.handle(session, parsed, request, response);
		assertEquals(MockHttpServletResponse.SC_OK, response.getStatusCode());
		response.flushBuffer();
		DtoContainer<UserEventsDto> container = new DtoContainer<UserEventsDto>(UserEventsDto.class, false);
		contentHandler.fromString(response.getOutputStreamContent(), container);
		UserEventsDto actual = container.getSingle();
		UserEventsDto expected = new UserEventsDto("23", 2, 0);
		LongOpenHashSet items = new LongOpenHashSet(new long[]{256, 1024});
		for (Event evt : session.getUserEvents(23, items)) {
			String eid = Long.toString(evt.getId());
			String iid = Long.toString(evt.getItemId());
			long timestamp = evt.getTimestamp();
			String _rid = session.getEventRevId(evt.getId());
			String type = "event";
			Double value = null;
			if (evt instanceof Rating) {
				Rating r = (Rating)evt;
				if (r.getPreference() != null) {
					value = r.getPreference().getValue();
					type = "rating";
				} else {
					continue;
				}
			}
			expected.addEvent(type, eid, iid, timestamp, value, _rid);
		}
		assertDtoEquals(actual, expected);
	}

	@Test
	public void testGetUserMetadataRequestHandler() throws Exception {
		request.setRequestURI(CONTEXT_PATH + "/users/23");
		request.setMethod("GET");
		ParsedUrl parsed = ServerUtils.parseUrl(request, manager.getDefinedResources());
		RequestHandler handler = manager.getHandler(RequestMethod.GET, parsed);
		handler.handle(session, parsed, request, response);
		assertEquals(MockHttpServletResponse.SC_OK, response.getStatusCode());
		response.flushBuffer();
		DtoContainer<UserDto> result= new DtoContainer<UserDto>(UserDto.class, false);
		contentHandler.fromString(response.getOutputStreamContent(), result);
		UserDto actual = result.getSingle();
		UserDto expected = new UserDto("23");
		assertDtoEquals(expected, actual);
	}

	@Test
	public void testGetUserPredictionsActionHandler() throws Exception {
		request.setRequestURI(CONTEXT_PATH + "/users/23/predictions");
		request.setMethod("GET");
		ParsedUrl parsed = ServerUtils.parseUrl(request, manager.getDefinedResources());
		RequestHandler handler = manager.getHandler(RequestMethod.GET, parsed);
		handler.handle(session, parsed, request, response);
		assertEquals(MockHttpServletResponse.SC_OK, response.getStatusCode());
		response.flushBuffer();
		DtoContainer<UserPredictionsDto> result= new DtoContainer<UserPredictionsDto>(UserPredictionsDto.class, false);
		contentHandler.fromString(response.getOutputStreamContent(), result);
		UserPredictionsDto dto = result.getSingle();
		assertEquals("23", dto.user_id);
		assertEquals(new Integer(0), dto.start);
		int expectedCount = 0;
		Object2DoubleOpenHashMap<String> preds = new Object2DoubleOpenHashMap<String>();
		for (PredictionDto pred : dto.predictions){
			preds.put(pred.item, pred.value);
		}
		for (long iid : session.getUserPredictions(23).keySet()) {
			assertTrue(preds.get(Long.toString(iid)) >= 0);
			expectedCount++;
		}
		assertEquals(new Integer(expectedCount), dto.count);
	}

	@Test
	public void testGetUserPredictionsActionHandler2() throws Exception {
		request.setRequestURI(CONTEXT_PATH + "/users/23/predictions");
		request.setQueryString("item=1024&item=8192");
		request.setMethod("GET");
		ParsedUrl parsed = ServerUtils.parseUrl(request, manager.getDefinedResources());
		RequestHandler handler = manager.getHandler(RequestMethod.GET, parsed);
		handler.handle(session, parsed, request, response);
		assertEquals(MockHttpServletResponse.SC_OK, response.getStatusCode());
		response.flushBuffer();
		DtoContainer<UserPredictionsDto> result= new DtoContainer<UserPredictionsDto>(UserPredictionsDto.class, false);
		contentHandler.fromString(response.getOutputStreamContent(), result);
		UserPredictionsDto dto = result.getSingle();
		assertEquals("23", dto.user_id);
		assertEquals(new Integer(0), dto.start);
		Object2DoubleOpenHashMap<String> preds = new Object2DoubleOpenHashMap<String>();
		for (PredictionDto pred : dto.predictions){
			preds.put(pred.item, pred.value);
		}
		LongOpenHashSet items = new LongOpenHashSet(new long[]{1024, 8192});
		int expectedCount = 0;
		for (long iid : session.getUserPredictions(23, items).keySet()) {
			assertTrue(preds.get(Long.toString(iid)) >= 0);
			expectedCount++;
		}
		assertEquals(new Integer(expectedCount), dto.count);
	}

	@Test
	public void testGetUserPredictionsActionHandler3() throws Exception {
		request.setRequestURI(CONTEXT_PATH + "/users/23/predictions");
		request.setQueryString("useStoredRatings=true");
		request.setMethod("GET");
		ParsedUrl parsed = ServerUtils.parseUrl(request, manager.getDefinedResources());
		RequestHandler handler = manager.getHandler(RequestMethod.GET, parsed);
		handler.handle(session, parsed, request, response);
		assertEquals(MockHttpServletResponse.SC_OK, response.getStatusCode());
		response.flushBuffer();
		DtoContainer<UserPreferencesDto> result= new DtoContainer<UserPreferencesDto>(UserPreferencesDto.class, false);
		contentHandler.fromString(response.getOutputStreamContent(), result);
		UserPreferencesDto dto = result.getSingle();
		assertEquals("23", dto.user_id);
		assertEquals(new Integer(0), dto.start);
		assertEquals(new Integer(6), dto.count);
		Map<Long, Double> userRatings = session.getCurrentUserRatings(23);
		Map<Long, Double> userPredictions = session.getUserPredictions(23);
		for (PreferenceDto pref : dto.preferences){
			if (pref.type.equals("prediction")) {
				Double predictionValue = userPredictions.get(Long.parseLong(pref.item));
				assertNotNull(predictionValue);
				assertEquals(predictionValue, pref.value, EPSILON);
			} else {
				Double ratingValue = userRatings.get(Long.parseLong(pref.item));
				assertNotNull(ratingValue);
				assertEquals(ratingValue, pref.value, EPSILON);
			}
		}
	}

	@Test
	public void testGetUserPredictionsActionHandler4() throws Exception {
		request.setRequestURI(CONTEXT_PATH + "/users/23/predictions");
		request.setQueryString("item=8192&item=32&item=1024&item=2048&useStoredRatings=true");
		request.setMethod("GET");
		ParsedUrl parsed = ServerUtils.parseUrl(request, manager.getDefinedResources());
		RequestHandler handler = manager.getHandler(RequestMethod.GET, parsed);
		handler.handle(session, parsed, request, response);
		assertEquals(MockHttpServletResponse.SC_OK, response.getStatusCode());
		response.flushBuffer();
		DtoContainer<UserPreferencesDto> result= new DtoContainer<UserPreferencesDto>(UserPreferencesDto.class, false);
		contentHandler.fromString(response.getOutputStreamContent(), result);
		UserPreferencesDto dto = result.getSingle();
		assertEquals("23", dto.user_id);
		assertEquals(new Integer(0), dto.start);
		assertEquals(new Integer(4), dto.count);
		Object2ObjectOpenHashMap<String, PreferenceDto> prefs = new Object2ObjectOpenHashMap<String, PreferenceDto>();
		for (PreferenceDto pref : dto.preferences) {
			prefs.put(pref.item, pref);
		}
		assertTrue(prefs.get("8192").value >= 0);
		assertEquals("prediction", prefs.get("8192").type);
		assertTrue(prefs.get("2048").value >= 0);
		assertEquals("prediction", prefs.get("2048").type);
		assertEquals("rating", prefs.get("32").type);
		assertEquals(4.4, prefs.get("32").value, EPSILON);
		assertEquals("rating", prefs.get("1024").type);
		assertEquals(5.0, prefs.get("1024").value, EPSILON);
	}

	@Test
	public void testGetUserRatingsRequestHandler() throws Exception {
		request.setRequestURI(CONTEXT_PATH + "/users/23/events/ratings");
		request.setMethod("GET");
		ParsedUrl parsed = ServerUtils.parseUrl(request, manager.getDefinedResources());
		RequestHandler handler = manager.getHandler(RequestMethod.GET, parsed);
		handler.handle(session, parsed, request, response);
		assertEquals(MockHttpServletResponse.SC_OK, response.getStatusCode());
		response.flushBuffer();
		DtoContainer<UserRatingsDto> container = new DtoContainer<UserRatingsDto>(UserRatingsDto.class, false);
		contentHandler.fromString(response.getOutputStreamContent(), container);
		UserRatingsDto actual = container.getSingle();
		UserRatingsDto expected = new UserRatingsDto("23", 6, 0);
		for (Rating r : session.getUserRatings(23)) {
			String eid = Long.toString(r.getId());
			String iid = Long.toString(r.getItemId());
			long timestamp = r.getTimestamp();
			String _rid = session.getEventRevId(r.getId());
			Double value = null;
			if (r.getPreference() != null) {
				value = r.getPreference().getValue();
			}
			expected.addRating(eid, iid, timestamp, value, _rid);
		}
		assertDtoEquals(expected, actual);
	}

	@Test
	public void testGetUserRatingsRequestHandler2() throws Exception {
		request.setRequestURI(CONTEXT_PATH + "/users/23/events/ratings");
		request.setQueryString("item=256&item=1024");
		request.setMethod("GET");
		ParsedUrl parsed = ServerUtils.parseUrl(request, manager.getDefinedResources());
		RequestHandler handler = manager.getHandler(RequestMethod.GET, parsed);
		handler.handle(session, parsed, request, response);
		assertEquals(MockHttpServletResponse.SC_OK, response.getStatusCode());
		response.flushBuffer();
		DtoContainer<UserRatingsDto> container = new DtoContainer<UserRatingsDto>(UserRatingsDto.class, false);
		contentHandler.fromString(response.getOutputStreamContent(), container);
		UserRatingsDto actual = container.getSingle();
		UserRatingsDto expected = new UserRatingsDto("23", 3, 0);
		LongOpenHashSet items = new LongOpenHashSet(new long[]{256, 1024});
		for (Rating r : session.getUserRatings(23, items)) {
			String eid = Long.toString(r.getId());
			String iid = Long.toString(r.getItemId());
			long timestamp = r.getTimestamp();
			String _rid = session.getEventRevId(r.getId());
			Double value = null;
			if (r.getPreference() != null) {
				value = r.getPreference().getValue();
			}
			expected.addRating(eid, iid, timestamp, value, _rid);
		}
		assertDtoEquals(expected, actual);
	}

	@Test
	public void testGetUserRatingsRequestHandler3() throws Exception {
		request.setRequestURI(CONTEXT_PATH + "/users/23/events/ratings");
		request.setQueryString("item=256&item=1024&null=false");
		request.setMethod("GET");
		ParsedUrl parsed = ServerUtils.parseUrl(request, manager.getDefinedResources());
		RequestHandler handler = manager.getHandler(RequestMethod.GET, parsed);
		handler.handle(session, parsed, request, response);
		assertEquals(MockHttpServletResponse.SC_OK, response.getStatusCode());
		response.flushBuffer();
		DtoContainer<UserRatingsDto> container = new DtoContainer<UserRatingsDto>(UserRatingsDto.class, false);
		contentHandler.fromString(response.getOutputStreamContent(), container);
		UserRatingsDto actual = container.getSingle();
		UserRatingsDto expected = new UserRatingsDto("23", 2, 0);
		LongOpenHashSet items = new LongOpenHashSet(new long[]{256, 1024});
		for (Rating r : session.getUserRatings(23, items)) {
			String eid = Long.toString(r.getId());
			String iid = Long.toString(r.getItemId());
			long timestamp = r.getTimestamp();
			String _rid = session.getEventRevId(r.getId());
			Double value = null;
			if (r.getPreference() != null) {
				value = r.getPreference().getValue();
			} else {
				continue;
			}
			expected.addRating(eid, iid, timestamp, value, _rid);
		}
		assertDtoEquals(expected, actual);
	}

	@Test
	public void testGetUserRecommendationsHandler() throws Exception {
		request.setRequestURI(CONTEXT_PATH + "/users/938/recommendations");
		request.setMethod("GET");
		ParsedUrl parsed = ServerUtils.parseUrl(request, manager.getDefinedResources());
		RequestHandler handler = manager.getHandler(RequestMethod.GET, parsed);
		handler.handle(session, parsed, request, response);
		assertEquals(MockHttpServletResponse.SC_OK, response.getStatusCode());
		response.flushBuffer();
		DtoContainer<UserRecommendationsDto> container =
				new DtoContainer<UserRecommendationsDto>(UserRecommendationsDto.class, false);
		contentHandler.fromString(response.getOutputStreamContent(), container);
		UserRecommendationsDto actual = container.getSingle();
		UserRecommendationsDto expected = new UserRecommendationsDto("938", 5, 0);
		for (long recommendation : session.getUserRecommendations(938)) {
			expected.addRecommendation(Long.toString(recommendation));
		}
		assertDtoEquals(expected, actual);
	}

	@Test
	public void testGetUserRecommendationsHandler2() throws Exception {
		request.setRequestURI(CONTEXT_PATH + "/users/23/recommendations");
		request.setQueryString("count=2");
		request.setMethod("GET");
		ParsedUrl parsed = ServerUtils.parseUrl(request, manager.getDefinedResources());
		RequestHandler handler = manager.getHandler(RequestMethod.GET, parsed);
		handler.handle(session, parsed, request, response);
		assertEquals(MockHttpServletResponse.SC_OK, response.getStatusCode());
		response.flushBuffer();
		DtoContainer<UserRecommendationsDto> container = 
				new DtoContainer<UserRecommendationsDto>(UserRecommendationsDto.class, false);
		contentHandler.fromString(response.getOutputStreamContent(), container);
		UserRecommendationsDto actual = container.getSingle();
		assertTrue(actual.count.intValue() <= 2);
	}

	@Test
	public void testGetUserRecommendationsHandler3() throws Exception {
		request.setRequestURI(CONTEXT_PATH + "/users/23/recommendations");
		request.setQueryString("count=5");
		request.setMethod("GET");
		ParsedUrl parsed = ServerUtils.parseUrl(request, manager.getDefinedResources());
		RequestHandler handler = manager.getHandler(RequestMethod.GET, parsed);
		handler.handle(session, parsed, request, response);
		assertEquals(MockHttpServletResponse.SC_OK, response.getStatusCode());
		response.flushBuffer();
		DtoContainer<UserRecommendationsDto> container = 
				new DtoContainer<UserRecommendationsDto>(UserRecommendationsDto.class, false);
		contentHandler.fromString(response.getOutputStreamContent(), container);
		UserRecommendationsDto actual = container.getSingle();
		assertTrue(actual.count.intValue() <= 5);
	}

	@Test
	public void testGetUserStatisticsRequestHandler() throws Exception {
		request.setRequestURI(CONTEXT_PATH + "/users/23/statistics");
		request.setMethod("GET");
		ParsedUrl parsed = ServerUtils.parseUrl(request, manager.getDefinedResources());
		RequestHandler handler = manager.getHandler(RequestMethod.GET, parsed);
		handler.handle(session, parsed, request, response);
		assertEquals(MockHttpServletResponse.SC_OK, response.getStatusCode());
		response.flushBuffer();
		DtoContainer<UserStatisticsDto> container = new DtoContainer<UserStatisticsDto>(UserStatisticsDto.class, false);
		contentHandler.fromString(response.getOutputStreamContent(), container);
		UserStatisticsDto actual = container.getSingle();
		UserStatisticsDto expected = new UserStatisticsDto("23", 6, 2, 4.7);
		assertDtoEquals(expected, actual);
	}

	protected <T extends Dto> void assertDtoEquals(T expected, T actual) throws Exception {
		Assert.assertEquals(expected.getClass(), actual.getClass()); // sanity
		for (Field f: expected.getClass().getFields()) {
			Object expectedValue = f.get(expected);
			Object actualValue = f.get(actual);

			if (expectedValue == null) {
				Assert.assertNull(f.getName() + " expected to be null", actualValue);
			} else {
				Assert.assertNotNull(f.getName() + " expected to be not null", actualValue);

				if (f.getType().isArray()) {
					Object[] eva = (Object[]) expectedValue;
					Object[] ava = (Object[]) actualValue;

					if (Dto.class.isAssignableFrom(f.getType().getComponentType())) {
						// can't rely on JUnit yet, do it by hand
						Assert.assertEquals(f.getName() + " array lengths differ", eva.length, ava.length);
						for (int i = 0; i < eva.length; i++)
							assertDtoEquals((Dto) eva[i], (Dto) ava[i]);
					} else
						/* Unfortunately, this means that AssertDtoEquals is sensitive to
						 * the ordering of arrays, which isn't relevant to our DTO's.
						 */
						Assert.assertTrue(f.getName() + " simple arrays aren't equal", Arrays.equals(eva, ava));
				} else {
					// plain object or Dto
					if (Dto.class.isAssignableFrom(f.getType()))
						assertDtoEquals((Dto) expectedValue, (Dto) actualValue);
					else if (Double.class.isAssignableFrom(f.getType()))
						Assert.assertEquals(f.getName() + " values aren't equal", (Double)expectedValue, (Double)actualValue, EPSILON);
					else
						Assert.assertEquals(f.getName() + " values aren't equal", expectedValue, actualValue);
				}
			}
		}
	}
}