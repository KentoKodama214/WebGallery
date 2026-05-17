package com.web.gallery.repository;

import com.web.gallery.model.FileModel;

/**
 * ファイルを永続化するRepositoryクラス
 */
public interface FileRepository {

	void save(FileModel fileModel);
	
	void delete(String filePath);
}