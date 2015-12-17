package rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import control.FlightInfoControl;
import control.FlightThread;
import entity.Flight;
import exception.InvalidDataException;
import exception.NoFlightsFoundException;
import exception.NoServerConnectionFoundException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PUT;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

@Path("flightinfo")
public class FlightInfoRest {

    Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").setPrettyPrinting().create();
    FlightInfoControl flightControl = new FlightInfoControl();
    
    @Context
    private UriInfo context;

    public FlightInfoRest() {
    }

    @GET
    @Produces("application/json")
    @Path("{from}/{date}/{numTickets}")
    public Response getFlightsFromOrigin(@PathParam("from") String from, @PathParam("date") String date, @PathParam("numTickets") int numTickets) throws InvalidDataException, NoFlightsFoundException, NoServerConnectionFoundException, InterruptedException {

        
        /*
        * Checking for illegal inputs
        */
        // Check if origin is 3 characters long. If not, throw InvalidDataException
        if(from.length() != 3) {
            throw new InvalidDataException(3, "Illegal input (Origin)");
        }
        
        Date date2;
        
        // Check if able to parse date to right format.
        try {            
            DateFormat sdfISO = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            DateFormat sdfISO2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            date2 = sdfISO.parse(date);
            date = sdfISO2.format(date2);
        } catch (Exception e) {
            throw new InvalidDataException(3, "Illegal input (Date format)");
        }
        
        return Response.ok(gson.toJson(flightControl.getFlightsFromOrigin(from, date, numTickets)), MediaType.APPLICATION_JSON).build();
    }
    
    
    @GET
    @Produces("application/json")
    @Path("{from}/{to}/{date}/{numTickets}")
    public Response getFlightsFromOrigin(@PathParam("from") String from, @PathParam("to") String to, @PathParam("date") String date, @PathParam("numTickets") int numTickets) throws InvalidDataException, NoFlightsFoundException, NoServerConnectionFoundException, InterruptedException {
        List<JsonObject> flightList = new ArrayList();
        JsonObject flight = new JsonObject();
        String london = "LHR"; // London --> has to be origin or destination to get any flights
        String toDestination = "";
        String fromOrigin = "";
        String jsonStr = null;
        Date date2;
        boolean isDestinationAccessable = false;
        
        ExecutorService service = Executors.newFixedThreadPool(5);
        JsonObject response = new JsonObject();
        
        /*
        * Checking for illegal inputs
        */
        // Check if origin is 3 characters long. If not, throw InvalidDataException
        if(from.length() != 3) {
            throw new InvalidDataException(3, "Illegal input (Origin)");
        }
        
        // Check if able to parse date to right format.
        try {
            DateFormat sdfISO = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            date2 = sdfISO.parse(date);
        } catch (ParseException e) {
            throw new InvalidDataException(3, "Illegal input (Date format)");
        }
        
        
        /*
        *   The amount of flights will be scaled down because of the limited amount of API calls per day, we have from British Airways.
        *   Therefore the only distinations we got, will be those from "destinationList" and "London".
        *
        *   We will check if "from" parameter is one of these destinations, if not we will return a NoFlightsFoundException.
        */
        if(from.equals(london)) {
            fromOrigin = from;
            toDestination = to;
        } else if(to.equals(london)) {
            toDestination = to;
            fromOrigin = from;
        } else {
            throw new NoFlightsFoundException(1, "No Flights found.");
        }
        
        String urlToUse = "https://api.ba.com/rest-v1/v1/flightOfferMktAffiliates;departureDateTimeOutbound=" + date + ";locationCodeOriginOutbound=" + fromOrigin + ";locationCodeDestinationOutbound=" + toDestination + ";cabin=Economy;ADT=" + numTickets + ";CHD=0;INF=0;format=.json";

        try {
            FlightThread flightThread = new FlightThread(flightList, urlToUse);
            service.submit(flightThread);
        } catch (Exception e) {
            throw new NoServerConnectionFoundException(10, "Could not connect to url API.");
        }
           
        
        
        service.shutdown();
        service.awaitTermination(1, TimeUnit.MINUTES);
        
        
        if(flightList.size() <= 0) {
            throw new NoFlightsFoundException(1, "Could not find any flights.");
        } else {
            
            response.addProperty("airline", "AoA Airlines (British Airways)");
//            response.add("flights", flightList);
            return Response.ok(gson.toJson(response), MediaType.APPLICATION_JSON).build();
        }
    }
}
