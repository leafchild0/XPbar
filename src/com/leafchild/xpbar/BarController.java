package com.leafchild.xpbar;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import se.mbaeumer.fxmessagebox.MessageBox;
import se.mbaeumer.fxmessagebox.MessageBoxType;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Pattern;


public class BarController {

    @FXML
    public Label lvlValue;
    @FXML
    public Label toTheNextLvlv;
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
        lvlValue.setText(currentLevel + "");
        toTheNextLvlv.setText(((int) currLvlNeededXp) + "");
        pBar.setProgress(currPrBarValue);
        pInd.setProgress(currPrBarValue);
    }

    @FXML
    protected void initialize() {
        initializeValues();
    }


    @FXML
    public void handleButtonAction() {

        String addValue = addField.getText();
        Double curr = pBar.getProgress();
        if (checkAddValue(addValue)) {
            currPrBarValue = curr + calculatePersent(Double.parseDouble(addValue));
            if(currPrBarValue >= 1.0) {
                showNewOKMessage("New Level!!!");
                //calculate how much points last adding will cost
                //increase level by 1
                pBar.setProgress(0.0);
                pInd.setProgress(0.0);
                currLvlNeededXp = currLvlNeededXp + currLvlNeededXp*0.4;
                currentLevel = currentLevel + 1;
                currPrBarValue = calculatePersent((currPrBarValue - 1.0)*100);
                //increase level points
                lvlValue.setText(currentLevel + "");
                toTheNextLvlv.setText(((int) currLvlNeededXp) + "");
            }
            pBar.setProgress(currPrBarValue);
            pInd.setProgress(currPrBarValue);

            //Empty
            addField.setText("");
        }
        else{
            showNewOKMessage("Value should be a number");
        }

    }

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

    private double calculatePersent(double userInput){

        double result;
        result = userInput/currLvlNeededXp;
        DecimalFormat df = new DecimalFormat("#.##");
        result = Double.valueOf(df.format(result));

        return result;
    }

    @FXML
    public void saveProgress() {

        dbWrapper.insertData(collectCurrentData());
        showNewOKMessage("Progress was saved");
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

        dbWrapper.removeUserData("name", "leafchild");
        currLvlNeededXp = 100;
        currentLevel = 1;
        currPrBarValue = 0;
        initializeValues();
        showNewOKMessage("Progress was removed");
    }
}