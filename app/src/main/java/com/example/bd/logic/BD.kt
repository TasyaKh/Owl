package com.example.bd.logic

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.DatabaseUtils
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.bd.logic.browser.Link
import com.example.bd.logic.words.Word

class BD(context: Context?) {
    private val mDataBase //запросы к бд делает (бд сюда сохранить)
            : SQLiteDatabase


    //Имена таблиц в бд
  fun getTablesNames():String{
      val c: Cursor = mDataBase.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null)
      var names = ""

      if (c.moveToFirst()) {
          while (!c.isAfterLast) {
             names+= c.getString(0) + " "

              c.moveToNext()
          }
      }
        c.close()

      return names
  }


    fun insert(word: Word): Long { //вставить данные в бд
        val cv = getContentValuesWord(word)

        return mDataBase.insert(TABLE_NAME_WORDS, null, cv) //вставить эту сформированную строку в таблицу
    }


    private fun getContentValuesWord(word: Word): ContentValues{

        val cv = ContentValues() //та же ситуация, что и с Insert

        cv.put(COLUMN_WORD, word.englishWord)
        cv.put(COLUMN_TRANSLATE, word.ruWord)
        cv.put(COLUMN_DESCRIPTION, word.description)
        cv.put(COLUMN_PRIORITY, word.priority)
        cv.put(COLUMN_DATETIME,word.datetime)  //получить дату создания слова
        cv.put(COLUMN_LEVEL,word.level)

        return cv
    }


    fun update(word: Word): Int {   //обновить поле
        val cv = getContentValuesWord(word)

        //mDataBase.query(TABLE_NAME,null,"id = " + wr.getId(),null,null,null,null);
        return mDataBase.update(TABLE_NAME_WORDS, cv, "$COLUMN_ID = ?", arrayOf(word.id.toString()))
    }

    fun deleteAll() {   //снести все
        mDataBase.delete(TABLE_NAME_WORDS, null, null)
    }

    fun delete(id: Long) { //удалить по id
        mDataBase.delete(TABLE_NAME_WORDS, "$COLUMN_ID = ?", arrayOf(id.toString()))
    }

//    fun swap(w1:Word, w2:Word){
//
//        val newWord1Id = Word(w2.id, w1.ruWord,w1.ruWord,w1.description, w1.priority, w1.datetime)
//        val newWord2Id = Word(w1.id, w2.ruWord,w2.ruWord,w2.description, w2.priority, w2.datetime)
//
//        update(newWord1Id)
//        update(newWord2Id)
//    }

    fun select(id: Long): Word { //выбрать строку по id
        //ищет строку (некий курсор на строку ставим)
        val mCursor = mDataBase.query(
            TABLE_NAME_WORDS,
            null,
            "$COLUMN_ID = ?",
            arrayOf(id.toString()),
            null,
            null,
            null
        )

        mCursor.moveToFirst()
        val word =  getWordFromCursor(mCursor)

        mCursor.close()
        return word
    }


    //get word from cursor
    private fun getWordFromCursor(mCursor:Cursor): Word {

        val id =  mCursor.getLong(NUM_COLUMN_ID)
        val ruWord = mCursor.getString(NUM_COLUMN_WORD)
        val enWord = mCursor.getString(NUM_COLUMN_TRANSLATE)
        val description = mCursor.getString(NUM_COLUMN_DESCRIPTION)
        val priority = mCursor.getInt(NUM_COLUMN_PRIORITY)
        val datetime = mCursor.getLong(NUM_COLUMN_DATETIME)
        val level = mCursor.getInt(NUM_COLUMN_LEVEL)

        return Word(
            id,
            ruWord,
            enWord,
            description,
            priority,
            datetime,
            level
        )
    }


    fun countRowsWords(): Long {   //посчитать количество строк в таблице
        return DatabaseUtils.queryNumEntries(mDataBase, TABLE_NAME_WORDS)
    }


    fun selectAllFromStart(): ArrayList<Word> { //выбрать все данные с конца таблицы
        val mCursor = mDataBase.query(TABLE_NAME_WORDS, null, null, null, null, null, null)
        val arr = ArrayList<Word>()
        mCursor.moveToFirst()

        if (!mCursor.isAfterLast) {
            do {
                val word = getWordFromCursor(mCursor)

                arr.add(word)
            } while (mCursor.moveToNext())
        }

        mCursor.close()
        return arr
    }

    fun selectAllFromEnd(): ArrayList<Word> { //выбрать все данные с конца таблицы
        val mCursor = mDataBase.query(TABLE_NAME_WORDS, null, null, null, null, null, null)
        val arr = ArrayList<Word>()
        mCursor.moveToLast()

        if (!mCursor.isBeforeFirst) {
            do {
                val word = getWordFromCursor(mCursor)

                arr.add(word)
            } while (mCursor.moveToPrevious())
        }

        mCursor.close()
        return arr
    }


    fun selectPriorityWords(): ArrayList<Word> { //выбрать слова, которые имеют приоритет
        val mCursor = mDataBase.query(
            TABLE_NAME_WORDS, null,
            "$COLUMN_PRIORITY > 0", null,
            null, null, null
        )

        val arrPriority = ArrayList<Word>()
        mCursor.moveToFirst()

        if (!mCursor.isAfterLast) {
            do {
               val word = getWordFromCursor(mCursor)
                arrPriority.add(word)
            } while (mCursor.moveToNext())
        }

        mCursor.close()
        return arrPriority
    }


