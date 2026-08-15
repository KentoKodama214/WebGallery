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
}