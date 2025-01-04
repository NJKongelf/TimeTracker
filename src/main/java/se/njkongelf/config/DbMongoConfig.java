package se.njkongelf.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoSocketOpenException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

@Configuration
public class DbMongoConfig {

  @Value("${spring.data.mongodb.uri}")
  private String connectionString;

  @Bean
  public MongoClient dbConnection(){
    try {
      return mongoClient();
    }catch (MongoSocketOpenException ex){
      return null;
    }
  }



  private MongoClient mongoClient() {
    MongoClient client;
    CodecRegistry pojoCodecRegistry = fromProviders(PojoCodecProvider.builder().automatic(true).build());
    CodecRegistry codecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), pojoCodecRegistry);

    try {
      client = MongoClients.create(MongoClientSettings.builder()
        .applyConnectionString(new ConnectionString(connectionString))
        .codecRegistry(codecRegistry)
        .build());
    }catch (MongoSocketOpenException ex){
      client= null;  ;
    }
    return client;
  }
}
