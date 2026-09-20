package com.web.gallery.model;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.controller.request.PhotoListRequest;
import com.web.gallery.domain.account.AccountId;
import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.common.IpAddress;
import com.web.gallery.domain.common.Referer;
import com.web.gallery.enumeration.DirectionEnum;
import com.web.gallery.enumeration.SortPhotoEnum;
import java.util.Collections;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class PhotoListGetModelTest {

  private static final IpAddress IP_ADDRESS = new IpAddress("203.0.113.1");
  private static final Referer REFERER = new Referer("https://example.com/");

  private PhotoListRequest baseRequest() {
    PhotoListRequest request = new PhotoListRequest();
    request.setDirectionKbn(DirectionEnum.VERTICAL);
    request.setIsFavorite(true);
    request.setTagList("太陽 海");
    request.setSortBy(SortPhotoEnum.SEASON);
    request.setPageNo(2);
    request.setSearchExecuted(true);
    request.setLogInitialView(true);
    return request;
  }

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class from {
    @Test
    @Order(1)
    @DisplayName("正常系：全項目が設定されている場合、そのまま値が反映されること")
    void from_allFieldsSet() {
      PhotoListRequest request = baseRequest();

      PhotoListGetModel actual =
          PhotoListGetModel.from(request, 1L, "aaaaaaaa", IP_ADDRESS, REFERER);

      assertEquals(new AccountNo(1L), actual.getAccountNo());
      assertEquals(new AccountId("aaaaaaaa"), actual.getPhotoAccountId());
      assertEquals(DirectionEnum.VERTICAL, actual.getDirectionKbn());
      assertTrue(actual.getIsFavoriteOnly().value());
      assertEquals(2, actual.getTagList().size());
      assertEquals("太陽", actual.getTagList().get(0));
      assertEquals("海", actual.getTagList().get(1));
      assertEquals(SortPhotoEnum.SEASON, actual.getSortBy());
      assertEquals(2, actual.getPageNo());
      assertTrue(actual.getSearchExecuted());
      assertTrue(actual.getLogInitialView());
      assertEquals(IP_ADDRESS, actual.getIpAddress());
      assertEquals(REFERER, actual.getReferer());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：ログイン中のアカウント番号が未設定（null）の場合、nullが設定されること")
    void from_accountNoNull() {
      PhotoListRequest request = baseRequest();

      PhotoListGetModel actual =
          PhotoListGetModel.from(request, null, "aaaaaaaa", IP_ADDRESS, REFERER);

      assertNull(actual.getAccountNo());
    }

    @Test
    @Order(3)
    @DisplayName("正常系：お気に入りのみフィルタ・検索実行・初回閲覧ログの各フラグが未設定の場合、falseが設定されること")
    void from_optionalFlagsNull() {
      PhotoListRequest request = baseRequest();
      request.setIsFavorite(null);
      request.setSearchExecuted(null);
      request.setLogInitialView(null);

      PhotoListGetModel actual =
          PhotoListGetModel.from(request, 1L, "aaaaaaaa", IP_ADDRESS, REFERER);

      assertFalse(actual.getIsFavoriteOnly().value());
      assertFalse(actual.getSearchExecuted());
      assertFalse(actual.getLogInitialView());
    }

    @Test
    @Order(4)
    @DisplayName("正常系：タグリストが未設定（null）の場合、空リストが設定されること")
    void from_tagListNull() {
      PhotoListRequest request = baseRequest();
      request.setTagList(null);

      PhotoListGetModel actual =
          PhotoListGetModel.from(request, 1L, "aaaaaaaa", IP_ADDRESS, REFERER);

      assertTrue(actual.getTagList().isEmpty());
    }

    @Test
    @Order(5)
    @DisplayName("正常系：タグリストの全角スペース区切りが半角スペースとして分割されること")
    void from_tagListFullSpaceSeparator() {
      PhotoListRequest request = baseRequest();
      request.setTagList("太陽　海");

      PhotoListGetModel actual =
          PhotoListGetModel.from(request, 1L, "aaaaaaaa", IP_ADDRESS, REFERER);

      assertEquals(2, actual.getTagList().size());
      assertEquals("太陽", actual.getTagList().get(0));
      assertEquals("海", actual.getTagList().get(1));
    }

    @Test
    @Order(6)
    @DisplayName("正常系：連続した空白による空文字トークンが除外されること")
    void from_tagListIgnoresBlankTokens() {
      PhotoListRequest request = baseRequest();
      request.setTagList("太陽" + "　".repeat(5) + "海");

      PhotoListGetModel actual =
          PhotoListGetModel.from(request, 1L, "aaaaaaaa", IP_ADDRESS, REFERER);

      assertEquals(2, actual.getTagList().size());
    }

    @Test
    @Order(7)
    @DisplayName("正常系：タグ指定数が上限（20件）を超える場合、上限件数までに切り詰められること")
    void from_tagListExceedsMaxSize() {
      PhotoListRequest request = baseRequest();
      request.setTagList(String.join(" ", Collections.nCopies(25, "tag")));

      PhotoListGetModel actual =
          PhotoListGetModel.from(request, 1L, "aaaaaaaa", IP_ADDRESS, REFERER);

      assertEquals(20, actual.getTagList().size());
    }
  }
}
