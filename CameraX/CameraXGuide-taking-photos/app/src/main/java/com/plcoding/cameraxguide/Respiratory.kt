import kotlin.math.abs
import kotlin.math.pow

public fun respiratoryRateCalculator(
    accelValuesX: MutableList<Float>,
    accelValuesY: MutableList<Float>,
    accelValuesZ: MutableList<Float>
): Int {
    var previousValue = 0f // Better to initialize to 0 instead of 10f
    var currentValue: Float
    var k = 0

    if (accelValuesY.size > 11) {
        // Initialize previousValue with the first meaningful value
        previousValue = kotlin.math.sqrt(
            accelValuesZ[10].toDouble().pow(2.0) +
                    accelValuesX[10].toDouble().pow(2.0) +
                    accelValuesY[10].toDouble().pow(2.0)
        ).toFloat()
    }

    // Loop through the list starting at index 11
    for (i in 11 until accelValuesY.size) {
        currentValue = kotlin.math.sqrt(
            accelValuesZ[i].toDouble().pow(2.0) +
                    accelValuesX[i].toDouble().pow(2.0) +
                    accelValuesY[i].toDouble().pow(2.0)
        ).toFloat()

        if (abs(previousValue - currentValue) > 0.15) {
            k++
        }
        previousValue = currentValue
    }

    // Assuming data is collected over 45 seconds, compute the respiratory rate
    val rate = (k.toDouble() / 45.0) * 30
    return rate.toInt()
}

