package org.grouplens.lenskit.webapp;

import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.io.Closeable;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.grouplens.lenskit.collections.ScoredLongList;
import org.grouplens.lenskit.core.LenskitRecommender;
import org.grouplens.lenskit.cursors.Cursor;
import org.grouplens.lenskit.cursors.Cursors;
import org.grouplens.lenskit.data.Event;
import org.grouplens.lenskit.data.dao.DataAccessObject;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.vectors.VectorEntry;

public class Session implements Closeable {

	private LenskitRecommender recommender;
	private ServerDataAccessObject dao;
	private Long2ObjectOpenHashMap<Event> eventMap;
	private LongSet userSet;
	private LongSet itemSet;

	/**
	 * Construct a new Session to back a web application.
	 * @param recommender The {@link LenskitRecommender} that will provide
	 * item rating predictions and recommendations.
	 */
	public Session(LenskitRecommender recommender) {
		DataAccessObject recommenderDao = recommender.getRatingDataAccessObject();
		if (recommenderDao instanceof ServerDataAccessObject) {
			dao = (ServerDataAccessObject)recommender.getRatingDataAccessObject();
		} else {
			throw new RuntimeException("Recommender's DAO must be Server DAO");
		}
		
		eventMap = new Long2ObjectOpenHashMap<Event>();
		Cursor<? extends Event> eventCursor = dao.getEvents();
		try {
			for (Event e : eventCursor) {
				eventMap.put(e.getId(), e);
			}
		} finally {
			eventCursor.close();
		}		
		userSet = new LongOpenHashSet(Cursors.makeList(dao.getUsers()));
		itemSet = new LongOpenHashSet(Cursors.makeList(dao.getItems()));
		this.recommender = recommender;
	}

	/**
	 * Add a new item.
	 * @param itemId The numerical ID of the item.
	 */
	public void addItem(long itemId) {
		dao.addItem(itemId);
		itemSet.add(itemId);
	}

	/**
	 * Add a new user.
	 * @param userId The numerical ID of the user.
	 */
	public void addUser(long userId) {
		dao.addUser(userId);
		userSet.add(userId);
	}

	/**
	 * Add a new rating.
	 * @param r The rating to be added.
	 */
	public void addRating(Rating r) {
		dao.addEvent(r);
		eventMap.put(r.getId(), r);
		itemSet.add(r.getItemId());
		userSet.add(r.getUserId());
	}
	
	/**
	 * Remove an existing event.
	 * @param eventId The numerical ID of the event.
	 * @throws ResourceNotFoundException if the event does not already exist.
	 */
	public void deleteEvent(long eventId) throws ResourceNotFoundException {
		if (eventMap.get(eventId) == null) {
			throw new ResourceNotFoundException("Event " + eventId + " does not exist");
		}
		dao.deleteEvent(eventId);
		eventMap.remove(eventId);
	}

	/**
	 * Remove an existing item.
	 * @param itemId The numerical ID of the item.
	 * @throws ResourceNotFoundException if the item does not already exist.
	 */
	public void deleteItem(long itemId) throws ResourceNotFoundException {
		if (!itemSet.contains(itemId)) {
			throw new ResourceNotFoundException("Item " + itemId + " does not exist");				
		}
		dao.deleteItem(itemId);
		itemSet.remove(itemId);
	}

	/**
	 * Remove an existing user.
	 * @param userId The numerical ID of the user.
	 * @throws ResourceNotFoundException if the user does not already exist.
	 */
	public void deleteUser(long userId) throws ResourceNotFoundException {
		if (!this.userSet.contains(userId)) {
			throw new ResourceNotFoundException("User " + userId + " does not exist");
		}
		dao.deleteUser(userId);
		userSet.remove(userId);
	}

	/**
	 * Retrieve an existing event.
	 * @param eventId The numerical ID of the event.
	 * @return The event
	 * @throws ResourceNotFoundException if the event does not already exist.
	 */
	public Event getEvent(long eventId) throws ResourceNotFoundException {
		Event evt = eventMap.get(eventId);
		if (evt == null) {
			throw new ResourceNotFoundException("Event " + eventId + " does not exist");
		}
		return evt;
	}
	
	/**
	 * Retrieve an existing Rating.
	 * @param ratingId The numerical ID of the rating.
	 * @return The rating
	 * @throws ResourceNotFoundException if the rating does not already exist or
	 * if there exists an event with the specified ID that is not a rating.
	 */
	public Rating getRating(long ratingId) throws ResourceNotFoundException {
		Event evt = eventMap.get(ratingId);
		if (evt == null || ! (evt instanceof Rating)) {
			throw new ResourceNotFoundException("Rating " + ratingId + " does not exist");
		}
		return (Rating)evt;
	}

