package com.web.gallery.controller.integration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.web.gallery.AccountPrincipal;
import com.web.gallery.aggregate.Photo;
import com.web.gallery.constant.MessageConst;
import com.web.gallery.domain.account.AccountId;
import com.web.gallery.domain.account.AccountName;
import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.account.Password;
import com.web.gallery.domain.photo.ImageFilePath;
import com.web.gallery.entity.photo.PhotoFavorite;
import com.web.gallery.entity.photo.PhotoMst;
import com.web.gallery.entity.photo.PhotoTagMst;
import com.web.gallery.enumeration.AuthorityEnum;
import com.web.gallery.enumeration.DirectionEnum;
import com.web.gallery.enumeration.ErrorEnum;
import com.web.gallery.model.account.AccountModel;
import com.web.gallery.repository.FileRepository;
import com.web.gallery.repository.PhotoAggregateRepository;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
@AutoConfigureMockMvc
public class PhotoControllerIntegrationTest {
  /** 新規登録時のバリデーション（Content-Type・マジックバイト）を通過させるための、実際のJPEGファイルの先頭バイト列 */
  private static final byte[] JPEG_BYTES = {
    (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0x00, 0x10
  };

  @Autowired private MockMvc mockMvc;

  @Autowired private JdbcTemplate jdbcTemplate;

  /**
   * サーバ生成の不透明オブジェクトキー（{@code {accountId}/{写真番号}-{ランダム32桁}.{拡張子}}）であることを検証する
   *
   * @param key 実際のキー
   * @param accountId アカウントID
   * @param photoNo 写真番号
   * @param extension 拡張子（ドットなし）
   */
  private static void assertOpaqueObjectKey(
      String key, String accountId, long photoNo, String extension) {
    String expectedPattern = "^" + accountId + "/" + photoNo + "-[0-9a-f]{32}\\." + extension + "$";
    assertTrue(
        key.matches(expectedPattern),
        "オブジェクトキーが不正です。expected pattern: " + expectedPattern + ", actual: " + key);
  }

  /**
   * 指定のピクセルサイズを持つ実際のJPEGバイト列を生成する
   *
   * <p>PhotoDirectionResolverによる向き区分の自動判定を実際のImageIOの処理を通して検証するために使用する （{@link
   * #JPEG_BYTES}のようなマジックバイトのみのダミーではピクセルサイズが判定できないため）
   *
   * @param width 幅（ピクセル）
   * @param height 高さ（ピクセル）
   * @return JPEGバイト列
   */
  private static byte[] createJpegBytes(int width, int height) throws IOException {
    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    ImageIO.write(image, "jpg", outputStream);
    return outputStream.toByteArray();
  }

  /** S3ストレージアクセスはモックする（統合テストでは実ストレージへ接続しない）。 署名付きURL発行は渡されたオブジェクトキーをそのまま返し、キーベースのアサーションを維持する。 */
  @MockitoBean private FileRepository fileRepository;

  @BeforeEach
  void setUpFileRepositoryStub() {
    lenient()
        .when(fileRepository.getPresignedUrl(any(ImageFilePath.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
  }

  /**
   * 写真一覧の絞り込みログはREQUIRES_NEWで独立した別コネクションのトランザクションとして書き込むため、
   * フィクスチャ（{@code @Sql}）で投入したaccount行が本テストのトランザクション内で未コミットのままだと、
   * 外部キー制約の検証がその行のコミットを待ち続けて自己デッドロックする。 そのため、フィクスチャ投入後にここで一度物理コミットしてから新しいテスト用トランザクションを開始する
   */
  @BeforeEach
  void commitFixtures() {
    TestTransaction.flagForCommit();
    TestTransaction.end();
    TestTransaction.start();
  }

  /**
   * commitFixturesで物理コミットしたフィクスチャ・テスト結果が他のテストクラスへ残留しないよう、 テスト終了後に明示的にTRUNCATE（CASCADE）して物理コミットする
   */
  @AfterEach
  void cleanUpCommittedFixtures() {
    TestTransaction.end();
    TestTransaction.start();
    jdbcTemplate.execute(
        """
					TRUNCATE TABLE
						photo.photo_favorite,
						photo.photo_tag_mst,
						photo.photo_mst,
						common.refresh_token,
						common.location_mst,
						common.account,
						common.kbn_mst
					CASCADE
					""");
    TestTransaction.flagForCommit();
    TestTransaction.end();
  }

  private String readJsonFile(String fileName) throws Exception {
    return new String(
        new ClassPathResource(
                "json/controller/integration/PhotoControllerIntegrationTest/" + fileName)
            .getInputStream()
            .readAllBytes(),
        StandardCharsets.UTF_8);
  }

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/controller/PhotoControllerIntegrationTest.sql")
  class getPhotoList {
    @Test
    @Order(1)
    @DisplayName("正常系：Nullのパラメータがある場合")
    void getPhotoList_with_null_parameter() throws JacksonException, Exception {
      String photoAccountId = "aaaaaaaa";

      MvcResult result =
          mockMvc
              .perform(get("/api/v1/accounts/" + photoAccountId + "/photos"))
              .andExpect(status().isOk())
              .andExpect(content().contentType(MediaType.APPLICATION_JSON))
              .andExpect(jsonPath("$.isLast").value(false))
              .andReturn();

      String jsonResponse = result.getResponse().getContentAsString();
      ObjectMapper objectMapper = new ObjectMapper();
      JsonNode photoList = objectMapper.readTree(jsonResponse).get("photoList");
      assertEquals(5, photoList.size());

      assertEquals(1, photoList.get(0).get("accountNo").asInt());
      assertEquals(9, photoList.get(0).get("photoNo").asInt());
      assertFalse(photoList.get(0).get("isFavorite").asBoolean());
      assertEquals(
          "https://www.xxx.com/aaaaaaaa/DSC19.jpg", photoList.get(0).get("imageFilePath").asText());
      assertEquals("caption19", photoList.get(0).get("caption").asText());
      assertEquals(
          DirectionEnum.HORIZONTAL,
          DirectionEnum.getOrDefault(photoList.get(0).get("directionKbn").asText()));

      assertEquals(1, photoList.get(1).get("accountNo").asInt());
      assertEquals(8, photoList.get(1).get("photoNo").asInt());
      assertFalse(photoList.get(1).get("isFavorite").asBoolean());
      assertEquals(
          "https://www.xxx.com/aaaaaaaa/DSC18.jpg", photoList.get(1).get("imageFilePath").asText());
      assertEquals("caption18", photoList.get(1).get("caption").asText());
      assertEquals(
          DirectionEnum.VERTICAL,
          DirectionEnum.getOrDefault(photoList.get(1).get("directionKbn").asText()));

      assertEquals(1, photoList.get(2).get("accountNo").asInt());
      assertEquals(7, photoList.get(2).get("photoNo").asInt());
      assertFalse(photoList.get(2).get("isFavorite").asBoolean());
      assertEquals(
          "https://www.xxx.com/aaaaaaaa/DSC17.jpg", photoList.get(2).get("imageFilePath").asText());
      assertEquals("caption17", photoList.get(2).get("caption").asText());
      assertEquals(
          DirectionEnum.VERTICAL,
          DirectionEnum.getOrDefault(photoList.get(2).get("directionKbn").asText()));

      assertEquals(1, photoList.get(3).get("accountNo").asInt());
      assertEquals(6, photoList.get(3).get("photoNo").asInt());
      assertFalse(photoList.get(3).get("isFavorite").asBoolean());
      assertEquals(
          "https://www.xxx.com/aaaaaaaa/DSC16.jpg", photoList.get(3).get("imageFilePath").asText());
      assertEquals("caption16", photoList.get(3).get("caption").asText());
      assertEquals(
          DirectionEnum.HORIZONTAL,
          DirectionEnum.getOrDefault(photoList.get(3).get("directionKbn").asText()));

      assertEquals(1, photoList.get(4).get("accountNo").asInt());
      assertEquals(5, photoList.get(4).get("photoNo").asInt());
      assertFalse(photoList.get(4).get("isFavorite").asBoolean());
      assertEquals(
          "https://www.xxx.com/aaaaaaaa/DSC15.jpg", photoList.get(4).get("imageFilePath").asText());
      assertEquals("caption15", photoList.get(4).get("caption").asText());
      assertEquals(
          DirectionEnum.VERTICAL,
          DirectionEnum.getOrDefault(photoList.get(4).get("directionKbn").asText()));
    }

    @Test
    @Order(2)
    @DisplayName("正常系：タグに半角スペースが含まれている場合")
    void getPhotoList_with_halfspace_tag() throws JacksonException, Exception {
      String photoAccountId = "aaaaaaaa";

      MvcResult result =
          mockMvc
              .perform(
                  get("/api/v1/accounts/" + photoAccountId + "/photos").param("tagList", "太陽 青空"))
              .andExpect(status().isOk())
              .andExpect(content().contentType(MediaType.APPLICATION_JSON))
              .andExpect(jsonPath("$.isLast").value(true))
              .andReturn();

      String jsonResponse = result.getResponse().getContentAsString();
      ObjectMapper objectMapper = new ObjectMapper();
      JsonNode photoList = objectMapper.readTree(jsonResponse).get("photoList");
      assertEquals(1, photoList.size());

      assertEquals(1, photoList.get(0).get("accountNo").asInt());
      assertEquals(1, photoList.get(0).get("photoNo").asInt());
      assertFalse(photoList.get(0).get("isFavorite").asBoolean());
      assertEquals(
          "https://www.xxx.com/aaaaaaaa/DSC11.jpg", photoList.get(0).get("imageFilePath").asText());
      assertEquals("caption11", photoList.get(0).get("caption").asText());
      assertEquals(
          DirectionEnum.HORIZONTAL,
          DirectionEnum.getOrDefault(photoList.get(0).get("directionKbn").asText()));
    }

    @Test
    @Order(3)
    @DisplayName("正常系：タグに全角スペースが含まれている場合")
    void getPhotoList_with_fullspace_tag() throws JacksonException, Exception {
      String photoAccountId = "aaaaaaaa";

      MvcResult result =
          mockMvc
              .perform(
                  get("/api/v1/accounts/" + photoAccountId + "/photos").param("tagList", "太陽　青空"))
              .andExpect(status().isOk())
              .andExpect(content().contentType(MediaType.APPLICATION_JSON))
              .andExpect(jsonPath("$.isLast").value(true))
              .andReturn();

      String jsonResponse = result.getResponse().getContentAsString();
      ObjectMapper objectMapper = new ObjectMapper();
      JsonNode photoList = objectMapper.readTree(jsonResponse).get("photoList");
      assertEquals(1, photoList.size());

      assertEquals(1, photoList.get(0).get("accountNo").asInt());
      assertEquals(1, photoList.get(0).get("photoNo").asInt());
      assertFalse(photoList.get(0).get("isFavorite").asBoolean());
      assertEquals(
          "https://www.xxx.com/aaaaaaaa/DSC11.jpg", photoList.get(0).get("imageFilePath").asText());
      assertEquals("caption11", photoList.get(0).get("caption").asText());
      assertEquals(
          DirectionEnum.HORIZONTAL,
          DirectionEnum.getOrDefault(photoList.get(0).get("directionKbn").asText()));
    }

    @Test
    @Order(4)
    @DisplayName("正常系：写真が0件の場合")
    void getPhotoList_not_found_photo() throws JacksonException, Exception {
      String photoAccountId = "aaaaaaaa";

      MvcResult result =
          mockMvc
              .perform(
                  get("/api/v1/accounts/" + photoAccountId + "/photos").param("tagList", "太陽　海"))
              .andExpect(status().isOk())
              .andExpect(content().contentType(MediaType.APPLICATION_JSON))
              .andExpect(jsonPath("$.isLast").value(true))
              .andReturn();

      String jsonResponse = result.getResponse().getContentAsString();
      ObjectMapper objectMapper = new ObjectMapper();
      JsonNode photoList = objectMapper.readTree(jsonResponse).get("photoList");
      assertEquals(0, photoList.size());
    }

    @Test
    @Order(5)
    @DisplayName("異常系：pageNoが不正な場合は400を返す")
    void getPhotoList_badRequest() throws Exception {
      String photoAccountId = "aaaaaaaa";

      mockMvc
          .perform(get("/api/v1/accounts/" + photoAccountId + "/photos").param("pageNo", "0"))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.message").value(MessageConst.ERR_INVALID_INPUT));
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/controller/PhotoControllerIntegrationTest.sql")
  class savePhoto {
    @Test
    @Order(1)
    @DisplayName("正常系：更新。写真タグあり、撮影日時あり。Nullパラメータなし。リクエストの画像ファイルパスは無視されDB上の既存パスが維持される")
    void savePhoto_updatePhoto_with_photoTag_and_photoAt() throws Exception {
      String photoAccountId = "bbbbbbbb";

      AccountModel sessionAccount =
          AccountModel.builder()
              .accountNo(new AccountNo(2L))
              .accountId(new AccountId("bbbbbbbb"))
              .accountName(new AccountName("BBBBBBBB"))
              .password(new Password("$2a$10$password2"))
              .authorityKbn(AuthorityEnum.ADMINISTRATOR)
              .build();

      AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
      Authentication authentication =
          new UsernamePasswordAuthenticationToken(
              accountPrincipal, null, accountPrincipal.getAuthorities());

      OffsetDateTime transactionNow =
          jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);
      mockMvc
          .perform(
              multipart(HttpMethod.PUT, "/api/v1/accounts/" + photoAccountId + "/photos")
                  .contentType(MediaType.MULTIPART_FORM_DATA)
                  .param("photoNo", "1")
                  .param("caption", "caption111")
                  // リクエストのimageFilePathは攻撃を模した値。DB上の既存パスを信用し、この値は保存に使われないことを検証する
                  .param("imageFilePath", "https://evil.example.com/malicious/path.jpg")
                  .param("directionKbn", "VERTICAL")
                  .param("photoEnglishTitle", "title111")
                  .param("photoJapaneseTitle", "タイトル111")
                  .param(
                      "photoAt",
                      LocalDateTime.of(2000, 1, 1, 0, 0, 0)
                          .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                  .param("focalLength", "24")
                  .param("fValue", "8.0")
                  .param("shutterSpeed", "0.01")
                  .param("iso", "100")
                  .param("photoTagRegistRequestList[0].accountNo", "2")
                  .param("photoTagRegistRequestList[0].photoNo", "1")
                  .param("photoTagRegistRequestList[0].tagJapaneseName", "太陽")
                  .param("photoTagRegistRequestList[0].tagEnglishName", "sun")
                  .param("photoTagRegistRequestList[1].accountNo", "2")
                  .param("photoTagRegistRequestList[1].photoNo", "1")
                  .param("photoTagRegistRequestList[1].tagJapaneseName", "青空")
                  .param("photoTagRegistRequestList[1].tagEnglishName", "bluesky")
                  .with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
                  .with(csrf()))
          .andExpect(status().isOk())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.httpStatus").value(HttpStatus.OK.value()))
          .andExpect(jsonPath("$.isSuccess").value(true))
          .andExpect(jsonPath("$.message").value("写真登録が完了しました。"));

      // photo_mst登録チェック
      List<PhotoMst> actualPhotoMst =
          jdbcTemplate.query(
              "SELECT * FROM photo.photo_mst where account_no = 2 and photo_no=1",
              (rs, rowNum) ->
                  PhotoMst.builder()
                      .accountNo(rs.getLong("account_no"))
                      .photoNo(rs.getLong("photo_no"))
                      .createdBy(rs.getLong("created_by"))
                      .createdAt(rs.getObject("created_at", OffsetDateTime.class))
                      .updatedBy(rs.getLong("updated_by"))
                      .updatedAt(rs.getObject("updated_at", OffsetDateTime.class))
                      .isDeleted(rs.getBoolean("is_deleted"))
                      .photoAt(rs.getObject("photo_at", OffsetDateTime.class))
                      .locationNo(rs.getLong("location_no"))
                      .imageFilePath(rs.getString("image_file_path"))
                      .photoJapaneseTitle(rs.getString("photo_japanese_title"))
                      .photoEnglishTitle(rs.getString("photo_english_title"))
                      .caption(rs.getString("caption"))
                      .directionKbn(DirectionEnum.getOrDefault(rs.getString("direction_kbn")))
                      .focalLength(rs.getInt("focal_length"))
                      .fValue(rs.getBigDecimal("f_value"))
                      .shutterSpeed(rs.getBigDecimal("shutter_speed"))
                      .iso(rs.getInt("iso"))
                      .build());

      assertEquals(1, actualPhotoMst.size());
      assertEquals(2L, actualPhotoMst.getFirst().getAccountNo());
      assertEquals(1L, actualPhotoMst.getFirst().getPhotoNo());
      assertEquals(2L, actualPhotoMst.getFirst().getCreatedBy());
      assertEquals(
          OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)),
          actualPhotoMst.getFirst().getCreatedAt());
      assertEquals(2L, actualPhotoMst.getFirst().getUpdatedBy());
      assertEquals(transactionNow, actualPhotoMst.getFirst().getUpdatedAt());
      assertFalse(actualPhotoMst.getFirst().getIsDeleted());
      assertEquals(
          OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)),
          actualPhotoMst.getFirst().getPhotoAt().plusHours(9));
      assertEquals(0L, actualPhotoMst.getFirst().getLocationNo());
      // リクエストの悪意あるimageFilePathは無視され、DB上の既存パスのまま更新されることを検証
      assertEquals(
          "https://www.xxx.com/bbbbbbbb/DSC21.jpg", actualPhotoMst.getFirst().getImageFilePath());
      assertEquals("タイトル111", actualPhotoMst.getFirst().getPhotoJapaneseTitle());
      assertEquals("title111", actualPhotoMst.getFirst().getPhotoEnglishTitle());
      assertEquals("caption111", actualPhotoMst.getFirst().getCaption());
      assertEquals(DirectionEnum.VERTICAL, actualPhotoMst.getFirst().getDirectionKbn());
      assertEquals(24, actualPhotoMst.getFirst().getFocalLength());
      assertEquals(0, BigDecimal.valueOf(8.0).compareTo(actualPhotoMst.getFirst().getFValue()));
      assertEquals(
          0, BigDecimal.valueOf(0.01).compareTo(actualPhotoMst.getFirst().getShutterSpeed()));
      assertEquals(100, actualPhotoMst.getFirst().getIso());

      // photo_tag_mst登録チェック（更新時は既存タグを一旦削除してから再登録するため、いずれも新規登録扱い）
      List<PhotoTagMst> actualPhotoTagMst =
          jdbcTemplate.query(
              "SELECT * FROM photo.photo_tag_mst WHERE account_no=2 and photo_no=1",
              (rs, rowNum) ->
                  PhotoTagMst.builder()
                      .accountNo(rs.getLong("account_no"))
                      .photoNo(rs.getLong("photo_no"))
                      .tagNo(rs.getLong("tag_no"))
                      .createdBy(rs.getLong("created_by"))
                      .createdAt(rs.getObject("created_at", OffsetDateTime.class))
                      .tagJapaneseName(rs.getObject("tag_japanese_name").toString())
                      .tagEnglishName(rs.getObject("tag_english_name").toString())
                      .build());
      assertEquals(2, actualPhotoTagMst.size());

      assertEquals(2L, actualPhotoTagMst.get(0).getAccountNo());
      assertEquals(1L, actualPhotoTagMst.get(0).getPhotoNo());
      assertEquals(1L, actualPhotoTagMst.get(0).getTagNo());
      assertEquals(2L, actualPhotoTagMst.get(0).getCreatedBy());
      assertEquals(transactionNow, actualPhotoTagMst.get(0).getCreatedAt());
      assertEquals("太陽", actualPhotoTagMst.get(0).getTagJapaneseName());
      assertEquals("sun", actualPhotoTagMst.get(0).getTagEnglishName());
      assertEquals(2L, actualPhotoTagMst.get(1).getAccountNo());
      assertEquals(1L, actualPhotoTagMst.get(1).getPhotoNo());
      assertEquals(2L, actualPhotoTagMst.get(1).getTagNo());
      assertEquals(2L, actualPhotoTagMst.get(1).getCreatedBy());
      assertEquals(transactionNow, actualPhotoTagMst.get(1).getCreatedAt());
      assertEquals("青空", actualPhotoTagMst.get(1).getTagJapaneseName());
      assertEquals("bluesky", actualPhotoTagMst.get(1).getTagEnglishName());
    }

    @Test
    @Order(2)
    @DisplayName("異常系：アクセス不正。ForbiddenAccountExceptionをthrowする")
    void savePhoto_ForbiddenAccountException() throws Exception {
      String photoAccountId = "bbbbbbbb";

      AccountModel sessionAccount =
          AccountModel.builder()
              .accountNo(new AccountNo(1L))
              .accountId(new AccountId("aaaaaaaa"))
              .accountName(new AccountName("AAAAAAAA"))
              .password(new Password("$2a$10$password1"))
              .authorityKbn(AuthorityEnum.ADMINISTRATOR)
              .build();

      AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
      Authentication authentication =
          new UsernamePasswordAuthenticationToken(
              accountPrincipal, null, accountPrincipal.getAuthorities());

      mockMvc
          .perform(
              multipart(HttpMethod.PUT, "/api/v1/accounts/" + photoAccountId + "/photos")
                  .contentType(MediaType.MULTIPART_FORM_DATA)
                  .param("photoNo", "1")
                  .param("imageFilePath", "https://www.xxx.com/bbbbbbbb/DSC21.jpg")
                  .with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
                  .with(csrf()))
          .andExpect(status().isForbidden())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.httpStatus").value(HttpStatus.FORBIDDEN.value()))
          .andExpect(
              jsonPath("$.errorCode").value(ErrorEnum.NOT_AUTHORIZED_TO_EDIT_PHOTO.getErrorCode()))
          .andExpect(
              jsonPath("$.errorMessage")
                  .value(ErrorEnum.NOT_AUTHORIZED_TO_EDIT_PHOTO.getErrorMessage()));
    }

    @Test
    @Order(3)
    @DisplayName("異常系：画像ファイルパスが未指定。BadRequestExceptionをthrowする")
    void savePhoto_BadRequestException_filepath_is_null() throws Exception {
      String photoAccountId = "bbbbbbbb";

      AccountModel sessionAccount =
          AccountModel.builder()
              .accountNo(new AccountNo(2L))
              .accountId(new AccountId("bbbbbbbb"))
              .accountName(new AccountName("BBBBBBBB"))
              .password(new Password("$2a$10$password2"))
              .authorityKbn(AuthorityEnum.MINI)
              .build();

      AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
      Authentication authentication =
          new UsernamePasswordAuthenticationToken(
              accountPrincipal, null, accountPrincipal.getAuthorities());

      mockMvc
          .perform(
              multipart(HttpMethod.PUT, "/api/v1/accounts/" + photoAccountId + "/photos")
                  .contentType(MediaType.MULTIPART_FORM_DATA)
                  .param("photoNo", "1")
                  .with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
                  .with(csrf()))
          .andExpect(status().isBadRequest())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.httpStatus").value(HttpStatus.BAD_REQUEST.value()))
          .andExpect(jsonPath("$.isSuccess").value(false))
          .andExpect(jsonPath("$.message").value(ErrorEnum.INVALID_INPUT.getErrorMessage()));
    }

    @Test
    @Order(4)
    @DisplayName("異常系：画像ファイルパス以外のパラメータ不正。BadRequestExceptionをthrowする")
    void savePhoto_BadRequestException_others() throws Exception {
      String photoAccountId = "bbbbbbbb";

      AccountModel sessionAccount =
          AccountModel.builder()
              .accountNo(new AccountNo(2L))
              .accountId(new AccountId("bbbbbbbb"))
              .accountName(new AccountName("BBBBBBBB"))
              .password(new Password("$2a$10$password2"))
              .authorityKbn(AuthorityEnum.ADMINISTRATOR)
              .build();

      AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
      Authentication authentication =
          new UsernamePasswordAuthenticationToken(
              accountPrincipal, null, accountPrincipal.getAuthorities());

      mockMvc
          .perform(
              multipart(HttpMethod.PUT, "/api/v1/accounts/" + photoAccountId + "/photos")
                  .contentType(MediaType.MULTIPART_FORM_DATA)
                  .param("photoNo", "1")
                  .param("imageFilePath", "https://www.xxx.com/bbbbbbbb/DSC21.jpg")
                  .param("focalLength", "-1")
                  .with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
                  .with(csrf()))
          .andExpect(status().isBadRequest())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.httpStatus").value(HttpStatus.BAD_REQUEST.value()))
          .andExpect(jsonPath("$.isSuccess").value(false))
          .andExpect(jsonPath("$.message").value(ErrorEnum.INVALID_INPUT.getErrorMessage()));
    }

    @Test
    @Order(5)
    @DisplayName("異常系：存在しない写真番号を指定した場合、PhotoNotFoundExceptionをthrowする")
    void savePhoto_PhotoNotFoundException() throws Exception {
      String photoAccountId = "bbbbbbbb";

      AccountModel sessionAccount =
          AccountModel.builder()
              .accountNo(new AccountNo(2L))
              .accountId(new AccountId("bbbbbbbb"))
              .accountName(new AccountName("BBBBBBBB"))
              .password(new Password("$2a$10$password2"))
              .authorityKbn(AuthorityEnum.ADMINISTRATOR)
              .build();

      AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
      Authentication authentication =
          new UsernamePasswordAuthenticationToken(
              accountPrincipal, null, accountPrincipal.getAuthorities());

      mockMvc
          .perform(
              multipart(HttpMethod.PUT, "/api/v1/accounts/" + photoAccountId + "/photos")
                  .contentType(MediaType.MULTIPART_FORM_DATA)
                  .param("photoNo", "99")
                  .param("caption", "caption21")
                  .param("imageFilePath", "https://www.xxx.com/DSC99.jpg")
                  .param("directionKbn", "VERTICAL")
                  .param("photoEnglishTitle", "")
                  .param("photoJapaneseTitle", "タイトル")
                  .with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
                  .with(csrf()))
          .andExpect(status().isNotFound())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.httpStatus").value(HttpStatus.NOT_FOUND.value()))
          .andExpect(jsonPath("$.errorCode").value(ErrorEnum.PHOTO_NOT_FOUND.getErrorCode()))
          .andExpect(jsonPath("$.errorMessage").value(ErrorEnum.PHOTO_NOT_FOUND.getErrorMessage()));
    }
  }

  @Nested
  @Order(3)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/controller/PhotoControllerIntegrationTest.sql")
  class registPhotos {
    @Test
    @Order(1)
    @DisplayName("正常系：新規登録。写真1枚、タグなし、撮影日時なし。Nullパラメータあり。向き区分は画像の実際のピクセルサイズから判定されること")
    void registPhotos_single_not_photoTag_and_photoAt() throws Exception {
      String photoAccountId = "bbbbbbbb";
      // 縦長（幅<高さ）の画像。向き区分がクライアントの指定でなく、実際のピクセルサイズから判定されることを検証する
      MockMultipartFile multipartFile =
          new MockMultipartFile(
              "imageFiles", "DSC111.jpg", MediaType.IMAGE_JPEG_VALUE, createJpegBytes(100, 200));

      AccountModel sessionAccount =
          AccountModel.builder()
              .accountNo(new AccountNo(2L))
              .accountId(new AccountId("bbbbbbbb"))
              .accountName(new AccountName("BBBBBBBB"))
              .password(new Password("$2a$10$password2"))
              .authorityKbn(AuthorityEnum.ADMINISTRATOR)
              .build();

      AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
      Authentication authentication =
          new UsernamePasswordAuthenticationToken(
              accountPrincipal, null, accountPrincipal.getAuthorities());

      OffsetDateTime transactionNow =
          jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);
      mockMvc
          .perform(
              multipart("/api/v1/accounts/" + photoAccountId + "/photos")
                  .file(multipartFile)
                  .contentType(MediaType.MULTIPART_FORM_DATA)
                  .param("caption", "")
                  .param("photoEnglishTitle", "")
                  .param("photoJapaneseTitle", "タイトル4")
                  .with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
                  .with(csrf()))
          .andExpect(status().isOk())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.httpStatus").value(HttpStatus.OK.value()))
          .andExpect(jsonPath("$.isSuccess").value(true))
          .andExpect(jsonPath("$.message").value("写真登録が完了しました。"))
          .andExpect(jsonPath("$.registeredCount").value(1));

      // photo_mst登録チェック
      List<PhotoMst> actualPhotoMst =
          jdbcTemplate.query(
              "SELECT * FROM photo.photo_mst where account_no = 2 and photo_no=4",
              (rs, rowNum) ->
                  PhotoMst.builder()
                      .accountNo(rs.getLong("account_no"))
                      .photoNo(rs.getLong("photo_no"))
                      .createdBy(rs.getLong("created_by"))
                      .createdAt(rs.getObject("created_at", OffsetDateTime.class))
                      .updatedBy(rs.getLong("updated_by"))
                      .updatedAt(rs.getObject("updated_at", OffsetDateTime.class))
                      .isDeleted(rs.getBoolean("is_deleted"))
                      .photoAt(rs.getObject("photo_at", OffsetDateTime.class))
                      .locationNo(rs.getLong("location_no"))
                      .imageFilePath(rs.getString("image_file_path"))
                      .photoJapaneseTitle(rs.getString("photo_japanese_title"))
                      .photoEnglishTitle(rs.getString("photo_english_title"))
                      .caption(rs.getString("caption"))
                      .directionKbn(DirectionEnum.getOrDefault(rs.getString("direction_kbn")))
                      .focalLength(rs.getInt("focal_length"))
                      .fValue(rs.getBigDecimal("f_value"))
                      .shutterSpeed(rs.getBigDecimal("shutter_speed"))
                      .iso(rs.getInt("iso"))
                      .build());

      assertEquals(1, actualPhotoMst.size());
      assertEquals(2L, actualPhotoMst.getFirst().getAccountNo());
      assertEquals(4L, actualPhotoMst.getFirst().getPhotoNo());
      assertEquals(2L, actualPhotoMst.getFirst().getCreatedBy());
      assertEquals(transactionNow, actualPhotoMst.getFirst().getCreatedAt());
      assertEquals(2L, actualPhotoMst.getFirst().getUpdatedBy());
      assertEquals(transactionNow, actualPhotoMst.getFirst().getUpdatedAt());
      assertFalse(actualPhotoMst.getFirst().getIsDeleted());
      assertEquals(
          OffsetDateTime.of(1900, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)),
          actualPhotoMst.getFirst().getPhotoAt().plusHours(9));
      assertEquals(0L, actualPhotoMst.getFirst().getLocationNo());
      assertOpaqueObjectKey(actualPhotoMst.getFirst().getImageFilePath(), "bbbbbbbb", 4L, "jpg");
      assertEquals(
          "DSC111.jpg",
          jdbcTemplate.queryForObject(
              "SELECT image_file_name FROM photo.photo_mst WHERE account_no=2 AND photo_no=4",
              String.class));
      assertEquals("タイトル4", actualPhotoMst.getFirst().getPhotoJapaneseTitle());
      assertEquals("", actualPhotoMst.getFirst().getPhotoEnglishTitle());
      assertEquals("", actualPhotoMst.getFirst().getCaption());
      assertEquals(DirectionEnum.VERTICAL, actualPhotoMst.getFirst().getDirectionKbn());
      assertEquals(0, actualPhotoMst.getFirst().getFocalLength());
      assertEquals(0, BigDecimal.ZERO.compareTo(actualPhotoMst.getFirst().getFValue()));
      assertEquals(0, BigDecimal.ZERO.compareTo(actualPhotoMst.getFirst().getShutterSpeed()));
      assertEquals(0, actualPhotoMst.getFirst().getIso());

      // photo_tag_mst登録チェック
      List<PhotoTagMst> actualPhotoTagMst =
          jdbcTemplate.query(
              "SELECT * FROM photo.photo_tag_mst WHERE account_no=2 and photo_no=4",
              (rs, rowNum) ->
                  PhotoTagMst.builder()
                      .accountNo(rs.getLong("account_no"))
                      .photoNo(rs.getLong("photo_no"))
                      .tagNo(rs.getLong("tag_no"))
                      .createdBy(rs.getLong("created_by"))
                      .createdAt(rs.getObject("created_at", OffsetDateTime.class))
                      .tagJapaneseName(rs.getObject("tag_japanese_name").toString())
                      .tagEnglishName(rs.getObject("tag_english_name").toString())
                      .build());
      assertEquals(0, actualPhotoTagMst.size());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：新規一括登録。写真3枚に共通のタイトル〜タグを設定して登録する。縦・横・正方形が混在しても写真ごとに正しい向きで登録されること")
    void registPhotos_multiple_with_common_metadata() throws Exception {
      String photoAccountId = "bbbbbbbb";

      AccountModel sessionAccount =
          AccountModel.builder()
              .accountNo(new AccountNo(2L))
              .accountId(new AccountId("bbbbbbbb"))
              .accountName(new AccountName("BBBBBBBB"))
              .password(new Password("$2a$10$password2"))
              .authorityKbn(AuthorityEnum.ADMINISTRATOR)
              .build();

      AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
      Authentication authentication =
          new UsernamePasswordAuthenticationToken(
              accountPrincipal, null, accountPrincipal.getAuthorities());

      mockMvc
          .perform(
              multipart("/api/v1/accounts/" + photoAccountId + "/photos")
                  // 縦長・横長・正方形の3枚を1リクエストに混在させ、それぞれ正しい向きで登録されることを検証する
                  .file(
                      new MockMultipartFile(
                          "imageFiles",
                          "DSC111.jpg",
                          MediaType.IMAGE_JPEG_VALUE,
                          createJpegBytes(100, 200)))
                  .file(
                      new MockMultipartFile(
                          "imageFiles",
                          "DSC222.jpg",
                          MediaType.IMAGE_JPEG_VALUE,
                          createJpegBytes(200, 100)))
                  .file(
                      new MockMultipartFile(
                          "imageFiles",
                          "DSC333.jpg",
                          MediaType.IMAGE_JPEG_VALUE,
                          createJpegBytes(150, 150)))
                  .contentType(MediaType.MULTIPART_FORM_DATA)
                  .param("photoJapaneseTitle", "共通タイトル")
                  .param("photoTagRegistRequestList[0].tagJapaneseName", "太陽")
                  .param("photoTagRegistRequestList[0].tagEnglishName", "sun")
                  .with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
                  .with(csrf()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.registeredCount").value(3));

      // 3枚とも共通のタイトル・タグで登録されるが、向き区分は画像ごとに異なることを確認する
      List<PhotoMst> actualPhotoMstList =
          jdbcTemplate.query(
              "SELECT * FROM photo.photo_mst WHERE account_no = 2 AND photo_no IN (4, 5, 6) ORDER BY photo_no",
              (rs, rowNum) ->
                  PhotoMst.builder()
                      .accountNo(rs.getLong("account_no"))
                      .photoNo(rs.getLong("photo_no"))
                      .photoJapaneseTitle(rs.getString("photo_japanese_title"))
                      .directionKbn(DirectionEnum.getOrDefault(rs.getString("direction_kbn")))
                      .imageFilePath(rs.getString("image_file_path"))
                      .build());
      assertEquals(3, actualPhotoMstList.size());
      for (PhotoMst photoMst : actualPhotoMstList) {
        assertEquals("共通タイトル", photoMst.getPhotoJapaneseTitle());
      }
      assertEquals(DirectionEnum.VERTICAL, actualPhotoMstList.get(0).getDirectionKbn());
      assertEquals(DirectionEnum.HORIZONTAL, actualPhotoMstList.get(1).getDirectionKbn());
      assertEquals(DirectionEnum.SQUARE, actualPhotoMstList.get(2).getDirectionKbn());
      assertOpaqueObjectKey(actualPhotoMstList.get(0).getImageFilePath(), "bbbbbbbb", 4L, "jpg");
      assertOpaqueObjectKey(actualPhotoMstList.get(1).getImageFilePath(), "bbbbbbbb", 5L, "jpg");
      assertOpaqueObjectKey(actualPhotoMstList.get(2).getImageFilePath(), "bbbbbbbb", 6L, "jpg");

      // 3枚ともタグ「太陽」が共通で付与されていることを確認する
      Integer taggedPhotoCount =
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM photo.photo_tag_mst WHERE account_no = 2"
                  + " AND photo_no IN (4, 5, 6) AND tag_japanese_name = '太陽'",
              Integer.class);
      assertEquals(3, taggedPhotoCount);
    }

    @Test
    @Order(3)
    @DisplayName("異常系：アクセス不正。ForbiddenAccountExceptionをthrowする")
    void registPhotos_ForbiddenAccountException() throws Exception {
      String photoAccountId = "bbbbbbbb";
      MockMultipartFile multipartFile =
          new MockMultipartFile("imageFiles", "DSC111.jpg", MediaType.IMAGE_JPEG_VALUE, JPEG_BYTES);

      AccountModel sessionAccount =
          AccountModel.builder()
              .accountNo(new AccountNo(1L))
              .accountId(new AccountId("aaaaaaaa"))
              .accountName(new AccountName("AAAAAAAA"))
              .password(new Password("$2a$10$password1"))
              .authorityKbn(AuthorityEnum.ADMINISTRATOR)
              .build();

      AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
      Authentication authentication =
          new UsernamePasswordAuthenticationToken(
              accountPrincipal, null, accountPrincipal.getAuthorities());

      mockMvc
          .perform(
              multipart("/api/v1/accounts/" + photoAccountId + "/photos")
                  .file(multipartFile)
                  .contentType(MediaType.MULTIPART_FORM_DATA)
                  .with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
                  .with(csrf()))
          .andExpect(status().isForbidden())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.httpStatus").value(HttpStatus.FORBIDDEN.value()))
          .andExpect(
              jsonPath("$.errorCode").value(ErrorEnum.NOT_AUTHORIZED_TO_EDIT_PHOTO.getErrorCode()))
          .andExpect(
              jsonPath("$.errorMessage")
                  .value(ErrorEnum.NOT_AUTHORIZED_TO_EDIT_PHOTO.getErrorMessage()));
    }

    @Test
    @Order(4)
    @DisplayName("異常系：登録上限に達している。PhotoNotAdditableExceptionをthrowする")
    void registPhotos_PhotoNotAdditableException() throws Exception {
      String photoAccountId = "aaaaaaaa";
      MockMultipartFile multipartFile =
          new MockMultipartFile("imageFiles", "DSC111.jpg", MediaType.IMAGE_JPEG_VALUE, JPEG_BYTES);

      AccountModel sessionAccount =
          AccountModel.builder()
              .accountNo(new AccountNo(1L))
              .accountId(new AccountId("aaaaaaaa"))
              .accountName(new AccountName("AAAAAAAA"))
              .password(new Password("$2a$10$password1"))
              .authorityKbn(AuthorityEnum.MINI)
              .build();

      AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
      Authentication authentication =
          new UsernamePasswordAuthenticationToken(
              accountPrincipal, null, accountPrincipal.getAuthorities());

      mockMvc
          .perform(
              multipart("/api/v1/accounts/" + photoAccountId + "/photos")
                  .file(multipartFile)
                  .contentType(MediaType.MULTIPART_FORM_DATA)
                  .param("photoJapaneseTitle", "タイトル")
                  .with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
                  .with(csrf()))
          .andExpect(status().isBadRequest())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.httpStatus").value(HttpStatus.BAD_REQUEST.value()))
          .andExpect(
              jsonPath("$.errorCode").value(ErrorEnum.REACHED_REGISTRATION_LIMIT.getErrorCode()))
          .andExpect(
              jsonPath("$.errorMessage")
                  .value(ErrorEnum.REACHED_REGISTRATION_LIMIT.getErrorMessage()));
    }

    @Test
    @Order(5)
    @DisplayName("異常系：画像ファイルが1枚も指定されていない。BadRequestExceptionをthrowする")
    void registPhotos_BadRequestException_imageFiles_is_empty() throws Exception {
      String photoAccountId = "bbbbbbbb";

      AccountModel sessionAccount =
          AccountModel.builder()
              .accountNo(new AccountNo(2L))
              .accountId(new AccountId("bbbbbbbb"))
              .accountName(new AccountName("BBBBBBBB"))
              .password(new Password("$2a$10$password2"))
              .authorityKbn(AuthorityEnum.MINI)
              .build();

      AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
      Authentication authentication =
          new UsernamePasswordAuthenticationToken(
              accountPrincipal, null, accountPrincipal.getAuthorities());

      mockMvc
          .perform(
              multipart("/api/v1/accounts/" + photoAccountId + "/photos")
                  .contentType(MediaType.MULTIPART_FORM_DATA)
                  .param("photoJapaneseTitle", "タイトル")
                  .with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
                  .with(csrf()))
          .andExpect(status().isBadRequest())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.httpStatus").value(HttpStatus.BAD_REQUEST.value()))
          .andExpect(jsonPath("$.isSuccess").value(false))
          .andExpect(jsonPath("$.message").value(ErrorEnum.INVALID_INPUT.getErrorMessage()));
    }

    @Test
    @Order(6)
    @DisplayName("異常系：パラメータ不正。BadRequestExceptionをthrowする")
    void registPhotos_BadRequestException_others() throws Exception {
      String photoAccountId = "bbbbbbbb";
      MockMultipartFile multipartFile =
          new MockMultipartFile("imageFiles", "DSC111.jpg", MediaType.IMAGE_JPEG_VALUE, JPEG_BYTES);

      AccountModel sessionAccount =
          AccountModel.builder()
              .accountNo(new AccountNo(2L))
              .accountId(new AccountId("bbbbbbbb"))
              .accountName(new AccountName("BBBBBBBB"))
              .password(new Password("$2a$10$password2"))
              .authorityKbn(AuthorityEnum.ADMINISTRATOR)
              .build();

      AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
      Authentication authentication =
          new UsernamePasswordAuthenticationToken(
              accountPrincipal, null, accountPrincipal.getAuthorities());

      mockMvc
          .perform(
              multipart("/api/v1/accounts/" + photoAccountId + "/photos")
                  .file(multipartFile)
                  .contentType(MediaType.MULTIPART_FORM_DATA)
                  .param("photoJapaneseTitle", "タイトル")
                  .param("focalLength", "-1")
                  .with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
                  .with(csrf()))
          .andExpect(status().isBadRequest())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.httpStatus").value(HttpStatus.BAD_REQUEST.value()))
          .andExpect(jsonPath("$.isSuccess").value(false))
          .andExpect(jsonPath("$.message").value(ErrorEnum.INVALID_INPUT.getErrorMessage()));
    }

    @Test
    @Order(7)
    @DisplayName("異常系：FileDuplicateExceptionをthrowする")
    void registPhotos_FileDuplicateException() throws Exception {
      String photoAccountId = "bbbbbbbb";
      MockMultipartFile multipartFile =
          new MockMultipartFile("imageFiles", "DSC21.jpg", MediaType.IMAGE_JPEG_VALUE, JPEG_BYTES);

      AccountModel sessionAccount =
          AccountModel.builder()
              .accountNo(new AccountNo(2L))
              .accountId(new AccountId("bbbbbbbb"))
              .accountName(new AccountName("BBBBBBBB"))
              .password(new Password("$2a$10$password2"))
              .authorityKbn(AuthorityEnum.ADMINISTRATOR)
              .build();

      AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
      Authentication authentication =
          new UsernamePasswordAuthenticationToken(
              accountPrincipal, null, accountPrincipal.getAuthorities());

      mockMvc
          .perform(
              multipart("/api/v1/accounts/" + photoAccountId + "/photos")
                  .file(multipartFile)
                  .contentType(MediaType.MULTIPART_FORM_DATA)
                  .param("caption", "")
                  .param("photoEnglishTitle", "")
                  .param("photoJapaneseTitle", "タイトル")
                  .with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
                  .with(csrf()))
          .andExpect(status().isConflict())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.httpStatus").value(HttpStatus.CONFLICT.value()))
          .andExpect(jsonPath("$.errorCode").value(ErrorEnum.DUPLICATE_PHOTO_FILE.getErrorCode()))
          .andExpect(
              jsonPath("$.errorMessage").value(ErrorEnum.DUPLICATE_PHOTO_FILE.getErrorMessage()));
    }

    @Test
    @Order(8)
    @DisplayName("異常系：ファイルサイズが上限（app.photo.maxFileSizeMb=5MB）を超える場合、BadRequestExceptionをthrowする")
    void registPhotos_BadRequestException_imageFileSizeExceeded() throws Exception {
      String photoAccountId = "bbbbbbbb";
      // サーブレット側の上限（spring.servlet.multipart.max-file-size=6MB）は超えないが、
      // アプリ側の上限（app.photo.maxFileSizeMb=5MB）は超えるサイズのファイルを生成する
      byte[] oversizedBytes = new byte[5 * 1024 * 1024 + 1024];
      System.arraycopy(JPEG_BYTES, 0, oversizedBytes, 0, JPEG_BYTES.length);
      MockMultipartFile multipartFile =
          new MockMultipartFile(
              "imageFiles", "DSC999.jpg", MediaType.IMAGE_JPEG_VALUE, oversizedBytes);

      AccountModel sessionAccount =
          AccountModel.builder()
              .accountNo(new AccountNo(2L))
              .accountId(new AccountId("bbbbbbbb"))
              .accountName(new AccountName("BBBBBBBB"))
              .password(new Password("$2a$10$password2"))
              .authorityKbn(AuthorityEnum.ADMINISTRATOR)
              .build();

      AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
      Authentication authentication =
          new UsernamePasswordAuthenticationToken(
              accountPrincipal, null, accountPrincipal.getAuthorities());

      mockMvc
          .perform(
              multipart("/api/v1/accounts/" + photoAccountId + "/photos")
                  .file(multipartFile)
                  .contentType(MediaType.MULTIPART_FORM_DATA)
                  .param("photoJapaneseTitle", "タイトル")
                  .with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
                  .with(csrf()))
          .andExpect(status().isBadRequest())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.httpStatus").value(HttpStatus.BAD_REQUEST.value()))
          .andExpect(jsonPath("$.isSuccess").value(false))
          .andExpect(
              jsonPath("$.message").value(ErrorEnum.IMAGE_FILE_SIZE_EXCEEDED.getErrorMessage()));
    }
  }

  /**
   * 一意制約違反等の実際のDB制約違反は、写真番号の採番がアカウント単位のロックで直列化されているため
   * 単一トランザクション・単一スレッドの結合テストでは決定的に再現できない。そのため{@link
   * PhotoAggregateRepository}をモック化してRepository層からの{@link GalleryException}送出を再現し、
   * Controller〜ControllerAdviceが実際のDB制約違反時と同じ409レスポンスへ正しく変換することを検証する。 Spring
   * TestのApplicationContextキャッシュにより、このモック化は本ネストクラス内のみで完結し、 他のネストクラス・テストケースには影響しない
   */
  @Nested
  @Order(6)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/controller/PhotoControllerIntegrationTest.sql")
  class repositoryFailure {
    @MockitoBean private PhotoAggregateRepository photoAggregateRepository;

    @Test
    @Order(1)
    @DisplayName("異常系：写真マスタ登録でDB制約違反が発生した場合、RegistFailureExceptionにより409を返す")
    void registPhotos_RegistFailureException() throws Exception {
      String photoAccountId = "bbbbbbbb";
      MockMultipartFile multipartFile =
          new MockMultipartFile("imageFiles", "DSC999.jpg", MediaType.IMAGE_JPEG_VALUE, JPEG_BYTES);

      doThrow(ErrorEnum.FAIL_TO_REGIST_PHOTO.toException())
          .when(photoAggregateRepository)
          .regist(any(Photo.class));

      AccountModel sessionAccount =
          AccountModel.builder()
              .accountNo(new AccountNo(2L))
              .accountId(new AccountId("bbbbbbbb"))
              .accountName(new AccountName("BBBBBBBB"))
              .password(new Password("$2a$10$password2"))
              .authorityKbn(AuthorityEnum.ADMINISTRATOR)
              .build();

      AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
      Authentication authentication =
          new UsernamePasswordAuthenticationToken(
              accountPrincipal, null, accountPrincipal.getAuthorities());

      mockMvc
          .perform(
              multipart("/api/v1/accounts/" + photoAccountId + "/photos")
                  .file(multipartFile)
                  .contentType(MediaType.MULTIPART_FORM_DATA)
                  .param("photoJapaneseTitle", "タイトル")
                  .with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
                  .with(csrf()))
          .andExpect(status().isConflict())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.httpStatus").value(HttpStatus.CONFLICT.value()))
          .andExpect(jsonPath("$.errorCode").value(ErrorEnum.FAIL_TO_REGIST_PHOTO.getErrorCode()))
          .andExpect(
              jsonPath("$.errorMessage").value(ErrorEnum.FAIL_TO_REGIST_PHOTO.getErrorMessage()));
    }

    @Test
    @Order(2)
    @DisplayName("異常系：写真マスタ更新でDB制約違反等により対象行を更新できなかった場合、UpdateFailureExceptionにより409を返す")
    void savePhoto_UpdateFailureException() throws Exception {
      String photoAccountId = "bbbbbbbb";

      doThrow(ErrorEnum.FAIL_TO_UPDATE_PHOTO.toException())
          .when(photoAggregateRepository)
          .update(any(Photo.class));

      AccountModel sessionAccount =
          AccountModel.builder()
              .accountNo(new AccountNo(2L))
              .accountId(new AccountId("bbbbbbbb"))
              .accountName(new AccountName("BBBBBBBB"))
              .password(new Password("$2a$10$password2"))
              .authorityKbn(AuthorityEnum.ADMINISTRATOR)
              .build();

      AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
      Authentication authentication =
          new UsernamePasswordAuthenticationToken(
              accountPrincipal, null, accountPrincipal.getAuthorities());

      mockMvc
          .perform(
              multipart(HttpMethod.PUT, "/api/v1/accounts/" + photoAccountId + "/photos")
                  .contentType(MediaType.MULTIPART_FORM_DATA)
                  .param("photoNo", "1")
                  .param("caption", "caption111")
                  .param("imageFilePath", "https://www.xxx.com/bbbbbbbb/DSC21.jpg")
                  .param("directionKbn", "VERTICAL")
                  .param("photoEnglishTitle", "title111")
                  .param("photoJapaneseTitle", "タイトル111")
                  .param(
                      "photoAt",
                      LocalDateTime.of(2000, 1, 1, 0, 0, 0)
                          .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                  .param("focalLength", "24")
                  .param("fValue", "8.0")
                  .param("shutterSpeed", "0.01")
                  .param("iso", "100")
                  .with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
                  .with(csrf()))
          .andExpect(status().isConflict())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.httpStatus").value(HttpStatus.CONFLICT.value()))
          .andExpect(jsonPath("$.errorCode").value(ErrorEnum.FAIL_TO_UPDATE_PHOTO.getErrorCode()))
          .andExpect(
              jsonPath("$.errorMessage").value(ErrorEnum.FAIL_TO_UPDATE_PHOTO.getErrorMessage()));
    }
  }

  /** 未認証（認証情報なし）で認証必須の写真関連エンドポイントにアクセスした場合の共通挙動を検証する */
  @Nested
  @Order(7)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/controller/PhotoControllerIntegrationTest.sql")
  class unauthenticatedAccess {
    @Test
    @Order(1)
    @DisplayName("異常系：未認証で写真新規登録にアクセスすると403ではなく401で共通JSONエラーを返す")
    void registPhotos_unauthenticated() throws Exception {
      String photoAccountId = "bbbbbbbb";
      MockMultipartFile multipartFile =
          new MockMultipartFile("imageFiles", "DSC111.jpg", MediaType.IMAGE_JPEG_VALUE, JPEG_BYTES);

      mockMvc
          .perform(
              multipart("/api/v1/accounts/" + photoAccountId + "/photos")
                  .file(multipartFile)
                  .contentType(MediaType.MULTIPART_FORM_DATA))
          .andExpect(status().isUnauthorized())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.httpStatus").value(HttpStatus.UNAUTHORIZED.value()))
          .andExpect(jsonPath("$.errorCode").value("E-A-0002"))
          .andExpect(jsonPath("$.errorMessage").isNotEmpty());
    }

    @Test
    @Order(2)
    @DisplayName("異常系：未認証で写真更新にアクセスすると403ではなく401で共通JSONエラーを返す")
    void savePhoto_unauthenticated() throws Exception {
      String photoAccountId = "bbbbbbbb";

      mockMvc
          .perform(
              multipart(HttpMethod.PUT, "/api/v1/accounts/" + photoAccountId + "/photos")
                  .contentType(MediaType.MULTIPART_FORM_DATA)
                  .param("photoNo", "1")
                  .param("imageFilePath", "https://www.xxx.com/bbbbbbbb/DSC21.jpg"))
          .andExpect(status().isUnauthorized())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.httpStatus").value(HttpStatus.UNAUTHORIZED.value()))
          .andExpect(jsonPath("$.errorCode").value("E-A-0002"));
    }

    @Test
    @Order(3)
    @DisplayName("異常系：未認証で写真削除にアクセスすると403ではなく401で共通JSONエラーを返す")
    void deletePhoto_unauthenticated() throws Exception {
      String photoAccountId = "aaaaaaaa";

      mockMvc
          .perform(
              delete("/api/v1/accounts/" + photoAccountId + "/photos")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("delete_photo_success.json")))
          .andExpect(status().isUnauthorized())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.httpStatus").value(HttpStatus.UNAUTHORIZED.value()))
          .andExpect(jsonPath("$.errorCode").value("E-A-0002"));
    }

    @Test
    @Order(4)
    @DisplayName("異常系：未認証で写真登録上限チェックにアクセスすると403ではなく401で共通JSONエラーを返す")
    void getPhotoUpperLimit_unauthenticated() throws Exception {
      mockMvc
          .perform(get("/api/v1/accounts/aaaaaaaa/photos/upper-limit"))
          .andExpect(status().isUnauthorized())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.httpStatus").value(HttpStatus.UNAUTHORIZED.value()))
          .andExpect(jsonPath("$.errorCode").value("E-A-0002"));
    }
  }

  @Nested
  @Order(4)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/controller/PhotoControllerIntegrationTest.sql")
  class deletePhoto {
    @Test
    @Order(1)
    @DisplayName("正常系")
    void deletePhoto_success() throws Exception {
      String photoAccountId = "aaaaaaaa";
      String loginAccountId = "aaaaaaaa";

      AccountModel sessionAccount =
          AccountModel.builder()
              .accountNo(new AccountNo(1L))
              .accountId(new AccountId(loginAccountId))
              .accountName(new AccountName("AAAAAAAA"))
              .password(new Password("$2a$10$password1"))
              .authorityKbn(AuthorityEnum.ADMINISTRATOR)
              .build();

      AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
      Authentication authentication =
          new UsernamePasswordAuthenticationToken(
              accountPrincipal, null, accountPrincipal.getAuthorities());

      OffsetDateTime transactionNow =
          jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);
      mockMvc
          .perform(
              delete("/api/v1/accounts/" + photoAccountId + "/photos")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("delete_photo_success.json"))
                  .with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
                  .with(csrf()))
          .andExpect(status().isOk())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.httpStatus").value(200))
          .andExpect(jsonPath("$.isSuccess").value(true))
          .andExpect(jsonPath("$.message").value("写真削除が完了しました。"));

      // photo_mst削除チェック
      List<PhotoMst> actualPhotoMst =
          jdbcTemplate.query(
              "SELECT * FROM photo.photo_mst where account_no = 1 and photo_no=1",
              (rs, rowNum) ->
                  PhotoMst.builder()
                      .accountNo(rs.getLong("account_no"))
                      .photoNo(rs.getLong("photo_no"))
                      .createdBy(rs.getLong("created_by"))
                      .createdAt(rs.getObject("created_at", OffsetDateTime.class))
                      .updatedBy(rs.getLong("updated_by"))
                      .updatedAt(rs.getObject("updated_at", OffsetDateTime.class))
                      .isDeleted(rs.getBoolean("is_deleted"))
                      .photoAt(rs.getObject("photo_at", OffsetDateTime.class))
                      .locationNo(rs.getLong("location_no"))
                      .imageFilePath(rs.getString("image_file_path"))
                      .photoJapaneseTitle(rs.getString("photo_japanese_title"))
                      .photoEnglishTitle(rs.getString("photo_english_title"))
                      .caption(rs.getString("caption"))
                      .directionKbn(DirectionEnum.getOrDefault(rs.getString("direction_kbn")))
                      .focalLength(rs.getInt("focal_length"))
                      .fValue(rs.getBigDecimal("f_value"))
                      .shutterSpeed(rs.getBigDecimal("shutter_speed"))
                      .iso(rs.getInt("iso"))
                      .build());
      assertEquals(1L, actualPhotoMst.getFirst().getCreatedBy());
      assertEquals(
          OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)),
          actualPhotoMst.getFirst().getCreatedAt());
      assertEquals(1L, actualPhotoMst.getFirst().getUpdatedBy());
      assertEquals(transactionNow, actualPhotoMst.getFirst().getUpdatedAt());
      assertTrue(actualPhotoMst.getFirst().getIsDeleted());

