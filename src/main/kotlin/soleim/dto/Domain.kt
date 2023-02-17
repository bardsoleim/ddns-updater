package soleim.dto
import kotlinx.serialization.Serializable

@Serializable
data class Domain(val dnsProvider: String, val domain: String,
                  val password: String, var ip: String? = null)