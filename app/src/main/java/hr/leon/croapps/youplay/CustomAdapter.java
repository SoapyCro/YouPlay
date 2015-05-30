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

import org.joda.time.Duration;
import org.joda.time.Hours;
import org.joda.time.Minutes;
import org.joda.time.Period;
import org.joda.time.Seconds;
import org.joda.time.format.ISOPeriodFormat;
import org.joda.time.format.PeriodFormatter;
import org.w3c.dom.Text;

import java.io.InputStream;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class CustomAdapter extends ArrayAdapter<Item> {

    static class ViewHolder {
        private TextView videoTitle;
        private TextView viewCount;
        private TextView likeCount;
        private TextView dislikeCount;
        private TextView videoDuration;
        private ImageView thumbnail;
    }

    public CustomAdapter(Context context, ArrayList<Item> list) {
        super(context, R.layout.custom_listview, list);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder = null;
        Item item = getItem(position);
        if (convertView == null) {

            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.custom_listview, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.videoTitle = (TextView) convertView.findViewById(R.id.videoTitle);
            viewHolder.viewCount = (TextView) convertView.findViewById(R.id.viewCount);
            viewHolder.likeCount = (TextView) convertView.findViewById(R.id.likeCount);
            viewHolder.dislikeCount = (TextView) convertView.findViewById(R.id.dislikeCount);
            viewHolder.videoDuration = (TextView) convertView.findViewById(R.id.videoDuration);
            viewHolder.thumbnail = (ImageView) convertView.findViewById(R.id.thumbnail);

            globalSet(item, viewHolder);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            globalSet(item, viewHolder);
        }
        return convertView;
    }

    private void globalSet(Item item, ViewHolder viewHolder) {
        long views = Integer.parseInt(item.getViews());
        DecimalFormat formatter = new DecimalFormat("#,###");
        String formatted = formatter.format(views);

        viewHolder.thumbnail.setImageBitmap(Bitmap.createScaledBitmap(item.getBmp(), 300, 225, false));
        viewHolder.videoTitle.setText(item.getTitle());
        viewHolder.viewCount.setText(formatted);
        viewHolder.likeCount.setText(item.getLikes());
        viewHolder.videoDuration.setText(convertTime(item.getDuration()));
        viewHolder.dislikeCount.setText(item.getDislikes());
    }

    private String convertTime(String duration) {
        PeriodFormatter formatter = ISOPeriodFormat.standard();
        Period p = formatter.parsePeriod(duration);

        Hours h = p.toStandardHours();
        Minutes m = p.toStandardMinutes();
        Seconds s = p.toStandardSeconds();

        int hours, minutes, seconds;
        hours = h.getHours();
        minutes = m.getMinutes() % 60;
        seconds = s.getSeconds() % 60;

        StringBuilder time = new StringBuilder();

        if (hours > 0) {
            time.append(hours);
            time.append(":");
        }
        if (minutes > 0) {
            if (minutes < 10)
                time.append("0");
            time.append(minutes);
            time.append(":");
        }
        if (seconds > 0) {
            if (seconds < 10)
                time.append("0");
            time.append(seconds);
        }
        return time.toString();
    }

}
