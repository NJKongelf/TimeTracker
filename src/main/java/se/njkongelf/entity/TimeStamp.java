package se.njkongelf.entity;

import java.util.Date;

public record TimeStamp(Long timestamp, Date date) {
    public TimeStamp(Long timestamp, Date date) {
        this.timestamp = timestamp;
        this.date = date;
    }
}
