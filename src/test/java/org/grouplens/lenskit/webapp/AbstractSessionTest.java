package org.grouplens.lenskit.webapp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.grouplens.lenskit.data.Event;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.event.SimpleNullRating;
import org.grouplens.lenskit.data.event.SimpleRating;
import org.junit.After;
import org.junit.Test;

public abstract class AbstractSessionTest {

	protected Session session;
	public static final double EPSILON = 1.0e-6;

	@After
	public void cleanUp() {
		session.close();
	}

	@Test
	public void testAddNullRating() throws ResourceNotFoundException {
		SimpleNullRating r = new SimpleNullRating(1, 2, 3, 4000);
		session.addRating(r);
		Rating result = session.getRating(1);
		assertEquals(1, result.getId());
		assertEquals(2, result.getUserId());
		assertEquals(3, result.getItemId());
		assertNull(result.getPreference());
		assertEquals(4000, result.getTimestamp());
		assertNotNull(session.getEventRevId(result.getId()));
	}

	@Test 
	public void testAddRating() throws ResourceNotFoundException {
		SimpleRating r = new SimpleRating(1, 2, 3, 4.0, 5000);
		session.addRating(r);
		Rating result = session.getRating(1);
		assertEquals(1, result.getId());
		assertEquals(2, result.getUserId());
		assertEquals(3, result.getItemId());
		assertEquals(4.0, result.getPreference().getValue(), EPSILON);
		assertEquals(5000, result.getTimestamp());
		assertNotNull(session.getEventRevId(result.getId()));
	}
	
	@Test
	public void testDeleteRating() throws ResourceNotFoundException {
		assertTrue(session.containsEvent(85));
		session.deleteEvent(85);
		assertFalse(session.containsEvent(85));
	}
	
	@Test
	public void testDeleteRatingFailure() {
		try {
			session.deleteEvent(1);
			//Should throw ResourceNotFoundException
			fail();
		} catch (ResourceNotFoundException e) {}
	}

	@Test
	public void testGetEventSuccess() throws ResourceNotFoundException {
		Event evt = session.getEvent(5);
		assertEquals(5, evt.getId());
		assertEquals(23, evt.getUserId());
		assertEquals(256, evt.getItemId());
		assertEquals(20000, evt.getTimestamp());

		evt = session.getEvent(10);
		assertEquals(10, evt.getId());
		assertEquals(735, evt.getUserId());
		assertEquals(256, evt.getItemId());
		assertEquals(48000, evt.getTimestamp());

		evt = session.getEvent(15);
		assertEquals(15, evt.getId());
		assertEquals(23, evt.getUserId());
		assertEquals(32, evt.getItemId());
		assertEquals(32000, evt.getTimestamp());

		evt = session.getEvent(25);
		assertEquals(25, evt.getId());
		assertEquals(491, evt.getUserId());
		assertEquals(2048, evt.getItemId());
		assertEquals(14000, evt.getTimestamp());

		evt = session.getEvent(35);
		assertEquals(35, evt.getId());
		assertEquals(306, evt.getUserId());
		assertEquals(256, evt.getItemId());
		assertEquals(23000, evt.getTimestamp());

		evt = session.getEvent(70);
		assertEquals(70, evt.getId());
		assertEquals(938, evt.getUserId());
		assertEquals(512, evt.getItemId());
		assertEquals(82000, evt.getTimestamp());

		evt = session.getEvent(75);
		assertEquals(75, evt.getId());
		assertEquals(23, evt.getUserId());
		assertEquals(1024, evt.getItemId());
		assertEquals(93000, evt.getTimestamp());

		evt = session.getEvent(95);
		assertEquals(95, evt.getId());
		assertEquals(23, evt.getUserId());
		assertEquals(8192, evt.getItemId());
		assertEquals(37000, evt.getTimestamp());

		evt = session.getEvent(45);
		assertEquals(45, evt.getId());
		assertEquals(837, evt.getUserId());
		assertEquals(256, evt.getItemId());
		assertEquals(83000, evt.getTimestamp());

		evt = session.getEvent(40);
		assertEquals(40, evt.getId());
		assertEquals(294, evt.getUserId());
		assertEquals(256, evt.getItemId());
		assertEquals(90000, evt.getTimestamp());

		evt = session.getEvent(85);
		assertEquals(85, evt.getId());
		assertEquals(23, evt.getUserId());
		assertEquals(256, evt.getItemId());
		assertEquals(130000, evt.getTimestamp());

		evt = session.getEvent(80);
		assertEquals(80, evt.getId());
		assertEquals(23, evt.getUserId());
		assertEquals(8192, evt.getItemId());
		assertEquals(150000, evt.getTimestamp());			
	}

