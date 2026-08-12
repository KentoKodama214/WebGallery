package com.web.gallery.controller.integration;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.web.gallery.AccountPrincipal;
import com.web.gallery.domain.account.AccountId;
import com.web.gallery.domain.account.AccountName;
import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.account.Password;
import com.web.gallery.domain.common.CreatedBy;
import com.web.gallery.domain.common.CreatedAt;
import com.web.gallery.domain.common.IsDeleted;
import com.web.gallery.domain.common.UpdatedBy;
import com.web.gallery.domain.common.UpdatedAt;
import com.web.gallery.domain.photo.PhotoNo;
import com.web.gallery.domain.photo.PhotoAt;
import com.web.gallery.domain.photo.LocationNo;
import com.web.gallery.domain.photo.ImageFilePath;
import com.web.gallery.domain.photo.PhotoJapaneseTitle;
import com.web.gallery.domain.photo.PhotoEnglishTitle;
import com.web.gallery.domain.photo.Caption;
import com.web.gallery.domain.photo.FocalLength;
import com.web.gallery.domain.photo.FValue;
import com.web.gallery.domain.photo.ShutterSpeed;
import com.web.gallery.domain.photo.Iso;
import com.web.gallery.domain.photo.TagNo;
import com.web.gallery.domain.photo.TagJapaneseName;
import com.web.gallery.domain.photo.TagEnglishName;
import com.web.gallery.entity.PhotoFavorite;
import com.web.gallery.model.AccountModel;
import com.web.gallery.entity.PhotoMst;
import com.web.gallery.entity.PhotoTagMst;
import com.web.gallery.enumuration.AuthorityEnum;
import com.web.gallery.enumuration.DirectionEnum;
import com.web.gallery.enumuration.ErrorEnum;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
@AutoConfigureMockMvc
public class PhotoRestControllerIntegrationTest {
	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private JdbcTemplate jdbcTemplate;

	private String readJsonFile(String fileName) throws Exception {
		return new String(
				new ClassPathResource("json/controller/integration/PhotoRestControllerIntegrationTest/" + fileName).getInputStream().readAllBytes(),
				StandardCharsets.UTF_8);
	}

	@Nested
	@Order(1)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	@Sql("/sql/common/cleanup.sql")
	@Sql("/sql/controller/PhotoRestControllerIntegrationTest.sql")
	class getPhotoList {
		@Test
		@Order(1)
		@DisplayName("正常系：Nullのパラメータがある場合")
		void getPhotoList_with_null_parameter() throws JacksonException, Exception {
			String photoAccountId = "aaaaaaaa";

			MvcResult result = mockMvc.perform(
					get("/api/v1/accounts/" + photoAccountId + "/photos")
				)
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
			assertEquals("https://www.xxx.com/aaaaaaaa/DSC19.jpg", photoList.get(0).get("imageFilePath").asText());
			assertEquals("caption19", photoList.get(0).get("caption").asText());
			assertEquals(DirectionEnum.HORIZONTAL, DirectionEnum.getOrDefault(photoList.get(0).get("directionKbn").asText()));
			
			assertEquals(1, photoList.get(1).get("accountNo").asInt());
			assertEquals(8, photoList.get(1).get("photoNo").asInt());
			assertFalse(photoList.get(1).get("isFavorite").asBoolean());
			assertEquals("https://www.xxx.com/aaaaaaaa/DSC18.jpg", photoList.get(1).get("imageFilePath").asText());
			assertEquals("caption18", photoList.get(1).get("caption").asText());
			assertEquals(DirectionEnum.VERTICAL, DirectionEnum.getOrDefault(photoList.get(1).get("directionKbn").asText()));
			
			assertEquals(1, photoList.get(2).get("accountNo").asInt());
			assertEquals(7, photoList.get(2).get("photoNo").asInt());
			assertFalse(photoList.get(2).get("isFavorite").asBoolean());
			assertEquals("https://www.xxx.com/aaaaaaaa/DSC17.jpg", photoList.get(2).get("imageFilePath").asText());
			assertEquals("caption17", photoList.get(2).get("caption").asText());
			assertEquals(DirectionEnum.VERTICAL, DirectionEnum.getOrDefault(photoList.get(2).get("directionKbn").asText()));
			
			assertEquals(1, photoList.get(3).get("accountNo").asInt());
			assertEquals(6, photoList.get(3).get("photoNo").asInt());
			assertFalse(photoList.get(3).get("isFavorite").asBoolean());
			assertEquals("https://www.xxx.com/aaaaaaaa/DSC16.jpg", photoList.get(3).get("imageFilePath").asText());
			assertEquals("caption16", photoList.get(3).get("caption").asText());
			assertEquals(DirectionEnum.HORIZONTAL, DirectionEnum.getOrDefault(photoList.get(3).get("directionKbn").asText()));
			
			assertEquals(1, photoList.get(4).get("accountNo").asInt());
			assertEquals(5, photoList.get(4).get("photoNo").asInt());
			assertFalse(photoList.get(4).get("isFavorite").asBoolean());
			assertEquals("https://www.xxx.com/aaaaaaaa/DSC15.jpg", photoList.get(4).get("imageFilePath").asText());
			assertEquals("caption15", photoList.get(4).get("caption").asText());
			assertEquals(DirectionEnum.VERTICAL, DirectionEnum.getOrDefault(photoList.get(4).get("directionKbn").asText()));
		}
		
		@Test
		@Order(2)
		@DisplayName("正常系：タグに半角スペースが含まれている場合")
		void getPhotoList_with_halfspace_tag() throws JacksonException, Exception {
			String photoAccountId = "aaaaaaaa";

			MvcResult result = mockMvc.perform(
					get("/api/v1/accounts/" + photoAccountId + "/photos")
					.param("tagList", "太陽 青空")
				)
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
			assertEquals("https://www.xxx.com/aaaaaaaa/DSC11.jpg", photoList.get(0).get("imageFilePath").asText());
			assertEquals("caption11", photoList.get(0).get("caption").asText());
			assertEquals(DirectionEnum.HORIZONTAL, DirectionEnum.getOrDefault(photoList.get(0).get("directionKbn").asText()));
		}
		
