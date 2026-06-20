package com.pushup.alarm.ui.challenge

import kotlin.random.Random

data class MathProblem(
    val question: String,
    val answer: Int
)

class MathChallengeFallback(
    private val problemCount: Int = 5
) {
    private var currentProblemIndex = 0
    private val problems = mutableListOf<MathProblem>()

    init {
        generateProblems()
    }

    private fun generateProblems() {
        problems.clear()
        repeat(problemCount) {
            val a = Random.nextInt(1, 50)
            val b = Random.nextInt(1, 50)
            if (Random.nextBoolean()) {
                problems.add(MathProblem("$a + $b =", a + b))
            } else {
                val max = maxOf(a, b)
                val min = minOf(a, b)
                problems.add(MathProblem("$max - $min =", max - min))
            }
        }
    }

    fun getCurrentProblem(): MathProblem? {
        if (currentProblemIndex >= problems.size) return null
        return problems[currentProblemIndex]
    }

    fun checkAnswer(answer: Int): Boolean {
        val problem = getCurrentProblem() ?: return false
        return if (answer == problem.answer) {
            currentProblemIndex++
            true
        } else {
            false
        }
    }

    fun getProgress(): Pair<Int, Int> = Pair(currentProblemIndex, problems.size)

    fun isComplete(): Boolean = currentProblemIndex >= problems.size

    fun reset() {
        currentProblemIndex = 0
        generateProblems()
    }
}
