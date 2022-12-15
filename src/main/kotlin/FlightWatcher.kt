import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import BoardingState.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun main() {
    runBlocking {
        println("Getting latest flight info...")
        val flights = fetchFlights()
        val flightDescription = flights.joinToString {
            "${it.passengerName} (${it.flightNumber})"
        }

        println("Found Flights for $flightDescription")
        val flightsAtGate = MutableStateFlow(flights.size)
        launch {
            flightsAtGate.collect { flighCount ->
                println("There are $flighCount flights being tracked")
            }
        }
        println("Finished tracking all flights")
         launch{
            flights.forEach {
                watchFlight(it)
                flightsAtGate.value = flightsAtGate.value - 1
            }
        }
    }
}

suspend fun watchFlight(initialFlight: FlightStatus) {
    val passengerName = initialFlight.passengerName
    val currentFlight: Flow<FlightStatus> = flow {
        var flight = initialFlight
        while (flight.departureTimeInMinutes >= 0 && !flight.isFlightCanceled) {
            emit(flight)
            delay(1000)
            flight = flight.copy(departureTimeInMinutes = flight.departureTimeInMinutes -1)
        }
    }
    currentFlight.collect {
        val status = when (it.boardingStatus) {
            FlightCanceled -> "Your Flight is canceled"
            BoardingNotStarted -> "Boarding will start soon"
            WaitingToBoard -> "Other passengers are boarding"
            Boarding -> "You can now board the plane"
            BoardingEnded -> "The boarding doors have closed"
        } + " (Flight Departs in ${it.departureTimeInMinutes} minutes"
        println("$passengerName: $status")
    }

    println("Finished tracking $passengerName's flight")
}

suspend fun fetchFlights(
        passengerNames: List<String> = listOf("John", "Becky")
    ) = passengerNames.map { fetchFlight(it)}