package org.grouplens.lenskit.webapp;

import java.lang.reflect.Field;
import java.util.Arrays;

import org.grouplens.common.dto.Dto;
import org.grouplens.common.dto.DtoContainer;
import org.grouplens.common.dto.DtoContentHandler;
import org.grouplens.lenskit.webapp.dto.EventDto;
import org.grouplens.lenskit.webapp.dto.ItemEventsDto;
import org.grouplens.lenskit.webapp.dto.ItemPurchasesDto;
import org.grouplens.lenskit.webapp.dto.ItemRatingsDto;
import org.grouplens.lenskit.webapp.dto.ItemStatisticsDto;
import org.grouplens.lenskit.webapp.dto.SystemStatisticsDto;
import org.grouplens.lenskit.webapp.dto.UserDto;
import org.grouplens.lenskit.webapp.dto.UserEventsDto;
import org.grouplens.lenskit.webapp.dto.UserPredictionsDto;
import org.grouplens.lenskit.webapp.dto.UserPreferencesDto;
import org.grouplens.lenskit.webapp.dto.UserPurchasesDto;
import org.grouplens.lenskit.webapp.dto.UserRatingsDto;
import org.grouplens.lenskit.webapp.dto.UserRecommendationsDto;
import org.grouplens.lenskit.webapp.dto.UserStatisticsDto;
import org.junit.Assert;
import org.junit.Test;

public abstract class AbstractDtoTest {

	protected DtoContentHandler contentHandler;

	@Test
	public void testUserDto() throws Exception {
		UserDto expected = new UserDto("516");
		DtoContainer<UserDto> container = new DtoContainer<UserDto>(UserDto.class, expected);
		String output = contentHandler.toString(container);
		DtoContainer<UserDto> result = new DtoContainer<UserDto>(UserDto.class);
		contentHandler.fromString(output, result);
		assertDtoEquals(expected, result.getSingle());
	}

	@Test
	public void testUserStatisticsDto() throws Exception {
		UserStatisticsDto expected = new UserStatisticsDto("516", 632, 532, 3.4);
		DtoContainer<UserStatisticsDto> container =
				new DtoContainer<UserStatisticsDto>(UserStatisticsDto.class, expected);		
		String output = contentHandler.toString(container);
		DtoContainer<UserStatisticsDto> result = 
				new DtoContainer<UserStatisticsDto>(UserStatisticsDto.class);
		contentHandler.fromString(output, result);
		assertDtoEquals(expected, result.getSingle());
	}

	@Test
	public void testUserPredictionsDto() throws Exception {
		UserPredictionsDto expected = new UserPredictionsDto("516", 3, 0);
		expected.addPrediction("1024", 3.13);
		expected.addPrediction("4096", 2.75);
		expected.addPrediction("7890", 4.77);
		DtoContainer<UserPredictionsDto> container =
				new DtoContainer<UserPredictionsDto>(UserPredictionsDto.class, expected);
		String output = contentHandler.toString(container);		
		DtoContainer<UserPredictionsDto> result =
				new DtoContainer<UserPredictionsDto>(UserPredictionsDto.class);
		contentHandler.fromString(output, result);
		assertDtoEquals(expected, result.getSingle());
	}

	@Test
	public void testUserPreferencesDto() throws Exception {
		UserPreferencesDto expected = new UserPreferencesDto("516", 4, 0);
		expected.addPreference("1023", "rating", 3.13);
		expected.addPreference("4096", "prediction", 2.75);
		expected.addPreference("7890", "rating", 4.77);
		expected.addPreference("4567", "prediction", 1.75);
		DtoContainer<UserPreferencesDto> container =
				new DtoContainer<UserPreferencesDto>(UserPreferencesDto.class, expected);
		String output = contentHandler.toString(container);		
		DtoContainer<UserPreferencesDto> result =
				new DtoContainer<UserPreferencesDto>(UserPreferencesDto.class);
		contentHandler.fromString(output, result);
		assertDtoEquals(expected, result.getSingle());
	}

