package amitay.nachmani.image.merge.BackUp;

import android.content.Context;

import org.opencv.android.JavaCameraView;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

/**
 * Created by Amitay on 22-Jul-15.
 */
public class MyJavaCameraView extends JavaCameraView {

    public MyJavaCameraView(Context context, int cameraId) {
        super(context, cameraId);
    }

    private class JavaCameraFrame implements CvCameraViewFrame {
        @Override
        public Mat gray() {
            return mYuvFrameData.submat(0, mHeight, 0, mWidth);
        }

        @Override
        public Mat rgba() {
            Imgproc.cvtColor(mYuvFrameData, mRgba, Imgproc.COLOR_YUV2RGBA_NV21, 4);
            return mRgba;
        }

        public JavaCameraFrame(Mat Yuv420sp, int width, int height) {
            super();
            mWidth = width;
            mHeight = height;
            mYuvFrameData = Yuv420sp;
            mRgba = new Mat();
        }

        public void release() {
            mRgba.release();
        }

        private Mat mYuvFrameData;
        private Mat mRgba;
        private int mWidth;
        private int mHeight;
    };
}
