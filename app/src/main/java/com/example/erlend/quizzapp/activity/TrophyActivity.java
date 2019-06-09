package com.example.erlend.quizzapp.activity;

import android.content.Intent;
import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import com.annimon.stream.Stream;
import com.example.erlend.quizz_fra_dekompilert.R;
import com.example.erlend.quizzapp.adapter.TrophyAdapter;
import com.example.erlend.quizzapp.model.TrophyManager;
import com.example.erlend.quizzapp.DataManager.Type;
import com.example.erlend.quizzapp.customView.BottomNavigationFragment;
import com.example.erlend.quizzapp.customView.NoDataActivity;

import java.util.List;

public class TrophyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(TrophyManager.getUnlockedTrophies().size() == 0 ){
            Intent intentNoData = new Intent(getApplicationContext(), NoDataActivity.class);
            intentNoData.putExtra(NoDataActivity.KEY_NO_DATA_TYPE, Type.TROPHY);
            startActivity(intentNoData);
            finish();
            return;
        }

        getSupportActionBar().setTitle(getResources().getString(R.string.actionbarheader_trophy));
        setContentView(R.layout.activity_trophy);
        List<TrophyManager.Trophy> listTrophies = TrophyManager.getListTrophies();
        listTrophies = Stream.of(listTrophies).sorted().toList();

        //Velger riktig element i navgasjonen i bånn
        ((BottomNavigationFragment) getSupportFragmentManager().findFragmentById(R.id.bottom_navigation_fragment)).setSelectedItem(R.id.navigation_trophy);

        int numUnlocked = TrophyManager.getUnlockedTrophies().size();
        int total = listTrophies.size();

        TextView txtTrophyInformation = findViewById(R.id.txtTrophyInfo);
        Resources res = getApplicationContext().getResources();
        txtTrophyInformation.setText(String.format(
                "%s %s %s %s %s",
                res.getString(R.string.taken_trophy),
                numUnlocked, res.getString(R.string.of_trophy),
                total,
                res.getString(R.string.trophies)));

        RecyclerView recyclerView = findViewById(R.id.recyclerViewTrophies);
        recyclerView.setAdapter(new TrophyAdapter(listTrophies, getApplicationContext()));
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
    }

}
