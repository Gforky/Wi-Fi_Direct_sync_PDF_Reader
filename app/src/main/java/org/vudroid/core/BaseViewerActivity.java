package org.vudroid.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.StreamCorruptedException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.wifi.p2p.WifiP2pInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import org.vudroid.R;
import org.vudroid.core.events.CurrentPageListener;
import org.vudroid.core.events.DecodingProgressListener;
import org.vudroid.core.models.CurrentPageModel;
import org.vudroid.core.models.DecodingProgressModel;
import org.vudroid.pdfdroid.codec.PdfContext;


public class BaseViewerActivity extends Activity implements DecodingProgressListener, CurrentPageListener
{
    private static final int MENU_EXIT = 0;
    private static final int CLEAR_CANVAS = 1;
    public static DecodeService decodeService;
    private DocumentView documentView;
    private CanvasView canvasView;
    private int currentIndex = 0;
    private CurrentPageModel currentPageModel;
    private FrameLayout canvas = null;
    private int lastPage;
    private String host;
    public static Boolean sORc;
	public static ServerThread serverThread;
	public static ClientThread clientThread;
	private Socket client;
	private Socket socket;
	private String readMessage;
	private Boolean canChangePage = true;
	public static String[] path;
	public static String downX="";
	public static String downY="";
	private String points = "";
	private ArrayList<String> movePath = new ArrayList<String>();	




    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Bundle bundle = getIntent().getExtras();
        host = bundle.getString("host");
        sORc = bundle.getBoolean("sORc");
        initDecodeService();
        final DecodingProgressModel progressModel = new DecodingProgressModel();
        progressModel.addEventListener(this);
        currentPageModel = new CurrentPageModel();
        currentPageModel.addEventListener(this);
        documentView = new DocumentView(this, progressModel, currentPageModel);
        documentView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        decodeService.setContentResolver(getContentResolver());
        decodeService.setContainerView(documentView);
        documentView.setDecodeService(decodeService);
        decodeService.open(getIntent().getData());
        canvasView = new CanvasView(this,currentIndex,getIntent().getData().getLastPathSegment());
        
        
        setContentView(R.layout.document);
        canvas = (FrameLayout)this.findViewById(R.id.canvas);
        canvas.addView(documentView);
        canvas.addView(canvasView);
        documentView.showDocument();
        canvasView.showCanvas();             
        
        final Button prev = (Button)this.findViewById(R.id.prev);
        final Button next = (Button)this.findViewById(R.id.next);
        
        if (sORc) {
			Log.d("STATE", "I am the MASTER! :D");
			//createClientSocket(info, mIP);
			createServerSocket();
			Toast.makeText(getApplicationContext(),"Master device", Toast.LENGTH_SHORT).show();
		} else {
			//createServerSocket();
			createClientSocket(host);
			Log.d("STATE", "I am the SLAVE ! :(");
			Toast.makeText(getApplicationContext(), "Slave device", Toast.LENGTH_SHORT).show();
		}
        
