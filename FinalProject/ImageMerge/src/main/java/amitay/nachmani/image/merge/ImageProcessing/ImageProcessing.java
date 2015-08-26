package amitay.nachmani.image.merge.ImageProcessing;

import android.graphics.Bitmap;
import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.core.TermCriteria;
import org.opencv.imgproc.Imgproc;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import amitay.nachmani.image.merge.Data.Data;
import amitay.nachmani.image.merge.General.GeneralInfo;
import amitay.nachmani.image.merge.General.MarkValues;
import amitay.nachmani.image.merge.ImageMergeMainActivity;
import amitay.nachmani.image.merge.Tracker.MovementTracker;


/**
 * Created by Amitay on 22-Jul-15.
 */
public class ImageProcessing {

    // 3 color values and 2 coordinates
    private static final int mNumberOfAttributesForKmeans = 5;

    private static AlgorithmName mAlgorithm = AlgorithmName.MY_ALGORITHM;

    /**
     *  DrawTracks:
     *  Draw all the tracks we have in tracks on background image
     *
     *  @param background
     *  @parma tracks
     *  @return
     */
    public static final Mat DrawTracks(Mat background,ArrayList<MovementTracker> tracks)
    {
        Mat newImage = background.clone();

        // Iterate over all the tracks
        for(MovementTracker track : tracks)
        {
            // Get the marking color
            Scalar color = track.GetMarking().GetMarkingColor();

            // Iterate all over the points and connect a line between eac two points
            for(int i = 1 ; i < track.GetMarkedPoints().size() ; i++)
            {
                Core.line(newImage,track.GetPoint(i-1),track.GetPoint(i),color,5);
            }
        }
        return newImage;
    }

    /**
     * MergeMatWithPoints:
     *
     * Go over all the color point and paint them on the background image
     *
     * @param background
     * @param data
     * @return
     */
    public static final Mat MergeMatWithPoints(Mat background,Data data)
    {
        Mat mergeMat = background.clone();
        Point centerOfGravity = data.GetCenterOfGravity();

        // Go over the point the paint there color on the background image
        if(data.GetExtractForegroundPoints() == null) { return mergeMat; }
        for(ColorPoint point : data.GetExtractForegroundPoints())
        {
            // Check if the point is active and we need to draw it or not
            if(point.mStatus == PointStatus.ACTIVE) {

                // Check if the point is in the screen area
                if((int) (point.y + centerOfGravity.y) > ImageMergeMainActivity.GetScreenHeight() || (int) (point.y + centerOfGravity.y) < 0) { continue; }
                if((int) (point.x + centerOfGravity.x) > ImageMergeMainActivity.GetScreenWidth() || (int) (point.x + centerOfGravity.x) < 0) { continue; }

                // Draw the points including movement about the center of gravity
                mergeMat.put((int) (point.y + centerOfGravity.y), (int) (point.x + centerOfGravity.x), point.mColor);
            }
        }

        return mergeMat;
    }

