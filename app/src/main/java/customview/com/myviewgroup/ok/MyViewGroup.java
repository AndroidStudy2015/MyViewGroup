package customview.com.myviewgroup.ok;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import customview.com.myviewgroup.BuildConfig;

/**
 * Created by a on 2016/6/27.
 * 只考虑Padding，不考虑margin的写法（比较简单）
 */
public class MyViewGroup extends ViewGroup {

    private int desireWidth;
    private int desireHeight;
    private float x;
    private float y;

    public MyViewGroup(Context context) {
        this(context, null);
    }

    public MyViewGroup(Context context, AttributeSet attrs) {
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
                measureChild(v, widthMeasureSpec,
                        heightMeasureSpec);
                //由于横向排列，累加所有的子View的宽度
                desireWidth += v.getMeasuredWidth();
                //高度是子View中最高的高度
                desireHeight = Math
                        .max(desireHeight, v.getMeasuredHeight());
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
//        super.onMeasure(widthMeasureSpec,heightMeasureSpec);
        setMeasuredDimension(resolveSize(desireWidth, widthMeasureSpec),
                resolveSize(desireHeight, heightMeasureSpec));
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
//        解释一下：int l, int t, int r, int b，这四个值
//        这四个值，就是通过onMeasure方法测量后，得到的这个MyViewGroup的左上右下值
//        注意：l,t,r,b是考虑了其所在的父ViewGroup的padding值的（MyviewGroup自身的padding值，当然也考虑进去了）
//        这四个值，大家可以打印出来看看就明白了，这个不清楚的话，很难写好onLayout方法
//        l=MyViewGroup的父ViewGroup的leftPadding
//        t=MyViewGroup的父ViewGroup的topPadding
//        r=MyViewGroup的父ViewGroup的leftPadding+这个MyViewGroup的宽度(即上文的desireWidth)
//        t=MyViewGroup的父ViewGroup的topPadding+这个MyViewGroup的高度(即上文的desireHeight)

        //这个自定义的ViewGroup的有效内容的四个边界
        final int parentLeft = getPaddingLeft();//这个MyViewGroup的最左边的距离（纯粹的内容的左边界，不含padding）
        final int parentTop = getPaddingTop();//这个MyViewGroup的最上边的距离（纯粹的内容的上边界，不含padding）
        final int parentRight = r - l - getPaddingRight();//这个MyViewGroup的最右边的距离（纯粹的内容的右边界，不含padding）
        final int parentBottom = b - t - getPaddingBottom();//这个MyViewGroup的最下边的距离（纯粹的内容的下边界，不含padding）

        if (BuildConfig.DEBUG)
            Log.d("onlayout", "parentleft: " + parentLeft + "   parenttop: "
                    + parentTop + "   parentright: " + parentRight
                    + "   parentbottom: " + parentBottom + "\n" + "   l: " + l + "   t: " + t + "   r: " + r + "   b: " + b);

        int left = parentLeft;
        int top = parentTop;

        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View v = getChildAt(i);
            if (v.getVisibility() != View.GONE) {
                //得到每一个子View的测量后的宽高
                final int childWidth = v.getMeasuredWidth();
                final int childHeight = v.getMeasuredHeight();
                //开始布局每一个子View（左、上、右=左+子View宽、下=上+子View高）
                v.layout(left, top, left + childWidth, top + childHeight);
                //由于本例是横向排列的，所以每一个子View的left值要递增
                left += childWidth;
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int action = event.getAction();

        if (BuildConfig.DEBUG)
            Log.d("onTouchEvent", "action: " + action);

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                x = event.getX();
                y = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                float mx = event.getX();
                float my = event.getY();

                //此处的moveBy是根据水平或是垂直排放的方向，
                //来选择是水平移动还是垂直移动
                moveBy((int) (x - mx), (int) (y - my));

                x = mx;
                y = my;
                break;

        }
        return true;
    }

    //此处的moveBy是根据水平或是垂直排放的方向，
//来选择是水平移动还是垂直移动
    public void moveBy(int deltaX, int deltaY) {
        if (BuildConfig.DEBUG)
            Log.d("moveBy", "deltaX: " + deltaX + "    deltaY: " + deltaY);

            if (Math.abs(deltaX) >= Math.abs(deltaY))
                scrollBy(deltaX, 0);
        }

    }