	@Test
	public void testUserRecommendationsDto() throws Exception {
		UserRecommendationsDto expected = new UserRecommendationsDto("516", 4, 0);
		expected.addRecommendation("1");
		expected.addRecommendation("2");
		expected.addRecommendation("3");
		expected.addRecommendation("4");
		DtoContainer<UserRecommendationsDto> container =
				new DtoContainer<UserRecommendationsDto>(UserRecommendationsDto.class, expected);
		String output = contentHandler.toString(container);		
		DtoContainer<UserRecommendationsDto> result =
				new DtoContainer<UserRecommendationsDto>(UserRecommendationsDto.class);
		contentHandler.fromString(output, result);
		assertDtoEquals(expected, result.getSingle());
	}

	@Test
	public void testUserEventsDto() throws Exception {
		UserEventsDto expected = new UserEventsDto("516", 5, 0);
		expected.addEvent("rating", "0900", "1024", 20000, 4.0, "aaa");
		expected.addEvent("purchase", "0500", "4096", 35000, "bbb");
		expected.addEvent("rating", "0480", "8192", 50000, 3.0, "ccc");
		expected.addEvent("purchase", "0550", "2048", 19000, "ddd");
		expected.addEvent("unrating", "0880", "4096", 15000, "eee");
		DtoContainer<UserEventsDto> container =
				new DtoContainer<UserEventsDto>(UserEventsDto.class, expected);		
		String output = contentHandler.toString(container);
		DtoContainer<UserEventsDto> result =
				new DtoContainer<UserEventsDto>(UserEventsDto.class);
		contentHandler.fromString(output, result);
		assertDtoEquals(expected, result.getSingle());
	}

	@Test
	public void testUserRatingsDto() throws Exception {
		UserRatingsDto expected = new UserRatingsDto("516", 5, 0);
		expected.addRating("0945", "16384", 20000, 3.66, "aaa");
		expected.addRating("0234", "512", 65000, 4.00, "bbb");
		expected.addRating("0443", "1024", 54000, 3.50, "ccc");
		expected.addRating("0555", "4096", 33000, 3.75, "ddd");
		expected.addRating("0665", "4096", 44000, null, "eee");
		DtoContainer<UserRatingsDto> container =
				new DtoContainer<UserRatingsDto>(UserRatingsDto.class, expected);		
		String output = contentHandler.toString(container);
		DtoContainer<UserRatingsDto> result =
				new DtoContainer<UserRatingsDto>(UserRatingsDto.class);
		contentHandler.fromString(output, result);
		assertDtoEquals(expected, result.getSingle());;
	}

	@Test
	public void testUserPurchasesDto() throws Exception {
		UserPurchasesDto expected = new UserPurchasesDto("516", 3, 0);
		expected.addPurchase("0945", "16384", 20000);
		expected.addPurchase("0234", "512", 65000);
		expected.addPurchase("0555", "4096", 55000);

		DtoContainer<UserPurchasesDto> container =
				new DtoContainer<UserPurchasesDto>(UserPurchasesDto.class, expected);
		String output = contentHandler.toString(container);	
		DtoContainer<UserPurchasesDto> result =
				new DtoContainer<UserPurchasesDto>(UserPurchasesDto.class);
		contentHandler.fromString(output, result);
		assertDtoEquals(expected, result.getSingle());		
	}

	@Test
	public void testItemStatisticsDto() throws Exception {
		ItemStatisticsDto expected = new ItemStatisticsDto("1024", 700, 600, 4.8);
		DtoContainer<ItemStatisticsDto> container =
				new DtoContainer<ItemStatisticsDto>(ItemStatisticsDto.class, expected);		
		String output = contentHandler.toString(container);
		DtoContainer<ItemStatisticsDto> result =
				new DtoContainer<ItemStatisticsDto>(ItemStatisticsDto.class);
		contentHandler.fromString(output, result);
		assertDtoEquals(expected, result.getSingle());
	}

	@Test
	public void testItemEventsDto() throws Exception {
		ItemEventsDto expected = new ItemEventsDto("1024", 4, 0);
		expected.addEvent("rating", "0900", "516", 20000, 4.0, "aaa");
		expected.addEvent("purchase", "0500", "408", 35000, "bbb");
		expected.addEvent("rating", "0480", "372", 50000, 3.0, "ccc");
		expected.addEvent("purchase", "0550", "987", 19000, "ddd");
		DtoContainer<ItemEventsDto> container =
				new DtoContainer<ItemEventsDto>(ItemEventsDto.class, expected);		
		String output = contentHandler.toString(container);		
		DtoContainer<ItemEventsDto> result =
				new DtoContainer<ItemEventsDto>(ItemEventsDto.class);
		contentHandler.fromString(output, result);
		assertDtoEquals(expected, result.getSingle());
	}

