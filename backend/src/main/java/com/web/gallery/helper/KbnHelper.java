package com.web.gallery.helper;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.web.gallery.model.KbnMstModelList;

/**
 * 区分マスタに関するHelperクラス
 */
@Component
public class KbnHelper {
	/**
	 * データベースから取得した区分マスタの一覧を、グループ単位に分けてLinkedHashMapに変換する
	 *
	 * @param kbnMstModelList	{@link KbnMstModelList}
	 * @return					区分マスタのLinkedHashMap
	 */
	public Map<String, KbnMstModelList> convertToLinkedHashMap(KbnMstModelList kbnMstModelList){
		KbnMstModelList sortedKbnMstModelList = kbnMstModelList.sortBySortOrder();

		LinkedHashMap<String, KbnMstModelList> kbnMstLinkedHashMap = new LinkedHashMap<String, KbnMstModelList>();
		for(String kbnGroupJapaneseName : sortedKbnMstModelList.stream().map(kbnMstModel -> kbnMstModel.getKbnGroupJapaneseName().value()).distinct().toList()) {
			kbnMstLinkedHashMap.put(kbnGroupJapaneseName, sortedKbnMstModelList.filterByKbnGroupJapaneseName(kbnGroupJapaneseName));
		}

		return kbnMstLinkedHashMap;
	}
}
