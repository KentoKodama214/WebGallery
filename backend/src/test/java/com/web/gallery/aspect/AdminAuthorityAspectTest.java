package com.web.gallery.aspect;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.web.gallery.enumeration.AuthorityEnum;
import com.web.gallery.enumeration.ErrorEnum;
import com.web.gallery.exception.ForbiddenAccountException;
import com.web.gallery.helper.SessionHelper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class AdminAuthorityAspectTest {
  @InjectMocks private AdminAuthorityAspect adminAuthorityAspect;

  @Mock private SessionHelper sessionHelper;

  @Test
  @DisplayName("正常系：管理者権限を持つ場合は例外が発生しないこと")
  void validateAdminAuthority_success() {
    doReturn(AuthorityEnum.ADMINISTRATOR).when(sessionHelper).getAuthorityKbn();

    assertDoesNotThrow(() -> adminAuthorityAspect.validateAdminAuthority());
  }

  @Test
  @DisplayName("異常系：管理者権限を持たない場合はForbiddenAccountExceptionが発生すること")
  void validateAdminAuthority_forbidden() {
    doReturn(AuthorityEnum.NORMAL).when(sessionHelper).getAuthorityKbn();

    ForbiddenAccountException exception =
        assertThrows(
            ForbiddenAccountException.class, () -> adminAuthorityAspect.validateAdminAuthority());

    assertEquals(ErrorEnum.NOT_AUTHORIZED_TO_ADMIN.getErrorCode(), exception.getErrorCode());
  }
}
