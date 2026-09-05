package com.web.gallery.repository.impl.integration;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.photo.PhotoNo;
import com.web.gallery.entity.PhotoFavorite;
import com.web.gallery.exception.FavoriteNotFoundException;
import com.web.gallery.exception.GalleryException;
import com.web.gallery.exception.RegistFailureException;
import com.web.gallery.model.PhotoFavoriteDeleteModel;
import com.web.gallery.model.PhotoFavoriteModel;
import com.web.gallery.repository.impl.PhotoFavoriteRepositoryImpl;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@Transactional
public class PhotoFavoriteRepositoryImplIntegrationTest {
  @Autowired private PhotoFavoriteRepositoryImpl photoFavoriteRepositoryImpl;

  @Autowired private JdbcTemplate jdbcTemplate;

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/repository/PhotoFavoriteRepositoryImplIntegrationTest.sql")
  class regist {
    @Test
    @Order(1)
    @DisplayName("正常系")
    void regist_contain_null_parameter() throws GalleryException {
      PhotoFavoriteModel favoriteModel =
          PhotoFavoriteModel.builder()
              .accountNo(new AccountNo(1L))
              .favoritePhotoAccountNo(new AccountNo(2L))
              .favoritePhotoNo(new PhotoNo(1L))
              .build();

      OffsetDateTime transactionNow =
          jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);
      photoFavoriteRepositoryImpl.regist(favoriteModel);

      List<PhotoFavorite> actualData =
          jdbcTemplate.query(
              "SELECT * FROM photo.photo_favorite WHERE account_no=1 and favorite_photo_account_no=2 and favorite_photo_no=1",
              (rs, rowNum) ->
                  PhotoFavorite.builder()
                      .accountNo(rs.getLong("account_no"))
                      .favoritePhotoAccountNo(rs.getLong("favorite_photo_account_no"))
                      .favoritePhotoNo(rs.getLong("favorite_photo_no"))
                      .createdBy(rs.getLong("created_by"))
                      .createdAt(rs.getObject("created_at", OffsetDateTime.class))
                      .build());
      assertEquals(1, actualData.size());
      assertEquals(1L, actualData.getFirst().getAccountNo());
      assertEquals(2L, actualData.getFirst().getFavoritePhotoAccountNo());
      assertEquals(1L, actualData.getFirst().getFavoritePhotoNo());
      assertEquals(1L, actualData.getFirst().getCreatedBy());
      assertEquals(transactionNow, actualData.getFirst().getCreatedAt());
    }

    @Test
    @Order(2)
    @DisplayName("異常系：RegistFailureExceptionをthrowする")
    void regist_RegistFailureException() {
      PhotoFavoriteModel favoriteModel =
          PhotoFavoriteModel.builder()
              .accountNo(new AccountNo(1L))
              .favoritePhotoAccountNo(new AccountNo(1L))
              .favoritePhotoNo(new PhotoNo(1L))
              .build();

      assertThrows(
          RegistFailureException.class, () -> photoFavoriteRepositoryImpl.regist(favoriteModel));
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/repository/PhotoFavoriteRepositoryImplIntegrationTest.sql")
  class delete {
    @Test
    @Order(1)
    @DisplayName("正常系")
    void delete_contain_null_parameter() throws GalleryException {
      PhotoFavoriteDeleteModel favoriteDeleteModel =
          PhotoFavoriteDeleteModel.builder()
              .accountNo(new AccountNo(1L))
              .favoritePhotoAccountNo(new AccountNo(1L))
              .favoritePhotoNo(new PhotoNo(1L))
              .build();

      photoFavoriteRepositoryImpl.delete(favoriteDeleteModel);

      List<PhotoFavorite> actualData =
          jdbcTemplate.query(
              "SELECT * FROM photo.photo_favorite WHERE account_no=1 and favorite_photo_account_no=1 and favorite_photo_no=1",
              (rs, rowNum) ->
                  PhotoFavorite.builder()
                      .accountNo(rs.getLong("account_no"))
                      .favoritePhotoAccountNo(rs.getLong("favorite_photo_account_no"))
                      .favoritePhotoNo(rs.getLong("favorite_photo_no"))
                      .createdBy(rs.getLong("created_by"))
                      .createdAt(rs.getObject("created_at", OffsetDateTime.class))
                      .build());
      assertEquals(0, actualData.size());

      List<PhotoFavorite> actualRestData =
          jdbcTemplate.query(
              "SELECT * FROM photo.photo_favorite",
              (rs, rowNum) ->
                  PhotoFavorite.builder()
                      .accountNo(rs.getLong("account_no"))
                      .favoritePhotoAccountNo(rs.getLong("favorite_photo_account_no"))
                      .favoritePhotoNo(rs.getLong("favorite_photo_no"))
                      .createdBy(rs.getLong("created_by"))
                      .createdAt(rs.getObject("created_at", OffsetDateTime.class))
                      .build());
      assertEquals(3, actualRestData.size());
    }

    @Test
    @Order(2)
    @DisplayName("異常系：対象のお気に入りが存在しない場合、FavoriteNotFoundExceptionをthrowする")
    void delete_FavoriteNotFoundException() throws GalleryException {
      PhotoFavoriteDeleteModel favoriteDeleteModel =
          PhotoFavoriteDeleteModel.builder()
              .accountNo(new AccountNo(1L))
              .favoritePhotoAccountNo(new AccountNo(3L))
              .favoritePhotoNo(new PhotoNo(1L))
              .build();

      assertThrows(
          FavoriteNotFoundException.class,
          () -> photoFavoriteRepositoryImpl.delete(favoriteDeleteModel));
    }
  }

  @Nested
  @Order(3)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/repository/PhotoFavoriteRepositoryImplIntegrationTest.sql")
  class clear {
    @Test
    @Order(1)
    @DisplayName("正常系：")
    void clear_success() {
      PhotoFavoriteDeleteModel favoriteDeleteModel =
          PhotoFavoriteDeleteModel.builder()
              .favoritePhotoAccountNo(new AccountNo(1L))
              .favoritePhotoNo(new PhotoNo(1L))
              .build();

      photoFavoriteRepositoryImpl.clear(favoriteDeleteModel);

      List<PhotoFavorite> actualData =
          jdbcTemplate.query(
              "SELECT * FROM photo.photo_favorite WHERE favorite_photo_account_no=1 and favorite_photo_no=1",
              (rs, rowNum) ->
                  PhotoFavorite.builder()
                      .accountNo(rs.getLong("account_no"))
                      .favoritePhotoAccountNo(rs.getLong("favorite_photo_account_no"))
                      .favoritePhotoNo(rs.getLong("favorite_photo_no"))
                      .createdBy(rs.getLong("created_by"))
                      .createdAt(rs.getObject("created_at", OffsetDateTime.class))
                      .build());
      assertEquals(0, actualData.size());

      List<PhotoFavorite> actualRestData =
          jdbcTemplate.query(
              "SELECT * FROM photo.photo_favorite",
              (rs, rowNum) ->
                  PhotoFavorite.builder()
                      .accountNo(rs.getLong("account_no"))
                      .favoritePhotoAccountNo(rs.getLong("favorite_photo_account_no"))
                      .favoritePhotoNo(rs.getLong("favorite_photo_no"))
                      .createdBy(rs.getLong("created_by"))
                      .createdAt(rs.getObject("created_at", OffsetDateTime.class))
                      .build());
      assertEquals(3, actualRestData.size());
    }
  }
}
