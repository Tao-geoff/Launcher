package com.trigon.launcher


import android.os.Bundle
import android.view.KeyEvent
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.trigon.launcher.adapter.APKAdapter
import com.trigon.launcher.dialog.AppDialogFragment
import com.trigon.launcher.vo.APKItem

class HomePageActivity : AppCompatActivity() {
    private  lateinit var recyclerView: RecyclerView
    private lateinit var apkAdapter: APKAdapter
    private  val apkList = mutableListOf<APKItem>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.home_page)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        initRecyclerView()
        loadApkData()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return super.onKeyDown(keyCode, event)
    }
    private fun initRecyclerView(){
        recyclerView = findViewById(R.id.add_apk_frame)

        //使用横向 LinearLayoutManager
        recyclerView.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.HORIZONTAL,
            false
        )
         // 初始化适配器
        apkAdapter = APKAdapter(apkList){ apkItem, position ->
            onApkItemClick(apkItem,position)
        }
        recyclerView.adapter = apkAdapter
        // 优化机顶盒遥控器焦点导航
        recyclerView.isFocusable = true
        recyclerView.isFocusableInTouchMode = false
    }
    private fun loadApkData(){
        // 示例数据 - 后期可以替换为实际的 APK 扫描
        apkList.clear()

        // 添加一个"添加应用"按钮
        apkList.add(
            APKItem(
            packageName = null,
            appName = "Add App",
            iconRes = R.drawable.add_apk_selector,
            isInstalled = false
        )
        )

//        // 示例：已安装的应用
//        apkList.add(APKItem(
//            packageName = "com.example.app1",
//            appName = "App 1",
//            iconRes = R.drawable.add_apk_selector,
//            isInstalled = true
//        ))
         apkAdapter.updateData(apkList)
    }
    private fun onApkItemClick(apkItem: APKItem, position: Int) {
        if (apkItem.packageName == null) {
            // 打开选择 APK 的对话框
            val dialogFragment = AppDialogFragment.newInstance { selectedApk ->
                // 回调：将选中的 APK 添加到首页
                addApkToHomePage(selectedApk)
            }
            dialogFragment.show(supportFragmentManager, "AppDialogFragment")
        } else {
            // 启动已添加的应用
            //launchApp(apkItem.packageName)
        }
    }
    private fun addApkToHomePage(apkItem: APKItem) {
        // 在"Add App"按钮之前插入
        val insertPosition = apkList.size - 1
        
        // 同时更新列表和 Adapter
        apkList.add(insertPosition, apkItem)
        apkAdapter.insertItem(apkItem, insertPosition)
        
        Toast.makeText(this, "已添加 ${apkItem.appName}", Toast.LENGTH_SHORT).show()
    }

}