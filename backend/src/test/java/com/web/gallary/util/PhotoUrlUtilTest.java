package com.web.gallary.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class PhotoUrlUtilTest {
	@Nested
	@Order(1)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class getPhotoListUrl {
		@Test
		@Order(1)
		@DisplayName("正常系")
		void getPhotoListUrl_success() {
			String accountId = "dummyAccountId";
			assertEquals("/photo/" + accountId + "/photo_list", PhotoUrlUtil.getPhotoListUrl(accountId));
		}
		
		@Test
		@Order(2)
		@DisplayName("正常系：アカウントIDがNullの場合、ログインページのパスを返す")
		void getPhotoListUrl_accountId_is_null() {
			assertEquals("/login", PhotoUrlUtil.getPhotoListUrl(null));
		}

		@Test
		@Order(3)
		@DisplayName("正常系：アカウントIDが空文字の場合、ログインページのパスを返す")
		void getPhotoListUrl_accountId_is_empty() {
			assertEquals("/login", PhotoUrlUtil.getPhotoListUrl(" "));
		}
	}
}