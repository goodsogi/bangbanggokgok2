/**
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *//*

package co.kr.app.bangbanggokgok.common;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageLoader.ImageContainer;
import com.android.volley.toolbox.ImageLoader.ImageListener;

*/
/**
 * Handles fetching an image from a URL as well as the life-cycle of the
 * associated request.
 *//*

public class CircleImageView extends ImageView {
	*/
/** The URL of the network image to load *//*

	private String mUrl;

	*/
/**
	 * Resource ID of the image to be used as a placeholder until the network image is loaded.
	 *//*

	private int mDefaultImageId;

	*/
/**
	 * Resource ID of the image to be used if the network response fails.
	 *//*

	private int mErrorImageId;

	*/
/** Local copy of the ImageLoader. *//*

	private ImageLoader mImageLoader;

	*/
/** Current ImageContainer. (either in-flight or finished) *//*

	private ImageContainer mImageContainer;

	private int mWidth = 0;

	public CircleImageView(Context context) {
		this(context, null);
	}

	public CircleImageView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public CircleImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		int[] attrsArray = new int[] { android.R.attr.layout_width, android.R.attr.layout_height };
		TypedArray ta = context.obtainStyledAttributes(attrs, attrsArray);
		int layout_width = ta.getDimensionPixelSize(0, ViewGroup.LayoutParams.MATCH_PARENT);
		int layout_height = ta.getDimensionPixelSize(1, ViewGroup.LayoutParams.MATCH_PARENT);
		ta.recycle();
		mWidth = Math.max(layout_width, layout_height);
		Log.e("TEST", "WIDTH = " + mWidth);
	}

	*/
/**
	 * Sets URL of the image that should be loaded into this view. Note that calling this will
	 * immediately either set the cached image (if available) or the default image specified by
	 * {@link CircleImageView#setDefaultImageResId(int)} on the view.
	 *
	 * NOTE: If applicable, {@link CircleImageView#setDefaultImageResId(int)} and
	 * {@link CircleImageView#setErrorImageResId(int)} should be called prior to calling
	 * this function.
	 *
	 * @param url The URL that should be loaded into this ImageView.
	 * @param imageLoader ImageLoader that will be used to make the request.
	 *//*

	public void setImageUrl(String url, ImageLoader imageLoader) {
		mUrl = url;
		mImageLoader = imageLoader;
		// The URL has potentially changed. See if we need to load it.
		loadImageIfNecessary(false);
	}

	*/
/**
	 * Sets the default image resource ID to be used for this view until the attempt to load it
	 * completes.
	 *//*

	public void setDefaultImageResId(int defaultImage) {
		mDefaultImageId = defaultImage;
	}

	*/
/**
	 * Sets the error image resource ID to be used for this view in the event that the image
	 * requested fails to load.
	 *//*

	public void setErrorImageResId(int errorImage) {
		mErrorImageId = errorImage;
	}

	*/
/**
	 * Loads the image for the view if it isn't already loaded.
	 * @param isInLayoutPass True if this was invoked from a layout pass, false otherwise.
	 *//*

	private void loadImageIfNecessary(final boolean isInLayoutPass) {
		int width = getWidth();
		int height = getHeight();

		boolean isFullyWrapContent = getLayoutParams() != null && getLayoutParams().height == LayoutParams.WRAP_CONTENT
				&& getLayoutParams().width == LayoutParams.WRAP_CONTENT;
		// if the view's bounds aren't known yet, and this is not a wrap-content/wrap-content
		// view, hold off on loading the image.
		if (width == 0 && height == 0 && !isFullyWrapContent) {
			return;
		}

		// if the URL to be loaded in this view is empty, cancel any old requests and clear the
		// currently loaded image.
		if (TextUtils.isEmpty(mUrl)) {
			if (mImageContainer != null) {
				mImageContainer.cancelRequest();
				mImageContainer = null;
			}
			setImageResource(mDefaultImageId);
			return;
		}

		// if there was an old request in this view, check if it needs to be canceled.
		if (mImageContainer != null && mImageContainer.getRequestUrl() != null) {
			if (mImageContainer.getRequestUrl().equals(mUrl)) {
				// if the request is from the same URL, return.
				return;
			} else {
				// if there is a pre-existing request, cancel it if it's fetching a different URL.
				mImageContainer.cancelRequest();
				setImageResource(mDefaultImageId);
			}
		}

		if (mImageLoader == null || mUrl == null) {
			if (mDefaultImageId != 0) {
				setImageResource(mDefaultImageId);
			}
		}

		// The pre-existing content of this view didn't match the current URL. Load the new image
		// from the network.
		ImageContainer newContainer = mImageLoader.get(mUrl, new ImageListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				if (mErrorImageId != 0) {
					setImageResource(mErrorImageId);
				}
			}

			@Override
			public void onResponse(final ImageContainer response, boolean isImmediate) {
				// If this was an immediate response that was delivered inside of a layout
				// pass do not set the image immediately as it will trigger a requestLayout
				// inside of a layout. Instead, defer setting the image by posting back to
				// the main thread.
				if (isImmediate && isInLayoutPass) {
					post(new Runnable() {
						@Override
						public void run() {
							onResponse(response, false);
						}
					});
					return;
				}

				if (response.getBitmap() != null) {
					setImageBitmap(getRoundedCornerBitmap(response.getBitmap(), mWidth));
				} else if (mDefaultImageId != 0) {
					setImageResource(mDefaultImageId);
				}
			}
		});

		// update the ImageContainer to be the new bitmap container.
		mImageContainer = newContainer;
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		loadImageIfNecessary(true);
	}

	@Override
	protected void onDetachedFromWindow() {
		if (mImageContainer != null) {
			// If the view was bound to an image request, cancel it and clear
			// out the image from the view.
			mImageContainer.cancelRequest();
			setImageBitmap(null);
			// also clear out the container so we can reload the image if necessary.
			mImageContainer = null;
		}
		super.onDetachedFromWindow();
	}

	@Override
	protected void drawableStateChanged() {
		super.drawableStateChanged();
		invalidate();
	}

	@Override
	public void setImageBitmap(Bitmap bm) {
		this.setScaleType(ScaleType.FIT_CENTER);
		super.setImageBitmap(bm);
	}

	@Override
	public void setImageResource(int resId) {
		this.setScaleType(ScaleType.FIT_CENTER);
		super.setImageResource(resId);
	}

	public Bitmap getRoundedCornerBitmap(Bitmap bmp, int radius) {
		Bitmap sbmp;

		if (bmp.getWidth() != radius || bmp.getHeight() != radius) {
			float smallest = Math.min(bmp.getWidth(), bmp.getHeight());
			float factor = smallest / radius;
			sbmp = Bitmap.createScaledBitmap(bmp, (int) (bmp.getWidth() / factor), (int) (bmp.getHeight() / factor), false);
		} else {
			sbmp = bmp;
		}

		Bitmap output = Bitmap.createBitmap(radius, radius, Config.ARGB_8888);
		Canvas canvas = new Canvas(output);

		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, radius, radius);

		paint.setAntiAlias(true);
		paint.setFilterBitmap(true);
		paint.setDither(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(Color.parseColor("#BAB399"));
		canvas.drawCircle(radius / 2, radius / 2, radius / 2, paint);
		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(sbmp, rect, rect, paint);

		return output;
	}
}
*/