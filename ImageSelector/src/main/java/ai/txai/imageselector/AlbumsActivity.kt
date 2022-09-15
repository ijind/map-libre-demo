package ai.txai.imageselector

import ai.txai.common.mvvm.BaseMvvmActivity
import ai.txai.common.mvvm.BaseViewModel
import ai.txai.common.utils.setDebounceClickListener
import ai.txai.imageselector.adapter.FolderAdapter
import ai.txai.imageselector.databinding.ImageSelectorAlbumsLayoutBinding
import ai.txai.imageselector.entry.Folder
import ai.txai.imageselector.model.ImageModel
import ai.txai.imageselector.utils.ImageSelector
import android.content.Intent
import androidx.recyclerview.widget.LinearLayoutManager
import java.util.ArrayList

class AlbumsActivity: BaseMvvmActivity<ImageSelectorAlbumsLayoutBinding, BaseViewModel>() {
    private var folders: ArrayList<Folder>? = null
    private var folder: Folder? = null

    override fun initViewObservable() {
        super.initViewObservable()
        loadImageForSDCard()
        initListener()
    }

    private fun initListener() {
        binding.albumsBackImg.setOnClickListener { finish() }
    }

    /**
     * 从SDCard加载图片。
     */
    private fun loadImageForSDCard() {
        ImageModel.loadImageForSDCard(this) { folders ->
            this.folders = folders
            runOnUiThread {
                if (!this.folders.isNullOrEmpty()) {
                    initFolderList()
                }
            }
        }
    }

    /**
     * 初始化图片文件夹列表
     */
    private fun initFolderList() {
        if (folders.isNullOrEmpty()) return

        binding.rvFolder.layoutManager = LinearLayoutManager(this)
        val adapter = FolderAdapter(this, folders!!)
        adapter.setOnFolderSelectListener(object: FolderAdapter.OnFolderSelectListener {
            override fun onFolderSelect(folder: Folder) {
                val intent = Intent()
                intent.putExtra(ImageSelector.SELECT_FOLDER_RESULT, folder.images)
                intent.putExtra(ImageSelector.SELECT_FOLDER_NAME, folder.name)
                setResult(RESULT_OK, intent)
                finish()
            }
        })
        binding.rvFolder.adapter = adapter
    }

    override fun initViewBinding(): ImageSelectorAlbumsLayoutBinding {
        return ImageSelectorAlbumsLayoutBinding.inflate(layoutInflater)
    }
}