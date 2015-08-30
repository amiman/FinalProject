package amitay.nachmani.image.merge.Adapters;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import amitay.nachmani.image.merge.R;

/**
 * Created by Amitay on 04-Aug-15.
 */
public class ImagePagerAdapter extends ArrayAdapter<File> {

    private Context mContext;
    private ArrayList<File> mFiles;
    private ImageView mImageView;

    public ImagePagerAdapter(Context context, int resource, ArrayList<File> files) {
        super(context, resource, files);

        mContext = context;

        mFiles = SortImagesByDate(files);
        //mFiles = files;
    }

    /**
     * SortImagesByDate:
     *
     * Sorts the images according to date
     *
     * @param files
     * @return
     */
    private ArrayList<File> SortImagesByDate(ArrayList<File> files) {

        Collections.sort(files, new Comparator<File>() {
            public int compare(File f1, File f2) {
                DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
                Date modifedDateF1 = new Date(f1.lastModified());
                Date modifedDateF2 = new Date(f2.lastModified());
                return modifedDateF1.compareTo(modifedDateF2);
            }
        });

        return files;
    }

    public void UpdateFilesArray(ArrayList<File> newFile)
    {
        mFiles = newFile;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.load_images_list_row, parent, false);

        //TODO: Load the bitmap for now just display the name
        // Load bitmap and display
        //ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);

        TextView fileName = (TextView) rowView.findViewById(R.id.list_row_file_name);
        TextView fileDate = (TextView) rowView.findViewById(R.id.list_row_file_date);


        // Create name and creation date

        // Show only the name of the image and not the full path
        String[] pathParts = mFiles.get(position).toString().split("/");
        fileName.setGravity(Gravity.CENTER);
        fileName.setText(pathParts[pathParts.length - 1]);

        // Get last modified date
        DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
        Date modifedDate = new Date(mFiles.get(position).lastModified());
        String formatedModifedDate = df.format(modifedDate);
        fileDate.setGravity(Gravity.CENTER);
        fileDate.setText(formatedModifedDate);

        return rowView;
    }

}
