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
        binding.status.text = "Обновяване…"

        thread {
            try {
                val readings = VtmisScraper.fetch()
                val stations = readings.map { it.station }.toSet()

                if (
                    stations.contains("РК Варна") &&
                    stations.contains("БЦ Варна")
                ) {
                    WindRepository.save(this, readings)
                    WindWidgetProvider.updateAll(this)

                    runOnUiThread {
                        showSavedData()
                        binding.refreshNow.isEnabled = true
                    }
                } else {
                    runOnUiThread {
                        binding.status.text = "Не са намерени двата датчика"
                        binding.refreshNow.isEnabled = true
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    binding.status.text =
                        "Грешка: ${e.message ?: "неизвестна грешка"}"
                    binding.refreshNow.isEnabled = true
                }
            }
        }
    }

    private fun showSavedData() {
        val rkSpeed = WindRepository.value(this, "rk_speed")
        val rkGust = WindRepository.value(this, "rk_max")
        val rkDirection = WindRepository.value(this, "rk_direction")

        val bcSpeed = WindRepository.value(this, "bc_speed")
        val bcGust = WindRepository.value(this, "bc_max")
        val bcDirection = WindRepository.value(this, "bc_direction")

        binding.rkSpeedValue.text = rkSpeed
        binding.rkGustValue.text = rkGust
        binding.rkDirectionValue.text = "$rkDirection°"
        binding.rkDirectionName.text = directionName(rkDirection)

        binding.bcSpeedValue.text = bcSpeed
        binding.bcGustValue.text = bcGust
        binding.bcDirectionValue.text = "$bcDirection°"
        binding.bcDirectionName.text = directionName(bcDirection)

        /*
         * Данните показват ОТКЪДЕ идва вятърът.
         * Стрелката трябва да сочи НАКЪДЕ духа,
         * затова добавяме 180 градуса.
         */
        rkDirection.replace(',', '.').toFloatOrNull()?.let {
            binding.rkArrow.rotation = normalizeDegrees(it + 180f)
        }

        bcDirection.replace(',', '.').toFloatOrNull()?.let {
            binding.bcArrow.rotation = normalizeDegrees(it + 180f)
        }

        val updated = WindRepository.value(
            this,
            "updated_at",
            "още няма данни"
        )

        binding.status.text = "Обновено $updated"
    }

    private fun normalizeDegrees(value: Float): Float {
        return ((value % 360f) + 360f) % 360f
    }

    private fun directionName(value: String): String {
        val degrees = value.replace(',', '.').toDoubleOrNull()
            ?: return "—"

        val normalized = ((degrees % 360.0) + 360.0) % 360.0
        val index = ((normalized + 22.5) / 45.0).toInt() % 8

        return listOf(
            "С (СЕВЕР)",
            "СИ (СЕВЕРОИЗТОК)",
            "И (ИЗТОК)",
            "ЮИ (ЮГОИЗТОК)",
            "Ю (ЮГ)",
            "ЮЗ (ЮГОЗАПАД)",
            "З (ЗАПАД)",
            "СЗ (СЕВЕРОЗАПАД)"
        )[index]
    }
}

