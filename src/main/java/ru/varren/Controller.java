package ru.varren;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ResourceBundle;

public class Controller  implements Initializable {
    @FXML
    private ListView<SaveBean> listView;

    @FXML
    private Button load;

    @FXML
    private Button restart;

    @FXML private Label filePath;

    @FXML
    protected void handleLoadButtonAction(ActionEvent event) {
        service.load(listView.getSelectionModel().getSelectedItem());
    }

    @FXML
    protected void handleRestartButtonAction(ActionEvent event) {
        service.restart(listView.getSelectionModel().getSelectedItem());
    }
    @FXML
    protected void handleChangePathButtonAction(ActionEvent event) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDirectory = directoryChooser.showDialog(((Node)event.getTarget()).getScene().getWindow());
        Service.instance().setSavePath(selectedDirectory.toPath());
        filePath.textProperty().set(Service.instance().getStoreFilePath());
    }

    private Service service = Service.instance();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        filePath.textProperty().set(Service.instance().getStoreFilePath());
        listView.setItems(service.getAllSaves());
        listView.setCellFactory(param ->new ListCell<SaveBean>() {
            private HBox images = new HBox();
            private int baseH = 1080;
            private int baseW = 1920;

            private void setImage(String path, int id){
                ImageView imageView = (ImageView) images.getChildren().get(id);
                File screenshot = new File(path);

                if (screenshot.exists()) {
                    try {
                        imageView.setImage(new Image(screenshot.toURI().toURL().toString()));
                        imageView.onMouseEnteredProperty().addListener(o->{
                            imageView.setFitHeight(baseH);
                            imageView.setFitWidth(baseW);
                        });
                        imageView.onMouseExitedProperty().addListener(o->{
                            imageView.setFitHeight(baseH/7);
                            imageView.setFitWidth(baseW/7);
                        });
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                }else{
                    imageView.setImage(null);
                }
            }

            @Override
            public void updateItem(SaveBean save, boolean empty) {
                super.updateItem(save, empty);
                // init if needed
                if (images.getChildren().size() == 0){
                    for (int i = 0;i < 5;i++){
                        ImageView image = new ImageView();
                        images.getChildren().add(image);
                        image.setFitHeight(baseH/7);
                        image.setFitWidth(baseW/7);
                    }
                }

                if (empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    for (int i = 0 ; i<5; i++)
                        setImage(save.screenshots.get(i), i);
                    String text = save.name +
                            "\nAutosave: " + save.autoSaveZone +
                            "\n"+ simpleDateFormat.format(save.autoSaveDatetime)+
                            "\nQuicksave: " + save.quickSaveZone +
                            "\n"+ simpleDateFormat.format(save.quickSaveDatetime);
                    setText(text);
                    setGraphic(images);
                }
            }
            String pattern = "yyyy.MM.dd HH:mm:ss";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

        });
    }


}
