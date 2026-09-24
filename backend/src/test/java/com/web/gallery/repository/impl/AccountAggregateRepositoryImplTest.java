package com.web.gallery.repository.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.web.gallery.aggregate.Account;
import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.photo.PhotoNo;
import com.web.gallery.dto.PhotoDeletionDto;
import com.web.gallery.entity.account.AccountAuthorityCondition;
import com.web.gallery.entity.account.AccountCondition;
import com.web.gallery.entity.account.LoginHistoryCondition;
import com.web.gallery.entity.photo.PhotoFavoriteCondition;
import com.web.gallery.entity.photo.PhotoListFilterLogCondition;
import com.web.gallery.entity.photo.PhotoTagMstCondition;
import com.web.gallery.entity.photo.PhotoViewLogCondition;
import com.web.gallery.mapper.AccountAuthorityMapper;
import com.web.gallery.mapper.AccountMapper;
import com.web.gallery.mapper.LoginHistoryMapper;
import com.web.gallery.mapper.PhotoFavoriteMapper;
import com.web.gallery.mapper.PhotoListFilterLogMapper;
import com.web.gallery.mapper.PhotoMstMapper;
import com.web.gallery.mapper.PhotoTagMstMapper;
import com.web.gallery.mapper.PhotoViewLogMapper;
import com.web.gallery.mapper.RefreshTokenMapper;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class AccountAggregateRepositoryImplTest {
  @InjectMocks private AccountAggregateRepositoryImpl accountAggregateRepositoryImpl;

  @Mock private AccountMapper accountMapper;

  @Mock private AccountAuthorityMapper accountAuthorityMapper;

  @Mock private PhotoFavoriteMapper photoFavoriteMapper;

  @Mock private PhotoTagMstMapper photoTagMstMapper;

  @Mock private PhotoMstMapper photoMstMapper;

  @Mock private RefreshTokenMapper refreshTokenMapper;

  @Mock private LoginHistoryMapper loginHistoryMapper;

  @Mock private PhotoListFilterLogMapper photoListFilterLogMapper;

  @Mock private PhotoViewLogMapper photoViewLogMapper;

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

      ArgumentCaptor<PhotoFavoriteCondition> favoriteConditionCaptor =
          ArgumentCaptor.forClass(PhotoFavoriteCondition.class);
      verify(photoFavoriteMapper, times(2)).delete(favoriteConditionCaptor.capture());
      List<PhotoFavoriteCondition> favoriteConditions = favoriteConditionCaptor.getAllValues();
      assertEquals(1L, favoriteConditions.get(0).getAccountNo());
      assertEquals(1L, favoriteConditions.get(1).getFavoritePhotoAccountNo());

      ArgumentCaptor<PhotoTagMstCondition> tagConditionCaptor =
          ArgumentCaptor.forClass(PhotoTagMstCondition.class);
      verify(photoTagMstMapper).delete(tagConditionCaptor.capture());
      assertEquals(1L, tagConditionCaptor.getValue().getAccountNo());

      ArgumentCaptor<PhotoViewLogCondition> photoViewLogConditionCaptor =
          ArgumentCaptor.forClass(PhotoViewLogCondition.class);
      verify(photoViewLogMapper).delete(photoViewLogConditionCaptor.capture());
      assertEquals(1L, photoViewLogConditionCaptor.getValue().getPhotoAccountNo());

      verify(photoMstMapper).deletePhotosByAccountNo(1L);

      verify(refreshTokenMapper).revokeAllByAccountNo(1L, 1L);

      ArgumentCaptor<LoginHistoryCondition> loginHistoryConditionCaptor =
          ArgumentCaptor.forClass(LoginHistoryCondition.class);
      verify(loginHistoryMapper).delete(loginHistoryConditionCaptor.capture());
      assertEquals(1L, loginHistoryConditionCaptor.getValue().getAccountNo());

      ArgumentCaptor<PhotoListFilterLogCondition> photoListFilterLogConditionCaptor =
          ArgumentCaptor.forClass(PhotoListFilterLogCondition.class);
      verify(photoListFilterLogMapper).delete(photoListFilterLogConditionCaptor.capture());
      assertEquals(1L, photoListFilterLogConditionCaptor.getValue().getPhotoAccountNo());

      ArgumentCaptor<AccountAuthorityCondition> accountAuthorityConditionCaptor =
          ArgumentCaptor.forClass(AccountAuthorityCondition.class);
      verify(accountAuthorityMapper).delete(accountAuthorityConditionCaptor.capture());
      assertEquals(1L, accountAuthorityConditionCaptor.getValue().getAccountNo());

      ArgumentCaptor<AccountCondition> accountConditionCaptor =
          ArgumentCaptor.forClass(AccountCondition.class);
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

    @Test
    @Order(3)
    @DisplayName("正常系：外部キー制約上必要な順序（お気に入り→タグ→閲覧ログ→写真マスタ→トークン→ログイン履歴/絞込ログ→権限→アカウント本体）で削除すること")
    void delete_order() {
      doReturn(List.of()).when(photoMstMapper).deletePhotosByAccountNo(1L);

      Account account = Account.forDelete(new AccountNo(1L));
      accountAggregateRepositoryImpl.delete(account);

      InOrder inOrder =
          inOrder(
              photoFavoriteMapper,
              photoTagMstMapper,
              photoViewLogMapper,
              photoMstMapper,
              refreshTokenMapper,
              loginHistoryMapper,
              photoListFilterLogMapper,
              accountAuthorityMapper,
              accountMapper);

      // お気に入り（自分が登録した分・自分の写真に対する他人の分）を削除
      inOrder.verify(photoFavoriteMapper, times(2)).delete(any(PhotoFavoriteCondition.class));
      // 写真タグを削除
      inOrder.verify(photoTagMstMapper).delete(any(PhotoTagMstCondition.class));
      // 写真詳細閲覧ログを削除（写真マスタの物理削除に先立って実施）
      inOrder.verify(photoViewLogMapper).delete(any(PhotoViewLogCondition.class));
      // 写真マスタを物理削除
      inOrder.verify(photoMstMapper).deletePhotosByAccountNo(1L);
      // リフレッシュトークンを失効
      inOrder.verify(refreshTokenMapper).revokeAllByAccountNo(1L, 1L);
      // ログイン履歴を削除（アカウントの物理削除に先立って実施）
      inOrder.verify(loginHistoryMapper).delete(any(LoginHistoryCondition.class));
      // 写真一覧絞り込みログを削除（アカウントの物理削除に先立って実施）
      inOrder.verify(photoListFilterLogMapper).delete(any(PhotoListFilterLogCondition.class));
      // アカウント権限を削除（アカウントの物理削除に先立って実施）
      inOrder.verify(accountAuthorityMapper).delete(any(AccountAuthorityCondition.class));
      // アカウントを物理削除
      inOrder.verify(accountMapper).delete(any(AccountCondition.class));
    }
  }
}
