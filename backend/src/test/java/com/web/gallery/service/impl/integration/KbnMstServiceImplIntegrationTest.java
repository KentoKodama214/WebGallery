package com.web.gallery.service.impl.integration;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.domain.common.KbnCode;
import com.web.gallery.model.KbnMstModelList;
import com.web.gallery.service.impl.KbnMstServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@Transactional
public class KbnMstServiceImplIntegrationTest {
  @Autowired private KbnMstServiceImpl kbnMstServiceImpl;

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class getPrefectureList {
    @Test
    @Order(1)
    @DisplayName("正常系：区分マスタが存在する場合")
    @Sql("/sql/common/cleanup.sql")
    @Sql("/sql/service/KbnMstServiceImplIntegrationTest.sql")
    void getPrefectureList_found() {
      KbnMstModelList actual = kbnMstServiceImpl.getPrefectureList();
      assertEquals(47, actual.size());

      KbnMstModelList actualSorded = actual.sortBySortOrder();
      assertEquals(new KbnCode("Hokkaido"), actualSorded.get(0).getKbnCode());
      assertEquals(new KbnCode("Aomori"), actualSorded.get(1).getKbnCode());
      assertEquals(new KbnCode("Iwate"), actualSorded.get(2).getKbnCode());
      assertEquals(new KbnCode("Miyagi"), actualSorded.get(3).getKbnCode());
      assertEquals(new KbnCode("Akita"), actualSorded.get(4).getKbnCode());
      assertEquals(new KbnCode("Yamagata"), actualSorded.get(5).getKbnCode());
      assertEquals(new KbnCode("Fukushima"), actualSorded.get(6).getKbnCode());
      assertEquals(new KbnCode("Ibaraki"), actualSorded.get(7).getKbnCode());
      assertEquals(new KbnCode("Tochigi"), actualSorded.get(8).getKbnCode());
      assertEquals(new KbnCode("Gunma"), actualSorded.get(9).getKbnCode());
      assertEquals(new KbnCode("Saitama"), actualSorded.get(10).getKbnCode());
      assertEquals(new KbnCode("Chiba"), actualSorded.get(11).getKbnCode());
      assertEquals(new KbnCode("Tokyo"), actualSorded.get(12).getKbnCode());
      assertEquals(new KbnCode("Kanagawa"), actualSorded.get(13).getKbnCode());
      assertEquals(new KbnCode("Niigata"), actualSorded.get(14).getKbnCode());
      assertEquals(new KbnCode("Toyama"), actualSorded.get(15).getKbnCode());
      assertEquals(new KbnCode("Ishikawa"), actualSorded.get(16).getKbnCode());
      assertEquals(new KbnCode("Fukui"), actualSorded.get(17).getKbnCode());
      assertEquals(new KbnCode("Yamanashi"), actualSorded.get(18).getKbnCode());
      assertEquals(new KbnCode("Nagano"), actualSorded.get(19).getKbnCode());
      assertEquals(new KbnCode("Gifu"), actualSorded.get(20).getKbnCode());
      assertEquals(new KbnCode("Shizuoka"), actualSorded.get(21).getKbnCode());
      assertEquals(new KbnCode("Aichi"), actualSorded.get(22).getKbnCode());
      assertEquals(new KbnCode("Mie"), actualSorded.get(23).getKbnCode());
      assertEquals(new KbnCode("Shiga"), actualSorded.get(24).getKbnCode());
      assertEquals(new KbnCode("Kyoto"), actualSorded.get(25).getKbnCode());
      assertEquals(new KbnCode("Osaka"), actualSorded.get(26).getKbnCode());
      assertEquals(new KbnCode("Hyogo"), actualSorded.get(27).getKbnCode());
      assertEquals(new KbnCode("Nara"), actualSorded.get(28).getKbnCode());
      assertEquals(new KbnCode("Wakayama"), actualSorded.get(29).getKbnCode());
      assertEquals(new KbnCode("Tottori"), actualSorded.get(30).getKbnCode());
      assertEquals(new KbnCode("Shimane"), actualSorded.get(31).getKbnCode());
      assertEquals(new KbnCode("Okayama"), actualSorded.get(32).getKbnCode());
      assertEquals(new KbnCode("Hiroshima"), actualSorded.get(33).getKbnCode());
      assertEquals(new KbnCode("Yamaguchi"), actualSorded.get(34).getKbnCode());
      assertEquals(new KbnCode("Tokushima"), actualSorded.get(35).getKbnCode());
      assertEquals(new KbnCode("Kagawa"), actualSorded.get(36).getKbnCode());
      assertEquals(new KbnCode("Ehime"), actualSorded.get(37).getKbnCode());
      assertEquals(new KbnCode("Kochi"), actualSorded.get(38).getKbnCode());
      assertEquals(new KbnCode("Fukuoka"), actualSorded.get(39).getKbnCode());
      assertEquals(new KbnCode("Saga"), actualSorded.get(40).getKbnCode());
      assertEquals(new KbnCode("Nagasaki"), actualSorded.get(41).getKbnCode());
      assertEquals(new KbnCode("Kumamoto"), actualSorded.get(42).getKbnCode());
      assertEquals(new KbnCode("Oita"), actualSorded.get(43).getKbnCode());
      assertEquals(new KbnCode("Miyazaki"), actualSorded.get(44).getKbnCode());
      assertEquals(new KbnCode("Kagoshima"), actualSorded.get(45).getKbnCode());
      assertEquals(new KbnCode("Okinawa"), actualSorded.get(46).getKbnCode());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：区分マスタが存在しない場合")
    @Sql("/sql/common/cleanup.sql")
    void getPrefectureList_not_found() {
      KbnMstModelList actual = kbnMstServiceImpl.getPrefectureList();
      assertEquals(0, actual.size());
    }
  }
}
