package bg.travelgin.vtmiswind

import org.jsoup.Jsoup

object VtmisScraper {
    private const val URL = "https://www.vtmis.bg/bg/meteobg"

    fun fetch(): List<WindReading> {
        val document = Jsoup.connect(URL)
            .userAgent("Mozilla/5.0 (Android) VtmisWindWidget/1.0")
            .timeout(20_000)
            .get()

        val wanted = setOf("РК Варна", "БЦ Варна")

        return document.select("tr").mapNotNull { row ->
            val cells = row.select("th, td")
                .map { it.text().replace('\u00A0', ' ').trim() }
                .filter { it.isNotBlank() }

            if (cells.isEmpty()) return@mapNotNull null

            val stationIndex = cells.indexOfFirst { cell ->
                wanted.any { wantedName ->
                    cell.equals(wantedName, ignoreCase = true) ||
                        cell.replace(Regex("\\s+"), " ")
                            .contains(wantedName, ignoreCase = true)
                }
            }

            if (stationIndex < 0) return@mapNotNull null

            val station = wanted.first {
                cells[stationIndex].contains(it, ignoreCase = true)
            }

            val values = cells.drop(stationIndex + 1)
                .map { it.replace(',', '.') }
                .filter { it.matches(Regex("-?\\d+(\\.\\d+)?")) }

            if (values.size < 3) return@mapNotNull null

            WindReading(
                station = station,
                speedMs = values.getOrElse(0) { "—" },
                maxSpeedMs = values.getOrElse(1) { "—" },
                directionDeg = values.getOrElse(2) { "—" },
                speedKnots = values.getOrElse(3) { "—" },
                maxSpeedKnots = values.getOrElse(4) { "—" },
                temperatureC = values.getOrElse(5) { "—" }
            )
        }.distinctBy { it.station }
    }
}
