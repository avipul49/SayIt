package com.sayit.vipulmittal.sayit;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

public class MainFragment extends Fragment {
    private boolean listening = false;
    private TextView displayedMessage;

    public MainFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        ImageButton btnSpeak = (ImageButton) rootView.findViewById(R.id.btnSpeak);
        displayedMessage = (TextView) rootView.findViewById(R.id.message);
        btnSpeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                promptSpeechInput();
            }
        });
        return rootView;
    }


    private void promptSpeechInput() {
        Intent intent = new Intent();
        String text = getString(R.string.tap_on_mic_start);
        if (listening)
            intent.setAction(MyService.STOP);
        else {
            intent.setAction(MyService.START);
            text = getString(R.string.tap_on_mic_stop);
        }
        listening = !listening;
        displayedMessage.setText(text);
        getActivity().sendBroadcast(intent);
    }
}