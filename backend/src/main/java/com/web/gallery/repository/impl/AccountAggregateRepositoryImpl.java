package com.web.gallery.repository.impl;

import com.web.gallery.aggregate.Account;
import com.web.gallery.dto.PhotoDeletionDto;
import com.web.gallery.entity.AccountCondition;
import com.web.gallery.entity.LoginHistoryCondition;
import com.web.gallery.entity.PhotoFavoriteCondition;
import com.web.gallery.entity.PhotoListFilterLogCondition;
import com.web.gallery.entity.PhotoTagMstCondition;
import com.web.gallery.entity.PhotoViewLogCondition;
import com.web.gallery.mapper.AccountMapper;
import com.web.gallery.mapper.LoginHistoryMapper;
import com.web.gallery.mapper.PhotoFavoriteMapper;
import com.web.gallery.mapper.PhotoListFilterLogMapper;
import com.web.gallery.mapper.PhotoMstMapper;
import com.web.gallery.mapper.PhotoTagMstMapper;
import com.web.gallery.mapper.PhotoViewLogMapper;
import com.web.gallery.mapper.RefreshTokenMapper;
import com.web.gallery.model.PhotoNoList;
import com.web.gallery.repository.AccountAggregateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

/**
 * アカウント集約（{@link Account}）を永続化するRepositoryの実装クラス
 *
 * <p>お気に入り・写真タグ・写真マスタ・リフレッシュトークン・アカウントの各テーブルへの永続化を、
 * アカウント削除というユースケース単位で整合性のある1操作としてまとめる。他のRepositoryには依存せず、 Mapperを直接操作する
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class AccountAggregateRepositoryImpl implements AccountAggregateRepository {

  private final AccountMapper accountMapper;
  private final PhotoFavoriteMapper photoFavoriteMapper;
  private final PhotoTagMstMapper photoTagMstMapper;
  private final PhotoMstMapper photoMstMapper;
  private final RefreshTokenMapper refreshTokenMapper;
  private final LoginHistoryMapper loginHistoryMapper;
  private final PhotoListFilterLogMapper photoListFilterLogMapper;
  private final PhotoViewLogMapper photoViewLogMapper;

  /**
   * アカウント集約を削除する
   *
   * @param account {@link Account}
   */
  @Override
  public void delete(Account account) {
    Long accountNo = account.getAccountNo().value();

    // 自分が登録したお気に入りを削除
    photoFavoriteMapper.delete(PhotoFavoriteCondition.byAccountNo(accountNo));

    // 自分の写真に対する他人のお気に入りを削除
    photoFavoriteMapper.delete(PhotoFavoriteCondition.byFavoritePhotoAccountNo(accountNo));

    // 写真タグを削除
    photoTagMstMapper.delete(PhotoTagMstCondition.byAccountNo(accountNo));

    // 写真詳細閲覧ログを削除（外部キー制約に抵触しないよう、写真マスタの物理削除に先立って実施）
    photoViewLogMapper.delete(PhotoViewLogCondition.byPhotoAccountNo(accountNo));

    // 写真マスタを物理削除し、削除時点で未削除だった写真番号を取得
    // SELECTとDELETEの間のTOCTOUギャップを無くすため単一SQLでアトミックに実施し、
    // 既に論理削除済みだった写真はイベントの重複発行を避けるため対象から除外する
    PhotoNoList deletedPhotoNoList =
        PhotoNoList.from(
            photoMstMapper.deletePhotosByAccountNo(accountNo).stream()
                .filter(dto -> !dto.getIsDeleted())
                .map(PhotoDeletionDto::getPhotoNo)
                .toList());
    account.recordDeletedPhotoNos(deletedPhotoNoList);

    // リフレッシュトークンを失効
    refreshTokenMapper.revokeAllByAccountNo(accountNo, accountNo);

    // ログイン履歴・写真一覧絞り込みログを削除（外部キー制約に抵触しないよう、アカウントの物理削除に先立って実施）
    loginHistoryMapper.delete(LoginHistoryCondition.byAccountNo(accountNo));
    photoListFilterLogMapper.delete(PhotoListFilterLogCondition.byPhotoAccountNo(accountNo));

    // アカウントを物理削除
    accountMapper.delete(AccountCondition.byAccountNo(accountNo));
  }
}
