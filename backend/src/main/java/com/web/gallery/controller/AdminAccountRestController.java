package com.web.gallery.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import com.web.gallery.annotation.RequireAdminAuthority;
import com.web.gallery.constant.ApiRoutes;
import com.web.gallery.constant.MessageConst;
import com.web.gallery.controller.response.AdminAccountListItemResponse;
import com.web.gallery.controller.response.AdminAccountLockResponse;
import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.exception.GalleryException;
import com.web.gallery.service.AccountService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * 管理者用アカウント管理に関するAPI通信を扱うRestControllerクラス
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "管理者アカウント管理", description = "管理者用アカウント管理に関するAPI")
@SecurityRequirement(name = "Bearer")
public class AdminAccountRestController {
	private final AccountService accountService;

	/**
	 * 管理者用アカウント一覧取得
	 *
	 * @return	{@link AdminAccountListItemResponse}のリスト
	 * @throws	GalleryException	管理者権限がない場合
	 */
	@Operation(summary = "管理者用アカウント一覧取得", description = "削除済みを含む全アカウントの一覧を取得する")
	@ApiResponse(responseCode = "200", description = "取得成功")
	@ApiResponse(responseCode = "403", description = "管理者権限がない", content = @Content)
	@RequireAdminAuthority
	@GetMapping(ApiRoutes.API_ADMIN_ACCOUNTS)
	public ResponseEntity<List<AdminAccountListItemResponse>> getAdminAccountList()
			throws GalleryException {

		List<AdminAccountListItemResponse> responseList = accountService.getAccountListForAdmin().stream()
				.map(AdminAccountListItemResponse::from)
				.toList();

		return ResponseEntity.ok(responseList);
	}

	/**
	 * アカウントロック解除
	 *
	 * @param	accountNo	アカウント番号
	 * @return				{@link AdminAccountLockResponse}
	 * @throws	GalleryException	以下のいずれかに該当する場合
	 *                          	・管理者権限がない場合
	 *                          	・更新に失敗した場合
	 */
	@Operation(summary = "アカウントロック解除", description = "指定したアカウントのロックを解除する")
	@ApiResponse(responseCode = "200", description = "ロック解除成功")
	@ApiResponse(responseCode = "403", description = "管理者権限がない", content = @Content)
	@RequireAdminAuthority
	@PutMapping(ApiRoutes.API_ADMIN_ACCOUNT_UNLOCK)
	public ResponseEntity<AdminAccountLockResponse> unlockAccount(
			@PathVariable Long accountNo) throws GalleryException {

		accountService.unlockAccount(new AccountNo(accountNo));

		return ResponseEntity.ok(AdminAccountLockResponse.of(MessageConst.UNLOCK_ACCOUNT));
	}

	/**
	 * アカウント強制ロック
	 *
	 * @param	accountNo	アカウント番号
	 * @return				{@link AdminAccountLockResponse}
	 * @throws	GalleryException	以下のいずれかに該当する場合
	 *                          	・管理者権限がない場合
	 *                          	・更新に失敗した場合
	 */
	@Operation(summary = "アカウント強制ロック", description = "指定したアカウントを強制的にロックする")
	@ApiResponse(responseCode = "200", description = "ロック成功")
	@ApiResponse(responseCode = "403", description = "管理者権限がない", content = @Content)
	@RequireAdminAuthority
	@PutMapping(ApiRoutes.API_ADMIN_ACCOUNT_LOCK)
	public ResponseEntity<AdminAccountLockResponse> lockAccount(
			@PathVariable Long accountNo) throws GalleryException {

		accountService.lockAccount(new AccountNo(accountNo));

		return ResponseEntity.ok(AdminAccountLockResponse.of(MessageConst.LOCK_ACCOUNT));
	}
}