	@Test
	public void getEventFailure() {
		try {
			session.getEvent(105);
			//ResourceNotFoundException should be thrown
			fail();
		} catch (ResourceNotFoundException e) {}
	}


	@Test
	public void testGetRatingSuccess() throws ResourceNotFoundException {
		Rating r = session.getRating(5);
		assertEquals(5, r.getId());
		assertEquals(23, r.getUserId());
		assertEquals(256, r.getItemId());
		assertEquals(20000, r.getTimestamp());
		assertEquals(4.0, r.getPreference().getValue(), EPSILON);

		r = session.getRating(10);
		assertEquals(10, r.getId());
		assertEquals(735, r.getUserId());
		assertEquals(256, r.getItemId());
		assertEquals(48000, r.getTimestamp());
		assertEquals(3.5, r.getPreference().getValue(), EPSILON);

		r = session.getRating(15);
		assertEquals(15, r.getId());
		assertEquals(23, r.getUserId());
		assertEquals(32, r.getItemId());
		assertEquals(32000, r.getTimestamp());
		assertEquals(4.4, r.getPreference().getValue(), EPSILON);

		r = session.getRating(25);
		assertEquals(25, r.getId());
		assertEquals(491, r.getUserId());
		assertEquals(2048, r.getItemId());
		assertEquals(14000, r.getTimestamp());
		assertEquals(5.0, r.getPreference().getValue(), EPSILON);

		r = session.getRating(35);
		assertEquals(35, r.getId());
		assertEquals(306, r.getUserId());
		assertEquals(256, r.getItemId());
		assertEquals(23000, r.getTimestamp());
		assertEquals(3.8, r.getPreference().getValue(), EPSILON);

		r = session.getRating(70);
		assertEquals(70, r.getId());
		assertEquals(938, r.getUserId());
		assertEquals(512, r.getItemId());
		assertEquals(82000, r.getTimestamp());
		assertEquals(4.7, r.getPreference().getValue(), EPSILON);

		r = session.getRating(75);
		assertEquals(75, r.getId());
		assertEquals(23, r.getUserId());
		assertEquals(1024, r.getItemId());
		assertEquals(93000, r.getTimestamp());
		assertEquals(5.0, r.getPreference().getValue(), EPSILON);


		r = session.getRating(95);
		assertEquals(95, r.getId());
		assertEquals(23, r.getUserId());
		assertEquals(8192, r.getItemId());
		assertEquals(37000, r.getTimestamp());
		assertEquals(3.8, r.getPreference().getValue(), EPSILON);

		r = session.getRating(45);
		assertEquals(45, r.getId());
		assertEquals(837, r.getUserId());
		assertEquals(256, r.getItemId());
		assertEquals(83000, r.getTimestamp());
		assertEquals(2.9, r.getPreference().getValue(), EPSILON);

		r = session.getRating(40);
		assertEquals(40, r.getId());
		assertEquals(294, r.getUserId());
		assertEquals(256, r.getItemId());
		assertEquals(90000, r.getTimestamp());
		assertEquals(1.5, r.getPreference().getValue(), EPSILON);

		r = session.getRating(85);
		assertEquals(85, r.getId());
		assertEquals(23, r.getUserId());
		assertEquals(256, r.getItemId());
		assertEquals(130000, r.getTimestamp());
		assertNull(r.getPreference());

		r = session.getRating(80);
		assertEquals(80, r.getId());
		assertEquals(23, r.getUserId());
		assertEquals(8192, r.getItemId());
		assertEquals(150000, r.getTimestamp());
		assertNull(r.getPreference());
	}

