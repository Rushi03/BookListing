package com.example.android.booklisting;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Rushi Patel on 7/29/2016.
 */
public class ResultsAdapter extends ArrayAdapter<Book> {
    static class ViewHolder {
        TextView resultTitle;
        TextView resultAuthor;
    }

    private ViewHolder holder = new ViewHolder();


    public ResultsAdapter(Activity context, ArrayList<Book> searchBook) {
        super(context, 0, searchBook);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //ViewHolder is more efficient because it spends less time finding views
        if (convertView == null) {

            convertView = LayoutInflater.from(getContext()).inflate
                    (R.layout.list_item, parent, false);
            holder.resultTitle = (TextView) convertView.findViewById(R.id.book_title);

            holder.resultAuthor = (TextView) convertView.findViewById(R.id.author);
        }

        Book currentBook = getItem(position);

        holder.resultTitle.setText(currentBook.getTitle());

        holder.resultAuthor.setText(currentBook.getBookAuthor());

        return convertView;
    }
}
