package com.web.gallery.domain.model.auth;

import com.web.gallery.domain.constant.Consts;
import com.web.gallery.domain.model.common.TokenHash;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.Locale;

/**
 * リカバリーコードの値オブジェクト
 *
 * <p>パスキーをすべて失ったときに、新しいパスキーを登録するための使い捨てのコード。 値は区切りのハイフン・空白を取り除き、英字を大文字に揃えた形（正規化後）で保持する
 *
 * @param value リカバリーコード（正規化後）
 */
public record RecoveryCode(String value) implements Serializable {

  /** 表示時にハイフンを挟む間隔（文字数） */
  private static final int DISPLAY_GROUP_SIZE = 4;

  /**
   * コンパクトコンストラクタ
   *
   * <p>入力の揺れ（ハイフン・空白の有無、小文字）を吸収するため、正規化してから保持する
   *
   * @throws IllegalArgumentException nullまたは空白の場合
   */
  public RecoveryCode {
    if (value == null) {
      throw new IllegalArgumentException("リカバリーコードはnullにできません");
    }
    value = value.replaceAll("[\\s-]", "").toUpperCase(Locale.ROOT);
    if (value.isEmpty()) {
      throw new IllegalArgumentException("リカバリーコードは空白にできません");
    }
  }

  /**
   * 新しいリカバリーコードを生成する
   *
   * @param random 暗号論的乱数生成器
   * @return {@link RecoveryCode}
   */
  public static RecoveryCode generate(SecureRandom random) {
    StringBuilder builder = new StringBuilder(Consts.RECOVERY_CODE_LENGTH);
    for (int i = 0; i < Consts.RECOVERY_CODE_LENGTH; i++) {
      builder.append(
          Consts.RECOVERY_CODE_ALPHABET.charAt(
              random.nextInt(Consts.RECOVERY_CODE_ALPHABET.length())));
    }
    return new RecoveryCode(builder.toString());
  }

  /**
   * 保存・照合に使うハッシュ（SHA-256の16進数表記）を求める
   *
   * <p>コードは80ビットの乱数のため、総当たりへの耐性を目的としたストレッチング（BCrypt等）は不要とし、 照合のたびに一定の速さで計算できるSHA-256を使う
   *
   * @return {@link TokenHash}
   */
  public TokenHash toHash() {
    try {
      MessageDigest digest = MessageDigest.getInstance(Consts.SHA_256);
      return new TokenHash(
          HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8))));
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256アルゴリズムが利用できません", e);
    }
  }

  /**
   * 利用者に表示する形（4文字ごとにハイフンで区切った形）を返す
   *
   * @return 表示用の文字列（例: {@code ABCD-EFGH-JKMN-PQRS}）
   */
  public String toDisplayString() {
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < value.length(); i++) {
      if (i > 0 && i % DISPLAY_GROUP_SIZE == 0) {
        builder.append('-');
      }
      builder.append(value.charAt(i));
    }
    return builder.toString();
  }
}
