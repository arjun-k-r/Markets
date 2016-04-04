package com.chariotinstruments.markets;

import android.app.Activity;
import android.widget.EditText;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by user on 3/15/16.
 */
public class PhaseTwoControl implements ParseOpenPosition.ParseOpenPositionAsyncListener{

    private OpenOptionPosition position;
    private OptionOrder order;
    private Activity uiActivity;
    private EditText curData;
    private TextView console;
    private double delta;
    private boolean isActive;

    //this constructor is for new positions that are found during analysis.
    public PhaseTwoControl(Activity activity, OptionOrder orderIn){
        uiActivity = activity;
        this.order = orderIn;
        this.console = (TextView) uiActivity.findViewById(R.id.dataTextView);
        this.curData = (EditText) uiActivity.findViewById(R.id.currentTextBox);
        this.isActive = false;

        //get the recently opened order:
        new ParseOpenPosition(uiActivity, this, "SPY").execute();
    }

    //This constructor is used for when a trade already existed when the app was first opened/started in p1.
    public PhaseTwoControl(Activity activity, OpenOptionPosition positionIn){
        uiActivity = activity;
        this.console = (TextView) uiActivity.findViewById(R.id.dataTextView);
        this.curData = (EditText) uiActivity.findViewById(R.id.currentTextBox);
        this.position = positionIn;
        this.isActive = false; //don't start the loop yet.

        outputPositionUI();
    }

    public void setDelta(double deltaIn){
        delta = deltaIn;
    }

    public void start(){
        isActive = true;
    }

    public void stop(){
        isActive = false;
    }

    //region process

    private void openPositionLoop(){
        if(isActive){
            new ParseOpenPosition(uiActivity, this, "SPY").execute();
        }
    }

    private void checkGainLoss(double gainLoss){
        if(gainLoss > 3 || gainLoss < -4){
            isActive = false;
            PhaseTwoTradeControl p2 = new PhaseTwoTradeControl(uiActivity);
            p2.executeClosingTrade(position);
        }
    }

    //end region

    public void onParseOpenPositionComplete(OpenOptionPosition positionIn){
        this.position = positionIn;

        //pass gainloss to checker.
        checkGainLoss(positionIn.getGainLoss());

        //push to UI.
        outputPositionUI();

        //Call open position loop
        openPositionLoop();
    }

    public void outputPositionUI(){
        String output = "";
        String currentOutput = "";
        String formattedExpiry = "";

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        try {
            Date date = sdf.parse(position.getExpiryDate());
            formattedExpiry = sdf.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }


        output = output + "CFI: " + position.getCFI() + "\n";
        output = output + "Cost Basis: " + position.getCostBasis() + "\n";
        output = output + "Last Price: " + position.getLastPrice() + "\n";
        output = output + "Expiry: " + position.getExpiryDate() + "\n";
        output = output + "PutCall: " + position.getPutOrCall() + "\n";
        output = output + "Quantity: " + position.getQuantity() + "\n";
        output = output + "Sec Type: " + position.getSecType() + "\n";
        output = output + "Strike: " + position.getStrikePrice() + "\n";
        output = output + "symbol: " + position.getSymbol() + "\n";
        output = output + "Gain/Loss: " + position.getGainLoss();

        currentOutput = currentOutput + position.getQuantity() + " ";
        currentOutput = currentOutput + position.getSymbol() + ", ";
        currentOutput = currentOutput + formattedExpiry + ", ";
        currentOutput = currentOutput + position.getStrikePrice() + ", ";
        currentOutput = currentOutput + position.getCFI() + ", ";
        currentOutput = currentOutput + position.getGainLoss() + ", ";
        currentOutput = currentOutput + delta + " ";


        this.console.setText(output);
        this.curData.setText(currentOutput);

    }
}