		@Test
		@Order(3)
		@DisplayName("正常系：タグに全角スペースが含まれている場合")
		void getPhotoList_with_fullspace_tag() throws JacksonException, Exception {
			String photoAccountId = "aaaaaaaa";

			MvcResult result = mockMvc.perform(
					get("/api/v1/accounts/" + photoAccountId + "/photos")
					.param("tagList", "太陽　青空")
				)
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
			assertEquals("https://www.xxx.com/aaaaaaaa/DSC11.jpg", photoList.get(0).get("imageFilePath").asText());
			assertEquals("caption11", photoList.get(0).get("caption").asText());
			assertEquals(DirectionEnum.HORIZONTAL, DirectionEnum.getOrDefault(photoList.get(0).get("directionKbn").asText()));
		}
		
		@Test
		@Order(4)
		@DisplayName("正常系：写真が0件の場合")
		void getPhotoList_not_found_photo() throws JacksonException, Exception {
			String photoAccountId = "aaaaaaaa";

			MvcResult result = mockMvc.perform(
					get("/api/v1/accounts/" + photoAccountId + "/photos")
					.param("tagList", "太陽　海")
				)
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.isLast").value(true))
				.andReturn();
			
			String jsonResponse = result.getResponse().getContentAsString();
			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode photoList = objectMapper.readTree(jsonResponse).get("photoList");
			assertEquals(0, photoList.size());
		}
	}
	
