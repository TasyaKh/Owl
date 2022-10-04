package com.example.bd.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import com.example.bd.adapters.RecycleStatisticAdapterPlayHome
import android.os.Bundle
import android.widget.ImageButton
import com.example.bd.R
import android.widget.ImageSwitcher
import android.widget.TextView

import androidx.recyclerview.widget.RecyclerView

import com.example.bd.logic.words.PlayWithWords
import com.example.bd.logic.words.Word

import java.util.*

//Всплывающий диалог (вызывается, когда во время игры хотим узнать результат наших попыток)
class PlayAlertDialog(context: Context?, playWithWrd: PlayWithWords) : AlertDialog(context) {

    private var wordStatistics //слова со статистикой
            : ArrayList<Word>? = null
    private var deletedWords //слова, с которыми закончили игру
            : ArrayList<Word>? = null
    private var myAdapter //адаптер для листа для просмотра списка слов
            : RecycleStatisticAdapterPlayHome? = null

    init {
        wordStatistics = playWithWrd.playingWords
        deletedWords = playWithWrd.deletedWords
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.play_alert_dialog)

        myAdapter = RecycleStatisticAdapterPlayHome(context, wordStatistics)
        myAdapter!!.seeTranslate = RecycleStatisticAdapterPlayHome.SeeTranslate.WITHOUT


        val recyclerView: RecyclerView = findViewById(R.id.recyclerview)
        //Задать адаптер
        recyclerView.adapter = myAdapter


        val nextPrevButton = findViewById<ImageSwitcher>(R.id.next_prev) //переключатель кнонпок для просмотра слов
        val inGame = findViewById<ImageButton>(R.id.in_game) //слова, которые еще в игре
        val outGame = findViewById<ImageButton>(R.id.out_game) //вышли из игры

        val title = findViewById<TextView>(R.id.title)


        inGame.setOnClickListener {
            myAdapter!!.setArrayMyData(wordStatistics)
            myAdapter!!.seeTranslate = RecycleStatisticAdapterPlayHome.SeeTranslate.WITHOUT

            title!!.text = context.resources.getString(R.string.game)

            nextPrevButton.showNext()
            updateList()
        }

        outGame.setOnClickListener{

            myAdapter!!.setArrayMyData(deletedWords)
            myAdapter!!.seeTranslate = RecycleStatisticAdapterPlayHome.SeeTranslate.WITH

            title!!.text = context.resources.getString(R.string.correct)

            nextPrevButton.showNext()
            updateList()
        }

    }

    //обновить ListView
    @SuppressLint("NotifyDataSetChanged")
    private fun updateList() {
        //каждый раз отправляем данные по новой
        myAdapter!!.notifyDataSetChanged()
    }

    //Срабатывает при закрытии Dialog
    override fun dismiss() {
        super.dismiss()
        wordStatistics = null
        deletedWords = null
    }

    //когда надо показать статистику игры
    override fun show() {
        super.show()
        myAdapter!!.seeTranslate = RecycleStatisticAdapterPlayHome.SeeTranslate.WITHOUT
    }

}