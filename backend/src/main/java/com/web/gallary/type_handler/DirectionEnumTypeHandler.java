package com.web.gallary.type_handler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import com.web.gallary.enumuration.DirectionEnum;

/**
 * 向きのJavaとPostgresのEnum型を仲介する型変換クラス
 */
public class DirectionEnumTypeHandler extends BaseTypeHandler<DirectionEnum> {
	// Java -> DB (WHERE句やINSERTで使用)
	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, DirectionEnum parameter, JdbcType jdbcType)
			throws SQLException {
		ps.setString(i, parameter.getDbValue());
	}

	// DB -> Java (SELECTの結果取得で使用)
	@Override
	public DirectionEnum getNullableResult(ResultSet rs, String columnName) throws SQLException {
		return fromDbValue(rs.getString(columnName));
	}

	@Override
	public DirectionEnum getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		return fromDbValue(rs.getString(columnIndex));
	}

	@Override
	public DirectionEnum getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		return fromDbValue(cs.getString(columnIndex));
	}

	private DirectionEnum fromDbValue(String dbValue) {
		if (dbValue == null)
			return null;
		for (DirectionEnum direction : DirectionEnum.values()) {
			if (direction.getDbValue().equals(dbValue))
				return direction;
		}
		return null;
	}
}