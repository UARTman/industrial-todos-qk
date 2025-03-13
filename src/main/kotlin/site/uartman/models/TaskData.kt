package site.uartman.models

import org.jooq.DSLContext
import org.jooq.generated.tables.pojos.Task
import org.jooq.generated.tables.records.TaskRecord
import org.jooq.generated.tables.references.TASK

data class TaskData(val text: String, val done: Boolean)

fun TaskData.create(dsl: DSLContext, userId: Int): Task {
    val tr = dsl.newRecord(TASK)
    tr.text = text
    tr.done = done
    tr.ownerId = userId
    tr.store()
    return tr.into(Task::class.java)
}
