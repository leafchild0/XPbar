package com.leafchild.xpbar;

import com.mongodb.*;

import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;

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
            //test db
            //DB db = mongo.getDB("test");
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

    protected ArrayList<LinkedHashMap<String, String>> searchData(String key, String value){

        ArrayList<LinkedHashMap<String, String>> searchResults = new ArrayList<>();
        BasicDBObject orderBy = new BasicDBObject("createdDate", -1);
        //BasicDBObject orderBy = new BasicDBObject("currLvlNeededXp", 1);
        //BasicDBObject orderByTotal = new BasicDBObject("totalAmountOfXp", -1);
        //BasicDBObject orderByTotal = new BasicDBObject("key", -1);

        BasicDBObject searchQuery = new BasicDBObject();
        searchQuery.put(key, value);
        //Find values, sort them, and get only last modified data
        DBCursor cursor = table.find(searchQuery).sort(orderBy);
        while (cursor.hasNext()) {
            DBObject tempResult = cursor.next();
            String createdDate = (String) tempResult.get("createdDate");
            String name = (String) tempResult.get("name");
            String level = (String) tempResult.get("currentLevel");
            String totalAmountOfXp = (String) tempResult.get("totalAmountOfXp");
            String currLvlNeededXp = (String) tempResult.get("currLvlNeededXp");
            String currPrBarValue = (String) tempResult.get("currPrBarValue");
            String description = (String) tempResult.get("description");
            String addedValue = (String) tempResult.get("addedValue");

            //Put them into map
            LinkedHashMap<String, String> tempMap = new LinkedHashMap<>();
            tempMap.put("name", name);
            tempMap.put("currentLevel", level + "");
            tempMap.put("createdDate", createdDate);
            tempMap.put("totalAmountOfXp", totalAmountOfXp);
            tempMap.put("currLvlNeededXp", currLvlNeededXp);
            tempMap.put("currPrBarValue", currPrBarValue);
            tempMap.put("description", description);
            tempMap.put("addedValue", addedValue);

            searchResults.add(tempMap);
        }

        return searchResults;
    }

    protected void removeUserData(String key, String value){

        BasicDBObject searchQuery = new BasicDBObject();
        searchQuery.put(key, value);
        table.remove(searchQuery);

    }
}
