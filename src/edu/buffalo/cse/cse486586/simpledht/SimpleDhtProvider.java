package edu.buffalo.cse.cse486586.simpledht;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.Iterator;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.TextView;

public class SimpleDhtProvider extends ContentProvider {
	private SimpleDhtDatabase md;
	SQLiteDatabase db;
	private static final String BASE_PATH = SimpleDhtDatabase.Dht_Table;
	int port;
	public static final Uri mUri =buildUri("content", "edu.buffalo.cse.cse486586.simpledht.provider");
	public static Socket socket1=null;
	public static Socket socket2=null;
	public static Socket socket3=null;
	public static int portNo1=11108;
	public static int portNo2=11112;
	public static int portNo3=11116;
	public static String portStr="";
	public static String pred=null;
	public static String succ=null;
	public static String cursor_key=null;
	public static String cursor_value=null;
	String name=null;
	public static int count=0;
	String ip ="10.0.2.2";
	ObjectOutputStream objOutput= null;
	ObjectInputStream objInput=null;

	private static Uri buildUri(String scheme, String authority) {
		Uri.Builder uriBuilder = new Uri.Builder();
		uriBuilder.authority(authority);
		uriBuilder.scheme(scheme);
		return uriBuilder.build();
	}
	private static final String AUTHORITY = "edu.buffalo.cse.cse486586.simpledht.provider";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
			+ "/" + SimpleDhtDatabase.Dht_Table);

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		db=md.getWritableDatabase();
		Log.v("content provider","before inserting");
		String key=values.getAsString(SimpleDhtDatabase.key);
		String value=values.getAsString(SimpleDhtDatabase.value);
		int flag_succ=0;
		int flag_pred=0;
		int flag_self=0;
		int flag_force_succ=0;
		String type=null;
		int port=0;
		String ip ="10.0.2.2";

		try{

			if(genHash(key).compareTo(genHash(portStr))<0){
				if(genHash(key).compareTo(genHash(pred))>0 || genHash(portStr).compareTo(genHash(pred))<0){	
					flag_self=1;
				}
				else{
					flag_pred=1;
				}
			}			
			else if(genHash(key).compareTo(genHash(portStr))>0){
				if(genHash(portStr).compareTo(genHash(succ))>0){
					flag_force_succ=1;
				}
				else {
					flag_succ=1;
				}

			}
			if( flag_self==1){
				db.insertWithOnConflict(SimpleDhtDatabase.Dht_Table, null, values, SQLiteDatabase.CONFLICT_REPLACE);
			}
			else if(flag_pred==1){
				port=findport(pred);
				type="insert";

			}
			else if(flag_succ==1){
				port=findport(succ);
				type="insert";
			}
			else{
				port=findport(succ);
				type="forceInsert";
			}
			if(flag_self!=1){
				Message msg= new Message();
				msg.setKey(key);
				msg.setValue(value);
				msg.setType(type);
				socket1=new Socket(ip, port);
				objOutput= new ObjectOutputStream(socket1.getOutputStream());
				objOutput.writeObject(msg);
				objOutput.close();
			}
		}catch(Exception e){
			e.printStackTrace();
		}

		//db.replace(BASE_PATH,null,values);
		//db.close();
		//getContext().getContentResolver().notifyChange(uri, null);
		return null;
	}

	@Override
	public boolean onCreate() {
		md=new SimpleDhtDatabase(getContext(),SimpleDhtDatabase.dbName,null,SimpleDhtDatabase.version);
		db=md.getWritableDatabase();
		db.delete(BASE_PATH, null, null);
		TelephonyManager tel =(TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
		portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
		if(portStr.equals("5554")){
			pred=succ="5554";
			count=1;
		}
		else {
			new ConnectRequest().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);

		}

		try{
			ServerSocket serverSocket = new ServerSocket (10000);
			new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR , serverSocket);


		}catch(Exception e){
			Log.v("ERROR", "In Server establish");
		}
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
			String sortOrder) {
		Cursor cursor=null;
		String[] arrstring= new String[2];
		arrstring[0]=SimpleDhtDatabase.key;
		arrstring[1]=SimpleDhtDatabase.value;
		MatrixCursor mcursor=new MatrixCursor(arrstring);
		ArrayList<Message> arlsmsg= new ArrayList<Message>();
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		queryBuilder.setTables(SimpleDhtDatabase.Dht_Table);
		Message msg= new Message();
		
		if(selection==null){

			cursor = queryBuilder.query(md.getReadableDatabase(),
					projection, selection, selectionArgs, null, null,sortOrder);

			cursor.setNotificationUri(getContext().getContentResolver(), uri);
		}
		else{
			Log.v(portStr,"in gdump");
			if(!selection.equals("gdump")){
				cursor = queryBuilder.query(md.getReadableDatabase(),
						projection, SimpleDhtDatabase.key+"=?", new String[]{selection}, null, null, sortOrder);
				cursor.setNotificationUri(getContext().getContentResolver(), uri);
				if(!cursor.moveToFirst()){
					try{
					socket1=new Socket(ip,findport(pred));
					objOutput= new ObjectOutputStream(socket1.getOutputStream());
					msg.setKey(selection);
					msg.setValue(portStr);
					msg.setType("query");
					objOutput.writeObject(msg);
					objInput=new ObjectInputStream(socket1.getInputStream());
					if(objInput!=null){
					msg=(Message) objInput.readObject();
					}
					objInput.close();
					objOutput.close();
					socket1.close();
					if(msg.getKey().equals("nothing")){
						socket2= new Socket(ip,findport(succ));
						ObjectOutputStream objOutput1= new ObjectOutputStream(socket2.getOutputStream());
						objOutput1.writeObject(msg);
						objInput=new ObjectInputStream(socket2.getInputStream());
						if(objInput!=null){
						msg=(Message) objInput.readObject();
						}
						objInput.close();
						objOutput.close();
						socket2.close();
					}
					if(!msg.getKey().equals("nothing")){
					mcursor.addRow(new Object[]{msg.getKey(),msg.getValue()});
					}
					cursor=mcursor;
					}catch(Exception e){
						e.printStackTrace();
					}
				}
			}
			
			else{
				try {
					
					cursor = queryBuilder.query(md.getReadableDatabase(),
							projection, null, selectionArgs, null, null,sortOrder);
					cursor.moveToFirst();
					do{
						mcursor.addRow(new Object[]{cursor.getString(cursor.getColumnIndex(SimpleDhtDatabase.key)),cursor.getString(cursor.getColumnIndex(SimpleDhtDatabase.value))});
					}while(cursor.moveToNext());
					if(!succ.equals(pred)){
					socket1=new Socket(ip,findport(pred));
					
					objOutput= new ObjectOutputStream(socket1.getOutputStream());
					msg.setKey(portStr);
					msg.setValue(portStr);
					msg.setType("gdump");
					
					objOutput.writeObject(msg);
					objInput= new ObjectInputStream(socket1.getInputStream());
					if(objInput!=null){
						arlsmsg=(ArrayList<Message>) objInput.readObject();
					}
					objInput.close();
					objOutput.close();
					socket1.close();
					Iterator<Message> it=arlsmsg.iterator();
					while(it.hasNext()){
						Message message= it.next();
						mcursor.addRow(new Object[]{message.getKey(),message.getValue()});
					}
					
					socket2=new Socket(ip,findport(succ));
					objOutput= new ObjectOutputStream(socket2.getOutputStream());

					objOutput.writeObject(msg);
					objInput= new ObjectInputStream(socket2.getInputStream());
					if(objInput!=null){
						arlsmsg=new ArrayList();
						arlsmsg=(ArrayList<Message>) objInput.readObject();
					}
					objInput.close();
					objOutput.close();
					socket2.close();
					it=arlsmsg.iterator();
					while(it.hasNext()){
						Message message= it.next();
						mcursor.addRow(new Object[]{message.getKey(),message.getValue()});
					}
					}
					else{
						
						socket2= new Socket(ip,findport(succ));
						objOutput= new ObjectOutputStream(socket2.getOutputStream());
						msg.setKey(portStr);
						msg.setValue(portStr);
						msg.setType("gdump");
						objOutput.writeObject(msg);
						objInput= new ObjectInputStream(socket2.getInputStream());
						if(objInput!=null){
							arlsmsg=new ArrayList();
							arlsmsg=(ArrayList<Message>) objInput.readObject();
						}
						objInput.close();
						objOutput.close();
						socket2.close();
						Iterator<Message> it1= arlsmsg.iterator();
						//it=arlsmsg.iterator();
						while(it1.hasNext()){
							Message message= it1.next();
							mcursor.addRow(new Object[]{message.getKey(),message.getValue()});
						}
					}
					cursor=mcursor;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		}
		
		return cursor;

	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	private String genHash(String input) throws NoSuchAlgorithmException {
		MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
		byte[] sha1Hash = sha1.digest(input.getBytes());
		Formatter formatter = new Formatter();
		for (byte b : sha1Hash) {
			formatter.format("%02x", b);
		}
		return formatter.toString();
	}

	public int findport(String avd){
		int port=0;
		if(avd.equals("5554")){
			port=11108;
		}
		else if(avd.equals("5556")){
			port=11112;
		}
		else if(avd.equals("5558")){
			port=11116;
		}
		return port;
	}

	private class ConnectRequest extends AsyncTask<String, Void, Void>{

		@Override
		protected Void doInBackground(String... arg0) {
			try{
				if(!portStr.equals("5554")){
					Message objConnectMsg= new Message();
					objConnectMsg.setKey(portStr);
					objConnectMsg.setValue(portStr);
					objConnectMsg.setType("connect");
					Socket socket=new Socket(ip, portNo1);
					objOutput= new ObjectOutputStream(socket.getOutputStream());
					objOutput.writeObject(objConnectMsg);
					Log.v("connect", "after connect message sent");
					objInput=new ObjectInputStream(socket.getInputStream());
					if(objInput!=null){
						Message recConnectmsg= (Message) objInput.readObject();
						pred=recConnectmsg.getKey();
						succ=recConnectmsg.getValue();
						Log.v("server" + portStr, "pred" +pred);
						Log.v("server" + portStr, "succ" +succ);
					}
				}
			}catch(Exception e){
				Log.v("connect"," exception in connecting to node 0");
				e.printStackTrace();
			}
			return null;
		}

	}

	private class ServerTask extends AsyncTask<ServerSocket, String, Void>{

		@Override
		protected Void doInBackground(ServerSocket... sockets) {
			ServerSocket serverSocket = sockets[0];
			Socket socket=null;
			ContentValues values=null;
			Log.v("server","in server");
			Log.v("avd is",portStr);
			try{
				while(true){
					socket = serverSocket.accept();
					Log.v("In server","connection accepted");
					objInput= new ObjectInputStream(socket.getInputStream());
					Log.v("In server","object read");
					//Log.v("avd is",portStr);
					if(portStr.equals("5554")){
						Message recConnectMsg=(Message)objInput.readObject();
						if(recConnectMsg.type.equals("connect")){
							count=count+1;
							pred=recConnectMsg.getKey();
							succ=recConnectMsg.getValue();
							Log.v("server" + portStr, "pred" +pred);
							Log.v("server" + portStr, "succ" +succ);
							if(count==2){
								Log.v("server", "before server sent");
								pred=recConnectMsg.getKey();
								succ=recConnectMsg.getKey();
								Message objConnectmsg1= new Message();
								objConnectmsg1.setKey("5554");
								objConnectmsg1.setValue("5554");
								objConnectmsg1.setType("position");
								objOutput= new ObjectOutputStream(socket.getOutputStream());
								objOutput.writeObject(objConnectmsg1);
								Log.v("server","sent to" + recConnectMsg.getKey());
							}
							else if(count==3){
								String key= recConnectMsg.getKey();
								if(key.equals("5558")){
									pred="5556";
									succ=key;
								}
								else if(key.equals("5556")){
									pred=key;
									succ="5558";
								} 
								Log.v("server" + portStr, "pred" +pred);
								Log.v("server" + portStr, "succ" +succ);
								socket2= new Socket(ip,portNo2);
								socket3=new Socket(ip,portNo3);
								ObjectOutputStream obj2= new ObjectOutputStream(socket2.getOutputStream());
								ObjectOutputStream obj3= new ObjectOutputStream(socket3.getOutputStream());
								Message objConnectMsg2= new Message();
								objConnectMsg2.setKey("5558");
								objConnectMsg2.setValue("5554");
								objConnectMsg2.setType("position");
								Message objConnectMsg3= new Message();
								objConnectMsg3.setKey("5554");
								objConnectMsg3.setValue("5556");
								objConnectMsg3.setType("position");
								obj2.writeObject(objConnectMsg2);
								obj3.writeObject(objConnectMsg3);
							}

						}
						else if(recConnectMsg.type.equals("insert")){
							values=new ContentValues();
							values.put(SimpleDhtDatabase.key, recConnectMsg.getKey());
							values.put(SimpleDhtDatabase.value, recConnectMsg.getValue());
							//db.insert(SimpleDhtDatabase.Dht_Table, null, values);
							Uri newUri = getContext().getContentResolver().insert(
									mUri,   
									values
									);
						}
						else if(recConnectMsg.type.equals("forceInsert")){
							values=new ContentValues();
							values.put(SimpleDhtDatabase.key, recConnectMsg.getKey());
							values.put(SimpleDhtDatabase.value, recConnectMsg.getValue());
							db.insertWithOnConflict(SimpleDhtDatabase.Dht_Table, null, values, SQLiteDatabase.CONFLICT_REPLACE);
						}
						else if(recConnectMsg.type.equals("gdump")){
							Cursor cursor=null;
							Message msg= null;
							String cursor_key=null;
							String cursor_value=null;
							ArrayList<Message> armsg= new ArrayList<Message>();
							cursor=getContext().getContentResolver().query(mUri, null, null, null, null);
							if(cursor.moveToFirst()){
								do{
									cursor_key= cursor.getString(cursor.getColumnIndex(SimpleDhtDatabase.key));
									cursor_value=cursor.getString(cursor.getColumnIndex(SimpleDhtDatabase.value));
									msg=new Message();
									msg.setKey(cursor_key);
									msg.setValue(cursor_value);
									armsg.add(msg);
									//publishProgress(msg);
									//cursor.moveToNext();
								}while(cursor.moveToNext());
							}
							objOutput=new ObjectOutputStream(socket.getOutputStream());
							objOutput.writeObject(armsg);

						}
						else if(recConnectMsg.type.equals("query")){
							Cursor cursor=null;
							Message msg= null;
							String cursor_key=null;
							String cursor_value=null;
							msg= new Message();
							//ArrayList<Message> armsg= new ArrayList<Message>();
							cursor=getContext().getContentResolver().query(mUri, null, recConnectMsg.getKey(), null, null);
							if(!cursor.moveToFirst()){
								msg.setKey("nothing");
								msg.setValue(portStr);
								//msg.setType("query");
							}
							else{
								cursor_key= cursor.getString(cursor.getColumnIndex(SimpleDhtDatabase.key));
								cursor_value=cursor.getString(cursor.getColumnIndex(SimpleDhtDatabase.value));
								msg.setKey(cursor_key);
								msg.setValue(cursor_value);
							}
							objOutput=new ObjectOutputStream(socket.getOutputStream());
							objOutput.writeObject(msg);
								
						}
					}
					else if(objInput!=null){
						Message recPosition= (Message)objInput.readObject();
						if(recPosition.getType().equals("position")){
							pred=recPosition.getKey();
							succ=recPosition.getValue();
							Log.v("server" + portStr, "pred" +pred);
							Log.v("server" + portStr, "succ" +succ);
						}
						else if(recPosition.type.equals("insert")){
							values=new ContentValues();
							values.put(SimpleDhtDatabase.key, recPosition.getKey());
							values.put(SimpleDhtDatabase.value, recPosition.getValue());
							Uri newUri = getContext().getContentResolver().insert(
									mUri,   
									values
									);
						}
						else if(recPosition.type.equals("forceInsert")){
							values=new ContentValues();
							values.put(SimpleDhtDatabase.key, recPosition.getKey());
							values.put(SimpleDhtDatabase.value, recPosition.getValue());
							db.insertWithOnConflict(SimpleDhtDatabase.Dht_Table, null, values, SQLiteDatabase.CONFLICT_REPLACE);
						}
						else if(recPosition.type.equals("gdump")){
							Cursor cursor=null;
							Message msg= null;
							String cursor_key=null;
							String cursor_value=null;
							ArrayList<Message> armsg= new ArrayList<Message>();
							cursor=getContext().getContentResolver().query(mUri, null, null, null, null);
							if(cursor.moveToFirst()){
								do{
									cursor_key= cursor.getString(cursor.getColumnIndex(SimpleDhtDatabase.key));
									cursor_value=cursor.getString(cursor.getColumnIndex(SimpleDhtDatabase.value));
									msg=new Message();
									msg.setKey(cursor_key);
									msg.setValue(cursor_value);
									armsg.add(msg);
									//publishProgress(msg);
									//cursor.moveToNext();
								}while(cursor.moveToNext());
							}
							objOutput=new ObjectOutputStream(socket.getOutputStream());
							objOutput.writeObject(armsg);

						}
						else if(recPosition.type.equals("query")){
							Cursor cursor=null;
							Message msg= null;
							String cursor_key=null;
							String cursor_value=null;
							msg= new Message();
							//ArrayList<Message> armsg= new ArrayList<Message>();
							cursor=getContext().getContentResolver().query(mUri, null, recPosition.getKey(), null, null);
							if(!cursor.moveToFirst()){
								msg.setKey("nothing");
								msg.setValue(portStr);
								//msg.setType("query");
							}
							else{
								cursor_key= cursor.getString(cursor.getColumnIndex(SimpleDhtDatabase.key));
								cursor_value=cursor.getString(cursor.getColumnIndex(SimpleDhtDatabase.value));
								msg.setKey(cursor_key);
								msg.setValue(cursor_value);
							}
							objOutput=new ObjectOutputStream(socket.getOutputStream());
							objOutput.writeObject(msg);
								
						}
					}
				}
			}catch(Exception e){
				Log.v("server", "error in accepting connection");
				e.printStackTrace();
			}
			// TODO Auto-generated method stub
			return null;
		}


	}
}


