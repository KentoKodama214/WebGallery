package com.web.gallery.presentation.controller.converter;

import com.web.gallery.application.model.account.AccountListGetModel;
import com.web.gallery.application.model.account.AccountModel;
import com.web.gallery.domain.model.account.AccountId;
import com.web.gallery.domain.model.account.AccountName;
import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.account.BirthDate;
import com.web.gallery.domain.model.account.BirthplacePrefectureKbnCode;
import com.web.gallery.domain.model.account.FreeMemo;
import com.web.gallery.domain.model.account.LoginFailureCount;
import com.web.gallery.domain.model.account.Password;
import com.web.gallery.domain.model.account.ResidentPrefectureKbnCode;
import com.web.gallery.presentation.controller.request.account.AccountListRequest;
import com.web.gallery.presentation.controller.request.account.AccountRegistRequest;
import com.web.gallery.presentation.controller.request.account.AccountUpdateRequest;
import org.springframework.stereotype.Component;

/** アカウント関連のRequestをModelへ変換するConverterクラス */
@Component
public class AccountConverter {

  /**
   * アカウント一覧リクエストからAccountListGetModelを生成する
   *
   * @param request {@link AccountListRequest}
   * @return {@link AccountListGetModel}
   */
  public AccountListGetModel toAccountListGetModel(AccountListRequest request) {
    return AccountListGetModel.builder().pageNo(request.getPageNo()).build();
  }

  /**
   * アカウント登録リクエストからAccountModelを生成する
   *
   * @param request {@link AccountRegistRequest}
   * @return {@link AccountModel}
   */
  public AccountModel toAccountModelForRegist(AccountRegistRequest request) {
    return AccountModel.builder()
        .accountId(new AccountId(request.getAccountId()))
        .accountName(new AccountName(request.getAccountName()))
        .password(new Password(request.getPassword()))
        .birthdate(request.getBirthdate() != null ? new BirthDate(request.getBirthdate()) : null)
        .sexKbn(request.getSexKbn())
        .birthplacePrefectureKbnCode(
            request.getBirthplacePrefectureKbnCode() != null
                ? new BirthplacePrefectureKbnCode(request.getBirthplacePrefectureKbnCode())
                : null)
        .residentPrefectureKbnCode(
            request.getResidentPrefectureKbnCode() != null
                ? new ResidentPrefectureKbnCode(request.getResidentPrefectureKbnCode())
                : null)
        .freeMemo(request.getFreeMemo() != null ? new FreeMemo(request.getFreeMemo()) : null)
        .loginFailureCount(new LoginFailureCount(0))
        .build();
  }

  /**
   * アカウント更新リクエストからAccountModelを生成する
   *
   * @param request {@link AccountUpdateRequest}
   * @param accountNo アカウント番号
   * @return {@link AccountModel}
   */
  public AccountModel toAccountModelForUpdate(AccountUpdateRequest request, Long accountNo) {
    return AccountModel.builder()
        .accountNo(new AccountNo(accountNo))
        .accountId(new AccountId(request.getAccountId()))
        .accountName(new AccountName(request.getAccountName()))
        .password(
            request.getNewPassword() == null || request.getNewPassword().isEmpty()
                ? null
                : new Password(request.getNewPassword()))
        .birthdate(request.getBirthdate() != null ? new BirthDate(request.getBirthdate()) : null)
        .sexKbn(request.getSexKbn())
        .birthplacePrefectureKbnCode(
            request.getBirthplacePrefectureKbnCode() != null
                ? new BirthplacePrefectureKbnCode(request.getBirthplacePrefectureKbnCode())
                : null)
        .residentPrefectureKbnCode(
            request.getResidentPrefectureKbnCode() != null
                ? new ResidentPrefectureKbnCode(request.getResidentPrefectureKbnCode())
                : null)
        .freeMemo(request.getFreeMemo() != null ? new FreeMemo(request.getFreeMemo()) : null)
        .build();
  }
}
