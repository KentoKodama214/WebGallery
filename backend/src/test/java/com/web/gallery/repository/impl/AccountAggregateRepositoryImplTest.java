package com.web.gallery.repository.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;

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
import org.springframework.test.context.ActiveProfiles;

import com.web.gallery.aggregate.Account;
import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.photo.PhotoNo;
import com.web.gallery.dto.PhotoDeletionDto;
import com.web.gallery.entity.AccountCondition;
import com.web.gallery.entity.PhotoFavoriteCondition;
import com.web.gallery.entity.PhotoTagMstCondition;
import com.web.gallery.mapper.AccountMapper;
import com.web.gallery.mapper.PhotoFavoriteMapper;
import com.web.gallery.mapper.PhotoMstMapper;
import com.web.gallery.mapper.PhotoTagMstMapper;
import com.web.gallery.mapper.RefreshTokenMapper;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class AccountAggregateRepositoryImplTest {
	@InjectMocks
	private AccountAggregateRepositoryImpl accountAggregateRepositoryImpl;

	@Mock
	private AccountMapper accountMapper;

	@Mock
	private PhotoFavoriteMapper photoFavoriteMapper;

	@Mock
	private PhotoTagMstMapper photoTagMstMapper;

	@Mock
	private PhotoMstMapper photoMstMapper;

	@Mock
	private RefreshTokenMapper refreshTokenMapper;

	@Nested
	@Order(1)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class delete {
		@Test
		@Order(1)
		@DisplayName("正常系：お気に入り・タグ・写真・リフレッシュトークン・アカウント本体を削除し、未削除だった写真番号を記録すること")
		void delete_success() {
			PhotoDeletionDto undeleted = new PhotoDeletionDto();
			undeleted.setPhotoNo(1L);
			undeleted.setIsDeleted(false);

			PhotoDeletionDto alreadyDeleted = new PhotoDeletionDto();
			alreadyDeleted.setPhotoNo(2L);
			alreadyDeleted.setIsDeleted(true);

			doReturn(List.of(undeleted, alreadyDeleted)).when(photoMstMapper).deletePhotosByAccountNo(1L);

			Account account = Account.forDelete(new AccountNo(1L));
			accountAggregateRepositoryImpl.delete(account);

			ArgumentCaptor<PhotoFavoriteCondition> favoriteConditionCaptor = ArgumentCaptor.forClass(PhotoFavoriteCondition.class);
			verify(photoFavoriteMapper, times(2)).delete(favoriteConditionCaptor.capture());
			List<PhotoFavoriteCondition> favoriteConditions = favoriteConditionCaptor.getAllValues();
			assertEquals(1L, favoriteConditions.get(0).getAccountNo());
			assertEquals(1L, favoriteConditions.get(1).getFavoritePhotoAccountNo());

			ArgumentCaptor<PhotoTagMstCondition> tagConditionCaptor = ArgumentCaptor.forClass(PhotoTagMstCondition.class);
			verify(photoTagMstMapper).delete(tagConditionCaptor.capture());
			assertEquals(1L, tagConditionCaptor.getValue().getAccountNo());

			verify(photoMstMapper).deletePhotosByAccountNo(1L);

			verify(refreshTokenMapper).revokeAllByAccountNo(1L, 1L);

			ArgumentCaptor<AccountCondition> accountConditionCaptor = ArgumentCaptor.forClass(AccountCondition.class);
			verify(accountMapper).delete(accountConditionCaptor.capture());
			assertEquals(1L, accountConditionCaptor.getValue().getAccountNo());

			assertEquals(List.of(new PhotoNo(1L)), account.getDeletedPhotoNoList().toList());
		}

		@Test
		@Order(2)
		@DisplayName("正常系：削除対象の写真が存在しない場合、削除された写真番号一覧は空になること")
		void delete_no_photos() {
			doReturn(List.of()).when(photoMstMapper).deletePhotosByAccountNo(1L);

			Account account = Account.forDelete(new AccountNo(1L));
			accountAggregateRepositoryImpl.delete(account);

			assertTrue(account.getDeletedPhotoNoList().isEmpty());
		}
	}
}
