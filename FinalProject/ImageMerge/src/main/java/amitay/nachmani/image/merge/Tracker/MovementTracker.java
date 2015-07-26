package amitay.nachmani.image.merge.Tracker;

import org.opencv.core.Point;

import java.util.ArrayList;

import amitay.nachmani.image.merge.General.MarkValues;

/**
 * Created by Amitay on 20-Jul-15.
 */
public class MovementTracker {

    private ArrayList<Point> mTrackingPoints;
    private MarkValues.Marking mMark;

    public MovementTracker()
    {
        mTrackingPoints = new ArrayList<Point>();
        mMark = MarkValues.Marking.BACKGROUND;
    }

    public MovementTracker(MarkValues.Marking mark)
    {
        mTrackingPoints = new ArrayList<Point>();
        mMark = mark;
    }

    public void AddPoint(Point point)
    {
        mTrackingPoints.add(point.clone());
    }

    public Point GetPoint(int index)
    {
        return mTrackingPoints.get(index);
    }

    public MarkValues.Marking GetMarking()
    {
        return mMark;
    }

    public ArrayList<Point> GetMarkedPoints() { return mTrackingPoints; }

    /**
     * CleanMarkedPoints:
     *
     * After we got the marked trail we can discard the points.
     */
    public void CleanMarkedPoints()
    {
        mTrackingPoints.clear();
    }

}
