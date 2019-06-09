package com.example.erlend.quizzapp.customView;


import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.erlend.quizz_fra_dekompilert.R;
import com.example.erlend.quizzapp.util.Callbackmethod;

public class StatRow extends RelativeLayout{

    private TextView txtKey;
    private TextView txtValue;
    private Callbackmethod callbackInitViewDone;

    public StatRow(Context context, AttributeSet attrs) {
        super(context, attrs);

        //https://www.javacodegeeks.com/2015/11/java-8-streams-api-grouping-partitioning-stream.html
        initRefUI(context);
        initArgs(context, attrs);
    }

    public StatRow(Context context, String key, String value) {
        super(context);
        initRefUI(context);
        txtKey.setText(key);
        txtValue.setText(value);
    }

    public StatRow(Context context) {
        super(context);
        initRefUI(context);
    }

    private void initRefUI(Context context){
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutInflater.inflate(R.layout.stat_row, this, true);

        txtKey = findViewById(R.id.stat_row_key);
        txtValue = findViewById(R.id.stat_row_value);
        if(callbackInitViewDone != null)
            callbackInitViewDone.callback(null);
    }

    private void initArgs(Context context, AttributeSet attrs){
        TypedArray argsArray = context.obtainStyledAttributes(attrs, R.styleable.OptionsStatRow);

        String strKey = argsArray.getString(R.styleable.OptionsStatRow_rowName);
        String strValue = argsArray.getString(R.styleable.OptionsStatRow_rowValue);

        txtKey.setText(strKey);
        txtValue.setText(strValue);
    }

    public void onInitViewDone(Callbackmethod callback){
        this.callbackInitViewDone = callback;
    }

    public void setRowValue(String value){
        txtValue.setText(value);
    }

    public void setRowValue(int value){
        txtValue.setText(String.format("%s ",value));
    }

    public void setRowKey(String key){
        txtKey.setText(key);
    }




}
