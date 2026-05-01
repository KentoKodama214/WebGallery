package com.web.gallary.mapper;

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
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import com.web.gallary.dto.PhotoDetailDto;
import com.web.gallary.dto.PhotoDetailGetDto;
import com.web.gallary.dto.PhotoDto;
import com.web.gallary.dto.PhotoListGetDto;
import com.web.gallary.enumuration.DirectionEnum;

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
			photoSelectDto.setAccountNo(1);
			photoSelectDto.setPhotoAccountNo(1);
			
			List<PhotoDto> actual = photoDetailMapper.getPhotoList(photoSelectDto);
			
			PhotoDto actualPhotoDto1 = actual.stream().sorted(Comparator.comparing(PhotoDto::getPhotoNo)).toList().getFirst();
			assertEquals(1, actualPhotoDto1.getAccountNo());
			assertEquals(1, actualPhotoDto1.getPhotoNo());
			assertEquals(2, actualPhotoDto1.getFavoriteCount());
			assertEquals(true, actualPhotoDto1.getIsFavorite());
			assertEquals(OffsetDateTime.of(2021, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualPhotoDto1.getPhotoAt());
			assertEquals("https://www.xxx.com/DSC111.jpg", actualPhotoDto1.getImageFilePath());
			assertEquals("キャプション11", actualPhotoDto1.getCaption());
			assertEquals(DirectionEnum.VERTICAL, actualPhotoDto1.getDirectionKbn());
			
			PhotoDto actualPhotoDto2 = actual.stream().sorted(Comparator.comparing(PhotoDto::getPhotoNo)).toList().getLast();
			assertEquals(1, actualPhotoDto2.getAccountNo());
			assertEquals(2, actualPhotoDto2.getPhotoNo());
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
			photoSelectDto.setAccountNo(1);
			photoSelectDto.setPhotoAccountNo(3);
			
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
			photoGetDto.setAccountNo(1);
			photoGetDto.setPhotoAccountNo(1);
			photoGetDto.setPhotoNo(1);
			
			PhotoDetailDto actual = photoDetailMapper.getPhotoDetail(photoGetDto);
			assertEquals(1, actual.getAccountNo());
			assertEquals(1, actual.getPhotoNo());
			assertEquals(true, actual.getIsFavorite());
			assertEquals(OffsetDateTime.of(2021, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actual.getPhotoAt());
			assertEquals(1, actual.getLocationNo());
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
			photoGetDto.setAccountNo(1);
			photoGetDto.setPhotoAccountNo(3);
			photoGetDto.setPhotoNo(1);
			
			PhotoDetailDto actual = photoDetailMapper.getPhotoDetail(photoGetDto);
			assertNull(actual);
		}
	}
}