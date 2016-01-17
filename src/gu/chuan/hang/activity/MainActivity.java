package gu.chuan.hang.activity;

import gu.chuan.hang.R;
import gu.chuan.hang.adapter.AudioAdapter;
import gu.chuan.hang.bean.AudioBean;
import gu.chuan.hang.bean.ResponseBean;
import gu.chuan.hang.gson.AudioDeserializer;
import gu.chuan.hang.gson.GsonRequest;

import java.util.List;

import libcore.io.VolleySingleton;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.GridView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class MainActivity extends FragmentActivity {
	public static final String TAG = MainActivity.class.getSimpleName();

	private AudioAdapter mAdapter;
	private GridView mGridView;
	private RequestQueue mRequestQueue;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_main);
		mGridView = (GridView) findViewById(R.id.gv);
		mAdapter = new AudioAdapter(this);
		mGridView.setAdapter(mAdapter);
		mRequestQueue = VolleySingleton.getInstance(getApplicationContext())
				.getRequestQueue();
	}

	@Override
	protected void onResume() {
		super.onResume();
		loadData();
	}

	public void loadData() {
		String base_url="http://www.dingdongfm.com/DingDongFM/servlet/MobileServlet";
		Uri.Builder uriBuilder = Uri.parse(base_url).buildUpon()
				.appendQueryParameter("model", "3")
				.appendQueryParameter("ObjectName", "D_Audio")
				.appendQueryParameter("CategoryArray","10000000001")
		.appendQueryParameter("StartRow","0")
		.appendQueryParameter("EndRow","60");
		String requestUrl=uriBuilder.build().toString();
		//也可以直接使用	
		//String requestUrl = "http://www.dingdongfm.com/DingDongFM/servlet/MobileServlet?model=3&ObjectName=D_Audio&CategoryArray=10000000001&StartRow=0&EndRow=60";
		GsonRequest<ResponseBean> msgRequest =

		new GsonRequest<ResponseBean>(requestUrl,

		ResponseBean.class, null, new Response.Listener<ResponseBean>() {

			@Override
			public void onResponse(ResponseBean responseBean) {
				GsonBuilder gsonBuilder = new GsonBuilder();
				// 注册解析AudioBean.class数据的解析工具类，如果没有指定默认使用AudioBean进行解析
				// （如果AudioBean中使用了@SerializedName进行key和变量的映射，否则使用一一对应进行解析）;
				gsonBuilder.registerTypeAdapter(AudioBean.class,
						new AudioDeserializer());
				Gson gson = gsonBuilder.create();
				List<AudioBean> audioList = null;
				try {
					audioList = gson.fromJson(responseBean.getResponseObject()
							.get("D_Audio"), new TypeToken<List<AudioBean>>() {
					}.getType());
				} catch (Exception e) {
					e.printStackTrace();
				}
				mAdapter.setAudios(audioList);

			}
		},

		new ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError error) {
				System.err.println("APP request error: " + error.getCause());
				Toast.makeText(MainActivity.this, "请检查网络请求url是否可用~",
						Toast.LENGTH_LONG).show();
			}
		}){
			//通过复写该方法可以修改请求的优先级
			@Override
			public com.android.volley.Request.Priority getPriority() {
				return Priority.IMMEDIATE;
			}
		};
		// 禁用缓存，volley中有一套缓存机制，根据当前数据的生存时间是否过期来决定是否从网络上获取
		msgRequest.setShouldCache(false);
		// 给request添加tag；
		msgRequest.setTag(TAG);
		mRequestQueue.add(msgRequest);
	}
	@Override
	protected void onStop() {
		super.onStop();
		//当前页面不再可见的时候，取消request；（包括adapter中的所有的图片request）
		if (mRequestQueue != null) {
			mRequestQueue.cancelAll(TAG);
		}
	}


}
