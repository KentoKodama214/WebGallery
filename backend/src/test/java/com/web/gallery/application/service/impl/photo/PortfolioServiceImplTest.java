package com.web.gallery.application.service.impl.photo;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.web.gallery.application.aggregate.Portfolio;
import com.web.gallery.application.config.PhotoConfig;
import com.web.gallery.application.config.PortfolioConfig;
import com.web.gallery.application.model.account.AccountModel;
import com.web.gallery.application.model.photo.PhotoNoList;
import com.web.gallery.application.model.photo.PortfolioDetailModel;
import com.web.gallery.application.model.photo.PortfolioDisplayOrderMoveModel;
import com.web.gallery.application.model.photo.PortfolioListGetModel;
import com.web.gallery.application.model.photo.PortfolioModelList;
import com.web.gallery.application.model.photo.PortfolioNoList;
import com.web.gallery.application.model.photo.PortfolioPageModel;
import com.web.gallery.application.model.photo.PortfolioPhotoModel;
import com.web.gallery.application.model.photo.PortfolioPhotoModelList;
import com.web.gallery.application.model.photo.PortfolioPhotoPageModel;
import com.web.gallery.application.model.photo.PortfolioPhotoSearchModel;
import com.web.gallery.application.model.photo.PortfolioTagModelList;
import com.web.gallery.domain.enumeration.AuthorityEnum;
import com.web.gallery.domain.enumeration.ErrorEnum;
import com.web.gallery.domain.enumeration.MoveDirectionEnum;
import com.web.gallery.domain.enumeration.PortfolioStatusEnum;
import com.web.gallery.domain.enumeration.PortfolioTemplateEnum;
import com.web.gallery.domain.exception.BadRequestException;
import com.web.gallery.domain.exception.GalleryException;
import com.web.gallery.domain.exception.PortfolioNotFoundException;
import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.photo.DisplayOrder;
import com.web.gallery.domain.model.photo.ImageFilePath;
import com.web.gallery.domain.model.photo.PhotoCount;
import com.web.gallery.domain.model.photo.PhotoJapaneseTitle;
import com.web.gallery.domain.model.photo.PhotoNo;
import com.web.gallery.domain.model.photo.PortfolioCount;
import com.web.gallery.domain.model.photo.PortfolioDescription;
import com.web.gallery.domain.model.photo.PortfolioNo;
import com.web.gallery.domain.model.photo.PortfolioTheme;
import com.web.gallery.domain.model.photo.PortfolioUpperLimits;
import com.web.gallery.domain.model.photo.TemplateSettings;
import com.web.gallery.domain.service.PortfolioQuotaPolicy;
import com.web.gallery.domain.service.PortfolioTemplateSettingsPolicy;
import com.web.gallery.infrastructure.persistence.repository.account.AccountRepositoryImpl;
import com.web.gallery.infrastructure.persistence.repository.photo.FileRepositoryImpl;
import com.web.gallery.infrastructure.persistence.repository.photo.PortfolioAggregateRepositoryImpl;
import com.web.gallery.infrastructure.persistence.repository.photo.PortfolioMstRepositoryImpl;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
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
class PortfolioServiceImplTest {

  @InjectMocks private PortfolioServiceImpl portfolioServiceImpl;

  @Mock private PortfolioMstRepositoryImpl portfolioMstRepositoryImpl;

  @Mock private PortfolioAggregateRepositoryImpl portfolioAggregateRepositoryImpl;

  @Mock private FileRepositoryImpl fileRepositoryImpl;

  @Mock private PortfolioTemplateSettingsPolicy portfolioTemplateSettingsPolicy;

  @Mock private AccountRepositoryImpl accountRepositoryImpl;

  @Mock private PortfolioConfig portfolioConfig;

  @Mock private PortfolioQuotaPolicy portfolioQuotaPolicy;

  @Mock private PhotoConfig photoConfig;

  private static final AccountNo ACCOUNT_NO = new AccountNo(1L);

  private static final PortfolioNo PORTFOLIO_NO = new PortfolioNo(2L);

