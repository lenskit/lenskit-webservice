package org.grouplens.lenskit.webapp.handler;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.grouplens.common.dto.DtoContainer;
import org.grouplens.lenskit.webapp.BadRequestException;
import org.grouplens.lenskit.webapp.ServerUtils;
import org.grouplens.lenskit.webapp.ServerUtils.ParsedUrl;
import org.grouplens.lenskit.webapp.ServerUtils.SerializationFormat;
import org.grouplens.lenskit.webapp.ResourceNotFoundException;
import org.grouplens.lenskit.webapp.Session;
import org.grouplens.lenskit.webapp.dto.ItemDto;

/**
 * A {@link RequestHandler} to service requests of the form:
 * GET /items/[iid]
 */
public class GetItemMetadataRequestHandler extends RequestHandler {

	public GetItemMetadataRequestHandler() {
		super();
		addResource("items", true);
	}
	
	@Override
	public RequestMethod getMethod() {
		return RequestMethod.GET;
	}

	@Override
	public void handle(Session session, ParsedUrl parsed, HttpServletRequest request, HttpServletResponse response) throws Exception {
		long itemId;
		try {
			itemId = Long.parseLong(parsed.getResourceMap().get("items"));
		} catch (NumberFormatException e) {
			throw new BadRequestException("Invalid Item ID", e);
		}
		if (!session.containsItem(itemId)) {
			throw new ResourceNotFoundException("Item " + itemId + " does not exist");
		}
		
		SerializationFormat responseFormat = ServerUtils.determineResponseFormat(parsed, request.getHeader("Accept"));
		ItemDto dto = new ItemDto(Long.toString(itemId));
		DtoContainer<ItemDto> container = new DtoContainer<ItemDto>(ItemDto.class, dto);
		writeResponse(container, response, responseFormat);
		response.setStatus(HttpServletResponse.SC_OK);
	}
}
