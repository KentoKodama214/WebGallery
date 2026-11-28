package com.web.gallery.application.model.auth;

import com.web.gallery.domain.model.auth.CredentialId;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * PasskeyCredentialModelのコレクションを表すクラス
 *
 * @param passkeyCredentialModelList {@link PasskeyCredentialModel}のリスト
 */
public record PasskeyCredentialModelList(List<PasskeyCredentialModel> passkeyCredentialModelList)
    implements Iterable<PasskeyCredentialModel> {

  public PasskeyCredentialModelList {
    Objects.requireNonNull(passkeyCredentialModelList);
  }

  /**
   * PasskeyCredentialModelのリストからPasskeyCredentialModelListを生成する
   *
   * @param passkeyCredentialModelList {@link PasskeyCredentialModel}のリスト
   * @return {@link PasskeyCredentialModelList}
   */
  public static PasskeyCredentialModelList of(
      List<PasskeyCredentialModel> passkeyCredentialModelList) {
    return new PasskeyCredentialModelList(passkeyCredentialModelList);
  }

  /**
   * 空のPasskeyCredentialModelListを生成する
   *
   * @return {@link PasskeyCredentialModelList}
   */
  public static PasskeyCredentialModelList empty() {
    return PasskeyCredentialModelList.of(List.of());
  }

  /**
   * クレデンシャルIDの一覧を取得する
   *
   * <p>パスキー登録時のexcludeCredentials（同じ認証器への二重登録を防ぐ）の組み立てに使用する
   *
   * @return {@link CredentialId}のList
   */
  public List<CredentialId> toCredentialIdList() {
    return passkeyCredentialModelList.stream()
        .map(PasskeyCredentialModel::getCredentialId)
        .toList();
  }

  /**
   * 要素数を取得する
   *
   * @return 要素数
   */
  public Integer size() {
    return passkeyCredentialModelList.size();
  }

  /**
   * 要素が空かどうかを取得する
   *
   * @return 要素が空の場合はtrue
   */
  public Boolean isEmpty() {
    return passkeyCredentialModelList.isEmpty();
  }

  /**
   * 指定インデックスの要素を取得する
   *
   * @param index インデックス
   * @return {@link PasskeyCredentialModel}
   */
  public PasskeyCredentialModel get(int index) {
    return passkeyCredentialModelList.get(index);
  }

  /**
   * Streamに変換する
   *
   * @return {@link PasskeyCredentialModel}のStream
   */
  public Stream<PasskeyCredentialModel> stream() {
    return passkeyCredentialModelList.stream();
  }

  /**
   * Listに変換する
   *
   * @return {@link PasskeyCredentialModel}のList
   */
  public List<PasskeyCredentialModel> toList() {
    return passkeyCredentialModelList;
  }

  @Override
  public Iterator<PasskeyCredentialModel> iterator() {
    return passkeyCredentialModelList.iterator();
  }
}
