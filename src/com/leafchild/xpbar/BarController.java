package com.leafchild.xpbar;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import se.mbaeumer.fxmessagebox.MessageBox;
import se.mbaeumer.fxmessagebox.MessageBoxType;

import java.text.DecimalFormat;
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

    public void initApp(){

        lvlValue.setText(currentLevel + "");
        toTheNextLvlv.setText(((int) currLvlNeededXp) + "");
    }

    @FXML
    protected void initialize() {
        initApp();
    }


    @FXML
    public void handleButtonAction() {

        String addValue = addField.getText();
        Double curr = pBar.getProgress();
        if (checkAddValue(addValue)) {
            double nextCurrent = curr + calculatePersent(Double.parseDouble(addValue));
            if(nextCurrent >= 1.0) {
                showNewOKMessage("New Level!!!");
                //calculate how much points last adding will cost
                //increase level by 1
                pBar.setProgress(0.0);
                pInd.setProgress(0.0);
                currLvlNeededXp = currLvlNeededXp + currLvlNeededXp*0.4;
                currentLevel = currentLevel + 1;
                nextCurrent = calculatePersent((nextCurrent - 1.0)*100);
                //increase level points
                initApp();
            }
            pBar.setProgress(nextCurrent);
            pInd.setProgress(nextCurrent);

            //Empty
            addField.setText("");
        }
        else{
            showNewOKMessage("Value should be a number");
        }

    }

    private boolean checkAddValue(String addValue){

        //TODO: if != null
        String numberPattern = "^[0-9]*$";
        return Pattern.matches(numberPattern, addValue);
    }

    private void showNewOKMessage(String message) {

        MessageBox mb = new MessageBox(message, MessageBoxType.OK_ONLY);
        mb.showAndWait();
    }

    private double calculatePersent(double userInput){

        double result;
        result = userInput/currLvlNeededXp;
        DecimalFormat df = new DecimalFormat("#.##");
        result = Double.valueOf(df.format(result));

        return result;
    }

}