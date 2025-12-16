package com.trigon.launcher.dialog

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.trigon.launcher.R
import com.trigon.launcher.adapter.APKAdapter
import com.trigon.launcher.vo.APKItem

class AppDialogFragment :DialogFragment() {
    // 控件引用
    private lateinit var recommendedAppsTab: TextView
    private lateinit var installedAppsTab: TextView
    private lateinit var folderTab: TextView
    private lateinit var systemAppsTab: TextView
    private lateinit var recyclerView: RecyclerView

    // Adapter 和数据
    private lateinit var apkAdapter: APKAdapter
    private val allApps = mutableListOf<APKItem>()
    private val recommendedApps = mutableListOf<APKItem>()
    private val installedApps = mutableListOf<APKItem>()
    private val systemApps = mutableListOf<APKItem>()

    private var onApkSelected: ((APKItem) -> Unit)? = null

    // 当前选中的分类
    private var currentCategory = Category.RECOMMENDED
    enum class Category {
        RECOMMENDED, INSTALLED, FOLDER, SYSTEM
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 填充对话框的布局
         return inflater.inflate(R.layout.app_dialog, container, false)
    }
    //在 onViewCreated 中进行其他视图设置
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 初始化视图
        initViews(view)
        
        // 初始化 RecyclerView
        initRecyclerView()
        
        // 加载应用数据
        loadInstalledApps()
        
