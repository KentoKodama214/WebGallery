package com.web.gallery.infrastructure.persistence.repository.photo;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.web.gallery.application.model.photo.PhotoNoList;
import com.web.gallery.application.model.photo.PortfolioDetailModel;
import com.web.gallery.application.model.photo.PortfolioDetailModelList;
import com.web.gallery.application.model.photo.PortfolioListGetModel;
import com.web.gallery.application.model.photo.PortfolioModel;
import com.web.gallery.application.model.photo.PortfolioModelList;
import com.web.gallery.application.model.photo.PortfolioPhotoModel;
import com.web.gallery.application.model.photo.PortfolioPhotoPageModel;
import com.web.gallery.application.model.photo.PortfolioPhotoSearchModel;
import com.web.gallery.application.model.photo.PortfolioTagModel;
import com.web.gallery.domain.enumeration.DirectionEnum;
import com.web.gallery.domain.enumeration.PortfolioStatusEnum;
import com.web.gallery.domain.enumeration.PortfolioTemplateEnum;
import com.web.gallery.domain.exception.PortfolioNotFoundException;
import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.photo.DisplayOrder;
import com.web.gallery.domain.model.photo.ImageFilePath;
import com.web.gallery.domain.model.photo.PhotoCount;
import com.web.gallery.domain.model.photo.PhotoJapaneseTitle;
import com.web.gallery.domain.model.photo.PhotoNo;
import com.web.gallery.domain.model.photo.PortfolioDescription;
import com.web.gallery.domain.model.photo.PortfolioNo;
import com.web.gallery.domain.model.photo.PortfolioTheme;
import com.web.gallery.domain.model.photo.TagEnglishName;
import com.web.gallery.domain.model.photo.TagJapaneseName;
import com.web.gallery.domain.model.photo.TagNo;
import com.web.gallery.domain.model.photo.TemplateSettings;
import com.web.gallery.infrastructure.persistence.dto.photo.PortfolioDto;
import com.web.gallery.infrastructure.persistence.dto.photo.PortfolioPhotoDto;
import com.web.gallery.infrastructure.persistence.entity.photo.PortfolioMst;
import com.web.gallery.infrastructure.persistence.entity.photo.PortfolioMstCondition;
import com.web.gallery.infrastructure.persistence.entity.photo.PortfolioPhotoCondition;
import com.web.gallery.infrastructure.persistence.entity.photo.PortfolioTagMst;
import com.web.gallery.infrastructure.persistence.entity.photo.PortfolioTagMstCondition;
import com.web.gallery.infrastructure.persistence.mapper.photo.PortfolioMstMapper;
import com.web.gallery.infrastructure.persistence.mapper.photo.PortfolioPhotoMapper;
import com.web.gallery.infrastructure.persistence.mapper.photo.PortfolioTagMstMapper;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
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
class PortfolioMstRepositoryImplTest {

  @InjectMocks private PortfolioMstRepositoryImpl portfolioMstRepositoryImpl;

  @Mock private PortfolioMstMapper portfolioMstMapper;

  @Mock private PortfolioPhotoMapper portfolioPhotoMapper;

  @Mock private PortfolioTagMstMapper portfolioTagMstMapper;

  private static final OffsetDateTime CREATED_AT =
      OffsetDateTime.of(2026, 1, 1, 9, 0, 0, 0, ZoneOffset.ofHours(9));
  private static final OffsetDateTime UPDATED_AT =
      OffsetDateTime.of(2026, 2, 1, 9, 0, 0, 0, ZoneOffset.ofHours(9));

  /**
   * ポートフォリオマスタのEntityを生成する
   *
   * @param portfolioNo ポートフォリオ番号
   * @param templateSettings テンプレート設定（DB上のJSON。nullも指定できる）
   * @return {@link PortfolioMst}
   */
  private static PortfolioMst portfolioMst(Long portfolioNo, Map<String, Object> templateSettings) {
    return PortfolioMst.builder()
        .id(10L)
        .accountNo(1L)
        .portfolioNo(portfolioNo)
        .createdBy(1L)
        .createdAt(CREATED_AT)
        .updatedBy(1L)
        .updatedAt(UPDATED_AT)
        .theme("日本の自然")
        .description("説明文")
        .templateKbn(PortfolioTemplateEnum.GRID)
        .templateSettings(templateSettings)
        .settingsVersion(1)
        .statusKbn(PortfolioStatusEnum.PUBLISHED)
        .build();
  }

