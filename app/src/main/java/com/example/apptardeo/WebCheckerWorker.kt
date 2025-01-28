package com.example.apptardeo

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Button
import android.widget.EditText
import androidx.core.app.ActivityCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.apptardeo.R
import org.jsoup.Jsoup
import java.lang.Exception

/**
 * Worker que se encarga de realizar la verificación periódica de una página web para encontrar una palabra clave.
 * Si la palabra se encuentra en la página, se envía una notificación al usuario.
 *
 * @param appContext El contexto de la aplicación.
 * @param workerParams Los parámetros del Worker.
 */
class WebCheckerWorker(appContext: Context, workerParams: WorkerParameters) : Worker(appContext, workerParams) {

    /**
     * Método que se ejecuta en segundo plano para realizar la verificación de la página web.
     * Verifica si la palabra clave está presente en la página web especificada en las preferencias compartidas.
     * Si se encuentra la palabra, se envía una notificación.
     *
     * @return Resultado de la tarea (éxito o fallo).
     */
    override fun doWork(): Result {
        try {
            val sharedPreferences = applicationContext.getSharedPreferences("WebCheckerPrefs", Context.MODE_PRIVATE)

            // Obtener el estado del semáforo desde las preferencias compartidas
            val semaforo = sharedPreferences.getString("semaforo", null) ?: return Result.failure()

            // Si el semáforo está detenido o la tarea ha sido cancelada, finalizar el trabajo
            if (isStopped || semaforo.equals("R")) {
                println("stopped")
                return Result.failure()
            }

            // Obtener la URL y la palabra clave desde las preferencias compartidas
            val url = sharedPreferences.getString("url", null) ?: return Result.failure()
            val word = sharedPreferences.getString("word", null) ?: return Result.failure()

            // Si el semáforo está detenido o la tarea ha sido cancelada, finalizar el trabajo
            if (isStopped || semaforo.equals("R")) {
                return Result.failure()
            }

            // Conectar a la página web y obtener el documento HTML
            val doc = Jsoup.connect(url).get()

            // Si el semáforo está detenido o la tarea ha sido cancelada, finalizar el trabajo
            if (isStopped || semaforo.equals("R")) {
                return Result.failure()
            }

            // Verificar si la palabra está presente en el texto de la página web
            if (doc.text().contains(word, ignoreCase = true)) {
                // Si la palabra está presente, enviar una notificación
                sendNotification()
            }

        } catch (e: Exception) {
            e.printStackTrace()
            return Result.failure()
        }

        return Result.success()
    }

    /**
     * Método que envía una notificación al usuario cuando se encuentra la palabra en la página web.
     * Crea un canal de notificación (para API 26+) y muestra la notificación en la barra de estado.
     */
    private fun sendNotification() {
        val context = applicationContext

        // Crear el canal de notificación (solo necesario para API 26+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "guestlist_channel", // ID del canal
                "Guestlist Notification", // Nombre del canal
                NotificationManager.IMPORTANCE_DEFAULT // Importancia de la notificación
            ).apply {
                description = "Canal para notificaciones de palabras encontradas" // Descripción del canal
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel) // Crear el canal de notificación
        }

        // Crear y enviar la notificación
        val notification: Notification = NotificationCompat.Builder(context, "guestlist_channel")
            .setContentTitle("¡Se encontró la palabra!") // Título de la notificación
            .setContentText("La palabra fue encontrada en la página web.") // Texto de la notificación
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Icono de la notificación
            .setPriority(NotificationCompat.PRIORITY_DEFAULT) // Prioridad de la notificación
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1, notification) // Mostrar la notificación
    }
}
