package se.njkongelf;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
@EnableAutoConfiguration(exclude={MongoAutoConfiguration.class})
@SpringBootApplication
public class TimeTracker {

  private static ConfigurableApplicationContext context;

  public static void main(String[] args) {
    Thread springThread = new Thread(() -> {JavaFxInit.main(args);
    });
    springThread.setDaemon(true); // Gör tråden bakgrundsprocess
    springThread.start();

      context = SpringApplication.run(TimeTracker.class, args);






  }
  public static ConfigurableApplicationContext getContext() {
    return context;
  }
}
