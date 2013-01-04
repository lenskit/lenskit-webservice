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
import org.grouplens.lenskit.webapp.ServerUtils;
import org.grouplens.lenskit.webapp.ServerUtils.ParsedUrl;
import org.grouplens.lenskit.webapp.ServerUtils.SerializationFormat;
import org.grouplens.lenskit.webapp.Session;
import org.grouplens.lenskit.webapp.dto.ItemEventsDto;

//Invoked by calling GET /items/[iid]/events
public class GetItemEventsRequestHandler extends RequestHandler {

	public GetItemEventsRequestHandler() {
		super();
		addResource("items", true);
		addResource("events", false);
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
			List<Event> events;
			boolean excludeNull = (parsed.getParamMap().get("null") != null && parsed.getParamMap().get("null").size() == 1
										&& parsed.getParamMap().get("null").get(0).equalsIgnoreCase("false"));
			List<String> uids = parsed.getParamMap().get("user");
			if (uids != null) {
				Set<Long> userIds = new LongOpenHashSet();
				for (String str : uids) {
					userIds.add(Long.parseLong(str));
				}
				events = session.getItemEvents(iid, userIds);
			} else {
				events = session.getItemEvents(iid);
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
			ItemEventsDto itemEvents = new ItemEventsDto(Long.toString(iid), events.size(), 0);
			for (Event evt : events) {
				String eid = Long.toString(evt.getId());
				String uid = Long.toString(evt.getUserId());
				String _rid = session.getEventRevId(evt.getId());
				long timestamp = evt.getTimestamp();
				if (evt instanceof Rating) {
					Rating r = (Rating)evt;
					if (r.getPreference() != null) {
						itemEvents.addEvent("rating", eid, uid, timestamp, r.getPreference().getValue(), _rid);
					}
					else if (!excludeNull) {
						itemEvents.addEvent("unrating", eid, uid, timestamp, _rid);
					}
				} else {
					itemEvents.addEvent("event", eid, uid, timestamp, _rid);
				}
			}
			DtoContainer<ItemEventsDto> container = new DtoContainer<ItemEventsDto>(ItemEventsDto.class, itemEvents);
			writeResponse(container, response, responseFormat);
			response.setStatus(HttpServletResponse.SC_OK);
		} catch(NumberFormatException e) {
			throw new BadRequestException("Invalid Event ID", e);
		}
	}
}