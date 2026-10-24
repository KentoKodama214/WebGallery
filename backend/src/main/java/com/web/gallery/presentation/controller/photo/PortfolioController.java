package com.web.gallery.presentation.controller.photo;

import com.web.gallery.application.model.photo.PortfolioDetailModel;
import com.web.gallery.application.model.photo.PortfolioModelList;
import com.web.gallery.application.model.photo.PortfolioPhotoPageModel;
import com.web.gallery.application.service.photo.PortfolioService;
import com.web.gallery.domain.constant.ApiRoutes;
import com.web.gallery.domain.constant.MessageConst;
import com.web.gallery.domain.enumeration.ErrorEnum;
import com.web.gallery.domain.enumeration.PortfolioStatusEnum;
import com.web.gallery.domain.exception.GalleryException;
import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.photo.PortfolioNo;
import com.web.gallery.infrastructure.security.SessionHelper;
import com.web.gallery.infrastructure.web.ValidationErrorLogger;
import com.web.gallery.presentation.converter.photo.PortfolioConverter;
import com.web.gallery.presentation.request.photo.PortfolioListRequest;
import com.web.gallery.presentation.request.photo.PortfolioPhotoSearchRequest;
import com.web.gallery.presentation.request.photo.PortfolioSaveRequest;
import com.web.gallery.presentation.request.photo.PortfolioStatusUpdateRequest;
import com.web.gallery.presentation.response.photo.PortfolioDetailGetResponse;
import com.web.gallery.presentation.response.photo.PortfolioEditResponse;
import com.web.gallery.presentation.response.photo.PortfolioListGetResponse;
import com.web.gallery.presentation.response.photo.PortfolioPhotoSearchGetResponse;
import com.web.gallery.presentation.response.photo.PortfolioTemplateListGetResponse;
import com.web.gallery.presentation.response.photo.PortfolioUpperLimitResponse;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * ポートフォリオの設定に関するAPI通信を扱うControllerクラス
 *
 * <p>すべてのAPIは認証必須かつ本人のみが操作できる。公開ページ向けの公開中ポートフォリオの参照は、このAPIではなく 別途用意する表示用のAPIで行う
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "ポートフォリオ", description = "ポートフォリオの設定に関するAPI")
@SecurityRequirement(name = "Bearer")
public class PortfolioController {

  private final PortfolioService portfolioService;
  private final SessionHelper sessionHelper;
  private final PortfolioConverter portfolioConverter;

  /**
   * ポートフォリオ一覧取得
   *
   * @param accountId アカウントID
   * @param request {@link PortfolioListRequest}（テーマ・タグの絞り込み条件）
   * @param result バリデーション結果
   * @return {@link PortfolioListGetResponse}
   * @throws GalleryException 以下のいずれかに該当する場合 ・認証ユーザーと異なるアカウントIDの場合 ・リクエストパラメータが不正な場合
   */
  @Operation(
      summary = "ポートフォリオ一覧取得",
      description = "本人のポートフォリオ一覧（下書きを含む）を、テーマ・タグ名の部分一致で絞り込み、更新日時の新しい順に取得する")
  @ApiResponse(responseCode = "200", description = "取得成功")
  @ApiResponse(responseCode = "400", description = "リクエストパラメータ不正", content = @Content)
  @ApiResponse(responseCode = "403", description = "認証ユーザーと異なるアカウントIDを指定", content = @Content)
  @GetMapping(ApiRoutes.API_PORTFOLIOS)
  public ResponseEntity<PortfolioListGetResponse> getPortfolioList(
      @PathVariable String accountId,
      @ParameterObject @ModelAttribute @Validated PortfolioListRequest request,
      BindingResult result)
      throws GalleryException {
    verifyOwner(accountId);
    verifyNoBindingErrors(result);

    PortfolioModelList portfolioModelList =
        portfolioService.getPortfolioList(
            portfolioConverter.toPortfolioListGetModel(
                request, new AccountNo(sessionHelper.getAccountNo())));

    return ResponseEntity.ok(PortfolioListGetResponse.from(portfolioModelList));
  }

