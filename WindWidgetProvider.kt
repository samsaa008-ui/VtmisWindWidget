package bg.travelgin.vtmiswind

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
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
        appWidgetIds.forEach { id ->
            appWidgetManager.updateAppWidget(id, buildViews(context))
        }
        schedulePeriodicRefresh(context)
        requestRefresh(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH) {
            requestRefresh(context)
            updateAll(context, loading = true)
        }
    }

    companion object {
        const val ACTION_REFRESH = "bg.travelgin.vtmiswind.ACTION_REFRESH"
        private const val PERIODIC_WORK = "vtmis_periodic_refresh"
        private const val MANUAL_WORK = "vtmis_manual_refresh"

        fun updateAll(context: Context, loading: Boolean = false) {
            val manager = AppWidgetManager.getInstance(context)
            val component = ComponentName(context, WindWidgetProvider::class.java)
            manager.getAppWidgetIds(component).forEach { id ->
                manager.updateAppWidget(id, buildViews(context, loading))
            }
        }

        private fun buildViews(
            context: Context,
            loading: Boolean = false
        ): RemoteViews {
            val views = RemoteViews(context.packageName, R.layout.widget_wind)

            views.setTextViewText(
                R.id.rk_speed,
                "${WindRepository.value(context, "rk_speed")} m/s"
            )
            views.setTextViewText(
                R.id.rk_max,
                "порив ${WindRepository.value(context, "rk_max")} m/s"
            )
            views.setTextViewText(
                R.id.rk_direction,
                "${WindRepository.value(context, "rk_direction")}°"
            )

            views.setTextViewText(
                R.id.bc_speed,
                "${WindRepository.value(context, "bc_speed")} m/s"
            )
            views.setTextViewText(
                R.id.bc_max,
                "порив ${WindRepository.value(context, "bc_max")} m/s"
            )
            views.setTextViewText(
                R.id.bc_direction,
                "${WindRepository.value(context, "bc_direction")}°"
            )

            val updated = WindRepository.value(context, "updated_at", "няма данни")
            views.setTextViewText(
                R.id.updated_at,
                if (loading) "Обновяване…" else "Обновено: $updated"
            )

            val refreshIntent = Intent(context, WindWidgetProvider::class.java).apply {
                action = ACTION_REFRESH
            }
            val refreshPendingIntent = PendingIntent.getBroadcast(
                context,
                10,
                refreshIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.refresh_button, refreshPendingIntent)

            val websiteIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://www.vtmis.bg/bg/meteobg")
            )
            val websitePendingIntent = PendingIntent.getActivity(
                context,
                11,
                websiteIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, websitePendingIntent)

            return views
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
