package com.bondworks.config;

import com.bondworks.auth.AuthInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
  private final AuthInterceptor authInterceptor;
  private final String corsOrigin;

  public WebConfig(AuthInterceptor authInterceptor, @Value("${bondworks.cors-origin}") String corsOrigin) {
    this.authInterceptor = authInterceptor;
    this.corsOrigin = corsOrigin;
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(authInterceptor).addPathPatterns("/api/**");
  }

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/**")
        .allowedOrigins(corsOrigin, "http://localhost:5173", "http://127.0.0.1:5173")
        .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
        .allowedHeaders("*")
        .exposedHeaders("Location")
        .allowCredentials(false);
  }
}
