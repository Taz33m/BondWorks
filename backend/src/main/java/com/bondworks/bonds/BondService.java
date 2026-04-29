package com.bondworks.bonds;

import com.bondworks.common.ApiException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class BondService {
  private final JdbcTemplate jdbc;

  public BondService(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public List<Map<String, Object>> list() {
    return jdbc.queryForList("""
        SELECT id, code, ticker, issuer, cusip_like_id, coupon, maturity_date, sector, rating,
               currency, face_value, mid_price, mid_yield
        FROM bonds
        ORDER BY sector, maturity_date
        """);
  }

  public Map<String, Object> get(UUID id) {
    List<Map<String, Object>> rows = jdbc.queryForList("""
        SELECT id, code, ticker, issuer, cusip_like_id, coupon, maturity_date, sector, rating,
               currency, face_value, mid_price, mid_yield
        FROM bonds WHERE id = ?
        """, id);
    if (rows.isEmpty()) {
      throw new ApiException(HttpStatus.NOT_FOUND, "BOND_NOT_FOUND", "Bond not found");
    }
    return rows.getFirst();
  }

  public UUID resolveId(String codeOrId) {
    try {
      return UUID.fromString(codeOrId);
    } catch (IllegalArgumentException ignored) {
      List<UUID> ids = jdbc.query("SELECT id FROM bonds WHERE code = ?", (rs, rowNum) -> (UUID) rs.getObject("id"), codeOrId);
      if (ids.isEmpty()) {
        throw new ApiException(HttpStatus.NOT_FOUND, "BOND_NOT_FOUND", "Bond code not found");
      }
      return ids.getFirst();
    }
  }
}
