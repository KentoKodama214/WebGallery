package com.web.gallary.repository.impl;

import org.springframework.stereotype.Repository;

import com.web.gallary.model.FileModel;
import com.web.gallary.repository.FileRepository;

import lombok.RequiredArgsConstructor;

/**
 * ファイルを永続化するRepositoryの実装クラス
 */
@Repository
@RequiredArgsConstructor
public class FileRepositoryImpl implements FileRepository {

	@Override
	public void save(FileModel fileModel) {
		// TODO 自動生成されたメソッド・スタブ
		return;
	}

	@Override
	public void delete(String filePath) {
		// TODO 自動生成されたメソッド・スタブ
		return;
	}
}