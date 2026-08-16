package com.web.gallery.model;

import static org.junit.jupiter.api.Assertions.*;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.test.context.ActiveProfiles;

import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.photo.Caption;
import com.web.gallery.domain.photo.FavoriteCount;
import com.web.gallery.domain.photo.ImageFilePath;
import com.web.gallery.domain.photo.IsFavorite;
import com.web.gallery.domain.photo.PhotoAt;
import com.web.gallery.domain.photo.PhotoNo;
import com.web.gallery.enumuration.DirectionEnum;

@ActiveProfiles("test")
public class PhotoModelListTest {

	private PhotoModel createPhotoModel(Long photoNo, DirectionEnum directionKbn, Boolean isFavorite) {
		return PhotoModel.builder()
				.accountNo(new AccountNo(1L))
				.photoNo(new PhotoNo(photoNo))
				.favoriteCount(new FavoriteCount(1))
				.isFavorite(new IsFavorite(isFavorite))
				.photoAt(new PhotoAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
				.imageFilePath(new ImageFilePath("https://localhost:8080/image/aaaaaaaa/DSC" + photoNo + ".jpg"))
				.caption(new Caption("キャプション" + photoNo))
				.directionKbn(directionKbn)
				.photoTagModelList(PhotoTagModelList.empty())
				.build();
	}

	@Nested
	@Order(1)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class filterByDirectionKbn {
		@Test
		@Order(1)
		@DisplayName("正常系：抽出条件が未指定の場合はフィルタリングしないこと")
		void filterByDirectionKbn_not_condition() {
			PhotoModelList photoModelList = PhotoModelList.of(List.of(
					createPhotoModel(1L, DirectionEnum.VERTICAL, false),
					createPhotoModel(2L, DirectionEnum.HORIZONTAL, false)));

			PhotoModelList actual = photoModelList.filterByDirectionKbn(DirectionEnum.NONE);

			assertEquals(2, actual.size());
		}

		@Test
		@Order(2)
		@DisplayName("正常系：抽出条件と一致するものだけに絞り込まれること")
		void filterByDirectionKbn_match_condition() {
			PhotoModelList photoModelList = PhotoModelList.of(List.of(
					createPhotoModel(1L, DirectionEnum.VERTICAL, false),
					createPhotoModel(2L, DirectionEnum.HORIZONTAL, false)));

			PhotoModelList actual = photoModelList.filterByDirectionKbn(DirectionEnum.VERTICAL);

			assertEquals(1, actual.size());
			assertEquals(new PhotoNo(1L), actual.get(0).getPhotoNo());
		}
	}

	@Nested
	@Order(2)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class filterByFavorite {
		@Test
		@Order(1)
		@DisplayName("正常系：お気に入りのみが指定されていない場合はフィルタリングしないこと")
		void filterByFavorite_not_condition() {
			PhotoModelList photoModelList = PhotoModelList.of(List.of(
					createPhotoModel(1L, DirectionEnum.VERTICAL, true),
					createPhotoModel(2L, DirectionEnum.VERTICAL, false)));

			PhotoModelList actual = photoModelList.filterByFavorite(false);

			assertEquals(2, actual.size());
		}

		@Test
		@Order(2)
		@DisplayName("正常系：お気に入りのみが指定されている場合はお気に入りの写真のみに絞り込まれること")
		void filterByFavorite_match_condition() {
			PhotoModelList photoModelList = PhotoModelList.of(List.of(
					createPhotoModel(1L, DirectionEnum.VERTICAL, true),
					createPhotoModel(2L, DirectionEnum.VERTICAL, false)));

			PhotoModelList actual = photoModelList.filterByFavorite(true);

			assertEquals(1, actual.size());
			assertEquals(new PhotoNo(1L), actual.get(0).getPhotoNo());
		}
	}
}
