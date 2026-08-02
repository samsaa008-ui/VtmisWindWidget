package bg.travelgin.vtmiswind

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.RemoteViews
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class WindWidgetProvider : AppWidgetProvider() {

    override fun onEnabled(context: Context) {
        schedulePeriodicRefresh(context)
        requestRefresh(context)
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { appWidgetId ->
            appWidgetManager.updateAppWidget(
                appWidgetId,
                buildViews(context, appWidgetId)
            )
        }
        schedulePeriodicRefresh(context)
        requestRefresh(context)
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle
    ) {
        appWidgetManager.updateAppWidget(
            appWidgetId,
            buildViews(context, appWidgetId)
        )
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH) {
            updateAll(context, loading = true)
            requestRefresh(context)
        }
    }

    companion object {
        const val ACTION_REFRESH = "bg.travelgin.vtmiswind.ACTION_REFRESH"
        private const val PERIODIC_WORK = "vtmis_periodic_refresh"
        private const val MANUAL_WORK = "vtmis_manual_refresh"

        fun updateAll(context: Context, loading: Boolean = false) {
            val manager = AppWidgetManager.getInstance(context)
            val component = ComponentName(
                context,
                WindWidgetProvider::class.java
            )

            manager.getAppWidgetIds(component).forEach { appWidgetId ->
                manager.updateAppWidget(
                    appWidgetId,
                    buildViews(context, appWidgetId, loading)
                )
            }
        }

        private fun buildViews(
            context: Context,
            appWidgetId: Int,
            loading: Boolean = false
        ): RemoteViews {
            val views = RemoteViews(
                context.packageName,
                R.layout.widget_wind
            )

            val rkSpeed = WindRepository.value(context, "rk_speed")
            val rkGust = WindRepository.value(context, "rk_max")
            val rkDirection = WindRepository.value(
                context,
                "rk_direction"
            )
            val bcSpeed = WindRepository.value(context, "bc_speed")
            val bcGust = WindRepository.value(context, "bc_max")
            val bcDirection = WindRepository.value(
                context,
                "bc_direction"
            )

            views.setTextViewText(R.id.rk_speed, rkSpeed)
            views.setTextViewText(R.id.rk_gust, rkGust)
            views.setTextViewText(
                R.id.rk_direction,
                formatDegrees(rkDirection)
            )
            views.setTextViewText(
                R.id.rk_direction_name,
                directionShortName(rkDirection)
            )

            views.setTextViewText(R.id.bc_speed, bcSpeed)
            views.setTextViewText(R.id.bc_gust, bcGust)
            views.setTextViewText(
                R.id.bc_direction,
                formatDegrees(bcDirection)
            )
            views.setTextViewText(
                R.id.bc_direction_name,
                directionShortName(bcDirection)
            )

            rkDirection.replace(',', '.').toFloatOrNull()?.let {
                views.setFloat(
                    R.id.rk_arrow,
                    "setRotation",
                    normalizeDegrees(it + 180f)
                )
            }

            bcDirection.replace(',', '.').toFloatOrNull()?.let {
                views.setFloat(
                    R.id.bc_arrow,
                    "setRotation",
                    normalizeDegrees(it + 180f)
                )
            }

            val updated = WindRepository.value(
                context,
                "updated_at",
                "няма данни"
            )

            views.setTextViewText(
                R.id.updated_at,
                if (loading) "Обновяване…" else "Обновено $updated"
            )

            attachClicks(context, views, appWidgetId)
            return views
        }

        private fun formatDegrees(value: String): String {
            return if (value == "—") "—" else "$value°"
        }

        private fun normalizeDegrees(value: Float): Float {
            return ((value % 360f) + 360f) % 360f
        }

        private fun directionShortName(value: String): String {
            val degrees = value.replace(',', '.').toDoubleOrNull()
                ?: return "—"

            val normalized = ((degrees % 360.0) + 360.0) % 360.0
            val index = ((normalized + 22.5) / 45.0).toInt() % 8

            return listOf(
                "С", "СИ", "И", "ЮИ",
                "Ю", "ЮЗ", "З", "СЗ"
            )[index]
        }

        private fun attachClicks(
            context: Context,
            views: RemoteViews,
            appWidgetId: Int
        ) {
            val refreshIntent = Intent(
                context,
                WindWidgetProvider::class.java
            ).apply {
                action = ACTION_REFRESH
            }

            val refreshPendingIntent = PendingIntent.getBroadcast(
                context,
                1000 + appWidgetId,
                refreshIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
            )

            views.setOnClickPendingIntent(
                R.id.refresh_button,
                refreshPendingIntent
            )

            val openAppIntent = Intent(
                context,
                MainActivity::class.java
            ).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
            }

            val openAppPendingIntent = PendingIntent.getActivity(
                context,
                2000 + appWidgetId,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
            )

            views.setOnClickPendingIntent(
                R.id.widget_root,
                openAppPendingIntent
            )
        }

        fun requestRefresh(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = OneTimeWorkRequestBuilder<WindUpdateWorker>()
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                MANUAL_WORK,
                ExistingWorkPolicy.REPLACE,
                request
            )
        }

        private fun schedulePeriodicRefresh(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = PeriodicWorkRequestBuilder<WindUpdateWorker>(
                15,
                TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                PERIODIC_WORK,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }
    }
}
