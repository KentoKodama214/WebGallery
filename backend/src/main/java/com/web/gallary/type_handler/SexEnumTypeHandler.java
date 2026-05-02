package com.web.gallary.type_handler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import com.web.gallary.enumuration.SexEnum;

/**
 * 性別のJavaとPostgresのEnum型を仲介する型変換クラス
 */
public class SexEnumTypeHandler extends BaseTypeHandler<SexEnum> {
	// Java -> DB (WHERE句やINSERTで使用)
	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, SexEnum parameter, JdbcType jdbcType) throws SQLException {
		ps.setString(i, parameter.getDbValue());
	}
	
	// DB -> Java (SELECTの結果取得で使用)
	@Override
	public SexEnum getNullableResult(ResultSet rs, String columnName) throws SQLException {
		return fromDbValue(rs.getString(columnName));
	}
	
	@Override
	public SexEnum getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		return fromDbValue(rs.getString(columnIndex));
	}
	
	@Override
	public SexEnum getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		return fromDbValue(cs.getString(columnIndex));
	}
	
	private SexEnum fromDbValue(String dbValue) {
		if (dbValue == null) return null;
		for (SexEnum sex : SexEnum.values()) {
			if (sex.getDbValue().equals(dbValue)) return sex;
		}
		return null;
	}
}