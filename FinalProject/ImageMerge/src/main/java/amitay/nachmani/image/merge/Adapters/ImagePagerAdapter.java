package amitay.nachmani.image.merge.Adapters;

import android.content.Context;
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
        mFiles = files;
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
        fileName.setText(pathParts[pathParts.length - 1]);

        // Get last modified date
        DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        Date modifedDate = new Date(mFiles.get(position).lastModified());
        String formatedModifedDate = df.format(modifedDate);
        fileDate.setText(formatedModifedDate);

        return rowView;
    }

}
