package com.bondworks.auth;

import com.bondworks.common.ApiException;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
  private final JdbcTemplate jdbc;
  private final JwtService jwtService;

  public AuthService(JdbcTemplate jdbc, JwtService jwtService) {
    this.jdbc = jdbc;
    this.jwtService = jwtService;
  }

  public Map<String, Object> demoLogin(String email) {
    CurrentUser user = findByEmail(email);
    return Map.of("token", jwtService.issue(user), "user", user);
  }

  public CurrentUser findByEmail(String email) {
    return jdbc.query("""
        SELECT id, email, name, role FROM users WHERE email = ?
        """, rs -> {
      if (!rs.next()) {
        throw new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_DEMO_USER", "Unknown demo user");
      }
      return new CurrentUser(UUID.fromString(rs.getString("id")), rs.getString("email"), rs.getString("name"), rs.getString("role"));
    }, email);
  }

  public CurrentUser findById(UUID id) {
    return jdbc.query("""
        SELECT id, email, name, role FROM users WHERE id = ?
        """, rs -> {
      if (!rs.next()) {
        throw new ApiException(HttpStatus.UNAUTHORIZED, "UNKNOWN_USER", "User no longer exists");
      }
      return new CurrentUser(UUID.fromString(rs.getString("id")), rs.getString("email"), rs.getString("name"), rs.getString("role"));
    }, id);
  }

  public CurrentUser findByApiKey(String apiKey) {
    return jdbc.query("""
        SELECT u.id, u.email, u.name, u.role
        FROM demo_api_keys k
        JOIN users u ON u.id = k.user_id
        WHERE k.api_key = ?
        """, rs -> {
      if (!rs.next()) {
        throw new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_API_KEY", "Invalid API key");
      }
      return new CurrentUser(UUID.fromString(rs.getString("id")), rs.getString("email"), rs.getString("name"), rs.getString("role"));
    }, apiKey);
  }

  public CurrentUser fromBearer(String token) {
    Map<String, Object> claims = jwtService.verify(token);
    return findById(UUID.fromString(String.valueOf(claims.get("sub"))));
  }
}
