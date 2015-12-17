package control;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import exception.NoFlightsFoundException;
import exception.NoServerConnectionFoundException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.joda.time.DateTime;
import org.joda.time.Minutes;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

public class FlightInfoControl {
    
    List<JsonObject> flightList = new ArrayList();
    List<String> destinationList = new ArrayList();
    
    public List<JsonObject> getFlightsFromOrigin(String from, String date, int numTickets) throws NoFlightsFoundException, NoServerConnectionFoundException, InterruptedException {
        destinationList.add("BCN"); // Barcelona
        destinationList.add("CPH"); // København
        destinationList.add("CDG"); // Paris
        destinationList.add("BER"); // Berlin
        destinationList.add("MAD"); // Madrid
        String london = "LHR"; // London --> has to be origin or destination to get any flights
        String toDestination = "";
        String fromOrigin = "";
        String jsonStr = null;
        Date date2;
        boolean isDestinationAccessable = false;
        
        // Threadpool sættes til 1 da BA API har begrænset API kald i sekundet.
        ExecutorService service = Executors.newFixedThreadPool(1);
        
        /*
        *   The amount of flights will be scaled down because of the limited amount of API calls per day, we have from British Airways.
        *   Therefore the only distinations we got, will be those from "destinationList" and "London".
        *
        *   We will check if "from" parameter is one of these destinations, if not we will return a NoFlightsFoundException.
        */
        if(from.equals(london)) {
            fromOrigin = from;
        } else {
            for(String destination : destinationList) {
                if(from.equals(destination)) {
                    isDestinationAccessable = true;
                    fromOrigin = destination;
                    toDestination = london;
                }
            }
            
            if(isDestinationAccessable == false) {
                throw new NoFlightsFoundException(1, "No Flights found.");
            }
        }
        
        if(!isDestinationAccessable) {
            for (String dest : destinationList) {
                toDestination = dest;
                String urlToUse = "https://api.ba.com/rest-v1/v1/flightOfferMktAffiliates;departureDateTimeOutbound=" + 
                        date + ";locationCodeOriginOutbound=" + fromOrigin + ";locationCodeDestinationOutbound=" + 
                        toDestination + ";cabin=Economy;ADT=" + numTickets + ";CHD=0;INF=0;format=.json";
                
                try {
                    FlightThread flightThread = new FlightThread(flightList, urlToUse);
                    service.execute(flightThread);
                } catch (Exception e) {
                    throw new NoServerConnectionFoundException(10, "Could not connect to url API.");
                }

            }
        } else {
            String urlToUse = "https://api.ba.com/rest-v1/v1/flightOfferMktAffiliates;departureDateTimeOutbound=" + 
                        date + ";locationCodeOriginOutbound=" + fromOrigin + ";locationCodeDestinationOutbound=" + 
                        toDestination + ";cabin=Economy;ADT=" + numTickets + ";CHD=0;INF=0;format=.json";
            
            try {
                FlightThread flightThread = new FlightThread(flightList, urlToUse);
                service.submit(flightThread);
            } catch (Exception e) {
                throw new NoServerConnectionFoundException(10, "Could not connect to url API.");
            }
        }
        
        service.shutdown();
        service.awaitTermination(1, TimeUnit.MINUTES);
        System.out.println("All threads completed");
        
        if(flightList.size() <= 0) {
            throw new NoFlightsFoundException(1, "Could not find any flights.");
        } else {
            return flightList;
        }
    }
}
