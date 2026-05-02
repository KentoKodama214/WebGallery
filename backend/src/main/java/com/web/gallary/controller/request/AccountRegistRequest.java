package com.web.gallary.controller.request;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import com.web.gallary.enumuration.SexEnum;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * アカウント登録時のリクエストパラメータを保持するクラス
 */
@Data
public class AccountRegistRequest {
	/** 
	 * アカウントID
	 * <p>
	 * 半角英数8〜16桁、ブランクなし
	 */
	@NotBlank(message = "{validation.common.notBlank}")
	@Size(min = 8, max = 16, message = "{validation.common.min_max_length}")
	@Pattern(regexp = "[a-zA-Z0-9]{8,16}", message = "{validation.common.pattern}")
	private String accountId;
	
	/** 
	 * アカウント名
	 * <p>
	 * ブランクなし
	 */
	@NotBlank(message = "{validation.common.notBlank}")
	@Pattern(regexp = "[^　]+", message = "{validation.common.all_space}")
	private String accountName;

	/** 
	 * パスワード
	 * <p>
	 * 半角英数8桁以上、ブランクなし
	 */
	@NotBlank(message = "{validation.common.notBlank}")
	@Size(min = 8, message = "{validation.common.min_length}")
	@Pattern(regexp = "[a-zA-Z0-9]{8,}", message = "{validation.common.pattern}")
	private String password;
	
	/** 
	 * 生年月日
	 * <p>
	 * yyyy-mm-ddで、過去日付
	 */
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	@Past(message = "{validation.common.pastDate}")
	private LocalDate birthdate;

	/** 
	 * 性別区分
	 * <p>
	 * {@link SexEnum}
	 */
	private SexEnum sexKbn;

	/** 出身都道府県区分コード */
	private String birthplacePrefectureKbnCode;

	/** 在住都道府県区分コード */
	private String residentPrefectureKbnCode;

	/** フリーメモ */
	private String freeMemo;
}