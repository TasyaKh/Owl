package com.example.bd.fragments

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.*
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import com.example.bd.R
import com.example.bd.animation.AnimationTypes
import com.example.bd.databinding.FragmentPlayBinding
import com.example.bd.logic.*
import com.example.bd.logic.words.LanguageWord
import com.example.bd.logic.words.PlayWithWords
import com.example.bd.logic.words.Sorter
import com.example.bd.logic.words.Word
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.Snackbar.SnackbarLayout


class PlayFragment : Fragment() {

    private var binding //Связыватель
            : FragmentPlayBinding? = null
    private var priorityTranslate //Какое слово в какое поле
            : LanguageWord? = null
    private var translate //писать перевод слова сюда
            : EditText? = null

    private lateinit var playWithWords //Здесь выбирается случайное слово по какому либо фильтру
            : PlayWithWords

    //private lateinit var bdWords:BDWords

    //Привязать View и инициализировать поля
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        binding = FragmentPlayBinding.inflate(inflater, container, false)
        val root: View = binding!!.root

        val bd = BD(context) //Получить базу со словами
        playWithWords = PlayWithWords(bd) //Отправить базу в сортировщик

        playWithWords.refillWords()
        //playWithWords.setRandomWord();
        translate = root.findViewById(R.id.translate)

        //create menu
        val menuHost: MenuHost = requireActivity()

        // Add menu items without using the Fragment Menu APIs
        // Note how we can tie the MenuProvider to the viewLifecycleOwner
        // and an optional Lifecycle.State (here, RESUMED) to indicate when
        // the menu should be visible

