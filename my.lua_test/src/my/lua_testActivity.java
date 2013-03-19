package my;

import my.lua_test.R;                 
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import org.keplerproject.luajava.*;
import android.widget.TextView;
import android.util.Log;
import android.view.SurfaceView;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.Button;
import android.view.WindowManager;
import android.view.Window; 
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Uri;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;


import android.content.DialogInterface; 
import android.app.AlertDialog;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.media.AudioManager;
import android.media.MediaRecorder; 
import android.media.CamcorderProfile;
import android.hardware.Camera.AutoFocusCallback;
import android.content.res.Resources;
import android.content.res.Configuration;
import android.media.MediaPlayer;



//2012.05.26 下午3點 版

//  

public class lua_testActivity extends Activity  implements  SurfaceHolder.Callback   {
    /** Called when the activity is first created. */	
	
	public LuaState L = null;//  LuaStateFactory.newLuaState();
	
	public LinkedList<String> screen_text = new LinkedList<String>();
	public String screen_txt = "";
	public String lua_file = "";
	public String [] list ;
	
	public Camera camera ; 
	public SurfaceView surfaceView;
	public SurfaceHolder surfaceHolder;
	
	
	
	public SurfaceView surfaceView2;
	public SurfaceHolder surfaceHolder2;
	
	public Camera.Parameters parameters ;
	
	private static final int UPDATE_SETTING_SUCCESS = 0x0001;   
	
	Object semaphore = new Object();
	
	public screenHandler  screen_handler = new screenHandler(); 
	public dec_screen_light_Handler dec_screen_light_handler = new dec_screen_light_Handler();
	
	public MediaRecorder recorder = new  MediaRecorder();
	public MediaRecorder arecorder = new  MediaRecorder();
	
	public boolean is_video = false;
	public boolean is_script_running = false;
	public boolean is_audio = false;
	
   public int audio_ch=2;
   public int	audio_bitrate=256000;
   public int	audio_samplerate=48000;
   public int	video_bitrate=36000000;
   public int	video_fps=30;
   public int	video_width=1280;
   public int	video_height=720;
   public int vaudio_ch=2;
   public int	vaudio_bitrate=192000;
   public int	vaudio_samplerate=48000;
   
   //public boolean update_finish = true;
   public MediaPlayer player = new  MediaPlayer ();
   
   public MediaPlayer shutter_trigger = new  MediaPlayer ();
   
   public String  battery_level = "%";
   
   public SensorManager sensormanager;
   public List <Sensor>  sensors = null;
   
   //
   
   public float x=320 , y=240 , z ;
   public int curWidth,curHeight;
   public Bitmap bitmap1 , bitmap2 , curBitmap ;
   
   //
   
   
   private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver(){   
	    @Override  
	    public void onReceive(Context arg0, Intent intent) {   
	      // TODO Auto-generated method stub   
	      int level = intent.getIntExtra("level", 0);
	      battery_level =  String.valueOf(level) + "%";   
	                                                                                                                   
	     ( (TextView)findViewById( R.id.inf_screen )  ).setText("電力 "+level+"%");
	      
	    }   
	  };   
   
   
  
   Resources res ;
   Configuration conf;
	
    @Override
    public void onCreate(Bundle savedInstanceState) { 
    	
    	//System.loadLibrary("luajava-1.1");
    	
    	

    	
    	//設定橫向螢幕
    	this.setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE  );
    	
		this.getWindow().setFlags ( 
				WindowManager.LayoutParams.FLAG_FULLSCREEN, 
        		WindowManager.LayoutParams.FLAG_FULLSCREEN
        		);
		
		this.getWindow().setFlags(
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, 
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
				);
		
        this.requestWindowFeature( Window.FEATURE_NO_TITLE );
        
        this.registerReceiver(this.mBatInfoReceiver,    
        	    new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
       
        L = LuaStateFactory.newLuaState();
		L.pushJavaObject(this);
		L.setGlobal("activity");
		
	   res = getResources();
       conf = res.getConfiguration();

		conf.locale = Locale.TAIWAN;
		res.updateConfiguration(conf, null);

        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
            
        appobject = this.getApplicationContext();
      
      surfaceView = (SurfaceView)findViewById(R.id.surfaceView1);
       surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS); 
        
      
        
  
////////////////////////////////////////////////////////////////////////////////////////////        
        

