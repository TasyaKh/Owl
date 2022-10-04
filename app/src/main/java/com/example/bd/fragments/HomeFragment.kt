package com.example.bd.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.core.view.MenuCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.example.bd.R
import com.example.bd.activities.IActivityBeforeStateChanged
import com.example.bd.activities.notifications.UploadWorker
import com.example.bd.adapters.RecycleDictionaryHomeAdapterPlayHome
import com.example.bd.animation.AnimationTypes
import com.example.bd.collbacks.SimpleItemTouchHelperCallback
import com.example.bd.databinding.FragmentHomeBinding
import com.example.bd.fragments.save_settings.HomeSaveSettings
import com.example.bd.logic.*
import com.example.bd.logic.translator.TranslateFromTo
import com.example.bd.logic.translator.TxtToSpeech
import com.example.bd.logic.words.LanguageWord
import com.example.bd.logic.words.SortWord
import com.example.bd.logic.words.Word
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.mlkit.nl.translate.TranslateLanguage
import java.util.concurrent.TimeUnit


class HomeFragment : Fragment() {

    interface IBottomSheet {
        fun setAllDataFromWord(word: Word)
        fun expandWindow(isVisible: Boolean)
        fun editingWordWasDeleted(posit: Long):Boolean
    }

    private var bd //БД слов
            : BD? = null
    private var myAdapter //Адаптер для ListView (Отображает слова в виде списка)
            : RecycleDictionaryHomeAdapterPlayHome? = null

    private var bottomSheet //Лайоут для изменения данных слов
            : BottomSheet? = null

    //private val ADD_ACTIVITY = 0 //Для Add  activity requestCode
    //private val UPDATE_ACTIVITY = 1 //onActivityResult requestCode
    private var _binding //Привязать View к фрагменту
            : FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = _binding!!.root

        bottomSheet = BottomSheet()
        setHasOptionsMenu(true)
        return root
    }


    private fun createNotification(){
        //TODO: ВЕРНУТЬСЯ notification
        val randomPriorityWord: Word?=
            NotificationLogic.getRandomPriorityWord(bd!!)

        //запустить уведомление со случайным словом с приоритетом
        if(randomPriorityWord!=null) {

            //задать титл и содержимое сообщения
            val data = Data.Builder()
                .putString("title",  "Запомни :)")
                .putString("text", "${randomPriorityWord.englishWord} - ${randomPriorityWord.ruWord}")
                .build()


            val uploadWorkRequest: WorkRequest =
                OneTimeWorkRequestBuilder<UploadWorker>()
                    .setInputData(data)
                    .setInitialDelay(10,TimeUnit.MINUTES)
                    .build()

            WorkManager
                .getInstance(requireContext())
                .enqueue(uploadWorkRequest)
        }
    }


    @SuppressLint("UseRequireInsteadOfGet")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //getContext().deleteDatabase("simple.db");
        //вывести список бд
