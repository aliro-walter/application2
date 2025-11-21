
package com.walter.myinternshiplogbook;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class LogAdapter extends ArrayAdapter<Log> {

    public LogAdapter(Context context, ArrayList<Log> logs) {
        super(context, 0, logs);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Log log = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_log, parent, false);
        }

        TextView logContentTextView = convertView.findViewById(R.id.logContentTextView);
        ImageView evidenceImageView = convertView.findViewById(R.id.evidenceImageView);

        logContentTextView.setText(log.getLogContent());

        byte[] evidenceImage = log.getEvidenceImage();
        if (evidenceImage != null) {
            evidenceImageView.setImageBitmap(BitmapFactory.decodeByteArray(evidenceImage, 0, evidenceImage.length));
            evidenceImageView.setVisibility(View.VISIBLE);
        } else {
            evidenceImageView.setVisibility(View.GONE);
        }

        return convertView;
    }
}
