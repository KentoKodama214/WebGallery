package com.web.gallery.infrastructure.persistence.repository.auth;

import com.web.gallery.application.model.auth.PasskeyCredentialModel;
import com.web.gallery.application.model.auth.PasskeyCredentialModelList;
import com.web.gallery.application.repository.auth.PasskeyCredentialRepository;
import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.auth.AttestedCredentialData;
import com.web.gallery.domain.model.auth.CredentialId;
import com.web.gallery.domain.model.auth.IsBackedUp;
import com.web.gallery.domain.model.auth.IsBackupEligible;
import com.web.gallery.domain.model.auth.IsUvInitialized;
import com.web.gallery.domain.model.auth.LastUsedAt;
import com.web.gallery.domain.model.auth.PasskeyName;
import com.web.gallery.domain.model.auth.PasskeyNo;
import com.web.gallery.domain.model.auth.PasskeyTransports;
import com.web.gallery.domain.model.auth.SignCount;
import com.web.gallery.domain.model.common.CreatedAt;
import com.web.gallery.infrastructure.persistence.entity.auth.PasskeyCredential;
import com.web.gallery.infrastructure.persistence.entity.auth.PasskeyCredentialCondition;
import com.web.gallery.infrastructure.persistence.entity.auth.PasskeyCredentialUpdateTarget;
import com.web.gallery.infrastructure.persistence.mapper.auth.PasskeyCredentialMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * パスキー（WebAuthnクレデンシャル）データを永続化するRepositoryの実装クラス
 *
 * @author Kento Kodama
 * @version 1.0.0
 * @since 1.0.0
 */
@Repository
@RequiredArgsConstructor
public class PasskeyCredentialRepositoryImpl implements PasskeyCredentialRepository {
  private final PasskeyCredentialMapper passkeyCredentialMapper;

  @Override
  public void save(PasskeyCredentialModel passkeyCredentialModel) {
    passkeyCredentialMapper.insert(PasskeyCredential.from(passkeyCredentialModel));
  }

  @Override
  public PasskeyCredentialModel findByCredentialId(CredentialId credentialId) {
    List<PasskeyCredential> list =
        passkeyCredentialMapper.select(
            PasskeyCredentialCondition.byCredentialId(credentialId.value()));
    return list.isEmpty() ? null : toPasskeyCredentialModel(list.getFirst());
  }

  @Override
  public PasskeyCredentialModel findByCredentialIdForUpdate(CredentialId credentialId) {
    List<PasskeyCredential> list =
        passkeyCredentialMapper.selectForUpdate(
            PasskeyCredentialCondition.byCredentialId(credentialId.value()));
    return list.isEmpty() ? null : toPasskeyCredentialModel(list.getFirst());
  }

  @Override
  public PasskeyCredentialModelList findByAccountNo(AccountNo accountNo) {
    return PasskeyCredentialModelList.of(
        passkeyCredentialMapper
            .select(PasskeyCredentialCondition.byAccountNo(accountNo.value()))
            .stream()
            .map(this::toPasskeyCredentialModel)
            .toList());
  }

  @Override
  public Integer countByAccountNo(AccountNo accountNo) {
    return passkeyCredentialMapper.count(PasskeyCredentialCondition.byAccountNo(accountNo.value()));
  }

  @Override
  public void updateAuthenticationResult(PasskeyCredentialModel passkeyCredentialModel) {
    passkeyCredentialMapper.update(
        PasskeyCredentialCondition.byPasskeyNo(passkeyCredentialModel.getPasskeyNo().value()),
        PasskeyCredentialUpdateTarget.forAuthenticationResult(passkeyCredentialModel));
  }

  @Override
  public Integer rename(AccountNo accountNo, PasskeyNo passkeyNo, PasskeyName passkeyName) {
    return passkeyCredentialMapper.update(
        PasskeyCredentialCondition.byAccountNoAndPasskeyNo(accountNo.value(), passkeyNo.value()),
        PasskeyCredentialUpdateTarget.forRename(passkeyName.value(), accountNo.value()));
  }

  @Override
  public Integer delete(AccountNo accountNo, PasskeyNo passkeyNo) {
    return passkeyCredentialMapper.delete(
        PasskeyCredentialCondition.byAccountNoAndPasskeyNo(accountNo.value(), passkeyNo.value()));
  }

  /**
   * PasskeyCredentialエンティティからPasskeyCredentialModelを組み立てる
   *
   * @param entity {@link PasskeyCredential}
   * @return {@link PasskeyCredentialModel}
   */
  private PasskeyCredentialModel toPasskeyCredentialModel(PasskeyCredential entity) {
    return PasskeyCredentialModel.builder()
        .passkeyNo(new PasskeyNo(entity.getPasskeyNo()))
        .accountNo(new AccountNo(entity.getAccountNo()))
        .credentialId(new CredentialId(entity.getCredentialId()))
        .attestedCredentialData(new AttestedCredentialData(entity.getAttestedCredentialData()))
        .signCount(new SignCount(entity.getSignCount()))
        .transports(new PasskeyTransports(entity.getTransports()))
        .isBackupEligible(new IsBackupEligible(entity.getIsBackupEligible()))
        .isBackedUp(new IsBackedUp(entity.getIsBackedUp()))
        .isUvInitialized(new IsUvInitialized(entity.getIsUvInitialized()))
        .passkeyName(new PasskeyName(entity.getPasskeyName()))
        .lastUsedAt(entity.getLastUsedAt() != null ? new LastUsedAt(entity.getLastUsedAt()) : null)
        .createdAt(entity.getCreatedAt() != null ? new CreatedAt(entity.getCreatedAt()) : null)
        .build();
  }
}
