package org.grouplens.lenskit.webapp.handler;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.grouplens.common.dto.DtoContainer;
import org.grouplens.lenskit.webapp.ServerUtils;
import org.grouplens.lenskit.webapp.ServerUtils.ParsedUrl;
import org.grouplens.lenskit.webapp.ServerUtils.SerializationFormat;
import org.grouplens.lenskit.webapp.BadRequestException;
import org.grouplens.lenskit.webapp.Session;
import org.grouplens.lenskit.webapp.dto.UserPredictionsDto;
import org.grouplens.lenskit.webapp.dto.UserPreferencesDto;

/**
 * A {@link RequestHandler} to service requests of the form:
 * GET /users/[uid]/predictions
 */
public class GetUserPredictionsRequestHandler extends RequestHandler {

	public GetUserPredictionsRequestHandler() {
		super();
		addResource("users", true);
		addResource("predictions", false);
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
			throw new BadRequestException("Invalid User or Item ID in URL", e);
		}
		
		SerializationFormat responseFormat = ServerUtils.determineResponseFormat(parsed, request.getHeader("Accept"));
		List<String> itemIds = parsed.getParamMap().get("item");
		LongOpenHashSet specifiedItems = new LongOpenHashSet();
		if (itemIds != null) {
			for (String id : itemIds) {
				specifiedItems.add(Long.parseLong(id));
			}
		}			
		
		List<String> useStored = parsed.getParamMap().get("useStoredRatings");		
		if (useStored != null && useStored.size() == 1 && useStored.get(0).equalsIgnoreCase("true")) {
			Map<Long, Double> ratings;
			Map<Long, Double> predictions;
			
			if (!specifiedItems.isEmpty()) {
				ratings = session.getCurrentUserRatings(userId, specifiedItems);
				specifiedItems.removeAll(ratings.keySet());
				predictions = session.getUserPredictions(userId, specifiedItems);					
			} else {
				ratings = session.getCurrentUserRatings(userId);
				predictions = session.getUserPredictions(userId);
			}
			
			UserPreferencesDto dto = new UserPreferencesDto(Long.toString(userId),
					ratings.size() + predictions.size(), 0);
			for (Map.Entry<Long, Double> r : ratings.entrySet()) {
				dto.addPreference(Long.toString(r.getKey()), "rating", r.getValue());
			}

			for (Map.Entry<Long, Double> p : predictions.entrySet()) {
				dto.addPreference(Long.toString(p.getKey()), "prediction", p.getValue());
			}
			DtoContainer<UserPreferencesDto> responseContainer =
					new DtoContainer<UserPreferencesDto>(UserPreferencesDto.class, dto);
			writeResponse(responseContainer, response, responseFormat);
		} else {
			Map<Long, Double> predictions;
			if (specifiedItems.isEmpty()) {
				predictions = session.getUserPredictions(userId);
			} else {
				predictions = session.getUserPredictions(userId, specifiedItems);
			}
			UserPredictionsDto dto = new UserPredictionsDto(Long.toString(userId), predictions.size(), 0);
			for (Map.Entry<Long, Double> p : predictions.entrySet()) {
				dto.addPrediction(Long.toString(p.getKey()), p.getValue());					
			}
			DtoContainer<UserPredictionsDto> responseContainer =
					new DtoContainer<UserPredictionsDto>(UserPredictionsDto.class, dto);
			writeResponse(responseContainer, response, responseFormat);
		}
		
		response.setStatus(HttpServletResponse.SC_OK);
	}
}