package com.web.gallery.repository.impl;

import com.web.gallery.domain.account.AccountId;
import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.dto.AccountDto;
import com.web.gallery.entity.account.Account;
import com.web.gallery.entity.account.AccountAuthority;
import com.web.gallery.entity.account.AccountAuthorityCondition;
import com.web.gallery.entity.account.AccountAuthorityUpdateTarget;
import com.web.gallery.entity.account.AccountCondition;
import com.web.gallery.entity.account.AccountUpdateTarget;
import com.web.gallery.enumeration.ErrorEnum;
import com.web.gallery.exception.GalleryException;
import com.web.gallery.mapper.AccountAuthorityMapper;
import com.web.gallery.mapper.AccountMapper;
import com.web.gallery.model.account.AccountGetModel;
import com.web.gallery.model.account.AccountModel;
import com.web.gallery.model.account.AccountModelList;
import com.web.gallery.model.account.AccountPageModel;
import com.web.gallery.repository.AccountRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;

/** アカウントデータを永続化するRepositoryの実装クラス */
@Slf4j
@Repository
@RequiredArgsConstructor
public class AccountRepositoryImpl implements AccountRepository {

  private final AccountMapper accountMapper;
  private final AccountAuthorityMapper accountAuthorityMapper;
  private final PasswordEncoder passwordEncoder;

  /**
   * Accountテーブルで該当するレコードを取得する
   *
   * @param accountNo アカウント番号
   * @return AccountModel {@link AccountModel}
   *     <p>取得できない場合はnullを返す
   */
  @Override
  public AccountModel getByAccountNo(AccountNo accountNo) {
    List<AccountDto> accountDtoList =
        accountMapper.select(AccountCondition.byAccountNo(accountNo.value()));
    return accountDtoList.isEmpty() ? null : AccountModel.from(accountDtoList.getFirst());
  }

  /**
   * Accountテーブルで該当するレコードを取得する
   *
   * @param accountId アカウントId
   * @return AccountModel {@link AccountModel}
   *     <p>取得できない場合はnullを返す
   */
  @Override
  public AccountModel getByAccountId(AccountId accountId) {
    List<AccountDto> accountDtoList =
        accountMapper.select(AccountCondition.byAccountId(accountId.value()));
    return accountDtoList.isEmpty() ? null : AccountModel.from(accountDtoList.getFirst());
  }

  /**
   * Accountテーブルへ登録する
   *
   * <p>{@code common.account}と{@code common.account_authority}へ、同一のアカウント番号で登録する
   *
   * @param accountModel {@link AccountModel}
   * @throws GalleryException 登録に失敗した場合
   */
  @Override
  public void regist(AccountModel accountModel) throws GalleryException {
    Long accountNo = accountMapper.nextAccountNo();
    Account account = Account.from(accountModel, passwordEncoder, accountNo);

    try {
      accountMapper.insert(account);
    } catch (DuplicateKeyException e) {
      log.warn("Account: Duplicate Key (AccountId: {})", accountModel.getAccountId().value(), e);
      throw ErrorEnum.FAIL_TO_REGIST_ACCOUNT.toException();
    }
    accountAuthorityMapper.insert(AccountAuthority.forRegist(accountNo));
  }

  /**
   * Accountテーブルで該当するレコードを更新する
   *
   * @param accountModel {@link AccountModel}
   * @throws GalleryException 更新に失敗した場合
   */
  @Override
  public void update(AccountModel accountModel) throws GalleryException {
    AccountCondition condition = AccountCondition.byAccountNo(accountModel.getAccountNo().value());
    AccountUpdateTarget target = AccountUpdateTarget.fromForUpdate(accountModel, passwordEncoder);

    try {
      if (accountMapper.update(condition, target) < 1) {
        log.warn("Account: Update Failed (AccountNo: {})", accountModel.getAccountNo().value());
        throw ErrorEnum.FAIL_TO_UPDATE_ACCOUNT.toException();
      }
    } catch (DuplicateKeyException e) {
      log.warn("Account: Duplicate Key (AccountNo: {})", accountModel.getAccountNo().value(), e);
      throw ErrorEnum.FAIL_TO_UPDATE_ACCOUNT.toException();
    }
  }

  /**
   * Accountテーブルのログイン失敗回数を更新する
   *
   * @param accountModel {@link AccountModel}
   * @throws GalleryException 更新に失敗した場合
   */
  @Override
  public void updateLoginFailureCount(AccountModel accountModel) throws GalleryException {
    AccountCondition condition = AccountCondition.byAccountNo(accountModel.getAccountNo().value());
    AccountUpdateTarget target = AccountUpdateTarget.fromForUpdateLoginFailure(accountModel);

    if (accountMapper.update(condition, target) < 1) {
      log.warn("Account: Update Failed (AccountNo: {})", accountModel.getAccountNo().value());
      throw ErrorEnum.FAIL_TO_UPDATE_ACCOUNT.toException();
    }
  }

