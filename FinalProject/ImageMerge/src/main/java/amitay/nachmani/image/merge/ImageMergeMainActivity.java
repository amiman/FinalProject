package amitay.nachmani.image.merge;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.core.Point;

import amitay.nachmani.image.merge.Data.Data;
import amitay.nachmani.image.merge.General.ApplicationStage;
import amitay.nachmani.image.merge.General.ButtonAction;
import amitay.nachmani.image.merge.General.GeneralInfo;
import amitay.nachmani.image.merge.General.MarkValues;
import amitay.nachmani.image.merge.ImageProcessing.ImageProcessing;
import amitay.nachmani.image.merge.Tracker.MovementTracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class ImageMergeMainActivity extends Activity implements CvCameraViewListener2 {

    // Constants
    public  static final int    MAT_TYPE                    = CvType.CV_8UC4;
    private static final String TAG                         = "OCVSample::Activity";
    private static final String FOREGROUND                  = "FOREGROUND";
    private static final String BACKGROUND                  = "BACKGROUND";
    private static final String DONE                        = "DONE";
    private static final String MOVE_TO_BACK                = "MOVE TO BACKGROUND";
    private static final String MOVE_FOREGROUND             = "MOVE EXTRACT AREA";
    private static final String PROGRESS_MESSAGE            = "Running Algorithm Please Wait";
    private static final String CANT_CREATE_FOLDER          = "Couldn't create folder for app so the image wpuldnot be saved to phone";
    private static final String POSTIVE_DIALOG_BUTTON       = "OK";

    // Camera
    private CameraBridgeViewBase    mOpenCvCameraView;
    private Camera                  mCamera    =   null;
    private int                     mFrameWidth;
    private int                     mFrameHeight;

    // Data
    private Data                    mData;

    // Status
    private ApplicationStage mApplicationStage;
    private MarkValues.Marking mMark = MarkValues.Marking.BACKGROUND;
    private ButtonAction mButtonAction = ButtonAction.MOVE_FOREGROUND;

    // Movement tracker
    private MovementTracker mTracker;
    private ArrayList<MovementTracker> mTracks;

    // Image
    private Bitmap mBitmap;
    private float mScale = 0;

    // View
    //private Button mTakePictureButton;
    private ImageButton mTakePictureButton;
    private Button mMarkBackgroundButton;
    private Button mMarkForegroundButton;
    private Button mDoneButton;
    private Button mMoveForegroundPixelsToBack;
    private Button mMoveForeground;
    private String[] mImageName;
    private SurfaceHolder mNewSurfaceHolder;

    // Progress bar
    private ProgressDialog mProgressBar;
    private int mProgressBarStatus;
    private Handler mProgressBarHandler;

    /**
     * BaseLoaderCallback:
     *
     * Loader of the opencv libary
     */
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    if(mOpenCvCameraView != null) {
                        mOpenCvCameraView.enableView();
                    }
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public ImageMergeMainActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        Log.d(GeneralInfo.DEBUG_TAG,"onRestoreInstanceState");
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onRestart() {
        Log.d(GeneralInfo.DEBUG_TAG, "Restart");
        super.onRestart();
    }

    @Override
    protected void onStart() {
        Log.d(GeneralInfo.DEBUG_TAG, "OnStart");
        super.onStart();
    }

    @Override
    protected void onStop() {
        Log.d(GeneralInfo.DEBUG_TAG, "OnStop");
        super.onStop();
    }

    @Override
    public void onActivityReenter(int resultCode, Intent data) {
        Log.d(GeneralInfo.DEBUG_TAG, "onActivityReenter");
        super.onActivityReenter(resultCode, data);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        Log.d(GeneralInfo.DEBUG_TAG, "onWindowFocusChanged");
        Log.d(GeneralInfo.DEBUG_TAG, "onWindowFocusChanged: " + mApplicationStage);
        super.onWindowFocusChanged(hasFocus);
        switch(mApplicationStage)
        {
            case SEGMENTATION_MARK:
                DrawNewTracksOnView();
                break;
            case MOVE_FOREGROUND_AND_EDIT:
                DrawAlgorithmResult();
                break;
            default:
                break;
        }

    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(GeneralInfo.DEBUG_TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_9, this, mLoaderCallback);
        /*if (!OpenCVLoader.initDebug()) {
            Log.i(TAG, "problem loading");
        }*/

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Set camera view base
        setContentView(R.layout.main_surface_view);
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.tutorial1_activity_java_surface_view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);

        // Get Button
        //mTakePictureButton = (Button) findViewById(R.id.btnTakePicture);
        mTakePictureButton = (ImageButton) findViewById(R.id.btnTakePicture);
        mTakePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SaveImageToData();
            }
        });

        // Choose smaller resolution for images
        mCamera = Camera.open();
        Camera.Parameters params = mCamera.getParameters();
        mCamera.release();

        // Create data
        mData = new Data();

        // Create tracker
        mTracker = new MovementTracker();
        mTracks = new ArrayList<MovementTracker>();

        // bitmap and scale
        mBitmap = null;

        // Change state to initialization
        mApplicationStage = ApplicationStage.INITIALIZATION;
    }

    @Override
    public void onPause()
    {
        Log.d(GeneralInfo.DEBUG_TAG, "Pause");
        switch(mApplicationStage) {
            case SEGMENTATION_MARK_INITIALIZATION:
                break;
            case SEGMENTATION_MARK:
                break;
            case SEGMENTATION_ALGORITHM:
                break;
            case MOVE_FOREGROUND_AND_EDIT:
                break;
            default:
                if (mOpenCvCameraView != null)
                    mOpenCvCameraView.disableView();
                break;
        }

        super.onPause();
        Log.d(GeneralInfo.DEBUG_TAG, "End Pause");
    }

    @Override
    public void onResume()
    {
        super.onResume();
        Log.d(GeneralInfo.DEBUG_TAG, "Resume");
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_9, this, mLoaderCallback);

        Log.d(GeneralInfo.DEBUG_TAG, mApplicationStage.name());

        // According to the current stage initialize the proper stage
        switch(mApplicationStage)
        {
            case SEGMENTATION_MARK:
                DrawNewTracksOnView();
                break;
            default:
                if (mOpenCvCameraView != null)
                    mOpenCvCameraView.enableView();
                break;
        }
        Log.d(GeneralInfo.DEBUG_TAG, "End Resume");
    }

    public void onDestroy() {
        Log.d(GeneralInfo.DEBUG_TAG, "Destroy");
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return true;
    }

    /**
     *
     * @param width -  the width of the frames that will be delivered
     * @param height - the height of the frames that will be delivered
     */
    public void onCameraViewStarted(int width, int height) {
        Log.i(TAG, "onCameraViewStarted");
    }

    /**
     * onCameraViewStopped:
     *
     * When we stop camera we start to show the marked image
     */
    public void onCameraViewStopped() {

        Log.d(GeneralInfo.DEBUG_TAG, "onCameraViewStopped");

        /*
        // When we stop retrieving images we change the last image to be the second image wait for tracks to be made
        Mat combinedMat = ImageProcessing.DrawTracks(mData.GetSecondImage(), mTracks);

        // Allocate space for the bitmap that will contain the image
        mBitmap = Bitmap.createBitmap(mFrameWidth, mFrameHeight, Bitmap.Config.ARGB_8888);

        // Convert the combindedMat to bitmap
        Utils.matToBitmap(combinedMat, mBitmap);

        // Display the bitmap on the canvas
        DisplayBitmap();
        */
    }

    /**
     * onCameraFrame:
     *
     * This method responsible for the frame that will be displayed.
     * It gets an input frame and manipulate it and then sending the manipulated results to be displayed.
     * This manipulation is a function of the application stage.
     *
     * @param inputFrame
     * @return
     */
    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {

        // Check application stage
        if(mApplicationStage == ApplicationStage.INITIALIZATION)
        {
            Mat returnedImage = inputFrame.rgba();

            // Initialize width and height
            mFrameHeight = returnedImage.height();
            mFrameWidth =  returnedImage.width();

            // Initialize scale
            InitializeScale();

            // Initialize mData
            mData.Initialize(returnedImage.height(), returnedImage.width(), returnedImage.type());

            // Save current frame
            mData.SetCurrentImage(returnedImage);

            // Change the stage to START
            mApplicationStage = ApplicationStage.START;

            // Return
            return returnedImage;
        } else if(mApplicationStage == ApplicationStage.START) {

            Mat returnedImage = inputFrame.rgba();

            // Save current frame
            mData.SetCurrentImage(returnedImage);

            // TODO: Check if we need another stage or jump stright to FIRST_IMAGE

            // Change the stage to FIRST_IMAGE
            mApplicationStage = ApplicationStage.FIRST_IMAGE;

            // Return
            return returnedImage;

        } else if(mApplicationStage == ApplicationStage.FIRST_IMAGE) {

            Mat returnedImage = inputFrame.rgba();

            // Save current frame
            mData.SetCurrentImage(returnedImage);

            // Return
            return returnedImage;
        } else if(mApplicationStage == ApplicationStage.SECOND_IMAGE) {

            // When we present the image we give the first image as a reference by merging the first image with high opacity

            Mat returnedImage = inputFrame.rgba();

            // Save current frame
            mData.SetCurrentImage(returnedImage);

            // Merge the current frame and the first image
            Mat mergeImage = new Mat(mFrameWidth, mFrameHeight,MAT_TYPE);
            double alpha = 0.3;
            double beta = 1- alpha;
            double gamma = 0;
            Core.addWeighted(mData.GetFirstImage(), alpha, mData.GetCurrentImage(), beta, gamma, mergeImage);

            return  mergeImage;
        } else if(mApplicationStage == ApplicationStage.SEGMENTATION_MARK_INITIALIZATION) {

            // Show the combination between the second image and the marked areas
            Mat combinedMat = ImageProcessing.DrawTracks(mData.GetSecondImage(),mTracks);

            // We don't need the camera any more so stop camera
            mOpenCvCameraView.disableFpsMeter();
            mOpenCvCameraView.disableView();

            //InitializeMarkSegmentationView();

            return combinedMat;
        }


        return inputFrame.rgba();
    }

    /********************************************* Initialization's *************************************************************/
    /**
     * InitializeScale:
     *
     * Initialize bitmap and scale. The scale is the ratio between view and image from the camera.
     */
    private void InitializeScale()
    {
        if ((mOpenCvCameraView.getLayoutParams().width == LayoutParams.MATCH_PARENT) && (mOpenCvCameraView.getLayoutParams().height == LayoutParams.MATCH_PARENT))
            mScale = Math.min(((float) mOpenCvCameraView.getHeight()) / mFrameHeight, ((float) mOpenCvCameraView.getWidth()) / mFrameWidth);
        else
            mScale = 0;
    }

    /**
     * InitializeMarkSegmentationView:
     *
     * This is done once we are in the application stage of SEGMENTATION_MARK_INITIALIZATION
     */
    private void InitializeMarkSegmentationView()
    {
        // Make sure that thee camera has been disconnected
        //mOpenCvCameraView.disableView();

        // First remove the "take picture" button that we don't any more and replace it by a new "Done"  button


        //mOpenCvCameraView.unSetCvCameraViewListener();

        if(mTakePictureButton != null) {
            mNewSurfaceHolder = mOpenCvCameraView.getHolder();
            mOpenCvCameraView = null;
            ViewGroup layout = (ViewGroup) mTakePictureButton.getParent();
            if (null != layout) {//for safety only  as you are doing onClick
                layout.removeView(mTakePictureButton);
                mTakePictureButton = null;
                //layout.removeView(mOpenCvCameraView);
            }
        }

        mDoneButton = new Button(this);
        mDoneButton.setText(DONE);
        mDoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Change application stage
                mApplicationStage = ApplicationStage.SEGMENTATION_ALGORITHM;

                // Go to run segmentation algorithm view
                InitializeRunSegmentationAlgorithmView();
            }
        });
        FrameLayout.LayoutParams frameLayoutParmasAlgorithm = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        frameLayoutParmasAlgorithm.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        mDoneButton.setLayoutParams(frameLayoutParmasAlgorithm);

        // Second add the tools we need for image segmentation and set there position in the frame layout

        // Background Button
        mMarkBackgroundButton = new Button(this);
        mMarkBackgroundButton.setText(BACKGROUND);
        mMarkBackgroundButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Change current chosen mark to be background
                mMark = MarkValues.Marking.BACKGROUND;
            }
        });
        FrameLayout.LayoutParams frameLayoutParmasBackground = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        frameLayoutParmasBackground.gravity = Gravity.LEFT;
        mMarkBackgroundButton.setLayoutParams(frameLayoutParmasBackground);

        // Foreground Button
        mMarkForegroundButton = new Button(this);
        mMarkForegroundButton.setText(FOREGROUND);
        mMarkForegroundButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Change current chosen mark to be foreground
                mMark = MarkValues.Marking.FOREGROUND;
            }
        });
        FrameLayout.LayoutParams frameLayoutParmasForeground = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        frameLayoutParmasForeground.gravity = Gravity.RIGHT;
        mMarkForegroundButton.setLayoutParams(frameLayoutParmasForeground);

        // Third Add the buttons to the view
        FrameLayout frameMainLayout = (FrameLayout) findViewById(R.id.frame_main_layout);
        frameMainLayout.addView(mMarkBackgroundButton);
        frameMainLayout.addView(mMarkForegroundButton);
        frameMainLayout.addView(mDoneButton);

        // Forth Change the application stage to SEGMENTATION_MARK
        mApplicationStage = ApplicationStage.SEGMENTATION_MARK;

        // Fifth check if we can deallocate memory
        MemoryChecker();
    }

    /**
     * InitializeRunSegmentationAlgorithmView:
     *
     * After the user marked the are of background and foreground we can run the segmentation algorithm
     *
     */
    private void InitializeRunSegmentationAlgorithmView()
    {
        // Create a progress bar for the algorithm progress
        FrameLayout frameMainLayout = (FrameLayout) findViewById(R.id.frame_main_layout);

        // prepare for a progress bar dialog
        mProgressBar = new ProgressDialog(frameMainLayout.getContext());
        mProgressBar.setCancelable(true);
        mProgressBar.setMessage(PROGRESS_MESSAGE);
        mProgressBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressBar.setProgress(0);
        mProgressBar.setMax(100);
        mProgressBar.show();

        //reset progress bar status
        mProgressBarStatus = 0;

        // create the handler for the progress bar
        mProgressBarHandler = new Handler();

        new Thread(new Runnable() {
            public void run() {
                while (mProgressBarStatus < 100) {

                    // process some tasks
                    mProgressBarStatus = ImageProcessing.RunSegmentationAlgorithm(mData,mTracks,mProgressBarStatus);

                    // your computer is too fast, sleep 1 second
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }


                    // Update the progress bar
                    mProgressBarHandler.post(new Runnable() {
                        public void run() {
                            mProgressBar.setProgress(mProgressBarStatus);
                        }
                    });
                }

                // ok, finished running algorithm
                if (mProgressBarStatus >= 100) {

                    // sleep 2 seconds, so that you can see the 100%
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    // close the progress bar dialog
                    mProgressBar.dismiss();

                    // Run basic calculation for MOVE_FOREGROUND_INITIALIZATION stage
                    mData.CalculateExtractForegroundCenterOfGravity();
                    mData.NormallizeExtractForegroundPointsByCenterOfMass();

                    // draw result
                    DrawAlgorithmResult();

                    // Change application stage to MOVE_FOREGROUND_AND_EDIT_INITIALIZATION
                    mApplicationStage = ApplicationStage.MOVE_FOREGROUND_AND_EDIT_INITIALIZATION;

                    // Change mTracks to start track deletion tracks
                    InitializeTracksForDeletionTracks();
                }
            }
        }).start();

        // Initialize the new view for this stage
        InitializeMoveForegroundAndEdit();
    }

    /**
     * InitializeTracksForDeletionTracks:
     *
     * When we start the MoveForegroundAndEdit stage we going to track the deletion the user is doing.
     */
    private void InitializeTracksForDeletionTracks()
    {
        // Clear current tracks
        mTracks.clear();
        mTracks = null;
        mTracker = null;
        System.gc();

        // Create new trcks array list
        mTracks = new ArrayList<MovementTracker>();

    }

    private void InitializeMoveForegroundAndEdit()
    {
        // Remove the unnecessary buttons
        ViewGroup layout = (ViewGroup) mMarkBackgroundButton.getParent();
        if(null!=layout)
        {
            //for safety only  as you are doing onClick
            layout.removeView(mMarkBackgroundButton);
            layout.removeView(mMarkForegroundButton);
        }

        // Add move extract foreground to background image button
        mMoveForegroundPixelsToBack = new Button(this);
        mMoveForegroundPixelsToBack.setText(MOVE_TO_BACK);
        mMoveForegroundPixelsToBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Change the action to move foreground to back
                mButtonAction = ButtonAction.MOVE_FOREGROUND_TO_BACK;
            }
        });
        FrameLayout.LayoutParams frameLayoutParmasForegroundToBack = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        frameLayoutParmasForegroundToBack.gravity = Gravity.LEFT;
        mMoveForegroundPixelsToBack.setLayoutParams(frameLayoutParmasForegroundToBack);

        // Add move foreground button
        mMoveForeground = new Button(this);
        mMoveForeground.setText(MOVE_FOREGROUND);
        mMoveForeground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Change the action to move foreground pixels
                mButtonAction = ButtonAction.MOVE_FOREGROUND;
            }
        });
        FrameLayout.LayoutParams frameLayoutParmasMoveForeground = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        frameLayoutParmasMoveForeground.gravity = Gravity.RIGHT;
        mMoveForeground.setLayoutParams(frameLayoutParmasMoveForeground);

        // Change the action the DONE button does
        mDoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // When the user done moving the extract foreground go to save image stage
                SaveImageInitilazation();
            }
        });

        // Add buttons to view
        FrameLayout frameMainLayout = (FrameLayout) findViewById(R.id.frame_main_layout);
        frameMainLayout.addView(mMoveForegroundPixelsToBack);
        frameMainLayout.addView(mMoveForeground);

        // Change application stage
        mApplicationStage = ApplicationStage.MOVE_FOREGROUND_AND_EDIT;

    }

    /**
     * SaveImageInitilazation:
     *
     * Save the final image to app folder. Convert it to bitmap and let the user choose a name for the image.
     */
    private void SaveImageInitilazation()
    {
        // Check if we already created a folder fot the application
        File appFolder = new File(GeneralInfo.APPLICATION_PATH);
        boolean successCreatingDir = true;
        if(!appFolder.isDirectory())
        {
            // If we didn't already created the folder than create it now
            successCreatingDir = appFolder.mkdir();
            if(!successCreatingDir)
            {
                Toast.makeText(getApplicationContext(), CANT_CREATE_FOLDER, Toast.LENGTH_SHORT).show();
            }
        }

        // If we are here that means that the app has already got a folder on phone.

        // Pop up a dialog for image name
        FrameLayout frameMainLayout = (FrameLayout) findViewById(R.id.frame_main_layout);
        PopDialogFileName(frameMainLayout);

    }

    /**
     * PopDialogFileName:
     *
     *
     * @return
     */
    private void PopDialogFileName(final View v)
    {
        // Pop up a dialog for the user to insert the image name
        // Build a dialog box
        AlertDialog.Builder alert = new AlertDialog.Builder(v.getContext());
        final EditText inputImageName = new EditText(v.getContext());
        inputImageName.setTag(1);

        LinearLayout dialogLayout = new LinearLayout(v.getContext());
        dialogLayout.addView(inputImageName);
        alert.setView(dialogLayout);

        mImageName = new String[1];
        alert.setPositiveButton(POSTIVE_DIALOG_BUTTON, new DialogInterface.OnClickListener() {
            //@Override
            public void onClick(DialogInterface dialog, int which) {

                // Get the input text from edit text view
                mImageName[0] = inputImageName.getText().toString();

                Log.d(GeneralInfo.DEBUG_TAG,mImageName[0]);

                // After we get the file name we save the image
                SaveImageToDisk();
            }
        });

        alert.show();
    }

    /********************************************* Initialization's  end *************************************************************/

    /***
     * SaveImageToData:
     *
     * Saves the current image to be the first image of the merge set or the second image of the merge set according to
     * the application stage. It save sit to the data object.
     *
     */
    public void SaveImageToData()
    {
        if(mApplicationStage == ApplicationStage.FIRST_IMAGE)
        {
            // Save the current image to be the first image
            mData.SetFirstImage();

            // Change stage to SECOND_IMAGE
            mApplicationStage = ApplicationStage.SECOND_IMAGE;

        } else if(mApplicationStage == ApplicationStage.SECOND_IMAGE) {

            // Save the current image to be the second image
            mData.SetSecondImage();

            // Release data
            mData.ReleaseCurrentImage();

            // Change stage to SEGMENTATION_MARK_INITIALIZATION
            mApplicationStage = ApplicationStage.SEGMENTATION_MARK_INITIALIZATION;

            // Draw current image
            DrawMat(mData.GetSecondImage());

        }
    }

    public void SaveImageToDisk()
    {

        // Get the final merge image
        Mat imageMerge = ImageProcessing.MergeMatWithPoints(mData.GetFirstImage(), mData);

        // Allocate space for the bitmap that will contain the image
        if(mBitmap == null) {
            mBitmap = Bitmap.createBitmap(mFrameWidth, mFrameHeight, Bitmap.Config.ARGB_8888);
        }

        // Convert the algorithm result to bitmap
        Utils.matToBitmap(imageMerge, mBitmap);

        // Save the image
        File appFolder = new File(GeneralInfo.APPLICATION_PATH);

        // Create imageDir
        File pathToImage = new File(appFolder,mImageName[0]);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(pathToImage);

            // Use the compress method on the BitMap object to write image to the OutputStream
            mBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);

            // Close output streams
            fos.close();

            // Change application status
            mApplicationStage = ApplicationStage.FINALIZE;

            // Clean memory
            MemoryChecker();

            // Go back to start activity
            finish();

            Log.d(GeneralInfo.DEBUG_TAG,"after finsish()");
            return;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    ///////////////////////////////////////////////////// On touch ///////////////////////////////////////////////////////////
    /**
     * onTouchEvent:
     *
     * Monitors the touch the user do. As a function of application stage decide what to do.
     *
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event){

        // If we are not in stage of marking do not do anything
        if(mApplicationStage != ApplicationStage.SEGMENTATION_MARK_INITIALIZATION
                && mApplicationStage != ApplicationStage.SEGMENTATION_MARK
                && mApplicationStage != ApplicationStage.MOVE_FOREGROUND_AND_EDIT
                && mApplicationStage != ApplicationStage.MOVE_FOREGROUND_AND_EDIT_INITIALIZATION)
        {
            return super.onTouchEvent(event);
        } else if(mApplicationStage == ApplicationStage.SEGMENTATION_MARK_INITIALIZATION) {

            // Initialize the new view for this stage
            InitializeMarkSegmentationView();

        } else if (mApplicationStage == ApplicationStage.MOVE_FOREGROUND_AND_EDIT_INITIALIZATION) {

            // Initialize the new view for this stage
            InitializeMoveForegroundAndEdit();
        }


        if(mApplicationStage == ApplicationStage.SEGMENTATION_MARK_INITIALIZATION || mApplicationStage == ApplicationStage.SEGMENTATION_MARK)
        {
            return SegmentationTouchEvent(event);
        }

        if(mApplicationStage == ApplicationStage.MOVE_FOREGROUND_AND_EDIT)
        {
            return MoveForegroundAndEditTouchEvent(event);
        }

        return super.onTouchEvent(event);
    }

    /**
     * SegmentationTouchEvent:
     *
     * The behavior touch
     * @param event
     */
    private boolean SegmentationTouchEvent(MotionEvent event)
    {
        // If this is stage of marking start to mark the pixels that are marked
        int action = MotionEventCompat.getActionMasked(event);

        float x;
        float y;

        switch(action) {
            case (MotionEvent.ACTION_DOWN) :

                // Start tracking finger

                // Create a new tracker for the track
                mTracker = new MovementTracker(mMark);

                // Get the coordinate of the touch event
                x = event.getAxisValue(MotionEvent.AXIS_X);
                y = event.getAxisValue(MotionEvent.AXIS_Y);

                // Add the point to the tracker
                mTracker.AddPoint(new Point(x, y));

                return true;

            case (MotionEvent.ACTION_MOVE) :

                // Get the coordinate of the touch event
                x = event.getAxisValue(MotionEvent.AXIS_X);
                y = event.getAxisValue(MotionEvent.AXIS_Y);

                // Add the point to the tracker
                mTracker.AddPoint(new Point(x, y));

                return true;

            case (MotionEvent.ACTION_UP) :

                // Stop tracking movement

                // Get the coordinate of the touch event
                x = event.getAxisValue(MotionEvent.AXIS_X);
                y = event.getAxisValue(MotionEvent.AXIS_Y);

                // Add the point to the tracker
                mTracker.AddPoint(new Point(x, y));

                // Update marked tracks with the new marked pixels
                mTracks.add(mTracker);

                // Draw new tracks on view
                DrawNewTracksOnView();

                return true;

            default :
                return super.onTouchEvent(event);
        }
    }

    private boolean MoveForegroundAndEditTouchEvent(MotionEvent event) {
        int action = MotionEventCompat.getActionMasked(event);

        float x;
        float y;

        switch(action) {
            case (MotionEvent.ACTION_DOWN) :

                if(mButtonAction == ButtonAction.MOVE_FOREGROUND) { return true; }

                // Initialize new deletion track
                mTracker = new MovementTracker(MarkValues.Marking.NO_MARK);

                // Get the coordinate of the touch event
                x = event.getAxisValue(MotionEvent.AXIS_X);
                y = event.getAxisValue(MotionEvent.AXIS_Y);

                // Add the current point to unactive track
                mTracker.AddPoint(new Point(x,y));

                return true;

            case (MotionEvent.ACTION_MOVE) :

                if(mButtonAction == ButtonAction.MOVE_FOREGROUND) { return true; }

                // Get the coordinate of the touch event
                x = event.getAxisValue(MotionEvent.AXIS_X);
                y = event.getAxisValue(MotionEvent.AXIS_Y);

                // Add the current point to unactive track
                mTracker.AddPoint(new Point(x, y));

                return true;

            case (MotionEvent.ACTION_UP) :

                // Get the coordinate of the touch event
                x = event.getAxisValue(MotionEvent.AXIS_X);
                y = event.getAxisValue(MotionEvent.AXIS_Y);

                // Depending on the current button action decide what to do
                if(mButtonAction == ButtonAction.MOVE_FOREGROUND) {
                    // Update the foreground center of gravity
                    mData.UpdateCenterOfGravity(x, y);
                } else if(mButtonAction == ButtonAction.MOVE_FOREGROUND_TO_BACK) {

                    // Add the current point to unactive track
                    mTracker.AddPoint(new Point(x,y));

                    // Add current deltion track to tracks
                    mTracks.add(mTracker);

                    // UpdatePointStauts
                    mData.UpdatePointStatus(mTracks);
                }

                // Draw new foreground view
                DrawAlgorithmResult();

                return true;

            default :
                return super.onTouchEvent(event);
        }
    }

    ///////////////////////////////////////////////////// End On touch ///////////////////////////////////////////////////////////

    /**
     * DrawNewTracksOnView:
     *
     * This method colors the new tracks on to the second image we have and than display it on the screen
     *
     */
    private void DrawNewTracksOnView() {

        // Combine the tracks we found and the image to be segmented
        Mat combinedMat = ImageProcessing.DrawTracks(mData.GetSecondImage(), mTracks);

        // Allocate space for the bitmap that will contain the image
        if(mBitmap == null) {
            mBitmap = Bitmap.createBitmap(mFrameWidth, mFrameHeight, Bitmap.Config.ARGB_8888);
        }

        // Convert the combindedMat to bitmap
        Utils.matToBitmap(combinedMat, mBitmap);

        // Display the new bitmap
        DisplayBitmap();

    }

    /**
     *
     */
    private void DrawAlgorithmResult()
    {
        // Merge the foreground pixels to the firstImage image
        Mat combinedMat = ImageProcessing.MergeMatWithPoints(mData.GetFirstImage(), mData);

        // Allocate space for the bitmap that will contain the image
        if(mBitmap == null) {
            mBitmap = Bitmap.createBitmap(mFrameWidth, mFrameHeight, Bitmap.Config.ARGB_8888);
        }

        // Convert the algorithm result to bitmap
        Utils.matToBitmap(combinedMat, mBitmap);

        // Display the new bitmap
        DisplayBitmap();
    }

    /**
     * DrawMat:
     *
     * Draws the mat to the surface
     * @param mat
     */
    private void DrawMat(Mat mat)
    {
        // Allocate space for the bitmap that will contain the image
        if(mBitmap == null) {
            mBitmap = Bitmap.createBitmap(mFrameWidth, mFrameHeight, Bitmap.Config.ARGB_8888);
        }

        // Convert the algorithm result to bitmap
        Utils.matToBitmap(mat, mBitmap);

        // Display the new bitmap
        DisplayBitmap();
    }

    /**
     * DisplayBitmap:
     *
     * Displays the current bitmap on view.
     * It converts the bitmap to the view size according to the scale change between view and image
     *
     */
    public void DisplayBitmap() {

        Canvas canvas = null;
        if(mOpenCvCameraView != null) {
            canvas = mOpenCvCameraView.getHolder().lockCanvas();
        } else {
            canvas = mNewSurfaceHolder.lockCanvas();
        }

        if (canvas != null) {
            canvas.drawColor(0, android.graphics.PorterDuff.Mode.CLEAR);
            Log.d(TAG, "mStretch value: " + mScale);

            if (mScale != 0) {
                canvas.drawBitmap(mBitmap, new Rect(0, 0, mBitmap.getWidth(), mBitmap.getHeight()),
                        new Rect((int) ((canvas.getWidth() - mScale * mBitmap.getWidth()) / 2),
                                (int) ((canvas.getHeight() - mScale * mBitmap.getHeight()) / 2),
                                (int) ((canvas.getWidth() - mScale * mBitmap.getWidth()) / 2 + mScale * mBitmap.getWidth()),
                                (int) ((canvas.getHeight() - mScale * mBitmap.getHeight()) / 2 + mScale * mBitmap.getHeight())), null);
            } else {
                canvas.drawBitmap(mBitmap, new Rect(0, 0, mBitmap.getWidth(), mBitmap.getHeight()),
                        new Rect((canvas.getWidth() - mBitmap.getWidth()) / 2,
                                (canvas.getHeight() - mBitmap.getHeight()) / 2,
                                (canvas.getWidth() - mBitmap.getWidth()) / 2 + mBitmap.getWidth(),
                                (canvas.getHeight() - mBitmap.getHeight()) / 2 + mBitmap.getHeight()), null);
            }

            if(mOpenCvCameraView != null) {
                mOpenCvCameraView.getHolder().unlockCanvasAndPost(canvas);
            } else {
                mNewSurfaceHolder.unlockCanvasAndPost(canvas);
            }
        }
    }

    /**
     * MemoryChecker:
     *
     * Every time we finisgh a stage we check what memory can be deallocated
     */
    public void MemoryChecker()
    {
        switch(mApplicationStage) {

            case SEGMENTATION_MARK:
                mTakePictureButton = null;
                System.gc();
                break;
            case SEGMENTATION_ALGORITHM:
                System.gc();
                break;
            case MOVE_FOREGROUND_AND_EDIT:
                mMarkBackgroundButton = null;
                mMarkForegroundButton = null;
                System.gc();
                break;
            case FINALIZE:
                mData.CleanMemory();
                System.gc();
                break;
            default:
                System.gc();
                break;
        }
    }
}
