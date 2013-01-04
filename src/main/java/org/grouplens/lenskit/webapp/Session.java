package org.grouplens.lenskit.webapp;

import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
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

public class Session implements Closeable {

	private LenskitRecommender recommender;
	private ServerDataAccessObject dao;
	private LongSet eventSet;
	private LongSet userSet;
	private LongSet itemSet;

	public Session(LenskitRecommender recommender) {
		DataAccessObject recommenderDao = recommender.getRatingDataAccessObject();
		//FIXME This is really incomplete error handling
		if (recommenderDao instanceof ServerDataAccessObject)
			dao = (ServerDataAccessObject)recommender.getRatingDataAccessObject();
		else
			throw new RuntimeException("Recommender's DAO must be Server DAO");
		
		eventSet = new LongOpenHashSet();
		Cursor<? extends Event> eventCursor = dao.getEvents();
		try {
			for (Event evt : eventCursor) {
				eventSet.add(evt.getId());
			}
		} finally {
			eventCursor.close();
		}
		userSet = new LongOpenHashSet(Cursors.makeList(dao.getUsers()));
		itemSet = new LongOpenHashSet(Cursors.makeList(dao.getItems()));
		this.recommender = recommender;
	}

	public void addItem(long itemId) {
		dao.addItem(itemId);
		itemSet.add(itemId);
	}

	public void addUser(long userId) {
		dao.addUser(userId);
		userSet.add(userId);
	}

	public void addRating(Rating r) {
		dao.addEvent(r);
		eventSet.add(r.getId());
		itemSet.add(r.getItemId());
		userSet.add(r.getUserId());
	}
	
	public void deleteEvent(long eventId) throws ResourceNotFoundException {
		if (!eventSet.contains(eventId))
			throw new ResourceNotFoundException("Event " + eventId + " does not exist");
		dao.deleteEvent(eventId);
		eventSet.remove(eventId);
	}

	public void deleteItem(long itemId) throws ResourceNotFoundException {
		if (!itemSet.contains(itemId)) 
			throw new ResourceNotFoundException("Item " + itemId + " does not exist");
		dao.deleteItem(itemId);
		itemSet.remove(itemId);
	}

	public void deleteUser(long userId) throws ResourceNotFoundException {
		if (!this.userSet.contains(userId))
			throw new ResourceNotFoundException("User " +userId + " does not exist");
		dao.deleteUser(userId);
		userSet.remove(userId);
	}

	public Event getEvent(long eventId) throws ResourceNotFoundException {
		if (!this.eventSet.contains(eventId))
			throw new ResourceNotFoundException("Event " + eventId + " does not exist");
		Cursor<? extends Event> eventCursor = dao.getEvents();
		Event returnEvt = null;
		for (Event evt : eventCursor) {
			if (evt.getId() == eventId) returnEvt = evt;
		}
		eventCursor.close();
		if (returnEvt == null) throw new ResourceNotFoundException("Event " + eventId + " does not exist");
		else return returnEvt;
	}
	
	public Rating getRating(long ratingId) throws ResourceNotFoundException {
		if (!this.eventSet.contains(ratingId))
			throw new ResourceNotFoundException("Rating " + ratingId + " does not exist");
		Cursor<Rating> ratingCursor = dao.getEvents(Rating.class);
		Rating returnRating = null;
		for (Rating r : ratingCursor) {
			if (r.getId() == ratingId) returnRating = r;
		}
		if (returnRating == null) throw new ResourceNotFoundException("Rating " + ratingId + " does not exist");
		else return returnRating;
	}

	public List<Event> getItemEvents(long itemId) throws ResourceNotFoundException {
		if (!itemSet.contains(itemId)) {
			throw new ResourceNotFoundException("Item " + itemId + " does not exist");
		} else {
			return Cursors.makeList(dao.getItemEvents(itemId));
		}
	}

	public List<Event> getItemEvents(long itemId, Set<Long> users) throws ResourceNotFoundException {
		if (!itemSet.contains(itemId))
			throw new ResourceNotFoundException("Item " + itemId + " does not exist");

		Cursor<? extends Event> eventCursor = dao.getItemEvents(itemId);
		try
		{
			List<Event> eventList = new ObjectArrayList<Event>();
			for (Event evt : eventCursor) {
				if (users.contains(evt.getUserId())) eventList.add(evt);
			}
			return eventList;
		} finally {
			eventCursor.close();
		}
	}

	public List<Rating> getItemRatings(long itemId) throws ResourceNotFoundException {
		if (!itemSet.contains(itemId)) {
			throw new ResourceNotFoundException("Item " + itemId + " does not exist");
		} else {
			return Cursors.makeList(dao.getItemEvents(itemId, Rating.class));
		}
	}

