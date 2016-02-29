package com.chariotinstruments.markets;

import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;

public class MainActivity extends AppCompatActivity implements ParseAccountData.ParseAccountDataAsyncListener, ParseOptionOrderPreview.ParseOptionOrderPreviewListener {

    TextView dataTextView;
    EditText symbolEditText;
    APIKeys apiKeys;
    TradeKingApiCalls tk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dataTextView = (TextView)findViewById(R.id.dataTextView);
        dataTextView.setMovementMethod(new ScrollingMovementMethod());
        symbolEditText = (EditText)findViewById(R.id.symbolEditText);
        apiKeys = new APIKeys();
        tk = new TradeKingApiCalls();

        //this potentially avoids the error in running network operations on the main thread.
        int SDK_INT = android.os.Build.VERSION.SDK_INT;
        if (SDK_INT > 8)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);
            //your codes here

        }
    }

    public void getOptionData(View v){

        new ParseOptionOrderPreview(this, this).execute();
    }

    public void getAccountData(View v){
        new ParseAccountData(this, this).execute();
    }

    public void getSymbolData(View v) throws JSONException {
        String symbol = symbolEditText.getText().toString().toUpperCase();
        if(symbol.trim().length() == 0){
            Toast toast = Toast.makeText(this, "Enter a symbol", Toast.LENGTH_SHORT);
            toast.show();
        }else {
            PhaseOneControl p1 = new PhaseOneControl(this, symbol);
            p1.start();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    public void onParseAccountDataComplete(AccountData aData){
        String output = "";

        output = output + "Total cash: ";
        output = output + Double.toString(aData.getAccountValue()) + "\n";
        output = output + "Cash Available: ";
        output = output + Double.toString(aData.getCashAvailable()) + "\n";
        output = output + "Unsettled Funds: ";
        output = output + Double.toString(aData.getUnsettledFunds()) +"\n";
        output = output + "Stocks: ";
        output = output + Double.toString(aData.getStockValue()) + "\n";
        output = output + "Options: ";
        output = output + Double.toString(aData.getOptionValue()) + "\n";
        output = output + "Uncleared Deposits: ";
        output = output + Double.toString(aData.getUnclearedDeposists());

        dataTextView.setText(output);
    }

    public void onParseOptionOrderPreviewComplete(String response){
        dataTextView.setText(response);
    }

}
