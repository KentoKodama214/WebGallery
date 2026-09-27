package com.web.gallery.infrastructure.persistence.repository.account;

import com.web.gallery.application.aggregate.Account;
import com.web.gallery.application.model.photo.PhotoNoList;
import com.web.gallery.application.repository.account.AccountAggregateRepository;
import com.web.gallery.infrastructure.persistence.dto.photo.PhotoDeletionDto;
import com.web.gallery.infrastructure.persistence.entity.account.AccountAuthorityCondition;
import com.web.gallery.infrastructure.persistence.entity.account.AccountCondition;
import com.web.gallery.infrastructure.persistence.entity.account.LoginHistoryCondition;
import com.web.gallery.infrastructure.persistence.entity.common.LocationMstCondition;
import com.web.gallery.infrastructure.persistence.entity.inquiry.InquiryMstCondition;
import com.web.gallery.infrastructure.persistence.entity.inquiry.InquiryReplyMstCondition;
import com.web.gallery.infrastructure.persistence.entity.photo.PhotoFavoriteCondition;
import com.web.gallery.infrastructure.persistence.entity.photo.PhotoListFilterLogCondition;
import com.web.gallery.infrastructure.persistence.entity.photo.PhotoTagMstCondition;
import com.web.gallery.infrastructure.persistence.entity.photo.PhotoViewLogCondition;
import com.web.gallery.infrastructure.persistence.mapper.account.AccountAuthorityMapper;
import com.web.gallery.infrastructure.persistence.mapper.account.AccountMapper;
import com.web.gallery.infrastructure.persistence.mapper.account.LoginHistoryMapper;
import com.web.gallery.infrastructure.persistence.mapper.auth.RefreshTokenMapper;
import com.web.gallery.infrastructure.persistence.mapper.common.LocationMstMapper;
import com.web.gallery.infrastructure.persistence.mapper.inquiry.InquiryMstMapper;
import com.web.gallery.infrastructure.persistence.mapper.inquiry.InquiryReplyMstMapper;
import com.web.gallery.infrastructure.persistence.mapper.photo.PhotoFavoriteMapper;
import com.web.gallery.infrastructure.persistence.mapper.photo.PhotoListFilterLogMapper;
import com.web.gallery.infrastructure.persistence.mapper.photo.PhotoMstMapper;
import com.web.gallery.infrastructure.persistence.mapper.photo.PhotoTagMstMapper;
import com.web.gallery.infrastructure.persistence.mapper.photo.PhotoViewLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

/**
 * アカウント集約（{@link Account}）を永続化するRepositoryの実装クラス
 *
 * <p>お気に入り・写真タグ・写真マスタ・リフレッシュトークン・ロケーションマスタ・お問い合わせ・アカウント権限・
 * アカウントの各テーブルへの永続化を、アカウント削除というユースケース単位で整合性のある1操作としてまとめる。 他のRepositoryには依存せず、Mapperを直接操作する。
 *
 * <p>{@code common.account(account_no)}を参照する外部キーはいずれも{@code ON DELETE RESTRICT}であるため、
 * 参照元テーブルの削除漏れはアカウント本体の物理削除を外部キー違反で失敗させる。テーブル追加時は {@code
 * AccountAggregateRepositoryImplIntegrationTest}の参照元テーブル網羅テストが検出する
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class AccountAggregateRepositoryImpl implements AccountAggregateRepository {

  private final AccountMapper accountMapper;
  private final AccountAuthorityMapper accountAuthorityMapper;
  private final PhotoFavoriteMapper photoFavoriteMapper;
  private final PhotoTagMstMapper photoTagMstMapper;
  private final PhotoMstMapper photoMstMapper;
  private final RefreshTokenMapper refreshTokenMapper;
  private final LoginHistoryMapper loginHistoryMapper;
  private final PhotoListFilterLogMapper photoListFilterLogMapper;
  private final PhotoViewLogMapper photoViewLogMapper;
  private final LocationMstMapper locationMstMapper;
  private final InquiryMstMapper inquiryMstMapper;
  private final InquiryReplyMstMapper inquiryReplyMstMapper;

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

    // ロケーションマスタを削除（common.account への外部キーがON DELETE RESTRICTのため、
    // 残したままだとアカウント本体の物理削除が外部キー違反になる）
    locationMstMapper.delete(LocationMstCondition.byAccountNo(accountNo));

    // お問い合わせ返信を削除する。inquiry_reply_mst は inquiry_mst と common.account の双方を
    // ON DELETE RESTRICT で参照するため、お問い合わせ本体・アカウント本体より先に削除する。
    // 1. このアカウントが管理者として投稿した返信（admin_account_no 参照の解消）
    // 2. このアカウントが登録したお問い合わせに紐づく返信（inquiry_id 参照の解消）
    inquiryReplyMstMapper.delete(InquiryReplyMstCondition.byAdminAccountNo(accountNo));
    inquiryReplyMstMapper.delete(InquiryReplyMstCondition.byInquiryAccountNo(accountNo));

    // お問い合わせ本体を削除（common.account への外部キーがON DELETE RESTRICTのため、
    // 残したままだとアカウント本体の物理削除が外部キー違反になる）
    inquiryMstMapper.delete(InquiryMstCondition.byAccountNo(accountNo));

    // アカウント権限を削除（外部キー制約に抵触しないよう、アカウントの物理削除に先立って実施）
    accountAuthorityMapper.delete(AccountAuthorityCondition.byAccountNo(accountNo));

    // アカウントを物理削除
    accountMapper.delete(AccountCondition.byAccountNo(accountNo));
  }
}
