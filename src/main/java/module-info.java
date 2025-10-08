module TimeTracker {
  requires javafx.controls;
  requires javafx.fxml;
  requires spring.data.mongodb;
  requires spring.data.commons;
  requires spring.boot.autoconfigure;
  requires spring.boot;
  requires spring.context;
  requires spring.core;
  requires static lombok;
  requires org.mongodb.bson;
  requires org.mongodb.driver.sync.client;
  requires org.mongodb.driver.core;
  requires spring.beans;
  requires com.querydsl.core;
  requires spring.tx;
  requires feign.core;
  requires org.slf4j;
  requires com.google.gson;
    requires java.desktop;
    opens se.njkongelf.db.entity to spring.core, spring.data.commons, com.google.gson;
  opens se.njkongelf.db.services to spring.core, spring.beans;
  opens se.njkongelf.controller to javafx.fxml;
  opens se.njkongelf to javafx.fxml, spring.core, spring.beans;
  opens se.njkongelf.config;
  exports se.njkongelf;

}
