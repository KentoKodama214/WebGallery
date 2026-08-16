package com.web.gallery.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.test.context.ActiveProfiles;

import com.web.gallery.domain.account.AccountId;
import com.web.gallery.domain.common.IsDeleted;

@ActiveProfiles("test")
public class AccountModelListTest {

	@Nested
	@Order(1)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class filterByIsDeleted {
		@Test
		@Order(1)
		@DisplayName("正常系：削除フラグに一致するアカウントのみに絞り込まれること")
		void filterByIsDeleted_success() {
			AccountModelList accountModelList = AccountModelList.of(List.of(
					AccountModel.builder().accountId(new AccountId("aaaaaaaa")).isDeleted(new IsDeleted(true)).build(),
					AccountModel.builder().accountId(new AccountId("bbbbbbbb")).isDeleted(new IsDeleted(false)).build()));

			AccountModelList actual = accountModelList.filterByIsDeleted(false);

			assertEquals(1, actual.size());
			assertEquals(new AccountId("bbbbbbbb"), actual.get(0).getAccountId());
		}
	}
}
