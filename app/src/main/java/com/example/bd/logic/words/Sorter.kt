package com.example.bd.logic.words

object Sorter {
    //Сортировать по ранней дате
    var SORT_BY_OLD_DATA = java.util.Comparator { o1: Word, o2: Word ->
        val one = o1.datetime
        val two = o2.datetime
        one.compareTo(two)
    }

    //сортировать по последней дате
    var SORT_BY_NEAR_DATA = java.util.Comparator { o1: Word, o2: Word ->
        val one = o1.datetime
        val two = o2.datetime
        two.compareTo(one)
    }

    //получить компаратор, который сортирует WordStatistic по Количеству правильно угаданных слов
    var SORT_WORD_STATISTIC_BY_CORRECT = java.util.Comparator { o1: Word, o2: Word ->
        val w1 = o1 as WordStatistic
        val w2 = o2 as WordStatistic
        val one = (w1.countCorrect - (w1.allAttempts - w1.countCorrect)).toLong()
        val two = (w2.countCorrect - (w2.allAttempts - w2.countCorrect)).toLong()
        two.compareTo(one)
    }


    var SORT_BY_PRIORITY = { arrayForSort: List<Word> ->
            val arrWithPriorityWords = mutableListOf<Word>()

            for(word in arrayForSort){
                if(word.priority > 0)arrWithPriorityWords.add(word)
            }

            arrWithPriorityWords
        }


    //Получить слово по его нучалу
    fun getWordsByStartSymbols(
        words: List<Word>,
        startWords: String,
        languageWord: LanguageWord
    ): List<Word> {

        var sortWords = mutableListOf<Word>()
        if (languageWord == LanguageWord.ENGLISH) {
            sortWords = getWordsByEn(words, startWords)
        } else if (languageWord == LanguageWord.RUSSIAN) {
            sortWords = getWordsByRus(words, startWords)
        }

        return sortWords
    }

    //Получить все слова по русскому началу слова
    private fun getWordsByRus(words: List<Word>, startWord: String):  MutableList<Word> {

        val sortWords = mutableListOf<Word>()
        for (word in words) {
            val enWord = word.ruWord
            if (enWord.startsWith(startWord)) {
                sortWords.add(word)
            }
        }

        return sortWords
    }

    //Получить все слова по английскому началу слова
    private fun getWordsByEn(words: List<Word>, startWord: String):  MutableList<Word> {

        val sortWords =mutableListOf<Word>()
        for (word in words) {
            val enWord = word.englishWord
            if (enWord.startsWith(startWord)) {
                sortWords.add(word)
            }
        }

        return sortWords
    }
}