    /**
     * RunSegmentationAlgorithm:
     *
     * Run a segmentation algorithm in order to differ between the background and foreground of the entire image.
     * Return a new image that has only the foreground so we can merge the foreground to the first image.
     * This method runs in stages for each stage it returns a status int a number between 0 - 100 dependns on the
     * progress stage
     *
     * @param data
     * @param tracks
     * @return
     */
    public static final int RunSegmentationAlgorithm(Data data,ArrayList<MovementTracker> tracks,int currentProgress) {
        if(currentProgress == 0) {

            // Initialize the return image
            //foregroundImage = new Mat(background.rows(), background.cols(), background.type(), MarkValues.NO_MARK_VALUE);

            //TODO: optimize this using line iterator and line from opencv. needs to implemnent those classes in java so needs to be copy from opencv c++ source code and translate to java
            // Extract all the pixels the user mark to foreground pixels and background pixels

            // First merge all the tracks into the marked image
            data.SetMarkedImageMask();
            Mat tempMask = DrawTracks(data.GetMarkedImageMask(),tracks);
            data.SetMarkedImageMask(tempMask);

            // Second go over the marked mask and extract the background and foreground values
            Log.d(GeneralInfo.DEBUG_TAG, "Start ExtractMaskPixels");
            ExtractMaskPixels(data);
            Log.d(GeneralInfo.DEBUG_TAG, "End ExtractMaskPixels");

            // Release mask
            if(mAlgorithm.equals(AlgorithmName.KMEANS)) {
                data.ReleaseImageMask();
            }

            // return current progress
            currentProgress = 25;

        } else if(currentProgress == 25) {

            //TODO: change kMeans algorithm to a better segmentation algo

            switch(mAlgorithm) {
                case MY_ALGORITHM:

                    // Calculate color statistic of points
                    Log.d(GeneralInfo.DEBUG_TAG, "Start CalculateForegroundBackgroundStatistic");
                    CalculateForegroundBackgroundStatistic(data);
                    Log.d(GeneralInfo.DEBUG_TAG, "End CalculateForegroundBackgroundStatistic");
                    break;

                case KMEANS:

                    // Create Kmeans matrix
                    Log.d(GeneralInfo.DEBUG_TAG, "Start SetKmeansMatrix");
                    data.SetKmeansMatrix(CreateKMeansMatrix(data));
                    Log.d(GeneralInfo.DEBUG_TAG, "End SetKmeansMatrix");
                    break;
            }

            currentProgress = 50;

        } else if(currentProgress == 50) {

            //TODO: change kMeans algorithm to a better segmentation algo

            switch(mAlgorithm) {
                case MY_ALGORITHM:

                    // Calculate start label for each point in the image according to the statistic
                    Log.d(GeneralInfo.DEBUG_TAG, "Start CalculateStartLabelAccordingToStatistic");
                    CalculateStartLabelAccordingToStatistic(data);
                    Log.d(GeneralInfo.DEBUG_TAG, "End CalculateStartLabelAccordingToStatistic");

                    // Extracted connected component and fill holes
                    Log.d(GeneralInfo.DEBUG_TAG, "Start ExtractConnectedComponent");
                    //ExtractConnectedComponent(data);
                    //ExtractConnectedComponent(data);
                    FillAreasUsingOpening(data);
                    Log.d(GeneralInfo.DEBUG_TAG, "End ExtractConnectedComponent");

                    break;

                case KMEANS:

                    // Run kmeans
                    Log.d(GeneralInfo.DEBUG_TAG, "Start CreateBestLabelsFromUserMarks");
                    CreateBestLabelsFromUserMarks(data);
                    Log.d(GeneralInfo.DEBUG_TAG, "End CreateBestLabelsFromUserMarks");
                    Log.d(GeneralInfo.DEBUG_TAG, "Start kmeans");
                    Core.kmeans(data.GetmKmeansMatrix(), 2, data.GetBestLabels(), new TermCriteria(TermCriteria.MAX_ITER | TermCriteria.EPS, 10, 0.0001), 1, Core.KMEANS_USE_INITIAL_LABELS);
                    Log.d(GeneralInfo.DEBUG_TAG, "End kmeans");

                    // Release unused matrix
                    data.ReleaseKmeansMatrix();

                    break;
            }

            currentProgress = 75;

        } else if(currentProgress == 75) {

            // Add original mark labels to final results
            //AddMarksToFinalLabels(data);

            // Convert best labels to foreground image
            Log.d(GeneralInfo.DEBUG_TAG, "Start ConvertBestLabelsToForegroundImage");
            ConvertBestLabelsToForegroundImage(data);
            Log.d(GeneralInfo.DEBUG_TAG, "End ConvertBestLabelsToForegroundImage");

            switch(mAlgorithm) {
                case MY_ALGORITHM:
                    break;

                case KMEANS:

                    // Release unused matrix
                    data.ReleaseKmeansBestLabelsMatrix();
                    break;
            }
            currentProgress = 100;
        }

        return currentProgress;
    }

