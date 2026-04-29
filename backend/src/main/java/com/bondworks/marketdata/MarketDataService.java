package com.bondworks.marketdata;

import com.bondworks.common.Numbers;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class MarketDataService {
  private final JdbcTemplate jdbc;

  public MarketDataService(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public List<Map<String, Object>> prints(UUID bondId) {
    return jdbc.queryForList("""
        SELECT id, bond_id, price, quantity, side, printed_at, source
        FROM market_prints
        WHERE bond_id = ?
        ORDER BY printed_at DESC
        LIMIT 50
        """, bondId);
  }

  public BigDecimal recentPrint(UUID bondId) {
    List<BigDecimal> rows = jdbc.query("""
        SELECT price FROM market_prints
        WHERE bond_id = ?
        ORDER BY printed_at DESC
        LIMIT 1
        """, (rs, rowNum) -> rs.getBigDecimal("price"), bondId);
    return rows.isEmpty() ? null : rows.getFirst();
  }

  public BigDecimal tapeVwap(UUID bondId) {
    BigDecimal vwap = vwapForQuery("""
        SELECT price, quantity FROM market_prints
        WHERE bond_id = ? AND printed_at >= now() - interval '30 minutes'
        """, bondId);
    if (vwap != null) {
      return vwap;
    }
    return vwapForQuery("""
        SELECT price, quantity FROM (
          SELECT price, quantity FROM market_prints
          WHERE bond_id = ?
          ORDER BY printed_at DESC
          LIMIT 10
        ) p
        """, bondId);
  }

  private BigDecimal vwapForQuery(String sql, UUID bondId) {
    List<Map<String, Object>> prints = jdbc.queryForList(sql, bondId);
    if (prints.isEmpty()) {
      return null;
    }
    BigDecimal notional = BigDecimal.ZERO;
    BigDecimal quantity = BigDecimal.ZERO;
    for (Map<String, Object> print : prints) {
      BigDecimal q = Numbers.bd(print.get("quantity"));
      notional = notional.add(Numbers.bd(print.get("price")).multiply(q));
      quantity = quantity.add(q);
    }
    return quantity.compareTo(BigDecimal.ZERO) == 0 ? null : Numbers.div(notional, quantity);
  }
}
