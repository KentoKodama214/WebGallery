package com.web.gallery.model;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * PhotoDetailModelのコレクションを表すクラス
 *
 * @param	photoDetailModelList	{@link PhotoDetailModel}のリスト
 */
public record PhotoDetailModelList(List<PhotoDetailModel> photoDetailModelList) implements Iterable<PhotoDetailModel> {

	public PhotoDetailModelList {
		Objects.requireNonNull(photoDetailModelList);
	}

	/**
	 * PhotoDetailModelのリストからPhotoDetailModelListを生成する
	 *
	 * @param	photoDetailModelList	{@link PhotoDetailModel}のリスト
	 * @return							{@link PhotoDetailModelList}
	 */
	public static PhotoDetailModelList of(List<PhotoDetailModel> photoDetailModelList) {
		return new PhotoDetailModelList(photoDetailModelList);
	}

	/**
	 * 空のPhotoDetailModelListを生成する
	 *
	 * @return	{@link PhotoDetailModelList}
	 */
	public static PhotoDetailModelList empty() {
		return PhotoDetailModelList.of(List.of());
	}

	/**
	 * 要素数を取得する
	 *
	 * @return	要素数
	 */
	public Integer size() {
		return photoDetailModelList.size();
	}

	/**
	 * 要素が空かどうかを取得する
	 *
	 * @return	要素が空の場合はtrue
	 */
	public Boolean isEmpty() {
		return photoDetailModelList.isEmpty();
	}

	/**
	 * 指定インデックスの要素を取得する
	 *
	 * @param	index	インデックス
	 * @return			{@link PhotoDetailModel}
	 */
	public PhotoDetailModel get(int index) {
		return photoDetailModelList.get(index);
	}

	/**
	 * 先頭の要素を取得する
	 *
	 * @return	{@link PhotoDetailModel}
	 */
	public PhotoDetailModel getFirst() {
		return photoDetailModelList.getFirst();
	}

	/**
	 * Streamに変換する
	 *
	 * @return	{@link PhotoDetailModel}のStream
	 */
	public Stream<PhotoDetailModel> stream() {
		return photoDetailModelList.stream();
	}

	/**
	 * Listに変換する
	 *
	 * @return	{@link PhotoDetailModel}のList
	 */
	public List<PhotoDetailModel> toList() {
		return photoDetailModelList;
	}

	@Override
	public Iterator<PhotoDetailModel> iterator() {
		return photoDetailModelList.iterator();
	}
}
