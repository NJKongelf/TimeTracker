package se.njkongelf;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.concurrent.CountDownLatch;

@EnableAutoConfiguration(exclude = {MongoAutoConfiguration.class})
@SpringBootApplication
public class TimeTracker {

  private static ConfigurableApplicationContext context;
  private static final CountDownLatch latch = new CountDownLatch(1);

  public static void main(String[] args) {
    Thread springThread = new Thread(() -> {
      context = SpringApplication.run(TimeTracker.class, args);
      latch.countDown(); // Signalera att kontexten är redo
    });
    springThread.setDaemon(true); // Gör tråden till en bakgrundsprocess
    springThread.start();

    // Starta JavaFX-applikationen
    JavaFxInit.main(args);

  }
  public static ConfigurableApplicationContext getContext() {
    try {
      latch.await(); // Vänta tills kontexten är initierad
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("Spring context initialization interrupted", e);
    }
    return context;
  }
}

