package site.uartman


import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import jakarta.inject.Inject
import org.flywaydb.core.Flyway
import org.hamcrest.CoreMatchers.`is`
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import site.uartman.models.AuthCredentials
import site.uartman.models.TokenData


@QuarkusTest
class AuthTest {

    @Inject
    lateinit var flyway: Flyway

    @AfterEach
    fun afterEach() {
        flyway.clean()
        flyway.migrate()
    }

    @Test
    fun testRegistration() {
        given()
            .body(AuthCredentials("user", "password"))
            .contentType(ContentType.JSON)
            .`when`()
            .post("/auth/register")
            .then()
            .statusCode(200)
            .body("username", `is`("user"))

        given()
            .body(AuthCredentials("user", "password"))
            .contentType(ContentType.JSON)
            .`when`()
            .post("/auth/register")
            .then()
            .statusCode(400)
            .body("details", `is`("User user already exists."))
    }

    @Test
    fun testLogin() {
        given()
            .body(AuthCredentials("user", "password"))
            .contentType(ContentType.JSON)
            .`when`()
            .post("/auth/register")
            .then()
            .statusCode(200)
            .body("username", `is`("user"))

        val tokenResponse = given()
            .body(AuthCredentials("user", "password"))
            .contentType(ContentType.JSON)
            .`when`()
            .post("/auth/login")
            .then()
            .statusCode(200)
            .extract()
            .`as`(TokenData::class.java)

        given()
            .header("Authorization", "Bearer ${tokenResponse.token}")
            .`when`()
            .get("/auth/username")
            .then()
            .statusCode(200)
            .body("username", `is`("user"))
    }

    @Test
    fun testLoginNoUsername() {
        given()
            .body(AuthCredentials("user", "password"))
            .contentType(ContentType.JSON)
            .`when`()
            .post("/auth/login")
            .then()
            .statusCode(401)
            .body("details", `is`("User user does not exist."))
    }

    @Test
    fun testLoginWrongUsername() {
        Given {
            body(AuthCredentials("user", "password"))
            contentType(ContentType.JSON)
        }.When {
            post("/auth/register")
        }.Then {
            statusCode(200)
            body("username", `is`("user"))
        }

        given()
            .body(AuthCredentials("user", "pasword"))
            .contentType(ContentType.JSON)
            .`when`()
            .post("/auth/login")
            .then()
            .statusCode(401)
            .body("details", `is`("Wrong password."))
    }
}
