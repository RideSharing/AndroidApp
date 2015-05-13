package com.halley.statistic;



import android.annotation.SuppressLint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.Legend.LegendPosition;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.XAxis.XAxisPosition;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import com.github.mikephil.charting.data.Entry;

import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.Highlight;
import com.halley.custom_theme.CustomActionBar;
import com.halley.helper.SessionManager;
import com.halley.registerandlogin.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class StatisticActivity extends ActionBarActivity implements OnSeekBarChangeListener,
        OnChartValueSelectedListener {
    int year =Calendar.getInstance().get(Calendar.YEAR);
    private String[] mMonths_en = new String[] {
            "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    };
    private String[] mMonths_vi = new String[] {
            "Một", "Hai", "Ba", "Bốn", "Năm", "Sáu", "Bảy", "Tám", "Chín", "Mười", "Mười một", "Mười hai"
    };
    private String[] mYears =new String[]{
            String.valueOf(year-3),String.valueOf(year-2),String.valueOf(year-1),String.valueOf(year)+" (*)"
    };
    private String[] mType;
    private String[] orderby;
    private HorizontalBarChart mChartCustomer,mChartDriver;
    private SeekBar mSeekBarX;
    private TextView tvX;
    private LinearLayout lnDriver,lnCustomer,lnSwitchRole;
    private Typeface tf;
    ActionBar actionBar;
    private CustomActionBar custom_actionbar;
    private SessionManager session;
    private SweetAlertDialog pDialog;
    private Spinner spin,spinbyYear,spinbyType;
    Switch swStatistic_role;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistic);
        orderby= new String[] {
                getResources().getString(R.string.month).toString(),
                getResources().getString(R.string.year).toString()};
        mType= new String[] {
                getResources().getString(R.string.statistic_itinerary).toString(),
                getResources().getString(R.string.statistic_money).toString()};
        lnDriver=(LinearLayout)findViewById(R.id.layoutDriver);
        lnCustomer=(LinearLayout)findViewById(R.id.layoutCustomer);
        lnSwitchRole=(LinearLayout)findViewById(R.id.layout_SwitchRole);
        swStatistic_role=(Switch)findViewById(R.id.statistic_role);
        swStatistic_role.setChecked(true);
        mChartCustomer = (HorizontalBarChart) findViewById(R.id.chartCustomer);
        mChartDriver = (HorizontalBarChart) findViewById(R.id.chartDriver);
        spin=(Spinner) findViewById(R.id.spOrderBy);
        spinbyYear=(Spinner) findViewById(R.id.spOrderByYear);
        spinbyType=(Spinner) findViewById(R.id.spOrderByType);
        // Progress dialog
        pDialog = new SweetAlertDialog(this,SweetAlertDialog.PROGRESS_TYPE);
        pDialog.setCancelable(false);
        custom_actionbar=new CustomActionBar(this,actionBar,pDialog,2);
        actionBar=custom_actionbar.getActionBar();
        // Session manager
        session = new SessionManager(getApplicationContext());
        tvX = (TextView) findViewById(R.id.tvXMax);
        mSeekBarX = (SeekBar) findViewById(R.id.seekBar1);
        initChart(mChartCustomer);
        initChart(mChartDriver);
        if(!session.isDriver()){
            lnSwitchRole.setVisibility(View.GONE);
            lnDriver.setVisibility(View.GONE);
        }
        else {
            if (swStatistic_role.isChecked()) {
                setData(12, 200f, 1, mChartCustomer);
                lnDriver.setVisibility(View.GONE);
                lnCustomer.setVisibility(View.VISIBLE);

            } else {
                setData(12, 200f, 1, mChartDriver);
                lnDriver.setVisibility(View.VISIBLE);
                lnCustomer.setVisibility(View.GONE);
            }
        }

        initSpinner(spin);
        initSpinner(spinbyYear);
        initSpinner(spinbyType);
        swStatistic_role.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if(isChecked) {
                    setData(12, 200f, 1, mChartCustomer);
                    lnDriver.setVisibility(View.GONE);
                    lnCustomer.setVisibility(View.VISIBLE);

                }
                else {
                    setData(12, 200f, 1, mChartDriver);
                    lnDriver.setVisibility(View.VISIBLE);
                    lnCustomer.setVisibility(View.GONE);
                }
                spin.setSelection(0);
                spinbyYear.setSelection(0);
                spinbyType.setSelection(0);
                mSeekBarX.setMax(12);
                mSeekBarX.setProgress(12);
            }
        });
    }

    public void initSpinner(Spinner sp){
        ArrayAdapter<String> adapter=null;
        if(sp.getId()==R.id.spOrderBy){
             adapter=new ArrayAdapter<String>
                    (
                            this,
                            android.R.layout.simple_spinner_item,
                            orderby
                    );

        }else if(sp.getId()==R.id.spOrderByYear){
            adapter=new ArrayAdapter<String>
                    (
                            this,
                            android.R.layout.simple_spinner_item,
                            mYears
                    );

        }
        else if(sp.getId()==R.id.spOrderByType){
            adapter=new ArrayAdapter<String>
                    (
                            this,
                            android.R.layout.simple_spinner_item,
                            mType
                    );
        }
        adapter.setDropDownViewResource
                (android.R.layout.simple_spinner_dropdown_item);
        sp.setAdapter(adapter);
        sp.setOnItemSelectedListener(new MyProcessEvent());

    }


    public void initChart(HorizontalBarChart mChart){
        mChart.setOnChartValueSelectedListener(this);
        // mChart.setHighlightEnabled(false);
        mChart.setDrawBarShadow(false);

        mChart.setDrawValueAboveBar(true);
        mChart.setDescription("");

        // if more than 60 entries are displayed in the chart, no values will be
        // drawn
        mChart.setMaxVisibleValueCount(60);

        // scaling can now only be done on x- and y-axis separately
        mChart.setPinchZoom(false);

        // draw shadows for each bar that show the maximum value
        // mChart.setDrawBarShadow(true);

        // mChart.setDrawXLabels(false);

        mChart.setDrawGridBackground(false);

        // mChart.setDrawYLabels(false);

        tf = Typeface.createFromAsset(getAssets(), "fonts/OpenSans-Regular.ttf");

        XAxis xl = mChart.getXAxis();
        xl.setPosition(XAxisPosition.BOTTOM);
        xl.setTypeface(tf);
        xl.setDrawAxisLine(true);
        xl.setDrawGridLines(true);
        xl.setGridLineWidth(0.3f);

        YAxis yl = mChart.getAxisLeft();
        yl.setTypeface(tf);
        yl.setDrawAxisLine(true);
        yl.setDrawGridLines(true);
        yl.setGridLineWidth(0.3f);
//        yl.setInverted(true);

        YAxis yr = mChart.getAxisRight();
        yr.setTypeface(tf);
        yr.setDrawAxisLine(true);
        yr.setDrawGridLines(false);
//        yr.setInverted(true);

        setData(12, 50,1,mChart);
        mChart.animateY(2500);
        // setting data
        mSeekBarX.setMax(12);
        mSeekBarX.setProgress(12);
        mSeekBarX.setOnSeekBarChangeListener(this);
        Legend l = mChart.getLegend();
        l.setPosition(LegendPosition.BELOW_CHART_LEFT);
        l.setFormSize(8f);
        l.setXEntrySpace(4f);
        // mChart.setDrawLegend(false);

    }


    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        if(seekBar.getMax()==12) {
            tvX.setText("" + ((mSeekBarX.getProgress()<12)?mSeekBarX.getProgress() +1:mSeekBarX.getProgress()));
            setData((mSeekBarX.getProgress() < 12) ? mSeekBarX.getProgress() + 1 : mSeekBarX.getProgress(), 200f, 1,(swStatistic_role.isChecked())?mChartCustomer:mChartDriver);
        }
        else{
            tvX.setText("" + ((mSeekBarX.getProgress()<4)?mSeekBarX.getProgress() +1:mSeekBarX.getProgress()));
            setData((mSeekBarX.getProgress() < 4) ? mSeekBarX.getProgress() + 1 : mSeekBarX.getProgress(), 200f, 2,(swStatistic_role.isChecked())?mChartCustomer:mChartDriver);
        }
        if(swStatistic_role.isChecked())
            mChartCustomer.invalidate();
        else
            mChartDriver.invalidate();
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        // TODO Auto-generated method stub

    }

    private void setData(int count,float range,int orderby,HorizontalBarChart mChart) {

        ArrayList<String> xVals = new ArrayList<String>();
        for (int i = 0; i < count; i++) {
            if(orderby==1) {
                xVals.add((Locale.getDefault().getLanguage().equals("en")) ? mMonths_en[i % 12] : mMonths_vi[i % 12]);
            }
            else{
                xVals.add(mYears[i%4]);
            }
        }

        ArrayList<BarEntry> yVals1 = new ArrayList<BarEntry>();

        for (int i = 0; i < count; i++) {
            float mult = (range + 1);
            float val = (float) (Math.random() * mult);
            yVals1.add(new BarEntry(val, i));
        }

        BarDataSet set1 = new BarDataSet(yVals1, getResources().getString(R.string.statistic_itinerary)+"/"+getResources().getString(R.string.statistic_money));
        set1.setBarSpacePercent(35f);

        ArrayList<BarDataSet> dataSets = new ArrayList<BarDataSet>();
        dataSets.add(set1);

        BarData data = new BarData(xVals, dataSets);
        data.setValueTextSize(10f);
        data.setValueTypeface(tf);
        mChart.setData(data);
    }

    @SuppressLint("NewApi")
    @Override
    public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {

        if (e == null)
            return;

        RectF bounds = mChartCustomer.getBarBounds((BarEntry) e);
        PointF position = mChartCustomer.getPosition(e, mChartCustomer.getData().getDataSetByIndex(dataSetIndex)
                .getAxisDependency());



        Log.i("bounds", bounds.toString());
        Log.i("position", position.toString());
    }

    public void onNothingSelected() {
    };
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.move_left_in_activity, R.anim.move_right_out_activity);
        finish();
    }

    private class MyProcessEvent implements
            AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parent, View arg1, int arg2, long arg3) {
            Spinner spinner = (Spinner) parent;
            if(spinner.getId() == R.id.spOrderBy)
            {
                if(arg2==0){
                    spinbyYear.setEnabled(true);
                    spinbyYear.setClickable(true);
                    mSeekBarX.setMax(12);
                    mSeekBarX.setProgress(12);
                    setData(12,200f,1,(swStatistic_role.isChecked())?mChartCustomer:mChartDriver);
                }
                else{
                    spinbyYear.setEnabled(false);
                    spinbyYear.setClickable(false);
                    mSeekBarX.setMax(4);
                    mSeekBarX.setProgress(4);
                    setData(4,200f,2,(swStatistic_role.isChecked())?mChartCustomer:mChartDriver);
                }
            }
            else if(spinner.getId() == R.id.spOrderByYear)
            {

            }


        }

        public void onNothingSelected(AdapterView<?> arg0) {

        }
    }
}
