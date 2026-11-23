package com.web.gallery.domain.model.auth;

import java.io.Serializable;
import java.util.Arrays;

/**
 * 認証器データの値オブジェクト
 *
 * @param value 認証器データ
 */
public record AttestedCredentialData(byte[] value) implements Serializable {

  /**
   * コンパクトコンストラクタ
   *
   * @throws IllegalArgumentException nullまたは空の場合
   */
  public AttestedCredentialData {
    if (value == null) {
      throw new IllegalArgumentException("認証器データはnullにできません");
    }
    if (value.length == 0) {
      throw new IllegalArgumentException("認証器データは空にできません");
    }
    // 呼び出し元の配列を後から書き換えられても値が変わらないよう防御的にコピーする
    value = value.clone();
  }

  /**
   * 認証器データのコピーを返す
   *
   * <p>内部の配列を外部から書き換えられないよう、アクセサは常にコピーを返す
   *
   * @return 認証器データ
   */
  @Override
  public byte[] value() {
    return value.clone();
  }

  /**
   * 内容（バイト列）が等しいかどうかを判定する
   *
   * @param o 比較対象
   * @return 内容が等しい場合true
   */
  @Override
  public boolean equals(Object o) {
    return o instanceof AttestedCredentialData other && Arrays.equals(value, other.value);
  }

  /**
   * 内容（バイト列）に基づくハッシュコードを返す
   *
   * @return ハッシュコード
   */
  @Override
  public int hashCode() {
    return Arrays.hashCode(value);
  }

  /**
   * 文字列表現を返す（バイナリの内容はログに出さない）
   *
   * @return 文字列表現
   */
  @Override
  public String toString() {
    return "AttestedCredentialData[length=" + value.length + "]";
  }
}
