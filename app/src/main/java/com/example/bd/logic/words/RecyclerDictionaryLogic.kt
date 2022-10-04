package com.example.bd.logic.words;

import android.util.Log
import com.example.bd.R

object RecyclerDictionaryLogic {

    //обновить курсор, когда делаем swipe
    fun newCursorPosAfterSwipe(newWordPosAfterSwipe:Int, positionSwiped: Int, selectedCursorPos:Int):Int{
        //изменить позицию курсора
        //если слово с которым работаем находится между удаленным и вставленным словом

        var newCursorPos:Int = -1
        if(positionSwiped<selectedCursorPos)
        {
            if(newWordPosAfterSwipe>=selectedCursorPos)
                newCursorPos = selectedCursorPos-1
            else  newCursorPos = selectedCursorPos
        }else if(positionSwiped>selectedCursorPos){
            if(newWordPosAfterSwipe<=selectedCursorPos){
                newCursorPos = selectedCursorPos+1
            }  else  newCursorPos = selectedCursorPos
        }else {
            newCursorPos = newWordPosAfterSwipe
        }

        return newCursorPos
    }



    //TODO: not string resources
    fun getTimePassedFromCurrent(timeStart:Long):String{

        val currTime = System.currentTimeMillis()
        val diff = currTime - timeStart
        val timeSave:String

        Log.d("time", "currTime $currTime     timeApart  $diff     millTime $timeStart ")

        val diffSeconds: Long = diff / 1000

        val f =  R.string.sec

        if(diffSeconds<=60) return "$diffSeconds s"


        val diffMinutes: Long = diffSeconds / 60
        if(diffMinutes<=60) return "$diffMinutes min"

        val diffHours: Long = diffMinutes/ 60
        if(diffHours<=24) return "$diffHours h"

        val diffDays: Long = diffHours / 24
        return "$diffDays d"
    }
}
