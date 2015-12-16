package rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import exception.InvalidDataException;
import exception.NoFlightsFoundException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import static javax.ws.rs.HttpMethod.POST;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PUT;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("flightreservation")
public class ReservationRest {

    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    
    @Context
    private UriInfo context;

    public ReservationRest() {
    }

    @POST
    @Consumes("application/json")
    @Produces("application/json")
    public Response reservationRequest(String content) throws InvalidDataException, NoFlightsFoundException {
        JsonObject requestJson = new JsonParser().parse(content).getAsJsonObject();
        JsonObject response = new JsonObject();
        JsonObject passenger = new JsonObject();
        JsonArray passengers = new JsonArray();
        
        
        // Check if all fields are not null.
        if(requestJson.get("flightID") == null ||
                requestJson.get("numberOfSeats") == null ||
                requestJson.get("ReserveeName") == null ||
                requestJson.get("ReservePhone") == null ||
                requestJson.get("ReserveeEmail") == null ||
                requestJson.get("Passengers") == null) {
            throw new InvalidDataException(3, "Illegal Input");
        }
        
        // Check if FlightID is right type (contains "BA").
        if(!requestJson.get("flightID").toString().substring(1, 3).equals("BA")) {
            throw new InvalidDataException(13, "FlightID not right type. Does not contain 'BA'.");
        }
        
        if(!requestJson.get("ReserveeEmail").getAsString().contains("@")) {
            throw new InvalidDataException(14, "Not valid email.");
        }
        
        // CHeck if number of seats is the same as amount of passengers in passenger array.
        if(requestJson.get("numberOfSeats").getAsInt() != requestJson.getAsJsonArray("Passengers").size()) {
            throw new InvalidDataException(12, "Number of passengers does not equal the amount of seats that you requested.");
        }
        
        // Check if number of seats is less than 1.
        if(requestJson.get("numberOfSeats").getAsInt() < 1) {
            throw new InvalidDataException(10, "You must have at least 1 passenger for your reservation.");
        }
        // Check if number of seats is bigger than 9.
        if(requestJson.get("numberOfSeats").getAsInt() > 9) {
            throw new NoFlightsFoundException(2, "None or not enough avilable tickets.");
        }
        
        // Check if there's at least 1 passenger.
        if(requestJson.getAsJsonArray("Passengers").size() < 1) {
            throw new InvalidDataException(10, "You must have at least 1 passenger for your reservation.");
        }
        // Check if number of passengers is less then 10.
        if(requestJson.getAsJsonArray("Passengers").size() > 9) {
            throw new NoFlightsFoundException(2, "None or not enough avilable tickets.");
        }
        
        String flightID = requestJson.get("flightID").getAsString();
        String[] flightIDSplit = flightID.split("\\|");
        
        response.addProperty("flightID", flightID);
        response.addProperty("Origin", flightIDSplit[2]);
        response.addProperty("Destination", flightIDSplit[3]);
        response.addProperty("Date", flightIDSplit[1]);
        response.addProperty("FlightTime", flightIDSplit[4]);
        response.addProperty("numberOfSeats", requestJson.get("numberOfSeats").getAsInt());
        response.addProperty("ReserveeName", requestJson.get("ReserveeName").getAsString());
        for(JsonElement pass : requestJson.getAsJsonArray("Passengers")) {
            passenger.addProperty("firstName", pass.getAsJsonObject().get("firstName").getAsString());
            passenger.addProperty("lastName", pass.getAsJsonObject().get("lastName").getAsString());
            passengers.add(passenger);
        }
        response.add("Passengers", passengers);
        
        return Response.ok(gson.toJson(response), MediaType.APPLICATION_JSON).build();
        
    }
}
