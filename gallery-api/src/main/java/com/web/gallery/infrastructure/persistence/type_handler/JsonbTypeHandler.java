package com.web.gallery.infrastructure.persistence.type_handler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * JSONオブジェクト（{@code Map<String, Object>}）とPostgresのjsonb型を仲介する型変換クラス
 *
 * <p>ポートフォリオのテンプレート設定（{@code portfolio_mst.template_settings}・{@code
 * portfolio_photo.photo_settings}）の読み書きに使用する
 */
public class JsonbTypeHandler extends BaseTypeHandler<Map<String, Object>> {

  private static final JsonMapper JSON_MAPPER = JsonMapper.builder().build();

  private static final TypeReference<LinkedHashMap<String, Object>> MAP_TYPE =
      new TypeReference<>() {};

  // Java -> DB (WHERE句やINSERTで使用)
  @Override
  public void setNonNullParameter(
      PreparedStatement ps, int i, Map<String, Object> parameter, JdbcType jdbcType)
      throws SQLException {
    try {
      // Types.OTHERで渡すと、PostgreSQL側で代入先のjsonb型として解釈される
      ps.setObject(i, JSON_MAPPER.writeValueAsString(parameter), Types.OTHER);
    } catch (JacksonException e) {
      throw new SQLException("Failed to serialize JSON for jsonb column.", e);
    }
  }

  // DB -> Java (SELECTの結果取得で使用)
  @Override
  public Map<String, Object> getNullableResult(ResultSet rs, String columnName)
      throws SQLException {
    return fromJson(rs.getString(columnName));
  }

  @Override
  public Map<String, Object> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
    return fromJson(rs.getString(columnIndex));
  }

  @Override
  public Map<String, Object> getNullableResult(CallableStatement cs, int columnIndex)
      throws SQLException {
    return fromJson(cs.getString(columnIndex));
  }

  private Map<String, Object> fromJson(String json) throws SQLException {
    if (json == null) return null;
    try {
      return JSON_MAPPER.readValue(json, MAP_TYPE);
    } catch (JacksonException e) {
      throw new SQLException("Failed to deserialize JSON from jsonb column.", e);
    }
  }
}
