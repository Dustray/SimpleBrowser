package com.dustray.tools;

import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;

/**
 * Created by weixu on 2017/4/1.
 */

public class AnimationHelper {
    //动画类
    public Animation getRotateAnimation(int fromPosition, int toPosition) {
        //动画旋转（从多少角度开始，到多少角度，后边参数为旋转中心位置）
        RotateAnimation animation = new RotateAnimation(fromPosition, toPosition,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setInterpolator(new DecelerateInterpolator());
        //animation.setFillAfter(true);  //动画旋转结束保持现在状态
        animation.setDuration(400); //旋转时间
        return animation;
    }

    public Animation getTranslateAnimation(int fromLeft, int toLeft, int fromTop, int toTop) {
        //动画位移
        TranslateAnimation animation = new TranslateAnimation(fromLeft, toLeft, fromTop, toTop);
        animation.setInterpolator(new DecelerateInterpolator());
        //animation.setFillAfter(true);  //动画位移结束保持现在状态
        animation.setDuration(300); //位移时间
        return animation;
    }

    public Animation getScaleAnimation(float fromX, float toX, float fromY, float toY,
                                       int pivotXType, float pivotXValue, int pivotYType, float pivotYValue) {
        ScaleAnimation animation = new ScaleAnimation(fromX, toX, fromY, toY,
                pivotXType, pivotXValue, pivotYType, pivotYValue);
    //第一个参数fromX为动画起始时 X坐标上的伸缩尺寸
    //第二个参数toX为动画结束时 X坐标上的伸缩尺寸
    //第三个参数fromY为动画起始时Y坐标上的伸缩尺寸
    //第四个参数toY为动画结束时Y坐标上的伸缩尺寸
        /*说明:
                    以上四种属性值
                    0.0表示收缩到没有
                    1.0表示正常无伸缩
                    值小于1.0表示收缩
                    值大于1.0表示放大
        */
    //第五个参数pivotXType为动画在X轴相对于物件位置类型
    //第六个参数pivotXValue为动画相对于物件的X坐标的开始位置
    //第七个参数pivotXType为动画在Y轴相对于物件位置类型
    //第八个参数pivotYValue为动画相对于物件的Y坐标的开始位置
        animation.setInterpolator(new DecelerateInterpolator());
        //animation.setFillAfter(true);  //动画位移结束保持现在状态
        animation.setDuration(300); //位移时间
        return animation;
    }
}
