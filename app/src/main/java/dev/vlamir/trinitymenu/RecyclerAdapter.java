package dev.vlamir.trinitymenu;

import android.text.Html;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import static android.text.Html.fromHtml;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.MyViewHolder> {
    private final String[] mDataset;

    // Provide a suitable constructor (depends on the kind of dataset)
    RecyclerAdapter(String[] myDataset) {
        mDataset = myDataset;
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public RecyclerAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                           int viewType) {
        // create a new view
        //TextView v = (TextView) LayoutInflater.from(parent.getContext())
        //        .inflate(R.layout.text_view, parent, false);
        //MyViewHolder vh = new MyViewHolder(v);
        // TODO
        return null;//vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.textView.setText(fromHtml(mDataset[position], Html.FROM_HTML_MODE_COMPACT));

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.length;
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    static class MyViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        final TextView textView;

        MyViewHolder(TextView v) {
            super(v);
            textView = v;
        }
    }

}