//   fun changeLevelOfWord(idWord:Long, levelUpOn:Int){
//
//       val query = "SELECT * FROM $TABLE_NAME_LEVEL WHERE $COLUMN_ID_WORD = $idWord"
//       val mCursor = mDataBase.rawQuery(query,null)
//       mCursor.moveToFirst()
//
//       if(mCursor.count<=0){
//           insertLevelOfWord(idWord,1)
//           //create new raw with level of word
//       }else{
//           val currentLevel = mCursor.getInt(NUM_COLUMN_LEVEL)
//           updateLevelOfWords(idWord,currentLevel + levelUpOn)
//       }
//
//       mCursor.close()
//
//    }


//    private fun updateLevelOfWords(idWord:Long, levelUpOn:Int):Int{
//
//        val cv = ContentValues() //хранит данные
//
//        cv.put(COLUMN_ID_WORD, idWord) //положить первую колонку
//        cv.put(COLUMN_LEVEL, levelUpOn) //во вторую
//
//        return mDataBase.update(TABLE_NAME_LEVEL, cv, "$COLUMN_ID_WORD = ?", arrayOf(idWord.toString()))
//    }
//
//
//    private fun insertLevelOfWord(idWord:Long, newLevel:Int):Long{
//
//        val cv = ContentValues() //хранит данные
//
//        cv.put(COLUMN_ID_WORD, idWord) //положить первую колонку
//        cv.put(COLUMN_LEVEL, newLevel) //во вторую
//
//        return mDataBase.insert(TABLE_NAME_LEVEL, null, cv) //вставить эту сформированную строку в таблицу
//    }

    fun updateLink(link: Link): Int {   //обновить поле
        val cv = ContentValues() //та же ситуация, что и с Insert

        cv.put(COLUMN_LINKS_URL, link.url)
        cv.put(COLUMN_LINKS_TITLE, link.title)

        //mDataBase.query(TABLE_NAME,null,"id = " + wr.getId(),null,null,null,null);
        return mDataBase.update(TABLE_NAME_LINKS, cv, "$COLUMN_ID = ?", arrayOf(link.id.toString()))
    }

    fun selectLink(id: Long): Link { //выбрать строку по id
        //ищет строку (некий курсор на строку ставим)
        val mCursor = mDataBase.query(
            TABLE_NAME_LINKS,
            null,
            "$COLUMN_ID = ?",
            arrayOf(id.toString()),
            null,
            null,
            null
        )

        mCursor.moveToFirst()
        val id =  mCursor.getLong(NUM_COLUMN_ID_LINKS)
        val url = mCursor.getString(NUM_COLUMN_URL)
        val title = mCursor.getString(NUM_COLUMN_TITLE)

        val link = Link(id, url, title)

        mCursor.close()
        return link
    }


    fun insertLink(link:Link): Long {
        val cv = ContentValues() //та же ситуация, что и с Insert

        cv.put(COLUMN_LINKS_URL, link.url)
        cv.put(COLUMN_LINKS_TITLE, link.title)

        return mDataBase.insert(TABLE_NAME_LINKS, null, cv) //вставить эту сформированную строку в таблицу
    }

    fun clearAllLinks(){
        mDataBase.delete(TABLE_NAME_LINKS, null, null)
    }

    fun  deleteLink(idLink:Long){
        mDataBase.delete(TABLE_NAME_LINKS, "$COLUMN_ID_LINKS = ?", arrayOf(idLink.toString()))
    }


    fun selectAllFromEndLinks(): ArrayList<Link> { //выбрать все данные с конца таблицы
        val mCursor = mDataBase.query(TABLE_NAME_LINKS, null, null, null, null, null, null)
        val arr = ArrayList<Link>()
        mCursor.moveToLast()

        if (!mCursor.isBeforeFirst) {
            do {

                val id =  mCursor.getLong(NUM_COLUMN_ID_LINKS)
                val url = mCursor.getString(NUM_COLUMN_URL)
                val title = mCursor.getString(NUM_COLUMN_TITLE)

                val link = Link(id, url, title)
                arr.add(link)

            } while (mCursor.moveToPrevious())
        }

        mCursor.close()
        return arr
    }

    //Создать бд
    private class OpenHelper(context: Context?) :
        SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

        override fun onCreate(db: SQLiteDatabase) {

            createTableWords(db)
            createTableLinks(db)
        }


        private fun createTableWords(db: SQLiteDatabase){
            //table for words
            val query = "CREATE TABLE $TABLE_NAME_WORDS (" +
                    "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$COLUMN_WORD TEXT, " +
                    "$COLUMN_TRANSLATE TEXT, " +
                    "$COLUMN_DESCRIPTION TEXT, " +
                    "$COLUMN_PRIORITY INTEGER, " +
                    "$COLUMN_DATETIME INTEGER, " +
                    "$COLUMN_LEVEL INTEGER);"
            db.execSQL(query)
        }



        private fun createTableLinks(db: SQLiteDatabase){

            //table for words
            val query = "CREATE TABLE $TABLE_NAME_LINKS (" +
                    "$COLUMN_ID_LINKS INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$COLUMN_LINKS_URL TEXT, " +
                    "$COLUMN_LINKS_TITLE TEXT);"
            db.execSQL(query)
        }


        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            //db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            //onCreate(db);
        }
    }

    companion object {

        private const val DATABASE_NAME = "simple.db" //имя БД
        private const val DATABASE_VERSION = 1 //Версия БД

        //table 1 for words
        private const val TABLE_NAME_WORDS = "Words" //Имя таблицы

        private const val COLUMN_ID = "id" //столбец ид
        private const val COLUMN_WORD = "Word" //столбец слово
        private const val COLUMN_TRANSLATE = "Translate" //столбец перевод
        private const val COLUMN_DESCRIPTION = "Description" //столбец описание
        private const val COLUMN_PRIORITY = "Priority" //столбец приоритет
        private const val COLUMN_DATETIME = "Datetime" //столбец когда слово было создано
        private const val COLUMN_LEVEL = "Level" //столбец уровня

        private const val NUM_COLUMN_ID = 0 //столбец ид номер
        private const val NUM_COLUMN_WORD = 1 //столбец слово номер
        private const val NUM_COLUMN_TRANSLATE = 2 //столбец перевод номер
        private const val NUM_COLUMN_DESCRIPTION = 3 //столбец описание номер
        private const val NUM_COLUMN_PRIORITY = 4 //столбец приоритет номер
        private const val NUM_COLUMN_DATETIME = 5 //столбец времени,когда слово было создано
        private const val NUM_COLUMN_LEVEL = 6 //столбец уровня



        //table 2 for words
        private const val TABLE_NAME_LINKS= "Links" //Имя таблицы

        private const val COLUMN_ID_LINKS = "id"
        private const val COLUMN_LINKS_URL= "Url"
        private const val COLUMN_LINKS_TITLE = "Title"

        private const val NUM_COLUMN_ID_LINKS = 0
        private const val NUM_COLUMN_URL = 1
        private const val NUM_COLUMN_TITLE = 2



    }

    init {
        val mOpenHelper = OpenHelper(context) //создать базу данных
        mDataBase =
            mOpenHelper.writableDatabase //передаем созданную бузу данных для дальнейшего управления
    }
}