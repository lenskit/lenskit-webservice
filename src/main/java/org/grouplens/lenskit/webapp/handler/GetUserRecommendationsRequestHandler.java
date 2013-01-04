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

//Invoked by calling GET /users/[uid]/recommendations
public class GetUserRecommendationsRequestHandler extends RequestHandler{

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
		try {
			long id = Long.parseLong(parsed.getResourceMap().get("users"));
			SerializationFormat responseFormat = ServerUtils.determineResponseFormat(parsed, request.getHeader("Accept"));
			List<Long> recommendations = session.getUserRecommendations(id);
			int count = recommendations.size();
			List<String> countParams = parsed.getParamMap().get("count");
			if (countParams != null && !countParams.isEmpty()) {
				int countParam = Integer.parseInt(countParams.get(0));
				if (countParam < recommendations.size()) {
					count = countParam;
				}
			}
			UserRecommendationsDto dto = new UserRecommendationsDto(Long.toString(id), count, 0);
			DtoContainer<UserRecommendationsDto> container = new DtoContainer<UserRecommendationsDto>(UserRecommendationsDto.class, dto);
			
			for (int i = 0; i < count && i < recommendations.size(); i++) {
				dto.addRecommendation(Long.toString(recommendations.get(i)));
			}
			writeResponse(container, response, responseFormat);
			response.setStatus(HttpServletResponse.SC_OK);
		} catch (NumberFormatException e) {
			throw new BadRequestException("Invalid User ID or Count Parameter", e);
		}
	}
}
