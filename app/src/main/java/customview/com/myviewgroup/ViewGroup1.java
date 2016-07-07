package customview.com.myviewgroup;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

/**
 * 练习写一个自定义ViewGroup，需要注意：
 * 1. onMeasure的写法
 * * 在这里，我们需要做两件事情
 * a . 遍历所有的子View，通过measureChild来测量所有的子View（注意Gone的子View不测量）（第一步是自定义ViewGroup必须要做的）
 * b . 要手动测量这个自定义ViewGroup的宽高（这一步不处理也行，但是不能使用wrap_content来设置宽高）
 * 1. 如果你不通过 setMeasuredDimension()这个方法来处理的话，
 * 你的自定义ViewGroup是可以正常使用的，仅仅是在设置Wrap_content，
 * 却得到match_parent的效果，这一点跟自定义View一样
 * （这是因为Android测量模式引起的一个bug，必须手动处理）
 * 2.  处理方法，参见代码，这个代码可以作为处理自定义View或ViewGroup
 * 的wrap_content问题的模板代码。
 * 3.  上面的模板代码指的是调用setMeasuredDimension(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec));
 * 以及measureWidth(widthMeasureSpec)和measureHeight(heightMeasureSpec)的写法是固定的
 * 其中关键是result = wrap_content_width;
 * 这一步，如果你是自定义View的话，这里随便给个你认为合适的数字就行，
 * 将会作为wrap_content时，这个自定义View的默认宽高
 * 但是如果你是自定义ViewGroup的话，这个result需要动脑筋了，因为作为ViewGroup内部是要包含子View的，所以说当你的ViewGroup
 * 要使用wrap_content时，你的本意是想包裹住所有的子View，所以这里的result值，就是所有子View的宽度之和（或者高度之和），而
 * 这里又引出了onMeasure方法要执行多次的问题，会影响你累加子View的宽度（或高度），所以要在onMeasure方法执行第一次的时候，
 * 去累加宽度（或高度），否则你得到的宽高值就是正确值得几倍
 * c . 必须手动处理子View的Margin值（ 实践证明，自定义Viewgroup的话padding值是不需要考虑的，其继承的ViewGroup已经帮我们考虑了padding值）
 */
public class ViewGroup1 extends ViewGroup {

    int wrap_content_width = 0;
    int wrap_content_height = 0;
    int isFirst = 0;

    public ViewGroup1(Context context) {
        this(context, null);
    }

    public ViewGroup1(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        isFirst++;
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);


        int childCount = getChildCount();
        if (childCount > 0) {
            for (int i = 0; i < childCount; i++) {
                View child = getChildAt(i);
                if (child.getVisibility() != GONE) {
                    measureChild(child, widthMeasureSpec, heightMeasureSpec);
                    MyLayoutParams marginLayoutParams= (MyLayoutParams) child.getLayoutParams();
                    int topMargin = marginLayoutParams.topMargin;
                    Log.e("qqq",topMargin+"---------------------");
                    if (isFirst == 1) {
                        wrap_content_width = Math.max(child.getMeasuredWidth(), wrap_content_width);
                        wrap_content_height = wrap_content_height + child.getMeasuredHeight();
                        Log.e("q", "第" + i + "个子view的高度:" + child.getMeasuredHeight());
                        Log.e("q", "总高度：" + wrap_content_height + ":");
                    }
                }
            }
        }
//        setMeasuredDimension(resolveSize(wrap_content_width, widthMeasureSpec),
//                resolveSize(wrap_content_height, heightMeasureSpec));
        setMeasuredDimension(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec));
    }

    private int measureWidth(int widthMeasureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(widthMeasureSpec);
        int specSize = MeasureSpec.getSize(widthMeasureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            //如果是EXACTLY模式，测量宽度就是父类传递过来的宽度，
            // （或者说你在xml布局里写的具体的宽度数值或者match_parent）
            result = specSize;
        } else {
            result = wrap_content_width;
            if (specMode == MeasureSpec.AT_MOST) {
                //如果是AT_MOST模式，测量宽度就是你自己设置的默认宽度与父类传递过
                // 来的宽度之间的一个最小值（为什么取最小值呢，因为如果你设置的默认
                // 值很大，已经超过了父控件的大小，那么这个控件在父控件里就显示不全了
                // ，如果你设置的默认值小于父控件，那么就用你设置的默认值）
                result = Math.min(result, specSize);
            }
        }
        return result;
    }

    private int measureHeight(int heightMeasureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(heightMeasureSpec);
        int specSize = MeasureSpec.getSize(heightMeasureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            result = wrap_content_height;
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }

        return result;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        int paddingLeft = getPaddingLeft();

        int paddingRight = getPaddingRight();
        int paddingTop = getPaddingTop();
        int paddingBottom = getPaddingBottom();

        int parentWidthUsed = paddingLeft + paddingRight;
        int parentHeightUsed = paddingTop + paddingBottom;


        int childCount = getChildCount();
        if (changed) {

            int childWidthUsed = 0;
            int childHeightUsed = 0;
            for (int i = 0; i < childCount; i++) {
                int left, top, right, bottom;


                View child = getChildAt(i);
                if (child.getVisibility() != GONE) {



                    left = childWidthUsed+paddingLeft ;

                    top = childHeightUsed+paddingTop;
                    right = child.getMeasuredWidth()+paddingRight;
                    bottom = childHeightUsed + child.getMeasuredHeight()+paddingBottom;

//                    usedWidth = child.getMeasuredWidth();
                    childHeightUsed += child.getMeasuredHeight();


                    child.layout(left, top, right, bottom);
                }

            }

        }
    }

    public static class MyLayoutParams extends MarginLayoutParams {
        public MyLayoutParams(MarginLayoutParams source) {
            super(source);
        }

        public MyLayoutParams(android.view.ViewGroup.LayoutParams source) {
            super(source);
        }

        public MyLayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public MyLayoutParams(int width, int height) {
            super(width, height);
        }
    }

    @Override
    protected MyLayoutParams generateDefaultLayoutParams() {
        return new MyLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    @Override
    protected android.view.ViewGroup.LayoutParams generateLayoutParams(android.view.ViewGroup.LayoutParams p) {
        return new MyLayoutParams(p);
    }

    @Override
    public android.view.ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MyLayoutParams(getContext(), attrs);
    }

    @Override
    protected boolean checkLayoutParams(android.view.ViewGroup.LayoutParams p) {
        return p instanceof MyLayoutParams;
    }
}
