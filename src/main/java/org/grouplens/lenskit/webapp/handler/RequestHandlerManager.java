package org.grouplens.lenskit.webapp.handler;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import java.util.EnumMap;
import java.util.Set;

import org.grouplens.lenskit.webapp.ServerUtils.ParsedUrl;
import org.grouplens.lenskit.webapp.handler.RequestHandler.RequestMethod;

public class RequestHandlerManager {

	private final EnumMap<RequestMethod, ObjectOpenHashSet<RequestHandler>> handlers;
	
	private ObjectOpenHashSet<String> definedResources;

	public RequestHandlerManager() {
		definedResources = new ObjectOpenHashSet<String>();
		handlers = new EnumMap<RequestMethod, ObjectOpenHashSet<RequestHandler>>(RequestMethod.class);
		for (RequestMethod m : RequestMethod.values()) {
			handlers.put(m, new ObjectOpenHashSet<RequestHandler>());
		}
	}

	public void addHandler(RequestHandler handler) {
		if (handler == null) {
			throw new NullPointerException("Handler Cannot be Null");
		}
		handlers.get(handler.getMethod()).add(handler);
		definedResources.addAll(handler.getResourceList());
	}

	public void removeHandler(RequestHandler handler) {
		if (handler == null) {
			throw new NullPointerException("Handler Cannot be Null");
		}
		handlers.get(handler.getMethod()).remove(handler);
	}

	public RequestHandler getHandler(RequestMethod method, ParsedUrl parsed) {
		for (RequestHandler handler : handlers.get(method)) {
			if (handler.isCorrectHandler(parsed.getResourceMap())) {
				return handler;
			}
		}
		return null;
	}
	
	public Set<String> getDefinedResources() {
		return definedResources;
	}
}
