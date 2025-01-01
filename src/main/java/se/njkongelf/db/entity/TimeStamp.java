package se.njkongelf.db.entity;

import java.time.LocalDateTime;

public record TimeStamp(LocalDateTime date) {
  public TimeStamp(LocalDateTime date) {
    this.date = date;
  }
}
/*
//https://stackoverflow.com/questions/67622840/how-to-configure-in-memory-embedded-database-for-mongodb-similar-to-h2-database
//    @Override
//    public MongoClient mongoClient() {
//
//        MongoServer server = new MongoServer(new MemoryBackend());
//        // bind on a random local port
//        InetSocketAddress serverAddress = server.bind();
//        return  mongoClient=MongoClients.create(
//                MongoClientSettings.builder()
//                        .applyToClusterSettings(builder ->
//                                builder.hosts(Arrays.asList(new ServerAddress(serverAddress))))
//                        .build());
//
//    }*/
