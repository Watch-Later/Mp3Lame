package android.media;

import android.content.Context;
import android.os.Environment;
import java.io.IOException;
import java.io.InputStream;

public class TransferThread extends Thread {
	
	private TransferCallback callback;
	private Context context;
	public TransferThread(Context context, TransferCallback callback){
		this.callback = callback;
		this.context = context;
	}
	
	@Override
	public void run() {
		transfer();
	}

	/**
	 * 测试
	 */
	private void transfer(){
		String rootPath = Environment.getExternalStorageDirectory().getPath();
        String amrPath = rootPath + "/test.amr";
        try {
            InputStream pcmStream = context.getAssets().open("test.pcm");
            AmrEncoder.pcm2Amr(pcmStream, amrPath);
            callback.onSuccess();
        } catch (IOException e) {
        	callback.onFailed();
            e.printStackTrace();
        }
	}
	
	
	public interface TransferCallback{
		
		void onSuccess();
		
		void onFailed();
	}

}
