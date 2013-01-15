package org.grouplens.lenskit.webapp.handler;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.grouplens.common.dto.DtoContainer;
import org.grouplens.lenskit.webapp.BadRequestException;
import org.grouplens.lenskit.webapp.ServerUtils;
import org.grouplens.lenskit.webapp.ServerUtils.ParsedUrl;
import org.grouplens.lenskit.webapp.ServerUtils.SerializationFormat;
import org.grouplens.lenskit.webapp.Session;
import org.grouplens.lenskit.webapp.dto.UserRecommendationsDto;

/**
 * A {@link RequestHandler} to service requests of the form:
 * GET /users/[uid]/recommendations
 */
public class GetUserRecommendationsRequestHandler extends RequestHandler {

	public GetUserRecommendationsRequestHandler() {
		super();
		addResource("users", true);
		addResource("recommendations", false);
	}

	@Override
	public RequestMethod getMethod() {
		return RequestMethod.GET;
	}

	@Override
	public void handle(Session session, ParsedUrl parsed, HttpServletRequest request, HttpServletResponse response) throws Exception {
		long userId;
		try {
			userId = Long.parseLong(parsed.getResourceMap().get("users"));
		} catch (NumberFormatException e) {
			throw new BadRequestException("Invalid User ID", e);
		}
		
		SerializationFormat responseFormat = ServerUtils.determineResponseFormat(parsed, request.getHeader("Accept"));
		List<Long> recommendations = session.getUserRecommendations(userId);
		
		int countParam = Integer.MAX_VALUE;
		List<String> countParams = parsed.getParamMap().get("count");
		if (countParams != null && !countParams.isEmpty()) {
			try {
				countParam = Integer.parseInt(countParams.get(0));
			} catch (NumberFormatException e) {
				throw new BadRequestException("Invalid Count Parameter");
			}
		}
		
		int count = Math.min(recommendations.size(), countParam);
		UserRecommendationsDto dto = new UserRecommendationsDto(Long.toString(userId), count, 0);
		DtoContainer<UserRecommendationsDto> container = new DtoContainer<UserRecommendationsDto>(UserRecommendationsDto.class, dto);		
		for (long recommendationId : recommendations.subList(0, count)) {
			dto.addRecommendation(Long.toString(recommendationId));
		}
		
		writeResponse(container, response, responseFormat);
		response.setStatus(HttpServletResponse.SC_OK);
	}
}
