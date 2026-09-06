package com.web.gallery.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * application.ymlのファイルに関するプロパティを保持するConfigクラス
 *
 * @author Kento Kodama
 * @version 1.0.0
 * @since 1.0.0
 */
@RequiredArgsConstructor
@Getter
@ConfigurationProperties(prefix = "app.photo")
public class PhotoConfig {
  /** 写真一覧で、1ページあたりの表示枚数 */
  private final Integer photoCountPerPage;

  /** 最大ファイルサイズ（MB） */
  private final Integer maxFileSizeMb;

  /** mini-userの写真登録上限枚数 */
  private final Integer miniUserUpperLimit;

  /** normal-userの写真登録上限枚数 */
  private final Integer normalUserUpperLimit;
}
