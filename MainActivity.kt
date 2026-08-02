package bg.travelgin.vtmiswind

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import bg.travelgin.vtmiswind.databinding.ActivityMainBinding
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        showSavedData()

        binding.refreshNow.setOnClickListener {
            refreshDirectly()
        }
    }

    override fun onResume() {
        super.onResume()
        showSavedData()
    }

    private fun refreshDirectly() {
        binding.refreshNow.isEnabled = false
        binding.status.text = "Изтегляне на данните…"

        thread {
            try {
                val readings = VtmisScraper.fetch()
                val stations = readings.map { it.station }.toSet()

                if (
                    stations.contains("РК Варна") &&
                    stations.contains("БЦ Варна")
                ) {
                    WindRepository.save(this, readings)

                    runOnUiThread {
                        showSavedData()
                        binding.refreshNow.isEnabled = true
                    }

                    WindWidgetProvider.updateAll(this)
                } else {
                    runOnUiThread {
                        binding.status.text =
                            "Не са намерени двата датчика. " +
                            "Получени редове: ${readings.size}"
                        binding.refreshNow.isEnabled = true
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    binding.status.text =
                        "Грешка: ${e.javaClass.simpleName}: " +
                            (e.message ?: "неизвестна грешка")
                    binding.refreshNow.isEnabled = true
                }
            }
        }
    }

    private fun showSavedData() {
        val updated = WindRepository.value(
            this,
            "updated_at",
            "още няма данни"
        )

        binding.status.text =
            "Последно обновяване: $updated"

        binding.rkPreview.text =
            "РК Варна: " +
                "${WindRepository.value(this, "rk_speed")} m/s · " +
                "порив ${WindRepository.value(this, "rk_max")} m/s · " +
                "${WindRepository.value(this, "rk_direction")}°"

        binding.bcPreview.text =
            "БЦ Варна: " +
                "${WindRepository.value(this, "bc_speed")} m/s · " +
                "порив ${WindRepository.value(this, "bc_max")} m/s · " +
                "${WindRepository.value(this, "bc_direction")}°"
    }
}
