package com.leafchild.xpbar;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.MapValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;
import se.mbaeumer.fxmessagebox.MessageBox;
import se.mbaeumer.fxmessagebox.MessageBoxType;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class BarController {

    @FXML
    public TableColumn<Map, String> tnLevel;
    @FXML
    public TableColumn<Map, String> tTotal;
    @FXML
    public TableColumn<Map, String> tLevel;
    @FXML
    public TableView<HashMap<String, String>> tableView;
    @FXML
    public MenuItem about;
    @FXML
    private TextField addField;
    @FXML
    private Button addButton;
    @FXML
    private ProgressBar pBar;
    @FXML
    private ProgressIndicator pInd;

    private double currLvlNeededXp = 100.0;
    private int currentLevel = 1;
    private double totalAmountOfXp = 0.0;
    private double currPrBarValue = 0.0;

    private DBWrapper dbWrapper = DBWrapper.getInstance();

    /**
     * Init class, called on app start to prepopulate values
     */
    public void initializeValues(){

        //Search data in initialization
        HashMap<String, String> data = dbWrapper.searchData("name", "leafchild");
        //If found update values
        if (data.size() != 0) {
            currentLevel = Integer.parseInt(data.get("currentLevel"));
            currLvlNeededXp = Double.parseDouble(data.get("currLvlNeededXp"));
            totalAmountOfXp = Double.parseDouble(data.get("totalAmountOfXp"));
            currPrBarValue = Double.parseDouble(data.get("currPrBarValue"));
        }
        pBar.setProgress(currPrBarValue);
        pInd.setProgress(currPrBarValue);
    }

    /**
     * Default method that will be calling on app start
     */
    @FXML
    protected void initialize() {

        initializeValues();
        populateTable();
    }

    /**
     * Handles button "Add" click
     * Add value into table abd progress bar
     */
    @FXML
    public void handleButtonAction() {

        String addValue = addField.getText();
        Double curr = pBar.getProgress();
        if (checkAddValue(addValue)) {
            currPrBarValue = curr + calculatePercent(Double.parseDouble(addValue));
            if(currPrBarValue >= 1.0) {
                showNewOKMessage("New Level!!!");
                //calculate how much points last adding will cost
                //increase level by 1
                pBar.setProgress(0.0);
                pInd.setProgress(0.0);
                currLvlNeededXp = currLvlNeededXp + currLvlNeededXp*0.4;
                currentLevel = currentLevel + 1;
                currPrBarValue = calculatePercent((currPrBarValue - 1.0) * 100);
            }
            pBar.setProgress(currPrBarValue);
            pInd.setProgress(currPrBarValue);

            //Empty
            addField.setText("");

            //Increase total
            totalAmountOfXp = totalAmountOfXp + Double.parseDouble(addValue);

            //Update table
            updateTable();
        }
        else{
            showNewOKMessage("Value should be a number");
        }

    }

    /**
     * Verify if user value is correct number
     * @param addValue - input value from user
     * @return true if it's number
     */
    private boolean checkAddValue(String addValue){

        if (addValue != null) {
            String numberPattern = "^[0-9]*$";
            return Pattern.matches(numberPattern, addValue);
        } else return false;
    }

    private void showNewOKMessage(String message) {

        MessageBox mb = new MessageBox(message, MessageBoxType.OK_ONLY);
        mb.showAndWait();
    }
    private String showNewQuestionMessage(String message) {

        MessageBox mb = new MessageBox(message, MessageBoxType.YES_NO);
        mb.showAndWait();
        return mb.getMessageBoxResult().name();
    }

    /**
     * Calculates percent depends on current level
     * @param userInput - user input
     * @return calculated percent
     */
    private double calculatePercent(double userInput){

        double result;
        result = userInput/currLvlNeededXp;
        DecimalFormat df = new DecimalFormat("#.##");
        result = Double.valueOf(df.format(result));

        return result;
    }

    /**
     * Saves progress when user clicks on Save in menu
     */
    @FXML
    public void saveProgress() {

        dbWrapper.insertData(collectCurrentData());
        showNewOKMessage("Progress was saved");
    }

    @FXML
    public void aboutApp() {

        showNewOKMessage("XP Bar, V 0.2\n Designed by Victor Malyshev\n mailto: vmalyshev0@gmail.com");
    }

    public void closeApp(ActionEvent actionEvent) {

        Stage primaryStage = (Stage) addButton.getScene().getWindow();
        MenuItem menuItem = (MenuItem) actionEvent.getTarget();
        if (menuItem.getText().equals("Close")){

            String answer = showNewQuestionMessage("Do you wanna save the progress?");
            // do what you have to do
            if(answer.equals("YES")){
                saveProgress();
            }
            primaryStage.close();
        }
    }


    private void populateTable() {

        tLevel.setCellValueFactory(new MapValueFactory<String>("currentLevel"));
        tLevel.setMinWidth(30);
        tnLevel.setCellValueFactory(new MapValueFactory<String>("currLvlNeededXp"));
        tnLevel.setMinWidth(30);
        tTotal.setCellValueFactory(new MapValueFactory<String>("totalAmountOfXp"));
        tTotal.setMinWidth(30);
        tableView.setItems(FXCollections.observableArrayList(collectCurrentData()));

        tableView.setEditable(false);
        tableView.getSelectionModel().setCellSelectionEnabled(true);

        Callback<TableColumn<Map, String>, TableCell<Map, String>>
                cellFactoryForMap = new Callback<TableColumn<Map, String>,
                TableCell<Map, String>>() {
            @Override
            public TableCell call(TableColumn p) {
                TextFieldTableCell cell = new TextFieldTableCell(new StringConverter() {
                    @Override
                    public String toString(Object t) {
                        return t.toString();
                    }
                    @Override
                    public Object fromString(String string) {
                        return string;
                    }
                    //cell.setAlignment(Pos.CENTER);
                    });
                cell.setAlignment(Pos.CENTER);
                return cell;


            }
        };
        tnLevel.setCellFactory(cellFactoryForMap);
        tLevel.setCellFactory(cellFactoryForMap);
        tTotal.setCellFactory(cellFactoryForMap);

    }

    private HashMap<String, String> collectCurrentData(){

        HashMap<String, String> latestData = new HashMap<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");


        latestData.put("name","leafchild");
        latestData.put("currentLevel", currentLevel + "");
        latestData.put("currLvlNeededXp", currLvlNeededXp + "");
        latestData.put("createdDate",dateFormat.format(new Date()));
        latestData.put("totalAmountOfXp", totalAmountOfXp + "");
        latestData.put("currPrBarValue", currPrBarValue + "");


        return latestData;
    }

    @FXML
    public void deleteCurrentData() {

        String sure = showNewQuestionMessage("Are you really want to remove all the data?");

        if (sure.equals("YES")) {
            dbWrapper.removeUserData("name", "leafchild");
            currLvlNeededXp = 100;
            currentLevel = 1;
            currPrBarValue = 0;
            totalAmountOfXp = 0;
            initializeValues();
            updateTable();
            showNewOKMessage("Progress was removed");
        }

    }


    private void updateTable(){

        final ObservableList<HashMap<String, String>> items = tableView.getItems();
        if( items == null || items.size() == 0) return;

        //final HashMap<String, String> item = tableView.getItems().get(0);
        items.remove(0);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                items.add(0, collectCurrentData());
            }
        });

    }
}