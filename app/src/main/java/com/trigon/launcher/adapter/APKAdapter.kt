package com.trigon.launcher.adapter
import android.animation.ObjectAnimator
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.transition.Hold
import com.trigon.launcher.R
import com.trigon.launcher.vo.APKItem

class APKAdapter (
    private var apkList: List<APKItem>,
    private val onItemClick: (APKItem,Int) ->Unit
) : RecyclerView.Adapter<APKAdapter.APKViewHolder>(){
    // ViewHolder 内部类，持有 Item 视图引用
    class APKViewHolder(itemView: View):RecyclerView.ViewHolder(itemView) {
        val apkContainer: ViewGroup = itemView.findViewById(R.id.apk_container)
        val apkButton: FrameLayout = itemView.findViewById(R.id.apk_button)
        val apkIcon: ImageView = itemView.findViewById(R.id.apk_icon)
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

        // 获取要显示的 Drawable
        val iconDrawable = when {
            apkItem.iconDrawable != null -> apkItem.iconDrawable
            apkItem.iconRes != null -> holder.itemView.context.getDrawable(apkItem.iconRes)
            else -> holder.itemView.context.getDrawable(R.drawable.add_icon)
        }
        
        // 设置图标到 ImageView
        holder.apkIcon.setImageDrawable(iconDrawable)
        
        // 根据是否为真实 APK 决定是否提取颜色
        if (apkItem.packageName != null) {
            // 真实的 APK，从图标中提取颜色并设置背景
            iconDrawable?.let { drawable ->
                extractColorAndSetBackground(drawable, holder.apkButton)
            }
        } else {
            // "Add App" 按钮，使用默认背景
            holder.apkButton.setBackgroundResource(R.drawable.apk_item_background)
        }
        // 【关键修复】在 itemView 上监听焦点变化
        // RecyclerView 的焦点在 item 级别，不在内部子视图
        holder.itemView.setOnFocusChangeListener { view, hasFocus ->
           // Log.d("APKAdapter", "itemView Focus Changed: $hasFocus, position: $position")
            if (hasFocus) {
                // 获得焦点时，缩放整个容器（包含按钮和文字）
                scaleView(holder.apkContainer, 1.1f)
                // 添加阴影效果，提升视觉层次
                animateElevation(holder.apkContainer, 12f)
            } else {
                // 失去焦点时，恢复原始大小
                scaleView(holder.apkContainer, 1.0f)
                // 恢复阴影
                animateElevation(holder.apkContainer, 0f)
            }
        }
        
        // 设置点击事件（现在在 itemView 上）
        holder.itemView.setOnClickListener {
           // Log.d("APKAdapter", "Item clicked: $position")
            onItemClick(apkItem, position)
        }
        
        // 可选：根据是否已安装设置不同的样式
        //holder.apkButton.alpha = if (apkItem.isInstalled) 1.0f else 0.6f
    }
    
    /**
     * 从 Drawable 中提取主色调并设置为背景
     */
    private fun extractColorAndSetBackground(drawable: Drawable, container: FrameLayout) {
        try {
            // 将 Drawable 转换为 Bitmap
            val bitmap = drawableToBitmap(drawable)


            // 使用 Palette 提取颜色
            Palette.from(bitmap).generate { palette ->
                palette?.let {
                    // 尝试获取主色调，优先级：Vibrant > LightVibrant > Muted > DominantColor
                    val backgroundColor = it.vibrantSwatch?.rgb
                        ?: it.lightVibrantSwatch?.rgb
                        ?: it.mutedSwatch?.rgb
                        ?: it.dominantSwatch?.rgb
                        ?: Color.parseColor("#d1d2cd") // 默认颜色

                    // 创建带状态的背景
                    val stateListDrawable = createStateListDrawable(backgroundColor)
                    container.background = stateListDrawable
                }
            }
//            container.background = getDominantColorByFrequency(bitmap)
        } catch (e: Exception) {
            // 如果提取失败，使用默认背景
            e.printStackTrace()
            container.setBackgroundResource(R.drawable.apk_item_background)
        }
    }
    fun getDominantColorByFrequency(bitmap: Bitmap): Drawable {
        val colorCountMap = HashMap<Int, Int>()
        val width = bitmap.width/4
        val height = bitmap.height/4

        // 遍历每个像素
        for (x in 0 until width) {
            for (y in 0 until height) {
                val pixel = bitmap.getPixel(x, y)
                // 可以忽略完全透明的像素
                if (Color.alpha(pixel) < 50) continue
                colorCountMap[pixel] = colorCountMap.getOrDefault(pixel, 0) + 1
            }
        }

        // 找到出现次数最多的颜色
        var dominantColor = Color.GRAY // 默认颜色
        var maxCount = 0
        for ((color, count) in colorCountMap) {
            if (count > maxCount) {
                maxCount = count
                dominantColor = color
            }
        }
        return ColorDrawable(dominantColor)
    }
    /**
     * 将 Drawable 转换为 Bitmap
     */
    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        }
        
        val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 100
        val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 100
        
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        
        return bitmap
    }
    
    /**
     * 创建带状态的背景 Drawable（普通、按下、聚焦）
     */
    private fun createStateListDrawable(baseColor: Int): StateListDrawable {
        val stateListDrawable = StateListDrawable()
        
        // 计算深色（用于按下和聚焦状态）
        val darkerColor = darkenColor(baseColor, 0.15f)
        
        // 选中状态
        stateListDrawable.addState(
            intArrayOf(android.R.attr.state_selected),
            createRoundedDrawable(baseColor)
        )
        
        // 按下状态
        stateListDrawable.addState(
            intArrayOf(android.R.attr.state_pressed),
            createRoundedDrawable(darkerColor)
        )
        
        // 聚焦状态
        stateListDrawable.addState(
            intArrayOf(android.R.attr.state_focused),
            createRoundedDrawable(darkerColor)
        )
        
        // 默认状态
        stateListDrawable.addState(
            intArrayOf(),
            createRoundedDrawable(baseColor)
        )
        
        return stateListDrawable
    }
    
    /**
     * 创建圆角矩形 Drawable
     */
    private fun createRoundedDrawable(color: Int): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 30f  
            setColor(color)
        }
    }
    
    /**
     * 将颜色变暗
     */
    private fun darkenColor(color: Int, factor: Float): Int {
        val hsv = FloatArray(3)
        Color.colorToHSV(color, hsv)
        hsv[2] *= (1f - factor) // 降低亮度
        return Color.HSVToColor(hsv)
    }
    
    /**
     * 缩放视图（带动画效果）
     * @param view 要缩放的 View（可以是 Button、ImageView、TextView 等）
     * @param scale 缩放比例（1.0 为原始大小，1.15 为放大 15%）
     */
    private fun scaleView(view: View, scale: Float) {
        val duration = 200L // 动画持续时间（毫秒）
        
        // X 轴缩放动画
        val scaleXAnimator = ObjectAnimator.ofFloat(view, "scaleX", view.scaleX, scale)
        scaleXAnimator.duration = duration
        scaleXAnimator.interpolator = AccelerateDecelerateInterpolator()
        
        // Y 轴缩放动画
        val scaleYAnimator = ObjectAnimator.ofFloat(view, "scaleY", view.scaleY, scale)
        scaleYAnimator.duration = duration
        scaleYAnimator.interpolator = AccelerateDecelerateInterpolator()
        
        // 同时执行两个动画
        scaleXAnimator.start()
        scaleYAnimator.start()
    }
    
    /**
     * 动画改变视图的阴影效果（Z轴高度）
     * @param view 要改变阴影的 View
     * @param elevation 目标阴影高度（dp值）
     */
    private fun animateElevation(view: View, elevation: Float) {
        val duration = 200L
        val elevationAnimator = ObjectAnimator.ofFloat(view, "elevation", view.elevation, elevation)
        elevationAnimator.duration = duration
        elevationAnimator.interpolator = AccelerateDecelerateInterpolator()
        elevationAnimator.start()
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