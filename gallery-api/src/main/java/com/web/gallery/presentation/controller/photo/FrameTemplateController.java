package com.web.gallery.presentation.controller.photo;

import com.web.gallery.application.service.photo.FrameTemplateService;
import com.web.gallery.domain.constant.ApiRoutes;
import com.web.gallery.domain.constant.MessageConst;
import com.web.gallery.domain.enumeration.ErrorEnum;
import com.web.gallery.domain.exception.GalleryException;
import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.photo.FrameTemplateNo;
import com.web.gallery.infrastructure.security.SessionHelper;
import com.web.gallery.infrastructure.web.ValidationErrorLogger;
import com.web.gallery.presentation.converter.photo.FrameTemplateConverter;
import com.web.gallery.presentation.request.photo.FrameTemplateSaveRequest;
import com.web.gallery.presentation.response.photo.FrameTemplateEditResponse;
import com.web.gallery.presentation.response.photo.FrameTemplateItemResponse;
import com.web.gallery.presentation.response.photo.FrameTemplateListGetResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 額縁テンプレートに関するAPI通信を扱うControllerクラス
 *
 * <p>すべてのAPIは認証必須かつ本人のみが操作できる。額縁テンプレートはポートフォリオの額縁型テンプレートで選択して使い、 選択時に額縁定義をポートフォリオへコピーする
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "額縁テンプレート", description = "ポートフォリオの額縁型テンプレートで使う額縁テンプレートに関するAPI")
@SecurityRequirement(name = "Bearer")
public class FrameTemplateController {

  private final FrameTemplateService frameTemplateService;
  private final SessionHelper sessionHelper;
  private final FrameTemplateConverter frameTemplateConverter;

  /**
   * 額縁テンプレート一覧取得
   *
   * @param accountId アカウントID
   * @return {@link FrameTemplateListGetResponse}
   * @throws GalleryException 認証ユーザーと異なるアカウントIDの場合
   */
  @Operation(summary = "額縁テンプレート一覧取得", description = "本人の額縁テンプレート一覧を、額縁テンプレート番号の昇順に取得する")
  @ApiResponse(responseCode = "200", description = "取得成功")
  @ApiResponse(responseCode = "403", description = "認証ユーザーと異なるアカウントIDを指定", content = @Content)
  @GetMapping(ApiRoutes.API_FRAME_TEMPLATES)
  public ResponseEntity<FrameTemplateListGetResponse> getFrameTemplateList(
      @PathVariable String accountId) throws GalleryException {
    verifyOwner(accountId);

    return ResponseEntity.ok(
        FrameTemplateListGetResponse.from(
            frameTemplateService.getFrameTemplateList(
                new AccountNo(sessionHelper.getAccountNo()))));
  }

  /**
   * 額縁テンプレート詳細取得
   *
   * @param accountId アカウントID
   * @param frameTemplateNo 額縁テンプレート番号
   * @return {@link FrameTemplateItemResponse}
   * @throws GalleryException 以下のいずれかに該当する場合 ・認証ユーザーと異なるアカウントIDの場合 ・額縁テンプレートが存在しない場合
   */
  @Operation(summary = "額縁テンプレート詳細取得", description = "本人の額縁テンプレートを取得する")
  @ApiResponse(responseCode = "200", description = "取得成功")
  @ApiResponse(responseCode = "403", description = "認証ユーザーと異なるアカウントIDを指定", content = @Content)
  @ApiResponse(responseCode = "404", description = "額縁テンプレートが存在しない", content = @Content)
  @GetMapping(ApiRoutes.API_FRAME_TEMPLATE_DETAIL)
  public ResponseEntity<FrameTemplateItemResponse> getFrameTemplate(
      @PathVariable String accountId, @PathVariable Long frameTemplateNo) throws GalleryException {
    verifyOwner(accountId);

    return ResponseEntity.ok(
        FrameTemplateItemResponse.from(
            frameTemplateService.getFrameTemplate(
                new AccountNo(sessionHelper.getAccountNo()), toFrameTemplateNo(frameTemplateNo))));
  }

  /**
   * 額縁テンプレート登録
   *
   * @param accountId アカウントID
   * @param request {@link FrameTemplateSaveRequest}
   * @param result バリデーション結果
   * @return {@link FrameTemplateEditResponse}
   * @throws GalleryException 以下のいずれかに該当する場合 ・認証ユーザーと異なるアカウントIDの場合 ・リクエストパラメータが不正な場合 ・額縁定義が不正な場合
   *     ・作成数が上限に達している場合 ・登録に失敗した場合
   */
  @Operation(summary = "額縁テンプレート登録", description = "額縁テンプレートを新規登録する")
  @ApiResponse(responseCode = "200", description = "登録成功")
  @ApiResponse(
      responseCode = "400",
      description = "リクエストパラメータ・額縁定義が不正、または作成数が上限に達している",
      content = @Content)
  @ApiResponse(responseCode = "403", description = "認証ユーザーと異なるアカウントIDを指定", content = @Content)
  @ApiResponse(responseCode = "409", description = "登録失敗", content = @Content)
  @PostMapping(ApiRoutes.API_FRAME_TEMPLATES)
  public ResponseEntity<FrameTemplateEditResponse> registFrameTemplate(
      @PathVariable String accountId,
      @RequestBody @Validated FrameTemplateSaveRequest request,
      BindingResult result)
      throws GalleryException {
    verifyOwner(accountId);
    verifyNoBindingErrors(result);

    FrameTemplateNo frameTemplateNo =
        frameTemplateService.registFrameTemplate(
            frameTemplateConverter.toFrameTemplateModel(
                request, new AccountNo(sessionHelper.getAccountNo())));

    return ResponseEntity.ok(
        FrameTemplateEditResponse.of(MessageConst.REGIST_FRAME_TEMPLATE, frameTemplateNo.value()));
  }

