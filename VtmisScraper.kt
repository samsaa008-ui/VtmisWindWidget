package bg.travelgin.vtmiswind

import org.jsoup.Jsoup

object VtmisScraper {

    private const val URL = "https://vtmis.bg/wx/meteo.php"

    fun fetch(): List<WindReading> {
        val document = Jsoup.connect(URL)
            .userAgent(
                "Mozilla/5.0 (Linux; Android 16) " +
                    "AppleWebKit/537.36 Chrome/130 Mobile Safari/537.36"
            )
            .referrer("https://www.vtmis.bg/bg/meteobg")
            .timeout(30_000)
            .followRedirects(true)
            .get()

        val wantedStations = listOf("РК Варна", "БЦ Варна")
        val results = mutableListOf<WindReading>()

        document.select("tr").forEach { row ->
            val cells = row.select("th, td")
                .map { cell ->
                    cell.text()
                        .replace('\u00A0', ' ')
                        .replace(Regex("\\s+"), " ")
                        .trim()
                }
                .filter { it.isNotBlank() }

            if (cells.isEmpty()) {
                return@forEach
            }

            val station = wantedStations.firstOrNull { wanted ->
                cells.first().equals(wanted, ignoreCase = true) ||
                    cells.first().contains(wanted, ignoreCase = true)
            } ?: return@forEach

            val values = cells.drop(1)
                .map { value ->
                    value.replace(',', '.').trim()
                }

            if (values.size >= 3) {
                results.add(
                    WindReading(
                        station = station,
                        speedMs = values.getOrElse(0) { "—" },
                        maxSpeedMs = values.getOrElse(1) { "—" },
                        directionDeg = values.getOrElse(2) { "—" },
                        speedKnots = values.getOrElse(3) { "—" },
                        maxSpeedKnots = values.getOrElse(4) { "—" },
                        temperatureC = values.getOrElse(5) { "—" }
                    )
                )
            }
        }

        return results.distinctBy { it.station }
    }
} 