	public List<Rating> getItemRatings(long itemId, Set<Long> users) throws ResourceNotFoundException {
		if (!itemSet.contains(itemId)) 
			throw new ResourceNotFoundException("Item " + itemId + " does not exist");

		Cursor<Rating> ratingCursor = dao.getItemEvents(itemId, Rating.class);
		try {
			List<Rating> ratings = new ObjectArrayList<Rating>();
			for (Rating r : ratingCursor) {
				if (users.contains(r.getUserId())) ratings.add(r);
			}
			return ratings;
		} finally {
			ratingCursor.close();
		}
	}

	public Map<Long, Double> getCurrentItemRatings(long itemId) throws ResourceNotFoundException {
		if (!itemSet.contains(itemId))
			throw new ResourceNotFoundException("Item " + itemId + " does not exist");

		List<Rating> allRatings = Cursors.makeList(dao.getItemEvents(itemId, Rating.class));
		Collections.sort(allRatings, new TimestampComparator());
		Long2DoubleOpenHashMap latestRatings = new Long2DoubleOpenHashMap();
		for (Rating r : allRatings) {
			if (r.getPreference() == null) latestRatings.remove(r.getUserId());
			else latestRatings.put(r.getUserId(), r.getPreference().getValue());
		}
		return latestRatings;
	}
	
	public Map<Long, Double> getCurrentItemRatings(long itemId, Set<Long> users) throws ResourceNotFoundException {
		if (!this.itemSet.contains(itemId))
			throw new ResourceNotFoundException("Item " + itemId + " does not exist");
		
		List<Rating> allRatings = Cursors.makeList(dao.getItemEvents(itemId, Rating.class));
		Collections.sort(allRatings, new TimestampComparator());
		Long2DoubleOpenHashMap latestRatings = new Long2DoubleOpenHashMap();
		for (Rating r : allRatings) {
			if (r.getPreference() == null) latestRatings.remove(r.getUserId());
			else if (users.contains(r.getUserId())) latestRatings.put(r.getUserId(), r.getPreference().getValue());
		}
		return latestRatings;
	}

	public List<Event> getUserEvents(long userId) throws ResourceNotFoundException {
		if (!this.userSet.contains(userId)) {
			throw new ResourceNotFoundException("User " + userId + " does not exist");
		} else {
			return Cursors.makeList(dao.getUserEvents(userId));
		}

	}

	public List<Event> getUserEvents(long userId, Set<Long> items) throws ResourceNotFoundException {
		if (!this.userSet.contains(userId))
			throw new ResourceNotFoundException("User " + userId + " does not exist");

		Cursor<? extends Event> eventCursor = dao.getUserEvents(userId);
		try {
			List<Event> eventList = new ObjectArrayList<Event>();
			for (Event evt : eventCursor) {
				if (items.contains(evt.getItemId())) eventList.add(evt);
			}
			return eventList;
		} finally {
			eventCursor.close();
		}
	}

	public List<Rating> getUserRatings(long userId) throws ResourceNotFoundException {
		if (!this.userSet.contains(userId)) {
			throw new ResourceNotFoundException("User " + userId + " does not exist");
		} else {
			return Cursors.makeList(dao.getUserEvents(userId, Rating.class));
		}
	}

	public List<Rating> getUserRatings(long userId, Set<Long> items) throws ResourceNotFoundException {
		if (!this.userSet.contains(userId)) 
			throw new ResourceNotFoundException("User " + userId + " does not exist");

		Cursor<Rating> ratingCursor = dao.getUserEvents(userId, Rating.class);
		try {
			List<Rating> ratings = new ObjectArrayList<Rating>();
			for (Rating r : ratingCursor) {
				if (items.contains(r.getItemId())) ratings.add(r);
			}
			return ratings;
		} finally {
			ratingCursor.close();
		}
	}

	public Map<Long, Double> getUserPredictions(long userId) throws ResourceNotFoundException {
		if (!this.userSet.contains(userId))
			throw new ResourceNotFoundException("User " + userId + " does not exist");

		Map<Long, Double> predictions = new Long2DoubleOpenHashMap();
		LongSet predItems = new LongOpenHashSet(this.itemSet);
		for (long l : getCurrentUserRatings(userId).keySet()) {
			predItems.remove(l);
		}
		for (Map.Entry<Long, Double> e : recommender.getRatingPredictor().score(userId, predItems)) {
			predictions.put(e.getKey(), e.getValue());
		}
		return predictions;
	}

	public Map<Long, Double> getUserPredictions(long userId, Set<Long> items) throws ResourceNotFoundException {
		if (!this.userSet.contains(userId))
			throw new ResourceNotFoundException("User " + userId + " does not exist");

		for (long id : getCurrentUserRatings(userId).keySet()) {
			items.remove(id);
		}
		Map<Long, Double> predictions = new Long2DoubleOpenHashMap();
		for (Map.Entry<Long, Double> e : recommender.getRatingPredictor().score(userId, items)) {
			predictions.put(e.getKey(), e.getValue());
		}
		return predictions;
	}
	
