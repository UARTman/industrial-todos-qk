package site.uartman.models

import io.quarkus.elytron.security.common.BcryptUtil
import org.jooq.DSLContext
import org.jooq.generated.tables.records.User_Record
import org.jooq.generated.tables.references.USER_

data class AuthCredentials(
    val username: String,
    val password: String,
)

fun AuthCredentials.validate(ctx: DSLContext): Boolean? {
    val user = ctx.select().from(USER_).where(USER_.NAME.eq(username)).fetchOneInto(User_Record::class.java)
    if (user == null) return null
    return BcryptUtil.matches(password, user.password)
}

fun AuthCredentials.register(ctx: DSLContext): Int {
    val pw = BcryptUtil.bcryptHash(password)
    var ur = ctx.newRecord(USER_, User_Record(null, username, pw))
    return ur.store()
}
