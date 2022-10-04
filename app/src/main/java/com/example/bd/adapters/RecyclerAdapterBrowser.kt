package com.example.bd.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bd.R
import com.example.bd.collbacks.ItemTouchHelperAdapter
import com.example.bd.fragments.IChangeURL
import com.example.bd.fragments.IWorkWithDB
import com.example.bd.logic.BD
import com.example.bd.logic.browser.Link
import java.util.ArrayList

class RecyclerAdapterBrowser(ctx:Context, private var links //массив выводимых слов
: ArrayList<Link>, changeURL: IChangeURL,private val bd:BD
) : RecyclerView.Adapter<RecyclerAdapterBrowser.ViewHolder>(), ItemTouchHelperAdapter {

    private val mLayoutInflater //привязывает все лайоуты (прямоугольники со словами к фрагменту)
            : LayoutInflater = LayoutInflater.from(ctx)

    private val changeURL:IChangeURL = changeURL


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerAdapterBrowser.ViewHolder {

        val view = mLayoutInflater.inflate(R.layout.link_item_list, parent, false)
        return ViewHolder(view)
    }

    fun addLink(index:Int, link:Link){

        var insert = index
        if(index <  links.size)insert = 0
        else if(index > links.size)insert =  links.size - 1

        links.add(insert, link)
        notifyItemInserted(insert)
    }


    override fun onBindViewHolder(holder: RecyclerAdapterBrowser.ViewHolder, position: Int) {

        val link = links[position]
        holder.url.text = link.url
        holder.title.text = link.title

        holder.itemView.setOnClickListener {
            changeURL.changeURL(link.url)

//            bd.deleteLink(link.id)
//            links.removeAt(position)
//            notifyItemRemoved(position)
//
//            val newId =  bd.insertLink(link)
//            links.add(Link(newId,link.url,link.title))
//            notifyItemInserted(itemCount - 1)

        }

    }


    override fun getItemCount(): Int {
        return links.size
    }

    fun  clearAll(){
        links.clear()
        notifyDataSetChanged()
    }





    inner class ViewHolder internal constructor(view: View) : RecyclerView.ViewHolder(view) {

        val url
                : TextView = view.findViewById(R.id.url)
        val title
                : TextView= view.findViewById(R.id.title)


    }


    override fun onItemSwipe(position: Int) {
        val link = links.removeAt(position)
        notifyItemRemoved(position)

        bd.deleteLink(link.id)
    }

}