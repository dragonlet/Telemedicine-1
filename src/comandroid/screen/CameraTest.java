package comandroid.screen;

/**
 * 
 * @author huichaohua
 * @2014-3-19
 * @class description null
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import com.android.ecg.R;



import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;

@SuppressLint("NewApi")
public class CameraTest extends Activity { 
	SurfaceView sView; 
    SurfaceHolder surfaceHolder; 
    int screenWidth, screenHeight;         
    Camera camera;                    // 鐎规矮绠熺化鑽ょ埠閹�?鏁ら惃鍕弾閻╁憡婧�         
    boolean isPreview = false;        //閺勵垰鎯侀崷銊︾セ鐟欏牅鑵� 
    private String ipname; 

    @SuppressWarnings("deprecation") 
    @Override
public void onCreate(Bundle savedInstanceState) { 
    super.onCreate(savedInstanceState); 
    // 鐠佸墽鐤嗛崗銊ョ潌 
         requestWindowFeature(Window.FEATURE_NO_TITLE); 
         getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN); 
    setContentView(R.layout.activity_test_vedio); 
       
    // 閼惧嘲褰嘔P閸︽澘娼� 
    Intent intent = getIntent(); 
    Bundle data = intent.getExtras(); 
    ipname = data.getString("ipname"); 
                       
            screenWidth = 640; 
            screenHeight = 480;                 
            sView = (SurfaceView) findViewById(R.id.sView);                  // 閼惧嘲褰囬悾宀勬桨娑撶挅urfaceView缂佸嫪娆�                 
           
            surfaceHolder = sView.getHolder();                               // 閼惧嘲绶盨urfaceView閻ㄥ嚪urfaceHolder 
            
            // 娑撶皧urfaceHolder濞ｈ濮炴稉?閲滈崶鐐剁殶閻╂垵鎯夐崳?
            surfaceHolder.addCallback(new Callback() { 
                    @Override
                    public void surfaceChanged(SurfaceHolder holder, int format, int width,int height) {                                 
                    } 
                    @Override
                    public void surfaceCreated(SurfaceHolder holder) {                                                         
                            initCamera();                                            // 閹垫挸绱戦幗鍕剼婢�?
                    } 
                    @Override
                    public void surfaceDestroyed(SurfaceHolder holder) { 
                            // 婵″倹鐏塩amera娑撳秳璐焠ull ,闁插﹥鏂侀幗鍕剼婢�?
                            if (camera != null) { 
                                    if (isPreview) 
                                            camera.stopPreview(); 
                                    camera.release(); 
                                    camera = null; 
                            } 
                        System.exit(0); 
                    }                 
            }); 
            // 鐠佸墽鐤嗙拠顧檜rfaceView閼奉亜绻佹稉宥囨樊閹躲倗绱﹂崘?    
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS); 
               
} 
   
	private void initCamera() {
		if (!isPreview) {
			camera = Camera.open();
		}
		if (camera != null && !isPreview) {
			try {
				Camera.Parameters parameters = camera.getParameters();
				parameters.setPreviewSize(screenWidth, screenHeight); // 鐠佸墽鐤嗘０鍕潔閻撗呭閻ㄥ嫬銇囩亸?
				parameters.setPreviewFpsRange(20, 30); // 濮ｅ繒顫楅弰鍓с仛20~30鐢�?
				parameters.setPictureFormat(ImageFormat.NV21); // 鐠佸墽鐤嗛崶鍓у閺嶇厧绱�
				parameters.setPictureSize(screenWidth, screenHeight); // 鐠佸墽鐤嗛悡褏澧栭惃鍕亣鐏�?
				// camera.setParameters(parameters); //
				// android2.3.3娴犮儱鎮楁稉宥夋付鐟曚焦顒濈悰灞煎敩閻�?
//				camera.setDisplayOrientation(180);
				camera.setPreviewDisplay(surfaceHolder); // 闁俺绻僑urfaceView閺勫墽銇氶崣鏍ㄦ珯閻㈠娼�
				camera.setPreviewCallback(new StreamIt(ipname)); // 鐠佸墽鐤嗛崶鐐剁殶閻ㄥ嫮琚�
				camera.startPreview(); // 瀵�?顫愭０鍕潔
				camera.autoFocus(null); // 閼奉亜濮╃�靛湱鍔�
			} catch (Exception e) {
				e.printStackTrace();
			}
			isPreview = true;
		}
	} 
   
} 

class StreamIt implements Camera.PreviewCallback { 
    private String ipname; 
    public StreamIt(String ipname){ 
            this.ipname = ipname; 
    } 
       
@Override
public void onPreviewFrame(byte[] data, Camera camera) { 
    Size size = camera.getParameters().getPreviewSize();           
    try{  
            //鐠嬪啰鏁mage.compressToJpeg閿涘牞绱氱亸鍝琔V閺嶇厧绱￠崶鎯у剼閺佺増宓乨ata鏉烆兛璐焜pg閺嶇厧绱� 
        YuvImage image = new YuvImage(data, ImageFormat.NV21, size.width, size.height, null);   
        if(image!=null){ 
                ByteArrayOutputStream outstream = new ByteArrayOutputStream(); 
            image.compressToJpeg(new Rect(0, 0, size.width, size.height), 80, outstream);  
            outstream.flush(); 
            //閸氼垳鏁ょ痪璺ㄢ柤鐏忓棗娴橀崓蹇旀殶閹诡喖褰傞柅浣稿毉閸�?
            Thread th = new MyThread(outstream,ipname); 
            th.start();                
        }   
    }catch(Exception ex){   
        Log.e("Sys","Error:"+ex.getMessage());   
    }         
} 
} 
   
class MyThread extends Thread{         
    private byte byteBuffer[] = new byte[1024]; 
    private OutputStream outsocket;         
    private ByteArrayOutputStream myoutputstream; 
    private String ipname; 
       
    public MyThread(ByteArrayOutputStream myoutputstream,String ipname){ 
            this.myoutputstream = myoutputstream; 
            this.ipname = ipname; 
    try { 
                    myoutputstream.close(); 
            } catch (IOException e) { 
                    e.printStackTrace(); 
            } 
    } 
       
public void run() { 
    try{ 
            //鐏忓棗娴橀崓蹇旀殶閹诡噣?鏉╁槩ocket閸欐垿?閸戝搫骞� 
        Socket tempSocket = new Socket(ipname, 6000); 
        outsocket = tempSocket.getOutputStream(); 
        ByteArrayInputStream inputstream = new ByteArrayInputStream(myoutputstream.toByteArray()); 
        int amount; 
        while ((amount = inputstream.read(byteBuffer)) != -1) { 
            outsocket.write(byteBuffer, 0, amount); 
        } 
        myoutputstream.flush(); 
        myoutputstream.close(); 
        tempSocket.close();                    
    } catch (IOException e) { 
        e.printStackTrace(); 
    } 
} 

} 
