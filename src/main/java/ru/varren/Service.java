package ru.varren;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class Service {
    public static String PATH_METRO_SAVES_BASE;
    public static String PATH_METRO_SAVES_CUSTOM;

    public static String USER_CONF_FILENAME = "user.cfg";
    public static String AUTO_SAVE_FILENAME = "m3_auto_save";
    public static String QUICK_SAVE_FILENAME = "m3_quick_save";

    private static String pattern = "YY-MM-dd_HH-mm-ss";
    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

    private static final Service instance = new Service();
    private ObservableList<SaveBean> allSaves = FXCollections.observableArrayList();

    private long latestSave = 0;

    private Properties prop = new Properties();
    /***************************************************************************************
     PUBLIC
     ****************************************************************************************/

    public ObservableList<SaveBean> getAllSaves() {
        return allSaves;
    }

    public void save() {
        long currSaveTime = getLastModifiedSaveTime();
        if (currSaveTime > getLastModifiedStoreTime()) {
            String date = simpleDateFormat.format(new Date());
            String fromDir = PATH_METRO_SAVES_BASE;
            String toDir = PATH_METRO_SAVES_CUSTOM + "Save_" + date + File.separator;

            new File(toDir).mkdirs();
            copy(fromDir, toDir);
            makeScreenshot(toDir);
            allSaves.add(new SaveBean(Paths.get(toDir)));
            setLastModifiedStoreTime(currSaveTime);
        }
    }

    public void load(SaveBean save) {
        if (save != null && save.path != null) {
            String toDir = PATH_METRO_SAVES_BASE;
            String fromDir = save.path + File.separator;
            copy(fromDir, toDir);
            System.out.println("Loaded");
        }
    }

    public void restart(SaveBean save) {
        if (save != null && save.path != null) {
            try {
                Runtime.getRuntime().exec("TASKKILL /F /IM MetroExodus.exe");

                load(save);
                Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler com.epicgames.launcher://apps/Snapdragon?action=launch&silent=true");

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /***************************************************************************************
     PRIVATE
     ****************************************************************************************/
    private void copy(String fromDir, String toDir) {
        try {
            // Files.copy(Paths.get(fromDir + USER_CONF_FILENAME),
            //         Paths.get(toDir + USER_CONF_FILENAME));


            Files.copy(Paths.get(fromDir + AUTO_SAVE_FILENAME),
                    Paths.get(toDir + AUTO_SAVE_FILENAME), StandardCopyOption.REPLACE_EXISTING);

            Files.copy(Paths.get(fromDir + QUICK_SAVE_FILENAME),
                    Paths.get(toDir + QUICK_SAVE_FILENAME), StandardCopyOption.REPLACE_EXISTING);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private Service() {
        init();
    }
    private void init(){
        initPathsInfo();
        initSavesInfo();
    }

    private void initSavesInfo() {
        try {
            Path dir = Paths.get(PATH_METRO_SAVES_CUSTOM);

            List<SaveBean> saves = Files.list(dir)
                    .filter(f -> Files.isDirectory(f))
                    .map(SaveBean::new).collect(Collectors.toList());
            allSaves.clear();
            allSaves.addAll(saves);


            Optional<SaveBean> latest = allSaves.stream().max(Comparator.comparingLong(SaveBean::lastSaveTime));
            latestSave = latest.map(SaveBean::lastSaveTime).orElse(0L);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    private void initPathsInfo() {
        String metroSavesPathStr = System.getProperty("user.home") + File.separator + "Saved Games" + File.separator + "metro exodus";
        Path metroSavesPath = Paths.get(metroSavesPathStr);

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(metroSavesPath)) {
            for (Path entry : stream) {
                // i believe there should be only 1 entry, so dont bother with checks
                if (entry.toFile().isDirectory() && new File(entry.toString() + File.separator + USER_CONF_FILENAME).exists()) {
                    PATH_METRO_SAVES_BASE = entry.toString() + File.separator;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        PATH_METRO_SAVES_CUSTOM = PATH_METRO_SAVES_BASE;

        loadConfig();

    }
    private void loadConfig(){
        String fileName = "./settings.config";
        InputStream is = null;
        Path file = Paths.get(fileName);
        if (file.toFile().exists()) {
            System.out.println(file.toString());
            try {
                is = new FileInputStream(fileName);
                prop.load(is);
                if (prop.getProperty("path") != null)
                    PATH_METRO_SAVES_CUSTOM =  prop.getProperty("path");
            } catch (IOException ex) {
                ex.printStackTrace();
            } finally {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public void setSavePath(Path path){
        prop.setProperty("path", path.toString());
        String fileName = "settings.config";
        OutputStream os = null;
        try {
            os = new FileOutputStream(fileName);
            prop.store(os, "");
            init();
        } catch (IOException ex) {
            ex.printStackTrace();
        }finally {
            try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public static Service instance() {
        return instance;
    }

    private long getLastModifiedSaveTime() {
        long lastModQ = Paths.get(PATH_METRO_SAVES_BASE + QUICK_SAVE_FILENAME).toFile().lastModified();
        long lastModA = Paths.get(PATH_METRO_SAVES_BASE + AUTO_SAVE_FILENAME).toFile().lastModified();
        return lastModQ > lastModA ? lastModQ : lastModA;
    }

    private long getLastModifiedStoreTime() {
        return latestSave;
    }


    private void setLastModifiedStoreTime(long latestSave) {
        this.latestSave = latestSave;
    }

    private void makeScreenshot(String path) {
        //will make the screenshot of the primary screen during save
        makeScreenshot(path,false, ""); // save full screen
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            private int id = 0;
            @Override
            public void run() {
                makeScreenshot(path,true, String.valueOf(id));
                id++;
                if (id>=5) timer.cancel();
            }
        }, 0, 5000);
    }
    private void makeScreenshot(String path, boolean resize, String id) {
        try {
            Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
            BufferedImage capture = new Robot().createScreenCapture(screenRect);
            if (resize) capture = resizeImage(capture);
            ImageIO.write(capture, "png", new File(path + File.separator + "screenshot" + id + ".png"));

        } catch (AWTException | IOException e) {
            e.printStackTrace();
        }
    }

    private static BufferedImage resizeImage(BufferedImage originalImage){
        int type =  originalImage.getType();
        BufferedImage resizedImage = new BufferedImage(originalImage.getWidth()/6, originalImage.getHeight()/7, type);
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(originalImage, 0, 0, originalImage.getWidth()/6, originalImage.getHeight()/7, null);
        g.dispose();

        return resizedImage;
    }

    public String getStoreFilePath() {
        return PATH_METRO_SAVES_CUSTOM;
    }
}
