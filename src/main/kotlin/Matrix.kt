import java.lang.Integer.max
import kotlin.concurrent.thread

operator fun Number.plus(other: Number): Number {
    return when (this) {
        is Long -> this.toLong() + other.toLong()
        is Int -> this.toInt() + other.toInt()
        is Short -> this.toShort() + other.toShort()
        is Byte -> this.toByte() + other.toByte()
        is Double -> this.toDouble() + other.toDouble()
        is Float -> this.toFloat() + other.toFloat()
        else -> throw RuntimeException("Unknown numeric type")
    }
}

operator fun Number.minus(other: Number): Number {
    return when (this) {
        is Long -> this.toLong() - other.toLong()
        is Int -> this.toInt() - other.toInt()
        is Short -> this.toShort() - other.toShort()
        is Byte -> this.toByte() - other.toByte()
        is Double -> this.toDouble() - other.toDouble()
        is Float -> this.toFloat() - other.toFloat()
        else -> throw RuntimeException("Unknown numeric type")
    }
}


operator fun Number.times(other: Number): Number {
    return when (this) {
        is Long -> this.toLong() * other.toLong()
        is Int -> this.toInt() * other.toInt()
        is Short -> this.toShort() * other.toShort()
        is Byte -> this.toByte() * other.toByte()
        is Double -> this.toDouble() * other.toDouble()
        is Float -> this.toFloat() * other.toFloat()
        else -> throw RuntimeException("Unknown numeric type")
    }
}


data class Matrix<T : Number>(var rows: Int, var cols: Int, var isInitialize: Boolean = true) {
    private var array: MutableList<MutableList<T>> = mutableListOf()

    @Suppress("Unchecked_cast")
    private val zero: T = 0 as T

    private val minRows = 64 // If number of rows less equal than this value, naive algorithm will be used
    private val maxRows = 2048 // If number of rows greater equal than this value, sequential algorithm will be used


    init {
        if (isInitialize) {
            for (i in 0 until rows) {
                array.add(mutableListOf())
                for (j in 0 until cols) {
                    array[i].add(zero)
                }
            }
        }
    }

    private fun copyFrom(other: Matrix<T>) {
        for (i in 0 until rows) {
            for (j in 0 until cols) {
                array[i][j] = other[i][j]
            }
        }
    }

    private fun addPadding(maxN : Int): Matrix<T> {
        val n = findPowerOfTwo(maxN)
        val res = Matrix<T>(n, n)
        for (i in 0 until rows) {
            for (j in 0 until cols) {
                res[i][j] = array[i][j]
            }
        }
        return res
    }

    private fun findPowerOfTwo(n: Int): Int {
        var res = 1
        while (res < n)
            res *= 2
        return res
    }

    private fun getSubMatrix(rowMin: Int, rowMax: Int, colsMin: Int, colsMax: Int): Matrix<T> {
        assert(rowMax - rowMin > 0 && colsMax - colsMin > 0)
        val res = Matrix<T>(rowMax - rowMin, colsMax - colsMin, false)
        res.array = array.subList(rowMin, rowMax).map { it.subList(colsMin, colsMax) }.toMutableList()
        return res
    }

    fun print() {
        for (i in 0 until rows) {
            for (j in 0 until cols) {
                print("${array[i][j]} ")
            }
            println()
        }
    }

    operator fun get(x: Int, y: Int) = array[x][y]

    operator fun get(x: Int) = array[x]

    operator fun set(x: Int, y: Int, newValue: T) {
        array[x][y] = newValue
    }

    fun set(list: List<T>) {
        assert(list.size == rows * cols)
        for (i in 0 until rows) {
            for (j in 0 until cols) {
                array[i][j] = list[j + i * cols]
            }
        }
    }

    @Suppress("Unchecked_cast")
    operator fun plus(other: Matrix<T>): Matrix<T> {
        val res = Matrix<T>(rows, cols)
        for (i in 0 until rows) {
            for (j in 0 until cols) {
                res[i][j] = (array[i][j] + other[i][j]) as T
            }
        }
        return res
    }

    @Suppress("Unchecked_cast")
    operator fun plusAssign(other: Matrix<T>) {
        for (i in 0 until rows) {
            for (j in 0 until cols) {
                array[i][j] = (array[i][j] + other[i][j]) as T
            }
        }
    }

    @Suppress("Unchecked_cast")
    operator fun minus(other: Matrix<T>): Matrix<T> {
        val res = Matrix<T>(rows, cols)
        for (i in 0 until rows) {
            for (j in 0 until cols) {
                res[i][j] = (array[i][j] - other[i][j]) as T
            }
        }
        return res
    }

    @Suppress("Unchecked_cast")
    operator fun minusAssign(other: Matrix<T>) {
        for (i in 0 until rows) {
            for (j in 0 until cols) {
                array[i][j] = (array[i][j] - other[i][j]) as T
            }
        }
    }

