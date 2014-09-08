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
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;
import se.mbaeumer.fxmessagebox.MessageBox;
import se.mbaeumer.fxmessagebox.MessageBoxType;

import java.io.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

public class BarController {

    @FXML
    public TableColumn<Map, String> tAdded;
    @FXML
    public TableView<HashMap<String, String>> tableView;
    @FXML
    public MenuItem about;
    @FXML
    public TableColumn<Map, String> tDescription;
    @FXML
    public TextArea tArea;
    @FXML
    public Label curLevel;
    @FXML
    public Label totalXP;
    @FXML
    public MenuItem export;
    @FXML
    public MenuItem importData;
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
    private int addedValue = 1;
    private double totalAmountOfXp = 0.0;
    private double currPrBarValue = 0.0;
    private String lastDescription = "";
    private Stage primaryStage;

    private DBWrapper dbWrapper = DBWrapper.getInstance();

    /**
     * Init class, called on app start to pre-populate values
     */
    public void initializeValues(){

        //Search data in initialization
        ArrayList<LinkedHashMap<String, String>> data = dbWrapper.searchData("name", "leafchild");
        //If found update values
        if (data.size() != 0) {
            //Last result
            currentLevel = Integer.parseInt(data.get(0).get("currentLevel"));
            currLvlNeededXp = Double.parseDouble(data.get(0).get("currLvlNeededXp"));
            totalAmountOfXp = Double.parseDouble(data.get(0).get("totalAmountOfXp"));
            currPrBarValue = Double.parseDouble(data.get(0).get("currPrBarValue"));
        }
        //Initialize table
        populateTable(data);

        curLevel.setText(currentLevel + "");
        totalXP.setText(((int) totalAmountOfXp) + "");

        pBar.setProgress(currPrBarValue);
        pInd.setProgress(currPrBarValue);

        //menuBar.setPrefWidth(addButton.getScene().getWidth());
    }

    /**
     * Default method that will be calling on app start
     */
    @FXML
    public void initialize() {

        initializeValues();
    }