//       Log.d(
//            "database list", Arrays.toString(
//                Objects.requireNonNull(context)?.databaseList())
//        )

        bd = BD(context)
        createNotification()
        //получить список таблиц в бд
        //Log.d("database tables list", bdWords!!.getTablesNames())

         //for (i in 0..100)bdWords?.insert(Word(-1, "hello$i", "привет", "-", 0,0,i%20));

        //Лист для просмтора слов
        val recyclerView: RecyclerView = view.findViewById(R.id.recycler_view)

        myAdapter = context?.let {
            RecycleDictionaryHomeAdapterPlayHome(
                it,
                bd!!.selectAllFromStart(), bottomSheet as IBottomSheet
            )
        }

        //Задать адаптер

        //Задать адаптер и добавить колюэк для событий (например swipe)
        val callback: ItemTouchHelper.Callback = SimpleItemTouchHelperCallback(myAdapter)
        val touchHelper = ItemTouchHelper(callback)
        touchHelper.attachToRecyclerView(recyclerView)

        recyclerView.adapter = myAdapter
    }

    //Создать меню опций
    @SuppressLint("UseRequireInsteadOfGet")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_fragment_home, menu)
        super.onCreateOptionsMenu(menu, inflater)

        MenuCompat.setGroupDividerEnabled(menu, true)

        val sortFind = menu.findItem(R.id.find_by_name)

        val sortEn = menu.findItem(R.id.language_en)
        val sortRu = menu.findItem(R.id.language_ru)

        val sortOldData= menu.findItem(R.id.sort_old_data)
        val sortNearData = menu.findItem(R.id.sort_near_data)
        val sortDefault= menu.findItem(R.id.sort_default)

        val sortPriority= menu.findItem(R.id.show_priority_words)


        //загрузить сохраненные настройки для типа сортировки по дате
        val homeSaveSettings =
            HomeSaveSettings(context!!)

        when (homeSaveSettings.loadSettingsSortWord()) {
            SortWord.NEAR_DATA -> onOptionsItemSelected(sortNearData)
            SortWord.OLD_DATA -> onOptionsItemSelected(sortOldData)
            SortWord.DEFAULT -> onOptionsItemSelected(sortDefault)
            SortWord.NAME -> {
            }
        }
        //загрузить сохраненные настройки по типу сортировки языка слова
        when (homeSaveSettings.loadSettingsLanguageWord()) {
            LanguageWord.ENGLISH -> onOptionsItemSelected(sortEn)
            LanguageWord.RUSSIAN -> onOptionsItemSelected(sortRu)
        }

        val searchView: SearchView = sortFind.actionView as SearchView
        val sortByPriority: ToggleButton = sortPriority.actionView as ToggleButton


        searchView.queryHint = getString(R.string.search)
        //для поискового элемента (лупа)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String): Boolean {
                myAdapter!!.setSortStartWords(query)
                myAdapter!!.sortWords(SortWord.NAME)

                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {


                if (myAdapter!!.getSizeArray() < 5000 && newText.isNotEmpty()) {
                    myAdapter!!.setSortStartWords(newText)
                    myAdapter!!.sortWords(SortWord.NAME)

                }
                return false
            }
        })

        searchView.setOnCloseListener {
            onOptionsItemSelected(sortDefault)
            //задать лист, который был до поиска
            myAdapter!!.setOriginalList()
            false
        }

        sortByPriority.setOnClickListener{

            if(sortByPriority.isChecked){
                myAdapter!!.sortWords(SortWord.PRIORITY)
                //  bottomSheet!!.restrictInputAndEditWord(true)
            }else{
                //bottomSheet!!.restrictInputAndEditWord(false)
                onOptionsItemSelected(sortDefault)
                updateList(SortWord.DEFAULT)
            }
        }

    }

    //скрыть клавиатуру
    private fun Context.hideKeyboard(view: View?) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    //По нажатию на любой элемент в меню
    @SuppressLint("NonConstantResourceId", "NotifyDataSetChanged", "UseRequireInsteadOfGet")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val saveSettings =
            HomeSaveSettings(requireContext())

        var languageWord = LanguageWord.ENGLISH
        var sortWord = SortWord.NEAR_DATA
        when (item.itemId) {
            R.id.language_en -> {
                myAdapter!!.setLanguageSortWord(languageWord)
                saveSettings.saveLanguageWord(languageWord)
            }
            R.id.language_ru -> {
                languageWord = LanguageWord.RUSSIAN
                myAdapter!!.setLanguageSortWord(languageWord)
                saveSettings.saveLanguageWord(languageWord)
            }
            R.id.sort_near_data -> {
                // myAdapter!!.setSortWord(sortWord)
                saveSettings.saveSortWord(sortWord)
                myAdapter!!.sortWords(sortWord)
            }
            R.id.sort_old_data -> {
                sortWord = SortWord.OLD_DATA
                //myAdapter!!.setSortWord(sortWord)
                saveSettings.saveSortWord(sortWord)
                myAdapter!!.sortWords(sortWord)
            }
            R.id.sort_default-> {
                sortWord = SortWord.DEFAULT
                //myAdapter!!.setSortWord(sortWord)
                saveSettings.saveSortWord(sortWord)
                myAdapter!!.sortWords(sortWord)
            }
        }

        //myAdapter!!.notifyDataSetChanged()
        item.isChecked = true
        //updateList(0,myAdapter.getSizeArray());
        return super.onOptionsItemSelected(item)
    }

    //обновляем данные в листе
    @SuppressLint("NotifyDataSetChanged")
    private fun updateList(sortWord: SortWord) {
        myAdapter!!.setArrayMyData(bd!!.selectAllFromStart())
        myAdapter!!.sortWords(sortWord)
        // myAdapter!!.notifyDataSetChanged()
    }

    override fun onStop() {

        //TODO: надо при смене фрагмента на другой убрать текст в EditText т.к. ОН ОСТАЕТСЯ В ПОЛЯХ (оптимизировать этот вариант)
        if(activity is IActivityBeforeStateChanged) {
            val activity = activity as IActivityBeforeStateChanged

            //если активность все еще работает но фрагмент приостановлен значит фрагмент был изменен на другой
            if (!activity.activityWantToStop) {
                bottomSheet?.close()
            }
        }

        super.onStop()
    }

    override fun onDestroyView() {
        bottomSheet?.close()
        super.onDestroyView()

        //bottomSheet = null
        //bdWords = null
        //myAdapter = null

        //TODO: переделать (динамическое добавление и удаление фрагментов)
    }









    //CLASS BOTTOM SHEET (лист, выдвигающийс снизу экрана)
    inner class BottomSheet:IBottomSheet {

        private var translateFromTo:TranslateFromTo? = null

        private var editingWord //текущее слово, которое редактируем
                : Word?


        private var bottomSheet //Лайоут, на котором все редактирвоание происходит
                : LinearLayout
        private val toSpeech    //Класс, отвечабщий за произношение слова, нужен для озвучивания англ слова
                : TxtToSpeech

        //fields
        private var field1:EditText //поле 1
        private var field2:EditText //поле 2
        private var descript:EditText ///описание к слову
        private var translatedText:TextView //переведенный текст


        //Поведение нижнего экрана
        private fun bottomSheetBehaviour(bottomSheetBehavior: BottomSheetBehavior<View>){

            // настройка максимальной высоты
            //        Log.d("dpi dp", dpi.toString() + "  " +
            //        resources.getDimension(com.example.myapplication.R.dimen.bar_sheet_bottom).toString())

            // настройка возможности скрыть элемент при свайпе вниз
            //bottomSheetBehavior.isHideable = true

            // настройка колбэков при изменениях (state changed видимость окна)
            bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {

                    //изменить отступы RecycleView List, в зависимости от bottom_sheet
//                    val params = CoordinatorLayout.LayoutParams(
//                        CoordinatorLayout.LayoutParams.MATCH_PARENT,
//                        CoordinatorLayout.LayoutParams.MATCH_PARENT
//                    );


                    when(newState){

                        BottomSheetBehavior.STATE_COLLAPSED->{ //если закрыли

                            //скрыть клавиатуру
                            context?.hideKeyboard(view)

                            /** происходит перерисовка листа с элементами и его отступов
                             * Внимание! Этот вариант не очень эффективен**/
                            //изменить отступы recycleView когда bottom_sheet меняет размеры
                            val marginsList = resources.getDimension((R.dimen.size_5dp)).toInt()
                            val bottSheetHeight:Int = resources.getDimension((R.dimen.bottom_sheet_peek_height)).toInt()

                            //TODO:CAN BE SOME ERRORS PARAMS Margins
                           // params.setMargins(marginsList,marginsList,marginsList,bottSheetHeight);
                            //binding.recyclerView.layoutParams = params;

                            //animation
                            AnimationTypes.animateFadeInOuT( binding.floatingButton,true,context)

                        }


                        BottomSheetBehavior.STATE_EXPANDED->{ //открыли
                            //animation
                            //анимировать кнопку для сохранения при расширении этого экрана
                            val animAlpha: Animation = AnimationUtils.loadAnimation(context, R.anim.move_butt)
                            val save = bottomSheet.findViewById<Button>(R.id.save)
                            save.startAnimation(animAlpha)

                            AnimationTypes.animateFadeInOuT( binding.floatingButton,false,context)
                        }

                        BottomSheetBehavior.STATE_HIDDEN-> {

                            //изменить отступы recycleView когда bottom_sheet меняет размеры
                            val marginsList = resources.getDimension((R.dimen.size_5dp)).toInt()
                           // params.setMargins(marginsList,marginsList,marginsList,marginsList);
                           // binding.recyclerView.layoutParams = params;
                        }
                        BottomSheetBehavior.STATE_DRAGGING -> {

                        }
                        BottomSheetBehavior.STATE_HALF_EXPANDED -> {

                        }
                        BottomSheetBehavior.STATE_SETTLING -> {

                        }
                    }

                }


                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    //скрытие плавающей кнопки при скролле
//                 if(slideOffset>0){
//
//                     binding.floatingButtonScrollTo.animate().alpha(slideOffset).setDuration(0).start()
//                 }
                }
            })
        }



        private fun initButtonFloatingScrollTo() {
            binding.floatingButtonScrollTo.setOnClickListener{
                if(editingWord!=null){
                    binding.recyclerView.scrollToPosition(myAdapter!!.getPositionCursor())
                }
            }
        }

        //плавающая кнопка для сохранения и для добавления нового слова
        private fun initButtonFloatingBehaviour(bottomSheetBehavior:BottomSheetBehavior<View>){


            //действия для плавающей кнопки
            binding.floatingButton.setOnClickListener {

                //если закрыто окно, то по нажатию на кнопку ввести новое слово
                if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED ||
                    bottomSheetBehavior.state == BottomSheetBehavior.STATE_HIDDEN ){

                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                    clearAll()
                }

            }

            Log.d("state", bottomSheetBehavior.state.toString())
        }


        init {

            editingWord = null
            binding.floatingButtonScrollTo.visibility = View.INVISIBLE

            // получение вью нижнего экрана
            val llBottomSheet:LinearLayout = binding.bottomSheet.root

            // настройка поведения нижнего экрана
            val bottomSheetBehavior: BottomSheetBehavior<View> = BottomSheetBehavior.from(llBottomSheet)

            //задать поведение для нижнего экрана
            bottomSheetBehaviour(bottomSheetBehavior)

            //задать поведение для плавающей кнопки
            initButtonFloatingBehaviour(bottomSheetBehavior)
            initButtonFloatingScrollTo()

            bottomSheet =  binding.bottomSheet.root
            toSpeech = TxtToSpeech(context)

            field2 = bottomSheet.findViewById(R.id.field_two)
            field1 = bottomSheet.findViewById(R.id.field_one)
            descript = bottomSheet.findViewById(R.id.description_edit)

            val insertTranslated = bottomSheet.findViewById<ImageButton>(R.id.insert_translated)
            translatedText =  bottomSheet.findViewById<TextView>(R.id.translated)


            //определить поле, с которым работает пользователь
            val focusFieldChanged = {insertTranslateIntoField:EditText->
                View.OnFocusChangeListener { v, hasFocus ->

                    //если пользователь это поле редактирует
                    if(hasFocus){

                        //задаем событие для кнопки, которая текст сохраняет в поле insertTranslateIntoField
                        insertTranslated.setOnClickListener{
                            insertTranslateIntoField.setText(translatedText.text)
                        }

                        //val editField:EditText = v as EditText

                        /**
                         * требуется сделать так чтобы переводчик включался сразу же, как
                         * пользователь поставил курсор в EditText
                         * */


                        // editField.text=editField.text

                        //val translated:TextView =  bottomSheet.findViewById(R.id.translated)
                        //init translator когда поле изменено
                        initTranslator(insertTranslateIntoField)

                    }else{
                        translateFromTo?.close()
                    }

                }
            }

            field1.onFocusChangeListener = focusFieldChanged(field2)
            field2.onFocusChangeListener = focusFieldChanged(field1)

            field1.addTextChangedListener(TextChanged())
            field2.addTextChangedListener(TextChanged())


        }


        //init translator
        private fun initTranslator(insertTranslateIntoField:View) {
            //перевести текст

            when (insertTranslateIntoField.id) {
                R.id.field_one -> translateFromTo = TranslateFromTo(
                    TranslateLanguage.RUSSIAN,
                    TranslateLanguage.ENGLISH
                )//check(txt, 'A', 'z')
                R.id.field_two -> translateFromTo = TranslateFromTo(
                    TranslateLanguage.ENGLISH,
                    TranslateLanguage.RUSSIAN
                )//check(txt, 'А', 'я')
            }
        }


        //init buttons
        init {

            //проговаривание слова
            val speak = bottomSheet.findViewById<ImageButton>(R.id.speak)

            //Начать проговаривание английского слова при нажатии на кнопку
            speak?.setOnClickListener {
                toSpeech.setText(field1.text.toString())
                toSpeech.speakOut()
            }

            //кнопка сохранения слова
            val save =  bottomSheet.findViewById<Button>(R.id.save)
            val doTranslate =  bottomSheet.findViewById<ImageButton>(R.id.do_translate)

            doTranslate.setOnClickListener{

                val txt = if(field1.isFocused) field1.text.toString()
                else field2.text.toString()

                doTranslate(txt)
            }

            save.setOnClickListener {

                //сообщение пользователю  результате
                val msgToast:String

                //Редактирование существующего слвоа
                if (editingWord != null) {

                    //получить слово из поля для редактирвания
                    val word = wordFromEdit() ?: return@setOnClickListener
                    val result = bd!!.update(word)

                    //обновить курсор
                    if (result > 0) {
                        msgToast = getString(R.string.saved)
                        myAdapter!!.updateByCursor(word)

                        myAdapter!!.notifyItemChanged(myAdapter!!.getPositionCursor())
                    } else msgToast = getString(R.string.word_was_deleted)

                }else{  //создание нового слова и очищение всех полей
                    val wordCreated = createNewWord()

                    msgToast = if(wordCreated) getString(R.string.created_new_word)
                    else getString(R.string.one_field_empty)
                    clearAll()
                }


                Toast.makeText(context, msgToast, Toast.LENGTH_LONG).show()
            }

        }


        //создать новое слово
        private fun createNewWord():Boolean{

            val wordCreated:Boolean

            //если нет пустых полей то разрешаем схранение
            if(field1.text.isNotEmpty() && field2.text.isNotEmpty()) {

                val enTxt = field1.text.toString().trim()
                val ruTxt = field2.text.toString().trim()

                val datetime = System.currentTimeMillis()
                var word = Word(
                    -1, enTxt, ruTxt,
                    descript.text.toString().trim { it <= ' ' }, 0, datetime, 0
                )

                val id = bd!!.insert(word)
                //correcct word id
                word = Word(
                    id, enTxt, ruTxt,
                    descript.text.toString().trim { it <= ' ' }, 0, datetime, 0
                )

                // Log.d("posit cursor", myAdapter!!.getPositionCursor().toString())
                //Добавить слово в адаптер
                myAdapter!!.addWord(word)
                myAdapter!!.updateHeader()

                wordCreated = true
            }

            //если есть пустые поля то не разрешаем схранение
            else{
                wordCreated = false
            }

            return wordCreated
        }


        //Прекратить проговаривание слова и закрыть GoneLayout
        override fun expandWindow(isVisible: Boolean) {

            val llBottomSheet:LinearLayout = binding.bottomSheet.root
            // настройка поведения нижнего экрана
            val bottomSheetBehavior: BottomSheetBehavior<LinearLayout> = BottomSheetBehavior.from(llBottomSheet)

            if (isVisible) {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            } else {
                //прекратить проговаривание слова
                toSpeech.silent()
                //Прикрыть окно для редактивраоения слова
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                //убрать редактируемое слово
                editingWord = null
                //почистить все поля, если пользователь что то вводил в поля для редактирования слова
                clearAll()
            }
        }

        override fun editingWordWasDeleted(posit: Long):Boolean {
            var wordDeleted = false

            if(editingWord!=null && posit == editingWord!!.id){
                wordDeleted = true
                clearAll()
            }

            return wordDeleted
        }

        //заполнить все поля,которые содержат данные
        override fun setAllDataFromWord(word: Word) {

            editingWord = word

            field2.setText(word.ruWord)
            field1.setText(word.englishWord)
            descript.setText(word.description)

            binding.floatingButtonScrollTo.visibility = View.VISIBLE
        }


        //translate txt
        fun doTranslate(s:String){

            if (s.isNotEmpty()) {
                //включить переводчик TODO: UPGRADE (CREATE FAST TRANSLATE)
                //перевести текст
                translateFromTo?.translateTxt(translatedText,s.toString())
            } else{
                translatedText.setText(R.string.points_txt)
            }

        }

        //очистить все поля
        private fun clearAll(){

            field2.text.clear()
            field1.text.clear()

            field2.setHint(R.string.ru)
            field1.setHint(R.string.en)
            descript.text.clear()
            bottomSheet.findViewById<TextView>(R.id.translated).setText(R.string.points_txt)

            editingWord = null
            myAdapter?.updateCursor(-1)
            toSpeech.silent()
            binding.floatingButtonScrollTo.visibility = View.INVISIBLE
        }

        //закрыть и освободить ресурсы
        fun close(){
            clearAll()
            toSpeech.close()
        }


