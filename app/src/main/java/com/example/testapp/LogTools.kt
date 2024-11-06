package com.example.testapp

import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import java.io.BufferedReader
import java.io.InputStreamReader

class LogTools (private val context: Context){
    private var level2Array= arrayListOf<String>()
    private var level3Array= arrayListOf<String>()

    fun createLogs() {
        try {
            val level2LogOutputStream = context.openFileOutput("level2Log.txt",
                AppCompatActivity.MODE_PRIVATE
            )
            val level3LogOutputStream = context.openFileOutput("level3log.txt",
                AppCompatActivity.MODE_PRIVATE
            )
            for (i in 1..24) {
                level2LogOutputStream.write("0,".toByteArray())
                level3LogOutputStream.write("0,".toByteArray())
            }
        }catch(e: Exception){
            Log.e("Error", e.toString())}
        val prefs = context.getSharedPreferences("prefs", AppCompatActivity.MODE_PRIVATE)
        val editor=prefs.edit()
        editor.putBoolean("firstStart",false)
        editor.apply()
    }
    fun updateLogs(){
        val level2LogInputStream=context.openFileInput("level2Log.txt")
        val level3LogInputStream=context.openFileInput("level3log.txt")
        val level2InputStreamReader=InputStreamReader(level2LogInputStream)
        val level3InputStreamReader=InputStreamReader(level3LogInputStream)
        val level2BufferedReader= BufferedReader(level2InputStreamReader)
        val level3BufferedReader= BufferedReader(level3InputStreamReader)
        val level2Text=level2BufferedReader.readLine()
        val level3Text=level3BufferedReader.readLine()
        var i=0
        var start=0
        if (level2Text.length>20){
            i=0
            start=0
            while(i<level2Text.length){
                if (level2Text[i]==','){
                    level2Array.add(level2Text.substring(start,i))
                    start=i+1
                    i+=1
                }else{
                    i+=1
                }}
        }
        else{
            for(x in 1..24){
                level2Array.add("0")
            }}
        if(level3Text!=null) {
            i=0
            start=0
            while (i < level3Text.length) {
                if (level3Text[i] == ',') {
                    level3Array.add(level3Text.substring(start, i))
                    start = i + 1
                    i += 1
                } else {
                    i += 1
                }
            }
        }else{
            for(y in 1..24){
                level3Array.add("0")
            }


        }}



    private fun saveLogs(){
        val level2LogOutputStream = context.openFileOutput("level2Log.txt",
            AppCompatActivity.MODE_PRIVATE
        )
        val level3LogOutputStream = context.openFileOutput("level3log.txt",
            AppCompatActivity.MODE_PRIVATE
        )
        for (element in level2Array) {
            level2LogOutputStream.write(element.toByteArray())
            level2LogOutputStream.write(",".toByteArray())
        }
        for (element in level3Array) {
            level3LogOutputStream.write(element.toByteArray())
            level3LogOutputStream.write(",".toByteArray())
        }
    }

    //problem here
    fun logLevel2Drowsiness(time:Int){
        level2Array[time]=((level2Array[time].toInt())+1).toString()
        Log.d("Level 2 Array",level2Array.toString())
        saveLogs()

    }

    fun logLevel3Drowsiness(time:Int){
        level3Array[time]=((level3Array[time].toInt())+1).toString()
        Log.d("Level 3 Array",level3Array.toString())
        saveLogs()
    }

}