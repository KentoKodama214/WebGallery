package com.web.gallery.model;

import static org.junit.jupiter.api.Assertions.*;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.photo.Caption;
import com.web.gallery.domain.photo.FavoriteCount;
import com.web.gallery.domain.photo.ImageFilePath;
import com.web.gallery.domain.photo.IsFavorite;
import com.web.gallery.domain.photo.PhotoAt;
import com.web.gallery.domain.photo.PhotoNo;
import com.web.gallery.enumeration.DirectionEnum;

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

	@Test
	@DisplayName("正常系：指定のComparatorでソートされること")
	void sorted_success() {
		PhotoModelList photoModelList = PhotoModelList.of(List.of(
				createPhotoModel(1L, DirectionEnum.VERTICAL, false),
				createPhotoModel(2L, DirectionEnum.HORIZONTAL, false)));

		PhotoModelList actual = photoModelList.sorted(
				Comparator.comparing((PhotoModel photoModel) -> photoModel.getPhotoNo().value(), Comparator.reverseOrder()));

		assertEquals(2, actual.size());
		assertEquals(new PhotoNo(2L), actual.get(0).getPhotoNo());
		assertEquals(new PhotoNo(1L), actual.get(1).getPhotoNo());
	}
}
