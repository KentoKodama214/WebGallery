package com.web.gallery.model;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * PhotoDeleteModelのコレクションを表すクラス
 *
 * @param	photoDeleteModelList	{@link PhotoDeleteModel}のリスト
 */
public record PhotoDeleteModelList(List<PhotoDeleteModel> photoDeleteModelList) implements Iterable<PhotoDeleteModel> {

	public PhotoDeleteModelList {
		Objects.requireNonNull(photoDeleteModelList);
	}

	/**
	 * PhotoDeleteModelのリストからPhotoDeleteModelListを生成する
	 *
	 * @param	photoDeleteModelList	{@link PhotoDeleteModel}のリスト
	 * @return							{@link PhotoDeleteModelList}
	 */
	public static PhotoDeleteModelList of(List<PhotoDeleteModel> photoDeleteModelList) {
		return new PhotoDeleteModelList(photoDeleteModelList);
	}

	/**
	 * 空のPhotoDeleteModelListを生成する
	 *
	 * @return	{@link PhotoDeleteModelList}
	 */
	public static PhotoDeleteModelList empty() {
		return PhotoDeleteModelList.of(List.of());
	}

	/**
	 * 要素数を取得する
	 *
	 * @return	要素数
	 */
	public Integer size() {
		return photoDeleteModelList.size();
	}

	/**
	 * 要素が空かどうかを取得する
	 *
	 * @return	要素が空の場合はtrue
	 */
	public Boolean isEmpty() {
		return photoDeleteModelList.isEmpty();
	}

	/**
	 * 指定インデックスの要素を取得する
	 *
	 * @param	index	インデックス
	 * @return			{@link PhotoDeleteModel}
	 */
	public PhotoDeleteModel get(int index) {
		return photoDeleteModelList.get(index);
	}

	/**
	 * 先頭の要素を取得する
	 *
	 * @return	{@link PhotoDeleteModel}
	 */
	public PhotoDeleteModel getFirst() {
		return photoDeleteModelList.getFirst();
	}

	/**
	 * Streamに変換する
	 *
	 * @return	{@link PhotoDeleteModel}のStream
	 */
	public Stream<PhotoDeleteModel> stream() {
		return photoDeleteModelList.stream();
	}

	/**
	 * Listに変換する
	 *
	 * @return	{@link PhotoDeleteModel}のList
	 */
	public List<PhotoDeleteModel> toList() {
		return photoDeleteModelList;
	}

	@Override
	public Iterator<PhotoDeleteModel> iterator() {
		return photoDeleteModelList.iterator();
	}
}
