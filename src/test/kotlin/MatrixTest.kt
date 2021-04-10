import org.junit.Test
import kotlin.test.assertTrue
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

class MatrixTest {
    private val a = Matrix<Int>(4, 4)
    private val b = Matrix<Int>(4, 4)
    init {
        a.set(listOf(11, 2, 34, 111,
                    452, 12, 55, 66,
                    332, 43, 435, 20,
                    90, 123, 54, 33))
        b.set(listOf(17, 212, 4, 71,
                    42, 162, 5, 626,
                    3, 433, 415, 250,
                    903, 13, 4, 353))
    }

    private fun <T : Number>check(a : Matrix<T>, b : Matrix<T>) : Boolean {
        if(a.rows != b.rows || a.cols != b.cols)
            return false
        for (i in 0 until a.rows) {
            for (j in 0 until a.cols) {
                if(a[i][j] != b[i][j])
                    return false
            }
        }
        return true
    }

    @Test
    fun testPLus()  {
        val res = a + b
        val e = Matrix<Int>(4, 4)
        e.set(listOf( 28, 214, 38, 182,
                      494, 174, 60, 692,
                      335, 476, 850, 270,
                      993, 136, 58, 386 ))
        assertTrue(check(res, e))

    }

    @Test
    fun testMinus()  {
        val res = a - b
        val e = Matrix<Int>(4, 4)
        e.set(listOf( -6, -210, 30, 40,
                    410, -150, 50, -560,
                    329, -390, 20, -230,
                    -813, 110, 50, -320 ))
        assertTrue(check(res, e))

    }

    @Test
    fun testDefaultTimes()  {
        val res = a.defaultTimes(b)
        val e = Matrix<Int>(4, 4)
        e.set(listOf( 100606, 18821, 14608, 49716,
                        67951, 122441, 24957, 76652,
                        26815, 265965, 182148, 166300,
                        36657, 62817, 23517, 108537 ))
        assertTrue(check(res, e))
    }

    @Test
    fun testStrassenTimes() {
        val n = 500
        val a = Matrix<Double>(n, n)
        for(i in 0 until n) {
            for(j in 0 until n) {
                a[i][j] = (i + j).toDouble()
            }
        }
        assertTrue(check(a * a, a.defaultTimes(a)))
    }

    @Test
    fun testStrassenTimes2() {
        val a = Matrix<Double>(50, 100)
        val b = Matrix<Double>(100, 200)
        for(i in 0 until 50) {
            for(j in 0 until 100) {
                a[i][j] = (i + j + 42).toDouble()
            }
        }
        for(i in 0 until 100) {
            for(j in 0 until 50) {
                b[i][j] = (i - j + 69).toDouble()
            }
        }
        assertTrue(check(a * b, a.defaultTimes(b)))
    }


    @ExperimentalTime
    @Test
    fun testSpeed() {
        val n = 1000
        val a = Matrix<Int>(n, n)
        for(i in 0 until n) {
            for(j in 0 until n) {
                a[i][j] = (i + j) % 300
            }
        }
        val duration1 = measureTime {
            val b = a.defaultTimes(a)
        }
        println("Time defaultTimes: ${duration1.inMilliseconds} ms\n")

        val duration2 = measureTime {
            val b = a * a
        }
        println("Time strassenTimes: ${duration2.inMilliseconds} ms\n")
    }

    @ExperimentalTime
    @Test
    fun testSpeed2() {
        val n = 2000
        val a = Matrix<Double>(n, n)
        for(i in 0 until n) {
            for(j in 0 until n) {
                a[i][j] = (i + j).toDouble()
            }
        }

        val duration2 = measureTime {
            val b = a * a
        }
        println("Time strassenTimes 2: ${duration2.inMilliseconds} ms\n")
    }

}