  /**
   * ポートフォリオ作成上限チェック
   *
   * @param accountId アカウントID
   * @return {@link PortfolioUpperLimitResponse}
   * @throws GalleryException 認証ユーザーと異なるアカウントIDの場合
   */
  @Operation(
      summary = "ポートフォリオ作成上限チェック",
      description = "作成済みのポートフォリオ（下書きを含む）の件数が、権限区分ごとの上限に達しているかをチェックする")
  @ApiResponse(responseCode = "200", description = "チェック成功")
  @ApiResponse(responseCode = "403", description = "認証ユーザーと異なるアカウントIDを指定", content = @Content)
  @GetMapping(ApiRoutes.API_PORTFOLIO_UPPER_LIMIT)
  public ResponseEntity<PortfolioUpperLimitResponse> getPortfolioUpperLimit(
      @PathVariable String accountId) throws GalleryException {
    verifyOwner(accountId);

    return ResponseEntity.ok(
        PortfolioUpperLimitResponse.of(
            portfolioService.isReachedUpperLimit(new AccountNo(sessionHelper.getAccountNo()))));
  }

  /**
   * ポートフォリオ用の写真検索
   *
   * @param accountId アカウントID
   * @param request {@link PortfolioPhotoSearchRequest}
   * @param result バリデーション結果
   * @return {@link PortfolioPhotoSearchGetResponse}
   * @throws GalleryException 以下のいずれかに該当する場合 ・認証ユーザーと異なるアカウントIDの場合 ・リクエストパラメータが不正な場合
   */
  @Operation(
      summary = "ポートフォリオ用の写真検索",
      description =
          "ポートフォリオに設定する写真を、本人の写真からタイトル・撮影場所の名前・タグ名の部分一致で検索する（撮影日時の新しい順）。"
              + "非公開の撮影場所も検索対象とするため、本人のみが利用できる")
  @ApiResponse(responseCode = "200", description = "取得成功")
  @ApiResponse(responseCode = "400", description = "リクエストパラメータ不正", content = @Content)
  @ApiResponse(responseCode = "403", description = "認証ユーザーと異なるアカウントIDを指定", content = @Content)
  @GetMapping(ApiRoutes.API_PORTFOLIO_PHOTOS)
  public ResponseEntity<PortfolioPhotoSearchGetResponse> searchPortfolioPhotos(
      @PathVariable String accountId,
      @ParameterObject @ModelAttribute @Validated PortfolioPhotoSearchRequest request,
      BindingResult result)
      throws GalleryException {
    verifyOwner(accountId);
    verifyNoBindingErrors(result);

    PortfolioPhotoPageModel pageModel =
        portfolioService.searchPhotos(
            portfolioConverter.toPortfolioPhotoSearchModel(
                request, new AccountNo(sessionHelper.getAccountNo())));

    return ResponseEntity.ok(PortfolioPhotoSearchGetResponse.from(pageModel));
  }

  /**
   * ポートフォリオ詳細取得
   *
   * @param accountId アカウントID
   * @param portfolioNo ポートフォリオ番号
   * @return {@link PortfolioDetailGetResponse}
   * @throws GalleryException 以下のいずれかに該当する場合 ・認証ユーザーと異なるアカウントIDの場合 ・ポートフォリオが存在しない場合
   */
  @Operation(summary = "ポートフォリオ詳細取得", description = "本人のポートフォリオの詳細情報（写真・タグを含む）を取得する")
  @ApiResponse(responseCode = "200", description = "取得成功")
  @ApiResponse(responseCode = "403", description = "認証ユーザーと異なるアカウントIDを指定", content = @Content)
  @ApiResponse(responseCode = "404", description = "ポートフォリオが存在しない", content = @Content)
  @GetMapping(ApiRoutes.API_PORTFOLIO_DETAIL)
  public ResponseEntity<PortfolioDetailGetResponse> getPortfolioDetail(
      @PathVariable String accountId, @PathVariable Long portfolioNo) throws GalleryException {
    verifyOwner(accountId);

    PortfolioDetailModel detail =
        portfolioService.getPortfolioDetail(
            new AccountNo(sessionHelper.getAccountNo()), toPortfolioNo(portfolioNo));

    return ResponseEntity.ok(PortfolioDetailGetResponse.from(detail));
  }

