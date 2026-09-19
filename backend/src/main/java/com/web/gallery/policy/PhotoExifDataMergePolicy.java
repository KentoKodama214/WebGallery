package com.web.gallery.policy;

import com.web.gallery.domain.photo.ExifData;
import org.springframework.stereotype.Component;

/** 画像ファイルから抽出したEXIF情報とクライアント申告のEXIF情報のどちらを採用するかを判定するドメインサービス */
@Component
public class PhotoExifDataMergePolicy {

  /**
   * 画像ファイルから抽出したEXIF情報とクライアント申告のEXIF情報を項目単位でマージする
   *
   * <p>ユーザーが手入力で申告した値を項目ごとに優先して採用し、未入力の項目についてのみ、 画像ファイルから抽出できた値（JPEGのEXIF）を採用する
   *
   * @param extracted 画像ファイルから抽出したEXIF情報
   * @param clientSubmitted クライアント申告のEXIF情報
   * @return マージ後の{@link ExifData}
   */
  public ExifData merge(ExifData extracted, ExifData clientSubmitted) {
    return new ExifData(
        clientSubmitted.focalLength() != null
            ? clientSubmitted.focalLength()
            : extracted.focalLength(),
        clientSubmitted.fValue() != null ? clientSubmitted.fValue() : extracted.fValue(),
        clientSubmitted.shutterSpeed() != null
            ? clientSubmitted.shutterSpeed()
            : extracted.shutterSpeed(),
        clientSubmitted.iso() != null ? clientSubmitted.iso() : extracted.iso());
  }
}
