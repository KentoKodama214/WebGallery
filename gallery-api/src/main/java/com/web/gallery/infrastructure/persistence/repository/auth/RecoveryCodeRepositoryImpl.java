package com.web.gallery.infrastructure.persistence.repository.auth;

import com.web.gallery.application.model.auth.RecoveryCodeList;
import com.web.gallery.application.repository.auth.RecoveryCodeRepository;
import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.auth.RecoveryCode;
import com.web.gallery.infrastructure.persistence.entity.auth.RecoveryCodeCondition;
import com.web.gallery.infrastructure.persistence.mapper.auth.RecoveryCodeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * リカバリーコードを永続化するRepositoryの実装クラス
 *
 * @author Kento Kodama
 * @version 1.0.0
 * @since 1.0.0
 */
@Repository
@RequiredArgsConstructor
public class RecoveryCodeRepositoryImpl implements RecoveryCodeRepository {
  private final RecoveryCodeMapper recoveryCodeMapper;

  @Override
  public void saveAll(AccountNo accountNo, RecoveryCodeList recoveryCodeList) {
    if (recoveryCodeList.isEmpty()) {
      return;
    }
    recoveryCodeMapper.bulkInsert(
        recoveryCodeList.stream()
            .map(code ->
                com.web.gallery.infrastructure.persistence.entity.auth.RecoveryCode.of(
                    accountNo.value(), code.toHash().value()))
            .toList());
  }

  @Override
  public void deleteByAccountNo(AccountNo accountNo) {
    recoveryCodeMapper.delete(RecoveryCodeCondition.byAccountNo(accountNo.value()));
  }

  @Override
  public Boolean consume(AccountNo accountNo, RecoveryCode recoveryCode) {
    return recoveryCodeMapper.delete(
            RecoveryCodeCondition.byAccountNoAndCodeHash(
                accountNo.value(), recoveryCode.toHash().value()))
        > 0;
  }

  @Override
  public Integer countByAccountNo(AccountNo accountNo) {
    return recoveryCodeMapper.count(RecoveryCodeCondition.byAccountNo(accountNo.value()));
  }
}
