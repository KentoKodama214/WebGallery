package com.web.gallery.model;

import org.springframework.web.multipart.MultipartFile;

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
	private String filePath;
	
	/** ファイル */
	@NonNull
	private MultipartFile imageFile;
}