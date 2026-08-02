package bg.travelgin.vtmiswind

import org.jsoup.Jsoup
import java.nio.charset.Charset

object VtmisScraper {

    private const val URL =
        "https://www.vtmis.bg/wx/meteo_sea.php"

    fun fetch(): List<WindReading> {
        val response = Jsoup.connect(URL)
            .userAgent(
                "Mozilla/5.0 (Linux; Android 16) " +
                    "AppleWebKit/537.36 Chrome/130 Mobile Safari/537.36"
            )
            .referrer("https://www.vtmis.bg/bg/meteobg")
            .ignoreContentType(true)
            .followRedirects(true)
            .timeout(30_000)
            .execute()

        val bytes = response.bodyAsBytes()

        val htmlUtf8 = bytes.toString(Charsets.UTF_8)
        val htmlWindows1251 =
            bytes.toString(Charset.forName("windows-1251"))

        val html = when {
            htmlUtf8.contains("РК Варна") ||
                htmlUtf8.contains("БЦ Варна") -> htmlUtf8

            htmlWindows1251.contains("РК Варна") ||
                htmlWindows1251.contains("БЦ Варна") -> htmlWindows1251

            else -> response.body()
        }

        val document = Jsoup.parse(html, URL)
        val wantedStations = listOf("РК Варна", "БЦ Варна")
        val results = mutableListOf<WindReading>()

        document.select("tr").forEach 
