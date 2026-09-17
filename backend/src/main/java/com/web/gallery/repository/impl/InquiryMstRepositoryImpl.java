package com.web.gallery.repository.impl;

import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.inquiry.InquiryId;
import com.web.gallery.domain.inquiry.InquiryNo;
import com.web.gallery.dto.InquiryDetailDto;
import com.web.gallery.dto.InquiryDto;
import com.web.gallery.entity.InquiryMstCondition;
import com.web.gallery.entity.InquiryReplyMst;
import com.web.gallery.entity.InquiryReplyMstCondition;
import com.web.gallery.enumeration.ErrorEnum;
import com.web.gallery.exception.GalleryException;
import com.web.gallery.mapper.InquiryMstMapper;
import com.web.gallery.mapper.InquiryReplyMstMapper;
import com.web.gallery.model.InquiryDetailModel;
import com.web.gallery.model.InquiryGetModel;
import com.web.gallery.model.InquiryModelList;
import com.web.gallery.model.InquiryPageModel;
import com.web.gallery.repository.InquiryMstRepository;
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
    return InquiryPageModel.of(InquiryModelList.from(pageDtoList), isLast);
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
    return InquiryDetailModel.from(dto, replyMstList);
  }
}
