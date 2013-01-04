package org.grouplens.lenskit.webapp.handler;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.grouplens.common.dto.DtoContainer;
import org.grouplens.lenskit.webapp.BadRequestException;
import org.grouplens.lenskit.webapp.ServerUtils;
import org.grouplens.lenskit.webapp.ServerUtils.ParsedUrl;
import org.grouplens.lenskit.webapp.ServerUtils.SerializationFormat;
import org.grouplens.lenskit.webapp.Session;
import org.grouplens.lenskit.webapp.dto.UserStatisticsDto;

//Invoked by calling GET /users/[uid]/statistics
public class GetUserStatisticsRequestHandler extends RequestHandler {

	public GetUserStatisticsRequestHandler() {
		super();
		addResource("users", true);
		addResource("statistics", false);
	}

	@Override
	public RequestMethod getMethod() {
		return RequestMethod.GET;
	}

	@Override
	public void handle(Session session, ParsedUrl parsed, HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
			long userId = Long.parseLong(parsed.getResourceMap().get("users"));
			SerializationFormat responseFormat = ServerUtils.determineResponseFormat(parsed, request.getHeader("Accept"));
			int eventCount = session.getUserEvents(userId).size();
			int nRatings = 0;
			double ratingTotal = 0;
			Map<Long, Double> latestRatings = session.getCurrentUserRatings(userId);
			for (double ratingVal : latestRatings.values()) {
				ratingTotal += ratingVal;
				nRatings++;
			}
			double averageRating = 0;
			if (nRatings != 0) averageRating = ratingTotal/nRatings;
			UserStatisticsDto dto = new UserStatisticsDto(Long.toString(userId), eventCount, nRatings, averageRating);
			DtoContainer<UserStatisticsDto> container = new DtoContainer<UserStatisticsDto>(UserStatisticsDto.class, dto);
			writeResponse(container, response, responseFormat);
			response.setStatus(HttpServletResponse.SC_OK);
		} catch (NumberFormatException e) {
			throw new BadRequestException("Invalid User ID", e);
		}
	}
}