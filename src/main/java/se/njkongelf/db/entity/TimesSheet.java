package se.njkongelf.db.entity;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;
@Data
@Document("Timesheet")
public class TimesSheet {
  @Id
  private String id;
  private Date workday;
  private boolean active;
  private List<TimeStamp> timeStamps;
}
