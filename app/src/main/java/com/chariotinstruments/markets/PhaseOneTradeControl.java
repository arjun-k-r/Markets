package com.chariotinstruments.markets;

import android.app.Activity;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by user on 3/12/16.
 * The flow of this class is:
 * 1. executeTrade() is called from phase one control.
 * 2. wait for the expirations and strike to return (onComplete methods)
 * 3. The preview order is created and executed in the onStrikeComplete method
 * 4. Check buying power in onOptionOrderPreviewComplete, execute real trade or paper trade here (set delta, limit).
 * 5. Start p2 in onOptionOrderComplete;
 */
public class PhaseOneTradeControl extends BaseControl implements ParseOptionStrikePrice.ParseOptionStrikePriceAsyncListener, ParseOptionExpirations.ParseOptionExpirationsAsyncListener, ParseOptionOrderPreview.ParseOptionOrderPreviewListener, ParseOptionOrder.ParseOptionOrderListener{
    private TextView consoleView;
    private EditText currentTextBox;

    private boolean isOpeningTrade;
    private boolean isCall;
    private ArrayList<Double> strikeList;
    private String formattedExpiration;
    private Activity uiActivity;
    private String symbol;
    private String expiration;
    private double strikePrice;
    private double curPrice;
    private double buyingPower;
    private double delta;
    private FixmlModel liveFixml;
    private boolean isLiveTrading;

    public PhaseOneTradeControl(boolean isOpeningTrade, boolean isCall, Activity activity, String symbol, double curPrice, double buyingPower, boolean isLive){
        super(activity);
        this.isOpeningTrade = isOpeningTrade;
        this.isCall = isCall;
        this.uiActivity = activity;
        this.symbol = symbol;
        this.curPrice = curPrice;
        this.buyingPower = buyingPower;
        this.consoleView = (TextView)activity.findViewById(R.id.dataTextView);
        this.isLiveTrading = isLive;
    }

    protected void executeTrade(){
        //get the exact expiration and strike we'll be using.
        new ParseOptionExpirations(uiActivity, this, symbol).execute();
        new ParseOptionStrikePrice(uiActivity, this, symbol, isCall, curPrice).execute();
    }

    public void onParseOptionExpirationsComplete(String expiration) {
        this.expiration = expiration;
    }

    public void onParseOptionStrikePriceComplete(double strikePriceIn){
        this.strikePrice = strikePriceIn;

        //now that we have strike and expiration, build the fixml.
        FixmlModel fixml = new FixmlModel(false);
        fixml = buildFixml();

        //execute the order preview to get the total cost, commissions, limit etc..before the real order.
        new ParseOptionOrderPreview(uiActivity, this, fixml).execute();
    }

    public void onParseOptionOrderPreviewComplete(OptionOrderPreview order){
        double totalCost = 0.0;
        FixmlModel fixml = order.getFixml();

        totalCost = order.getTotalCost();
        this.delta = order.getDelta();

        //add the limit price to the real order since we're submitting a real order, not market.
        fixml.setLimitPrice(order.getAskPrice());

        //make sure we have the funds.
        if(totalCost < buyingPower && isLiveTrading){
            //use the same fixml data from the preview for consistency.
            new ParseOptionOrder(uiActivity, this, fixml).execute();
        }
        //If paper trading, call the paper trading constructor on p2.
        if(!isLiveTrading){
            PhaseTwoControl p2 = new PhaseTwoControl(uiActivity, fixml);
            p2.setDelta(this.delta);
        }
    }

    public void onParseOptionOrderComplete(OptionOrder order){

        //Ensure the order was successful.
        if(!order.getIsException()) {
            PhaseTwoControl p2 = new PhaseTwoControl(uiActivity, order, true);
            p2.setDelta(this.delta);
        }else{
            //if there's a warning, set it to the console.
            consoleView.setText(order.getException());
        }
    }

    private FixmlModel buildFixml(){
        FixmlModel fixml = new FixmlModel(false);
        int orderSide = -1;
        String posEffect = "";
        String CFI = "";

        if(isOpeningTrade){
            orderSide = 1;
            posEffect = "O";
        }else{
            orderSide = 2;
            posEffect = "C";
        }

        if(isCall){
            CFI = "OC";
        }else{
            CFI = "OP";
        }

        //TODO:setting the qty to 1 for now so I don't lose it all on the first bet.
        fixml.createFIXMLObject(false, orderSide, posEffect, CFI, "OPT", this.expiration, this.strikePrice, this.symbol, super.getQuantity(), 0.0);


        return fixml;
    }


}
