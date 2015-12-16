package exception;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class InvalidDataExceptionMapper implements ExceptionMapper<InvalidDataException> {

    private static Gson gson = new GsonBuilder().setPrettyPrinting().create();
    
    @Context
    ServletContext context;
    
    @Override
    public Response toResponse(InvalidDataException ex) {
        JsonObject json = new JsonObject();
        
        json.addProperty("httpError", "400");
        json.addProperty("errorCode", ex.getErrorCode());
        json.addProperty("message", ex.getMessage());
        
        return Response
                .status(Response.Status.BAD_REQUEST)
                .entity(gson.toJson(json))
                .type(MediaType.APPLICATION_JSON)
                .build();
         
    }
    
}