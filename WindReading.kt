package bg.travelgin.vtmiswind

data class WindReading(
    val station: String,
    val speedMs: String,
    val maxSpeedMs: String,
    val directionDeg: String,
    val speedKnots: String,
    val maxSpeedKnots: String,
    val temperatureC: String
)
