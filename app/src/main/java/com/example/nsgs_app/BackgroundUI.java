package com.example.nsgs_app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.view.View;

public class BackgroundUI {

    private static AnimationDrawable animationDrawable;

    public static void backgroundUI(Context context, View view){

        animationDrawable = (AnimationDrawable) view.getBackground();

        view.setBackground(animationDrawable);
        if(animationDrawable != null){
            animationDrawable.setEnterFadeDuration(2500);
            animationDrawable.setExitFadeDuration(5000);
            animationDrawable.start();
        } else{
            System.out.println("AnimationDrawable is null");
        }

    }







}
