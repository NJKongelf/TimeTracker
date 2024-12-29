package se.njkongelf.db.entity;

import java.util.Date;

public record TimeStamp(Long timestamp, Date date) {
    public TimeStamp(Long timestamp, Date date) {
        this.timestamp = timestamp;
        this.date = date;
    }
}
/*
https://stackoverflow.com/questions/67622840/how-to-configure-in-memory-embedded-database-for-mongodb-similar-to-h2-database
    @Override
    public MongoClient mongoClient() {

        MongoServer server = new MongoServer(new MemoryBackend());
        // bind on a random local port
        InetSocketAddress serverAddress = server.bind();
        return  mongoClient=MongoClients.create(
                MongoClientSettings.builder()
                        .applyToClusterSettings(builder ->
                                builder.hosts(Arrays.asList(new ServerAddress(serverAddress))))
                        .build());

    }*/
