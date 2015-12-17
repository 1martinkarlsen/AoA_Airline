package control;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import entity.Flight;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Interval;
import org.joda.time.Minutes;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

public class FlightThread extends Thread implements Runnable {

    Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").setPrettyPrinting().create();
    JsonParser parser = new JsonParser();
    private volatile List<JsonObject> flightList;
    private final String urlToUse;
    
    public FlightThread(List<JsonObject> flightList, String urlToUse) {
        this.flightList = flightList;
        this.urlToUse = urlToUse;
    }
    
    @Override
    public void run() {
        StringBuilder sb = new StringBuilder();
        HttpURLConnection urlConn = null;
        InputStreamReader in = null;
        try {
            URL url = new URL(urlToUse);
            
            // Sættes til 1 sekund sleep da BA API har begrænset API kald i sekundet.
            Thread.sleep(1000);
            urlConn = (HttpURLConnection) url.openConnection();
            urlConn.setRequestProperty("client-key", "9kc4my2t28pry8vmmk5m4hvx");
            
            if (urlConn != null) {
                urlConn.setReadTimeout(60 * 1000);
            }
            if (urlConn != null && urlConn.getInputStream() != null) {
                in = new InputStreamReader(urlConn.getInputStream(),
                        Charset.defaultCharset());
                BufferedReader bufferedReader = new BufferedReader(in);
                if (bufferedReader != null) {
                    int cp;
                    while ((cp = bufferedReader.read()) != -1) {
                        sb.append((char) cp);
                    }
                    bufferedReader.close();
                }
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Exception while calling URL:" + urlToUse, e);
        }
        
        JsonArray jsonArr = new JsonArray();
        JsonObject json = new JsonParser().parse(sb.toString()).getAsJsonObject();
        
        JsonArray pricedItineraryArr = json.getAsJsonObject("OTA_AirLowFareSearchRS").getAsJsonObject("PricedItineraries").getAsJsonArray("PricedItinerary");
        for(JsonElement pricedItinerary : pricedItineraryArr) {
            JsonObject flight = new JsonObject();
            
            // Udregning af travelTime.
            DateTime startDate = new DateTime(pricedItinerary.getAsJsonObject().getAsJsonObject("AirItinerary").getAsJsonObject("OriginDestinationOptions")
                    .getAsJsonObject("OriginDestinationOption").getAsJsonObject("FlightSegment").get("@DepartureDateTime").getAsString());
            DateTime endDate = new DateTime(pricedItinerary.getAsJsonObject().getAsJsonObject("AirItinerary").getAsJsonObject("OriginDestinationOptions")
                    .getAsJsonObject("OriginDestinationOption").getAsJsonObject("FlightSegment").get("@ArrivalDateTime").getAsString());
            Minutes travelTime = Minutes.minutesBetween(startDate, endDate);
            
            // Date departure
            String strDate = pricedItinerary.getAsJsonObject().getAsJsonObject("AirItinerary").getAsJsonObject("OriginDestinationOptions")
                        .getAsJsonObject("OriginDestinationOption").getAsJsonObject("FlightSegment").get("@DepartureDateTime").getAsString();
            
            DateTimeFormatter dateParser = ISODateTimeFormat.dateTimeParser().withZoneUTC();
            DateTime date = dateParser.parseDateTime(strDate);
            
            flight.addProperty("date", date.toString());
            // Number of Seats
            flight.addProperty("numberOfSeats", pricedItinerary.getAsJsonObject().getAsJsonObject("AirItineraryPricingInfo")
                    .getAsJsonObject("PTC_FareBreakdowns").getAsJsonObject("PTC_FareBreakdown").getAsJsonObject("PassengerTypeQuantity").get("@Quantity").getAsString());
            // Total price
            // Modtager som GBP og ikke EUR.            
            flight.addProperty("totalPrice", pricedItinerary.getAsJsonObject().getAsJsonObject("AirItineraryPricingInfo")
                    .getAsJsonObject("PTC_FareBreakdowns").getAsJsonObject("PTC_FareBreakdown").getAsJsonObject("FareInfo")
                    .getAsJsonObject("PassengerFare").getAsJsonObject("TotalFare").get("@Amount").getAsNumber());
            // Flight ID
            String str = pricedItinerary.getAsJsonObject().getAsJsonObject("AirItinerary").getAsJsonObject("OriginDestinationOptions")
                    .getAsJsonObject("OriginDestinationOption").getAsJsonObject("FlightSegment").getAsJsonObject("MarketingAirline").get("@Code").getAsString() +
                    pricedItinerary.getAsJsonObject().getAsJsonObject("AirItinerary").getAsJsonObject("OriginDestinationOptions")
                    .getAsJsonObject("OriginDestinationOption").getAsJsonObject("FlightSegment").get("@FlightNumber").getAsString() + "|" +
                    date.toString() + "|" + pricedItinerary.getAsJsonObject().getAsJsonObject("AirItinerary").getAsJsonObject("OriginDestinationOptions")
                    .getAsJsonObject("OriginDestinationOption").getAsJsonObject("FlightSegment").getAsJsonObject("DepartureAirport").get("@LocationCode").getAsString() +
                    "|" + pricedItinerary.getAsJsonObject().getAsJsonObject("AirItinerary").getAsJsonObject("OriginDestinationOptions")
                    .getAsJsonObject("OriginDestinationOption").getAsJsonObject("FlightSegment").getAsJsonObject("ArrivalAirport").get("@LocationCode").getAsString() +
                    "|" + travelTime.getMinutes();
            flight.addProperty("flightID", str);
            
            
            flight.addProperty("traveltime", travelTime.getMinutes());
            
            // Destination
            flight.addProperty("destination", pricedItinerary.getAsJsonObject().getAsJsonObject("AirItinerary").getAsJsonObject("OriginDestinationOptions")
                    .getAsJsonObject("OriginDestinationOption").getAsJsonObject("FlightSegment").getAsJsonObject("ArrivalAirport").get("@LocationCode").getAsString());
            
            // Origin
            flight.addProperty("origin", pricedItinerary.getAsJsonObject().getAsJsonObject("AirItinerary").getAsJsonObject("OriginDestinationOptions")
                    .getAsJsonObject("OriginDestinationOption").getAsJsonObject("FlightSegment").getAsJsonObject("DepartureAirport").get("@LocationCode").getAsString());
            
            System.out.println(flight);
            flightList.add(flight);
            
        }
        
    }
    
}
    
