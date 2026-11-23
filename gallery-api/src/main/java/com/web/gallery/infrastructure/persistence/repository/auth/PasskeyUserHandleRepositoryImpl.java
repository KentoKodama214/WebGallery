package com.web.gallery.infrastructure.persistence.repository.auth;

import com.web.gallery.application.repository.auth.PasskeyUserHandleRepository;
import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.auth.PasskeyUserHandle;
import com.web.gallery.infrastructure.persistence.entity.auth.PasskeyUserHandleCondition;
import com.web.gallery.infrastructure.persistence.mapper.auth.PasskeyUserHandleMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * パスキーのユーザーハンドルを永続化するRepositoryの実装クラス
 *
 * @author Kento Kodama
 * @version 1.0.0
 * @since 1.0.0
 */
@Repository
@RequiredArgsConstructor
public class PasskeyUserHandleRepositoryImpl implements PasskeyUserHandleRepository {
  private final PasskeyUserHandleMapper passkeyUserHandleMapper;

  @Override
  public PasskeyUserHandle findByAccountNo(AccountNo accountNo) {
    List<com.web.gallery.infrastructure.persistence.entity.auth.PasskeyUserHandle> list =
        passkeyUserHandleMapper.select(PasskeyUserHandleCondition.byAccountNo(accountNo.value()));
    return list.isEmpty() ? null : new PasskeyUserHandle(list.getFirst().getUserHandle());
  }

  @Override
  public void saveIfAbsent(AccountNo accountNo, PasskeyUserHandle userHandle) {
    passkeyUserHandleMapper.insertIfAbsent(
        com.web.gallery.infrastructure.persistence.entity.auth.PasskeyUserHandle.of(
            accountNo.value(), userHandle.value()));
  }
}
