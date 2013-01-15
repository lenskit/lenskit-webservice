package org.grouplens.lenskit.webapp.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.grouplens.common.dto.DtoContainer;
import org.grouplens.lenskit.webapp.ServerUtils;
import org.grouplens.lenskit.webapp.ServerUtils.ParsedUrl;
import org.grouplens.lenskit.webapp.BadRequestException;
import org.grouplens.lenskit.webapp.Session;
import org.grouplens.lenskit.webapp.ServerUtils.SerializationFormat;
import org.grouplens.lenskit.webapp.dto.RatingDto;

/**
 * A {@link RequestHandler} to service requests of the form:
 * GET /users/[uid]/currentRatings
 */
public class GetCurrentRatingsRequestHandler extends RequestHandler {

	public GetCurrentRatingsRequestHandler() {
		super();
		addResource("users", true);
		addResource("currentRatings", false);
	}

	@Override
	public RequestMethod getMethod() {
		return RequestMethod.GET;
	}

	@Override
	public void handle(Session session, ParsedUrl parsed, HttpServletRequest request, HttpServletResponse response) throws Exception {
		long uid;
		try {
			uid = Long.parseLong(parsed.getResourceMap().get("users"));
		} catch (NumberFormatException e) {
			throw new BadRequestException("Invalid User ID", e);
		}
		
		SerializationFormat responseFormat = ServerUtils.determineResponseFormat(parsed, request.getHeader("Accept"));
		Map<Long, Double> latestRatings = session.getCurrentUserRatings(uid);
		List<RatingDto> ratings = new ArrayList<RatingDto>();
		for (Map.Entry<Long, Double> e : latestRatings.entrySet()) {
			ratings.add(new RatingDto(e.getKey().toString(), e.getValue()));
		}

		DtoContainer<RatingDto> container = new DtoContainer<RatingDto>(RatingDto.class, ratings);
		writeResponse(container, response, responseFormat);
		response.setStatus(HttpServletResponse.SC_OK);
	}
}
