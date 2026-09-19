package com.web.gallery.policy;

import com.web.gallery.domain.photo.ExifData;
import org.springframework.stereotype.Component;

/** 画像ファイルから抽出したEXIF情報とクライアント申告のEXIF情報のどちらを採用するかを判定するドメインサービス */
@Component
public class PhotoExifDataMergePolicy {

  /**
   * 画像ファイルから抽出したEXIF情報とクライアント申告のEXIF情報を項目単位でマージする
   *
   * <p>画像ファイル自体に埋め込まれた実データの方が信頼性が高いため項目ごとに優先して採用し、
   * PNG等EXIFを保持しない形式や項目が記録されていない画像では、ユーザーが手入力で申告した値を採用する
   *
   * @param extracted 画像ファイルから抽出したEXIF情報
   * @param clientSubmitted クライアント申告のEXIF情報
   * @return マージ後の{@link ExifData}
   */
  public ExifData merge(ExifData extracted, ExifData clientSubmitted) {
    return new ExifData(
        extracted.focalLength() != null ? extracted.focalLength() : clientSubmitted.focalLength(),
        extracted.fValue() != null ? extracted.fValue() : clientSubmitted.fValue(),
        extracted.shutterSpeed() != null
            ? extracted.shutterSpeed()
            : clientSubmitted.shutterSpeed(),
        extracted.iso() != null ? extracted.iso() : clientSubmitted.iso());
  }
}
