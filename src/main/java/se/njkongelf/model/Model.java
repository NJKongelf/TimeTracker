package se.njkongelf.model;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.control.TextField;
import javafx.util.converter.LongStringConverter;
import se.njkongelf.controller.Controller;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicLong;

public class Model {
    private Controller controller;
    private Properties properties;

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public void setController(Controller controller) {
        this.controller = controller;
    }


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
            try (FileWriter fileWriter = new FileWriter(System.getProperty("user.home") + File.separator + "Timetracker" + File.separator + "Timetracked_"
                    + LocalDateTime.now().format(DateTimeFormatter.ofPattern("YYYY-MM-dd")) + ".ttb")) {
                for (LocalDateTime s : timelist) {
                    fileWriter.write(s.toEpochSecond(ZoneOffset.UTC) + "\n");
                }
                fileWriter.flush();
                fileWriter.close();
            }
        }
    }

    public void printToFile(List<LocalDateTime> timelist, AtomicLong calculatedTime) throws IOException {
        if (timelist.size() > 0) {
            FileWriter fileWriter = new FileWriter("Timetracked_"
                    + LocalDateTime.now().format(DateTimeFormatter.ofPattern("YYYY-MM-dd")) + ".txt");
            for (LocalDateTime s : timelist) {
                fileWriter.write(s.format(DateTimeFormatter.ofPattern("HH:mm:ss")) + "\n");
            }
            fileWriter.write("Tracked time: " + LocalDateTime.ofEpochSecond(calculatedTime.get(), 0, ZoneOffset.UTC)
                    .format(DateTimeFormatter.ofPattern("HH:mm:ss")));
            fileWriter.flush();
            fileWriter.close();
        }
    }

    public void readBackupFile(List<LocalDateTime> timelist,
                               ObservableList<String> listview,
                               AtomicLong calculatedTime,
                               TextField trackedTime) throws IOException {
        String file = System.getProperty("user.home")
                + File.separator
                + "Timetracker"
                + File.separator
                + "Timetracked_"
                + LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("YYYY-MM-dd"))
                + ".ttb";
        if (Files.exists(Paths.get(file), LinkOption.NOFOLLOW_LINKS)) {
            FileReader stream = new FileReader(file);
            BufferedReader streamReader = new BufferedReader(stream);
            List<String> stringList = streamReader.lines().toList();
            stringList.stream().forEach(s -> {
                LongStringConverter longStringConverter = new LongStringConverter();
                timelist.add(LocalDateTime
                        .ofEpochSecond(longStringConverter.fromString(s), 0, ZoneOffset.UTC));
            });
            timelist.stream().forEach(time -> {
                listview.add(time
                        .format(DateTimeFormatter
                                .ofPattern("HH:mm:ss YYYY-MM-dd")));
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
                                .ofPattern("HH:mm:ss")));
            } else {
                time = calcUnEvenList(timelist, time);
                calculatedTime.set(time);
                trackedTime.setText(LocalDateTime
                        .ofEpochSecond(time, 0, ZoneOffset.UTC)
                        .format(DateTimeFormatter
                                .ofPattern("HH:mm:ss")));
            }
        }
    }
    public ChangeListener<Integer> spinngerListner(){
        return new ChangeListener<Integer>() {
            @Override
            public void changed(ObservableValue<? extends Integer> observableValue, Integer integer, Integer t1) {
                properties.setProperty("workinghours",observableValue.getValue().toString());
            }
        };
    }

    public Properties readInSettingsFile(String path){
        Properties prop = new Properties();
        try {
            prop.load(new FileInputStream(path));
        } catch (FileNotFoundException e) {
//            throw new RuntimeException(e);
        } catch (IOException e) {
//            throw new RuntimeException(e);
        }
        return prop;
    }
    public void saveSettings(String path){
        try {
            FileOutputStream fos = new FileOutputStream(path);
            properties.store(fos,"Saving settings");
            fos.flush();
            fos.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
