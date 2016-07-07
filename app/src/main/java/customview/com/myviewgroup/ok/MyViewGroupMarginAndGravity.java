package customview.com.myviewgroup.ok;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;

import customview.com.myviewgroup.BuildConfig;
import customview.com.myviewgroup.R;

/**
 * Created by a on 2016/6/27.
 * 考虑margin的写法（麻烦一点）
 */
public class MyViewGroupMarginAndGravity extends ViewGroup {

    private int desireWidth;
    private int desireHeight;

    public MyViewGroupMarginAndGravity(Context context) {
        this(context, null);
    }

    public MyViewGroupMarginAndGravity(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        // ★1. 计算所有child view 要占用的空间
        desireWidth = 0;//累加所有子View的宽度，作为MyViewGroup宽度设为wrap_content时的宽度
        desireHeight = 0;//本例子是横向排列的ViewGroup，所以，MyViewGroup高度设为wrap_content时，其高度是其所有子View中最高子View的高度
        int count = getChildCount();
        for (int i = 0; i < count; ++i) {
            View v = getChildAt(i);
            //1.1 开始遍历测量所有的子View，并且根据实际情况累加子View的宽（或者高），为了计算整个viewgroup的宽度（高度）
            if (v.getVisibility() != View.GONE) {//不去测量Gone的子View

                LayoutParams lp = (LayoutParams) v.getLayoutParams();

                //▲变化1：将measureChild改为measureChildWithMargin
                measureChildWithMargins(v, widthMeasureSpec, 0,
                        heightMeasureSpec, 0);
              /*原来：  measureChild(v, widthMeasureSpec,
                        heightMeasureSpec);*/

                //▲变化2：这里在累加所有的子View的宽度时加上他自己的margin
                desireWidth += v.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
                desireHeight = Math
                        .max(desireHeight, v.getMeasuredHeight() + lp.topMargin + lp.bottomMargin);

                /*原来：//由于横向排列，累加所有的子View的宽度
                desireWidth += v.getMeasuredWidth();
                //高度是子View中最高的高度
                desireHeight = Math
                        .max(desireHeight, v.getMeasuredHeight());*/
            }
        }

        // 1.2 考虑padding值
        //到目前为止desireWidth为所有子View的宽度的累加，作为MyViewGroup的总宽度，要加上左右padding值
        desireWidth += getPaddingLeft() + getPaddingRight();
        //高度同理略
        desireHeight += getPaddingTop() + getPaddingBottom();


        //★2.测量ViewGroup的宽高，如果不写这一步，使用wrap_content时效果为match_parent的效果
        // （下面的写法比较简洁，《Android群英传》介绍了另外一种写法，比这个稍微麻烦一点）
        // see if the size is big enough
        desireWidth = Math.max(desireWidth, getSuggestedMinimumWidth());
        desireHeight = Math.max(desireHeight, getSuggestedMinimumHeight());

        setMeasuredDimension(resolveSize(desireWidth, widthMeasureSpec),
                resolveSize(desireHeight, heightMeasureSpec));
    }



    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int parentLeft = getPaddingLeft();
        final int parentRight = r - l - getPaddingRight();
        final int parentTop = getPaddingTop();
        final int parentBottom = b - t - getPaddingBottom();

        if (BuildConfig.DEBUG)
            Log.d("onlayout", "parentleft: " + parentLeft + "   parenttop: "
                    + parentTop + "   parentright: " + parentRight
                    + "   parentbottom: " + parentBottom);

        int left = parentLeft;
        int top = parentTop;

        int count = getChildCount();
        for (int i = 0; i < count; ++i) {
            View v = getChildAt(i);
            if (v.getVisibility() != View.GONE) {
                LayoutParams lp = (LayoutParams) v.getLayoutParams();
                final int childWidth = v.getMeasuredWidth();
                final int childHeight = v.getMeasuredHeight();
                //第三次变化：增加gravity
                final int gravity = lp.gravity;
                final int horizontalGravity = gravity
                        & Gravity.HORIZONTAL_GRAVITY_MASK;
                final int verticalGravity = gravity
                        & Gravity.VERTICAL_GRAVITY_MASK;



                //▲变化1：左侧要加上这个子View的左侧margin
                left += lp.leftMargin;
                //▲变化2：上侧要加上子View的margin
                top = parentTop + lp.topMargin;

                //第三次变化：增加gravity
                if (gravity != -1) {
                    switch (verticalGravity) {
                        case Gravity.TOP:
                            break;
                        case Gravity.CENTER_VERTICAL:
                            top = parentTop
                                    + (parentBottom - parentTop - childHeight)
                                    / 2 + lp.topMargin - lp.bottomMargin;
                            break;
                        case Gravity.BOTTOM:
                            top = parentBottom - childHeight - lp.bottomMargin;
                            break;
                    }
                }


                if (BuildConfig.DEBUG) {
                    Log.d("onlayout", "child[width: " + childWidth
                            + ", height: " + childHeight + "]");
                    Log.d("onlayout", "child[left: " + left + ", top: "
                            + top + ", right: " + (left + childWidth)
                            + ", bottom: " + (top + childHeight));
                }
                v.layout(left, top, left + childWidth, top + childHeight);
                //▲变化3：因为是横向排列的，所以下一个View的左侧加上这个view的右侧的margin（如果是纵向排列的则对应改变top）
                left += childWidth + lp.rightMargin;

            }
        }
    }

//★★★★★★★★★★★★★★★★★★★★★★★要使用margin必须写下面的方法★★★★★★★★★★★★★★★★★★★★★
//***开始***每一个自定义ViewGroup都必须，自定义这个类LayoutParams，以及后面的三个方法，否则强转报异常，模板代码照抄即可**************

    public static class LayoutParams extends MarginLayoutParams {

        //★比上一版增加了gravity
        public int gravity = -1;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
            //获取自定义的属性SlideGroup
            TypedArray ta = c.obtainStyledAttributes(attrs,
                    R.styleable.SlideGroup);
            //得到用户在xml布局文件中layout_gravity对应的值
            gravity = ta.getInt(R.styleable.SlideGroup_layout_gravity, -1);
            //释放资源
            ta.recycle();
        }

        public LayoutParams(int width, int height) {
            this(width, height, -1);
        }

        public LayoutParams(int width, int height, int gravity) {
            super(width, height);
            this.gravity = gravity;
        }

        public LayoutParams(android.view.ViewGroup.LayoutParams source) {
            super(source);
        }

        public LayoutParams(MarginLayoutParams source) {
            super(source);
        }
    }

    @Override
    protected android.view.ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
    }

    @Override
    public android.view.ViewGroup.LayoutParams generateLayoutParams(
            AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override
    protected android.view.ViewGroup.LayoutParams generateLayoutParams(
            android.view.ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }
    //***结束***每一个自定义ViewGroup都必须，自定义这个类LayoutParams，以及后面的三个方法，否则强转报异常，模板代码照抄即可**************
}
