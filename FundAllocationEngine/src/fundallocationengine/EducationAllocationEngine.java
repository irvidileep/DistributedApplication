/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fundallocationengine;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.QueryBuilder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Abhinaya
 *
 *
 *
 *
 */
public class EducationAllocationEngine extends FundAllocationEngine {

    protected DBCollection educationCollection;
    protected DBCollection educationRatingCollection;
    
    // initializing the base class constructor with request id, state id and requested amount
   
    public EducationAllocationEngine(int requestId, int stateId, double requestedAmount) {
        super(requestId, stateId, requestedAmount);
        // Connecting to the mongo db..
        try {
            mongoClient = new MongoClient("localhost", 27017);
            dB = mongoClient.getDB("mydb");
            System.out.println(dB.toString());
            educationCollection = dB.getCollection("education");
            educationRatingCollection = dB.getCollection("education_rating");
        } catch (UnknownHostException e) {
            System.out.println(e.getMessage());
        }
    }
    // Check the decision score of the education department
    public void ifGreaterThanNormalDecisionScore() {

        DBObject dbObj = new BasicDBObject();
        dbObj.put("state_id", stateId);
        DBObject dbObject = QueryBuilder.start("state_id").in(dbObj).get();

        DBCursor dbCursor = educationRatingCollection.find(dbObject);
        if (Float.parseFloat(dbCursor.next().get("decision_score").toString()) > 750) {
            setAllocatedAmount(0.8 * (allocatedAmount));
        }
    }
    
    // Check the previous year fund rating score of the education department
    public void ifGreaterThanNormalPrevYearFundRating() {

        DBObject dbObj = new BasicDBObject();
        dbObj.put("state_id", stateId);
        DBObject dbObject = QueryBuilder.start("state_id").in(dbObj).get();

        DBCursor dbCursor = educationRatingCollection.find(dbObject);
        if (Float.parseFloat(dbCursor.next().get("prev_year_fund_rating").toString()) > 7) {
            setAllocatedAmount(0.7 * (allocatedAmount));
        }
    }
        
    // Check if the state id falls under top ten high schools
    public void ifTopTenHighSchools() {
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        List<Float> arrayList = new ArrayList<>();
        float stateHighSchoolPercentage = 0;


        DBObject dbObj = new BasicDBObject();
        dbObj.put("year", currentYear);
        DBObject dbObject = QueryBuilder.start("year").in(dbObj).get();

        DBCursor dbCursor = educationCollection.find(dbObject);
        
        while (dbCursor.hasNext()) {
            float currentHighSchoolRate = (Float.parseFloat(dbCursor.next().get("high_school").toString()));
            arrayList.add(currentHighSchoolRate);
            
           
            float topStateId = Float.parseFloat(dbCursor.curr().get("state_id").toString());
            
            if (topStateId == (float)getStateId()) {
                stateHighSchoolPercentage = currentHighSchoolRate;
            }
        }
        Collections.sort(arrayList);
        for (int i = arrayList.size() - 1; i > (arrayList.size() - 10); i--) {
            if (arrayList.get(i) == stateHighSchoolPercentage) {
                setAllocatedAmount(getAllocatedAmount() - 0.02 * getRequestedAmount());
            }
        }

    }
    
    // Check if the state falls under top ten bachelors rating in the US
    public void ifTopTenBachelors() {
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        List<Float> arrayList = new ArrayList<>();
        float stateBachelorsPercentage = 0;


        DBObject dbObj = new BasicDBObject();
        dbObj.put("year", currentYear);
        DBObject dbObject = QueryBuilder.start("year").in(dbObj).get();

        DBCursor dbCursor = educationCollection.find(dbObject);
        
        while (dbCursor.hasNext()) {

            float currentHighSchoolRate = (Float.parseFloat(dbCursor.next().get("bachelors").toString()));
            arrayList.add(currentHighSchoolRate);
            
            float topStateId = Float.parseFloat(dbCursor.curr().get("state_id").toString());
            if (topStateId == (float)getStateId()) {
                stateBachelorsPercentage = currentHighSchoolRate;
            }
        }
        Collections.sort(arrayList);
        for (int i = arrayList.size() - 1; i > (arrayList.size() - 10); i--) {
            if (arrayList.get(i) == stateBachelorsPercentage) {
                setAllocatedAmount(getAllocatedAmount() - 0.02 * getRequestedAmount());
            }
        }

    }

    // Check if the state falls under top ten Advanced degree rating in the US
    public void ifTopTenAdvancedDegree() {
        
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        List<Float> arrayList = new ArrayList<>();
        float stateAdvancedPercentage = 0;

       
        DBObject dbObj = new BasicDBObject();
        dbObj.put("year", currentYear);
       
        DBObject dbObject = QueryBuilder.start("year").in(dbObj).get();

        DBCursor dbCursor = educationCollection.find(dbObject);
        while (dbCursor.hasNext()) {

            float currentHighSchoolRate = (Float.parseFloat(dbCursor.next().get("advanced_degree").toString()));
            arrayList.add(currentHighSchoolRate);
            float topStateId = Float.parseFloat(dbCursor.curr().get("state_id").toString());
            if (topStateId == (float)getStateId()) {
                stateAdvancedPercentage = currentHighSchoolRate;
            }
        }
        Collections.sort(arrayList);
        for (int i = arrayList.size() - 1; i > (arrayList.size() - 5); i--) {
            if (arrayList.get(i) == stateAdvancedPercentage) {
                setAllocatedAmount(getAllocatedAmount() - 0.02 * getRequestedAmount());
            }
        }

    }
    
