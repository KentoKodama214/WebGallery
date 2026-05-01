package com.web.gallary.repository;

import com.web.gallary.model.FileModel;

/**
 * ファイルを永続化するRepositoryクラス
 */
public interface FileRepository {

	void save(FileModel fileModel);
	
	void delete(String filePath);
}