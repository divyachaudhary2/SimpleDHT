package edu.buffalo.cse.cse486586.simpledht;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class SimpleDhtMainActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_simple_dht_main);
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_simple_dht_main);

		TextView tv = (TextView) findViewById(R.id.textView1);
		tv.setMovementMethod(new ScrollingMovementMethod());
		findViewById(R.id.button3).setOnClickListener(
				new OnTestClickListener(tv, getContentResolver()));
		findViewById(R.id.button1).setOnClickListener(
				new Dump(tv,getContentResolver()));
		findViewById(R.id.button2).setOnClickListener(
				new GlobalDump(tv,getContentResolver()));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_simple_dht_main, menu);
		return true;
	}

//	private class ConnectRequest extends AsyncTask<String, Void, Void>{
//
//		@Override
//		protected Void doInBackground(String... arg0) {
//			try{
//				if(!portStr.equals("5554")){
//					Message objConnectMsg= new Message();
//					objConnectMsg.setKey(portStr);
//					objConnectMsg.setValue(portStr);
//					objConnectMsg.setType("connect");
//					Socket socket=new Socket(ip, portNo1);
//					objOutput= new ObjectOutputStream(socket.getOutputStream());
//					objOutput.writeObject(objConnectMsg);
//					Log.v("connect", "after connect message sent");
//					objInput=new ObjectInputStream(socket.getInputStream());
//					if(objInput!=null){
//					Message recConnectmsg= (Message) objInput.readObject();
//					pred=recConnectmsg.getKey();
//					succ=recConnectmsg.getValue();
//					Log.v("server" + portStr, "pred" +pred);
//					Log.v("server" + portStr, "succ" +succ);
//					}
//				}
//			}catch(Exception e){
//				Log.v("connect"," exception in connecting to node 0");
//				e.printStackTrace();
//			}
//			return null;
//		}
//
//	}

//	private class ServerTask extends AsyncTask<ServerSocket, String, Void>{
//
//		@Override
//		protected Void doInBackground(ServerSocket... sockets) {
//			ServerSocket serverSocket = sockets[0];
//			Socket socket=null;
//			Log.v("server","in server");
//			Log.v("avd is",portStr);
//			try{
//				while(true){
//					socket = serverSocket.accept();
//					Log.v("In server","connection accepted");
//					objInput= new ObjectInputStream(socket.getInputStream());
//					Log.v("In server","object read");
//					//Log.v("avd is",portStr);
//					if(portStr.equals("5554")){
//						Message recConnectMsg=(Message)objInput.readObject();
//						if(recConnectMsg.type.equals("connect")){
//							count=count+1;
//							pred=recConnectMsg.getKey();
//							succ=recConnectMsg.getValue();
//							Log.v("server" + portStr, "pred" +pred);
//							Log.v("server" + portStr, "succ" +succ);
//							if(count==2){
//								Log.v("server", "before server sent");
//								pred=recConnectMsg.getKey();
//								succ=recConnectMsg.getKey();
//								Message objConnectmsg1= new Message();
//								objConnectmsg1.setKey("5554");
//								objConnectmsg1.setValue("5554");
//								objConnectmsg1.setType("position");
//								objOutput= new ObjectOutputStream(socket.getOutputStream());
//								objOutput.writeObject(objConnectmsg1);
//								Log.v("server","sent to" + recConnectMsg.getKey());
//								//							String keyyy= "5558";
//								//							if((genHash(portStr).compareTo(genHash(pred))>0) ){
//								//								Log.v(portStr, portStr+"bigger than"+pred);
//								//									if(genHash(portStr).compareTo(genHash(keyyy))>0){
//								//										Log.v(portStr, portStr+"bigger than"+keyyy);
//								//									}
//								//									else{
//								//										Log.v("5558", "biggest");
//								//									}
//								//										
//								//							}
//								//							else if((genHash(portStr).compareTo(genHash(pred))<0)){
//								//								Log.v("5554", portStr+"is smaller than"+pred);
//								//								if((genHash(pred).compareTo(genHash(keyyy))>0)){
//								//									Log.v(pred,pred+" is biggest");
//								//								}
//								//								else{
//								//									Log.v("5558", "biggest");
//								//								}
//								//							}
//								//Log.v("5554", genHash(portStr));
//								//Log.v("5556", genHash(pred));
//
//								//Log.v("5558", genHash(keyyy));
//							}
//							else if(count==3){
//								String key= recConnectMsg.getKey();
//								if(key.equals("5558")){
//									pred="5556";
//									succ=key;
//								}
//								else if(key.equals("5556")){
//									pred=key;
//									succ="5558";
//								} 
//								Log.v("server" + portStr, "pred" +pred);
//								Log.v("server" + portStr, "succ" +succ);
//								socket2= new Socket(ip,portNo2);
//								socket3=new Socket(ip,portNo3);
//								ObjectOutputStream obj2= new ObjectOutputStream(socket2.getOutputStream());
//								ObjectOutputStream obj3= new ObjectOutputStream(socket3.getOutputStream());
//								Message objConnectMsg2= new Message();
//								objConnectMsg2.setKey("5558");
//								objConnectMsg2.setValue("5554");
//								objConnectMsg2.setType("position");
//								Message objConnectMsg3= new Message();
//								objConnectMsg3.setKey("5554");
//								objConnectMsg3.setValue("5556");
//								objConnectMsg3.setType("position");
//								obj2.writeObject(objConnectMsg2);
//								obj3.writeObject(objConnectMsg3);
//							}
//
//						}
//						else if(recConnectMsg.type.equals("insert")){
//							
//						}
//					}
//					else if(objInput!=null){
//						Message recPosition= (Message)objInput.readObject();
//						if(recPosition.getType().equals("position")){
//						pred=recPosition.getKey();
//						succ=recPosition.getValue();
//						Log.v("server" + portStr, "pred" +pred);
//						Log.v("server" + portStr, "succ" +succ);
//						}
//						else
//					}
//				}
//			}catch(Exception e){
//				Log.v("server", "error in accepting connection");
//				e.printStackTrace();
//			}
//			// TODO Auto-generated method stub
//			return null;
//		}
//
//
//
//	}
//	public String genHash(String input) throws NoSuchAlgorithmException {
//		MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
//		byte[] sha1Hash = sha1.digest(input.getBytes());
//		Formatter formatter = new Formatter();
//		for (byte b : sha1Hash) {
//			formatter.format("%02x", b);
//		}
//		return formatter.toString();
//	}

}
