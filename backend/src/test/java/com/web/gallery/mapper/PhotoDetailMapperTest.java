package com.web.gallery.mapper;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.photo.PhotoNo;
import com.web.gallery.dto.PhotoDetailDto;
import com.web.gallery.dto.PhotoDetailGetDto;
import com.web.gallery.dto.PhotoDto;
import com.web.gallery.dto.PhotoListGetDto;
import com.web.gallery.enumuration.DirectionEnum;

@MybatisTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class PhotoDetailMapperTest {
	@Autowired
	private PhotoDetailMapper photoDetailMapper;

	@Nested
	@Order(1)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	@Sql("/sql/common/cleanup.sql")
	@Sql("/sql/mapper/PhotoDetailMapperTest.sql")
	class getPhotoList {
		@Test
		@Order(1)
		@DisplayName("正常系：selectで1件以上の場合")
		void getPhotoList_some_photos() {
			PhotoListGetDto photoSelectDto = new PhotoListGetDto();
			photoSelectDto.setAccountNo(new AccountNo(1L));
			photoSelectDto.setPhotoAccountNo(new AccountNo(1L));

			List<PhotoDto> actual = photoDetailMapper.getPhotoList(photoSelectDto);

			PhotoDto actualPhotoDto1 = actual.stream().sorted(Comparator.comparing(p -> p.getPhotoNo().value())).toList().getFirst();
			assertEquals(1L, actualPhotoDto1.getAccountNo().value());
			assertEquals(1L, actualPhotoDto1.getPhotoNo().value());
			assertEquals(2, actualPhotoDto1.getFavoriteCount().value());
			assertEquals(true, actualPhotoDto1.getIsFavorite().value());
			assertEquals(OffsetDateTime.of(2021, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualPhotoDto1.getPhotoAt().value());
			assertEquals("https://www.xxx.com/DSC111.jpg", actualPhotoDto1.getImageFilePath().value());
			assertEquals("キャプション11", actualPhotoDto1.getCaption().value());
			assertEquals(DirectionEnum.VERTICAL, actualPhotoDto1.getDirectionKbn());

			PhotoDto actualPhotoDto2 = actual.stream().sorted(Comparator.comparing(p -> p.getPhotoNo().value())).toList().getLast();
			assertEquals(1L, actualPhotoDto2.getAccountNo().value());
			assertEquals(2L, actualPhotoDto2.getPhotoNo().value());
			assertEquals(1, actualPhotoDto2.getFavoriteCount().value());
			assertEquals(false, actualPhotoDto2.getIsFavorite().value());
			assertEquals(OffsetDateTime.of(2021, 2, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualPhotoDto2.getPhotoAt().value());
			assertEquals("https://www.xxx.com/DSC222.jpg", actualPhotoDto2.getImageFilePath().value());
			assertEquals("キャプション12", actualPhotoDto2.getCaption().value());
			assertEquals(DirectionEnum.HORIZONTAL, actualPhotoDto2.getDirectionKbn());
		}

		@Test
		@Order(2)
		@DisplayName("正常系：selectで0件の場合")
		void getPhotoList_not_found() {
			PhotoListGetDto photoSelectDto = new PhotoListGetDto();
			photoSelectDto.setAccountNo(new AccountNo(1L));
			photoSelectDto.setPhotoAccountNo(new AccountNo(3L));

			List<PhotoDto> actual = photoDetailMapper.getPhotoList(photoSelectDto);
			assertEquals(new ArrayList<PhotoDto>(), actual);
		}
	}

	@Nested
	@Order(2)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	@Sql("/sql/common/cleanup.sql")
	@Sql("/sql/mapper/PhotoDetailMapperTest.sql")
	class getPhotoDetail {
		@Test
		@Order(1)
		@DisplayName("正常系：selectで1件の場合")
		void getPhotoDetail_found() {
			PhotoDetailGetDto photoGetDto = new PhotoDetailGetDto();
			photoGetDto.setAccountNo(new AccountNo(1L));
			photoGetDto.setPhotoAccountNo(new AccountNo(1L));
			photoGetDto.setPhotoNo(new PhotoNo(1L));

			PhotoDetailDto actual = photoDetailMapper.getPhotoDetail(photoGetDto);
			assertEquals(1L, actual.getAccountNo().value());
			assertEquals(1L, actual.getPhotoNo().value());
			assertEquals(true, actual.getIsFavorite().value());
			assertEquals(OffsetDateTime.of(2021, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actual.getPhotoAt().value());
			assertEquals(1L, actual.getLocationNo().value());
			assertEquals("住所1", actual.getAddress().value());
			assertEquals(0, BigDecimal.valueOf(38.1).compareTo(actual.getLatitude().value()));
			assertEquals(0, BigDecimal.valueOf(115.1).compareTo(actual.getLongitude().value()));
			assertEquals("ロケーション1", actual.getLocationName().value());
			assertEquals("https://www.xxx.com/DSC111.jpg", actual.getImageFilePath().value());
			assertEquals("タイトル11", actual.getPhotoJapaneseTitle().value());
			assertEquals("title11", actual.getPhotoEnglishTitle().value());
			assertEquals("キャプション11", actual.getCaption().value());
			assertEquals(DirectionEnum.VERTICAL, actual.getDirectionKbn());
			assertEquals(24, actual.getFocalLength().value());
			assertEquals(0, BigDecimal.valueOf(8.0).compareTo(actual.getFValue().value()));
			assertEquals(0, BigDecimal.valueOf(1).compareTo(actual.getShutterSpeed().value()));
			assertEquals(100, actual.getIso().value());
		}

		@Test
		@Order(2)
		@DisplayName("正常系：selectで0件の場合")
		void getPhotoDetail_not_found() {
			PhotoDetailGetDto photoGetDto = new PhotoDetailGetDto();
			photoGetDto.setAccountNo(new AccountNo(1L));
			photoGetDto.setPhotoAccountNo(new AccountNo(3L));
			photoGetDto.setPhotoNo(new PhotoNo(1L));

			PhotoDetailDto actual = photoDetailMapper.getPhotoDetail(photoGetDto);
			assertNull(actual);
		}
	}
}
