package ai.txai.feedback.adapter

import ai.txai.common.glide.GlideUtils
import ai.txai.common.glide.GlideUtils.loadImage
import ai.txai.common.log.LOG
import ai.txai.common.utils.AndroidUtils
import ai.txai.common.utils.setDebounceClickListener
import ai.txai.feedback.databinding.FeedbackRemoveImageLayoutBinding
import ai.txai.feedback.databinding.FeedbackUploadPhotoLayoutBinding
import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class UploadImageAdapter(private val context: Context): RecyclerView.Adapter<UploadImageAdapter.UploadImageVH>() {
    private val imageList = ArrayList<Uri>()
    private val layoutInflater by lazy { LayoutInflater.from(context) }

    private var removeListener: OnItemRemoveListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UploadImageVH {
        val binding = FeedbackRemoveImageLayoutBinding.inflate(layoutInflater, parent, false)
        return UploadImageVH(binding)
    }

    override fun onBindViewHolder(holder: UploadImageVH, position: Int) {
        holder.binding.imageView.setRadius(AndroidUtils.dip2px(context, 14f))
        holder.binding.imageView.setImageURI(imageList[position])
        holder.binding.removeImg.setDebounceClickListener {
            imageList.removeAt(position)
            removeListener?.onItemRemove(position)
            notifyDataSetChanged()
        }
    }

    override fun getItemCount() = imageList.size

    fun setData(source: ArrayList<Uri>) {
        imageList.addAll(source)
        notifyDataSetChanged()
    }

    fun setItemRemoveListener(listener: OnItemRemoveListener) {
        removeListener = listener
    }

    interface OnItemRemoveListener {
        fun onItemRemove(position: Int)
    }

    inner class UploadImageVH(val binding: FeedbackRemoveImageLayoutBinding):
        RecyclerView.ViewHolder(binding.root)
}