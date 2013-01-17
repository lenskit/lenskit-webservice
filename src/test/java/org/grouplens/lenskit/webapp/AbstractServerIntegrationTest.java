package org.grouplens.lenskit.webapp;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.servlet.http.HttpServletResponse;

import org.grouplens.common.dto.DtoContainer;
import org.grouplens.common.dto.DtoContentHandler;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.event.SimpleNullRating;
import org.grouplens.lenskit.data.event.SimpleRating;
import org.grouplens.lenskit.webapp.dto.RatingDto;
import org.junit.Test;

public abstract class AbstractServerIntegrationTest {

	private static final String URL_BASE = "http://localhost:8081/lenskit";
	protected String acceptHeader;
	protected String contentType;
	protected DtoContentHandler contentHandler;

	@Test
	public void testAddNullRating() throws IOException {
		//Need a "random" eid and timestamp to avoid conflicts with other integration tests
		Rating nullRating = new SimpleNullRating(System.currentTimeMillis(), 23, 35, 89000);
		RatingDto dto = new RatingDto(nullRating);
		DtoContainer<RatingDto> container = new DtoContainer<RatingDto>(RatingDto.class, dto);
		String requestBody = contentHandler.toString(container);
		testHttpRequest(URL_BASE + "/users/23/events/ratings", acceptHeader, "POST",
				requestBody, contentType, HttpServletResponse.SC_CREATED);
	}

	@Test
	public void testAddRating() throws IOException {
		//Need a "random" eid and timestamp to avoid conflicts with other integration tests
		Rating rating = new SimpleRating(System.currentTimeMillis() + 90, 938, 35, 4.0, 96000);
		RatingDto dto = new RatingDto(rating);
		DtoContainer<RatingDto> container = new DtoContainer<RatingDto>(RatingDto.class, dto);
		String requestBody = contentHandler.toString(container);
		testHttpRequest(URL_BASE + "/users/938/events/ratings", acceptHeader, "POST",
				requestBody, contentType, HttpServletResponse.SC_CREATED);
	}

	@Test
	public void testAddRatingConflict() throws IOException {
		Rating rating = new SimpleRating(5, 23, 256, 4.0, 21000);
		RatingDto dto = new RatingDto(rating);
		DtoContainer<RatingDto> container = new DtoContainer<RatingDto>(RatingDto.class, dto);
		String requestBody = contentHandler.toString(container);
		//As this rating already exists, adding it will cause a conflict
		testHttpRequest(URL_BASE + "/users/23/events/ratings", acceptHeader, "POST",
				requestBody, contentType, HttpServletResponse.SC_CONFLICT);
	}

	@Test
	public void testAddRatingInvalidContent() throws IOException {
		String requestBody = "gfajklaf"; //Garbage Text
		testHttpRequest(URL_BASE + "/users/491/events.ratings", acceptHeader, "POST",
				requestBody, contentType, HttpServletResponse.SC_BAD_REQUEST);
	}

	@Test
	public void testAddRatingUserNotFound() throws IOException {
		Rating rating = new SimpleRating(210, 31, 35, 4.0, 71000);
		RatingDto dto = new RatingDto(rating);
		DtoContainer<RatingDto> container = new DtoContainer<RatingDto>(RatingDto.class, dto);
		String requestBody = contentHandler.toString(container);
		//User 31 has not been created, so this user cannot be found
		testHttpRequest(URL_BASE + "/users/31/events/ratings", acceptHeader, "POST",
				requestBody, contentType, HttpServletResponse.SC_NOT_FOUND);
	}

	@Test
	public void testAddRatingIdsNonMatching() throws IOException {
		Rating rating = new SimpleRating(130, 491, 35, 4.0, 44000);
		RatingDto dto = new RatingDto(rating);
		DtoContainer<RatingDto> container = new DtoContainer<RatingDto>(RatingDto.class, dto);
		String requestBody = contentHandler.toString(container);
		//Request body specifies user 491, but URL specifies user 23
		testHttpRequest(URL_BASE + "/users/23/events/ratings", acceptHeader, "POST",
				requestBody, contentType, HttpServletResponse.SC_BAD_REQUEST);
	}

