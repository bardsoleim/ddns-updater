package soleim.db

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import soleim.db.models.Domains
import soleim.dto.Domain

interface DocumentDao {
    suspend fun getAll(): List<Domain>
    suspend fun add(domain: Domain): Domain?
    suspend fun delete(domain: String)
}

class DocumentDaoImpl : DocumentDao {

    private fun resultRowToDomainDTO(row: ResultRow) = Domain(
        dnsProvider = row[Domains.dnsProvider],
        domain = row[Domains.domain],
        password = row[Domains.password],
        ip = row[Domains.ip],
    )

    override suspend fun getAll(): List<Domain> = DatabaseFactory.dbQuery {
        Domains.selectAll().map(::resultRowToDomainDTO)
    }

    override suspend fun add(domainDto: Domain): Domain? =
        DatabaseFactory.dbQuery {
            val insertStatement = Domains.insert {
                it[ip] = domainDto.ip.toString()
                it[domain] = domainDto.domain
                it[dnsProvider] = domainDto.dnsProvider
                it[password] = domainDto.password
            }
            insertStatement.resultedValues?.singleOrNull()?.let(::resultRowToDomainDTO)
        }

    override suspend fun delete(domain: String) {
        DatabaseFactory.dbQuery {
            Domains.deleteWhere { Domains.domain eq domain }
        }
    }
}