	public Map<Long, Double> getUserPreferences(long userId) throws ResourceNotFoundException {
		if (!this.userSet.contains(userId))
			throw new ResourceNotFoundException("User " + userId + " does not exist");

		LongOpenHashSet predictItems = new LongOpenHashSet(this.itemSet);
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
	
	public Map<Long, Double> getUserPreferences(long userId, Set<Long> items) throws ResourceNotFoundException {
		if (!this.userSet.contains(userId))
			throw new ResourceNotFoundException("User " + userId + " does not exist");
		LongOpenHashSet itemsCopy = new LongOpenHashSet(items);
		Map<Long, Double> preferences = new Long2DoubleOpenHashMap();
		Map<Long, Double> ratings = getCurrentUserRatings(userId, items);
		for (Map.Entry<Long, Double> e : ratings.entrySet()) {
			itemsCopy.remove(e.getKey());
			preferences.put(e.getKey(), e.getValue());
		}
		for (Map.Entry<Long, Double> e : getUserPredictions(userId, itemsCopy).entrySet()) {
			preferences.put(e.getKey(), e.getValue());
		}
		return preferences;
	}

	public Map<Long, Double> getCurrentUserRatings(long userId) throws ResourceNotFoundException {
		if (!this.userSet.contains(userId))
			throw new ResourceNotFoundException("User " + userId + " does not exist");

		List<Rating> allRatings = Cursors.makeList(dao.getUserEvents(userId, Rating.class));
		Collections.sort(allRatings, new TimestampComparator());
		Long2DoubleOpenHashMap latestRatings = new Long2DoubleOpenHashMap();
		for (Rating r : allRatings) {
			if (r.getPreference() == null) latestRatings.remove(r.getItemId());
			else latestRatings.put(r.getItemId(), r.getPreference().getValue());
		}
		return latestRatings;
	}
	
	public Map<Long, Double> getCurrentUserRatings(long userId, Set<Long> items) throws ResourceNotFoundException {
		if (!this.userSet.contains(userId))
			throw new ResourceNotFoundException("User " + userId + " does not exist");
		
		List<Rating> allRatings = Cursors.makeList(dao.getUserEvents(userId, Rating.class));
		Collections.sort(allRatings, new TimestampComparator());
		Long2DoubleOpenHashMap latestRatings = new Long2DoubleOpenHashMap();
		for (Rating r : allRatings) {
			if (r.getPreference() == null) latestRatings.remove(r.getItemId());
			else if (items.contains(r.getItemId())) latestRatings.put(r.getItemId(), r.getPreference().getValue());
		}
		return latestRatings;
	}

	public List<Long> getUserRecommendations(long userId) throws ResourceNotFoundException {
		if (!this.userSet.contains(userId))
			throw new ResourceNotFoundException("User " + userId + " does not exist");

		ScoredLongList recommendations = recommender.getItemRecommender().recommend(userId);
		Set<Long> recSet =  recommendations.scoreVector().keySet();
		return new ObjectArrayList<Long>(recSet);
	}

	public int getUserCount() {
		return userSet.size();
	}

	public int getItemCount() {
		return itemSet.size();
	}

	public int getEventCount() {
		return eventSet.size();
	}
	
	public String getUserRevId(long userId) throws ResourceNotFoundException {
		if (!userSet.contains(userId))
			throw new ResourceNotFoundException("User " + userId + " does not exist");
		return dao.getUserRevId(userId);
	}

	public String getItemRevId(long itemId) throws ResourceNotFoundException {
		if (!itemSet.contains(itemId))
			throw new ResourceNotFoundException("Item " + itemId + " does not exist");
		return dao.getItemRevId(itemId);
	}

	public String getEventRevId(long eventId) throws ResourceNotFoundException {
		if (!eventSet.contains(eventId))
			throw new ResourceNotFoundException("Event " + eventId + " does not exist");
		return dao.getEventRevId(eventId);
	}
	
	public boolean containsItem(long itemId) {
		return itemSet.contains(itemId);
	}
	
	public boolean containsUser(long userId) {
		return userSet.contains(userId);
	}
	
	public boolean containsEvent(long eventId) {
		return eventSet.contains(eventId);
	}

	@Override
	public void close() {
		dao.close();
		recommender.close();		
	}
	
	private class TimestampComparator implements Comparator<Event> {

		@Override
		public int compare(Event e1, Event e2) {
			if (e1.getTimestamp() < e2.getTimestamp()) return -1;
			if (e1.getTimestamp() > e2.getTimestamp()) return 1;
			else return 0;
		}	
	}
}