package org.grouplens.lenskit.webapp.handler;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.grouplens.common.dto.DtoContainer;
import org.grouplens.lenskit.webapp.ServerUtils;
import org.grouplens.lenskit.webapp.ServerUtils.ParsedUrl;
import org.grouplens.lenskit.webapp.ServerUtils.SerializationFormat;
import org.grouplens.lenskit.webapp.dto.SystemStatisticsDto;
import org.grouplens.lenskit.webapp.Session;

/**
 * A {@link RequestHandler} to service requests of the form
 * GET /statistics
 */
public class GetSystemStatisticsRequestHandler extends RequestHandler {

	public GetSystemStatisticsRequestHandler() {
		super();
		addResource("statistics", false);
	}
	
	@Override
	public RequestMethod getMethod() {
		return RequestMethod.GET;
	}

	@Override
	public void handle(Session session, ParsedUrl parsed, HttpServletRequest request, HttpServletResponse response) throws Exception {
		SerializationFormat responseFormat = ServerUtils.determineResponseFormat(parsed, request.getHeader("Accept"));
		int userCount = session.getUserCount();
		int itemCount = session.getItemCount();
		int eventCount = session.getEventCount();
		SystemStatisticsDto dto = new SystemStatisticsDto(userCount, itemCount, eventCount);
		DtoContainer<SystemStatisticsDto> container = new DtoContainer<SystemStatisticsDto>(SystemStatisticsDto.class, dto);
		writeResponse(container, response, responseFormat);
		response.setStatus(HttpServletResponse.SC_OK);
	}
}
