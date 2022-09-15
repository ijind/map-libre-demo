package ai.txai.imageselector.adapter

import ai.txai.common.utils.ToastUtils
import android.view.LayoutInflater
import ai.txai.imageselector.utils.VersionUtils
import android.view.ViewGroup
import ai.txai.imageselector.R
import ai.txai.imageselector.entry.Image
import ai.txai.imageselector.view.SquareImageView
import android.content.Context
import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.load.engine.DiskCacheStrategy
import java.util.ArrayList

class ImageAdapter(
    private val context: Context,
    private val maxCount: Int,
    private val isSingle: Boolean,
    private val isViewImage: Boolean
) : RecyclerView.Adapter<ImageAdapter.ViewHolder>() {

    var data: ArrayList<Image>? = null
        private set
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    //保存选中的图片
    val selectImages = ArrayList<Image>()
    private var selectListener: OnImageSelectListener? = null
    private var itemClickListener: OnItemClickListener? = null
    private var useCamera = false
    private val isAndroidQ = VersionUtils.isAndroidQ()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return if (viewType == TYPE_IMAGE) {
            val view = inflater.inflate(R.layout.image_selector_images_item_layout, parent, false)
            ViewHolder(viewType, view)
        } else {
            val view = inflater.inflate(R.layout.image_selector_camera_layout, parent, false)
            ViewHolder(viewType, view)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (getItemViewType(position) == TYPE_IMAGE) {
            val image = getImage(position)
            Glide.with(context).load(if (isAndroidQ) image.uri else image.path)
                .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE))
                .into(holder.ivImage)
            setItemSelect(holder, isSelectContainImage(image))
            setSelectedDisable(holder, image)

            holder.ivGif.visibility = if (image.isGif) View.VISIBLE else View.GONE

            //点击选中/取消选中图片
            holder.ivSelectIcon.setOnClickListener { checkedImage(holder, image) }

            holder.itemView.setOnClickListener {
                if (isViewImage) {
                    if (itemClickListener != null) {
                        val p = holder.bindingAdapterPosition
                        itemClickListener!!.onItemClick(image, if (useCamera) p - 1 else p)
                    }
                } else {
                    checkedImage(holder, image)
                }
            }
        } else if (getItemViewType(position) == TYPE_CAMERA) {
            holder.itemView.setOnClickListener {
                if (itemClickListener != null) {
                    itemClickListener!!.onCameraClick()
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (useCamera && position == 0) {
            TYPE_CAMERA
        } else {
            TYPE_IMAGE
        }
    }

    private fun checkedImage(holder: ViewHolder, image: Image) {
        if (isSelectContainImage(image)) {
            //如果图片已经选中，就取消选中
            unSelectImage(image)
            setItemSelect(holder, false)
            return
        }

        if (isSingle) {
            //如果是单选，就先清空已经选中的图片，再选中当前图片
            clearImageSelect()
            selectImage(image)
            setItemSelect(holder, true)
            return
        }

        if (selectImages.size >= maxCount) {
            val prompt = "Up to $maxCount pictures can be selected at a time"
            ToastUtils.show(prompt)
        }
        if (maxCount <= 0 || selectImages.size < maxCount) {
            //如果不限制图片的选中数量
            // 还没有达到最大限制，就直接选中当前图片。
            selectImage(image)
            setItemSelect(holder, true)
        }
    }

    /**
     * 选中图片
     *
     * @param image
     */
    private fun selectImage(image: Image) {
        image.isFromPreview = "true"
        selectImages.add(image)
        if (selectListener != null) {
            selectListener!!.onImageSelect(image, true, selectImages.size)
        }
    }

    /**
     * 取消选中图片
     *
     * @param image
     */
    private fun unSelectImage(image: Image) {
        selectImages.remove(image)
        if (selectListener != null) {
            selectListener!!.onImageSelect(image, false, selectImages.size)
        }
    }

    override fun getItemCount(): Int {
        return if (useCamera) imageCount + 1 else imageCount
    }

    private val imageCount: Int
        get() = if (data == null) 0 else data!!.size

    fun refresh(data: ArrayList<Image>?, useCamera: Boolean) {
        this.data = data
        this.useCamera = useCamera
        notifyDataSetChanged()
    }

    private fun getImage(position: Int): Image {
        return data!![if (useCamera) position - 1 else position]
    }

    fun getFirstVisibleImage(firstVisibleItem: Int): Image? {
        return if (data != null && data!!.isNotEmpty()) {
            if (useCamera) {
                data!![if (firstVisibleItem > 0) firstVisibleItem - 1 else 0]
            } else {
                data!![if (firstVisibleItem < 0) 0 else firstVisibleItem]
            }
        } else null
    }

    /**
     * 设置图片选中和未选中的效果
     */
    private fun setItemSelect(holder: ViewHolder, isSelect: Boolean) {
        if (isSelect) {
            holder.ivSelectIcon.setImageResource(R.drawable.image_selector_selected_ic)
            holder.ivMasking.alpha = 0.6f
            holder.ivMasking.visibility = View.VISIBLE
        } else {
            holder.ivSelectIcon.setImageResource(R.drawable.image_selector_unselect_ic)
            holder.ivMasking.visibility = View.GONE
        }
    }

    private fun setSelectedDisable(holder: ViewHolder, image: Image) {
        val isSelect = isSelectContainImage(image)
        if (isSelect) {
            if (!image.isFromPreview.toBoolean()) {
                holder.ivSelectIcon.isEnabled = false
                holder.ivMasking.alpha = 0.8f
                holder.ivMasking.visibility = View.VISIBLE
            }
        } else {
            holder.ivSelectIcon.isEnabled = true
            holder.ivMasking.visibility = View.GONE
        }
    }

    private fun isSelectContainImage(image: Image): Boolean {
        if (selectImages.isNullOrEmpty()) return false

        var isContain = false
        selectImages.forEach {
            if (it.path == image.path) isContain = true
        }

        return isContain
    }

    private fun clearImageSelect() {
        if (data != null && selectImages.size == 1) {
            val index = data!!.indexOf(selectImages[0])
            selectImages.clear()
            if (index != -1) {
                notifyItemChanged(if (useCamera) index + 1 else index)
            }
        }
    }

    fun setSelectedImages(selected: ArrayList<String>?) {
        if (data != null && selected != null) {
            for (path in selected) {
                if (isFull) {
                    return
                }
                for (image in data!!) {
                    if (path == image.path) {
                        if (!isSelectContainImage(image)) {
                            selectImages.add(image)
                        }
                        break
                    }
                }
            }
            notifyDataSetChanged()
        }
    }

    private val isFull: Boolean
        get() = if (isSingle && selectImages.size == 1) {
            true
        } else maxCount > 0 && selectImages.size == maxCount

    fun setOnImageSelectListener(listener: OnImageSelectListener?) {
        selectListener = listener
    }

    fun setOnItemClickListener(listener: OnItemClickListener?) {
        itemClickListener = listener
    }

    class ViewHolder(viewType: Int, itemView: View) : RecyclerView.ViewHolder(itemView) {
        var ivImage: SquareImageView = itemView.findViewById(R.id.iv_image)
        var ivSelectIcon: ImageView = itemView.findViewById(R.id.iv_select)
        var ivMasking: ImageView = itemView.findViewById(R.id.iv_masking)
        var ivGif: ImageView = itemView.findViewById(R.id.iv_gif)
        var ivCamera: SquareImageView? = if (viewType == TYPE_CAMERA) {
            itemView.findViewById(R.id.iv_camera)
        } else { null }
    }

    interface OnImageSelectListener {
        fun onImageSelect(image: Image?, isSelect: Boolean, selectCount: Int)
    }

    interface OnItemClickListener {
        fun onItemClick(image: Image?, position: Int)
        fun onCameraClick()
    }

    companion object {
        private const val TYPE_CAMERA = 1
        private const val TYPE_IMAGE = 2
    }

}