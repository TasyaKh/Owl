package com.example.bd.activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.Navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI.navigateUp
import androidx.navigation.ui.NavigationUI.setupActionBarWithNavController
import androidx.navigation.ui.NavigationUI.setupWithNavController
import com.example.bd.R
import com.example.bd.databinding.ActivityMainBinding
import com.example.bd.fragments.BrowserFragment


//до того как в активности изменилось какое либо состояние сохранит значение переменной
//TODO: ИЗМЕНИТЬ main activity
interface IActivityBeforeStateChanged{
    var activityWantToStop: Boolean //до того как активность будет приостановлена
}

class MainActivity : AppCompatActivity(), IActivityBeforeStateChanged {

    companion object{
        //const val RECUEST_CODE = 3
    }

    override var activityWantToStop = false

    private var mAppBarConfiguration //нужен, чтобы установить связь между кнопками менб+ю и фрагментами
            : AppBarConfiguration? = null
    private var binding //связывает различные элементы с активностью
            : ActivityMainBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        setSupportActionBar(binding!!.appBarMain.toolbar)
        val drawer = binding!!.drawerLayout
        val navigationView = binding!!.navView

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = AppBarConfiguration.Builder(
            R.id.nav_home, R.id.nav_reference, R.id.nav_play
        )
            .setOpenableLayout(drawer)
            .build()
        val navController = findNavController(this, R.id.nav_host_fragment_content_main)
        setupActionBarWithNavController(this, navController, mAppBarConfiguration!!)
        setupWithNavController(navigationView, navController)

//            alarmM.setRepeating(
//                AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), (
//                        1000 * 5).toLong(), pendingIntent
//            )
   }


//    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
//        if(keyCode == KeyEvent.KEYCODE_BACK){
//           return true
//        }else{
//           return super.onKeyDown(keyCode, event)
//        }
//    }

    override fun onBackPressed() {

        val navHost= this.supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main)

        if (navHost != null) {
            var currFragment = navHost.childFragmentManager.fragments.get(0)
            //вво фрагменте браузер по нажатию кнопки назад мы возвращаемся на предыдущую страницу html
            if(currFragment is BrowserFragment){
                currFragment =  currFragment as BrowserFragment
                currFragment.onBackPressed()
            }
//            Toast.makeText(applicationContext,
//                navHost.childFragmentManager.fragments.get(0).toString(),Toast.LENGTH_SHORT).show()
        }
    }

    //Создать выдвижное меню слева
    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(this, R.id.nav_host_fragment_content_main)
        return (navigateUp(navController, mAppBarConfiguration!!)
                || super.onSupportNavigateUp())
    }

    override fun onStart() {
        activityWantToStop = false
        super.onStart()
    }

    override fun onStop() {

        activityWantToStop = true
        super.onStop()
    }

}

