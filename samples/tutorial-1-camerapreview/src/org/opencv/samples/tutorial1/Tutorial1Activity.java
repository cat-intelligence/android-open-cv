package org.opencv.samples.tutorial1;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraActivity;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.File;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.opencv.samples.tutorial1.Const.NOTE_FE_CAM_DELAY;

public class Tutorial1Activity extends CameraActivity implements CvCameraViewListener2, View.OnTouchListener {
    private static final String TAG = "OCVSample::Activity";

    private CameraBridgeViewBase mOpenCvCameraView;
    private Mat matrixImg;
    private View blinkingView;
    
    private final BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            if (status == LoaderCallbackInterface.SUCCESS) {
                Log.i(TAG, "OpenCV loaded successfully");
                mOpenCvCameraView.enableView();
                mOpenCvCameraView.setOnTouchListener(Tutorial1Activity.this);
                mOpenCvCameraView.setCvCameraViewListener(Tutorial1Activity.this);
            } else {
                super.onManagerConnected(status);
            }
        }
    };
    private boolean showPreview = true;
    private File folder;

    public Tutorial1Activity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.tutorial1_surface_view);

        blinkingView = findViewById(R.id.blinking_effect);
        blinkingView.setOnTouchListener(this);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.tutorial1_activity_java_surface_view);

        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);

        mOpenCvCameraView.setCvCameraViewListener(this);
        folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        Toast.makeText(this, "Touch on screen to take a picture", Toast.LENGTH_LONG)
                .show();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    protected List<? extends CameraBridgeViewBase> getCameraViewList() {
        return Collections.singletonList(mOpenCvCameraView);
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
    }

    public void onCameraViewStopped() {
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        Mat mat = inputFrame.rgba();
        if (!showPreview) {
            this.matrixImg = mat; // take a pickture
            showPreview = true;
        }
        // to the camera view show
        return inputFrame.rgba();
    }

    /**
     * Touch camera to take a picture
     *
     * @param v     The view the touch event has been dispatched to.
     * @param event The MotionEvent object containing full information about
     *              the event.
     * @return True if the listener has consumed the event, false otherwise.
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        showPreview = !showPreview; // not set to const FALSE value because it can forever = false by error of cam
        Log.e(Const.TAG, "Touch showPreview: " + showPreview);
        makeBlinking();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // save picture
                if (matrixImg != null) {
                    String imgName = folder.getAbsolutePath()
                            + "/" + new Date().getTime() + ".png";
                    Log.e(Const.TAG, "imgName: " + imgName);
                    Imgcodecs.imwrite(imgName, matrixImg);
                }
            }
        }, NOTE_FE_CAM_DELAY);
        return false;
    }
    private void makeBlinking() {
        blinkingView.setVisibility(View.VISIBLE);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                blinkingView.setVisibility(View.GONE);
            }
        }, Const.BLINK_DELAY);
    }
}