        prev.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View view) {
		    	canChangePage = false;
				prevPage();				
			}
        });
        
        next.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View view) {
		    	canChangePage = false;
				nextPage();

			}
        	
        });
    }
    
    public void clientWrite(String string){
    	clientThread.write(string
                .getBytes());
    }
    
    public void serverWrite(String string){
    	serverThread.write(string
                .getBytes());
    }
 /**********************************************************************/  
    public void prevPage(){
    	lastPage = currentIndex;
		currentIndex--;
		if (currentIndex < 0){
			currentIndex = 0;
		}
		if (sORc) {
			serverWrite("prev");
		} else {
			clientWrite("prev");
		}
		canvasView.changeCanvasLeft(lastPage, currentIndex);
		if(lastPage == currentIndex){

		}
		else{
		documentView.goToPage(currentIndex);
		}
    }
    
    public void nextPage(){
    	lastPage = currentIndex;
		currentIndex++;
		if (currentIndex > decodeService.getPageCount()-1){
			currentIndex = decodeService.getPageCount()-1;	
		}
		if (sORc) {
			serverWrite("next");
		} else {
			clientWrite("next");
		}
		canvasView.changeCanvasRight(lastPage, currentIndex);
		if(lastPage == currentIndex){

		}
		else{
		documentView.goToPage(currentIndex);
		}
    }
 /**************************************************************************/   
    @Override
	public void decodingProgressChanged(final int currentlyDecoding)
    {
        runOnUiThread(new Runnable()
        {
            @Override
			public void run()
            {
                getWindow().setFeatureInt(Window.FEATURE_INDETERMINATE_PROGRESS, currentlyDecoding == 0 ? 10000 : currentlyDecoding);
            }
        });
    }


    private void setWindowTitle()
    {
        final String name = getIntent().getData().getLastPathSegment();
        getWindow().setTitle(name);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState)
    {
        super.onPostCreate(savedInstanceState);
        setWindowTitle();
    }


    private void initDecodeService()
    {
        if (decodeService == null)
        {
            decodeService = createDecodeService();
        }
    }

    protected DecodeService createDecodeService()
    {
        return new DecodeServiceBase(new PdfContext());
    }

    @Override
    protected void onStop()
    {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        decodeService.recycle();
        decodeService = null;
        super.onDestroy();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        menu.add(0, MENU_EXIT, 0, "Exit");
        menu.add(1, CLEAR_CANVAS, 1, "Clear");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case MENU_EXIT:
                System.exit(0);
            case CLEAR_CANVAS:
            	CanvasView.mdrawBitmap.recycle();
            	System.gc();
            	CanvasView.mdrawBitmap = Bitmap.createBitmap(1000, 2000, Bitmap.Config.ARGB_8888);
    	        CanvasView.mdrawCanvas = new Canvas(CanvasView.mdrawBitmap);
    	        CanvasView.mdrawCanvas.save(Canvas.ALL_SAVE_FLAG);
                return true;
        }
        return false;
    }
    
