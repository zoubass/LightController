package cz.zoubelu.lightcontroller.adapter;


import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.List;

import cz.zoubelu.lightcontroller.AllTimeStatsActivity;
import cz.zoubelu.lightcontroller.LastDayStatsActivity;
import cz.zoubelu.lightcontroller.MotionGraphActivity;
import cz.zoubelu.lightcontroller.R;
import cz.zoubelu.lightcontroller.model.Card;

public class CardsAdapter extends RecyclerView.Adapter<CardsAdapter.MyViewHolder> {

    private Context mContext;
    private List<Card> cardList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title, count;
        public ImageView thumbnail, overflow;

        public MyViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.title);
            count = view.findViewById(R.id.count);
            thumbnail = view.findViewById(R.id.thumbnail);
            overflow = view.findViewById(R.id.overflow);
        }
    }


    public CardsAdapter(Context mContext, List<Card> cardList) {
        this.mContext = mContext;
        this.cardList = cardList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_layout, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        Card card = cardList.get(position);
        holder.title.setText(card.getName());

        Glide.with(mContext).load(card.getThumbnail()).into(holder.thumbnail);

        holder.thumbnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showStats(holder.title);
            }
        });
    }

    /**
     * Starting new activity depending on selected card
     */
    private void showStats(TextView title) {
        if (title.getText().equals("All Time stats")) {
            Intent intent = new Intent(mContext, AllTimeStatsActivity.class);
            mContext.startActivity(intent);
        } else if (title.getText().equals("Last Day")) {
            Intent intent = new Intent(mContext, LastDayStatsActivity.class);
            mContext.startActivity(intent);
        } else if (title.getText().equals("Motion")) {
            Intent intent = new Intent(mContext, MotionGraphActivity.class);
            mContext.startActivity(intent);
        }
    }

    @Override
    public int getItemCount() {
        return cardList.size();
    }
}