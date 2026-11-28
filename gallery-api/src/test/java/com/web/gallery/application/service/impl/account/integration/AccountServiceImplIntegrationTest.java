package com.web.gallery.application.service.impl.account.integration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.web.gallery.application.model.account.AccountListGetModel;
import com.web.gallery.application.model.account.AccountModel;
import com.web.gallery.application.model.account.AccountPageModel;
import com.web.gallery.application.model.auth.PasskeyRegistrationOptionsModel;
import com.web.gallery.application.model.auth.RecoveryCodeList;
import com.web.gallery.application.model.auth.WebAuthnChallengeModel;
import com.web.gallery.application.repository.auth.WebAuthnChallengeRepository;
import com.web.gallery.application.repository.photo.FileRepository;
import com.web.gallery.application.service.impl.account.AccountServiceImpl;
import com.web.gallery.domain.enumeration.AuthorityEnum;
import com.web.gallery.domain.enumeration.SexEnum;
import com.web.gallery.domain.enumeration.WebAuthnChallengePurposeEnum;
import com.web.gallery.domain.exception.BadRequestException;
import com.web.gallery.domain.exception.ForbiddenAccountException;
import com.web.gallery.domain.exception.GalleryException;
import com.web.gallery.domain.exception.RegistFailureException;
import com.web.gallery.domain.exception.UpdateFailureException;
import com.web.gallery.domain.model.account.AccountId;
import com.web.gallery.domain.model.account.AccountName;
import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.account.BirthplacePrefectureKbnCode;
import com.web.gallery.domain.model.account.LoginFailureCount;
import com.web.gallery.domain.model.account.ResidentPrefectureKbnCode;
import com.web.gallery.domain.model.auth.PasskeyChallenge;
import com.web.gallery.domain.model.auth.PasskeyName;
import com.web.gallery.domain.model.auth.PasskeyResponseJson;
import com.web.gallery.domain.model.auth.RecoveryCode;
import com.web.gallery.domain.model.common.ExpiresAt;
import com.web.gallery.domain.model.photo.ImageFilePath;
import com.web.gallery.infrastructure.persistence.entity.account.Account;
import com.web.gallery.testsupport.PasskeyTestAuthenticator;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@Transactional
public class AccountServiceImplIntegrationTest {
  @Autowired private AccountServiceImpl accountServiceImpl;

  @Autowired private JdbcTemplate jdbcTemplate;

  @Autowired private WebAuthnChallengeRepository webAuthnChallengeRepository;

  /** S3ストレージアクセスはモックする（統合テストでは実ストレージへ接続しない） */
  @MockitoBean private FileRepository fileRepository;

  /**
   * DBサーバの現在時刻を取得する
   *
   * <p>DB側で{@code NOW()}により書き込まれる時刻を検証する際の境界値として使う。JVM（ホスト）と PostgreSQL（macOSではDocker
   * Desktopの別VM）はクロックが独立しており、{@link OffsetDateTime#now()}を
   * 境界値にすると両者のずれ（実測でDB側が数百マイクロ秒遅れる）でアサーションが不安定になるため、 比較する両辺をDBのクロックに揃える。
   *
   * <p>トランザクション開始時刻を返す{@code NOW()}ではなく、文の実行時刻を返す {@code
   * clock_timestamp()}を使う。テスト側のトランザクションは検証対象の処理より前に 開始しているため、{@code NOW()}では正しい境界値にならない。
   *
   * @return DBサーバの現在時刻
   */
  private OffsetDateTime dbClockNow() {
    return jdbcTemplate.queryForObject(
        "SELECT clock_timestamp()", (rs, rowNum) -> rs.getObject(1, OffsetDateTime.class));
  }

  /**
   * 本番と同じ新規登録フロー（新規登録オプションの発行 → パスキーの作成 → 登録）でアカウントを登録する
   *
   * @param authenticator パスキーを作成する擬似認証器
   * @param accountModel 登録するアカウント
   * @return 発行されたリカバリーコード
   */
  private RecoveryCodeList signUp(PasskeyTestAuthenticator authenticator, AccountModel accountModel)
      throws GalleryException {
    PasskeyRegistrationOptionsModel options =
        accountServiceImpl.createSignupOptions(
            accountModel.getAccountId(), accountModel.getAccountName());
    String json =
        authenticator.createRegistrationResponseJson(
            PasskeyTestAuthenticator.RP_ID,
            options.getChallenge().value(),
            options.getUserHandle().value(),
            accountModel.getAccountId().value());
    return accountServiceImpl.registAccount(
        accountModel, new PasskeyResponseJson(json), new PasskeyName("テスト用パスキー"));
  }

  /**
   * フィクスチャで投入した既存アカウントに、擬似認証器のパスキーを登録する
   *
   * <p>パスキーの追加登録には本人確認が必要なため、登録用チャレンジを直接発行して登録処理のみを通す
   *
   * @param accountNo アカウント番号
   * @return パスキーを保持する擬似認証器
   */
  private PasskeyTestAuthenticator registerTestPasskey(AccountNo accountNo)
      throws GalleryException {
    PasskeyTestAuthenticator authenticator = new PasskeyTestAuthenticator();
    String challenge = "cmVnaXN0cmF0aW9uLWNoYWxsZW5nZS0" + accountNo.value();
    webAuthnChallengeRepository.save(
        WebAuthnChallengeModel.of(
            new PasskeyChallenge(challenge),
            WebAuthnChallengePurposeEnum.REGISTRATION,
            accountNo,
            new ExpiresAt(OffsetDateTime.now().plusMinutes(5))));
    String json =
        authenticator.createRegistrationResponseJson(
            PasskeyTestAuthenticator.RP_ID, challenge, "dXNlci1oYW5kbGU", "aaaaaaaa");
    accountServiceImpl.registerPasskey(
        accountNo, new PasskeyResponseJson(json), new PasskeyName("テスト用パスキー"));
    return authenticator;
  }

