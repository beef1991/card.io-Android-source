package io.card.payment;

/* CardScannerTester.java
 * See the file "LICENSE.md" for the full license governing this code.
 */

import java.util.Iterator;

import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;

/**
 * This class is used for Robotium testing ONLY!
 * 
 * ALL classes that match *Tester are excluded from the library jar. As such, they should only be
 * accessed by reflection.
 */
public class CardScannerTester extends CardScanner {
    private static final String TAG = "CardScannerTester";
    private static Iterator<byte[]> recording = null;

    static final long FRAME_INTERVAL = (long) (1000.0 / 30);

    private Handler mHandler = new Handler();
    private int testFrameCount = 0;
    private boolean scanAllowed = false;

    public CardScannerTester(CardIOActivity scanActivity, int currentFrameOrientation) {
        super(scanActivity, currentFrameOrientation);
        // TODO Auto-generated constructor stub
        useCamera = false;
    }

    private Runnable frameRunner = new Runnable() {
        @Override
        public void run() {
            if (!scanAllowed)
                return;
            if (recording == null) {
                Log.e(TAG, "null recording!");
                return;
            }
            if (recording.hasNext()) {
                Log.i(TAG, "Setting test frame: " + testFrameCount++);
                onPreviewFrame(recording.next(), null);
                mHandler.postDelayed(this, FRAME_INTERVAL);
            } else {
                Log.w(TAG, "No more frames left at " + testFrameCount);
                mHandler.postDelayed(expireRunner, 5000); // give up after 5 sec.
            }
        }
    };

    private Runnable expireRunner = new Runnable() {
        @Override
        public void run() {
            if (!scanAllowed)
                return;
            mScanActivityRef.get().finish();
        }
    };

    public static void setRecording(Iterator<byte[]> r) {
        recording = r;
    }

    @Override
    boolean resumeScanning(SurfaceHolder holder) {
        boolean result = super.resumeScanning(holder);
        scanAllowed = true;
        mHandler.postDelayed(frameRunner, FRAME_INTERVAL);
        return result;
    }

    @Override
    public void pauseScanning() {
        scanAllowed = false;
        super.pauseScanning();
    }
}