    /**
     * ExtractMaskPixels:
     *
     * Goes over the mask in data and for each point in the mask decide if to added it to foreground background or do nothing
     * @param data
     */
    private static void ExtractMaskPixels(Data data)
    {
        Mat mask = data.GetMarkedImageMask();
        int size = (int) mask.total() * mask.channels();
        byte[] maskData = ConvertMatToPrimitive(mask);

        Mat secondImage = data.GetSecondImage();

        // Go over all the pixels in the mask if we found a pixel with value of foreground or background add it.
        data.InitializeForegroundBackgroundPixels();
        for(int i = 0; i < size; i = i + mask.channels())
        {
            // Convert index to x y coordinate
            int normalizeI = i/mask.channels();
            int x = normalizeI%mask.cols();
            int y = (int)Math.floor(normalizeI/mask.cols());

            // Get color of the pixel from the second image
            byte[] color = new byte[4];
            secondImage.get(y, x, color);

            // Check if the color is a background or foreground color
            if(maskData[i] == MarkValues.FOREGROUND_VALUE_BYTE) {
                data.AddForegroundPixel(new ColorPoint(x,y,color));
            } else if(maskData[i] == MarkValues.BACKGROUND_VALUE_BYTE) {
                data.AddBackgroundPixel(new ColorPoint(x,y,color));
            }
        }

        // Get the bounding box for the foreground pixels
        data.ExtractMinMaxForegroundPoint();
    }


    /**************************************************** KMeans **************************************************************/
    //TODO: finish this explanation
    /**
     * CreateKMeansMatrix:
     *
     * creats the opencv kmeans matrix
     *
     *
     * @param data
     * @return
     */
    private static Mat CreateKMeansMatrix(Data data)
    {
        Mat secondImage = data.GetSecondImage();
        Mat kMeansMatrix = new Mat((int)secondImage.total(),mNumberOfAttributesForKmeans, CvType.CV_32F);

        // Go over all the pixel in the image
        int size = (int) secondImage.total() * secondImage.channels();
        byte[] secondImageData = ConvertMatToPrimitive(secondImage);

        int j = 0;
        for(int i = 0; i < size; i = i + secondImage.channels())
        {
            // Convert index to x y coordinate
            int normalizeI = i/secondImage.channels();
            int x = normalizeI%secondImage.cols();
            int y = (int)Math.floor(normalizeI/secondImage.cols());

            //float[] values = {secondImageData[i]/255,secondImageData[i+1]/255,secondImageData[i+2]/255,x/secondImage.cols(), y/secondImage.rows()};
            //kMeansMatrix.put(j,0,values);
            kMeansMatrix.put(j,0,(float) secondImageData[i]/255);
            kMeansMatrix.put(j,1,(float) secondImageData[i+1]/255);
            kMeansMatrix.put(j,2,(float) secondImageData[i+2]/255);
            kMeansMatrix.put(j,3,(float) x/secondImage.cols());
            kMeansMatrix.put(j,4,(float) y/secondImage.rows());
            j  = j + 1;
        }

        return kMeansMatrix;
    }

    //TODO: change this to consider the color of each starting pixel.
    /**
     * CreateBestLabelsFromUserMarks:
     *
     *
     * @param data
     */
    private static void CreateBestLabelsFromUserMarks(Data data)
    {

        // Initialize
        data.SetBestStartingLabels(AlgorithmName.KMEANS);
        Mat labels = data.GetBestLabels();

        // Go over all the pixels that are foreground and change their label
        for(Point point : data.GetForeroundPoints())
        {
            labels.put((int)(point.y*data.GetSecondImage().cols() + point.x),0,1f);
        }

        // Update best labels
        data.SetBestStartingLabels(labels);

    }

    /**************************************************** KMeans **************************************************************/

    /**************************************************** My algorithm **************************************************************/

    /**
     * CalculateForegroundBackgroundStatistic:
     *
     * Go over all the pixels in the background and foreground and calculate statistic what is the chance of
     * a given color to be part of the foreground and background
     *
     *
     */
    private static void CalculateForegroundBackgroundStatistic(Data data)
    {
        int numberOfBins = 16; // a power of 2

        data.SetStatisticalNumberOfBins(numberOfBins);
        data.SetForegroundStatistic(CalculateStatisticOfColorPoints(data.GetForeroundPoints(),numberOfBins));
        data.SetBackgroundStatistic(CalculateStatisticOfColorPoints(data.GetBackgroundPoints(),numberOfBins));
    }

