package exception;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class NoServerConnectionFoundExceptionMapper implements ExceptionMapper<NoServerConnectionFoundException> {

    private static Gson gson = new GsonBuilder().setPrettyPrinting().create();
    
    @Override
    public Response toResponse(NoServerConnectionFoundException ex) {
        JsonObject json = new JsonObject();
        json.addProperty("httpError", "500");
        json.addProperty("errorCode", ex.getErrorCode());
        json.addProperty("message", ex.getMessage());
        
        return Response
                .status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(gson.toJson(json))
                .type(MediaType.APPLICATION_JSON)
                .build();
         
    }
    
}