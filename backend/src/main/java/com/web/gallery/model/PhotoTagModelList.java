package com.web.gallery.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import com.web.gallery.constant.Consts;
import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.photo.PhotoNo;
import com.web.gallery.entity.PhotoTagMst;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/**
 * PhotoTagModelのコレクションを表すクラス
 */
@Value
@Builder
public class PhotoTagModelList implements Iterable<PhotoTagModel> {
	/** PhotoTagModelのリスト */
	@NonNull
	private List<PhotoTagModel> photoTagModelList;

	/**
	 * PhotoTagModelのリストからPhotoTagModelListを生成する
	 *
	 * @param	photoTagModelList	{@link PhotoTagModel}のリスト
	 * @return						{@link PhotoTagModelList}
	 */
	public static PhotoTagModelList of(List<PhotoTagModel> photoTagModelList) {
		return PhotoTagModelList.builder().photoTagModelList(photoTagModelList).build();
	}

	/**
	 * 空のPhotoTagModelListを生成する
	 *
	 * @return	{@link PhotoTagModelList}
	 */
	public static PhotoTagModelList empty() {
		return PhotoTagModelList.of(List.of());
	}

	/**
	 * PhotoTagMstエンティティのリストからPhotoTagModelListを生成する<p>
	 * タグ番号の昇順でソートして生成する
	 *
	 * @param	photoTagMstList	{@link PhotoTagMst}のリスト
	 * @return					{@link PhotoTagModelList}
	 */
	public static PhotoTagModelList from(List<PhotoTagMst> photoTagMstList) {
		return PhotoTagModelList.of(photoTagMstList.stream().map(PhotoTagModel::from).toList()).sortByTagNo();
	}

	/**
	 * タグ番号の昇順でソートしたPhotoTagModelListを生成する
	 *
	 * @return	{@link PhotoTagModelList}
	 */
	public PhotoTagModelList sortByTagNo() {
		return PhotoTagModelList.of(photoTagModelList.stream()
				.sorted(Comparator.comparing(photoTagModel -> photoTagModel.getTagNo().value()))
				.toList());
	}

	/**
	 * アカウント番号・写真番号でフィルタリングしたPhotoTagModelListを生成する
	 *
	 * @param	accountNo	フィルター条件のアカウント番号
	 * @param	photoNo		フィルター条件の写真番号
	 * @return				{@link PhotoTagModelList}
	 */
	public PhotoTagModelList filterByPhoto(AccountNo accountNo, PhotoNo photoNo) {
		return PhotoTagModelList.of(photoTagModelList.stream()
				.filter(photoTagModel ->
					photoTagModel.getAccountNo().value().equals(accountNo.value()) &&
					Objects.equals(photoTagModel.getPhotoNo().value(), photoNo.value()))
				.toList());
	}

	/**
	 * 指定のタグをすべて保持しているかどうかを判定する<p>
	 * タグが未指定の場合はtrueを返す
	 *
	 * @param	tags	判定対象のタグのリスト
	 * @return			すべてのタグを保持している場合はtrue
	 */
	public Boolean containsAllTags(List<String> tags) {
		if (tags.isEmpty() || Consts.STRING_EMPTY.equals(tags.getFirst())) return true;

		List<String> photoTags = new ArrayList<String>();
		photoTags.addAll(photoTagModelList.stream().map(photoTagModel -> photoTagModel.getTagJapaneseName().value()).toList());
		photoTags.addAll(photoTagModelList.stream().map(photoTagModel -> photoTagModel.getTagEnglishName().value()).toList());

		return photoTags.containsAll(tags);
	}

	/**
	 * 要素数を取得する
	 *
	 * @return	要素数
	 */
	public Integer size() {
		return photoTagModelList.size();
	}

	/**
	 * 要素が空かどうかを取得する
	 *
	 * @return	要素が空の場合はtrue
	 */
	public Boolean isEmpty() {
		return photoTagModelList.isEmpty();
	}

	/**
	 * 指定インデックスの要素を取得する
	 *
	 * @param	index	インデックス
	 * @return			{@link PhotoTagModel}
	 */
	public PhotoTagModel get(int index) {
		return photoTagModelList.get(index);
	}

	/**
	 * Streamに変換する
	 *
	 * @return	{@link PhotoTagModel}のStream
	 */
	public Stream<PhotoTagModel> stream() {
		return photoTagModelList.stream();
	}

	/**
	 * Listに変換する
	 *
	 * @return	{@link PhotoTagModel}のList
	 */
	public List<PhotoTagModel> toList() {
		return photoTagModelList;
	}

	@Override
	public Iterator<PhotoTagModel> iterator() {
		return photoTagModelList.iterator();
	}
}
