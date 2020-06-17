package com.time.service.tests;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.Response;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.time.service.Time;
import com.time.service.TimeService;

public class TimeServiceTest {

	@ParameterizedTest
    @CsvSource({"sofia,Europe/Sofia", "paris,Europe/Paris",
                "london,Europe/London", "chicago,America/Chicago"})
    public void testGettingExistingTime(String cityName, String timeZoneID) {
        TimeService server = new TimeService();
        Response response = (Response) server.getCurrentTime(cityName);
        
        assertEquals(200, response.getStatus());
        assertEquals(cityName, ((Time)response.getEntity()).getCityName());
        assertEquals(timeZoneID, ((Time)response.getEntity()).getTimeZone());
        assertEquals(0, ((Time)response.getEntity()).getOffset());
    }
    
    @ParameterizedTest
    @ValueSource(strings = {"johannesburg", "cairo", "toronto"})
    public void testGettingNotExistingTime(String cityName) {
        TimeService server = new TimeService();
        Response response = server.getCurrentTime(cityName);
        
        assertEquals(404, response.getStatus());
        assertEquals(4, ((List<Time>)server.getAllCurrentTimes().getEntity()).size());
    }
    
    @Test
    public void testGettingAllCurrentTimes() {
        Set<String> cityNames = new HashSet<>();
        cityNames.add("sofia");
        cityNames.add("london");
        cityNames.add("paris");
        cityNames.add("chicago");
        
        Set<String> timeZones = new HashSet<>();
        timeZones.add("Europe/Sofia");
        timeZones.add("Europe/London");
        timeZones.add("Europe/Paris");
        timeZones.add("America/Chicago");
        
        TimeService server = new TimeService();
        Response response = server.getAllCurrentTimes();
        assertEquals(200, response.getStatus());
        List<Time> allTimes = (List<Time>)response.getEntity();
        assertEquals(4, allTimes.size());
        
        for (Time city : allTimes) {
            assertTrue(cityNames.contains(city.getCityName()));
            assertTrue(timeZones.contains(city.getTimeZone()));
            assertEquals(0, city.getOffset());
            
            cityNames.remove(city.getCityName());
            timeZones.remove(city.getTimeZone());
        }
        assertEquals(0, cityNames.size());
        assertEquals(0, timeZones.size());
    }
    
    @Test
    public void testGettingAllCurrentTimesWithZeroAvailable() {
        TimeService server = new TimeService();
        server.removeTime("sofia");
        server.removeTime("london");
        server.removeTime("paris");
        server.removeTime("chicago");
        
        Response response = server.getAllCurrentTimes();
        assertEquals(204, response.getStatus());
    }
    
    @ParameterizedTest
    @ValueSource(strings = {"sofia", "london", "paris", "chicago"})
    public void testDeletingExistingTime(String cityName) {
        TimeService server = new TimeService();
        Response response = server.removeTime(cityName);
        
        assertEquals(204, response.getStatus());
        
        List<Time> allTimes = (List<Time>)server.getAllCurrentTimes().getEntity();
        assertEquals(3, allTimes.size());
        for (Time city : allTimes) {
            assertFalse(cityName.equals(city.getCityName()));
        }
    }
    
    @ParameterizedTest
    @ValueSource(strings = {"johannesburg", "cairo", "toronto"})
    public void testDeletingNotExistingTime(String cityName) {
        TimeService server = new TimeService();
        Response response = server.removeTime(cityName);
        
        assertEquals(404, response.getStatus());
        assertEquals(4, ((List<Time>)server.getAllCurrentTimes().getEntity()).size());
    }
    
