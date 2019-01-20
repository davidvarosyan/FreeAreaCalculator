package com.picsart.freeareacalculator;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.Stack;

import static android.view.MotionEvent.INVALID_POINTER_ID;

public class FreeSpaceView extends View {

	private float mPosX;
	private float mPosY;
	private float scale;

	private ScaleGestureDetector scaleGestureDetector;

	private ArrayList<RectF> rectArrayList;
	private static final int ROW_SIZE = 20;
	private Paint rectPaint;
	private Paint strokePaint;
	private boolean showPreview;
	private Paint rectGradientPaint;
	private int matrix[][];


	public FreeSpaceView(Context context) {
		this(context, null);
	}

	public FreeSpaceView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public FreeSpaceView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		initMatrix();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.clipRect(0, 0, getWidth(), getHeight());
		canvas.drawColor(Color.WHITE);

		if (!showPreview) {
			for (RectF rectF : rectArrayList) {
				canvas.drawRect(rectF, rectPaint);
				canvas.drawRect(rectF, rectGradientPaint);
			}
		} else if (matrix != null) {
			canvas.save();

			canvas.scale(scale, scale);
			canvas.translate(mPosX, mPosY);
			//canvas.scale(0.5f, 0.5f);
			for (RectF rectF : rectArrayList) {
				canvas.drawRect(rectF, rectPaint);
				canvas.drawRect(rectF, rectGradientPaint);
			}
			for (int i = 0; i < matrix.length; i++) {
				canvas.drawLine((i + 1) * ROW_SIZE, 0, (i + 1) * ROW_SIZE, getHeight(), strokePaint);
			}

			for (int i = 0; i < matrix[0].length; i++) {
				canvas.drawLine(0, (i + 1) * ROW_SIZE, getWidth(), (i + 1) * ROW_SIZE, strokePaint);
			}

			for (int i = 0; i < matrix.length; i++) {
				for (int j = 0; j < matrix[0].length; j++) {
					canvas.drawText(String.valueOf(matrix[i][j]), i * ROW_SIZE , j * ROW_SIZE + ROW_SIZE, strokePaint);
				}
			}

			canvas.restore();
		}

	}

	private void init() {
		rectArrayList = new ArrayList<>();
		strokePaint = new Paint();
		strokePaint.setStyle(Paint.Style.STROKE);
		strokePaint.setColor(Color.BLACK);
		rectPaint = new Paint();
		rectPaint.setStyle(Paint.Style.FILL);
		rectPaint.setColor(Color.GRAY);
		rectGradientPaint = new Paint();
		rectGradientPaint.setStyle(Paint.Style.STROKE);
		rectGradientPaint.setColor(Color.BLACK);

		scaleGestureDetector = new ScaleGestureDetector(getContext(), new ScaleGestureDetector.OnScaleGestureListener() {
			@Override
			public boolean onScale(ScaleGestureDetector detector) {
				scale *= detector.getScaleFactor();

				// Don't let the object get too small or too large.
				scale = Math.max(0.1f, Math.min(scale, 10f));
				invalidate();
				return true;
			}

			@Override
			public boolean onScaleBegin(ScaleGestureDetector detector) {
				return true;
			}

			@Override
			public void onScaleEnd(ScaleGestureDetector detector) {

			}
		});
	}

	private void initMatrix() {
		matrix = new int[getWidth() / ROW_SIZE][getHeight() / ROW_SIZE];
		for (int[] aMatrix : matrix) {
			Arrays.fill(aMatrix, 1);
		}
		scale = 1;
	}




    /*Matrix
    StringBuilder []a = new StringBuilder[mapY];
        for(int i = 0 ; i<mapY;i++){
            StringBuilder stringBuilder = new StringBuilder();
            for(int j = 0 ; j< mapX ; j++){
                stringBuilder.append(rectMap[j][i]);
            }
            a[i] = stringBuilder;
        }
        for(StringBuilder stringBuilder:a){
            System.out.println(stringBuilder.toString());
        }*/


	public void addRect() {
		int width = new Random().nextInt(getWidth());
		int height = new Random().nextInt(getHeight());
		RectF rectF = new RectF(0, 0, width, height);
		rectArrayList.add(rectF);
		computeMatrix();
		invalidate();
	}

	private void getMaxLenghtRectInfoFromMatrixRow(int rowLenght, int row[], int[] resultOutput) {
		Stack<Integer> result = new Stack<>();
		int top_val;
		int startY = 0;
		int endY = 0;
		int width = 0;
		int max_area = 0;
		int area;
		int i = 0;
		while (i < rowLenght) {
			if (result.empty() || row[result.peek()] <= row[i]) {
				result.push(i++);
			} else {
				int y = result.peek();
				top_val = row[result.peek()];
				result.pop();
				area = top_val * i;
				if (!result.empty()) {
					area = top_val * (i - result.peek() - 1);
				}
				if (max_area < area) {
					if (result.empty()) {
						startY = y;
						endY = startY + 1;
						width = top_val;
						max_area = area;
					} else {
						startY = result.peek();
						endY = i;
						width = top_val;
						max_area = area;
					}
				}
			}
		}
		while (!result.empty()) {
			int y = result.peek();
			top_val = row[result.peek()];
			result.pop();
			area = top_val * i;
			if (!result.empty()) {
				area = top_val * (i - result.peek() - 1);
			}
			if (max_area < area) {
				if (result.empty()) {
					startY = y;
					endY = startY + 1;
					width = top_val;
					max_area = area;
				} else {
					startY = result.peek();
					endY = i;
					width = top_val;
					max_area = area;
				}
			}
		}
		resultOutput[0] = max_area;
		resultOutput[1] = startY;
		resultOutput[2] = endY;
		resultOutput[3] = width;
	}

	/**
	 * Returns largest rectangle with all 1s in matrix[][]
	 */
	private RectF getMaxLenghtRectFromMatrix(int matrixWidth, int matrixHeight, int matrix[][]) {
		int[] result = new int[4];
		int width = 0;
		int endX = 0;

		getMaxLenghtRectInfoFromMatrixRow(matrixHeight, matrix[0], result);
		for (int i = 1; i < matrixWidth; i++) {
			for (int j = 0; j < matrixHeight; j++) {
				if (matrix[i][j] == 1) {
					matrix[i][j] += matrix[i - 1][j];
				}
			}
			int[] res = new int[4];
			getMaxLenghtRectInfoFromMatrixRow(matrixHeight, matrix[i], res);
			if (res[0] > result[0] && res[2] - res[1] + 1 > 1) {
				result = res;
				endX = i + 1;
				width = result[3];
			}
		}
		return new RectF(endX - width, result[1] + 1, endX, result[2]);

	}

	private void computeMatrix() {
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[0].length; j++) {
				for (RectF rectF : rectArrayList) {
					if (rectF.contains(i * ROW_SIZE + ROW_SIZE / 2, j * ROW_SIZE + ROW_SIZE / 2)) {
						matrix[i][j] = 0;
						break;
					}
				}
			}
		}
		getMaxLenghtRectFromMatrix(matrix.length, matrix[0].length, matrix);
	}

	public void changePreviewMode() {
		showPreview = !showPreview;
		invalidate();
	}

	private float mLastTouchX;
	private float mLastTouchY;
	private int mActivePointerId;


	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (showPreview) {
			// Let the ScaleGestureDetector inspect all events.
			scaleGestureDetector.onTouchEvent(ev);

			final int action = MotionEventCompat.getActionMasked(ev);

			//Compute drag delta x and delata y
			switch (action) {
				case MotionEvent.ACTION_DOWN: {
					final int pointerIndex = MotionEventCompat.getActionIndex(ev);
					final float x = MotionEventCompat.getX(ev, pointerIndex);
					final float y = MotionEventCompat.getY(ev, pointerIndex);

					// Remember where we started (for dragging)
					mLastTouchX = x;
					mLastTouchY = y;
					// Save the ID of this pointer (for dragging)
					mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
					break;
				}

				case MotionEvent.ACTION_MOVE: {
					// Find the index of the active pointer and fetch its position
					final int pointerIndex =
							MotionEventCompat.findPointerIndex(ev, mActivePointerId);

					final float x = MotionEventCompat.getX(ev, pointerIndex);
					final float y = MotionEventCompat.getY(ev, pointerIndex);

					// Calculate the distance moved
					final float dx = x - mLastTouchX;
					final float dy = y - mLastTouchY;

					mPosX += dx;
					mPosY += dy;

					invalidate();

					// Remember this touch position for the next move event
					mLastTouchX = x;
					mLastTouchY = y;

					break;
				}

				case MotionEvent.ACTION_UP: {
					mActivePointerId = INVALID_POINTER_ID;
					break;
				}

				case MotionEvent.ACTION_CANCEL: {
					mActivePointerId = INVALID_POINTER_ID;
					break;
				}

				case MotionEvent.ACTION_POINTER_UP: {

					final int pointerIndex = MotionEventCompat.getActionIndex(ev);
					final int pointerId = MotionEventCompat.getPointerId(ev, pointerIndex);

					if (pointerId == mActivePointerId) {
						// This was our active pointer going up. Choose a new
						// active pointer and adjust accordingly.
						final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
						mLastTouchX = MotionEventCompat.getX(ev, newPointerIndex);
						mLastTouchY = MotionEventCompat.getY(ev, newPointerIndex);
						mActivePointerId = MotionEventCompat.getPointerId(ev, newPointerIndex);
					}
					break;
				}
			}
			return true;
		}
		return true;
	}

	public boolean isShowPreview() {
		return showPreview;
	}
}
