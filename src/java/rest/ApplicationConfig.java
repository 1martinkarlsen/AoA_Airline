package rest;

import java.util.Set;
import javax.ws.rs.core.Application;

@javax.ws.rs.ApplicationPath("api")
public class ApplicationConfig extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> resources = new java.util.HashSet<Class<?>>();
        addRestResourceClasses(resources);
        return resources;
    }

    private void addRestResourceClasses(Set<Class<?>> resources) {
        resources.add(exception.InvalidDataExceptionMapper.class);
        resources.add(exception.NoFlightsFoundExceptionMapper.class);
        resources.add(exception.NoServerConnectionFoundExceptionMapper.class);
        resources.add(rest.FlightInfoRest.class);
        resources.add(rest.ReservationRest.class);
    }
    
}
