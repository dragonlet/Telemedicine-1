package comandroid.screen;


/**
 * 
 * @author huichaohua
 * @2014-3-19
 * @class description 
 */


import com.android.ecg.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TableLayout;





public class TestVedioActivity extends Activity {
	
	String ipname = null; 
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		 // 璁剧疆鍏ㄥ睆 
		 requestWindowFeature(Window.FEATURE_NO_TITLE); 
		 getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
		 setContentView(R.layout.activity_test_vedio);
		 final Builder builder = new AlertDialog.Builder(this);  
		 
		 builder.setTitle("请输入服务器的ip地址");
		
         TableLayout loginForm = (TableLayout)getLayoutInflater().inflate( R.layout.activity_test_login, null);                 
         final EditText iptext = (EditText)loginForm.findViewById(R.id.ipedittext);                                 
         builder.setView(loginForm);                             
         
         builder.setPositiveButton("确定"
                
                 , new OnClickListener() { 
                         @Override
                         public void onClick(DialogInterface dialog, int which) { 
                             
                                 ipname = iptext.getText().toString().trim(); 
                                 Bundle data = new Bundle(); 
                                 data.putString("ipname",ipname);                                         
                                 Intent intent = new Intent(TestVedioActivity.this,CameraTest.class); 
                                 intent.putExtras(data); 
                                 startActivity(intent); 
                         } 
                 }); 
       
         builder.setNegativeButton("取消"
                 ,  new OnClickListener() 
                 { 
                         @Override
                         public void onClick(DialogInterface dialog, int which) 
                         { 
                                
                                 System.exit(1); 
                         } 
                 }); 
       
         builder.create().show(); 
		 
	}
	
}
