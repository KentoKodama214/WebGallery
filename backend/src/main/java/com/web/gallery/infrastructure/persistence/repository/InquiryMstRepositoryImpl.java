package com.web.gallery.infrastructure.persistence.repository;

import com.web.gallery.application.model.inquiry.InquiryDetailModel;
import com.web.gallery.application.model.inquiry.InquiryGetModel;
import com.web.gallery.application.model.inquiry.InquiryModel;
import com.web.gallery.application.model.inquiry.InquiryModelList;
import com.web.gallery.application.model.inquiry.InquiryPageModel;
import com.web.gallery.application.model.inquiry.InquiryReplyModel;
import com.web.gallery.application.model.inquiry.InquiryReplyModelList;
import com.web.gallery.application.repository.InquiryMstRepository;
import com.web.gallery.domain.enumeration.ErrorEnum;
import com.web.gallery.domain.exception.GalleryException;
import com.web.gallery.domain.model.account.AccountId;
import com.web.gallery.domain.model.account.AccountName;
import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.inquiry.InquiryBody;
import com.web.gallery.domain.model.inquiry.InquiryId;
import com.web.gallery.domain.model.inquiry.InquiryNo;
import com.web.gallery.domain.model.inquiry.InquirySubject;
import com.web.gallery.domain.model.inquiry.ReplyBody;
import com.web.gallery.domain.model.inquiry.ReplyNo;
import com.web.gallery.infrastructure.persistence.dto.InquiryDetailDto;
import com.web.gallery.infrastructure.persistence.dto.InquiryDto;
import com.web.gallery.infrastructure.persistence.entity.inquiry.InquiryMstCondition;
import com.web.gallery.infrastructure.persistence.entity.inquiry.InquiryReplyMst;
import com.web.gallery.infrastructure.persistence.entity.inquiry.InquiryReplyMstCondition;
import com.web.gallery.infrastructure.persistence.mapper.InquiryMstMapper;
import com.web.gallery.infrastructure.persistence.mapper.InquiryReplyMstMapper;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

/** お問い合わせマスタデータを永続化するRepositoryの実装クラス */
@Slf4j
@Repository
@RequiredArgsConstructor
public class InquiryMstRepositoryImpl implements InquiryMstRepository {

  private final InquiryMstMapper inquiryMstMapper;
  private final InquiryReplyMstMapper inquiryReplyMstMapper;

  /**
   * アカウント番号から新しいお問い合わせ番号を発番する
   *
   * @param accountNo アカウント番号
   * @return 新規採番したお問い合わせ番号
   */
  @Override
  public InquiryNo getNewInquiryNo(AccountNo accountNo) {
    Long maxInquiryNo = inquiryMstMapper.getMaxInquiryNo(accountNo.value());
    return InquiryNo.next(maxInquiryNo);
  }

  /**
   * 自分のお問い合わせ一覧を、ページング情報に従い取得する
   *
   * <p>最後のページかどうかを判定するため、DBからは1ページあたりの表示件数より1件多く取得し、 実際に返す件数が上限を超えていた場合は表示件数分のみに切り詰める
   *
   * @param inquiryGetModel {@link InquiryGetModel}
   * @return {@link InquiryPageModel}
   */
  @Override
  public InquiryPageModel getInquiryList(InquiryGetModel inquiryGetModel) {
    List<InquiryDto> inquiryDtoList =
        inquiryMstMapper.selectList(InquiryMstCondition.forList(inquiryGetModel));
    return toPageModel(inquiryDtoList, inquiryGetModel.getLimit());
  }

  /**
   * お問い合わせ一覧を、ページング情報に従い取得する（管理者用、全アカウントが対象）
   *
   * @param inquiryGetModel {@link InquiryGetModel}
   * @return {@link InquiryPageModel}
   */
  @Override
  public InquiryPageModel getInquiryListForAdmin(InquiryGetModel inquiryGetModel) {
    List<InquiryDto> inquiryDtoList =
        inquiryMstMapper.selectListForAdmin(InquiryMstCondition.forAdminList(inquiryGetModel));
    return toPageModel(inquiryDtoList, inquiryGetModel.getLimit());
  }

  private InquiryPageModel toPageModel(List<InquiryDto> inquiryDtoList, Integer limit) {
    Boolean isLast = inquiryDtoList.size() < limit;
    List<InquiryDto> pageDtoList = isLast ? inquiryDtoList : inquiryDtoList.subList(0, limit - 1);
    return InquiryPageModel.of(
        InquiryModelList.of(pageDtoList.stream().map(this::toInquiryModel).toList()), isLast);
  }

