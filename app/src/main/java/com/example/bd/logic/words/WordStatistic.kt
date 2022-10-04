package com.example.bd.logic.words

class WordStatistic(word: Word) : Word(
    word
) {
    var countCorrect //правильно попали
            : Int = 0
    var allAttempts //все попытки
            = 0
        private set

    fun updateStatistic(isCorrectHit: Boolean) {
        if (isCorrectHit) {
            countCorrect++
        }
        allAttempts++
    }

    override fun toString(): String {
        return (englishWord + "/" + ruWord + " correct: "
                + countCorrect + " attempts: " + allAttempts)
    }

    fun updateLevel() {
        if (level < 10) level++
    }
}