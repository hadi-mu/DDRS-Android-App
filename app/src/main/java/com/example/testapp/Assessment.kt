package com.example.testapp

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.assessment_activity.*
import kotlin.random.Random

class Assessment: AppCompatActivity() {

    private val operatorArray = listOf("+", "-", "*")
    private var correctAnswers = 0
    private var question = ""
    private var answer = "none"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.assessment_activity)
        assess()
    }

    private fun assess() {
        var q = ""
        Log.d("START", "STARTED")
        when (correctAnswers) {
            0 -> {
                q = easyQuestion()
                layout.setBackgroundColor(Color.RED)
            }
            1 -> {
                q = mediumQuestion()
                layout.setBackgroundColor(Color.RED)
            }
            2 -> {
                q = hardQuestion()
                layout.setBackgroundColor(Color.RED)
            }
            3 -> readyToDrive()
        }
        question = q
        qText.text = question
    }


    private fun readyToDrive() {
        readinessIndicator.text = "READY TO DRIVE!"
        layout.setBackgroundColor(Color.GREEN)
    }

    fun checkAnswer(V: View) {
        val inputAnswer = answerInput.text
        if (inputAnswer.toString() == answer) {
            correctAnswers += 1
        } else correctAnswers = 0
        assess()

    }

    private fun easyQuestion(): String {
        val value1 = Random.nextInt(10)
        var value2 = Random.nextInt(10)
        while (value2 > value1) {
            value2 = Random.nextInt(10)
        }
        val operation = operatorArray[Random.nextInt(3)]
        when (operation) {
            "+" -> {
                answer = (value1 + value2).toString()
            }
            "-" -> {
                answer = (value1 - value2).toString()
            }
            "*" -> {
                answer = (value1 * value2).toString()
            }
        }
        return "$value1 $operation $value2"
    }

    private fun mediumQuestion(): String {
        val value1 = Random.nextInt(100)
        var value2 = Random.nextInt(100)
        while (value2 > value1) {
            value2 = Random.nextInt(100)
        }
        val operation = operatorArray[Random.nextInt(2)]
        when (operation) {
            "+" -> {
                answer = (value1 + value2).toString()
            }
            "-" -> {
                answer = (value1 - value2).toString()
            }
        }
        return "$value1 $operation $value2"
    }

    private fun hardQuestion(): String {
        val value1 = Random.nextInt(1000)
        var value2 = Random.nextInt(1000)
        while (value2 > value1) {
            value2 = Random.nextInt(1000)
        }
            val operation = operatorArray[Random.nextInt(2)]
            when (operation) {
                "+" -> {
                    answer = (value1 + value2).toString()
                }
                "-" -> {
                    answer = (value1 - value2).toString()
                }
            }
            return "$value1 $operation $value2"
        }
    }



