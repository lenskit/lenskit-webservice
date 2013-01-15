package org.grouplens.lenskit.webapp.handler;

import java.util.Random;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.grouplens.common.dto.DtoContainer;
import org.grouplens.common.dto.ParseException;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.event.SimpleNullRating;
import org.grouplens.lenskit.data.event.SimpleRating;
import org.grouplens.lenskit.webapp.BadRequestException;
import org.grouplens.lenskit.webapp.ConflictException;
import org.grouplens.lenskit.webapp.ResourceNotFoundException;
import org.grouplens.lenskit.webapp.ServerUtils;
import org.grouplens.lenskit.webapp.ServerUtils.ParsedUrl;
import org.grouplens.lenskit.webapp.ServerUtils.SerializationFormat;
import org.grouplens.lenskit.webapp.Session;
import org.grouplens.lenskit.webapp.dto.RatingDto;

/**
 * A {@link RequestHandler} to service requests of the form:
 * POST /users/[user_id]/events/ratings
 */
public class AddRatingRequestHandler extends RequestHandler {

	@Override
	public RequestMethod getMethod() {
		return RequestMethod.POST;
	}

	public AddRatingRequestHandler() {
		super();
		addResource("users", true);
		addResource("events", false);
		addResource("ratings", false);
	}

	@Override
	public void handle(Session session, ParsedUrl parsed, HttpServletRequest request, HttpServletResponse response) throws Exception {
		long userId;
		try {
			userId = Long.parseLong(parsed.getResourceMap().get("users"));
		} catch (NumberFormatException e) {
			throw new BadRequestException("Request contains invalid user ID");
		}
		if (!session.containsUser(userId)) {
			throw new ResourceNotFoundException("User " + userId + " does not exist");
		}
		
		SerializationFormat requestFormat = ServerUtils.determineRequestFormat(request.getContentType());
		DtoContainer<RatingDto> ratingContainer = new DtoContainer<RatingDto>(RatingDto.class);
		try {
			readRequest(ratingContainer, request, requestFormat);
		} catch (ParseException e) {
			throw new BadRequestException("Could not parse request body");
		}	
		
		RatingDto requestDto = ratingContainer.getSingle();
		if (requestDto == null) {
			throw new BadRequestException("Body must specify a valid rating");
		}
		String event_id = requestDto.event_id;
		if (event_id == null) {
			// The rating has no event ID, so we assign it a random one
			Random rand = new Random();
			do {
				event_id = Long.toString(Math.abs(rand.nextLong()));
			} while (session.containsEvent(Long.parseLong(event_id)));
		} else if (session.containsEvent(Long.parseLong(event_id))) {
			throw new ConflictException("Event " + event_id + " already exists");
		}		
		String item_id = requestDto.item_id;
		if (item_id == null) {
			throw new BadRequestException("Request Body Must Specify Valid Event");
		}
		if (requestDto.user_id == null) {
			requestDto.user_id = Long.toString(userId);
		} else if (!requestDto.user_id.equals(Long.toString(userId))) {
			throw new BadRequestException("User IDs specified in URL and request body do not match");
		}
		
		long timestamp = -1;
		if (requestDto.timestamp != null) {
			timestamp = requestDto.timestamp;
		}
		Rating r;
		if (requestDto.value == null) {
			r = new SimpleNullRating(Long.parseLong(event_id), userId, Long.parseLong(item_id), timestamp);
		} else {
			r = new SimpleRating(Long.parseLong(event_id), userId, Long.parseLong(item_id), requestDto.value, timestamp);
		}
		session.addRating(r);
		
		SerializationFormat responseFormat = ServerUtils.determineResponseFormat(parsed, request.getHeader("Accept"));
		RatingDto dto = new RatingDto(r);
		dto._revision_id = session.getEventRevId(r.getId());
		ratingContainer.set(dto);
		response.setStatus(HttpServletResponse.SC_CREATED);
		writeResponse(ratingContainer, response, responseFormat);
	}
}