package com.picsart.freeareacalculator;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
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
					canvas.drawText(String.valueOf(matrix[i][j]), i * ROW_SIZE, j * ROW_SIZE + ROW_SIZE, strokePaint);
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
		scale = 1;

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
	}


	public void addRect() {
		int width = new Random().nextInt(getWidth());
		int height = new Random().nextInt(getHeight());
		RectF rectF = new RectF(0, 0, width, height);
		initMatrix();
		computeMatrix();
		if (rectArrayList.isEmpty()) {
			rectF.offsetTo(getWidth() / 2 - rectF.width() / 2, getHeight() / 2 - rectF.height() / 2);
		} else {
			Rect r = maxRect(matrix);
			int cx = r.centerX() * ROW_SIZE;
			int cy = r.centerY() * ROW_SIZE;
			rectF.offsetTo(cx - rectF.width() / 2, cy - rectF.height() / 2);
		}
		rectArrayList.add(rectF);
		initMatrix();
		computeMatrix();
		maxRect(matrix);
		invalidate();
	}

	public void clear() {
		rectArrayList.clear();
		initMatrix();
		invalidate();
	}


	static int[] maxRectInfo(int column[]) {
		Stack<Integer> columnElementsIndexes = new Stack<>();

		int topValue;
		int columnHeight = column.length;
		int startY = 0;
		int endY = 0;
		int rowWidth = 0;

		int maxArea = 0;
		int area;
		int i = 0;

		while (i < columnHeight) {
			if (columnElementsIndexes.empty() || column[columnElementsIndexes.peek()] <= column[i])
				columnElementsIndexes.push(i++);

			else {

				topValue = column[columnElementsIndexes.peek()];
				columnElementsIndexes.pop();

				if (!columnElementsIndexes.empty()) {
					area = topValue * (i - columnElementsIndexes.peek() - 1);
					if (maxArea < area) {
						maxArea = area;
						endY = i;
						startY = i - columnElementsIndexes.peek() - 1;
						rowWidth = topValue;
					}
				}
			}
		}
		int possibleStartY = 0;
		int possibleEndY = 0;

		while (!columnElementsIndexes.empty()) {
			topValue = column[columnElementsIndexes.peek()];
			columnElementsIndexes.pop();
			area = topValue * i;
			possibleEndY = i;
			possibleStartY = 0;
			if (!columnElementsIndexes.empty()) {
				area = topValue * (i - columnElementsIndexes.peek() - 1);
				possibleEndY = i;
				possibleStartY = columnElementsIndexes.peek() - 1;
			}

			if (area > maxArea) {
				maxArea = area;
				rowWidth = topValue;
				endY = possibleEndY;
				startY = possibleStartY;

			}
		}
		return new int[]{maxArea, startY, endY, rowWidth};
	}

	static Rect maxRect(int matrix[][]) {
		int matrixWidth = matrix.length;
		int matrixHeight = matrix[0].length;
		int result[] = maxRectInfo(matrix[0]);
		int endX = 0;

		for (int i = 1; i < matrixWidth; i++) {

			for (int j = 0; j < matrixHeight; j++) {

				if (matrix[i][j] == 1) {
					matrix[i][j] += matrix[i - 1][j];
				}
			}
			int res[] = maxRectInfo(matrix[i]);
			if (res[0] > result[0]) {
				result = res;
				endX = i;
			}
		}

		return new Rect(endX - result[3] + 1, result[1] - 1, endX + 1, result[2] - 1);
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