    /**
     * CalculateStatisticOfColorPoints:
     *
     * Goes over all the point in points and calculate the chance of each color to be in this group of points
     *
     * @param points
     * @param numberOfBins
     * @return
     */
    private static double[] CalculateStatisticOfColorPoints(ArrayList<ColorPoint> points,int numberOfBins)
    {
        // Initialize 3-dimensional statistical bins
        double[] statisticalBins = new double[numberOfBins*numberOfBins*numberOfBins];

        // Go over foreground points
        for(ColorPoint point : points)
        {
            // Find the bin the color of the point will be
            int binIndex = 1;
            for(int i = 0 ; i < 3 ; i++)
            {
                // convert the byte value to int
                int channelColor = Math.abs(point.mColor[i]);

                // binIndex of colors
                binIndex *= channelColor/(256/numberOfBins);
            }

            // Count +1/numberOfPoints in the appropriate bin
            statisticalBins[binIndex] = statisticalBins[binIndex] + 1d/points.size();
        }

        return statisticalBins;
    }

    /**
     * CalculateStartLabelAccordingToStatistic:
     *
     *  Goes over all the pixels in the image and according to the calculated statistic decide if the point is part of
     *  the foreground or the background.
     * @param data
     */
    private static void CalculateStartLabelAccordingToStatistic(Data data)
    {
        Mat secondImage = data.GetSecondImage();

        // Initialize
        data.SetBestStartingLabels(AlgorithmName.MY_ALGORITHM);
        Mat labels = data.GetBestLabels();

        // Get the number of statistical buns in order to calculate in which bin the color is in and statistical information
        int numberOfBins = data.GetNumberOfStatisticalBin();
        double[] backgroundStat = data.GetBackgroundStatistic();
        double[] foregroundStat = data.GetForegroundStatistic();

        /*
        for(int i = 0 ; i < foregroudStat.length ; i++)
        {
            Log.d(GeneralInfo.DEBUG_TAG,Double.toString(foregroudStat[i]));
        }*/
        // Go over all the pixel in the image
        int size = (int) secondImage.total() * secondImage.channels();
        byte[] secondImageData = ConvertMatToPrimitive(secondImage);

        for(int i = 0; i < size; i = i + secondImage.channels())
        {
            // Convert index to x y coordinate
            int normalizeI = i/secondImage.channels();
            int x = normalizeI%secondImage.cols();
            int y = (int)Math.floor(normalizeI/secondImage.cols());

            // Find the bin the color of the point will be
            int binIndex = 1;
            for(int j = 0 ; j < 3 ; j++)
            {
                // convert the byte value to int
                int channelColor = Math.abs(secondImageData[i+j]);

                // binIndex of colors
                binIndex *= channelColor/(256/numberOfBins);
            }

            // Check what is the probability to be in the foreground and the background
            if(foregroundStat[binIndex] > backgroundStat[binIndex])
            {
                labels.put(y, x, 1);
            } else if (foregroundStat[binIndex] < backgroundStat[binIndex]) {
                labels.put(y, x, 0);
            } else {

                // TODO: fix consider the distance form each probability
                labels.put(y, x, 0);
            }

        }
    }

