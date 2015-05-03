
package org.vudroid.core;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Handles reading and writing of messages with socket buffers. Uses a Handler
 * to post messages to UI thread for UI updates.
 */
public class ChatManager implements Runnable {

    private Socket socket = null;

    public ChatManager(Socket socket) {
        this.socket = socket;
    }

    private InputStream iStream;
    private OutputStream oStream;
    private static final String TAG = "ChatHandler";
    private ObjectInputStream objInputStream;
    private ObjectOutputStream objOutputStream;
    public static String readMessage;

    @Override
    public void run() {
        try {
            iStream = socket.getInputStream();
            oStream = socket.getOutputStream();
            byte[] buffer = new byte[1024];
            int bytes;
            oStream = socket.getOutputStream();
			objOutputStream = new ObjectOutputStream(oStream);
			objOutputStream.writeObject("HELLO BUDDY!!!!!!!");
			Log.d("STATE", "client sent message: " + "HELLO BUDDY!!!!!!!");
			//oStream.close();
		
            //while (true) {
               // try {
                    // Read from the InputStream
                	//iStream = socket.getInputStream();
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
                            System.out.println("HEHOAHIOH&&&&&"+readMessage);
                            // Send the obtained bytes to the UI Activity
                            Log.d(TAG, "Rec:" + String.valueOf(buffer));
                        } catch (IOException e) {
                            Log.e(TAG, "disconnected", e);
                        }
                    }
                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
               // }
            //}
        } catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
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