    @ParameterizedTest
    @CsvSource({"johannesburg,Africa/Johannesburg", "cairo,Africa/Cairo", "toronto,America/Toronto"})
    public void testAddingNotExistingTimeByProvidingObject(String cityName, String timeZoneID) throws URISyntaxException {
        TimeService server = new TimeService();
        Time city = new Time(cityName, timeZoneID);
        Response response = server.addTime(city);
        
        assertEquals(201, response.getStatus());
        assertEquals("http://localhost:8080/TimeService/home/" + cityName, response.getHeaderString("Location"));
        
        Response getResponse = server.getCurrentTime(cityName);
        assertEquals(cityName, ((Time)getResponse.getEntity()).getCityName());
        assertEquals(timeZoneID, ((Time)getResponse.getEntity()).getTimeZone());
        assertEquals(0, ((Time)getResponse.getEntity()).getOffset());
        assertEquals(5, ((List<Time>)server.getAllCurrentTimes().getEntity()).size());
    }
    
    @ParameterizedTest
    @CsvSource({"sofia,Europe/Sofia", "paris,Europe/Paris",
                "london,Europe/London", "chicago,America/Chicago"})
    public void testAddingExistingTimeByProvidingObject() throws URISyntaxException {
        TimeService server = new TimeService();
        Time city = new Time("sofia", "Europe/Sofia");
        Response response = server.addTime(city);
        
        assertEquals(409, response.getStatus());
        assertEquals(4, ((List<Time>)server.getAllCurrentTimes().getEntity()).size());
    }
    
    @ParameterizedTest
    @ValueSource(strings = {"sofia", "london", "paris", "chicago"})
    public void testModifyingExistingTimeByProvidingOffset(String cityName) {
        TimeService server = new TimeService();
        
        Response response = server.modifyTime(cityName, "100");
        assertEquals(200, response.getStatus());
        
        Response getResponse = server.getCurrentTime(cityName);
        assertEquals(100, ((Time)getResponse.getEntity()).getOffset());
        
        response = server.modifyTime(cityName, "-50");
        assertEquals(200, response.getStatus());
        assertEquals(50, ((Time)getResponse.getEntity()).getOffset());
    }
    
    @ParameterizedTest
    @ValueSource(strings = {"johannesburg", "cairo", "toronto"})
    public void testModifyingNotExistingTimeByProvidingOffset(String cityName) {
        TimeService server = new TimeService();
        
        Response response = server.modifyTime(cityName, "100");
        
        assertEquals(204, response.getStatus());
    }
    
    @ParameterizedTest
    @ValueSource(strings = {"sofia", "london", "paris", "chicago"})
    public void testModifyingOrCreatingExistingTimeByProvidingObject(String cityName) throws URISyntaxException {
        TimeService server = new TimeService();
        Time city = new Time(cityName, "Africa/Johannesburg");
        
        Response response = server.modifyOrCreateTime(city);
        
        assertEquals(200, response.getStatus());
        
        Response getResponse = server.getCurrentTime(cityName);
        assertEquals("Africa/Johannesburg", ((Time)getResponse.getEntity()).getTimeZone());
        assertEquals(0, ((Time)getResponse.getEntity()).getOffset());
        assertEquals(4, ((List<Time>)server.getAllCurrentTimes().getEntity()).size());
    }
    
    @ParameterizedTest
    @CsvSource({"johannesburg,Africa/Johannesburg", "cairo,Africa/Cairo", "toronto,America/Toronto"})
    public void testModifyingOrCreatingNotExistingTimeByProvidingObject(String cityName, String timeZoneID) throws URISyntaxException {
        TimeService server = new TimeService();
        Time city = new Time(cityName, timeZoneID);
        
        Response response = server.modifyOrCreateTime(city);
        
        assertEquals(201, response.getStatus());
        assertEquals("http://localhost:8080/TimeService/home/" + cityName, response.getHeaderString("Location"));
        
        Response getResponse = server.getCurrentTime(cityName);
        assertEquals(cityName, ((Time)getResponse.getEntity()).getCityName());
        assertEquals(timeZoneID, ((Time)getResponse.getEntity()).getTimeZone());
        assertEquals(0, ((Time)getResponse.getEntity()).getOffset());
        assertEquals(5, ((List<Time>)server.getAllCurrentTimes().getEntity()).size());
    }

}
