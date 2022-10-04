package com.example.bd.logic

import com.example.bd.logic.words.Word

object NotificationLogic{

   fun getRandomPriorityWord(bd: BD): Word?{
       val priorityWords: List<Word> = bd.selectPriorityWords()
       var wordRand: Word? = null

       if(priorityWords.isNotEmpty())
            wordRand = priorityWords.random()

       return wordRand
    }

}