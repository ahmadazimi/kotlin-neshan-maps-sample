package org.neshan.sample.kotlin.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import java.util.logging.Logger

abstract class BaseActivity : AppCompatActivity() {

    val log: Logger = Logger.getLogger(this@BaseActivity::class.java.simpleName)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
        loadUi()
        listener()
        load()
    }

    open fun init() {}
    open fun loadUi() {}
    open fun listener() {}
    open fun load() {}

}
