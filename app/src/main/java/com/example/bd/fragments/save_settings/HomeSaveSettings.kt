package com.example.bd.fragments.save_settings

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.bd.logic.words.LanguageWord
import com.example.bd.logic.words.SortWord
import java.util.HashSet

class HomeSaveSettings(context: Context) {

    companion object{
        private const val LANGUAGE_WORD = "LanguageWord" //язык
        private const val SORT_WORD = "SortWord" //тип сортировки

        private const val SAVE_SETTINGS = "SaveSettings" //дать название
    }

    private val settingsSort //нужен, чтобы сохранить настройки
            : SharedPreferences


    //сохранить тип языка
    fun saveLanguageWord(languageWord: LanguageWord) {
        val editor = settingsSort.edit()
        editor.putInt(LANGUAGE_WORD, languageWord.numEnum)
        editor.apply()
    }

    //сохранить тип сортировки
    fun saveSortWord(sortWord: SortWord) {
        val editor = settingsSort.edit()
        editor.putInt(SORT_WORD, sortWord.numEnum)
        editor.apply()
    }

    //выгрузить язык
    fun loadSettingsLanguageWord(): LanguageWord {
        var languageWord = LanguageWord.ENGLISH

        Log.d("prefs", settingsSort.all.toString())
        Log.d("savePref", settingsSort.getStringSet(SAVE_SETTINGS, HashSet()).toString())

        val numEnum = settingsSort.getInt(LANGUAGE_WORD, -1)

        for (`val` in LanguageWord.values()) {
            if (`val`.numEnum == numEnum) {
                languageWord = `val`
                break
            }
        }
        return languageWord
    }

    //выгрузить тип сортировки
    fun loadSettingsSortWord(): SortWord {
        var sortWord = SortWord.NEAR_DATA

        // Log.d("prefs", settingsSort.all.toString())
        //Log.d("savePref", settingsSort.getStringSet(SAVE_SETTINGS, HashSet()).toString())

        val numEnum = settingsSort.getInt(SORT_WORD, -1)

        for (`val` in SortWord.values()) {
            if (`val`.numEnum == numEnum) {
                sortWord = `val`
                break
            }
        }
        return sortWord
    }

    init {
        //контекст
        settingsSort = context.getSharedPreferences(SAVE_SETTINGS, Context.MODE_PRIVATE)
    }
}