package com.example.apptardeo

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkInfo
import android.Manifest
import android.text.Editable
import android.text.TextWatcher
import java.util.UUID
import java.util.concurrent.TimeUnit

/**
 * Actividad principal de la aplicación que gestiona la interfaz de usuario y el control de trabajos de verificación de sitios web.
 * Permite al usuario ingresar una URL y una palabra clave, y realizar verificaciones periódicas en la web.
 */
class MainActivity : AppCompatActivity() {

    private val workTag = "WebCheckerWork" // Etiqueta para identificar los trabajos programados
    private var workId = UUID.randomUUID() // ID único para el trabajo actual
    private var semaforo = "R" // Estado del semáforo (R: detenido, V: en ejecución)

    /**
     * Método que se ejecuta cuando la actividad es creada.
     * Se encarga de inicializar los componentes de la interfaz y configurar los listeners.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Solicitar permisos para notificaciones si es necesario
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001)
            }
        }

        // Inicialización de los campos de texto, botones y etiqueta de estado
        val urlField = findViewById<EditText>(R.id.url)
        val wordField = findViewById<EditText>(R.id.palabra)
        val btnPlay = findViewById<Button>(R.id.btnPlay)
        val btnStop = findViewById<Button>(R.id.btnStop)
        val statusText = findViewById<TextView>(R.id.statusText)

        // Listener para el botón "Play"
        btnPlay.setOnClickListener {
            val url = urlField.text.toString()
            val word = wordField.text.toString()

            // Validar URL
            if (!android.util.Patterns.WEB_URL.matcher(url).matches()) {
                urlField.error = "Por favor, ingresa una URL válida."
                return@setOnClickListener
            }

            // Validar palabra
            if (word.isEmpty()) {
                wordField.error = "Por favor, ingresa una palabra."
                return@setOnClickListener
            }

            // Guardar valores en SharedPreferences
            val sharedPreferences = getSharedPreferences("WebCheckerPrefs", MODE_PRIVATE)
            sharedPreferences.edit().putString("url", url).apply()
            sharedPreferences.edit().putString("word", word).apply()

            // Cambiar estado del semáforo y actualizar estado en pantalla
            this.semaforo = "V"
            sharedPreferences.edit().putString("semaforo", semaforo).apply()
            statusText.text = "Estado: En ejecución"

            // Cancelar trabajos anteriores y encolar un nuevo trabajo
            WorkManager.getInstance(this).cancelAllWorkByTag(this.workTag)
            val workRequest = PeriodicWorkRequestBuilder<WebCheckerWorker>(
                15, TimeUnit.MINUTES
            ).addTag(this.workTag).build()

            WorkManager.getInstance(this).enqueue(workRequest)
            this.workId = workRequest.id
        }

        // Listener para el botón "Stop"
        btnStop.setOnClickListener {
            println("Pulso botón stop")
            this.semaforo = "R"
            val sharedPreferences = getSharedPreferences("WebCheckerPrefs", MODE_PRIVATE)
            sharedPreferences.edit().putString("semaforo", semaforo).apply()

            stopWork()
            statusText.text = "Estado: Detenido"
        }

        // Listener para capturar cambios en el campo de texto de la URL
        urlField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val url = s.toString()
                val sharedPreferences = getSharedPreferences("WebCheckerPrefs", MODE_PRIVATE)
                sharedPreferences.edit().putString("url", url).apply()
                println("URL ingresada: $url")
            }
        })

        // Listener para capturar cambios en el campo de texto de la palabra clave
        wordField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val word = s.toString()
                val sharedPreferences = getSharedPreferences("WebCheckerPrefs", MODE_PRIVATE)
                sharedPreferences.edit().putString("word", word).apply()
                println("Palabra ingresada: $word")
            }
        })
    }

    /**
     * Detiene el trabajo en ejecución y cancela todos los trabajos asociados con la etiqueta.
     * También limpia los trabajos en cola que estén en estado en ejecución o encolado.
     */
    private fun stopWork() {
        WorkManager.getInstance(this).cancelWorkById(this.workId)
        WorkManager.getInstance(this).pruneWork()
        WorkManager.getInstance(this).getWorkInfosByTag(workTag).get().forEach { workInfo ->
            println("Trabajo ID: ${workInfo.id}, Estado: ${workInfo.state}")
            if (workInfo.state == WorkInfo.State.ENQUEUED || workInfo.state == WorkInfo.State.RUNNING) {
                WorkManager.getInstance(this).cancelWorkById(workInfo.id)
                println("Trabajo con ID ${workInfo.id} cancelado")
            }
        }
    }
}
