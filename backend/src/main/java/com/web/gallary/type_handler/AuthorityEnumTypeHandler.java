package com.web.gallary.type_handler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import com.web.gallary.enumuration.AuthorityEnum;

/**
 * 権限のJavaとPostgresのEnum型を仲介する型変換クラス
 */
public class AuthorityEnumTypeHandler extends BaseTypeHandler<AuthorityEnum> {
	// Java -> DB (WHERE句やINSERTで使用)
	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, AuthorityEnum parameter, JdbcType jdbcType) throws SQLException {
		ps.setString(i, parameter.getDbValue());
	}
	
	// DB -> Java (SELECTの結果取得で使用)
	@Override
	public AuthorityEnum getNullableResult(ResultSet rs, String columnName) throws SQLException {
		return fromDbValue(rs.getString(columnName));
	}
	
	@Override
	public AuthorityEnum getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		return fromDbValue(rs.getString(columnIndex));
	}
	
	@Override
	public AuthorityEnum getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		return fromDbValue(cs.getString(columnIndex));
	}
	
	private AuthorityEnum fromDbValue(String dbValue) {
		if (dbValue == null) return null;
		for (AuthorityEnum authority : AuthorityEnum.values()) {
			if (authority.getDbValue().equals(dbValue)) return authority;
		}
		return null;
	}
}