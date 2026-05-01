package com.web.gallary.helper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.web.gallary.model.KbnMstModel;

/**
 * 区分マスタに関するHelperクラス
 */
@Component
public class KbnHelper {
	/**
	 * データベースから取得した区分マスタの一覧を、グループ単位に分けてLinkedHashMapに変換する
	 * 
	 * @param kbnMstModelList	{@link KbnMstModel}
	 * @return					区分マスタのLinkedHashMap
	 */
	public Map<String, List<KbnMstModel>> convertToLinkedHashMap(List<KbnMstModel> kbnMstModelList){
		LinkedHashMap<String, List<KbnMstModel>> kbnMstLinkedHashMap = new LinkedHashMap<String, List<KbnMstModel>>();
		
		kbnMstModelList = kbnMstModelList.stream().sorted(Comparator.comparing(KbnMstModel::getSortOrder)).toList();
		KbnMstModel firstKbnMstModel = kbnMstModelList.getFirst();
		KbnMstModel lastKbnMstModel = kbnMstModelList.getLast();
		List<KbnMstModel> tempKbnMstModelList = new ArrayList<KbnMstModel>();
		String kbnGroupJapaneseName = null;
		
		for(KbnMstModel kbnMstModel : kbnMstModelList) {
			if(kbnMstModel.equals(firstKbnMstModel)) {
				kbnGroupJapaneseName = kbnMstModel.getKbnGroupJapaneseName();
				tempKbnMstModelList.add(kbnMstModel);
			}
			else if(kbnMstModel.equals(lastKbnMstModel)) {
				tempKbnMstModelList.add(kbnMstModel);
				kbnMstLinkedHashMap.put(kbnGroupJapaneseName, tempKbnMstModelList);
			}
			else if(!kbnGroupJapaneseName.equals(kbnMstModel.getKbnGroupJapaneseName())) {
				kbnMstLinkedHashMap.put(kbnGroupJapaneseName, tempKbnMstModelList);
				tempKbnMstModelList = new ArrayList<KbnMstModel>();
				kbnGroupJapaneseName = kbnMstModel.getKbnGroupJapaneseName();
				tempKbnMstModelList.add(kbnMstModel);
			}
			else {
				tempKbnMstModelList.add(kbnMstModel);
			}
		};
		
		return kbnMstLinkedHashMap;
	}
}