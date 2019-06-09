package com.example.erlend.quizzapp.customView;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import com.example.erlend.quizz_fra_dekompilert.R;
import com.example.erlend.quizzapp.activity.QuestionActivity;
import com.example.erlend.quizzapp.util.InputControl;
import com.example.erlend.quizzapp.util.Util;
import com.example.erlend.quizzapp.util.Callbackmethod;
import com.example.erlend.quizzapp.model.MultipleChoiceQuestion;

import java.util.ArrayList;
import java.util.Iterator;

public class MultipleChoiceQuestionFragment extends Fragment implements OnClickListener, InputControl {
    private boolean answerGivenIsCorrect;
    private Callbackmethod<Boolean> callbackAnswerChoosen;
    private ArrayList<String> listAlternatives = new ArrayList();
    private MultipleChoiceQuestion question;

    private ArrayList<Button> listBtnAlternatives = new ArrayList<>();
    private Callbackmethod callbackCreatedView;

    private RelativeLayout root;
    private Button btnChoosenAnswer;
    private Button btnCorrectAnswer;

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        callbackAnswerChoosen = (Callbackmethod) getArguments().getSerializable(QuestionActivity.KEY_BUNDLE_CALLBACK_ANSWER_CHOOSEN);
        question = getArguments().getParcelable(QuestionActivity.KEY_BUNDLE_QUESTION);
        listAlternatives = question.getAlternatives();
        if (listAlternatives.size() == 0) {
            throw new IllegalStateException("No alternatives sent to MultipleChoice fragment");
        }
        generateUI();
        super.onViewStateRestored(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.root = (RelativeLayout) inflater.inflate(R.layout.fragment_multiple_choice, container, false);
        return this.root;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(callbackCreatedView != null)
            callbackCreatedView.callback(null);
    }

    public void generateUI() {
        LinearLayout contMultipleCol1 = (LinearLayout) root.findViewById(R.id.contMultipleCol1);
        LinearLayout contMultipleCol2 = (LinearLayout) root.findViewById(R.id.contMultipleCol2);
        int counter = 1;
        if (listAlternatives.size() > 3) {
            contMultipleCol2.setVisibility(View.VISIBLE);
        }
        else {
            //https://stackoverflow.com/questions/3985787/android-relativelayout-programmatically-set-centerinparent
            LayoutParams layoutParams = (LayoutParams) contMultipleCol1.getLayoutParams();
            if (Util.isPortrait(getResources())) {
                layoutParams.addRule(14, -1);
            }
            else {
                layoutParams.addRule(13, -1);
            }
            contMultipleCol1.setLayoutParams(layoutParams);
        }
        Iterator it = listAlternatives.iterator();
        while (it.hasNext()) {
            String alternativeStr = (String) it.next();
            Button button = new Button(getContext());
            button.setText(alternativeStr);
            button.setOnClickListener(this);
            listBtnAlternatives.add(button);
            if(alternativeStr.equals(question.getCorrectChoice())){
                btnCorrectAnswer = button;
            }
            if (counter <= 3) {
                contMultipleCol1.addView(button);
            }
            else {
                contMultipleCol2.addView(button);
            }
            counter++;
        }
    }

    @Override
    public void onClick(View clickedView) {
        if (clickedView instanceof Button) {
            boolean correctAnswerChoosen;
            btnChoosenAnswer = (Button) clickedView;
            if (this.question.checkAnswer(((Button) clickedView).getText().toString())) {
                correctAnswerChoosen = true;
            }
            else {
                correctAnswerChoosen = false;
            }
            this.callbackAnswerChoosen.callback(correctAnswerChoosen);
        }
    }

    @Override
    public void disableInputs() {
        for(Button button: listBtnAlternatives){
            button.setClickable(false);
        }
    }

    @Override
    public void enableInputs() {
        for(Button button: listBtnAlternatives){
            button.setClickable(true);
        }
    }

    @Override
    public void onCreatedView(Callbackmethod callback) {
        callbackCreatedView = callback;
    }

    @Override
    public void markQuestionSelection() {
        if(btnChoosenAnswer != null)
            btnChoosenAnswer.getBackground().setColorFilter(getResources().getColor(R.color.colorAnswerMarked), PorterDuff.Mode.MULTIPLY);
    }

    @Override
    public void markQuestionCorrectness() {
        if(btnChoosenAnswer == null)
            return;

        if(answerGivenIsCorrect)
            btnChoosenAnswer.getBackground().setColorFilter(getResources().getColor(R.color.colorAnswerCorrect), PorterDuff.Mode.MULTIPLY);
        else
            btnChoosenAnswer.getBackground().setColorFilter(getResources().getColor(R.color.colorAnswerWrong), PorterDuff.Mode.MULTIPLY);
    }

    @Override
    public void markCorrectQuestion() {
        btnCorrectAnswer.getBackground().setColorFilter(getResources().getColor(R.color.colorAnswerCorrect), PorterDuff.Mode.MULTIPLY);

    }
}
