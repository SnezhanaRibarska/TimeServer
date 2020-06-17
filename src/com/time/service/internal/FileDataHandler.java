package com.time.service.internal;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.time.service.Time;

public class FileDataHandler implements DataHandler{

private final String fileName = "WebContent\\inputFile.txt";
    
    @Override
    public Map<String, Time> readInputData() {
        Map<String, Time> inputData = new HashMap<String, Time>();
        String lineFromFile;
        
        try(BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            while ((lineFromFile = reader.readLine()) != null) {
                Time city = new Time();
                
                String cityName = lineFromFile.substring(0, lineFromFile.indexOf(","));
                city.setCityName(cityName);
                lineFromFile = lineFromFile.substring(lineFromFile.indexOf(",") + 1);
                
                String timeZone = lineFromFile.substring(0, lineFromFile.indexOf(","));
                city.setTimeZone(timeZone);
                lineFromFile = lineFromFile.substring(lineFromFile.indexOf(",") + 1);
                
                city.setOffset(Integer.parseInt(lineFromFile));
                inputData.put(city.getCityName(), city);
                
                city.setCurrentTime();
            }
        } catch (FileNotFoundException e) {
            System.out.println("Error! The file that has to be opened cannot be found!");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return inputData;
    }

    @Override
    public void writeData(Collection<Time> allTimes) {
        try (PrintWriter writer = new PrintWriter(fileName)) {
            for (Time city : allTimes) {
                writer.print(city.getCityName());
                writer.print(",");
                writer.print(city.getTimeZone());
                writer.print(",");
                writer.println(city.getOffset());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