    // Check if the state's high school rating was high compared to the previous year

    public void compareYearRatingHighSchool() {

        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        int pastYear = currentYear - 1;

        DBObject dbObject1 = BasicDBObjectBuilder.start("state_id", stateId).add("year", currentYear).get();
        DBObject dbObject2 = BasicDBObjectBuilder.start("state_id", stateId).add("year", pastYear).get();


        DBCursor dbCursor1 = educationCollection.find(dbObject1);
        DBCursor dbCursor2 = educationCollection.find(dbObject2);

        if ((Float.parseFloat(dbCursor1.next().get("high_school").toString())) - (Float.parseFloat(dbCursor2.next().get("high_school").toString())) > 0.3) {
            setAllocatedAmount(getAllocatedAmount() - 0.02 * (requestedAmount));
        }
    }

    // Check if the state's bachelors rating was high compared to the previous year
    public void compareYearRatingBachelors() {

        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        int pastYear = currentYear - 1;

        DBObject dbObject1 = BasicDBObjectBuilder.start("state_id", stateId).add("year", currentYear).get();
        DBObject dbObject2 = BasicDBObjectBuilder.start("state_id", stateId).add("year", pastYear).get();

        DBCursor dbCursor1 = educationCollection.find(dbObject1);
        DBCursor dbCursor2 = educationCollection.find(dbObject2);

        if ((Float.parseFloat(dbCursor1.next().get("bachelors").toString())) - (Float.parseFloat(dbCursor2.next().get("bachelors").toString())) > 0.2) {
            setAllocatedAmount(getAllocatedAmount() - 0.05 * (requestedAmount));
        }
    }

    // Check if the state's Advanced Degress rating was high compared to the previous year
    public void compareYearRatingAdvancedDegree() {

        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        int pastYear = currentYear - 1;

        DBObject dbObject1 = BasicDBObjectBuilder.start("state_id", stateId).add("year", currentYear).get();
        DBObject dbObject2 = BasicDBObjectBuilder.start("state_id", stateId).add("year", pastYear).get();

        DBCursor dbCursor1 = educationCollection.find(dbObject1);
        DBCursor dbCursor2 = educationCollection.find(dbObject2);

        if ((Float.parseFloat(dbCursor1.next().get("advanced_degree").toString())) - (Float.parseFloat(dbCursor2.next().get("advanced_degree").toString())) > 0.2) {
            setAllocatedAmount(getAllocatedAmount() - 0.03 * (requestedAmount));
        }
    }

    // Check if the state's previous year funding  was high compared to the previous year
    public void compareLastFunding() {
        boolean percentageFlag = false;

        try {
            statement = connect.createStatement();
            resultSet = statement.executeQuery("SELECT ALLOCATED_FUND FROM FUND.REQUEST_HISTORY WHERE STATE_ID = " + getStateId() + " AND DEPARTMENT_ID = " + getDepartmentId() + " AND STATUS_ID = 1");
            while (resultSet.next()) {
                double amount = resultSet.getDouble(1);
                if (amount > 0.65 * (getAllocatedAmount())) {
                    percentageFlag = true;
                }
            }

            if (percentageFlag == true) {
                setAllocatedAmount(0.65 * (getAllocatedAmount()));
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    // Check if the state's funding request greater than the fed budget amount for the department
    public void checkGreaterThanAvailable() {
        double amount = 0;
        try {
            statement = connect.createStatement();
            resultSet = statement.executeQuery("SELECT FUND_AMOUNT FROM FUND.FEDFUND WHERE FUND_DESIGNATION = 'Federal Fund'");
            while (resultSet.next()) {
                amount = resultSet.getDouble(1);
            }
            if (getAllocatedAmount() > 0.5 * amount) {
                setAllocatedAmount(0.15 * (getAllocatedAmount()));
            } else if (getAllocatedAmount() < 0.5 * amount && getAllocatedAmount() > 0.3 * amount) {
                setAllocatedAmount(0.18 * (getAllocatedAmount()));
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
    
    // main function which executes all of the above methods/ rule for a specific request
    @Override
    public double mainProcedure() {
        ifGreaterThanNormalDecisionScore();
        ifGreaterThanNormalPrevYearFundRating();
        compareYearRatingHighSchool();
        compareYearRatingBachelors();
        compareYearRatingAdvancedDegree();
        compareLastFunding();
        ifTopTenHighSchools();
        ifTopTenBachelors();
        ifTopTenAdvancedDegree();
        checkGreaterThanAvailable();
        updateSQL();
        return getAllocatedAmount();
    }

    public static void main(String[] args) {

        EducationAllocationEngine eAE = new EducationAllocationEngine(6, 13, 6748399);
        eAE.mainProcedure();
        System.out.println(eAE.getAllocatedAmount());

    }
}
