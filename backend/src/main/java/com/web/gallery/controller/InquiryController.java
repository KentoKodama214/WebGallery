package com.web.gallery.controller;

import com.web.gallery.constant.ApiRoutes;
import com.web.gallery.constant.MessageConst;
import com.web.gallery.controller.request.inquiry.InquiryListRequest;
import com.web.gallery.controller.request.inquiry.InquiryRegistRequest;
import com.web.gallery.controller.response.inquiry.InquiryDetailGetResponse;
import com.web.gallery.controller.response.inquiry.InquiryListGetResponse;
import com.web.gallery.controller.response.inquiry.InquiryRegistResponse;
import com.web.gallery.controller.response.inquiry.InquiryWithdrawalResponse;
import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.inquiry.InquiryNo;
import com.web.gallery.enumeration.ErrorEnum;
import com.web.gallery.exception.GalleryException;
import com.web.gallery.helper.SessionHelper;
import com.web.gallery.helper.ValidationErrorLogger;
import com.web.gallery.model.inquiry.InquiryDetailModel;
import com.web.gallery.model.inquiry.InquiryListGetModel;
import com.web.gallery.model.inquiry.InquiryPageModel;
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

/** お問い合わせに関するAPI通信を扱うControllerクラス */
@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "お問い合わせ", description = "お問い合わせに関するAPI")
@SecurityRequirement(name = "Bearer")
public class InquiryController {

  private final InquiryService inquiryService;
  private final SessionHelper sessionHelper;

  /**
   * お問い合わせ登録
   *
   * @param request {@link InquiryRegistRequest}
   * @param result バリデーション結果
   * @return {@link InquiryRegistResponse}
   * @throws GalleryException 以下のいずれかに該当する場合 ・リクエストパラメータが不正な場合 ・登録に失敗した場合
   */
  @Operation(summary = "お問い合わせ登録", description = "お問い合わせを新規登録する")
  @ApiResponse(responseCode = "200", description = "登録成功")
  @ApiResponse(responseCode = "400", description = "リクエストパラメータ不正", content = @Content)
  @PostMapping(ApiRoutes.API_INQUIRIES)
  public ResponseEntity<InquiryRegistResponse> registInquiry(
      @RequestBody @Validated InquiryRegistRequest request, BindingResult result)
      throws GalleryException {

    if (result.hasErrors()) {
      ValidationErrorLogger.logFieldErrors(log, result);
      throw ErrorEnum.INVALID_INPUT.toException();
    }

    AccountNo accountNo = new AccountNo(sessionHelper.getAccountNo());
    InquiryDetailModel requestDetail = InquiryDetailModel.from(request, accountNo);
    InquiryNo inquiryNo = inquiryService.registInquiry(requestDetail);

    return ResponseEntity.ok(
        InquiryRegistResponse.of(MessageConst.REGIST_INQUIRY, inquiryNo.value()));
  }

  /**
   * 自分のお問い合わせ一覧取得
   *
   * @param inquiryListRequest {@link InquiryListRequest}
   * @param result バリデーション結果
   * @return {@link InquiryListGetResponse}
   * @throws GalleryException リクエストパラメータが不正な場合
   */
  @Operation(summary = "お問い合わせ一覧取得", description = "自分のお問い合わせ一覧を取得する")
  @ApiResponse(responseCode = "200", description = "取得成功")
  @ApiResponse(responseCode = "400", description = "リクエストパラメータ不正", content = @Content)
  @GetMapping(ApiRoutes.API_INQUIRIES)
  public ResponseEntity<InquiryListGetResponse> getInquiryList(
      @ParameterObject @ModelAttribute @Validated InquiryListRequest inquiryListRequest,
      BindingResult result)
      throws GalleryException {

    if (result.hasErrors()) {
      ValidationErrorLogger.logFieldErrors(log, result);
      throw ErrorEnum.INVALID_INPUT.toException();
    }

    AccountNo accountNo = new AccountNo(sessionHelper.getAccountNo());
    InquiryPageModel inquiryPageModel =
        inquiryService.getInquiryList(InquiryListGetModel.from(inquiryListRequest, accountNo));

    return ResponseEntity.ok(InquiryListGetResponse.from(inquiryPageModel));
  }

  /**
   * 自分のお問い合わせ詳細取得
   *
   * <p>未読の返信が存在する場合、取得と同時に既読化する
   *
   * @param inquiryNo お問い合わせ番号
   * @return {@link InquiryDetailGetResponse}
   * @throws GalleryException お問い合わせが存在しない場合
   */
  @Operation(summary = "お問い合わせ詳細取得", description = "自分のお問い合わせの詳細情報（返信を含む）を取得する")
  @ApiResponse(responseCode = "200", description = "取得成功")
  @ApiResponse(responseCode = "400", description = "お問い合わせが存在しない", content = @Content)
  @GetMapping(ApiRoutes.API_INQUIRY_DETAIL)
  public ResponseEntity<InquiryDetailGetResponse> getInquiryDetail(@PathVariable Long inquiryNo)
      throws GalleryException {

    AccountNo accountNo = new AccountNo(sessionHelper.getAccountNo());
    InquiryDetailModel detail =
        inquiryService.getInquiryDetail(accountNo, new InquiryNo(inquiryNo));

    return ResponseEntity.ok(InquiryDetailGetResponse.from(detail));
  }

  /**
   * お問い合わせ取り下げ
   *
   * @param inquiryNo お問い合わせ番号
   * @return {@link InquiryWithdrawalResponse}
   * @throws GalleryException 以下のいずれかに該当する場合 ・お問い合わせが存在しない場合 ・取り下げに失敗した場合
   */
  @Operation(summary = "お問い合わせ取り下げ", description = "自分のお問い合わせを取り下げる")
  @ApiResponse(responseCode = "200", description = "取り下げ成功")
  @ApiResponse(responseCode = "400", description = "お問い合わせが存在しない", content = @Content)
  @PostMapping(ApiRoutes.API_INQUIRY_WITHDRAWAL)
  public ResponseEntity<InquiryWithdrawalResponse> withdrawInquiry(@PathVariable Long inquiryNo)
      throws GalleryException {

    AccountNo accountNo = new AccountNo(sessionHelper.getAccountNo());
    inquiryService.withdrawInquiry(accountNo, new InquiryNo(inquiryNo));

    return ResponseEntity.ok(InquiryWithdrawalResponse.of(MessageConst.WITHDRAW_INQUIRY));
  }
}
