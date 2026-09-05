package com.web.gallery.repository;

import com.web.gallery.domain.photo.ImageFilePath;
import com.web.gallery.model.FileModel;

/** ファイルを永続化するRepositoryクラス */
public interface FileRepository {

  /**
   * ファイルを保存する
   *
   * @param fileModel {@link FileModel}
   */
  void save(FileModel fileModel);

  /**
   * ファイルを削除する
   *
   * @param filePath {@link ImageFilePath}
   */
  void delete(ImageFilePath filePath);
}
