import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.*
import io.ktor.client.request.get
import kotlinx.coroutines.*

private const val BASE_URL = "http://kotlin-book.bignerdranch.com/2e"
private const val FLIGHT_ENDPOINT = "$BASE_URL/flight"
private const val LOYALTY_ENDPOINT = "$BASE_URL/loyalty"

// Look at dispatcher IO
//fun main() {
//    runBlocking {
//        println("Started...")
//        launch {
//            val flight = fetchFlight("John")
//            println(flight)
//        }
//        println("...Finished")
//    }
//}

suspend fun fetchFlight(passengerName: String): FlightStatus = coroutineScope {
    var client = HttpClient(CIO)
    val flightResponse = async {
        println("Started fetching flight info...")
        client.get<String>(FLIGHT_ENDPOINT).also {
            println("Finished Fetching Flight Info")
        }
    }
    val loyaltyResponse = async {
        client.get<String>(LOYALTY_ENDPOINT).also {
            println("Finished Fetching Loyalty Info")
        }
    }
    delay(500)
    println("Combining flight data")
    FlightStatus.parse(
        passengerName = passengerName,
        flightResponse = flightResponse.await(),
        loyaltyResponse = loyaltyResponse.await()
    )
}
