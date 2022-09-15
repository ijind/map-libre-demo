package ai.txai.common.countrycode

import android.view.View
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.animator.PopupAnimator
import com.lxj.xpopup.enums.PopupAnimation

class TranslateAnimator(targetView: View, animation: PopupAnimation) :
    PopupAnimator(targetView, 300, animation) {

    companion object {
        private const val TAG = "TranslateAnimator"
    }
    private var startTranslationX = 0f
    private  var startTranslationY = 0f
    private var defTranslationX = 0f
    private  var defTranslationY = 0f
    override fun initAnimator() {
        defTranslationX = targetView.translationX
        defTranslationY = targetView.translationY

        targetView.alpha = 0f
        applyTranslation()
        startTranslationX = targetView.translationX
        startTranslationY = targetView.translationY
    }

    override fun animateShow() {
        targetView.animate().translationX(defTranslationX).translationY(defTranslationY)
            .alpha(1f)
            .setInterpolator(FastOutSlowInInterpolator())
            .setDuration(XPopup.getAnimationDuration().toLong())
            .withLayer()
            .start()
    }

    override fun animateDismiss() {
        targetView.animate().translationX(startTranslationX).translationY(startTranslationY)
            .alpha(0f)
            .setInterpolator(FastOutSlowInInterpolator())
            .setDuration(XPopup.getAnimationDuration().toLong())
            .withLayer()
            .start()
    }

    private fun applyTranslation() {
        when (popupAnimation) {
            PopupAnimation.TranslateAlphaFromLeft -> targetView.translationX =
                -targetView.measuredWidth.toFloat()
            PopupAnimation.TranslateAlphaFromTop -> {
                defTranslationY = 0f
                defTranslationX = 0f
                targetView.translationY =
                    - targetView.measuredHeight.toFloat()
            }
            PopupAnimation.TranslateAlphaFromRight -> targetView.translationX =
                targetView.measuredWidth.toFloat()
            PopupAnimation.TranslateAlphaFromBottom -> {
                defTranslationY = 0f
                targetView.translationY =
                    targetView.measuredHeight.toFloat()
            }
        }
    }
}