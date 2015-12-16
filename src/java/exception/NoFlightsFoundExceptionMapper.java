package exception;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class NoFlightsFoundExceptionMapper implements ExceptionMapper<NoFlightsFoundException> {

    private static Gson gson = new GsonBuilder().setPrettyPrinting().create();
    
    @Override
    public Response toResponse(NoFlightsFoundException ex) {
        JsonObject json = new JsonObject();
        json.addProperty("httpError", "404");
        json.addProperty("errorCode", ex.getErrorCode());
        json.addProperty("message", ex.getMessage());
        
        return Response
                .status(Status.NOT_FOUND)
                .entity(gson.toJson(json))
                .type(MediaType.APPLICATION_JSON)
                .build();

    }
    
}
