package com.bondworks.auth;

import com.bondworks.common.ApiException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {
  private final AuthService authService;

  public AuthInterceptor(AuthService authService) {
    this.authService = authService;
  }

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
    if ("OPTIONS".equalsIgnoreCase(request.getMethod()) || isPublic(request.getRequestURI())) {
      return true;
    }
    String apiKey = request.getHeader("X-API-Key");
    if (apiKey != null && !apiKey.isBlank()) {
      AuthContext.set(authService.findByApiKey(apiKey));
      return true;
    }
    String authorization = request.getHeader("Authorization");
    if (authorization != null && authorization.startsWith("Bearer ")) {
      AuthContext.set(authService.fromBearer(authorization.substring("Bearer ".length())));
      return true;
    }
    throw new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHENTICATED", "Authentication is required");
  }

  @Override
  public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
    AuthContext.clear();
  }

  private boolean isPublic(String uri) {
    return uri.equals("/api/auth/demo-login")
        || uri.equals("/actuator/health")
        || uri.startsWith("/ws")
        || uri.equals("/health");
  }
}
