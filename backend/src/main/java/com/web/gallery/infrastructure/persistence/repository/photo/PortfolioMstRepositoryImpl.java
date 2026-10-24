package com.web.gallery.infrastructure.persistence.repository.photo;

import com.web.gallery.application.model.photo.PhotoNoList;
import com.web.gallery.application.model.photo.PortfolioDetailModel;
import com.web.gallery.application.model.photo.PortfolioDetailModelList;
import com.web.gallery.application.model.photo.PortfolioListGetModel;
import com.web.gallery.application.model.photo.PortfolioModel;
import com.web.gallery.application.model.photo.PortfolioModelList;
import com.web.gallery.application.model.photo.PortfolioPhotoModel;
import com.web.gallery.application.model.photo.PortfolioPhotoModelList;
import com.web.gallery.application.model.photo.PortfolioPhotoPageModel;
import com.web.gallery.application.model.photo.PortfolioPhotoSearchModel;
import com.web.gallery.application.model.photo.PortfolioTagModel;
import com.web.gallery.application.model.photo.PortfolioTagModelList;
import com.web.gallery.application.repository.photo.PortfolioMstRepository;
import com.web.gallery.domain.enumeration.ErrorEnum;
import com.web.gallery.domain.exception.GalleryException;
import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.photo.DisplayOrder;
import com.web.gallery.domain.model.photo.ImageFilePath;
import com.web.gallery.domain.model.photo.PhotoCount;
import com.web.gallery.domain.model.photo.PhotoJapaneseTitle;
import com.web.gallery.domain.model.photo.PhotoNo;
import com.web.gallery.domain.model.photo.PortfolioDescription;
import com.web.gallery.domain.model.photo.PortfolioNo;
import com.web.gallery.domain.model.photo.PortfolioTheme;
import com.web.gallery.domain.model.photo.TagEnglishName;
import com.web.gallery.domain.model.photo.TagJapaneseName;
import com.web.gallery.domain.model.photo.TagNo;
import com.web.gallery.domain.model.photo.TemplateSettings;
import com.web.gallery.infrastructure.persistence.dto.photo.PortfolioDto;
import com.web.gallery.infrastructure.persistence.dto.photo.PortfolioPhotoDto;
import com.web.gallery.infrastructure.persistence.entity.photo.PortfolioMst;
import com.web.gallery.infrastructure.persistence.entity.photo.PortfolioMstCondition;
import com.web.gallery.infrastructure.persistence.entity.photo.PortfolioPhotoCondition;
import com.web.gallery.infrastructure.persistence.entity.photo.PortfolioTagMst;
import com.web.gallery.infrastructure.persistence.entity.photo.PortfolioTagMstCondition;
import com.web.gallery.infrastructure.persistence.mapper.photo.PortfolioMstMapper;
import com.web.gallery.infrastructure.persistence.mapper.photo.PortfolioPhotoMapper;
import com.web.gallery.infrastructure.persistence.mapper.photo.PortfolioTagMstMapper;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

/** ポートフォリオマスタデータ（写真・タグを含む）を参照するRepositoryの実装クラス */
@Slf4j
@Repository
@RequiredArgsConstructor
public class PortfolioMstRepositoryImpl implements PortfolioMstRepository {

  private final PortfolioMstMapper portfolioMstMapper;
  private final PortfolioPhotoMapper portfolioPhotoMapper;
  private final PortfolioTagMstMapper portfolioTagMstMapper;

  /**
   * アカウント番号から新しいポートフォリオ番号を発番する
   *
   * <p>登録済みのポートフォリオ番号の最大値の次の番号とする。ポートフォリオは物理削除するため、 最大の番号のポートフォリオを削除した後に登録すると、その番号が再び使われる
   *
   * @param accountNo アカウント番号
   * @return 新規採番したポートフォリオ番号
   */
  @Override
  public PortfolioNo getNewPortfolioNo(AccountNo accountNo) {
    return PortfolioNo.next(portfolioMstMapper.getMaxPortfolioNo(accountNo.value()));
  }

  /**
   * アカウントが作成済みのポートフォリオの件数（下書きを含む）を取得する
   *
   * @param accountNo アカウント番号
   * @return 作成済みの件数
   */
  @Override
  public Integer count(AccountNo accountNo) {
    return portfolioMstMapper.count(PortfolioMstCondition.byAccountNo(accountNo.value()));
  }

