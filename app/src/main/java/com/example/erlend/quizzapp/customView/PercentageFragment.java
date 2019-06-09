package com.example.erlend.quizzapp.customView;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.example.erlend.quizz_fra_dekompilert.R;
import com.example.erlend.quizzapp.activity.QuestionActivity;
import com.example.erlend.quizzapp.util.InputControl;
import com.example.erlend.quizzapp.util.Util;
import com.example.erlend.quizzapp.util.Callbackmethod;
import com.example.erlend.quizzapp.model.PercentageQuestion;

public class PercentageFragment extends Fragment implements InputControl {
    public static final String KEY_BUNDLE_SELECTED_PERCENTAGE = "KEY_BUNDLE_SELECTED_PERCENTAGE";
    private static final int STEP_PERCENTAGE = 10;

    private Button btnSubmitAnswer;
    private Callbackmethod<Boolean> callbackAnswerChoosen;
    private SeekBar percentageSelector;
    private PercentageQuestion question;
    private TextView txtPercentageSelected;

    private Callbackmethod<Object> callbackCreatedView;

    private boolean answerIsCorrect;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LinearLayout root = (LinearLayout) inflater.inflate(R.layout.fragment_percentage_question, container, false);
        this.percentageSelector = (SeekBar) root.findViewById(R.id.seekbarPercentageAnswer);
        this.txtPercentageSelected = (TextView) root.findViewById(R.id.txtPercentageSelected);
        this.btnSubmitAnswer = (Button) root.findViewById(R.id.btnSubmitPercentageAnswer);
        this.percentageSelector.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateTxtViewPercantageSelected(getProgress());
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        this.btnSubmitAnswer.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (question.checkAnswer(getProgress())) {
                    callbackAnswerChoosen.callback(true);
                    answerIsCorrect = true;
                } else {
                    callbackAnswerChoosen.callback(false);
                    answerIsCorrect = false;
                }
            }
        });

        this.callbackAnswerChoosen = (Callbackmethod) getArguments().getSerializable(QuestionActivity.KEY_BUNDLE_CALLBACK_ANSWER_CHOOSEN);
        this.question = (PercentageQuestion) getArguments().getParcelable(QuestionActivity.KEY_BUNDLE_QUESTION);

        int restoredPercentage = getArguments().getInt(KEY_BUNDLE_SELECTED_PERCENTAGE);
        restoredPercentage = restoredPercentage == -1 ?0:restoredPercentage;
        this.percentageSelector.setProgress(restoredPercentage);
        updateTxtViewPercantageSelected(restoredPercentage);

        return root;
    }

    //Prosentviseren viser bare prosenten i steg på 10. Så hvis 12% er valgt, vil 10% vises.
    public int getProgress(){
        return Util.roundToNearest(percentageSelector.getProgress(), STEP_PERCENTAGE);
    }


    @Override
    public void onResume() {
        super.onResume();
        if(callbackCreatedView != null)
            callbackCreatedView.callback(null);
    }

    private void updateTxtViewPercantageSelected(int percentage) {
        this.txtPercentageSelected.setText(String.format("%s%%", new Object[]{String.valueOf(percentage)}));
    }

    @Override
    public void disableInputs() {
        btnSubmitAnswer.setClickable(false);
        percentageSelector.setEnabled(false);
    }

    @Override
    public void enableInputs() {
        btnSubmitAnswer.setClickable(true);
        percentageSelector.setEnabled(true);
    }


    @Override
    public void onCreatedView(Callbackmethod callback) {
        this.callbackCreatedView = callback;
    }

    @Override
    public void markQuestionSelection() {
        txtPercentageSelected.setBackgroundColor(getResources().getColor(R.color.colorAnswerMarked));
    }

    @Override
    public void markQuestionCorrectness() {
        if(answerIsCorrect){
            txtPercentageSelected.setBackgroundColor(getResources().getColor(R.color.colorAnswerCorrect));
        }
        else{
            txtPercentageSelected.setBackgroundColor(getResources().getColor(R.color.colorAnswerWrong));
            txtPercentageSelected.setText(
                    String.format("%s %s %s: ",
                    getProgress(),
                    getResources().getString(R.string.wrong),
                    ((PercentageQuestion) question).getCorrectAnswer()));
        }
    }

    @Override
    public void markCorrectQuestion() {

    }

}
