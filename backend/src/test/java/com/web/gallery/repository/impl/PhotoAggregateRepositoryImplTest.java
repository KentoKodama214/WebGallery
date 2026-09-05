package com.web.gallery.repository.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.web.gallery.aggregate.Photo;
import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.photo.ImageFile;
import com.web.gallery.domain.photo.ImageFilePath;
import com.web.gallery.domain.photo.PhotoNo;
import com.web.gallery.domain.photo.TagEnglishName;
import com.web.gallery.domain.photo.TagJapaneseName;
import com.web.gallery.entity.PhotoFavoriteCondition;
import com.web.gallery.entity.PhotoMst;
import com.web.gallery.entity.PhotoMstCondition;
import com.web.gallery.entity.PhotoMstUpdateTarget;
import com.web.gallery.entity.PhotoTagMst;
import com.web.gallery.entity.PhotoTagMstCondition;
import com.web.gallery.exception.FileDuplicateException;
import com.web.gallery.exception.GalleryException;
import com.web.gallery.exception.PhotoNotFoundException;
import com.web.gallery.exception.RegistFailureException;
import com.web.gallery.exception.UpdateFailureException;
import com.web.gallery.mapper.PhotoFavoriteMapper;
import com.web.gallery.mapper.PhotoMstMapper;
import com.web.gallery.mapper.PhotoTagMstMapper;
import com.web.gallery.model.PhotoDetailModel;
import com.web.gallery.model.PhotoTagModel;
import com.web.gallery.model.PhotoTagModelList;
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
import org.springframework.dao.DuplicateKeyException;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.multipart.MultipartFile;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class PhotoAggregateRepositoryImplTest {
  @InjectMocks private PhotoAggregateRepositoryImpl photoAggregateRepositoryImpl;

  @Mock private PhotoMstMapper photoMstMapper;

  @Mock private PhotoTagMstMapper photoTagMstMapper;

  @Mock private PhotoFavoriteMapper photoFavoriteMapper;

  private PhotoDetailModel buildDetail(
      AccountNo accountNo, PhotoNo photoNo, ImageFilePath imageFilePath, PhotoTagModelList tags) {
    MultipartFile multipartFile =
        new MockMultipartFile(
            "file", "DSC111.jpg", "multipart/form-data", "sample image".getBytes());
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
    @DisplayName("正常系：重複がなければ写真マスタ・タグを登録すること")
    void regist_success() throws GalleryException {
      AccountNo accountNo = new AccountNo(1L);
      PhotoNo photoNo = new PhotoNo(5L);
      ImageFilePath imageFilePath = new ImageFilePath("/path/DSC111.jpg");
      PhotoTagModelList tags =
          PhotoTagModelList.of(List.of(buildTag(accountNo, "太陽"), buildTag(accountNo, "海")));
      PhotoDetailModel requestDetail = buildDetail(accountNo, null, new ImageFilePath(""), tags);
      Photo photo = Photo.forRegist(requestDetail, photoNo, imageFilePath);

      doReturn(false).when(photoMstMapper).isExistPhoto(any(PhotoMstCondition.class));

      ArgumentCaptor<PhotoMst> photoMstCaptor = ArgumentCaptor.forClass(PhotoMst.class);
      doReturn(1).when(photoMstMapper).insert(photoMstCaptor.capture());

      @SuppressWarnings("unchecked")
      ArgumentCaptor<List<PhotoTagMst>> photoTagMstListCaptor = ArgumentCaptor.forClass(List.class);
      doReturn(2).when(photoTagMstMapper).insertBulk(photoTagMstListCaptor.capture());

      photoAggregateRepositoryImpl.regist(photo);

      verify(photoMstMapper).isExistPhoto(any(PhotoMstCondition.class));
      verify(photoMstMapper).insert(any(PhotoMst.class));
      verify(photoTagMstMapper).insertBulk(anyList());

      PhotoMst photoMstCapture = photoMstCaptor.getValue();
      assertEquals(accountNo.value(), photoMstCapture.getAccountNo());
      assertEquals(photoNo.value(), photoMstCapture.getPhotoNo());
      assertEquals(imageFilePath.value(), photoMstCapture.getImageFilePath());

      List<PhotoTagMst> photoTagMstCaptureList = photoTagMstListCaptor.getValue();
      assertEquals(2, photoTagMstCaptureList.size());
      assertEquals(1L, photoTagMstCaptureList.get(0).getTagNo());
      assertEquals(photoNo.value(), photoTagMstCaptureList.get(0).getPhotoNo());
      assertEquals(2L, photoTagMstCaptureList.get(1).getTagNo());
    }

    @Test
    @Order(2)
    @DisplayName("異常系：同じファイル名が既に存在する場合、FileDuplicateExceptionをthrowすること")
    void regist_duplicate() throws GalleryException {
      AccountNo accountNo = new AccountNo(1L);
      PhotoDetailModel requestDetail =
          buildDetail(accountNo, null, new ImageFilePath(""), PhotoTagModelList.empty());
      Photo photo =
          Photo.forRegist(requestDetail, new PhotoNo(5L), new ImageFilePath("/path/DSC111.jpg"));

      doReturn(true).when(photoMstMapper).isExistPhoto(any(PhotoMstCondition.class));

      assertThrows(FileDuplicateException.class, () -> photoAggregateRepositoryImpl.regist(photo));

      verify(photoMstMapper, times(0)).insert(any(PhotoMst.class));
      verify(photoTagMstMapper, times(0)).insertBulk(anyList());
    }

    @Test
    @Order(3)
    @DisplayName("異常系：写真マスタ登録でDuplicateKeyExceptionが発生した場合、RegistFailureExceptionをthrowすること")
    void regist_RegistFailureException() {
      AccountNo accountNo = new AccountNo(1L);
      PhotoDetailModel requestDetail =
          buildDetail(accountNo, null, new ImageFilePath(""), PhotoTagModelList.empty());
      Photo photo =
          Photo.forRegist(requestDetail, new PhotoNo(5L), new ImageFilePath("/path/DSC111.jpg"));

      doReturn(false).when(photoMstMapper).isExistPhoto(any(PhotoMstCondition.class));
      doThrow(DuplicateKeyException.class).when(photoMstMapper).insert(any(PhotoMst.class));

      assertThrows(RegistFailureException.class, () -> photoAggregateRepositoryImpl.regist(photo));

      verify(photoTagMstMapper, times(0)).insertBulk(anyList());
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
      PhotoDetailModel requestDetail =
          buildDetail(accountNo, photoNo, new ImageFilePath("/path/DSC111.jpg"), tags);
      Photo photo = Photo.forUpdate(requestDetail);

      ArgumentCaptor<PhotoMstCondition> conditionCaptor =
          ArgumentCaptor.forClass(PhotoMstCondition.class);
      ArgumentCaptor<PhotoMstUpdateTarget> targetCaptor =
          ArgumentCaptor.forClass(PhotoMstUpdateTarget.class);
      doReturn(1).when(photoMstMapper).update(conditionCaptor.capture(), targetCaptor.capture());

      ArgumentCaptor<PhotoTagMstCondition> tagConditionCaptor =
          ArgumentCaptor.forClass(PhotoTagMstCondition.class);
      doReturn(1).when(photoTagMstMapper).delete(tagConditionCaptor.capture());

      @SuppressWarnings("unchecked")
      ArgumentCaptor<List<PhotoTagMst>> photoTagMstListCaptor = ArgumentCaptor.forClass(List.class);
      doReturn(1).when(photoTagMstMapper).insertBulk(photoTagMstListCaptor.capture());

      photoAggregateRepositoryImpl.update(photo);

      verify(photoMstMapper).update(any(PhotoMstCondition.class), any(PhotoMstUpdateTarget.class));
      verify(photoTagMstMapper).delete(any(PhotoTagMstCondition.class));
      verify(photoTagMstMapper).insertBulk(anyList());

      assertEquals(accountNo.value(), conditionCaptor.getValue().getAccountNo());
      assertEquals(photoNo.value(), conditionCaptor.getValue().getPhotoNo());
      assertFalse(conditionCaptor.getValue().getIsDeleted());

      assertEquals(accountNo.value(), tagConditionCaptor.getValue().getAccountNo());
      assertEquals(photoNo.value(), tagConditionCaptor.getValue().getPhotoNo());
    }

    @Test
    @Order(2)
    @DisplayName("異常系：写真マスタの更新件数が0件の場合、UpdateFailureExceptionをthrowすること")
    void update_UpdateFailureException() {
      AccountNo accountNo = new AccountNo(1L);
      PhotoNo photoNo = new PhotoNo(5L);
      PhotoDetailModel requestDetail =
          buildDetail(
              accountNo, photoNo, new ImageFilePath("/path/DSC111.jpg"), PhotoTagModelList.empty());
      Photo photo = Photo.forUpdate(requestDetail);

      doReturn(0)
          .when(photoMstMapper)
          .update(any(PhotoMstCondition.class), any(PhotoMstUpdateTarget.class));

      assertThrows(UpdateFailureException.class, () -> photoAggregateRepositoryImpl.update(photo));

      verify(photoTagMstMapper, times(0)).delete(any(PhotoTagMstCondition.class));
    }
  }

  @Nested
  @Order(3)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class delete {
    @Test
    @Order(1)
    @DisplayName("正常系：お気に入り・タグを削除してから写真マスタを論理削除すること")
    void delete_success() throws GalleryException {
      AccountNo accountNo = new AccountNo(1L);
      PhotoNo photoNo = new PhotoNo(5L);
      ImageFilePath imageFilePathForDelete = new ImageFilePath("/path/DSC111.jpg");
      Photo photo = Photo.forDelete(accountNo, photoNo, imageFilePathForDelete);

      ArgumentCaptor<PhotoFavoriteCondition> favoriteConditionCaptor =
          ArgumentCaptor.forClass(PhotoFavoriteCondition.class);
      doReturn(1).when(photoFavoriteMapper).delete(favoriteConditionCaptor.capture());

      ArgumentCaptor<PhotoTagMstCondition> tagConditionCaptor =
          ArgumentCaptor.forClass(PhotoTagMstCondition.class);
      doReturn(1).when(photoTagMstMapper).delete(tagConditionCaptor.capture());

      ArgumentCaptor<PhotoMstCondition> conditionCaptor =
          ArgumentCaptor.forClass(PhotoMstCondition.class);
      ArgumentCaptor<PhotoMstUpdateTarget> targetCaptor =
          ArgumentCaptor.forClass(PhotoMstUpdateTarget.class);
      doReturn(1).when(photoMstMapper).update(conditionCaptor.capture(), targetCaptor.capture());

      photoAggregateRepositoryImpl.delete(photo);

      verify(photoFavoriteMapper).delete(any(PhotoFavoriteCondition.class));
      verify(photoTagMstMapper).delete(any(PhotoTagMstCondition.class));
      verify(photoMstMapper).update(any(PhotoMstCondition.class), any(PhotoMstUpdateTarget.class));

      assertNull(favoriteConditionCaptor.getValue().getAccountNo());
      assertEquals(
          accountNo.value(), favoriteConditionCaptor.getValue().getFavoritePhotoAccountNo());
      assertEquals(photoNo.value(), favoriteConditionCaptor.getValue().getFavoritePhotoNo());

      assertEquals(accountNo.value(), tagConditionCaptor.getValue().getAccountNo());
      assertEquals(photoNo.value(), tagConditionCaptor.getValue().getPhotoNo());

      assertEquals(accountNo.value(), conditionCaptor.getValue().getAccountNo());
      assertEquals(photoNo.value(), conditionCaptor.getValue().getPhotoNo());
      assertFalse(conditionCaptor.getValue().getIsDeleted());
      assertTrue(targetCaptor.getValue().getIsDeleted());
    }

    @Test
    @Order(2)
    @DisplayName("異常系：写真マスタの更新件数が0件（対象写真が存在しない）の場合、PhotoNotFoundExceptionをthrowすること")
    void delete_PhotoNotFoundException() {
      AccountNo accountNo = new AccountNo(1L);
      PhotoNo photoNo = new PhotoNo(5L);
      Photo photo = Photo.forDelete(accountNo, photoNo, new ImageFilePath("/path/DSC111.jpg"));

      doReturn(1).when(photoFavoriteMapper).delete(any(PhotoFavoriteCondition.class));
      doReturn(1).when(photoTagMstMapper).delete(any(PhotoTagMstCondition.class));
      doReturn(0)
          .when(photoMstMapper)
          .update(any(PhotoMstCondition.class), any(PhotoMstUpdateTarget.class));

      assertThrows(PhotoNotFoundException.class, () -> photoAggregateRepositoryImpl.delete(photo));
    }
  }
}
