package com.example.bd.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import com.example.bd.R
import com.example.bd.databinding.FragmentBrowserBinding
import com.example.bd.fragments.save_settings.BrowserSaveSettings
import org.adblockplus.libadblockplus.android.webview.AdblockWebView


interface IChangeURL{
    fun changeURL(url:String)
}

interface IOnBackPressedFragment{
    fun onBackPressed()
}

class BrowserFragment : Fragment(), IChangeURL, IOnBackPressedFragment {
    private var _binding //Связать с View
            : FragmentBrowserBinding? = null
    private val binding get() = _binding!!

    //  private val bd = BD(context)

    //Установить View
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBrowserBinding.inflate(inflater, container, false)
        return _binding!!.root
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        //You need to add the following line for this solution to work; thanks skayred
        //You need to add the following line for this solution to work; thanks skayred



        val settings = BrowserSaveSettings(requireContext())
        val webView = view.findViewById<AdblockWebView>(R.id.browser)


        val scrollPosY: Int? =  savedInstanceState?.getInt("webviewScrollY")
        val linkLast: String = settings.loadLastLink().url

        webView.loadUrl(linkLast)

        if (scrollPosY != null) {
            webView.scrollY = scrollPosY
        }

        webView!!.settings.apply {
            defaultTextEncodingName = "utf-8"
            // builtInZoomControls = true
            javaScriptEnabled = true
        }



//        view.isFocusableInTouchMode = true
//        view.requestFocus()
//        view.setOnKeyListener { v, keyCode, event ->
//            if( keyCode == KeyEvent.KEYCODE_BACK){
//               if(webView.canGoBack())
//                   webView.goBack()
//            }
//
//            true
//        }



        //create menu back, forward navigation
        val menuHost: MenuHost = requireActivity()

        menuHost.addMenuProvider(object : MenuProvider {

            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // Add menu items here
                menuInflater.inflate(R.menu.menu_browser_bar, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Handle the menu selection

                val itemIsChecked: Boolean = when (menuItem.itemId) {
                    R.id.back -> {
                        if (webView.canGoBack()) webView.goBack()
                        true
                    }
                    R.id.forward -> {
                        if (webView.canGoForward()) webView.goForward()
                        true
                    }
                    R.id.links -> {
                        BrowserAlertDialog(context,this@BrowserFragment as IChangeURL).show()
                        true
                    }
                    else -> false
                }

                menuItem.isChecked = itemIsChecked

                return itemIsChecked
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)


        super.onViewCreated(view, savedInstanceState)
    }

//    //TODO: SET APP BAR PARAMS FOR SCROLL
//    private fun setAppBarParams(setParams:Boolean){
//        val toolbar:CollapsingToolbarLayout? = activity?.findViewById(R.id.collapsing_toolbar)
//        val params = toolbar?.layoutParams as  AppBarLayout.LayoutParams
//
//        if(setParams)
//        params.scrollFlags = (AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL
//                or AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS)
//        else params.scrollFlags = 0
//    }

    override fun changeURL(url: String) {
        binding.browser.loadUrl(url)
    }

    //Отобразить содержимое (текст)
    override fun onStart() {
        super.onStart()

        val view = view
        if (view == null) {
            Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show()
            return
        }
    }


    //Уничтожить View
    override fun onSaveInstanceState(outState: Bundle) {
        val settings = BrowserSaveSettings(requireContext())
        val webview = binding.browser
        settings.saveWebViewState(webview.url)

       //сохранить позицию scroll-а
        outState.putInt("webviewScrollY", webview.scrollY)
       // webview.saveState(outState)
       // settings.saveGoBackForvard(webview.copyBackForwardList())
        super.onSaveInstanceState(outState)
    }



    override fun onBackPressed() {
        val webview = binding.browser
        if(webview.canGoBack() ){
            webview.goBack()
        }
    }


}








////for web view
//class WebViewClientImpl(activity: Activity?) : WebViewClient() {
//    private var activity: Activity? = null
//
//    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
//
//        //разрешает открывать последующую ссылку в этом же окне приложения
//        if (request != null) {
//            view?.loadUrl(request.url.toString())
//        }
//
//        return super.shouldOverrideUrlLoading(view, request)
//    }
//
//
////    override fun shouldInterceptRequest(
////        view: WebView?,
////        request: WebResourceRequest?
////    ): WebResourceResponse? {
////
////        val url = request?.url.toString()
////
////        if (url.contains("google") || url.contains("facebook")) {
////            val textStream: InputStream = ByteArrayInputStream("".toByteArray())
////            return getTextWebResource(textStream)
////        }
////
////        return super.shouldInterceptRequest(view, request)
////    }
//
//    private fun getTextWebResource(data: InputStream): WebResourceResponse {
//        return
//        WebResourceResponse("text/plain", "UTF-8", data)
//    }
//}


//    init {
//        this.activity = activity
//    }
//}