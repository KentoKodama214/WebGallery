package com.web.gallery.controller.response;

import java.util.Objects;
import java.util.Optional;

import org.springframework.http.HttpStatus;

import com.web.gallery.constant.Consts;
import com.web.gallery.constant.MessageConst;
import com.web.gallery.controller.request.PhotoSaveRequest;
import com.web.gallery.model.PhotoSaveResultModel;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 写真登録・編集のレスポンスパラメータを保持するクラス
 */
@Schema(description = "写真編集レスポンス")
@Data
@Builder
public class PhotoEditResponse {
	/** HTTPステータス */
	@Schema(description = "HTTPステータスコード", example = "200")
	private Integer httpStatus;

	/** 登録成功 */
	@Schema(description = "成功", example = "true")
	private Boolean isSuccess;

	/** メッセージ */
	@Schema(description = "メッセージ")
	private String message;

	/** 写真番号 */
	@Schema(description = "写真番号")
	private Long photoNo;

	/** 画像ファイルパス */
	@Schema(description = "画像ファイルパス")
	private String imageFilePath;

	/**
	 * 成功レスポンスを生成する
	 *
	 * @param	message			メッセージ
	 * @param	photoNo			写真番号
	 * @param	imageFilePath	画像ファイルパス
	 * @return					{@link PhotoEditResponse}
	 */
	public static PhotoEditResponse of(String message, Long photoNo, String imageFilePath) {
		return PhotoEditResponse.builder()
				.httpStatus(HttpStatus.OK.value())
				.isSuccess(true)
				.message(message)
				.photoNo(photoNo)
				.imageFilePath(imageFilePath)
				.build();
	}

	/**
	 * 写真保存の成功レスポンスを生成する<p>
	 * 新規登録でファイルアップロードされた場合は実際に保存されたファイルパス（パストラバーサル対策済み）を、
	 * それ以外の場合はリクエストの画像ファイルパスを設定する
	 *
	 * @param	photoSaveResultModel	{@link PhotoSaveResultModel}
	 * @param	photoSaveRequest		{@link PhotoSaveRequest}
	 * @return							{@link PhotoEditResponse}
	 */
	public static PhotoEditResponse of(PhotoSaveResultModel photoSaveResultModel, PhotoSaveRequest photoSaveRequest) {
		String savedImageFilePath = Objects.nonNull(photoSaveResultModel.getImageFilePath())
				? photoSaveResultModel.getImageFilePath().value()
				: Optional.ofNullable(photoSaveRequest.getImageFilePath()).orElse(Consts.STRING_EMPTY);

		return of(MessageConst.REGIST_PHOTO, photoSaveResultModel.getPhotoNo().value(), savedImageFilePath);
	}
}