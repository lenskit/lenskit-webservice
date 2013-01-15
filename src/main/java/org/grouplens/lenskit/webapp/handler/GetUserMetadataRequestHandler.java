package org.grouplens.lenskit.webapp.handler;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.grouplens.common.dto.DtoContainer;
import org.grouplens.lenskit.webapp.BadRequestException;
import org.grouplens.lenskit.webapp.ResourceNotFoundException;
import org.grouplens.lenskit.webapp.ServerUtils;
import org.grouplens.lenskit.webapp.ServerUtils.ParsedUrl;
import org.grouplens.lenskit.webapp.ServerUtils.SerializationFormat;
import org.grouplens.lenskit.webapp.Session;
import org.grouplens.lenskit.webapp.dto.UserDto;

/**
 * A {@link RequestHandler} to service requests of the form:
 * GET /users/[uid]
 */
public class GetUserMetadataRequestHandler extends RequestHandler {

	public GetUserMetadataRequestHandler() {
		super();
		addResource("users", true);
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
		if (!session.containsUser(userId)) {
			throw new ResourceNotFoundException("User " + userId + " does not exist.");
		}
		
		SerializationFormat responseFormat = ServerUtils.determineResponseFormat(parsed, request.getHeader("Accept"));
		UserDto dto = new UserDto(Long.toString(userId));
		DtoContainer<UserDto> container = new DtoContainer<UserDto>(UserDto.class, dto);
		writeResponse(container, response, responseFormat);
		response.setStatus(HttpServletResponse.SC_OK);

	}
}
