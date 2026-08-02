package bg.travelgin.vtmiswind

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import bg.travelgin.vtmiswind.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.refreshNow.setOnClickListener {
            WindWidgetProvider.requestRefresh(this)
            binding.status.text = "Заявено е обновяване…"
        }
    }

    override fun onResume() {
        super.onResume()

        val updated = WindRepository.value(this, "updated_at", "още няма данни")
        binding.status.text = "Последно обновяване: $updated"

        binding.rkPreview.text =
            "РК Варна: ${WindRepository.value(this, "rk_speed")} m/s · " +
                "порив ${WindRepository.value(this, "rk_max")} m/s · " +
                "${WindRepository.value(this, "rk_direction")}°"

        binding.bcPreview.text =
            "БЦ Варна: ${WindRepository.value(this, "bc_speed")} m/s · " +
                "порив ${WindRepository.value(this, "bc_max")} m/s · " +
                "${WindRepository.value(this, "bc_direction")}°"
    }
}