        /*bitmap2 = BitmapFactory.decodeResource(  getResources()    , R.raw.line  );
        
        surfaceView2 = (SurfaceView)findViewById(R.id.surface_level );
        surfaceHolder2 = surfaceView2.getHolder();
        
        SurfaceHolder.Callback  level_callback  = new SurfaceHolder.Callback()
        {

			public void surfaceChanged(SurfaceHolder holder, int format,
					int width, int height) {
				//需要
				 on_level_change(new float[3]);
			}

			public void surfaceCreated(SurfaceHolder holder) {
			}

			public void surfaceDestroyed(SurfaceHolder holder) {
			}
        	
        };

		surfaceHolder2.addCallback(  level_callback  );
        surfaceHolder2.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);  */
        

        
//////////////////////////////////////////////////////////////////////////////////////////        
        
      /*  sensormanager = (SensorManager) getSystemService(SENSOR_SERVICE);        
        sensors =  sensormanager.getSensorList(Sensor.TYPE_ORIENTATION);

        SensorEventListener sl =new SensorEventListener ()
        {
			public void onAccuracyChanged(Sensor sensor, int accuracy) {
				//不需要
			}
			public void onSensorChanged(SensorEvent event) {
				 on_level_change(event.values);
			}
        
        };
        sensormanager.registerListener  (   sl   ,  sensors.get(0) ,  SensorManager.SENSOR_DELAY_GAME  );
        */
        
    /////////////////////////////////////////////////////////////////////
        
        
        Button run_script = (Button)this.findViewById( R.id.run_script  );
        run_script.setOnClickListener(
        		new  Button.OnClickListener(){
        			public void onClick(View v) {
        				
        				if(  is_video == true  ){
        					print_screen(  res.getString(R.string.video_rec_ing )  );
        					return;
        				}
        				
        				if(  is_audio == true  ){
        					print_screen(res.getString(R.string.audio_rec_ing )    );
        					return;
        				}
        				
        				if( lua_file == "" ){
        					print_screen(res.getString(R.string.file_load_no ));
        					return;
        				}
        			  
        				File f = new File( "/sdcard/ez_Lua_Script_Camera/lua_scripts/"+ lua_file);
        				if(  f.exists() == false  ){
        					print_screen(  res.getString(R.string.file_load_fail )  );
        					return;
        				}        				
        				 ( (TextView)findViewById( R.id.lua_file_screen)  ).setText(lua_file);
        				 all_pics=0;
        				 ( (TextView)findViewById( R.id.shutter_times_screen  )  ).setText("拍攝張數 : 00000"   );
        				(new lua_run()).start(); 
        			}
        		}
        );
        