    /**
     *
     * @param data
     */
    private static void ExtractConnectedComponent(Data data)
    {
        Mat labels = data.GetBestLabels();
        Mat mask = new Mat(labels.rows()+2,labels.cols()+2,CvType.CV_8SC1);
        byte[] value = new byte[1];

        for(int i = 0 ; i < labels.rows() ; i++)
        {
            for(int j = 0 ; j < labels.cols() ; j++)
            {
                labels.get(i,j,value);
                if(value[0] == 1)
                {
                    mask.put(i + 1, j + 1, 1);
                }
            }
        }

        // Find connected components in the labels
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        //Mat hierarchy = new Mat();
        Imgproc.findContours(labels, contours, new Mat() , Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);

        // Go over the contours and for each contour if we found an original mark that add this mark keep the contour
        // else discard it
        Mat markMask = data.GetMarkedImageMask();
        List<MatOfPoint> keepContours = new ArrayList<MatOfPoint>();
        byte[] maskValue = new byte[4];
        for(MatOfPoint contour : contours) {

            boolean keepContour = false;

            // Go over all the points in the contour
            for (Point point : contour.toList()) {

                // Check if the point of the contour is a part of the original user marks
                markMask.get((int) point.y, (int) point.x, maskValue);

                if(maskValue[0] == MarkValues.FOREGROUND_VALUE_BYTE)
                {
                    // Keep the current contour
                    keepContour = true;
                    break;
                }
            }

            if(keepContour) {

                // Add contour
                keepContours.add(contour);
            }
        }

        // Fill in the holes of each contour
        int labelCount = 1;
        for(MatOfPoint contour : keepContours) {
            Point midPoint = CalculateContourCenter(contour.toList());
            Imgproc.floodFill(labels, mask, new Point((int) midPoint.x, (int) midPoint.y), new Scalar(labelCount),new Rect(),new Scalar(0) ,new Scalar(0) , 8 | (1 << 8) );
        }
        data.SetBestStartingLabels(AlgorithmName.MY_ALGORITHM);
        labels = data.GetBestLabels();
        //Imgproc.drawContours(data.GetBestLabels(),keepContours,-1,new Scalar(1),-1);
        //Imgproc.drawContours(data.GetBestLabels(),contours,-1,new Scalar(1),-1);

        // Copy back from labels to fillmask
        byte[] v = {1};
        byte[] newMaskVal = new byte[1];
        for(int i = 1 ; i < mask.rows()-1 ; i++)
        {
            for (int j = 1; j < mask.cols()-1; j++)
            {
                mask.get(i, j, newMaskVal);

                if(newMaskVal[0] == 1)
                {
                    labels.put(i - 1, j - 1,1);
                }
            }
        }

        // Add the borders themself
        for(MatOfPoint contour : contours) {

            // Go over all the points in the contour
            for (Point point : contour.toList()) {
                labels.put((int) point.y, (int) point.x, 1);
            }
        }
    }

    /**
     *
     * @param contour
     * @retrun
     */
    private static Point CalculateContourCenter(List<Point> contour)
    {
        Point middleCoord = new Point();
        for(Point point : contour)
        {
            middleCoord.x = middleCoord.x + point.x;
            middleCoord.y = middleCoord.y + point.y;
        }

        middleCoord.x = middleCoord.x/contour.size();
        middleCoord.y = middleCoord.y/contour.size();

        return middleCoord;
    }

    private static void FillAreasUsingOpening(Data data)
    {
        Mat element = Imgproc.getStructuringElement(Imgproc.CV_SHAPE_ELLIPSE,new Size(15,15));
        Imgproc.morphologyEx(data.GetBestLabels(),data.GetBestLabels(), Imgproc.MORPH_DILATE,element);
    }

    /**
     *
     * @param data
     */
    private static void AddMarksToFinalLabels(Data data)
    {
        Mat labels = data.GetBestLabels();

        int[] value = {1};
        // Go over all the original mark pixels and add them to the final image
        for(ColorPoint point : data.GetForeroundPoints())
        {
            labels.put((int)point.y,(int)point.x,value);
        }

    }
    /**************************************************** My algorithm **************************************************************/

