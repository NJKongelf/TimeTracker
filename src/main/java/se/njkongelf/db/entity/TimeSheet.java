package se.njkongelf.db.entity;

import com.querydsl.core.annotations.QueryEntity;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@QueryEntity
@Document(collection = "timesheet")
public class TimeSheet {
  @Id
  private String id;
  private String workday;
  private boolean active;
  private String notes;
  private List<TimeStamp> timeStamps;
}
