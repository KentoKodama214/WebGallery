package com.web.gallery.constant;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * デフォルト値を管理するクラス
 */
public final class Consts {
	// String
	/** 空文字（""） */
	public static final String STRING_EMPTY = "";
	/** 半角スペース */
	public static final String HALF_SPACE = " ";
	/** 全角スペース */
	public static final String FULL_SPACE = "　";
	/** none */
	public static final String STRING_NONE = "none";
	/** mini-user */
	public static final String AUTHORITY_MINI = "mini-user";
	/** normal-user */
	public static final String AUTHORITY_NORMAL = "normal-user";
	/** special-user */
	public static final String AUTHORITY_SPECIAL = "special-user";
	/** administrator */
	public static final String ADMINISTRATOR = "administrator";
	/** prefecture */
	public static final String PREFECTURE = "prefecture";
	
	// ZoneOffset
	public static final ZoneOffset JST = ZoneOffset.ofHours(9);
	
	// LocalDate
	/** 1900-01-01 */
	public static final LocalDate MIN_LOCAL_DATE = LocalDate.of(1900, 1, 1);
	
	// OffsetDateTime
	/** 1900-01-01 00:00:00 (+9:00) */
	public static final OffsetDateTime MIN_OFFSET_DATE_TIME = OffsetDateTime.of(1900, 1, 1, 0, 0, 0, 0, JST);

	// Hash Algorithm
	public static final String SHA_256 = "SHA-256";

	// Integer
	/** 写真一覧のタグリストで指定できるタグ数の上限 */
	public static final Integer TAG_LIST_MAX_SIZE = 20;

	/** 1枚の写真に登録できるタグ数の上限（アノテーション属性で参照するためint型で定義する） */
	public static final int PHOTO_TAG_MAX_SIZE = 20;

	/** タグ名（日本語・英語）の最大文字数（DBの varchar(20) に合わせる） */
	public static final int TAG_NAME_MAX_LENGTH = 20;
}