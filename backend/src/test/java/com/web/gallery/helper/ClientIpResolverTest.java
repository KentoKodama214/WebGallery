package com.web.gallery.helper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.web.gallery.domain.common.IpAddress;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class ClientIpResolverTest {

  @InjectMocks private ClientIpResolver clientIpResolver;

  @Mock private HttpServletRequest request;

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class resolve {
    @Test
    @Order(1)
    @DisplayName("正常系：送信元IPアドレスが取得できる場合、そのまま返すこと")
    void resolve_success() {
      doReturn("192.0.2.1").when(request).getRemoteAddr();

      IpAddress actual = clientIpResolver.resolve(request);

      assertEquals(new IpAddress("192.0.2.1"), actual);
    }

    @Test
    @Order(2)
    @DisplayName("異常系：送信元IPアドレスがnullの場合、unknownを返すこと")
    void resolve_null() {
      doReturn(null).when(request).getRemoteAddr();

      IpAddress actual = clientIpResolver.resolve(request);

      assertEquals(new IpAddress("unknown"), actual);
    }

    @Test
    @Order(3)
    @DisplayName("異常系：送信元IPアドレスが空文字の場合、unknownを返すこと")
    void resolve_blank() {
      doReturn("").when(request).getRemoteAddr();

      IpAddress actual = clientIpResolver.resolve(request);

      assertEquals(new IpAddress("unknown"), actual);
    }
  }
}
