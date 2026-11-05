package com.web.gallery.infrastructure.persistence.mapper.photo;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.domain.enumeration.DirectionEnum;
import com.web.gallery.infrastructure.persistence.dto.photo.AdminPhotoDto;
import com.web.gallery.infrastructure.persistence.dto.photo.AdminPhotoListGetDto;
import com.web.gallery.infrastructure.persistence.dto.photo.PhotoDetailDto;
import com.web.gallery.infrastructure.persistence.dto.photo.PhotoDetailGetDto;
import com.web.gallery.infrastructure.persistence.dto.photo.PhotoDto;
import com.web.gallery.infrastructure.persistence.dto.photo.PhotoListGetDto;
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

@MybatisTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class PhotoDetailMapperTest {
  @Autowired private PhotoDetailMapper photoDetailMapper;

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

      PhotoDto actualPhotoDto1 =
          actual.stream().sorted(Comparator.comparing(PhotoDto::getPhotoNo)).toList().getFirst();
      assertEquals(1L, actualPhotoDto1.getAccountNo());
      assertEquals(1L, actualPhotoDto1.getPhotoNo());
      assertEquals(2, actualPhotoDto1.getFavoriteCount());
      assertEquals(true, actualPhotoDto1.getIsFavorite());
      assertEquals(
          OffsetDateTime.of(2021, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)),
          actualPhotoDto1.getPhotoAt());
      assertEquals("https://www.xxx.com/DSC111.jpg", actualPhotoDto1.getImageFilePath());
      assertEquals("キャプション11", actualPhotoDto1.getCaption());
      assertEquals(DirectionEnum.VERTICAL, actualPhotoDto1.getDirectionKbn());

      PhotoDto actualPhotoDto2 =
          actual.stream().sorted(Comparator.comparing(PhotoDto::getPhotoNo)).toList().getLast();
      assertEquals(1L, actualPhotoDto2.getAccountNo());
      assertEquals(2L, actualPhotoDto2.getPhotoNo());
      assertEquals(1, actualPhotoDto2.getFavoriteCount());
      assertEquals(false, actualPhotoDto2.getIsFavorite());
      assertEquals(
          OffsetDateTime.of(2021, 2, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)),
          actualPhotoDto2.getPhotoAt());
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

    @Test
    @Order(5)
    @DisplayName("正常系：季節・時期順で月日が同じ場合、年の降順に並び替えられること")
    void getPhotoList_sortBy_season_sameMonthDay_sortedByYearDesc() {
      // account4の写真は12/25が3件（2021・2022・2023年）と11/20が1件のため、
      // 季節順では12/25のグループ内が年降順（2023→2022→2021）になり、その後11/20が続くこと
      PhotoListGetDto photoSelectDto = new PhotoListGetDto();
      photoSelectDto.setAccountNo(4L);
      photoSelectDto.setPhotoAccountNo(4L);
      photoSelectDto.setSortBy("SEASON");
      photoSelectDto.setLimit(100);
      photoSelectDto.setOffset(0);

      List<PhotoDto> actual = photoDetailMapper.getPhotoList(photoSelectDto);
      assertEquals(4, actual.size());
      assertEquals(3L, actual.get(0).getPhotoNo());
      assertEquals(2L, actual.get(1).getPhotoNo());
      assertEquals(1L, actual.get(2).getPhotoNo());
      assertEquals(4L, actual.get(3).getPhotoNo());
    }

    @Test
    @Order(6)
    @DisplayName("正常系：向き区分で絞り込まれること")
    void getPhotoList_filterByDirectionKbn() {
      PhotoListGetDto photoSelectDto = new PhotoListGetDto();
      photoSelectDto.setAccountNo(1L);
      photoSelectDto.setPhotoAccountNo(1L);
      photoSelectDto.setDirectionKbn(DirectionEnum.VERTICAL);
      photoSelectDto.setLimit(100);
      photoSelectDto.setOffset(0);

      List<PhotoDto> actual = photoDetailMapper.getPhotoList(photoSelectDto);

      assertEquals(1, actual.size());
      assertEquals(1L, actual.getFirst().getPhotoNo());
      assertEquals(DirectionEnum.VERTICAL, actual.getFirst().getDirectionKbn());
    }

    @Test
    @Order(7)
    @DisplayName("正常系：お気に入りのみに絞り込まれること")
    void getPhotoList_filterByFavorite() {
      // account1自身がお気に入りしているのはphoto_no=1のみ（photo_no=2はaccount2のみがお気に入り）
      PhotoListGetDto photoSelectDto = new PhotoListGetDto();
      photoSelectDto.setAccountNo(1L);
      photoSelectDto.setPhotoAccountNo(1L);
      photoSelectDto.setIsFavoriteOnly(true);
      photoSelectDto.setLimit(100);
      photoSelectDto.setOffset(0);

      List<PhotoDto> actual = photoDetailMapper.getPhotoList(photoSelectDto);

      assertEquals(1, actual.size());
      assertEquals(1L, actual.getFirst().getPhotoNo());
      assertTrue(actual.getFirst().getIsFavorite());
    }

    @Test
    @Order(8)
    @DisplayName("正常系：タグをすべて保持する写真のみに絞り込まれること")
    void getPhotoList_filterByTags() {
      // 「太陽」「青空」を両方保持するのはphoto_no=1のみ（photo_no=2は「太陽」のみ保持）
      PhotoListGetDto photoSelectDto = new PhotoListGetDto();
      photoSelectDto.setAccountNo(1L);
      photoSelectDto.setPhotoAccountNo(1L);
      photoSelectDto.setTagList(List.of("太陽", "青空"));
      photoSelectDto.setLimit(100);
      photoSelectDto.setOffset(0);

      List<PhotoDto> actual = photoDetailMapper.getPhotoList(photoSelectDto);

      assertEquals(1, actual.size());
      assertEquals(1L, actual.getFirst().getPhotoNo());
    }

    @Test
    @Order(9)
    @DisplayName("正常系：お気に入り数の降順に並び替えられること（撮影日時降順とは逆順になること）")
    void getPhotoList_sortBy_favorite() {
      // 撮影日時降順ではphoto_no=2が先だが、お気に入り数（photo_no=1が2件、photo_no=2が1件）順では逆転する
      PhotoListGetDto photoSelectDto = new PhotoListGetDto();
      photoSelectDto.setAccountNo(1L);
      photoSelectDto.setPhotoAccountNo(1L);
      photoSelectDto.setSortBy("FAVORITE");
      photoSelectDto.setLimit(100);
      photoSelectDto.setOffset(0);

      List<PhotoDto> actual = photoDetailMapper.getPhotoList(photoSelectDto);

      assertEquals(2, actual.size());
      assertEquals(1L, actual.get(0).getPhotoNo());
      assertEquals(2, actual.get(0).getFavoriteCount());
      assertEquals(2L, actual.get(1).getPhotoNo());
      assertEquals(1, actual.get(1).getFavoriteCount());
    }

    @Test
    @Order(10)
    @DisplayName("正常系：所有者本人以外が閲覧する場合、管理者により非公開にされた写真は含めないこと")
    void getPhotoList_hiddenByAdmin_excluded_for_others() {
      PhotoListGetDto photoSelectDto = new PhotoListGetDto();
      photoSelectDto.setAccountNo(1L);
      photoSelectDto.setPhotoAccountNo(2L);
      photoSelectDto.setIncludeHiddenByAdmin(false);
      photoSelectDto.setLimit(100);
      photoSelectDto.setOffset(0);

      List<PhotoDto> actual = photoDetailMapper.getPhotoList(photoSelectDto);

      assertEquals(List.of(1L, 2L), actual.stream().map(PhotoDto::getPhotoNo).sorted().toList());
      assertTrue(actual.stream().noneMatch(PhotoDto::getIsHiddenByAdmin));
    }

    @Test
    @Order(11)
    @DisplayName("正常系：未ログインで閲覧する場合、管理者により非公開にされた写真は含めないこと")
    void getPhotoList_hiddenByAdmin_excluded_for_anonymous() {
      PhotoListGetDto photoSelectDto = new PhotoListGetDto();
      photoSelectDto.setAccountNo(null);
      photoSelectDto.setPhotoAccountNo(2L);
      photoSelectDto.setIncludeHiddenByAdmin(false);
      photoSelectDto.setLimit(100);
      photoSelectDto.setOffset(0);

      List<PhotoDto> actual = photoDetailMapper.getPhotoList(photoSelectDto);

      assertEquals(List.of(1L, 2L), actual.stream().map(PhotoDto::getPhotoNo).sorted().toList());
    }

    @Test
    @Order(12)
    @DisplayName("正常系：所有者本人が閲覧する場合、管理者により非公開にされた写真も含め、管理者非公開フラグを返すこと")
    void getPhotoList_hiddenByAdmin_included_for_owner() {
      PhotoListGetDto photoSelectDto = new PhotoListGetDto();
      photoSelectDto.setAccountNo(2L);
      photoSelectDto.setPhotoAccountNo(2L);
      photoSelectDto.setIncludeHiddenByAdmin(true);
      photoSelectDto.setLimit(100);
      photoSelectDto.setOffset(0);

      List<PhotoDto> actual = photoDetailMapper.getPhotoList(photoSelectDto);

      List<PhotoDto> sorted =
          actual.stream().sorted(Comparator.comparing(PhotoDto::getPhotoNo)).toList();
      assertEquals(List.of(1L, 2L, 3L), sorted.stream().map(PhotoDto::getPhotoNo).toList());
      assertFalse(sorted.get(0).getIsHiddenByAdmin());
      assertFalse(sorted.get(1).getIsHiddenByAdmin());
      assertTrue(sorted.get(2).getIsHiddenByAdmin());
    }

    @Test
    @Order(13)
    @DisplayName("正常系：お気に入り数順の場合も、所有者本人以外には管理者により非公開にされた写真を含めないこと")
    void getPhotoList_sortBy_favorite_hiddenByAdmin_excluded() {
      PhotoListGetDto photoSelectDto = new PhotoListGetDto();
      photoSelectDto.setAccountNo(1L);
      photoSelectDto.setPhotoAccountNo(2L);
      photoSelectDto.setIncludeHiddenByAdmin(false);
      photoSelectDto.setSortBy("FAVORITE");
      photoSelectDto.setLimit(100);
      photoSelectDto.setOffset(0);

      List<PhotoDto> actual = photoDetailMapper.getPhotoList(photoSelectDto);

      assertEquals(List.of(1L, 2L), actual.stream().map(PhotoDto::getPhotoNo).sorted().toList());
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
      assertEquals(
          OffsetDateTime.of(2021, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actual.getPhotoAt());
      assertEquals(1L, actual.getLocationNo());
      assertEquals("住所1", actual.getAddress());
      assertEquals(0, BigDecimal.valueOf(38.1).compareTo(actual.getLatitude()));
      assertEquals(0, BigDecimal.valueOf(115.1).compareTo(actual.getLongitude()));
      assertEquals("ロケーション1", actual.getDisplayName());
      assertEquals("https://www.xxx.com/DSC111.jpg", actual.getImageFilePath());
      assertEquals("タイトル11", actual.getPhotoJapaneseTitle());
      assertEquals("title11", actual.getPhotoEnglishTitle());
      assertEquals("キャプション11", actual.getCaption());
      assertEquals(DirectionEnum.VERTICAL, actual.getDirectionKbn());
      assertEquals(24, actual.getFocalLength());
      assertEquals(0, BigDecimal.valueOf(8.0).compareTo(actual.getFValue()));
      assertEquals(0, BigDecimal.valueOf(1).compareTo(actual.getShutterSpeed()));
      assertEquals(100, actual.getIso());
      assertEquals(true, actual.getIsLocationPublic());
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

    @Test
    @Order(3)
    @DisplayName("正常系：所有者本人以外が閲覧する場合、管理者により非公開にされた写真は取得できないこと")
    void getPhotoDetail_hiddenByAdmin_not_found_for_others() {
      PhotoDetailGetDto photoGetDto = new PhotoDetailGetDto();
      photoGetDto.setAccountNo(1L);
      photoGetDto.setPhotoAccountNo(2L);
      photoGetDto.setPhotoNo(3L);
      photoGetDto.setIncludeHiddenByAdmin(false);

      assertNull(photoDetailMapper.getPhotoDetail(photoGetDto));
    }

    @Test
    @Order(4)
    @DisplayName("正常系：所有者本人が閲覧する場合、管理者により非公開にされた写真も取得でき、管理者非公開フラグがtrueであること")
    void getPhotoDetail_hiddenByAdmin_found_for_owner() {
      PhotoDetailGetDto photoGetDto = new PhotoDetailGetDto();
      photoGetDto.setAccountNo(2L);
      photoGetDto.setPhotoAccountNo(2L);
      photoGetDto.setPhotoNo(3L);
      photoGetDto.setIncludeHiddenByAdmin(true);

      PhotoDetailDto actual = photoDetailMapper.getPhotoDetail(photoGetDto);

      assertEquals(3L, actual.getPhotoNo());
      assertEquals("タイトル23", actual.getPhotoJapaneseTitle());
      assertTrue(actual.getIsHiddenByAdmin());
    }

    @Test
    @Order(5)
    @DisplayName("正常系：管理者により非公開にされていない写真は、管理者非公開フラグがfalseであること")
    void getPhotoDetail_not_hidden() {
      PhotoDetailGetDto photoGetDto = new PhotoDetailGetDto();
      photoGetDto.setAccountNo(1L);
      photoGetDto.setPhotoAccountNo(2L);
      photoGetDto.setPhotoNo(1L);
      photoGetDto.setIncludeHiddenByAdmin(false);

      assertFalse(photoDetailMapper.getPhotoDetail(photoGetDto).getIsHiddenByAdmin());
    }
  }

  @Nested
  @Order(3)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/mapper/PhotoDetailMapperTest.sql")
  class getPhotoListForAdmin {
    @Test
    @Order(1)
    @DisplayName("正常系：絞り込みなしの場合、全アカウントの削除されていない写真を、管理者により非公開にされた写真も含めて登録日時の新しい順に取得すること")
    void getPhotoListForAdmin_all() {
      AdminPhotoListGetDto dto = new AdminPhotoListGetDto();
      dto.setLimit(100);
      dto.setOffset(0);

      List<AdminPhotoDto> actual = photoDetailMapper.getPhotoListForAdmin(dto);

      // 論理削除済みのaccount1・photo3は含めない。登録日時が同じ場合はアカウント番号の昇順・写真番号の降順
      assertEquals(
          List.of("2-3", "1-2", "1-1", "2-2", "2-1", "4-4", "4-3", "4-2", "4-1"),
          actual.stream().map(dto1 -> dto1.getAccountNo() + "-" + dto1.getPhotoNo()).toList());

      AdminPhotoDto first = actual.getFirst();
      assertEquals(2L, first.getAccountNo());
      assertEquals("bbbbbbbb", first.getAccountId());
      assertEquals("BBBBBBBB", first.getAccountName());
      assertEquals(3L, first.getPhotoNo());
      assertEquals("タイトル23", first.getPhotoJapaneseTitle());
      assertEquals("https://www.xxx.com/DSC666.jpg", first.getImageFilePath());
      assertTrue(first.getIsHiddenByAdmin());
      assertEquals(
          OffsetDateTime.of(2000, 1, 3, 0, 0, 0, 0, ZoneOffset.ofHours(0)), first.getCreatedAt());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：管理者非公開フラグで絞り込んだ場合、該当する写真のみを取得すること")
    void getPhotoListForAdmin_filterByHidden() {
      AdminPhotoListGetDto dto = new AdminPhotoListGetDto();
      dto.setIsHiddenByAdmin(true);
      dto.setLimit(100);
      dto.setOffset(0);

      List<AdminPhotoDto> actual = photoDetailMapper.getPhotoListForAdmin(dto);

      assertEquals(1, actual.size());
      assertEquals(2L, actual.getFirst().getAccountNo());
      assertEquals(3L, actual.getFirst().getPhotoNo());
    }

    @Test
    @Order(3)
    @DisplayName("正常系：アカウントIDで絞り込んだ場合、そのアカウントの写真のみを取得すること")
    void getPhotoListForAdmin_filterByAccountId() {
      AdminPhotoListGetDto dto = new AdminPhotoListGetDto();
      dto.setAccountId("bbbbbbbb");
      dto.setLimit(100);
      dto.setOffset(0);

      List<AdminPhotoDto> actual = photoDetailMapper.getPhotoListForAdmin(dto);

      assertEquals(List.of(3L, 2L, 1L), actual.stream().map(AdminPhotoDto::getPhotoNo).toList());
    }

    @Test
    @Order(4)
    @DisplayName("正常系：取得件数上限と取得開始位置に従って取得すること")
    void getPhotoListForAdmin_paging() {
      AdminPhotoListGetDto dto = new AdminPhotoListGetDto();
      dto.setLimit(2);
      dto.setOffset(1);

      List<AdminPhotoDto> actual = photoDetailMapper.getPhotoListForAdmin(dto);

      assertEquals(
          List.of("1-2", "1-1"),
          actual.stream().map(dto1 -> dto1.getAccountNo() + "-" + dto1.getPhotoNo()).toList());
    }
  }
}
