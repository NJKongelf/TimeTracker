package se.njkongelf.db.services;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import se.njkongelf.config.LocalDateTypeAdapter;
import se.njkongelf.db.TimeSheetRepository;
import se.njkongelf.db.entity.TimeSheet;
import se.njkongelf.db.entity.TimeStamp;
import se.njkongelf.enums.DateFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;



@Service
@AllArgsConstructor
public class TimeSheetService {

  private TimeSheetRepository repository;

  public TimeSheet getWorkday(String workday) {
    return repository.findByWorkday(workday);
  }

  public TimeSheet createTimeSheet() {
    TimeSheet sheet = new TimeSheet();
    sheet.setWorkday(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
    sheet.setNotes("");
    repository.save(sheet);
    return sheet;
  }

  public void updateTimeSheet(TimeSheet sheet) {
    repository.save(sheet);
  }

  public TimeSheet offlineTimeSheet(List<LocalDateTime> timeList, @Nullable TimeSheet orginalSheet) {
    Optional<TimeSheet> nullableTimesheet = Optional.ofNullable(orginalSheet);
    TimeSheet sheet = nullableTimesheet.orElseGet(TimeSheet::new);
    sheet.setWorkday(timeList.get(0).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
    sheet.setTimeStamps(new ArrayList<>());
    timeList.forEach(timeEntry -> sheet.getTimeStamps().add(new TimeStamp(timeEntry)));
    return sheet;
  }
  public String jsonOfTimeSheet(TimeSheet timeSheet){
    Gson gson = new GsonBuilder()
      .registerTypeAdapter(LocalDateTime.class, new LocalDateTypeAdapter())
      .setPrettyPrinting()
      .create();
    return gson.toJson(timeSheet);
  }
  public List<TimeSheet> getTenLatestTimeSheets(){
    List<TimeSheet> sheets = repository.findAll(Sort.by(Sort.Order.desc("workday"))).stream().limit(10L).toList();
    return sheets.stream()
      .filter(sheet -> {Optional<List<TimeStamp>> timeStamps= Optional.ofNullable(sheet.getTimeStamps());
        return timeStamps.isPresent();
      })
      .filter(sheet -> !(sheet.getWorkday().equals(LocalDateTime
                    .now()
                    .format(DateTimeFormatter.ofPattern(DateFormat.DATE_FORMAT.getCode())))))
      .toList();

        //LocalDateTime
        //            .now()
        //            .format(DateTimeFormatter.ofPattern(DATE_FORMAT)
    //return sheets;

  }
}
