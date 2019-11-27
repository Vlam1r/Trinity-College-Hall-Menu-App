package dev.vlamir.trinitymenu;

import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

public class FoodFragment extends Fragment {

    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;

    public FoodFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_food, container, false);
/*
        recyclerView = v.findViewById(R.id.recycler);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(v.getContext());
        recyclerView.setLayoutManager(layoutManager);

        // specify an adapter (see also next example)
        mAdapter = new RecyclerAdapter(parse());
        recyclerView.setAdapter(mAdapter);*/

        return v;
    }

    private String[] parse() {
        /*String[] in = new String[0];
        if (getArguments() != null && getArguments().getString("food")!=null) {
            in = getArguments().getString("food")
                    .split("\n");
        }
        else return in;

        ArrayList<String> sol = new ArrayList<>();
        for (String s : in) {
            if (s.contains(":") && !s.startsWith("(")) {
                sol.add(s);
            } else {
                sol.set(sol.size() - 1, sol.get(sol.size() - 1).concat("\n").concat(s));
            }
        }
        return sol.toArray(new String[0]);*/
        return null;
    }

    void updateText() {
        /*TODO*/
        ((TextView) getView().findViewById(R.id.food)).setText(
                Html.fromHtml(getArguments().getString("food")
                        .replace("\n", "<br>"), Html.FROM_HTML_MODE_COMPACT));
        /*synchronized (mAdapter) {
            mAdapter.notifyAll();
        }*/
    }
}
