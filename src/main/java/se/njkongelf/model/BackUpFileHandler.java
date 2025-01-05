package se.njkongelf.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import se.njkongelf.config.LocalDateTypeAdapter;
import se.njkongelf.db.entity.TimeSheet;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class BackUpFileHandler {
  private static final String DATE_FORMAT = "yyyy-MM-dd";

  public void writeBackupFile(String output, TimeSheet sheet) {
    try (FileWriter fileWriter = new FileWriter(fileLocation(sheet.getWorkday()))) {
      fileWriter.write(output);
      fileWriter.flush();
    } catch (IOException ignored) {

    }
  }

  public String fileLocation(String date) {
    return defaultDirectoryPath()
      + File.separator
      + "Timetracked_"
      + date
      + ".json";
  }

  public static String defaultDirectoryPath() {
    return System.getProperty("user.home")
      + File.separator
      + "Timetracker";
  }

  public Path fileLocationUri(String date) {
    return Path.of(fileLocation(date));

  }

  public boolean localBackupExsist(String date) {
    return Files.exists(Paths.get(fileLocation(date)), LinkOption.NOFOLLOW_LINKS);
  }

  public List<String> backupFilesDates() {
    Path directoryPath = Paths.get(defaultDirectoryPath());
    List<String> dates = new ArrayList<>();
    try (Stream<Path> files = Files.list(directoryPath)) {
      dates = files
        .filter(Files::isRegularFile)
        .filter(path -> path.toString().toLowerCase().endsWith(".json"))
        .filter(path -> path.toString().contains("Timetracked_"))
        .map(path -> {
          int start = path.toString().indexOf("ked_") + 4;
          int end = path.toString().toLowerCase().indexOf(".json");
          return path.toString().substring(start, end);
        })
        .toList();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return dates;
  }

  public void processLocalBackupFile(List<LocalDateTime> timelist, String date)  {
    try {
      TimeSheet sheet = readLocalFile(date);
      sheet.getTimeStamps().forEach(timeStamp -> timelist.add(timeStamp.date()));
    } catch (IOException ignored) {}

  }
  public TimeSheet readLocalFile(String date) throws IOException{
    Gson gson = new GsonBuilder()
      .registerTypeAdapter(LocalDateTime.class, new LocalDateTypeAdapter())
      .create();
    Reader reader = new FileReader(this.fileLocation(date));
    TimeSheet sheet = gson.fromJson(reader, TimeSheet.class);
    reader.close();
    return sheet;
  }
}
