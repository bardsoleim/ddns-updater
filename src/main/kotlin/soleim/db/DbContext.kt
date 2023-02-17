package soleim.db

import org.jetbrains.exposed.sql.*
import kotlinx.coroutines.*
import org.jetbrains.exposed.sql.transactions.*
import org.jetbrains.exposed.sql.transactions.experimental.*
import soleim.db.models.Domains

object DatabaseFactory {
    fun init() {
        val driverClassName = "org.h2.Driver"
        val jdbcURL = "jdbc:h2:file:./build/db"
        val database = Database.connect(jdbcURL, driverClassName)
        transaction(database) {
            SchemaUtils.create(Domains)
        }
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}

val dao: DocumentDao by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { DocumentDaoImpl() }

