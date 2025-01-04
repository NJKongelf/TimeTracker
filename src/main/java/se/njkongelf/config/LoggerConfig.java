package se.njkongelf.config;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;



@Configuration
public class LoggerConfig {

  @Bean("logBean")
  public Logger globalLogger() {
    return LoggerFactory.getLogger("GlobalLogger");
  }
}
