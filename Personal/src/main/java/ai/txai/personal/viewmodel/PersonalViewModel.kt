package ai.txai.personal.viewmodel

import ai.txai.common.api.CommonApiRepository
import ai.txai.common.log.LOG
import ai.txai.common.mvvm.BaseViewModel
import ai.txai.common.observer.CommonObserver
import ai.txai.common.thread.TScheduler
import ai.txai.personal.api.PersonalApiRepository
import ai.txai.common.data.ImageInfo
import android.app.Application
import androidx.lifecycle.MutableLiveData
import io.reactivex.android.schedulers.AndroidSchedulers
import java.io.File

class PersonalViewModel(application: Application): BaseViewModel(application) {
    companion object {
        private const val TAG = "PersonalViewModel"
    }

    private var uploadFileInfo = MutableLiveData<ImageInfo>()
    fun getUploadFileInfo() = uploadFileInfo

    private var updateUserInfoResult = MutableLiveData<Boolean>()
    fun getUpdateUserInfoResult() = updateUserInfoResult

    fun uploadUserImg(type: String, file: File) {
        val fileList = ArrayList<File>()
        fileList.add(file)
        CommonApiRepository.uploadImageFile("user", type, fileList)
            ?.subscribeOn(TScheduler.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe(object: CommonObserver<ArrayList<ImageInfo>>() {
                override fun onSuccess(t: ArrayList<ImageInfo>?) {
                    super.onSuccess(t)
                    LOG.d(TAG, "on upload success $t")
                    t?.let {
                        uploadFileInfo.value = t[0]
                    }
                }

                override fun onFailed(msg: String?) {
                    super.onFailed(msg)
                }
            })
    }

    fun updateUserInfo(avatar: String, name: String, email: String) {
        PersonalApiRepository.updatePersonalInfo(avatar, name, email)
            .subscribeOn(TScheduler.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object: CommonObserver<Boolean>() {
                override fun onSuccess(t: Boolean?) {
                    super.onSuccess(t)
                    t?.let { updateUserInfoResult.value = t }
                }

                override fun onFailed(msg: String?) {
                    super.onFailed(msg)
                    updateUserInfoResult.value = null
                    showToast(msg, false)
                }
            })
    }
}