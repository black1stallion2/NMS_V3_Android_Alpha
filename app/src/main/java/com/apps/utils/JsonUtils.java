package com.apps.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.StringTokenizer;

import com.apps.adapter.AdapterPlaylistDialog;
import com.apps.interfaces.ClickListenerPlayList;
import com.apps.item.ItemPlayList;
import com.apps.item.ItemSong;
import com.apps.onlinemp3.R;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;

public class JsonUtils {
	
	private Context _context;
	private DBHelper dbHelper;

	// constructor
	public JsonUtils(Context context) {
		this._context = context;
		dbHelper = new DBHelper(_context);
	}
	public static String getJSONString(String url) {
		String jsonString = null;
		HttpURLConnection linkConnection = null;
		try {
			URL linkurl = new URL(url);
			linkConnection = (HttpURLConnection) linkurl.openConnection();
			int responseCode = linkConnection.getResponseCode();
			if (responseCode == HttpURLConnection.HTTP_OK) {
				InputStream linkinStream = linkConnection.getInputStream();
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				int j = 0;
				while ((j = linkinStream.read()) != -1) {
					baos.write(j);
				}
				byte[] data = baos.toByteArray();
				jsonString = new String(data);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (linkConnection != null) {
				linkConnection.disconnect();
			}
		}
		return jsonString;
	}

	public static boolean isNetworkAvailable(Context activity) {
		ConnectivityManager connectivityManager
				= (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}
	
	public int getScreenWidth() {
		int columnWidth;
		WindowManager wm = (WindowManager) _context
				.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();

		final Point point = new Point();
		
			point.x = display.getWidth();
			point.y = display.getHeight();
		
		columnWidth = point.x;
		return columnWidth;
	}

	private static final DecelerateInterpolator DECCELERATE_INTERPOLATOR = new DecelerateInterpolator();
	private static final AccelerateInterpolator ACCELERATE_INTERPOLATOR = new AccelerateInterpolator();
	private static final OvershootInterpolator OVERSHOOT_INTERPOLATOR = new OvershootInterpolator(4);

	public static void animateHeartButton(final View v) {
		AnimatorSet animatorSet = new AnimatorSet();

		ObjectAnimator rotationAnim = ObjectAnimator.ofFloat(v, "rotation", 0f, 360f);
		rotationAnim.setDuration(300);
		rotationAnim.setInterpolator(ACCELERATE_INTERPOLATOR);

		ObjectAnimator bounceAnimX = ObjectAnimator.ofFloat(v, "scaleX", 0.2f, 1f);
		bounceAnimX.setDuration(300);
		bounceAnimX.setInterpolator(OVERSHOOT_INTERPOLATOR);

		ObjectAnimator bounceAnimY = ObjectAnimator.ofFloat(v, "scaleY", 0.2f, 1f);
		bounceAnimY.setDuration(300);
		bounceAnimY.setInterpolator(OVERSHOOT_INTERPOLATOR);
		bounceAnimY.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationStart(Animator animation) {
			}

			@Override
			public void onAnimationEnd(Animator animation) {
			}
		});

		animatorSet.play(bounceAnimX).with(bounceAnimY).after(rotationAnim);
		animatorSet.start();
	}

