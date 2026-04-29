package com.bondworks.auth;

import com.bondworks.common.ApiException;
import org.springframework.http.HttpStatus;

public final class AuthContext {
  private static final ThreadLocal<CurrentUser> CURRENT = new ThreadLocal<>();

  private AuthContext() {}

  public static void set(CurrentUser user) {
    CURRENT.set(user);
  }

  public static CurrentUser require() {
    CurrentUser user = CURRENT.get();
    if (user == null) {
      throw new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHENTICATED", "Authentication is required");
    }
    return user;
  }

  public static void clear() {
    CURRENT.remove();
  }
}
