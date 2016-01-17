package libcore.io;

import gu.chuan.hang.tool.HttpUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.v4.util.LruCache;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

public class VolleySingleton {
	private static VolleySingleton mInstance;
	private RequestQueue mRequestQueue;
	private ImageLoader mImageLoader;
	private static Context mAppContext;
	private static DiskLruCache mDiskLruCache = null;
	private static boolean mDiskInitFinshed = false;
	//这里使用lock而不是使用synchronize，毫无以为会导致某些图片不能保存到sd卡和从网络上获取某个本地已经换成的图片的冗余
	//但是，这让会让用户感到更为流畅、交互性更好
	Lock mDiskCacheLock = new ReentrantLock();
	//给图片sd卡缓存，分配的空间大小
	private static final int DISK_CACHE_SIZE = 1024 * 1024 * 10;
	private static final String DISK_CACHE_SUBDIR = "thumbnails_gch";

	private VolleySingleton(Context context) {
		mAppContext = context;
		mRequestQueue = getRequestQueue();
		//这里最大使用使用系统为APP分配的内存的1/8的空间缓存图片
		final int maxMemory = (int) (Runtime.getRuntime().maxMemory()/1024);
		final int cacheSize = maxMemory / 8;
		mImageLoader = new ImageLoader(mRequestQueue,
				new ImageLoader.ImageCache() {

					private final LruCache<String, Bitmap> cache = new LruCache<String, Bitmap>(
							cacheSize){
						@Override
						protected int sizeOf(String key, Bitmap value) {
							return value.getRowBytes() * value.getHeight() / 1024+1;
						}
					};
					@Override
					public Bitmap getBitmap(String url) {
						Bitmap bitmap = cache.get(url);
						//(这也是个缺憾吧，从UI线程中获取sd卡上的图片)
						if (bitmap == null) {
							bitmap = getBitmapFromDiskCache(url);
						}
						return bitmap;
					}

					@Override
					public void putBitmap(String url, Bitmap bitmap) {
						cache.put(url, bitmap);
						// 在工作线程中，将图片保存到SD卡,只有当前联网，才有必要将图片保存一下
						if (HttpUtil.isNetworkConnected(mAppContext)) {
							SaveBitmap2Sisk saveBitmap2DiskTask = new SaveBitmap2Sisk(
									url, bitmap);
							//这里使用一个仅仅只有一个线程的线程池来保存图片（PS：频繁的创建、销毁线程会影响APP的性能）
							Executors.newSingleThreadExecutor().execute(
									saveBitmap2DiskTask);
						} else {
							// 如果当前没有网络，就不再进行保存了
						}

					}

				});
		File cacheDir = getDiskCacheDir(mAppContext, DISK_CACHE_SUBDIR);
		new InitDiskCacheTask().execute(cacheDir);

	}

	public Bitmap getBitmapFromDiskCache(String imageUrl) {
		if (mDiskInitFinshed) {
			if (mDiskCacheLock.tryLock()) {
				try {
					// manipulate protected state
					if (mDiskLruCache != null) {
						DiskLruCache.Snapshot snapShot = null;
						try {
							snapShot = mDiskLruCache.get(MD5Utils
									.hashKeyForDisk(imageUrl));
						} catch (IOException e) {
							e.printStackTrace();
						}
						if (snapShot != null) {
							InputStream is = snapShot.getInputStream(0);
							Bitmap bitmap = BitmapFactory.decodeStream(is);
							return bitmap;
						}
					}

				} finally {
					mDiskCacheLock.unlock();
				}
			} else {
				// ignore
			}
		}
		return null;
	}

	class SaveBitmap2Sisk extends Thread {
		String imageUrl;
		Bitmap bitmap;

		public SaveBitmap2Sisk(String imageUrl, Bitmap bitmap) {
			this.imageUrl = imageUrl;
			this.bitmap = bitmap;
		}

		@Override
		public void run() {
			putBitmap2DiskCache(imageUrl, bitmap);
		}

	}

	public void putBitmap2DiskCache(String imageUrl, Bitmap bitmap) {
		if (mDiskInitFinshed) {
			if (mDiskCacheLock.tryLock()) {
				try {
					if (mDiskLruCache != null
							&& mDiskLruCache.get(MD5Utils
									.hashKeyForDisk(imageUrl)) == null) {
						DiskLruCache.Editor editor = mDiskLruCache
								.edit(MD5Utils.hashKeyForDisk(imageUrl));
						if (editor != null) {
							OutputStream outputStream = editor
									.newOutputStream(0);
							if (bitmap.compress(CompressFormat.PNG, 100,
									outputStream)) {
								editor.commit();
							} else {
								editor.abort();
							}
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					mDiskCacheLock.unlock();
				}
			} else {
				// ignore
			}
		}

	}

	class InitDiskCacheTask extends AsyncTask<File, Void, Void> {
		@Override
		protected Void doInBackground(File... params) {
			mDiskCacheLock.lock();
			File cacheDir = params[0];
			try {
				mDiskLruCache = DiskLruCache.open(cacheDir,
						getAppVersion(mAppContext), 1, DISK_CACHE_SIZE);
				mDiskInitFinshed = true;
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				mDiskCacheLock.unlock();
			}
			return null;

		}
	}

	@SuppressLint("NewApi")
	private File getDiskCacheDir(Context context, String uniqueName) {
		String cachePath = null;
		if (Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState())) {
			File sdFile = context.getExternalCacheDir();
			// 当文件浏览器打开这个位置的时候，会返回null；
			if (sdFile == null) {
				cachePath = context.getCacheDir().getPath();
			} else {
				cachePath = context.getExternalCacheDir().getPath();
			}
		} else {
			if (Build.VERSION.SDK_INT >= 9) {
				if (!Environment.isExternalStorageRemovable()) {
					File sdFile = context.getExternalCacheDir();
					if (sdFile == null) {
						cachePath = context.getCacheDir().getPath();
					} else {
						cachePath = context.getExternalCacheDir().getPath();
					}
				} else {
					cachePath = context.getCacheDir().getPath();
				}
			}

		}
		return new File(cachePath + File.separator + uniqueName);
	}

	private int getAppVersion(Context context) {
		try {
			PackageInfo info = context.getPackageManager().getPackageInfo(
					context.getPackageName(), 0);
			return info.versionCode;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return 1;
	}

	public static synchronized VolleySingleton getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new VolleySingleton(context);
		}
		return mInstance;
	}

	public RequestQueue getRequestQueue() {
		if (mRequestQueue == null) {
			//底层使用okHttp进行数据传输
			mRequestQueue = Volley.newRequestQueue(mAppContext,
					new OkHttpStack());
			//当然了，你也可以选择不使用okHttp，并且高版本的okHttp已经不能提供这个服务了
//			 mRequestQueue = Volley.newRequestQueue(mAppContext);
		}
		return mRequestQueue;
	}

	public <T> void addToRequestQueue(Request<T> req) {
		getRequestQueue().add(req);
	}

	public ImageLoader getImageLoader() {
		return mImageLoader;
	}

}