	public static void animatePhotoLike(final View vBgLike, final View ivLike) {
		vBgLike.setVisibility(View.VISIBLE);
		ivLike.setVisibility(View.VISIBLE);

		vBgLike.setScaleY(0.1f);
		vBgLike.setScaleX(0.1f);
		vBgLike.setAlpha(1f);
		ivLike.setScaleY(0.1f);
		ivLike.setScaleX(0.1f);

		android.animation.AnimatorSet animatorSet = new android.animation.AnimatorSet();

		android.animation.ObjectAnimator bgScaleYAnim = android.animation.ObjectAnimator.ofFloat(vBgLike, "scaleY", 0.1f, 1f);
		bgScaleYAnim.setDuration(200);
		bgScaleYAnim.setInterpolator(DECCELERATE_INTERPOLATOR);
		android.animation.ObjectAnimator bgScaleXAnim = android.animation.ObjectAnimator.ofFloat(vBgLike, "scaleX", 0.1f, 1f);
		bgScaleXAnim.setDuration(200);
		bgScaleXAnim.setInterpolator(DECCELERATE_INTERPOLATOR);
		android.animation.ObjectAnimator bgAlphaAnim = android.animation.ObjectAnimator.ofFloat(vBgLike, "alpha", 1f, 0f);
		bgAlphaAnim.setDuration(200);
		bgAlphaAnim.setStartDelay(150);
		bgAlphaAnim.setInterpolator(DECCELERATE_INTERPOLATOR);

		android.animation.ObjectAnimator imgScaleUpYAnim = android.animation.ObjectAnimator.ofFloat(ivLike, "scaleY", 0.1f, 1f);
		imgScaleUpYAnim.setDuration(300);
		imgScaleUpYAnim.setInterpolator(DECCELERATE_INTERPOLATOR);
		android.animation.ObjectAnimator imgScaleUpXAnim = android.animation.ObjectAnimator.ofFloat(ivLike, "scaleX", 0.1f, 1f);
		imgScaleUpXAnim.setDuration(300);
		imgScaleUpXAnim.setInterpolator(DECCELERATE_INTERPOLATOR);

		android.animation.ObjectAnimator imgScaleDownYAnim = android.animation.ObjectAnimator.ofFloat(ivLike, "scaleY", 1f, 0f);
		imgScaleDownYAnim.setDuration(300);
		imgScaleDownYAnim.setInterpolator(ACCELERATE_INTERPOLATOR);
		android.animation.ObjectAnimator imgScaleDownXAnim = android.animation.ObjectAnimator.ofFloat(ivLike, "scaleX", 1f, 0f);
		imgScaleDownXAnim.setDuration(300);
		imgScaleDownXAnim.setInterpolator(ACCELERATE_INTERPOLATOR);

		animatorSet.playTogether(bgScaleYAnim, bgScaleXAnim, bgAlphaAnim, imgScaleUpYAnim, imgScaleUpXAnim);
		animatorSet.play(imgScaleDownYAnim).with(imgScaleDownXAnim).after(imgScaleUpYAnim);

		animatorSet.addListener(new android.animation.AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(android.animation.Animator animation) {
				vBgLike.setVisibility(View.INVISIBLE);
				ivLike.setVisibility(View.INVISIBLE);
			}
		});
		animatorSet.start();
	}

	public static String milliSecondsToTimer(long milliseconds){
		String finalTimerString = "";
		String secondsString = "";

		// Convert total duration into time
		int hours = (int)( milliseconds / (1000*60*60));
		int minutes = (int)(milliseconds % (1000*60*60)) / (1000*60);
		int seconds = (int) ((milliseconds % (1000*60*60)) % (1000*60) / 1000);
		// Add hours if there
		if(hours > 0){
			finalTimerString = hours + ":";
		}

		// Prepending 0 to seconds if it is one digit
		if(seconds < 10){
			secondsString = "0" + seconds;
		}else{
			secondsString = "" + seconds;}

		finalTimerString = finalTimerString + minutes + ":" + secondsString;

		// return timer string
		return finalTimerString;
	}

	public static int getProgressPercentage(long currentDuration, long totalDuration){
		Double percentage = (double) 0;

		long currentSeconds = (int) (currentDuration / 1000);
		long totalSeconds = (int) (totalDuration / 1000);

		// calculating percentage
		percentage =(((double)currentSeconds)/totalSeconds)*100;

		// return percentage
		return percentage.intValue();
	}

	public static long getSeekFromPercentage(int percentage, long totalDuration){

		long currentSeconds = 0;
		long totalSeconds = (int) (totalDuration / 1000);

		// calculating percentage
		currentSeconds = (percentage*totalSeconds)/100;

		// return percentage
		return currentSeconds*1000;
	}

	/**
	 * Function to change progress to timer
	 * @param progress -
	 * @param totalDuration
	 * returns current duration in milliseconds
	 * */
	public int progressToTimer(int progress, int totalDuration) {
		int currentDuration = 0;
		totalDuration = (int) (totalDuration / 1000);
		currentDuration = (int) ((((double)progress) / 100) * totalDuration);

		// return current duration in milliseconds
		return currentDuration * 1000;
	}

	public static int calculateTime(String duration)
	{
		int time=0, min, sec;
		try {
			StringTokenizer st = new StringTokenizer(duration, ".");
			min = Integer.parseInt(st.nextToken());
			sec = Integer.parseInt(st.nextToken());
		} catch (Exception e) {
			StringTokenizer st = new StringTokenizer(duration,":");
			min = Integer.parseInt(st.nextToken());
			sec = Integer.parseInt(st.nextToken());
		}
		time = ((min*60)+sec)*1000;
		return time;
	}

	public void forceRTLIfSupported(Window window) {
		if(_context.getResources().getString(R.string.isRTL).equals("true")) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
				window.getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
			}
		}
	}

	public void openPlaylists(final ItemSong itemSong) {
		final Dialog dialog = new Dialog(_context);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.layout_dialog_playlist);

		final ArrayList<ItemPlayList> arrayList_playlist = dbHelper.loadPlayList();

		final Button button_close = dialog.findViewById(R.id.button_close_dialog);
		final TextView textView = dialog.findViewById(R.id.textView_empty_dialog_pl);
		final RecyclerView recyclerView = dialog.findViewById(R.id.recyclerView_dialog_playlist);
		LinearLayoutManager linearLayoutManager = new LinearLayoutManager(_context);
		recyclerView.setLayoutManager(linearLayoutManager);
		recyclerView.setItemAnimator(new DefaultItemAnimator());
		recyclerView.setHasFixedSize(true);
		AdapterPlaylistDialog adapterPlaylist = new AdapterPlaylistDialog(_context, arrayList_playlist, new ClickListenerPlayList() {
			@Override
			public void onClick(int position) {
				dbHelper.addToPlayList(itemSong, arrayList_playlist.get(position).getId());
				Toast.makeText(_context, _context.getString(R.string.song_add_to_playlist) + arrayList_playlist.get(position).getName(), Toast.LENGTH_SHORT).show();
				dialog.dismiss();
			}

			@Override
			public void onItemZero() {
				textView.setVisibility(View.VISIBLE);
				recyclerView.setVisibility(View.GONE);
			}
		});
		recyclerView.setAdapter(adapterPlaylist);
		if(arrayList_playlist.size() == 0) {
			textView.setVisibility(View.VISIBLE);
			recyclerView.setVisibility(View.GONE);
		}

		button_close.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				dialog.dismiss();
			}
		});

		WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
		lp.copyFrom(dialog.getWindow().getAttributes());
		lp.width = WindowManager.LayoutParams.MATCH_PARENT;
		lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
		dialog.getWindow().setAttributes(lp);
		dialog.show();
	}
}
