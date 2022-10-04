package com.example.bd.logic.words

import com.example.bd.logic.BD
import java.util.*

class PlayWithWords(  //БД слов
    private val bd: BD
) {
    enum class SortBy {
        ALL, PRIORITY
    }

    //Получить текущее слово
    var currentWord //Текущее выпавшее слово
            : WordStatistic?
        private set
    var playingWords //Приоритетные слова
            : ArrayList<Word>? = null
        private set
    private var indexCurrentWord //индекс текущего слова
            : Int

    //Задать приоритет
    //Получить приоритет
    var sortBy //Тип приоритетности слов
            : SortBy
    private val correctMin //минимальное количество правильных слов
            : Int
    var deletedWords //удаленные правильные слова
            : ArrayList<Word>
        private set

    //TODO: WordStatistic поментять
    //Преобразовать слово в WordStatistic
    private fun fromWordIntoWordStatistic(words: ArrayList<Word>): ArrayList<Word> {
        val statistic = ArrayList<Word>()
        for (word in words) {
            val wordStatistic = WordStatistic(word)
            statistic.add(wordStatistic)
        }
        return statistic
    }

    //Получить случайный int
    fun getRandomInt(max: Int): Int {
        val random = Random()
        return random.nextInt(max)
    }

    //Перезаполнить слова
    fun refillWords() {

        var words = ArrayList<Word>()
        if (sortBy == SortBy.PRIORITY) {
            words = bd.selectPriorityWords() //SLOWLY
        } else if (sortBy == SortBy.ALL) {
            words = bd.selectAllFromStart() //SLOWLY
        }

        playingWords = fromWordIntoWordStatistic(words) //SLOWLY
        deletedWords = ArrayList()
    }

    //Получить случайное слово
    fun setRandomWord() {
        if (playingWords != null && playingWords!!.size > 0) {
            val randRaw = getRandomInt(playingWords!!.size)
            currentWord = playingWords!![randRaw] as WordStatistic
            indexCurrentWord = randRaw
        } else currentWord = null
    }

    //Обновить статистику слова и состояние массивов, которые содержат слова
    fun upgradeWordsStatistic(isCorrect: Boolean): Int {

        var res = 0 //при обновлении без завершении игры со словом

        if (indexCurrentWord >= playingWords!!.size) {
            res = -1  //в случае ошибки
            return res
        }

        val statistic = playingWords!![indexCurrentWord] as WordStatistic
        statistic.updateStatistic(isCorrect)

        if (statistic.countCorrect >= correctMin &&
            statistic.allAttempts - statistic.countCorrect <= statistic.countCorrect - correctMin
            && currentWord !=null) {
            // correctStatistic.append(statistic.toString()).append("/n");
            deletedWords.add(currentWord!!)
            playingWords!!.removeAt(indexCurrentWord)

            updateLevelDeletedWord(currentWord!!)
            res = 1 //если со словом завершили игру
        }
        return res
    }

    //обновить уровень удаленного слова
    private fun updateLevelDeletedWord(wr: WordStatistic){

        wr.updateLevel()
        bd.update(wr as Word)
       // bdWords.changeLevelOfWord(wr.id, 1)
    }


    val sizePriorityWords: Int
        get() = playingWords!!.size

    init {
        refillWords()
        indexCurrentWord = 0
        sortBy = SortBy.PRIORITY
        currentWord = null
        correctMin = 3
        deletedWords = ArrayList()
    }
}