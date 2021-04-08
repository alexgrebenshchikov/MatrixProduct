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

    private fun addPadding(): Matrix<T> {
        val n = findPowerOfTwo(kotlin.math.max(rows, cols))
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

    private fun strassen(other: Matrix<T>) {
        if (rows <= 64) {
            copyFrom(defaultTimes(other))
            return
        }
        val splitA = splitMatrix()
        val splitB = other.splitMatrix()

        val f1 = splitA[0][0] + splitA[1][1]
        val f2 = splitA[1][0] + splitA[1][1]
        val f3 = splitA[0][0]
        val f4 = splitA[1][1]
        val f5 = splitA[0][0] + splitA[0][1]
        val f6 = splitA[1][0] - splitA[0][0]
        val f7 = splitA[0][1] - splitA[1][1]

        val g1 = splitB[0][0] + splitB[1][1]
        val g2 = splitB[0][0]
        val g3 = splitB[0][1] - splitB[1][1]
        val g4 = splitB[1][0] - splitB[0][0]
        val g5 = splitB[1][1]
        val g6 = splitB[0][0] + splitB[0][1]
        val g7 = splitB[1][0] + splitB[1][1]

        f1.strassen(g1)
        f2.strassen(g2)
        f3.strassen(g3)
        f4.strassen(g4)
        f5.strassen(g5)
        f6.strassen(g6)
        f7.strassen(g7)


        val c11 = f1 + f4 - f5 + f7
        val c12 = f3 + f5
        val c21 = f2 + f4
        val c22 = f1 - f2 + f3 + f6


        splitA[0][0].copyFrom(c11)
        splitA[0][1].copyFrom(c12)
        splitA[1][0].copyFrom(c21)
        splitA[1][1].copyFrom(c22)
    }

    operator fun times(other: Matrix<T>): Matrix<T> {
        assert(cols == other.rows)
        val a = addPadding()
        val b = other.addPadding()
        a.strassen(b)
        return a.getSubMatrix(0, rows, 0, other.cols)
    }
}

