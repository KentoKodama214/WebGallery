package com.web.gallery.repository.impl;

import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.entity.LoginHistory;
import com.web.gallery.entity.LoginHistoryCondition;
import com.web.gallery.mapper.LoginHistoryMapper;
import com.web.gallery.model.LoginHistoryModel;
import com.web.gallery.repository.LoginHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * ログイン履歴データを永続化するRepositoryの実装クラス
 *
 * @author Kento Kodama
 * @version 1.0.0
 * @since 1.0.0
 */
@Repository
@RequiredArgsConstructor
public class LoginHistoryRepositoryImpl implements LoginHistoryRepository {
  private final LoginHistoryMapper loginHistoryMapper;

  @Override
  public void save(LoginHistoryModel loginHistoryModel) {
    LoginHistory loginHistory = LoginHistory.from(loginHistoryModel);
    loginHistoryMapper.insert(loginHistory);
  }

  @Override
  public void deleteByAccountNo(AccountNo accountNo) {
    loginHistoryMapper.delete(LoginHistoryCondition.byAccountNo(accountNo.value()));
  }
}
