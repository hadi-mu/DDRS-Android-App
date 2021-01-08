package com.example.testapp

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import kotlinx.android.synthetic.main.activity_log.*
import java.io.BufferedReader
import java.io.InputStreamReader

class LogActivity : AppCompatActivity(){

    var level2Array= arrayListOf<String>()
    var level3Array= arrayListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log)
        loadData()
        plotGraph()
    }
    fun loadData() {
        val level2LogInputStream = openFileInput("level2Log.txt")
        val level3LogInputStream = openFileInput("level3log.txt")
        val level2InputStreamReader = InputStreamReader(level2LogInputStream)
        val level3InputStreamReader = InputStreamReader(level3LogInputStream)
        val level2BufferedReader = BufferedReader(level2InputStreamReader)
        val level3BufferedReader = BufferedReader(level3InputStreamReader)
        val level2Text = level2BufferedReader.readLine()
        val level3Text = level3BufferedReader.readLine()
        var l2Count = 0
        var l2start = 0
        if (level2Text != null) {
            l2Count = 0
            l2start = 0
            while (l2Count < level2Text.length) {
                if (level2Text[l2Count] == ',') {
                    level2Array.add(level2Text.substring(l2start, l2Count))
                    l2start = l2Count + 1
                    l2Count += 1
                } else {
                    l2Count += 1
                }
            }
        } else {
            for (x in 1..24) {
                level2Array.add("0")
            }}
            if (level3Text.length > 20) {
                var l3Count = 0
                var l3Start = 0
                while (l3Count < level3Text.length) {
                    if (level3Text[l3Count] == ',') {
                        level3Array.add(level3Text.substring(l3Start, l3Count))
                        l3Start=l3Count+1
                        l3Count += 1
                    } else {
                        l3Count += 1
                    }
                }
            } else {
                Log.d("Level3", "Runs2")
                for (x in 1..24) {
                    level3Array.add("0")
                }
            }


    }
    fun plotGraph(){
        val level2Series:LineGraphSeries<DataPoint> = LineGraphSeries(arrayOf())
        var xlabel=0
        for (element in level2Array){
            val data=DataPoint(xlabel.toDouble(),element.toDouble())
            xlabel+=1
            level2Series.appendData(data,true,24)
        val level3Series:LineGraphSeries<DataPoint> = LineGraphSeries(arrayOf())
        var xlabel2=0
        Log.d("Level 3 ",level3Array.toString())
        for (value in level3Array){
            val point=DataPoint(xlabel2.toDouble(),value.toDouble())
            xlabel2+=1
            level3Series.appendData(point,true,24)
        }

        level2Series.title="Level 2 Plot"
        level3Series.title="Level 3 Plot"
        level2Series.color= Color.RED
        level3Series.color= Color.BLUE

        graph.addSeries(level2Series)
        graph.addSeries(level3Series)
        graph.viewport.setScalableY(true)

    }


}}