  /**
   * 本人のポートフォリオ一覧（下書きを含む）を、テーマ・タグ名の部分一致で絞り込み、更新日時の新しい順に取得する
   *
   * @param portfolioListGetModel {@link PortfolioListGetModel}（絞り込み条件は未指定可）
   * @return {@link PortfolioModelList}
   */
  @Override
  public PortfolioModelList getPortfolioList(PortfolioListGetModel portfolioListGetModel) {
    List<PortfolioDto> dtoList =
        portfolioMstMapper.selectList(
            PortfolioMstCondition.byAccountNoAndKeywords(
                portfolioListGetModel.getAccountNo().value(),
                toContainsPattern(portfolioListGetModel.getTheme()),
                toContainsPattern(portfolioListGetModel.getTag())));
    return PortfolioModelList.of(dtoList.stream().map(this::toPortfolioModel).toList());
  }

  /**
   * 本人の未削除の写真を、検索語がタイトル・撮影場所の名前・撮影場所の住所・タグ名のいずれかに部分一致するもので絞り込み、撮影日時の新しい順に1ページ分取得する
   *
   * <p>次のページの有無を判定するため、1ページの件数より1件多く取得する
   *
   * @param portfolioPhotoSearchModel {@link PortfolioPhotoSearchModel}（1ページあたりの件数を設定済み）
   * @return {@link PortfolioPhotoPageModel}（画像ファイルパスはオブジェクトキーのまま）
   */
  @Override
  public PortfolioPhotoPageModel searchAvailablePhotos(
      PortfolioPhotoSearchModel portfolioPhotoSearchModel) {
    Integer pageSize = portfolioPhotoSearchModel.getPageSize();
    List<PortfolioPhotoDto> dtoList =
        portfolioPhotoMapper.selectAvailablePhotos(
            portfolioPhotoSearchModel.getAccountNo().value(),
            toContainsPattern(portfolioPhotoSearchModel.getKeyword()),
            pageSize + 1,
            (portfolioPhotoSearchModel.getPageNo() - 1) * pageSize);
    Boolean isLast = dtoList.size() <= pageSize;
    return PortfolioPhotoPageModel.of(
        PortfolioPhotoModelList.of(
            dtoList.stream().limit(pageSize).map(this::toAvailablePhotoModel).toList()),
        isLast);
  }

  /**
   * 検索語を、部分一致のLIKEパターンに変換する
   *
   * <p>検索語に含まれるLIKEのワイルドカード文字（{@code %}・{@code _}）とエスケープ文字（{@code \}）は、 文字そのものとして一致させるためエスケープする
   *
   * @param keyword 検索語（null許容）
   * @return LIKEパターン。検索語がnullの場合はnull
   */
  private String toContainsPattern(String keyword) {
    if (Objects.isNull(keyword)) {
      return null;
    }
    String escaped = keyword.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    return "%" + escaped + "%";
  }

  /**
   * ポートフォリオに設定する写真の検索結果をPortfolioPhotoModelに変換する
   *
   * @param dto 写真番号・タイトル・画像ファイルパス・向きのみを持つ{@link PortfolioPhotoDto}
   * @return {@link PortfolioPhotoModel}（写真単位の設定は空）
   */
  private PortfolioPhotoModel toAvailablePhotoModel(PortfolioPhotoDto dto) {
    return PortfolioPhotoModel.builder()
        .photoNo(new PhotoNo(dto.getPhotoNo()))
        .photoSettings(TemplateSettings.empty())
        .photoJapaneseTitle(new PhotoJapaneseTitle(dto.getPhotoJapaneseTitle()))
        .imageFilePath(new ImageFilePath(dto.getImageFilePath()))
        .directionKbn(dto.getDirectionKbn())
        .build();
  }

  /**
   * 本人のポートフォリオの詳細情報（写真・タグを含む）を取得する
   *
   * @param accountNo アカウント番号
   * @param portfolioNo ポートフォリオ番号
   * @return {@link PortfolioDetailModel}
   * @throws GalleryException ポートフォリオが存在しない（未登録・削除済み）場合
   */
  @Override
  public PortfolioDetailModel getPortfolioDetail(AccountNo accountNo, PortfolioNo portfolioNo)
      throws GalleryException {
    List<PortfolioMst> portfolioMstList =
        portfolioMstMapper.select(
            PortfolioMstCondition.byAccountAndPortfolio(accountNo.value(), portfolioNo.value()));
    if (portfolioMstList.isEmpty()) {
      log.info(
          "PortfolioMst: Not Found (AccountNo: {}, PortfolioNo: {})",
          accountNo.value(),
          portfolioNo.value());
      throw ErrorEnum.PORTFOLIO_NOT_FOUND.toException();
    }
    return toPortfolioDetailModel(portfolioMstList.get(0));
  }

