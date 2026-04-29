package com.bondworks.auth;

import com.bondworks.common.ApiException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
  private final ObjectMapper objectMapper;
  private final byte[] secret;

  public JwtService(ObjectMapper objectMapper, @Value("${bondworks.jwt-secret}") String secret) {
    this.objectMapper = objectMapper;
    this.secret = secret.getBytes(StandardCharsets.UTF_8);
  }

  public String issue(CurrentUser user) {
    try {
      String header = encode(objectMapper.writeValueAsBytes(Map.of("alg", "HS256", "typ", "JWT")));
      String payload = encode(objectMapper.writeValueAsBytes(Map.of(
          "sub", user.id().toString(),
          "email", user.email(),
          "name", user.name(),
          "role", user.role(),
          "exp", Instant.now().plusSeconds(8 * 60 * 60).getEpochSecond())));
      String body = header + "." + payload;
      return body + "." + sign(body);
    } catch (Exception ex) {
      throw new IllegalStateException("Unable to issue token", ex);
    }
  }

  @SuppressWarnings("unchecked")
  public Map<String, Object> verify(String token) {
    try {
      String[] parts = token.split("\\.");
      if (parts.length != 3) {
        throw invalid();
      }
      String body = parts[0] + "." + parts[1];
      if (!MessageDigestSafe.equals(sign(body), parts[2])) {
        throw invalid();
      }
      Map<String, Object> claims = objectMapper.readValue(Base64.getUrlDecoder().decode(parts[1]), Map.class);
      long exp = Long.parseLong(String.valueOf(claims.get("exp")));
      if (Instant.now().getEpochSecond() > exp) {
        throw new ApiException(HttpStatus.UNAUTHORIZED, "TOKEN_EXPIRED", "Session token expired");
      }
      return claims;
    } catch (ApiException ex) {
      throw ex;
    } catch (Exception ex) {
      throw invalid();
    }
  }

  private ApiException invalid() {
    return new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN", "Invalid session token");
  }

  private String sign(String body) throws Exception {
    Mac mac = Mac.getInstance("HmacSHA256");
    mac.init(new SecretKeySpec(secret, "HmacSHA256"));
    return encode(mac.doFinal(body.getBytes(StandardCharsets.UTF_8)));
  }

  private String encode(byte[] bytes) {
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }

  private static final class MessageDigestSafe {
    static boolean equals(String left, String right) {
      byte[] a = left.getBytes(StandardCharsets.UTF_8);
      byte[] b = right.getBytes(StandardCharsets.UTF_8);
      if (a.length != b.length) return false;
      int diff = 0;
      for (int i = 0; i < a.length; i++) diff |= a[i] ^ b[i];
      return diff == 0;
    }
  }
}