	@Test
	public void testGetRatingFailure() {
		try {
			session.getRating(1019);
			//ResourceNotFoundException should be thrown
			fail();
		} catch (ResourceNotFoundException e) {}
	}

	@Test
	public void testGetItemEventsSucess() throws ResourceNotFoundException {
		List<Event> itemEvents = session.getItemEvents(256);
		assertEquals(6, itemEvents.size());
		LongArrayList idList = new LongArrayList(6);
		for (Event evt : itemEvents) {
			idList.add(evt.getId());
		}
		assertEquals(6, idList.size());
		assertTrue(idList.contains(5));
		assertTrue(idList.contains(10));
		assertTrue(idList.contains(35));
		assertTrue(idList.contains(45));
		assertTrue(idList.contains(40));
		assertTrue(idList.contains(85));
	}

	@Test
	public void testGetItemEventsFailure() {
		try {
			session.getItemEvents(65535);
			//ResourceNotFoundExcpeption should be thrown
			fail();
		} catch (ResourceNotFoundException e) {}
	}

	@Test
	public void testGetSpecificItemEventsSuccess() throws ResourceNotFoundException {
		LongOpenHashSet desiredUsers = new LongOpenHashSet();
		desiredUsers.add(23);
		desiredUsers.add(294);
		desiredUsers.add(735);
		List<Event> itemEvents = session.getItemEvents(256, desiredUsers);
		LongArrayList eventIds = new LongArrayList();
		for (Event evt : itemEvents) {
			eventIds.add(evt.getId());
		}
		assertEquals(4, eventIds.size());
		assertTrue(eventIds.contains(5));
		assertTrue(eventIds.contains(10));
		assertTrue(eventIds.contains(40));
		assertTrue(eventIds.contains(85));
	}

	@Test
	public void testGetSpecificItemEventsEmpty() throws ResourceNotFoundException {
		LongOpenHashSet desiredUsers = new LongOpenHashSet();
		desiredUsers.add(31);
		desiredUsers.add(90);
		desiredUsers.add(43);
		List<Event> itemEvents = session.getItemEvents(256, desiredUsers);
		assertTrue(itemEvents.isEmpty());
	}

	@Test
	public void testGetSpecificItemEventsFailure() {
		try {
			LongOpenHashSet desiredUsers = new LongOpenHashSet();
			desiredUsers.add(23);
			desiredUsers.add(294);
			desiredUsers.add(735);
			session.getItemEvents(32768, desiredUsers);
			//ResourceNotFoundException should be thrown
			fail();
		} catch (ResourceNotFoundException e) {}
	}

	@Test
	public void testGetItemRatingsSuccess() throws ResourceNotFoundException {
		List<Rating> itemRatings = session.getItemRatings(256);
		assertEquals(6, itemRatings.size());
		LongArrayList eventIds = new LongArrayList(6);
		for (Rating r: itemRatings) {
			eventIds.add(r.getId());
		}
		assertEquals(6, eventIds.size());
		assertTrue(eventIds.contains(5));
		assertTrue(eventIds.contains(10));
		assertTrue(eventIds.contains(35));
		assertTrue(eventIds.contains(45));
		assertTrue(eventIds.contains(40));
		assertTrue(eventIds.contains(85));
	}

	@Test
	public void testGetItemRatingsFailure() {
		try {
			session.getItemRatings(65535);
			//ResourceNotFoundException should be thrown
			fail();
		} catch (ResourceNotFoundException e) {}
	}