	/**
	 * Retrieve all events concerning a specific item.
	 * @param itemId The numerical ID of the item.
	 * @return A list of all events for this item.
	 * @throws ResourceNotFoundException if the item does not exist.
	 */
	public List<Event> getItemEvents(long itemId) throws ResourceNotFoundException {
		if (!itemSet.contains(itemId)) {
			throw new ResourceNotFoundException("Item " + itemId + " does not exist");
		}
		return Cursors.makeList(dao.getItemEvents(itemId));
	}

	/**
	 * Retrieve all events concerning a specific item and group of users.
	 * @param itemId The numerical ID of the item.
	 * @param users A set of numerical IDs for all users.
	 * @return A list of all events concerning the specified item and users.
	 * @throws ResourceNotFoundException if the item does not exist.
	 */
	public List<Event> getItemEvents(long itemId, Set<Long> users) throws ResourceNotFoundException {
		if (!itemSet.contains(itemId)) {
			throw new ResourceNotFoundException("Item " + itemId + " does not exist");
		}

		Cursor<? extends Event> eventCursor = dao.getItemEvents(itemId);
		try {
			List<Event> eventList = new ObjectArrayList<Event>();
			for (Event evt : eventCursor) {
				if (users.contains(evt.getUserId())) {
					eventList.add(evt);
				}
			}
			return eventList;
		} finally {
			eventCursor.close();
		}
	}

	/**
	 * Retrieve all rating events concerning a specific item.
	 * @param itemId The numerical ID of the item.
	 * @return A list of all of the item's ratings.
	 * @throws ResourceNotFoundException if the item does not exist.
	 */
	public List<Rating> getItemRatings(long itemId) throws ResourceNotFoundException {
		if (!itemSet.contains(itemId)) {
			throw new ResourceNotFoundException("Item " + itemId + " does not exist");
		}
		return Cursors.makeList(dao.getItemEvents(itemId, Rating.class));
	}

	/**
	 * Retrieve all rating events concerning a specific item and group of users.
	 * @param itemId The numerical ID of the item.
	 * @param users A set of numerical IDs for the users.
	 * @return A list of all ratings concerning the specified item and users.
	 * @throws ResourceNotFoundException
	 */
	public List<Rating> getItemRatings(long itemId, Set<Long> users) throws ResourceNotFoundException {
		if (!itemSet.contains(itemId)) { 
			throw new ResourceNotFoundException("Item " + itemId + " does not exist");
		}

		Cursor<Rating> ratingCursor = dao.getItemEvents(itemId, Rating.class);
		try {
			List<Rating> ratings = new ObjectArrayList<Rating>();
			for (Rating r : ratingCursor) {
				if (users.contains(r.getUserId())) {
					ratings.add(r);
				}
			}
			return ratings;
		} finally {
			ratingCursor.close();
		}
	}

	/**
	 * Retrieve an item's most recent valid rating value for all possible users.
	 * @param itemId The numerical ID of the item.
	 * @return A mapping from a user's numerical ID to their 
	 * most recent rating for the item.
	 * @throws ResourceNotFoundException if the item does not exist.
	 */
	public Map<Long, Double> getCurrentItemRatings(long itemId) throws ResourceNotFoundException {
		if (!itemSet.contains(itemId)) {
			throw new ResourceNotFoundException("Item " + itemId + " does not exist");
		}

		List<Rating> allRatings = Cursors.makeList(dao.getItemEvents(itemId, Rating.class));
		Collections.sort(allRatings, new TimestampComparator());
		Long2DoubleOpenHashMap latestRatings = new Long2DoubleOpenHashMap();
		for (Rating r : allRatings) {
			if (r.getPreference() == null) {
				latestRatings.remove(r.getUserId());
			} else {
				latestRatings.put(r.getUserId(), r.getPreference().getValue());
			}
		}
		return latestRatings;
	}
	