	@Test
	public void testDeleteRating() throws IOException {
		//Necessary to keep the two tests from clashing with one another, since they can't be isolated
		if (this instanceof XmlServerIntegrationTest) {
			testHttpRequest(URL_BASE + "/events/95?revId=1048416b-b6b9-4007-8457-8dae0a139d5e", 
					acceptHeader, "DELETE", HttpServletResponse.SC_NO_CONTENT);
		}
		else if (this instanceof JsonServerIntegrationTest) {
			testHttpRequest(URL_BASE + "/events/85?revId=377bc200-8780-4baa-a921-ddd990a6075b",
					acceptHeader, "DELETE", HttpServletResponse.SC_NO_CONTENT);
		}
	}

	@Test
	public void testDeleteRatingNotFound() throws IOException {
		testHttpRequest(URL_BASE + "/events/6?revId=4b297694-bb35-42d5-80f3-ff1d89ad20dc",
				acceptHeader, "DELETE", HttpServletResponse.SC_NOT_FOUND);
	}

	@Test
	public void testDeleteRatingInvalidRevId() throws IOException {
		testHttpRequest(URL_BASE + "/events/5?revId=4b297694-bb35-42d5-80f3-ff1d89ad20dd",
				acceptHeader, "DELETE", HttpServletResponse.SC_FORBIDDEN);
	}

	@Test
	public void testGetCurrentRatings() throws IOException {
		testHttpRequest(URL_BASE + "/users/23/currentRatings",
				acceptHeader, "GET",HttpServletResponse.SC_OK);
	}

	@Test
	public void testGetCurrentRatingsNotFound() throws IOException {
		testHttpRequest(URL_BASE + "/users/42/currentRatings",
				acceptHeader, "GET", HttpServletResponse.SC_NOT_FOUND);
	}

	@Test
	public void testGetEvent() throws IOException {
		testHttpRequest(URL_BASE + "/events/80", acceptHeader,
				"GET", HttpServletResponse.SC_OK);
	}

	@Test
	public void testGetEventNotFound() throws IOException {
		testHttpRequest(URL_BASE + "/events/10000", acceptHeader,
				"GET", HttpServletResponse.SC_NOT_FOUND);
	}

	@Test
	public void testGetItemEvents() throws IOException {
		testHttpRequest(URL_BASE + "/items/256/events", acceptHeader,
				"GET", HttpServletResponse.SC_OK);
	}

	@Test
	public void testGetItemEvents2() throws IOException {
		testHttpRequest(URL_BASE + "/items/256/events?user=23", acceptHeader,
				"GET", HttpServletResponse.SC_OK);
	}

	@Test
	public void testGetItemEvents3() throws IOException {
		testHttpRequest(URL_BASE + "/items/256/events?null=false",
				acceptHeader, "GET", HttpServletResponse.SC_OK);
	}

	@Test
	public void testGetItemEvents4() throws IOException {
		testHttpRequest(URL_BASE + "/items/256/events?user=23&null=false",
				acceptHeader, "GET", HttpServletResponse.SC_OK);
	}

	@Test
	public void testGetItemEventsNotFound() throws IOException {
		testHttpRequest(URL_BASE + "/items/257/events", acceptHeader,
				"GET", HttpServletResponse.SC_NOT_FOUND);
	}

	@Test
	public void testGetItemMetadata() throws IOException {
		testHttpRequest(URL_BASE + "/items/256", acceptHeader,
				"GET", HttpServletResponse.SC_OK);
	}

	@Test
	public void testGetItemMetadataNotFound() throws IOException {
		testHttpRequest(URL_BASE + "/items/255", acceptHeader,
				"GET", HttpServletResponse.SC_NOT_FOUND);
	}

	@Test
	public void testGetItemRatings() throws IOException {
		testHttpRequest(URL_BASE + "/items/256/events/ratings",
				acceptHeader, "GET", HttpServletResponse.SC_OK);
	}

	@Test
	public void testGetItemRatings2() throws IOException {
		testHttpRequest(URL_BASE + "/items/256/events/ratings?user=23",
				acceptHeader, "GET", HttpServletResponse.SC_OK);
	}

