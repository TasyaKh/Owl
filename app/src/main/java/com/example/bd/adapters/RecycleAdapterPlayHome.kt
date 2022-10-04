package com.example.bd.adapters

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import com.example.bd.logic.words.Word
import java.util.ArrayList


abstract class RecycleAdapterPlayHome(ctx: Context, words:ArrayList<Word>?) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var lastPositionAppear =
        -1 //нужен чтобы анимация создания нового элемента правильно отображалась

    protected val mLayoutInflater //привязывает все лайоуты (прямоугольники со словами к фрагменту)
            : LayoutInflater = LayoutInflater.from(ctx)

    protected var words //массив выводимых слов
            : ArrayList<Word>? = null



    init {
        this.words = words

    }

    abstract override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int)

    //получить размер массива слов
    fun getSizeArray():Int{
        return words!!.size
    }

    //задать массив с данными слов
    open fun setArrayMyData(arrayMyData: ArrayList<Word>?) {
        words = arrayMyData
    }

    //задать дизайн нашему листу
    abstract override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder

    /**
     * задать анимацию создания или обновления блока в листе
     */
    fun setAnimation(viewToAnimate: View, position: Int) {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (position > lastPositionAppear) {
            val animation = AnimationUtils.loadAnimation(mLayoutInflater.context, android.R.anim.slide_in_left)
            animation.duration = 250
            viewToAnimate.startAnimation(animation)
            lastPositionAppear = position
        } else {
            lastPositionAppear--
        }
    }

    //получить число элементов в списке words.size()
    override fun getItemCount(): Int {
        return words!!.size
    }

    //Получить id элепмента по индексу
    fun getWordId(position: Int): Long {
        val wr = words!![position]
        return wr.id
    }
}

