package com.web.gallery.repository.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.web.gallery.domain.common.KbnClassCode;
import com.web.gallery.entity.KbnMst;
import com.web.gallery.entity.KbnMstCondition;
import com.web.gallery.mapper.KbnMstMapper;
import com.web.gallery.model.KbnMstModelList;
import java.util.ArrayList;
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
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class KbnMstRepositoryImplTest {
  @InjectMocks private KbnMstRepositoryImpl kbnMstRepositoryImpl;

  @Mock private KbnMstMapper kbnMstMapper;

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class get {
    @Test
    @Order(1)
    @DisplayName("正常系：区分マスタが取得できた場合")
    void get_found() {
      String kbnClassCode = "prefecture";

      List<KbnMst> expected = new ArrayList<KbnMst>();
      expected.add(
          KbnMst.builder()
              .kbnClassCode(kbnClassCode)
              .kbnCode("Hokkaido")
              .sortOrder(1)
              .kbnGroupCode("Hokkaido_Tohoku")
              .kbnClassJapaneseName("都道府県")
              .kbnGroupJapaneseName("北海道・東北")
              .kbnJapaneseName("北海道")
              .kbnClassEnglishName("prefecture")
              .kbnGroupEnglishName("Hokkaido_Tohoku")
              .kbnEnglishName("Hokkaido")
              .explanation("北海道はでっかいどう")
              .build());
      expected.add(
          KbnMst.builder()
              .kbnClassCode(kbnClassCode)
              .kbnCode("Okinawa")
              .sortOrder(47)
              .kbnGroupCode("Kyushu_Okinawa")
              .kbnClassJapaneseName("都道府県")
              .kbnGroupJapaneseName("九州・沖縄")
              .kbnJapaneseName("沖縄")
              .kbnClassEnglishName("prefecture")
              .kbnGroupEnglishName("Kyushu_Okinawa")
              .kbnEnglishName("Okinawa")
              .explanation("沖縄は南国")
              .build());

      ArgumentCaptor<KbnMstCondition> kbnMstCaptor = ArgumentCaptor.forClass(KbnMstCondition.class);
      doReturn(expected).when(kbnMstMapper).select(kbnMstCaptor.capture());

      KbnMstModelList actual = kbnMstRepositoryImpl.get(new KbnClassCode(kbnClassCode));

      KbnMstCondition kbnMstCapture = kbnMstCaptor.getValue();
      assertEquals(kbnClassCode, kbnMstCapture.getKbnClassCode());

      assertEquals(expected.size(), actual.size());
      assertEquals(expected.get(0).getKbnClassCode(), actual.get(0).getKbnClassCode().value());
      assertEquals(expected.get(0).getKbnCode(), actual.get(0).getKbnCode().value());
      assertEquals(expected.get(0).getSortOrder(), actual.get(0).getSortOrder().value());
      assertEquals(expected.get(0).getKbnGroupCode(), actual.get(0).getKbnGroupCode().value());
      assertEquals(
          expected.get(0).getKbnClassJapaneseName(),
          actual.get(0).getKbnClassJapaneseName().value());
      assertEquals(
          expected.get(0).getKbnGroupJapaneseName(),
          actual.get(0).getKbnGroupJapaneseName().value());
      assertEquals(
          expected.get(0).getKbnJapaneseName(), actual.get(0).getKbnJapaneseName().value());
      assertEquals(
          expected.get(0).getKbnClassEnglishName(), actual.get(0).getKbnClassEnglishName().value());
      assertEquals(
          expected.get(0).getKbnGroupEnglishName(), actual.get(0).getKbnGroupEnglishName().value());
      assertEquals(expected.get(0).getKbnEnglishName(), actual.get(0).getKbnEnglishName().value());
      assertEquals(expected.get(0).getExplanation(), actual.get(0).getExplanation().value());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：区分マスタが取得できなかった場合")
    void get_not_found() {
      String kbnClassCode = "prefecture";

      List<KbnMst> expected = new ArrayList<KbnMst>();
      ArgumentCaptor<KbnMstCondition> kbnMstCaptor = ArgumentCaptor.forClass(KbnMstCondition.class);
      doReturn(expected).when(kbnMstMapper).select(kbnMstCaptor.capture());

      KbnMstModelList actual = kbnMstRepositoryImpl.get(new KbnClassCode(kbnClassCode));

      KbnMstCondition kbnMstCapture = kbnMstCaptor.getValue();
      assertEquals(kbnClassCode, kbnMstCapture.getKbnClassCode());
      assertEquals(0, actual.size());
    }
  }
}
