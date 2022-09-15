package ai.txai.imageselector.adapter

import ai.txai.imageselector.entry.Folder
import android.view.LayoutInflater
import ai.txai.imageselector.utils.VersionUtils
import android.view.ViewGroup
import ai.txai.imageselector.R
import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.load.engine.DiskCacheStrategy
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.ArrayList

class FolderAdapter(private val context: Context, private val folders: ArrayList<Folder>) :
    RecyclerView.Adapter<FolderAdapter.ViewHolder>() {
    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var selectItem = 0
    private var listener: OnFolderSelectListener? = null
    private val isAndroidQ = VersionUtils.isAndroidQ()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = inflater.inflate(R.layout.image_selector_folder_layout, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val folder = folders[position]
        val images = folder.images
        holder.tvFolderName.text = folder.name
        if (!images.isNullOrEmpty()) {
            val size = context.getString(R.string.image_selector_image_num, images.size)
            holder.tvFolderSize.text = "($size)"

            Glide.with(context).load(if (isAndroidQ) images[0].uri else images[0].path)
                .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE))
                .into(holder.ivImage)
        } else {
            val size = context.getString(R.string.image_selector_image_num, 0)
            holder.tvFolderSize.text = "($size)"
            holder.ivImage.setImageBitmap(null)
        }
        holder.itemView.setOnClickListener {
            selectItem = holder.bindingAdapterPosition
            notifyDataSetChanged()
            if (listener != null) {
                listener!!.onFolderSelect(folder)
            }
        }
    }

    override fun getItemCount(): Int {
        return folders.size
    }

    fun setOnFolderSelectListener(listener: OnFolderSelectListener?) {
        this.listener = listener
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var ivImage: ImageView = itemView.findViewById(R.id.iv_image)
        var tvFolderName: TextView = itemView.findViewById(R.id.tv_folder_name)
        var tvFolderSize: TextView = itemView.findViewById(R.id.tv_folder_size)

    }

    interface OnFolderSelectListener {
        fun onFolderSelect(folder: Folder)
    }

}