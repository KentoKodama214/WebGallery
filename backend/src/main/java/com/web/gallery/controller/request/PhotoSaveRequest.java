package com.web.gallery.controller.request;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.web.gallery.enumeration.DirectionEnum;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * 写真保存時のリクエストパラメータを保持するクラス
 */
@Schema(description = "写真保存リクエスト")
@Data
public class PhotoSaveRequest {
	/** 写真番号 */
	@Schema(description = "写真番号（更新時のみ指定）", example = "1")
	@Positive(message = "{validation.common.positive}")
	private Long photoNo;

	/** お気に入り数 */
	@Schema(description = "お気に入り数")
	private Integer favoriteCount;

	/** お気に入り */
	@Schema(description = "お気に入り")
	private Boolean isFavorite;

	/** 撮影日時 */
	@Schema(description = "撮影日時", example = "2024-01-01T12:00:00")
	@Past(message = "{validation.common.pastDate}")
	private LocalDateTime photoAt;

	/** ロケーション番号 */
	@Schema(description = "ロケーション番号")
	private Long locationNo;

	/** 住所 */
	@Schema(description = "住所", example = "東京都渋谷区")
	private String address;

	/** 緯度 */
	@Schema(description = "緯度", example = "35.6812")
	private BigDecimal latitude;

	/** 経度 */
	@Schema(description = "経度", example = "139.7671")
	private BigDecimal longitude;

	/** ロケーション名 */
	@Schema(description = "ロケーション名", example = "渋谷スクランブル交差点")
	private String locationName;

	/** 画像ファイル */
	@Schema(description = "画像ファイル（新規登録時）")
	private MultipartFile imageFile;

	/** 画像ファイルパス */
	@Schema(description = "画像ファイルパス（更新時）")
	private String imageFilePath;

	/** 写真タイトル日本語名 */
	@Schema(description = "写真タイトル日本語名", example = "東京タワー")
	private String photoJapaneseTitle;

	/** 写真タイトル英語名 */
	@Schema(description = "写真タイトル英語名", example = "Tokyo Tower")
	private String photoEnglishTitle;

	/** キャプション */
	@Schema(description = "キャプション", example = "夕暮れの東京タワー")
	private String caption;

	/**
	 * 向き区分
	 * <p>
	 * {@link DirectionEnum}
	 */
	@Schema(description = "向き区分")
	@NotNull(message = "{validation.common.notBlank}")
	private DirectionEnum directionKbn;

	/** 焦点距離 */
	@Schema(description = "焦点距離（mm）", example = "50")
	@Positive(message = "{validation.common.positive}")
	private Integer focalLength;

	/** F値 */
	@Schema(description = "F値", example = "2.8")
	@Positive(message = "{validation.common.positive}")
	private BigDecimal fValue;

	/** シャッタースピード */
	@Schema(description = "シャッタースピード（秒）", example = "0.004")
	@Positive(message = "{validation.common.positive}")
	private BigDecimal shutterSpeed;

	/** ISO */
	@Schema(description = "ISO感度", example = "100")
	@Positive(message = "{validation.common.positive}")
	private Integer iso;

	/** 写真タグリスト */
	@Schema(description = "写真タグリスト")
	@Valid
	private List<PhotoTagSaveRequest> photoTagRegistRequestList;
}