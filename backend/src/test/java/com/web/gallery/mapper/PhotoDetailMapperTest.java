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

import com.web.gallery.dto.PhotoDetailDto;
import com.web.gallery.dto.PhotoDetailGetDto;
import com.web.gallery.dto.PhotoDto;
import com.web.gallery.dto.PhotoListGetDto;
import com.web.gallery.enumeration.DirectionEnum;

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
			photoSelectDto.setAccountNo(1L);
			photoSelectDto.setPhotoAccountNo(1L);
			photoSelectDto.setLimit(100);
			photoSelectDto.setOffset(0);

			List<PhotoDto> actual = photoDetailMapper.getPhotoList(photoSelectDto);

			PhotoDto actualPhotoDto1 = actual.stream().sorted(Comparator.comparing(PhotoDto::getPhotoNo)).toList().getFirst();
			assertEquals(1L, actualPhotoDto1.getAccountNo());
			assertEquals(1L, actualPhotoDto1.getPhotoNo());
			assertEquals(2, actualPhotoDto1.getFavoriteCount());
			assertEquals(true, actualPhotoDto1.getIsFavorite());
			assertEquals(OffsetDateTime.of(2021, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualPhotoDto1.getPhotoAt());
			assertEquals("https://www.xxx.com/DSC111.jpg", actualPhotoDto1.getImageFilePath());
			assertEquals("キャプション11", actualPhotoDto1.getCaption());
			assertEquals(DirectionEnum.VERTICAL, actualPhotoDto1.getDirectionKbn());

			PhotoDto actualPhotoDto2 = actual.stream().sorted(Comparator.comparing(PhotoDto::getPhotoNo)).toList().getLast();
			assertEquals(1L, actualPhotoDto2.getAccountNo());
			assertEquals(2L, actualPhotoDto2.getPhotoNo());
			assertEquals(1, actualPhotoDto2.getFavoriteCount());
			assertEquals(false, actualPhotoDto2.getIsFavorite());
			assertEquals(OffsetDateTime.of(2021, 2, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualPhotoDto2.getPhotoAt());
			assertEquals("https://www.xxx.com/DSC222.jpg", actualPhotoDto2.getImageFilePath());
			assertEquals("キャプション12", actualPhotoDto2.getCaption());
			assertEquals(DirectionEnum.HORIZONTAL, actualPhotoDto2.getDirectionKbn());
		}

		@Test
		@Order(2)
		@DisplayName("正常系：selectで0件の場合")
		void getPhotoList_not_found() {
			PhotoListGetDto photoSelectDto = new PhotoListGetDto();
			photoSelectDto.setAccountNo(1L);
			photoSelectDto.setPhotoAccountNo(3L);
			photoSelectDto.setLimit(100);
			photoSelectDto.setOffset(0);

			List<PhotoDto> actual = photoDetailMapper.getPhotoList(photoSelectDto);
			assertEquals(new ArrayList<PhotoDto>(), actual);
		}

		@Test
		@Order(3)
		@DisplayName("正常系：limit・offsetで取得件数・取得開始位置が絞り込まれること")
		void getPhotoList_limit_offset() {
			// account1の写真は2件（photo_no 1, 2）のため、limit=1で1件のみ取得できること
			PhotoListGetDto firstPageDto = new PhotoListGetDto();
			firstPageDto.setAccountNo(1L);
			firstPageDto.setPhotoAccountNo(1L);
			firstPageDto.setLimit(1);
			firstPageDto.setOffset(0);

			List<PhotoDto> firstPageActual = photoDetailMapper.getPhotoList(firstPageDto);
			assertEquals(1, firstPageActual.size());
			// デフォルト（撮影日時降順）のため、先頭は撮影日時が新しいphoto_no=2であること
			assertEquals(2L, firstPageActual.getFirst().getPhotoNo());

			// offset=1を指定すると、残りの1件（photo_no=1）が取得できること
			PhotoListGetDto secondPageDto = new PhotoListGetDto();
			secondPageDto.setAccountNo(1L);
			secondPageDto.setPhotoAccountNo(1L);
			secondPageDto.setLimit(1);
			secondPageDto.setOffset(1);

			List<PhotoDto> secondPageActual = photoDetailMapper.getPhotoList(secondPageDto);
			assertEquals(1, secondPageActual.size());
			assertEquals(1L, secondPageActual.getFirst().getPhotoNo());
		}

		@Test
		@Order(4)
		@DisplayName("正常系：季節・時期順（月日の降順）に並び替えられること")
		void getPhotoList_sortBy_season() {
			// account1の写真は1月・2月の撮影のため、季節順（月日降順）では撮影日時降順と同順になること
			PhotoListGetDto photoSelectDto = new PhotoListGetDto();
			photoSelectDto.setAccountNo(1L);
			photoSelectDto.setPhotoAccountNo(1L);
			photoSelectDto.setSortBy("SEASON");
			photoSelectDto.setLimit(100);
			photoSelectDto.setOffset(0);

			List<PhotoDto> actual = photoDetailMapper.getPhotoList(photoSelectDto);
			assertEquals(2, actual.size());
			assertEquals(2L, actual.get(0).getPhotoNo());
			assertEquals(1L, actual.get(1).getPhotoNo());
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
			photoGetDto.setAccountNo(1L);
			photoGetDto.setPhotoAccountNo(1L);
			photoGetDto.setPhotoNo(1L);

			PhotoDetailDto actual = photoDetailMapper.getPhotoDetail(photoGetDto);
			assertEquals(1L, actual.getAccountNo());
			assertEquals(1L, actual.getPhotoNo());
			assertEquals(true, actual.getIsFavorite());
			assertEquals(OffsetDateTime.of(2021, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actual.getPhotoAt());
			assertEquals(1L, actual.getLocationNo());
			assertEquals("住所1", actual.getAddress());
			assertEquals(0, BigDecimal.valueOf(38.1).compareTo(actual.getLatitude()));
			assertEquals(0, BigDecimal.valueOf(115.1).compareTo(actual.getLongitude()));
			assertEquals("ロケーション1", actual.getLocationName());
			assertEquals("https://www.xxx.com/DSC111.jpg", actual.getImageFilePath());
			assertEquals("タイトル11", actual.getPhotoJapaneseTitle());
			assertEquals("title11", actual.getPhotoEnglishTitle());
			assertEquals("キャプション11", actual.getCaption());
			assertEquals(DirectionEnum.VERTICAL, actual.getDirectionKbn());
			assertEquals(24, actual.getFocalLength());
			assertEquals(0, BigDecimal.valueOf(8.0).compareTo(actual.getFValue()));
			assertEquals(0, BigDecimal.valueOf(1).compareTo(actual.getShutterSpeed()));
			assertEquals(100, actual.getIso());
		}

		@Test
		@Order(2)
		@DisplayName("正常系：selectで0件の場合")
		void getPhotoDetail_not_found() {
			PhotoDetailGetDto photoGetDto = new PhotoDetailGetDto();
			photoGetDto.setAccountNo(1L);
			photoGetDto.setPhotoAccountNo(3L);
			photoGetDto.setPhotoNo(1L);

			PhotoDetailDto actual = photoDetailMapper.getPhotoDetail(photoGetDto);
			assertNull(actual);
		}
	}
}