  /**
   * ポートフォリオ写真のDtoを生成する
   *
   * @param photoNo 写真番号
   * @param displayOrder 表示順
   * @param photoSettings 写真設定（DB上のJSON。nullも指定できる）
   * @return {@link PortfolioPhotoDto}
   */
  private static PortfolioPhotoDto photoDto(
      Long photoNo, Integer displayOrder, Map<String, Object> photoSettings) {
    PortfolioPhotoDto dto = new PortfolioPhotoDto();
    dto.setAccountNo(1L);
    dto.setPortfolioNo(2L);
    dto.setPhotoNo(photoNo);
    dto.setDisplayOrder(displayOrder);
    dto.setPhotoSettings(photoSettings);
    dto.setPhotoJapaneseTitle("タイトル" + photoNo);
    dto.setImageFilePath("1/" + photoNo + "-abc.jpg");
    dto.setDirectionKbn(DirectionEnum.SQUARE);
    return dto;
  }

  /**
   * ポートフォリオタグのEntityを生成する
   *
   * @return {@link PortfolioTagMst}
   */
  private static PortfolioTagMst tag() {
    return PortfolioTagMst.builder()
        .id(1L)
        .accountNo(1L)
        .portfolioNo(2L)
        .tagNo(1L)
        .createdBy(1L)
        .createdAt(CREATED_AT)
        .tagJapaneseName("自然")
        .tagEnglishName("")
        .build();
  }

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class getNewPortfolioNo {
    @Test
    @Order(1)
    @DisplayName("正常系：登録済みの最大ポートフォリオ番号の次の番号を返すこと")
    void getNewPortfolioNo_success() {
      doReturn(3L).when(portfolioMstMapper).getMaxPortfolioNo(1L);

      assertEquals(
          new PortfolioNo(4L), portfolioMstRepositoryImpl.getNewPortfolioNo(new AccountNo(1L)));
    }

    @Test
    @Order(2)
    @DisplayName("正常系：ポートフォリオが未登録の場合、1を返すこと")
    void getNewPortfolioNo_first() {
      doReturn(null).when(portfolioMstMapper).getMaxPortfolioNo(1L);

      assertEquals(
          new PortfolioNo(1L), portfolioMstRepositoryImpl.getNewPortfolioNo(new AccountNo(1L)));
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class count {
    @Test
    @Order(1)
    @DisplayName("正常系：アカウント番号で絞り込んだ件数をそのまま返すこと")
    void count_success() {
      doReturn(3).when(portfolioMstMapper).count(any(PortfolioMstCondition.class));

      assertEquals(3, portfolioMstRepositoryImpl.count(new AccountNo(1L)));

      ArgumentCaptor<PortfolioMstCondition> conditionCaptor =
          ArgumentCaptor.forClass(PortfolioMstCondition.class);
      verify(portfolioMstMapper).count(conditionCaptor.capture());
      assertEquals(1L, conditionCaptor.getValue().getAccountNo());
      assertNull(conditionCaptor.getValue().getPortfolioNo());
    }
  }

  @Nested
  @Order(3)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class searchAvailablePhotos {
    /**
     * 写真の検索結果のDtoを生成する
     *
     * @param photoNo 写真番号
     * @return {@link PortfolioPhotoDto}
     */
    private PortfolioPhotoDto photoDto(Long photoNo) {
      PortfolioPhotoDto dto = new PortfolioPhotoDto();
      dto.setPhotoNo(photoNo);
      dto.setPhotoJapaneseTitle("写真" + photoNo);
      dto.setImageFilePath("1/" + photoNo + "-aaa.jpg");
      dto.setDirectionKbn(DirectionEnum.VERTICAL);
      return dto;
    }

    @Test
    @Order(1)
    @DisplayName("正常系：1ページの件数より1件多く取得し、余った場合は切り捨てて最後のページでないとすること")
    void searchAvailablePhotos_hasNext() {
      doReturn(List.of(photoDto(5L), photoDto(4L), photoDto(3L)))
          .when(portfolioPhotoMapper)
          .selectAvailablePhotos(1L, "%金閣%", 3, 2);

      PortfolioPhotoPageModel actual =
          portfolioMstRepositoryImpl.searchAvailablePhotos(
              PortfolioPhotoSearchModel.builder()
                  .accountNo(new AccountNo(1L))
                  .keyword("金閣")
                  .pageNo(2)
                  .pageSize(2)
                  .build());

      assertFalse(actual.getIsLast());
      assertEquals(2, actual.getPortfolioPhotoModelList().size());
      PortfolioPhotoModel first = actual.getPortfolioPhotoModelList().get(0);
      assertEquals(new PhotoNo(5L), first.getPhotoNo());
      assertEquals(new PhotoJapaneseTitle("写真5"), first.getPhotoJapaneseTitle());
      assertEquals(new ImageFilePath("1/5-aaa.jpg"), first.getImageFilePath());
      assertEquals(DirectionEnum.VERTICAL, first.getDirectionKbn());
      assertEquals(TemplateSettings.empty(), first.getPhotoSettings());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：取得件数が1ページの件数以下の場合、最後のページとし、未指定の検索語はnullで渡すこと")
    void searchAvailablePhotos_last() {
      doReturn(List.of(photoDto(1L)))
          .when(portfolioPhotoMapper)
          .selectAvailablePhotos(1L, null, 21, 0);

      PortfolioPhotoPageModel actual =
          portfolioMstRepositoryImpl.searchAvailablePhotos(
              PortfolioPhotoSearchModel.builder()
                  .accountNo(new AccountNo(1L))
                  .pageNo(1)
                  .pageSize(20)
                  .build());

      assertTrue(actual.getIsLast());
      assertEquals(1, actual.getPortfolioPhotoModelList().size());
    }
  }

  @Nested
  @Order(4)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class getPortfolioList {
    @Test
    @Order(1)
    @DisplayName("正常系：絞り込み条件が未指定の場合、アカウント番号だけで取得し、Dtoの全項目をModelに変換すること")
    void getPortfolioList_success() {
      PortfolioDto dto = new PortfolioDto();
      dto.setAccountNo(1L);
      dto.setPortfolioNo(2L);
      dto.setTheme("日本の自然");
      dto.setTemplateKbn(PortfolioTemplateEnum.FEATURE);
      dto.setStatusKbn(PortfolioStatusEnum.DRAFT);
      dto.setPhotoCount(3);
      dto.setCreatedAt(CREATED_AT);
      dto.setUpdatedAt(UPDATED_AT);
      doReturn(List.of(dto)).when(portfolioMstMapper).selectList(any(PortfolioMstCondition.class));

      PortfolioModelList actual =
          portfolioMstRepositoryImpl.getPortfolioList(
              PortfolioListGetModel.builder().accountNo(new AccountNo(1L)).build());

      ArgumentCaptor<PortfolioMstCondition> conditionCaptor =
          ArgumentCaptor.forClass(PortfolioMstCondition.class);
      verify(portfolioMstMapper).selectList(conditionCaptor.capture());
      assertEquals(1L, conditionCaptor.getValue().getAccountNo());
      assertNull(conditionCaptor.getValue().getPortfolioNo());
      assertNull(conditionCaptor.getValue().getThemePattern());
      assertNull(conditionCaptor.getValue().getTagPattern());

      assertEquals(1, actual.size());
      PortfolioModel model = actual.get(0);
      assertEquals(new AccountNo(1L), model.getAccountNo());
      assertEquals(new PortfolioNo(2L), model.getPortfolioNo());
      assertEquals(new PortfolioTheme("日本の自然"), model.getTheme());
      assertEquals(PortfolioTemplateEnum.FEATURE, model.getTemplateKbn());
      assertEquals(PortfolioStatusEnum.DRAFT, model.getStatusKbn());
      assertEquals(new PhotoCount(3), model.getPhotoCount());
      assertEquals(CREATED_AT, model.getCreatedAt());
      assertEquals(UPDATED_AT, model.getUpdatedAt());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：0件の場合、空のリストを返すこと")
    void getPortfolioList_empty() {
      doReturn(List.of()).when(portfolioMstMapper).selectList(any(PortfolioMstCondition.class));

      assertTrue(
          portfolioMstRepositoryImpl
              .getPortfolioList(
                  PortfolioListGetModel.builder().accountNo(new AccountNo(1L)).build())
              .isEmpty());
    }

    @Test
    @Order(3)
    @DisplayName("正常系：テーマ・タグ名を部分一致のLIKEパターンにし、ワイルドカード文字（%・_）とエスケープ文字（\\）をエスケープすること")
    void getPortfolioList_keywords() {
      doReturn(List.of()).when(portfolioMstMapper).selectList(any(PortfolioMstCondition.class));

      portfolioMstRepositoryImpl.getPortfolioList(
          PortfolioListGetModel.builder()
              .accountNo(new AccountNo(1L))
              .theme("100%_自然")
              .tag("a\\b")
              .build());

      ArgumentCaptor<PortfolioMstCondition> conditionCaptor =
          ArgumentCaptor.forClass(PortfolioMstCondition.class);
      verify(portfolioMstMapper).selectList(conditionCaptor.capture());
      assertEquals("%100\\%\\_自然%", conditionCaptor.getValue().getThemePattern());
      assertEquals("%a\\\\b%", conditionCaptor.getValue().getTagPattern());
    }
  }

  @Nested
  @Order(5)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class getPortfolioDetail {
    @Test
    @Order(1)
    @DisplayName("正常系：ポートフォリオ本体・写真・タグを取得し、全項目をModelに変換すること")
    void getPortfolioDetail_success() throws Exception {
      doReturn(List.of(portfolioMst(2L, Map.of("rows", List.of(Map.of("pattern", "TWO_EQUAL"))))))
          .when(portfolioMstMapper)
          .select(any(PortfolioMstCondition.class));
      doReturn(List.of(photoDto(5L, 1, Map.of("caption", "金閣寺")), photoDto(3L, 2, null)))
          .when(portfolioPhotoMapper)
          .selectList(any(PortfolioPhotoCondition.class));
      doReturn(List.of(tag()))
          .when(portfolioTagMstMapper)
          .select(any(PortfolioTagMstCondition.class));

      PortfolioDetailModel actual =
          portfolioMstRepositoryImpl.getPortfolioDetail(new AccountNo(1L), new PortfolioNo(2L));

      ArgumentCaptor<PortfolioMstCondition> conditionCaptor =
          ArgumentCaptor.forClass(PortfolioMstCondition.class);
      verify(portfolioMstMapper).select(conditionCaptor.capture());
      assertEquals(1L, conditionCaptor.getValue().getAccountNo());
      assertEquals(2L, conditionCaptor.getValue().getPortfolioNo());
      ArgumentCaptor<PortfolioPhotoCondition> photoConditionCaptor =
          ArgumentCaptor.forClass(PortfolioPhotoCondition.class);
      verify(portfolioPhotoMapper).selectList(photoConditionCaptor.capture());
      assertEquals(1L, photoConditionCaptor.getValue().getAccountNo());
      assertEquals(2L, photoConditionCaptor.getValue().getPortfolioNo());

      assertEquals(new AccountNo(1L), actual.getAccountNo());
      assertEquals(new PortfolioNo(2L), actual.getPortfolioNo());
      assertEquals(new PortfolioTheme("日本の自然"), actual.getTheme());
      assertEquals(new PortfolioDescription("説明文"), actual.getDescription());
      assertEquals(PortfolioTemplateEnum.GRID, actual.getTemplateKbn());
      assertEquals(
          new TemplateSettings(Map.of("rows", List.of(Map.of("pattern", "TWO_EQUAL")))),
          actual.getTemplateSettings());
      assertEquals(PortfolioStatusEnum.PUBLISHED, actual.getStatusKbn());
      assertEquals(CREATED_AT, actual.getCreatedAt());
      assertEquals(UPDATED_AT, actual.getUpdatedAt());

      PortfolioPhotoModel photo = actual.getPortfolioPhotoModelList().get(0);
      assertEquals(new AccountNo(1L), photo.getAccountNo());
      assertEquals(new PortfolioNo(2L), photo.getPortfolioNo());
      assertEquals(new PhotoNo(5L), photo.getPhotoNo());
      assertEquals(new DisplayOrder(1), photo.getDisplayOrder());
      assertEquals(new TemplateSettings(Map.of("caption", "金閣寺")), photo.getPhotoSettings());
      assertEquals(new PhotoJapaneseTitle("タイトル5"), photo.getPhotoJapaneseTitle());
      assertEquals(new ImageFilePath("1/5-abc.jpg"), photo.getImageFilePath());
      assertEquals(DirectionEnum.SQUARE, photo.getDirectionKbn());
      // DB上の写真設定がnullの場合は項目なしとして扱う
      assertEquals(
          TemplateSettings.empty(), actual.getPortfolioPhotoModelList().get(1).getPhotoSettings());

      PortfolioTagModel tagModel = actual.getPortfolioTagModelList().get(0);
      assertEquals(new AccountNo(1L), tagModel.getAccountNo());
      assertEquals(new PortfolioNo(2L), tagModel.getPortfolioNo());
      assertEquals(new TagNo(1L), tagModel.getTagNo());
      assertEquals(new TagJapaneseName("自然"), tagModel.getTagJapaneseName());
      assertEquals(new TagEnglishName(""), tagModel.getTagEnglishName());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：DB上のテンプレート設定がnullの場合、項目なしの設定に変換すること")
    void getPortfolioDetail_nullSettings() throws Exception {
      doReturn(List.of(portfolioMst(2L, null)))
          .when(portfolioMstMapper)
          .select(any(PortfolioMstCondition.class));
      doReturn(List.of()).when(portfolioPhotoMapper).selectList(any(PortfolioPhotoCondition.class));
      doReturn(List.of()).when(portfolioTagMstMapper).select(any(PortfolioTagMstCondition.class));

      PortfolioDetailModel actual =
          portfolioMstRepositoryImpl.getPortfolioDetail(new AccountNo(1L), new PortfolioNo(2L));

      assertEquals(TemplateSettings.empty(), actual.getTemplateSettings());
      assertTrue(actual.getPortfolioPhotoModelList().isEmpty());
      assertTrue(actual.getPortfolioTagModelList().isEmpty());
    }

    @Test
    @Order(3)
    @DisplayName("異常系：対象が存在しない（未登録・削除済み）場合、PortfolioNotFoundExceptionをthrowすること")
    void getPortfolioDetail_notFound() {
      doReturn(List.of()).when(portfolioMstMapper).select(any(PortfolioMstCondition.class));

      assertThrows(
          PortfolioNotFoundException.class,
          () ->
              portfolioMstRepositoryImpl.getPortfolioDetail(
                  new AccountNo(1L), new PortfolioNo(2L)));
      verify(portfolioPhotoMapper, never()).selectList(any(PortfolioPhotoCondition.class));
    }
  }

  @Nested
  @Order(6)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class getPortfolioDetailListByPhoto {
    @Test
    @Order(1)
    @DisplayName("正常系：写真を使用しているポートフォリオごとに、写真・タグを含む詳細情報を返すこと")
    void getPortfolioDetailListByPhoto_success() {
      doReturn(List.of(portfolioMst(2L, Map.of()), portfolioMst(4L, Map.of())))
          .when(portfolioMstMapper)
          .selectByPhoto(1L, 5L);
      doReturn(List.of(photoDto(5L, 1, Map.of())))
          .when(portfolioPhotoMapper)
          .selectList(any(PortfolioPhotoCondition.class));
      doReturn(List.of()).when(portfolioTagMstMapper).select(any(PortfolioTagMstCondition.class));

      PortfolioDetailModelList actual =
          portfolioMstRepositoryImpl.getPortfolioDetailListByPhoto(
              new AccountNo(1L), new PhotoNo(5L));

      assertEquals(2, actual.size());
      assertEquals(new PortfolioNo(2L), actual.get(0).getPortfolioNo());
      assertEquals(new PortfolioNo(4L), actual.get(1).getPortfolioNo());
      assertEquals(1, actual.get(0).getPortfolioPhotoModelList().size());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：写真を使用しているポートフォリオがない場合、空のリストを返すこと")
    void getPortfolioDetailListByPhoto_empty() {
      doReturn(List.of()).when(portfolioMstMapper).selectByPhoto(anyLong(), anyLong());

      assertTrue(
          portfolioMstRepositoryImpl
              .getPortfolioDetailListByPhoto(new AccountNo(1L), new PhotoNo(5L))
              .isEmpty());
    }
  }

  @Nested
  @Order(7)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class countAvailablePhotos {
    @Test
    @Order(1)
    @DisplayName("正常系：写真番号の一覧をLongのリストに変換して件数を問い合わせること")
    void countAvailablePhotos_success() {
      doReturn(2).when(portfolioPhotoMapper).countAvailablePhotos(1L, List.of(3L, 5L));

      Integer actual =
          portfolioMstRepositoryImpl.countAvailablePhotos(
              new AccountNo(1L), PhotoNoList.from(List.of(3L, 5L)));

      assertEquals(2, actual);
    }

    @Test
    @Order(2)
    @DisplayName("正常系：写真番号の一覧が空の場合、問い合わせずに0を返すこと（空のIN句によるSQL構文エラーを避ける）")
    void countAvailablePhotos_empty() {
      Integer actual =
          portfolioMstRepositoryImpl.countAvailablePhotos(new AccountNo(1L), PhotoNoList.empty());

      assertEquals(0, actual);
      verify(portfolioPhotoMapper, never()).countAvailablePhotos(anyLong(), anyList());
    }
  }
}
