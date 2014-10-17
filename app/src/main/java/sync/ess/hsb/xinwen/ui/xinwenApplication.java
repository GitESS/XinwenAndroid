package sync.ess.hsb.xinwen.ui;

import android.annotation.TargetApi;
import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.StrictMode;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

@ReportsCrashes(socketTimeout = 5000, formKey = "", httpMethod = org.acra.sender.HttpSender.Method.PUT, reportType = org.acra.sender.HttpSender.Type.JSON, formUri = "http://acme.hemant.iriscouch.com/acra-xinwen/_design/acra-storage/_update/report", formUriBasicAuthLogin = "heman", formUriBasicAuthPassword = "password12345")
public class xinwenApplication extends Application {

	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	@SuppressWarnings("unused")
	@Override
	public void onCreate() {
		if (sync.ess.hsb.xinwen.util.Constants.Config.DEVELOPER_MODE
				&& Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
			StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
					.detectAll().penaltyDialog().build());
			StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
					.detectAll().penaltyDeath().build());
		}
		ACRA.init(this);

		super.onCreate();

		initImageLoader(getApplicationContext());
	}

	public static void initImageLoader(Context context) {
		// This configuration tuning is custom. You can tune every option, you
		// may tune some of them,
		// or you can create default configuration by
		// ImageLoaderConfiguration.createDefault(this);
		// method.
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
				context).threadPriority(Thread.NORM_PRIORITY - 2)
				.denyCacheImageMultipleSizesInMemory()
				.diskCacheFileNameGenerator(new Md5FileNameGenerator())
				.diskCacheSize(50 * 1024 * 1024)
				// 50 Mb
				.tasksProcessingOrder(QueueProcessingType.LIFO)
				.writeDebugLogs() // Remove for release app
				.build();
		// Initialize ImageLoader with configuration.
		ImageLoader.getInstance().init(config);
	}

}
