package com.web.gallery.policy;

import com.web.gallery.domain.photo.ImageFile;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Component;

/** 写真ファイルの拡張子に関するビジネスルールを判定するドメインサービス */
@Component
public class PhotoFileExtensionPolicy {

  /** アップロードを許可する画像ファイルの拡張子一覧（小文字） */
  private static final List<String> ALLOWED_EXTENSIONS =
      List.of("jpg", "jpeg", "png", "gif", "webp");

  /**
   * 画像ファイルの拡張子が許可されているかどうかを判定する
   *
   * @param imageFile {@link ImageFile}
   * @return 許可されている拡張子の場合、true
   */
  public Boolean isAllowedExtension(ImageFile imageFile) {
    String filename = imageFile.value().getOriginalFilename();
    if (filename == null) {
      return false;
    }

    int lastDotIndex = filename.lastIndexOf('.');
    if (lastDotIndex < 0 || lastDotIndex == filename.length() - 1) {
      return false;
    }

    String extension = filename.substring(lastDotIndex + 1).toLowerCase(Locale.ROOT);
    return ALLOWED_EXTENSIONS.contains(extension);
  }
}
