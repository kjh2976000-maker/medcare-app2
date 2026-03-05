package com.medcare.pillreminder

import android.Manifest
import android.app.AlarmManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.medcare.pillreminder.data.DataStore
import com.medcare.pillreminder.data.Medication

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var alarmHelper: AlarmHelper
    private val dataStore by lazy { DataStore(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        alarmHelper = AlarmHelper(this)
        requestAlarmPermissions()

        webView = WebView(this)
        setContentView(webView)

        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.webChromeClient = WebChromeClient()
        webView.webViewClient = WebViewClient()
        webView.addJavascriptInterface(WebAppInterface(), "Android")
        webView.loadUrl("https://kjh2976000-maker.github.io/medcare-app/")
    }

    private fun requestAlarmPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
            }
        }
    }

    inner class WebAppInterface {
        @JavascriptInterface
        fun saveMedications(json: String) {
            val type = object : TypeToken<List<Medication>>() {}.type
            val meds: List<Medication> = Gson().fromJson(json, type)
            dataStore.saveMedications(meds)
            alarmHelper.scheduleAllAlarms()
            runOnUiThread {
                Toast.makeText(this@MainActivity, "알람 설정 완료", Toast.LENGTH_SHORT).show()
            }
        }

        @JavascriptInterface
        fun getMedications(): String {
            return Gson().toJson(dataStore.loadMedications())
        }

        @JavascriptInterface
        fun showToast(message: String) {
            runOnUiThread {
                Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
            }
        }

        @JavascriptInterface
        fun isNativeApp(): Boolean = true
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()
        alarmHelper.scheduleAllAlarms()
    }
}