	/**
	 * Retrieve an item's most recent valid rating values
	 * from a specific group of users.
	 * @param itemId The numerical ID of the item.
	 * @param users A set of numerical IDs for the users.
	 * @return A mapping from a user's numerical ID to their
	 * most recent rating for the item.
	 * @throws ResourceNotFoundException if the item does not exist.
	 */
	public Map<Long, Double> getCurrentItemRatings(long itemId, Set<Long> users) throws ResourceNotFoundException {
		if (!this.itemSet.contains(itemId)) {
			throw new ResourceNotFoundException("Item " + itemId + " does not exist");
		}
		
		List<Rating> allRatings = Cursors.makeList(dao.getItemEvents(itemId, Rating.class));
		Collections.sort(allRatings, new TimestampComparator());
		Long2DoubleOpenHashMap latestRatings = new Long2DoubleOpenHashMap();
		for (Rating r : allRatings) {
			if (r.getPreference() == null) {
				latestRatings.remove(r.getUserId());
			}
			else if (users.contains(r.getUserId())) {
				latestRatings.put(r.getUserId(), r.getPreference().getValue());
			}
		}
		return latestRatings;
	}

	/**
	 * Retrieve all events concerning a specific user.
	 * @param userId The numerical ID for the user.
	 * @return A list of all events for this user.
	 * @throws ResourceNotFoundException if the user does not exist.
	 */
	public List<Event> getUserEvents(long userId) throws ResourceNotFoundException {
		if (!this.userSet.contains(userId)) {
			throw new ResourceNotFoundException("User " + userId + " does not exist");
		} else {
			return Cursors.makeList(dao.getUserEvents(userId));
		}

	}

	/**
	 * Retrieve all events concerning a specific user and group of items.
	 * @param userId The numerical ID for the user.
	 * @param items A set of numerical IDs for the items.
	 * @return A list of all events concerning the specified user and items.
	 * @throws ResourceNotFoundException if the user does not exist.
	 */
	public List<Event> getUserEvents(long userId, Set<Long> items) throws ResourceNotFoundException {
		if (!this.userSet.contains(userId)) {
			throw new ResourceNotFoundException("User " + userId + " does not exist");
		}

		Cursor<? extends Event> eventCursor = dao.getUserEvents(userId);
		try {
			List<Event> eventList = new ObjectArrayList<Event>();
			for (Event evt : eventCursor) {
				if (items.contains(evt.getItemId())) {
					eventList.add(evt);
				}
			}
			return eventList;
		} finally {
			eventCursor.close();
		}
	}

	/**
	 * Retrieve all rating events for the specified user.
	 * @param userId The numerical ID for the user.
	 * @return A list of all ratings for this user.
	 * @throws ResourceNotFoundException if the user does not exist.
	 */
	public List<Rating> getUserRatings(long userId) throws ResourceNotFoundException {
		if (!this.userSet.contains(userId)) {
			throw new ResourceNotFoundException("User " + userId + " does not exist");
		} else {
			return Cursors.makeList(dao.getUserEvents(userId, Rating.class));
		}
	}

	/**
	 * Retrieve all rating events for a specific user and group of items.
	 * @param userId The numerical ID for the user.
	 * @param items A set of numerical IDs for the items.
	 * @return A list of all rating events concerning the specified user and items.
	 * @throws ResourceNotFoundException if the user does not exist.
	 */
	public List<Rating> getUserRatings(long userId, Set<Long> items) throws ResourceNotFoundException {
		if (!this.userSet.contains(userId)) {
			throw new ResourceNotFoundException("User " + userId + " does not exist");
		}

		Cursor<Rating> ratingCursor = dao.getUserEvents(userId, Rating.class);
		try {
			List<Rating> ratings = new ObjectArrayList<Rating>();
			for (Rating r : ratingCursor) {
				if (items.contains(r.getItemId())) {
					ratings.add(r);
				}
			}
			return ratings;
		} finally {
			ratingCursor.close();
		}
	}

	/**
	 * Retrieve all available item rating predictions for a certain user.
	 * @param userId The numerical ID for the user.
	 * @return A mapping from an item's ID to its rating prediction for this user.
	 * @throws ResourceNotFoundException if the user does not exist.
	 */
	public Map<Long, Double> getUserPredictions(long userId) throws ResourceNotFoundException {
		if (!this.userSet.contains(userId)) {
			throw new ResourceNotFoundException("User " + userId + " does not exist");
		}

		Map<Long, Double> predictions = new Long2DoubleOpenHashMap();
		LongSet predItems = new LongOpenHashSet(itemSet);
		for (long l : getCurrentUserRatings(userId).keySet()) {
			predItems.remove(l);
		}
		for (VectorEntry e : recommender.getRatingPredictor().score(userId, predItems)) {
			predictions.put(e.getKey(), e.getValue());
		}
		return predictions;
	}