        // 默认显示推荐应用
        showCategory(Category.RECOMMENDED)
    }
     private fun initViews(view: View) {
        recommendedAppsTab = view.findViewById(R.id.recommended_apps)
        installedAppsTab = view.findViewById(R.id.installed_apps)
        folderTab = view.findViewById(R.id.folder)
        systemAppsTab = view.findViewById(R.id.system_apps)
        recyclerView = view.findViewById(R.id.apps_dialog_frame)
        
        // 设置点击监听
        recommendedAppsTab.setOnFocusChangeListener { v ,hasFocus ->
            if(hasFocus)
            {
                showCategory(Category.RECOMMENDED)
                v.isSelected  =true
            }else
            {
                v.isSelected  =false
            }

        }
        
        installedAppsTab.setOnFocusChangeListener {v, hasFocus ->
            if(hasFocus)
            {
                showCategory(Category.INSTALLED)
                v.isSelected  =true
            }else
             {
                 v.isSelected  =false
             }
        }
        
        folderTab.setOnFocusChangeListener {v,hasFocus ->
            if(hasFocus)
            {
                showCategory(Category.FOLDER)
                v.isSelected  =true
            }else
             {
                 v.isSelected  =false
             }
        }
        
        systemAppsTab.setOnFocusChangeListener {v,hasFocus ->
            if(hasFocus)
            {
                showCategory(Category.SYSTEM)
                v.isSelected =true
            }else
             {
                 v.isSelected =false
             }
        }
    }
    companion object {
        // 工厂方法，传入回调
        fun newInstance(onApkSelected: (APKItem) -> Unit): AppDialogFragment {
            return AppDialogFragment().apply {
                this.onApkSelected = onApkSelected
            }
        }
    }
    private fun initRecyclerView() {
        // 使用 GridLayoutManager，每行显示 3 个（根据您的布局宽度调整）
        val spanCount = 3
        recyclerView.layoutManager = GridLayoutManager(requireContext(), spanCount)
        
        // 初始化 Adapter
        apkAdapter = APKAdapter(emptyList()) { apkItem, position ->
            onDialogItemClick(apkItem, position)
        }
        recyclerView.adapter = apkAdapter
        
        // TV 优化
        //recyclerView.isFocusable = true
       // recyclerView.isFocusableInTouchMode = false
    }

     private fun onDialogItemClick(apkItem: APKItem, position: Int) {
        // 将选中的 APK 回调给 HomePageActivity
        onApkSelected?.invoke(apkItem)
        
        // 关闭对话框
        dismiss()
        
        Toast.makeText(
            requireContext(),
            "已添加 ${apkItem.appName} 到首页",
            Toast.LENGTH_SHORT
        ).show()
    }

     /**
     * 获取机顶盒上所有已安装的应用
     */
    private fun loadInstalledApps() {
        val packageManager =  requireContext().packageManager
         val mainIntent = Intent(Intent.ACTION_MAIN,null).apply {
             addCategory(Intent.CATEGORY_LAUNCHER)
         }
        // 获取所有已安装的应用
        //val packages = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
         val resolveInfos = packageManager.queryIntentActivities(mainIntent, PackageManager.MATCH_ALL)
        
        allApps.clear()
        installedApps.clear()
        systemApps.clear()
        
        for (resolveInfo in resolveInfos) {
            val appInfo = resolveInfo.activityInfo.applicationInfo
            val packageName = appInfo.packageName
            // 跳过自己
            if (appInfo.packageName == requireContext().packageName) {
                continue
            }

            val apkItem = APKItem(
                packageName = packageName,
                appName = resolveInfo.loadLabel(packageManager).toString(),
                iconDrawable = resolveInfo.loadIcon(packageManager), // 使用resolveInfo的loadIcon
                isInstalled = true,
                versionName = try {
                    packageManager.getPackageInfo(packageName, 0).versionName
                } catch (e: Exception) {
                    "Unknown"
                },
                versionCode = try {
                    packageManager.getPackageInfo(packageName, 0).longVersionCode
                } catch (e: Exception) {
                    0L
                }
            )
            
            allApps.add(apkItem)
            
            // 区分系统应用和用户应用
            if (appInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0) {
                // 系统应用
                systemApps.add(apkItem)
            } else {
                // 用户安装的应用
                installedApps.add(apkItem)
            }
        }
        
        // 设置推荐应用（这里可以根据您的业务逻辑筛选）
        recommendedApps.clear()
        recommendedApps.addAll(installedApps.take(10)) // 示例：取前10个用户应用作为推荐
    }
    /**
     * 切换显示不同分类
     */
    private fun showCategory(category: Category) {
        currentCategory = category
        
        // 更新标签样式（可选）
        updateTabStyle()
        
        // 根据分类更新数据
        val dataToShow = when (category) {
            Category.RECOMMENDED -> recommendedApps
            Category.INSTALLED -> installedApps
            Category.FOLDER -> emptyList() // 文件夹功能待实现
            Category.SYSTEM -> systemApps
        }
        
        apkAdapter.updateData(dataToShow)
    }
     /**
     * 更新标签选中状态（可选：添加视觉反馈）
     */
    private fun updateTabStyle() {
        // 重置所有标签
        resetTabStyle(recommendedAppsTab)
        resetTabStyle(installedAppsTab)
        resetTabStyle(folderTab)
        resetTabStyle(systemAppsTab)
        
        // 高亮选中的标签
        val selectedTab = when (currentCategory) {
            Category.RECOMMENDED -> recommendedAppsTab
            Category.INSTALLED -> installedAppsTab
            Category.FOLDER -> folderTab
            Category.SYSTEM -> systemAppsTab
        }
        highlightTab(selectedTab)
    }
    
    private fun resetTabStyle(tab: TextView) {
        tab.alpha = 0.7f
        // 或者设置不同的背景
    }
    
    private fun highlightTab(tab: TextView) {
        tab.alpha = 1.0f
        // 或者设置选中背景
    }
     
    /**
     * 应用项点击事件
     */
    private fun onAppItemClick(apkItem: APKItem, position: Int) {
        apkItem.packageName?.let { packageName ->
            try {
                // 启动应用
                val intent = requireContext().packageManager
                    .getLaunchIntentForPackage(packageName)
                
                if (intent != null) {
                    startActivity(intent)
                    dismiss() // 关闭对话框
                } else {
                    Toast.makeText(
                        requireContext(),
                        "无法启动该应用",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "启动失败: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    override fun onStart(){
        super.onStart()
        dialog?.window?.apply{
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }
}