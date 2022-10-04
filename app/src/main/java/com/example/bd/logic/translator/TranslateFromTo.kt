package com.example.bd.logic.translator

import android.util.Log
import android.widget.TextView
import com.google.android.gms.tasks.Task
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.*


class TranslateFromTo(from: String, to: String) {

    //переводчик получить
    private val translator: Translator
    // Download the model.

    //модель языка
    //private var langModel:TranslateRemoteModel = TranslateRemoteModel.Builder(to).build()


    fun getTranslator(): Translator{

        return translator
    }


//    private fun modelIsExists(): Boolean {
//
//        return modelManager.isModelDownloaded(langModel)
//    }

    //install model
    private fun downloadModel(langDownload:String){

        // Download the model.

        val modelManager = RemoteModelManager.getInstance()
        val langModel = TranslateRemoteModel.Builder(langDownload).build()

        val conditions = DownloadConditions.Builder()
            .build()
        modelManager.download(langModel, conditions)
            .addOnSuccessListener {
                Log.d("Model","$langDownload downloaded")
                // Model downloaded.
            }
            .addOnFailureListener {
                Log.d("Model","$langDownload not downloaded")
                // Error.
            }
    }


    //delete model
    fun deleteModel(langDelete:String){


        val modelManager = RemoteModelManager.getInstance()
        // Get translation models stored on the device.
        modelManager.getDownloadedModels(TranslateRemoteModel::class.java)
            .addOnSuccessListener { models ->
                // ...
            }
            .addOnFailureListener {
                // Error.
            }

        // Delete the German model if it's on the device.
        val germanModel = TranslateRemoteModel.Builder(langDelete).build()
        modelManager.deleteDownloadedModel(germanModel)
            .addOnSuccessListener {
                Log.d("model Deleting: ", langDelete)
                // Model deleted.
            }
            .addOnFailureListener {
                Log.e("model Deleting: ", "cant delete model: $langDelete")
                // Error.
            }

    }


    init {

        //deleteModel(to)
        //deleteModel(from)

        val options = TranslatorOptions.Builder()
            .setSourceLanguage(from)
            .setTargetLanguage(to)
            .build()

       // val modelManager = RemoteModelManager.getInstance()
        translator = Translation.getClient(options)

        translator.downloadModelIfNeeded()
            .addOnSuccessListener {
                Log.d("Dictionary","downloaded")}
            .addOnFailureListener {
                Log.d("Dictionary","not downloaded")}

    }


     fun translateTxt(saveInto: TextView, txt:String):Task<String>?{

         val transl:Task<String>? = null

         try {
             val translate = translator.translate(txt)
                 .addOnFailureListener {

                     saveInto.text = ("Error translate" + it.printStackTrace().toString())
                     Log.d("translate", it.printStackTrace().toString())
                 }
                 .addOnSuccessListener {
                     saveInto.text = it
                     Log.d("translated", it)
                 }


         }catch (e:IllegalStateException ){
             Log.e("Translator","was closed")
         }

    return transl
    }

    fun close(){
        translator.close()
    }
}