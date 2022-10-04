
package com.example.bd.adapters

import android.content.Context
import android.content.res.Resources
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bd.R
import com.example.bd.logic.words.Word
import com.example.bd.logic.words.WordStatistic

//адаптер отвечающий за показ слов и текуще  статистики
class RecycleStatisticAdapterPlayHome(ctx:Context, arr: ArrayList<Word>?): RecycleAdapterPlayHome(ctx, arr){ // end myAdapter

    enum class SeeTranslate {
        WITH, WITHOUT
    }

    var seeTranslate //Можно ли просмотреть перевод слова на родном языке
            : SeeTranslate = SeeTranslate.WITHOUT


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        //задать дизайн нашему листу
        val view = mLayoutInflater.inflate(R.layout.list_design_play, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(h: RecyclerView.ViewHolder, position: Int){

        val holder = h as ViewHolder
        val wr = words!![position] as WordStatistic

        //При нажатии на любой элемент разрешить просмотр перевода на родной язык для всех элементов

        holder.itemView.setOnClickListener {

            //  if (getArrayMyData() !== deletedWords) {
            seeTranslate = if (seeTranslate == SeeTranslate.WITHOUT)
                SeeTranslate.WITH
            else SeeTranslate.WITHOUT

            val txt:String = wr.ruWord

            //посмотреть перевод
            if (seeTranslate == SeeTranslate.WITH) holder.ru.text = txt
            else holder.ru.text = ""

            //  }
        }

        holder.en.text = wr.englishWord
        holder.ru.text = ""

        val correct = holder.correct
        val incorrect = holder.incorrect

        //кол-во попыток правильного угадывания слова
        val incorrectWr:Int =(wr.allAttempts - wr.countCorrect)
        //вычислить количество попыток неправильного угадывания слова
        val correctWr:Int = wr.countCorrect

        correct.text = correctWr.toString()
        incorrect.text =  incorrectWr.toString()


        setColorCorrIncorr(correctWr,incorrectWr,correct,incorrect)
    }

   private fun setColorCorrIncorr(correctWr:Int, incorrectWr:Int, correct:View, incorr:View){

       val colorCorr:Int
       val colorIncorrect:Int

       //задать цвет в зависимости от количества правильных/неправильных слов
       val res:Resources = mLayoutInflater.context.resources

       if (incorrectWr < correctWr){
           colorCorr = res.getColor(R.color.blue_A100, res.newTheme())
           colorIncorrect = res.getColor(R.color.gray, res.newTheme())
       }
       else if(incorrectWr == correctWr){
           colorCorr = res.getColor(R.color.gray, res.newTheme())
           colorIncorrect = colorCorr
       }
       else {
           colorIncorrect = res.getColor(R.color.pink_A100, res.newTheme())
           colorCorr =res.getColor(R.color.gray, res.newTheme())
       }

       correct.setBackgroundColor(colorCorr)
       incorr.setBackgroundColor(colorIncorrect)
    }


   fun getArrayMyData():ArrayList<Word>{
       return words!!
   }


    inner class ViewHolder internal constructor(view: View) : RecyclerView.ViewHolder(view) {
        val en //английское слово
                : TextView = view.findViewById(R.id.field_big)
        val ru //русское слово
                : TextView = view.findViewById(R.id.field_small)

        val correct //id слова в листе (порядковый номер)
                : TextView = view.findViewById(R.id.id_correct)
        val incorrect //id слова в листе (порядковый номер)
                : TextView = view.findViewById(R.id.id_incorrect)

    }


  }
