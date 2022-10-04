package com.example.bd.animation

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils

object AnimationTypes {

    fun animateFadeInOuT(view: View, fadeIn:Boolean, context: Context?){

        val anim: Animation

        if(fadeIn){
            anim = AnimationUtils.loadAnimation(context, android.R.anim.fade_in)
            view.visibility = View.VISIBLE
        }else{
            anim = AnimationUtils.loadAnimation(context, android.R.anim.fade_out)
            view.visibility = View.INVISIBLE
        }

        anim.duration = 100
        view.startAnimation(anim)
    }


    //изменить цвет элемента from to
    fun changeBackgroundColor(colorFrom: Int, colorTo: Int, view:View) {
        val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo)
        colorAnimation.duration = 1000 // milliseconds

        //анимировать цвет
        colorAnimation.addUpdateListener { animator: ValueAnimator ->
            view.setBackgroundColor(animator.animatedValue as Int)
        }
        colorAnimation.start()
    }

}