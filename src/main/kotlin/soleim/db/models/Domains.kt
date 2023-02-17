package soleim.db.models

import org.jetbrains.exposed.sql.Table

object Domains : Table() {

    val dnsProvider = varchar("dnsProvider", 1024)
    val domain = varchar("domain", 1024)
    val password = varchar("password", 1024)
    val ip = varchar("ip", 1024).nullable()

    override val primaryKey = PrimaryKey(domain)
}
