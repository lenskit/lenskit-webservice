package org.grouplens.lenskit.webapp.handler;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.grouplens.common.dto.DtoContainer;
import org.grouplens.lenskit.data.Event;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.webapp.BadRequestException;
import org.grouplens.lenskit.webapp.ServerUtils.ParsedUrl;
import org.grouplens.lenskit.webapp.ServerUtils.SerializationFormat;
import org.grouplens.lenskit.webapp.ServerUtils;
import org.grouplens.lenskit.webapp.Session;
import org.grouplens.lenskit.webapp.dto.UserEventsDto;

//Invoked by calling GET /users/[uid]/events
public class GetUserEventsRequestHandler extends RequestHandler {

	public GetUserEventsRequestHandler() {
		super();
		addResource("users", true);
		addResource("events", false);
	}

	@Override
	public RequestMethod getMethod() {
		return RequestMethod.GET;
	}

	@Override
	public void handle(Session session, ParsedUrl parsed, HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
			long uid = Long.parseLong(parsed.getResourceMap().get("users"));
			SerializationFormat responseFormat = ServerUtils.determineResponseFormat(parsed, request.getHeader("Accept"));
			List<Event> events;
			boolean excludeNull = (parsed.getParamMap().get("null") != null && parsed.getParamMap().get("null").size() == 1
										&& parsed.getParamMap().get("null").get(0).equalsIgnoreCase("false"));
			List<String> iids = parsed.getParamMap().get("item");
			if (iids != null) {
				Set<Long> itemIds = new LongOpenHashSet();
				for (String str : iids) {
					itemIds.add(Long.parseLong(str));
				}
				events = session.getUserEvents(uid, itemIds);
			} else {
				events = session.getUserEvents(uid);
			}
			if (excludeNull) {
				ObjectArrayList<Event> toBeRemoved = new ObjectArrayList<Event>();
				for (Event evt : events) {
					if (evt instanceof Rating) {
						Rating r = (Rating)evt;
						if (r.getPreference() == null)
							toBeRemoved.add(evt);
					}
				}
				events.removeAll(toBeRemoved);
			}
			UserEventsDto userEvents = new UserEventsDto(Long.toString(uid), events.size(), 0);
			for (Event evt : events) {
				String eid = Long.toString(evt.getId());
				String iid = Long.toString(evt.getItemId());
				String _rid = session.getEventRevId(evt.getId());
				long timestamp = evt.getTimestamp();
				if (evt instanceof Rating) {
					Rating r = (Rating)evt;
					if (r.getPreference() != null) {
						userEvents.addEvent("rating", eid, iid, timestamp, r.getPreference().getValue(), _rid);
					}
					else if (!excludeNull) {
						userEvents.addEvent("unrating", eid, iid, timestamp, _rid);
					}
				} else {
					userEvents.addEvent("event", eid, iid, timestamp, _rid);
				}
			}
			DtoContainer<UserEventsDto> container = new DtoContainer<UserEventsDto>(UserEventsDto.class, userEvents);
			writeResponse(container, response, responseFormat);
			response.setStatus(HttpServletResponse.SC_OK);
		} catch(NumberFormatException e) {
			throw new BadRequestException("Invalid Event ID", e);
		}
	}
}