  /**
   * 写真を指定したポートフォリオ詳細情報を生成する
   *
   * @param portfolioNo ポートフォリオ番号（登録リクエストの場合はnull）
   * @param statusKbn 公開状態区分（登録・更新リクエストの場合はnull）
   * @param photoNos 表示順に並べた写真番号
   * @return {@link PortfolioDetailModel}
   */
  private static PortfolioDetailModel detail(
      PortfolioNo portfolioNo, PortfolioStatusEnum statusKbn, Long... photoNos) {
    List<PortfolioPhotoModel> photos =
        Arrays.stream(photoNos)
            .map(
                photoNo ->
                    PortfolioPhotoModel.builder()
                        .photoNo(new PhotoNo(photoNo))
                        .photoSettings(new TemplateSettings(Map.of("theme", "テーマ" + photoNo)))
                        .build())
            .toList();
    return PortfolioDetailModel.builder()
        .accountNo(ACCOUNT_NO)
        .portfolioNo(portfolioNo)
        .theme(new PortfolioTheme("日本の自然"))
        .description(new PortfolioDescription("説明文"))
        .templateKbn(PortfolioTemplateEnum.FEATURE)
        .templateSettings(TemplateSettings.empty())
        .statusKbn(statusKbn)
        .portfolioPhotoModelList(PortfolioPhotoModelList.of(photos))
        .portfolioTagModelList(PortfolioTagModelList.empty())
        .build();
  }