  /**
   * 指定した写真を使用している未削除のポートフォリオの詳細情報（写真・タグを含む）を取得する
   *
   * @param accountNo アカウント番号
   * @param photoNo 写真番号
   * @return {@link PortfolioDetailModelList}
   */
  @Override
  public PortfolioDetailModelList getPortfolioDetailListByPhoto(
      AccountNo accountNo, PhotoNo photoNo) {
    List<PortfolioMst> portfolioMstList =
        portfolioMstMapper.selectByPhoto(accountNo.value(), photoNo.value());
    return PortfolioDetailModelList.of(
        portfolioMstList.stream().map(this::toPortfolioDetailModel).toList());
  }

  /**
   * 指定した写真番号のうち、アカウントが所有する未削除の写真の件数を取得する
   *
   * @param accountNo アカウント番号
   * @param photoNoList 写真番号の一覧
   * @return 件数（写真番号の一覧が空の場合は0）
   */
  @Override
  public Integer countAvailablePhotos(AccountNo accountNo, PhotoNoList photoNoList) {
    if (photoNoList.isEmpty()) {
      // IN句が空になるとSQL構文エラーになるため、問い合わせずに0件とする
      return 0;
    }
    return portfolioPhotoMapper.countAvailablePhotos(
        accountNo.value(), photoNoList.stream().map(PhotoNo::value).toList());
  }

  private PortfolioModel toPortfolioModel(PortfolioDto dto) {
    return PortfolioModel.builder()
        .accountNo(new AccountNo(dto.getAccountNo()))
        .portfolioNo(new PortfolioNo(dto.getPortfolioNo()))
        .theme(new PortfolioTheme(dto.getTheme()))
        .templateKbn(dto.getTemplateKbn())
        .statusKbn(dto.getStatusKbn())
        .photoCount(new PhotoCount(dto.getPhotoCount()))
        .createdAt(dto.getCreatedAt())
        .updatedAt(dto.getUpdatedAt())
        .build();
  }

  private PortfolioDetailModel toPortfolioDetailModel(PortfolioMst portfolioMst) {
    List<PortfolioPhotoDto> photoDtoList =
        portfolioPhotoMapper.selectList(
            PortfolioPhotoCondition.byAccountAndPortfolio(
                portfolioMst.getAccountNo(), portfolioMst.getPortfolioNo()));
    List<PortfolioTagMst> tagList =
        portfolioTagMstMapper.select(
            PortfolioTagMstCondition.byAccountAndPortfolio(
                portfolioMst.getAccountNo(), portfolioMst.getPortfolioNo()));

    return PortfolioDetailModel.builder()
        .accountNo(new AccountNo(portfolioMst.getAccountNo()))
        .portfolioNo(new PortfolioNo(portfolioMst.getPortfolioNo()))
        .theme(new PortfolioTheme(portfolioMst.getTheme()))
        .description(new PortfolioDescription(portfolioMst.getDescription()))
        .templateKbn(portfolioMst.getTemplateKbn())
        .templateSettings(toTemplateSettings(portfolioMst.getTemplateSettings()))
        .statusKbn(portfolioMst.getStatusKbn())
        .createdAt(portfolioMst.getCreatedAt())
        .updatedAt(portfolioMst.getUpdatedAt())
        .portfolioPhotoModelList(
            PortfolioPhotoModelList.of(
                photoDtoList.stream().map(this::toPortfolioPhotoModel).toList()))
        .portfolioTagModelList(
            PortfolioTagModelList.of(tagList.stream().map(this::toPortfolioTagModel).toList()))
        .build();
  }

  private PortfolioPhotoModel toPortfolioPhotoModel(PortfolioPhotoDto dto) {
    return PortfolioPhotoModel.builder()
        .accountNo(new AccountNo(dto.getAccountNo()))
        .portfolioNo(new PortfolioNo(dto.getPortfolioNo()))
        .photoNo(new PhotoNo(dto.getPhotoNo()))
        .displayOrder(new DisplayOrder(dto.getDisplayOrder()))
        .photoSettings(toTemplateSettings(dto.getPhotoSettings()))
        .photoJapaneseTitle(new PhotoJapaneseTitle(dto.getPhotoJapaneseTitle()))
        .imageFilePath(new ImageFilePath(dto.getImageFilePath()))
        .directionKbn(dto.getDirectionKbn())
        .build();
  }

  private PortfolioTagModel toPortfolioTagModel(PortfolioTagMst entity) {
    return PortfolioTagModel.builder()
        .accountNo(new AccountNo(entity.getAccountNo()))
        .portfolioNo(new PortfolioNo(entity.getPortfolioNo()))
        .tagNo(new TagNo(entity.getTagNo()))
        .tagJapaneseName(new TagJapaneseName(entity.getTagJapaneseName()))
        .tagEnglishName(new TagEnglishName(entity.getTagEnglishName()))
        .build();
  }

  private TemplateSettings toTemplateSettings(Map<String, Object> settings) {
    return settings != null ? new TemplateSettings(settings) : TemplateSettings.empty();
  }
}
