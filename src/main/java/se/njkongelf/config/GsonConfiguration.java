package se.njkongelf.config;

import org.springframework.boot.autoconfigure.gson.GsonBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

@Configuration
public class GsonConfiguration {

  @Bean
  public GsonBuilderCustomizer typeAdapterRegistration() {
    return builder -> {
      builder.registerTypeAdapter(LocalDateTime.class, new LocalDateTypeAdapter());
    };
  }
}