    @Suppress("Unchecked_cast")
    fun defaultTimes(other: Matrix<T>): Matrix<T> {
        assert(cols == other.rows)
        val res = Matrix<T>(rows, other.cols)
        for (i in 0 until res.rows) {
            for (j in 0 until res.cols) {
                for (k in 0 until cols) {
                    res[i][j] = (res[i][j] + array[i][k] * other[k][j]) as T
                }
            }
        }
        return res
    }


    private fun splitMatrix(): List<List<Matrix<T>>> {
        val a11 = getSubMatrix(0, rows / 2, 0, cols / 2)
        val a12 = getSubMatrix(0, rows / 2, cols / 2, cols)
        val a21 = getSubMatrix(rows / 2, rows, 0, cols / 2)
        val a22 = getSubMatrix(rows / 2, rows, cols / 2, cols)
        return listOf(listOf(a11, a12), listOf(a21, a22))
    }

    private fun copyMatrix(): Matrix<T> {
        val res = Matrix<T>(rows, cols)
        for (i in 0 until rows) {
            for (j in 0 until cols) {
                res[i][j] = array[i][j]
            }
        }
        return res
    }


    private fun writeAnswer(splitA: List<List<Matrix<T>>>, f: List<Matrix<T>>) {
        splitA[0][0].copyFrom(f[0])
        splitA[0][0] += f[3]
        splitA[0][0] -= f[4]
        splitA[0][0] += f[6]

        splitA[0][1].copyFrom(f[2])
        splitA[0][1] += f[4]

        splitA[1][0].copyFrom(f[1])
        splitA[1][0] += f[3]

        splitA[1][1].copyFrom(f[0])
        splitA[1][1] -= f[1]
        splitA[1][1] += f[2]
        splitA[1][1] += f[5]
    }

    /**
     * This version of algorithm uses more memory, but recursive calls run parallel
     */
    private fun strassen(other: Matrix<T>) {
        if (rows <= minRows) {
            copyFrom(defaultTimes(other))
            return
        }
        val splitA = splitMatrix()
        val splitB = other.splitMatrix()

        val f: MutableList<Matrix<T>> = mutableListOf()
        val g: MutableList<Matrix<T>> = mutableListOf()

        f.add(splitA[0][0] + splitA[1][1])
        f.add(splitA[1][0] + splitA[1][1])
        f.add(splitA[0][0].copyMatrix())
        f.add(splitA[1][1].copyMatrix())
        f.add(splitA[0][0] + splitA[0][1])
        f.add(splitA[1][0] - splitA[0][0])
        f.add(splitA[0][1] - splitA[1][1])

        g.add(splitB[0][0] + splitB[1][1])
        g.add(splitB[0][0])
        g.add(splitB[0][1] - splitB[1][1])
        g.add(splitB[1][0] - splitB[0][0])
        g.add(splitB[1][1])
        splitB[0][1] += splitB[0][0]
        g.add(splitB[0][1])
        splitB[1][0] += splitB[1][1]
        g.add(splitB[1][0])


        val threadPull: MutableList<Thread> = mutableListOf()
        for (i in 0 until 7) {
            threadPull.add(
                thread {
                    f[i].strassen2(g[i])
                }
            )
        }
        for (i in 0 until 7) {
            threadPull[i].join()
        }

        writeAnswer(splitA, f)
    }

    /**
     * This version of algorithm uses less memory, but recursive calls run sequentially
     */
    private fun strassen2(other: Matrix<T>) {
        if (rows <= minRows) {
            copyFrom(defaultTimes(other))
            return
        }
        val splitA = splitMatrix()
        val splitB = other.splitMatrix()

        val f: MutableList<Matrix<T>> = mutableListOf()


        f.add(splitA[0][0] + splitA[1][1])
        f[0].strassen2(splitB[0][0] + splitB[1][1])


        f.add(splitA[1][0] + splitA[1][1])
        f[1].strassen2(splitB[0][0])

        f.add(splitA[0][0].copyMatrix())
        f[2].strassen2(splitB[0][1] - splitB[1][1])

        f.add(splitA[1][1].copyMatrix())
        f[3].strassen2(splitB[1][0] - splitB[0][0])

        f.add(splitA[0][0] + splitA[0][1])
        f[4].strassen2(splitB[1][1])

        f.add(splitA[1][0] - splitA[0][0])
        f[5].strassen2(splitB[0][0] + splitB[0][1])

        f.add(splitA[0][1] - splitA[1][1])
        f[6].strassen2(splitB[1][0] + splitB[1][1])


        writeAnswer(splitA, f)
    }


    operator fun times(other: Matrix<T>): Matrix<T> {
        assert(cols == other.rows)
        val maxN = max(max(rows, cols), other.cols)
        val a = addPadding(maxN)
        val b = other.addPadding(maxN)
        if(a.rows >= maxRows)
            a.strassen2(b)
        else
            a.strassen(b)
        return a.getSubMatrix(0, rows, 0, other.cols)
    }
}

