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
   * @param filePath {@link ImageFilePath}（S3オブジェクトキー）
   */
  void delete(ImageFilePath filePath);

  /**
   * 指定したプレフィックス配下のファイルを一括削除する
   *
   * <p>アカウント削除時に、そのアカウントの写真ディレクトリ（{@code {accountId}/}）配下を まとめて削除する用途で利用する。
   *
   * @param prefix {@link ImageFilePath}（削除対象のキープレフィックス）
   */
  void deleteByPrefix(ImageFilePath prefix);

  /**
   * 指定したキーのファイルを取得するための署名付きURL（pre-signed GET URL）を発行する
   *
   * @param filePath {@link ImageFilePath}（S3オブジェクトキー）
   * @return {@link ImageFilePath}（署名付きURL文字列）
   */
  ImageFilePath getPresignedUrl(ImageFilePath filePath);
}
