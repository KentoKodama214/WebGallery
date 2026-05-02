package com.web.gallary.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.web.gallary.entity.PhotoFavorite;

/**
 * 写真お気に入りテーブルのMapperクラス
 */
@Mapper
public interface PhotoFavoriteMapper {
	/**
	 * 写真お気に入りを登録する
	 * 
	 * @param	photoFavorite	{@link PhotoFavorite}
	 * @return					登録件数
	 */
	public Integer insert(PhotoFavorite photoFavorite);
	
	/**
	 * 写真お気に入りを削除する
	 * 
	 * @param	photoFavorite	削除対象の抽出条件
	 * @return					削除件数
	 */
	public Integer delete(PhotoFavorite photoFavorite);
}