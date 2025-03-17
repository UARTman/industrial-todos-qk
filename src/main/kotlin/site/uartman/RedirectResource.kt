package site.uartman

import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.core.Response
import java.net.URI

@Path("/")
class RedirectResource {

    @GET
    fun redirectToSwagger() : Response {
        return Response.seeOther(URI.create("/q/swagger-ui")).build()
    }
}