	/**
	 * Retrieve rating predictions for the specified user and
	 * concerning the specified items.
	 * @param userId The numerical ID for the user.
	 * @param items A set of numerical IDs for the items.
	 * @return A mapping from an items ID to its rating prediction for this user.
	 * @throws ResourceNotFoundException if the user does not exist.
	 */
	public Map<Long, Double> getUserPredictions(long userId, Set<Long> items) throws ResourceNotFoundException {
		if (!this.userSet.contains(userId)) {
			throw new ResourceNotFoundException("User " + userId + " does not exist");
		}

		for (long id : getCurrentUserRatings(userId).keySet()) {
			items.remove(id);
		}
		Map<Long, Double> predictions = new Long2DoubleOpenHashMap();
		for (VectorEntry e : recommender.getRatingPredictor().score(userId, items)) {
			predictions.put(e.getKey(), e.getValue());
		}
		return predictions;
	}
	
	/**
	 * Retrieve all available preferences (i.e. ratings and predicted ratings)
	 * for a specific user.
	 * @param userId The numerical ID for the user.
	 * @return A mapping from an item's ID to its rating or predicted rating.
	 * @throws ResourceNotFoundException if the user does not exist.
	 */
	public Map<Long, Double> getUserPreferences(long userId) throws ResourceNotFoundException {
		if (!this.userSet.contains(userId)) {
			throw new ResourceNotFoundException("User " + userId + " does not exist");
		}

		LongOpenHashSet predictItems = new LongOpenHashSet(itemSet);
		Map<Long, Double> ratings = getCurrentUserRatings(userId);
		for (long ratedId : ratings.keySet()) {
			predictItems.remove(ratedId);
		}
		Map<Long, Double> preferences = new Long2DoubleOpenHashMap();
		for (Map.Entry<Long, Double> e : getUserPredictions(userId, predictItems).entrySet()) {
			preferences.put(e.getKey(), e.getValue());
		}
		for (Map.Entry<Long, Double> e : ratings.entrySet()) {
			preferences.put(e.getKey(), e.getValue());
		}
		return preferences;
	}
	
	/**
	 * Retrieve all available preferences (i.e. ratings and predicted ratings)
	 * for a specific user and group of items.
	 * @param userId The numerical ID of the user.
	 * @param items A set of numerical IDs for the items.
	 * @return A mapping from item ID to rating or predicted rating.
	 * @throws ResourceNotFoundException
	 */
	public Map<Long, Double> getUserPreferences(long userId, Set<Long> items) throws ResourceNotFoundException {
		if (!this.userSet.contains(userId)) {
			throw new ResourceNotFoundException("User " + userId + " does not exist");
		}
		LongOpenHashSet predictItems = new LongOpenHashSet(items);
		Map<Long, Double> preferences = new Long2DoubleOpenHashMap();
		Map<Long, Double> ratings = getCurrentUserRatings(userId, items);
		
		for (Map.Entry<Long, Double> e : ratings.entrySet()) {
			predictItems.remove(e.getKey());
			preferences.put(e.getKey(), e.getValue());
		}
		preferences.putAll(getUserPredictions(userId, predictItems));
		return preferences;
	}

	/**
	 * Retrieve the user's most recent rating value for all possible items.
	 * @param userId  The user's numerical ID.
	 * @return A mapping from item ID to the user's rating for that item.
	 * @throws ResourceNotFoundException if the user does not exist.
	 */
	public Map<Long, Double> getCurrentUserRatings(long userId) throws ResourceNotFoundException {
		if (!this.userSet.contains(userId)) {
			throw new ResourceNotFoundException("User " + userId + " does not exist");
		}

		List<Rating> allRatings = Cursors.makeList(dao.getUserEvents(userId, Rating.class));
		Collections.sort(allRatings, new TimestampComparator());
		Long2DoubleOpenHashMap latestRatings = new Long2DoubleOpenHashMap();
		for (Rating r : allRatings) {
			if (r.getPreference() == null) {
				latestRatings.remove(r.getItemId());
			} else {
				latestRatings.put(r.getItemId(), r.getPreference().getValue());
			}
		}
		return latestRatings;
	}
	
