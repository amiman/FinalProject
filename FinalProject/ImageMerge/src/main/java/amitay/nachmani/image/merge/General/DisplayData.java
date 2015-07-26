package amitay.nachmani.image.merge.General;

import android.graphics.Point;
import android.util.DisplayMetrics;

/**
 * Created by Amitay on 20-Jul-15.
 *
 * This class holds general data about the display of the platform
 *
 */
public class DisplayData {

    private int mDisplayWidth;
    private int mDisplayHeight;

    private DisplayMetrics mDisplayMatrix;

    public DisplayData(Point size)
    {
        mDisplayWidth = size.x;
        mDisplayHeight = size.y;
    }

    public DisplayData(DisplayMetrics matrix)
    {
        mDisplayMatrix = matrix;
    }

    public DisplayMetrics GetDisplayMetrix()
    {
        return mDisplayMatrix;
    }

    public int GetDisplayWidth()
    {
        return mDisplayWidth;
    }

    public int GetDisplayHeight()
    {
        return mDisplayHeight;
    }
}
