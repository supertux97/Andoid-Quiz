package com.example.erlend.quizzapp.customView;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.erlend.quizz_fra_dekompilert.R;
import com.example.erlend.quizzapp.activity.QuestionActivity;
import com.example.erlend.quizzapp.util.Callbackmethod;
import com.example.erlend.quizzapp.model.TrueFalseQuestion;
import com.example.erlend.quizzapp.util.InputControl;

public class TrueFalseFragment extends Fragment implements OnClickListener, InputControl {
    private Button btnFalse;
    private Button btnTrue;
    private Callbackmethod callbackAnswerChoosen;
    private TrueFalseQuestion question;
    private Callbackmethod callbackCreatedView;

    private Button selectedButton;
    private boolean answerIsCorrect;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ConstraintLayout root = (ConstraintLayout) inflater.inflate(R.layout.fragment_true_false, container, false);
        this.btnTrue = (Button) root.findViewById(R.id.btnTrue);
        this.btnFalse = (Button) root.findViewById(R.id.btnFalse);
        this.btnTrue.setOnClickListener(this);
        this.btnFalse.setOnClickListener(this);
        return root;
    }

    @Override
    public void onClick(View clickedView) {
        selectedButton = (Button) clickedView;
        boolean answer;
        if (clickedView == btnTrue) {
            btnFalse.clearAnimation();
            answer = true;
        }
        else if (clickedView == btnFalse) {
            btnFalse.clearAnimation();
            answer = false;
        }
        else {
            throw new IllegalStateException("En knapp annen enn sant/usant ble trykket på i FrueFalse fragment");
        }

        boolean correctButtonCliked = question.checkAnswer(answer);

        answerIsCorrect = correctButtonCliked;
        callbackAnswerChoosen.callback(correctButtonCliked);

    }

    @Override
    public void onStart() {
        super.onStart();
    }


    @Override
    public void onResume() {
        super.onResume();
        if(callbackCreatedView != null)
            callbackCreatedView.callback(null);

        if (getArguments() == null) {
            throw new IllegalArgumentException("Recieved bundle was null: TrueFalseFragment");
        }

        this.callbackAnswerChoosen = (Callbackmethod) getArguments().getSerializable(QuestionActivity.KEY_BUNDLE_CALLBACK_ANSWER_CHOOSEN);
        this.question = (TrueFalseQuestion) getArguments().getParcelable(QuestionActivity.KEY_BUNDLE_QUESTION);
    }


    @Override
    public void disableInputs() {
        if(!isAdded()){
            return;
        }

        btnFalse.setClickable(false);
        btnTrue.setClickable(false);
    }

    @Override
    public void enableInputs() {
        if(!isAdded()){
            return;
        }

        btnFalse.setClickable(true);
        btnTrue.setClickable(true);
    }

    @Override
    public void onCreatedView(Callbackmethod callback) {
        this.callbackCreatedView = callback;
    }

    @Override
    public void markQuestionSelection() {
        if(!isAdded()){
            return;
        }

        if(selectedButton != null)
            //https://stackoverflow.com/questions/25694151/change-background-color-of-the-button-without-changing-its-style
            selectedButton.getBackground().setColorFilter(getResources().getColor(R.color.colorAnswerMarked), PorterDuff.Mode.MULTIPLY);
    }

    @Override
    public void markQuestionCorrectness() {
        if(!isAdded()){
            return;
        }

        if(selectedButton == null)
            return;

        if(answerIsCorrect){
            selectedButton.getBackground().setColorFilter(getResources().getColor(R.color.colorAnswerCorrect), PorterDuff.Mode.MULTIPLY);
        }
        else{
            selectedButton.getBackground().setColorFilter(getResources().getColor(R.color.colorAnswerWrong), PorterDuff.Mode.MULTIPLY);

        }
    }

    @Override
    public void markCorrectQuestion() {

    }
}
