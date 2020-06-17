package com.time.service;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.UriBuilder;
import org.glassfish.jersey.client.ClientConfig;

public class TimeClient {

	public static void main(String[] args) {
        getExistingTime();
        getNotExistingTime();
        deleteExistingTime();
        deleteNotExistingTime();
        addNotExistingCityWithPostAndApplication_XML();
        addNotExistingCityWithPostAndApplication_JSON();
        tryingToAddExistingCityWithPost();
        
        getAllAvailableTimes();
    }

    private static void getExistingTime() {
        WebTarget target = createWebTargetObjectWithRootURI();       
        Response response = target.path("/home").path("/sofia").request(MediaType.APPLICATION_XML).get();
        Time city = response.readEntity(Time.class);
        
        assert 200 == response.getStatus();
        assert "sofia".equals(city.getCityName());
        assert "Europe/Sofia".equals(city.getTimeZone());
        assert 0 == city.getOffset();
    }
    
    private static void getNotExistingTime() {
        WebTarget target = createWebTargetObjectWithRootURI();
        Response response = target.path("/home").path("/johannesburg").request(MediaType.APPLICATION_XML).get();
        
        assert 404 == response.getStatus();
    }
    
    private static void getAllAvailableTimes() {
        WebTarget target = createWebTargetObjectWithRootURI();
        
        Response response = target.path("/home").path("/allTimes").request().get();
        List<Time> cities = response.readEntity(new GenericType<List<Time>>() {});
        
        Set<String> cityNames = new HashSet<>();
        cityNames.add("sofia");
        cityNames.add("paris");
        cityNames.add("london");
        cityNames.add("chicago");
        
        Set<String> timeZones = new HashSet<>();
        timeZones.add("Europe/Sofia");
        timeZones.add("Europe/London");
        timeZones.add("Europe/Paris");
        timeZones.add("America/Chicago");
        
        assert 4 == cities.size();
        for (Time city : cities) {
            assert cityNames.contains(city.getCityName());
            assert timeZones.contains(city.getTimeZone());
            cityNames.remove(city.getCityName());
            timeZones.remove(city.getTimeZone());
        }
    }
    
    private static void deleteExistingTime() {
        WebTarget target = createWebTargetObjectWithRootURI();
        Response response = target.path("/home").path("/sofia").request().delete();
        
        assert response.getStatus() == 204;
        
        Time city = new Time("sofia", "Europe/Sofia");
        target.path("/home").request().post(Entity.entity(city, MediaType.APPLICATION_XML));
    }
    
    private static void deleteNotExistingTime() {
        WebTarget target = createWebTargetObjectWithRootURI();
        Response response = target.path("/home").path("/johannesburg").request().delete();
        
        assert response.getStatus() == 404;
    }
    
    private static void addNotExistingCityWithPostAndApplication_XML() {
        testAddingNotExistingCityWithPostAndByProviding(MediaType.APPLICATION_XML);
    }
    
    private static void addNotExistingCityWithPostAndApplication_JSON() {
        testAddingNotExistingCityWithPostAndByProviding(MediaType.APPLICATION_JSON);
    }
    
    private static void testAddingNotExistingCityWithPostAndByProviding(String mediaType) {
        WebTarget target = createWebTargetObjectWithRootURI();
        
        Response deleteResponse = target.path("/home").path("/johannesburg").request().delete();
        Response response = addCityWithPost(target, mediaType);
        Time city = response.readEntity(Time.class);
        
        assert 201 == response.getStatus() : "The status code representing a created resource is 201";
        assert "http://localhost:8080/TimeService/home/johannesburg".equals(response.getHeaderString("Location")) :
               "The absolute path to the resource must be provided in the header file";
        
        // restoring previous server state
        if (deleteResponse.getStatus() == 404) {
            target.path("/home").path("/johannesburg").request().delete();
        }
    }
    
    // MediaType.APPLICATION_XML is used for the test
    private static void tryingToAddExistingCityWithPost() {
        WebTarget target = createWebTargetObjectWithRootURI();
        Response deleteResponse = target.path("/home").path("/johannesburg").request().delete();
        
        // it is tested that adding a not existing city works
        addCityWithPost(target, MediaType.APPLICATION_XML);
        Response response = addCityWithPost(target, MediaType.APPLICATION_XML);

        assert 409 == response.getStatus() : "The status code representing an already created resource is 409";
        
        // restoring previous server state
        if (deleteResponse.getStatus() == 404) {
            target.path("/home").path("/johannesburg").request().delete();
        }
    }
    
    // the methods below should be checked and tested
    private static void addNotExistingCityWithPutByProvidingObjectAndApplication_XML() {
        testAddingNotExistingCityWithPutByProvidingObjectAnd(MediaType.APPLICATION_XML);
    }
    
    private static void addNotExistingCityWithPutByProvidingObjectAndApplication_JSON() {
        testAddingNotExistingCityWithPutByProvidingObjectAnd(MediaType.APPLICATION_JSON);
    }
    
