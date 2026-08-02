package bg.travelgin.vtmiswind

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
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
        appWidgetIds.forEach { id ->
            appWidgetManager.updateAppWidget(
                id,
                buildViews(context, appWidgetManager, id)
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
            buildViews(context, appWidgetManager, appWidgetId)
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
        private const val SMALL_WIDTH_DP = 220

        fun updateAll(context: Context, loading: Boolean = false) {
            val manager = AppWidgetManager.getInstance(context)
            val component = ComponentName(context, WindWidgetProvider::class.java)

            manager.getAppWidgetIds(component).forEach { id ->
                manager.updateAppWidget(
                    id,
                    buildViews(context, manager, id, loading)
                )
            }
        }

        private fun buildViews(
            context: Context,
            manager: AppWidgetManager,
            appWidgetId: Int,
            loading: Boolean = false
        ): RemoteViews {
            val minWidth = manager.getAppWidgetOptions(appWidgetId)
                .getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)

            return if (minWidth in 1 until SMALL_WIDTH_DP) {
                buildSmallViews(context, appWidgetId, loading)
            } else {
                buildLargeViews(context, appWidgetId, loading)
            }
        }

        private fun buildSmallViews(
            context: Context,
            appWidgetId: Int,
            loading: Boolean
        ): RemoteViews {
            val views = RemoteViews(
                context.packageName,
                R.layout.widget_wind_small
            )

            val rkSpeed = WindRepository.value(context, "rk_speed")
            val bcSpeed = WindRepository.value(context, "bc_speed")
            val updated = WindRepository.value(context, "updated_at", "—")

            views.setTextViewText(R.id.rk_speed_small, "$rkSpeed m/s")
            views.setTextViewText(R.id.bc_speed_small, "$bcSpeed m/s")
            views.setTextColor(R.id.rk_speed_small, speedColor(rkSpeed))
            views.setTextColor(R.id.bc_speed_small, speedColor(bcSpeed))
            views.setTextViewText(
                R.id.updated_at_small,
                if (loading) "Обновяване…" else updated
            )

            attachClicks(context, views, appWidgetId)
            return views
        }

        private fun buildLargeViews(
            context: Context,
            appWidgetId: Int,
            loading: Boolean
        ): RemoteViews {
            val views = RemoteViews(
                context.packageName,
                R.layout.widget_wind
            )

            val rkSpeed = WindRepository.value(context, "rk_speed")
            val bcSpeed = WindRepository.value(context, "bc_speed")

            views.setTextViewText(R.id.rk_speed, "$rkSpeed m/s")
            views.setTextViewText(
                R.id.rk_max,
                "Порив ${WindRepository.value(context, "rk_max")} m/s"
            )
            views.setTextViewText(
                R.id.rk_direction,
                "Посока ${WindRepository.value(context, "rk_direction")}°"
            )

            views.setTextViewText(R.id.bc_speed, "$bcSpeed m/s")
            views.setTextViewText(
                R.id.bc_max,
                "Порив ${WindRepository.value(context, "bc_max")} m/s"
            )
            views.setTextViewText(
                R.id.bc_direction,
                "Посока ${WindRepository.value(context, "bc_direction")}°"
            )

            views.setTextColor(R.id.rk_speed, speedColor(rkSpeed))
            views.setTextColor(R.id.bc_speed, speedColor(bcSpeed))

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

        private fun speedColor(value: String): Int {
            val speed = value.replace(',', '.').toDoubleOrNull() ?: 0.0

            return when {
                speed < 5.0 -> Color.parseColor("#73E6C2")
                speed < 10.0 -> Color.parseColor("#FFD166")
                else -> Color.parseColor("#FF7B7B")
            }
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
