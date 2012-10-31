/**
 * @author Valeriy Chevtaev myltik@gmail.com
 */

package ru.chupakabr.dev.android.pgcards;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.GestureDetector;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.OnGestureListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.admob.android.ads.AdManager;
import com.admob.android.ads.AdView;

public class PGCards extends Activity implements OnGestureListener, OnDoubleTapListener {
    /** Called when the activity is first created. */
	
	private static final long CHECK_CONNECTION_DELAY = 60000;
	
//	private final String TAG = "PGCards";
	private final int DLG_POPUP = 1;
	
	static private final int[] ITEMS = new int[] {
		R.string.item1, R.string.item2, R.string.item3, R.string.item4,
		R.string.item5, R.string.item6, R.string.item7, R.string.item8
	};
	
	ViewFlipper switcher;
	
	private GestureDetector gestureDetector;
	private float scrolledDistanceX = 0;
	private boolean scrollTouching = false;

	private Handler taskHandler = new Handler();
	private Timer timer = new Timer();
	
	
	/**
	 * 
	 * @author VChevtaev
	 *
	 */
	private class CheckConnectionTask extends AsyncTask<Void, Void, Boolean> {

		/* (non-Javadoc)
		 * @see android.os.AsyncTask#doInBackground(Params[])
		 */
		@Override
		protected Boolean doInBackground(Void... params) {
	        ConnectivityManager conManager = 
	    		(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		    NetworkInfo wifiInfo = conManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
//		    NetworkInfo wimaxInfo = conManager.getNetworkInfo(ConnectivityManager.TYPE_WIMAX); // SDK8
		    NetworkInfo mobileInfo = conManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

		    if ((wifiInfo != null && wifiInfo.isConnected())
//		    		|| (wimaxInfo != null && wimaxInfo.isConnected()) // SDK8
		    		|| (mobileInfo != null && mobileInfo.isConnected()))
		    {
		    	return Boolean.TRUE;
		    }

			return Boolean.FALSE;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (result) {
		        AdView adView = (AdView) findViewById(R.id.ad);
		        adView.requestFreshAd();
		    }
		}
	}
	
	/**
	 * 
	 * @author VChevtaev
	 *
	 */
	private class CheckConnectionTimerTask extends TimerTask {

		private Runnable runnable = new Runnable() {
			public void run() {
				new CheckConnectionTask().execute();
			}
		};
		
		/* (non-Javadoc)
		 * @see java.util.TimerTask#run()
		 */
		@Override
		public void run() {
			taskHandler.post(runnable);
		}
		
	}
	
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
//    	Log.d(TAG, "Start creation");
    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
//        Log.d(TAG, "Get the Switcher");
        switcher = (ViewFlipper) findViewById(R.id.switcher);

        // v1.0: Load predefined views
//        Log.d(TAG, "Create views");
        LayoutInflater layoutInflater = 
        		(LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        for (int viewName : ITEMS) {
        	TextView view = (TextView) layoutInflater.inflate(R.layout.item, null);
        	view.setText(viewName);
        	switcher.addView(view);
		}
        
        gestureDetector = new GestureDetector(this);
        
        
        // Check internet connection
        timer.schedule(new CheckConnectionTimerTask(), 0, CHECK_CONNECTION_DELAY);

        // Ads testing
//	    AdManager.setTestDevices(new String[] {
//        		AdManager.TEST_EMULATOR
//        });
	    
//        Log.d(TAG, "Done creation");
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.main, menu);
		
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_item_exit:
			finish();
			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_UP && scrollTouching) {
			scrollTouching = false;
			
			if (scrolledDistanceX > 0) {
				switcher.showNext();
			} else if (scrolledDistanceX < 0) {
				switcher.showPrevious();
			}
			
			return true;
		}
		
		return gestureDetector.onTouchEvent(event);
	}

	/* (non-Javadoc)
	 * @see android.view.GestureDetector.OnGestureListener#onScroll(android.view.MotionEvent, android.view.MotionEvent, float, float)
	 */
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		scrolledDistanceX = distanceX;
		scrollTouching = true;
		
		return false;
	}
	
	/* (non-Javadoc)
	 * @see android.view.GestureDetector.OnDoubleTapListener#onDoubleTapEvent(android.view.MotionEvent)
	 */
	public boolean onDoubleTapEvent(MotionEvent e) {
		showDialog(DLG_POPUP);
		return true;
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DLG_POPUP:
			Dialog dlg = new Dialog(this);
			
			dlg.setContentView(R.layout.popup);
			dlg.setTitle(R.string.select_card);
			
			TableLayout layout = (TableLayout) dlg.findViewById(R.id.dlg_popup);
	        LayoutInflater layoutInflater = 
        		(LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	        
	        int i = 0;
	        TableRow curRow = null;
	        for (int viewName : ITEMS) {
	        	if (i % 2 == 0) {
	        		// Add another row
	        		curRow = (TableRow) layoutInflater.inflate(R.layout.popup_item_row, null);

	        		// Add current row to the view
	        		layout.addView(curRow);
	        	}
	        	
	        	// Create button and add it to the row
	        	Button view = (Button) layoutInflater.inflate(R.layout.popup_item, null);
	        	view.setText(viewName);
	        	view.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						TableRow parentRow = (TableRow) v.getParent();
						TableLayout parentTable = (TableLayout) parentRow.getParent();
						int inRowIdx = parentRow.indexOfChild(v);
						int inTableIdx = parentTable.indexOfChild(parentRow);
						int idx = inRowIdx + inTableIdx * 2;
						
						PGCards.this.switcher.setDisplayedChild(idx);
						PGCards.this.removeDialog(DLG_POPUP);
					}
				});
	        	curRow.addView(view);

	        	i++;
	        }
			
			return dlg;
		}
		
		return super.onCreateDialog(id);
	}

	/* (non-Javadoc)
	 * @see android.view.GestureDetector.OnGestureListener#onDown(android.view.MotionEvent)
	 */
	public boolean onDown(MotionEvent e) {
		return false;
	}

	/* (non-Javadoc)
	 * @see android.view.GestureDetector.OnGestureListener#onFling(android.view.MotionEvent, android.view.MotionEvent, float, float)
	 */
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		return false;
	}

	/* (non-Javadoc)
	 * @see android.view.GestureDetector.OnGestureListener#onLongPress(android.view.MotionEvent)
	 */
	public void onLongPress(MotionEvent e) {
	}

	/* (non-Javadoc)
	 * @see android.view.GestureDetector.OnGestureListener#onShowPress(android.view.MotionEvent)
	 */
	public void onShowPress(MotionEvent e) {
	}

	/* (non-Javadoc)
	 * @see android.view.GestureDetector.OnGestureListener#onSingleTapUp(android.view.MotionEvent)
	 */
	public boolean onSingleTapUp(MotionEvent e) {
		return false;
	}

	/* (non-Javadoc)
	 * @see android.view.GestureDetector.OnDoubleTapListener#onDoubleTap(android.view.MotionEvent)
	 */
	public boolean onDoubleTap(MotionEvent arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see android.view.GestureDetector.OnDoubleTapListener#onSingleTapConfirmed(android.view.MotionEvent)
	 */
	public boolean onSingleTapConfirmed(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}
}