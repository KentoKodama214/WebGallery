package com.web.gallery.presentation.controller.photo;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.web.gallery.application.model.photo.PortfolioDetailModel;
import com.web.gallery.application.model.photo.PortfolioListGetModel;
import com.web.gallery.application.model.photo.PortfolioModel;
import com.web.gallery.application.model.photo.PortfolioModelList;
import com.web.gallery.application.model.photo.PortfolioPhotoModel;
import com.web.gallery.application.model.photo.PortfolioPhotoModelList;
import com.web.gallery.application.model.photo.PortfolioPhotoPageModel;
import com.web.gallery.application.model.photo.PortfolioPhotoSearchModel;
import com.web.gallery.application.model.photo.PortfolioTagModelList;
import com.web.gallery.application.service.photo.PortfolioService;
import com.web.gallery.domain.enumeration.DirectionEnum;
import com.web.gallery.domain.enumeration.ErrorEnum;
import com.web.gallery.domain.enumeration.PortfolioStatusEnum;
import com.web.gallery.domain.enumeration.PortfolioTemplateEnum;
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
import com.web.gallery.domain.model.photo.TemplateSettings;
import com.web.gallery.infrastructure.security.SessionHelper;
import com.web.gallery.presentation.controller.common.CommonControllerAdvice;
import com.web.gallery.presentation.converter.photo.PortfolioConverter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
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
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.json.JsonMapper;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class PortfolioControllerTest {

  @InjectMocks private PortfolioController portfolioController;

  @Mock private PortfolioService portfolioService;

  @Mock private SessionHelper sessionHelper;

  @Spy private PortfolioConverter portfolioConverter = new PortfolioConverter();

  private MockMvc mockMvc;

  private static final String PORTFOLIOS = "/api/v1/accounts/aaaaaaaa/portfolios";

  @BeforeEach
  void setUp() {
    lenient().doReturn(1L).when(sessionHelper).getAccountNo();
    lenient().doReturn("aaaaaaaa").when(sessionHelper).getAccountId();
    JsonMapper jsonMapper = JsonMapper.builder().build();

    JacksonJsonHttpMessageConverter converter = new JacksonJsonHttpMessageConverter(jsonMapper);

    mockMvc =
        MockMvcBuilders.standaloneSetup(portfolioController)
            .setMessageConverters(converter)
            .setControllerAdvice(new CommonControllerAdvice())
            .build();
  }

  private String readJsonFile(String fileName) throws Exception {
    return new String(
        new ClassPathResource("json/controller/PortfolioControllerTest/" + fileName)
            .getInputStream()
            .readAllBytes(),
        StandardCharsets.UTF_8);
  }

  /**
   * 写真・タグの件数を指定した登録・更新リクエストのJSONを生成する
   *
   * @param photoCount 写真の枚数
   * @param tagCount タグの件数
   * @return リクエストのJSON
   */
  private String saveJson(int photoCount, int tagCount) {
    String photoList =
        String.join(
            ",",
            IntStream.rangeClosed(1, photoCount)
                .mapToObj(no -> "{\"photoNo\":" + no + "}")
                .toList());
    String tagList =
        String.join(
            ",",
            IntStream.rangeClosed(1, tagCount)
                .mapToObj(no -> "{\"tagJapaneseName\":\"タグ" + no + "\"}")
                .toList());
    return "{\"theme\":\"日本の自然\",\"templateKbn\":\"feature\",\"photoList\":["
        + photoList
        + "],\"tagList\":["
        + tagList
        + "]}";
  }

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class getPortfolioList {
    @Test
    @Order(1)
    @DisplayName("正常系：本人のポートフォリオ一覧（下書きを含む）を取得できること")
    void getPortfolioList_success() throws Exception {
      doReturn(
              PortfolioModelList.of(
                  List.of(
                      PortfolioModel.builder()
                          .accountNo(new AccountNo(1L))
                          .portfolioNo(new PortfolioNo(2L))
                          .theme(new PortfolioTheme("日本の自然"))
                          .templateKbn(PortfolioTemplateEnum.GRID)
                          .statusKbn(PortfolioStatusEnum.DRAFT)
                          .photoCount(new PhotoCount(5))
                          .build())))
          .when(portfolioService)
          .getPortfolioList(any());

      mockMvc
          .perform(get(PORTFOLIOS))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.portfolioList[0].portfolioNo").value(2))
          .andExpect(jsonPath("$.portfolioList[0].theme").value("日本の自然"))
          .andExpect(jsonPath("$.portfolioList[0].templateKbn").value("grid"))
          .andExpect(jsonPath("$.portfolioList[0].statusKbn").value("draft"))
          .andExpect(jsonPath("$.portfolioList[0].photoCount").value(5));

      // 絞り込み条件が未指定の場合は、ログイン中のアカウント番号だけを渡す
      verify(portfolioService)
          .getPortfolioList(PortfolioListGetModel.builder().accountNo(new AccountNo(1L)).build());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：テーマ・タグの絞り込み条件を、前後の空白を除いてServiceに渡すこと")
    void getPortfolioList_keywords() throws Exception {
      doReturn(PortfolioModelList.empty()).when(portfolioService).getPortfolioList(any());

      mockMvc
          .perform(get(PORTFOLIOS).param("theme", " 自然 ").param("tag", "風景"))
          .andExpect(status().isOk());

      verify(portfolioService)
          .getPortfolioList(
              PortfolioListGetModel.builder()
                  .accountNo(new AccountNo(1L))
                  .theme("自然")
                  .tag("風景")
                  .build());
    }

    @Test
    @Order(3)
    @DisplayName("異常系：テーマが最大文字数（100文字）を超える場合、400を返しServiceを呼び出さないこと")
    void getPortfolioList_tooLongTheme() throws Exception {
      mockMvc
          .perform(get(PORTFOLIOS).param("theme", "あ".repeat(101)))
          .andExpect(status().isBadRequest());

      verify(portfolioService, times(0)).getPortfolioList(any());
    }

    @Test
    @Order(4)
    @DisplayName("異常系：認証ユーザーと異なるアカウントIDを指定した場合、403を返しServiceを呼び出さないこと")
    void getPortfolioList_forbidden() throws Exception {
      doReturn("bbbbbbbb").when(sessionHelper).getAccountId();

      mockMvc
          .perform(get(PORTFOLIOS))
          .andExpect(status().isForbidden())
          .andExpect(
              jsonPath("$.errorCode")
                  .value(ErrorEnum.NOT_AUTHORIZED_TO_EDIT_PORTFOLIO.getErrorCode()));

      verify(portfolioService, times(0)).getPortfolioList(any());
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class getPortfolioDetail {
    @Test
    @Order(1)
    @DisplayName("正常系：本人のポートフォリオ詳細（写真・タグを含む）を取得できること")
    void getPortfolioDetail_success() throws Exception {
      doReturn(
              PortfolioDetailModel.builder()
                  .accountNo(new AccountNo(1L))
                  .portfolioNo(new PortfolioNo(2L))
                  .theme(new PortfolioTheme("日本の自然"))
                  .description(new PortfolioDescription("説明文"))
                  .templateKbn(PortfolioTemplateEnum.FEATURE)
                  .templateSettings(TemplateSettings.empty())
                  .statusKbn(PortfolioStatusEnum.PUBLISHED)
                  .portfolioPhotoModelList(
                      PortfolioPhotoModelList.of(
                          List.of(
                              PortfolioPhotoModel.builder()
                                  .photoNo(new PhotoNo(3L))
                                  .displayOrder(new DisplayOrder(1))
                                  .photoSettings(new TemplateSettings(Map.of("theme", "日本の自然")))
                                  .photoJapaneseTitle(new PhotoJapaneseTitle("金閣寺"))
                                  .imageFilePath(new ImageFilePath("https://signed/3.jpg"))
                                  .build())))
                  .portfolioTagModelList(PortfolioTagModelList.empty())
                  .build())
          .when(portfolioService)
          .getPortfolioDetail(new AccountNo(1L), new PortfolioNo(2L));

      mockMvc
          .perform(get(PORTFOLIOS + "/2"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.portfolioNo").value(2))
          .andExpect(jsonPath("$.templateKbn").value("feature"))
          .andExpect(jsonPath("$.statusKbn").value("published"))
          .andExpect(jsonPath("$.photoList[0].photoNo").value(3))
          .andExpect(jsonPath("$.photoList[0].photoSettings.theme").value("日本の自然"))
          .andExpect(jsonPath("$.photoList[0].imageFilePath").value("https://signed/3.jpg"));
    }

    @Test
    @Order(2)
    @DisplayName("異常系：ポートフォリオ番号が0以下の場合、400を返しServiceを呼び出さないこと")
    void getPortfolioDetail_invalidPortfolioNo() throws Exception {
      mockMvc
          .perform(get(PORTFOLIOS + "/0"))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.message").value(ErrorEnum.INVALID_INPUT.getErrorMessage()));

      verify(portfolioService, times(0)).getPortfolioDetail(any(), any());
    }

    @Test
    @Order(3)
    @DisplayName("異常系：認証ユーザーと異なるアカウントIDを指定した場合、403を返しServiceを呼び出さないこと")
    void getPortfolioDetail_forbidden() throws Exception {
      doReturn("bbbbbbbb").when(sessionHelper).getAccountId();

      mockMvc.perform(get(PORTFOLIOS + "/2")).andExpect(status().isForbidden());

      verify(portfolioService, times(0)).getPortfolioDetail(any(), any());
    }

    @Test
    @Order(4)
    @DisplayName("異常系：ポートフォリオが存在しない場合、404を返すこと")
    void getPortfolioDetail_notFound() throws Exception {
      doThrow(ErrorEnum.PORTFOLIO_NOT_FOUND.toException())
          .when(portfolioService)
          .getPortfolioDetail(any(), any());

      mockMvc
          .perform(get(PORTFOLIOS + "/2"))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.errorCode").value(ErrorEnum.PORTFOLIO_NOT_FOUND.getErrorCode()))
          .andExpect(
              jsonPath("$.errorMessage").value(ErrorEnum.PORTFOLIO_NOT_FOUND.getErrorMessage()));
    }
  }

  @Nested
  @Order(3)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class registPortfolio {
    @Test
    @Order(1)
    @DisplayName("正常系：リクエストをModelに変換して登録し、採番したポートフォリオ番号を返すこと")
    void registPortfolio_success() throws Exception {
      doReturn(new PortfolioNo(7L)).when(portfolioService).registPortfolio(any());

      mockMvc
          .perform(
              post(PORTFOLIOS)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("save_grid_success.json")))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.isSuccess").value(true))
          .andExpect(jsonPath("$.message").value("ポートフォリオを登録しました。"))
          .andExpect(jsonPath("$.portfolioNo").value(7));

      ArgumentCaptor<PortfolioDetailModel> captor =
          ArgumentCaptor.forClass(PortfolioDetailModel.class);
      verify(portfolioService).registPortfolio(captor.capture());
      PortfolioDetailModel model = captor.getValue();
      assertEquals(new AccountNo(1L), model.getAccountNo());
      assertEquals(new PortfolioTheme("日本の自然"), model.getTheme());
      assertEquals(PortfolioTemplateEnum.GRID, model.getTemplateKbn());
      assertEquals(
          new TemplateSettings(
              Map.of(
                  "rows",
                  List.of(Map.of("pattern", "TWO_EQUAL"), Map.of("pattern", "THREE_EQUAL")))),
          model.getTemplateSettings());
      assertEquals(
          List.of(
              new PhotoNo(5L), new PhotoNo(3L), new PhotoNo(8L), new PhotoNo(1L), new PhotoNo(2L)),
          model.getPortfolioPhotoModelList().stream()
              .map(PortfolioPhotoModel::getPhotoNo)
              .toList());
      assertEquals(
          new TemplateSettings(Map.of("caption", "金閣寺")),
          model.getPortfolioPhotoModelList().get(0).getPhotoSettings());
      assertEquals(
          new TagJapaneseName("風景"), model.getPortfolioTagModelList().get(1).getTagJapaneseName());
      assertEquals(
          new TagEnglishName(""), model.getPortfolioTagModelList().get(1).getTagEnglishName());
    }

    @Test
    @Order(2)
    @DisplayName("異常系：テーマが空の場合、400を返しServiceを呼び出さないこと")
    void registPortfolio_blankTheme() throws Exception {
      mockMvc
          .perform(
              post(PORTFOLIOS)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("save_badrequest_blank_theme.json")))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.message").value(ErrorEnum.INVALID_INPUT.getErrorMessage()));

      verify(portfolioService, times(0)).registPortfolio(any());
    }

    @Test
    @Order(3)
    @DisplayName("異常系：未知のテンプレートを指定した場合、400を返しServiceを呼び出さないこと")
    void registPortfolio_unknownTemplate() throws Exception {
      mockMvc
          .perform(
              post(PORTFOLIOS)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("save_badrequest_unknown_template.json")))
          .andExpect(status().isBadRequest());

      verify(portfolioService, times(0)).registPortfolio(any());
    }

    @Test
    @Order(4)
    @DisplayName("異常系：写真一覧が未指定の場合、400を返しServiceを呼び出さないこと")
    void registPortfolio_missingPhotoList() throws Exception {
      mockMvc
          .perform(
              post(PORTFOLIOS)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("save_badrequest_missing_photo_list.json")))
          .andExpect(status().isBadRequest());

      verify(portfolioService, times(0)).registPortfolio(any());
    }

    @Test
    @Order(5)
    @DisplayName("異常系：写真一覧が空（0枚）の場合、下書きでも400を返しServiceを呼び出さないこと")
    void registPortfolio_emptyPhotoList() throws Exception {
      mockMvc
          .perform(
              post(PORTFOLIOS)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("save_badrequest_empty_photo_list.json")))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.message").value(ErrorEnum.INVALID_INPUT.getErrorMessage()));

      verify(portfolioService, times(0)).registPortfolio(any());
    }

    @Test
    @Order(6)
    @DisplayName("異常系：写真番号が0以下の場合、400を返しServiceを呼び出さないこと")
    void registPortfolio_invalidPhotoNo() throws Exception {
      mockMvc
          .perform(
              post(PORTFOLIOS)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("save_badrequest_invalid_photo_no.json")))
          .andExpect(status().isBadRequest());

      verify(portfolioService, times(0)).registPortfolio(any());
    }

    @Test
    @Order(7)
    @DisplayName("正常系：写真が上限（200枚）・タグが上限（30件）ちょうどの場合、Serviceを呼び出して登録すること")
    void registPortfolio_exactlyAtSizeLimit() throws Exception {
      doReturn(new PortfolioNo(7L)).when(portfolioService).registPortfolio(any());

      mockMvc
          .perform(
              post(PORTFOLIOS).contentType(MediaType.APPLICATION_JSON).content(saveJson(200, 30)))
          .andExpect(status().isOk());

      verify(portfolioService, times(1)).registPortfolio(any());
    }

    @Test
    @Order(8)
    @DisplayName("異常系：写真が上限（200枚）を超える場合、400を返しServiceを呼び出さないこと")
    void registPortfolio_tooManyPhotos() throws Exception {
      mockMvc
          .perform(
              post(PORTFOLIOS).contentType(MediaType.APPLICATION_JSON).content(saveJson(201, 0)))
          .andExpect(status().isBadRequest());

      verify(portfolioService, times(0)).registPortfolio(any());
    }

    @Test
    @Order(9)
    @DisplayName("異常系：タグが上限（30件）を超える場合、400を返しServiceを呼び出さないこと")
    void registPortfolio_tooManyTags() throws Exception {
      mockMvc
          .perform(
              post(PORTFOLIOS).contentType(MediaType.APPLICATION_JSON).content(saveJson(1, 31)))
          .andExpect(status().isBadRequest());

      verify(portfolioService, times(0)).registPortfolio(any());
    }

    @Test
    @Order(10)
    @DisplayName("異常系：作成数が上限に達している場合、400と上限に達した旨のメッセージを返すこと")
    void registPortfolio_reachedLimit() throws Exception {
      doThrow(ErrorEnum.REACHED_PORTFOLIO_LIMIT.toException())
          .when(portfolioService)
          .registPortfolio(any());

      mockMvc
          .perform(
              post(PORTFOLIOS)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("save_grid_success.json")))
          .andExpect(status().isBadRequest())
          .andExpect(
              jsonPath("$.message").value(ErrorEnum.REACHED_PORTFOLIO_LIMIT.getErrorMessage()));
    }

    @Test
    @Order(11)
    @DisplayName("異常系：認証ユーザーと異なるアカウントIDを指定した場合、403を返しServiceを呼び出さないこと")
    void registPortfolio_forbidden() throws Exception {
      doReturn("bbbbbbbb").when(sessionHelper).getAccountId();

      mockMvc
          .perform(
              post(PORTFOLIOS)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("save_grid_success.json")))
          .andExpect(status().isForbidden());

      verify(portfolioService, times(0)).registPortfolio(any());
    }

    @Test
    @Order(12)
    @DisplayName("異常系：テンプレートの設定内容が不正な場合、400とエラーメッセージを返すこと")
    void registPortfolio_invalidSettings() throws Exception {
      doThrow(ErrorEnum.INVALID_PORTFOLIO_SETTINGS.toException())
          .when(portfolioService)
          .registPortfolio(any());

      mockMvc
          .perform(
              post(PORTFOLIOS)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("save_grid_success.json")))
          .andExpect(status().isBadRequest())
          .andExpect(
              jsonPath("$.message").value(ErrorEnum.INVALID_PORTFOLIO_SETTINGS.getErrorMessage()));
    }

    @Test
    @Order(13)
    @DisplayName("異常系：登録に失敗した場合、409を返すこと")
    void registPortfolio_registFailure() throws Exception {
      doThrow(ErrorEnum.FAIL_TO_REGIST_PORTFOLIO.toException())
          .when(portfolioService)
          .registPortfolio(any());

      mockMvc
          .perform(
              post(PORTFOLIOS)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("save_grid_success.json")))
          .andExpect(status().isConflict())
          .andExpect(
              jsonPath("$.errorCode").value(ErrorEnum.FAIL_TO_REGIST_PORTFOLIO.getErrorCode()));
    }
  }

  @Nested
  @Order(4)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class updatePortfolio {
    @Test
    @Order(1)
    @DisplayName("正常系：パスパラメータのポートフォリオ番号を設定したModelで更新すること")
    void updatePortfolio_success() throws Exception {
      mockMvc
          .perform(
              put(PORTFOLIOS + "/2")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("save_grid_success.json")))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.message").value("ポートフォリオを更新しました。"))
          .andExpect(jsonPath("$.portfolioNo").value(2));

      ArgumentCaptor<PortfolioDetailModel> captor =
          ArgumentCaptor.forClass(PortfolioDetailModel.class);
      verify(portfolioService).updatePortfolio(captor.capture());
      assertEquals(new AccountNo(1L), captor.getValue().getAccountNo());
      assertEquals(new PortfolioNo(2L), captor.getValue().getPortfolioNo());
    }

    @Test
    @Order(2)
    @DisplayName("異常系：テーマが空の場合、400を返しServiceを呼び出さないこと")
    void updatePortfolio_blankTheme() throws Exception {
      mockMvc
          .perform(
              put(PORTFOLIOS + "/2")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("save_badrequest_blank_theme.json")))
          .andExpect(status().isBadRequest());

      verify(portfolioService, times(0)).updatePortfolio(any());
    }

    @Test
    @Order(3)
    @DisplayName("異常系：写真一覧が空（0枚）の場合、400を返しServiceを呼び出さないこと")
    void updatePortfolio_emptyPhotoList() throws Exception {
      mockMvc
          .perform(
              put(PORTFOLIOS + "/2")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("save_badrequest_empty_photo_list.json")))
          .andExpect(status().isBadRequest());

      verify(portfolioService, times(0)).updatePortfolio(any());
    }

    @Test
    @Order(4)
    @DisplayName("異常系：認証ユーザーと異なるアカウントIDを指定した場合、403を返しServiceを呼び出さないこと")
    void updatePortfolio_forbidden() throws Exception {
      doReturn("bbbbbbbb").when(sessionHelper).getAccountId();

      mockMvc
          .perform(
              put(PORTFOLIOS + "/2")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("save_grid_success.json")))
          .andExpect(status().isForbidden());

      verify(portfolioService, times(0)).updatePortfolio(any());
    }

    @Test
    @Order(5)
    @DisplayName("異常系：公開中で更新後の内容が公開できない場合、400とエラーメッセージを返すこと")
    void updatePortfolio_notPublishable() throws Exception {
      doThrow(ErrorEnum.PORTFOLIO_NOT_PUBLISHABLE.toException())
          .when(portfolioService)
          .updatePortfolio(any());

      mockMvc
          .perform(
              put(PORTFOLIOS + "/2")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("save_grid_success.json")))
          .andExpect(status().isBadRequest())
          .andExpect(
              jsonPath("$.message").value(ErrorEnum.PORTFOLIO_NOT_PUBLISHABLE.getErrorMessage()));
    }

    @Test
    @Order(6)
    @DisplayName("異常系：ポートフォリオが存在しない場合、404を返すこと")
    void updatePortfolio_notFound() throws Exception {
      doThrow(ErrorEnum.PORTFOLIO_NOT_FOUND.toException())
          .when(portfolioService)
          .updatePortfolio(any());

      mockMvc
          .perform(
              put(PORTFOLIOS + "/2")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("save_grid_success.json")))
          .andExpect(status().isNotFound());
    }
  }

  @Nested
  @Order(5)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class updatePortfolioStatus {
    @Test
    @Order(1)
    @DisplayName("正常系：公開を指定した場合、公開処理を呼び出すこと")
    void updatePortfolioStatus_publish() throws Exception {
      mockMvc
          .perform(
              put(PORTFOLIOS + "/2/status")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("status_published.json")))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.message").value("ポートフォリオを公開しました。"))
          .andExpect(jsonPath("$.portfolioNo").value(2));

      verify(portfolioService).publishPortfolio(new AccountNo(1L), new PortfolioNo(2L));
      verify(portfolioService, never()).unpublishPortfolio(any(), any());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：下書きを指定した場合、下書きに戻す処理を呼び出すこと")
    void updatePortfolioStatus_unpublish() throws Exception {
      mockMvc
          .perform(
              put(PORTFOLIOS + "/2/status")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("status_draft.json")))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.message").value("ポートフォリオを下書きに戻しました。"));

      verify(portfolioService).unpublishPortfolio(new AccountNo(1L), new PortfolioNo(2L));
      verify(portfolioService, never()).publishPortfolio(any(), any());
    }

    @Test
    @Order(3)
    @DisplayName("異常系：未知の公開状態を指定した場合、400を返しServiceを呼び出さないこと")
    void updatePortfolioStatus_unknownStatus() throws Exception {
      mockMvc
          .perform(
              put(PORTFOLIOS + "/2/status")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("status_badrequest_unknown.json")))
          .andExpect(status().isBadRequest());

      verify(portfolioService, times(0)).publishPortfolio(any(), any());
      verify(portfolioService, times(0)).unpublishPortfolio(any(), any());
    }

    @Test
    @Order(4)
    @DisplayName("異常系：認証ユーザーと異なるアカウントIDを指定した場合、403を返しServiceを呼び出さないこと")
    void updatePortfolioStatus_forbidden() throws Exception {
      doReturn("bbbbbbbb").when(sessionHelper).getAccountId();

      mockMvc
          .perform(
              put(PORTFOLIOS + "/2/status")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("status_published.json")))
          .andExpect(status().isForbidden());

      verify(portfolioService, times(0)).publishPortfolio(any(), any());
    }

    @Test
    @Order(5)
    @DisplayName("異常系：公開に必要な設定が不足している場合、400とエラーメッセージを返すこと")
    void updatePortfolioStatus_notPublishable() throws Exception {
      doThrow(ErrorEnum.PORTFOLIO_NOT_PUBLISHABLE.toException())
          .when(portfolioService)
          .publishPortfolio(any(), any());

      mockMvc
          .perform(
              put(PORTFOLIOS + "/2/status")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("status_published.json")))
          .andExpect(status().isBadRequest())
          .andExpect(
              jsonPath("$.message").value(ErrorEnum.PORTFOLIO_NOT_PUBLISHABLE.getErrorMessage()));
    }
  }

  @Nested
  @Order(6)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class deletePortfolio {
    @Test
    @Order(1)
    @DisplayName("正常系：ポートフォリオを削除できること")
    void deletePortfolio_success() throws Exception {
      mockMvc
          .perform(delete(PORTFOLIOS + "/2"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.message").value("ポートフォリオを削除しました。"))
          .andExpect(jsonPath("$.portfolioNo").value(2));

      verify(portfolioService).deletePortfolio(new AccountNo(1L), new PortfolioNo(2L));
    }

    @Test
    @Order(2)
    @DisplayName("異常系：認証ユーザーと異なるアカウントIDを指定した場合、403を返しServiceを呼び出さないこと")
    void deletePortfolio_forbidden() throws Exception {
      doReturn("bbbbbbbb").when(sessionHelper).getAccountId();

      mockMvc.perform(delete(PORTFOLIOS + "/2")).andExpect(status().isForbidden());

      verify(portfolioService, times(0)).deletePortfolio(any(), any());
    }

    @Test
    @Order(3)
    @DisplayName("異常系：ポートフォリオが存在しない場合、404を返すこと")
    void deletePortfolio_notFound() throws Exception {
      doThrow(ErrorEnum.PORTFOLIO_NOT_FOUND.toException())
          .when(portfolioService)
          .deletePortfolio(any(), any());

      mockMvc.perform(delete(PORTFOLIOS + "/2")).andExpect(status().isNotFound());
    }
  }

  @Nested
  @Order(7)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class getPortfolioTemplateList {
    @Test
    @Order(1)
    @DisplayName("正常系：テンプレートとグリッド型の行パターンの一覧を取得できること")
    void getPortfolioTemplateList_success() throws Exception {
      mockMvc
          .perform(get("/api/v1/portfolio-templates"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.templateList[0].templateKbn").value("feature"))
          .andExpect(jsonPath("$.templateList[1].templateKbn").value("grid"))
          .andExpect(jsonPath("$.gridRowPatternList[0].pattern").value("TWO_EQUAL"))
          .andExpect(jsonPath("$.gridRowPatternList[1].photoCount").value(3));

      verifyNoInteractions(portfolioService);
    }
  }

  @Nested
  @Order(8)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class getPortfolioUpperLimit {
    @Test
    @Order(1)
    @DisplayName("正常系：作成数が上限に達しているかを返すこと")
    void getPortfolioUpperLimit_success() throws Exception {
      doReturn(true).when(portfolioService).isReachedUpperLimit(new AccountNo(1L));

      mockMvc
          .perform(get(PORTFOLIOS + "/upper-limit"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.isReachedUpperLimit").value(true));
    }

    @Test
    @Order(2)
    @DisplayName("異常系：認証ユーザーと異なるアカウントIDを指定した場合、403を返しServiceを呼び出さないこと")
    void getPortfolioUpperLimit_forbidden() throws Exception {
      doReturn("bbbbbbbb").when(sessionHelper).getAccountId();

      mockMvc.perform(get(PORTFOLIOS + "/upper-limit")).andExpect(status().isForbidden());

      verify(portfolioService, times(0)).isReachedUpperLimit(any());
    }
  }

  @Nested
  @Order(9)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class searchPortfolioPhotos {
    private static final String PORTFOLIO_PHOTOS = "/api/v1/accounts/aaaaaaaa/portfolio-photos";

    @Test
    @Order(1)
    @DisplayName("正常系：検索条件をServiceに渡し、写真番号・タイトル・画像URL・向きと最後のページかどうかを返すこと")
    void searchPortfolioPhotos_success() throws Exception {
      doReturn(
              PortfolioPhotoPageModel.of(
                  PortfolioPhotoModelList.of(
                      List.of(
                          PortfolioPhotoModel.builder()
                              .photoNo(new PhotoNo(3L))
                              .photoSettings(TemplateSettings.empty())
                              .photoJapaneseTitle(new PhotoJapaneseTitle("金閣寺"))
                              .imageFilePath(new ImageFilePath("https://signed/1/3-aaa.jpg"))
                              .directionKbn(DirectionEnum.VERTICAL)
                              .build())),
                  true))
          .when(portfolioService)
          .searchPhotos(any());

      mockMvc
          .perform(
              get(PORTFOLIO_PHOTOS)
                  .param("title", "金閣")
                  .param("location", "京都")
                  .param("tag", " ")
                  .param("pageNo", "2"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.photoList[0].photoNo").value(3))
          .andExpect(jsonPath("$.photoList[0].photoJapaneseTitle").value("金閣寺"))
          .andExpect(jsonPath("$.photoList[0].imageFilePath").value("https://signed/1/3-aaa.jpg"))
          .andExpect(jsonPath("$.photoList[0].directionKbn").value("vertical"))
          .andExpect(jsonPath("$.isLast").value(true));

      verify(portfolioService)
          .searchPhotos(
              PortfolioPhotoSearchModel.builder()
                  .accountNo(new AccountNo(1L))
                  .title("金閣")
                  .location("京都")
                  .pageNo(2)
                  .build());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：ページ番号が未指定の場合、1ページ目を検索すること")
    void searchPortfolioPhotos_defaultPage() throws Exception {
      doReturn(PortfolioPhotoPageModel.of(PortfolioPhotoModelList.empty(), true))
          .when(portfolioService)
          .searchPhotos(any());

      mockMvc.perform(get(PORTFOLIO_PHOTOS)).andExpect(status().isOk());

      verify(portfolioService)
          .searchPhotos(
              PortfolioPhotoSearchModel.builder().accountNo(new AccountNo(1L)).pageNo(1).build());
    }

    @Test
    @Order(3)
    @DisplayName("異常系：ページ番号が0以下の場合、400を返しServiceを呼び出さないこと")
    void searchPortfolioPhotos_invalidPageNo() throws Exception {
      mockMvc
          .perform(get(PORTFOLIO_PHOTOS).param("pageNo", "0"))
          .andExpect(status().isBadRequest());

      verify(portfolioService, times(0)).searchPhotos(any());
    }

    @Test
    @Order(4)
    @DisplayName("異常系：認証ユーザーと異なるアカウントIDを指定した場合、403を返しServiceを呼び出さないこと")
    void searchPortfolioPhotos_forbidden() throws Exception {
      doReturn("bbbbbbbb").when(sessionHelper).getAccountId();

      mockMvc.perform(get(PORTFOLIO_PHOTOS)).andExpect(status().isForbidden());

      verify(portfolioService, times(0)).searchPhotos(any());
    }
  }
}
