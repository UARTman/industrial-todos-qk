package site.uartman

import jakarta.annotation.security.RolesAllowed
import jakarta.inject.Inject
import jakarta.ws.rs.DELETE
import jakarta.ws.rs.GET
import jakarta.ws.rs.POST
import jakarta.ws.rs.PUT
import jakarta.ws.rs.Path
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.SecurityContext
import org.jboss.resteasy.reactive.ResponseStatus
import org.jboss.resteasy.reactive.RestResponse
import org.jboss.resteasy.reactive.server.ServerExceptionMapper
import org.jooq.DSLContext
import org.jooq.generated.tables.pojos.Task
import org.jooq.generated.tables.records.TaskRecord
import org.jooq.generated.tables.references.TASK
import org.jooq.generated.tables.references.USER_
import site.uartman.models.TaskData
import site.uartman.models.ErrorData
import site.uartman.models.create

class TaskNotFoundException(val taskId: Int): RuntimeException()

class TaskNotYoursException(val taskId: Int, val username: String): RuntimeException()

@Path("/tasks")
class TasksResource {
    @Inject
    lateinit var dsl: DSLContext


    @ServerExceptionMapper
    @ResponseStatus(404)
    fun mapTNFException(e: TaskNotFoundException): RestResponse<ErrorData> {
        return RestResponse.status(RestResponse.Status.NOT_FOUND, ErrorData("Task with id=${e.taskId} not found."))
    }

    @ServerExceptionMapper
    @ResponseStatus(401)
    fun mapTNYException(e: TaskNotYoursException): RestResponse<ErrorData> {
        return RestResponse.status(RestResponse.Status.UNAUTHORIZED, ErrorData("Task with id=${e.taskId} does not belong to user ${e.username}."))
    }

    @GET
    @RolesAllowed("User")
    fun getTasks(@Context ctx: SecurityContext) : List<Task> {
        val username = ctx.userPrincipal.name

        val userTasks = dsl.select(TASK)
            .from(
                TASK
                    .join(USER_)
                    .on(TASK.OWNER_ID.eq(USER_.ID))
            )
            .where(USER_.NAME.eq(username))
            .fetchInto(Task::class.java)

        return userTasks
    }

    @POST
    @RolesAllowed("User")
    fun createTask(@Context ctx: SecurityContext, task: TaskData) : Task {
        val username = ctx.userPrincipal.name;
        val userId = dsl.select(USER_.ID).from(USER_).where(USER_.NAME.eq(username)).fetchOneInto(Int::class.java)

        return task.create(dsl, userId!!)
    }

    @Path("/{id}")
    @DELETE
    @RolesAllowed("User")
    fun deleteTask(@Context ctx: SecurityContext, id: Int) : Task {
        val username = ctx.userPrincipal.name
        val userId = dsl.select(USER_.ID).from(USER_).where(USER_.NAME.eq(username)).fetchOneInto(Int::class.java)

        val task = dsl.select().from(TASK).where(TASK.ID.eq(id)).fetchOneInto(TaskRecord::class.java)

        if (task == null)
            throw TaskNotFoundException(id)
        if (task.ownerId != userId)
            throw TaskNotYoursException(id, username)

        task.delete()

        return task.into(Task::class.java)
    }

    @Path("/{id}")
    @PUT
    @RolesAllowed("User")
    fun updateTask(@Context ctx: SecurityContext, id: Int, taskData: TaskData): Task {
        val username = ctx.userPrincipal.name
        val userId = dsl.select(USER_.ID).from(USER_).where(USER_.NAME.eq(username)).fetchOneInto(Int::class.java)

        val task = dsl.select().from(TASK).where(TASK.ID.eq(id)).fetchOneInto(TaskRecord::class.java)

        if (task == null)
            throw TaskNotFoundException(id)
        if (task.ownerId != userId)
            throw TaskNotYoursException(id, username)

        task.apply {
            text = taskData.text
            done = taskData.done
        }
        task.store()

        return task.into(Task::class.java)
    }
}
