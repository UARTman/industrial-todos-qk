package site.uartman

import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import io.restassured.common.mapper.TypeRef
import io.restassured.http.ContentType
import io.restassured.module.kotlin.extensions.Extract
import jakarta.inject.Inject
import org.flywaydb.core.Flyway
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.jooq.DSLContext
import org.jooq.generated.tables.pojos.Task
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import site.uartman.models.AuthCredentials
import site.uartman.models.TaskData
import site.uartman.models.TokenData

@QuarkusTest
class TasksTest {

    @Inject
    lateinit var flyway: Flyway
    @Inject
    lateinit var dsl: DSLContext

    lateinit var userToken: String
    lateinit var user1Token: String

    @BeforeEach
    fun beforeEach() {
        flyway.clean()
        flyway.migrate()
        given()
            .body(AuthCredentials("user", "pass"))
            .contentType(ContentType.JSON)
            .`when`()
            .post("/auth/register")
            .then()
            .statusCode(200)

        given()
            .body(AuthCredentials("user1", "pass1"))
            .contentType(ContentType.JSON)
            .`when`()
            .post("/auth/register")
            .then()
            .statusCode(200)

        userToken = given()
            .body(AuthCredentials("user", "pass"))
            .contentType(ContentType.JSON)
            .`when`()
            .post("/auth/login")
            .then()
            .statusCode(200)
            .Extract {
                `as`(TokenData::class.java).token
            }

        user1Token = given()
            .body(AuthCredentials("user1", "pass1"))
            .contentType(ContentType.JSON)
            .`when`()
            .post("/auth/login")
            .then()
            .statusCode(200)
            .extract()
            .`as`(TokenData::class.java).token
    }


    @AfterEach
    fun afterEach() {
        flyway.clean()
    }


    @Test()
    fun tasksEmpty() {
        given()
            .header("Authorization", "Bearer ${userToken}")
            .`when`()
            .get("/tasks")
            .then()
            .statusCode(200)
            .body(`is`("[]"))
    }

    @Test
    fun tasksNotAllowed() {
        given()
            .`when`()
            .get("/tasks")
            .then()
            .statusCode(401)
    }

    @Test
    fun addTask() {
        given()
            .header("Authorization", "Bearer $userToken")
            .contentType(ContentType.JSON)
            .body(TaskData("test", false))
            .`when`()
            .post("/tasks")
            .then()
            .statusCode(200)
            .body("text", `is`("test"))
            .body("done", `is`(false))
            .body("id", notNullValue())
            .body("ownerId", notNullValue())

        val tasks = given()
            .header("Authorization", "Bearer $userToken")
            .`when`()
            .get("/tasks")
            .then()
            .statusCode(200)
            .extract()
            .`as`(object : TypeRef<MutableList<Task>>() {})

        assertEquals(tasks.size, 1)
        assertEquals(tasks[0].text, "test")
        assertEquals(tasks[0].done, false)
        assertNotNull(tasks[0].id)
        assertNotNull(tasks[0].ownerId)
    }
}