    /**
     * Handles button "Add" click
     * Add value into table abd progress bar
     */
    @FXML
    public void handleButtonAction() {

        String addValue = addField.getText();
        Double curr = pBar.getProgress();
        lastDescription = tArea.getText();
        if (checkAddValue(addValue) && !lastDescription.equals("")) {
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
                curLevel.setText(currentLevel + "");

                //Cleanup the table
                cleanupTable();
            }
            addedValue = (int) Double.parseDouble(addValue);
            pBar.setProgress(currPrBarValue);
            pInd.setProgress(currPrBarValue);

            //Empty
            addField.setText("");
            tArea.setText("");

            //Increase total
            totalAmountOfXp = totalAmountOfXp + Double.parseDouble(addValue);
            totalXP.setText(((int) totalAmountOfXp) + "");

            //Update table
            updateTable();
        }
        else{
            showNewOKMessage("Value should be a number and description should not be empty");
        }

    }

    /**
     * Verify if user value is correct number
     * @param addValue - input value from user
     * @return true if it's number
     */
    private boolean checkAddValue(String addValue) {

        if (addValue != null && !addValue.equals("")) {
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
    private double calculatePercent(double userInput) {

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

        showNewOKMessage("XP Bar, V 0.3\n Designed by Victor Malyshev\n mailto: vmalyshev0@gmail.com");
    }

    public void closeApp(ActionEvent actionEvent) {

        primaryStage = (Stage) addButton.getScene().getWindow();
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


    private void populateTable(ArrayList<LinkedHashMap<String, String>> tableData) {

        tAdded.setCellValueFactory(new MapValueFactory<String>("addedValue"));
        tDescription.setCellValueFactory(new MapValueFactory<String>("description"));

        //Show only for last level
        for(int i = 0; i < tableData.size(); i++) {
            LinkedHashMap<String, String> tempMap = tableData.get(i);
            if(!tempMap.get("currentLevel").equals(currentLevel + "")) tableData.remove(tempMap);
        }

        tableView.setItems(FXCollections.<HashMap<String, String>>observableArrayList(tableData));

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
                    });
                cell.setAlignment(Pos.CENTER);
                return cell;


            }
        };
        tAdded.setCellFactory(cellFactoryForMap);
        tDescription.setCellFactory(cellFactoryForMap);

    }

    private HashMap<String, Object> collectCurrentData() {

        HashMap<String, Object> latestData = new HashMap<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");


        latestData.put("name","leafchild");
        latestData.put("currentLevel", currentLevel);
        latestData.put("currLvlNeededXp", (int) currLvlNeededXp);
        latestData.put("createdDate", dateFormat.format(new Date()));
        latestData.put("totalAmountOfXp", (int) totalAmountOfXp);
        latestData.put("currPrBarValue", currPrBarValue);
        latestData.put("description", lastDescription);
        latestData.put("addedValue", addedValue);


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
            cleanupTable();
            initializeValues();
            showNewOKMessage("Progress was removed");
        }

    }


    private void updateTable() {

        final ObservableList items = tableView.getItems();
        //if( items == null || items.size() == 0) return;
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                items.add(items.size(), collectCurrentData());
            }
        });

    }

    private void cleanupTable() {

        final ObservableList<HashMap<String, String>> items = tableView.getItems();
        if( items == null || items.size() == 0) return;
            items.removeAll(items);
    }

    public void exportAllTheDatainFile(ActionEvent actionEvent) {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Saves Exported Data To");
        boolean isSaved;

        //Set to user directory or go to default if cannot access
        String userDirectoryString = System.getProperty("user.home");
        File userDirectory = new File(userDirectoryString);
        fileChooser.setInitialDirectory(userDirectory);
        // Set extension filter
        FileChooser.ExtensionFilter extFilter =
                new FileChooser.ExtensionFilter("JSON files (*.json)", "*.json");
        fileChooser.getExtensionFilters().add(extFilter);

        // Show save file dialog
        File file = fileChooser.showSaveDialog(primaryStage);
        if(file != null){
            isSaved = saveAllDataFromDB(file);

            if(isSaved) showNewOKMessage("All the data was exported");
        }
    }

    private boolean saveAllDataFromDB(File file) {

        FileWriter fileWriter;
        boolean result = true;
        try {
            //Get the data
            ArrayList<LinkedHashMap<String, String>> allData = dbWrapper.searchData("name", "leafchild");

            if(!file.getName().contains(".json")) file = new File(file.getAbsolutePath() + ".json");
            fileWriter = new FileWriter(file);
            fileWriter.write(allData.toString().replace("},", "},\n"));
            fileWriter.close();
        } catch (IOException ex) {
            showNewOKMessage("ERROR during saving the file");
            result = false;
        }

        return result;
    }

    public void importAllTheDatainFile(ActionEvent actionEvent) {


        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import from");
        boolean isRestored = false;

        //Set to user directory or go to default if cannot access
        String userDirectoryString = System.getProperty("user.home");
        File userDirectory = new File(userDirectoryString);
        fileChooser.setInitialDirectory(userDirectory);
        // Set extension filter
        FileChooser.ExtensionFilter extFilter =
                new FileChooser.ExtensionFilter("JSON files (*.json)", "*.json");
        fileChooser.getExtensionFilters().add(extFilter);

        // Show save file dialog
        File file = fileChooser.showOpenDialog(primaryStage);
        if(file != null){
            String answer = showNewQuestionMessage("Are you sure? Importing will remove all the old data");
            if(answer.equals("YES")){
                isRestored = importDataToDB(file);
            }


            if(isRestored) {
                showNewOKMessage("All the data was imported from " + file.getName());
                initializeValues();
            }
        }
    }

    private boolean importDataToDB(File file) {

        BufferedReader reader;
        boolean result = true;
        String tempLine;
        HashMap<String, Object> tempMap;

        try {

            //Then need cleanup the data from DB
            dbWrapper.removeUserData("name", "leafchild");
            //Get the data
            reader = new BufferedReader(new FileReader(file));
            while ((tempLine = reader.readLine()) != null) {

                //Need to create a HashMap with this data
                tempMap = new HashMap<>();

                if(tempLine.contains("[") || tempLine.contains("]")) tempLine = tempLine.replace("]", "").replace("[", "");
                if(tempLine.contains("{")) tempLine = tempLine.replace("{", "");
                if(tempLine.contains("},")) tempLine = tempLine.replace("}", "");
                String [] decription = tempLine.split(", description=");
                String[] mapData = decription[0].split(",");
                for (int i = 0; i < mapData.length; i++) {

                    String tempPair =  mapData[i];
                    String [] keyValue = tempPair.split("=");
                    tempMap.put(keyValue[0].trim(), keyValue[1]);

                }
                //Hooray
                String descriptionWithAdded = decription[1].substring(0, decription[1].length() - 1);
                String [] descriptionWithAddedArray = descriptionWithAdded.split("addedValue=");

                tempMap.put("description", descriptionWithAddedArray[0]);
                tempMap.put("addedValue", descriptionWithAddedArray[1]);
                //Exact 8 pairs
                if(tempMap.size() == 8) dbWrapper.insertData(tempMap);
            }
        } catch (IOException ex) {
            showNewOKMessage("ERROR during saving the file");
            result = false;
        }

        return result;
    }
}