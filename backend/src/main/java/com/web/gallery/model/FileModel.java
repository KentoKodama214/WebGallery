package com.web.gallery.model;

import com.web.gallery.domain.photo.ImageFile;
import com.web.gallery.domain.photo.ImageFilePath;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/**
 * ファイル情報を受け渡すためのModelクラス
 */
@Value
@Builder
public class FileModel {
	/** ファイルパス */
	@NonNull
	private ImageFilePath filePath;

	/** ファイル */
	@NonNull
	private ImageFile imageFile;

	/**
	 * ファイルパスとファイルからFileModelを生成する
	 *
	 * @param	filePath	ファイルパス
	 * @param	imageFile	画像ファイル
	 * @return				{@link FileModel}
	 */
	public static FileModel of(ImageFilePath filePath, ImageFile imageFile) {
		return FileModel.builder()
				.filePath(filePath)
				.imageFile(imageFile)
				.build();
	}
}