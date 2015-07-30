package amitay.nachmani.image.merge.Data;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;

import java.util.ArrayList;

import amitay.nachmani.image.merge.General.MarkValues;
import amitay.nachmani.image.merge.ImageProcessing.ColorPoint;
import amitay.nachmani.image.merge.ImageProcessing.PointStatus;
import amitay.nachmani.image.merge.Tracker.MovementTracker;
import amitay.nachmani.image.merge.Tutorial1Activity;

/**
 * Created by Amitay on 20-Jul-15.
 */
public class Data {

    // Images
    private Mat mCurrentImage;
    private Mat mFirstImage;
    private Mat mSecondImage;
    private Mat mMarkedImageMask;
    private Mat mForegroundImage;

    // Kmeans
    private Mat mKmeansMatrix;
    private Mat mKmeansBestLabels;

    // Tracks data
    private ArrayList<Point> mBackgroundPixels;
    private ArrayList<Point> mForegroundPixels;
    private Point mMinForegroundPoint;
    private Point mMaxForegroundPoint;

    // Extract foreground
    private ArrayList<ColorPoint> mExtractForeground;
    private Point mCenterOfGravity;
    private int mRadius;

    // Status
    private boolean mMarkedImageMaskChanged = true;

    public Data(){ mRadius = 10; };

    public void Initialize(int height, int width, int cvType)
    {
        mCurrentImage = new Mat(height,width,cvType);
    }

    public Data(int height, int width, int cvType)
    {
        mCurrentImage = new Mat(height,width,cvType);
    }
    public void SetCurrentImage(Mat image)
    {
        mCurrentImage = image.clone();
    }

    public void SetFirstImage()
    {
        mFirstImage = mCurrentImage.clone();
    }

    public void SetSecondImage()
    {
        mSecondImage = mCurrentImage.clone();
    }

    public void SetMarkedImageMask()
    {
        // Create a three value mask
        // (0,0,0):         a pixel with no mark
        // (124,124,124):   a pixel marked as background
        // (255,255,255):   a pixel marked as foreground
        mMarkedImageMask = new Mat(mSecondImage.rows(),mSecondImage.cols(), Tutorial1Activity.MAT_TYPE, MarkValues.NO_MARK_VALUE);
    }

    public void SetMarkedImageMask(Mat newMask)
    {
        mMarkedImageMask = newMask;
    }

    public void SetKmeansMatrix(Mat kMeansMatrix)
    {
        mKmeansMatrix = kMeansMatrix;
    }

    public void SetKmeansBestLabels()
    {

        mKmeansBestLabels = new Mat((int)mSecondImage.total(),1, CvType.CV_32SC1,new Scalar(0));
    }

    public void SetKmeansBestLabels(Mat labels)
    {
        mKmeansBestLabels = labels;
    }

    public void SetForegroundImage()
    {
        mForegroundImage = new Mat(mSecondImage.rows(),mSecondImage.cols(),mSecondImage.type());
    }

    public void SetExtractForeground()
    {
        mExtractForeground = new ArrayList<ColorPoint>();
    }

    /**
     * InitializeForegroundBackgroundPixels:
     *
     * Initialize the lists to store the background pixels nad foreground pixels
     *
     */
    public void InitializeForegroundBackgroundPixels()
    {
        mBackgroundPixels = new ArrayList<Point>();
        mForegroundPixels = new ArrayList<Point>();
    }

    public void AddBackgroundPixel(Point point)
    {
        mBackgroundPixels.add(point);
    }

    public void AddForegroundPixel(Point point)
    {
        mForegroundPixels.add(point);
    }

    public void AddExtractForegroundPoint(ColorPoint point)
    {
        mExtractForeground.add(point);
    }

    public void ExtractMinMaxForegroundPoint()
    {
        // Go over the point in the foreground and find the bounding box
        mMinForegroundPoint = new Point(mSecondImage.rows(),mSecondImage.cols());
        mMaxForegroundPoint = new Point(0,0);
        for(Point p : mForegroundPixels)
        {
            if(p.x > mMaxForegroundPoint.x) { mMaxForegroundPoint.x = p.x; }
            if(p.y > mMaxForegroundPoint.y) { mMaxForegroundPoint.y = p.y; }
            if(p.x < mMinForegroundPoint.x) { mMinForegroundPoint.x = p.x; }
            if(p.y < mMinForegroundPoint.y) { mMinForegroundPoint.y = p.y; }
        }
    }

