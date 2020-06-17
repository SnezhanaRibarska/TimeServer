package com.time.service;

import java.util.HashMap;
import java.util.Map;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.time.service.internal.DataHandler;
import com.time.service.internal.FileDataHandler;

@Path("/home")
public class TimeService {

	private DataHandler dataHandler;
    private Map<String, Time> availableTimes;
    
    public TimeService() {
        dataHandler = new FileDataHandler();
        availableTimes = dataHandler.readInputData();
    	
    	availableTimes = new HashMap<>();
    	Time sofia = new Time("sofia", "Europe/Sofia");
    	Time london = new Time("london", "Europe/London");
    	Time paris = new Time("paris", "Europe/Paris");
    	Time chicago = new Time("chicago", "America/Chicago");
    	
    	availableTimes.put("sofia", sofia);
    	availableTimes.put("london", london);
    	availableTimes.put("paris", paris);
    	availableTimes.put("chicago", chicago);
    }

    @GET
    @Path("/{cityName}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getCurrentTime(@PathParam("cityName") String name) {
        Response response;
        
        updateAllTimes();
        
        if (availableTimes.containsKey(name)) {
            GenericEntity<Time> entity = new GenericEntity<Time>(availableTimes.get(name)) {};
            response = Response.status(200)
                               .header("Content-Type", "application/xml")
                               .entity(entity)
                               .build();
        } else {
            response = Response.status(404).build();
        }

        return response;
    }
    
    @GET
    @Path("/allTimes")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getAllCurrentTimes() {
        Response response;
        
        updateAllTimes();
        
        if (availableTimes.size() > 0) {
            List<Time> allTimes = new LinkedList<>(availableTimes.values());
            response = Response.status(200)
                               .entity(allTimes)
                               .build();
        } else {
            response = Response.status(204).build();
        }
        
        return response;
    }
    
    @DELETE
    @Path("/{cityName}")
    public Response removeTime(@PathParam("cityName") String name) {
        Response response;
        if (availableTimes.containsKey(name)) {
        	availableTimes.remove(name);
            response = Response.status(204).build();
        } else {
            response = Response.status(404).build();
        }
        
        dataHandler.writeData(availableTimes.values());
        return response;
    }

    // this POST method is used to create a resource
    @POST
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response addTime(Time city) throws URISyntaxException {
        Response response;
        if (availableTimes.containsKey(city.getCityName())) {
            response = Response.status(409).build();
        } else {
            availableTimes.put(city.getCityName(), city);
            response = Response.created(new URI("http://thetimenow/TimeService/home/".concat(city.getCityName())))
                               .header("Content-Type", "application/xml")
                               .entity(city)
                               .build();
        }
        
        dataHandler.writeData(availableTimes.values());
        return response;
    }
    
    // this PUT method modifies an existing resource
    @PUT
    @Path("/{cityName}")
    public Response modifyTime(@PathParam("cityName") String name, @MatrixParam("offset") String offset) {
        Response response;
        
        if (!availableTimes.containsKey(name)) {
            response = Response.status(204).build();
        } else {
            availableTimes.get(name).addOffset(Long.parseLong(offset));
            response = Response.status(200)
                               .header("Content-Type", "application/xml")
                               .entity(availableTimes.get(name))
                               .build();
        }
        
        dataHandler.writeData(availableTimes.values());
        return response;
    }
    
    // this PUT method either modifies or creates a resource
    @PUT
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response modifyOrCreateTime(Time city) throws URISyntaxException {
        Response response;
        
        if (!availableTimes.containsKey(city.getCityName())) {
            response = Response.created(new URI("http://thetimenow/TimeService/home/".concat(city.getCityName())))
                               .header("Content-Type", "application/xml")
                               .entity(city)
                               .build();
        } else {
            response = Response.status(200).header("Content-Type", "application/xml").entity(city).build();
        }
        
        availableTimes.put(city.getCityName(), city);
        dataHandler.writeData(availableTimes.values());
        return response;
    }
    
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getCurrentTimes() {
        String outputText = "Welcome to this simple web service which tells the time!\n\n"
                    + "Here you can see or remove the current time in a city by using "
                    + "GET or DELETE requests respectively\nand adding the following "
                    +"at the end of this URL:\n\n";
        
        for (String cityName : availableTimes.keySet()) {
            outputText += "\t- " + "\"/" + cityName + "\" for "
                                 + cityName.substring(0, 1).toUpperCase()
                                 + cityName.substring(1) + "\n";
        }
        
        outputText += "\nYou can see all available times by adding: \"/allTimes\"\n";
        outputText += "\nYou can add new time by using a POST request and providing\n"
                        + "the XML or JSON representation of the object in the body of the request\n";
        outputText += "\nYou can add or modify time by using a PUT request and providing\n"
                        + "the XML or JSON representation of the object in the body of the request\n";
        outputText += "\nYou can modify time by adding an offset: \"/{cityName};offset=offsetInMilliseconds\"\n";
        
        return outputText;
    }
    
    private void updateAllTimes() {
    	for (Time time : availableTimes.values()) {
    		time.setCurrentTime();
    	}
    }
    
}
