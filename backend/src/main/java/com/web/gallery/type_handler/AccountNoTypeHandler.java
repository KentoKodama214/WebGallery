package com.web.gallery.type_handler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import com.web.gallery.domain.account.AccountNo;

/**
 * AccountNoの値オブジェクトとDB間の型変換を行うTypeHandler
 */
public class AccountNoTypeHandler extends BaseTypeHandler<AccountNo> {
	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, AccountNo parameter, JdbcType jdbcType) throws SQLException {
		ps.setLong(i, parameter.getValue());
	}

	@Override
	public AccountNo getNullableResult(ResultSet rs, String columnName) throws SQLException {
		long value = rs.getLong(columnName);
		return rs.wasNull() ? null : new AccountNo(value);
	}

	@Override
	public AccountNo getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		long value = rs.getLong(columnIndex);
		return rs.wasNull() ? null : new AccountNo(value);
	}

	@Override
	public AccountNo getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		long value = cs.getLong(columnIndex);
		return cs.wasNull() ? null : new AccountNo(value);
	}
}
