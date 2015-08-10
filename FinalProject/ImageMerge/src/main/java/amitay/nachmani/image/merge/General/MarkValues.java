package amitay.nachmani.image.merge.General;

import org.opencv.core.Scalar;

/**
 * Created by Amitay on 22-Jul-15.
 */
public class MarkValues {

    public static final Scalar NO_MARK_VALUE = new Scalar(0,0,0,1);
    public static final Scalar FOREGROUND_VALUE = new Scalar(255,255,255,1);
    public static final Scalar BACKGROUND_VALUE = new Scalar(124,124,124,1);

    public static final byte FOREGROUND_VALUE_BYTE = (byte)255;
    public static final byte BACKGROUND_VALUE_BYTE = (byte)124;

    public static final byte[] FOREGROUND_VALUE_BYTE_ARRAY = {(byte)255,(byte)255,(byte)255,1};
    public static final byte[] BACKGROUND_VALUE_BYTE_ARRAY = {(byte)124,(byte)124,(byte)124,1};
    public static final byte[] NO_MARK_VALUE_BYTE_ARRAY = {(byte)0,(byte)0,(byte)0,1};


    /**
     * Marking:
     *
     * The marking options and conversion from marking to mark color
     */
    public enum Marking {
        BACKGROUND,FOREGROUND,NO_MARK;

        public Scalar GetMarkingColor()
        {
            switch (this) {
                case BACKGROUND:
                    return  BACKGROUND_VALUE;

                case FOREGROUND:
                    return  FOREGROUND_VALUE;

                case NO_MARK:
                    return NO_MARK_VALUE;

                default:
                    return  BACKGROUND_VALUE;
            }

        }
    }
}
