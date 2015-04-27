package com.halley.custom_theme;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.halley.registerandlogin.R;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * Created by enclaveit on 4/21/15.
 */
public class CustomActionBar {
    private ActionBarActivity activity;
    ActionBar actionBar;
    private SweetAlertDialog pDialog;
    private final int ACTIONBAR_MAINSCREEN=1;
    private final int ACTIONBAR_SCREEN=2;

    public CustomActionBar(ActionBarActivity activity, ActionBar actionBar, SweetAlertDialog pDialog,int i) {
        this.activity = activity;
        this.actionBar = actionBar;
        this.pDialog = pDialog;
        if(i==ACTIONBAR_MAINSCREEN) customActionBarMain();
        else customActionBar();
    }


    public ActionBar getActionBar() {
        return actionBar;
    }

    public void setActionBar(ActionBar actionBar) {
        this.actionBar = actionBar;
    }

    public void customActionBarMain() {
        actionBar = activity.getSupportActionBar();
        actionBar.setElevation(0);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setBackgroundDrawable(new ColorDrawable(activity.getResources()
                .getColor(R.color.bg_login)));

        actionBar.setStackedBackgroundDrawable(new ColorDrawable(Color.rgb(19,
                143, 215)));
        // Enabling Up / Back navigation


        LayoutInflater mInflater = LayoutInflater.from(activity);

        View mCustomView = mInflater.inflate(R.layout.custom_actionbar_main,
                null);
        TextView mTitleTextView = (TextView) mCustomView
                .findViewById(R.id.title_text);

        Typeface tf = Typeface.createFromAsset(activity.getAssets(), "fonts/ANGEL.otf");
        mTitleTextView.setTypeface(tf);

        final ImageButton imageButton = (ImageButton) mCustomView
                .findViewById(R.id.imageButton);
        imageButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Dialog dialog = new Dialog(activity);
                dialog.setContentView(R.layout.dialog_info);
                dialog.setTitle(R.string.about_app);
                dialog.show();
            }
        });
        final ImageButton imageButton2 = (ImageButton) mCustomView
                .findViewById(R.id.imageButton2);
        imageButton2.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                pDialog.setTitleText(activity.getResources().getString(R.string.refresh));
                showDialog();

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        hideDialog();
                        activity.finish();
                        activity.startActivity(activity.getIntent());
                        activity.overridePendingTransition(0, 0);
                    }
                }, 2000);

            }
        });

        actionBar.setCustomView(mCustomView);
        actionBar.setDisplayShowCustomEnabled(true);

    }
    public void customActionBar() {
        actionBar = activity.getSupportActionBar();
        actionBar.setElevation(0);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setBackgroundDrawable(new ColorDrawable(activity.getResources()
                .getColor(R.color.bg_login)));
        // Enabling Up / Back navigation
        actionBar.setDisplayHomeAsUpEnabled(true);

        LayoutInflater mInflater = LayoutInflater.from(activity);

        View mCustomView = mInflater.inflate(R.layout.custom_actionbar, null);
        TextView mTitleTextView = (TextView) mCustomView
                .findViewById(R.id.title_text);

        Typeface tf = Typeface.createFromAsset(activity.getAssets(), "fonts/ANGEL.otf");
        mTitleTextView.setTypeface(tf);

        ImageButton imageButton = (ImageButton) mCustomView
                .findViewById(R.id.imageButton);
        imageButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Dialog dialog = new Dialog(activity);
                dialog.setContentView(R.layout.dialog_info);
                dialog.setTitle(R.string.about_app);
                dialog.show();
            }
        });

        actionBar.setCustomView(mCustomView);
        actionBar.setDisplayShowCustomEnabled(true);
    }
    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }
}
