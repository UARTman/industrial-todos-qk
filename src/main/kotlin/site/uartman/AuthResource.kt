package site.uartman

import io.smallrye.jwt.build.Jwt
import jakarta.annotation.security.RolesAllowed
import jakarta.inject.Inject
import jakarta.ws.rs.GET
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.core.SecurityContext
import org.jboss.resteasy.reactive.ResponseStatus
import org.jboss.resteasy.reactive.RestResponse
import org.jboss.resteasy.reactive.RestResponse.ResponseBuilder
import org.jboss.resteasy.reactive.server.ServerExceptionMapper
import org.jooq.DSLContext
import org.jooq.exception.IntegrityConstraintViolationException
import site.uartman.models.AuthCredentials
import site.uartman.models.ErrorData
import site.uartman.models.TokenData
import site.uartman.models.UsernameData
import site.uartman.models.register
import site.uartman.models.validate


class UserAlreadyExistsException(val username: String) : RuntimeException()
class NoSuchUserException(val username: String) : RuntimeException()
class WrongPasswordException(): RuntimeException()

@Path("/auth")
class AuthResource {

    @Inject
    lateinit var ctx: DSLContext

    @ServerExceptionMapper
    @Produces(MediaType.APPLICATION_JSON)
    @ResponseStatus(400)
    fun mapUAEException(e: UserAlreadyExistsException): RestResponse<ErrorData> {
        return RestResponse.status(RestResponse.Status.BAD_REQUEST, ErrorData("User ${e.username} already exists."))
    }

    @ServerExceptionMapper
    @ResponseStatus(401)
    fun mapNoUser(e: NoSuchUserException): RestResponse<ErrorData> {
        return RestResponse.status(RestResponse.Status.UNAUTHORIZED, ErrorData("User ${e.username} does not exist."))
    }

    @ServerExceptionMapper
    @ResponseStatus(401)
    fun mapBadPassword(e: WrongPasswordException): RestResponse<ErrorData> {
        return RestResponse.status(RestResponse.Status.UNAUTHORIZED, ErrorData("Wrong password."))
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/login")
    @ResponseStatus(200)
    fun login(creds: AuthCredentials): TokenData {
        val res = creds.validate(ctx)
        if (res == null)
            throw NoSuchUserException(creds.username)
        if (res == false)
            throw WrongPasswordException()
        val token = Jwt.issuer("https://example.com/issuer")
            .upn(creds.username)
            .groups(HashSet<String?>(mutableListOf<String?>("User")))
            .sign()

        return TokenData(token)
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/register")
    @ResponseStatus(200)
    fun register(creds: AuthCredentials): RestResponse<UsernameData> {
        try {
            ResponseBuilder
                .ok(creds.register(ctx), MediaType.APPLICATION_JSON)
                .build()
        } catch (e: IntegrityConstraintViolationException) {
            throw UserAlreadyExistsException(creds.username)
        }
        return RestResponse.status(Response.Status.OK, UsernameData(creds.username))
    }


    /* Get username of an authenticated user */
    @GET
    @Path("/username")
    @RolesAllowed("User")
    @Produces(MediaType.APPLICATION_JSON)
    fun getUsername(@Context ctx: SecurityContext): UsernameData {
        return UsernameData(ctx.userPrincipal.name)
    }
}
