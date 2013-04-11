package edu.buffalo.cse.cse486586.simpledht;

import android.content.ContentResolver;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class Dump implements OnClickListener  {
	private final ContentResolver mContentResolver;
	private final TextView mTextView;
	private final Uri mUri;
	//public static String cursor_key=null;
	//public static String cursor_value=null;
	Dump(TextView v,ContentResolver c){
		mTextView=v;
		mContentResolver=c;
		mUri = buildUri("content", "edu.buffalo.cse.cse486586.simpledht.provider");
		//mContentValues = initTestValues();
	}
	
	private Uri buildUri(String scheme, String authority) {
		Uri.Builder uriBuilder = new Uri.Builder();
		uriBuilder.authority(authority);
		uriBuilder.scheme(scheme);
		return uriBuilder.build();
	}

	public void onClick(View arg0) {
		new Task().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		
	}
	private class Task extends AsyncTask<Void, String, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			Cursor cursor=null;
			String[] msg= null;
			String cursor_key=null;
			String cursor_value=null;
			cursor=mContentResolver.query(mUri, null, null, null, SimpleDhtDatabase.key);
				
					if(cursor.moveToFirst()){
						do{
						cursor_key= cursor.getString(cursor.getColumnIndex(SimpleDhtDatabase.key));
						cursor_value=cursor.getString(cursor.getColumnIndex(SimpleDhtDatabase.value));
						msg=new String[2];
						msg[0]=cursor_key;
						msg[1]=cursor_value;
						publishProgress(msg);
						//cursor.moveToNext();
					}while(cursor.moveToNext());
					}
				return null;
		}
		protected void onProgressUpdate(String...msgs) {
			mTextView.append("");
			mTextView.append(msgs[0]);
			mTextView.append(",");
			mTextView.append(msgs[1]);
			mTextView.append("\n");
			//mTextView.s
			return;
		}
		
	}

}
