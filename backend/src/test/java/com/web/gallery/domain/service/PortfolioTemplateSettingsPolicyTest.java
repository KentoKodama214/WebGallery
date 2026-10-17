package com.web.gallery.domain.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verifyNoInteractions;

import com.web.gallery.domain.enumeration.PortfolioTemplateEnum;
import com.web.gallery.domain.model.photo.PhotoCount;
import com.web.gallery.domain.model.photo.TemplateSettings;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class PortfolioTemplateSettingsPolicyTest {

  @InjectMocks private PortfolioTemplateSettingsPolicy policy;

  @Mock private FeatureTemplateSettingsPolicy featureTemplateSettingsPolicy;

  @Mock private GridTemplateSettingsPolicy gridTemplateSettingsPolicy;

  private final TemplateSettings settings = new TemplateSettings(Map.of("key", "value"));

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class isValidTemplateSettings {
    @Test
    @Order(1)
    @DisplayName("正常系：フィーチャー型の場合、フィーチャー型のPolicyの判定結果を返すこと")
    void isValidTemplateSettings_feature() {
      doReturn(false).when(featureTemplateSettingsPolicy).isValidTemplateSettings(settings);

      assertFalse(policy.isValidTemplateSettings(PortfolioTemplateEnum.FEATURE, settings));
      verifyNoInteractions(gridTemplateSettingsPolicy);
    }

    @Test
    @Order(2)
    @DisplayName("正常系：グリッド型の場合、グリッド型のPolicyの判定結果を返すこと")
    void isValidTemplateSettings_grid() {
      doReturn(true).when(gridTemplateSettingsPolicy).isValidTemplateSettings(settings);

      assertTrue(policy.isValidTemplateSettings(PortfolioTemplateEnum.GRID, settings));
      verifyNoInteractions(featureTemplateSettingsPolicy);
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class isValidPhotoSettings {
    @Test
    @Order(1)
    @DisplayName("正常系：フィーチャー型の場合、フィーチャー型のPolicyの判定結果を返すこと")
    void isValidPhotoSettings_feature() {
      doReturn(true).when(featureTemplateSettingsPolicy).isValidPhotoSettings(settings);

      assertTrue(policy.isValidPhotoSettings(PortfolioTemplateEnum.FEATURE, settings));
      verifyNoInteractions(gridTemplateSettingsPolicy);
    }

    @Test
    @Order(2)
    @DisplayName("正常系：グリッド型の場合、グリッド型のPolicyの判定結果を返すこと")
    void isValidPhotoSettings_grid() {
      doReturn(false).when(gridTemplateSettingsPolicy).isValidPhotoSettings(settings);

      assertFalse(policy.isValidPhotoSettings(PortfolioTemplateEnum.GRID, settings));
      verifyNoInteractions(featureTemplateSettingsPolicy);
    }
  }

  @Nested
  @Order(3)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class isPublishable {
    @Test
    @Order(1)
    @DisplayName("正常系：フィーチャー型の場合、フィーチャー型のPolicyの判定結果を返すこと")
    void isPublishable_feature() {
      doReturn(true).when(featureTemplateSettingsPolicy).isPublishable(settings, new PhotoCount(3));

      assertTrue(policy.isPublishable(PortfolioTemplateEnum.FEATURE, settings, new PhotoCount(3)));
      verifyNoInteractions(gridTemplateSettingsPolicy);
    }

    @Test
    @Order(2)
    @DisplayName("正常系：グリッド型の場合、グリッド型のPolicyの判定結果を返すこと")
    void isPublishable_grid() {
      doReturn(false).when(gridTemplateSettingsPolicy).isPublishable(settings, new PhotoCount(3));

      assertFalse(policy.isPublishable(PortfolioTemplateEnum.GRID, settings, new PhotoCount(3)));
      verifyNoInteractions(featureTemplateSettingsPolicy);
    }
  }

  @Nested
  @Order(4)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class isPublishablePhotoSettings {
    @Test
    @Order(1)
    @DisplayName("正常系：フィーチャー型の場合、フィーチャー型のPolicyの判定結果を返すこと")
    void isPublishablePhotoSettings_feature() {
      doReturn(false).when(featureTemplateSettingsPolicy).isPublishablePhotoSettings(settings);

      assertFalse(policy.isPublishablePhotoSettings(PortfolioTemplateEnum.FEATURE, settings));
      verifyNoInteractions(gridTemplateSettingsPolicy);
    }

    @Test
    @Order(2)
    @DisplayName("正常系：グリッド型の場合、グリッド型のPolicyの判定結果を返すこと")
    void isPublishablePhotoSettings_grid() {
      doReturn(true).when(gridTemplateSettingsPolicy).isPublishablePhotoSettings(settings);

      assertTrue(policy.isPublishablePhotoSettings(PortfolioTemplateEnum.GRID, settings));
      verifyNoInteractions(featureTemplateSettingsPolicy);
    }
  }
}
