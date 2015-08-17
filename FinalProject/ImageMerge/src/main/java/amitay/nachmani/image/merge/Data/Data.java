package amitay.nachmani.image.merge.Data;


import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;

import java.util.ArrayList;

import amitay.nachmani.image.merge.General.GeneralInfo;
import amitay.nachmani.image.merge.General.MarkValues;
import amitay.nachmani.image.merge.ImageProcessing.ColorPoint;
import amitay.nachmani.image.merge.ImageProcessing.PointStatus;
import amitay.nachmani.image.merge.Tracker.MovementTracker;
import amitay.nachmani.image.merge.ImageMergeMainActivity;

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
    private ColorPoint[][] mExtractForegroundMat;
    private Point mCenterOfGravity;
    private int mRadius;

    // Status
    private boolean mMarkedImageMaskChanged = true;

    public Data(){ mRadius = 10; }

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
        //mCurrentImage = image.clone();
        mCurrentImage = image;
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
        mMarkedImageMask = new Mat(mSecondImage.rows(),mSecondImage.cols(), ImageMergeMainActivity.MAT_TYPE, MarkValues.NO_MARK_VALUE);
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

    /*
    public void BuildExtractedForegroundKDTree() {

        // Build a KD_tree for two dimensions
        mExtractForegroundKDTree = new KDTree(2);

        // Iterate over all the points in the extracted foreground points and insert there coordinate to KD tree
        for(ColorPoint point : mExtractForeground)
        {
            // The key in the KD tree will be the coordinate of the point
            double[] coordinate = new double[2];
            coordinate[0] = point.x;
            coordinate[1] = point.y;

            // Insert the point to the tree
            mExtractForegroundKDTree.insert(coordinate,point);
        }

    }*/

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

        mMinForegroundPoint.x = mMinForegroundPoint.x - mCenterOfGravity.x;
        mMaxForegroundPoint.x = mMaxForegroundPoint.x - mCenterOfGravity.x;
        mMinForegroundPoint.y = mMinForegroundPoint.y - mCenterOfGravity.y;
        mMaxForegroundPoint.y = mMaxForegroundPoint.y - mCenterOfGravity.y;
    }

    public void UpdateCenterOfGravity(float x,float y)
    {
        mCenterOfGravity.x = x;
        mCenterOfGravity.y = y;
    }

    /**
     * UpdatePointStatus:
     *
     * Change the status of all the points that are in a certain radius from the selected coordinate.
     *
     * @param x
     * @param y
     */
    public void UpdatePointStatus(float x,float y) {

        // Normalize the x and y point according to mCenterOfGravity
        double[] coordinate = new double[2];
        coordinate[0] = (x - mCenterOfGravity.x);
        coordinate[1] = (y - mCenterOfGravity.y);

        // Sanity check if the point is even in the bounding box
        if(coordinate[0] < mMinForegroundPoint.x
                || coordinate[0] > mMaxForegroundPoint.x
                || coordinate[1] < mMinForegroundPoint.y
                || coordinate[1] > mMaxForegroundPoint.y)
        {
            return;
        }

        // Find out if this point is in the extracted foreground points by a radius of mRadius
        int[] matCoordinate = new int[2];
        matCoordinate[0] = (int)(coordinate[0] - mMinForegroundPoint.x);
        matCoordinate[1] = (int)(coordinate[1] - mMinForegroundPoint.y);

        for(int i = matCoordinate[1] - mRadius ; i <= matCoordinate[1] + mRadius ; i++)
        {
            for(int j = matCoordinate[0] - mRadius ; j <= matCoordinate[0] + mRadius ; j++)
            {
                if(i >= 0 && i < mExtractForegroundMat.length && j >= 0 && j < mExtractForegroundMat[0].length)
                {
                    if (mExtractForegroundMat[i][j] != null) {
                        mExtractForegroundMat[i][j].mStatus = PointStatus.UNACTIVE;
                    }
                }
            }
        }

        /*
        for(ColorPoint point : mExtractForeground)
        {
            // Calculate the distance between the point and x and y use manhattan distance
            //double distance = Math.sqrt((point.x - coordinate[0])*(point.x - coordinate[0]) + (point.y - coordinate[1])*(point.y - coordinate[1]));
            double xCordDistance = Math.abs((point.x - coordinate[0]));
            double yCordDistance = Math.abs((point.y - coordinate[1]));
            if(xCordDistance > mRadius || yCordDistance > mRadius) { continue; }

            double distance = xCordDistance + yCordDistance;

            if(distance < mRadius )
            {
                // If we the point is indeed in the radius of change then change extracted foreground points change her status to UNACTIVE
                point.mStatus = PointStatus.UNACTIVE;
            }
        }
        */
        //double newX = (double)(x - mCenterOfGravity.x);
        //double newY = (double)(y - mCenterOfGravity.y);
        /*
        // Get the closest points to the selected coordinate from the kdtree
        Object[] listOfPoints = mExtractForegroundKDTree.nearest(coordinate, mRadius);

        // Go over all the points that are close enough and change there status
        for(int i = 0 ; i < listOfPoints.length ; i++)
        {
            ColorPoint point = (ColorPoint) listOfPoints[i];
            point.mStatus = PointStatus.UNACTIVE;
        }
        */
    }

    /**
     * UpdatePointStatus:
     *
     * Take the last track that was over and delete the points in the track
     *
     * @param tracks
     */
    public void UpdatePointStatus(ArrayList<MovementTracker> tracks)
    {
        // Get the last track
        MovementTracker lastTrack = tracks.get(tracks.size() - 1);

        // Update each point deletion according to the track points
        for(Point point : lastTrack.GetMarkedPoints())
        {
            UpdatePointStatus((float)point.x,(float)point.y);
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
        mKmeansMatrix = null;
        System.gc();
    }

    public void ReleaseKmeansBestLabelsMatrix()
    {
        mKmeansBestLabels.release();
        mKmeansBestLabels = null;
        System.gc();
    }

    public void ReleaseImageMask()
    {
        mMarkedImageMask.release();
        mMarkedImageMask = null;
        System.gc();
    }

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

    public void CleanMemory()
    {
        // Clean all the memory
        mCurrentImage = null;
        mFirstImage = null;
        mSecondImage = null;
        mMarkedImageMask = null;
        mForegroundImage = null;

        // Kmeans
        mKmeansMatrix = null;
        mKmeansBestLabels = null;

        // Tracks data
        mBackgroundPixels = null;
        mForegroundPixels = null;
        mMinForegroundPoint = null;
        mMaxForegroundPoint = null;

        // Extract foreground
        mExtractForeground = null;
        mCenterOfGravity = null;
        //mExtractForegroundKDTree = null;

    }


    /**
     * BuildExtractedForegroundMatrix:
     *
     * Builds a matrix with a reference to the color points this is done foe easy way to delete points in a radius.
     * we don't use this for the coloring itself becaues the array liat is faster when there is not a lot of points in the
     * block rectangle
     */
    public void BuildExtractedForegroundMatrix()
    {
        // Build the extracted foregroundPoint Mat for easy of seraching
        //mExtractForegroundMat = new Mat((int)(mMaxForegroundPoint.y -  mMinForegroundPoint.y),(int)(mMaxForegroundPoint.x -  mMinForegroundPoint.x), ImageMergeMainActivity.MAT_TYPE);
        mExtractForegroundMat = new ColorPoint[(int)(mMaxForegroundPoint.y -  mMinForegroundPoint.y)+1][(int)(mMaxForegroundPoint.x -  mMinForegroundPoint.x)+1];

        // Go over all the points in mExtractForeground an connect them to the matrix
        for(ColorPoint point : mExtractForeground)
        {
            mExtractForegroundMat[(int)(point.y-mMinForegroundPoint.y)][(int)(point.x-mMinForegroundPoint.x)] = point;
        }
    }
}
