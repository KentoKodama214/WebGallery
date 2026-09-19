package com.web.gallery.controller.request;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class PhotoListRequestTest {

  private PhotoListRequest requestWithTagList(String tagList) {
    PhotoListRequest request = new PhotoListRequest();
    request.setTagList(tagList);
    return request;
  }

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class isTagListSizeValid {
    @Test
    @Order(1)
    @DisplayName("正常系：tagListがnullの場合、trueを返すこと")
    void isTagListSizeValid_null() {
      PhotoListRequest request = requestWithTagList(null);

      assertTrue(request.isTagListSizeValid());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：tagListが空文字の場合、trueを返すこと")
    void isTagListSizeValid_blank() {
      PhotoListRequest request = requestWithTagList("");

      assertTrue(request.isTagListSizeValid());
    }

    @Test
    @Order(3)
    @DisplayName("正常系：タグ数が上限（20件）ちょうどの場合、trueを返すこと")
    void isTagListSizeValid_exactlyAtLimit() {
      String tagList = String.join(" ", Collections.nCopies(20, "tag"));
      PhotoListRequest request = requestWithTagList(tagList);

      assertTrue(request.isTagListSizeValid());
    }

    @Test
    @Order(4)
    @DisplayName("異常系：タグ数が上限（20件）を超える場合、falseを返すこと")
    void isTagListSizeValid_exceedsLimit() {
      String tagList = String.join(" ", Collections.nCopies(21, "tag"));
      PhotoListRequest request = requestWithTagList(tagList);

      assertFalse(request.isTagListSizeValid());
    }

    @Test
    @Order(5)
    @DisplayName("正常系：全角スペース区切りでも正しくタグ数がカウントされること")
    void isTagListSizeValid_fullSpaceSeparator() {
      String tagList = String.join("　", Collections.nCopies(20, "tag"));
      PhotoListRequest request = requestWithTagList(tagList);

      assertTrue(request.isTagListSizeValid());
    }

    @Test
    @Order(6)
    @DisplayName("正常系：連続した空白による空文字トークンはカウントから除外されること")
    void isTagListSizeValid_ignoresBlankTokens() {
      // 「太陽」＋全角スペース20個＋「海」。空文字トークンを除去すると実質2件のみとなること
      String tagList = "太陽" + "　".repeat(20) + "海";
      PhotoListRequest request = requestWithTagList(tagList);

      assertTrue(request.isTagListSizeValid());
    }
  }
}