        Button about = (Button)this.findViewById( R.id.about );
        about.setOnClickListener(
        		new  Button.OnClickListener(){
        			public void onClick(View v) {
        				
        				if(  is_video == true  ){
        					print_screen(  res.getString(R.string.video_rec_ing )    );
        					return;
        				}
        				
        				if(  is_audio == true  ){
        					print_screen(res.getString(R.string.audio_rec_ing )  );
        					return;
        				}

        				if(  is_script_running == true  ){
        					print_screen(res.getString(R.string.script_running )  );
        					return;
        				}
        				
        				Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("http://dl.dropbox.com/u/61164954/homepage/ez_LSC/index.htm")); 
        		        startActivity(i); 
        			}
        		}
        );
        
        Button audio_c = (Button)this.findViewById( R.id.audio  );
        audio_c.setOnClickListener(
        		new  Button.OnClickListener(){
        			public void onClick(View v) {
        				
        				if(  is_script_running == true  ) {
        					print_screen(res.getString(R.string.script_running )  );
        					return;
        				}
        				
        				if(  is_video == true  ){
        					print_screen(res.getString(R.string.video_rec_ing )  );
        					return;
        				}
        			
        				if( is_audio == false ){
        					print_screen(res.getString(R.string.audio_rec_start ) );
        					CharSequence cs =  res.getString(R.string.stop ); 
        					 Button keepa = (Button)findViewById( R.id.audio  );
        					keepa.setText(cs);
        					is_audio = true;
        					(new audio()).start(); 
        				}else{
        					arecorder.stop();
        					is_audio = false;
        					print_screen(res.getString(R.string.audio_rec_end));
        					CharSequence xcs = res.getString(R.string.audio_rec ); 
       					    Button keepax = (Button)findViewById( R.id.audio );
       				     	keepax.setText(xcs);
       				     	camera.startPreview();
        				}
        			}
        		}
        );

        Button movie = (Button)this.findViewById( R.id.movie );
        movie.setOnClickListener(
        		new  Button.OnClickListener(){
        			public void onClick(View v) {
        				
        				if(  is_script_running == true  ){
        					print_screen(res.getString(R.string.script_running )  );
        					return;
        				}

        				if(  is_audio == true  ){
        					print_screen(res.getString(R.string.audio_rec_ing )  );
        					return;
        				}
        	
        				if( is_video == false ){
        					print_screen(res.getString(R.string.video_rec_start ));
        					CharSequence cs = res.getString(R.string.stop ) ; 
        					 Button keepa = (Button)findViewById( R.id.movie );
        					keepa.setText(cs);				
        					parameters.setFocusMode("continuous-video");	
        					camera.setParameters( parameters );
        					is_video = true;
        					(new video()).start(); 
        				}else{
        					recorder.stop();
        					is_video = false;
        					print_screen(res.getString(R.string.video_rec_end ));
        					CharSequence xcs = res.getString(R.string.video_rec) ; 
       					    Button keepax = (Button)findViewById( R.id.movie );
       				     	keepax.setText(xcs);
       				     	camera.lock();
       				        camera.release();
       				        camera = Camera.open();
       						parameters = camera.getParameters();
       						parameters.setPreviewSize(640, 480);
       						parameters.setPreviewFrameRate(30);
       						parameters.setJpegQuality(100);
       						parameters.setFocusMode("continuous-video");
       						parameters.setPictureSize(2048, 1536);
       				        camera.setParameters( parameters );
       				        try{
       				        	camera.setPreviewDisplay(     surfaceHolder  ); 
       				        	}catch( IOException e){
       				        	}
       				     	camera.startPreview();
        				}
        			}
        		}
        );
        
        Button lua = (Button)this.findViewById( R.id.lua );
        lua.setOnClickListener(
        		new  Button.OnClickListener(){
        			public void onClick(View v) {
        				
        				if(  is_script_running == true  ){
        					print_screen(res.getString(R.string.script_running )  );
        					return;
        				}
        				
        				if(  is_audio == true  ){
        					print_screen(res.getString(R.string.audio_rec_ing )  );
        					return;
        				}
        				
        				if(  is_video == true  ){
        					print_screen( res.getString(R.string.video_rec_ing )   );
        					return;
        				}
        				show_choose_Dialog();
        			}
        		}
        );
        
      test_caller();
      load_config();

			        try {
			  		player.setDataSource( "/sdcard/ez_Lua_Script_Camera/sound/shuttersound.ogg" );
			  	} catch (IllegalArgumentException e) {
			  		// TODO Auto-generated catch block
			  		e.printStackTrace();
			  	} catch (IllegalStateException e) {
			  		// TODO Auto-generated catch block
			  		e.printStackTrace();
			  	} catch (IOException e) {
			  		// TODO Auto-generated catch block
			  		e.printStackTrace();
			  	}
			        
			        try {
						shutter_trigger.setDataSource("/sdcard/ez_Lua_Script_Camera/sound/trigger.wav");
					} catch (IllegalArgumentException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IllegalStateException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			        
			        
			        
        
    }
    
    
    
    
    public void  on_level_change (  float[] values  )
    {
    	
    	//float [] v = new float[3];
    	
    	
    	//boolean b = SensorManager.

    	
    	
    	//Sensor  tmp = sensormanager.getSensorList(Sensor.TYPE_MAGNETIC_FIELD).get(0);
    	
    	//SensorManager.remapCoordinateSystem(R,.remapCoordinateSystem(R, SensorManager.AXIS_X, SensorManager.AXIS_Z, outR);

    	
    	
    	( (TextView)findViewById( R.id.shutter_times_screen  )  ).setText(    String.format( "%.5f" , values[0])        );
    	
    	
    	//Canvas canvas = surfaceHolder2.lockCanvas();
    	//if(canvas != null)
    	{
    		//Paint paint = new Paint();
    		//paint .setAntiAlias(true);
    		//Matrix matrix = new Matrix();
    		//curWidth = bitmap2.getWidth() ;
    		//curHeight = bitmap2.getHeight();
    		
    		
    		//canvas.save();
    		

    		
    		//matrix.setRotate(-values[0],x,y);
    		//canvas.setMatrix(matrix);
    		//canvas.drawBitmap(bitmap2,  curWidth /2   ,   curHeight/2  , null );
    		
    		//canvas.restore();
    		 //surfaceHolder2.unlockCanvasAndPost(canvas);
    		
    	}
    	
    	//surfaceHolder2.unlockCanvasAndPost(canvas);
        
    }

    
    
    public void get_inf()
    {
    	//max_width = 3264;
    	//max_height =2448;
    	
    	//return;
    	
    	
    	//String allp = parameters.flatten();	
    	//String [] all_s = all.split(";");
    	//Map<String, String> map =   new HashMap<String, String>();
    	
    	//for(String  i : all_s) 
    		//map.put( i.split("=")[0]    ,  i.split("=")[1] );
        
    	//max_res_str = map.get("picture-size-values").split(",")[0];
    	
    	//max_width = Integer.parseInt( max_res_str.split("x")[0] );
    	//max_height = Integer.parseInt( max_res_str.split("x")[1] );
    	
    
    }

    
   //// /////設定檔載入
    
    public void load_config(){
    	BufferedReader in;    	
		try {
			
			String str;
			 in  = new BufferedReader(new FileReader("/sdcard/ez_Lua_Script_Camera/config/config.ini"));
			 try {
				 str = in.readLine() ;
				 audio_ch=   Integer.parseInt(str.split("=")[1]) ; 
				 str = in.readLine() ;
				  audio_bitrate=   Integer.parseInt(str.split("=")[1]) * 1000 ;  
				  str = in.readLine() ;
				  audio_samplerate=   Integer.parseInt(str.split("=")[1]) ;
				   str = in.readLine() ;
				   video_bitrate = Integer.parseInt(str.split("=")[1]) ; 
				   str = in.readLine() ;
				   video_fps=   Integer.parseInt(str.split("=")[1]) ; 
				  str = in.readLine() ;
				  video_width=   Integer.parseInt(str.split("=")[1]) ;
				  str = in.readLine() ;
				  video_height=   Integer.parseInt(str.split("=")[1]) ;
				 str = in.readLine() ;
				vaudio_ch=   Integer.parseInt(str.split("=")[1]) ;
				str = in.readLine() ;
				 vaudio_bitrate=   Integer.parseInt(str.split("=")[1]) * 1000 ;  
				 str = in.readLine() ;
				  vaudio_samplerate=   Integer.parseInt(str.split("=")[1]) ;
			} catch (IOException e) {
			}
		} catch (FileNotFoundException e) {
		}
    }
    //////////
	private class video extends Thread { 
	    @Override 
	    public void run() { 
	    	camera.stopPreview();
		    camera.unlock(); 
		    recorder = new MediaRecorder();  
		    recorder.setCamera(camera); 		 		    
		    recorder.setPreviewDisplay(surfaceHolder.getSurface()); 
		    recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA); 
		    recorder.setAudioSource(MediaRecorder.AudioSource.MIC);  
		    recorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH)); 
		    recorder.setPreviewDisplay(surfaceHolder.getSurface()); 
		   recorder.setAudioChannels(vaudio_ch);
		   recorder.setAudioSamplingRate(vaudio_samplerate);
		   recorder.setAudioEncodingBitRate(vaudio_bitrate);
		    recorder.setVideoEncodingBitRate( video_bitrate);
		    recorder.setVideoSize(video_width, video_height);
		    recorder.setVideoFrameRate(video_fps);
			long time = System.currentTimeMillis();
	        Date date = new Date(time);
	        SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMddhhmmssSSS");
		    recorder.setOutputFile("/sdcard/ez_Lua_Script_Camera/VIDEO/" + sdf.format(date)    +".mp4"); 
		   try{
		     recorder.prepare(); 
		    }catch( IOException e ){
		    }
		    recorder.start(); 
	    } 
	}	   
	
	//// audio
	private class audio extends Thread { 
	    @Override 
	    public void run() { 
            camera.stopPreview();
		    arecorder = new MediaRecorder();     
		    arecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		    arecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4 );
		    arecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC );
		    arecorder.setAudioChannels(audio_ch);
		    arecorder.setAudioSamplingRate(vaudio_samplerate);
		    arecorder.setAudioEncodingBitRate(audio_bitrate);
			long time = System.currentTimeMillis();
	        Date date = new Date(time);
	        SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMddhhmmssSSS");
		    arecorder.setOutputFile("/sdcard/ez_Lua_Script_Camera/AUDIO/" +  sdf.format(date)  +".mp4"); 
		   try{
		     arecorder.prepare(); 
		    }catch( IOException e ){
		    }
		    arecorder.start(); 
	    } 
	}	   
    public void show_choose_Dialog()
    {
		File file=new File("/sdcard/ez_Lua_Script_Camera/lua_scripts/");
		File[]  files = file.listFiles();
		 list = new String [ files.length];
		for(int i=0 ;   i< files.length ; i++  ) 
			list[i] = files[i].getName();
		
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(res.getString(R.string.choose_lua ));
        builder.setItems(   list  , new DialogInterface.OnClickListener(){

        	public void onClick (DialogInterface dialog, int which){
        		lua_file = list[which];
        	   print_screen(  res.getString(R.string.load )  +"\"" + list[which]+"\"" + res.getString(R.string.file )  );
        	}
        });
        builder.create().show();
     }

    public void test_caller(){
    	print_screen (res.getString(R.string.welcome )) ;	
    }
    
    //需要改進
    public String get_screen_txt (String tmp){	
    	String [] lines = tmp.split("\n") ;
    	for( int i=lines.length-1 ; i>=0 ; i--  ) screen_text.addFirst(lines[i]+"\n");
    	while (screen_text.size()>15) screen_text.removeLast();
    	String[] a = new String[screen_text.size()];
    	tmp = "";
    	for(String element : screen_text.toArray(a)) {
    		tmp += element;
    	}	
    	//update_finish = true;
    	return  tmp ;
    }
    
      public void message_inf( int th , String msg  ){
    	  print_screen(  String.format(msg, th)     );	  
      }
      
      public void message_inf_long( double th , String msg  ){
    	  print_screen(  String.format(msg, th)     );	  
      }
      
      public void message_inf_battery (  String msg  ){
    	  print_screen(  String.format(  msg    ,  battery_level   )     );	  
      }
    
	   public void onBackPressed() { 
		      this.finish(); 
		      return; 
		   } 
	   
		private class lua_run extends Thread { 
		    @Override 
		    public void run() { 
		    	if(   is_script_running == false  ){
		    		is_script_running = true;
		    		parameters.setFocusMode("auto");	
		    		camera.setParameters( parameters );
		    		//update_finish = false;
		    		print_screen ( lua_file + res.getString(R.string.run_start ) );    	
		    		
		    		//
		    		
		    		L.LdoFile("/sdcard/ez_Lua_Script_Camera/lua_scripts/"+ lua_file);
		    		

		    		//
		    		
		    		
		    		print_screen ( lua_file + res.getString(R.string.run_end ) );
		    		//update_finish = true;
		    		//不管script執行怎樣的設定,離開後初始清空
		    		parameters.setFocusMode("continuous-video");	
		    		camera.setParameters( parameters );
		    		camera.startPreview();
		    		is_script_running = false;
		    	}else{
		    		print_screen ( res.getString(R.string.script_running )  );
		    	}
		    } 
		}	   
		
		public void msleep ( int a  ) {
				try {
					Thread.sleep(a);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
		}
			
		public void take_picture () throws InterruptedException{
			camera.takePicture(  shutter_callback  , null , null , jpegCallback ) ; 

                synchronized(semaphore) {
                  semaphore.wait();
                }
   
			camera.startPreview();
		}
		

		
		//lua似乎無法處理long型態 因此使用double來放置長整數
		public  double  get_currentTime() {
			long tmp =  System.currentTimeMillis() ;
			//tmp  -=  40*365*24*60*60*1000 ;
			//tmp /= 100;
			return   (double) tmp;
		}
		
		/////////////////////////////////////////
		private Camera.ShutterCallback  shutter_callback = new Camera.ShutterCallback() {
		
		public void onShutter() {
			// TODO Auto-generated method stub
			
			AudioManager mgr = (AudioManager)getSystemService(Context.AUDIO_SERVICE); 
			mgr.setStreamMute(AudioManager.STREAM_SYSTEM, true); 

			
			
			
			try {
				player.prepare();
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			player.start();
			
			
		}
	}; 
		
 ///////////////////////////////////////////
	
	public int all_pics = 0 ;
	
	
	private Camera.PreviewCallback previewCallback = new  Camera.PreviewCallback() {
		
		public void onPreviewFrame(byte[] data, Camera camera) {
			// TODO Auto-generated method stub
			 //print_screen("x");
			
			Log.i("map", "Image Format: " ); 
			
		}
	};
		    
	private Camera.PictureCallback jpegCallback = new Camera.PictureCallback(){
		public void onPictureTaken  (byte[] data, Camera camera) {
			
			long time = System.currentTimeMillis();
	        Date date = new Date(time);
	        SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMddhhmmssSSS");
			
	        //( (TextView)findViewById( R.id.save_file  )  ).setText("檔案寫入 : " +   sdf.format(date) +".jpg 中..."    );
	        
	        //( (TextView)findViewById( R.id.save_file  )  ).requestLayout();
	        
	        try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	        
			String path = "/sdcard/ez_Lua_Script_Camera/DCIM/"+  sdf.format(date)    +".jpg";
			File file = new File(path);			
			try{
				file.createNewFile();
				OutputStream os2 = new BufferedOutputStream(new FileOutputStream(file),8192);
				os2.write(data);
				os2.close();
				
				( (TextView)findViewById( R.id.save_file  )  ).setText("檔案寫入 : " +   sdf.format(date) +".jpg 完成"    );
				
				all_pics++;
				
				 ( (TextView)findViewById( R.id.shutter_times_screen  )  ).setText("拍攝張數 : " +  String.format( "%05d" , all_pics)    );
				
				
			} catch ( IOException e ){
			}	
	       synchronized(semaphore) {
		           semaphore.notify();
		         }
		}
	};
	
	public void get_all_camera_param(){
		String all = parameters.flatten();	
		String [] all_s = all.split(";");
		String all_line = "";
		
		for ( String i : all_s  ){
			all_line =   all_line  +"\n" + i;
		}
		try{
			BufferedWriter out = new BufferedWriter(new FileWriter("/sdcard/ez_Lua_Script_Camera/camera_parameters.txt"));
			out.write(  res.getString(R.string.creator ) +"\n" );
			out.write( all_line  );
			out.close();
			} 
			catch (IOException e) { 
			}
	}
	
    public void autofocus(   ) throws InterruptedException{
    	camera.autoFocus(af);
            synchronized(semaphore) {
              semaphore.wait();
            }
    }
    
    AutoFocusCallback  af = new AutoFocusCallback(){    
    	
    	public void onAutoFocus(boolean arg0, Camera arg1) { 
    	
    		
    		
    		if(arg0  == true )
    			print_screen( res.getString(R.string.af_ok )  );
    		else
    			print_screen( res.getString(R.string.af_fail )  );
    		
    		synchronized(semaphore) {
	           semaphore.notify();
	           }
 	       }
    	};
    
    public void set_camera( String item , String param    ){
    	parameters.set(  item  , param ); 
    	camera.setParameters(parameters);
    }
    
    public void set_camera( String item , int param    ){
    	parameters.set(  item  , param ); 
    	camera.setParameters(parameters);
    }
    
    public void set_camera_recover(     ){
		parameters.setExposureCompensation(0);
		parameters.set("scene-mode-values","auto");
		camera.setParameters(parameters);
    }
   
    public void start_preview( ){
		camera.startPreview();
    }
    
    public void stop_preview( ){
		camera.stopPreview();
    }
    
    
    
   public class callback_level_class
   {
	   
	   public void surfaceCreated(SurfaceHolder holder) {
	   }
	   
	   public void surfaceDestroyed(SurfaceHolder holder) {
		   
	   }
	   
   
   }
    
    
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {		
		if( is_video == false  )
			camera.startPreview(); //錄影時候需要關閉
	}
	
	
	public void  exter_shutter_trigger ()
	{
			try {
				shutter_trigger.prepare();
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			shutter_trigger.start();
			
			
	}
    
	
	public void  exter_shutter_trigger_sec  (double s  ) throws InterruptedException
	{
			try {
				

				
				shutter_trigger.prepare();
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			int x =  (int) ((s * 1000.0) / 100.0)  ;
			for (  int i=0 ; i<x ; i ++   )
			{
				//activity:message_inf(i,"第%03d張拍攝完成");
				shutter_trigger.start();
				
				while ( shutter_trigger.isPlaying() )
						Thread.sleep(1);
				
				
				
			}
			
			
	}
	
	
    
	public void surfaceCreated(SurfaceHolder holder) {
		camera = Camera.open();
		parameters = camera.getParameters();

		parameters.setPreviewSize(640, 480);
		parameters.setPreviewFrameRate(30);
		parameters.setJpegQuality(100);
		parameters.setFocusMode("continuous-video");
		parameters.setPictureSize( 2048 , 1536 );
		camera.setParameters( parameters ); ///!!!!! 
		try{
			camera.setPreviewDisplay(holder);

		} catch (IOException exception){
			camera.release();
			camera = null;
		}
	}
    
	public void surfaceDestroyed(SurfaceHolder holder) {
		
		if(  is_audio == true  )
			arecorder.stop();
		if(  is_video == true  )
			recorder.stop();
		
		camera.stopPreview();
		camera.release();
		camera = null ;
	}
	

	
    public void print_screen(String tmp) {
    	screen_txt = tmp;
    	Message m = new Message();   
    	m.what = UPDATE_SETTING_SUCCESS; 
    	screen_handler.sendMessage(m);  
    	
    	//搞不定的同步問題,先關閉
    	/*while( update_finish != true  )
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
			}*/
    	//update_finish =false;
    	msleep(40);
    }
    
    
    public float light = 100;
    public void set_screen_light(float light_value){
    	light = light_value;
    	Message m = new Message();   
    	m.what = UPDATE_SETTING_SUCCESS; 
    	dec_screen_light_handler.sendMessage(m);
    }
    
    public void  set_screen_light_header(){
    	WindowManager.LayoutParams lp = getWindow().getAttributes(); 
    	lp.screenBrightness = light / 100.0f; 
    	getWindow().setAttributes(lp); 

    }
    
    /////////////////  
	
  	class   dec_screen_light_Handler extends Handler {   
  	      @Override  
  	      public void handleMessage(Message msg) {   
  	          switch (msg.what) {   
  	         case UPDATE_SETTING_SUCCESS:   
  	   		
  	        	 set_screen_light_header();
  	         	
  	             break;   
  	         }          
  	       super.handleMessage(msg);   
  	    }   
  	 }  
    ////////////////////  
    
    
    
  /////////////////  
	
	class screenHandler extends Handler {   
	      @Override  
	      public void handleMessage(Message msg) {   
	          switch (msg.what) {   
	         case UPDATE_SETTING_SUCCESS:   
	         	TextView debug_screen = (TextView)findViewById( R.id.debug_screen );
	         	
	         	debug_screen.setText( get_screen_txt ( screen_txt  ) );		         	
	         	//update_finish = true;
	         	
	             break;   
	         }          
	       super.handleMessage(msg);   
	    }   
	 }  
  ////////////////////     
	
	
	/// 2g/3g
	
	public boolean enable_3g = true;
	public set3gHandler  set3g_handler = new set3gHandler ();
	
	public void  set_3g_enable (boolean enable)
	{
	        enable_3g = enable;
	    	Message m = new Message();   
	    	m.what = UPDATE_SETTING_SUCCESS; 
	    	set3g_handler .sendMessage(m);  
	    	
       	 try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	      
	}
	
	class set3gHandler extends Handler {   
	      @Override  
	      public void handleMessage(Message msg) {   
	          switch (msg.what) {   
	         case UPDATE_SETTING_SUCCESS:   
	        	 setMobileDataEnabled();
	        	 if ( enable_3g == true)
	        		 print_screen("已開啟2G/3G數據傳輸");
	        	 else
	        		 print_screen("已關閉2G/3G數據傳輸");

	             break;   
	         }          
	       super.handleMessage(msg);   
	    }   
	 }  
	
	
	private void setMobileDataEnabled() {  
		
		boolean dataConnection ;
		dataConnection  = enable_3g;
	     try {  
	       final ConnectivityManager conman = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);  
	       final Class conmanClass = Class.forName(conman.getClass().getName());  
	       final Field iConnectivityManagerField = conmanClass.getDeclaredField("mService");  
	       iConnectivityManagerField.setAccessible(true);  
	       final Object iConnectivityManager = iConnectivityManagerField.get(conman);  
	       final Class iConnectivityManagerClass = Class.forName(iConnectivityManager.getClass().getName());  
	       final Method setMobileDataEnabledMethod = iConnectivityManagerClass.getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);  
	       setMobileDataEnabledMethod.setAccessible(true);  
	       setMobileDataEnabledMethod.invoke(iConnectivityManager, dataConnection);  
	     } catch (Exception e) { 
	     }
	}
	
	//飛航模式
	private Context appobject ;//= this.getApplicationContext();
	public boolean enable_airplane = true;
	public setairplaneHandler  setairplane_handler = new setairplaneHandler ();
	

	
	class setairplaneHandler extends Handler {   
	      @Override  
	      public void handleMessage(Message msg) {   
	          switch (msg.what) {   
	         case UPDATE_SETTING_SUCCESS:   
	        	 
	        	 setAirplaneMode(   appobject   ,  enable_airplane );
	        	 
	        	 if ( enable_airplane == true)
	        		 print_screen("已開啟飛航模式");
	        	 else
	        		 print_screen("已關閉飛航模式");

	             break;   
	         }          
	       super.handleMessage(msg);   
	    }   
	 }  
	
	public void set_airplane(boolean air)
	{
		if(air == true)
			 enable_airplane = true;
		else
			 enable_airplane = false;
		
    	Message m = new Message();   
    	m.what = UPDATE_SETTING_SUCCESS; 
    	setairplane_handler  .sendMessage(m);  
    	
   	 try {
			Thread.sleep(400);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	public static void setAirplaneMode(Context context, boolean status){
		boolean isAirplaneModeOn = isAirplaneModeOn(context);
		if((status && isAirplaneModeOn) || (!status && !isAirplaneModeOn)){
			return;
		}
		int mode = status ? 1 : 0;
		Settings.System.putInt(context.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, mode);
		Intent i = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
		i.putExtra("state", mode);
		context.sendBroadcast(i);
	}

	public static boolean isAirplaneModeOn(Context context){
		return Settings.System.getInt(context.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0) != 0;
	}
	
	
	
	
	
	
	
	
	
	
	
}