        menuHost.addMenuProvider(object : MenuProvider {

            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // Add menu items here
                menuInflater.inflate(R.menu.menu_fragment_play, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            // Handle the menu selection

                val itemIsChecked =
                    when (menuItem.itemId) {
                        R.id.sort_all -> {
                            playWithWords.sortBy = PlayWithWords.SortBy.ALL
                            true
                        }
                        R.id.sort_priority -> {
                            playWithWords.sortBy =
                                PlayWithWords.SortBy.PRIORITY
                            true
                        }
                        else -> false
                    }
                menuItem.isChecked = itemIsChecked

                playWithWords.refillWords()
                updateRandomWordAndFields()

                return itemIsChecked
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        return root
    }



    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        updateRandomWordAndFields()
        //разрешить скролл описания для слова
        val descr = view.findViewById<TextView>(R.id.description)
        descr.movementMethod = ScrollingMovementMethod()

        val refresh = view.findViewById<ImageButton>(R.id.refresh)

        //Перезаполнить список слов
        refresh.setOnClickListener { v: View? ->
            playWithWords.refillWords()
            updateRandomWordAndFields()
        }

        val statistic = view.findViewById<ImageButton>(R.id.see_statistic)

        //Посмотреть статистику (правильные и неправильные слова)
        statistic.setOnClickListener { v: View? ->

            // FragmentManager manager = getChildFragmentManager();
        val listWords = playWithWords.playingWords

            //TODO: Не оптимизировано
            listWords?.sortWith(Sorter.SORT_WORD_STATISTIC_BY_CORRECT)

            //Отобразить статистику

            val playAlertDialog =  PlayAlertDialog(context,playWithWords)
            playAlertDialog.show()

        }

        //кнопка для провекки слова
        val check = view.findViewById<Button>(R.id.check)
        //прооказать изображение правильно или неправильно ответил пользователь
        val resultImg:ImageSwitcher = view.findViewById(R.id.result_img)

        //по нажатию на кнопку, проверить слово
        check.setOnClickListener {

            if (playWithWords.currentWord == null) {                   //Если слово существует, то
                Toast.makeText(context, getString(R.string.no_words), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val userInput =
                translate!!.text.toString().trim { it <= ' ' } //получить слово от пользователя

            if(userInput.isEmpty()) {                   //Если поле со словом пустое, то
                Toast.makeText(context, getString(R.string.field_empty), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            var correct = false
            val currentWord = playWithWords.currentWord

            if (priorityTranslate == LanguageWord.ENGLISH) {        //если нужно перевести на русский
                if (userInput.equals(currentWord?.ruWord , ignoreCase = true)) {
                    correct = true
                }
            }
            else {                                                  //если нужно перевести на английский
                if (userInput.equals(currentWord?.englishWord , ignoreCase = true)) {
                    correct = true
                }
            }


            val res = playWithWords.upgradeWordsStatistic(correct)
            if (res<0) //обновить статистику слов
                Toast.makeText(
                    context,
                    "Can't update word upgradeWordsStatistic(correct) Play",
                    Toast.LENGTH_SHORT
                ).show()
            else if(res > 0) {
                showSnackbar(playWithWords.currentWord!!)
            }

            switchImg(resultImg,correct)
            updateRandomWordAndFields() //получить новое случайное слово
            animateBackgroundCheck(correct) //анимировать правильный ответ или нет
        }
    }


   fun switchImg(resultImg:ImageSwitcher, correct:Boolean){
       if(correct &&  resultImg.currentView.id == R.id.wrong){
           resultImg.showNext()

       }else if(!correct &&  resultImg.currentView.id != R.id.wrong){
           resultImg.showNext()
       }
   }


   private fun showSnackbar(wr: Word){

        val snackbar :Snackbar = Snackbar.make(requireView(), "", 1000*5);
        //привязать вью снека к основному фрагменту
        snackbar.setBackgroundTint(Color.GRAY)
        val customSnackView: View = layoutInflater.inflate(R.layout.level_up, null)

        //задать фон для снека
        customSnackView.setBackgroundResource(R.drawable.gradient)

       val fieldBig = customSnackView.findViewById<TextView>(R.id.field_big)
       val fieldSmall = customSnackView.findViewById<TextView>(R.id.field_small)
       val level = customSnackView.findViewById<TextView>(R.id.level)

       val lv = "${getString(R.string.level_up)} ${wr.level}"
       fieldBig.text = wr.englishWord
       fieldSmall.text = wr.ruWord
       level.text = lv

        val snackbarLayout = snackbar.view as SnackbarLayout

        val params:CoordinatorLayout.LayoutParams =  snackbar.view.layoutParams as CoordinatorLayout.LayoutParams
        params.gravity = Gravity.TOP
        snackbar.view.layoutParams = params


        snackbarLayout.setPadding(0, 0, 0, 0)
        snackbarLayout.addView(customSnackView, 0);
        snackbar.show();
    }

    //получить случайное слово и записать его
    private fun updateRandomWordAndFields() {

        val view = requireView()
        val allWords = view.findViewById<TextView>(R.id.count_words)

        allWords.text = playWithWords.sizePriorityWords.toString() //upgrade counter

        val descr = view.findViewById<TextView>(R.id.description)
        playWithWords.setRandomWord() //получить случайное слово

        val currentWord = playWithWords.currentWord
        val description: String

        if (currentWord != null) {   //Если слово выпало, то
            description = currentWord.description //записываем его описание
            descr.text =
                if (description != "") description else getString(R.string.description)
        } else {
            descr.text = getString(R.string.description)
        }

        writeRandomWord()
    }

    //анимировать (правильно или неправильно ответили) при нажатии на кнопку проверить
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun animateBackgroundCheck(isCorrect: Boolean) {

        val colorFrom: Int
        val colorTo: Int
        val scrollView = view?.findViewById<HorizontalScrollView>(R.id.scrollWord)

        colorTo = resources.getColor(R.color.card_view, resources.newTheme())

        if (isCorrect) {
            colorFrom = resources.getColor(R.color.green_A100, resources.newTheme())
        } else {
            colorFrom = resources.getColor(R.color.pink_A100, resources.newTheme())
        }


        if (scrollView != null) {
            AnimationTypes.changeBackgroundColor(colorFrom, colorTo,scrollView)
        }
    }

    //записать случайно выпавшее слово в поля и задать длы польвоателя на каком языке ответ писать
    private fun writeRandomWord() {

        val view = requireView()
        val word = view.findViewById<TextView>(R.id.word)
        val currentWord = playWithWords.currentWord

        translate!!.text.clear()
        word.text = getString(R.string.none)

        if (currentWord != null) {

            val enRu = playWithWords.getRandomInt(2)
            //translate!!.text.clear()

            when (enRu) {
                0 -> {
                    priorityTranslate = LanguageWord.ENGLISH
                    word.text = currentWord.englishWord
                }
                1 -> {
                    priorityTranslate = LanguageWord.RUSSIAN
                    word.text = currentWord.ruWord
                }
            }
        }
        // Toast.makeText(getContext(), word.toString(), Toast.LENGTH_SHORT).show();
    }

    //Уничтожить View
    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}