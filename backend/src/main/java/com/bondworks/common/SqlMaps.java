package com.bondworks.common;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.jdbc.core.RowMapper;

public final class SqlMaps {
  private SqlMaps() {}

  public static RowMapper<Map<String, Object>> rowMapper() {
    return (rs, rowNum) -> toMap(rs);
  }

  public static Map<String, Object> toMap(ResultSet rs) throws SQLException {
    ResultSetMetaData meta = rs.getMetaData();
    Map<String, Object> row = new LinkedHashMap<>();
    for (int i = 1; i <= meta.getColumnCount(); i++) {
      row.put(meta.getColumnLabel(i), rs.getObject(i));
    }
    return row;
  }
}
