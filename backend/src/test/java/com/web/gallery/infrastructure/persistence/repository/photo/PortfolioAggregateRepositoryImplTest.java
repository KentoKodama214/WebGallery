package com.web.gallery.infrastructure.persistence.repository.photo;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.web.gallery.application.aggregate.Portfolio;
import com.web.gallery.application.model.photo.PortfolioDetailModel;
import com.web.gallery.application.model.photo.PortfolioNoList;
import com.web.gallery.application.model.photo.PortfolioPhotoModel;
import com.web.gallery.application.model.photo.PortfolioPhotoModelList;
import com.web.gallery.application.model.photo.PortfolioTagModel;
import com.web.gallery.application.model.photo.PortfolioTagModelList;
import com.web.gallery.domain.enumeration.PortfolioStatusEnum;
import com.web.gallery.domain.enumeration.PortfolioTemplateEnum;
import com.web.gallery.domain.exception.PortfolioNotFoundException;
import com.web.gallery.domain.exception.RegistFailureException;
import com.web.gallery.domain.exception.UpdateFailureException;
import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.photo.PhotoNo;
import com.web.gallery.domain.model.photo.PortfolioDescription;
import com.web.gallery.domain.model.photo.PortfolioNo;
import com.web.gallery.domain.model.photo.PortfolioTheme;
import com.web.gallery.domain.model.photo.TagEnglishName;
import com.web.gallery.domain.model.photo.TagJapaneseName;
import com.web.gallery.domain.model.photo.TemplateSettings;
import com.web.gallery.infrastructure.persistence.entity.photo.PortfolioMst;
import com.web.gallery.infrastructure.persistence.entity.photo.PortfolioMstCondition;
import com.web.gallery.infrastructure.persistence.entity.photo.PortfolioMstUpdateTarget;
import com.web.gallery.infrastructure.persistence.entity.photo.PortfolioPhoto;
import com.web.gallery.infrastructure.persistence.entity.photo.PortfolioPhotoCondition;
import com.web.gallery.infrastructure.persistence.entity.photo.PortfolioTagMst;
import com.web.gallery.infrastructure.persistence.entity.photo.PortfolioTagMstCondition;
import com.web.gallery.infrastructure.persistence.mapper.photo.PortfolioMstMapper;
import com.web.gallery.infrastructure.persistence.mapper.photo.PortfolioPhotoMapper;
import com.web.gallery.infrastructure.persistence.mapper.photo.PortfolioTagMstMapper;
import java.util.List;
import java.util.Map;
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
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class PortfolioAggregateRepositoryImplTest {

  @InjectMocks private PortfolioAggregateRepositoryImpl portfolioAggregateRepositoryImpl;

  @Mock private PortfolioMstMapper portfolioMstMapper;

  @Mock private PortfolioPhotoMapper portfolioPhotoMapper;

  @Mock private PortfolioTagMstMapper portfolioTagMstMapper;

  /**
   * 写真2枚・タグ1件を持つ新規登録用のPortfolioを生成する
   *
   * @return {@link Portfolio}
   */
  private static Portfolio portfolioWithChildren() {
    return Portfolio.forRegist(
        PortfolioDetailModel.builder()
            .accountNo(new AccountNo(1L))
            .theme(new PortfolioTheme("日本の自然"))
            .description(new PortfolioDescription("説明文"))
            .templateKbn(PortfolioTemplateEnum.GRID)
            .templateSettings(
                new TemplateSettings(Map.of("rows", List.of(Map.of("pattern", "TWO_EQUAL")))))
            .portfolioPhotoModelList(
                PortfolioPhotoModelList.of(
                    List.of(
                        PortfolioPhotoModel.builder()
                            .photoNo(new PhotoNo(20L))
                            .photoSettings(new TemplateSettings(Map.of("caption", "金閣寺")))
                            .build(),
                        PortfolioPhotoModel.builder()
                            .photoNo(new PhotoNo(10L))
                            .photoSettings(TemplateSettings.empty())
                            .build())))
            .portfolioTagModelList(
                PortfolioTagModelList.of(
                    List.of(
                        PortfolioTagModel.builder()
                            .tagJapaneseName(new TagJapaneseName("自然"))
                            .tagEnglishName(new TagEnglishName("nature"))
                            .build())))
            .build(),
        new PortfolioNo(2L));
  }

  /**
   * 写真・タグを持たない新規登録用のPortfolioを生成する
   *
   * @return {@link Portfolio}
   */
  private static Portfolio portfolioWithoutChildren() {
    return Portfolio.forRegist(
        PortfolioDetailModel.builder()
            .accountNo(new AccountNo(1L))
            .theme(new PortfolioTheme("日本の自然"))
            .description(new PortfolioDescription(""))
            .templateKbn(PortfolioTemplateEnum.FEATURE)
            .templateSettings(TemplateSettings.empty())
            .portfolioPhotoModelList(PortfolioPhotoModelList.empty())
            .portfolioTagModelList(PortfolioTagModelList.empty())
            .build(),
        new PortfolioNo(2L));
  }

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class regist {
    @Test
    @Order(1)
    @DisplayName("正常系：ポートフォリオ本体を登録してから、写真・タグを表示順・タグ番号付きで一括登録すること")
    @SuppressWarnings("unchecked")
    void regist_success() throws Exception {
      doReturn(1).when(portfolioMstMapper).insert(any(PortfolioMst.class));

      portfolioAggregateRepositoryImpl.regist(portfolioWithChildren());

      ArgumentCaptor<PortfolioMst> mstCaptor = ArgumentCaptor.forClass(PortfolioMst.class);
      ArgumentCaptor<List<PortfolioPhoto>> photoCaptor = ArgumentCaptor.forClass(List.class);
      ArgumentCaptor<List<PortfolioTagMst>> tagCaptor = ArgumentCaptor.forClass(List.class);
      // 写真・タグは外部キーでポートフォリオ本体を参照するため、本体を先に登録する
      InOrder inOrder = inOrder(portfolioMstMapper, portfolioPhotoMapper, portfolioTagMstMapper);
      inOrder.verify(portfolioMstMapper).insert(mstCaptor.capture());
      inOrder.verify(portfolioPhotoMapper).insertBulk(photoCaptor.capture());
      inOrder.verify(portfolioTagMstMapper).insertBulk(tagCaptor.capture());

      PortfolioMst mst = mstCaptor.getValue();
      assertEquals(1L, mst.getAccountNo());
      assertEquals(2L, mst.getPortfolioNo());
      assertEquals(1L, mst.getCreatedBy());
      assertEquals(1L, mst.getUpdatedBy());
      assertEquals("日本の自然", mst.getTheme());
      assertEquals("説明文", mst.getDescription());
      assertEquals(PortfolioTemplateEnum.GRID, mst.getTemplateKbn());
      assertEquals(
          Map.of("rows", List.of(Map.of("pattern", "TWO_EQUAL"))), mst.getTemplateSettings());
      assertEquals(1, mst.getSettingsVersion());
      assertEquals(PortfolioStatusEnum.DRAFT, mst.getStatusKbn());

      List<PortfolioPhoto> photos = photoCaptor.getValue();
      assertEquals(2, photos.size());
      assertEquals(1L, photos.get(0).getAccountNo());
      assertEquals(2L, photos.get(0).getPortfolioNo());
      assertEquals(20L, photos.get(0).getPhotoNo());
      assertEquals(1L, photos.get(0).getCreatedBy());
      assertEquals(1, photos.get(0).getDisplayOrder());
      assertEquals(Map.of("caption", "金閣寺"), photos.get(0).getPhotoSettings());
      assertEquals(10L, photos.get(1).getPhotoNo());
      assertEquals(2, photos.get(1).getDisplayOrder());
      assertEquals(Map.of(), photos.get(1).getPhotoSettings());

      List<PortfolioTagMst> tags = tagCaptor.getValue();
      assertEquals(1, tags.size());
      assertEquals(1L, tags.get(0).getAccountNo());
      assertEquals(2L, tags.get(0).getPortfolioNo());
      assertEquals(1L, tags.get(0).getTagNo());
      assertEquals(1L, tags.get(0).getCreatedBy());
      assertEquals("自然", tags.get(0).getTagJapaneseName());
      assertEquals("nature", tags.get(0).getTagEnglishName());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：写真・タグが0件の場合、一括登録を呼び出さないこと（空のVALUES句によるSQL構文エラーを避ける）")
    void regist_noChildren() throws Exception {
      doReturn(1).when(portfolioMstMapper).insert(any(PortfolioMst.class));

      portfolioAggregateRepositoryImpl.regist(portfolioWithoutChildren());

      verify(portfolioPhotoMapper, never()).insertBulk(anyList());
      verify(portfolioTagMstMapper, never()).insertBulk(anyList());
    }

    @Test
    @Order(3)
    @DisplayName("異常系：ポートフォリオ本体の登録件数が0件の場合、RegistFailureExceptionをthrowし、写真・タグを登録しないこと")
    void regist_insertFailed() {
      doReturn(0).when(portfolioMstMapper).insert(any(PortfolioMst.class));

      assertThrows(
          RegistFailureException.class,
          () -> portfolioAggregateRepositoryImpl.regist(portfolioWithChildren()));
      verify(portfolioPhotoMapper, never()).insertBulk(anyList());
      verify(portfolioTagMstMapper, never()).insertBulk(anyList());
    }

    @Test
    @Order(4)
    @DisplayName("異常系：ポートフォリオ番号が衝突しDuplicateKeyExceptionが発生した場合、RegistFailureExceptionをthrowすること")
    void regist_duplicateKey() {
      doThrow(new DuplicateKeyException("duplicate"))
          .when(portfolioMstMapper)
          .insert(any(PortfolioMst.class));

      assertThrows(
          RegistFailureException.class,
          () -> portfolioAggregateRepositoryImpl.regist(portfolioWithChildren()));
      verify(portfolioPhotoMapper, never()).insertBulk(anyList());
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class update {
    @Test
    @Order(1)
    @DisplayName("正常系：ポートフォリオ本体を更新し、写真・タグを全件削除してから登録し直すこと")
    void update_success() throws Exception {
      doReturn(1)
          .when(portfolioMstMapper)
          .update(any(PortfolioMstCondition.class), any(PortfolioMstUpdateTarget.class));

      portfolioAggregateRepositoryImpl.update(portfolioWithChildren());

      ArgumentCaptor<PortfolioMstCondition> conditionCaptor =
          ArgumentCaptor.forClass(PortfolioMstCondition.class);
      ArgumentCaptor<PortfolioMstUpdateTarget> targetCaptor =
          ArgumentCaptor.forClass(PortfolioMstUpdateTarget.class);
      ArgumentCaptor<PortfolioPhotoCondition> photoConditionCaptor =
          ArgumentCaptor.forClass(PortfolioPhotoCondition.class);
      ArgumentCaptor<PortfolioTagMstCondition> tagConditionCaptor =
          ArgumentCaptor.forClass(PortfolioTagMstCondition.class);
      InOrder inOrder = inOrder(portfolioMstMapper, portfolioPhotoMapper, portfolioTagMstMapper);
      inOrder.verify(portfolioMstMapper).update(conditionCaptor.capture(), targetCaptor.capture());
      // 表示順・タグ番号を振り直すため、全件削除してから登録し直す
      inOrder.verify(portfolioPhotoMapper).delete(photoConditionCaptor.capture());
      inOrder.verify(portfolioTagMstMapper).delete(tagConditionCaptor.capture());
      inOrder.verify(portfolioPhotoMapper).insertBulk(anyList());
      inOrder.verify(portfolioTagMstMapper).insertBulk(anyList());

      assertEquals(1L, conditionCaptor.getValue().getAccountNo());
      assertEquals(2L, conditionCaptor.getValue().getPortfolioNo());
      PortfolioMstUpdateTarget target = targetCaptor.getValue();
      assertEquals(1L, target.getUpdatedBy());
      assertEquals("日本の自然", target.getTheme());
      assertEquals("説明文", target.getDescription());
      assertEquals(PortfolioTemplateEnum.GRID, target.getTemplateKbn());
      assertEquals(
          Map.of("rows", List.of(Map.of("pattern", "TWO_EQUAL"))), target.getTemplateSettings());
      assertEquals(1, target.getSettingsVersion());
      assertEquals(PortfolioStatusEnum.DRAFT, target.getStatusKbn());
      assertEquals(1L, photoConditionCaptor.getValue().getAccountNo());
      assertEquals(2L, photoConditionCaptor.getValue().getPortfolioNo());
      assertEquals(1L, tagConditionCaptor.getValue().getAccountNo());
      assertEquals(2L, tagConditionCaptor.getValue().getPortfolioNo());
    }

    @Test
    @Order(2)
    @DisplayName("異常系：ポートフォリオ本体の更新件数が0件の場合、UpdateFailureExceptionをthrowし、写真・タグを変更しないこと")
    void update_updateFailed() {
      doReturn(0)
          .when(portfolioMstMapper)
          .update(any(PortfolioMstCondition.class), any(PortfolioMstUpdateTarget.class));

      assertThrows(
          UpdateFailureException.class,
          () -> portfolioAggregateRepositoryImpl.update(portfolioWithChildren()));
      verify(portfolioPhotoMapper, never()).delete(any(PortfolioPhotoCondition.class));
      verify(portfolioPhotoMapper, never()).insertBulk(anyList());
      verify(portfolioTagMstMapper, never()).delete(any(PortfolioTagMstCondition.class));
    }

    @Test
    @Order(3)
    @DisplayName("異常系：写真の登録し直しでDuplicateKeyExceptionが発生した場合、UpdateFailureExceptionをthrowすること")
    void update_duplicateKey() {
      doReturn(1)
          .when(portfolioMstMapper)
          .update(any(PortfolioMstCondition.class), any(PortfolioMstUpdateTarget.class));
      doThrow(new DuplicateKeyException("duplicate"))
          .when(portfolioPhotoMapper)
          .insertBulk(anyList());

      assertThrows(
          UpdateFailureException.class,
          () -> portfolioAggregateRepositoryImpl.update(portfolioWithChildren()));
    }
  }

  @Nested
  @Order(3)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class updateStatus {
    @Test
    @Order(1)
    @DisplayName("正常系：公開状態・更新者のみを更新すること")
    void updateStatus_success() throws Exception {
      doReturn(1)
          .when(portfolioMstMapper)
          .update(any(PortfolioMstCondition.class), any(PortfolioMstUpdateTarget.class));
      Portfolio portfolio = portfolioWithChildren();
      portfolio.publish();

      portfolioAggregateRepositoryImpl.updateStatus(portfolio);

      ArgumentCaptor<PortfolioMstCondition> conditionCaptor =
          ArgumentCaptor.forClass(PortfolioMstCondition.class);
      ArgumentCaptor<PortfolioMstUpdateTarget> targetCaptor =
          ArgumentCaptor.forClass(PortfolioMstUpdateTarget.class);
      verify(portfolioMstMapper).update(conditionCaptor.capture(), targetCaptor.capture());
      assertEquals(1L, conditionCaptor.getValue().getAccountNo());
      assertEquals(2L, conditionCaptor.getValue().getPortfolioNo());
      assertEquals(1L, targetCaptor.getValue().getUpdatedBy());
      assertEquals(PortfolioStatusEnum.PUBLISHED, targetCaptor.getValue().getStatusKbn());
      // 公開状態の変更では内容を変更しないため、他の項目は「変更なし」としてnullで渡す
      assertNull(targetCaptor.getValue().getTheme());
      assertNull(targetCaptor.getValue().getTemplateSettings());
      verify(portfolioPhotoMapper, never()).insertBulk(anyList());
    }

    @Test
    @Order(2)
    @DisplayName("異常系：更新件数が0件の場合、UpdateFailureExceptionをthrowすること")
    void updateStatus_updateFailed() {
      doReturn(0)
          .when(portfolioMstMapper)
          .update(any(PortfolioMstCondition.class), any(PortfolioMstUpdateTarget.class));

      assertThrows(
          UpdateFailureException.class,
          () -> portfolioAggregateRepositoryImpl.updateStatus(portfolioWithChildren()));
    }
  }

  @Nested
  @Order(4)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class delete {
    @Test
    @Order(1)
    @DisplayName("正常系：写真・タグを物理削除したうえで、ポートフォリオ本体を物理削除すること")
    void delete_success() throws Exception {
      doReturn(1).when(portfolioMstMapper).delete(any(PortfolioMstCondition.class));

      portfolioAggregateRepositoryImpl.delete(
          Portfolio.forDelete(new AccountNo(1L), new PortfolioNo(2L)));

      ArgumentCaptor<PortfolioMstCondition> conditionCaptor =
          ArgumentCaptor.forClass(PortfolioMstCondition.class);
      ArgumentCaptor<PortfolioPhotoCondition> photoConditionCaptor =
          ArgumentCaptor.forClass(PortfolioPhotoCondition.class);
      ArgumentCaptor<PortfolioTagMstCondition> tagConditionCaptor =
          ArgumentCaptor.forClass(PortfolioTagMstCondition.class);
      // 写真・タグは外部キーでポートフォリオ本体を参照するため、先に削除する
      InOrder inOrder = inOrder(portfolioPhotoMapper, portfolioTagMstMapper, portfolioMstMapper);
      inOrder.verify(portfolioPhotoMapper).delete(photoConditionCaptor.capture());
      inOrder.verify(portfolioTagMstMapper).delete(tagConditionCaptor.capture());
      inOrder.verify(portfolioMstMapper).delete(conditionCaptor.capture());
      verify(portfolioMstMapper, never())
          .update(any(PortfolioMstCondition.class), any(PortfolioMstUpdateTarget.class));

      assertEquals(1L, conditionCaptor.getValue().getAccountNo());
      assertEquals(2L, conditionCaptor.getValue().getPortfolioNo());
      assertEquals(1L, photoConditionCaptor.getValue().getAccountNo());
      assertEquals(2L, photoConditionCaptor.getValue().getPortfolioNo());
      assertEquals(1L, tagConditionCaptor.getValue().getAccountNo());
      assertEquals(2L, tagConditionCaptor.getValue().getPortfolioNo());
    }

    @Test
    @Order(2)
    @DisplayName("異常系：対象が存在しない（未登録・削除済み）場合、PortfolioNotFoundExceptionをthrowすること")
    void delete_notFound() {
      doReturn(0).when(portfolioMstMapper).delete(any(PortfolioMstCondition.class));

      assertThrows(
          PortfolioNotFoundException.class,
          () ->
              portfolioAggregateRepositoryImpl.delete(
                  Portfolio.forDelete(new AccountNo(1L), new PortfolioNo(2L))));
    }
  }

  @Nested
  @Order(5)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class updateDisplayOrder {
    @Test
    @Order(1)
    @DisplayName("正常系：ポートフォリオ番号を並び順のままLongのリストに変換して表示順を更新すること")
    void updateDisplayOrder_success() throws Exception {
      doReturn(3).when(portfolioMstMapper).updateDisplayOrder(1L, List.of(3L, 1L, 2L));

      portfolioAggregateRepositoryImpl.updateDisplayOrder(
          new AccountNo(1L), PortfolioNoList.from(List.of(3L, 1L, 2L)));

      verify(portfolioMstMapper).updateDisplayOrder(1L, List.of(3L, 1L, 2L));
    }

    @Test
    @Order(2)
    @DisplayName("正常系：ポートフォリオ番号が空の場合、更新しないこと（空のVALUES句によるSQL構文エラーを避ける）")
    void updateDisplayOrder_empty() throws Exception {
      portfolioAggregateRepositoryImpl.updateDisplayOrder(
          new AccountNo(1L), PortfolioNoList.empty());

      verify(portfolioMstMapper, never()).updateDisplayOrder(anyLong(), anyList());
    }

    @Test
    @Order(3)
    @DisplayName("異常系：更新件数が指定した件数と一致しない場合、UpdateFailureExceptionをthrowすること")
    void updateDisplayOrder_updateFailed() {
      doReturn(2).when(portfolioMstMapper).updateDisplayOrder(1L, List.of(3L, 1L, 2L));

      assertThrows(
          UpdateFailureException.class,
          () ->
              portfolioAggregateRepositoryImpl.updateDisplayOrder(
                  new AccountNo(1L), PortfolioNoList.from(List.of(3L, 1L, 2L))));
    }
  }
}
