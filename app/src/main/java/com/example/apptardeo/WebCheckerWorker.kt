package com.example.apptardeo

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.core.app.NotificationCompat
import org.jsoup.Jsoup

class WebCheckerWorker(appContext: Context, workerParams: WorkerParameters) : Worker(appContext, workerParams) {
    override fun doWork(): Result {
        // Obtener la URL y la palabra desde los datos de entrada
        val url = inputData.getString("url") ?: return Result.failure()
        val palabra = inputData.getString("palabra") ?: return Result.failure()

        return try {
            // Realizamos el scraping de la página
            val doc = Jsoup.connect(url).get()
            // Verificamos si la palabra está en el texto de la página
            if (doc.text().contains(palabra, ignoreCase = true)) {
                sendNotification(palabra)
            }
            Result.success() // Si todo salió bien, retornamos éxito
        } catch (e: Exception) {
            e.printStackTrace() // Imprimir el error para depuración
            Result.failure() // En caso de error, retornamos un fallo
        }
    }

    private fun sendNotification(palabra: String) {
        val context = applicationContext
        val channelId = "web_checker_channel"
        val channelName = "Web Checker Notifications"

        // Crear el canal de notificación (requerido para Android 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        // Crear la notificación
        val notification: Notification =
            NotificationCompat.Builder(context, channelId)
                .setContentTitle("¡Se encontró la palabra!")
                .setContentText("La palabra \"$palabra\" fue encontrada en la página web.")
                .setSmallIcon(android.R.drawable.ic_dialog_info) // Icono pequeño de la notificación
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()

        // Enviar la notificación
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1, notification)
    }
}