  /**
   * 額縁テンプレート更新
   *
   * @param accountId アカウントID
   * @param frameTemplateNo 額縁テンプレート番号
   * @param request {@link FrameTemplateSaveRequest}
   * @param result バリデーション結果
   * @return {@link FrameTemplateEditResponse}
   * @throws GalleryException 以下のいずれかに該当する場合 ・認証ユーザーと異なるアカウントIDの場合 ・リクエストパラメータが不正な場合
   *     ・額縁テンプレートが存在しない場合 ・額縁定義が不正な場合 ・更新に失敗した場合
   */
  @Operation(
      summary = "額縁テンプレート更新",
      description = "額縁テンプレートを更新する。この額縁テンプレートを選択済みのポートフォリオは額縁定義をコピーして保持しているため、更新は反映されない")
  @ApiResponse(responseCode = "200", description = "更新成功")
  @ApiResponse(responseCode = "400", description = "リクエストパラメータ・額縁定義が不正", content = @Content)
  @ApiResponse(responseCode = "403", description = "認証ユーザーと異なるアカウントIDを指定", content = @Content)
  @ApiResponse(responseCode = "404", description = "額縁テンプレートが存在しない", content = @Content)
  @ApiResponse(responseCode = "409", description = "更新失敗", content = @Content)
  @PutMapping(ApiRoutes.API_FRAME_TEMPLATE_DETAIL)
  public ResponseEntity<FrameTemplateEditResponse> updateFrameTemplate(
      @PathVariable String accountId,
      @PathVariable Long frameTemplateNo,
      @RequestBody @Validated FrameTemplateSaveRequest request,
      BindingResult result)
      throws GalleryException {
    verifyOwner(accountId);
    verifyNoBindingErrors(result);

    frameTemplateService.updateFrameTemplate(
        frameTemplateConverter.toFrameTemplateModel(
            request,
            new AccountNo(sessionHelper.getAccountNo()),
            toFrameTemplateNo(frameTemplateNo)));

    return ResponseEntity.ok(
        FrameTemplateEditResponse.of(MessageConst.UPDATE_FRAME_TEMPLATE, frameTemplateNo));
  }

  /**
   * 額縁テンプレート削除
   *
   * @param accountId アカウントID
   * @param frameTemplateNo 額縁テンプレート番号
   * @return {@link FrameTemplateEditResponse}
   * @throws GalleryException 以下のいずれかに該当する場合 ・認証ユーザーと異なるアカウントIDの場合 ・額縁テンプレートが存在しない場合
   */
  @Operation(
      summary = "額縁テンプレート削除",
      description = "額縁テンプレートを削除する。この額縁テンプレートを選択済みのポートフォリオは額縁定義をコピーして保持しているため、影響しない")
  @ApiResponse(responseCode = "200", description = "削除成功")
  @ApiResponse(responseCode = "403", description = "認証ユーザーと異なるアカウントIDを指定", content = @Content)
  @ApiResponse(responseCode = "404", description = "額縁テンプレートが存在しない", content = @Content)
  @DeleteMapping(ApiRoutes.API_FRAME_TEMPLATE_DETAIL)
  public ResponseEntity<FrameTemplateEditResponse> deleteFrameTemplate(
      @PathVariable String accountId, @PathVariable Long frameTemplateNo) throws GalleryException {
    verifyOwner(accountId);

    frameTemplateService.deleteFrameTemplate(
        new AccountNo(sessionHelper.getAccountNo()), toFrameTemplateNo(frameTemplateNo));

    return ResponseEntity.ok(
        FrameTemplateEditResponse.of(MessageConst.DELETE_FRAME_TEMPLATE, frameTemplateNo));
  }

  /**
   * パスパラメータのアカウントIDが認証ユーザー本人のものかを検証する
   *
   * @param accountId パスパラメータのアカウントID
   * @throws GalleryException 認証ユーザーと異なるアカウントIDの場合
   */
  private void verifyOwner(String accountId) throws GalleryException {
    if (!accountId.equals(sessionHelper.getAccountId())) {
      throw ErrorEnum.NOT_AUTHORIZED_TO_EDIT_FRAME_TEMPLATE.toException();
    }
  }

  /**
   * リクエストボディのバリデーション結果にエラーがないかを検証する
   *
   * @param result バリデーション結果
   * @throws GalleryException バリデーションエラーがある場合
   */
  private void verifyNoBindingErrors(BindingResult result) throws GalleryException {
    if (result.hasErrors()) {
      ValidationErrorLogger.logFieldErrors(log, result);
      throw ErrorEnum.INVALID_INPUT.toException();
    }
  }

  /**
   * パスパラメータの額縁テンプレート番号を値オブジェクトに変換する
   *
   * @param frameTemplateNo パスパラメータの額縁テンプレート番号
   * @return {@link FrameTemplateNo}
   * @throws GalleryException 0以下の場合（値オブジェクトの生成で例外となりシステムエラー扱いになるのを避ける）
   */
  private FrameTemplateNo toFrameTemplateNo(Long frameTemplateNo) throws GalleryException {
    if (frameTemplateNo == null || frameTemplateNo <= 0) {
      throw ErrorEnum.INVALID_INPUT.toException();
    }
    return new FrameTemplateNo(frameTemplateNo);
  }
}
