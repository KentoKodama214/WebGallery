package com.web.gallery.service.impl.integration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.web.gallery.domain.account.AccountId;
import com.web.gallery.domain.account.AccountName;
import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.account.BirthplacePrefectureKbnCode;
import com.web.gallery.domain.account.LoginFailureCount;
import com.web.gallery.domain.account.Password;
import com.web.gallery.domain.account.ResidentPrefectureKbnCode;
import com.web.gallery.domain.photo.ImageFilePath;
import com.web.gallery.entity.account.Account;
import com.web.gallery.enumeration.AuthorityEnum;
import com.web.gallery.enumeration.SexEnum;
import com.web.gallery.exception.BadRequestException;
import com.web.gallery.exception.ForbiddenAccountException;
import com.web.gallery.exception.GalleryException;
import com.web.gallery.exception.UpdateFailureException;
import com.web.gallery.model.account.AccountListGetModel;
import com.web.gallery.model.account.AccountModel;
import com.web.gallery.model.account.AccountPageModel;
import com.web.gallery.repository.FileRepository;
import com.web.gallery.service.impl.AccountServiceImpl;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
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
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
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

  /** S3ストレージアクセスはモックする（統合テストでは実ストレージへ接続しない） */
  @MockitoBean private FileRepository fileRepository;

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
      assertEquals("$2a$10$password1", userDetails.getPassword());
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
    @DisplayName("正常系：アカウントを新規登録")
    @Sql("/sql/common/cleanup.sql")
    @Sql("/sql/common/ResetAccountNoSeq.sql")
    void registAccount_success() throws GalleryException {
      AccountModel accountModel =
          AccountModel.builder()
              .accountId(new AccountId("mmmmmmmm"))
              .accountName(new AccountName("MMMMMMMM"))
              .password(new Password("mmmmmmmm"))
              .build();

      OffsetDateTime transactionNow =
          jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);
      assertTrue(accountServiceImpl.registAccount(accountModel));

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
                      .password(rs.getString("password"))
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
    }

    @Test
    @Order(2)
    @DisplayName("正常系：アカウントが既に存在する")
    @Sql("/sql/common/cleanup.sql")
    @Sql("/sql/service/AccountServiceImplIntegrationTest.sql")
    void registAccount_account_already_exist() throws GalleryException {
      AccountModel accountModel =
          AccountModel.builder()
              .accountId(new AccountId("aaaaaaaa"))
              .accountName(new AccountName("AAAAAAAA"))
              .password(new Password("aaaaaaaa"))
              .build();
      assertFalse(accountServiceImpl.registAccount(accountModel));
    }

    @Test
    @Order(3)
    @DisplayName("異常系：区分マスタに存在しない都道府県区分コードの場合、BadRequestExceptionをthrowする")
    @Sql("/sql/common/cleanup.sql")
    @Sql("/sql/common/ResetAccountNoSeq.sql")
    void registAccount_invalid_prefecture_code() {
      AccountModel accountModel =
          AccountModel.builder()
              .accountId(new AccountId("nnnnnnnn"))
              .accountName(new AccountName("NNNNNNNN"))
              .password(new Password("nnnnnnnn"))
              .birthplacePrefectureKbnCode(new BirthplacePrefectureKbnCode("NotExistCode"))
              .build();

      assertThrows(BadRequestException.class, () -> accountServiceImpl.registAccount(accountModel));
    }

    @Test
    @Order(4)
    @DisplayName("異常系：出身地が未指定で在住地が区分マスタに存在しない都道府県区分コードの場合、BadRequestExceptionをthrowする")
    @Sql("/sql/common/cleanup.sql")
    @Sql("/sql/common/ResetAccountNoSeq.sql")
    @Sql("/sql/common/PrefectureKbnMst.sql")
    void registAccount_invalid_resident_prefecture_code() {
      AccountModel accountModel =
          AccountModel.builder()
              .accountId(new AccountId("oooooooo"))
              .accountName(new AccountName("OOOOOOOO"))
              .password(new Password("oooooooo"))
              .residentPrefectureKbnCode(new ResidentPrefectureKbnCode("NotExistCode"))
              .build();

      assertThrows(BadRequestException.class, () -> accountServiceImpl.registAccount(accountModel));
    }

    @Test
    @Order(5)
    @DisplayName("正常系：区分マスタに実在する都道府県区分コードを指定した場合、登録できること")
    @Sql("/sql/common/cleanup.sql")
    @Sql("/sql/common/ResetAccountNoSeq.sql")
    @Sql("/sql/common/PrefectureKbnMst.sql")
    void registAccount_valid_prefecture_code() throws GalleryException {
      AccountModel accountModel =
          AccountModel.builder()
              .accountId(new AccountId("pppppppp"))
              .accountName(new AccountName("PPPPPPPP"))
              .password(new Password("pppppppp"))
              .birthplacePrefectureKbnCode(new BirthplacePrefectureKbnCode("Tokyo"))
              .residentPrefectureKbnCode(new ResidentPrefectureKbnCode("Tokyo"))
              .build();

      assertTrue(accountServiceImpl.registAccount(accountModel));

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
      assertFalse(accountServiceImpl.updateAccount(accountModel, null));

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
                      .password(rs.getString("password"))
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
      assertEquals("$2a$10$password1", actualData.getFirst().getPassword());
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
      assertTrue(accountServiceImpl.updateAccount(accountModel, null));

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
                      .password(rs.getString("password"))
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
      assertEquals("$2a$10$password1", actualData.getFirst().getPassword());
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
          UpdateFailureException.class, () -> accountServiceImpl.updateAccount(accountModel, null));
    }

    @Test
    @Order(4)
    @DisplayName("正常系：パスワードのみ変更（アカウントID変更なし）した場合、リフレッシュトークンが失効すること")
    void updateAccount_change_password_only() throws GalleryException {
      // フィクスチャのパスワードはBCrypt照合できないダミー値のため、実際に照合可能な値へ差し替える
      jdbcTemplate.update(
          "UPDATE common.account SET password=? WHERE account_no=1",
          "$2a$10$k19cLX6F2brrOLYp74GstejFJfeGjm52TMk..ELwoJeTcBcUnBVM2");
      jdbcTemplate.update(
          "INSERT INTO common.refresh_token VALUES(DEFAULT, 1, 'hash-account1', NOW() + interval '7 days', NOW(), 1, NOW(), false)");

      AccountModel accountModel =
          AccountModel.builder()
              .accountNo(new AccountNo(1L))
              .accountId(new AccountId("aaaaaaaa"))
              .password(new Password("newpassword01"))
              .build();

      assertFalse(accountServiceImpl.updateAccount(accountModel, new Password("password01")));

      Integer activeRefreshTokenCount =
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM common.refresh_token WHERE account_no=1 AND is_revoked=false",
              Integer.class);
      assertEquals(0, activeRefreshTokenCount);
    }

    @Test
    @Order(5)
    @DisplayName("正常系：アカウントID・パスワードいずれも変更しない場合、リフレッシュトークンは失効しないこと")
    void updateAccount_no_change() throws GalleryException {
      jdbcTemplate.update(
          "INSERT INTO common.refresh_token VALUES(DEFAULT, 1, 'hash-account1', NOW() + interval '7 days', NOW(), 1, NOW(), false)");

      AccountModel accountModel =
          AccountModel.builder()
              .accountNo(new AccountNo(1L))
              .accountId(new AccountId("aaaaaaaa"))
              .build();

      assertFalse(accountServiceImpl.updateAccount(accountModel, null));

      Integer activeRefreshTokenCount =
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM common.refresh_token WHERE account_no=1 AND is_revoked=false",
              Integer.class);
      assertEquals(1, activeRefreshTokenCount);
    }

    @Test
    @Order(6)
    @DisplayName("正常系：アカウントIDのみ変更（パスワード変更なし）した場合、リフレッシュトークンが失効すること")
    void updateAccount_change_accountId_only() throws GalleryException {
      jdbcTemplate.update(
          "INSERT INTO common.refresh_token VALUES(DEFAULT, 1, 'hash-account1', NOW() + interval '7 days', NOW(), 1, NOW(), false)");

      AccountModel accountModel =
          AccountModel.builder()
              .accountNo(new AccountNo(1L))
              .accountId(new AccountId("newaccid"))
              .build();

      assertFalse(accountServiceImpl.updateAccount(accountModel, null));

      Integer activeRefreshTokenCount =
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM common.refresh_token WHERE account_no=1 AND is_revoked=false",
              Integer.class);
      assertEquals(0, activeRefreshTokenCount);
    }

    /**
     * Controller層（{@code AccountController}）は新パスワード指定時に現在のパスワード未指定を事前に弾くため、
     * Service層のこのダミー照合（応答時間を一定に保つためのBCrypt照合1回分のコスト）へは通常到達しない。 Service単体でこの防御的分岐を直接検証する
     */
    @Test
    @Order(7)
    @DisplayName("異常系：パスワード変更時に現在のパスワードが未指定の場合、ForbiddenAccountExceptionをthrowする")
    void updateAccount_passwordChange_missingCurrentPassword() {
      AccountModel accountModel =
          AccountModel.builder()
              .accountNo(new AccountNo(1L))
              .accountId(new AccountId("aaaaaaaa"))
              .password(new Password("newpassword01"))
              .build();

      assertThrows(
          ForbiddenAccountException.class,
          () -> accountServiceImpl.updateAccount(accountModel, null));
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
      assertEquals("$2a$10$password1", actual.getPassword().value());
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
    @Test
    @Order(1)
    @DisplayName("正常系：アカウントと関連データがすべて物理削除されること")
    void deleteAccount_success() throws GalleryException {
      accountServiceImpl.deleteAccount(
          new AccountNo(1L), new AccountId("aaaaaaaa"), new Password("password01"));

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
    @Order(2)
    @DisplayName("異常系：現在のパスワードが一致しない場合は削除されずForbiddenAccountExceptionをthrowする")
    void deleteAccount_currentPassword_mismatch() {
      assertThrows(
          ForbiddenAccountException.class,
          () ->
              accountServiceImpl.deleteAccount(
                  new AccountNo(1L), new AccountId("aaaaaaaa"), new Password("wrongpassword")));

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
          new AccountNo(1L), new AccountId("aaaaaaaa"), new Password("password01"));
      verify(fileRepository, never()).deleteByPrefix(any(ImageFilePath.class));

      TestTransaction.flagForCommit();
      TestTransaction.end();
      TestTransaction.start();

      verify(fileRepository, times(1)).deleteByPrefix(new ImageFilePath("aaaaaaaa/"));

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
    @DisplayName("異常系：管理者ロック中のアカウントは、正しいパスワードでもForbiddenAccountExceptionをthrowする")
    void deleteAccount_isReauthLocked_adminLocked() {
      jdbcTemplate.update("UPDATE common.account SET is_admin_locked=true WHERE account_no=1");

      assertThrows(
          ForbiddenAccountException.class,
          () ->
              accountServiceImpl.deleteAccount(
                  new AccountNo(1L), new AccountId("aaaaaaaa"), new Password("password01")));

      Integer accountCount =
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM common.account WHERE account_no=1", Integer.class);
      assertEquals(1, accountCount);
    }

    @Test
    @Order(5)
    @DisplayName("異常系：ログイン失敗回数が上限のアカウントは、正しいパスワードでもForbiddenAccountExceptionをthrowする")
    void deleteAccount_isReauthLocked_failCountLocked() {
      jdbcTemplate.update("UPDATE common.account SET login_failure_count=3 WHERE account_no=1");

      assertThrows(
          ForbiddenAccountException.class,
          () ->
              accountServiceImpl.deleteAccount(
                  new AccountNo(1L), new AccountId("aaaaaaaa"), new Password("password01")));

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
      String password = "KKKKKKKK";

      List<GrantedAuthority> authorities = new ArrayList<>();
      authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));

      Authentication authentication =
          new UsernamePasswordAuthenticationToken(username, password, authorities);
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
                      .password(rs.getString("password"))
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
      assertEquals("$2a$10$password11", actualData.getFirst().getPassword());
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

  @Nested
  @Order(8)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/service/AccountServiceImplIntegrationTest.sql")
  class handleAuthenticationFailureBadCredentials {
    @BeforeEach
    void setUp() {
      // handle()のREQUIRES_NEWによる別コネクションの更新が@Sqlで投入したフィクスチャ行を
      // 参照できるよう、投入データ（TRUNCATE・INSERT）を物理コミットしてから
      // 新しいテスト用トランザクションを開始する
      TestTransaction.flagForCommit();
      TestTransaction.end();
      TestTransaction.start();
    }

    @AfterEach
    void tearDown() {
      // テスト本体が例外系の場合、現在のテストトランザクションはロールバック専用に
      // なっている可能性があるため、一度終了・再開してクリーンな状態にしてからTRUNCATEし、
      // setUp()で物理コミットしたフィクスチャ行が後続の他テストへ残留しないよう明示的に物理コミットする
      TestTransaction.end();
      TestTransaction.start();
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
    @Order(1)
    @DisplayName("正常系：アカウントが存在する場合")
    void handle_account_found() throws GalleryException {
      String username = "aaaaaaaa";
      String password = "AAAAAAAA";

      List<GrantedAuthority> authorities = new ArrayList<>();
      authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
      Authentication authentication =
          new UsernamePasswordAuthenticationToken(username, password, authorities);

      String message = "Invalid username or password";
      BadCredentialsException exception = new BadCredentialsException(message);

      AuthenticationFailureBadCredentialsEvent event =
          new AuthenticationFailureBadCredentialsEvent(authentication, exception);

      // handle()はREQUIRES_NEWで本メソッドの呼び出しとは別の物理トランザクションで実行されるため、
      // DBのNOW()ではなくJavaのOffsetDateTime.now()で挟んだ時間範囲でupdatedAtを検証する
      OffsetDateTime beforeHandle = OffsetDateTime.now();
      accountServiceImpl.handle(event);
      OffsetDateTime afterHandle = OffsetDateTime.now();

      List<Account> actualData =
          jdbcTemplate.query(
              "SELECT * FROM common.account where account_id='aaaaaaaa'",
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
                      .password(rs.getString("password"))
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
      assertFalse(actualData.getFirst().getUpdatedAt().isBefore(beforeHandle));
      assertFalse(actualData.getFirst().getUpdatedAt().isAfter(afterHandle));
      assertFalse(actualData.getFirst().getIsDeleted());
      assertEquals("aaaaaaaa", actualData.getFirst().getAccountId());
      assertEquals("AAAAAAAA", actualData.getFirst().getAccountName());
      assertEquals("$2a$10$password1", actualData.getFirst().getPassword());
      assertEquals(LocalDate.of(1991, 2, 14), actualData.getFirst().getBirthdate());
      assertEquals(SexEnum.NONE, actualData.getFirst().getSexKbn());
      assertEquals("none", actualData.getFirst().getBirthplacePrefectureKbnCode());
      assertEquals("none", actualData.getFirst().getResidentPrefectureKbnCode());
      assertEquals("", actualData.getFirst().getFreeMemo());
      assertEquals(
          OffsetDateTime.of(2002, 1, 1, 9, 0, 0, 0, ZoneOffset.ofHours(0)),
          actualData.getFirst().getLastLoginDatetime().plusHours(9));
      assertEquals(1, actualData.getFirst().getLoginFailureCount());
      assertFalse(actualData.getFirst().getIsAdminLocked());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：アカウントが存在しない場合")
    void handle_account_not_found() throws GalleryException {
      String username = "zzzzzzzz";
      String password = "ZZZZZZZZ";

      List<GrantedAuthority> authorities = new ArrayList<>();
      authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
      Authentication authentication =
          new UsernamePasswordAuthenticationToken(username, password, authorities);

      String message = "Invalid username or password";
      BadCredentialsException exception = new BadCredentialsException(message);

      AuthenticationFailureBadCredentialsEvent event =
          new AuthenticationFailureBadCredentialsEvent(authentication, exception);

      accountServiceImpl.handle(event);

      List<Account> actualData =
          jdbcTemplate.query(
              "SELECT * FROM common.account where account_id='zzzzzzzz'",
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
                      .password(rs.getString("password"))
                      .birthdate(rs.getObject("birthdate", LocalDate.class))
                      .sexKbn(SexEnum.getOrDefault(rs.getString("sex_kbn")))
                      .birthplacePrefectureKbnCode(rs.getString("birthplace_prefecture_kbn_code"))
                      .residentPrefectureKbnCode(rs.getString("resident_prefecture_kbn_code"))
                      .freeMemo(rs.getString("free_memo"))
                      .lastLoginDatetime(rs.getObject("last_login_datetime", OffsetDateTime.class))
                      .loginFailureCount(rs.getInt("login_failure_count"))
                      .isAdminLocked(rs.getBoolean("is_admin_locked"))
                      .build());

      assertEquals(0, actualData.size());
    }
  }
}
