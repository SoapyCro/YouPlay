package hr.leon.croapps.youplay;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class CustomAdapter extends ArrayAdapter<Item>{

    private Bitmap bmp;

    public CustomAdapter(Context context, ArrayList<Item> list) {
        super(context, R.layout.custom_listview, list);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        final View customView = inflater.inflate(R.layout.custom_listview, parent, false);

        Item item = getItem(position);

        TextView videoTitle = (TextView) customView.findViewById(R.id.videoTitle);
        TextView videoDescription = (TextView) customView.findViewById(R.id.videoDescription);

        new AsyncTask<Item, Void, Void>(){
            @Override
            protected Void doInBackground(Item... params) {
                try{
                    String string = params[0].getImageUrl();
                    InputStream in = new URL(string).openStream();
                    bmp = BitmapFactory.decodeStream(in);
                }catch (Exception e){
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                if(bmp != null) {
                    ImageView thumbnail = (ImageView) customView.findViewById(R.id.thumbnail);
                    thumbnail.setImageBitmap(Bitmap.createScaledBitmap(bmp, thumbnail.getWidth(), thumbnail.getHeight(), false));
                }
            }
        }.execute(getItem(position));


        videoTitle.setText(item.getTitle());
        videoDescription.setText(item.getId());
        return customView;
    }
}
