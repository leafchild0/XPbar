package com.leafchild.xpbar;

import com.mongodb.*;

import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by: vmalyshev
 * Project: XPbar
 * Date: 09-May-14
 * Time: 16:39
 */
public class DBWrapper {

    private static DBWrapper db = null;
    private static DBCollection table = null;

    private DBWrapper(){}

    protected static DBWrapper getInstance(){

        if(db == null){
            db = new DBWrapper();
            db.initializeConnection();
        }
        return db;
    }

    private void initializeConnection() {

        MongoClient mongo;
        try {
            mongo = new MongoClient("localhost", 27017);
            DB db = mongo.getDB("xpBar");
            table = db.getCollection("user");
        } catch(UnknownHostException e) {
            System.out.println("Exception: " + e);
        }
    }

    protected void insertData(HashMap<String, String> data){

        BasicDBObject newData = new BasicDBObject();

        for (String key: data.keySet()){
            newData.put(key, data.get(key));
        }

        table.insert(newData);
    }

    protected HashMap<String, String> searchData(String key, String value){

        HashMap<String, String> searchResults = new HashMap<>();
        BasicDBObject orderBy = new BasicDBObject("createdDate", -1);

        BasicDBObject searchQuery = new BasicDBObject();
        searchQuery.put(key, value);
        //Find values, sort them, and get only last modified data
        DBCursor cursor = table.find(searchQuery).sort(orderBy).limit(1);
        while (cursor.hasNext()) {
            DBObject tempResult = cursor.next();
            String createdDate = (String) tempResult.get("createdDate");
            String name = (String) tempResult.get("name");
            String level = (String) tempResult.get("currentLevel");
            String totalAmountOfXp = (String) tempResult.get("totalAmountOfXp");
            String currLvlNeededXp = (String) tempResult.get("currLvlNeededXp");
            String currPrBarValue = (String) tempResult.get("currPrBarValue");

            //Put them into map
            searchResults.put("name", name);
            searchResults.put("currentLevel", level + "");
            searchResults.put("createdDate", createdDate);
            searchResults.put("totalAmountOfXp", totalAmountOfXp);
            searchResults.put("currLvlNeededXp", currLvlNeededXp);
            searchResults.put("currPrBarValue", currPrBarValue);
        }

        return searchResults;
    }

    protected void removeUserData(String key, String value){

        BasicDBObject searchQuery = new BasicDBObject();
        searchQuery.put(key, value);
        table.remove(searchQuery);

    }
}