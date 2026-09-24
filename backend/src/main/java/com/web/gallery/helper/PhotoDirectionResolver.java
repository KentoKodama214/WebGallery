package com.web.gallery.helper;

import com.web.gallery.enumeration.DirectionEnum;
import java.io.IOException;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * アップロードされた画像ファイルの実際のピクセル縦横比から、写真の向きを判定するHelperクラス
 *
 * <p>複数枚の新規一括登録では、写真ごとに縦向き・横向きが混在しうるため、クライアントが送信する単一の値を 信用せず、画像ファイル自体のピクセルサイズから機械的に判定する
 */
@Component
public class PhotoDirectionResolver {

  /**
   * 画像ファイルの実際のピクセルサイズから向きを判定する
   *
   * <p>画像ヘッダーのメタデータのみを読み取り、ピクセルデータ全体はデコードしない。 ピクセルサイズが取得できない場合（未知のフォーマット・読み込み失敗）は{@link
   * DirectionEnum#NONE}を返す
   *
   * @param imageFile 判定対象の画像ファイル
   * @return {@link DirectionEnum}
   */
  public DirectionEnum resolve(MultipartFile imageFile) {
    try (ImageInputStream imageInputStream =
        ImageIO.createImageInputStream(imageFile.getInputStream())) {
      if (imageInputStream == null) {
        return DirectionEnum.NONE;
      }

      Iterator<ImageReader> readers = ImageIO.getImageReaders(imageInputStream);
      if (!readers.hasNext()) {
        return DirectionEnum.NONE;
      }

      ImageReader reader = readers.next();
      try {
        reader.setInput(imageInputStream);
        int width = reader.getWidth(0);
        int height = reader.getHeight(0);
        if (width > height) {
          return DirectionEnum.HORIZONTAL;
        }
        if (width < height) {
          return DirectionEnum.VERTICAL;
        }
        return DirectionEnum.SQUARE;
      } finally {
        reader.dispose();
      }
    } catch (IOException e) {
      return DirectionEnum.NONE;
    }
  }
}