  /**
   * 本人確認オプションを発行し、擬似認証器のパスキーで署名した認証レスポンスを返す
   *
   * @param authenticator パスキーを保持する擬似認証器
   * @param accountNo アカウント番号
   * @return 本人確認用の認証レスポンス
   */
  private PasskeyResponseJson reauthenticate(
      PasskeyTestAuthenticator authenticator, AccountNo accountNo) {
    return new PasskeyResponseJson(
        authenticator.createAuthenticationResponseJson(
            PasskeyTestAuthenticator.RP_ID,
            accountServiceImpl.createReauthenticationOptions(accountNo).getChallenge().value()));
  }

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/service/AccountServiceImplIntegrationTest.sql")
  class loadUserByUsername {
    @Test
    @Order(1)
    @DisplayName("正常系")
    void loadUserByUsername_success() {
      UserDetails userDetails = accountServiceImpl.loadUserByUsername("aaaaaaaa");
      assertEquals("aaaaaaaa", userDetails.getUsername());
      // パスワードは保持しない
      assertNull(userDetails.getPassword());
    }

    @Test
    @Order(2)
    @DisplayName("異常系：UsernameNotFoundExceptionをthrowする")
    void loadUserByUsername_UsernameNotFoundException() {
      assertThrows(
          UsernameNotFoundException.class, () -> accountServiceImpl.loadUserByUsername("zzzzzzzz"));
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class registAccount {
    @Test
    @Order(1)
    @DisplayName("正常系：新規登録オプションで作成したパスキーとともにアカウントを登録し、リカバリーコードを発行すること")
    @Sql("/sql/common/cleanup.sql")
    @Sql("/sql/common/ResetAccountNoSeq.sql")
    void registAccount_success() throws GalleryException {
      AccountModel accountModel =
          AccountModel.builder()
              .accountId(new AccountId("mmmmmmmm"))
              .accountName(new AccountName("MMMMMMMM"))
              .build();

      OffsetDateTime transactionNow =
          jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);
      PasskeyTestAuthenticator authenticator = new PasskeyTestAuthenticator();
      RecoveryCodeList recoveryCodes = signUp(authenticator, accountModel);
      assertEquals(10, recoveryCodes.size());

      List<Account> actualData =
          jdbcTemplate.query(
              "SELECT * FROM common.account where account_id='mmmmmmmm'",
              (rs, rowNum) ->
                  Account.builder()
                      .accountNo(rs.getLong("account_no"))
                      .createdBy(rs.getLong("created_by"))
                      .createdAt(rs.getObject("created_at", OffsetDateTime.class))
                      .updatedBy(rs.getLong("updated_by"))
                      .updatedAt(rs.getObject("updated_at", OffsetDateTime.class))
                      .isDeleted(rs.getBoolean("is_deleted"))
                      .accountId(rs.getString("account_id"))
                      .accountName(rs.getString("account_name"))
                      .birthdate(rs.getObject("birthdate", LocalDate.class))
                      .sexKbn(SexEnum.getOrDefault(rs.getString("sex_kbn")))
                      .birthplacePrefectureKbnCode(rs.getString("birthplace_prefecture_kbn_code"))
                      .residentPrefectureKbnCode(rs.getString("resident_prefecture_kbn_code"))
                      .freeMemo(rs.getString("free_memo"))
                      .lastLoginDatetime(rs.getObject("last_login_datetime", OffsetDateTime.class))
                      .loginFailureCount(rs.getInt("login_failure_count"))
                      .isAdminLocked(rs.getBoolean("is_admin_locked"))
                      .build());

      assertEquals(1, actualData.size());
      assertEquals(1L, actualData.getFirst().getAccountNo());
      assertEquals(0L, actualData.getFirst().getCreatedBy());
      assertEquals(transactionNow, actualData.getFirst().getCreatedAt());
      assertEquals(0L, actualData.getFirst().getUpdatedBy());
      assertEquals(transactionNow, actualData.getFirst().getUpdatedAt());
      assertFalse(actualData.getFirst().getIsDeleted());
      assertEquals("mmmmmmmm", actualData.getFirst().getAccountId());
      assertEquals("MMMMMMMM", actualData.getFirst().getAccountName());
      assertEquals(LocalDate.of(1900, 1, 1), actualData.getFirst().getBirthdate());
      assertEquals(SexEnum.NONE, actualData.getFirst().getSexKbn());
      assertEquals("none", actualData.getFirst().getBirthplacePrefectureKbnCode());
      assertEquals("none", actualData.getFirst().getResidentPrefectureKbnCode());
      assertEquals("", actualData.getFirst().getFreeMemo());
      assertEquals(
          OffsetDateTime.of(1900, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)),
          actualData.getFirst().getLastLoginDatetime().plusHours(9));
      assertEquals(0, actualData.getFirst().getLoginFailureCount());

      // account_authorityにも同一のアカウント番号でMINI固定で登録されること
      AuthorityEnum actualAuthorityKbn =
          jdbcTemplate.queryForObject(
              "SELECT authority_kbn FROM common.account_authority WHERE account_no=1",
              (rs, rowNum) -> AuthorityEnum.getOrDefault(rs.getString("authority_kbn")));
      assertEquals(AuthorityEnum.MINI, actualAuthorityKbn);

      // 最初のパスキー・ユーザーハンドル・リカバリーコード（ハッシュのみ）が登録されること
      assertEquals(
          1,
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM common.passkey_credential WHERE account_no=1", Integer.class));
      assertEquals(
          1,
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM common.passkey_user_handle WHERE account_no=1", Integer.class));
      List<String> storedHashes =
          jdbcTemplate.queryForList(
              "SELECT code_hash FROM common.recovery_code WHERE account_no=1", String.class);
      assertEquals(10, storedHashes.size());
      for (RecoveryCode code : recoveryCodes) {
        assertFalse(storedHashes.contains(code.value()));
        assertTrue(storedHashes.contains(code.toHash().value()));
      }
      // 新規登録用チャレンジは消費されていること
      assertEquals(
          0,
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM common.webauthn_challenge", Integer.class));
    }

    @Test
    @Order(2)
    @DisplayName("異常系：アカウントIDが既に存在する場合、新規登録オプションの発行時にRegistFailureExceptionをthrowする")
    @Sql("/sql/common/cleanup.sql")
    @Sql("/sql/service/AccountServiceImplIntegrationTest.sql")
    void registAccount_account_already_exist() {
      assertThrows(
          RegistFailureException.class,
          () ->
              accountServiceImpl.createSignupOptions(
                  new AccountId("aaaaaaaa"), new AccountName("AAAAAAAA")));
    }

    @Test
    @Order(3)
    @DisplayName("異常系：新規登録オプションを発行していないチャレンジの登録レスポンスの場合、BadRequestExceptionをthrowし登録しない")
    @Sql("/sql/common/cleanup.sql")
    @Sql("/sql/common/ResetAccountNoSeq.sql")
    void registAccount_challenge_not_issued() {
      String json =
          new PasskeyTestAuthenticator()
              .createRegistrationResponseJson(
                  PasskeyTestAuthenticator.RP_ID,
                  "bm90LWlzc3VlZC1jaGFsbGVuZ2U",
                  "dXNlci1oYW5kbGU",
                  "qqqqqqqq");

      assertThrows(
          BadRequestException.class,
          () ->
              accountServiceImpl.registAccount(
                  AccountModel.builder()
                      .accountId(new AccountId("qqqqqqqq"))
                      .accountName(new AccountName("QQQQQQQQ"))
                      .build(),
                  new PasskeyResponseJson(json),
                  new PasskeyName("テスト用パスキー")));
      assertEquals(
          0,
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM common.account WHERE account_id='qqqqqqqq'", Integer.class));
    }

    @Test
    @Order(4)
    @DisplayName("異常系：区分マスタに存在しない都道府県区分コードの場合、BadRequestExceptionをthrowする")
    @Sql("/sql/common/cleanup.sql")
    @Sql("/sql/common/ResetAccountNoSeq.sql")
    void registAccount_invalid_prefecture_code() {
      AccountModel accountModel =
          AccountModel.builder()
              .accountId(new AccountId("nnnnnnnn"))
              .accountName(new AccountName("NNNNNNNN"))
              .birthplacePrefectureKbnCode(new BirthplacePrefectureKbnCode("NotExistCode"))
              .build();

      assertThrows(
          BadRequestException.class, () -> signUp(new PasskeyTestAuthenticator(), accountModel));
    }

    @Test
    @Order(5)
    @DisplayName("異常系：出身地が未指定で在住地が区分マスタに存在しない都道府県区分コードの場合、BadRequestExceptionをthrowする")
    @Sql("/sql/common/cleanup.sql")
    @Sql("/sql/common/ResetAccountNoSeq.sql")
    @Sql("/sql/common/PrefectureKbnMst.sql")
    void registAccount_invalid_resident_prefecture_code() {
      AccountModel accountModel =
          AccountModel.builder()
              .accountId(new AccountId("oooooooo"))
              .accountName(new AccountName("OOOOOOOO"))
              .residentPrefectureKbnCode(new ResidentPrefectureKbnCode("NotExistCode"))
              .build();

      assertThrows(
          BadRequestException.class, () -> signUp(new PasskeyTestAuthenticator(), accountModel));
    }

    @Test
    @Order(6)
    @DisplayName("正常系：区分マスタに実在する都道府県区分コードを指定した場合、登録できること")
    @Sql("/sql/common/cleanup.sql")
    @Sql("/sql/common/ResetAccountNoSeq.sql")
    @Sql("/sql/common/PrefectureKbnMst.sql")
    void registAccount_valid_prefecture_code() throws GalleryException {
      AccountModel accountModel =
          AccountModel.builder()
              .accountId(new AccountId("pppppppp"))
              .accountName(new AccountName("PPPPPPPP"))
              .birthplacePrefectureKbnCode(new BirthplacePrefectureKbnCode("Tokyo"))
              .residentPrefectureKbnCode(new ResidentPrefectureKbnCode("Tokyo"))
              .build();

      signUp(new PasskeyTestAuthenticator(), accountModel);

      String actualBirthplaceCode =
          jdbcTemplate.queryForObject(
              "SELECT birthplace_prefecture_kbn_code FROM common.account WHERE account_id='pppppppp'",
              String.class);
      assertEquals("Tokyo", actualBirthplaceCode);
    }
  }

  @Nested
  @Order(3)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/service/AccountServiceImplIntegrationTest.sql")
  class updateAccount {
    @Test
    @Order(1)
    @DisplayName("正常系：アカウントを更新")
    void updateAccount_success() throws GalleryException {
      AccountModel accountModel =
          AccountModel.builder()
              .accountNo(new AccountNo(1L))
              .accountId(new AccountId("zzzzzzzz"))
              .build();

      OffsetDateTime transactionNow =
          jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);
      assertFalse(accountServiceImpl.updateAccount(accountModel));

      List<Account> actualData =
          jdbcTemplate.query(
              "SELECT * FROM common.account where account_no=1",
              (rs, rowNum) ->
                  Account.builder()
                      .accountNo(rs.getLong("account_no"))
                      .createdBy(rs.getLong("created_by"))
                      .createdAt(rs.getObject("created_at", OffsetDateTime.class))
                      .updatedBy(rs.getLong("updated_by"))
                      .updatedAt(rs.getObject("updated_at", OffsetDateTime.class))
                      .isDeleted(rs.getBoolean("is_deleted"))
                      .accountId(rs.getString("account_id"))
                      .accountName(rs.getString("account_name"))
                      .birthdate(rs.getObject("birthdate", LocalDate.class))
                      .sexKbn(SexEnum.getOrDefault(rs.getString("sex_kbn")))
                      .birthplacePrefectureKbnCode(rs.getString("birthplace_prefecture_kbn_code"))
                      .residentPrefectureKbnCode(rs.getString("resident_prefecture_kbn_code"))
                      .freeMemo(rs.getString("free_memo"))
                      .lastLoginDatetime(rs.getObject("last_login_datetime", OffsetDateTime.class))
                      .loginFailureCount(rs.getInt("login_failure_count"))
                      .isAdminLocked(rs.getBoolean("is_admin_locked"))
                      .build());

      assertEquals(1, actualData.size());
      assertEquals(1L, actualData.getFirst().getAccountNo());
      assertEquals(1L, actualData.getFirst().getCreatedBy());
      assertEquals(
          OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)),
          actualData.getFirst().getCreatedAt());
      assertEquals(1L, actualData.getFirst().getUpdatedBy());
      assertEquals(transactionNow, actualData.getFirst().getUpdatedAt());
      assertFalse(actualData.getFirst().getIsDeleted());
      assertEquals("zzzzzzzz", actualData.getFirst().getAccountId());
      assertEquals("AAAAAAAA", actualData.getFirst().getAccountName());
      // 更新リクエストに含まれない項目は「変更なし」としてフィクスチャ値を維持する
      assertEquals(LocalDate.of(1991, 2, 14), actualData.getFirst().getBirthdate());
      assertEquals(SexEnum.NONE, actualData.getFirst().getSexKbn());
      assertEquals("none", actualData.getFirst().getBirthplacePrefectureKbnCode());
      assertEquals("none", actualData.getFirst().getResidentPrefectureKbnCode());
      assertEquals("", actualData.getFirst().getFreeMemo());
      assertEquals(
          OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)),
          actualData.getFirst().getLastLoginDatetime());
      assertEquals(0, actualData.getFirst().getLoginFailureCount());
      assertFalse(actualData.getFirst().getIsAdminLocked());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：アカウントが既に存在する")
    void updateAccount_account_already_exist() throws GalleryException {
      AccountModel accountModel =
          AccountModel.builder()
              .accountNo(new AccountNo(1L))
              .accountId(new AccountId("bbbbbbbb"))
              .build();
      assertTrue(accountServiceImpl.updateAccount(accountModel));

      List<Account> actualData =
          jdbcTemplate.query(
              "SELECT * FROM common.account where account_no=1",
              (rs, rowNum) ->
                  Account.builder()
                      .accountNo(rs.getLong("account_no"))
                      .createdBy(rs.getLong("created_by"))
                      .createdAt(rs.getObject("created_at", OffsetDateTime.class))
                      .updatedBy(rs.getLong("updated_by"))
                      .updatedAt(rs.getObject("updated_at", OffsetDateTime.class))
                      .isDeleted(rs.getBoolean("is_deleted"))
                      .accountId(rs.getString("account_id"))
                      .accountName(rs.getString("account_name"))
                      .birthdate(rs.getObject("birthdate", LocalDate.class))
                      .sexKbn(SexEnum.getOrDefault(rs.getString("sex_kbn")))
                      .birthplacePrefectureKbnCode(rs.getString("birthplace_prefecture_kbn_code"))
                      .residentPrefectureKbnCode(rs.getString("resident_prefecture_kbn_code"))
                      .freeMemo(rs.getString("free_memo"))
                      .lastLoginDatetime(rs.getObject("last_login_datetime", OffsetDateTime.class))
                      .loginFailureCount(rs.getInt("login_failure_count"))
                      .isAdminLocked(rs.getBoolean("is_admin_locked"))
                      .build());

      assertEquals(1, actualData.size());
      assertEquals(1L, actualData.getFirst().getAccountNo());
      assertEquals(1L, actualData.getFirst().getCreatedBy());
      assertEquals(
          OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)),
          actualData.getFirst().getCreatedAt());
      assertEquals(1L, actualData.getFirst().getUpdatedBy());
      assertEquals(
          OffsetDateTime.of(2001, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)),
          actualData.getFirst().getUpdatedAt());
      assertFalse(actualData.getFirst().getIsDeleted());
      assertEquals("aaaaaaaa", actualData.getFirst().getAccountId());
      assertEquals("AAAAAAAA", actualData.getFirst().getAccountName());
      assertEquals(LocalDate.of(1991, 2, 14), actualData.getFirst().getBirthdate());
      assertEquals(SexEnum.NONE, actualData.getFirst().getSexKbn());
      assertEquals("none", actualData.getFirst().getBirthplacePrefectureKbnCode());
      assertEquals("none", actualData.getFirst().getResidentPrefectureKbnCode());
      assertEquals("", actualData.getFirst().getFreeMemo());
      assertEquals(
          OffsetDateTime.of(2002, 1, 1, 9, 0, 0, 0, ZoneOffset.ofHours(0)),
          actualData.getFirst().getLastLoginDatetime().plusHours(9));
      assertEquals(0, actualData.getFirst().getLoginFailureCount());
      assertFalse(actualData.getFirst().getIsAdminLocked());
    }

    @Test
    @Order(3)
    @DisplayName("異常系：UpdateFailureExceptionをthrowする")
    void updateAccount_UpdateFailureException() throws GalleryException {
      AccountModel accountModel =
          AccountModel.builder()
              .accountNo(new AccountNo(99L))
              .accountId(new AccountId("zzzzzzzz"))
              .build();
      assertThrows(
          UpdateFailureException.class, () -> accountServiceImpl.updateAccount(accountModel));
    }

    @Test
    @Order(5)
    @DisplayName("正常系：アカウントIDを変更しない場合、リフレッシュトークンは失効しないこと")
    void updateAccount_no_change() throws GalleryException {
      jdbcTemplate.update(
          "INSERT INTO common.refresh_token VALUES(DEFAULT, 1, 'hash-account1', NOW() + interval '7 days', NOW(), 1, NOW(), false)");

      AccountModel accountModel =
          AccountModel.builder()
              .accountNo(new AccountNo(1L))
              .accountId(new AccountId("aaaaaaaa"))
              .build();

      assertFalse(accountServiceImpl.updateAccount(accountModel));

      Integer activeRefreshTokenCount =
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM common.refresh_token WHERE account_no=1 AND is_revoked=false",
              Integer.class);
      assertEquals(1, activeRefreshTokenCount);
    }

    @Test
    @Order(6)
    @DisplayName("正常系：アカウントIDを変更した場合、リフレッシュトークンが失効すること")
    void updateAccount_change_accountId_only() throws GalleryException {
      jdbcTemplate.update(
          "INSERT INTO common.refresh_token VALUES(DEFAULT, 1, 'hash-account1', NOW() + interval '7 days', NOW(), 1, NOW(), false)");

      AccountModel accountModel =
          AccountModel.builder()
              .accountNo(new AccountNo(1L))
              .accountId(new AccountId("newaccid"))
              .build();

      assertFalse(accountServiceImpl.updateAccount(accountModel));

      Integer activeRefreshTokenCount =
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM common.refresh_token WHERE account_no=1 AND is_revoked=false",
              Integer.class);
      assertEquals(0, activeRefreshTokenCount);
    }
  }

  @Nested
  @Order(4)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/service/AccountServiceImplIntegrationTest.sql")
  class getAccountById {
    @Test
    @Order(1)
    @DisplayName("正常系：アカウントが存在する場合")
    void getAccountById_found() {
      AccountModel actual = accountServiceImpl.getAccountById(new AccountId("aaaaaaaa"));

      assertEquals(1L, actual.getAccountNo().value());
      assertEquals("aaaaaaaa", actual.getAccountId().value());
      assertEquals("AAAAAAAA", actual.getAccountName().value());
      assertEquals(LocalDate.of(1991, 2, 14), actual.getBirthdate().value());
      assertEquals(SexEnum.NONE, actual.getSexKbn());
      assertEquals("none", actual.getBirthplacePrefectureKbnCode().value());
      assertEquals("none", actual.getResidentPrefectureKbnCode().value());
      assertEquals("", actual.getFreeMemo().value());
      assertEquals(AuthorityEnum.ADMINISTRATOR, actual.getAuthorityKbn());
      assertEquals(
          OffsetDateTime.of(2002, 1, 1, 9, 0, 0, 0, ZoneOffset.ofHours(0)),
          actual.getLastLoginDatetime().value().plusHours(9));
      assertEquals(new LoginFailureCount(0), actual.getLoginFailureCount());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：アカウントが存在しない場合、nullを返す")
    void getAccountById_not_found() {
      assertNull(accountServiceImpl.getAccountById(new AccountId("zzzzzzzz")));
    }
  }

  @Nested
  @Order(5)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class getAccountList {
    @Test
    @Order(1)
    @DisplayName("正常系：アカウントが存在する場合（1ページ目、5件に切り詰められ最後のページでないこと）")
    @Sql("/sql/common/cleanup.sql")
    @Sql("/sql/service/AccountServiceImplIntegrationTest.sql")
    void getAccountList_found() {
      AccountListGetModel accountListGetModel = AccountListGetModel.builder().pageNo(1).build();
      AccountPageModel actual = accountServiceImpl.getAccountList(accountListGetModel);

      assertFalse(actual.getIsLast());
      assertEquals(5, actual.getAccountModelList().size());
      assertEquals("aaaaaaaa", actual.getAccountModelList().get(0).getAccountId().value());
      assertEquals("bbbbbbbb", actual.getAccountModelList().get(1).getAccountId().value());
      assertEquals("cccccccc", actual.getAccountModelList().get(2).getAccountId().value());
      assertEquals("dddddddd", actual.getAccountModelList().get(3).getAccountId().value());
      assertEquals("eeeeeeee", actual.getAccountModelList().get(4).getAccountId().value());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：アカウントが存在する場合（2ページ目、まだ最後のページでないこと）")
    @Sql("/sql/common/cleanup.sql")
    @Sql("/sql/service/AccountServiceImplIntegrationTest.sql")
    void getAccountList_found_secondPage() {
      AccountListGetModel accountListGetModel = AccountListGetModel.builder().pageNo(2).build();
      AccountPageModel actual = accountServiceImpl.getAccountList(accountListGetModel);

      assertFalse(actual.getIsLast());
      assertEquals(5, actual.getAccountModelList().size());
      assertEquals("ffffffff", actual.getAccountModelList().get(0).getAccountId().value());
      assertEquals("gggggggg", actual.getAccountModelList().get(1).getAccountId().value());
      assertEquals("hhhhhhhh", actual.getAccountModelList().get(2).getAccountId().value());
      assertEquals("jjjjjjjj", actual.getAccountModelList().get(3).getAccountId().value());
      assertEquals("kkkkkkkk", actual.getAccountModelList().get(4).getAccountId().value());
    }

    @Test
    @Order(3)
    @DisplayName("正常系：アカウントが存在する場合（3ページ目、残り1件で最後のページと判定されること）")
    @Sql("/sql/common/cleanup.sql")
    @Sql("/sql/service/AccountServiceImplIntegrationTest.sql")
    void getAccountList_found_lastPage() {
      AccountListGetModel accountListGetModel = AccountListGetModel.builder().pageNo(3).build();
      AccountPageModel actual = accountServiceImpl.getAccountList(accountListGetModel);

      assertTrue(actual.getIsLast());
      assertEquals(1, actual.getAccountModelList().size());
      assertEquals("llllllll", actual.getAccountModelList().get(0).getAccountId().value());
    }

    @Test
    @Order(4)
    @DisplayName("正常系：アカウントが存在しない場合")
    void getAccountList_not_found() {
      AccountListGetModel accountListGetModel = AccountListGetModel.builder().pageNo(1).build();
      AccountPageModel actual = accountServiceImpl.getAccountList(accountListGetModel);
      assertEquals(0, actual.getAccountModelList().size());
      assertTrue(actual.getIsLast());
    }
  }

  @Nested
  @Order(6)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class getAccountListForAdmin {
    @Test
    @Order(1)
    @DisplayName("正常系：削除済みを含む全アカウントが存在する場合（1ページ目、5件に切り詰められ最後のページでないこと）")
    @Sql("/sql/common/cleanup.sql")
    @Sql("/sql/service/AccountServiceImplIntegrationTest.sql")
    void getAccountListForAdmin_found() {
      AccountListGetModel accountListGetModel = AccountListGetModel.builder().pageNo(1).build();
      AccountPageModel actual = accountServiceImpl.getAccountListForAdmin(accountListGetModel);

      assertFalse(actual.getIsLast());
      assertEquals(5, actual.getAccountModelList().size());
      assertEquals("aaaaaaaa", actual.getAccountModelList().get(0).getAccountId().value());
      assertEquals("bbbbbbbb", actual.getAccountModelList().get(1).getAccountId().value());
      assertEquals("cccccccc", actual.getAccountModelList().get(2).getAccountId().value());
      assertEquals("dddddddd", actual.getAccountModelList().get(3).getAccountId().value());
      assertEquals("eeeeeeee", actual.getAccountModelList().get(4).getAccountId().value());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：削除済みを含む全アカウントが存在する場合（2ページ目、削除済みアカウントも含まれること）")
    @Sql("/sql/common/cleanup.sql")
    @Sql("/sql/service/AccountServiceImplIntegrationTest.sql")
    void getAccountListForAdmin_found_secondPage() {
      AccountListGetModel accountListGetModel = AccountListGetModel.builder().pageNo(2).build();
      AccountPageModel actual = accountServiceImpl.getAccountListForAdmin(accountListGetModel);

      assertFalse(actual.getIsLast());
      assertEquals(5, actual.getAccountModelList().size());
      assertEquals("ffffffff", actual.getAccountModelList().get(0).getAccountId().value());
      assertEquals("gggggggg", actual.getAccountModelList().get(1).getAccountId().value());
      assertEquals("hhhhhhhh", actual.getAccountModelList().get(2).getAccountId().value());
      assertEquals("iiiiiiii", actual.getAccountModelList().get(3).getAccountId().value());
      assertEquals("jjjjjjjj", actual.getAccountModelList().get(4).getAccountId().value());
    }

    @Test
    @Order(3)
    @DisplayName("正常系：削除済みを含む全アカウントが存在する場合（3ページ目、残り2件で最後のページと判定されること）")
    @Sql("/sql/common/cleanup.sql")
    @Sql("/sql/service/AccountServiceImplIntegrationTest.sql")
    void getAccountListForAdmin_found_lastPage() {
      AccountListGetModel accountListGetModel = AccountListGetModel.builder().pageNo(3).build();
      AccountPageModel actual = accountServiceImpl.getAccountListForAdmin(accountListGetModel);

      assertTrue(actual.getIsLast());
      assertEquals(2, actual.getAccountModelList().size());
      assertEquals("kkkkkkkk", actual.getAccountModelList().get(0).getAccountId().value());
      assertEquals("llllllll", actual.getAccountModelList().get(1).getAccountId().value());
    }

    @Test
    @Order(4)
    @DisplayName("正常系：アカウントが存在しない場合")
    void getAccountListForAdmin_not_found() {
      AccountListGetModel accountListGetModel = AccountListGetModel.builder().pageNo(1).build();
      AccountPageModel actual = accountServiceImpl.getAccountListForAdmin(accountListGetModel);
      assertEquals(0, actual.getAccountModelList().size());
      assertTrue(actual.getIsLast());
    }
  }

  @Nested
  @Order(6)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/service/AccountServiceImplDeleteAccountIntegrationTest.sql")
  class deleteAccount {
    /** アカウント番号1のパスキーを保持する擬似認証器 */
    private PasskeyTestAuthenticator authenticator;

    @BeforeEach
    void registerPasskeyForReauthentication() throws GalleryException {
      authenticator = registerTestPasskey(new AccountNo(1L));
    }

    @Test
    @Order(1)
    @DisplayName("正常系：アカウントと関連データがすべて物理削除されること")
    void deleteAccount_success() throws GalleryException {
      accountServiceImpl.deleteAccount(
          new AccountNo(1L),
          new AccountId("aaaaaaaa"),
          reauthenticate(authenticator, new AccountNo(1L)));

      // アカウントが削除されたことを確認
      List<Account> accountData =
          jdbcTemplate.query(
              "SELECT * FROM common.account where account_no=1",
              (rs, rowNum) -> Account.builder().accountNo(rs.getLong("account_no")).build());
      assertEquals(0, accountData.size());

      // account_no=2のアカウントは残っていること
      List<Account> otherAccountData =
          jdbcTemplate.query(
              "SELECT * FROM common.account where account_no=2",
              (rs, rowNum) -> Account.builder().accountNo(rs.getLong("account_no")).build());
      assertEquals(1, otherAccountData.size());

      // 写真マスタが削除されたことを確認
      Integer photoMstCount =
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM photo.photo_mst where account_no=1", Integer.class);
      assertEquals(0, photoMstCount);

      // account_no=2の写真マスタは残っていること
      Integer otherPhotoMstCount =
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM photo.photo_mst where account_no=2", Integer.class);
      assertEquals(1, otherPhotoMstCount);

      // 写真タグが削除されたことを確認
      Integer photoTagCount =
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM photo.photo_tag_mst where account_no=1", Integer.class);
      assertEquals(0, photoTagCount);

      // 自分が登録したお気に入りが削除されたことを確認
      Integer favoriteByAccount =
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM photo.photo_favorite where account_no=1", Integer.class);
      assertEquals(0, favoriteByAccount);

      // 他人が自分の写真に対して登録したお気に入りが削除されたことを確認
      Integer favoriteForAccount =
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM photo.photo_favorite where favorite_photo_account_no=1",
              Integer.class);
      assertEquals(0, favoriteForAccount);

      // account_no=2が自分の写真をお気に入りにしたレコードは残っていること
      Integer otherFavoriteCount =
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM photo.photo_favorite where account_no=2 and favorite_photo_account_no=2",
              Integer.class);
      assertEquals(1, otherFavoriteCount);

      // リフレッシュトークンが失効・削除されたことを確認
      Integer refreshTokenCount =
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM common.refresh_token where account_no=1", Integer.class);
      assertEquals(0, refreshTokenCount);

      // account_no=2の有効なリフレッシュトークンは残っていること
      Integer otherRefreshTokenCount =
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM common.refresh_token where account_no=2 and is_revoked=false",
              Integer.class);
      assertEquals(1, otherRefreshTokenCount);

      // パスキー・リカバリーコードが削除されたことを確認
      assertEquals(
          0,
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM common.passkey_credential where account_no=1", Integer.class));
      assertEquals(
          0,
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM common.recovery_code where account_no=1", Integer.class));

      // アカウント権限が削除されたことを確認
      Integer accountAuthorityCount =
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM common.account_authority where account_no=1", Integer.class);
      assertEquals(0, accountAuthorityCount);

      // account_no=2のアカウント権限は残っていること
      Integer otherAccountAuthorityCount =
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM common.account_authority where account_no=2", Integer.class);
      assertEquals(1, otherAccountAuthorityCount);
    }

    @Test
    @Order(5)
    @DisplayName("異常系：管理者として投稿したお問い合わせ返信が残っている場合は削除されずBadRequestExceptionをthrowする")
    @Sql("/sql/common/cleanup.sql")
    @Sql("/sql/service/AccountServiceImplDeleteAccountIntegrationTest.sql")
    @Sql("/sql/service/AccountServiceImplDeleteAccountAdminReplyIntegrationTest.sql")
    void deleteAccount_withAdminReply() {
      // 返信を巻き込んで削除すると、無関係な第三者（account_no=2）のお問い合わせスレッドから
      // 回答本文だけが消えてしまうため、業務ルールとして削除自体を拒否する
      assertThrows(
          BadRequestException.class,
          () ->
              accountServiceImpl.deleteAccount(
                  new AccountNo(1L),
                  new AccountId("aaaaaaaa"),
                  reauthenticate(authenticator, new AccountNo(1L))));

      // アカウントも関連データも残っていること
      Integer accountCount =
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM common.account where account_no=1", Integer.class);
      assertEquals(1, accountCount);
      Integer photoMstCount =
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM photo.photo_mst where account_no=1", Integer.class);
      assertEquals(2, photoMstCount);

      // 他ユーザーのお問い合わせに紐づく返信が消えていないこと
      Integer replyByAccount =
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM common.inquiry_reply_mst where admin_account_no=1",
              Integer.class);
      assertEquals(1, replyByAccount);
    }

    @Test
    @Order(6)
    @DisplayName("正常系：自分のお問い合わせへの自己返信しか持たない場合は削除できる")
    @Sql("/sql/common/cleanup.sql")
    @Sql("/sql/service/AccountServiceImplDeleteAccountIntegrationTest.sql")
    @Sql("/sql/service/AccountServiceImplDeleteAccountSelfReplyIntegrationTest.sql")
    void deleteAccount_withSelfReplyOnly() throws GalleryException {
      // 自スレッドの返信は退会時にスレッドごと削除されるため、第三者のスレッドから回答本文だけが
      // 消えることはない。ブロックしてしまうと、動作確認等で自分のお問い合わせに返信した管理者が
      // 自力で退会できなくなる
      accountServiceImpl.deleteAccount(
          new AccountNo(1L),
          new AccountId("aaaaaaaa"),
          reauthenticate(authenticator, new AccountNo(1L)));

      Integer accountCount =
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM common.account where account_no=1", Integer.class);
      assertEquals(0, accountCount);

      // 自己返信もスレッドごと削除されていること
      Integer replyByAccount =
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM common.inquiry_reply_mst where admin_account_no=1",
              Integer.class);
      assertEquals(0, replyByAccount);
    }

    @Test
    @Order(2)
    @DisplayName("異常系：本人のものでないパスキーで署名した場合は削除されずForbiddenAccountExceptionをthrowする")
    void deleteAccount_reauthentication_failed() throws GalleryException {
      // 本人確認用のチャレンジはアカウント1に発行されているが、別のアカウント（2）のパスキーで署名する
      PasskeyResponseJson otherAuthenticatorsResponse =
          new PasskeyResponseJson(
              registerTestPasskey(new AccountNo(2L))
                  .createAuthenticationResponseJson(
                      PasskeyTestAuthenticator.RP_ID,
                      accountServiceImpl
                          .createReauthenticationOptions(new AccountNo(1L))
                          .getChallenge()
                          .value()));

      assertThrows(
          ForbiddenAccountException.class,
          () ->
              accountServiceImpl.deleteAccount(
                  new AccountNo(1L), new AccountId("aaaaaaaa"), otherAuthenticatorsResponse));

      Integer accountCount =
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM common.account where account_no=1", Integer.class);
      assertEquals(1, accountCount);
    }

    /**
     * 写真ファイルディレクトリの物理削除はトランザクションのコミット後に遅延実行される（{@code
     * AccountServiceImpl#deletePhotoDirectoryAfterCommit}）。DBロールバック時の不整合を防ぐための仕様であり、
     * トランザクションを実際にコミットしない限りこの経路は通らない。本テストでは{@link TestTransaction}で明示的にコミットし、
     * afterCommitコールバック内でfileRepository.deleteByPrefixが呼ばれることを検証する
     */
    @Test
    @Order(3)
    @DisplayName("正常系：トランザクションコミット後に写真ファイルディレクトリが物理削除される")
    void deleteAccount_deletesPhotoDirectoryAfterCommit() throws GalleryException {
      accountServiceImpl.deleteAccount(
          new AccountNo(1L),
          new AccountId("aaaaaaaa"),
          reauthenticate(authenticator, new AccountNo(1L)));
      verify(fileRepository, never()).deleteByPrefix(any(ImageFilePath.class));

      TestTransaction.flagForCommit();
      TestTransaction.end();
      TestTransaction.start();

      verify(fileRepository, times(1)).deleteByPrefix(new ImageFilePath("1/"));

      // 上記で物理コミットした削除結果が、通常の@Transactionalによる自動ロールバックに
      // 乗らないまま他のNestedクラスへ残留しないよう、明示的にTRUNCATE（CASCADE）して物理コミットする
      jdbcTemplate.execute(
          """
          TRUNCATE TABLE
          	photo.photo_favorite,
          	photo.photo_tag_mst,
          	photo.photo_mst,
          	common.refresh_token,
          	common.location_mst,
          	common.account,
          	common.kbn_mst
          CASCADE
          """);
      TestTransaction.flagForCommit();
      TestTransaction.end();
    }

    @Test
    @Order(4)
    @DisplayName("異常系：管理者ロック中のアカウントは、本人のパスキーで署名してもForbiddenAccountExceptionをthrowする")
    void deleteAccount_isReauthLocked_adminLocked() {
      jdbcTemplate.update("UPDATE common.account SET is_admin_locked=true WHERE account_no=1");

      assertThrows(
          ForbiddenAccountException.class,
          () ->
              accountServiceImpl.deleteAccount(
                  new AccountNo(1L),
                  new AccountId("aaaaaaaa"),
                  reauthenticate(authenticator, new AccountNo(1L))));

      Integer accountCount =
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM common.account WHERE account_no=1", Integer.class);
      assertEquals(1, accountCount);
    }

    @Test
    @Order(5)
    @DisplayName("異常系：ログイン失敗回数が上限のアカウントは、本人のパスキーで署名してもForbiddenAccountExceptionをthrowする")
    void deleteAccount_isReauthLocked_failCountLocked() {
      jdbcTemplate.update("UPDATE common.account SET login_failure_count=3 WHERE account_no=1");

      assertThrows(
          ForbiddenAccountException.class,
          () ->
              accountServiceImpl.deleteAccount(
                  new AccountNo(1L),
                  new AccountId("aaaaaaaa"),
                  reauthenticate(authenticator, new AccountNo(1L))));

      Integer accountCount =
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM common.account WHERE account_no=1", Integer.class);
      assertEquals(1, accountCount);
    }
  }

  @Nested
  @Order(7)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/service/AccountServiceImplIntegrationTest.sql")
  class handleAuthenticationSuccess {
    @Test
    @Order(1)
    @DisplayName("正常系")
    void handle_success() throws GalleryException {
      String username = "kkkkkkkk";

      List<GrantedAuthority> authorities = new ArrayList<>();
      authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));

      Authentication authentication =
          new UsernamePasswordAuthenticationToken(username, null, authorities);
      AuthenticationSuccessEvent event = new AuthenticationSuccessEvent(authentication);

      OffsetDateTime transactionNow =
          jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);
      OffsetDateTime beforeLogin = OffsetDateTime.now();
      accountServiceImpl.handle(event);
      OffsetDateTime afterLogin = OffsetDateTime.now();

      List<Account> actualData =
          jdbcTemplate.query(
              "SELECT * FROM common.account where account_no=11",
              (rs, rowNum) ->
                  Account.builder()
                      .accountNo(rs.getLong("account_no"))
                      .createdBy(rs.getLong("created_by"))
                      .createdAt(rs.getObject("created_at", OffsetDateTime.class))
                      .updatedBy(rs.getLong("updated_by"))
                      .updatedAt(rs.getObject("updated_at", OffsetDateTime.class))
                      .isDeleted(rs.getBoolean("is_deleted"))
                      .accountId(rs.getString("account_id"))
                      .accountName(rs.getString("account_name"))
                      .birthdate(rs.getObject("birthdate", LocalDate.class))
                      .sexKbn(SexEnum.getOrDefault(rs.getString("sex_kbn")))
                      .birthplacePrefectureKbnCode(rs.getString("birthplace_prefecture_kbn_code"))
                      .residentPrefectureKbnCode(rs.getString("resident_prefecture_kbn_code"))
                      .freeMemo(rs.getString("free_memo"))
                      .lastLoginDatetime(rs.getObject("last_login_datetime", OffsetDateTime.class))
                      .loginFailureCount(rs.getInt("login_failure_count"))
                      .isAdminLocked(rs.getBoolean("is_admin_locked"))
                      .build());

      assertEquals(1, actualData.size());
      assertEquals(11L, actualData.getFirst().getAccountNo());
      assertEquals(11L, actualData.getFirst().getCreatedBy());
      assertEquals(
          OffsetDateTime.of(2000, 1, 11, 0, 0, 0, 0, ZoneOffset.ofHours(0)),
          actualData.getFirst().getCreatedAt());
      assertEquals(11L, actualData.getFirst().getUpdatedBy());
      assertEquals(transactionNow, actualData.getFirst().getUpdatedAt());
      assertFalse(actualData.getFirst().getIsDeleted());
      assertEquals("kkkkkkkk", actualData.getFirst().getAccountId());
      assertEquals("KKKKKKKK", actualData.getFirst().getAccountName());
      assertEquals(LocalDate.of(1990, 1, 1), actualData.getFirst().getBirthdate());
      assertEquals(SexEnum.WOMAN, actualData.getFirst().getSexKbn());
      assertEquals("Okinawa", actualData.getFirst().getBirthplacePrefectureKbnCode());
      assertEquals("Tokyo", actualData.getFirst().getResidentPrefectureKbnCode());
      assertEquals("よろしく", actualData.getFirst().getFreeMemo());
      assertFalse(actualData.getFirst().getLastLoginDatetime().isBefore(beforeLogin));
      assertFalse(actualData.getFirst().getLastLoginDatetime().isAfter(afterLogin));
      assertEquals(0, actualData.getFirst().getLoginFailureCount());
      assertFalse(actualData.getFirst().getIsAdminLocked());
    }
  }
}
