package com.web.gallery.controller.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.web.gallery.AccountPrincipal;
import com.web.gallery.domain.account.AccountId;
import com.web.gallery.domain.account.AccountName;
import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.account.Password;
import com.web.gallery.enumeration.AuthorityEnum;
import com.web.gallery.model.account.AccountModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
@AutoConfigureMockMvc
public class LocationControllerIntegrationTest {
  @Autowired private MockMvc mockMvc;

  private Authentication authenticationFor(long accountNo, String accountId, String password) {
    AccountModel sessionAccount =
        AccountModel.builder()
            .accountNo(new AccountNo(accountNo))
            .accountId(new AccountId(accountId))
            .accountName(new AccountName(accountId.toUpperCase()))
            .password(new Password(password))
            .authorityKbn(AuthorityEnum.NORMAL)
            .build();
    AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
    return new UsernamePasswordAuthenticationToken(
        accountPrincipal, null, accountPrincipal.getAuthorities());
  }

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/controller/LocationControllerIntegrationTest.sql")
  class getLocationList {
    @Test
    @Order(1)
    @DisplayName("正常系：本人が登録済みの、削除されていないロケーションのみを取得できること")
    void getLocationList_success() throws Exception {
      mockMvc
          .perform(
              get("/api/v1/accounts/aaaaaaaa/locations")
                  .with(
                      SecurityMockMvcRequestPostProcessors.authentication(
                          authenticationFor(1L, "aaaaaaaa", "$2a$10$password1"))))
          .andExpect(status().isOk())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.locations.length()").value(1))
          .andExpect(jsonPath("$.locations[0].locationNo").value(1))
          .andExpect(jsonPath("$.locations[0].locationName").value("渋谷スクランブル交差点"))
          .andExpect(jsonPath("$.locations[0].address").value("東京都渋谷区"))
          .andExpect(jsonPath("$.locations[0].latitude").value(35.6812))
          .andExpect(jsonPath("$.locations[0].longitude").value(139.7671));
    }

    @Test
    @Order(2)
    @DisplayName("正常系：他人のアカウントを指定した場合、空のリストを返すこと（他人のロケーションは漏洩しない）")
    void getLocationList_other_account() throws Exception {
      mockMvc
          .perform(
              get("/api/v1/accounts/aaaaaaaa/locations")
                  .with(
                      SecurityMockMvcRequestPostProcessors.authentication(
                          authenticationFor(2L, "bbbbbbbb", "$2a$10$password2"))))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.locations").isArray())
          .andExpect(jsonPath("$.locations").isEmpty());
    }

    @Test
    @Order(3)
    @DisplayName("異常系：未認証の場合、401で共通JSONエラーを返すこと")
    void getLocationList_unauthenticated() throws Exception {
      mockMvc
          .perform(get("/api/v1/accounts/aaaaaaaa/locations"))
          .andExpect(status().isUnauthorized())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.httpStatus").value(HttpStatus.UNAUTHORIZED.value()));
    }
  }
}
