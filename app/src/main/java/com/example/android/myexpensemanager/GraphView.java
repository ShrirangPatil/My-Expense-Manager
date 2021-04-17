package com.example.android.myexpensemanager;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

public class GraphView extends View {
    /**
     * Context
     */
    Context mContext = null;
    /**
     * Axis lines
     */
    Paint mAxisPaint;
    Path mXAxis, mYAxis;
    private final int AXIS_STROKE_WIDTH = 5;
    /**
     * gap between x and y axis from screen
     */
    private final int PADDING = 40;

    /**
     * Highlighter lines
     */
    Paint mHighLightPaint;
    Path[] mHighLightXAxis = new Path[3];
    Path[] mHighLightYAxis = new Path[25];
    private final int HIGHLIGHT_STROKE_WIDTH = 2;
    /**
     * gap between y highlight
     */
    private final float HIGHLIGHT_GAP_Y = (float) 69.82;
    /**
     * gap between x highlight
     */
    private final int HIGHLIGHT_GAP_X = 82;
    private static final String TAG = GraphView.class.getName();

    /**
     * Characters in graph
     */
    Paint mCharPaint;
    private final int CHARACTER_STROKE_WIDTH = 100;

    private void  init(Context context) {
        mContext = context;
        mAxisPaint = new Paint();
        mAxisPaint.setAntiAlias(true);
        mAxisPaint.setStyle(Paint.Style.STROKE);
        mAxisPaint.setColor(Color.BLACK);
        mAxisPaint.setStrokeWidth(AXIS_STROKE_WIDTH);

        /**
         *  Display metrics
         */
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;
        Log.i(TAG, "Height "+height);
        Log.i(TAG, "Width "+width);
        Log.i(TAG, "Padding "+pxFromDp(context, PADDING));
        Log.i(TAG, "Density "+context.getResources().getDisplayMetrics().density);

        /**
         * X and Y axis
         */
        Point xAxisP1 = new Point((int) 0.0, (int) pxFromDp(context, PADDING));
        Point xAxisP2 = new Point((int) width, (int) pxFromDp(context, PADDING));
        mXAxis = new Path();
        mXAxis.moveTo(xAxisP1.x, xAxisP1.y);
        mXAxis.lineTo(xAxisP2.x, xAxisP2.y);
        mXAxis.close();

        Point yAxisP1 = new Point((int) pxFromDp(context, PADDING), 0);
        Point yAxisP2 = new Point((int) pxFromDp(context, PADDING), height);
        mYAxis = new Path();
        mYAxis.moveTo(yAxisP1.x, yAxisP1.y);
        mYAxis.lineTo(yAxisP2.x, yAxisP2.y);
        mYAxis.close();

        /**
         * Highlight lines
         */
        mHighLightPaint = new Paint();
        mHighLightPaint.setAntiAlias(true);
        mHighLightPaint.setStyle(Paint.Style.STROKE);
        mHighLightPaint.setColor(Color.GREEN);
        mHighLightPaint.setStrokeWidth(HIGHLIGHT_STROKE_WIDTH);

        for (int i = 1; i < 4; i++) {
            Point xHighLightP1 = new Point((int) 0.0, (int) pxFromDp(context, PADDING) + (int) pxFromDp(mContext, HIGHLIGHT_GAP_Y) * i);
            Point xHighLightP2 = new Point((int) width, (int) pxFromDp(context, PADDING) + (int) pxFromDp(mContext, HIGHLIGHT_GAP_Y) * i);
            mHighLightXAxis[i-1] = new Path();
            mHighLightXAxis[i-1].moveTo(xHighLightP1.x, xHighLightP1.y);
            mHighLightXAxis[i-1].lineTo(xHighLightP2.x, xHighLightP2.y);
            mHighLightXAxis[i-1].close();
        }

        for (int i = 1; i < 26; i++) {
            Point yHighLightP1 = new Point((int) pxFromDp(context, PADDING) + HIGHLIGHT_GAP_X * i, (int) 0.0);
            Point yHighLightP2 = new Point((int) pxFromDp(context, PADDING) + HIGHLIGHT_GAP_X * i, (int) height);
            mHighLightYAxis[i-1] = new Path();
            mHighLightYAxis[i-1].moveTo(yHighLightP1.x, yHighLightP1.y);
            mHighLightYAxis[i-1].lineTo(yHighLightP2.x, yHighLightP2.y);
            mHighLightYAxis[i-1].close();
        }

        /**
         * Characters
         */
        mCharPaint = new Paint();
        mCharPaint.setTextSize(50);
        mCharPaint.setStyle(Paint.Style.FILL);
        mCharPaint.setColor(Color.BLACK);
    }

    public GraphView(Context context) {
        super(context);
        init(context);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        /**
         * characters on y-axis
         */
        for (int i = 1; i < 5; i++) {
            canvas.drawText(Integer.toString(100*(5-i)), (int) pxFromDp(mContext, 5), (int) pxFromDp(mContext, HIGHLIGHT_GAP_Y) * i - 10, mCharPaint);
        }
        canvas.translate(0,canvas.getHeight());     // reset where 0,0 is located
        canvas.scale(1,-1);                     // invert
        super.onDraw(canvas);

        canvas.drawPath(mXAxis, mAxisPaint);
        canvas.drawPath(mYAxis, mAxisPaint);

        for (int i = 0; i < 3; i++) {
            canvas.drawPath(mHighLightXAxis[i], mHighLightPaint);
        }
        for (int i = 0; i < 25; i++) {
            canvas.drawPath(mHighLightYAxis[i], mHighLightPaint);
        }
    }

    private static float pxFromDp(final Context context, final float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }
}