  /**
   * InquiryDtoからInquiryModelを組み立てる
   *
   * @param dto {@link InquiryDto}
   * @return {@link InquiryModel}
   */
  private InquiryModel toInquiryModel(InquiryDto dto) {
    return InquiryModel.builder()
        .inquiryId(new InquiryId(dto.getId()))
        .accountNo(new AccountNo(dto.getAccountNo()))
        .accountId(dto.getAccountId() != null ? new AccountId(dto.getAccountId()) : null)
        .accountName(dto.getAccountName() != null ? new AccountName(dto.getAccountName()) : null)
        .inquiryNo(new InquiryNo(dto.getInquiryNo()))
        .subject(new InquirySubject(dto.getSubject()))
        .statusKbn(dto.getStatusKbn())
        .isReadByUser(dto.getIsReadByUser())
        .createdAt(dto.getCreatedAt())
        .build();
  }

  /**
   * 自分のお問い合わせの詳細情報（返信を含む）を取得する
   *
   * @param accountNo アカウント番号
   * @param inquiryNo お問い合わせ番号
   * @return {@link InquiryDetailModel}
   * @throws GalleryException お問い合わせが存在しなかった場合
   */
  @Override
  public InquiryDetailModel getInquiryDetail(AccountNo accountNo, InquiryNo inquiryNo)
      throws GalleryException {
    InquiryDetailDto dto =
        inquiryMstMapper.selectDetail(
            InquiryMstCondition.byAccountAndInquiryNo(accountNo.value(), inquiryNo.value()));
    return toDetailModel(dto, accountNo, inquiryNo);
  }

  /**
   * お問い合わせの詳細情報（返信を含む）を取得する（管理者用）
   *
   * @param inquiryId ID
   * @return {@link InquiryDetailModel}
   * @throws GalleryException お問い合わせが存在しなかった場合
   */
  @Override
  public InquiryDetailModel getInquiryDetailForAdmin(InquiryId inquiryId) throws GalleryException {
    InquiryDetailDto dto =
        inquiryMstMapper.selectDetailForAdmin(InquiryMstCondition.byId(inquiryId.value()));
    return toDetailModel(dto, null, null);
  }

  private InquiryDetailModel toDetailModel(
      InquiryDetailDto dto, AccountNo accountNo, InquiryNo inquiryNo) throws GalleryException {
    if (Objects.isNull(dto)) {
      log.warn("Inquiry not found. (AccountNo: {}, InquiryNo: {})", accountNo, inquiryNo);
      throw ErrorEnum.INQUIRY_NOT_FOUND.toException();
    }

    List<InquiryReplyMst> replyMstList =
        inquiryReplyMstMapper.selectList(InquiryReplyMstCondition.byInquiryId(dto.getId()));
    return InquiryDetailModel.builder()
        .accountNo(new AccountNo(dto.getAccountNo()))
        .accountId(dto.getAccountId() != null ? new AccountId(dto.getAccountId()) : null)
        .accountName(dto.getAccountName() != null ? new AccountName(dto.getAccountName()) : null)
        .inquiryId(new InquiryId(dto.getId()))
        .inquiryNo(new InquiryNo(dto.getInquiryNo()))
        .subject(new InquirySubject(dto.getSubject()))
        .body(new InquiryBody(dto.getBody()))
        .statusKbn(dto.getStatusKbn())
        .isReadByUser(dto.getIsReadByUser())
        .createdAt(dto.getCreatedAt())
        .replyModelList(
            InquiryReplyModelList.of(replyMstList.stream().map(this::toInquiryReplyModel).toList()))
        .build();
  }

  /**
   * InquiryReplyMstエンティティからInquiryReplyModelを組み立てる
   *
   * @param entity {@link InquiryReplyMst}
   * @return {@link InquiryReplyModel}
   */
  private InquiryReplyModel toInquiryReplyModel(InquiryReplyMst entity) {
    return InquiryReplyModel.builder()
        .inquiryId(new InquiryId(entity.getInquiryId()))
        .replyNo(new ReplyNo(entity.getReplyNo()))
        .adminAccountNo(new AccountNo(entity.getAdminAccountNo()))
        .body(new ReplyBody(entity.getBody()))
        .createdAt(entity.getCreatedAt())
        .build();
  }
}
