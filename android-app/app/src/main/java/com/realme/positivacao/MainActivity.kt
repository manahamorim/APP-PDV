package com.realme.positivacao

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessaging

/**
 * Tela principal: WebView carregando a PWA (GitHub Pages). O login,
 * a troca de senha obrigatoria no primeiro acesso e o "esqueci a senha"
 * agora acontecem inteiramente dentro da PWA (ver index.html), entao
 * este WebView so abre a URL raiz - sem parametros de autenticacao.
 * Suporta upload de arquivo (para o campo "Fotos da positivacao") via
 * onShowFileChooser.
 *
 * Tambem obtem o token do Firebase Cloud Messaging e injeta na PWA
 * (via window.registrarFcmTokenNativo) para que o backend consiga
 * enviar notificacoes push para o usuario logado.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar
    private var filePathCallback: ValueCallback<Array<Uri>>? = null

    private var pageLoaded = false
    private var pendingFcmToken: String? = null

    companion object {
        // Troque pela URL do seu GitHub Pages, se for diferente.
        const val WEB_URL = "https://manahamorim.github.io/APP-PDV/"
        const val FILE_CHOOSER_REQUEST_CODE = 51426
        const val NOTIFICATION_PERMISSION_REQUEST_CODE = 51427
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webView)
        progressBar = findViewById(R.id.progressBar)

        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.allowFileAccess = true
        webView.settings.mediaPlaybackRequiresUserGesture = false

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                progressBar.visibility = View.GONE
                pageLoaded = true
                pendingFcmToken?.let { injectFcmToken(it) }
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                this@MainActivity.filePathCallback?.onReceiveValue(null)
                this@MainActivity.filePathCallback = filePathCallback

                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "image/*"
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)

                startActivityForResult(
                    Intent.createChooser(intent, "Escolher fotos"),
                    FILE_CHOOSER_REQUEST_CODE
                )
                return true
            }
        }

        webView.loadUrl(WEB_URL)

        requestNotificationPermissionIfNeeded()
        fetchFcmToken()
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    private fun fetchFcmToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) return@addOnCompleteListener
            val token = task.result ?: return@addOnCompleteListener
            if (pageLoaded) {
                injectFcmToken(token)
            } else {
                pendingFcmToken = token
            }
        }
    }

    private fun injectFcmToken(token: String) {
        val escaped = token.replace("\\", "\\\\").replace("'", "\\'")
        val js = "if (window.registrarFcmTokenNativo) { window.registrarFcmTokenNativo('$escaped'); }"
        runOnUiThread {
            webView.evaluateJavascript(js, null)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == FILE_CHOOSER_REQUEST_CODE) {
            if (filePathCallback == null) {
                super.onActivityResult(requestCode, resultCode, data)
                return
            }
            val results = WebChromeClient.FileChooserParams.parseResult(resultCode, data)
            filePathCallback?.onReceiveValue(results)
            filePathCallback = null
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onBackPressed() {
        if (::webView.isInitialized && webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
