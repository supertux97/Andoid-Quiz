package com.example.erlend.quizzapp.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableContainer;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.erlend.quizz_fra_dekompilert.R;
import com.example.erlend.quizzapp.model.Category;
import com.example.erlend.quizzapp.model.Quiz;
import com.example.erlend.quizzapp.model.QuizStat;
import com.example.erlend.quizzapp.DataManager.QuizKategoryKeyBundle;
import com.example.erlend.quizzapp.DataManager.Type;
import com.example.erlend.quizzapp.util.Callbackmethod;
import com.example.erlend.quizzapp.util.InformationTitle;

import java.util.ArrayList;

//Genererer ruter brukeren kan trykke på for å velge kategorier eller quizer. Opretter i tilegg en egen "speisalkategori" for generelle spørsmål
public class CategoryQuizzAdapter extends Adapter<CategoryQuizzAdapter.CustomViewHolder> implements OnClickListener {
    private Callbackmethod callbackClickTile;
    private Callbackmethod<Object> callbackClickTileGeneral;
    private Context context;
    private ArrayList<? extends InformationTitle> dataset;
    private RecyclerView recyclerView;
    private Type type; //Om kategorier eller quizzer skal vises


    public CategoryQuizzAdapter(
            Context context,
            Type type,
            Callbackmethod callbackOnClickTitle,
            Callbackmethod callbackClickTitleGeneral,
            RecyclerView recyclerView,
            ArrayList<? extends InformationTitle> dataset) {

        this.context = context;
        this.type = type;
        this.callbackClickTile = callbackOnClickTitle;
        this.callbackClickTileGeneral = callbackClickTitleGeneral;
        this.recyclerView = recyclerView;
        this.dataset = dataset;
    }


    //Benytter statiske factory-metoder i stedet for to konstruktører for å gjøre initialiseringen mindre forvirrende
    public static CategoryQuizzAdapter getAdapterForQuizzes(
            Context context,
            Callbackmethod callbackOnClickTitle,
            RecyclerView recyclerView,
            ArrayList<? extends InformationTitle> dataset) {

       return new CategoryQuizzAdapter(context, Type.QUIZZES, callbackOnClickTitle, null, recyclerView, dataset);
    }

    public static CategoryQuizzAdapter getAdapterForCategories(
            Context context,
            Callbackmethod callbackOnClickTitle,
            Callbackmethod callbackClickRandom,
            RecyclerView recyclerView,
            ArrayList<? extends InformationTitle> dataset) {

        return new CategoryQuizzAdapter(context, Type.CATEGORY, callbackOnClickTitle, callbackClickRandom, recyclerView, dataset);
    }


    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View parentCont = LayoutInflater.from(parent.getContext()).inflate(R.layout.gui_sqare, parent, false);
        parentCont.setOnClickListener(this);
        return new CustomViewHolder(parentCont);
    }

    @Override
    public void onBindViewHolder(CustomViewHolder holder, int position) {
        String title;
        Bitmap icon;
        int color;

        //Når kategorien for almennspørsmål skal lages. Det fungerer å lese fra denne posisjonen av lista fordi getItemCount returnerer size +1 for kategorier
        if(type == Type.CATEGORY && position == dataset.size() ){
            title = context.getResources().getString(R.string.category_general);
            icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_general);
            color = context.getResources().getColor(R.color.colorLighterRed);
            holder.setOnClickListner(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    callbackClickTileGeneral.callback(null);
                }
            });
        }
        else {
            title =  dataset.get(position).getTitle();
            icon =  dataset.get(position).getImage();
            color =  dataset.get(position).getColorInt();
            if(type == Type.QUIZZES){
                color = context.getResources().getColor(R.color.colorDarkOrange);
            }
        }

        holder.txtTitle.setText(title);
        holder.imgViewIcon.setImageBitmap(icon);

        //https://stackoverflow.com/questions/19864337/how-can-i-change-colors-in-my-statelistdrawable
        StateListDrawable stateListDrawable = (StateListDrawable) holder.rootView.getBackground();
        DrawableContainer.DrawableContainerState drawableStates = (DrawableContainer.DrawableContainerState) stateListDrawable.getConstantState();
        Drawable[] listStatesDrawable = drawableStates.getChildren();

        GradientDrawable stateNormal = (GradientDrawable) listStatesDrawable[0];
        GradientDrawable statePressed = (GradientDrawable) listStatesDrawable[1];

        stateNormal.setColor(color);
        statePressed.setColor(lighter(color, 0.3f));
    }


    @Override
    public int getItemCount() {
        if (type == Type.CATEGORY){
            return dataset.size() +1; //Skal også opprette en egen kategori for allmennsprsmål
        }
        return  dataset.size();

    }

    @Override
    public void onClick(View view) {
        int position = recyclerView.getChildLayoutPosition(view);
        if (type == Type.CATEGORY) {
            callbackClickTile.callback(((Category) dataset.get(position)).getKey());
        }
        else if (type == Type.QUIZZES) {
            QuizStat.removeSavedQuizz();
            Quiz quiz = (Quiz) dataset.get(position);
            callbackClickTile.callback(new QuizKategoryKeyBundle(quiz.getKey(), quiz.getCategoryKey()));
        }
    }

    //All kode for denne metoden fra: https://stackoverflow.com/a/28058035
    private static int lighter(int color, float factor) {
        int red = (int) ((Color.red(color) * (1 - factor) / 255 + factor) * 255);
        int green = (int) ((Color.green(color) * (1 - factor) / 255 + factor) * 255);
        int blue = (int) ((Color.blue(color) * (1 - factor) / 255 + factor) * 255);
        return Color.argb(Color.alpha(color), red, green, blue);
    }


    static class CustomViewHolder extends ViewHolder {
        private ImageView imgViewIcon;
        private View rootView;
        private TextView txtTitle;

        private CustomViewHolder(View rootView) {
            super(rootView);
            txtTitle =  rootView.findViewById(R.id.txtGuiSquare);
            this.rootView = rootView;
            imgViewIcon =  rootView.findViewById(R.id.imgViewGuiSquare);
        }

        private void setOnClickListner(OnClickListener onClickListner){
            rootView.setOnClickListener(onClickListner);
        }
    }

}
