package org.grouplens.lenskit.webapp.handler;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.grouplens.common.dto.DtoContainer;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.webapp.BadRequestException;
import org.grouplens.lenskit.webapp.ServerUtils;
import org.grouplens.lenskit.webapp.ServerUtils.ParsedUrl;
import org.grouplens.lenskit.webapp.ServerUtils.SerializationFormat;
import org.grouplens.lenskit.webapp.Session;
import org.grouplens.lenskit.webapp.dto.UserRatingsDto;

/**
 * A {@link RequestHandler} to service requests of the form:
 * GET /users/[uid]/events/ratings
 */
public class GetUserRatingsRequestHandler extends RequestHandler {

	public GetUserRatingsRequestHandler() {
		super();
		addResource("users", true);
		addResource("events", false);
		addResource("ratings", false);
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
		List<String> nullSpecification = parsed.getParamMap().get("null");
		boolean excludeNull = nullSpecification != null && 
							  nullSpecification.size() == 1 && 
							  nullSpecification.get(0).equalsIgnoreCase("false");
		
		List<Rating> ratings;	
		List<String> iids = parsed.getParamMap().get("item");
		if (iids != null) {
			Set<Long> itemIds = new LongOpenHashSet();
			try {
				for (String str : iids) {
					itemIds.add(Long.parseLong(str));
				}
			} catch (NumberFormatException e) {
				throw new BadRequestException("Invalid Item ID");
			}
			ratings = session.getUserRatings(userId, itemIds);
		} else {
			ratings = session.getUserRatings(userId);
		}
		if (excludeNull) {
			ObjectArrayList<Rating> toBeRemoved = new ObjectArrayList<Rating>();
			for (Rating r : ratings) {
				if (r.getPreference() == null) {
					toBeRemoved.add(r);
				}
			}
			ratings.removeAll(toBeRemoved);
		}
		
		UserRatingsDto dto = new UserRatingsDto(Long.toString(userId), ratings.size(), 0);
		for (Rating r : ratings) {
			String eid = Long.toString(r.getId());
			String iid = Long.toString(r.getItemId());
			String _rid = session.getEventRevId(r.getId());
			long timestamp = r.getTimestamp();
			if (r.getPreference() != null) {
				dto.addRating(eid, iid, timestamp, r.getPreference().getValue(), _rid);
			}
			else if (!excludeNull) {
				dto.addRating(eid, iid, timestamp, null, _rid);
			}
		}
		DtoContainer<UserRatingsDto> container = new DtoContainer<UserRatingsDto>(UserRatingsDto.class, dto);
		writeResponse(container, response, responseFormat);
		response.setStatus(HttpServletResponse.SC_OK);
	}
}