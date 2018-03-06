package com.bigkoo.convenientbanner.holder

/**
 * Created by Sai on 15/12/14.
 * @param <T> 任何你指定的对象
</T> */

import android.content.Context
import android.view.View

interface Holder<in T> {
    fun createView(context: Context): View
    fun UpdateUI(context: Context, position: Int, data: T)
}