    /**`
     * ConvertBestLabelsToForegroundImage:
     *
     * creates the foreground image based on the labels of the segmentation
     *
     * @param data
     */
    private static void ConvertBestLabelsToForegroundImage(Data data)
    {
        // Initialize the foreground image
        //data.SetForegroundImage();
        Mat secondImage = data.GetSecondImage();
        //Mat foreground = data.GetForegroundImage();
        Mat labels = data.GetBestLabels();

        //Core.MinMaxLocResult a = Core.minMaxLoc(labels);
        //int[] value = new int[1];
        byte[] value = new byte[1];
        //labels.get((int)a.maxLoc.y,(int)a.maxLoc.x,value);

        // Get the max and min point of the foreground bounding point
        Point maxForegroundPoint = data.GetForegroundMaxPoint();
        Point minForegroundPoint = data.GetForegroundMinPoint();

        data.SetExtractForeground();

        if(mAlgorithm.equals(AlgorithmName.KMEANS)) {
            // Go over labels and create new foreground image
            for (int i = 0; i < labels.rows(); i++) {
                labels.get(i, 0, value);
                if (value[0] != 0) {
                    //Log.d(GeneralInfo.DEBUG_TAG, "foreground pixel");

                    // a foreground pixel
                    int x = i % secondImage.cols();
                    int y = (int) Math.floor(i / secondImage.cols());

                    // if the the pixel is out of the foreground bounding box continue
                    if (x > maxForegroundPoint.x || x < minForegroundPoint.x || y > maxForegroundPoint.y || y < minForegroundPoint.y) {
                        continue;
                    } else {

                        // The point is in the bounding box and a foreground pixel
                        byte[] color = new byte[4];
                        secondImage.get(y, x, color);
                        //foreground.put(y,x,color);

                        // Add the color data point
                        data.AddExtractForegroundPoint(new ColorPoint(x, y, color));
                    }

                }
            }
        } else if(mAlgorithm.equals(AlgorithmName.MY_ALGORITHM)) {
            // Go over labels and create new foreground image
            for (int i = 0; i < labels.rows(); i++) {
                for(int j = 0 ; j < labels.cols(); j++) {

                    labels.get(i, j, value);
                    if (value[0] != 0) {
                        //Log.d(GeneralInfo.DEBUG_TAG, "foreground pixel");

                        // a foreground pixel
                        // if the the pixel is out of the foreground bounding box continue
                        if (j > maxForegroundPoint.x || j < minForegroundPoint.x || i > maxForegroundPoint.y || i < minForegroundPoint.y) {
                            continue;
                        } else {

                            // The point is in the bounding box and a foreground pixel
                            byte[] color = new byte[4];
                            secondImage.get(i, j, color);
                            //foreground.put(y,x,color);

                            // Add the color data point
                            data.AddExtractForegroundPoint(new ColorPoint(j, i, color));
                        }
                    }
                }
            }
        }

        // Build the extracted foreground matrix
        data.BuildExtractedForegroundMatrix();
    }

    /**
     * CombineMat:
     *
     * Takes two images ,foreground and background, and combines them in the following way
     * if foreground(i,j) != MarkValues.NO_MARK_VALUE
     *      NewImage(i,j) = foreground(i,j)
     * else
     *      NewImage(i,j) = background(i,j)
     *
     *
     * @param foreground
     * @param background
     * @return
     */
    public static Mat CombineMat(Mat foreground,Mat background)
    {
        // Initialize the image to be the background.
        Mat newImage = background.clone();

        // Go over all the pixels and if the foreground value != MarkValues.NO_MARK_VALUE then replace the current pixel
        // value with the foreground value


        int numberOfBytesForPixel = foreground.channels();
        for(int i = 0 ; i < foreground.rows() ; i++)
        {
            for(int j = 0 ; j < foreground.cols() ; j++)
            {
                byte buff[] = new byte[numberOfBytesForPixel];

                // Get the pixel value
                foreground.get(i,j,buff);

                // Check the buff value
                if(buff[1] == GeneralInfo.BYTE_ZERO && buff[2] == GeneralInfo.BYTE_ZERO && buff[3] == GeneralInfo.BYTE_ZERO)
                {
                    newImage.put(i,j,buff);
                }
            }
        }
        // Convert mat values to primitive object for efficiency
        /*
        int size = (int) foreground.total() * foreground.channels();
        byte[] foregroundData = ConvertMatToPrimitive(foreground);
        byte[] newImageData = ConvertMatToPrimitive(newImage);
        for(int i = 0; i < size; i = i + foreground.channels())
        {
            for(int j = i ; j < i + foreground.channels() ; j++)
            {
                // Check if the foreground value is not MarkValues.NO_MARK_VALUE meaning ~= (0,0,0)

                // If this is not the final channel value keep checking
                if(j < foreground.channels() - 1) {
                    // If one of the channels is not equal to 0 go check the next pixel
                    if (foregroundData[j] == GeneralInfo.BYTE_ZERO)
                    {
                        break;
                    }
                } else {
                    if (foregroundData[j] != GeneralInfo.BYTE_ZERO)
                    {
                        // If all the pixels are different then zeros than value of the new image would be the foreground value
                        for(int n = j ; n > j - foreground.channels() ; n--)
                        {
                            newImageData[n] = foregroundData[j];
                        }
                    }
                }
            }
        }

        // Update the new image matrix to the new values
        newImage.put(0,0,newImageData);
        */
        return newImage;
    }