    private static void testAddingExistingCityWithPutByProvidingObject() {
        WebTarget target = createWebTargetObjectWithRootURI();
        Response deleteResponse = target.path("/home").path("/johannesburg").request().delete();
        
        // it is tested that adding a not existing city works
        addCityWithPut(target, MediaType.APPLICATION_XML);
        Response response = addCityWithPut(target, MediaType.APPLICATION_XML);
        
        assertEquals(200, response.getStatus());
        
        if (deleteResponse.getStatus() == 404) {
            target.path("/home").path("/johannesburg").request().delete();
        }
    }
    
    private static void testModifyingCityByProvidingObject() {
        WebTarget target = createWebTargetObjectWithRootURI();
        Time modifiedSofia = new Time("sofia", "Europe/London");
        
        Response response = target.path("/home").request().put(Entity.entity(modifiedSofia, MediaType.APPLICATION_XML));
        
        Time newSofia = target.path("/home").path("/sofia").request(MediaType.APPLICATION_XML).get(Time.class);
        assertEquals(200, response.getStatus());
        assertEquals("Europe/London", newSofia.getTimeZone());
        
        Time correctSofia = new Time("sofia", "Europe/Sofia");
        target.path("/home").request().put(Entity.entity(correctSofia, MediaType.APPLICATION_XML));
    }
    
    private static void testModifyingExistingCityByMatrixParameter() {
        WebTarget target = createWebTargetObjectWithRootURI();     
        Time city = target.path("/home").path("/sofia").request(MediaType.APPLICATION_XML).get(Time.class);
        
        Response response = target.path("/home").path("/sofia;offset=100").request().put(Entity.entity("", MediaType.TEXT_PLAIN));
        
        Time cityAfterRequest = target.path("/home").path("/sofia").request(MediaType.APPLICATION_XML).get(Time.class);
        assertEquals(100, cityAfterRequest.getOffset() - city.getOffset());
        assertEquals(200, response.getStatus());
    }
    
    private static void testModifyingNotExistingCityByMatrixParameter() {
        WebTarget target = createWebTargetObjectWithRootURI();
        
        Response response = target.path("/home").path("/johannesburg;offset=100").request().put(Entity.entity("", MediaType.TEXT_PLAIN));

        assertEquals(204, response.getStatus());
    }
    
    private static void testAddingNotExistingTimeWithPostByProvidingObjectXMLrepresentation() {
        WebTarget target = createWebTargetObjectWithRootURI();      
        Response deleteResponse = target.path("/home").path("/johannesburg").request().delete();
        Response response = target.path("/home")
                                  .request()
                                  .post(Entity.entity("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                                      + "<time><city>johannesburg</city><offset>0</offset><timeInMillis>0"
                                      + "</timeInMillis><timezone>Africa/Johannesburg</timezone></time>",
                                      MediaType.APPLICATION_XML));
        
        assertEquals(201, response.getStatus(), "The status code representing a created resource is 201");
        assertEquals("http://localhost:8080/TimeService/home/johannesburg", response.getHeaderString("Location"),
                     "The absolute path to the resource must be provided in the header file");
        
        // restoring previous server state
        if (deleteResponse.getStatus() == 404) {
            target.path("/home").path("/johannesburg").request().delete();
        }
    }
    
    private static void testAddingNotExistingCityWithPutByProvidingObjectAnd(String mediaType) {
        WebTarget target = createWebTargetObjectWithRootURI();
        
        Response deleteResponse = target.path("/home").path("/johannesburg").request().delete();
        Response response = addCityWithPut(target, mediaType);
        
        assertEquals(201, response.getStatus(), "The status code representing a created resource is 201");
        assertEquals("http://localhost:8080/TimeService/home/johannesburg", response.getHeaderString("Location"),
                     "The absolute path to the resource must be provided in the header file");
        
        // restoring previous server state
        if (deleteResponse.getStatus() == 404) {
            target.path("/home").path("/johannesburg").request().delete();
        }
    }
    
    private static Response addCityWithPost(WebTarget target, String mediaType) {
        Time city = new Time("johannesburg", "Africa/Johannesburg");
        Response response = target.path("/home")
                                  .request()
                                  .post(Entity.entity(city, mediaType));
        return response;
    }
    
    private static Response addCityWithPut(WebTarget target, String mediaType) {
        Time city = new Time("johannesburg", "Africa/Johannesburg");
        Response response = target.path("/home")
                                  .request()
                                  .put(Entity.entity(city, mediaType));
        return response;
    }
    
    private static WebTarget createWebTargetObjectWithRootURI() {
        ClientConfig config = new ClientConfig();
        Client client = ClientBuilder.newClient((Configuration) config);
        WebTarget target = client.target(UriBuilder.fromUri("http://localhost:8080/TimeService").build());
        return target;
    } 

}
