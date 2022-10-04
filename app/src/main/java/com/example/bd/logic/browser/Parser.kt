package com.example.bd.logic.browser

import org.jsoup.HttpStatusException
import org.jsoup.Jsoup
import org.jsoup.UnsupportedMimeTypeException
import org.jsoup.nodes.Document
import java.io.IOException
import java.net.MalformedURLException
import java.net.SocketTimeoutException

class Parser(private val url:String){

    private var doc: Document? = null

    fun getTitle():String{
      return doc?.title() ?: ""
    }

    @Throws(HttpStatusException::class, SocketTimeoutException::class,
        MalformedURLException::class, UnsupportedMimeTypeException::class, IOException::class)
    fun connectDoc():Boolean{

        var res = false
        try {
            doc = Jsoup.connect(url).get()
            res = true
        }catch (e:HttpStatusException){

        }catch (e: SocketTimeoutException){

        }catch (e: MalformedURLException){

        }catch (e: UnsupportedMimeTypeException){

        }catch (e: IOException){

        }

     return res
    }

//    fun execute():Future<Link> {
//
//            val task: Callable<Link> =
//                Callable<Link> {
//                    initDoc()
//                    val title = getTitle()
//                    Link(-1, url, title)
//                }
//
//        val executor = Executors.newSingleThreadExecutor()
//        val result  = executor.submit(task)
//
//       return result
//
//    }
}