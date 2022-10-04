package com.example.bd.activities.notifications

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters


class UploadWorker(appContext: Context, workerParams: WorkerParameters):
    Worker(appContext, workerParams) {

    override fun doWork(): Result {

        val title = inputData.getString("title")
        val text = inputData.getString("text")


        if (title != null && text!=null
            && title.isNotEmpty() && text.isNotEmpty()) {

            Notification(context = applicationContext, title,text)
        }else {
            Result.failure()
        }

        // Indicate whether the work finished successfully with the Result
        return Result.success()
    }
}
