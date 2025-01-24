package com.example.apptardeo

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.apptardeo.R
import java.util.UUID
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    // Atributos privados de la clase
    private val workTag = "WebCheckerWorkTag"
    private var workId: UUID? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Pedir permiso para enviar notificaciones (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        }

        // Referencias a los elementos de la vista
        val urlField: EditText = findViewById(R.id.url) // Campo de URL
        val wordField: EditText = findViewById(R.id.palabra) // Campo de palabra
        val btnPlay: Button = findViewById(R.id.btnPlay) // Botón Play
        val btnStop: Button = findViewById(R.id.btnStop) // Botón Stop

        // Iniciar el trabajo periódico cuando se hace clic en "Play"
        btnPlay.setOnClickListener {
            // Configurar el `PeriodicWorkRequest` para ejecutar cada 15 minutos
            val workRequest: PeriodicWorkRequest = PeriodicWorkRequestBuilder<WebCheckerWorker>(
                15, TimeUnit.MINUTES
            ).addTag(workTag).build()

            // Encolar el trabajo periódico
            WorkManager.getInstance(this).enqueue(workRequest)

            // Guardar el ID del trabajo para referencia
            workId = workRequest.id
        }

        // Detener el trabajo cuando se presiona el botón "Stop"
        btnStop.setOnClickListener {
            stopWork()
        }

        // Actualizar SharedPreferences cuando cambien los campos de texto
        val sharedPreferences = getSharedPreferences("WebCheckerPrefs", MODE_PRIVATE)

        urlField.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                sharedPreferences.edit().putString("url", s.toString()).apply()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        wordField.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                sharedPreferences.edit().putString("palabra", s.toString()).apply()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    // Función para detener el trabajo
    private fun stopWork() {
        val workManager = WorkManager.getInstance(this)

        // Cancelar todo el trabajo con la etiqueta específica
        workManager.cancelAllWorkByTag(workTag)

        // Cancelar el trabajo específico usando su ID
        workId?.let { workManager.cancelWorkById(it) }

        // Finalizar la aplicación si es necesario
        finishAffinity()
        System.exit(0)
    }
}
