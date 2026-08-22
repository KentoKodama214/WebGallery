package com.web.gallery.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.test.context.ActiveProfiles;

import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.photo.PhotoNo;
import com.web.gallery.domain.photo.TagEnglishName;
import com.web.gallery.domain.photo.TagJapaneseName;
import com.web.gallery.domain.photo.TagNo;

@ActiveProfiles("test")
public class PhotoTagModelListTest {

	@Nested
	@Order(1)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class filterByPhoto {
		@Test
		@Order(1)
		@DisplayName("正常系：該当写真のタグのみに絞り込まれること")
		void filterByPhoto_success() {
			PhotoTagModelList photoTagModelList = PhotoTagModelList.of(List.of(
					PhotoTagModel.builder()
							.accountNo(new AccountNo(1L))
							.photoNo(new PhotoNo(1L))
							.tagNo(new TagNo(1L))
							.tagJapaneseName(new TagJapaneseName("太陽"))
							.tagEnglishName(new TagEnglishName("sun"))
							.build(),
					PhotoTagModel.builder()
							.accountNo(new AccountNo(1L))
							.photoNo(new PhotoNo(2L))
							.tagNo(new TagNo(1L))
							.tagJapaneseName(new TagJapaneseName("海"))
							.tagEnglishName(new TagEnglishName("sea"))
							.build()));

			PhotoTagModelList actual = photoTagModelList.filterByPhoto(new AccountNo(1L), new PhotoNo(1L));

			assertEquals(1, actual.size());
			assertEquals(new PhotoNo(1L), actual.get(0).getPhotoNo());
		}
	}
}