	@Test
	public void testItemRatingsDto() throws Exception {
		ItemRatingsDto expected = new ItemRatingsDto("1024", 5, 0);
		expected.addRating("0945", "516", 20000, 3.66, "aaa");
		expected.addRating("0234", "484", 65000, 4.00, "bbb");
		expected.addRating("0443", "789", 54000, 3.50, "ccc");
		expected.addRating("0555", "565", 33000, 3.75, "eee");
		expected.addRating("0656", "323", 44000, null, "fff");
		DtoContainer<ItemRatingsDto> container =
				new DtoContainer<ItemRatingsDto>(ItemRatingsDto.class, expected);		
		String output = contentHandler.toString(container);
		DtoContainer<ItemRatingsDto> result =
				new DtoContainer<ItemRatingsDto>(ItemRatingsDto.class);
		contentHandler.fromString(output, result);
		assertDtoEquals(expected, result.getSingle());
	}

	@Test
	public void testItemPurchasesDto() throws Exception {
		ItemPurchasesDto expected = new ItemPurchasesDto("1024", 4, 0);
		expected.addPurchase("0945", "516", 20000);
		expected.addPurchase("0234", "484", 65000);
		expected.addPurchase("0443", "789", 54000);
		expected.addPurchase("0555", "565", 33000);
		DtoContainer<ItemPurchasesDto> container =
				new DtoContainer<ItemPurchasesDto>(ItemPurchasesDto.class, expected);	
		String output = contentHandler.toString(container);
		DtoContainer<ItemPurchasesDto> result =
				new DtoContainer<ItemPurchasesDto>(ItemPurchasesDto.class);
		contentHandler.fromString(output, result);
		assertDtoEquals(expected, result.getSingle());
	}

	@Test
	public void testEventDto() throws Exception {
		EventDto expected = new EventDto("purchase", "0310", "516", "1024", 33000, "abcd");
		DtoContainer<EventDto> container =
				new DtoContainer<EventDto>(EventDto.class, expected);
		String output = contentHandler.toString(container);		
		DtoContainer<EventDto> result = new DtoContainer<EventDto>(EventDto.class);
		contentHandler.fromString(output, result);
		assertDtoEquals(expected, result.getSingle());
	}

	@Test
	public void testStatisticsDto() throws Exception {
		SystemStatisticsDto expected = new SystemStatisticsDto(6300, 180000, 439000);
		DtoContainer<SystemStatisticsDto> container =
				new DtoContainer<SystemStatisticsDto>(SystemStatisticsDto.class, expected);		
		String output = contentHandler.toString(container);
		DtoContainer<SystemStatisticsDto> result =
				new DtoContainer<SystemStatisticsDto>(SystemStatisticsDto.class);
		contentHandler.fromString(output, result);
		assertDtoEquals(expected, result.getSingle());
	}

	private <T extends Dto> void assertDtoEquals(T expected, T actual) throws Exception {
		Assert.assertEquals(expected.getClass(), actual.getClass()); // sanity
		for (Field f: expected.getClass().getFields()) {
			Object expectedValue = f.get(expected);
			Object actualValue = f.get(actual);

			if (expectedValue == null) {
				Assert.assertNull(f.getName() + " expected to be null", actualValue);
			} else {
				Assert.assertNotNull(f.getName() + " expected to be not null", actualValue);

				if (f.getType().isArray()) {
					Object[] eva = (Object[]) expectedValue;
					Object[] ava = (Object[]) actualValue;

					if (Dto.class.isAssignableFrom(f.getType().getComponentType())) {
						// can't rely on JUnit yet, do it by hand
						Assert.assertEquals(f.getName() + " array lengths differ", eva.length, ava.length);
						for (int i = 0; i < eva.length; i++)
							assertDtoEquals((Dto) eva[i], (Dto) ava[i]);
					} else
						Assert.assertTrue(f.getName() + " simple arrays aren't equal", Arrays.equals(eva, ava));
				} else {
					// plain object or Dto
					if (Dto.class.isAssignableFrom(f.getType()))
						assertDtoEquals((Dto) expectedValue, (Dto) actualValue);
					else
						Assert.assertEquals(f.getName() + " values aren't equal", expectedValue, actualValue);
				}
			}
		}
	}
}