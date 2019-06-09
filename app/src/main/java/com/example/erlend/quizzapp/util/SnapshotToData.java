package com.example.erlend.quizzapp.util;

import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.Iterator;

/**A helpter class for retrieving various data from a DataSnapshot**/
public class SnapshotToData {

    private DataSnapshot dataSnapshot;

    public SnapshotToData(DataSnapshot dataSnapshot) {
        this.dataSnapshot = dataSnapshot;
    }

    public String getStringFromKey(String key){
        return dataSnapshot.child(key).getValue().toString();
    }

    public int getIntFromKey(String key){
        return Integer.parseInt(dataSnapshot.child(key).getValue().toString());
    }

    public float getFloatFromKey(String key){
        return Float.parseFloat(dataSnapshot.child(key).getValue().toString());
    }

    public <T> ArrayList<T> getArrayOfValuesFromKey(String key){
        ArrayList<T> list = new ArrayList<>();
        Iterator<DataSnapshot> iter = dataSnapshot.child(key).getChildren().iterator();
        while (iter.hasNext()){
            list.add((T) iter.next().getValue());
        }
        return list;
    }

    public boolean getBooleanFromKey(String key){
        return (boolean) dataSnapshot.child(key).getValue();
    }

}