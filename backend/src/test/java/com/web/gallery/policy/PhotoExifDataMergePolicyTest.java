package com.web.gallery.policy;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.domain.photo.ExifData;
import com.web.gallery.domain.photo.FValue;
import com.web.gallery.domain.photo.FocalLength;
import com.web.gallery.domain.photo.Iso;
import com.web.gallery.domain.photo.ShutterSpeed;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PhotoExifDataMergePolicyTest {
  private final PhotoExifDataMergePolicy photoExifDataMergePolicy = new PhotoExifDataMergePolicy();

  @Test
  @Order(1)
  @DisplayName("正常系：抽出値が全項目存在する場合、全項目とも抽出値を採用する")
  void merge_allExtractedFieldsPresent_usesExtracted() {
    ExifData extracted =
        new ExifData(new FocalLength(50), new FValue(BigDecimal.valueOf(2.8)), null, null);
    ExifData clientSubmitted =
        new ExifData(
            new FocalLength(24),
            new FValue(BigDecimal.valueOf(4.0)),
            new ShutterSpeed(BigDecimal.valueOf(0.01)),
            new Iso(400));

    ExifData merged = photoExifDataMergePolicy.merge(extracted, clientSubmitted);

    assertEquals(50, merged.focalLength().value());
    assertEquals(0, BigDecimal.valueOf(2.8).compareTo(merged.fValue().value()));
    // 抽出値が未設定の項目はクライアント申告値にフォールバックする
    assertEquals(0, BigDecimal.valueOf(0.01).compareTo(merged.shutterSpeed().value()));
    assertEquals(400, merged.iso().value());
  }

  @Test
  @Order(2)
  @DisplayName("正常系：抽出値が全項目未設定（EXIF非対応形式等）の場合、全項目ともクライアント申告値を採用する")
  void merge_extractedEmpty_usesClientSubmitted() {
    ExifData extracted = ExifData.empty();
    ExifData clientSubmitted =
        new ExifData(
            new FocalLength(24),
            new FValue(BigDecimal.valueOf(4.0)),
            new ShutterSpeed(BigDecimal.valueOf(0.01)),
            new Iso(400));

    ExifData merged = photoExifDataMergePolicy.merge(extracted, clientSubmitted);

    assertEquals(clientSubmitted, merged);
  }

  @Test
  @Order(3)
  @DisplayName("正常系：抽出値・クライアント申告値ともに全項目未設定の場合、全項目未設定のExifDataを返す")
  void merge_bothEmpty_returnsEmpty() {
    ExifData merged = photoExifDataMergePolicy.merge(ExifData.empty(), ExifData.empty());

    assertEquals(ExifData.empty(), merged);
  }
}
