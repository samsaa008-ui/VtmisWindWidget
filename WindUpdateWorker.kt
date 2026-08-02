package bg.travelgin.vtmiswind

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WindUpdateWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val readings = VtmisScraper.fetch()
            val names = readings.map { it.station }.toSet()

            if (!names.containsAll(setOf("РК Варна", "БЦ Варна"))) {
                WindRepository.saveError(
                    applicationContext,
                    "Не са намерени и двата реда в таблицата."
                )
                WindWidgetProvider.updateAll(applicationContext)
                return@withContext Result.retry()
            }

            WindRepository.save(applicationContext, readings)
            WindWidgetProvider.updateAll(applicationContext)
            Result.success()
        } catch (e: Exception) {
            WindRepository.saveError(
                applicationContext,
                e.message ?: e.javaClass.simpleName
            )
            WindWidgetProvider.updateAll(applicationContext)
            Result.retry()
        }
    }
}