  /** 設定の構造・公開可否の判定を、既定ですべて妥当とする */
  @BeforeEach
  void setUpPolicyStub() {
    // 作成数の上限は、既定では上限に達していない状態とする（上限の判定は registPortfolio の各テストで検証する）
    lenient()
        .doReturn(
            AccountModel.builder().accountNo(ACCOUNT_NO).authorityKbn(AuthorityEnum.MINI).build())
        .when(accountRepositoryImpl)
        .getByAccountNo(ACCOUNT_NO);
    lenient().doReturn(3).when(portfolioConfig).getMiniUserUpperLimit();
    lenient().doReturn(10).when(portfolioConfig).getNormalUserUpperLimit();
    lenient().doReturn(0).when(portfolioMstRepositoryImpl).count(ACCOUNT_NO);
    lenient()
        .doReturn(false)
        .when(portfolioQuotaPolicy)
        .isReached(
            any(AuthorityEnum.class), any(PortfolioCount.class), any(PortfolioUpperLimits.class));
    lenient()
        .doReturn(true)
        .when(portfolioTemplateSettingsPolicy)
        .isValidTemplateSettings(any(PortfolioTemplateEnum.class), any(TemplateSettings.class));
    lenient()
        .doReturn(true)
        .when(portfolioTemplateSettingsPolicy)
        .isValidPhotoSettings(any(PortfolioTemplateEnum.class), any(TemplateSettings.class));
    lenient()
        .doReturn(true)
        .when(portfolioTemplateSettingsPolicy)
        .isPublishable(
            any(PortfolioTemplateEnum.class), any(TemplateSettings.class), any(PhotoCount.class));
    lenient()
        .doReturn(true)
        .when(portfolioTemplateSettingsPolicy)
        .isPublishablePhotoSettings(any(PortfolioTemplateEnum.class), any(TemplateSettings.class));
  }

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class getPortfolioList {
    @Test
    @Order(1)
    @DisplayName("正常系：絞り込み条件に設定値の1ページあたりの件数を加えてRepositoryに渡し、取得した1ページ分をそのまま返すこと")
    void getPortfolioList_success() {
      doReturn(20).when(portfolioConfig).getPortfolioCountPerPage();
      PortfolioListGetModel condition =
          PortfolioListGetModel.builder().accountNo(ACCOUNT_NO).keyword("自然").pageNo(2).build();
      PortfolioPageModel expected = PortfolioPageModel.of(PortfolioModelList.empty(), true);
      doReturn(expected)
          .when(portfolioMstRepositoryImpl)
          .getPortfolioList(condition.toBuilder().pageSize(20).build());

      assertSame(expected, portfolioServiceImpl.getPortfolioList(condition));
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class isReachedUpperLimit {
    @Test
    @Order(1)
    @DisplayName("正常系：アカウントの権限区分・作成済みの件数・設定値の上限で判定した結果を返すこと")
    void isReachedUpperLimit_success() {
      doReturn(3).when(portfolioMstRepositoryImpl).count(ACCOUNT_NO);
      doReturn(true)
          .when(portfolioQuotaPolicy)
          .isReached(AuthorityEnum.MINI, new PortfolioCount(3), new PortfolioUpperLimits(3, 10));

      assertTrue(portfolioServiceImpl.isReachedUpperLimit(ACCOUNT_NO));
    }

    @Test
    @Order(2)
    @DisplayName("正常系：上限に達していない場合、falseを返すこと")
    void isReachedUpperLimit_notReached() {
      assertFalse(portfolioServiceImpl.isReachedUpperLimit(ACCOUNT_NO));
    }
  }

  @Nested
  @Order(3)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class searchPhotos {
    @Test
    @Order(1)
    @DisplayName("正常系：写真一覧と同じ1ページの件数で検索し、画像ファイルパスを署名付きURLに差し替えて返すこと")
    void searchPhotos_success() {
      PortfolioPhotoSearchModel condition =
          PortfolioPhotoSearchModel.builder().accountNo(ACCOUNT_NO).keyword("金閣").pageNo(2).build();
      doReturn(20).when(photoConfig).getPhotoCountPerPage();
      doReturn(
              PortfolioPhotoPageModel.of(
                  PortfolioPhotoModelList.of(
                      List.of(
                          PortfolioPhotoModel.builder()
                              .photoNo(new PhotoNo(3L))
                              .photoSettings(TemplateSettings.empty())
                              .photoJapaneseTitle(new PhotoJapaneseTitle("金閣寺"))
                              .imageFilePath(new ImageFilePath("1/3-aaa.jpg"))
                              .build())),
                  false))
          .when(portfolioMstRepositoryImpl)
          .searchAvailablePhotos(condition.toBuilder().pageSize(20).build());
      doReturn(new ImageFilePath("https://signed/1/3-aaa.jpg"))
          .when(fileRepositoryImpl)
          .getPresignedUrl(new ImageFilePath("1/3-aaa.jpg"));

      PortfolioPhotoPageModel actual = portfolioServiceImpl.searchPhotos(condition);

      assertFalse(actual.getIsLast());
      assertEquals(1, actual.getPortfolioPhotoModelList().size());
      assertEquals(
          new ImageFilePath("https://signed/1/3-aaa.jpg"),
          actual.getPortfolioPhotoModelList().get(0).getImageFilePath());
      assertEquals(new PhotoNo(3L), actual.getPortfolioPhotoModelList().get(0).getPhotoNo());
    }
  }

  @Nested
  @Order(4)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class getPortfolioDetail {
    @Test
    @Order(1)
    @DisplayName("正常系：写真の画像ファイルパスを署名付きURLに差し替えて返すこと")
    void getPortfolioDetail_success() throws GalleryException {
      PortfolioDetailModel stored = detail(PORTFOLIO_NO, PortfolioStatusEnum.DRAFT);
      PortfolioDetailModel withPhoto =
          stored.toBuilder()
              .portfolioPhotoModelList(
                  PortfolioPhotoModelList.of(
                      List.of(
                          PortfolioPhotoModel.builder()
                              .photoNo(new PhotoNo(3L))
                              .displayOrder(new DisplayOrder(1))
                              .photoSettings(TemplateSettings.empty())
                              .photoJapaneseTitle(new PhotoJapaneseTitle("金閣寺"))
                              .imageFilePath(new ImageFilePath("1/3-abc.jpg"))
                              .build())))
              .build();
      doReturn(withPhoto)
          .when(portfolioMstRepositoryImpl)
          .getPortfolioDetail(ACCOUNT_NO, PORTFOLIO_NO);
      doReturn(new ImageFilePath("https://signed/1/3-abc.jpg"))
          .when(fileRepositoryImpl)
          .getPresignedUrl(new ImageFilePath("1/3-abc.jpg"));

      PortfolioDetailModel actual =
          portfolioServiceImpl.getPortfolioDetail(ACCOUNT_NO, PORTFOLIO_NO);

      PortfolioPhotoModel photo = actual.getPortfolioPhotoModelList().get(0);
      assertEquals(new ImageFilePath("https://signed/1/3-abc.jpg"), photo.getImageFilePath());
      assertEquals(new PhotoJapaneseTitle("金閣寺"), photo.getPhotoJapaneseTitle());
      assertEquals(new PortfolioTheme("日本の自然"), actual.getTheme());
    }

    @Test
    @Order(2)
    @DisplayName("異常系：ポートフォリオが存在しない場合、PortfolioNotFoundExceptionをthrowすること")
    void getPortfolioDetail_notFound() throws GalleryException {
      doThrow(ErrorEnum.PORTFOLIO_NOT_FOUND.toException())
          .when(portfolioMstRepositoryImpl)
          .getPortfolioDetail(ACCOUNT_NO, PORTFOLIO_NO);

      assertThrows(
          PortfolioNotFoundException.class,
          () -> portfolioServiceImpl.getPortfolioDetail(ACCOUNT_NO, PORTFOLIO_NO));
      verify(fileRepositoryImpl, never()).getPresignedUrl(any(ImageFilePath.class));
    }
  }

  @Nested
  @Order(5)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class registPortfolio {
    @Test
    @Order(1)
    @DisplayName("正常系：新規採番したポートフォリオ番号で、下書きとして登録すること")
    void registPortfolio_success() throws GalleryException {
      doReturn(2).when(portfolioMstRepositoryImpl).countAvailablePhotos(eq(ACCOUNT_NO), any());
      doReturn(new PortfolioNo(7L)).when(portfolioMstRepositoryImpl).getNewPortfolioNo(ACCOUNT_NO);

      PortfolioNo actual = portfolioServiceImpl.registPortfolio(detail(null, null, 3L, 5L));

      assertEquals(new PortfolioNo(7L), actual);
      ArgumentCaptor<PhotoNoList> photoNoListCaptor = ArgumentCaptor.forClass(PhotoNoList.class);
      verify(portfolioMstRepositoryImpl)
          .countAvailablePhotos(eq(ACCOUNT_NO), photoNoListCaptor.capture());
      assertEquals(
          List.of(new PhotoNo(3L), new PhotoNo(5L)), photoNoListCaptor.getValue().toList());

      ArgumentCaptor<Portfolio> portfolioCaptor = ArgumentCaptor.forClass(Portfolio.class);
      verify(portfolioAggregateRepositoryImpl).regist(portfolioCaptor.capture());
      Portfolio registered = portfolioCaptor.getValue();
      assertEquals(new PortfolioNo(7L), registered.getPortfolioNo());
      assertEquals(PortfolioStatusEnum.DRAFT, registered.getDetail().getStatusKbn());
      assertEquals(
          new DisplayOrder(2),
          registered.getDetail().getPortfolioPhotoModelList().get(1).getDisplayOrder());
      // 下書き保存では公開可否を判定しない（設定途中の不整合を許容する）
      verify(portfolioTemplateSettingsPolicy, never())
          .isPublishable(
              any(PortfolioTemplateEnum.class), any(TemplateSettings.class), any(PhotoCount.class));
    }

    @Test
    @Order(2)
    @DisplayName("正常系：作成数の上限の判定に、アカウントの権限区分・作成済みの件数・設定値の上限を渡すこと")
    void registPortfolio_quotaArguments() throws GalleryException {
      doReturn(2).when(portfolioMstRepositoryImpl).count(ACCOUNT_NO);
      doReturn(1).when(portfolioMstRepositoryImpl).countAvailablePhotos(eq(ACCOUNT_NO), any());
      doReturn(new PortfolioNo(3L)).when(portfolioMstRepositoryImpl).getNewPortfolioNo(ACCOUNT_NO);

      portfolioServiceImpl.registPortfolio(detail(null, null, 3L));

      verify(portfolioQuotaPolicy)
          .isReached(AuthorityEnum.MINI, new PortfolioCount(2), new PortfolioUpperLimits(3, 10));
      verify(portfolioAggregateRepositoryImpl).regist(any(Portfolio.class));
    }

    @Test
    @Order(3)
    @DisplayName("異常系：作成数が上限に達している場合、REACHED_PORTFOLIO_LIMITのBadRequestExceptionをthrowし、登録しないこと")
    void registPortfolio_reachedLimit() throws GalleryException {
      doReturn(3).when(portfolioMstRepositoryImpl).count(ACCOUNT_NO);
      doReturn(true)
          .when(portfolioQuotaPolicy)
          .isReached(AuthorityEnum.MINI, new PortfolioCount(3), new PortfolioUpperLimits(3, 10));

      BadRequestException exception =
          assertThrows(
              BadRequestException.class,
              () -> portfolioServiceImpl.registPortfolio(detail(null, null, 3L)));

      assertEquals(ErrorEnum.REACHED_PORTFOLIO_LIMIT.getErrorCode(), exception.getErrorCode());
      verify(portfolioMstRepositoryImpl, never()).getNewPortfolioNo(any(AccountNo.class));
      verify(portfolioAggregateRepositoryImpl, never()).regist(any(Portfolio.class));
    }

    @Test
    @Order(4)
    @DisplayName("正常系：写真が0枚の下書きも登録できること")
    void registPortfolio_noPhoto() throws GalleryException {
      doReturn(0).when(portfolioMstRepositoryImpl).countAvailablePhotos(eq(ACCOUNT_NO), any());
      doReturn(new PortfolioNo(1L)).when(portfolioMstRepositoryImpl).getNewPortfolioNo(ACCOUNT_NO);

      assertEquals(new PortfolioNo(1L), portfolioServiceImpl.registPortfolio(detail(null, null)));
      verify(portfolioAggregateRepositoryImpl).regist(any(Portfolio.class));
    }

    @Test
    @Order(5)
    @DisplayName("異常系：同じ写真が重複して指定された場合、INVALID_INPUTのBadRequestExceptionをthrowし、登録しないこと")
    void registPortfolio_duplicatePhoto() throws GalleryException {
      BadRequestException exception =
          assertThrows(
              BadRequestException.class,
              () -> portfolioServiceImpl.registPortfolio(detail(null, null, 3L, 3L)));

      assertEquals(ErrorEnum.INVALID_INPUT.getErrorCode(), exception.getErrorCode());
      verify(portfolioAggregateRepositoryImpl, never()).regist(any(Portfolio.class));
    }

    @Test
    @Order(6)
    @DisplayName(
        "異常系：ポートフォリオ単位の設定の構造が不正な場合、INVALID_PORTFOLIO_SETTINGSのBadRequestExceptionをthrowすること")
    void registPortfolio_invalidTemplateSettings() throws GalleryException {
      doReturn(false)
          .when(portfolioTemplateSettingsPolicy)
          .isValidTemplateSettings(any(PortfolioTemplateEnum.class), any(TemplateSettings.class));

      BadRequestException exception =
          assertThrows(
              BadRequestException.class,
              () -> portfolioServiceImpl.registPortfolio(detail(null, null, 3L)));

      assertEquals(ErrorEnum.INVALID_PORTFOLIO_SETTINGS.getErrorCode(), exception.getErrorCode());
      verify(portfolioAggregateRepositoryImpl, never()).regist(any(Portfolio.class));
    }

    @Test
    @Order(7)
    @DisplayName("異常系：写真単位の設定の構造が不正な場合、INVALID_PORTFOLIO_SETTINGSのBadRequestExceptionをthrowすること")
    void registPortfolio_invalidPhotoSettings() throws GalleryException {
      doReturn(false)
          .when(portfolioTemplateSettingsPolicy)
          .isValidPhotoSettings(any(PortfolioTemplateEnum.class), any(TemplateSettings.class));

      BadRequestException exception =
          assertThrows(
              BadRequestException.class,
              () -> portfolioServiceImpl.registPortfolio(detail(null, null, 3L)));

      assertEquals(ErrorEnum.INVALID_PORTFOLIO_SETTINGS.getErrorCode(), exception.getErrorCode());
      verify(portfolioAggregateRepositoryImpl, never()).regist(any(Portfolio.class));
    }

    @Test
    @Order(8)
    @DisplayName(
        "異常系：本人が所有する未削除の写真以外が含まれる場合、PORTFOLIO_PHOTO_NOT_AVAILABLEのBadRequestExceptionをthrowすること")
    void registPortfolio_unavailablePhoto() throws GalleryException {
      doReturn(1).when(portfolioMstRepositoryImpl).countAvailablePhotos(eq(ACCOUNT_NO), any());

      BadRequestException exception =
          assertThrows(
              BadRequestException.class,
              () -> portfolioServiceImpl.registPortfolio(detail(null, null, 3L, 99L)));

      assertEquals(
          ErrorEnum.PORTFOLIO_PHOTO_NOT_AVAILABLE.getErrorCode(), exception.getErrorCode());
      verify(portfolioMstRepositoryImpl, never()).getNewPortfolioNo(any(AccountNo.class));
      verify(portfolioAggregateRepositoryImpl, never()).regist(any(Portfolio.class));
    }
  }

  @Nested
  @Order(6)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class updatePortfolio {
    @Test
    @Order(1)
    @DisplayName("正常系：下書きの場合、公開可否を判定せずに、公開状態を引き継いで更新すること")
    void updatePortfolio_draft() throws GalleryException {
      doReturn(detail(PORTFOLIO_NO, PortfolioStatusEnum.DRAFT, 3L))
          .when(portfolioMstRepositoryImpl)
          .getPortfolioDetail(ACCOUNT_NO, PORTFOLIO_NO);
      doReturn(2).when(portfolioMstRepositoryImpl).countAvailablePhotos(eq(ACCOUNT_NO), any());

      portfolioServiceImpl.updatePortfolio(detail(PORTFOLIO_NO, null, 5L, 3L));

      ArgumentCaptor<Portfolio> portfolioCaptor = ArgumentCaptor.forClass(Portfolio.class);
      verify(portfolioAggregateRepositoryImpl).update(portfolioCaptor.capture());
      Portfolio updated = portfolioCaptor.getValue();
      assertEquals(PORTFOLIO_NO, updated.getPortfolioNo());
      assertEquals(PortfolioStatusEnum.DRAFT, updated.getDetail().getStatusKbn());
      assertEquals(
          List.of(new PhotoNo(5L), new PhotoNo(3L)),
          updated.getDetail().getPortfolioPhotoModelList().stream()
              .map(PortfolioPhotoModel::getPhotoNo)
              .toList());
      verify(portfolioTemplateSettingsPolicy, never())
          .isPublishable(
              any(PortfolioTemplateEnum.class), any(TemplateSettings.class), any(PhotoCount.class));
    }

    @Test
    @Order(2)
    @DisplayName("正常系：公開中で、更新後の内容が公開できる場合、公開のまま更新すること")
    void updatePortfolio_published() throws GalleryException {
      doReturn(detail(PORTFOLIO_NO, PortfolioStatusEnum.PUBLISHED, 3L))
          .when(portfolioMstRepositoryImpl)
          .getPortfolioDetail(ACCOUNT_NO, PORTFOLIO_NO);
      doReturn(1).when(portfolioMstRepositoryImpl).countAvailablePhotos(eq(ACCOUNT_NO), any());

      portfolioServiceImpl.updatePortfolio(detail(PORTFOLIO_NO, null, 3L));

      ArgumentCaptor<Portfolio> portfolioCaptor = ArgumentCaptor.forClass(Portfolio.class);
      verify(portfolioAggregateRepositoryImpl).update(portfolioCaptor.capture());
      assertEquals(
          PortfolioStatusEnum.PUBLISHED, portfolioCaptor.getValue().getDetail().getStatusKbn());
      verify(portfolioTemplateSettingsPolicy)
          .isPublishable(
              PortfolioTemplateEnum.FEATURE, TemplateSettings.empty(), new PhotoCount(1));
    }

    @Test
    @Order(3)
    @DisplayName(
        "異常系：公開中で、更新後の内容が公開できない場合、PORTFOLIO_NOT_PUBLISHABLEのBadRequestExceptionをthrowし、更新しないこと")
    void updatePortfolio_publishedNotPublishable() throws GalleryException {
      doReturn(detail(PORTFOLIO_NO, PortfolioStatusEnum.PUBLISHED, 3L))
          .when(portfolioMstRepositoryImpl)
          .getPortfolioDetail(ACCOUNT_NO, PORTFOLIO_NO);
      doReturn(1).when(portfolioMstRepositoryImpl).countAvailablePhotos(eq(ACCOUNT_NO), any());
      doReturn(false)
          .when(portfolioTemplateSettingsPolicy)
          .isPublishablePhotoSettings(
              any(PortfolioTemplateEnum.class), any(TemplateSettings.class));

      BadRequestException exception =
          assertThrows(
              BadRequestException.class,
              () -> portfolioServiceImpl.updatePortfolio(detail(PORTFOLIO_NO, null, 3L)));

      assertEquals(ErrorEnum.PORTFOLIO_NOT_PUBLISHABLE.getErrorCode(), exception.getErrorCode());
      verify(portfolioAggregateRepositoryImpl, never()).update(any(Portfolio.class));
    }

    @Test
    @Order(4)
    @DisplayName("異常系：ポートフォリオが存在しない場合、PortfolioNotFoundExceptionをthrowし、更新しないこと")
    void updatePortfolio_notFound() throws GalleryException {
      doThrow(ErrorEnum.PORTFOLIO_NOT_FOUND.toException())
          .when(portfolioMstRepositoryImpl)
          .getPortfolioDetail(ACCOUNT_NO, PORTFOLIO_NO);

      assertThrows(
          PortfolioNotFoundException.class,
          () -> portfolioServiceImpl.updatePortfolio(detail(PORTFOLIO_NO, null, 3L)));
      verify(portfolioAggregateRepositoryImpl, never()).update(any(Portfolio.class));
    }

    @Test
    @Order(5)
    @DisplayName(
        "異常系：本人が所有する未削除の写真以外が含まれる場合、PORTFOLIO_PHOTO_NOT_AVAILABLEのBadRequestExceptionをthrowすること")
    void updatePortfolio_unavailablePhoto() throws GalleryException {
      doReturn(detail(PORTFOLIO_NO, PortfolioStatusEnum.DRAFT, 3L))
          .when(portfolioMstRepositoryImpl)
          .getPortfolioDetail(ACCOUNT_NO, PORTFOLIO_NO);
      doReturn(0).when(portfolioMstRepositoryImpl).countAvailablePhotos(eq(ACCOUNT_NO), any());

      BadRequestException exception =
          assertThrows(
              BadRequestException.class,
              () -> portfolioServiceImpl.updatePortfolio(detail(PORTFOLIO_NO, null, 99L)));

      assertEquals(
          ErrorEnum.PORTFOLIO_PHOTO_NOT_AVAILABLE.getErrorCode(), exception.getErrorCode());
      verify(portfolioAggregateRepositoryImpl, never()).update(any(Portfolio.class));
    }
  }

  @Nested
  @Order(7)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class publishPortfolio {
    @Test
    @Order(1)
    @DisplayName("正常系：公開できる場合、公開状態を公開に更新すること")
    void publishPortfolio_success() throws GalleryException {
      doReturn(detail(PORTFOLIO_NO, PortfolioStatusEnum.DRAFT, 3L, 5L))
          .when(portfolioMstRepositoryImpl)
          .getPortfolioDetail(ACCOUNT_NO, PORTFOLIO_NO);

      portfolioServiceImpl.publishPortfolio(ACCOUNT_NO, PORTFOLIO_NO);

      verify(portfolioTemplateSettingsPolicy)
          .isPublishable(
              PortfolioTemplateEnum.FEATURE, TemplateSettings.empty(), new PhotoCount(2));
      ArgumentCaptor<Portfolio> portfolioCaptor = ArgumentCaptor.forClass(Portfolio.class);
      verify(portfolioAggregateRepositoryImpl).updateStatus(portfolioCaptor.capture());
      assertEquals(
          PortfolioStatusEnum.PUBLISHED, portfolioCaptor.getValue().getDetail().getStatusKbn());
    }

    @Test
    @Order(2)
    @DisplayName(
        "異常系：ポートフォリオ単位の設定が公開できない場合、PORTFOLIO_NOT_PUBLISHABLEのBadRequestExceptionをthrowし、更新しないこと")
    void publishPortfolio_notPublishable() throws GalleryException {
      doReturn(detail(PORTFOLIO_NO, PortfolioStatusEnum.DRAFT))
          .when(portfolioMstRepositoryImpl)
          .getPortfolioDetail(ACCOUNT_NO, PORTFOLIO_NO);
      doReturn(false)
          .when(portfolioTemplateSettingsPolicy)
          .isPublishable(
              any(PortfolioTemplateEnum.class), any(TemplateSettings.class), any(PhotoCount.class));

      BadRequestException exception =
          assertThrows(
              BadRequestException.class,
              () -> portfolioServiceImpl.publishPortfolio(ACCOUNT_NO, PORTFOLIO_NO));

      assertEquals(ErrorEnum.PORTFOLIO_NOT_PUBLISHABLE.getErrorCode(), exception.getErrorCode());
      verify(portfolioAggregateRepositoryImpl, never()).updateStatus(any(Portfolio.class));
    }

    @Test
    @Order(3)
    @DisplayName(
        "異常系：写真単位の設定が公開できない場合、PORTFOLIO_NOT_PUBLISHABLEのBadRequestExceptionをthrowし、更新しないこと")
    void publishPortfolio_photoNotPublishable() throws GalleryException {
      doReturn(detail(PORTFOLIO_NO, PortfolioStatusEnum.DRAFT, 3L))
          .when(portfolioMstRepositoryImpl)
          .getPortfolioDetail(ACCOUNT_NO, PORTFOLIO_NO);
      doReturn(false)
          .when(portfolioTemplateSettingsPolicy)
          .isPublishablePhotoSettings(
              any(PortfolioTemplateEnum.class), any(TemplateSettings.class));

      assertThrows(
          BadRequestException.class,
          () -> portfolioServiceImpl.publishPortfolio(ACCOUNT_NO, PORTFOLIO_NO));
      verify(portfolioAggregateRepositoryImpl, never()).updateStatus(any(Portfolio.class));
    }

    @Test
    @Order(4)
    @DisplayName("異常系：ポートフォリオが存在しない場合、PortfolioNotFoundExceptionをthrowすること")
    void publishPortfolio_notFound() throws GalleryException {
      doThrow(ErrorEnum.PORTFOLIO_NOT_FOUND.toException())
          .when(portfolioMstRepositoryImpl)
          .getPortfolioDetail(ACCOUNT_NO, PORTFOLIO_NO);

      assertThrows(
          PortfolioNotFoundException.class,
          () -> portfolioServiceImpl.publishPortfolio(ACCOUNT_NO, PORTFOLIO_NO));
      verify(portfolioAggregateRepositoryImpl, never()).updateStatus(any(Portfolio.class));
    }
  }

  @Nested
  @Order(8)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class unpublishPortfolio {
    @Test
    @Order(1)
    @DisplayName("正常系：公開可否を判定せずに、公開状態を下書きに更新すること")
    void unpublishPortfolio_success() throws GalleryException {
      doReturn(detail(PORTFOLIO_NO, PortfolioStatusEnum.PUBLISHED, 3L))
          .when(portfolioMstRepositoryImpl)
          .getPortfolioDetail(ACCOUNT_NO, PORTFOLIO_NO);

      portfolioServiceImpl.unpublishPortfolio(ACCOUNT_NO, PORTFOLIO_NO);

      ArgumentCaptor<Portfolio> portfolioCaptor = ArgumentCaptor.forClass(Portfolio.class);
      verify(portfolioAggregateRepositoryImpl).updateStatus(portfolioCaptor.capture());
      assertEquals(
          PortfolioStatusEnum.DRAFT, portfolioCaptor.getValue().getDetail().getStatusKbn());
      verify(portfolioTemplateSettingsPolicy, never())
          .isPublishable(
              any(PortfolioTemplateEnum.class), any(TemplateSettings.class), any(PhotoCount.class));
    }

    @Test
    @Order(2)
    @DisplayName("異常系：ポートフォリオが存在しない場合、PortfolioNotFoundExceptionをthrowすること")
    void unpublishPortfolio_notFound() throws GalleryException {
      doThrow(ErrorEnum.PORTFOLIO_NOT_FOUND.toException())
          .when(portfolioMstRepositoryImpl)
          .getPortfolioDetail(ACCOUNT_NO, PORTFOLIO_NO);

      assertThrows(
          PortfolioNotFoundException.class,
          () -> portfolioServiceImpl.unpublishPortfolio(ACCOUNT_NO, PORTFOLIO_NO));
      verify(portfolioAggregateRepositoryImpl, never()).updateStatus(any(Portfolio.class));
    }
  }

  @Nested
  @Order(9)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class deletePortfolio {
    @Test
    @Order(1)
    @DisplayName("正常系：アカウント番号・ポートフォリオ番号を指定して削除すること")
    void deletePortfolio_success() throws GalleryException {
      portfolioServiceImpl.deletePortfolio(ACCOUNT_NO, PORTFOLIO_NO);

      ArgumentCaptor<Portfolio> portfolioCaptor = ArgumentCaptor.forClass(Portfolio.class);
      verify(portfolioAggregateRepositoryImpl).delete(portfolioCaptor.capture());
      assertEquals(ACCOUNT_NO, portfolioCaptor.getValue().getAccountNo());
      assertEquals(PORTFOLIO_NO, portfolioCaptor.getValue().getPortfolioNo());
    }

    @Test
    @Order(2)
    @DisplayName("異常系：ポートフォリオが存在しない場合、PortfolioNotFoundExceptionをthrowすること")
    void deletePortfolio_notFound() throws GalleryException {
      doThrow(ErrorEnum.PORTFOLIO_NOT_FOUND.toException())
          .when(portfolioAggregateRepositoryImpl)
          .delete(any(Portfolio.class));

      assertThrows(
          PortfolioNotFoundException.class,
          () -> portfolioServiceImpl.deletePortfolio(ACCOUNT_NO, PORTFOLIO_NO));
    }
  }

  @Nested
  @Order(10)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class moveDisplayOrder {
    private PortfolioDisplayOrderMoveModel moveModel(
        Long portfolioNo, MoveDirectionEnum direction) {
      return PortfolioDisplayOrderMoveModel.builder()
          .accountNo(ACCOUNT_NO)
          .portfolioNo(new PortfolioNo(portfolioNo))
          .direction(direction)
          .build();
    }

    @Test
    @Order(1)
    @DisplayName("正常系：全件の並び（行ロック付きで取得）で隣と入れ替えた並びで、表示順を更新すること")
    void moveDisplayOrder_success() throws GalleryException {
      doReturn(PortfolioNoList.from(List.of(3L, 1L, 2L)))
          .when(portfolioMstRepositoryImpl)
          .getPortfolioNoListForUpdate(ACCOUNT_NO);

      portfolioServiceImpl.moveDisplayOrder(moveModel(1L, MoveDirectionEnum.UP));

      verify(portfolioAggregateRepositoryImpl)
          .updateDisplayOrder(ACCOUNT_NO, PortfolioNoList.from(List.of(1L, 3L, 2L)));
    }

    @Test
    @Order(2)
    @DisplayName("正常系：先頭を上へ・末尾を下へ移動しようとした場合、更新しないこと")
    void moveDisplayOrder_boundary() throws GalleryException {
      doReturn(PortfolioNoList.from(List.of(3L, 1L, 2L)))
          .when(portfolioMstRepositoryImpl)
          .getPortfolioNoListForUpdate(ACCOUNT_NO);

      portfolioServiceImpl.moveDisplayOrder(moveModel(3L, MoveDirectionEnum.UP));
      portfolioServiceImpl.moveDisplayOrder(moveModel(2L, MoveDirectionEnum.DOWN));

      verify(portfolioAggregateRepositoryImpl, never())
          .updateDisplayOrder(any(AccountNo.class), any(PortfolioNoList.class));
    }

    @Test
    @Order(3)
    @DisplayName("異常系：ポートフォリオが存在しない場合、PortfolioNotFoundExceptionをthrowし更新しないこと")
    void moveDisplayOrder_notFound() throws GalleryException {
      doReturn(PortfolioNoList.from(List.of(3L, 1L, 2L)))
          .when(portfolioMstRepositoryImpl)
          .getPortfolioNoListForUpdate(ACCOUNT_NO);

      assertThrows(
          PortfolioNotFoundException.class,
          () -> portfolioServiceImpl.moveDisplayOrder(moveModel(9L, MoveDirectionEnum.DOWN)));
      verify(portfolioAggregateRepositoryImpl, never())
          .updateDisplayOrder(any(AccountNo.class), any(PortfolioNoList.class));
    }
  }
}
