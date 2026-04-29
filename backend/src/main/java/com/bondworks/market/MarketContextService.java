package com.bondworks.market;

import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class MarketContextService {
  private final JdbcTemplate jdbc;

  public MarketContextService(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public List<Map<String, Object>> latestYieldCurve() {
    return jdbc.queryForList("""
        SELECT curve_date, tenor, rate, source
        FROM yield_curve_points
        WHERE curve_date = (SELECT MAX(curve_date) FROM yield_curve_points)
        ORDER BY CASE tenor
          WHEN '1M' THEN 1 WHEN '2M' THEN 2 WHEN '3M' THEN 3 WHEN '4M' THEN 4
          WHEN '6M' THEN 5 WHEN '1Y' THEN 6 WHEN '2Y' THEN 7 WHEN '3Y' THEN 8
          WHEN '5Y' THEN 9 WHEN '7Y' THEN 10 WHEN '10Y' THEN 11 WHEN '20Y' THEN 12
          WHEN '30Y' THEN 13 ELSE 99 END
        """);
  }

  public List<Map<String, Object>> latestReferenceRates() {
    return jdbc.queryForList("""
        SELECT rate_date, rate_type, value, volume, source
        FROM reference_rates
        WHERE rate_date = (SELECT MAX(rate_date) FROM reference_rates)
        ORDER BY rate_type
        """);
  }

  public Map<String, Object> context() {
    return Map.of(
        "yield_curve", latestYieldCurve(),
        "reference_rates", latestReferenceRates(),
        "rates_regime", "Stable",
        "market_status", "OPEN");
  }
}
