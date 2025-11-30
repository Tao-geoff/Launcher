package com.trigon.launcher.adapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.trigon.launcher.R
import com.trigon.launcher.vo.APKItem

class APKAdapter (
    private var apkList: List<APKItem>,
    private val onItemClick: (APKItem,Int) ->Unit
) : RecyclerView.Adapter<APKAdapter.APKViewHolder>(){
    // ViewHolder 内部类，持有 Item 视图引用
    class APKViewHolder(itemView: View):RecyclerView.ViewHolder(itemView) {
        val apkButton: Button = itemView.findViewById(R.id.apk_button)
        val apkName: TextView = itemView.findViewById(R.id.apk_name)
    }
    // 创建 ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): APKViewHolder {
       val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_apk_button,parent,false)
        return APKViewHolder(view)    
    }
    //绑定数据到ViewHolder
    override fun onBindViewHolder(holder: APKViewHolder, position: Int) {
        val apkItem = apkList[position]
        
        // 设置应用名称
        holder.apkName.text = apkItem.appName ?: "Unknown"
        
        // 设置应用图标（支持 Drawable 和资源 ID）
    when {
        apkItem.iconDrawable != null -> {
            holder.apkButton.background = apkItem.iconDrawable
        }
        apkItem.iconRes != null -> {
            holder.apkButton.setBackgroundResource(apkItem.iconRes)
        }
        else -> {
            // 默认图标
            holder.apkButton.setBackgroundResource(R.drawable.add_apk_selector)
        }
    }
        
        // 设置点击事件
        holder.apkButton.setOnClickListener {
            onItemClick(apkItem, position)
        }
        
        // 整个 Item 的点击事件
        holder.itemView.setOnClickListener {
            onItemClick(apkItem, position)
        }
        
        // 可选：根据是否已安装设置不同的样式
        //holder.apkButton.alpha = if (apkItem.isInstalled) 1.0f else 0.6f
    }
    // 返回列表项数量
    override fun getItemCount(): Int = apkList.size

    // 更新数据的方法
    fun updateData(newList: List<APKItem>) {
        apkList = newList
        notifyDataSetChanged()
    }
    // 添加单个项目
    fun addItem(item: APKItem) {
        val newList = apkList.toMutableList()
        newList.add(item)
        apkList = newList
        notifyItemInserted(apkList.size - 1)
    }

    // 删除单个项目
    fun removeItem(position: Int) {
        if (position in apkList.indices) {
            val newList = apkList.toMutableList()
            newList.removeAt(position)
            apkList = newList
            notifyItemRemoved(position)
        }
    }
     // 在指定位置插入项目
    fun insertItem(item: APKItem, position: Int) {
        val newList = apkList.toMutableList()
        newList.add(position, item)
        apkList = newList
        notifyItemInserted(position)
    }
}