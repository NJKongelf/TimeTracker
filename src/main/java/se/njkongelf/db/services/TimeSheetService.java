package se.njkongelf.db.services;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import se.njkongelf.db.TimeSheetRepository;
import se.njkongelf.db.entity.TimeSheet;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Optional;

@Service
@AllArgsConstructor
public class TimeSheetService {

  private TimeSheetRepository repository;

  public TimeSheet getWorkday(String workday) {
    return repository.findByWorkday(workday);
  }

  public void createTimeSheet() {
    TimeSheet sheet = new TimeSheet();
    sheet.setWorkday(LocalDate.now().format(DateTimeFormatter.ofPattern("YYYY-MM-dd")));
    repository.save(sheet);
  }
}