    /**
     * ConvertMatToPrimitive:
     *
     * Converts the Mat data to primitive array. This is done for efficiency reasons.
     *
     * Note:
     * Assumes the mat is of type UC (unsigned char) and not something else
     * @param src
     * @return
     */
    public static byte[] ConvertMatToPrimitive(Mat src)
    {
        // Calculate the number of total values in the mat
        int size = (int) src.total() * src.channels();
        byte[] buff = new byte[size];

        // Get the data values and return them as byte array
        src.get(0, 0, buff);

        return buff;
    }

    /**
     * decodeYUV:
     *
     * Decode Y, U, and V values on the YUV 420 buffer described as YCbCr_422_SP by Android
     * David Manpearl 081201
     *
     * @param out
     * @param fg
     * @param width
     * @param height
     * @throws NullPointerException
     * @throws IllegalArgumentException
     */
    public static void decodeYUV(int[] out, byte[] fg, int width, int height)
            throws NullPointerException, IllegalArgumentException {
        int sz = width * height;
        if (out == null)
            throw new NullPointerException("buffer out is null");
        if (out.length < sz)
            throw new IllegalArgumentException("buffer out size " + out.length
                    + " < minimum " + sz);
        if (fg == null)
            throw new NullPointerException("buffer 'fg' is null");
        if (fg.length < sz)
            throw new IllegalArgumentException("buffer fg size " + fg.length
                    + " < minimum " + sz * 3 / 2);
        int i, j;
        int Y, Cr = 0, Cb = 0;
        for (j = 0; j < height; j++) {
            int pixPtr = j * width;
            final int jDiv2 = j >> 1;
            for (i = 0; i < width; i++) {
                Y = fg[pixPtr];
                if (Y < 0)
                    Y += 255;
                if ((i & 0x1) != 1) {
                    final int cOff = sz + jDiv2 * width + (i >> 1) * 2;
                    Cb = fg[cOff];
                    if (Cb < 0)
                        Cb += 127;
                    else
                        Cb -= 128;
                    Cr = fg[cOff + 1];
                    if (Cr < 0)
                        Cr += 127;
                    else
                        Cr -= 128;
                }
                int R = Y + Cr + (Cr >> 2) + (Cr >> 3) + (Cr >> 5);
                if (R < 0)
                    R = 0;
                else if (R > 255)
                    R = 255;
                int G = Y - (Cb >> 2) + (Cb >> 4) + (Cb >> 5) - (Cr >> 1)
                        + (Cr >> 3) + (Cr >> 4) + (Cr >> 5);
                if (G < 0)
                    G = 0;
                else if (G > 255)
                    G = 255;
                int B = Y + Cb + (Cb >> 1) + (Cb >> 2) + (Cb >> 6);
                if (B < 0)
                    B = 0;
                else if (B > 255)
                    B = 255;
                out[pixPtr++] = 0xff000000 + (B << 16) + (G << 8) + R;
            }
        }
    }

