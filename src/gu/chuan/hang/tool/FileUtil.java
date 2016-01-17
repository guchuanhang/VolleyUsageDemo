package gu.chuan.hang.tool;

import android.app.Activity;
import android.util.DisplayMetrics;

public class FileUtil {

	public static int getScreenWidth(Activity packageContext) {
		DisplayMetrics metrics = new DisplayMetrics();
		packageContext.getWindowManager().getDefaultDisplay()
				.getMetrics(metrics);
		return metrics.widthPixels;

	}
}