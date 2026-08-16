package com.web.gallery.model;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import com.web.gallery.dto.PhotoDto;
import com.web.gallery.entity.PhotoTagMst;
import com.web.gallery.enumuration.DirectionEnum;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/**
 * PhotoModelのコレクションを表すクラス
 */
@Value
@Builder
public class PhotoModelList implements Iterable<PhotoModel> {
	/** PhotoModelのリスト */
	@NonNull
	private List<PhotoModel> photoModelList;

	/**
	 * PhotoModelのリストからPhotoModelListを生成する
	 *
	 * @param	photoModelList	{@link PhotoModel}のリスト
	 * @return					{@link PhotoModelList}
	 */
	public static PhotoModelList of(List<PhotoModel> photoModelList) {
		return PhotoModelList.builder().photoModelList(photoModelList).build();
	}

	/**
	 * 空のPhotoModelListを生成する
	 *
	 * @return	{@link PhotoModelList}
	 */
	public static PhotoModelList empty() {
		return PhotoModelList.of(List.of());
	}

	/**
	 * PhotoDtoのリストとタグエンティティリストからPhotoModelListを生成する
	 *
	 * @param	photoDtoList	{@link PhotoDto}のリスト
	 * @param	photoTagMstList	全タグエンティティリスト（内部で該当写真のタグをフィルタリングする）
	 * @return					{@link PhotoModelList}
	 */
	public static PhotoModelList from(List<PhotoDto> photoDtoList, List<PhotoTagMst> photoTagMstList) {
		return PhotoModelList.of(photoDtoList.stream()
				.map(photoDto -> PhotoModel.from(photoDto, photoTagMstList))
				.toList());
	}

	/**
	 * 指定のComparatorでソートしたPhotoModelListを生成する
	 *
	 * @param	comparator	ソート条件のComparator
	 * @return				{@link PhotoModelList}
	 */
	public PhotoModelList sorted(Comparator<PhotoModel> comparator) {
		return PhotoModelList.of(photoModelList.stream().sorted(comparator).toList());
	}

	/**
	 * 向き区分でフィルタリングしたPhotoModelListを生成する<p>
	 * 条件がNONEの場合はフィルタリングしない
	 *
	 * @param	conditionDirectionKbn	フィルター条件の向き区分
	 * @return							{@link PhotoModelList}
	 */
	public PhotoModelList filterByDirectionKbn(DirectionEnum conditionDirectionKbn) {
		if (DirectionEnum.NONE.equals(conditionDirectionKbn)) return this;

		return PhotoModelList.of(photoModelList.stream()
				.filter(photoModel -> photoModel.getDirectionKbn().equals(conditionDirectionKbn))
				.toList());
	}

	/**
	 * お気に入りでフィルタリングしたPhotoModelListを生成する<p>
	 * isFavoriteOnlyがfalseの場合はフィルタリングしない
	 *
	 * @param	isFavoriteOnly	お気に入りに絞るならtrue
	 * @return					{@link PhotoModelList}
	 */
	public PhotoModelList filterByFavorite(Boolean isFavoriteOnly) {
		if (!isFavoriteOnly) return this;

		return PhotoModelList.of(photoModelList.stream()
				.filter(photoModel -> photoModel.getIsFavorite().value())
				.toList());
	}

	/**
	 * タグでフィルタリングしたPhotoModelListを生成する<p>
	 * タグが複数ある場合、すべてのタグを持つ写真にフィルタリングする
	 *
	 * @param	tags	フィルター条件のタグのリスト
	 * @return			{@link PhotoModelList}
	 */
	public PhotoModelList filterByTags(List<String> tags) {
		return PhotoModelList.of(photoModelList.stream()
				.filter(photoModel -> photoModel.getPhotoTagModelList().containsAllTags(tags))
				.toList());
	}

	/**
	 * 要素数を取得する
	 *
	 * @return	要素数
	 */
	public Integer size() {
		return photoModelList.size();
	}

	/**
	 * 要素が空かどうかを取得する
	 *
	 * @return	要素が空の場合はtrue
	 */
	public Boolean isEmpty() {
		return photoModelList.isEmpty();
	}

	/**
	 * 指定インデックスの要素を取得する
	 *
	 * @param	index	インデックス
	 * @return			{@link PhotoModel}
	 */
	public PhotoModel get(int index) {
		return photoModelList.get(index);
	}

	/**
	 * 先頭の要素を取得する
	 *
	 * @return	{@link PhotoModel}
	 */
	public PhotoModel getFirst() {
		return photoModelList.getFirst();
	}

	/**
	 * Streamに変換する
	 *
	 * @return	{@link PhotoModel}のStream
	 */
	public Stream<PhotoModel> stream() {
		return photoModelList.stream();
	}

	/**
	 * Listに変換する
	 *
	 * @return	{@link PhotoModel}のList
	 */
	public List<PhotoModel> toList() {
		return photoModelList;
	}

	@Override
	public Iterator<PhotoModel> iterator() {
		return photoModelList.iterator();
	}
}