    /**
     * CalculateExtractForegroundCenterOfGravity:
     *
     * Calculate the center of gravity for the extracted foreground points.
     * This is done for easy way to calculate were to draw thoose points after the user moved them.
     *
     */
    public void CalculateExtractForegroundCenterOfGravity()
    {
        // The center of mass for the pixes for every coordinate is the sum of coordinates divide by number of points
        mCenterOfGravity = new Point(0,0);

        for(ColorPoint point : mExtractForeground)
        {
            mCenterOfGravity.x = mCenterOfGravity.x + point.x;
            mCenterOfGravity.y = mCenterOfGravity.y + point.y;
        }
        mCenterOfGravity.x =  mCenterOfGravity.x / mExtractForeground.size();
        mCenterOfGravity.y =  mCenterOfGravity.y / mExtractForeground.size();

    }

    /**
     * NormallizeExtractForegroundPointsByCenterOfMass:
     *
     * The idea is that every time we would draw those points we will add the center of mass to there coordinate.
     * so each time the user move the extracted foreground we only change the center of mass for the points.
     *
     */
    public void NormallizeExtractForegroundPointsByCenterOfMass()
    {
        for(ColorPoint point : mExtractForeground)
        {
            point.x = point.x - mCenterOfGravity.x;
            point.y = point.y - mCenterOfGravity.y;
        }
    }

    public void UpdateCenterOfGravity(float x,float y)
    {
        mCenterOfGravity.x = x;
        mCenterOfGravity.y = y;
    }

    public void UpdatePointStatus(float x,float y)
    {
        // Normalize the x and y point according to mCenterOfGravity
        int newX = (int)(x - mCenterOfGravity.x);
        int newY = (int)(y - mCenterOfGravity.y);

        // Find out if this point is in the extracted foreground points by a radius of mRadius
        for(ColorPoint point : mExtractForeground)
        {

            // Calculate the distance between the point and x and y
            double distance = Math.sqrt((point.x - newX)*(point.x - newX) + (point.y - newY)*(point.y - newY));
            if(distance < mRadius )
            {
                // If we the point is indeed in the radius of change then change extracted foreground points change her status to UNACTIVE
                point.mStatus = PointStatus.UNACTIVE;
            }
        }
    }

    public Mat GetFirstImage()
    {
        return mFirstImage;
    }

    public Mat GetSecondImage()
    {
        return mSecondImage;
    }

    public Mat GetMarkedImageMask()
    {
        return mMarkedImageMask;
    }

    public Mat GetCurrentImage()
    {
        return mCurrentImage;
    }

    public Mat GetmKmeansMatrix()
    {
        return mKmeansMatrix;
    }

    public Mat GetKmeansBestLabels()
    {
        return mKmeansBestLabels;
    }

    public Mat GetForegroundImage()
    {
            return mForegroundImage;
    }

    public Point GetForegroundMaxPoint() { return mMaxForegroundPoint; }

    public Point GetForegroundMinPoint() { return mMinForegroundPoint; }

    public ArrayList<ColorPoint> GetExtractForegroundPoints() { return mExtractForeground; }

    public ArrayList<Point> GetBackgroundPoints()
    {
        return mBackgroundPixels;
    }

    public ArrayList<Point> GetForeroundPoints()
    {
        return mForegroundPixels;
    }

    public Point GetCenterOfGravity()
    {
        return mCenterOfGravity;
    }

    public void ReleaseCurrentImage()
    {
        mCurrentImage.release();
    }

    public void ReleaseKmeansMatrix()
    {
        mKmeansMatrix.release();
    }

    public void ReleaseKmeansBestLabelsMatrix() { mKmeansBestLabels.release(); }

    /**
     * MarkPixlesInMarkedImage:
     *
     * marks the pixels that were marked by the user when he scribble a line along the screen.
     *
     * @param tracker
     */
    public void MarkPixelsInMarkedImage(MovementTracker tracker)
    {
        // Go over all the points that been marked by the user and marked the line between eac two of them
        for(int i = 0 ; i < tracker.GetMarkedPoints().size() ; i++)
        {
            //Log.d(GeneralInfo.DEBUG_TAG, Double.toString(tracker.GetPoint(i - 1).x) + " " + Double.toString(tracker.GetPoint(i - 1).y));
            Core.line(mMarkedImageMask, tracker.GetPoint(i - 1), tracker.GetPoint(i), MarkValues.FOREGROUND_VALUE);
            //Core.circle(mMarkedImageMask, tracker.GetPoint(i), 5, MarkValues.FOREGROUND_VALUE,5);
        }
        // After We are done with this mark we can discard this mark track
        tracker.CleanMarkedPoints();

        // Set that the marked image has changed
        mMarkedImageMaskChanged = true;
    }

    /**
     * MarkedMaskChange:
     *
     * Change the status of mMarkedImageMask to flase to notify that there was not a change since the last time we draw this on
     * the UI
     *
     * @return
     */
    public void MarkedMaskChange()
    {
        mMarkedImageMaskChanged = false;
    }

    public boolean IsMarkedMaskChange()
    {
        return mMarkedImageMaskChanged;
    }


}
