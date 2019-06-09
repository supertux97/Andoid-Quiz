package com.example.erlend.quizzapp.adapter;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.erlend.quizz_fra_dekompilert.R;
import com.example.erlend.quizzapp.model.TrophyManager;

import java.lang.reflect.Field;
import java.util.List;

public class TrophyAdapter extends RecyclerView.Adapter<TrophyAdapter.CustomViewholder>{

    private List<TrophyManager.Trophy> dataset;
    private Context context;


    public TrophyAdapter(List<TrophyManager.Trophy> dataset, Context context){
        this.dataset = dataset;
        this.context = context;
    }


    @Override
    public CustomViewholder onCreateViewHolder(ViewGroup parent, int viewType) {
        View parentCont = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_trophy, parent, false);
        return new CustomViewholder(parentCont);
    }

    @Override
    public void onBindViewHolder(CustomViewholder holder, int position) {
        TrophyManager.Trophy trophyCurrPos = dataset.get(position);
        holder.txtTitle.setText(trophyCurrPos.getTitle());
        holder.txtDescription.setText(trophyCurrPos.getDescription());

        holder.txtTitle.setSelected(true);
        holder.txtTitle.setHorizontallyScrolling(true);


        //==Setter hastigheten for textscrollingen==
        //Kode for dette hentet direkte fra
        //https://stackoverflow.com/questions/8970927/marquee-set-speed
        Field marqueField = null;
        Object marque = null;

        try {
            marqueField = holder.txtTitle.getClass().getDeclaredField("marque");
            marqueField.setAccessible(true);

            marque = marqueField.get(holder.txtTitle);

        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        if(marque != null){
            String scrollSpeedFieldName = "mScrollUnit";
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                scrollSpeedFieldName = "mPixelsPerSecond";

            Field mf = null;
            try {
                mf = marque.getClass().getDeclaredField(scrollSpeedFieldName);
                mf.setAccessible(true);
                mf.setFloat(marque, 1.2f);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

        }


        //https://stackoverflow.com/questions/11036835/how-to-apply-a-color-filter-to-a-view-with-all-children
        if(trophyCurrPos.isUnlocked()){
            holder.rootView.setBackgroundColor(context.getResources().getColor(R.color.colorDarkOrange));
            holder.imgView.clearColorFilter();
            holder.txtTitle.setTextColor(Color.BLACK);
            holder.txtDescription.setTextColor(Color.BLACK);
            holder.txtXp.setVisibility(View.INVISIBLE);
        }
        else{
            holder.rootView.setBackgroundColor(context.getResources().getColor(R.color.lightGray));
            holder.imgView.setColorFilter(Color.argb(150, 200,200,200));
            holder.txtTitle.setTextColor(Color.DKGRAY);
            holder.txtDescription.setTextColor(Color.DKGRAY);
            holder.txtXp.setVisibility(View.VISIBLE);
            holder.txtXp.setText(String.format("(%s XP)", trophyCurrPos.getGainedXP()));

        }
    }

    @Override
    public int getItemCount() {
        return dataset.size();
    }


    static class CustomViewholder extends RecyclerView.ViewHolder{

        private View rootView;
        private TextView txtTitle;
        private TextView txtDescription;
        private TextView txtXp;
        private ImageView imgView;

        public CustomViewholder(View rootView) {
            super(rootView);
            this.rootView = rootView;
            txtTitle = rootView.findViewById(R.id.trophyRowTitle);
            txtDescription = rootView.findViewById(R.id.trophyRowDescription);
            txtXp = rootView.findViewById(R.id.trophyXpForTrophy);
            imgView = rootView.findViewById(R.id.imgTrophyRow);

        }
    }

}
