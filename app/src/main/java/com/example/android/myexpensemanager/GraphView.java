package com.example.android.myexpensemanager;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.view.View;

public class GraphView extends View {
    Paint mAxisPaint;
    Path mXAxis, mYAxis;
    private final int STROKE_WIDTH = 5;
    private final int PADDING = 40;

    private void  init(Context context) {
        mAxisPaint = new Paint();
        mAxisPaint.setAntiAlias(true);
        mAxisPaint.setStyle(Paint.Style.STROKE);
        mAxisPaint.setColor(Color.BLACK);
        mAxisPaint.setStrokeWidth(STROKE_WIDTH);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;

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
    }

    public GraphView(Context context) {
        super(context);
        init(context);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.translate(0,canvas.getHeight());     // reset where 0,0 is located
        canvas.scale(1,-1);                     // invert
        super.onDraw(canvas);

        canvas.drawPath(mXAxis, mAxisPaint);
        canvas.drawPath(mYAxis, mAxisPaint);
    }

    private static float pxFromDp(final Context context, final float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }
}
