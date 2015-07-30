package amitay.nachmani.image.merge.ImageProcessing;

import org.opencv.core.Point;

/**
 * Created by Amitay on 28-Jul-15.
 */
public class ColorPoint extends Point {

    public byte[] mColor;
    public PointStatus mStatus;

    public ColorPoint(double x,double y,byte[] color)
    {
        super(x,y);
        mColor = color.clone();
        mStatus = PointStatus.ACTIVE;
    }

    public void ChangePointStatus(PointStatus newStatus)
    {
        mStatus = newStatus;
    }
}
