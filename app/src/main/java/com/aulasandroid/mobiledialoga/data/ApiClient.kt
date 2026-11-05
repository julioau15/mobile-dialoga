package com.aulasandroid.mobiledialoga.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class ApiException(message: String, val statusCode: Int) : Exception(message)

/**
 * Cliente HTTP simples para o backend. Envia o token no header x-access-token
 * (o mesmo esperado pelo middleware verifyJWT do backend).
 *
 * Exemplo:
 *   val resp = api.get(ApiRotas.Pacientes.ME)
 *   val resp = api.put(ApiRotas.Notificacoes.marcarLida(5))
 *   val resp = api.post(ApiRotas.Anotacoes.BASE, JSONObject().put("texto", "..."))
 */
class ApiClient(private val token: () -> String?) {

    suspend fun get(rota: String, query: Map<String, String> = emptyMap()): JSONObject =
        request("GET", rota + montarQuery(query), null)

    suspend fun post(rota: String, body: JSONObject? = null): JSONObject =
        request("POST", rota, body)

    suspend fun put(rota: String, body: JSONObject? = null): JSONObject =
        request("PUT", rota, body)

    suspend fun delete(rota: String): JSONObject =
        request("DELETE", rota, null)

    private fun montarQuery(query: Map<String, String>): String {
        if (query.isEmpty()) return ""
        return "?" + query.entries.joinToString("&") {
            "${URLEncoder.encode(it.key, "UTF-8")}=${URLEncoder.encode(it.value, "UTF-8")}"
        }
    }

    private suspend fun request(metodo: String, rota: String, body: JSONObject?): JSONObject =
        withContext(Dispatchers.IO) {
            val conn = URL(ApiRotas.BASE_URL + rota).openConnection() as HttpURLConnection
            try {
                conn.requestMethod = metodo
                conn.setRequestProperty("Content-Type", "application/json")
                token()?.let { conn.setRequestProperty("x-access-token", it) }
                if (body != null) {
                    conn.doOutput = true
                    conn.outputStream.use { it.write(body.toString().toByteArray()) }
                }
                val codigo = conn.responseCode
                val stream = if (codigo < 400) conn.inputStream else conn.errorStream
                val texto = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
                val json = if (texto.isBlank()) JSONObject() else JSONObject(texto)
                if (!json.optBoolean("status", codigo < 400)) {
                    throw ApiException(json.optString("message", "Erro $codigo"), codigo)
                }
                json
            } finally {
                conn.disconnect()
            }
        }
}
