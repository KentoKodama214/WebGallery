package com.web.gallery.repository.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.multipart.MultipartFile;

import com.web.gallery.aggregate.Photo;
import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.photo.ImageFile;
import com.web.gallery.domain.photo.ImageFilePath;
import com.web.gallery.domain.photo.PhotoNo;
import com.web.gallery.domain.photo.TagEnglishName;
import com.web.gallery.domain.photo.TagJapaneseName;
import com.web.gallery.domain.photo.TagNo;
import com.web.gallery.exception.FileDuplicateException;
import com.web.gallery.exception.GalleryException;
import com.web.gallery.model.FileModel;
import com.web.gallery.model.PhotoDeleteModel;
import com.web.gallery.model.PhotoDetailModel;
import com.web.gallery.model.PhotoFavoriteDeleteModel;
import com.web.gallery.model.PhotoTagDeleteModel;
import com.web.gallery.model.PhotoTagModel;
import com.web.gallery.model.PhotoTagModelList;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class PhotoAggregateRepositoryImplTest {
	@InjectMocks
	private PhotoAggregateRepositoryImpl photoAggregateRepositoryImpl;

	@Mock
	private PhotoMstRepositoryImpl photoMstRepositoryImpl;

	@Mock
	private PhotoTagMstRepositoryImpl photoTagMstRepositoryImpl;

	@Mock
	private PhotoFavoriteRepositoryImpl photoFavoriteRepositoryImpl;

	@Mock
	private FileRepositoryImpl fileRepositoryImpl;

	private PhotoDetailModel buildDetail(AccountNo accountNo, PhotoNo photoNo, ImageFilePath imageFilePath, PhotoTagModelList tags) {
		MultipartFile multipartFile = new MockMultipartFile(
				"file",
				"DSC111.jpg",
				"multipart/form-data",
				"sample image".getBytes());
		return PhotoDetailModel.builder()
				.accountNo(accountNo)
				.photoNo(photoNo)
				.imageFile(new ImageFile(multipartFile))
				.imageFilePath(imageFilePath)
				.photoTagModelList(tags)
				.build();
	}

	private PhotoTagModel buildTag(AccountNo accountNo, String japaneseName) {
		return PhotoTagModel.builder()
				.accountNo(accountNo)
				.tagJapaneseName(new TagJapaneseName(japaneseName))
				.tagEnglishName(new TagEnglishName(japaneseName))
				.build();
	}

	@Nested
	@Order(1)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class regist {
		@Test
		@Order(1)
		@DisplayName("正常系：重複がなければ写真マスタ・タグを登録し、ファイルを保存すること")
		void regist_success() throws GalleryException {
			AccountNo accountNo = new AccountNo(1L);
			PhotoNo photoNo = new PhotoNo(5L);
			ImageFilePath imageFilePath = new ImageFilePath("/path/DSC111.jpg");
			PhotoTagModelList tags = PhotoTagModelList.of(List.of(buildTag(accountNo, "太陽"), buildTag(accountNo, "海")));
			PhotoDetailModel requestDetail = buildDetail(accountNo, null, new ImageFilePath(""), tags);
			Photo photo = Photo.forRegist(requestDetail, photoNo, imageFilePath);

			doReturn(false).when(photoMstRepositoryImpl).isExistPhoto(any(PhotoDetailModel.class));

			ArgumentCaptor<PhotoTagModel> photoTagModelCaptor = ArgumentCaptor.forClass(PhotoTagModel.class);
			doNothing().when(photoTagMstRepositoryImpl).regist(photoTagModelCaptor.capture());

			ArgumentCaptor<FileModel> fileModelCaptor = ArgumentCaptor.forClass(FileModel.class);
			doNothing().when(fileRepositoryImpl).save(fileModelCaptor.capture());

			photoAggregateRepositoryImpl.regist(photo);

			verify(photoMstRepositoryImpl).isExistPhoto(any(PhotoDetailModel.class));
			verify(photoMstRepositoryImpl).regist(photo.getDetail(), imageFilePath, photoNo);
			verify(photoTagMstRepositoryImpl, times(2)).regist(any(PhotoTagModel.class));
			verify(fileRepositoryImpl).save(any(FileModel.class));

			List<PhotoTagModel> photoTagModelCaptureList = photoTagModelCaptor.getAllValues();
			assertEquals(new TagNo(1L), photoTagModelCaptureList.get(0).getTagNo());
			assertEquals(photoNo, photoTagModelCaptureList.get(0).getPhotoNo());
			assertEquals(new TagNo(2L), photoTagModelCaptureList.get(1).getTagNo());

			FileModel fileModelCapture = fileModelCaptor.getValue();
			assertEquals(imageFilePath, fileModelCapture.getFilePath());
		}

		@Test
		@Order(2)
		@DisplayName("異常系：同じファイル名が既に存在する場合、FileDuplicateExceptionをthrowすること")
		void regist_duplicate() throws GalleryException {
			AccountNo accountNo = new AccountNo(1L);
			PhotoDetailModel requestDetail = buildDetail(accountNo, null, new ImageFilePath(""), PhotoTagModelList.empty());
			Photo photo = Photo.forRegist(requestDetail, new PhotoNo(5L), new ImageFilePath("/path/DSC111.jpg"));

			doReturn(true).when(photoMstRepositoryImpl).isExistPhoto(any(PhotoDetailModel.class));

			assertThrows(FileDuplicateException.class, () -> photoAggregateRepositoryImpl.regist(photo));

			verify(photoMstRepositoryImpl, times(0)).regist(any(PhotoDetailModel.class), any(ImageFilePath.class), any(PhotoNo.class));
			verify(photoTagMstRepositoryImpl, times(0)).regist(any(PhotoTagModel.class));
			verify(fileRepositoryImpl, times(0)).save(any(FileModel.class));
		}
	}

	@Nested
	@Order(2)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class update {
		@Test
		@Order(1)
		@DisplayName("正常系：写真マスタを更新し、タグを全削除してから再登録すること")
		void update_success() throws GalleryException {
			AccountNo accountNo = new AccountNo(1L);
			PhotoNo photoNo = new PhotoNo(5L);
			PhotoTagModelList tags = PhotoTagModelList.of(List.of(buildTag(accountNo, "太陽")));
			PhotoDetailModel requestDetail = buildDetail(accountNo, photoNo, new ImageFilePath("/path/DSC111.jpg"), tags);
			Photo photo = Photo.forUpdate(requestDetail);

			ArgumentCaptor<PhotoTagDeleteModel> photoTagDeleteModelCaptor = ArgumentCaptor.forClass(PhotoTagDeleteModel.class);
			doNothing().when(photoTagMstRepositoryImpl).clear(photoTagDeleteModelCaptor.capture());

			ArgumentCaptor<PhotoTagModel> photoTagModelCaptor = ArgumentCaptor.forClass(PhotoTagModel.class);
			doNothing().when(photoTagMstRepositoryImpl).regist(photoTagModelCaptor.capture());

			photoAggregateRepositoryImpl.update(photo);

			verify(photoMstRepositoryImpl).update(photo.getDetail());
			verify(photoTagMstRepositoryImpl).clear(any(PhotoTagDeleteModel.class));
			verify(photoTagMstRepositoryImpl).regist(any(PhotoTagModel.class));

			PhotoTagDeleteModel photoTagDeleteModelCapture = photoTagDeleteModelCaptor.getValue();
			assertEquals(accountNo, photoTagDeleteModelCapture.getAccountNo());
			assertEquals(photoNo, photoTagDeleteModelCapture.getPhotoNo());
		}
	}

	@Nested
	@Order(3)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class delete {
		@Test
		@Order(1)
		@DisplayName("正常系：お気に入り・タグを削除してから写真マスタを論理削除し、実ファイルを削除すること")
		void delete_success() throws GalleryException {
			AccountNo accountNo = new AccountNo(1L);
			PhotoNo photoNo = new PhotoNo(5L);
			ImageFilePath imageFilePathForDelete = new ImageFilePath("/path/DSC111.jpg");
			Photo photo = Photo.forDelete(accountNo, photoNo, imageFilePathForDelete);

			ArgumentCaptor<PhotoFavoriteDeleteModel> photoFavoriteDeleteModelCaptor = ArgumentCaptor.forClass(PhotoFavoriteDeleteModel.class);
			doNothing().when(photoFavoriteRepositoryImpl).clear(photoFavoriteDeleteModelCaptor.capture());

			ArgumentCaptor<PhotoTagDeleteModel> photoTagDeleteModelCaptor = ArgumentCaptor.forClass(PhotoTagDeleteModel.class);
			doNothing().when(photoTagMstRepositoryImpl).clear(photoTagDeleteModelCaptor.capture());

			ArgumentCaptor<PhotoDeleteModel> photoDeleteModelCaptor = ArgumentCaptor.forClass(PhotoDeleteModel.class);
			doNothing().when(photoMstRepositoryImpl).delete(photoDeleteModelCaptor.capture());

			photoAggregateRepositoryImpl.delete(photo);

			verify(photoFavoriteRepositoryImpl).clear(any(PhotoFavoriteDeleteModel.class));
			verify(photoTagMstRepositoryImpl).clear(any(PhotoTagDeleteModel.class));
			verify(photoMstRepositoryImpl).delete(any(PhotoDeleteModel.class));
			verify(fileRepositoryImpl).delete(imageFilePathForDelete);

			PhotoFavoriteDeleteModel photoFavoriteDeleteModelCapture = photoFavoriteDeleteModelCaptor.getValue();
			assertNull(photoFavoriteDeleteModelCapture.getAccountNo());
			assertEquals(accountNo, photoFavoriteDeleteModelCapture.getFavoritePhotoAccountNo());
			assertEquals(photoNo, photoFavoriteDeleteModelCapture.getFavoritePhotoNo());

			PhotoDeleteModel photoDeleteModelCapture = photoDeleteModelCaptor.getValue();
			assertEquals(accountNo, photoDeleteModelCapture.getAccountNo());
			assertEquals(photoNo, photoDeleteModelCapture.getPhotoNo());
			assertEquals(imageFilePathForDelete, photoDeleteModelCapture.getImageFilePath());
		}
	}
}
