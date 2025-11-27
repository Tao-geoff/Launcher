package com.trigon.launcher.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.DialogFragment
import com.trigon.launcher.R

class AppDialogFragment :DialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 填充对话框的布局
        val rootView = inflater.inflate(R.layout.app_dialog, container, false)
        return rootView
        //return super.onCreateView(inflater, container, savedInstanceState)
    }
    //在 onViewCreated 中进行其他视图设置
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 可以在这里进行额外的视图初始化
        
    }
    override fun onStart(){
        super.onStart()
        dialog?.window?.apply{
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }
}