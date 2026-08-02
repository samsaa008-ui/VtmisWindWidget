package bg.travelgin.vtmiswind

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object WindRepository {
    private const val PREFS = "wind_data"

    fun save(context: Context, readings: List<WindReading>) {
        val editor = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()

        readings.forEach { reading ->
            val prefix = if (reading.station == "РК Варна") "rk" else "bc"
            editor.putString("${prefix}_speed", reading.speedMs)
            editor.putString("${prefix}_max", reading.maxSpeedMs)
            editor.putString("${prefix}_direction", reading.directionDeg)
            editor.putString("${prefix}_knots", reading.speedKnots)
            editor.putString("${prefix}_max_knots", reading.maxSpeedKnots)
            editor.putString("${prefix}_temp", reading.temperatureC)
        }

        val updatedAt = SimpleDateFormat("dd.MM HH:mm", Locale("bg", "BG")).format(Date())
        editor.putString("updated_at", updatedAt)
        editor.putBoolean("has_data", readings.isNotEmpty())
        editor.apply()
    }

    fun saveError(context: Context, message: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString("last_error", message.take(180))
            .apply()
    }

    fun value(context: Context, key: String, fallback: String = "—"): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(key, fallback) ?: fallback

    fun hasData(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean("has_data", false)
}
