package gu.chuan.hang.adapter;

import gu.chuan.hang.R;
import gu.chuan.hang.bean.AudioBean;
import gu.chuan.hang.tool.FileUtil;

import java.util.List;

import libcore.io.VolleySingleton;

import org.json.JSONArray;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

public class AudioAdapter extends BaseAdapter {
	public static final String TAG = AudioAdapter.class.getSimpleName();

	public AudioAdapter(Context context) {
		mNumOfCulumns = 3;
		mLayoutInflater = LayoutInflater.from(context);
				mVolleyImageLoader = VolleySingleton.getInstance(context.
						getApplicationContext()).getImageLoader();
				
				
		mMarginWidth = (int) ((FileUtil.getScreenWidth((Activity) context))*1 / 32f);
		mImageSize = (FileUtil.getScreenWidth((Activity) context) - mMarginWidth)
				/ mNumOfCulumns - mMarginWidth;
	}
	private List<AudioBean> mAudioList;
	private LayoutInflater mLayoutInflater;
	private ImageLoader mVolleyImageLoader;
	private final int mNumOfCulumns;
	private int mImageSize = 0;
	private int mMarginWidth = 0;

	public void setAudios(List<AudioBean> arr) {
		mAudioList = arr;
		notifyDataSetChanged();

	}

	public JSONArray getAudios() {
		return null;
	}

	@Override
	public int getCount() {
		return mAudioList == null ? 0 : mAudioList.size();
	}

	@Override
	public AudioBean getItem(int position) {
		return mAudioList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		H h = null;
		if (convertView == null) {
			h = new H();
			convertView = mLayoutInflater.inflate(R.layout.gv_audio_find,
					parent, false);
			h.photo = (NetworkImageView) convertView
					.findViewById(R.id.iv_photo);
			LayoutParams lp = new LayoutParams(mImageSize, mImageSize);
			h.photo.setLayoutParams(lp);
			h.name = (TextView) convertView.findViewById(R.id.tv_name);
			h.name.setPadding(mMarginWidth / 2, mMarginWidth, 0, mMarginWidth);
			convertView.setTag(h);
		} else {
			h = (H) convertView.getTag();
		}
		final AudioBean item = getItem(position);
		if (item != null) {
			final String audioName = item.getName();
			final String iconUrl = item.getIcon();
			h.name.setText(audioName);
			//NetworkImageView auto-cancels in-flight requests when detached from the view hierarchy
			//所以在这里我们不需要给NetworkImageView设置tag，取消
			h.photo.setImageUrl(iconUrl, mVolleyImageLoader);
			h.photo.setDefaultImageResId(R.drawable.default_icon);
		}
		return convertView;
	}

	private class H {
		private NetworkImageView photo;
		private TextView name;
	}

}