    /**
     * ByteArrayToBitmap:
     *
     * Convert a byte array of RGBA color space to a bitmap of YUV color space
     *
     * @param data
     * @return
     */
    public static Bitmap ByteArrayToBitmap(byte[] data,int width,int height)
    {
        // We need to convert the data from byte array to YUV color space and only than we can display the image
        int[] convertedYUV = new int[width * height];
        decodeYUV(convertedYUV, data, width, height);
        //decodeYUVToARGB(convertedYUV, data, getFrameWidth(), getFrameHeight());

        // Reverse the order inorder to align the image to the view
        reverseIntArray(convertedYUV);

        // Convert to bitmap in order to display it on the phone
        //Matrix matrix = new Matrix();
        //matrix.postScale(mDisplayData.GetDisplayMetrix().scaledDensity,mDisplayData.GetDisplayMetrix().scaledDensity);

        Bitmap bitmap = Bitmap.createBitmap(convertedYUV, width, height, Bitmap.Config.ARGB_8888);
        //Bitmap reScalebitmap = Bitmap.createBitmap(bitmap, 0,0,getFrameWidth(), getFrameHeight(),matrix, true);

        // Stretch the final result to display dimensions.
        /*
        int w = this.getMeasuredWidth();
        int h = this.getMeasuredHeight();
        bitmap = Bitmap.createScaledBitmap(bitmap,h,w,false);
        */
        return bitmap;
    }

    /**
     * reverseIntArray:
     *
     * Reverse the values of the array
     *
     * @param data
     */
    private static void reverseIntArray(int[] data)
    {
        for(int i = 0; i < data.length / 2; i++)
        {
            int temp = data[i];
            data[i] = data[data.length - i - 1];
            data[data.length - i - 1] = temp;
        }
    }

}

/*
/**
     * RunSegmentationAlgorithmDebug:
     *
     * For debuging this on the main process and not on thread.
     * @param data
     * @param tracks
     * @param currentProgress
     */
/*
public static final void RunSegmentationAlgorithmDebug(Data data,ArrayList<MovementTracker> tracks,int currentProgress)
{
    Mat background = data.GetSecondImage();

    // Initialize the return image
    //foregroundImage = new Mat(background.rows(), background.cols(), background.type(), MarkValues.NO_MARK_VALUE);

    //TODO: optimize this using line iterator and line from opencv. needs to implemnent those classes in java so needs to be copy from opencv c++ source code and translate to java
    // Extract all the pixels the user mark to foreground pixels and background pixels

    // First merge all the tracks into the marked image
    data.SetMarkedImageMask();
    Mat tempMask = DrawTracks(data.GetMarkedImageMask(),tracks);
    data.SetMarkedImageMask(tempMask);

    // Second go over the marked mask and extract the background and foreground values
    Log.d(GeneralInfo.DEBUG_TAG, "Start ExtractMaskPixels");
    ExtractMaskPixels(data);
    Log.d(GeneralInfo.DEBUG_TAG, "End ExtractMaskPixels");
    // return current progress
    currentProgress = 10;


    //TODO: change kMeans algorithm to a better segmentation algo

    // Run kmeans
    Log.d(GeneralInfo.DEBUG_TAG, "Start CreateBestLabelsFromUserMarks");
    CreateBestLabelsFromUserMarks(data);
    Log.d(GeneralInfo.DEBUG_TAG, "End CreateBestLabelsFromUserMarks");

    // Create Kmeans matrix
    Log.d(GeneralInfo.DEBUG_TAG, "Start SetKmeansMatrix");
    data.SetKmeansMatrix(CreateKMeansMatrix(data));
    Log.d(GeneralInfo.DEBUG_TAG, "Done SetKmeansMatrix");

    currentProgress = 20;


    //TODO: change kMeans algorithm to a better segmentation algo


    Log.d(GeneralInfo.DEBUG_TAG, "Start kmeans");
    Core.kmeans(data.GetmKmeansMatrix(), 2, data.GetKmeansBestLabels(), new TermCriteria(TermCriteria.MAX_ITER | TermCriteria.EPS, 100, 0.0001), 1, Core.KMEANS_USE_INITIAL_LABELS);
    Log.d(GeneralInfo.DEBUG_TAG, "End kmeans");

    // Release unused matrix
    //data.ReleaseKmeansMatrix();

    currentProgress = 30;



    // Convert best labels to foreground image
    Log.d(GeneralInfo.DEBUG_TAG, "Start ConvertBestLabelsToForegroundImage");
    ConvertBestLabelsToForegroundImage(data);
    Log.d(GeneralInfo.DEBUG_TAG, "End ConvertBestLabelsToForegroundImage");

    currentProgress = 100;

}

 */