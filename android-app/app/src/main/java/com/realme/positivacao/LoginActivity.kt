package com.realme.positivacao

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

/**
 * Tela de login: valida e-mail + senha contra a aba "Usuários" da planilha,
 * via o mesmo endpoint doPost (action "login") usado pela PWA. Não usa
 * nenhuma biblioteca externa de rede — só java.net.HttpURLConnection, para
 * manter o projeto simples de compilar.
 */
class LoginActivity : AppCompatActivity() {

    companion object {
        // Mesma URL do Web App do Apps Script (Code.gs / CONFIG.WEBAPP_URL).
        const val WEBAPP_URL =
            "https://script.google.com/macros/s/AKfycbwvyAAD9zHy-T4ItarGMEphtTR3kWG5SgUUWzO4kVWaQj6K3lPF8BP7GTpn_dr0nUk/exec"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        if (prefs.getBoolean("loggedIn", false)) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        val emailField = findViewById<EditText>(R.id.emailField)
        val senhaField = findViewById<EditText>(R.id.senhaField)
        val btnEntrar = findViewById<Button>(R.id.btnEntrar)
        val errorText = findViewById<TextView>(R.id.errorText)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)

        btnEntrar.setOnClickListener {
            val email = emailField.text.toString().trim()
            val senha = senhaField.text.toString()

            if (email.isEmpty() || senha.isEmpty()) {
                errorText.text = "Informe e-mail e senha."
                errorText.visibility = View.VISIBLE
                return@setOnClickListener
            }

            errorText.visibility = View.GONE
            progressBar.visibility = View.VISIBLE
            btnEntrar.isEnabled = false

            Thread {
                try {
                    val payload = JSONObject()
                    payload.put("action", "login")
                    val innerPayload = JSONObject()
                    innerPayload.put("email", email)
                    innerPayload.put("senha", senha)
                    payload.put("payload", innerPayload)

                    val url = URL(WEBAPP_URL)
                    val conn = url.openConnection() as HttpURLConnection
                    conn.requestMethod = "POST"
                    conn.doOutput = true
                    conn.setRequestProperty("Content-Type", "text/plain; charset=utf-8")
                    conn.connectTimeout = 15000
                    conn.readTimeout = 15000

                    val writer = OutputStreamWriter(conn.outputStream, Charsets.UTF_8)
                    writer.write(payload.toString())
                    writer.flush()
                    writer.close()

                    val code = conn.responseCode
                    val stream = if (code in 200..299) conn.inputStream else conn.errorStream
                    val responseText = stream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                    conn.disconnect()

                    val json = JSONObject(responseText)

                    runOnUiThread {
                        progressBar.visibility = View.GONE
                        btnEntrar.isEnabled = true
                        if (json.optBoolean("ok", false)) {
                            val result = json.getJSONObject("result")
                            prefs.edit()
                                .putBoolean("loggedIn", true)
                                .putString("email", result.optString("email", email))
                                .putString("nome", result.optString("nome", ""))
                                .apply()
                            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                            finish()
                        } else {
                            errorText.text = json.optString("error", "Não foi possível entrar.")
                            errorText.visibility = View.VISIBLE
                        }
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        progressBar.visibility = View.GONE
                        btnEntrar.isEnabled = true
                        errorText.text = "Sem conexão. Tente novamente."
                        errorText.visibility = View.VISIBLE
                    }
                }
            }.start()
        }
    }
}
