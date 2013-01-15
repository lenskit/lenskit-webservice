package org.grouplens.lenskit.webapp.handler;


import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.grouplens.lenskit.webapp.BadRequestException;
import org.grouplens.lenskit.webapp.ServerUtils.ParsedUrl;
import org.grouplens.lenskit.webapp.Session;
import org.grouplens.lenskit.webapp.UnauthorizedModificationException;

/**
 * A {@link RequestHandler} to service requests of the form:
 * DELETE /events/[eid]
 */
public class DeleteRatingRequestHandler extends RequestHandler {

	public DeleteRatingRequestHandler() {
		super();
		addResource("events", true);
	}

	@Override
	public RequestMethod getMethod() {
		return RequestMethod.DELETE;
	}

	@Override
	public void handle(Session session, ParsedUrl parsed, HttpServletRequest request, HttpServletResponse response) throws Exception {
		long eventId;
		try {
			eventId = Long.parseLong(parsed.getResourceMap().get("events"));
		} catch (NumberFormatException e) {
			throw new BadRequestException("Invalid Event ID in URL", e);
		}
		
		List<String> revIds = parsed.getParamMap().get("revId");
		if (revIds.size() > 1) {
			throw new BadRequestException("Multiple Revision IDs Provided for one Event");
		}
		String revId = revIds.get(0);
		if (!session.getEventRevId(eventId).equals(revId)) {
			throw new UnauthorizedModificationException("Incorrect Revision ID Provided for Event " + eventId);
		}
		session.deleteEvent(eventId);
		response.setStatus(HttpServletResponse.SC_NO_CONTENT);
	}
}