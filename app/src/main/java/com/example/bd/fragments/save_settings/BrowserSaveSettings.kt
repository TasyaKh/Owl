package com.example.bd.fragments.save_settings

import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import com.example.bd.logic.BD
import com.example.bd.logic.browser.Link
import com.example.bd.logic.browser.Parser
import java.net.URL
import java.util.concurrent.Executors

//TODO:NOT USE
class BrowserSaveSettings(private val context: Context) {

    companion object{
        private const val ID= "Id" //last link id
        private const val LAST_LINK_URL= "LastLinkURL" //last link
        private const val TITLE= "Title" //last link title

        private const val SCROLL_POS_Y= "PosX" //last link title

        private const val SAVE_SETTINGS_LINKS = "SaveSettingsLinks" //дать название
    }


    private val settings //нужен, чтобы сохранить настройки
            : SharedPreferences= context.getSharedPreferences(SAVE_SETTINGS_LINKS, Context.MODE_PRIVATE)


    //сохранить последнюю ссылку
    private fun saveLink(link:Link) {

        val editor = settings.edit()
        editor.putString(LAST_LINK_URL, link.url)
        editor.putString(TITLE, link.title)
        editor.putLong(ID, link.id)

        editor.apply()
    }

    //выгрузить последнюю ссылку
    fun loadLastLink(): Link {

        val url: String = settings.getString(LAST_LINK_URL, "") as String
        val title: String = settings.getString(TITLE, "") as String
        val id: Long = settings.getLong(ID, -1)

        return Link(id, url, title)
    }

//    private fun saveScrollPosInSite(posY:Int){
//        val editor = settings.edit()
//        editor.putInt(SCROLL_POS_Y, posY)
//
//        editor.apply()
//    }

//    fun loadScrollPosInSite(): Int {
//
//        return settings.getInt(SCROLL_POS_Y, 0)
//    }

    fun  saveGoBackForvard(){

    }

    fun saveWebViewState(currentUrl:String?){

        //получить ссылку из ресурсов
        var lastLink =  loadLastLink()
      //  saveScrollPosInSite(scrollY)
        //если текущий линк равен сохраненному то ничего не делаем
        if(lastLink.url != currentUrl && currentUrl!=null){

            val executor = Executors.newSingleThreadExecutor()
            val handler = Handler(Looper.getMainLooper())

            executor.execute {

                val bd = BD(context)
                val parser = Parser( currentUrl)
                parser.connectDoc()
                val title = parser.getTitle()

                //сохранить последнюю ссылку
                handler.post {

                    val u = URL(currentUrl)

                    //если сайт тот же, но страница на нем другая, то пересохранить существующий путь
                    if(u.host.equals(URL(lastLink.url).host)){
                        lastLink = Link(lastLink.id,  currentUrl, title)

                        val savedCount = bd.updateLink(lastLink)  //обновить существующую ссылку
                        if(savedCount<=0 ){ // создать и сохранить новую ссылку, если прошлая была удалаена из БД

                            val insertedId = bd.insertLink(lastLink)
                            lastLink = Link(insertedId, currentUrl, title)
                        }
                    }else{ //если сайт другой, то создать и сохранить его
                        lastLink = Link(-1,  currentUrl, title)
                        val insertedId = bd.insertLink(lastLink)
                        lastLink = Link(insertedId, currentUrl, title)
                    }

                    saveLink(lastLink)
                }
            }
        }else  saveLink(lastLink)


    }

}