	@Test
	public void testGetItemRatings3() throws IOException {
		testHttpRequest(URL_BASE + "/items/256/events/ratings?null=false",
				acceptHeader, "GET", HttpServletResponse.SC_OK);
	}

	@Test
	public void testGetItemRatings4() throws IOException {
		testHttpRequest(URL_BASE + "/items/256/events/ratings?user=23&null=false",
				acceptHeader, "GET", HttpServletResponse.SC_OK);
	}

	@Test
	public void testGetItemRatingsNotFound() throws IOException {
		testHttpRequest(URL_BASE + "/items/257/events/ratings",
				acceptHeader, "GET", HttpServletResponse.SC_NOT_FOUND);
	}

	@Test
	public void testGetItemStatistics() throws IOException {
		testHttpRequest(URL_BASE + "/items/1024/statistics",
				acceptHeader, "GET", HttpServletResponse.SC_OK);
	}

	@Test
	public void testGetItemStatisticsNotFound() throws IOException {
		testHttpRequest(URL_BASE + "/items/1025/statistics",
				acceptHeader, "GET", HttpServletResponse.SC_NOT_FOUND);
	}

	@Test
	public void testGetSystemStatistics() throws IOException {
		testHttpRequest(URL_BASE + "/statistics", acceptHeader,
				"GET", HttpServletResponse.SC_OK);
	}

	@Test
	public void testGetUserEvents() throws IOException {
		testHttpRequest(URL_BASE + "/users/735/events",
				acceptHeader, "GET", HttpServletResponse.SC_OK);
	}

	@Test
	public void testGetUserEvents2() throws IOException {
		testHttpRequest(URL_BASE + "/users/735/events?item=256",
				acceptHeader, "GET", HttpServletResponse.SC_OK);
	}

	@Test
	public void testGetUserEvents3() throws IOException {
		testHttpRequest(URL_BASE + "/users/735/events?null=false",
				acceptHeader, "GET", HttpServletResponse.SC_OK);
	}

	@Test
	public void testGetUserEvents4() throws IOException {
		testHttpRequest(URL_BASE + "/users/23/events?item=256&null=false",
				acceptHeader, "GET", HttpServletResponse.SC_OK);
	}

	@Test
	public void testGetUserEventsNotFound() throws IOException {
		testHttpRequest(URL_BASE + "/users/1492/events",
				acceptHeader, "GET", HttpServletResponse.SC_NOT_FOUND);
	}

	@Test
	public void testGetUserMetadata() throws Exception {
		testHttpRequest(URL_BASE + "/users/23",
				acceptHeader, "GET", HttpServletResponse.SC_OK);
	}

	@Test
	public void testGetUserMetadataNotFound() throws Exception {
		testHttpRequest(URL_BASE + "/users/25",
				acceptHeader, "GET", HttpServletResponse.SC_NOT_FOUND);
	}

	@Test
	public void testGetUserPredictions() throws IOException {
		testHttpRequest(URL_BASE + "/users/23/predictions",
				acceptHeader, "GET", HttpServletResponse.SC_OK);
	}

	@Test
	public void testGetUserPredictions2() throws IOException {
		testHttpRequest(URL_BASE + "/users/735/predictions?item=1024",
				acceptHeader, "GET", HttpServletResponse.SC_OK);
	}

	@Test
	public void testGetUserPredictions3() throws IOException {
		testHttpRequest(URL_BASE + "/users/735/predictions?useStoredRatings=true",
				acceptHeader, "GET", HttpServletResponse.SC_OK);
	}

	@Test
	public void testGetUserPredictions4() throws IOException {
		testHttpRequest(URL_BASE + "/users/23/predictions?item=2048&useStoredRatings=true",
				acceptHeader, "GET", HttpServletResponse.SC_OK);
	}

	@Test
	public void testGetUserPredictionsNotFound() throws IOException {
		testHttpRequest(URL_BASE + "/users/1492/predictions",
				acceptHeader, "GET", HttpServletResponse.SC_NOT_FOUND);
	}

	@Test
	public void testGetUserRatings() throws IOException {
		testHttpRequest(URL_BASE + "/users/735/events/ratings",
				acceptHeader, "GET", HttpServletResponse.SC_OK);
	}