  /**
   * ポートフォリオ登録
   *
   * @param accountId アカウントID
   * @param request {@link PortfolioSaveRequest}
   * @param result バリデーション結果
   * @return {@link PortfolioEditResponse}
   * @throws GalleryException 以下のいずれかに該当する場合 ・認証ユーザーと異なるアカウントIDの場合 ・リクエストパラメータが不正な場合
   *     ・テンプレートの設定内容が不正な場合 ・設定できない写真が含まれる場合 ・登録に失敗した場合
   */
  @Operation(summary = "ポートフォリオ登録", description = "ポートフォリオを下書きとして新規登録する")
  @ApiResponse(responseCode = "200", description = "登録成功")
  @ApiResponse(
      responseCode = "400",
      description = "リクエストパラメータ・テンプレートの設定内容が不正、または設定できない写真が含まれる",
      content = @Content)
  @ApiResponse(responseCode = "403", description = "認証ユーザーと異なるアカウントIDを指定", content = @Content)
  @ApiResponse(responseCode = "409", description = "登録失敗", content = @Content)
  @PostMapping(ApiRoutes.API_PORTFOLIOS)
  public ResponseEntity<PortfolioEditResponse> registPortfolio(
      @PathVariable String accountId,
      @RequestBody @Validated PortfolioSaveRequest request,
      BindingResult result)
      throws GalleryException {
    verifyOwner(accountId);
    verifyNoBindingErrors(result);

    PortfolioNo portfolioNo =
        portfolioService.registPortfolio(
            portfolioConverter.toPortfolioDetailModel(
                request, new AccountNo(sessionHelper.getAccountNo())));

    return ResponseEntity.ok(
        PortfolioEditResponse.of(MessageConst.REGIST_PORTFOLIO, portfolioNo.value()));
  }

  /**
   * ポートフォリオ更新
   *
   * @param accountId アカウントID
   * @param portfolioNo ポートフォリオ番号
   * @param request {@link PortfolioSaveRequest}
   * @param result バリデーション結果
   * @return {@link PortfolioEditResponse}
   * @throws GalleryException 以下のいずれかに該当する場合 ・認証ユーザーと異なるアカウントIDの場合 ・リクエストパラメータが不正な場合
   *     ・ポートフォリオが存在しない場合 ・テンプレートの設定内容が不正な場合 ・設定できない写真が含まれる場合 ・公開中で、更新後の内容が公開できない場合 ・更新に失敗した場合
   */
  @Operation(
      summary = "ポートフォリオ更新",
      description = "ポートフォリオの内容を更新する。公開状態は変更しない。公開中の場合は、更新後の内容が公開できる状態である必要がある")
  @ApiResponse(responseCode = "200", description = "更新成功")
  @ApiResponse(
      responseCode = "400",
      description = "リクエストパラメータ・テンプレートの設定内容が不正、設定できない写真が含まれる、または公開中で公開に必要な設定が不足している",
      content = @Content)
  @ApiResponse(responseCode = "403", description = "認証ユーザーと異なるアカウントIDを指定", content = @Content)
  @ApiResponse(responseCode = "404", description = "ポートフォリオが存在しない", content = @Content)
  @ApiResponse(responseCode = "409", description = "更新失敗", content = @Content)
  @PutMapping(ApiRoutes.API_PORTFOLIO_DETAIL)
  public ResponseEntity<PortfolioEditResponse> updatePortfolio(
      @PathVariable String accountId,
      @PathVariable Long portfolioNo,
      @RequestBody @Validated PortfolioSaveRequest request,
      BindingResult result)
      throws GalleryException {
    verifyOwner(accountId);
    verifyNoBindingErrors(result);

    portfolioService.updatePortfolio(
        portfolioConverter.toPortfolioDetailModel(
            request, new AccountNo(sessionHelper.getAccountNo()), toPortfolioNo(portfolioNo)));

    return ResponseEntity.ok(PortfolioEditResponse.of(MessageConst.UPDATE_PORTFOLIO, portfolioNo));
  }

