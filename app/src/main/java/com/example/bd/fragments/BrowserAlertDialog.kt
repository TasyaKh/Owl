package com.example.bd.fragments


import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.webkit.URLUtil
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.recyclerview.widget.ItemTouchHelper
import com.example.bd.R

import androidx.recyclerview.widget.RecyclerView
import com.example.bd.adapters.RecyclerAdapterBrowser
import com.example.bd.collbacks.SimpleItemTouchHelperCallback
import com.example.bd.logic.BD
import com.example.bd.logic.browser.Link
import com.example.bd.logic.browser.Parser
import java.util.concurrent.Executors


interface IWorkWithDB{
    fun deleteFromDB(idLink: Long)
}

//Всплывающий диалог (вызывается, когда во время игры хотим узнать результат наших попыток)
class BrowserAlertDialog (context: Context?,private val changeURL:IChangeURL) : AlertDialog(context){

    private lateinit var myAdapter //адаптер для листа для просмотра списка
            : RecyclerAdapterBrowser

    private val bd = BD(context)

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.browser_alert_dialog)

        myAdapter = RecyclerAdapterBrowser(context, bd.selectAllFromEndLinks(), changeURL, bd)

        val recyclerView: RecyclerView = findViewById(R.id.recyclerview)
        //Задать адаптер
        recyclerView.adapter = myAdapter
        //Задать адаптер и добавить колюэк для событий (например swipe)
        val callback: ItemTouchHelper.Callback = SimpleItemTouchHelperCallback(myAdapter)
        val touchHelper = ItemTouchHelper(callback)
        touchHelper.attachToRecyclerView(recyclerView)


        val writeLink = findViewById<EditText>(R.id.write_link)
        val addLink = findViewById<ImageButton>(R.id.add_link)

        addLink.setOnClickListener{
            val url:String = writeLink.text.toString().trim()

            writeLink.text.clear()

            val validURL = URLUtil.isValidUrl(url)

            if(url.isNotEmpty() && validURL){
                addLink(url)
            }else if(!validURL){
                Toast.makeText(context,R.string.url_invalid,Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(context,R.string.field_empty,Toast.LENGTH_SHORT).show()
            }

        }



        //почистить все
        val clearAll = findViewById<Button>(R.id.clear_all)
        clearAll.setOnClickListener{

            //создать диалог, который повторно пользователя спрашивает желает ли он все удалить
            val builder = Builder(context)
            builder.setTitle(R.string.do_you_want_del_all)
            builder.setPositiveButton(R.string.cancel, DialogInterface.OnClickListener { dialog, which ->
                Toast.makeText(context,R.string.canceled,Toast.LENGTH_SHORT).show()
            })
            builder.setNegativeButton(R.string.delete, DialogInterface.OnClickListener { dialog, which ->
                //удалить все ссылки с БД
                bd.clearAllLinks()
                //обновить лист со ссылками
                myAdapter.clearAll()
                Toast.makeText(context,R.string.deleted_all,Toast.LENGTH_SHORT).show()
            })

            builder.setCancelable(true)
            builder.show()

        }


    }


    private fun addLink(url:String){

        var link: Link
        val executor = Executors.newSingleThreadExecutor()
        val handler = Handler(Looper.getMainLooper())
        executor.execute {

            val parser = Parser(url)
            val connected = parser.connectDoc()
            val title = parser.getTitle()

            //сохранить последнюю ссылку }

            handler.post {

                link = Link(-1, url, title)
                //и в бд эту сссылку тоже сохранить
                val bd: BD = BD(context)
                val insertedId = bd.insertLink(link)

                link = Link(insertedId, url, title)
                myAdapter.addLink((myAdapter.itemCount -1),link)

                if(connected){

                    Toast.makeText(context, R.string.added,Toast.LENGTH_SHORT).show()
                }else  Toast.makeText(context, R.string.err_connection,Toast.LENGTH_SHORT).show()
            }
        }
    }

    //удалить ссылку из бд
//    override fun deleteFromDB(idLink: Long) {
//        bd.deleteLink(idLink)
//    }


}

