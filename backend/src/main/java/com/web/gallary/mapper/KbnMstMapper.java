package com.web.gallary.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.web.gallary.entity.KbnMst;

/**
 * 区分マスタテーブルのMapperクラス
 */
@Mapper
public interface KbnMstMapper {
	/**
	 * 条件に該当する区分マスタの一覧を取得する
	 * 
	 * @param	kbnMst	抽出条件
	 * @return			{@link KbnMst}
	 */
	public List<KbnMst> select(KbnMst kbnMst);
}