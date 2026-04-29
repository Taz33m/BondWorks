package com.bondworks.common;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class Numbers {
  private Numbers() {}

  public static BigDecimal bd(Object value) {
    if (value instanceof BigDecimal b) {
      return b;
    }
    if (value instanceof Number n) {
      return BigDecimal.valueOf(n.doubleValue());
    }
    return new BigDecimal(String.valueOf(value));
  }

  public static BigDecimal div(BigDecimal left, BigDecimal right) {
    if (right.compareTo(BigDecimal.ZERO) == 0) {
      return BigDecimal.ZERO;
    }
    return left.divide(right, 10, RoundingMode.HALF_UP);
  }

  public static BigDecimal scale(BigDecimal value, int scale) {
    return value == null ? null : value.setScale(scale, RoundingMode.HALF_UP);
  }
}
