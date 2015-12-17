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
        if (from.length() != 3) {
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

        JsonObject response = new JsonObject();
        response.addProperty("airline", "AoA-Airline (British Airways)");
        JsonArray jsonArr = new JsonArray();
        for (JsonObject flight : flightControl.getFlightsFromOrigin(from, date, numTickets)) {
            jsonArr.add(flight);
        }
        response.add("flights", jsonArr);

        return Response.ok(gson.toJson(response), MediaType.APPLICATION_JSON).build();
    }

    @GET
    @Produces("application/json")
    @Path("{from}/{to}/{date}/{numTickets}")
    public Response getFlightsFromOrigin(@PathParam("from") String from, @PathParam("to") String to, @PathParam("date") String date, @PathParam("numTickets") int numTickets) throws InvalidDataException, NoFlightsFoundException, NoServerConnectionFoundException, InterruptedException {
        JsonObject response = new JsonObject();

        /*
         * Checking for illegal inputs
         */
        // Check if origin is 3 characters long. If not, throw InvalidDataException
        if (from.length() != 3) {
            throw new InvalidDataException(3, "Illegal input (Origin)");
        }

        Date date2;
        
        // Check if able to parse date to right format.
        try {
            DateFormat sdfISO = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            DateFormat sdfISO2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            date2 = sdfISO.parse(date);
            date = sdfISO2.format(date2);
        } catch (ParseException e) {
            throw new InvalidDataException(3, "Illegal input (Date format)");
        }

        response.addProperty("airline", "AoA-Airline (British Airways)");
        JsonArray jsonArr = new JsonArray();
        for (JsonObject flight : flightControl.getFlightsFromOriginAndDestination(from, to, date, numTickets)) {
            jsonArr.add(flight);
        }
        response.add("flights", jsonArr);

        return Response.ok(gson.toJson(response), MediaType.APPLICATION_JSON).build();
    }
}
