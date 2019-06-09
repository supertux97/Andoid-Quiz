package com.example.erlend.quizzapp.customView;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.erlend.quizz_fra_dekompilert.R;
import com.example.erlend.quizzapp.model.Xp;

import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

//Blir brukt av FiniishedQuizActivity og StatActivity til å vise brukerens Xp og nivå
public class FragmentXp extends Fragment {

    private RelativeLayout root;
    private TextView txtXp;
    private TextView txtLevel;
    private MaterialProgressBar progressXp;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        root = (RelativeLayout) inflater.inflate(R.layout.fragment_xp, container, false);
        txtXp = root.findViewById(R.id.statXp);
        txtLevel = root.findViewById(R.id.statLevel);
        progressXp = root.findViewById(R.id.progressStatXp);
        setXPInformation();
        setRetainInstance(false);
        return root;
    }

    private void setXPInformation(){
        txtXp.setText(String.format("%s/%s", Xp.getXp(), Xp.getXpPrLevel()));
        txtLevel.setText(String.format("%s: %s", getResources().getString(R.string.level), Xp.getLevel()));

        int progress = (int) (((float) Xp.getXp()) / ((float) Xp.getXpPrLevel()) * 100 );
        progressXp.setProgress(progress);
    }

    //Var planlegt, men fikk ikke tid til implementasjon
    public void setXpIncrement(int increment){
        TextView txtXpAdded = root.findViewById(R.id.txtXpAdded);
        String prefix = increment >= 0 ? "+":"";
        txtXpAdded.setText(String.format("%s%s Xp", prefix, increment));
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(null);
    }


    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(null);
    }
}
