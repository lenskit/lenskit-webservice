package org.grouplens.lenskit.webapp.handler;


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
import org.grouplens.lenskit.webapp.dto.EventDto;

//Invoked by calling GET /events/[eid]
public class GetEventRequestHandler extends RequestHandler {

	public GetEventRequestHandler() {
		super();
		addResource("events", true);
	}

	@Override
	public RequestMethod getMethod() {
		return RequestMethod.GET;
	}

	@Override
	public void handle(Session session, ParsedUrl parsed, HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
			long eid = Long.parseLong(parsed.getResourceMap().get("events"));
			SerializationFormat responseFormat = ServerUtils.determineResponseFormat(parsed, request.getHeader("Accept"));
			Event evt = session.getEvent(eid);
			EventDto dto;
			String eventId = Long.toString(evt.getId());
			String userId = Long.toString(evt.getUserId());
			String itemId = Long.toString(evt.getItemId());
			String _rev_id = session.getEventRevId(evt.getId());
			long timestamp = evt.getTimestamp();
			if (evt instanceof Rating) {
				Rating r = (Rating)evt;
				double value = -1.0;
				if (r.getPreference() != null) value = r.getPreference().getValue();
				dto = new EventDto("rating", eventId, userId, itemId, timestamp, value, _rev_id);
			}
			else {
				dto = new EventDto("event", eventId, userId, itemId, timestamp, _rev_id);
			}
			DtoContainer<EventDto> container = new DtoContainer<EventDto>(EventDto.class, dto);
			writeResponse(container, response, responseFormat);
			response.setStatus(HttpServletResponse.SC_OK);
		} catch (NumberFormatException e) {
			throw new BadRequestException("Invalid Event ID", e);
		}
	}
}