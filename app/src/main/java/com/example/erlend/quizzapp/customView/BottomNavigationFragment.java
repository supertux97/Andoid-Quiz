package com.example.erlend.quizzapp.customView;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.BottomNavigationView.OnNavigationItemSelectedListener;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.example.erlend.quizz_fra_dekompilert.R;
import com.example.erlend.quizzapp.activity.QuizzPickerActivity;
import com.example.erlend.quizzapp.activity.StatActivity;
import com.example.erlend.quizzapp.activity.TrophyActivity;

//Brukes til navigering mellom appens faner(kategorier, statistikk og trofeer)
public class BottomNavigationFragment extends Fragment implements OnNavigationItemSelectedListener {
    private BottomNavigationView view;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        this.view = (BottomNavigationView) inflater.inflate(R.layout.bottom_navigation, container, false);
        this.view.setOnNavigationItemSelectedListener(this);
        return this.view;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.navigation_quiz_picker) {
            startActivity(new Intent(getContext(), QuizzPickerActivity.class));
            setCurrNavigationItem(R.id.navigation_quiz_picker);
        }
        else if (item.getItemId() == R.id.navigation_stat) {
            startActivity(new Intent(getContext(), StatActivity.class));
            setCurrNavigationItem(R.id.navigation_stat);
        }
        else if (item.getItemId() == R.id.navigation_trophy) {
            startActivity(new Intent(getContext(), TrophyActivity.class));
            setCurrNavigationItem(R.id.navigation_trophy);
        }

        return true;
    }

    //Brukes av aktiviterne får å velge valgt fane
    public void setSelectedItem(int item){
        view.getMenu().findItem(item).setChecked(true);
    }

    public void setCurrNavigationItem(int resIntMenuItem){
        ((BottomNavigationFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.bottom_navigation_fragment)).setSelectedItem(resIntMenuItem);
    }
}
