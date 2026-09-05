package com.web.gallery.repository.impl.integration;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.domain.common.KbnClassCode;
import com.web.gallery.domain.common.KbnCode;
import com.web.gallery.model.KbnMstModelList;
import com.web.gallery.repository.impl.KbnMstRepositoryImpl;
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
public class KbnMstRepositoryImplIntegrationTest {
  @Autowired private KbnMstRepositoryImpl kbnMstRepositoryImpl;

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/repository/KbnMstRepositoryImplIntegrationTest.sql")
  class get {
    @Test
    @Order(1)
    @DisplayName("正常系：区分マスタが取得できた場合")
    void get_found() {
      KbnMstModelList actual = kbnMstRepositoryImpl.get(new KbnClassCode("sex"));
      assertEquals(2, actual.size());
      assertEquals(new KbnCode("man"), actual.get(0).getKbnCode());
      assertEquals(new KbnCode("woman"), actual.get(1).getKbnCode());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：区分マスタが取得できなかった場合")
    void get_not_found() {
      KbnMstModelList actual = kbnMstRepositoryImpl.get(new KbnClassCode("test"));
      assertEquals(0, actual.size());
    }
  }
}
