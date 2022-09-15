package ai.txai.common.animation

import android.animation.ObjectAnimator
import android.view.View
import android.view.animation.LinearInterpolator

/**
 * Time: 26/04/2022
 * Author Hay
 */
object AnimationUtils {
    fun newJumpAnim(view: View): ObjectAnimator {
        return newJumpAnim(view, -40f)
    }

    fun newJumpAnim(view: View, jumpY: Float): ObjectAnimator {
        val anim = ObjectAnimator.ofFloat(view, "translationY", 0f, jumpY, 0f)
        anim.interpolator = LinearInterpolator()
        anim.duration = 500
        return anim
    }
}