	/**
	 * Retrieve a user's most recent rating values for a specific group of items.
	 * @param userId The numerical ID of the user.
	 * @param items A set of numerical IDs for the items.
	 * @return a mapping from an item's ID to its rating by the specified user.
	 * @throws ResourceNotFoundException if the user does not exist.
	 */
	public Map<Long, Double> getCurrentUserRatings(long userId, Set<Long> items) throws ResourceNotFoundException {
		if (!this.userSet.contains(userId)) {
			throw new ResourceNotFoundException("User " + userId + " does not exist");
		}
		
		List<Rating> allRatings = Cursors.makeList(dao.getUserEvents(userId, Rating.class));
		Collections.sort(allRatings, new TimestampComparator());
		Long2DoubleOpenHashMap latestRatings = new Long2DoubleOpenHashMap();
		for (Rating r : allRatings) {
			if (r.getPreference() == null) {
				latestRatings.remove(r.getItemId());
			}
			else if (items.contains(r.getItemId())) {
				latestRatings.put(r.getItemId(), r.getPreference().getValue());
			}
		}
		return latestRatings;
	}

	/**
	 * Retrieve all items recommended for a user.
	 * @param userId The numerical ID of the user.
	 * @return A list of numerical IDs of the items recommended for the user.
	 * @throws ResourceNotFoundException if the user does not exist.
	 */
	public List<Long> getUserRecommendations(long userId) throws ResourceNotFoundException {
		if (!this.userSet.contains(userId)) {
			throw new ResourceNotFoundException("User " + userId + " does not exist");
		}

		ScoredLongList recommendations = recommender.getItemRecommender().recommend(userId);
		Set<Long> recSet =  recommendations.scoreVector().keySet();
		return new ObjectArrayList<Long>(recSet);
	}

	/**
	 * @return The total number of users.
	 */
	public int getUserCount() {
		return userSet.size();
	}

	/**
	 * @return The total number of items.
	 */
	public int getItemCount() {
		return itemSet.size();
	}

	/**
	 * @return The total number of events.
	 */
	public int getEventCount() {
		return eventMap.size();
	}
	
	/**
	 * Retrieve a user's revision ID.
	 * @param userId The numerical ID of the user.
	 * @return The user's revision ID, as a String.
	 * @throws ResourceNotFoundException if the user does not exist.
	 */
	public String getUserRevId(long userId) throws ResourceNotFoundException {
		if (!userSet.contains(userId)) {
			throw new ResourceNotFoundException("User " + userId + " does not exist");
		}
		return dao.getUserRevId(userId);
	}

	/**
	 * Retrieve an item's revision ID.
	 * @param itemId The numerical ID of the item.
	 * @return The item's revision ID, as a String.
	 * @throws ResourceNotFoundException if the item does not exist.
	 */
	public String getItemRevId(long itemId) throws ResourceNotFoundException {
		if (!itemSet.contains(itemId)) {
			throw new ResourceNotFoundException("Item " + itemId + " does not exist");
		}
		return dao.getItemRevId(itemId);
	}

	/**
	 * Retrieve an event's revision ID.
	 * @param eventId The numerical ID of the event.
	 * @return The events revision ID, as a String.
	 * @throws ResourceNotFoundException if the event does not exist.
	 */
	public String getEventRevId(long eventId) throws ResourceNotFoundException {
		if (eventMap.get(eventId) == null) {
			throw new ResourceNotFoundException("Event " + eventId + " does not exist");
		}
		return dao.getEventRevId(eventId);
	}
	
	/**
	 * Determine if an item exists.
	 * @param The numerical ID of the item.
	 * @return <tt>true</tt> if the item exists, <tt>false</tt> otherwise.
	 */
	public boolean containsItem(long itemId) {
		return itemSet.contains(itemId);
	}
	
	/**
	 * Determine if a user exists.
	 * @param The numerical ID of the user.
	 * @return <tt>true</tt> if the user exists, <tt>false</tt> otherwise.
	 */
	public boolean containsUser(long userId) {
		return userSet.contains(userId);
	}
	
	/**
	 * Determine if an even exists.
	 * @param The numerical ID of the event.
	 * @return <tt>true</tt> if the event exists, <tt>false</tt> otherwise.
	 */
	public boolean containsEvent(long eventId) {
		return eventMap.containsKey(eventId);
	}

	/**
	 * Close the Session and its release its associated resources.
	 */
	@Override
	public void close() {
		dao.close();
		recommender.close();		
	}
	
	private class TimestampComparator implements Comparator<Event> {
		@Override
		public int compare(Event e1, Event e2) {
			if (e1.getTimestamp() < e2.getTimestamp()) {
				return -1;
			} else if (e1.getTimestamp() > e2.getTimestamp()) {
				return 1;
			}
			else {
				return 0;
			}
		}	
	}
}