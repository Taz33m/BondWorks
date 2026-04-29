package com.bondworks.auth;

import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/demo-login")
  Map<String, Object> demoLogin(@RequestBody(required = false) Map<String, Object> body) {
    String email = body == null ? "trader@demo.com" : String.valueOf(body.getOrDefault("email", "trader@demo.com"));
    return authService.demoLogin(email);
  }
}
