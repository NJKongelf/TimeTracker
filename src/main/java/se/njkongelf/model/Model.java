package se.njkongelf.model;

import com.mongodb.MongoTimeoutException;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.TextField;
import javafx.util.converter.LongStringConverter;
import lombok.Setter;
import org.springframework.dao.DataAccessResourceFailureException;
import se.njkongelf.controller.Controller;
import se.njkongelf.db.entity.TimeSheet;
import se.njkongelf.db.entity.TimeStamp;
import se.njkongelf.db.services.TimeSheetService;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicLong;

@Setter
public class Model {
  private Controller controller;
  private Properties properties;
  private TimeSheetService timeSheetService;
  private TimeSheet timeSheet;
  private boolean dbonline;
  private static final String DATE_FORMAT = "yyyy-MM-dd";
  private static final String TIME_FORMAT = "HH:mm:ss";


  public long calcUnEvenList(List<LocalDateTime> timelist, long time) {
    if (timelist.size() > 1) {
      time = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC) - timelist.get(timelist.size() - 1).toEpochSecond(ZoneOffset.UTC);
      for (int i = timelist.size() - 1; i >= 2; i = i - 2) {
        long a = timelist.get(i - 1).toEpochSecond(ZoneOffset.UTC);
        long b = timelist.get(i - 2).toEpochSecond(ZoneOffset.UTC);
        time = time + (a - b);
      }
    } else {
      time = LocalDateTime.now()
        .toEpochSecond(ZoneOffset.UTC) - timelist.get(timelist.size() - 1).toEpochSecond(ZoneOffset.UTC);
    }
    return time;
  }

  public long calcEvenlist(List<LocalDateTime> timelist, long time) {
    for (int i = timelist.size(); i >= 2; i = i - 2) {
      long a = timelist.get(i - 1).toEpochSecond(ZoneOffset.UTC);
      long b = timelist.get(i - 2).toEpochSecond(ZoneOffset.UTC);
      time = time + (a - b);
    }
    return time;
  }

  public void backup_File(List<LocalDateTime> timelist) throws IOException {
    if (timelist.size() > 0) {
      if (dbonline) {
        updateDb(timelist);
      } else {
        try (FileWriter fileWriter = new FileWriter(System.getProperty("user.home") + File.separator + "Timetracker" + File.separator + "Timetracked_"
          + LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_FORMAT)) + ".ttb")) {
          for (LocalDateTime s : timelist) {
            fileWriter.write(s.toEpochSecond(ZoneOffset.UTC) + "\n");
          }
          fileWriter.flush();
        }
      }
    }
  }

  private void updateDb(List<LocalDateTime> timelist) {
    if (!timelist.isEmpty()) {
      List<TimeStamp> tempList = new ArrayList<>();
      timelist.forEach(time -> {
        tempList.add(new TimeStamp(time));
      });
      timeSheet.setTimeStamps(tempList);
      timeSheetService.updateTimeSheet(timeSheet);
    }
  }

  public void printToFile(List<LocalDateTime> timelist, AtomicLong calculatedTime) throws IOException {
    if (timelist.size() > 0) {
      FileWriter fileWriter = new FileWriter("Timetracked_"
        + LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_FORMAT)) + ".txt");
      for (LocalDateTime s : timelist) {
        fileWriter.write(s.format(DateTimeFormatter.ofPattern(TIME_FORMAT)) + "\n");
      }
      fileWriter.write("Tracked time: " + LocalDateTime.ofEpochSecond(calculatedTime.get(), 0, ZoneOffset.UTC)
        .format(DateTimeFormatter.ofPattern(TIME_FORMAT)));
      fileWriter.flush();
      fileWriter.close();
    }
  }

  public void readBackupFile(List<LocalDateTime> timelist,
                             ObservableList<String> listview,
                             AtomicLong calculatedTime,
                             TextField trackedTime) throws IOException {
    Optional<TimeSheet> timeSheetOptional = Optional.empty();
    try {

      timeSheetOptional = Optional.ofNullable(timeSheetService.getWorkday(LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_FORMAT))));
      timeSheetOptional.ifPresentOrElse(data -> {
        timeSheet = data;
        dbonline = true;
        Optional<List<TimeStamp>> timeStampList = Optional.ofNullable(data.getTimeStamps());
        timeStampList.ifPresent(timeStamps -> {
          timeStamps.forEach(timeStamp -> {
            timelist.add(timeStamp.date());
          });
          transferTimelist(timelist, listview, calculatedTime, trackedTime);
        });
      }, () -> {
        timeSheet = timeSheetService.createTimeSheet();
        dbonline = true;
      });
    } catch (MongoTimeoutException | DataAccessResourceFailureException ex) {
      dbonline = false;
      String file = System.getProperty("user.home")
        + File.separator
        + "Timetracker"
        + File.separator
        + "Timetracked_"
        + LocalDateTime.now()
        .format(DateTimeFormatter.ofPattern(DATE_FORMAT))
        + ".ttb";
      if (Files.exists(Paths.get(file), LinkOption.NOFOLLOW_LINKS)) {
        FileReader stream = new FileReader(file);
        BufferedReader streamReader = new BufferedReader(stream);
        List<String> stringList = streamReader.lines().toList();
        streamReader.close();
        stringList.stream().forEach(s -> {
          LongStringConverter longStringConverter = new LongStringConverter();
          timelist.add(LocalDateTime
            .ofEpochSecond(longStringConverter.fromString(s), 0, ZoneOffset.UTC));
        });
        transferTimelist(timelist, listview, calculatedTime, trackedTime);
      }
    }


  }

  private void transferTimelist(List<LocalDateTime> timelist, ObservableList<String> listview, AtomicLong calculatedTime, TextField trackedTime) {
    timelist.forEach(time -> {
      listview.add(time
        .format(DateTimeFormatter
          .ofPattern(String.format("%s %s", TIME_FORMAT, DATE_FORMAT))));
    });
    long time = 0;

    if (timelist.size() > 0) {
      calculatedTime.set(time);
      controller.starWorktime();
    }
    if (timelist.size() % 2 == 0) {
      time = calcEvenlist(timelist, time);
      calculatedTime.set(time);
      controller.starWorktime();
      trackedTime.setText(LocalDateTime
        .ofEpochSecond(time, 0, ZoneOffset.UTC)
        .format(DateTimeFormatter
          .ofPattern(TIME_FORMAT)));
    } else {
      time = calcUnEvenList(timelist, time);
      calculatedTime.set(time);
      trackedTime.setText(LocalDateTime
        .ofEpochSecond(time, 0, ZoneOffset.UTC)
        .format(DateTimeFormatter
          .ofPattern(TIME_FORMAT)));
    }
  }

  public ChangeListener<Integer> spinngerListner() {
    return (observableValue, integer, t1) -> properties.setProperty("workinghours", observableValue.getValue().toString());
  }

  public Properties readInSettingsFile(String path) {
    Properties prop = new Properties();

    try (FileInputStream stream = new FileInputStream(path)) {
      prop.load(stream);
    } catch (IOException ignored) {
    }
    return prop;
  }

  public void saveSettings(String path) {
    try (FileOutputStream fos = new FileOutputStream(path);) {
      properties.store(fos, "Saving settings");
      fos.flush();
    } catch (IOException ignored) {
    }

  }

}