//        fun restrictInputAndEditWord(restrictInput:Boolean){
//
//            if(restrictInput){
//                //все почистить и заткнуть
//                expandWindow(false)
//                //сделать невилимым поле для редактирования слова
//                bottomSheet.visibility = View.GONE
//                binding.floatingButton.visibility =  View.GONE
//            }else{
//                bottomSheet.visibility = View.VISIBLE
//                bottomSheet.isEnabled = true
//                binding.floatingButton.visibility =  View.VISIBLE
//            }
//        }


        private fun wordFromEdit(): Word? {

            val datetime = System.currentTimeMillis()
            var word: Word? = null

            if (editingWord != null) {
                word = Word(
                    editingWord!!.id,
                    field1.text.toString().trim { it <= ' ' },
                    field2.text.toString().trim { it <= ' ' },
                    descript.text.toString().trim { it <= ' ' },
                    editingWord!!.priority,
                    datetime,
                    0
                )
            }

            return word
        }







        //отслеживает изменнеие слова
        inner class TextChanged : TextWatcher{
            //var doTranslate: Task<String>? = null

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            //При изменении текста проверить в соответствии с элементом на правильном ли языке введен символ
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }

            //do fast translate
            override fun afterTextChanged(s: Editable) {
                if( s.length<50)
                  doTranslate(s.toString())
            }


        }

    }

}
