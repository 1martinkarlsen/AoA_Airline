package entity;

import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.5.2.v20140319-rNA", date="2015-12-16T15:22:24")
@StaticMetamodel(Flight.class)
public class Flight_ { 

    public static volatile SingularAttribute<Flight, Date> date;
    public static volatile SingularAttribute<Flight, Integer> numberOfSeats;
    public static volatile SingularAttribute<Flight, Integer> travelTime;
    public static volatile SingularAttribute<Flight, Number> totalPrice;
    public static volatile SingularAttribute<Flight, String> origin;
    public static volatile SingularAttribute<Flight, String> destination;
    public static volatile SingularAttribute<Flight, String> flightID;
    public static volatile SingularAttribute<Flight, Long> id;

}