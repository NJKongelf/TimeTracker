package se.njkongelf.db;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import se.njkongelf.db.entity.TimeSheet;

import java.util.Date;

public interface TimeSheetRepository extends MongoRepository<TimeSheet, Date> {
  @Query("{'workday': ?0 }")
  TimeSheet findByWorkday(String workday);
}