  /**
   * ポートフォリオ公開状態変更
   *
   * @param accountId アカウントID
   * @param portfolioNo ポートフォリオ番号
   * @param request {@link PortfolioStatusUpdateRequest}
   * @param result バリデーション結果
   * @return {@link PortfolioEditResponse}
   * @throws GalleryException 以下のいずれかに該当する場合 ・認証ユーザーと異なるアカウントIDの場合 ・リクエストパラメータが不正な場合
   *     ・ポートフォリオが存在しない場合 ・公開に必要な設定が不足している場合 ・更新に失敗した場合
   */
  @Operation(summary = "ポートフォリオ公開状態変更", description = "ポートフォリオを公開する、または下書きに戻す")
  @ApiResponse(responseCode = "200", description = "変更成功")
  @ApiResponse(
      responseCode = "400",
      description = "リクエストパラメータが不正、または公開に必要な設定が不足している",
      content = @Content)
  @ApiResponse(responseCode = "403", description = "認証ユーザーと異なるアカウントIDを指定", content = @Content)
  @ApiResponse(responseCode = "404", description = "ポートフォリオが存在しない", content = @Content)
  @ApiResponse(responseCode = "409", description = "更新失敗", content = @Content)
  @PutMapping(ApiRoutes.API_PORTFOLIO_STATUS)
  public ResponseEntity<PortfolioEditResponse> updatePortfolioStatus(
      @PathVariable String accountId,
      @PathVariable Long portfolioNo,
      @RequestBody @Validated PortfolioStatusUpdateRequest request,
      BindingResult result)
      throws GalleryException {
    verifyOwner(accountId);
    verifyNoBindingErrors(result);

    AccountNo accountNo = new AccountNo(sessionHelper.getAccountNo());
    if (request.getStatusKbn() == PortfolioStatusEnum.PUBLISHED) {
      portfolioService.publishPortfolio(accountNo, toPortfolioNo(portfolioNo));
      return ResponseEntity.ok(
          PortfolioEditResponse.of(MessageConst.PUBLISH_PORTFOLIO, portfolioNo));
    }

    portfolioService.unpublishPortfolio(accountNo, toPortfolioNo(portfolioNo));
    return ResponseEntity.ok(
        PortfolioEditResponse.of(MessageConst.UNPUBLISH_PORTFOLIO, portfolioNo));
  }

  /**
   * ポートフォリオ削除
   *
   * @param accountId アカウントID
   * @param portfolioNo ポートフォリオ番号
   * @return {@link PortfolioEditResponse}
   * @throws GalleryException 以下のいずれかに該当する場合 ・認証ユーザーと異なるアカウントIDの場合 ・ポートフォリオが存在しない場合
   */
  @Operation(summary = "ポートフォリオ削除", description = "ポートフォリオを削除する")
  @ApiResponse(responseCode = "200", description = "削除成功")
  @ApiResponse(responseCode = "403", description = "認証ユーザーと異なるアカウントIDを指定", content = @Content)
  @ApiResponse(responseCode = "404", description = "ポートフォリオが存在しない", content = @Content)
  @DeleteMapping(ApiRoutes.API_PORTFOLIO_DETAIL)
  public ResponseEntity<PortfolioEditResponse> deletePortfolio(
      @PathVariable String accountId, @PathVariable Long portfolioNo) throws GalleryException {
    verifyOwner(accountId);

    portfolioService.deletePortfolio(
        new AccountNo(sessionHelper.getAccountNo()), toPortfolioNo(portfolioNo));

    return ResponseEntity.ok(PortfolioEditResponse.of(MessageConst.DELETE_PORTFOLIO, portfolioNo));
  }

  /**
   * ポートフォリオテンプレート一覧取得
   *
   * @return {@link PortfolioTemplateListGetResponse}
   */
  @Operation(summary = "ポートフォリオテンプレート一覧取得", description = "選択できるテンプレートと、グリッド型テンプレートの行パターンの一覧を取得する")
  @ApiResponse(responseCode = "200", description = "取得成功")
  @GetMapping(ApiRoutes.API_PORTFOLIO_TEMPLATES)
  public ResponseEntity<PortfolioTemplateListGetResponse> getPortfolioTemplateList() {
    return ResponseEntity.ok(PortfolioTemplateListGetResponse.of());
  }

  /**
   * パスパラメータのアカウントIDが認証ユーザー本人のものかを検証する
   *
   * @param accountId パスパラメータのアカウントID
   * @throws GalleryException 認証ユーザーと異なるアカウントIDの場合
   */
  private void verifyOwner(String accountId) throws GalleryException {
    if (!accountId.equals(sessionHelper.getAccountId())) {
      throw ErrorEnum.NOT_AUTHORIZED_TO_EDIT_PORTFOLIO.toException();
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
   * パスパラメータのポートフォリオ番号を値オブジェクトに変換する
   *
   * @param portfolioNo パスパラメータのポートフォリオ番号
   * @return {@link PortfolioNo}
   * @throws GalleryException 0以下の場合（値オブジェクトの生成で例外となりシステムエラー扱いになるのを避ける）
   */
  private PortfolioNo toPortfolioNo(Long portfolioNo) throws GalleryException {
    if (portfolioNo == null || portfolioNo <= 0) {
      throw ErrorEnum.INVALID_INPUT.toException();
    }
    return new PortfolioNo(portfolioNo);
  }
}
