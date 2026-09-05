package com.web.gallery.model;

import com.web.gallery.domain.photo.ImageFilePath;
import com.web.gallery.domain.photo.PhotoNo;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/** 写真保存結果の情報を受け渡すためのModelクラス */
@Value
@Builder
public class PhotoSaveResultModel {
  /** 写真番号 */
  @NonNull private PhotoNo photoNo;

  /**
   * 画像ファイルパス（パストラバーサル対策済みのベース名を用いて実際に保存された値）
   *
   * <p>新規登録以外（更新のみ）の場合はファイルの再保存を行わないためnull
   */
  private ImageFilePath imageFilePath;
}
