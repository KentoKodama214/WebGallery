package com.web.gallery.presentation.controller.inquiry;

import com.web.gallery.application.model.inquiry.InquiryDetailModel;
import com.web.gallery.application.model.inquiry.InquiryPageModel;
import com.web.gallery.application.service.inquiry.InquiryService;
import com.web.gallery.domain.constant.ApiRoutes;
import com.web.gallery.domain.constant.MessageConst;
import com.web.gallery.domain.enumeration.ErrorEnum;
import com.web.gallery.domain.exception.GalleryException;
import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.inquiry.InquiryNo;
import com.web.gallery.infrastructure.security.SessionHelper;
import com.web.gallery.infrastructure.web.ValidationErrorLogger;
import com.web.gallery.presentation.converter.inquiry.InquiryConverter;
import com.web.gallery.presentation.request.inquiry.InquiryListRequest;
import com.web.gallery.presentation.request.inquiry.InquiryRegistRequest;
import com.web.gallery.presentation.response.inquiry.InquiryDetailGetResponse;
import com.web.gallery.presentation.response.inquiry.InquiryListGetResponse;
import com.web.gallery.presentation.response.inquiry.InquiryReadResponse;
import com.web.gallery.presentation.response.inquiry.InquiryRegistResponse;
import com.web.gallery.presentation.response.inquiry.InquiryWithdrawalResponse;
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
  private final InquiryConverter inquiryConverter;

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
    InquiryDetailModel requestDetail = inquiryConverter.toInquiryDetailModel(request, accountNo);
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
        inquiryService.getInquiryList(
            inquiryConverter.toInquiryListGetModel(inquiryListRequest, accountNo));

    return ResponseEntity.ok(InquiryListGetResponse.from(inquiryPageModel));
  }

  /**
   * 自分のお問い合わせ詳細取得
   *
   * <p>副作用を持たない参照専用のAPI。既読化は{@link #markInquiryAsRead}で別途行う
   *
   * @param inquiryNo お問い合わせ番号
   * @return {@link InquiryDetailGetResponse}
   * @throws GalleryException お問い合わせが存在しない場合
   */
  @Operation(
      summary = "お問い合わせ詳細取得",
      description = "自分のお問い合わせの詳細情報（返信を含む）を取得する。既読化は行わないため、既読にする場合は既読化APIを別途呼び出すこと")
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
   * 自分のお問い合わせ既読化
   *
   * <p>詳細取得（GET）で既読化すると、ブラウザ・プロキシのプリフェッチやリトライ、フロントの再取得で
   * 意図せず既読になってしまう。GETを安全なメソッドに保つため、状態を変える既読化はこのPOSTへ分離している
   *
   * @param inquiryNo お問い合わせ番号
   * @return {@link InquiryReadResponse}
   * @throws GalleryException 以下のいずれかに該当する場合 ・お問い合わせが存在しない場合 ・既読化に失敗した場合
   */
  @Operation(summary = "お問い合わせ既読化", description = "自分のお問い合わせを既読にする（既に既読の場合は何もしない）")
  @ApiResponse(responseCode = "200", description = "既読化成功")
  @ApiResponse(responseCode = "400", description = "お問い合わせが存在しない", content = @Content)
  @PostMapping(ApiRoutes.API_INQUIRY_READ)
  public ResponseEntity<InquiryReadResponse> markInquiryAsRead(@PathVariable Long inquiryNo)
      throws GalleryException {

    AccountNo accountNo = new AccountNo(sessionHelper.getAccountNo());
    inquiryService.markInquiryAsRead(accountNo, new InquiryNo(inquiryNo));

    return ResponseEntity.ok(InquiryReadResponse.of(MessageConst.MARK_INQUIRY_AS_READ));
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
