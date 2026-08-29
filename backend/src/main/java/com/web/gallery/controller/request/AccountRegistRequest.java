package com.web.gallery.controller.request;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import com.web.gallery.enumeration.SexEnum;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * アカウント登録時のリクエストパラメータを保持するクラス
 */
@Schema(description = "アカウント登録リクエスト")
@Data
public class AccountRegistRequest {
	/**
	 * アカウントID
	 * <p>
	 * 半角英数8〜16桁、ブランクなし
	 */
	@Schema(description = "アカウントID（半角英数8〜16桁）", example = "testuser01")
	@NotBlank(message = "{validation.common.notBlank}")
	@Size(min = 8, max = 16, message = "{validation.common.min_max_length}")
	@Pattern(regexp = "[a-zA-Z0-9]{8,16}", message = "{validation.common.pattern}")
	private String accountId;

	/**
	 * アカウント名
	 * <p>
	 * ブランクなし
	 */
	@Schema(description = "アカウント名", example = "テストユーザー")
	@NotBlank(message = "{validation.common.notBlank}")
	@Size(max = 50, message = "{validation.common.max_length}")
	@Pattern(regexp = "[^　]+", message = "{validation.common.all_space}")
	private String accountName;

	/**
	 * パスワード
	 * <p>
	 * 半角英数8桁以上、ブランクなし
	 */
	@Schema(description = "パスワード（半角英数8桁以上）", example = "password01")
	@NotBlank(message = "{validation.common.notBlank}")
	@Size(min = 8, message = "{validation.common.min_length}")
	@Pattern(regexp = "[a-zA-Z0-9]{8,}", message = "{validation.common.pattern}")
	private String password;

	/**
	 * 生年月日
	 * <p>
	 * yyyy-mm-ddで、過去日付
	 */
	@Schema(description = "生年月日", example = "1990-01-01")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	@Past(message = "{validation.common.pastDate}")
	private LocalDate birthdate;

	/**
	 * 性別区分
	 * <p>
	 * {@link SexEnum}
	 */
	@Schema(description = "性別区分")
	private SexEnum sexKbn;

	/** 出身都道府県区分コード */
	@Schema(description = "出身都道府県区分コード", example = "Hokkaido")
	private String birthplacePrefectureKbnCode;

	/** 在住都道府県区分コード */
	@Schema(description = "在住都道府県区分コード", example = "Tokyo")
	private String residentPrefectureKbnCode;

	/** フリーメモ */
	@Schema(description = "フリーメモ", example = "よろしくお願いします")
	private String freeMemo;
}