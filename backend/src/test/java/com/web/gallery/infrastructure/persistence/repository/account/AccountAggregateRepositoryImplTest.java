package com.web.gallery.infrastructure.persistence.repository.account;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.web.gallery.application.aggregate.Account;
import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.photo.PhotoNo;
import com.web.gallery.infrastructure.persistence.dto.photo.PhotoDeletionDto;
import com.web.gallery.infrastructure.persistence.entity.account.AccountAuthorityCondition;
import com.web.gallery.infrastructure.persistence.entity.account.AccountCondition;
import com.web.gallery.infrastructure.persistence.entity.account.LoginHistoryCondition;
import com.web.gallery.infrastructure.persistence.entity.common.LocationMstCondition;
import com.web.gallery.infrastructure.persistence.entity.inquiry.InquiryMstCondition;
import com.web.gallery.infrastructure.persistence.entity.inquiry.InquiryReplyMstCondition;
import com.web.gallery.infrastructure.persistence.entity.photo.PhotoFavoriteCondition;
import com.web.gallery.infrastructure.persistence.entity.photo.PhotoListFilterLogCondition;
import com.web.gallery.infrastructure.persistence.entity.photo.PhotoTagMstCondition;
import com.web.gallery.infrastructure.persistence.entity.photo.PhotoViewLogCondition;
import com.web.gallery.infrastructure.persistence.entity.photo.PortfolioMstCondition;
import com.web.gallery.infrastructure.persistence.entity.photo.PortfolioPhotoCondition;
import com.web.gallery.infrastructure.persistence.entity.photo.PortfolioTagMstCondition;
import com.web.gallery.infrastructure.persistence.mapper.account.AccountAuthorityMapper;
import com.web.gallery.infrastructure.persistence.mapper.account.AccountMapper;
import com.web.gallery.infrastructure.persistence.mapper.account.LoginHistoryMapper;
import com.web.gallery.infrastructure.persistence.mapper.auth.RefreshTokenMapper;
import com.web.gallery.infrastructure.persistence.mapper.common.LocationMstMapper;
import com.web.gallery.infrastructure.persistence.mapper.inquiry.InquiryMstMapper;
import com.web.gallery.infrastructure.persistence.mapper.inquiry.InquiryReplyMstMapper;
import com.web.gallery.infrastructure.persistence.mapper.photo.PhotoFavoriteMapper;
import com.web.gallery.infrastructure.persistence.mapper.photo.PhotoListFilterLogMapper;
import com.web.gallery.infrastructure.persistence.mapper.photo.PhotoMstMapper;
import com.web.gallery.infrastructure.persistence.mapper.photo.PhotoTagMstMapper;
import com.web.gallery.infrastructure.persistence.mapper.photo.PhotoViewLogMapper;
import com.web.gallery.infrastructure.persistence.mapper.photo.PortfolioMstMapper;
import com.web.gallery.infrastructure.persistence.mapper.photo.PortfolioPhotoMapper;
import com.web.gallery.infrastructure.persistence.mapper.photo.PortfolioTagMstMapper;
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

  @Mock private LocationMstMapper locationMstMapper;

  @Mock private InquiryMstMapper inquiryMstMapper;

  @Mock private InquiryReplyMstMapper inquiryReplyMstMapper;

  @Mock private PortfolioMstMapper portfolioMstMapper;

  @Mock private PortfolioPhotoMapper portfolioPhotoMapper;

  @Mock private PortfolioTagMstMapper portfolioTagMstMapper;

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

      ArgumentCaptor<PortfolioPhotoCondition> portfolioPhotoConditionCaptor =
          ArgumentCaptor.forClass(PortfolioPhotoCondition.class);
      verify(portfolioPhotoMapper).delete(portfolioPhotoConditionCaptor.capture());
      assertEquals(1L, portfolioPhotoConditionCaptor.getValue().getAccountNo());
      assertNull(portfolioPhotoConditionCaptor.getValue().getPortfolioNo());

      ArgumentCaptor<PortfolioTagMstCondition> portfolioTagConditionCaptor =
          ArgumentCaptor.forClass(PortfolioTagMstCondition.class);
      verify(portfolioTagMstMapper).delete(portfolioTagConditionCaptor.capture());
      assertEquals(1L, portfolioTagConditionCaptor.getValue().getAccountNo());
      assertNull(portfolioTagConditionCaptor.getValue().getPortfolioNo());

      // 論理削除済みのポートフォリオも含めて物理削除する（削除フラグを条件に含めない）
      ArgumentCaptor<PortfolioMstCondition> portfolioMstConditionCaptor =
          ArgumentCaptor.forClass(PortfolioMstCondition.class);
      verify(portfolioMstMapper).delete(portfolioMstConditionCaptor.capture());
      assertEquals(1L, portfolioMstConditionCaptor.getValue().getAccountNo());
      assertNull(portfolioMstConditionCaptor.getValue().getPortfolioNo());

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

      // 「閲覧者」として残したログ（外部キーを持たないためFK違反にならず放置されうる）は匿名化する
      verify(photoViewLogMapper).anonymizeViewer(1L);
      verify(photoListFilterLogMapper).anonymizeViewer(1L);

      ArgumentCaptor<LocationMstCondition> locationMstConditionCaptor =
          ArgumentCaptor.forClass(LocationMstCondition.class);
      verify(locationMstMapper).delete(locationMstConditionCaptor.capture());
      assertEquals(1L, locationMstConditionCaptor.getValue().getAccountNo());

      // お問い合わせ返信は「自分のお問い合わせに紐づく分」だけを削除する。
      // 「管理者として投稿した分」は他ユーザーのスレッドから回答が消えるため削除せず、
      // AccountServiceImpl#deleteAccount が削除自体をブロックする
      ArgumentCaptor<InquiryReplyMstCondition> inquiryReplyConditionCaptor =
          ArgumentCaptor.forClass(InquiryReplyMstCondition.class);
      verify(inquiryReplyMstMapper).delete(inquiryReplyConditionCaptor.capture());
      assertEquals(1L, inquiryReplyConditionCaptor.getValue().getInquiryAccountNo());
      assertNull(inquiryReplyConditionCaptor.getValue().getAdminAccountNo());

      ArgumentCaptor<InquiryMstCondition> inquiryMstConditionCaptor =
          ArgumentCaptor.forClass(InquiryMstCondition.class);
      verify(inquiryMstMapper).delete(inquiryMstConditionCaptor.capture());
      assertEquals(1L, inquiryMstConditionCaptor.getValue().getAccountNo());

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
    @DisplayName(
        "正常系：外部キー制約上必要な順序（お気に入り→タグ→ポートフォリオ写真→ポートフォリオタグ→ポートフォリオ→閲覧ログ→写真マスタ→トークン→ログイン履歴/絞込ログ→ロケーション→お問い合わせ返信→お問い合わせ→権限→アカウント本体）で削除すること")
    void delete_order() {
      doReturn(List.of()).when(photoMstMapper).deletePhotosByAccountNo(1L);

      Account account = Account.forDelete(new AccountNo(1L));
      accountAggregateRepositoryImpl.delete(account);

      InOrder inOrder =
          inOrder(
              photoFavoriteMapper,
              photoTagMstMapper,
              portfolioPhotoMapper,
              portfolioTagMstMapper,
              portfolioMstMapper,
              photoViewLogMapper,
              photoMstMapper,
              refreshTokenMapper,
              loginHistoryMapper,
              photoListFilterLogMapper,
              locationMstMapper,
              inquiryReplyMstMapper,
              inquiryMstMapper,
              accountAuthorityMapper,
              accountMapper);

      // お気に入り（自分が登録した分・自分の写真に対する他人の分）を削除
      inOrder.verify(photoFavoriteMapper, times(2)).delete(any(PhotoFavoriteCondition.class));
      // 写真タグを削除
      inOrder.verify(photoTagMstMapper).delete(any(PhotoTagMstCondition.class));
      // ポートフォリオ写真を削除（写真マスタ・ポートフォリオを外部キーで参照するため、それらに先立って実施）
      inOrder.verify(portfolioPhotoMapper).delete(any(PortfolioPhotoCondition.class));
      // ポートフォリオタグを削除（ポートフォリオを外部キーで参照するため、ポートフォリオに先立って実施）
      inOrder.verify(portfolioTagMstMapper).delete(any(PortfolioTagMstCondition.class));
      // ポートフォリオを物理削除（アカウントの物理削除に先立って実施）
      inOrder.verify(portfolioMstMapper).delete(any(PortfolioMstCondition.class));
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
      // ロケーションマスタを削除（アカウントの物理削除に先立って実施）
      inOrder.verify(locationMstMapper).delete(any(LocationMstCondition.class));
      // お問い合わせ返信を削除（お問い合わせ本体・アカウントの物理削除に先立って実施）
      inOrder.verify(inquiryReplyMstMapper).delete(any(InquiryReplyMstCondition.class));
      // お問い合わせ本体を削除（アカウントの物理削除に先立って実施）
      inOrder.verify(inquiryMstMapper).delete(any(InquiryMstCondition.class));
      // アカウント権限を削除（アカウントの物理削除に先立って実施）
      inOrder.verify(accountAuthorityMapper).delete(any(AccountAuthorityCondition.class));
      // アカウントを物理削除
      inOrder.verify(accountMapper).delete(any(AccountCondition.class));
    }
  }
}
