package ru.varren;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SaveBean {
    List<String> screenshots = new ArrayList<>();
    String name;
    Long quickSaveDatetime;
    Long autoSaveDatetime;
    String path;

    String quickSaveZone;
    String autoSaveZone;

    public SaveBean(Path p) {
        name = p.getFileName().toString();
        quickSaveDatetime = new File (p.toString() + File.separator+ Service.QUICK_SAVE_FILENAME).lastModified();
        autoSaveDatetime = new File (p.toString() + File.separator+ Service.AUTO_SAVE_FILENAME).lastModified();

        for(int i = 0;i<5;i++)
            screenshots.add(p.toString() + File.separator + "screenshot"+i + ".png");

        path = p.toString();

        quickSaveZone = calculateZone(p, Service.QUICK_SAVE_FILENAME);
        autoSaveZone = calculateZone(p, Service.AUTO_SAVE_FILENAME);
    }

    private String calculateZone(Path path, String file) {

        String rx = "(\\d+_\\w+)";
        Pattern p = Pattern.compile(rx);

        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(path.toFile() + File.separator + file));
            String line = reader.readLine();
            while (line != null) {
                line = reader.readLine();
                Matcher matcher = p.matcher(line);
                if (matcher.find()) {
                    String result = matcher.group(1);

                    return result;
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }

    public long lastSaveTime(){
        return quickSaveDatetime > autoSaveDatetime ? quickSaveDatetime: autoSaveDatetime;
    }
    @Override
    public String toString() {
        return name;
    }
}
