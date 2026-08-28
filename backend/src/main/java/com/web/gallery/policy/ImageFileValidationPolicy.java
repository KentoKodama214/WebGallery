package com.web.gallery.policy;

import java.io.IOException;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.web.gallery.config.PhotoConfig;
import com.web.gallery.domain.photo.ImageFile;

import lombok.RequiredArgsConstructor;

/**
 * アップロードされた画像ファイルの実効的な検証（Content-Type・マジックバイト・サイズ）に関するビジネスルールを判定するドメインサービス
 */
@Component
@RequiredArgsConstructor
public class ImageFileValidationPolicy {

	private final PhotoConfig photoConfig;

	/** 許可する画像ファイルのContent-Type */
	private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/gif", "image/webp");

	/** JPEGのマジックバイト（先頭3バイト） */
	private static final int[] JPEG_SIGNATURE = {0xFF, 0xD8, 0xFF};

	/** PNGのマジックバイト（先頭8バイト） */
	private static final int[] PNG_SIGNATURE = {0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};

	/** GIF87aのマジックバイト（先頭6バイト） */
	private static final int[] GIF_87A_SIGNATURE = {0x47, 0x49, 0x46, 0x38, 0x37, 0x61};

	/** GIF89aのマジックバイト（先頭6バイト） */
	private static final int[] GIF_89A_SIGNATURE = {0x47, 0x49, 0x46, 0x38, 0x39, 0x61};

	/** WebPコンテナのマジックバイト（先頭4バイト、"RIFF"） */
	private static final int[] RIFF_SIGNATURE = {0x52, 0x49, 0x46, 0x46};

	/** WebPのマジックバイト（9〜12バイト目、"WEBP"） */
	private static final int[] WEBP_SIGNATURE = {0x57, 0x45, 0x42, 0x50};

	/** WebPのマジックバイトのオフセット（"RIFF"の4バイト＋ファイルサイズの4バイトの後） */
	private static final int WEBP_SIGNATURE_OFFSET = 8;

	/** 1MBあたりのバイト数 */
	private static final long BYTES_PER_MB = 1024 * 1024;

	/**
	 * 画像ファイルのContent-Typeが許可された形式かどうかを判定する
	 *
	 * @param	imageFile	{@link ImageFile}
	 * @return				許可されたContent-Typeの場合、true
	 */
	public Boolean isAllowedContentType(ImageFile imageFile) {
		String contentType = imageFile.value().getContentType();
		return contentType != null && ALLOWED_CONTENT_TYPES.contains(contentType);
	}

	/**
	 * 画像ファイルの先頭バイト列（マジックバイト）が、既知の画像フォーマット（JPEG・PNG・GIF・WebP）のシグネチャと一致するかどうかを判定する<p>
	 * 偽装されたContent-Typeや拡張子を見破るため、実際のバイナリ内容を検証する。ファイルの読み込みに失敗した場合はfalseを返す
	 *
	 * @param	imageFile	{@link ImageFile}
	 * @return				既知の画像フォーマットのシグネチャと一致する場合、true
	 */
	public Boolean isValidSignature(ImageFile imageFile) {
		byte[] header;
		try {
			header = imageFile.value().getBytes();
		} catch (IOException e) {
			return false;
		}

		return matchesAt(header, 0, JPEG_SIGNATURE)
				|| matchesAt(header, 0, PNG_SIGNATURE)
				|| matchesAt(header, 0, GIF_87A_SIGNATURE)
				|| matchesAt(header, 0, GIF_89A_SIGNATURE)
				|| (matchesAt(header, 0, RIFF_SIGNATURE) && matchesAt(header, WEBP_SIGNATURE_OFFSET, WEBP_SIGNATURE));
	}

	/**
	 * 画像ファイルのサイズが、設定された上限（{@link PhotoConfig#getMaxFileSizeMb()}）を超えているかどうかを判定する
	 *
	 * @param	imageFile	{@link ImageFile}
	 * @return				上限を超えている場合、true
	 */
	public Boolean isSizeExceeded(ImageFile imageFile) {
		long maxFileSizeBytes = photoConfig.getMaxFileSizeMb() * BYTES_PER_MB;
		return imageFile.value().getSize() > maxFileSizeBytes;
	}

	/**
	 * バイト列の指定オフセット位置から、シグネチャと一致するかどうかを判定する
	 *
	 * @param	bytes		検証対象のバイト列
	 * @param	offset		検証開始位置
	 * @param	signature	期待するシグネチャ
	 * @return				一致する場合、true
	 */
	private boolean matchesAt(byte[] bytes, int offset, int[] signature) {
		if (bytes.length < offset + signature.length) return false;

		for (int i = 0; i < signature.length; i++) {
			if ((bytes[offset + i] & 0xFF) != signature[i]) return false;
		}
		return true;
	}
}
