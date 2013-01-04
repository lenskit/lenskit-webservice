package org.grouplens.lenskit.webapp;

import org.grouplens.lenskit.data.Event;
import org.grouplens.lenskit.data.dao.DataAccessObject;


public interface ServerDataAccessObject extends DataAccessObject {
	
	public void addUser(long userId);
		
	public void deleteUser(long userId);
	
	public void addItem(long itemId);
	
	public void deleteItem(long itemId);
	
	public void addEvent(Event evt);
	
	public void deleteEvent(long eventId);
	
	public String getUserRevId(long userId);
	
	public String getItemRevId(long itemId);

	public String getEventRevId(long eventId);
	
}
