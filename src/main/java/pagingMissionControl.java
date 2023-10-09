import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class pagingMissionControl {

    public static void main (String[] args) throws IOException, ParseException {

        Gson gson = new Gson();

        String input;

        Map <Integer, Integer> highLimitCount = new HashMap<>();
        Map <Integer, Integer> lowLimitCount = new HashMap<>();



        List <String> satInfoJson = new ArrayList<>();

        File file = new File("C:\\dev\\tools\\workspace\\pagingMissionControl\\pagingMissionControl\\src\\main\\resources\\input.txt");
        BufferedReader br = new BufferedReader(new FileReader(file));

        int batteryCount = 0;
        int tempCount = 0;

        while((input = br.readLine() ) !=null) //Decided to read in all the Strings at once for this challenge since we do not have to account for errors.
        {

            String [] parts = input.split("\\|");

            //keeping all information for future use.
            String timeStamp = parts[0];
            int satelliteId = Integer.parseInt(parts[1]);
            int redHighLimit = Integer.parseInt(parts[2]);
            int yellowHighLimit = Integer.parseInt(parts[3]);
            int yellowLowLimit = Integer.parseInt(parts[4]);
            int redLowLimit = Integer.parseInt(parts[5]);
            double rawValue = Double.parseDouble(parts[6]);
            String component = parts[7];

            String formattedTimeStamp = convertToUTC(timeStamp);
            DataFormat data = (new DataFormat(formattedTimeStamp, satelliteId,redHighLimit,yellowHighLimit,yellowLowLimit,redLowLimit,rawValue,component));
            JsonObject jsonObject = new JsonObject();
          //  Queue <DataFormat> satTime;

            //If for the same satellite there are three battery voltage readings that are under the red low limit within a five minute interval.
            //If for the same satellite there are three thermostat readings that exceed the red high limit within a five minute interval.
            highLimitCount.putIfAbsent(satelliteId, 0);
            lowLimitCount.putIfAbsent(satelliteId, 0);
            //TSTAT temperature status
            //BATT battery
            //1000, 1001 for satellite IDs
            //We check for the id of satellite and only add to the count if either battery low or temp high.
            //high is for temperature stores the id of the satellite and then adds to count if the raw value is below or above some number.
            //low is for battery and store id of the satellite and then adds to count if the raw value is below or above some number



            if(rawValue < redLowLimit && component.equals("BATT") )
            {
                lowLimitCount.put(satelliteId, lowLimitCount.getOrDefault(satelliteId, 0) + 1 );

                if(lowLimitCount.getOrDefault(satelliteId,0) >= 3)
                {
                    jsonObject.addProperty("satelliteId", data.satelliteId);
                    jsonObject.addProperty("severity", "RED LOW");
                    jsonObject.addProperty("component", data.component);
                    jsonObject.addProperty("timestamp", data.formattedTimeStamp);
                    String dataToJson = gson.toJson(jsonObject);
                    satInfoJson.add(dataToJson);
                    lowLimitCount.put(satelliteId, 0);
                }
            }
            if(rawValue > redHighLimit && component.equals("TSTAT")){
                highLimitCount.put(satelliteId, highLimitCount.getOrDefault(satelliteId,0) + 1);
                if(highLimitCount.getOrDefault(satelliteId, 0)>= 3)
                {
                    jsonObject.addProperty("satelliteId", data.satelliteId);
                    jsonObject.addProperty("severity", "RED HIGH");
                    jsonObject.addProperty("component", data.component);
                    jsonObject.addProperty("timestamp", data.formattedTimeStamp);

                    String dataToJson = gson.toJson(jsonObject);
                    satInfoJson.add(dataToJson);

                    highLimitCount.put(satelliteId,0);
                }
            }


            //<timestamp>|<satellite-id>|<red-high-limit>|<yellow-high-limit>|<yellow-low-limit>|<red-low-limit>|<raw-value>|<component>\
            //20180101 23:01:05.001|1001|101|98|25|20|99.9|TSTAT

        }

        for(String satInfoObj : satInfoJson) {
            System.out.println(satInfoObj);
        }


    }

    private static JsonObject createAlertJson(DataFormat data, String severity) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("satelliteId", data.satelliteId);
        jsonObject.addProperty("severity", severity);
        jsonObject.addProperty("component", data.component);
        jsonObject.addProperty("timestamp", data.formattedTimeStamp);
        return jsonObject;
    }
    private static String convertToUTC(String timeStamp) throws ParseException {

        SimpleDateFormat inputDateFormat = new SimpleDateFormat("yyyyMMdd HH:mm:ss.SSS");
        SimpleDateFormat outputDataFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        inputDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        outputDataFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        //TimeZone utc = TimeZone.getTimeZone("UTC");

        Date date = inputDateFormat.parse(timeStamp);
        //outputDataFormat.setTimeZone(utc);

        String newTimeStamp = outputDataFormat.format(date);
        return newTimeStamp;
    }

    private static class DataFormat {
        private int satelliteId, redHighLimit, yellowHighLimit,yellowLowLimit,redLowLimit;
        private double rawValue;
        private String formattedTimeStamp, component;
        public DataFormat(String formattedTimeStamp , int satelliteId, int redHighLimit, int yellowHighLimit, int yellowLowLimit, int redLowLimit, double rawValue, String component) {

            this.satelliteId = satelliteId;
            this.formattedTimeStamp = formattedTimeStamp;
            this.redHighLimit = redHighLimit;
            this.yellowHighLimit = yellowHighLimit;
            this.yellowLowLimit = yellowLowLimit;
            this.redLowLimit = redLowLimit;
            this.rawValue = rawValue;
            this.component = component;
        }

        public int getSatelliteId(){
            return satelliteId;
        }
        public int getRedHighLimit(){
            return getRedHighLimit();
        }
        public int getYellowHighLimit(){
            return yellowHighLimit;
        }
        public int getYellowLowLimit() {
            return yellowLowLimit;
        }
        public int getRedLowLimit(){
            return redLowLimit;
        }
        public double getRawValue(){
            return rawValue;
        }
        public String getFormattedTimeStamp(){
            return formattedTimeStamp;
        }
        public String getComponent(){
            return component;
        }
    }
}

