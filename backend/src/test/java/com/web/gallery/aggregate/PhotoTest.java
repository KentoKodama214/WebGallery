package com.web.gallery.aggregate;

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
import com.web.gallery.domain.photo.Caption;
import com.web.gallery.domain.photo.ImageFilePath;
import com.web.gallery.domain.photo.PhotoNo;
import com.web.gallery.domain.photo.TagEnglishName;
import com.web.gallery.domain.photo.TagJapaneseName;
import com.web.gallery.domain.photo.TagNo;
import com.web.gallery.model.PhotoDetailModel;
import com.web.gallery.model.PhotoTagModel;
import com.web.gallery.model.PhotoTagModelList;

@ActiveProfiles("test")
public class PhotoTest {

	private PhotoDetailModel buildDetail(AccountNo accountNo, PhotoNo photoNo, PhotoTagModelList tags) {
		return PhotoDetailModel.builder()
				.accountNo(accountNo)
				.photoNo(photoNo)
				.imageFilePath(new ImageFilePath(""))
				.caption(new Caption("caption"))
				.photoTagModelList(tags)
				.build();
	}

	private PhotoTagModel buildTag(AccountNo accountNo, PhotoNo photoNo, Long tagNo, String japaneseName) {
		return PhotoTagModel.builder()
				.accountNo(accountNo)
				.photoNo(photoNo)
				.tagNo(tagNo != null ? new TagNo(tagNo) : null)
				.tagJapaneseName(new TagJapaneseName(japaneseName))
				.tagEnglishName(new TagEnglishName(japaneseName))
				.build();
	}

	@Nested
	@Order(1)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class forRegist {
		@Test
		@Order(1)
		@DisplayName("正常系：新規採番された写真番号・画像ファイルパスが反映され、タグ番号が1からの連番に振り直されること")
		void forRegist_success() {
			AccountNo accountNo = new AccountNo(1L);
			PhotoTagModelList tags = PhotoTagModelList.of(List.of(
					buildTag(accountNo, null, null, "太陽"),
					buildTag(accountNo, null, null, "海")));
			PhotoDetailModel requestDetail = buildDetail(accountNo, null, tags);
			ImageFilePath assignedImageFilePath = new ImageFilePath("/path/to/file.jpg");

			Photo photo = Photo.forRegist(requestDetail, new PhotoNo(10L), assignedImageFilePath);

			assertEquals(accountNo, photo.getAccountNo());
			assertEquals(new PhotoNo(10L), photo.getPhotoNo());
			assertEquals(assignedImageFilePath, photo.getImageFilePath());
			assertFalse(photo.isDeleted());
			assertEquals(2, photo.getPhotoTagModelList().size());
			assertEquals(new TagNo(1L), photo.getPhotoTagModelList().get(0).getTagNo());
			assertEquals(new PhotoNo(10L), photo.getPhotoTagModelList().get(0).getPhotoNo());
			assertEquals(new TagNo(2L), photo.getPhotoTagModelList().get(1).getTagNo());
		}

		@Test
		@Order(2)
		@DisplayName("セキュリティ：タグのアカウント番号は入力値ではなく写真所有者の値に強制されること")
		void forRegist_forces_owner_account_no_on_tags() {
			AccountNo ownerAccountNo = new AccountNo(1L);
			AccountNo attackerSuppliedAccountNo = new AccountNo(999L);
			PhotoTagModelList tags = PhotoTagModelList.of(List.of(
					buildTag(attackerSuppliedAccountNo, new PhotoNo(1L), 1L, "太陽"),
					buildTag(attackerSuppliedAccountNo, new PhotoNo(1L), 2L, "海")));
			PhotoDetailModel requestDetail = buildDetail(ownerAccountNo, null, tags);

			Photo photo = Photo.forRegist(requestDetail, new PhotoNo(10L), new ImageFilePath("/path/to/file.jpg"));

			assertEquals(ownerAccountNo, photo.getPhotoTagModelList().get(0).getAccountNo());
			assertEquals(ownerAccountNo, photo.getPhotoTagModelList().get(1).getAccountNo());
			assertEquals(new PhotoNo(10L), photo.getPhotoTagModelList().get(0).getPhotoNo());
			assertEquals(new PhotoNo(10L), photo.getPhotoTagModelList().get(1).getPhotoNo());
		}
	}

	@Nested
	@Order(2)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class forUpdate {
		@Test
		@Order(1)
		@DisplayName("正常系：既存の写真番号が維持され、タグ番号が1からの連番に振り直されること")
		void forUpdate_success() {
			AccountNo accountNo = new AccountNo(1L);
			PhotoNo photoNo = new PhotoNo(5L);
			PhotoTagModelList tags = PhotoTagModelList.of(List.of(
					buildTag(accountNo, photoNo, 3L, "太陽")));
			PhotoDetailModel requestDetail = buildDetail(accountNo, photoNo, tags);

			Photo photo = Photo.forUpdate(requestDetail);

			assertEquals(accountNo, photo.getAccountNo());
			assertEquals(photoNo, photo.getPhotoNo());
			assertFalse(photo.isDeleted());
			assertEquals(1, photo.getPhotoTagModelList().size());
			assertEquals(new TagNo(1L), photo.getPhotoTagModelList().get(0).getTagNo());
		}
	}

	@Nested
	@Order(3)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class forDelete {
		@Test
		@Order(1)
		@DisplayName("正常系：削除済みとしてマークされ、削除する画像ファイルパスが保持されること")
		void forDelete_success() {
			AccountNo accountNo = new AccountNo(1L);
			PhotoNo photoNo = new PhotoNo(5L);
			ImageFilePath imageFilePathForDelete = new ImageFilePath("/path/to/delete.jpg");

			Photo photo = Photo.forDelete(accountNo, photoNo, imageFilePathForDelete);

			assertEquals(accountNo, photo.getAccountNo());
			assertEquals(photoNo, photo.getPhotoNo());
			assertEquals(imageFilePathForDelete, photo.getImageFilePathForDelete());
			assertTrue(photo.isDeleted());
		}
	}

	@Nested
	@Order(4)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class updateTags {
		@Test
		@Order(1)
		@DisplayName("正常系：タグが新しいリストに差し替わり、タグ番号が1からの連番に振り直されること")
		void updateTags_success() {
			AccountNo accountNo = new AccountNo(1L);
			PhotoNo photoNo = new PhotoNo(5L);
			PhotoTagModelList initialTags = PhotoTagModelList.of(List.of(buildTag(accountNo, photoNo, 1L, "太陽")));
			Photo photo = Photo.forUpdate(buildDetail(accountNo, photoNo, initialTags));

			PhotoTagModelList newTags = PhotoTagModelList.of(List.of(
					buildTag(accountNo, photoNo, null, "海"),
					buildTag(accountNo, photoNo, null, "山")));
			photo.updateTags(newTags);

			assertEquals(2, photo.getPhotoTagModelList().size());
			assertEquals("海", photo.getPhotoTagModelList().get(0).getTagJapaneseName().value());
			assertEquals(new TagNo(1L), photo.getPhotoTagModelList().get(0).getTagNo());
			assertEquals("山", photo.getPhotoTagModelList().get(1).getTagJapaneseName().value());
			assertEquals(new TagNo(2L), photo.getPhotoTagModelList().get(1).getTagNo());
		}
	}
}
