package com.example.bd.logic.words

import java.io.Serializable

open class Word : Serializable {
    val id //Id слова
            : Long
    val englishWord //Английское слово
            : String
    val ruWord //Русское слово
            : String
    val description //Описание
            : String
    var priority //Приоритет
            : Int
        private set
    val datetime //Время создания слова
            : Long
    var level //level of word in play state
            : Int
        protected set

    constructor(
        id: Long, englishWord: String, ruWord: String, description: String,
        priority: Int, datetime: Long, level: Int
    ) {
        this.id = id
        this.englishWord = englishWord
        this.ruWord = ruWord
        this.description = description
        this.priority = priority
        this.datetime = datetime
        this.level = level
    }

    constructor(word: Word) {
        id = word.id
        englishWord = word.englishWord
        ruWord = word.ruWord
        description = word.description
        priority = word.priority
        datetime = word.datetime
        level = word.level
    }

    override fun toString(): String {
        return "$englishWord - $ruWord"
    }

    fun updatePriority() {
        if (priority < 1) priority++ else priority = 0
    }
}