	@Nested
	@Order(2)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	@Sql("/sql/common/cleanup.sql")
	@Sql("/sql/controller/PhotoRestControllerIntegrationTest.sql")
	class savePhoto {
		@Test
		@Order(1)
		@DisplayName("正常系：新規登録。写真タグなし、撮影日時なし。Nullパラメータあり")
		void savePhoto_addPhoto_not_photoTag_and_photoAt() throws JacksonException, Exception {
			String photoAccountId = "bbbbbbbb";
			MockMultipartFile multipartFile = new MockMultipartFile(
					"imageFile",
					"DSC111.jpg",
					MediaType.MULTIPART_FORM_DATA_VALUE,
					"sample image".getBytes());
			
			AccountModel sessionAccount = AccountModel.builder()
					.accountNo(new AccountNo(2L))
					.accountId(new AccountId("bbbbbbbb"))
					.accountName(new AccountName("BBBBBBBB"))
					.password(new Password("$2a$10$password2"))
					.authorityKbn(AuthorityEnum.ADMINISTRATOR)
					.build();
			
			AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
			Authentication authentication = new UsernamePasswordAuthenticationToken(accountPrincipal, null, accountPrincipal.getAuthorities());
			
			mockMvc.perform(
					multipart("/api/v1/accounts/" + photoAccountId + "/photos")
					.file(multipartFile)
					.contentType(MediaType.MULTIPART_FORM_DATA)
					.param("accountNo", "2")
					.param("caption", "")
					.param("imageFilePath", "")
					.param("directionKbn", "VERTICAL")
					.param("photoEnglishTitle", "")
					.param("photoJapaneseTitle", "")
					.with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
					.with(csrf())
				)
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.httpStatus").value(HttpStatus.OK.value()))
				.andExpect(jsonPath("$.isSuccess").value(true))
				.andExpect(jsonPath("$.message").value("写真登録が完了しました。"));
			
			// photo_mst登録チェック
			List<PhotoMst> actualPhotoMst = jdbcTemplate.query(
					"SELECT * FROM photo.photo_mst where account_no = 2 and photo_no=4", (rs, rowNum) ->
					PhotoMst.builder()
						.accountNo(new AccountNo(rs.getLong("account_no")))
						.photoNo(new PhotoNo(rs.getLong("photo_no")))
						.createdBy(new CreatedBy(rs.getLong("created_by")))
						.createdAt(new CreatedAt(rs.getObject("created_at", OffsetDateTime.class)))
						.updatedBy(new UpdatedBy(rs.getLong("updated_by")))
						.updatedAt(new UpdatedAt(rs.getObject("updated_at", OffsetDateTime.class)))
						.isDeleted(new IsDeleted(rs.getBoolean("is_deleted")))
						.photoAt(new PhotoAt(rs.getObject("photo_at", OffsetDateTime.class)))
						.locationNo(new LocationNo(rs.getLong("location_no")))
						.imageFilePath(new ImageFilePath(rs.getString("image_file_path")))
						.photoJapaneseTitle(new PhotoJapaneseTitle(rs.getString("photo_japanese_title")))
						.photoEnglishTitle(new PhotoEnglishTitle(rs.getString("photo_english_title")))
						.caption(new Caption(rs.getString("caption")))
						.directionKbn(DirectionEnum.getOrDefault(rs.getString("direction_kbn")))
						.focalLength(new FocalLength(rs.getInt("focal_length")))
						.fValue(new FValue(rs.getBigDecimal("f_value")))
						.shutterSpeed(new ShutterSpeed(rs.getBigDecimal("shutter_speed")))
						.iso(new Iso(rs.getInt("iso")))
						.build());
			
			assertEquals(1, actualPhotoMst.size());
			assertEquals(new AccountNo(2L), actualPhotoMst.getFirst().getAccountNo());
			assertEquals(4L, actualPhotoMst.getFirst().getPhotoNo().value());
			assertFalse(actualPhotoMst.getFirst().getIsDeleted().value());
			assertEquals(OffsetDateTime.of(1900, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualPhotoMst.getFirst().getPhotoAt().value().plusHours(9));
			assertEquals(0L, actualPhotoMst.getFirst().getLocationNo().value());
			assertEquals("https://www.xxx.com/bbbbbbbb/DSC111.jpg", actualPhotoMst.getFirst().getImageFilePath().value());
			assertEquals("", actualPhotoMst.getFirst().getPhotoJapaneseTitle().value());
			assertEquals("", actualPhotoMst.getFirst().getPhotoEnglishTitle().value());
			assertEquals("", actualPhotoMst.getFirst().getCaption().value());
			assertEquals(DirectionEnum.VERTICAL, actualPhotoMst.getFirst().getDirectionKbn());
			assertEquals(0, actualPhotoMst.getFirst().getFocalLength().value());
			assertEquals(0, BigDecimal.ZERO.compareTo(actualPhotoMst.getFirst().getFValue().value()));
			assertEquals(0, BigDecimal.ZERO.compareTo(actualPhotoMst.getFirst().getShutterSpeed().value()));
			assertEquals(0, actualPhotoMst.getFirst().getIso().value());
			
			// photo_tag_mst登録チェック
			List<PhotoTagMst> actualPhotoTagMst = jdbcTemplate.query(
					"SELECT * FROM photo.photo_tag_mst WHERE account_no=2 and photo_no=4", (rs, rowNum) ->
							PhotoTagMst.builder()
								.accountNo(new AccountNo(rs.getLong("account_no")))
								.photoNo(new PhotoNo(rs.getLong("photo_no")))
								.tagNo(new TagNo(rs.getLong("tag_no")))
								.createdBy(new CreatedBy(rs.getLong("created_by")))
								.createdAt(new CreatedAt(rs.getObject("created_at", OffsetDateTime.class)))
								.tagJapaneseName(new TagJapaneseName(rs.getObject("tag_japanese_name").toString()))
								.tagEnglishName(new TagEnglishName(rs.getObject("tag_english_name").toString()))
								.build());
			assertEquals(0, actualPhotoTagMst.size());
		}
		
		@Test
		@Order(2)
		@DisplayName("正常系：新規登録。写真タグあり、撮影日時あり。Nullパラメータなし")
		void savePhoto_addPhoto_with_photoTag_and_photoAt() throws Exception {
			String photoAccountId = "bbbbbbbb";
			MockMultipartFile multipartFile = new MockMultipartFile(
					"imageFile",
					"DSC111.jpg",
					MediaType.MULTIPART_FORM_DATA_VALUE,
					"sample image".getBytes());
			
			AccountModel sessionAccount = AccountModel.builder()
					.accountNo(new AccountNo(2L))
					.accountId(new AccountId("bbbbbbbb"))
					.accountName(new AccountName("BBBBBBBB"))
					.password(new Password("$2a$10$password2"))
					.authorityKbn(AuthorityEnum.ADMINISTRATOR)
					.build();
			
			AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
			Authentication authentication = new UsernamePasswordAuthenticationToken(accountPrincipal, null, accountPrincipal.getAuthorities());
			
			mockMvc.perform(
					multipart("/api/v1/accounts/" + photoAccountId + "/photos")
					.file(multipartFile)
					.contentType(MediaType.MULTIPART_FORM_DATA)
					.param("accountNo", "2")
					.param("caption", "caption111")
					.param("imageFilePath", "")
					.param("directionKbn", "VERTICAL")
					.param("photoEnglishTitle", "title111")
					.param("photoJapaneseTitle", "タイトル111")
					.param("photoAt", LocalDateTime.of(2000, 1, 1, 0, 0, 0).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
					.param("focalLength", "24")
					.param("fValue", "8.0")
					.param("shutterSpeed", "0.01")
					.param("iso", "100")
					.param("photoTagRegistRequestList[0].accountNo", "2")
					.param("photoTagRegistRequestList[0].tagJapaneseName", "太陽")
					.param("photoTagRegistRequestList[0].tagEnglishName", "sun")
					.param("photoTagRegistRequestList[1].accountNo", "2")
					.param("photoTagRegistRequestList[1].tagJapaneseName", "青空")
					.param("photoTagRegistRequestList[1].tagEnglishName", "bluesky")
					.with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
					.with(csrf())
				)
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.httpStatus").value(HttpStatus.OK.value()))
				.andExpect(jsonPath("$.isSuccess").value(true))
				.andExpect(jsonPath("$.message").value("写真登録が完了しました。"));
			
			// photo_mst登録チェック
			List<PhotoMst> actualPhotoMst = jdbcTemplate.query(
					"SELECT * FROM photo.photo_mst where account_no = 2 and photo_no=4", (rs, rowNum) ->
					PhotoMst.builder()
						.accountNo(new AccountNo(rs.getLong("account_no")))
						.photoNo(new PhotoNo(rs.getLong("photo_no")))
						.createdBy(new CreatedBy(rs.getLong("created_by")))
						.createdAt(new CreatedAt(rs.getObject("created_at", OffsetDateTime.class)))
						.updatedBy(new UpdatedBy(rs.getLong("updated_by")))
						.updatedAt(new UpdatedAt(rs.getObject("updated_at", OffsetDateTime.class)))
						.isDeleted(new IsDeleted(rs.getBoolean("is_deleted")))
						.photoAt(new PhotoAt(rs.getObject("photo_at", OffsetDateTime.class)))
						.locationNo(new LocationNo(rs.getLong("location_no")))
						.imageFilePath(new ImageFilePath(rs.getString("image_file_path")))
						.photoJapaneseTitle(new PhotoJapaneseTitle(rs.getString("photo_japanese_title")))
						.photoEnglishTitle(new PhotoEnglishTitle(rs.getString("photo_english_title")))
						.caption(new Caption(rs.getString("caption")))
						.directionKbn(DirectionEnum.getOrDefault(rs.getString("direction_kbn")))
						.focalLength(new FocalLength(rs.getInt("focal_length")))
						.fValue(new FValue(rs.getBigDecimal("f_value")))
						.shutterSpeed(new ShutterSpeed(rs.getBigDecimal("shutter_speed")))
						.iso(new Iso(rs.getInt("iso")))
						.build());
			
			assertEquals(1, actualPhotoMst.size());
			assertEquals(new AccountNo(2L), actualPhotoMst.getFirst().getAccountNo());
			assertEquals(4L, actualPhotoMst.getFirst().getPhotoNo().value());
			assertFalse(actualPhotoMst.getFirst().getIsDeleted().value());
			assertEquals(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualPhotoMst.getFirst().getPhotoAt().value().plusHours(9));
			assertEquals(0L, actualPhotoMst.getFirst().getLocationNo().value());
			assertEquals("https://www.xxx.com/bbbbbbbb/DSC111.jpg", actualPhotoMst.getFirst().getImageFilePath().value());
			assertEquals("タイトル111", actualPhotoMst.getFirst().getPhotoJapaneseTitle().value());
			assertEquals("title111", actualPhotoMst.getFirst().getPhotoEnglishTitle().value());
			assertEquals("caption111", actualPhotoMst.getFirst().getCaption().value());
			assertEquals(DirectionEnum.VERTICAL, actualPhotoMst.getFirst().getDirectionKbn());
			assertEquals(24, actualPhotoMst.getFirst().getFocalLength().value());
			assertEquals(0, BigDecimal.valueOf(8.0).compareTo(actualPhotoMst.getFirst().getFValue().value()));
			assertEquals(0, BigDecimal.valueOf(0.01).compareTo(actualPhotoMst.getFirst().getShutterSpeed().value()));
			assertEquals(100, actualPhotoMst.getFirst().getIso().value());
			
			// photo_tag_mst登録チェック
			List<PhotoTagMst> actualPhotoTagMst = jdbcTemplate.query(
					"SELECT * FROM photo.photo_tag_mst WHERE account_no=2 and photo_no=4", (rs, rowNum) ->
							PhotoTagMst.builder()
								.accountNo(new AccountNo(rs.getLong("account_no")))
								.photoNo(new PhotoNo(rs.getLong("photo_no")))
								.tagNo(new TagNo(rs.getLong("tag_no")))
								.createdBy(new CreatedBy(rs.getLong("created_by")))
								.createdAt(new CreatedAt(rs.getObject("created_at", OffsetDateTime.class)))
								.tagJapaneseName(new TagJapaneseName(rs.getObject("tag_japanese_name").toString()))
								.tagEnglishName(new TagEnglishName(rs.getObject("tag_english_name").toString()))
								.build());
			assertEquals(2, actualPhotoTagMst.size());
			
			assertEquals(new AccountNo(2L), actualPhotoTagMst.get(0).getAccountNo());
			assertEquals(4L, actualPhotoTagMst.get(0).getPhotoNo().value());
			assertEquals(1L, actualPhotoTagMst.get(0).getTagNo().value());
			assertEquals("太陽", actualPhotoTagMst.get(0).getTagJapaneseName().value());
			assertEquals("sun", actualPhotoTagMst.get(0).getTagEnglishName().value());
			assertEquals(new AccountNo(2L), actualPhotoTagMst.get(1).getAccountNo());
			assertEquals(4L, actualPhotoTagMst.get(1).getPhotoNo().value());
			assertEquals(2L, actualPhotoTagMst.get(1).getTagNo().value());
			assertEquals("青空", actualPhotoTagMst.get(1).getTagJapaneseName().value());
			assertEquals("bluesky", actualPhotoTagMst.get(1).getTagEnglishName().value());
		}
		
		@Test
		@Order(3)
		@DisplayName("正常系：更新。写真タグあり、撮影日時あり。Nullパラメータなし")
		void savePhoto_updatePhoto_with_photoTag_and_photoAt() throws Exception {
			String photoAccountId = "bbbbbbbb";
			
			AccountModel sessionAccount = AccountModel.builder()
					.accountNo(new AccountNo(2L))
					.accountId(new AccountId("bbbbbbbb"))
					.accountName(new AccountName("BBBBBBBB"))
					.password(new Password("$2a$10$password2"))
					.authorityKbn(AuthorityEnum.ADMINISTRATOR)
					.build();
			
			AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
			Authentication authentication = new UsernamePasswordAuthenticationToken(accountPrincipal, null, accountPrincipal.getAuthorities());
			
			mockMvc.perform(
					multipart("/api/v1/accounts/" + photoAccountId + "/photos")
					.contentType(MediaType.MULTIPART_FORM_DATA)
					.param("accountNo", "2")
					.param("photoNo", "1")
					.param("caption", "caption111")
					.param("imageFilePath", "https://www.xxx.com/bbbbbbbb/DSC21.jpg")
					.param("directionKbn", "VERTICAL")
					.param("photoEnglishTitle", "title111")
					.param("photoJapaneseTitle", "タイトル111")
					.param("photoAt", LocalDateTime.of(2000, 1, 1, 0, 0, 0).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
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
					.with(csrf())
				)
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.httpStatus").value(HttpStatus.OK.value()))
				.andExpect(jsonPath("$.isSuccess").value(true))
				.andExpect(jsonPath("$.message").value("写真登録が完了しました。"));
			
			// photo_mst登録チェック
			List<PhotoMst> actualPhotoMst = jdbcTemplate.query(
					"SELECT * FROM photo.photo_mst where account_no = 2 and photo_no=1", (rs, rowNum) ->
					PhotoMst.builder()
						.accountNo(new AccountNo(rs.getLong("account_no")))
						.photoNo(new PhotoNo(rs.getLong("photo_no")))
						.createdBy(new CreatedBy(rs.getLong("created_by")))
						.createdAt(new CreatedAt(rs.getObject("created_at", OffsetDateTime.class)))
						.updatedBy(new UpdatedBy(rs.getLong("updated_by")))
						.updatedAt(new UpdatedAt(rs.getObject("updated_at", OffsetDateTime.class)))
						.isDeleted(new IsDeleted(rs.getBoolean("is_deleted")))
						.photoAt(new PhotoAt(rs.getObject("photo_at", OffsetDateTime.class)))
						.locationNo(new LocationNo(rs.getLong("location_no")))
						.imageFilePath(new ImageFilePath(rs.getString("image_file_path")))
						.photoJapaneseTitle(new PhotoJapaneseTitle(rs.getString("photo_japanese_title")))
						.photoEnglishTitle(new PhotoEnglishTitle(rs.getString("photo_english_title")))
						.caption(new Caption(rs.getString("caption")))
						.directionKbn(DirectionEnum.getOrDefault(rs.getString("direction_kbn")))
						.focalLength(new FocalLength(rs.getInt("focal_length")))
						.fValue(new FValue(rs.getBigDecimal("f_value")))
						.shutterSpeed(new ShutterSpeed(rs.getBigDecimal("shutter_speed")))
						.iso(new Iso(rs.getInt("iso")))
						.build());
			
			assertEquals(1, actualPhotoMst.size());
			assertEquals(new AccountNo(2L), actualPhotoMst.getFirst().getAccountNo());
			assertEquals(1L, actualPhotoMst.getFirst().getPhotoNo().value());
			assertFalse(actualPhotoMst.getFirst().getIsDeleted().value());
			assertEquals(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualPhotoMst.getFirst().getPhotoAt().value().plusHours(9));
			assertEquals(0L, actualPhotoMst.getFirst().getLocationNo().value());
			assertEquals("https://www.xxx.com/bbbbbbbb/DSC21.jpg", actualPhotoMst.getFirst().getImageFilePath().value());
			assertEquals("タイトル111", actualPhotoMst.getFirst().getPhotoJapaneseTitle().value());
			assertEquals("title111", actualPhotoMst.getFirst().getPhotoEnglishTitle().value());
			assertEquals("caption111", actualPhotoMst.getFirst().getCaption().value());
			assertEquals(DirectionEnum.VERTICAL, actualPhotoMst.getFirst().getDirectionKbn());
			assertEquals(24, actualPhotoMst.getFirst().getFocalLength().value());
			assertEquals(0, BigDecimal.valueOf(8.0).compareTo(actualPhotoMst.getFirst().getFValue().value()));
			assertEquals(0, BigDecimal.valueOf(0.01).compareTo(actualPhotoMst.getFirst().getShutterSpeed().value()));
			assertEquals(100, actualPhotoMst.getFirst().getIso().value());
			
			// photo_tag_mst登録チェック
			List<PhotoTagMst> actualPhotoTagMst = jdbcTemplate.query(
					"SELECT * FROM photo.photo_tag_mst WHERE account_no=2 and photo_no=1", (rs, rowNum) ->
							PhotoTagMst.builder()
								.accountNo(new AccountNo(rs.getLong("account_no")))
								.photoNo(new PhotoNo(rs.getLong("photo_no")))
								.tagNo(new TagNo(rs.getLong("tag_no")))
								.createdBy(new CreatedBy(rs.getLong("created_by")))
								.createdAt(new CreatedAt(rs.getObject("created_at", OffsetDateTime.class)))
								.tagJapaneseName(new TagJapaneseName(rs.getObject("tag_japanese_name").toString()))
								.tagEnglishName(new TagEnglishName(rs.getObject("tag_english_name").toString()))
								.build());
			assertEquals(2, actualPhotoTagMst.size());
			
			assertEquals(new AccountNo(2L), actualPhotoTagMst.get(0).getAccountNo());
			assertEquals(1L, actualPhotoTagMst.get(0).getPhotoNo().value());
			assertEquals(1L, actualPhotoTagMst.get(0).getTagNo().value());
			assertEquals("太陽", actualPhotoTagMst.get(0).getTagJapaneseName().value());
			assertEquals("sun", actualPhotoTagMst.get(0).getTagEnglishName().value());
			assertEquals(new AccountNo(2L), actualPhotoTagMst.get(1).getAccountNo());
			assertEquals(1L, actualPhotoTagMst.get(1).getPhotoNo().value());
			assertEquals(2L, actualPhotoTagMst.get(1).getTagNo().value());
			assertEquals("青空", actualPhotoTagMst.get(1).getTagJapaneseName().value());
			assertEquals("bluesky", actualPhotoTagMst.get(1).getTagEnglishName().value());
		}
		
		@Test
		@Order(4)
		@DisplayName("異常系：アクセス不正。ForbiddenAccountExceptionをthrowする")
		void savePhoto_ForbiddenAccountException() throws Exception {
			String photoAccountId = "bbbbbbbb";
			MockMultipartFile multipartFile = new MockMultipartFile(
					"imageFile",
					"DSC111.jpg",
					MediaType.MULTIPART_FORM_DATA_VALUE,
					"sample image".getBytes());
			
			AccountModel sessionAccount = AccountModel.builder()
					.accountNo(new AccountNo(1L))
					.accountId(new AccountId("aaaaaaaa"))
					.accountName(new AccountName("AAAAAAAA"))
					.password(new Password("$2a$10$password1"))
					.authorityKbn(AuthorityEnum.ADMINISTRATOR)
					.build();
			
			AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
			Authentication authentication = new UsernamePasswordAuthenticationToken(accountPrincipal, null, accountPrincipal.getAuthorities());
			
			mockMvc.perform(
					multipart("/api/v1/accounts/" + photoAccountId + "/photos")
					.file(multipartFile)
					.contentType(MediaType.MULTIPART_FORM_DATA)
					.param("accountNo", "1")
					.param("imageFilePath", "")
					.with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
					.with(csrf())
				)
				.andExpect(status().isForbidden())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.httpStatus").value(HttpStatus.FORBIDDEN.value()))
				.andExpect(jsonPath("$.errorCode").value(ErrorEnum.NOT_AUTHORIZED_TO_EDIT_PHOTO.getErrorCode()))
				.andExpect(jsonPath("$.errorMessage").value(ErrorEnum.NOT_AUTHORIZED_TO_EDIT_PHOTO.getErrorMessage()));
		}

		@Test
		@Order(5)
		@DisplayName("異常系：登録上限に達している。PhotoNotAdditableExceptionをthrowする")
		void savePhoto_PhotoNotAdditableException() throws Exception {
			String photoAccountId = "aaaaaaaa";
			MockMultipartFile multipartFile = new MockMultipartFile(
					"imageFile",
					"DSC111.jpg",
					MediaType.MULTIPART_FORM_DATA_VALUE,
					"sample image".getBytes());
			
			AccountModel sessionAccount = AccountModel.builder()
					.accountNo(new AccountNo(1L))
					.accountId(new AccountId("aaaaaaaa"))
					.accountName(new AccountName("AAAAAAAA"))
					.password(new Password("$2a$10$password1"))
					.authorityKbn(AuthorityEnum.MINI)
					.build();
			
			AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
			Authentication authentication = new UsernamePasswordAuthenticationToken(accountPrincipal, null, accountPrincipal.getAuthorities());
			
			mockMvc.perform(
					multipart("/api/v1/accounts/" + photoAccountId + "/photos")
					.file(multipartFile)
					.contentType(MediaType.MULTIPART_FORM_DATA)
					.param("accountNo", "1")
					.param("imageFilePath", "")
					.with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
					.with(csrf())
				)
				.andExpect(status().isBadRequest())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.httpStatus").value(HttpStatus.BAD_REQUEST.value()))
				.andExpect(jsonPath("$.errorCode").value(ErrorEnum.REACHED_REGISTRATION_LIMIT.getErrorCode()))
				.andExpect(jsonPath("$.errorMessage").value(ErrorEnum.REACHED_REGISTRATION_LIMIT.getErrorMessage()));
		}
		
		@Test
		@Order(6)
		@DisplayName("異常系：画像ファイル、ファイルパスともにnull。BadRequestExceptionをthrowする")
		void savePhoto_BadRequestException_file_and_filepath_is_null() throws Exception {
			String photoAccountId = "bbbbbbbb";
			
			AccountModel sessionAccount = AccountModel.builder()
					.accountNo(new AccountNo(2L))
					.accountId(new AccountId("bbbbbbbb"))
					.accountName(new AccountName("BBBBBBBB"))
					.password(new Password("$2a$10$password2"))
					.authorityKbn(AuthorityEnum.MINI)
					.build();
			
			AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
			Authentication authentication = new UsernamePasswordAuthenticationToken(accountPrincipal, null, accountPrincipal.getAuthorities());
			
			mockMvc.perform(
					multipart("/api/v1/accounts/" + photoAccountId + "/photos")
					.contentType(MediaType.MULTIPART_FORM_DATA)
					.param("accountNo", "2")
					.with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
					.with(csrf())
				)
				.andExpect(status().isBadRequest())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.httpStatus").value(HttpStatus.BAD_REQUEST.value()))
				.andExpect(jsonPath("$.isSuccess").value(false))
				.andExpect(jsonPath("$.message").value(ErrorEnum.INVALID_INPUT.getErrorMessage()));
		}
		
		@Test
		@Order(7)
		@DisplayName("異常系：画像ファイルがnull、ファイルパスがblank。BadRequestExceptionをthrowする")
		void savePhoto_BadRequestException_file_and_filepath_is_blank() throws Exception {
			String photoAccountId = "bbbbbbbb";
			
			AccountModel sessionAccount = AccountModel.builder()
					.accountNo(new AccountNo(2L))
					.accountId(new AccountId("bbbbbbbb"))
					.accountName(new AccountName("BBBBBBBB"))
					.password(new Password("$2a$10$password2"))
					.authorityKbn(AuthorityEnum.MINI)
					.build();
			
			AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
			Authentication authentication = new UsernamePasswordAuthenticationToken(accountPrincipal, null, accountPrincipal.getAuthorities());
			
			mockMvc.perform(
					multipart("/api/v1/accounts/" + photoAccountId + "/photos")
					.contentType(MediaType.MULTIPART_FORM_DATA)
					.param("accountNo", "2")
					.param("imageFilePath", "")
					.with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
					.with(csrf())
				)
				.andExpect(status().isBadRequest())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.httpStatus").value(HttpStatus.BAD_REQUEST.value()))
				.andExpect(jsonPath("$.isSuccess").value(false))
				.andExpect(jsonPath("$.message").value(ErrorEnum.INVALID_INPUT.getErrorMessage()));
		}
		
		@Test
		@Order(8)
		@DisplayName("異常系：画像ファイル、ファイルパス以外のパラメータ不正。BadRequestExceptionをthrowする")
		void savePhoto_BadRequestException_others() throws Exception {
			String photoAccountId = "bbbbbbbb";
			MockMultipartFile multipartFile = new MockMultipartFile(
					"imageFile",
					"DSC111.jpg",
					MediaType.MULTIPART_FORM_DATA_VALUE,
					"sample image".getBytes());
			
			AccountModel sessionAccount = AccountModel.builder()
					.accountNo(new AccountNo(2L))
					.accountId(new AccountId("bbbbbbbb"))
					.accountName(new AccountName("BBBBBBBB"))
					.password(new Password("$2a$10$password2"))
					.authorityKbn(AuthorityEnum.ADMINISTRATOR)
					.build();
			
			AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
			Authentication authentication = new UsernamePasswordAuthenticationToken(accountPrincipal, null, accountPrincipal.getAuthorities());
			
			mockMvc.perform(
					multipart("/api/v1/accounts/" + photoAccountId + "/photos")
					.file(multipartFile)
					.contentType(MediaType.MULTIPART_FORM_DATA)
					.param("accountNo", "")
					.param("imageFilePath", "")
					.with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
					.with(csrf())
				)
				.andExpect(status().isBadRequest())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.httpStatus").value(HttpStatus.BAD_REQUEST.value()))
				.andExpect(jsonPath("$.isSuccess").value(false))
				.andExpect(jsonPath("$.message").value(ErrorEnum.INVALID_INPUT.getErrorMessage()));
		}
		
		@Test
		@Order(9)
		@DisplayName("異常系：FileDuplicateExceptionをthrowする")
		void savePhoto_FileDuplicateException() throws Exception {
			String photoAccountId = "bbbbbbbb";
			MockMultipartFile multipartFile = new MockMultipartFile(
					"imageFile",
					"DSC21.jpg",
					MediaType.MULTIPART_FORM_DATA_VALUE,
					"sample image".getBytes());
			
			AccountModel sessionAccount = AccountModel.builder()
					.accountNo(new AccountNo(2L))
					.accountId(new AccountId("bbbbbbbb"))
					.accountName(new AccountName("BBBBBBBB"))
					.password(new Password("$2a$10$password2"))
					.authorityKbn(AuthorityEnum.ADMINISTRATOR)
					.build();
			
			AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
			Authentication authentication = new UsernamePasswordAuthenticationToken(accountPrincipal, null, accountPrincipal.getAuthorities());
			
			mockMvc.perform(
					multipart("/api/v1/accounts/" + photoAccountId + "/photos")
					.file(multipartFile)
					.contentType(MediaType.MULTIPART_FORM_DATA)
					.param("accountNo", "2")
					.param("caption", "")
					.param("imageFilePath", "")
					.param("directionKbn", "VERTICAL")
					.param("photoEnglishTitle", "")
					.param("photoJapaneseTitle", "")
					.with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
					.with(csrf())
				)
				.andExpect(status().isConflict())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.httpStatus").value(HttpStatus.CONFLICT.value()))
				.andExpect(jsonPath("$.errorCode").value(ErrorEnum.DUPLICATE_PHOTO_FILE.getErrorCode()))
				.andExpect(jsonPath("$.errorMessage").value(ErrorEnum.DUPLICATE_PHOTO_FILE.getErrorMessage()));
		}
		
		@Test
		@Order(10)
		@DisplayName("異常系：UpdateFailureExceptionをthrowする")
		void savePhoto_UpdateFailureException() throws Exception {
			String photoAccountId = "bbbbbbbb";
			
			AccountModel sessionAccount = AccountModel.builder()
					.accountNo(new AccountNo(2L))
					.accountId(new AccountId("bbbbbbbb"))
					.accountName(new AccountName("BBBBBBBB"))
					.password(new Password("$2a$10$password2"))
					.authorityKbn(AuthorityEnum.ADMINISTRATOR)
					.build();
			
			AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
			Authentication authentication = new UsernamePasswordAuthenticationToken(accountPrincipal, null, accountPrincipal.getAuthorities());
			
			mockMvc.perform(
					multipart("/api/v1/accounts/" + photoAccountId + "/photos")
					.contentType(MediaType.MULTIPART_FORM_DATA)
					.param("accountNo", "2")
					.param("photoNo", "99")
					.param("caption", "caption21")
					.param("imageFilePath", "https://www.xxx.com/DSC99.jpg")
					.param("directionKbn", "VERTICAL")
					.param("photoEnglishTitle", "")
					.param("photoJapaneseTitle", "")
					.with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
					.with(csrf())
				)
				.andExpect(status().isConflict())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.httpStatus").value(HttpStatus.CONFLICT.value()))
				.andExpect(jsonPath("$.errorCode").value(ErrorEnum.FAIL_TO_UPDATE_PHOTO.getErrorCode()))
				.andExpect(jsonPath("$.errorMessage").value(ErrorEnum.FAIL_TO_UPDATE_PHOTO.getErrorMessage()));
		}
	}
	
	@Nested
	@Order(3)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	@Sql("/sql/common/cleanup.sql")
	@Sql("/sql/controller/PhotoRestControllerIntegrationTest.sql")
	class deletePhoto {
		@Test
		@Order(1)
		@DisplayName("正常系")
		void deletePhoto_success() throws Exception {
			String photoAccountId = "aaaaaaaa";
			String loginAccountId = "aaaaaaaa";

			AccountModel sessionAccount = AccountModel.builder()
					.accountNo(new AccountNo(1L))
					.accountId(new AccountId(loginAccountId))
					.accountName(new AccountName("AAAAAAAA"))
					.password(new Password("$2a$10$password1"))
					.authorityKbn(AuthorityEnum.ADMINISTRATOR)
					.build();

			AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
			Authentication authentication = new UsernamePasswordAuthenticationToken(accountPrincipal, null, accountPrincipal.getAuthorities());

			mockMvc.perform(
					delete("/api/v1/accounts/" + photoAccountId + "/photos")
					.contentType(MediaType.APPLICATION_JSON)
					.content(readJsonFile("delete_photo_success.json"))
					.with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
					.with(csrf())
				)
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.httpStatus").value(200))
				.andExpect(jsonPath("$.isSuccess").value(true))
				.andExpect(jsonPath("$.message").value("写真削除が完了しました。"));
			
			// photo_mst削除チェック
			List<PhotoMst> actualPhotoMst = jdbcTemplate.query(
					"SELECT * FROM photo.photo_mst where account_no = 1 and photo_no=1", (rs, rowNum) ->
					PhotoMst.builder()
						.accountNo(new AccountNo(rs.getLong("account_no")))
						.photoNo(new PhotoNo(rs.getLong("photo_no")))
						.createdBy(new CreatedBy(rs.getLong("created_by")))
						.createdAt(new CreatedAt(rs.getObject("created_at", OffsetDateTime.class)))
						.updatedBy(new UpdatedBy(rs.getLong("updated_by")))
						.updatedAt(new UpdatedAt(rs.getObject("updated_at", OffsetDateTime.class)))
						.isDeleted(new IsDeleted(rs.getBoolean("is_deleted")))
						.photoAt(new PhotoAt(rs.getObject("photo_at", OffsetDateTime.class)))
						.locationNo(new LocationNo(rs.getLong("location_no")))
						.imageFilePath(new ImageFilePath(rs.getString("image_file_path")))
						.photoJapaneseTitle(new PhotoJapaneseTitle(rs.getString("photo_japanese_title")))
						.photoEnglishTitle(new PhotoEnglishTitle(rs.getString("photo_english_title")))
						.caption(new Caption(rs.getString("caption")))
						.directionKbn(DirectionEnum.getOrDefault(rs.getString("direction_kbn")))
						.focalLength(new FocalLength(rs.getInt("focal_length")))
						.fValue(new FValue(rs.getBigDecimal("f_value")))
						.shutterSpeed(new ShutterSpeed(rs.getBigDecimal("shutter_speed")))
						.iso(new Iso(rs.getInt("iso")))
						.build());
			assertTrue(actualPhotoMst.getFirst().getIsDeleted().value());
			
			// photo_tag_mst削除チェック
			List<PhotoTagMst> actualPhotoTagMst = jdbcTemplate.query(
						"SELECT * FROM photo.photo_tag_mst WHERE account_no=1 and photo_no=1", (rs, rowNum) ->
							PhotoTagMst.builder()
								.accountNo(new AccountNo(rs.getLong("account_no")))
								.photoNo(new PhotoNo(rs.getLong("photo_no")))
								.tagNo(new TagNo(rs.getLong("tag_no")))
								.createdBy(new CreatedBy(rs.getLong("created_by")))
								.createdAt(new CreatedAt(rs.getObject("created_at", OffsetDateTime.class)))
								.tagJapaneseName(new TagJapaneseName(rs.getObject("tag_japanese_name").toString()))
								.tagEnglishName(new TagEnglishName(rs.getObject("tag_english_name").toString()))
								.build());
			assertEquals(0, actualPhotoTagMst.size());
			
			// photo_favorite削除チェック
			List<PhotoFavorite> actualPhotoFavoriteData = jdbcTemplate.query(
					"SELECT * FROM photo.photo_favorite WHERE favorite_photo_account_no=1 and favorite_photo_no=1", (rs, rowNum) ->
						PhotoFavorite.builder()
							.accountNo(new AccountNo(rs.getLong("account_no")))
							.favoritePhotoAccountNo(new AccountNo(rs.getLong("favorite_photo_account_no")))
							.favoritePhotoNo(new PhotoNo(rs.getLong("favorite_photo_no")))
							.createdBy(new CreatedBy(rs.getLong("created_by")))
							.createdAt(new CreatedAt(rs.getObject("created_at", OffsetDateTime.class)))
							.build());
			assertEquals(0, actualPhotoFavoriteData.size());
		}
		
		@Test
		@Order(2)
		@DisplayName("異常系：不正アクセス。ForbiddenAccountExceptionをthrowする")
		void deletePhoto_ForbiddenAccountException() throws Exception {
			String photoAccountId = "aaaaaaaa";
			String loginAccountId = "eeeeeeee";

			AccountModel sessionAccount = AccountModel.builder()
					.accountNo(new AccountNo(1L))
					.accountId(new AccountId(loginAccountId))
					.accountName(new AccountName("EEEEEEEE"))
					.password(new Password("$2a$10$password5"))
					.authorityKbn(AuthorityEnum.ADMINISTRATOR)
					.build();

			AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
			Authentication authentication = new UsernamePasswordAuthenticationToken(accountPrincipal, null, accountPrincipal.getAuthorities());

			mockMvc.perform(
					delete("/api/v1/accounts/" + photoAccountId + "/photos")
					.contentType(MediaType.APPLICATION_JSON)
					.content(readJsonFile("delete_photo_forbidden.json"))
					.with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
					.with(csrf())
				)
				.andExpect(status().isForbidden())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.httpStatus").value(HttpStatus.FORBIDDEN.value()))
				.andExpect(jsonPath("$.errorCode").value(ErrorEnum.NOT_AUTHORIZED_TO_EDIT_PHOTO.getErrorCode()))
				.andExpect(jsonPath("$.errorMessage").value(ErrorEnum.NOT_AUTHORIZED_TO_EDIT_PHOTO.getErrorMessage()));
			
		}
		
		@Test
		@Order(3)
		@DisplayName("異常系：パラメータ不正。BadRequestExceptionをthrowする")
		void deletePhoto_BadRequestException() throws Exception {
			String photoAccountId = "aaaaaaaa";
			String loginAccountId = "aaaaaaaa";

			AccountModel sessionAccount = AccountModel.builder()
					.accountNo(new AccountNo(1L))
					.accountId(new AccountId(loginAccountId))
					.accountName(new AccountName("AAAAAAAA"))
					.password(new Password("$2a$10$password1"))
					.authorityKbn(AuthorityEnum.ADMINISTRATOR)
					.build();

			AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
			Authentication authentication = new UsernamePasswordAuthenticationToken(accountPrincipal, null, accountPrincipal.getAuthorities());

			mockMvc.perform(
					delete("/api/v1/accounts/" + photoAccountId + "/photos")
					.contentType(MediaType.APPLICATION_JSON)
					.content(readJsonFile("delete_photo_badrequest.json"))
					.with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
					.with(csrf())
				)
				.andExpect(status().isBadRequest())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.httpStatus").value(HttpStatus.BAD_REQUEST.value()))
				.andExpect(jsonPath("$.isSuccess").value(false))
				.andExpect(jsonPath("$.message").value(ErrorEnum.INVALID_INPUT.getErrorMessage()));
		}
		
		@Test
		@Order(4)
		@DisplayName("異常系：UpdateFailureExceptionをthrowする")
		void deletePhoto_UpdateFailureException() throws Exception {
			String photoAccountId = "aaaaaaaa";
			String loginAccountId = "aaaaaaaa";

			AccountModel sessionAccount = AccountModel.builder()
					.accountNo(new AccountNo(1L))
					.accountId(new AccountId(loginAccountId))
					.accountName(new AccountName("AAAAAAAA"))
					.password(new Password("$2a$10$password1"))
					.authorityKbn(AuthorityEnum.ADMINISTRATOR)
					.build();

			AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
			Authentication authentication = new UsernamePasswordAuthenticationToken(accountPrincipal, null, accountPrincipal.getAuthorities());

			mockMvc.perform(
					delete("/api/v1/accounts/" + photoAccountId + "/photos")
					.contentType(MediaType.APPLICATION_JSON)
					.content(readJsonFile("delete_photo_update_failure.json"))
					.with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
					.with(csrf())
				)
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.httpStatus").value(HttpStatus.CONFLICT.value()))
				.andExpect(jsonPath("$.errorCode").value(ErrorEnum.FAIL_TO_DELETE_PHOTO.getErrorCode()))
				.andExpect(jsonPath("$.errorMessage").value(ErrorEnum.FAIL_TO_DELETE_PHOTO.getErrorMessage()));
		}
	}

	@Nested
	@Order(4)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	@Sql("/sql/common/cleanup.sql")
	@Sql("/sql/controller/PhotoRestControllerIntegrationTest.sql")
	class getPhotoUpperLimit {
		@Test
		@Order(1)
		@DisplayName("正常系：自分のアカウントで上限未到達の場合")
		void getPhotoUpperLimit_not_reached() throws Exception {
			String photoAccountId = "bbbbbbbb";

			AccountModel sessionAccount = AccountModel.builder()
					.accountNo(new AccountNo(2L))
					.accountId(new AccountId(photoAccountId))
					.accountName(new AccountName("BBBBBBBB"))
					.password(new Password("$2a$10$password2"))
					.authorityKbn(AuthorityEnum.MINI)
					.build();

			AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
			Authentication authentication = new UsernamePasswordAuthenticationToken(accountPrincipal, null, accountPrincipal.getAuthorities());

			mockMvc.perform(
					get("/api/v1/accounts/" + photoAccountId + "/photos/upper-limit")
					.with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
					.with(csrf())
				)
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.isReachedUpperLimit").value(false));
		}

		@Test
		@Order(2)
		@DisplayName("正常系：自分のアカウントで上限到達の場合")
		void getPhotoUpperLimit_reached() throws Exception {
			String photoAccountId = "aaaaaaaa";

			AccountModel sessionAccount = AccountModel.builder()
					.accountNo(new AccountNo(1L))
					.accountId(new AccountId(photoAccountId))
					.accountName(new AccountName("AAAAAAAA"))
					.password(new Password("$2a$10$password1"))
					.authorityKbn(AuthorityEnum.MINI)
					.build();

			AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
			Authentication authentication = new UsernamePasswordAuthenticationToken(accountPrincipal, null, accountPrincipal.getAuthorities());

			mockMvc.perform(
					get("/api/v1/accounts/" + photoAccountId + "/photos/upper-limit")
					.with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
					.with(csrf())
				)
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.isReachedUpperLimit").value(true));
		}

		@Test
		@Order(3)
		@DisplayName("正常系：他人のアカウントの場合はfalse")
		void getPhotoUpperLimit_other_account() throws Exception {
			AccountModel sessionAccount = AccountModel.builder()
					.accountNo(new AccountNo(2L))
					.accountId(new AccountId("bbbbbbbb"))
					.accountName(new AccountName("BBBBBBBB"))
					.password(new Password("$2a$10$password2"))
					.authorityKbn(AuthorityEnum.MINI)
					.build();

			AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
			Authentication authentication = new UsernamePasswordAuthenticationToken(accountPrincipal, null, accountPrincipal.getAuthorities());

			mockMvc.perform(
					get("/api/v1/accounts/aaaaaaaa/photos/upper-limit")
					.with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
					.with(csrf())
				)
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.isReachedUpperLimit").value(false));
		}
	}
}