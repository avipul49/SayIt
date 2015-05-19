package com.sayit.vipulmittal.sayit.core;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.sayit.vipulmittal.sayit.R;
import com.sayit.vipulmittal.sayit.listenerModule.ListenerService;


public class MainFragment extends Fragment {
    private boolean listening = false;
    private TextView displayedMessage;
    private TextView captionText;

    enum Status {
        RUNNING {
            @Override
            public String getStatusCaption(Context context) {
                return context.getString(R.string.running);
            }
        }, LOADING {
            @Override
            public String getStatusCaption(Context context) {
                return context.getString(R.string.loading);
            }
        }, LOADED {
            @Override
            public String getStatusCaption(Context context) {
                return context.getString(R.string.loaded);
            }
        };

        public String getStatusCaption(Context context) {
            return null;
        }
    }

    public MainFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        ImageButton btnSpeak = (ImageButton) rootView.findViewById(R.id.btnSpeak);
        displayedMessage = (TextView) rootView.findViewById(R.id.message);
        captionText = (TextView) rootView.findViewById(R.id.status);
        btnSpeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startStopButtonClicked();
            }
        });
        return rootView;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        String text = getString(R.string.tap_on_mic_start);

        listening = false;
        if (ListenerService.running) {
            text = getString(R.string.tap_on_mic_stop);
            statusChanged(Status.RUNNING);
            listening = true;
        }
        displayedMessage.setText(text);

    }

    public void statusChanged(Status status) {
        captionText.setText(status.getStatusCaption(getActivity()));
    }

    private void startStopButtonClicked() {
        Intent intent = new Intent();
        String text = getString(R.string.tap_on_mic_start);
        if (listening)
            intent.setAction(ListenerService.STOP_SERVICE);
        else {
            intent.setAction(ListenerService.START);
            text = getString(R.string.tap_on_mic_stop);
        }
        listening = !listening;
        displayedMessage.setText(text);
        getActivity().sendBroadcast(intent);
    }
}