	@Test
	public void testGetSpecificItemRatingsSuccess() throws ResourceNotFoundException {
		LongOpenHashSet desiredUsers = new LongOpenHashSet();
		desiredUsers.add(23);
		desiredUsers.add(294);
		desiredUsers.add(735);
		List<Rating> itemRatings = session.getItemRatings(256, desiredUsers);
		LongArrayList eventIds = new LongArrayList();
		for (Rating r : itemRatings) {
			eventIds.add(r.getId());
		}
		assertEquals(4, eventIds.size());
		assertTrue(eventIds.contains(5));
		assertTrue(eventIds.contains(40));
		assertTrue(eventIds.contains(10));
		assertTrue(eventIds.contains(85));
	}

	@Test
	public void testGetSpecificItemRatingsEmpty() throws ResourceNotFoundException {
		LongOpenHashSet desiredUsers = new LongOpenHashSet();
		desiredUsers.add(31);
		desiredUsers.add(90);
		desiredUsers.add(43);
		List<Rating> itemRatings = session.getItemRatings(256, desiredUsers);
		assertTrue(itemRatings.isEmpty());
	}

	@Test
	public void testGetSpecificItemRatingsFailure() {
		try {
			LongOpenHashSet desiredUsers = new LongOpenHashSet();
			desiredUsers.add(23);
			desiredUsers.add(294);
			desiredUsers.add(735);
			session.getItemRatings(32768, desiredUsers);
			//ResourceNotFoundException should be thrown
			fail();
		} catch (ResourceNotFoundException e) {}
	}

	@Test
	public void testGetUserEventsSuccess() throws ResourceNotFoundException {
		List<Event> userEvents = session.getUserEvents(23);
		assertEquals(6, userEvents.size());
		LongArrayList idList = new LongArrayList(6);
		for (Event evt : userEvents) {
			idList.add(evt.getId());
		}
		assertEquals(6, idList.size());
		assertTrue(idList.contains(5));
		assertTrue(idList.contains(15));
		assertTrue(idList.contains(75));
		assertTrue(idList.contains(95));
		assertTrue(idList.contains(85));
		assertTrue(idList.contains(80));
	}

	@Test
	public void testGetUserEventsFailure() {
		try {
			session.getUserEvents(108);
			//ResourceNotFoundExcpeption should be thrown
			fail();
		} catch (ResourceNotFoundException e) {}
	}

	@Test
	public void testGetSpecificUserEventsSuccess() throws ResourceNotFoundException {
		LongOpenHashSet desiredItems = new LongOpenHashSet();
		desiredItems.add(256);
		desiredItems.add(1024);
		desiredItems.add(8192);
		List<Event> userEvents = session.getUserEvents(23, desiredItems);
		LongArrayList eventIds = new LongArrayList();
		for (Event evt : userEvents) {
			eventIds.add(evt.getId());
		}
		assertEquals(5, eventIds.size());
		assertTrue(eventIds.contains(5));
		assertTrue(eventIds.contains(75));
		assertTrue(eventIds.contains(95));
		assertTrue(eventIds.contains(85));
		assertTrue(eventIds.contains(80));
	}

	@Test
	public void testGetSpecificUserEventsEmpty() throws ResourceNotFoundException {
		LongOpenHashSet desiredItems = new LongOpenHashSet();
		desiredItems.add(32768);
		desiredItems.add(65535);
		desiredItems.add(2);
		List<Event> userEvents = session.getUserEvents(23, desiredItems);
		assertTrue(userEvents.isEmpty());
	}

	@Test
	public void testGetSpecificUserEventsFailure() {
		try {
			LongOpenHashSet desiredItems = new LongOpenHashSet();
			desiredItems.add(256);
			desiredItems.add(1024);
			desiredItems.add(8192);
			session.getUserEvents(108, desiredItems);
			//ResourceNotFoundException should be thrown
			fail();
		} catch (ResourceNotFoundException e) {}
	}

