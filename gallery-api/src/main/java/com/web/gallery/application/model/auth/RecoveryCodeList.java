package com.web.gallery.application.model.auth;

import com.web.gallery.domain.model.auth.RecoveryCode;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * RecoveryCodeのコレクションを表すクラス
 *
 * <p>発行直後の平文のリカバリーコードを、利用者へ一度だけ表示するために受け渡す（DBにはハッシュのみ保存する）
 *
 * @param recoveryCodeList {@link RecoveryCode}のリスト
 */
public record RecoveryCodeList(List<RecoveryCode> recoveryCodeList)
    implements Iterable<RecoveryCode> {

  public RecoveryCodeList {
    Objects.requireNonNull(recoveryCodeList);
  }

  /**
   * RecoveryCodeのリストからRecoveryCodeListを生成する
   *
   * @param recoveryCodeList {@link RecoveryCode}のリスト
   * @return {@link RecoveryCodeList}
   */
  public static RecoveryCodeList of(List<RecoveryCode> recoveryCodeList) {
    return new RecoveryCodeList(recoveryCodeList);
  }

  /**
   * 空のRecoveryCodeListを生成する
   *
   * @return {@link RecoveryCodeList}
   */
  public static RecoveryCodeList empty() {
    return RecoveryCodeList.of(List.of());
  }

  /**
   * 要素数を取得する
   *
   * @return 要素数
   */
  public Integer size() {
    return recoveryCodeList.size();
  }

  /**
   * 要素が空かどうかを取得する
   *
   * @return 要素が空の場合はtrue
   */
  public Boolean isEmpty() {
    return recoveryCodeList.isEmpty();
  }

  /**
   * 指定インデックスの要素を取得する
   *
   * @param index インデックス
   * @return {@link RecoveryCode}
   */
  public RecoveryCode get(int index) {
    return recoveryCodeList.get(index);
  }

  /**
   * Streamに変換する
   *
   * @return {@link RecoveryCode}のStream
   */
  public Stream<RecoveryCode> stream() {
    return recoveryCodeList.stream();
  }

  /**
   * Listに変換する
   *
   * @return {@link RecoveryCode}のList
   */
  public List<RecoveryCode> toList() {
    return recoveryCodeList;
  }

  @Override
  public Iterator<RecoveryCode> iterator() {
    return recoveryCodeList.iterator();
  }
}
