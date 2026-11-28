package com.web.gallery.application.service.impl.account;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.web.gallery.application.aggregate.Account;
import com.web.gallery.application.config.AccountConfig;
import com.web.gallery.application.config.LoginConfig;
import com.web.gallery.application.config.WebAuthnConfig;
import com.web.gallery.application.helper.GeoIpResolver;
import com.web.gallery.application.helper.WebAuthnVerifier;
import com.web.gallery.application.model.account.AccountGetModel;
import com.web.gallery.application.model.account.AccountListGetModel;
import com.web.gallery.application.model.account.AccountModel;
import com.web.gallery.application.model.account.AccountModelList;
import com.web.gallery.application.model.account.AccountPageModel;
import com.web.gallery.application.model.account.LoginHistoryModel;
import com.web.gallery.application.model.auth.LoginContextModel;
import com.web.gallery.application.model.auth.PasskeyAssertionModel;
import com.web.gallery.application.model.auth.PasskeyAssertionResultModel;
import com.web.gallery.application.model.auth.PasskeyAttestationModel;
import com.web.gallery.application.model.auth.PasskeyAuthenticationOptionsModel;
import com.web.gallery.application.model.auth.PasskeyCredentialModel;
import com.web.gallery.application.model.auth.PasskeyCredentialModelList;
import com.web.gallery.application.model.auth.PasskeyRegistrationOptionsModel;
import com.web.gallery.application.model.auth.RecoveryCodeList;
import com.web.gallery.application.model.auth.WebAuthnChallengeModel;
import com.web.gallery.application.model.common.KbnMstModelList;
import com.web.gallery.application.model.photo.PhotoNoList;
import com.web.gallery.application.repository.photo.FileRepository;
import com.web.gallery.domain.constant.Consts;
import com.web.gallery.domain.enumeration.AuthorityEnum;
import com.web.gallery.domain.enumeration.ErrorEnum;
import com.web.gallery.domain.enumeration.LoginMethodEnum;
import com.web.gallery.domain.enumeration.WebAuthnChallengePurposeEnum;
import com.web.gallery.domain.event.AccountAuthorityChangedEvent;
import com.web.gallery.domain.event.AccountDeletedEvent;
import com.web.gallery.domain.event.AccountLockedEvent;
import com.web.gallery.domain.event.AccountRegisteredEvent;
import com.web.gallery.domain.event.AccountUnlockedEvent;
import com.web.gallery.domain.event.AccountUpdatedEvent;
import com.web.gallery.domain.event.PhotoDeletedEvent;
import com.web.gallery.domain.exception.BadRequestException;
import com.web.gallery.domain.exception.ForbiddenAccountException;
import com.web.gallery.domain.exception.GalleryException;
import com.web.gallery.domain.exception.PasskeyNotFoundException;
import com.web.gallery.domain.exception.PasskeyVerificationException;
import com.web.gallery.domain.exception.RegistFailureException;
import com.web.gallery.domain.exception.UpdateFailureException;
import com.web.gallery.domain.model.account.AccountId;
import com.web.gallery.domain.model.account.AccountName;
import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.account.BirthplacePrefectureKbnCode;
import com.web.gallery.domain.model.account.FreeMemo;
import com.web.gallery.domain.model.account.IsAdminLocked;
import com.web.gallery.domain.model.account.LoginFailureCount;
import com.web.gallery.domain.model.account.ResidentPrefectureKbnCode;
import com.web.gallery.domain.model.auth.AttestedCredentialData;
import com.web.gallery.domain.model.auth.CredentialId;
import com.web.gallery.domain.model.auth.IsBackedUp;
import com.web.gallery.domain.model.auth.IsBackupEligible;
import com.web.gallery.domain.model.auth.IsUvInitialized;
import com.web.gallery.domain.model.auth.PasskeyChallenge;
import com.web.gallery.domain.model.auth.PasskeyName;
import com.web.gallery.domain.model.auth.PasskeyNo;
import com.web.gallery.domain.model.auth.PasskeyResponseJson;
import com.web.gallery.domain.model.auth.PasskeyTransports;
import com.web.gallery.domain.model.auth.PasskeyUserHandle;
import com.web.gallery.domain.model.auth.RecoveryCode;
import com.web.gallery.domain.model.auth.SignCount;
import com.web.gallery.domain.model.common.ExpiresAt;
import com.web.gallery.domain.model.common.IpAddress;
import com.web.gallery.domain.model.common.IpGeoLocation;
import com.web.gallery.domain.model.common.IsDeleted;
import com.web.gallery.domain.model.common.KbnClassCode;
import com.web.gallery.domain.model.photo.ImageFilePath;
import com.web.gallery.domain.model.photo.PhotoNo;
import com.web.gallery.infrastructure.persistence.repository.account.AccountAggregateRepositoryImpl;
import com.web.gallery.infrastructure.persistence.repository.account.AccountRepositoryImpl;
import com.web.gallery.infrastructure.persistence.repository.account.LoginHistoryRepositoryImpl;
import com.web.gallery.infrastructure.persistence.repository.auth.PasskeyCredentialRepositoryImpl;
import com.web.gallery.infrastructure.persistence.repository.auth.PasskeyUserHandleRepositoryImpl;
import com.web.gallery.infrastructure.persistence.repository.auth.RecoveryCodeRepositoryImpl;
import com.web.gallery.infrastructure.persistence.repository.auth.RefreshTokenRepositoryImpl;
import com.web.gallery.infrastructure.persistence.repository.auth.WebAuthnChallengeRepositoryImpl;
import com.web.gallery.infrastructure.persistence.repository.common.KbnMstRepositoryImpl;
import com.web.gallery.infrastructure.persistence.repository.inquiry.InquiryReplyMstRepositoryImpl;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class AccountServiceImplTest {
  @InjectMocks private AccountServiceImpl accountServiceImpl;

  @Mock private AccountRepositoryImpl accountRepositoryImpl;

  @Mock private AccountAggregateRepositoryImpl accountAggregateRepositoryImpl;

  @Mock private FileRepository fileRepository;

  @Mock private RefreshTokenRepositoryImpl refreshTokenRepositoryImpl;

  @Mock private LoginHistoryRepositoryImpl loginHistoryRepositoryImpl;

  @Mock private KbnMstRepositoryImpl kbnMstRepositoryImpl;

  @Mock private InquiryReplyMstRepositoryImpl inquiryReplyMstRepositoryImpl;

  @Mock
  private com.web.gallery.application.helper.ReauthenticationThrottle reauthenticationThrottle;

  @Mock private GeoIpResolver geoIpResolver;

  @Mock private LoginConfig loginConfig;

  @Mock private AccountConfig accountConfig;

  @Mock private Clock clock;

  @Mock private ApplicationEventPublisher applicationEventPublisher;

  @Mock private PasskeyCredentialRepositoryImpl passkeyCredentialRepositoryImpl;

  @Mock private PasskeyUserHandleRepositoryImpl passkeyUserHandleRepositoryImpl;

  @Mock private WebAuthnChallengeRepositoryImpl webAuthnChallengeRepositoryImpl;

  @Mock private RecoveryCodeRepositoryImpl recoveryCodeRepositoryImpl;

  @Mock private WebAuthnVerifier webAuthnVerifier;

  @Mock private WebAuthnConfig webAuthnConfig;

  /** 本人確認（再認証）用の認証レスポンス */
  private static final PasskeyResponseJson REAUTH_JSON =
      new PasskeyResponseJson("{\"id\":\"reauth\"}");

  /** 本人確認（再認証）用のチャレンジ */
  private static final PasskeyChallenge REAUTH_CHALLENGE = new PasskeyChallenge("reauth-challenge");

  @BeforeEach
  void setUpClock() {
    lenient().when(clock.instant()).thenReturn(Instant.now());
    lenient().when(clock.getZone()).thenReturn(Consts.JST);
  }

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class loadUserByUsername {
    @Test
    @Order(1)
    @DisplayName("正常系：アカウント情報からUserDetailsを構築すること")
    void loadUserByUsername_success() {
      String accountId = "aaaaaaaa";
      AccountModel account =
          AccountModel.builder()
              .accountNo(new AccountNo(1L))
              .accountId(new AccountId(accountId))
              .loginFailureCount(new LoginFailureCount(0))
              .build();
      doReturn(account).when(accountRepositoryImpl).getByAccountId(new AccountId(accountId));
      doReturn(3).when(loginConfig).getFailCount();

      UserDetails userDetails = accountServiceImpl.loadUserByUsername(accountId);
      assertEquals(accountId, userDetails.getUsername());
      // パスワードは保持しない
      assertNull(userDetails.getPassword());
    }

    @Test
    @Order(2)
    @DisplayName("異常系：UsernameNotFoundExceptionをthrowする")
    void loadUserByUsername_UsernameNotFoundException() {
      String accountId = "aaaaaaaa";
      doReturn(null).when(accountRepositoryImpl).getByAccountId(new AccountId(accountId));
      assertThrows(
          UsernameNotFoundException.class, () -> accountServiceImpl.loadUserByUsername(accountId));
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class registAccount {
    private static final PasskeyResponseJson JSON = new PasskeyResponseJson("{\"id\":\"x\"}");
    private static final PasskeyChallenge CHALLENGE = new PasskeyChallenge("signup-challenge");
    private static final PasskeyUserHandle USER_HANDLE = new PasskeyUserHandle("handle-1");
    private static final PasskeyName NAME = new PasskeyName("MacBook");

    private PasskeyAttestationModel attestation;

    @BeforeEach
    void setUp() {
      attestation =
          PasskeyAttestationModel.builder()
              .credentialId(new CredentialId("credential-new"))
              .attestedCredentialData(new AttestedCredentialData(new byte[] {9}))
              .signCount(new SignCount(0L))
              .transports(new PasskeyTransports("internal"))
              .isBackupEligible(new IsBackupEligible(true))
              .isBackedUp(new IsBackedUp(true))
              .isUvInitialized(new IsUvInitialized(true))
              .build();
    }

    private void stubVerification(WebAuthnChallengeModel storedChallenge) {
      doReturn(CHALLENGE).when(webAuthnVerifier).readRegistrationChallenge(JSON);
      doReturn(attestation).when(webAuthnVerifier).verifyRegistration(JSON, CHALLENGE);
      doReturn(storedChallenge).when(webAuthnChallengeRepositoryImpl).consumeForSignup(CHALLENGE);
    }

    private WebAuthnChallengeModel signupChallenge() {
      return WebAuthnChallengeModel.forSignup(
          CHALLENGE, USER_HANDLE, new ExpiresAt(OffsetDateTime.now(clock).plusMinutes(5)));
    }

    @Test
    @Order(1)
    @DisplayName("正常系：アカウント・ユーザーハンドル・最初のパスキーを登録し、リカバリーコードを発行して返すこと")
    void registAccount_success() throws GalleryException {
      AccountModel accountModel =
          AccountModel.builder().accountId(new AccountId("aaaaaaaa")).build();
      stubVerification(signupChallenge());
      doReturn(false).when(accountRepositoryImpl).isExistAccount(new AccountId("aaaaaaaa"));
      doReturn(null)
          .when(passkeyCredentialRepositoryImpl)
          .findByCredentialId(new CredentialId("credential-new"));
      doReturn(AccountModel.builder().accountNo(new AccountNo(7L)).build())
          .when(accountRepositoryImpl)
          .getByAccountId(new AccountId("aaaaaaaa"));

      RecoveryCodeList actual = accountServiceImpl.registAccount(accountModel, JSON, NAME);

      assertEquals(Consts.RECOVERY_CODE_COUNT, actual.size());
      // 発行したコードは互いに異なる
      assertEquals(Consts.RECOVERY_CODE_COUNT, actual.stream().distinct().count());
      verify(accountRepositoryImpl).regist(accountModel);
      // 登録オプションで払い出したユーザーハンドルでアカウントへ紐づける
      verify(passkeyUserHandleRepositoryImpl).saveIfAbsent(new AccountNo(7L), USER_HANDLE);

      ArgumentCaptor<PasskeyCredentialModel> passkeyCaptor =
          ArgumentCaptor.forClass(PasskeyCredentialModel.class);
      verify(passkeyCredentialRepositoryImpl).save(passkeyCaptor.capture());
      assertEquals(new AccountNo(7L), passkeyCaptor.getValue().getAccountNo());
      assertEquals(NAME, passkeyCaptor.getValue().getPasskeyName());
      verify(recoveryCodeRepositoryImpl).saveAll(new AccountNo(7L), actual);

      ArgumentCaptor<AccountRegisteredEvent> accountRegisteredEventCaptor =
          ArgumentCaptor.forClass(AccountRegisteredEvent.class);
      verify(applicationEventPublisher, times(1))
          .publishEvent(accountRegisteredEventCaptor.capture());
      assertEquals(new AccountId("aaaaaaaa"), accountRegisteredEventCaptor.getValue().accountId());
    }

    @Test
    @Order(2)
    @DisplayName("異常系：アカウントIDが既に存在する場合、RegistFailureExceptionをスローし登録しないこと")
    void registAccount_account_already_exist() throws GalleryException {
      AccountModel accountModel =
          AccountModel.builder().accountId(new AccountId("aaaaaaaa")).build();
      stubVerification(signupChallenge());
      doReturn(true).when(accountRepositoryImpl).isExistAccount(new AccountId("aaaaaaaa"));

      RegistFailureException exception =
          assertThrows(
              RegistFailureException.class,
              () -> accountServiceImpl.registAccount(accountModel, JSON, NAME));
      assertEquals(ErrorEnum.DUPLICATE_ACCOUNT_ID.getErrorCode(), exception.getErrorCode());
      verify(accountRepositoryImpl, never()).regist(any());
      verify(passkeyCredentialRepositoryImpl, never()).save(any());
      verify(applicationEventPublisher, never()).publishEvent(any());
    }

    @Test
    @Order(3)
    @DisplayName("異常系：登録時の一意制約違反でRegistFailureExceptionがスローされた場合、パスキーを登録しないこと")
    void registAccount_RegistFailureException() throws GalleryException {
      AccountModel accountModel =
          AccountModel.builder().accountId(new AccountId("aaaaaaaa")).build();
      stubVerification(signupChallenge());
      doReturn(false).when(accountRepositoryImpl).isExistAccount(new AccountId("aaaaaaaa"));
      doReturn(null)
          .when(passkeyCredentialRepositoryImpl)
          .findByCredentialId(new CredentialId("credential-new"));
      doThrow(RegistFailureException.class).when(accountRepositoryImpl).regist(accountModel);

      assertThrows(
          RegistFailureException.class,
          () -> accountServiceImpl.registAccount(accountModel, JSON, NAME));
      verify(passkeyCredentialRepositoryImpl, never()).save(any());
      verify(applicationEventPublisher, never()).publishEvent(any());
    }

    @Test
    @Order(4)
    @DisplayName("異常系：登録レスポンスの検証に失敗した場合、BadRequestExceptionをスローしチャレンジを消費しないこと")
    void registAccount_verificationFailed() throws GalleryException {
      AccountModel accountModel =
          AccountModel.builder().accountId(new AccountId("aaaaaaaa")).build();
      doReturn(CHALLENGE).when(webAuthnVerifier).readRegistrationChallenge(JSON);
      doThrow(new PasskeyVerificationException("origin mismatch"))
          .when(webAuthnVerifier)
          .verifyRegistration(JSON, CHALLENGE);

      BadRequestException exception =
          assertThrows(
              BadRequestException.class,
              () -> accountServiceImpl.registAccount(accountModel, JSON, NAME));
      assertEquals(ErrorEnum.INVALID_PASSKEY_REGISTRATION.getErrorCode(), exception.getErrorCode());
      verify(webAuthnChallengeRepositoryImpl, never()).consumeForSignup(any());
      verify(accountRepositoryImpl, never()).regist(any());
    }

    @Test
    @Order(5)
    @DisplayName("異常系：新規登録用チャレンジが未発行・使用済み・期限切れの場合、BadRequestExceptionをスローし登録しないこと")
    void registAccount_challengeNotAvailable() throws GalleryException {
      AccountModel accountModel =
          AccountModel.builder().accountId(new AccountId("aaaaaaaa")).build();
      stubVerification(null);

      BadRequestException exception =
          assertThrows(
              BadRequestException.class,
              () -> accountServiceImpl.registAccount(accountModel, JSON, NAME));
      assertEquals(ErrorEnum.INVALID_PASSKEY_REGISTRATION.getErrorCode(), exception.getErrorCode());
      verify(accountRepositoryImpl, never()).regist(any());
    }

    @Test
    @Order(6)
    @DisplayName("異常系：登録済みのクレデンシャルの場合、RegistFailureExceptionをスローし登録しないこと")
    void registAccount_duplicatePasskey() throws GalleryException {
      AccountModel accountModel =
          AccountModel.builder().accountId(new AccountId("aaaaaaaa")).build();
      stubVerification(signupChallenge());
      doReturn(false).when(accountRepositoryImpl).isExistAccount(new AccountId("aaaaaaaa"));
      doReturn(passkey(5L, "credential-new"))
          .when(passkeyCredentialRepositoryImpl)
          .findByCredentialId(new CredentialId("credential-new"));

      RegistFailureException exception =
          assertThrows(
              RegistFailureException.class,
              () -> accountServiceImpl.registAccount(accountModel, JSON, NAME));
      assertEquals(ErrorEnum.DUPLICATE_PASSKEY.getErrorCode(), exception.getErrorCode());
      verify(accountRepositoryImpl, never()).regist(any());
    }

    @Test
    @Order(7)
    @DisplayName("異常系：出身都道府県区分コードが区分マスタに実在しない場合はBadRequestExceptionをthrowし、パスキーを検証しないこと")
    void registAccount_invalid_prefecture_code() throws GalleryException {
      AccountModel accountModel =
          AccountModel.builder()
              .accountId(new AccountId("aaaaaaaa"))
              .birthplacePrefectureKbnCode(new BirthplacePrefectureKbnCode("99"))
              .build();
      doReturn(KbnMstModelList.empty())
          .when(kbnMstRepositoryImpl)
          .get(new KbnClassCode(Consts.PREFECTURE));

      assertThrows(
          BadRequestException.class,
          () -> accountServiceImpl.registAccount(accountModel, JSON, NAME));

      verify(kbnMstRepositoryImpl).get(new KbnClassCode(Consts.PREFECTURE));
      verify(webAuthnVerifier, never()).verifyRegistration(any(), any());
      verify(accountRepositoryImpl, never()).regist(any());
      verify(applicationEventPublisher, never()).publishEvent(any());
    }

    @Test
    @Order(8)
    @DisplayName("正常系：出身都道府県区分コードが未指定でも、在住都道府県区分コードが区分マスタに実在すれば登録できること")
    void registAccount_birthplaceNull_residentValid() throws GalleryException {
      AccountModel accountModel =
          AccountModel.builder()
              .accountId(new AccountId("aaaaaaaa"))
              .residentPrefectureKbnCode(new ResidentPrefectureKbnCode("13"))
              .build();
      doReturn(KbnMstModelList.of(List.of(newPrefectureKbnMstModel("13"))))
          .when(kbnMstRepositoryImpl)
          .get(new KbnClassCode(Consts.PREFECTURE));
      stubVerification(signupChallenge());
      doReturn(false).when(accountRepositoryImpl).isExistAccount(new AccountId("aaaaaaaa"));
      doReturn(AccountModel.builder().accountNo(new AccountNo(7L)).build())
          .when(accountRepositoryImpl)
          .getByAccountId(new AccountId("aaaaaaaa"));

      assertEquals(
          Consts.RECOVERY_CODE_COUNT,
          accountServiceImpl.registAccount(accountModel, JSON, NAME).size());
    }

    @Test
    @Order(9)
    @DisplayName("異常系：在住都道府県区分コードが区分マスタに実在しない場合はBadRequestExceptionをthrowする")
    void registAccount_residentInvalid() throws GalleryException {
      AccountModel accountModel =
          AccountModel.builder()
              .accountId(new AccountId("aaaaaaaa"))
              .birthplacePrefectureKbnCode(new BirthplacePrefectureKbnCode("13"))
              .residentPrefectureKbnCode(new ResidentPrefectureKbnCode("99"))
              .build();
      doReturn(KbnMstModelList.of(List.of(newPrefectureKbnMstModel("13"))))
          .when(kbnMstRepositoryImpl)
          .get(new KbnClassCode(Consts.PREFECTURE));

      assertThrows(
          BadRequestException.class,
          () -> accountServiceImpl.registAccount(accountModel, JSON, NAME));

      verify(accountRepositoryImpl, never()).regist(any());
    }
  }

  @Nested
  @Order(19)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class createSignupOptions {
    @Test
    @Order(1)
    @DisplayName("正常系：ユーザーハンドルを払い出し、新規登録用チャレンジとともに保存して登録オプションを返すこと")
    void createSignupOptions_success() throws GalleryException {
      doReturn(false).when(accountRepositoryImpl).isExistAccount(new AccountId("aaaaaaaa"));
      PasskeyChallenge challenge = new PasskeyChallenge("signup-challenge");
      doReturn(challenge).when(webAuthnVerifier).generateChallenge();
      PasskeyUserHandle userHandle = new PasskeyUserHandle("handle-1");
      doReturn(userHandle).when(webAuthnVerifier).generateUserHandle();
      doReturn("localhost").when(webAuthnConfig).getRpId();
      doReturn("WebGallery").when(webAuthnConfig).getRpName();
      doReturn(300).when(webAuthnConfig).getChallengeTimeoutSeconds();

      PasskeyRegistrationOptionsModel actual =
          accountServiceImpl.createSignupOptions(
              new AccountId("aaaaaaaa"), new AccountName("テストユーザー"));

      assertEquals(challenge, actual.getChallenge());
      assertEquals(userHandle, actual.getUserHandle());
      assertEquals(new AccountId("aaaaaaaa"), actual.getAccountId());
      assertEquals(new AccountName("テストユーザー"), actual.getAccountName());
      assertTrue(actual.getExcludeCredentialIds().isEmpty());

      ArgumentCaptor<WebAuthnChallengeModel> captor =
          ArgumentCaptor.forClass(WebAuthnChallengeModel.class);
      verify(webAuthnChallengeRepositoryImpl).save(captor.capture());
      assertEquals(WebAuthnChallengePurposeEnum.SIGNUP, captor.getValue().getPurposeKbn());
      assertNull(captor.getValue().getAccountNo());
      assertEquals(userHandle, captor.getValue().getUserHandle());
      // アカウントはまだ作成しない
      verify(accountRepositoryImpl, never()).regist(any());
    }

    @Test
    @Order(2)
    @DisplayName("異常系：アカウントIDが既に使われている場合、RegistFailureExceptionをスローしチャレンジを発行しないこと")
    void createSignupOptions_account_already_exist() {
      doReturn(true).when(accountRepositoryImpl).isExistAccount(new AccountId("aaaaaaaa"));

      RegistFailureException exception =
          assertThrows(
              RegistFailureException.class,
              () ->
                  accountServiceImpl.createSignupOptions(
                      new AccountId("aaaaaaaa"), new AccountName("テストユーザー")));
      assertEquals(ErrorEnum.DUPLICATE_ACCOUNT_ID.getErrorCode(), exception.getErrorCode());
      verify(webAuthnChallengeRepositoryImpl, never()).save(any());
    }
  }

  private static com.web.gallery.application.model.common.KbnMstModel newPrefectureKbnMstModel(
      String kbnCode) {
    return com.web.gallery.application.model.common.KbnMstModel.builder()
        .kbnClassCode(new KbnClassCode(Consts.PREFECTURE))
        .kbnCode(new com.web.gallery.domain.model.common.KbnCode(kbnCode))
        .sortOrder(new com.web.gallery.domain.model.common.SortOrder(1))
        .kbnGroupCode(new com.web.gallery.domain.model.common.KbnGroupCode("group"))
        .kbnClassJapaneseName(new com.web.gallery.domain.model.common.KbnClassJapaneseName("都道府県"))
        .kbnGroupJapaneseName(new com.web.gallery.domain.model.common.KbnGroupJapaneseName("グループ"))
        .kbnJapaneseName(new com.web.gallery.domain.model.common.KbnJapaneseName("東京都"))
        .kbnClassEnglishName(
            new com.web.gallery.domain.model.common.KbnClassEnglishName("prefecture"))
        .kbnGroupEnglishName(new com.web.gallery.domain.model.common.KbnGroupEnglishName("group"))
        .kbnEnglishName(new com.web.gallery.domain.model.common.KbnEnglishName("Tokyo"))
        .explanation(new com.web.gallery.domain.model.common.Explanation(""))
        .build();
  }

  @Nested
  @Order(3)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class updateAccount {
    @Test
    @Order(1)
    @DisplayName("正常系：アカウントを更新")
    void updateAccount_success() throws GalleryException {
      AccountModel accountModel =
          AccountModel.builder()
              .accountNo(new AccountNo(1L))
              .accountId(new AccountId("aaaaaaaa"))
              .build();
      doReturn(false)
          .when(accountRepositoryImpl)
          .isExistAccount(new AccountNo(1L), new AccountId("aaaaaaaa"));
      doNothing().when(accountRepositoryImpl).update(accountModel);
      assertFalse(accountServiceImpl.updateAccount(accountModel));

      ArgumentCaptor<AccountUpdatedEvent> accountUpdatedEventCaptor =
          ArgumentCaptor.forClass(AccountUpdatedEvent.class);
      verify(applicationEventPublisher, times(1)).publishEvent(accountUpdatedEventCaptor.capture());
      assertEquals(new AccountNo(1L), accountUpdatedEventCaptor.getValue().accountNo());
      assertEquals(new AccountId("aaaaaaaa"), accountUpdatedEventCaptor.getValue().accountId());
      // 更新前アカウントが取得できない場合、previousAccountId は null（リスナー側で全消去にフォールバック）
      assertNull(accountUpdatedEventCaptor.getValue().previousAccountId());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：アカウントが既に存在する")
    void updateAccount_account_already_exist() throws GalleryException {
      AccountModel accountModel =
          AccountModel.builder()
              .accountNo(new AccountNo(1L))
              .accountId(new AccountId("aaaaaaaa"))
              .build();
      doReturn(true)
          .when(accountRepositoryImpl)
          .isExistAccount(new AccountNo(1L), new AccountId("aaaaaaaa"));
      assertTrue(accountServiceImpl.updateAccount(accountModel));
      verify(accountRepositoryImpl, never()).update(accountModel);
      verify(applicationEventPublisher, never()).publishEvent(any());
    }

    @Test
    @Order(3)
    @DisplayName("異常系：UpdateFailureExceptionをthrowする")
    void updateAccount_UpdateFailureException() throws GalleryException {
      AccountModel accountModel =
          AccountModel.builder()
              .accountNo(new AccountNo(1L))
              .accountId(new AccountId("aaaaaaaa"))
              .build();
      doReturn(false)
          .when(accountRepositoryImpl)
          .isExistAccount(new AccountNo(1L), new AccountId("aaaaaaaa"));
      doThrow(UpdateFailureException.class).when(accountRepositoryImpl).update(accountModel);
      assertThrows(
          UpdateFailureException.class, () -> accountServiceImpl.updateAccount(accountModel));
      verify(applicationEventPublisher, never()).publishEvent(any());
    }

    @Test
    @Order(4)
    @DisplayName("正常系：アカウントIDを変更しない場合はリフレッシュトークンを失効しない")
    void updateAccount_does_not_revoke_refresh_tokens_without_account_id_change()
        throws GalleryException {
      AccountModel accountModel =
          AccountModel.builder()
              .accountNo(new AccountNo(1L))
              .accountId(new AccountId("aaaaaaaa"))
              .build();
      AccountModel storedAccount =
          AccountModel.builder()
              .accountNo(new AccountNo(1L))
              .accountId(new AccountId("aaaaaaaa"))
              .build();
      doReturn(false)
          .when(accountRepositoryImpl)
          .isExistAccount(new AccountNo(1L), new AccountId("aaaaaaaa"));
      doReturn(storedAccount).when(accountRepositoryImpl).getByAccountNo(new AccountNo(1L));
      doNothing().when(accountRepositoryImpl).update(accountModel);

      assertFalse(accountServiceImpl.updateAccount(accountModel));

      verify(refreshTokenRepositoryImpl, never()).revokeAllByAccountNo(any());
    }

    @Test
    @Order(5)
    @DisplayName("正常系：アカウントID変更時はリフレッシュトークンを全失効する")
    void updateAccount_revokes_refresh_tokens_on_account_id_change() throws GalleryException {
      AccountModel accountModel =
          AccountModel.builder()
              .accountNo(new AccountNo(1L))
              .accountId(new AccountId("newaccountid"))
              .build();
      AccountModel storedAccount =
          AccountModel.builder()
              .accountNo(new AccountNo(1L))
              .accountId(new AccountId("oldaccountid"))
              .build();
      doReturn(false)
          .when(accountRepositoryImpl)
          .isExistAccount(new AccountNo(1L), new AccountId("newaccountid"));
      doReturn(storedAccount).when(accountRepositoryImpl).getByAccountNo(new AccountNo(1L));
      doNothing().when(accountRepositoryImpl).update(accountModel);

      assertFalse(accountServiceImpl.updateAccount(accountModel));

      verify(refreshTokenRepositoryImpl, times(1)).revokeAllByAccountNo(new AccountNo(1L));

      // アカウントID変更時は新旧IDを載せ、リスナー側で新旧エントリを個別失効させる
      ArgumentCaptor<AccountUpdatedEvent> eventCaptor =
          ArgumentCaptor.forClass(AccountUpdatedEvent.class);
      verify(applicationEventPublisher, times(1)).publishEvent(eventCaptor.capture());
      assertEquals(new AccountId("newaccountid"), eventCaptor.getValue().accountId());
      assertEquals(new AccountId("oldaccountid"), eventCaptor.getValue().previousAccountId());
    }
  }

  @Nested
  @Order(4)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class getAccountById {
    @Test
    @Order(1)
    @DisplayName("正常系：アカウントが存在する場合")
    void getAccountById_found() {
      AccountModel account =
          AccountModel.builder()
              .accountNo(new AccountNo(1L))
              .accountId(new AccountId("aaaaaaaa"))
              .accountName(new AccountName("AAAAAAAA"))
              .birthdate(null)
              .sexKbn(null)
              .birthplacePrefectureKbnCode(new BirthplacePrefectureKbnCode("none"))
              .residentPrefectureKbnCode(new ResidentPrefectureKbnCode("none"))
              .freeMemo(new FreeMemo(""))
              .authorityKbn(null)
              .lastLoginDatetime(null)
              .loginFailureCount(new LoginFailureCount(0))
              .build();
      doReturn(account).when(accountRepositoryImpl).getByAccountId(new AccountId("aaaaaaaa"));

      AccountModel actual = accountServiceImpl.getAccountById(new AccountId("aaaaaaaa"));

      assertEquals(new AccountNo(1L), actual.getAccountNo());
      assertEquals(new AccountId("aaaaaaaa"), actual.getAccountId());
      assertEquals(new AccountName("AAAAAAAA"), actual.getAccountName());
      assertEquals(
          new BirthplacePrefectureKbnCode("none"), actual.getBirthplacePrefectureKbnCode());
      assertEquals(new ResidentPrefectureKbnCode("none"), actual.getResidentPrefectureKbnCode());
      assertEquals(new FreeMemo(""), actual.getFreeMemo());
      assertEquals(new LoginFailureCount(0), actual.getLoginFailureCount());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：アカウントが存在しない場合、nullを返す")
    void getAccountById_not_found() {
      doReturn(null).when(accountRepositoryImpl).getByAccountId(new AccountId("aaaaaaaa"));
      assertNull(accountServiceImpl.getAccountById(new AccountId("aaaaaaaa")));
    }
  }

  @Nested
  @Order(5)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class getAccountList {
    @Test
    @Order(1)
    @DisplayName("正常系：アカウントが存在する場合")
    void getAccountList_found() {
      AccountModelList accountModelList =
          AccountModelList.of(
              List.of(
                  AccountModel.builder().accountId(new AccountId("cccccccc")).build(),
                  AccountModel.builder().accountId(new AccountId("bbbbbbbb")).build(),
                  AccountModel.builder().accountId(new AccountId("aaaaaaaa")).build()));

      doReturn(5).when(accountConfig).getAccountCountPerPage();

      ArgumentCaptor<AccountGetModel> accountGetModelCaptor =
          ArgumentCaptor.forClass(AccountGetModel.class);
      doReturn(AccountPageModel.of(accountModelList, true))
          .when(accountRepositoryImpl)
          .getAccountList(accountGetModelCaptor.capture());

      AccountListGetModel accountListGetModel = AccountListGetModel.builder().pageNo(1).build();
      AccountPageModel actual = accountServiceImpl.getAccountList(accountListGetModel);
      assertEquals(accountModelList.size(), actual.getAccountModelList().size());
      assertEquals(new AccountId("aaaaaaaa"), actual.getAccountModelList().get(0).getAccountId());
      assertEquals(new AccountId("bbbbbbbb"), actual.getAccountModelList().get(1).getAccountId());
      assertEquals(new AccountId("cccccccc"), actual.getAccountModelList().get(2).getAccountId());
      assertTrue(actual.getIsLast());

      AccountGetModel accountGetModel = accountGetModelCaptor.getValue();
      assertEquals(6, accountGetModel.getLimit());
      assertEquals(0, accountGetModel.getOffset());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：アカウントが存在しない場合")
    void getAccountList_not_found() {
      doReturn(5).when(accountConfig).getAccountCountPerPage();
      doReturn(AccountPageModel.of(AccountModelList.empty(), true))
          .when(accountRepositoryImpl)
          .getAccountList(any(AccountGetModel.class));

      AccountListGetModel accountListGetModel = AccountListGetModel.builder().pageNo(1).build();
      AccountPageModel actual = accountServiceImpl.getAccountList(accountListGetModel);
      assertEquals(0, actual.getAccountModelList().size());
    }
  }

  @Nested
  @Order(6)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class getAccountListForAdmin {
    @Test
    @Order(1)
    @DisplayName("正常系：全アカウントを取得（削除済み含む）してソートされること")
    void getAccountListForAdmin_found() {
      AccountModelList accountModelList =
          AccountModelList.of(
              List.of(
                  AccountModel.builder()
                      .accountId(new AccountId("cccccccc"))
                      .isDeleted(new IsDeleted(true))
                      .build(),
                  AccountModel.builder()
                      .accountId(new AccountId("bbbbbbbb"))
                      .isDeleted(new IsDeleted(false))
                      .build(),
                  AccountModel.builder()
                      .accountId(new AccountId("aaaaaaaa"))
                      .isDeleted(new IsDeleted(false))
                      .build()));

      doReturn(5).when(accountConfig).getAccountCountPerPage();

      ArgumentCaptor<AccountGetModel> accountGetModelCaptor =
          ArgumentCaptor.forClass(AccountGetModel.class);
      doReturn(AccountPageModel.of(accountModelList, true))
          .when(accountRepositoryImpl)
          .getAccountListForAdmin(accountGetModelCaptor.capture());

      AccountListGetModel accountListGetModel = AccountListGetModel.builder().pageNo(1).build();
      AccountPageModel actual = accountServiceImpl.getAccountListForAdmin(accountListGetModel);
      assertEquals(3, actual.getAccountModelList().size());
      assertEquals(new AccountId("aaaaaaaa"), actual.getAccountModelList().get(0).getAccountId());
      assertEquals(new AccountId("bbbbbbbb"), actual.getAccountModelList().get(1).getAccountId());
      assertEquals(new AccountId("cccccccc"), actual.getAccountModelList().get(2).getAccountId());
      assertTrue(actual.getIsLast());

      AccountGetModel accountGetModel = accountGetModelCaptor.getValue();
      assertEquals(6, accountGetModel.getLimit());
      assertEquals(0, accountGetModel.getOffset());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：アカウントが存在しない場合")
    void getAccountListForAdmin_not_found() {
      doReturn(5).when(accountConfig).getAccountCountPerPage();
      doReturn(AccountPageModel.of(AccountModelList.empty(), true))
          .when(accountRepositoryImpl)
          .getAccountListForAdmin(any(AccountGetModel.class));

      AccountListGetModel accountListGetModel = AccountListGetModel.builder().pageNo(1).build();
      AccountPageModel actual = accountServiceImpl.getAccountListForAdmin(accountListGetModel);
      assertEquals(0, actual.getAccountModelList().size());
    }
  }

  @Nested
  @Order(7)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class unlockAccount {
    @Test
    @Order(1)
    @DisplayName("正常系：管理者ロックとログイン失敗回数の両方が解除されること")
    void unlockAccount_success() throws GalleryException {
      ArgumentCaptor<AccountModel> captor = ArgumentCaptor.forClass(AccountModel.class);
      doNothing().when(accountRepositoryImpl).updateLoginFailureCount(captor.capture());

      accountServiceImpl.unlockAccount(new AccountNo(1L));

      AccountModel accountModel = captor.getValue();
      assertEquals(new AccountNo(1L), accountModel.getAccountNo());
      assertEquals(new LoginFailureCount(0), accountModel.getLoginFailureCount());
      assertEquals(new IsAdminLocked(false), accountModel.getIsAdminLocked());
      assertNull(accountModel.getLastLoginDatetime());

      ArgumentCaptor<AccountUnlockedEvent> eventCaptor =
          ArgumentCaptor.forClass(AccountUnlockedEvent.class);
      verify(applicationEventPublisher, times(1)).publishEvent(eventCaptor.capture());
      assertEquals(new AccountNo(1L), eventCaptor.getValue().accountNo());
    }

    @Test
    @Order(2)
    @DisplayName("異常系：UpdateFailureExceptionをthrowする")
    void unlockAccount_UpdateFailureException() throws GalleryException {
      doThrow(UpdateFailureException.class)
          .when(accountRepositoryImpl)
          .updateLoginFailureCount(any(AccountModel.class));
      assertThrows(
          UpdateFailureException.class,
          () -> accountServiceImpl.unlockAccount(new AccountNo(999L)));
    }
  }

  @Nested
  @Order(8)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class lockAccount {
    @Test
    @Order(1)
    @DisplayName("正常系：管理者ロックフラグが立てられ、ログイン失敗回数も上限値に設定されること")
    void lockAccount_success() throws GalleryException {
      doReturn(10).when(loginConfig).getFailCount();

      ArgumentCaptor<AccountModel> captor = ArgumentCaptor.forClass(AccountModel.class);
      doNothing().when(accountRepositoryImpl).updateLoginFailureCount(captor.capture());

      accountServiceImpl.lockAccount(new AccountNo(1L));

      AccountModel accountModel = captor.getValue();
      assertEquals(new AccountNo(1L), accountModel.getAccountNo());
      assertEquals(new IsAdminLocked(true), accountModel.getIsAdminLocked());
      assertEquals(new LoginFailureCount(10), accountModel.getLoginFailureCount());

      ArgumentCaptor<AccountLockedEvent> eventCaptor =
          ArgumentCaptor.forClass(AccountLockedEvent.class);
      verify(applicationEventPublisher, times(1)).publishEvent(eventCaptor.capture());
      assertEquals(new AccountNo(1L), eventCaptor.getValue().accountNo());
    }

    @Test
    @Order(2)
    @DisplayName("異常系：UpdateFailureExceptionをthrowする")
    void lockAccount_UpdateFailureException() throws GalleryException {
      doReturn(10).when(loginConfig).getFailCount();
      doThrow(UpdateFailureException.class)
          .when(accountRepositoryImpl)
          .updateLoginFailureCount(any(AccountModel.class));
      assertThrows(
          UpdateFailureException.class, () -> accountServiceImpl.lockAccount(new AccountNo(999L)));
    }
  }

  @Nested
  @Order(9)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class deleteAccount {
    @Test
    @Order(1)
    @DisplayName("正常系：アカウントを削除する")
    void deleteAccount_success() throws GalleryException {
      Long accountNo = 1L;
      String accountId = "aaaaaaaa";

      AccountModel storedAccount =
          AccountModel.builder().accountNo(new AccountNo(accountNo)).build();
      doReturn(storedAccount).when(accountRepositoryImpl).getByAccountNo(new AccountNo(accountNo));
      stubReauthSuccess();

      doAnswer(
              invocation -> {
                Account account = invocation.getArgument(0);
                account.recordDeletedPhotoNos(
                    PhotoNoList.of(List.of(new PhotoNo(1L), new PhotoNo(2L))));
                return null;
              })
          .when(accountAggregateRepositoryImpl)
          .delete(any(Account.class));

      doNothing().when(fileRepository).deleteByPrefix(new ImageFilePath(accountId + "/"));
      // 他ユーザーのお問い合わせへ投稿した返信が無いため削除は許可される
      doReturn(false)
          .when(inquiryReplyMstRepositoryImpl)
          .existsReplyToOthersInquiry(new AccountNo(accountNo));

      accountServiceImpl.deleteAccount(
          new AccountNo(accountNo), new AccountId(accountId), REAUTH_JSON);

      ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
      verify(accountAggregateRepositoryImpl, times(1)).delete(accountCaptor.capture());
      assertEquals(new AccountNo(accountNo), accountCaptor.getValue().getAccountNo());
      assertTrue(accountCaptor.getValue().isDeleted());

      // S3のプレフィックスは変更されうるアカウントIDではなく不変のアカウント番号を用いる
      verify(fileRepository, times(1)).deleteByPrefix(new ImageFilePath(accountNo + "/"));

      ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
      verify(applicationEventPublisher, times(3)).publishEvent(eventCaptor.capture());
      List<Object> publishedEvents = eventCaptor.getAllValues();

      List<PhotoDeletedEvent> photoDeletedEvents =
          publishedEvents.stream()
              .filter(PhotoDeletedEvent.class::isInstance)
              .map(PhotoDeletedEvent.class::cast)
              .toList();
      assertEquals(2, photoDeletedEvents.size());
      assertEquals(new PhotoNo(1L), photoDeletedEvents.get(0).photoNo());
      assertEquals(new PhotoNo(2L), photoDeletedEvents.get(1).photoNo());

      List<AccountDeletedEvent> accountDeletedEvents =
          publishedEvents.stream()
              .filter(AccountDeletedEvent.class::isInstance)
              .map(AccountDeletedEvent.class::cast)
              .toList();
      assertEquals(1, accountDeletedEvents.size());
      assertEquals(new AccountNo(accountNo), accountDeletedEvents.get(0).accountNo());
      assertEquals(new AccountId(accountId), accountDeletedEvents.get(0).accountId());
    }

    @Test
    @Order(2)
    @DisplayName("異常系：パスキーによる本人確認に失敗した場合はForbiddenAccountExceptionをthrowし、削除しない")
    void deleteAccount_reauthentication_failed() {
      AccountModel storedAccount = AccountModel.builder().accountNo(new AccountNo(1L)).build();
      doReturn(storedAccount).when(accountRepositoryImpl).getByAccountNo(new AccountNo(1L));
      stubReauthLookup(new AccountNo(1L), new AccountNo(1L));
      doThrow(new PasskeyVerificationException("invalid signature"))
          .when(webAuthnVerifier)
          .verifyAuthentication(eq(REAUTH_JSON), eq(REAUTH_CHALLENGE), any());

      ForbiddenAccountException exception =
          assertThrows(
              ForbiddenAccountException.class,
              () ->
                  accountServiceImpl.deleteAccount(
                      new AccountNo(1L), new AccountId("aaaaaaaa"), REAUTH_JSON));
      assertEquals(ErrorEnum.REAUTHENTICATION_FAILED.getErrorCode(), exception.getErrorCode());

      verify(accountAggregateRepositoryImpl, never()).delete(any(Account.class));
      verify(applicationEventPublisher, never()).publishEvent(any());
      verify(passkeyCredentialRepositoryImpl, never()).updateAuthenticationResult(any());
      // 再認証失敗はインメモリのスロットルに記録される
      verify(reauthenticationThrottle, times(1)).recordFailure(1L);
    }

    @Test
    @Order(3)
    @DisplayName("異常系：他のアカウントのパスキーで署名した場合はForbiddenAccountExceptionをthrowし、チャレンジを消費しない")
    void deleteAccount_passkey_of_other_account() {
      AccountModel storedAccount = AccountModel.builder().accountNo(new AccountNo(1L)).build();
      doReturn(storedAccount).when(accountRepositoryImpl).getByAccountNo(new AccountNo(1L));
      doReturn(
              PasskeyAssertionModel.builder()
                  .credentialId(new CredentialId("credential-other"))
                  .challenge(REAUTH_CHALLENGE)
                  .build())
          .when(webAuthnVerifier)
          .readAuthentication(REAUTH_JSON);
      PasskeyCredentialModel otherAccountsPasskey =
          PasskeyCredentialModel.builder()
              .passkeyNo(new PasskeyNo(9L))
              .accountNo(new AccountNo(2L))
              .credentialId(new CredentialId("credential-other"))
              .attestedCredentialData(new AttestedCredentialData(new byte[] {1}))
              .signCount(new SignCount(0L))
              .transports(new PasskeyTransports("internal"))
              .isBackupEligible(new IsBackupEligible(true))
              .isBackedUp(new IsBackedUp(true))
              .isUvInitialized(new IsUvInitialized(true))
              .passkeyName(new PasskeyName("他人のパスキー"))
              .build();
      doReturn(otherAccountsPasskey)
          .when(passkeyCredentialRepositoryImpl)
          .findByCredentialIdForUpdate(new CredentialId("credential-other"));

      assertThrows(
          ForbiddenAccountException.class,
          () ->
              accountServiceImpl.deleteAccount(
                  new AccountNo(1L), new AccountId("aaaaaaaa"), REAUTH_JSON));

      verify(webAuthnChallengeRepositoryImpl, never()).consumeForReauthentication(any());
      verify(accountAggregateRepositoryImpl, never()).delete(any(Account.class));
    }

    @Test
    @Order(4)
    @DisplayName("異常系：管理者として他ユーザーのお問い合わせへ投稿した返信が残っている場合はBadRequestExceptionをthrowし、削除しない")
    void deleteAccount_with_admin_reply() {
      AccountModel storedAccount = AccountModel.builder().accountNo(new AccountNo(1L)).build();
      doReturn(storedAccount).when(accountRepositoryImpl).getByAccountNo(new AccountNo(1L));
      stubReauthSuccess();
      doReturn(true)
          .when(inquiryReplyMstRepositoryImpl)
          .existsReplyToOthersInquiry(new AccountNo(1L));

      // 返信を巻き込んで削除すると無関係な第三者のお問い合わせから回答本文だけが消えるため、
      // 業務ルールとして削除自体を拒否する
      assertThrows(
          BadRequestException.class,
          () ->
              accountServiceImpl.deleteAccount(
                  new AccountNo(1L), new AccountId("aaaaaaaa"), REAUTH_JSON));

      verify(accountAggregateRepositoryImpl, never()).delete(any(Account.class));
      verify(fileRepository, never()).deleteByPrefix(any(ImageFilePath.class));
      verify(applicationEventPublisher, never()).publishEvent(any());
      // 本人確認には成功しているため、再認証の失敗としては記録しない
      verify(reauthenticationThrottle, never()).recordFailure(1L);
    }
  }

  @Nested
  @Order(10)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class handleAuthenticationSuccess {
    @Test
    @Order(1)
    @DisplayName("正常系：ログイン失敗回数をリセットし、ログイン履歴を記録すること")
    void handle_success() throws GalleryException {
      String username = "aaaaaaaa";
      String password = "AAAAAAAA";

      List<GrantedAuthority> authorities = new ArrayList<>();
      authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));

      Authentication authentication =
          new UsernamePasswordAuthenticationToken(username, password, authorities);
      AuthenticationSuccessEvent event = new AuthenticationSuccessEvent(authentication);

      AccountModel account = AccountModel.builder().accountNo(new AccountNo(1L)).build();
      doReturn(account).when(accountRepositoryImpl).getByAccountId(new AccountId(username));

      ArgumentCaptor<AccountModel> accountModelCaptor = ArgumentCaptor.forClass(AccountModel.class);
      doNothing().when(accountRepositoryImpl).updateLoginFailureCount(accountModelCaptor.capture());

      accountServiceImpl.handle(event);

      AccountModel accountModel = accountModelCaptor.getValue();
      assertEquals(new AccountNo(1L), accountModel.getAccountNo());
      assertNull(accountModel.getAccountId());
      assertNull(accountModel.getAccountName());
      assertNull(accountModel.getBirthdate());
      assertNull(accountModel.getSexKbn());
      assertNull(accountModel.getBirthplacePrefectureKbnCode());
      assertNull(accountModel.getResidentPrefectureKbnCode());
      assertNull(accountModel.getFreeMemo());
      assertNull(accountModel.getAuthorityKbn());
      assertEquals(OffsetDateTime.now(clock), accountModel.getLastLoginDatetime().value());
      assertEquals(new LoginFailureCount(0), accountModel.getLoginFailureCount());
    }

    @Test
    @Order(2)
    @DisplayName("異常系：UpdateFailureExceptionをthrowする")
    void handle_UpdateFailureException() throws GalleryException {
      String username = "aaaaaaaa";
      String password = "AAAAAAAA";

      List<GrantedAuthority> authorities = new ArrayList<>();
      authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));

      Authentication authentication =
          new UsernamePasswordAuthenticationToken(username, password, authorities);
      AuthenticationSuccessEvent event = new AuthenticationSuccessEvent(authentication);

      AccountModel account = AccountModel.builder().accountNo(new AccountNo(1L)).build();
      doReturn(account).when(accountRepositoryImpl).getByAccountId(new AccountId(username));

      ArgumentCaptor<AccountModel> accountModelCaptor = ArgumentCaptor.forClass(AccountModel.class);
      doThrow(UpdateFailureException.class)
          .when(accountRepositoryImpl)
          .updateLoginFailureCount(accountModelCaptor.capture());

      assertThrows(UpdateFailureException.class, () -> accountServiceImpl.handle(event));

      AccountModel accountModel = accountModelCaptor.getValue();
      assertEquals(new AccountNo(1L), accountModel.getAccountNo());
      assertNull(accountModel.getAccountId());
      assertNull(accountModel.getAccountName());
      assertNull(accountModel.getBirthdate());
      assertNull(accountModel.getSexKbn());
      assertNull(accountModel.getBirthplacePrefectureKbnCode());
      assertNull(accountModel.getResidentPrefectureKbnCode());
      assertNull(accountModel.getFreeMemo());
      assertNull(accountModel.getAuthorityKbn());
      assertNotNull(accountModel.getLastLoginDatetime());
      assertEquals(new LoginFailureCount(0), accountModel.getLoginFailureCount());
    }

    @Test
    @Order(3)
    @DisplayName("正常系：認証情報にIPアドレス・ログイン方式が設定されている場合、ログイン履歴が記録されること")
    void handle_recordsLoginHistory() throws GalleryException {
      String username = "aaaaaaaa";
      String password = "AAAAAAAA";
      IpAddress ipAddress = new IpAddress("203.0.113.1");

      UsernamePasswordAuthenticationToken authentication =
          new UsernamePasswordAuthenticationToken(username, password, new ArrayList<>());
      authentication.setDetails(LoginContextModel.of(ipAddress, LoginMethodEnum.PASSKEY));
      AuthenticationSuccessEvent event = new AuthenticationSuccessEvent(authentication);

      AccountModel account = AccountModel.builder().accountNo(new AccountNo(1L)).build();
      doReturn(account).when(accountRepositoryImpl).getByAccountId(new AccountId(username));

      IpGeoLocation geoLocation = IpGeoLocation.empty();
      doReturn(geoLocation).when(geoIpResolver).resolve(ipAddress);

      accountServiceImpl.handle(event);

      ArgumentCaptor<LoginHistoryModel> loginHistoryModelCaptor =
          ArgumentCaptor.forClass(LoginHistoryModel.class);
      verify(loginHistoryRepositoryImpl).save(loginHistoryModelCaptor.capture());
      LoginHistoryModel loginHistoryModel = loginHistoryModelCaptor.getValue();
      assertEquals(new AccountNo(1L), loginHistoryModel.getAccountNo());
      assertEquals(ipAddress, loginHistoryModel.getIpAddress());
      assertEquals(LoginMethodEnum.PASSKEY, loginHistoryModel.getLoginMethodKbn());
    }

    @Test
    @Order(4)
    @DisplayName("正常系：認証情報にIPアドレスが設定されていない場合、ログイン履歴は記録されないこと")
    void handle_doesNotRecordLoginHistory_whenDetailsMissing() throws GalleryException {
      String username = "aaaaaaaa";
      String password = "AAAAAAAA";

      Authentication authentication =
          new UsernamePasswordAuthenticationToken(username, password, new ArrayList<>());
      AuthenticationSuccessEvent event = new AuthenticationSuccessEvent(authentication);

      AccountModel account = AccountModel.builder().accountNo(new AccountNo(1L)).build();
      doReturn(account).when(accountRepositoryImpl).getByAccountId(new AccountId(username));

      accountServiceImpl.handle(event);

      verifyNoInteractions(loginHistoryRepositoryImpl);
    }

    @Test
    @Order(5)
    @DisplayName("正常系：対象アカウントが取得できない場合、何も更新せず終了すること")
    void handle_accountNotFound() throws GalleryException {
      String username = "aaaaaaaa";
      String password = "AAAAAAAA";

      Authentication authentication =
          new UsernamePasswordAuthenticationToken(username, password, new ArrayList<>());
      AuthenticationSuccessEvent event = new AuthenticationSuccessEvent(authentication);

      doReturn(null).when(accountRepositoryImpl).getByAccountId(new AccountId(username));

      accountServiceImpl.handle(event);

      verify(accountRepositoryImpl, never()).updateLoginFailureCount(any());
      verifyNoInteractions(loginHistoryRepositoryImpl);
    }
  }

  @Nested
  @Order(12)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class reauthenticationThrottling {
    @Test
    @Order(1)
    @DisplayName("異常系：スロットルがロックアウト中なら本人確認を通さず署名も検証せず、失敗を記録してロックアウトを延長する")
    void reauth_blocked_when_throttled() {
      AccountModel storedAccount = AccountModel.builder().accountNo(new AccountNo(1L)).build();
      doReturn(storedAccount).when(accountRepositoryImpl).getByAccountNo(new AccountNo(1L));
      doReturn(true).when(reauthenticationThrottle).isLockedOut(1L);

      assertThrows(
          ForbiddenAccountException.class,
          () ->
              accountServiceImpl.deleteAccount(
                  new AccountNo(1L), new AccountId("aaaaaaaa"), REAUTH_JSON));

      verify(webAuthnVerifier, never()).readAuthentication(any());
      verify(accountAggregateRepositoryImpl, never()).delete(any(Account.class));
      // ロックアウト中の試行でも直近失敗時刻を更新する（スライディングウィンドウ）
      verify(reauthenticationThrottle, times(1)).recordFailure(1L);
    }

    @Test
    @Order(2)
    @DisplayName("正常系：本人確認に成功するとスロットルの失敗カウンタをリセットし、署名カウンタ等を更新する")
    void reauth_success_resets_throttle() throws GalleryException {
      stubAccountForReauth();
      accountServiceImpl.regenerateRecoveryCodes(new AccountNo(1L), REAUTH_JSON);

      verify(reauthenticationThrottle, times(1)).reset(1L);
      ArgumentCaptor<PasskeyCredentialModel> captor =
          ArgumentCaptor.forClass(PasskeyCredentialModel.class);
      verify(passkeyCredentialRepositoryImpl).updateAuthenticationResult(captor.capture());
      assertEquals(new SignCount(6L), captor.getValue().getSignCount());
      assertEquals(OffsetDateTime.now(clock), captor.getValue().getLastUsedAt().value());
    }

    @Test
    @Order(3)
    @DisplayName("異常系：ログイン失敗回数が上限に達したアカウントは本人確認を通さない")
    void reauth_blocked_when_account_locked() {
      AccountModel storedAccount =
          AccountModel.builder()
              .accountNo(new AccountNo(1L))
              .loginFailureCount(new LoginFailureCount(3))
              .build();
      doReturn(storedAccount).when(accountRepositoryImpl).getByAccountNo(new AccountNo(1L));
      doReturn(3).when(loginConfig).getFailCount();

      assertThrows(
          ForbiddenAccountException.class,
          () -> accountServiceImpl.regenerateRecoveryCodes(new AccountNo(1L), REAUTH_JSON));

      verify(webAuthnVerifier, never()).readAuthentication(any());
      verify(recoveryCodeRepositoryImpl, never()).saveAll(any(), any());
    }

    @Test
    @Order(4)
    @DisplayName("異常系：管理者ロック中のアカウントは本人確認を通さない")
    void reauth_blocked_when_admin_locked() {
      AccountModel storedAccount =
          AccountModel.builder()
              .accountNo(new AccountNo(1L))
              .isAdminLocked(new IsAdminLocked(true))
              .build();
      doReturn(storedAccount).when(accountRepositoryImpl).getByAccountNo(new AccountNo(1L));

      assertThrows(
          ForbiddenAccountException.class,
          () -> accountServiceImpl.regenerateRecoveryCodes(new AccountNo(1L), REAUTH_JSON));

      verify(webAuthnVerifier, never()).readAuthentication(any());
    }

    @Test
    @Order(5)
    @DisplayName("異常系：本人確認用チャレンジが未発行・使用済み・期限切れ（ログイン用など別用途を含む）の場合は本人確認を通さない")
    void reauth_challenge_not_available() {
      stubAccountForReauthLookupOnly();
      doReturn(null)
          .when(webAuthnChallengeRepositoryImpl)
          .consumeForReauthentication(REAUTH_CHALLENGE);

      assertThrows(
          ForbiddenAccountException.class,
          () -> accountServiceImpl.regenerateRecoveryCodes(new AccountNo(1L), REAUTH_JSON));

      verify(webAuthnVerifier, never()).verifyAuthentication(any(), any(), any());
      verify(reauthenticationThrottle, times(1)).recordFailure(1L);
    }

    @Test
    @Order(6)
    @DisplayName("異常系：他のアカウントに発行された本人確認用チャレンジの場合は本人確認を通さない")
    void reauth_challenge_of_other_account() {
      stubAccountForReauthLookupOnly();
      doReturn(
              WebAuthnChallengeModel.of(
                  REAUTH_CHALLENGE,
                  WebAuthnChallengePurposeEnum.REAUTHENTICATION,
                  new AccountNo(2L),
                  new ExpiresAt(OffsetDateTime.now(clock).plusMinutes(5))))
          .when(webAuthnChallengeRepositoryImpl)
          .consumeForReauthentication(REAUTH_CHALLENGE);

      assertThrows(
          ForbiddenAccountException.class,
          () -> accountServiceImpl.regenerateRecoveryCodes(new AccountNo(1L), REAUTH_JSON));

      verify(webAuthnVerifier, never()).verifyAuthentication(any(), any(), any());
    }
  }

  @Nested
  @Order(13)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class updateAccountAuthority {
    @Test
    @Order(1)
    @DisplayName("正常系：権限が更新され、AccountAuthorityChangedEventが発行されること")
    void updateAccountAuthority_success() throws GalleryException {
      ArgumentCaptor<AccountModel> captor = ArgumentCaptor.forClass(AccountModel.class);
      doNothing().when(accountRepositoryImpl).updateAuthority(captor.capture());

      AccountModel accountModel = AccountModel.forAuthorityChange(1L, AuthorityEnum.NORMAL);
      accountServiceImpl.updateAccountAuthority(accountModel);

      AccountModel capturedModel = captor.getValue();
      assertEquals(new AccountNo(1L), capturedModel.getAccountNo());
      assertEquals(AuthorityEnum.NORMAL, capturedModel.getAuthorityKbn());

      ArgumentCaptor<AccountAuthorityChangedEvent> eventCaptor =
          ArgumentCaptor.forClass(AccountAuthorityChangedEvent.class);
      verify(applicationEventPublisher, times(1)).publishEvent(eventCaptor.capture());
      assertEquals(new AccountNo(1L), eventCaptor.getValue().accountNo());
    }

    @Test
    @Order(2)
    @DisplayName("異常系：UpdateFailureExceptionをthrowする")
    void updateAccountAuthority_UpdateFailureException() throws GalleryException {
      doThrow(UpdateFailureException.class)
          .when(accountRepositoryImpl)
          .updateAuthority(any(AccountModel.class));

      AccountModel accountModel = AccountModel.forAuthorityChange(999L, AuthorityEnum.NORMAL);
      assertThrows(
          UpdateFailureException.class,
          () -> accountServiceImpl.updateAccountAuthority(accountModel));
    }
  }

  /**
   * パスキー関連テスト用の、本人確認（再認証）に成功するアカウントをスタブする
   *
   * @return スタブしたアカウント
   */
  private AccountModel stubAccountForReauth() {
    AccountModel storedAccount = stubAccount();
    stubReauthSuccess();
    return storedAccount;
  }

  /**
   * パスキー関連テスト用のアカウント（アカウント番号1）をスタブする
   *
   * @return スタブしたアカウント
   */
  private AccountModel stubAccount() {
    AccountModel storedAccount =
        AccountModel.builder()
            .accountNo(new AccountNo(1L))
            .accountId(new AccountId("aaaaaaaa"))
            .accountName(new AccountName("テストユーザー"))
            .build();
    doReturn(storedAccount).when(accountRepositoryImpl).getByAccountNo(new AccountNo(1L));
    return storedAccount;
  }

  /**
   * パスキー関連テスト用のアカウントと、本人確認に使うパスキー（アカウント番号1）の引き当てまでをスタブする
   *
   * @return スタブしたアカウント
   */
  private AccountModel stubAccountForReauthLookupOnly() {
    AccountModel storedAccount = stubAccount();
    doReturn(
            PasskeyAssertionModel.builder()
                .credentialId(new CredentialId("credential-reauth"))
                .challenge(REAUTH_CHALLENGE)
                .build())
        .when(webAuthnVerifier)
        .readAuthentication(REAUTH_JSON);
    doReturn(passkey(1L, "credential-reauth"))
        .when(passkeyCredentialRepositoryImpl)
        .findByCredentialIdForUpdate(new CredentialId("credential-reauth"));
    return storedAccount;
  }

  /**
   * 本人確認（再認証）の認証レスポンスの読み取り・パスキー・チャレンジの引き当てをスタブする
   *
   * @param passkeyOwner 署名したパスキーの所有者
   * @param challengeOwner 本人確認用チャレンジの発行先
   */
  private void stubReauthLookup(AccountNo passkeyOwner, AccountNo challengeOwner) {
    doReturn(
            PasskeyAssertionModel.builder()
                .credentialId(new CredentialId("credential-reauth"))
                .challenge(REAUTH_CHALLENGE)
                .build())
        .when(webAuthnVerifier)
        .readAuthentication(REAUTH_JSON);
    PasskeyCredentialModel credential = passkey(1L, "credential-reauth");
    doReturn(
            PasskeyCredentialModel.builder()
                .passkeyNo(credential.getPasskeyNo())
                .accountNo(passkeyOwner)
                .credentialId(credential.getCredentialId())
                .attestedCredentialData(credential.getAttestedCredentialData())
                .signCount(credential.getSignCount())
                .transports(credential.getTransports())
                .isBackupEligible(credential.getIsBackupEligible())
                .isBackedUp(credential.getIsBackedUp())
                .isUvInitialized(credential.getIsUvInitialized())
                .passkeyName(credential.getPasskeyName())
                .build())
        .when(passkeyCredentialRepositoryImpl)
        .findByCredentialIdForUpdate(new CredentialId("credential-reauth"));
    doReturn(
            WebAuthnChallengeModel.of(
                REAUTH_CHALLENGE,
                WebAuthnChallengePurposeEnum.REAUTHENTICATION,
                challengeOwner,
                new ExpiresAt(OffsetDateTime.now(clock).plusMinutes(5))))
        .when(webAuthnChallengeRepositoryImpl)
        .consumeForReauthentication(REAUTH_CHALLENGE);
  }

  /** アカウント番号1の本人確認（再認証）が成功するようスタブする */
  private void stubReauthSuccess() {
    stubReauthLookup(new AccountNo(1L), new AccountNo(1L));
    doReturn(
            PasskeyAssertionResultModel.builder()
                .signCount(new SignCount(6L))
                .isBackedUp(new IsBackedUp(true))
                .build())
        .when(webAuthnVerifier)
        .verifyAuthentication(eq(REAUTH_JSON), eq(REAUTH_CHALLENGE), any());
  }

  /**
   * テスト用のパスキーを生成する
   *
   * @param passkeyNo パスキー番号
   * @param credentialId クレデンシャルID
   * @return {@link PasskeyCredentialModel}
   */
  private PasskeyCredentialModel passkey(long passkeyNo, String credentialId) {
    return PasskeyCredentialModel.builder()
        .passkeyNo(new PasskeyNo(passkeyNo))
        .accountNo(new AccountNo(1L))
        .credentialId(new CredentialId(credentialId))
        .attestedCredentialData(new AttestedCredentialData(new byte[] {1}))
        .signCount(new SignCount(0L))
        .transports(new PasskeyTransports("internal"))
        .isBackupEligible(new IsBackupEligible(true))
        .isBackedUp(new IsBackedUp(true))
        .isUvInitialized(new IsUvInitialized(true))
        .passkeyName(new PasskeyName("MacBook"))
        .build();
  }

  @Nested
  @Order(14)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class createPasskeyRegistrationOptions {
    @BeforeEach
    void setUpWebAuthnConfig() {
      lenient().doReturn("localhost").when(webAuthnConfig).getRpId();
      lenient().doReturn("WebGallery").when(webAuthnConfig).getRpName();
      lenient().doReturn(300).when(webAuthnConfig).getChallengeTimeoutSeconds();
    }

    @Test
    @Order(1)
    @DisplayName("正常系：本人確認に成功した場合、登録用チャレンジを保存し、登録済みのクレデンシャルを除外対象として返すこと")
    void createPasskeyRegistrationOptions_success() throws GalleryException {
      stubAccountForReauth();
      doReturn(1).when(passkeyCredentialRepositoryImpl).countByAccountNo(new AccountNo(1L));
      PasskeyChallenge challenge = new PasskeyChallenge("challenge-1");
      doReturn(challenge).when(webAuthnVerifier).generateChallenge();
      doReturn(new PasskeyUserHandle("handle-1"))
          .when(passkeyUserHandleRepositoryImpl)
          .findByAccountNo(new AccountNo(1L));
      doReturn(PasskeyCredentialModelList.of(List.of(passkey(1L, "credential-1"))))
          .when(passkeyCredentialRepositoryImpl)
          .findByAccountNo(new AccountNo(1L));

      PasskeyRegistrationOptionsModel actual =
          accountServiceImpl.createPasskeyRegistrationOptions(new AccountNo(1L), REAUTH_JSON);

      assertEquals(challenge, actual.getChallenge());
      assertEquals("localhost", actual.getRpId().value());
      assertEquals("WebGallery", actual.getRpName().value());
      assertEquals(new PasskeyUserHandle("handle-1"), actual.getUserHandle());
      assertEquals(new AccountId("aaaaaaaa"), actual.getAccountId());
      assertEquals(new AccountName("テストユーザー"), actual.getAccountName());
      assertEquals(List.of(new CredentialId("credential-1")), actual.getExcludeCredentialIds());
      assertEquals(300_000L, actual.getTimeoutMillis().value());

      ArgumentCaptor<WebAuthnChallengeModel> captor =
          ArgumentCaptor.forClass(WebAuthnChallengeModel.class);
      verify(webAuthnChallengeRepositoryImpl).save(captor.capture());
      assertEquals(WebAuthnChallengePurposeEnum.REGISTRATION, captor.getValue().getPurposeKbn());
      verify(reauthenticationThrottle).reset(1L);
      assertEquals(new AccountNo(1L), captor.getValue().getAccountNo());
      assertEquals(
          OffsetDateTime.now(clock).plusSeconds(300), captor.getValue().getExpiresAt().value());
      verify(passkeyUserHandleRepositoryImpl, never()).saveIfAbsent(any(), any());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：ユーザーハンドルが未発行の場合、発行して保存したものを返すこと")
    void createPasskeyRegistrationOptions_issueUserHandle() throws GalleryException {
      stubAccountForReauth();
      doReturn(0).when(passkeyCredentialRepositoryImpl).countByAccountNo(new AccountNo(1L));
      doReturn(new PasskeyChallenge("challenge-1")).when(webAuthnVerifier).generateChallenge();
      PasskeyUserHandle generated = new PasskeyUserHandle("generated-handle");
      doReturn(generated).when(webAuthnVerifier).generateUserHandle();
      doReturn(null, generated)
          .when(passkeyUserHandleRepositoryImpl)
          .findByAccountNo(new AccountNo(1L));
      doReturn(PasskeyCredentialModelList.empty())
          .when(passkeyCredentialRepositoryImpl)
          .findByAccountNo(new AccountNo(1L));

      PasskeyRegistrationOptionsModel actual =
          accountServiceImpl.createPasskeyRegistrationOptions(new AccountNo(1L), REAUTH_JSON);

      assertEquals(generated, actual.getUserHandle());
      assertTrue(actual.getExcludeCredentialIds().isEmpty());
      verify(passkeyUserHandleRepositoryImpl).saveIfAbsent(new AccountNo(1L), generated);
    }

    @Test
    @Order(3)
    @DisplayName("異常系：パスキーによる本人確認に失敗した場合、ForbiddenAccountExceptionをスローし、チャレンジを発行しないこと")
    void createPasskeyRegistrationOptions_reauthenticationFailed() {
      stubAccountForReauthLookupOnly();
      doReturn(null)
          .when(webAuthnChallengeRepositoryImpl)
          .consumeForReauthentication(REAUTH_CHALLENGE);

      ForbiddenAccountException exception =
          assertThrows(
              ForbiddenAccountException.class,
              () ->
                  accountServiceImpl.createPasskeyRegistrationOptions(
                      new AccountNo(1L), REAUTH_JSON));
      assertEquals(ErrorEnum.REAUTHENTICATION_FAILED.getErrorCode(), exception.getErrorCode());
      verify(webAuthnChallengeRepositoryImpl, never()).save(any());
    }

    @Test
    @Order(4)
    @DisplayName("異常系：パスキーの登録数が上限に達している場合、BadRequestExceptionをスローし、チャレンジを発行しないこと")
    void createPasskeyRegistrationOptions_reachedLimit() {
      stubAccountForReauth();
      doReturn(Consts.PASSKEY_MAX_COUNT)
          .when(passkeyCredentialRepositoryImpl)
          .countByAccountNo(new AccountNo(1L));

      BadRequestException exception =
          assertThrows(
              BadRequestException.class,
              () ->
                  accountServiceImpl.createPasskeyRegistrationOptions(
                      new AccountNo(1L), REAUTH_JSON));
      assertEquals(ErrorEnum.REACHED_PASSKEY_LIMIT.getErrorCode(), exception.getErrorCode());
      verify(webAuthnChallengeRepositoryImpl, never()).save(any());
    }
  }

  @Nested
  @Order(15)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class registerPasskey {
    private static final PasskeyResponseJson JSON = new PasskeyResponseJson("{\"id\":\"x\"}");
    private static final PasskeyChallenge CHALLENGE = new PasskeyChallenge("challenge-1");

    private PasskeyAttestationModel attestation;

    @BeforeEach
    void setUp() {
      attestation =
          PasskeyAttestationModel.builder()
              .credentialId(new CredentialId("credential-new"))
              .attestedCredentialData(new AttestedCredentialData(new byte[] {9}))
              .signCount(new SignCount(0L))
              .transports(new PasskeyTransports("hybrid,internal"))
              .isBackupEligible(new IsBackupEligible(true))
              .isBackedUp(new IsBackedUp(true))
              .isUvInitialized(new IsUvInitialized(true))
              .build();
    }

    private void stubVerification(AccountNo challengeOwner) {
      doReturn(CHALLENGE).when(webAuthnVerifier).readRegistrationChallenge(JSON);
      doReturn(attestation).when(webAuthnVerifier).verifyRegistration(JSON, CHALLENGE);
      doReturn(
              WebAuthnChallengeModel.of(
                  CHALLENGE,
                  WebAuthnChallengePurposeEnum.REGISTRATION,
                  challengeOwner,
                  new ExpiresAt(OffsetDateTime.now(clock).plusMinutes(5))))
          .when(webAuthnChallengeRepositoryImpl)
          .consumeForRegistration(CHALLENGE);
    }

    @Test
    @Order(1)
    @DisplayName("正常系：検証に成功した場合、パスキーを登録して登録したパスキーを返すこと")
    void registerPasskey_success() throws GalleryException {
      stubVerification(new AccountNo(1L));
      doReturn(0).when(passkeyCredentialRepositoryImpl).countByAccountNo(new AccountNo(1L));
      PasskeyCredentialModel saved = passkey(5L, "credential-new");
      doReturn(null, saved)
          .when(passkeyCredentialRepositoryImpl)
          .findByCredentialId(new CredentialId("credential-new"));

      PasskeyCredentialModel actual =
          accountServiceImpl.registerPasskey(new AccountNo(1L), JSON, new PasskeyName("iPhone"));

      assertEquals(saved, actual);
      verify(accountRepositoryImpl).lockForUpdate(new AccountNo(1L));
      ArgumentCaptor<PasskeyCredentialModel> captor =
          ArgumentCaptor.forClass(PasskeyCredentialModel.class);
      verify(passkeyCredentialRepositoryImpl).save(captor.capture());
      assertEquals(new AccountNo(1L), captor.getValue().getAccountNo());
      assertEquals(new CredentialId("credential-new"), captor.getValue().getCredentialId());
      assertEquals(new PasskeyName("iPhone"), captor.getValue().getPasskeyName());
      assertEquals(new PasskeyTransports("hybrid,internal"), captor.getValue().getTransports());
    }

    @Test
    @Order(2)
    @DisplayName("異常系：登録レスポンスの検証に失敗した場合、BadRequestExceptionをスローし、チャレンジを消費しないこと")
    void registerPasskey_verificationFailed() {
      doReturn(CHALLENGE).when(webAuthnVerifier).readRegistrationChallenge(JSON);
      doThrow(new PasskeyVerificationException("origin mismatch"))
          .when(webAuthnVerifier)
          .verifyRegistration(JSON, CHALLENGE);

      BadRequestException exception =
          assertThrows(
              BadRequestException.class,
              () ->
                  accountServiceImpl.registerPasskey(
                      new AccountNo(1L), JSON, new PasskeyName("iPhone")));
      assertEquals(ErrorEnum.INVALID_PASSKEY_REGISTRATION.getErrorCode(), exception.getErrorCode());
      verify(webAuthnChallengeRepositoryImpl, never()).consumeForRegistration(any());
      verify(passkeyCredentialRepositoryImpl, never()).save(any());
    }

    @Test
    @Order(3)
    @DisplayName("異常系：チャレンジが未発行・使用済み・期限切れの場合、BadRequestExceptionをスローすること")
    void registerPasskey_challengeNotAvailable() {
      doReturn(CHALLENGE).when(webAuthnVerifier).readRegistrationChallenge(JSON);
      doReturn(attestation).when(webAuthnVerifier).verifyRegistration(JSON, CHALLENGE);
      doReturn(null).when(webAuthnChallengeRepositoryImpl).consumeForRegistration(CHALLENGE);

      BadRequestException exception =
          assertThrows(
              BadRequestException.class,
              () ->
                  accountServiceImpl.registerPasskey(
                      new AccountNo(1L), JSON, new PasskeyName("iPhone")));
      assertEquals(ErrorEnum.INVALID_PASSKEY_REGISTRATION.getErrorCode(), exception.getErrorCode());
      verify(passkeyCredentialRepositoryImpl, never()).save(any());
    }

    @Test
    @Order(4)
    @DisplayName("異常系：他のアカウントに発行されたチャレンジの場合、BadRequestExceptionをスローすること")
    void registerPasskey_challengeOfOtherAccount() {
      stubVerification(new AccountNo(2L));

      BadRequestException exception =
          assertThrows(
              BadRequestException.class,
              () ->
                  accountServiceImpl.registerPasskey(
                      new AccountNo(1L), JSON, new PasskeyName("iPhone")));
      assertEquals(ErrorEnum.INVALID_PASSKEY_REGISTRATION.getErrorCode(), exception.getErrorCode());
      verify(passkeyCredentialRepositoryImpl, never()).save(any());
    }

    @Test
    @Order(5)
    @DisplayName("異常系：パスキーの登録数が上限に達している場合、BadRequestExceptionをスローすること")
    void registerPasskey_reachedLimit() {
      stubVerification(new AccountNo(1L));
      doReturn(Consts.PASSKEY_MAX_COUNT)
          .when(passkeyCredentialRepositoryImpl)
          .countByAccountNo(new AccountNo(1L));

      BadRequestException exception =
          assertThrows(
              BadRequestException.class,
              () ->
                  accountServiceImpl.registerPasskey(
                      new AccountNo(1L), JSON, new PasskeyName("iPhone")));
      assertEquals(ErrorEnum.REACHED_PASSKEY_LIMIT.getErrorCode(), exception.getErrorCode());
      verify(passkeyCredentialRepositoryImpl, never()).save(any());
    }

    @Test
    @Order(6)
    @DisplayName("異常系：登録済みのクレデンシャルの場合、RegistFailureExceptionをスローすること")
    void registerPasskey_duplicate() {
      stubVerification(new AccountNo(1L));
      doReturn(0).when(passkeyCredentialRepositoryImpl).countByAccountNo(new AccountNo(1L));
      doReturn(passkey(5L, "credential-new"))
          .when(passkeyCredentialRepositoryImpl)
          .findByCredentialId(new CredentialId("credential-new"));

      RegistFailureException exception =
          assertThrows(
              RegistFailureException.class,
              () ->
                  accountServiceImpl.registerPasskey(
                      new AccountNo(1L), JSON, new PasskeyName("iPhone")));
      assertEquals(ErrorEnum.DUPLICATE_PASSKEY.getErrorCode(), exception.getErrorCode());
      verify(passkeyCredentialRepositoryImpl, never()).save(any());
    }
  }

  @Nested
  @Order(16)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class getPasskeyList {
    @Test
    @Order(1)
    @DisplayName("正常系：アカウントのパスキー一覧をRepositoryから取得して返すこと")
    void getPasskeyList_success() {
      PasskeyCredentialModelList list =
          PasskeyCredentialModelList.of(List.of(passkey(1L, "credential-1")));
      doReturn(list).when(passkeyCredentialRepositoryImpl).findByAccountNo(new AccountNo(1L));

      assertEquals(list, accountServiceImpl.getPasskeyList(new AccountNo(1L)));
    }
  }

  @Nested
  @Order(17)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class renamePasskey {
    @Test
    @Order(1)
    @DisplayName("正常系：自身のパスキーの名前を変更すること")
    void renamePasskey_success() throws GalleryException {
      doReturn(1)
          .when(passkeyCredentialRepositoryImpl)
          .rename(new AccountNo(1L), new PasskeyNo(5L), new PasskeyName("新しい名前"));

      assertDoesNotThrow(() ->
          accountServiceImpl.renamePasskey(
              new AccountNo(1L), new PasskeyNo(5L), new PasskeyName("新しい名前")));
    }

    @Test
    @Order(2)
    @DisplayName("異常系：自身のパスキーとして存在しない場合、PasskeyNotFoundExceptionをスローすること")
    void renamePasskey_notFound() {
      doReturn(0)
          .when(passkeyCredentialRepositoryImpl)
          .rename(new AccountNo(1L), new PasskeyNo(99L), new PasskeyName("新しい名前"));

      assertThrows(
          PasskeyNotFoundException.class,
          () ->
              accountServiceImpl.renamePasskey(
                  new AccountNo(1L), new PasskeyNo(99L), new PasskeyName("新しい名前")));
    }
  }

  @Nested
  @Order(18)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class deletePasskey {
    @Test
    @Order(1)
    @DisplayName("正常系：本人確認に成功した場合、自身のパスキーを削除すること")
    void deletePasskey_success() throws GalleryException {
      stubAccountForReauth();
      doReturn(
              PasskeyCredentialModelList.of(
                  List.of(passkey(1L, "credential-reauth"), passkey(5L, "credential-5"))))
          .when(passkeyCredentialRepositoryImpl)
          .findByAccountNo(new AccountNo(1L));
      doReturn(1)
          .when(passkeyCredentialRepositoryImpl)
          .delete(new AccountNo(1L), new PasskeyNo(5L));

      accountServiceImpl.deletePasskey(new AccountNo(1L), new PasskeyNo(5L), REAUTH_JSON);

      // 並行した削除で最後の1件まで消えないよう、アカウント単位で直列化する
      verify(accountRepositoryImpl).lockForUpdate(new AccountNo(1L));
      verify(passkeyCredentialRepositoryImpl).delete(new AccountNo(1L), new PasskeyNo(5L));
    }

    @Test
    @Order(2)
    @DisplayName("異常系：パスキーによる本人確認に失敗した場合、ForbiddenAccountExceptionをスローし、削除しないこと")
    void deletePasskey_reauthenticationFailed() {
      stubAccountForReauthLookupOnly();
      doReturn(null)
          .when(webAuthnChallengeRepositoryImpl)
          .consumeForReauthentication(REAUTH_CHALLENGE);

      assertThrows(
          ForbiddenAccountException.class,
          () ->
              accountServiceImpl.deletePasskey(new AccountNo(1L), new PasskeyNo(5L), REAUTH_JSON));
      verify(passkeyCredentialRepositoryImpl, never()).delete(any(), any());
    }

    @Test
    @Order(3)
    @DisplayName("異常系：自身のパスキーとして存在しない場合、PasskeyNotFoundExceptionをスローすること")
    void deletePasskey_notFound() {
      stubAccountForReauth();
      doReturn(
              PasskeyCredentialModelList.of(
                  List.of(passkey(1L, "credential-reauth"), passkey(5L, "credential-5"))))
          .when(passkeyCredentialRepositoryImpl)
          .findByAccountNo(new AccountNo(1L));

      assertThrows(
          PasskeyNotFoundException.class,
          () ->
              accountServiceImpl.deletePasskey(new AccountNo(1L), new PasskeyNo(99L), REAUTH_JSON));
      verify(passkeyCredentialRepositoryImpl, never()).delete(any(), any());
    }

    @Test
    @Order(4)
    @DisplayName("異常系：最後の1件のパスキーの場合、BadRequestExceptionをスローし削除しないこと")
    void deletePasskey_lastPasskey() {
      stubAccountForReauth();
      doReturn(PasskeyCredentialModelList.of(List.of(passkey(1L, "credential-reauth"))))
          .when(passkeyCredentialRepositoryImpl)
          .findByAccountNo(new AccountNo(1L));

      BadRequestException exception =
          assertThrows(
              BadRequestException.class,
              () ->
                  accountServiceImpl.deletePasskey(
                      new AccountNo(1L), new PasskeyNo(1L), REAUTH_JSON));
      assertEquals(ErrorEnum.CANNOT_DELETE_LAST_PASSKEY.getErrorCode(), exception.getErrorCode());
      verify(passkeyCredentialRepositoryImpl, never()).delete(any(), any());
    }
  }

  @Nested
  @Order(20)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class createReauthenticationOptions {
    @Test
    @Order(1)
    @DisplayName("正常系：アカウントに紐づく本人確認用チャレンジを保存し、本人のパスキーに限定したオプションを返すこと")
    void createReauthenticationOptions_success() {
      PasskeyChallenge challenge = new PasskeyChallenge("reauth-challenge");
      doReturn(challenge).when(webAuthnVerifier).generateChallenge();
      doReturn("localhost").when(webAuthnConfig).getRpId();
      doReturn(300).when(webAuthnConfig).getChallengeTimeoutSeconds();
      doReturn(PasskeyCredentialModelList.of(List.of(passkey(1L, "credential-1"))))
          .when(passkeyCredentialRepositoryImpl)
          .findByAccountNo(new AccountNo(1L));

      PasskeyAuthenticationOptionsModel actual =
          accountServiceImpl.createReauthenticationOptions(new AccountNo(1L));

      assertEquals(challenge, actual.getChallenge());
      assertEquals(List.of(new CredentialId("credential-1")), actual.getAllowCredentialIds());
      assertEquals(300_000L, actual.getTimeoutMillis().value());

      ArgumentCaptor<WebAuthnChallengeModel> captor =
          ArgumentCaptor.forClass(WebAuthnChallengeModel.class);
      verify(webAuthnChallengeRepositoryImpl).save(captor.capture());
      assertEquals(
          WebAuthnChallengePurposeEnum.REAUTHENTICATION, captor.getValue().getPurposeKbn());
      assertEquals(new AccountNo(1L), captor.getValue().getAccountNo());
    }
  }

  @Nested
  @Order(21)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class getRecoveryCodeCount {
    @Test
    @Order(1)
    @DisplayName("正常系：未使用のリカバリーコードの件数をRepositoryから取得して返すこと")
    void getRecoveryCodeCount_success() {
      doReturn(7).when(recoveryCodeRepositoryImpl).countByAccountNo(new AccountNo(1L));

      assertEquals(7, accountServiceImpl.getRecoveryCodeCount(new AccountNo(1L)));
    }
  }

  @Nested
  @Order(22)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class regenerateRecoveryCodes {
    @Test
    @Order(1)
    @DisplayName("正常系：本人確認に成功した場合、未使用のコードをすべて削除してから新しいコードを発行すること")
    void regenerateRecoveryCodes_success() throws GalleryException {
      stubAccountForReauth();

      RecoveryCodeList actual =
          accountServiceImpl.regenerateRecoveryCodes(new AccountNo(1L), REAUTH_JSON);

      assertEquals(Consts.RECOVERY_CODE_COUNT, actual.size());
      for (RecoveryCode code : actual) {
        assertEquals(Consts.RECOVERY_CODE_LENGTH, code.value().length());
      }
      var inOrder = inOrder(recoveryCodeRepositoryImpl);
      inOrder.verify(recoveryCodeRepositoryImpl).deleteByAccountNo(new AccountNo(1L));
      inOrder.verify(recoveryCodeRepositoryImpl).saveAll(new AccountNo(1L), actual);
    }

    @Test
    @Order(2)
    @DisplayName("異常系：パスキーによる本人確認に失敗した場合、ForbiddenAccountExceptionをスローし、既存のコードを残すこと")
    void regenerateRecoveryCodes_reauthenticationFailed() {
      stubAccountForReauthLookupOnly();
      doReturn(null)
          .when(webAuthnChallengeRepositoryImpl)
          .consumeForReauthentication(REAUTH_CHALLENGE);

      assertThrows(
          ForbiddenAccountException.class,
          () -> accountServiceImpl.regenerateRecoveryCodes(new AccountNo(1L), REAUTH_JSON));
      verify(recoveryCodeRepositoryImpl, never()).deleteByAccountNo(any());
      verify(recoveryCodeRepositoryImpl, never()).saveAll(any(), any());
    }
  }
}
