package com.web.gallery.infrastructure.persistence.repository;

import com.web.gallery.application.model.account.LoginHistoryModel;
import com.web.gallery.application.repository.LoginHistoryRepository;
import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.infrastructure.persistence.entity.account.LoginHistory;
import com.web.gallery.infrastructure.persistence.entity.account.LoginHistoryCondition;
import com.web.gallery.infrastructure.persistence.mapper.LoginHistoryMapper;
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
