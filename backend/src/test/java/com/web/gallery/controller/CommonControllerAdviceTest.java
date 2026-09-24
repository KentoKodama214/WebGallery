package com.web.gallery.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.web.gallery.enumeration.ErrorEnum;
import com.web.gallery.exception.BadRequestException;
import com.web.gallery.exception.FavoriteNotFoundException;
import com.web.gallery.exception.FileDuplicateException;
import com.web.gallery.exception.ForbiddenAccountException;
import com.web.gallery.exception.InquiryNotFoundException;
import com.web.gallery.exception.PhotoNotAdditableException;
import com.web.gallery.exception.PhotoNotFoundException;
import com.web.gallery.exception.RegistFailureException;
import com.web.gallery.exception.SystemException;
import com.web.gallery.exception.UpdateFailureException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class CommonControllerAdviceTest {
  @InjectMocks private CommonControllerAdvice commonControllerAdvice;

  private MockMvc mockMvc;

  @RestController
  static class TestController extends PhotoFavoriteController {
    TestController() {
      super(null, null);
    }

    @GetMapping("/test/bad_request")
    public String throwBadRequestException() throws BadRequestException {
      throw new BadRequestException(ErrorEnum.INVALID_INPUT);
    }

    @GetMapping("/test/forbidden")
    public String throwForbiddenAccountException() throws ForbiddenAccountException {
      throw new ForbiddenAccountException(ErrorEnum.INVALID_INPUT);
    }

    @GetMapping("/test/file_duplicate")
    public String throwFileDuplicateException() throws FileDuplicateException {
      throw new FileDuplicateException(ErrorEnum.INVALID_INPUT);
    }

    @GetMapping("/test/photo_not_additable")
    public String throwPhotoNotAdditableException() throws PhotoNotAdditableException {
      throw new PhotoNotAdditableException(ErrorEnum.INVALID_INPUT);
    }

    @GetMapping("/test/regist_failure")
    public String throwRegistFailureException() throws RegistFailureException {
      throw new RegistFailureException(ErrorEnum.INVALID_INPUT);
    }

    @GetMapping("/test/update_failure")
    public String throwUpdateFailureException() throws UpdateFailureException {
      throw new UpdateFailureException(ErrorEnum.INVALID_INPUT);
    }

    @GetMapping("/test/data_access_error")
    public String throwDataAccessException() {
      throw new DataIntegrityViolationException("unexpected data access error");
    }

    @GetMapping("/test/photo_not_found")
    public String throwPhotoNotFoundException() throws PhotoNotFoundException {
      throw new PhotoNotFoundException(ErrorEnum.INVALID_INPUT);
    }

    @GetMapping("/test/favorite_not_found")
    public String throwFavoriteNotFoundException() throws FavoriteNotFoundException {
      throw new FavoriteNotFoundException(ErrorEnum.INVALID_INPUT);
    }

    @GetMapping("/test/inquiry_not_found")
    public String throwInquiryNotFoundException() throws InquiryNotFoundException {
      throw new InquiryNotFoundException(ErrorEnum.INVALID_INPUT);
    }

    @PostMapping("/test/method_argument_not_valid")
    public String throwMethodArgumentNotValidException(@Valid @RequestBody TestRequestBody body) {
      return "ok";
    }

    @GetMapping("/test/missing_parameter")
    public String throwMissingServletRequestParameterException(@RequestParam String requiredParam) {
      return "ok";
    }

    @GetMapping("/test/type_mismatch/{id}")
    public String throwMethodArgumentTypeMismatchException(@PathVariable Long id) {
      return "ok";
    }

    @GetMapping("/test/max_upload_size")
    public String throwMaxUploadSizeExceededException() {
      throw new MaxUploadSizeExceededException(5_000_000L);
    }

    @GetMapping("/test/gallery_exception")
    public String throwGalleryException() throws SystemException {
      throw new SystemException(ErrorEnum.SYSTEM_ERROR);
    }

    @GetMapping("/test/unexpected_exception")
    public String throwUnexpectedException() {
      throw new IllegalStateException("unexpected error");
    }
  }

  static class TestRequestBody {
    @NotBlank private String requiredField;

    public String getRequiredField() {
      return requiredField;
    }

    public void setRequiredField(String requiredField) {
      this.requiredField = requiredField;
    }
  }

  @BeforeEach
  void setUp() {
    mockMvc =
        MockMvcBuilders.standaloneSetup(new TestController())
            .setControllerAdvice(commonControllerAdvice)
            .build();
  }

  private String readJsonFile(String fileName) throws Exception {
    return new String(
        new ClassPathResource("json/controller/CommonControllerAdviceTest/" + fileName)
            .getInputStream()
            .readAllBytes(),
        StandardCharsets.UTF_8);
  }

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class handleBadRequestException {
    @Test
    @Order(1)
    @DisplayName("正常系")
    void handleBadRequestException_not_login_user() throws Exception {
      mockMvc
          .perform(get("/test/bad_request"))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.httpStatus").value(400))
          .andExpect(jsonPath("$.isSuccess").value(false))
          .andExpect(jsonPath("$.message").value(ErrorEnum.INVALID_INPUT.getErrorMessage()));
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class handleFileForbiddenAccountException {
    @Test
    @Order(1)
    @DisplayName("正常系")
    void handleFileForbiddenAccountException_success() throws Exception {
      mockMvc
          .perform(get("/test/forbidden"))
          .andExpect(status().isForbidden())
          .andExpect(jsonPath("$.httpStatus").value(403))
          .andExpect(jsonPath("$.errorMessage").value(ErrorEnum.INVALID_INPUT.getErrorMessage()));
    }
  }

  @Nested
  @Order(3)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class handleFileDuplicateException {
    @Test
    @Order(1)
    @DisplayName("正常系")
    void handleFileDuplicateException_success() throws Exception {
      mockMvc
          .perform(get("/test/file_duplicate"))
          .andExpect(status().isConflict())
          .andExpect(jsonPath("$.httpStatus").value(409))
          .andExpect(jsonPath("$.errorMessage").value(ErrorEnum.INVALID_INPUT.getErrorMessage()));
    }
  }

  @Nested
  @Order(4)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class handlePhotoNotAdditableException {
    @Test
    @Order(1)
    @DisplayName("正常系")
    void handlePhotoNotAdditableException_success() throws Exception {
      mockMvc
          .perform(get("/test/photo_not_additable"))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.httpStatus").value(400))
          .andExpect(jsonPath("$.errorMessage").value(ErrorEnum.INVALID_INPUT.getErrorMessage()));
    }
  }

  @Nested
  @Order(5)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class handleInsertFailedException {
    @Test
    @Order(1)
    @DisplayName("正常系")
    void handleInsertFailedException_success() throws Exception {
      mockMvc
          .perform(get("/test/regist_failure"))
          .andExpect(status().isConflict())
          .andExpect(jsonPath("$.httpStatus").value(409))
          .andExpect(jsonPath("$.errorMessage").value(ErrorEnum.INVALID_INPUT.getErrorMessage()));
    }
  }

  @Nested
  @Order(6)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class handleUpdateFailureException {
    @Test
    @Order(1)
    @DisplayName("正常系")
    void handleUpdateFailureException_success() throws Exception {
      mockMvc
          .perform(get("/test/update_failure"))
          .andExpect(status().isConflict())
          .andExpect(jsonPath("$.httpStatus").value(409))
          .andExpect(jsonPath("$.errorMessage").value(ErrorEnum.INVALID_INPUT.getErrorMessage()));
    }
  }

  @Nested
  @Order(7)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class handleDataAccessException {
    @Test
    @Order(1)
    @DisplayName("正常系：内部情報を含まない汎用エラーレスポンスに変換される")
    void handleDataAccessException_success() throws Exception {
      mockMvc
          .perform(get("/test/data_access_error"))
          .andExpect(status().isInternalServerError())
          .andExpect(jsonPath("$.httpStatus").value(500))
          .andExpect(jsonPath("$.errorCode").value(ErrorEnum.SYSTEM_ERROR.getErrorCode()))
          .andExpect(jsonPath("$.errorMessage").value(ErrorEnum.SYSTEM_ERROR.getErrorMessage()));
    }
  }

  @Nested
  @Order(8)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class handlePhotoNotFoundException {
    @Test
    @Order(1)
    @DisplayName("正常系")
    void handlePhotoNotFoundException_success() throws Exception {
      mockMvc
          .perform(get("/test/photo_not_found"))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.httpStatus").value(404))
          .andExpect(jsonPath("$.errorMessage").value(ErrorEnum.INVALID_INPUT.getErrorMessage()));
    }
  }

  @Nested
  @Order(9)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class handleFavoriteNotFoundException {
    @Test
    @Order(1)
    @DisplayName("正常系")
    void handleFavoriteNotFoundException_success() throws Exception {
      mockMvc
          .perform(get("/test/favorite_not_found"))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.httpStatus").value(404))
          .andExpect(jsonPath("$.errorMessage").value(ErrorEnum.INVALID_INPUT.getErrorMessage()));
    }
  }

  @Nested
  @Order(10)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class handleInquiryNotFoundException {
    @Test
    @Order(1)
    @DisplayName("正常系")
    void handleInquiryNotFoundException_success() throws Exception {
      mockMvc
          .perform(get("/test/inquiry_not_found"))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.httpStatus").value(400))
          .andExpect(jsonPath("$.errorMessage").value(ErrorEnum.INVALID_INPUT.getErrorMessage()));
    }
  }

  @Nested
  @Order(11)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class handleRequestBindingException {
    @Test
    @Order(1)
    @DisplayName("正常系：MethodArgumentNotValidException（リクエストボディのバリデーション違反）")
    void handleRequestBindingException_methodArgumentNotValid() throws Exception {
      mockMvc
          .perform(
              post("/test/method_argument_not_valid")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("blank_required_field.json")))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.message").value(ErrorEnum.INVALID_INPUT.getErrorMessage()));
    }

    @Test
    @Order(2)
    @DisplayName("正常系：HttpMessageNotReadableException（不正なJSON）")
    void handleRequestBindingException_httpMessageNotReadable() throws Exception {
      mockMvc
          .perform(
              post("/test/method_argument_not_valid")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("invalid_json.json")))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.message").value(ErrorEnum.INVALID_INPUT.getErrorMessage()));
    }

    @Test
    @Order(3)
    @DisplayName("正常系：MissingServletRequestParameterException（必須パラメータ欠落）")
    void handleRequestBindingException_missingParameter() throws Exception {
      mockMvc
          .perform(get("/test/missing_parameter"))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.message").value(ErrorEnum.INVALID_INPUT.getErrorMessage()));
    }

    @Test
    @Order(4)
    @DisplayName("正常系：MethodArgumentTypeMismatchException（パス変数の型不一致）")
    void handleRequestBindingException_typeMismatch() throws Exception {
      mockMvc
          .perform(get("/test/type_mismatch/not_a_number"))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.message").value(ErrorEnum.INVALID_INPUT.getErrorMessage()));
    }

    @Test
    @Order(5)
    @DisplayName("正常系：MaxUploadSizeExceededException（アップロードサイズ超過）")
    void handleRequestBindingException_maxUploadSizeExceeded() throws Exception {
      mockMvc
          .perform(get("/test/max_upload_size"))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.message").value(ErrorEnum.INVALID_INPUT.getErrorMessage()));
    }
  }

  @Nested
  @Order(12)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class handleGalleryException {
    @Test
    @Order(1)
    @DisplayName("正常系：個別のExceptionHandlerを持たないGalleryExceptionは汎用エラーレスポンスに変換される")
    void handleGalleryException_success() throws Exception {
      mockMvc
          .perform(get("/test/gallery_exception"))
          .andExpect(status().isInternalServerError())
          .andExpect(jsonPath("$.httpStatus").value(500))
          .andExpect(jsonPath("$.errorCode").value(ErrorEnum.SYSTEM_ERROR.getErrorCode()))
          .andExpect(jsonPath("$.errorMessage").value(ErrorEnum.SYSTEM_ERROR.getErrorMessage()));
    }
  }

  @Nested
  @Order(13)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class handleUnexpectedException {
    @Test
    @Order(1)
    @DisplayName("正常系：予期しない例外は内部情報を含まない汎用エラーレスポンスに変換される")
    void handleUnexpectedException_success() throws Exception {
      mockMvc
          .perform(get("/test/unexpected_exception"))
          .andExpect(status().isInternalServerError())
          .andExpect(jsonPath("$.httpStatus").value(500))
          .andExpect(jsonPath("$.errorCode").value(ErrorEnum.SYSTEM_ERROR.getErrorCode()))
          .andExpect(jsonPath("$.errorMessage").value(ErrorEnum.SYSTEM_ERROR.getErrorMessage()));
    }
  }
}
