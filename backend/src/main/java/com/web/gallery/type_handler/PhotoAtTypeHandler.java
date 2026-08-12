package com.web.gallery.type_handler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import com.web.gallery.domain.photo.PhotoAt;

/**
 * PhotoAtの値オブジェクトとDB間の型変換を行うTypeHandler
 */
public class PhotoAtTypeHandler extends BaseTypeHandler<PhotoAt> {
	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, PhotoAt parameter, JdbcType jdbcType) throws SQLException {
		ps.setObject(i, parameter.value());
	}

	@Override
	public PhotoAt getNullableResult(ResultSet rs, String columnName) throws SQLException {
		OffsetDateTime value = rs.getObject(columnName, OffsetDateTime.class);
		return value == null ? null : new PhotoAt(value);
	}

	@Override
	public PhotoAt getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		OffsetDateTime value = rs.getObject(columnIndex, OffsetDateTime.class);
		return value == null ? null : new PhotoAt(value);
	}

	@Override
	public PhotoAt getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		OffsetDateTime value = cs.getObject(columnIndex, OffsetDateTime.class);
		return value == null ? null : new PhotoAt(value);
	}
}
