package org.grouplens.lenskit.webapp.handler;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.HashSet;
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
import org.grouplens.lenskit.webapp.dto.ItemRatingsDto;

//Invoked by calling GET /items/[iid]/events/ratings
public class GetItemRatingsRequestHandler extends RequestHandler {

	public GetItemRatingsRequestHandler() {
		super();
		addResource("items", true);
		addResource("events", false);
		addResource("ratings", false);
	}
	
	@Override
	public RequestMethod getMethod() {
		return RequestMethod.GET;
	}

	@Override
	public void handle(Session session, ParsedUrl parsed, HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
			long iid = Long.parseLong(parsed.getResourceMap().get("items"));
			SerializationFormat responseFormat = ServerUtils.determineResponseFormat(parsed, request.getHeader("Accept"));
			List<String> uids = parsed.getParamMap().get("user");
			List<String> nullSpecification = parsed.getParamMap().get("null");
			boolean excludeNull = ((nullSpecification != null) && (nullSpecification.size() == 1) && 
					(nullSpecification.get(0).equalsIgnoreCase("false")));
			List<Rating> ratings;
			
			if (uids != null) {
				Set<Long> userIds = new HashSet<Long>();
				for (String str : uids) {
					userIds.add(Long.parseLong(str));
				}
				ratings = session.getItemRatings(iid, userIds);
			} else {
				ratings = session.getItemRatings(iid);
			}
			if (excludeNull) {
				List<Rating> toBeRemoved = new ObjectArrayList<Rating>();
				for (Rating r : ratings) {
					if (r.getPreference() == null) {
						toBeRemoved.add(r);
					}
				}
				ratings.removeAll(toBeRemoved);
			}
			ItemRatingsDto dto = new ItemRatingsDto(Long.toString(iid), ratings.size(), 0);
			for (Rating r : ratings) {
				String eid = Long.toString(r.getId());
				String uid = Long.toString(r.getUserId());
				String _rid = session.getEventRevId(r.getId());
				long timestamp = r.getTimestamp();
				if (r.getPreference() != null) {
					dto.addRating(eid, uid, timestamp, r.getPreference().getValue(), _rid);
				} else if (!excludeNull) {
					dto.addRating(eid, uid, timestamp, null, _rid);
				}
			}
			DtoContainer<ItemRatingsDto> container = new DtoContainer<ItemRatingsDto>(ItemRatingsDto.class, dto);
			writeResponse(container, response, responseFormat);
			response.setStatus(HttpServletResponse.SC_OK);
		} catch (NumberFormatException e) {
			throw new BadRequestException("Invalid Item ID", e);
		}
	}
}