/********************************************************************************************************************************/
    public void createClientSocket(String host) {
		clientThread = new ClientThread(host, 8888);
		clientThread.start();
	}

	public void createServerSocket() {
		serverThread = new ServerThread();
		serverThread.start();
	}
	
	class ClientThread extends Thread{
		private InputStream iStream;
	    private OutputStream oStream;
	    private static final String TAG = "ChatHandler";
	    private ObjectInputStream objInputStream;
	    private ObjectOutputStream objOutputStream;
		private int port;
		private String host;
		
		public ClientThread(String host, int port) {
			// TODO Auto-generated constructor stub
			this.host = host;
			this.port = port;
		}
		@Override
		public void run() {
			// TODO Auto-generated method stub
			socket = new Socket();
			try {
				socket.bind(null);
				socket.connect(new InetSocketAddress(host, port), 5000);
				iStream = socket.getInputStream();
	            oStream = socket.getOutputStream();
	            byte[] buffer = new byte[10000];
	            int bytes = 0;
	            oStream = socket.getOutputStream();
				objOutputStream = new ObjectOutputStream(oStream);
				objOutputStream.writeObject("HELLO BUDDY!!!!!!!");
				Log.d("STATE", "client sent message: " + "HELLO BUDDY!!!!!!!");
	        			objInputStream = new ObjectInputStream(iStream);
	        			String s = ( String) objInputStream.readObject();
	        			Log.d("STATE", "client received message: " + s);
	        			while (true) {
	                        try {
	                            // Read from the InputStream
	                            bytes = iStream.read(buffer);
	                            if (bytes == -1) {
	                                break;
	                            }
	                            readMessage = new String(buffer, 0, bytes);
	                            if(readMessage.equals("next") && canChangePage == true){
	                            	nextPage();
		                            oStream.write("done".getBytes());
	                            }
	                            else if(readMessage.equals("prev") && canChangePage == true){
	                            	prevPage();
		                            oStream.write("done".getBytes());
	                            }
	                            
	                            else if(readMessage.equals("done")){
	                            	canChangePage = true;
	                            }
	                            else if(!readMessage.equals("next") && !readMessage.equals("prev") && !readMessage.equals("done")){
	                            	points = readMessage;
	                            	splitPoints();
	                            }
                            	
	                            // Send the obtained bytes to the UI Activity
	                            Log.d(TAG, "Rec:" + String.valueOf(buffer));
	                        } catch (IOException e) {
	                            Log.e(TAG, "disconnected", e);
	                        }
	                    }
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		public void write(byte[] buffer) {
	        try {
	            oStream.write(buffer);
	        } catch (IOException e) {
	            Log.e(TAG, "Exception during write", e);
	        }
	    }
		
	}
	
	class ServerThread extends Thread{
		private ServerSocket serverSocket;
		private InputStream iStream;
	    private OutputStream oStream;
	    private static final String TAG = "ChatHandler";
	    private ObjectInputStream objInputStream;
	    private ObjectOutputStream objOutputStream;
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				serverSocket = new ServerSocket(8888);
				client = serverSocket.accept();
				iStream = client.getInputStream();
	            oStream = client.getOutputStream();
	            byte[] buffer = new byte[10000];
	            int bytes = 0;
	            oStream = client.getOutputStream();
				objOutputStream = new ObjectOutputStream(oStream);
				objOutputStream.writeObject("HELLO BUDDY!!!!!!!");
				Log.d("STATE", "client sent message: " + "HELLO BUDDY!!!!!!!");
	        			objInputStream = new ObjectInputStream(iStream);
	        			String s = ( String) objInputStream.readObject();
	        			Log.d("STATE", "client received message: " + s);
	        			while (true) {
	                        try {
	                            // Read from the InputStream
	                            bytes = iStream.read(buffer);
	                            if (bytes == -1) {
	                                break;
	                            }
	                            readMessage = new String(buffer, 0, bytes);
	                            if(readMessage.equals("next") && canChangePage == true){
	                            	nextPage();
		                            oStream.write("done".getBytes());
	                            }
	                            else if(readMessage.equals("prev") && canChangePage == true){
	                            	prevPage();
		                            oStream.write("done".getBytes());
	                            }
	                            else if(readMessage.equals("done")){
	                            	canChangePage = true;
	                            }
	                            else if(!readMessage.equals("next") && !readMessage.equals("prev") && !readMessage.equals("done") ){
	                            	points = readMessage;
	                            	splitPoints();
	                            }
                            	
	                            // Send the obtained bytes to the UI Activity
	                            Log.d(TAG, "Rec:" + String.valueOf(buffer));
	                        } catch (IOException e) {
	                            Log.e(TAG, "disconnected", e);
	                        }
	                    }
			}catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		public void write(byte[] buffer) {
	        try {
	            oStream.write(buffer);
	        } catch (IOException e) {
	            Log.e(TAG, "Exception during write", e);
	        }
	    }
		
	}
/***********************************************************************************************************************************************/
	@Override
	public void currentPageChanged(int pageIndex) {
		// TODO Auto-generated method stub
		
	}
	
	private void splitPoints() {
		// TODO Auto-generated method stub
		String[] pointsArray = points.split(",");
		downX = pointsArray[0];
		downY = pointsArray[1];
		movePath.clear();
		if(pointsArray.length>3){
			for(int z = 2; z<pointsArray.length; z++){
				if(!pointsArray[z].equals("up"))
					movePath.add(pointsArray[z]);
			}
			path = (String[])movePath.toArray(new String[movePath.size()]);
			String endX = path[path.length-2];
			String endY = path[path.length-1];
			canvasView.uChange(path,downX,downY,endX,endY);
			movePath.clear();
		}
		
	}

}