      // photo_tag_mst削除チェック
      List<PhotoTagMst> actualPhotoTagMst =
          jdbcTemplate.query(
              "SELECT * FROM photo.photo_tag_mst WHERE account_no=1 and photo_no=1",
              (rs, rowNum) ->
                  PhotoTagMst.builder()
                      .accountNo(rs.getLong("account_no"))
                      .photoNo(rs.getLong("photo_no"))
                      .tagNo(rs.getLong("tag_no"))
                      .createdBy(rs.getLong("created_by"))
                      .createdAt(rs.getObject("created_at", OffsetDateTime.class))
                      .tagJapaneseName(rs.getObject("tag_japanese_name").toString())
                      .tagEnglishName(rs.getObject("tag_english_name").toString())
                      .build());
      assertEquals(0, actualPhotoTagMst.size());

      // photo_favorite削除チェック
      List<PhotoFavorite> actualPhotoFavoriteData =
          jdbcTemplate.query(
              "SELECT * FROM photo.photo_favorite WHERE favorite_photo_account_no=1 and favorite_photo_no=1",
              (rs, rowNum) ->
                  PhotoFavorite.builder()
                      .accountNo(rs.getLong("account_no"))
                      .favoritePhotoAccountNo(rs.getLong("favorite_photo_account_no"))
                      .favoritePhotoNo(rs.getLong("favorite_photo_no"))
                      .createdBy(rs.getLong("created_by"))
                      .createdAt(rs.getObject("created_at", OffsetDateTime.class))
                      .build());
      assertEquals(0, actualPhotoFavoriteData.size());
    }

    @Test
    @Order(2)
    @DisplayName("異常系：不正アクセス。ForbiddenAccountExceptionをthrowする")
    void deletePhoto_ForbiddenAccountException() throws Exception {
      String photoAccountId = "aaaaaaaa";
      String loginAccountId = "eeeeeeee";

      AccountModel sessionAccount =
          AccountModel.builder()
              .accountNo(new AccountNo(1L))
              .accountId(new AccountId(loginAccountId))
              .accountName(new AccountName("EEEEEEEE"))
              .password(new Password("$2a$10$password5"))
              .authorityKbn(AuthorityEnum.ADMINISTRATOR)
              .build();

      AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
      Authentication authentication =
          new UsernamePasswordAuthenticationToken(
              accountPrincipal, null, accountPrincipal.getAuthorities());

      mockMvc
          .perform(
              delete("/api/v1/accounts/" + photoAccountId + "/photos")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("delete_photo_forbidden.json"))
                  .with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
                  .with(csrf()))
          .andExpect(status().isForbidden())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.httpStatus").value(HttpStatus.FORBIDDEN.value()))
          .andExpect(
              jsonPath("$.errorCode").value(ErrorEnum.NOT_AUTHORIZED_TO_EDIT_PHOTO.getErrorCode()))
          .andExpect(
              jsonPath("$.errorMessage")
                  .value(ErrorEnum.NOT_AUTHORIZED_TO_EDIT_PHOTO.getErrorMessage()));
    }

    @Test
    @Order(3)
    @DisplayName("異常系：パラメータ不正。BadRequestExceptionをthrowする")
    void deletePhoto_BadRequestException() throws Exception {
      String photoAccountId = "aaaaaaaa";
      String loginAccountId = "aaaaaaaa";

      AccountModel sessionAccount =
          AccountModel.builder()
              .accountNo(new AccountNo(1L))
              .accountId(new AccountId(loginAccountId))
              .accountName(new AccountName("AAAAAAAA"))
              .password(new Password("$2a$10$password1"))
              .authorityKbn(AuthorityEnum.ADMINISTRATOR)
              .build();

      AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
      Authentication authentication =
          new UsernamePasswordAuthenticationToken(
              accountPrincipal, null, accountPrincipal.getAuthorities());

      mockMvc
          .perform(
              delete("/api/v1/accounts/" + photoAccountId + "/photos")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("delete_photo_badrequest.json"))
                  .with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
                  .with(csrf()))
          .andExpect(status().isBadRequest())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.httpStatus").value(HttpStatus.BAD_REQUEST.value()))
          .andExpect(jsonPath("$.isSuccess").value(false))
          .andExpect(jsonPath("$.message").value(ErrorEnum.INVALID_INPUT.getErrorMessage()));
    }

    @Test
    @Order(4)
    @DisplayName("異常系：対象写真が存在しない場合、404を返す")
    void deletePhoto_PhotoNotFoundException() throws Exception {
      String photoAccountId = "aaaaaaaa";
      String loginAccountId = "aaaaaaaa";

      AccountModel sessionAccount =
          AccountModel.builder()
              .accountNo(new AccountNo(1L))
              .accountId(new AccountId(loginAccountId))
              .accountName(new AccountName("AAAAAAAA"))
              .password(new Password("$2a$10$password1"))
              .authorityKbn(AuthorityEnum.ADMINISTRATOR)
              .build();

      AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
      Authentication authentication =
          new UsernamePasswordAuthenticationToken(
              accountPrincipal, null, accountPrincipal.getAuthorities());

      mockMvc
          .perform(
              delete("/api/v1/accounts/" + photoAccountId + "/photos")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("delete_photo_update_failure.json"))
                  .with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
                  .with(csrf()))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.httpStatus").value(HttpStatus.NOT_FOUND.value()))
          .andExpect(jsonPath("$.errorCode").value(ErrorEnum.PHOTO_NOT_FOUND.getErrorCode()))
          .andExpect(jsonPath("$.errorMessage").value(ErrorEnum.PHOTO_NOT_FOUND.getErrorMessage()));
    }
  }

  @Nested
  @Order(5)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/controller/PhotoControllerIntegrationTest.sql")
  class getPhotoUpperLimit {
    @Test
    @Order(1)
    @DisplayName("正常系：自分のアカウントで上限未到達の場合")
    void getPhotoUpperLimit_not_reached() throws Exception {
      String photoAccountId = "bbbbbbbb";

      AccountModel sessionAccount =
          AccountModel.builder()
              .accountNo(new AccountNo(2L))
              .accountId(new AccountId(photoAccountId))
              .accountName(new AccountName("BBBBBBBB"))
              .password(new Password("$2a$10$password2"))
              .authorityKbn(AuthorityEnum.MINI)
              .build();

      AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
      Authentication authentication =
          new UsernamePasswordAuthenticationToken(
              accountPrincipal, null, accountPrincipal.getAuthorities());

      mockMvc
          .perform(
              get("/api/v1/accounts/" + photoAccountId + "/photos/upper-limit")
                  .with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
                  .with(csrf()))
          .andExpect(status().isOk())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.isReachedUpperLimit").value(false));
    }

    @Test
    @Order(2)
    @DisplayName("正常系：自分のアカウントで上限到達の場合")
    void getPhotoUpperLimit_reached() throws Exception {
      String photoAccountId = "aaaaaaaa";

      AccountModel sessionAccount =
          AccountModel.builder()
              .accountNo(new AccountNo(1L))
              .accountId(new AccountId(photoAccountId))
              .accountName(new AccountName("AAAAAAAA"))
              .password(new Password("$2a$10$password1"))
              .authorityKbn(AuthorityEnum.MINI)
              .build();

      AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
      Authentication authentication =
          new UsernamePasswordAuthenticationToken(
              accountPrincipal, null, accountPrincipal.getAuthorities());

      mockMvc
          .perform(
              get("/api/v1/accounts/" + photoAccountId + "/photos/upper-limit")
                  .with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
                  .with(csrf()))
          .andExpect(status().isOk())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.isReachedUpperLimit").value(true));
    }

    @Test
    @Order(3)
    @DisplayName("正常系：他人のアカウントの場合はfalse")
    void getPhotoUpperLimit_other_account() throws Exception {
      AccountModel sessionAccount =
          AccountModel.builder()
              .accountNo(new AccountNo(2L))
              .accountId(new AccountId("bbbbbbbb"))
              .accountName(new AccountName("BBBBBBBB"))
              .password(new Password("$2a$10$password2"))
              .authorityKbn(AuthorityEnum.MINI)
              .build();

      AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
      Authentication authentication =
          new UsernamePasswordAuthenticationToken(
              accountPrincipal, null, accountPrincipal.getAuthorities());

      mockMvc
          .perform(
              get("/api/v1/accounts/aaaaaaaa/photos/upper-limit")
                  .with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
                  .with(csrf()))
          .andExpect(status().isOk())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.isReachedUpperLimit").value(false));
    }

    @Test
    @Order(4)
    @DisplayName("正常系：normal-userの残り登録可能枚数（app.photo.normalUserUpperLimit=20）が正しく算出される")
    void getPhotoUpperLimit_normalUser_remainingCount() throws Exception {
      // cccccccc（account_no=3, normal-user）は写真登録0枚
      String photoAccountId = "cccccccc";

      AccountModel sessionAccount =
          AccountModel.builder()
              .accountNo(new AccountNo(3L))
              .accountId(new AccountId(photoAccountId))
              .accountName(new AccountName("CCCCCCCC"))
              .password(new Password("$2a$10$password3"))
              .authorityKbn(AuthorityEnum.NORMAL)
              .build();

      AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
      Authentication authentication =
          new UsernamePasswordAuthenticationToken(
              accountPrincipal, null, accountPrincipal.getAuthorities());

      mockMvc
          .perform(
              get("/api/v1/accounts/" + photoAccountId + "/photos/upper-limit")
                  .with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
                  .with(csrf()))
          .andExpect(status().isOk())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.isReachedUpperLimit").value(false))
          .andExpect(jsonPath("$.remainingCount").value(20));
    }

    @Test
    @Order(5)
    @DisplayName("正常系：special-userは登録上限が存在せず、残り登録可能枚数はnull")
    void getPhotoUpperLimit_specialUser_noLimit() throws Exception {
      // eeeeeeee（account_no=5, special-user）
      String photoAccountId = "eeeeeeee";

      AccountModel sessionAccount =
          AccountModel.builder()
              .accountNo(new AccountNo(5L))
              .accountId(new AccountId(photoAccountId))
              .accountName(new AccountName("EEEEEEEE"))
              .password(new Password("$2a$10$password5"))
              .authorityKbn(AuthorityEnum.SPECIAL)
              .build();

      AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
      Authentication authentication =
          new UsernamePasswordAuthenticationToken(
              accountPrincipal, null, accountPrincipal.getAuthorities());

      mockMvc
          .perform(
              get("/api/v1/accounts/" + photoAccountId + "/photos/upper-limit")
                  .with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
                  .with(csrf()))
          .andExpect(status().isOk())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.isReachedUpperLimit").value(false))
          .andExpect(jsonPath("$.remainingCount").isEmpty());
    }

    @Test
    @Order(6)
    @DisplayName("正常系：administratorは登録上限が存在せず、残り登録可能枚数はnull")
    void getPhotoUpperLimit_administrator_noLimit() throws Exception {
      // ffffffff（account_no=6, administrator）
      String photoAccountId = "ffffffff";

      AccountModel sessionAccount =
          AccountModel.builder()
              .accountNo(new AccountNo(6L))
              .accountId(new AccountId(photoAccountId))
              .accountName(new AccountName("FFFFFFFF"))
              .password(new Password("$2a$10$password6"))
              .authorityKbn(AuthorityEnum.ADMINISTRATOR)
              .build();

      AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
      Authentication authentication =
          new UsernamePasswordAuthenticationToken(
              accountPrincipal, null, accountPrincipal.getAuthorities());

      mockMvc
          .perform(
              get("/api/v1/accounts/" + photoAccountId + "/photos/upper-limit")
                  .with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
                  .with(csrf()))
          .andExpect(status().isOk())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.isReachedUpperLimit").value(false))
          .andExpect(jsonPath("$.remainingCount").isEmpty());
    }
  }

  @Nested
  @Order(8)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/controller/PhotoControllerIntegrationTest.sql")
  class getPhotoDetail {
    @Test
    @Order(1)
    @DisplayName("正常系：EXIF・タグ・位置情報を含めて写真詳細を取得できること")
    void getPhotoDetail_success() throws Exception {
      mockMvc
          .perform(get("/api/v1/accounts/aaaaaaaa/photos/1"))
          .andExpect(status().isOk())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.accountNo").value(1))
          .andExpect(jsonPath("$.photoNo").value(1))
          .andExpect(jsonPath("$.imageFilePath").value("https://www.xxx.com/aaaaaaaa/DSC11.jpg"))
          .andExpect(jsonPath("$.photoJapaneseTitle").value("タイトル11"))
          .andExpect(jsonPath("$.caption").value("caption11"))
          .andExpect(jsonPath("$.locationNo").value(1))
          .andExpect(jsonPath("$.locationName").value("ロケーション1"))
          .andExpect(jsonPath("$.focalLength").value(24))
          .andExpect(jsonPath("$.photoTagList.length()").value(2))
          .andExpect(jsonPath("$.photoTagList[0].tagJapaneseName").value("太陽"))
          .andExpect(jsonPath("$.photoTagList[1].tagJapaneseName").value("青空"));
    }

    @Test
    @Order(2)
    @DisplayName("異常系：refererが最大長（2048文字）を超える場合、400を返す")
    void getPhotoDetail_badRequest_referer_too_long() throws Exception {
      String tooLongReferer = "a".repeat(2049);

      mockMvc
          .perform(get("/api/v1/accounts/aaaaaaaa/photos/1").param("referer", tooLongReferer))
          .andExpect(status().isBadRequest());
    }

    @Test
    @Order(3)
    @DisplayName("異常系：写真が存在しない場合、404を返す")
    void getPhotoDetail_notFound() throws Exception {
      mockMvc.perform(get("/api/v1/accounts/aaaaaaaa/photos/999")).andExpect(status().isNotFound());
    }

    /**
     * IDOR対策の検証：位置情報が非公開（{@code is_location_public=false}）の写真は、閲覧者が
     * 所有者本人でない限り撮影場所（ロケーション番号・住所・緯度経度・ロケーション名）を返さないこと（{@code PhotoServiceImpl#isLocationHiddenFor}）
     */
    @Test
    @Order(4)
    @DisplayName("セキュリティ：位置情報が非公開の写真は、所有者以外（未認証を含む）がアクセスした場合、位置情報が秘匿されること")
    void getPhotoDetail_locationHidden_forNonOwner() throws Exception {
      mockMvc
          .perform(get("/api/v1/accounts/aaaaaaaa/photos/11"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.locationNo").isEmpty())
          .andExpect(jsonPath("$.locationName").isEmpty())
          .andExpect(jsonPath("$.address").isEmpty())
          .andExpect(jsonPath("$.latitude").isEmpty())
          .andExpect(jsonPath("$.longitude").isEmpty());
    }

    @Test
    @Order(5)
    @DisplayName("セキュリティ：位置情報が非公開の写真でも、所有者本人がアクセスした場合は位置情報が見えること")
    void getPhotoDetail_locationVisible_forOwner() throws Exception {
      AccountModel sessionAccount =
          AccountModel.builder()
              .accountNo(new AccountNo(1L))
              .accountId(new AccountId("aaaaaaaa"))
              .accountName(new AccountName("AAAAAAAA"))
              .password(new Password("$2a$10$password1"))
              .authorityKbn(AuthorityEnum.MINI)
              .build();
      AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
      Authentication authentication =
          new UsernamePasswordAuthenticationToken(
              accountPrincipal, null, accountPrincipal.getAuthorities());

      mockMvc
          .perform(
              get("/api/v1/accounts/aaaaaaaa/photos/11")
                  .with(SecurityMockMvcRequestPostProcessors.authentication(authentication)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.locationNo").value(1))
          .andExpect(jsonPath("$.locationName").value("ロケーション1"));
    }
  }
}
