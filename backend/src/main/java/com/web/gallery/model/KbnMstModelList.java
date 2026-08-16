package com.web.gallery.model;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import com.web.gallery.entity.KbnMst;

/**
 * KbnMstModelのコレクションを表すクラス
 */
public record KbnMstModelList(List<KbnMstModel> kbnMstModelList) implements Iterable<KbnMstModel> {

	public KbnMstModelList {
		Objects.requireNonNull(kbnMstModelList);
	}

	/**
	 * KbnMstModelのリストからKbnMstModelListを生成する
	 *
	 * @param	kbnMstModelList	{@link KbnMstModel}のリスト
	 * @return					{@link KbnMstModelList}
	 */
	public static KbnMstModelList of(List<KbnMstModel> kbnMstModelList) {
		return new KbnMstModelList(kbnMstModelList);
	}

	/**
	 * 空のKbnMstModelListを生成する
	 *
	 * @return	{@link KbnMstModelList}
	 */
	public static KbnMstModelList empty() {
		return KbnMstModelList.of(List.of());
	}

	/**
	 * KbnMstエンティティのリストからKbnMstModelListを生成する
	 *
	 * @param	kbnMstList	{@link KbnMst}のリスト
	 * @return				{@link KbnMstModelList}
	 */
	public static KbnMstModelList from(List<KbnMst> kbnMstList) {
		return KbnMstModelList.of(kbnMstList.stream().map(KbnMstModel::from).toList());
	}

	/**
	 * 並び順の昇順でソートしたKbnMstModelListを生成する
	 *
	 * @return	{@link KbnMstModelList}
	 */
	public KbnMstModelList sortBySortOrder() {
		return KbnMstModelList.of(kbnMstModelList.stream()
				.sorted(Comparator.comparing(kbnMstModel -> kbnMstModel.getSortOrder().value()))
				.toList());
	}

	/**
	 * 区分グループ日本語名でフィルタリングしたKbnMstModelListを生成する
	 *
	 * @param	kbnGroupJapaneseName	フィルター条件の区分グループ日本語名
	 * @return							{@link KbnMstModelList}
	 */
	public KbnMstModelList filterByKbnGroupJapaneseName(String kbnGroupJapaneseName) {
		return KbnMstModelList.of(kbnMstModelList.stream()
				.filter(kbnMstModel -> kbnMstModel.getKbnGroupJapaneseName().value().equals(kbnGroupJapaneseName))
				.toList());
	}

	/**
	 * 要素数を取得する
	 *
	 * @return	要素数
	 */
	public Integer size() {
		return kbnMstModelList.size();
	}

	/**
	 * 要素が空かどうかを取得する
	 *
	 * @return	要素が空の場合はtrue
	 */
	public Boolean isEmpty() {
		return kbnMstModelList.isEmpty();
	}

	/**
	 * 指定インデックスの要素を取得する
	 *
	 * @param	index	インデックス
	 * @return			{@link KbnMstModel}
	 */
	public KbnMstModel get(int index) {
		return kbnMstModelList.get(index);
	}

	/**
	 * 先頭の要素を取得する
	 *
	 * @return	{@link KbnMstModel}
	 */
	public KbnMstModel getFirst() {
		return kbnMstModelList.getFirst();
	}

	/**
	 * 末尾の要素を取得する
	 *
	 * @return	{@link KbnMstModel}
	 */
	public KbnMstModel getLast() {
		return kbnMstModelList.getLast();
	}

	/**
	 * Streamに変換する
	 *
	 * @return	{@link KbnMstModel}のStream
	 */
	public Stream<KbnMstModel> stream() {
		return kbnMstModelList.stream();
	}

	/**
	 * Listに変換する
	 *
	 * @return	{@link KbnMstModel}のList
	 */
	public List<KbnMstModel> toList() {
		return kbnMstModelList;
	}

	@Override
	public Iterator<KbnMstModel> iterator() {
		return kbnMstModelList.iterator();
	}
}
