package com.web.gallery.repository.impl;

import com.web.gallery.domain.photo.ImageFilePath;
import com.web.gallery.model.FileModel;
import com.web.gallery.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/** ファイルを永続化するRepositoryの実装クラス */
@Repository
@RequiredArgsConstructor
public class FileRepositoryImpl implements FileRepository {

  @Override
  public void save(FileModel fileModel) {
    // TODO 自動生成されたメソッド・スタブ
    return;
  }

  @Override
  public void delete(ImageFilePath filePath) {
    // TODO 自動生成されたメソッド・スタブ
    return;
  }
}