	@Test
	public void testGetUserRatingsSuccess() throws ResourceNotFoundException {
		List<Rating> userRatings = session.getUserRatings(23);
		assertEquals(6, userRatings.size());
		LongArrayList eventIds = new LongArrayList(6);
		for (Rating r: userRatings) {
			eventIds.add(r.getId());
		}
		assertEquals(6, eventIds.size());
		assertTrue(eventIds.contains(5));
		assertTrue(eventIds.contains(15));
		assertTrue(eventIds.contains(75));
		assertTrue(eventIds.contains(95));
		assertTrue(eventIds.contains(80));
		assertTrue(eventIds.contains(85));
	}

	@Test
	public void testGetUserRatingsFailure() {
		try {
			session.getUserRatings(108);
			//ResourceNotFoundException should be thrown
			fail();
		} catch (ResourceNotFoundException e) {}
	}

	@Test
	public void testGetSpecificUserRatingsSuccess() throws ResourceNotFoundException {
		LongOpenHashSet desiredItems = new LongOpenHashSet();
		desiredItems.add(256);
		desiredItems.add(1024);
		desiredItems.add(8192);
		List<Rating> userRatings = session.getUserRatings(23, desiredItems);
		LongArrayList eventIds = new LongArrayList(5);
		for (Rating r : userRatings) {
			eventIds.add(r.getId());
		}
		assertEquals(5, eventIds.size());
		assertTrue(eventIds.contains(5));
		assertTrue(eventIds.contains(75));
		assertTrue(eventIds.contains(95));
		assertTrue(eventIds.contains(85));
		assertTrue(eventIds.contains(80));
	}

	@Test
	public void testGetSpecificUserRatingsEmpty() throws ResourceNotFoundException {
		LongOpenHashSet desiredItems = new LongOpenHashSet();
		desiredItems.add(32768);
		desiredItems.add(65535);
		desiredItems.add(2);
		List<Rating> userRatings = session.getUserRatings(23, desiredItems);
		assertTrue(userRatings.isEmpty());
	}

	@Test
	public void testGetSpecicUserRatingsFailure() {
		try {
			LongOpenHashSet desiredItems = new LongOpenHashSet();
			desiredItems.add(256);
			desiredItems.add(1024);
			desiredItems.add(8092);
			session.getUserRatings(108, desiredItems);
			//ResourceNotFoundException should be thrown
			fail();
		} catch (ResourceNotFoundException e) {}
	}

	@Test
	public void testGetCurrentItemRatingsSuccess() throws ResourceNotFoundException {
		Map<Long, Double> ratings = session.getCurrentItemRatings(256);
		assertEquals(4, ratings.size());
		assertEquals(3.5, ratings.get(735L), EPSILON);
		assertEquals(3.8, ratings.get(306L), EPSILON);
		assertEquals(2.9, ratings.get(837L), EPSILON);
		assertEquals(1.5, ratings.get(294L), EPSILON);
	}

	@Test
	public void testGetSpecificCurrentItemRatingsSuccess() throws ResourceNotFoundException {
		LongOpenHashSet desiredUsers = new LongOpenHashSet();
		desiredUsers.add(735);
		desiredUsers.add(306);
		desiredUsers.add(23);
		desiredUsers.add(294);
		Map<Long, Double> ratings = session.getCurrentItemRatings(256, desiredUsers);
		assertEquals(3, ratings.size());
		assertEquals(3.5, ratings.get(735L), EPSILON);
		assertEquals(3.8, ratings.get(306L), EPSILON);
		assertEquals(1.5, ratings.get(294L), EPSILON);
	}

	@Test
	public void testGetCurrentItemRatingsFailure() {
		try {
			session.getCurrentItemRatings(65535);
			//ResourceNotFoundException should be thrown
			fail();
		} catch (ResourceNotFoundException e) {}
	}