  /**
   * Accountテーブルのログイン失敗回数をSQL側で原子的にインクリメントする
   *
   * @param accountNo アカウント番号
   * @throws GalleryException 更新に失敗した場合
   */
  @Override
  public void incrementLoginFailureCount(AccountNo accountNo) throws GalleryException {
    if (accountMapper.incrementLoginFailureCount(accountNo.value()) < 1) {
      log.warn("Account: Update Failed (AccountNo: {})", accountNo.value());
      throw ErrorEnum.FAIL_TO_UPDATE_ACCOUNT.toException();
    }
  }

  /**
   * AccountAuthorityテーブルの権限区分を更新する
   *
   * @param accountModel {@link AccountModel}
   * @throws GalleryException 更新に失敗した場合
   */
  @Override
  public void updateAuthority(AccountModel accountModel) throws GalleryException {
    AccountAuthorityCondition condition =
        AccountAuthorityCondition.byAccountNo(accountModel.getAccountNo().value());
    AccountAuthorityUpdateTarget target = AccountAuthorityUpdateTarget.fromForUpdate(accountModel);

    if (accountAuthorityMapper.update(condition, target) < 1) {
      log.warn(
          "AccountAuthority: Update Failed (AccountNo: {})", accountModel.getAccountNo().value());
      throw ErrorEnum.FAIL_TO_UPDATE_ACCOUNT.toException();
    }
  }

  /**
   * アカウントIDに該当するアカウントの存在有無をチェックする（新規登録用、除外なし）
   *
   * @param accountId アカウントID
   * @return true：存在する
   */
  @Override
  public Boolean isExistAccount(AccountId accountId) {
    return accountMapper.isExistAccount(AccountCondition.forExistCheck(null, accountId.value()));
  }

  /**
   * アカウントIDに該当するアカウントの存在有無をチェックする（更新用、自分自身を除外）
   *
   * @param accountNo 検索対象外のアカウント番号
   * @param accountId アカウントID
   * @return true：存在する
   */
  @Override
  public Boolean isExistAccount(AccountNo accountNo, AccountId accountId) {
    return accountMapper.isExistAccount(
        AccountCondition.forExistCheck(accountNo.value(), accountId.value()));
  }

  /**
   * アカウントの一覧を、ページング情報に従い取得する
   *
   * <p>最後のページかどうかを判定するため、DBからは1ページあたりの表示件数より1件多く取得し、 実際に返す件数が上限を超えていた場合は表示件数分のみに切り詰める
   *
   * @param accountGetModel {@link AccountGetModel}
   * @return {@link AccountPageModel}
   */
  @Override
  public AccountPageModel getAccountList(AccountGetModel accountGetModel) {
    List<AccountDto> accountDtoList =
        accountMapper.selectList(AccountCondition.forList(accountGetModel));

    Boolean isLast = accountDtoList.size() < accountGetModel.getLimit();
    List<AccountDto> pageAccountDtoList =
        isLast ? accountDtoList : accountDtoList.subList(0, accountGetModel.getLimit() - 1);

    return AccountPageModel.of(AccountModelList.from(pageAccountDtoList), isLast);
  }

  /**
   * 管理者用：削除済みを含む全アカウントの一覧を、ページング情報に従い取得する
   *
   * <p>最後のページかどうかを判定するため、DBからは1ページあたりの表示件数より1件多く取得し、 実際に返す件数が上限を超えていた場合は表示件数分のみに切り詰める
   *
   * @param accountGetModel {@link AccountGetModel}
   * @return {@link AccountPageModel}
   */
  @Override
  public AccountPageModel getAccountListForAdmin(AccountGetModel accountGetModel) {
    List<AccountDto> accountDtoList =
        accountMapper.selectList(AccountCondition.forAdminList(accountGetModel));

    Boolean isLast = accountDtoList.size() < accountGetModel.getLimit();
    List<AccountDto> pageAccountDtoList =
        isLast ? accountDtoList : accountDtoList.subList(0, accountGetModel.getLimit() - 1);

    return AccountPageModel.of(AccountModelList.from(pageAccountDtoList), isLast);
  }

  /**
   * Accountテーブルから該当するレコードを物理削除する
   *
   * <p>外部キー制約に抵触しないよう、account_authorityの削除を先に行う
   *
   * @param accountNo アカウント番号
   */
  @Override
  public void delete(AccountNo accountNo) {
    accountAuthorityMapper.delete(AccountAuthorityCondition.byAccountNo(accountNo.value()));
    accountMapper.delete(AccountCondition.byAccountNo(accountNo.value()));
  }

  /**
   * アカウントの行ロックを取得する（排他制御用）
   *
   * @param accountNo アカウント番号
   */
  @Override
  public void lockForUpdate(AccountNo accountNo) {
    accountMapper.lockAccount(accountNo.value());
  }

  /**
   * ログイン試行を同一アカウントIDで直列化するためのトランザクションレベルのアドバイザリロックを取得する
   *
   * @param accountId アカウントID
   */
  @Override
  public void lockForLoginAttempt(AccountId accountId) {
    accountMapper.lockForLoginAttempt(accountId.value());
  }
}
