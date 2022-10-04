package com.example.bd.adapters;

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ImageSwitcher
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.bd.R
import com.example.bd.collbacks.ItemTouchHelperAdapter
import com.example.bd.fragments.HomeFragment
import com.example.bd.logic.*
import com.example.bd.logic.words.*
import kotlin.collections.ArrayList


class RecycleDictionaryHomeAdapterPlayHome(ctx:Context, arr: ArrayList<Word>?, IGoneLayout: HomeFragment.IBottomSheet?)
    : RecycleAdapterPlayHome(ctx, arr), ItemTouchHelperAdapter {

    private var originalArr:List<Word>?//в случае, если ищем определенное слово, то необходимо сохранить оригинальный размер массива

    private val TYPE_HEADER = 0 //позиция элемента хэдер определяется
    val TYPE_ITEM = 1 //позиция элемента списка определяется

    private var languageWord //язык сортировки
            : LanguageWord
    private var sortWord //параметры сортировки сортировать (по дате, по умолчанию, по имени)
            : SortWord

    private var sortStartWords //Если нужно отсортирвоать слово по определенным буквам
            : String

    private var selectedPos //выделить блок, с которым работаем
            : Int

    private val bd //база данных слов
            : BD = BD(ctx)
    private val bottomSheet //при удалении слова, передать просьбу о закрытии лайоута для редактиррвания слова
            : HomeFragment.IBottomSheet? = IGoneLayout

    //инициализировать поля класса
    init {
        originalArr = arr

        languageWord = LanguageWord.ENGLISH
        sortWord = SortWord.NEAR_DATA
        sortStartWords = ""
        selectedPos = -1
        setArrayMyData(arr)
    }

    fun getPositionCursor():Int{
        return selectedPos
    }

    //получить размер массива слов с учетом header-а
    override fun getItemCount(): Int {
        return getSizeArray() + 1
    }

    override fun getItemViewType(position: Int): Int {

        if (position == 0) {
            return TYPE_HEADER
        } else return TYPE_ITEM
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        //задать дизайн нашему листу
        val view:View

        return if (viewType == TYPE_ITEM) {
            //Inflating recycle view item layout
            view = mLayoutInflater.inflate(R.layout.list_design_home, parent, false)
            ItemViewHolder(view)
        } else if (viewType == TYPE_HEADER) {
            //Inflating header view
            view = mLayoutInflater.inflate(R.layout.list_head, parent, false)
            HeaderViewHolder(view)
        } else {
            view = mLayoutInflater.inflate(R.layout.list_design_home, parent, false)
            ItemViewHolder(view)
        }
    }


    override fun onItemSwipe(position: Int) {

        //удалить слово из массива и из БД
        // val idWord = getWordId(position)

        var positWord = position - TYPE_ITEM
        var wr = words!![positWord]
        bd.delete(wr.id)
        words!!.removeAt(positWord)
        notifyItemRemoved(position)

        val insertedId = bd.insert(wr)

        //если вставка прошла успешно, то в зависимости от типа сортировки добавляем слово с список для отображения
        if(insertedId>0){

            val newWordPos:Int
            wr = Word(
                insertedId,
                wr.englishWord,
                wr.ruWord,
                wr.description,
                wr.priority,
                wr.datetime,
                wr.level
            )

            //получить context
            val context = mLayoutInflater.context
            if(sortWord == SortWord.OLD_DATA){
                newWordPos = getSizeArray()+TYPE_ITEM
                words!!.add(wr)
                notifyItemChanged(newWordPos)

                Toast.makeText(context,context.getString(R.string.down),Toast.LENGTH_SHORT).show()
            }
            else if(sortWord == SortWord.NEAR_DATA || sortWord == SortWord.DEFAULT){
                newWordPos = TYPE_ITEM
                words!!.add(newWordPos - TYPE_ITEM,wr)
                notifyItemInserted(newWordPos)

                Toast.makeText(context,context.getString(R.string.up),Toast.LENGTH_SHORT).show()
            }else{
                newWordPos = -1
            }

            if(position == selectedPos)
                bottomSheet!!.setAllDataFromWord(wr)
            //обновить курсор, когда делаем swipe
            updateCursor(
                RecyclerDictionaryLogic.newCursorPosAfterSwipe(newWordPos,position, selectedPos)
            )
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        if (holder is ItemViewHolder) {

            val h = holder as ItemViewHolder
            h.bind()

        }else if(holder is HeaderViewHolder){
            val h= holder as HeaderViewHolder
            h.update()
        }

        setAnimation(holder.itemView, position)

    }


    fun updateHeader(){
        notifyItemChanged(TYPE_HEADER)
    }

    //задать массив с данными слов
    override fun setArrayMyData(arrayMyData: ArrayList<Word>?) {
        super.setArrayMyData(arrayMyData)
        originalArr = arrayMyData
    }



    //green cursor for user
    fun updateCursor(newPositionCursor: Int) {
        //убираем старый
        notifyItemChanged(selectedPos)
        selectedPos = newPositionCursor
        //ставим новый
        notifyItemChanged(selectedPos)
    }

    fun getTypeSortWord(): SortWord {
        return sortWord
    }

    //добавить слово в зависимости от типа сортирвоки
    fun addWord(word: Word):Int{

        var indexInserted = TYPE_ITEM -1
        if(getTypeSortWord()== SortWord.NEAR_DATA || getTypeSortWord()== SortWord.DEFAULT){
            words!!.add(indexInserted,word)
        }
        else{
            words!!.add(word)

           indexInserted = getSizeArray() - 1 + TYPE_ITEM
        }

        notifyItemInserted(indexInserted)
        return indexInserted
    }

//    fun addWordEnd(word: Word){
//       words!!.add(word)
//    }
//
//    //TODO: NEED OPTIMIZE ADD WORD IN START
//    fun addWordStart(word: Word){
//        words!!.add(0,word)
//    }

    fun setOriginalList(){
        words = originalArr as ArrayList<Word>?
    }

    //задать слово, по которому сортируем лист
    fun setSortStartWords(sortStartWords: String) {
        this.sortStartWords = sortStartWords
    }

    //Задать сортировать по (дата, по умолчанию)
//    fun setSortWord(sortWord: SortWord) {
//        this.sortWord = sortWord
//    }

    //задать язык сортировки
    fun setLanguageSortWord(languageWord: LanguageWord) {
        this.languageWord = languageWord
        notifyDataSetChanged()
    }

    //Сортировать слово по дате или по умолчанию
    fun sortWords(sortWord: SortWord) {

        this.sortWord = sortWord

        when (sortWord) {
            SortWord.OLD_DATA -> words!!.sortWith(Sorter.SORT_BY_OLD_DATA)
            SortWord.NEAR_DATA -> words!!.sortWith(Sorter.SORT_BY_NEAR_DATA)
            SortWord.NAME ->{
                words = ArrayList(Sorter.getWordsByStartSymbols(originalArr!!, sortStartWords, languageWord))
            }
            SortWord.DEFAULT->{
                words = bd.selectAllFromEnd()
                originalArr = words
            }
            SortWord.PRIORITY -> {
                words = ArrayList(Sorter.SORT_BY_PRIORITY(words!!))
                originalArr = words

            }
        }

        //обновить список полностью т.к. представление слов было полностью изменено
        notifyDataSetChanged()
        bottomSheet!!.expandWindow(false)
        updateCursor(-1)
    }

    //удалить слово
    fun delete(word: Word) {
        words!!.remove(word)
    }

    //обновить слово по Id в листе
    fun updateByCursor(word: Word) {
        //long []ids = words.
        words!![ selectedPos-TYPE_ITEM] = word
    }

    //Получить элемент с массива со словами по индексу
    private fun  getItem(position: Int): Word? {
        if(position>=0 && position<getSizeArray())
            return words!![position]
        else return null
    }


    inner class ItemViewHolder internal constructor(view: View) : RecyclerView.ViewHolder(view) {

        private val field1 //английское слово
                : TextView = view.findViewById(R.id.field_big)
        private val field2 //русское слово
                : TextView = view.findViewById(R.id.field_small)

        private val changePriorityImg //приоритет слова в листе и изображение
                : ImageSwitcher = view.findViewById(R.id.change_priority)
        private val time//русское слово
                : TextView = view.findViewById(R.id.timeV) //время
        private val level//русское слово
                : TextView = view.findViewById(R.id.level) //время

        fun bind(){


            //отслеживаем позицию недавнего удаления
            var deletedPosRecently:Int = -1

            // Log.d("speed scroll",String.valueOf(cnt++));
            itemView.isSelected = selectedPos == layoutPosition



            //при долгом нажатии удалить слово
            itemView.setOnLongClickListener {

                val posInArray = layoutPosition-TYPE_ITEM
                val idWord = getWordId(posInArray)

                bd.delete(idWord)
                words!!.removeAt(posInArray)
                //сохраняем позицию недавнего удаления
                deletedPosRecently = posInArray

                val editingWordWasDeleted:Boolean = bottomSheet?.editingWordWasDeleted(idWord) == true

                //Если позиция элемента в листе не вышла за пределы массива слов
                if (posInArray < getSizeArray() && !editingWordWasDeleted) {
                    //bottomSheet?.setAllDataFromWord(getItem(posit), posit)

                    //если было слово впереди удалено, то обновить позицию курсора для выбранного элемента на шаг назад
                    if(selectedPos>deletedPosRecently)
                        selectedPos-=1

                    updateCursor(selectedPos)
                } else if(editingWordWasDeleted){ //если слово было удалено
                    updateHeader()//обновить поле для подсчета слов
                    updateCursor(-1)
                    bottomSheet?.expandWindow(false)
                }

                notifyItemRemoved(layoutPosition)
                Toast.makeText(mLayoutInflater.context, mLayoutInflater.context.getString(R.string.removed), Toast.LENGTH_SHORT).show()

                false
            }


            /**
             * По умолчанию слова сохраняются от последней даты до недавней,
             * если сортировка задана с недавней даты, то необходимо брать слова с отчета последнего слова в массиве **/
            //получить слово учитывая текущую позицию в списке, с учетом начала позиции отобрадения слов
            val wr = words!![adapterPosition- TYPE_ITEM]


            //init holder events
            //При нажатии на элемент в листе нужно открыть панель редактирования слова
            itemView.setOnClickListener {

                /**
                 * если недавно произошло удалнение, то запрещаем открывать лист для редактирования слова
                 * ПРИЧИНА: пользователь делает случайное нажатие на последующий элемент списка (верх или низ),
                 * хотя он не хотел
                 * **/

                //учитываем позицию начала элемента в листе recycler view, не только words
               // val wr = getItem(layoutPosition-TYPE_ITEM)
                if (layoutPosition >= 0 && deletedPosRecently<0) {

                    Toast.makeText(mLayoutInflater.context,layoutPosition.toString(), Toast.LENGTH_SHORT).show()

                    bottomSheet?.setAllDataFromWord(wr)
                    bottomSheet?.expandWindow(true)
                    updateCursor(layoutPosition)

                }else if(deletedPosRecently > 0){
                    deletedPosRecently = -1
                }
                //Toast.makeText(mLayoutInflater.context,
                //      "layout pos "+holder.layoutPosition + "  adapter pos " + holder.adapterPosition , Toast.LENGTH_SHORT).show()

            }


            // holder.itemView.setOnTouchListener(OnSwipeTouchListener(holder.itemView.context))

            /**
             * переключить иображение приоритета слова
             * eсли приоритет > 0 и изображение на selected не выбрано, то меняем изображение
             * если изображение выбрано (selected), то при листании RecycleView он не должен переключать его на обратное
             * **/

            val switchPriorityImg = {

                if(wr.priority>0 && changePriorityImg.currentView.id != R.id.selected){
                    changePriorityImg.showNext()
                }else if(wr.priority<1 && changePriorityImg.currentView.id == R.id.selected ){
                    changePriorityImg.showNext()
                }
            }


            //переключить изобраение "приоритет" в зависимрости от приоритета слова
            switchPriorityImg()

            //holder.id.text = (position + 1).toString()
            languageSort(wr)
            
            //Изменить приоритет слова понажатию на image

            changePriorityImg.setOnClickListener {
                wr.updatePriority()
                bd.update(wr)
                changePriorityImg.showNext()
            }


            time.text = RecyclerDictionaryLogic.getTimePassedFromCurrent(wr.datetime)
            setLevel(wr)
        }


        //Задать язык сортировки
        private fun languageSort(wr: Word) {

            val eng = wr.englishWord
            val rus = wr.ruWord

            if (languageWord != LanguageWord.RUSSIAN) {
                field1.text = eng
                field2.text = rus
            } else {
                field1.text = rus
                field2.text = eng
            }
        }

        private fun setLevel(wr: Word){
            val lv = "${wr.level}"
            level.text = lv

            level.visibility = View.VISIBLE
            when(wr.level){
                0->{
                    level.visibility = View.INVISIBLE
                    level.setBackgroundResource(R.drawable.shape_circle)
                }
                1,2,3->{
                    level.setBackgroundResource(R.drawable.ic_easy)
                }
                4,5,6->{
                    level.setBackgroundResource(R.drawable.ic_middle)
                }
                7,8,9->{
                    level.setBackgroundResource(R.drawable.ic_hight)
                }
                else->{
                    level.setBackgroundResource(R.drawable.ic_legendary)
                }
            }
        }

    }



    inner class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var countWord: TextView = view.findViewById<TextView>(R.id.count_words)

        fun update(){
            val countW = mLayoutInflater.context.getString(R.string.words) + getSizeArray().toString()
            countWord.text = countW
        }

    }

}