	@Test
	public void testGetUserRatings2() throws IOException {
		testHttpRequest(URL_BASE + "/users/735/events/ratings?item=256",
				acceptHeader, "GET", HttpServletResponse.SC_OK);
	}

	@Test
	public void testGetUserRatings3() throws IOException {
		testHttpRequest(URL_BASE + "/users/735/events/ratings?null=false",
				acceptHeader, "GET", HttpServletResponse.SC_OK);
	}

	@Test
	public void testGetUserRatings4() throws IOException {
		testHttpRequest(URL_BASE + "/users/23/events/ratings?item=256&null=false",
				acceptHeader, "GET", HttpServletResponse.SC_OK);
	}

	@Test
	public void testGetUserRatingsNotFound() throws IOException {
		testHttpRequest(URL_BASE + "/users/24/events/ratings", acceptHeader,
				"GET", HttpServletResponse.SC_NOT_FOUND);
	}

	@Test
	public void testGetUserRecommendations() throws IOException {
		testHttpRequest(URL_BASE + "/users/23/recommendations", acceptHeader,
				"GET", HttpServletResponse.SC_OK);
	}

	@Test
	public void testGetUserRecommendations2() throws IOException {
		testHttpRequest(URL_BASE + "/users/23/recommendations?count=2",
				acceptHeader, "GET", HttpServletResponse.SC_OK);
	}

	@Test
	public void testGetUserRecommendationsNotFound() throws IOException {
		testHttpRequest(URL_BASE + "/users/24/recommendations",
				acceptHeader, "GET", HttpServletResponse.SC_NOT_FOUND);
	}

	@Test
	public void testGetUserStatistics() throws IOException {
		testHttpRequest(URL_BASE + "/users/23/statistics",
				acceptHeader, "GET", HttpServletResponse.SC_OK);
	}

	@Test
	public void testGetUserStatisticsNotFound() throws IOException {
		testHttpRequest(URL_BASE + "/users/24/statistics",
				acceptHeader, "GET", HttpServletResponse.SC_NOT_FOUND);
	}

	@Test
	public void testInvalidUrl() throws IOException {
		//should be /users/23/events/ratings
		testHttpRequest(URL_BASE + "/users/23/ratings",
				acceptHeader, "GET", HttpServletResponse.SC_BAD_REQUEST);
	}

	@Test
	public void testInvalidAcceptHeader() throws IOException {
		testHttpRequest(URL_BASE + "/users/23/events/ratings", "text/plain",
				"GET", HttpServletResponse.SC_NOT_ACCEPTABLE);
	}

	@Test
	public void testInvalidContentType() throws IOException {
		Rating rating = new SimpleRating(215, 23, 8192, 4.0, 79000);
		RatingDto dto = new RatingDto(rating);
		DtoContainer<RatingDto> container = new DtoContainer<RatingDto>(RatingDto.class, dto);
		String requestBody = contentHandler.toString(container);
		testHttpRequest(URL_BASE + "/users/23/events/ratings", acceptHeader, "POST",
				requestBody, "text/plain", HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);

	}

	@Test
	public void testInvalidId() throws IOException {
		testHttpRequest(URL_BASE + "/users/abc", acceptHeader, "GET", HttpServletResponse.SC_BAD_REQUEST);
	}

	private void testHttpRequest(String requestUrl, String acceptHeader, String requestMethod, int statusCode) throws IOException {
		URL url = new URL(requestUrl);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestProperty("Accept", acceptHeader);
		conn.setRequestMethod(requestMethod);
		int responseStatus = conn.getResponseCode();
		assertEquals(statusCode, responseStatus);
	}

	private void testHttpRequest(String requestUrl, String acceptHeader, String requestMethod, String requestBody, String contentType, int statusCode) throws IOException {
		URL url = new URL(requestUrl);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod(requestMethod);
		conn.setRequestProperty("Accept", acceptHeader);
		conn.setRequestProperty("Content-Type", contentType);
		conn.setDoOutput(true);
		OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
		writer.write(requestBody);
		writer.close();
		int responseStatus = conn.getResponseCode();
		assertEquals(statusCode, responseStatus);
	}
}