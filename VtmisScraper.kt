package bg.travelgin.vtmiswind

import org.jsoup.Jsoup

object VtmisScraper {

    private const val URL = "https://www.vtmis.bg/wx/meteo_sea.php"

    fun fetch(): List<WindReading> {
        val document = Jsoup.connect(URL)
            .userAgent("Mozilla/5.0")
            .referrer("https://www.vtmis.bg/bg/meteobg")
            .timeout(30000)
            .get()

        val wanted = listOf("РК Варна", "БЦ Варна")
        val results = mutableListOf<WindReading>()

        for (row in document.select("tr")) {
            val cells = row.select("th, td")
                .map {
                    it.text()
                        .replace('\u00A0', ' ')
                        .replace(Regex("\\s+"), " ")
                        .trim()
                }
                .filter { it.isNotBlank() }

            if (cells.isEmpty()) continue

            val stationIndex = cells.indexOfFirst { cell ->
                wanted.any { name ->
                    cell.contains(name, ignoreCase = true)
                }
            }

            if (stationIndex < 0) continue

            val station = wanted.first { name ->
                cells[stationIndex].contains(name, ignoreCase = true)
            }

            val values = cells
                .drop(stationIndex + 1)
                .map { it.replace(',', '.').trim() }
                .filter { it.matches(Regex("-?\\d+(\\.\\d+)?")) }

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
