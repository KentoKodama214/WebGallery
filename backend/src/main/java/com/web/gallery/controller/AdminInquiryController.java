package com.web.gallery.controller;

import com.web.gallery.annotation.RequireAdminAuthority;
import com.web.gallery.constant.ApiRoutes;
import com.web.gallery.constant.MessageConst;
import com.web.gallery.controller.request.AdminInquiryListRequest;
import com.web.gallery.controller.request.InquiryReplyRequest;
import com.web.gallery.controller.response.AdminInquiryDetailGetResponse;
import com.web.gallery.controller.response.AdminInquiryListGetResponse;
import com.web.gallery.controller.response.InquiryReplyResponse;
import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.inquiry.InquiryId;
import com.web.gallery.domain.inquiry.ReplyBody;
import com.web.gallery.domain.inquiry.ReplyNo;
import com.web.gallery.enumeration.ErrorEnum;
import com.web.gallery.exception.GalleryException;
import com.web.gallery.helper.SessionHelper;
import com.web.gallery.helper.ValidationErrorLogger;
import com.web.gallery.model.InquiryDetailModel;
import com.web.gallery.model.InquiryListGetModel;
import com.web.gallery.model.InquiryPageModel;
import com.web.gallery.service.InquiryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/** 管理者用お問い合わせ管理に関するAPI通信を扱うControllerクラス */
@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "管理者お問い合わせ管理", description = "管理者用お問い合わせ管理に関するAPI")
@SecurityRequirement(name = "Bearer")
public class AdminInquiryController {

  private final InquiryService inquiryService;
  private final SessionHelper sessionHelper;

  /**
   * 管理者用お問い合わせ一覧取得
   *
   * @param adminInquiryListRequest {@link AdminInquiryListRequest}
   * @param result バリデーション結果
   * @return {@link AdminInquiryListGetResponse}
   * @throws GalleryException 以下のいずれかに該当する場合 ・管理者権限がない場合 ・リクエストパラメータが不正な場合
   */
  @Operation(summary = "管理者用お問い合わせ一覧取得", description = "全アカウントのお問い合わせ一覧を取得する")
  @ApiResponse(responseCode = "200", description = "取得成功")
  @ApiResponse(responseCode = "400", description = "リクエストパラメータ不正", content = @Content)
  @ApiResponse(responseCode = "403", description = "管理者権限がない", content = @Content)
  @RequireAdminAuthority
  @GetMapping(ApiRoutes.API_ADMIN_INQUIRIES)
  public ResponseEntity<AdminInquiryListGetResponse> getAdminInquiryList(
      @ParameterObject @ModelAttribute @Validated AdminInquiryListRequest adminInquiryListRequest,
      BindingResult result)
      throws GalleryException {

    if (result.hasErrors()) {
      ValidationErrorLogger.logFieldErrors(log, result);
      throw ErrorEnum.INVALID_INPUT.toException();
    }

    InquiryPageModel inquiryPageModel =
        inquiryService.getInquiryListForAdmin(InquiryListGetModel.from(adminInquiryListRequest));

    return ResponseEntity.ok(AdminInquiryListGetResponse.from(inquiryPageModel));
  }

  /**
   * 管理者用お問い合わせ詳細取得
   *
   * @param inquiryId ID
   * @return {@link AdminInquiryDetailGetResponse}
   * @throws GalleryException 以下のいずれかに該当する場合 ・管理者権限がない場合 ・お問い合わせが存在しない場合
   */
  @Operation(summary = "管理者用お問い合わせ詳細取得", description = "お問い合わせの詳細情報（返信を含む）を取得する")
  @ApiResponse(responseCode = "200", description = "取得成功")
  @ApiResponse(responseCode = "400", description = "お問い合わせが存在しない", content = @Content)
  @ApiResponse(responseCode = "403", description = "管理者権限がない", content = @Content)
  @RequireAdminAuthority
  @GetMapping(ApiRoutes.API_ADMIN_INQUIRY_DETAIL)
  public ResponseEntity<AdminInquiryDetailGetResponse> getAdminInquiryDetail(
      @PathVariable Long inquiryId) throws GalleryException {

    InquiryDetailModel detail = inquiryService.getInquiryDetailForAdmin(new InquiryId(inquiryId));

    return ResponseEntity.ok(AdminInquiryDetailGetResponse.from(detail));
  }

  /**
   * お問い合わせ返信登録
   *
   * @param inquiryId ID
   * @param request {@link InquiryReplyRequest}
   * @param result バリデーション結果
   * @return {@link InquiryReplyResponse}
   * @throws GalleryException 以下のいずれかに該当する場合 ・管理者権限がない場合 ・リクエストパラメータが不正な場合 ・お問い合わせが存在しない場合
   *     ・返信の登録に失敗した場合
   */
  @Operation(summary = "お問い合わせ返信登録", description = "お問い合わせに返信する")
  @ApiResponse(responseCode = "200", description = "登録成功")
  @ApiResponse(
      responseCode = "400",
      description = "リクエストパラメータ不正またはお問い合わせが存在しない",
      content = @Content)
  @ApiResponse(responseCode = "403", description = "管理者権限がない", content = @Content)
  @RequireAdminAuthority
  @PostMapping(ApiRoutes.API_ADMIN_INQUIRY_REPLY)
  public ResponseEntity<InquiryReplyResponse> replyToInquiry(
      @PathVariable Long inquiryId,
      @RequestBody @Validated InquiryReplyRequest request,
      BindingResult result)
      throws GalleryException {

    if (result.hasErrors()) {
      ValidationErrorLogger.logFieldErrors(log, result);
      throw ErrorEnum.INVALID_INPUT.toException();
    }

    AccountNo adminAccountNo = new AccountNo(sessionHelper.getAccountNo());
    ReplyNo replyNo =
        inquiryService.replyToInquiry(
            new InquiryId(inquiryId), adminAccountNo, new ReplyBody(request.getBody()));

    return ResponseEntity.ok(InquiryReplyResponse.of(MessageConst.REPLY_INQUIRY, replyNo.value()));
  }
}