	@Test
	public void testGetCurrentItemRatingsEmpty() throws ResourceNotFoundException {
		LongOpenHashSet desiredUsers = new LongOpenHashSet();
		desiredUsers.add(938);
		desiredUsers.add(491);
		desiredUsers.add(23);
		Map<Long, Double> ratings = session.getCurrentItemRatings(256, desiredUsers);
		assertTrue(ratings.isEmpty());
	}

	@Test
	public void testGetCurrentUserRatingsSuccess() throws ResourceNotFoundException {
		Map<Long, Double> ratings =  session.getCurrentUserRatings(23);
		assertEquals(2, ratings.size());
		assertEquals(4.4, ratings.get(32L), EPSILON);
		assertEquals(5.0, ratings.get(1024L), EPSILON);
	}

	@Test
	public void testGetSpecificCurrentUserRatingsSuccess() throws ResourceNotFoundException {
		LongOpenHashSet desiredItems = new LongOpenHashSet();
		desiredItems.add(32);
		desiredItems.add(256);
		desiredItems.add(1024);
		Map<Long, Double> ratings = session.getCurrentUserRatings(23, desiredItems);
		assertEquals(2, ratings.size());
		assertEquals(4.4, ratings.get(32L), EPSILON);
		assertEquals(5.0, ratings.get(1024L), EPSILON);
	}

	@Test
	public void testGetCurrentUserRatingsFailure() {
		try {
			session.getCurrentUserRatings(108);
			//ResourceNotFoundException should be thrown
			fail();
		} catch (ResourceNotFoundException e) {}
	}

	@Test
	public void testGetCurrentUserRatingsEmpty() throws ResourceNotFoundException {
		LongOpenHashSet desiredItems = new LongOpenHashSet();
		desiredItems.add(256);
		desiredItems.add(8192);
		desiredItems.add(2048);
		Map<Long, Double> ratings = session.getCurrentUserRatings(23, desiredItems);
		assertTrue(ratings.isEmpty());
	}

	@Test
	public void testGetUserPredictionsSuccess1() throws ResourceNotFoundException {
		Map<Long, Double> userPreds = session.getUserPredictions(23);
		assertEquals(4, userPreds.size());
		assertTrue(userPreds.get(2048L) >= 0);
		assertTrue(userPreds.get(512L) >= 0);
		assertTrue(userPreds.get(256L) >= 0);
		assertTrue(userPreds.get(8192L) >= 0);
	}

	@Test
	public void testGetUserPredictionsSuccess2() throws ResourceNotFoundException {
		LongOpenHashSet itemSet = new LongOpenHashSet();
		itemSet.add(2048);
		Map<Long, Double> userPreds = session.getUserPredictions(23, itemSet);
		assertEquals(1, userPreds.size());
		assertTrue(userPreds.get(2048L) >= 0);
	}

	@Test
	public void testGetUserPredictionsFailure() {
		try {
			session.getUserPredictions(108);
			//Should throw ResourceNotFoundException
			fail();
		} catch (ResourceNotFoundException e) {}
	}

	@Test
	public void testGetUserPredictionsEmpty() throws ResourceNotFoundException {
		LongOpenHashSet itemSet = new LongOpenHashSet();
		itemSet.add(32);
		itemSet.add(1024);
		Map<Long, Double> userPreds = session.getUserPredictions(23, itemSet);
		assertTrue(userPreds.isEmpty());
	}

	@Test
	public void testGetUserPreferencesSuccess1() throws ResourceNotFoundException {
		Map<Long, Double> userPrefs = session.getUserPreferences(23);
		assertEquals(6, userPrefs.size());
		assertNotNull(userPrefs.get(256L));
		assertNotNull(userPrefs.get(2048L));
		assertNotNull(userPrefs.get(512L));
		assertNotNull(userPrefs.get(8192L));
		assertEquals(4.4, userPrefs.get(32L), EPSILON);
		assertEquals(5.0, userPrefs.get(1024L), EPSILON);
		assertTrue(userPrefs.get(512L) >= 0);
		assertTrue(userPrefs.get(2048L) >= 0);
		assertTrue(userPrefs.get(256L) >= 0);
		assertTrue(userPrefs.get(8192L) >= 0);
	}

	@Test
	public void testGetUserPreferencesSuccess2() throws ResourceNotFoundException {
		LongOpenHashSet desiredItems = new LongOpenHashSet();
		desiredItems.add(256);
		desiredItems.add(32);
		desiredItems.add(512);
		Map<Long, Double> userPrefs = session.getUserPreferences(735, desiredItems);
		assertEquals(3, userPrefs.size());
		assertNotNull(userPrefs.get(32L));
		assertNotNull(userPrefs.get(512L));
		assertEquals(3.5, userPrefs.get(256L), EPSILON);
		assertTrue(userPrefs.get(32L) >= 0);
		assertTrue(userPrefs.get(512L) >= 0);
	}

	@Test
	public void testGetUserPreferencesFailure() {
		try {
			session.getUserPreferences(999);
			//ResourceNotFoundException should be thrown
			fail();
		} catch (ResourceNotFoundException e) {}
	}

	@Test
	public void testGetUserRecommendationsSuccess() throws ResourceNotFoundException {
		List<Long> recos = session.getUserRecommendations(938);
		Set<Long> ratedIds = session.getCurrentUserRatings(938).keySet();
		int expectedSize = session.getItemCount() - ratedIds.size();
		assertEquals(expectedSize, recos.size());
		for (long id : ratedIds) {
			assertFalse(recos.contains(id));
		}
	}

	@Test
	public void testGetUserRecommendationsFailure() {
		try {
			session.getUserRecommendations(108);
			//ResourceNotFoundException should be thrown
			fail();
		} catch (ResourceNotFoundException e) {}
	}

	@Test
	public void testGetUserCount() {
		assertEquals(7, session.getUserCount());
	}

	@Test
	public void testGetItemCount() {
		assertEquals(6, session.getItemCount());
	}

	@Test
	public void testGetEventCount() {
		assertEquals(12, session.getEventCount());
	}

	@Test
	public void testContainsUserSuccess() {
		assertTrue(session.containsUser(23));
		assertTrue(session.containsUser(735));
		assertTrue(session.containsUser(491));
		assertTrue(session.containsUser(306));
		assertTrue(session.containsUser(938));
		assertTrue(session.containsUser(837));
		assertTrue(session.containsUser(294));
	}

	@Test
	public void testContainsUserFailure() {
		assertFalse(session.containsUser(108));
		assertFalse(session.containsUser(-194929));
	}

	@Test
	public void testContainsItemSuccess() {
		assertTrue(session.containsItem(256));
		assertTrue(session.containsItem(32));
		assertTrue(session.containsItem(2048));
		assertTrue(session.containsItem(512));
		assertTrue(session.containsItem(1024));
		assertTrue(session.containsItem(8192));
	}

	@Test
	public void testContainsItemFailure() {
		assertFalse(session.containsItem(16384));
		assertFalse(session.containsItem(32768));
		assertFalse(session.containsItem(65536));
	}

	@Test
	public void testContainsEventSuccess() {
		assertTrue(session.containsEvent(5));
		assertTrue(session.containsEvent(10));
		assertTrue(session.containsEvent(15));
		assertTrue(session.containsEvent(25));
		assertTrue(session.containsEvent(35));
		assertTrue(session.containsEvent(70));
		assertTrue(session.containsEvent(75));
		assertTrue(session.containsEvent(95));
		assertTrue(session.containsEvent(45));
		assertTrue(session.containsEvent(40));
	}

	@Test
	public void testContainsEventFailure() {
		assertFalse(session.containsEvent(20));
		assertFalse(session.containsEvent(30));
		assertFalse(session.containsEvent(-5842));
	}
}