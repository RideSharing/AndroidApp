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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
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
            String.valueOf(year-3),String.valueOf(year-2),String.valueOf(year-1),String.valueOf(year)+" (*)",String.valueOf(year+1),String.valueOf(year+2),String.valueOf(year+3)
    };
    private String[] orderby;
    private HorizontalBarChart mChart;
    private SeekBar mSeekBarX;
    private TextView tvX;

    private Typeface tf;
    ActionBar actionBar;
    private CustomActionBar custom_actionbar;
    private SessionManager session;
    private SweetAlertDialog pDialog;
    private Spinner spin,subSpin;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_statistic);
        orderby= new String[] {
                getResources().getString(R.string.month).toString(),
                getResources().getString(R.string.year).toString()};
        spin=(Spinner) findViewById(R.id.spOrderBy);
        subSpin=(Spinner) findViewById(R.id.spOrderByYear);
        // Progress dialog
        pDialog = new SweetAlertDialog(this,SweetAlertDialog.PROGRESS_TYPE);
        pDialog.setCancelable(false);
        custom_actionbar=new CustomActionBar(this,actionBar,pDialog,2);
        actionBar=custom_actionbar.getActionBar();
        // Session manager
        session = new SessionManager(getApplicationContext());
        tvX = (TextView) findViewById(R.id.tvXMax);
        mSeekBarX = (SeekBar) findViewById(R.id.seekBar1);
        mChart = (HorizontalBarChart) findViewById(R.id.chart1);
        initChart();
        ArrayAdapter<String> adapter1=new ArrayAdapter<String>
                (
                        this,
                        android.R.layout.simple_spinner_item,
                        orderby
                );
        //phải gọi lệnh này để hiển thị danh sách cho Spinner
        adapter1.setDropDownViewResource
                (android.R.layout.simple_list_item_single_choice);
        //Thiết lập adapter cho Spinner
        spin.setAdapter(adapter1);
        //thiết lập sự kiện chọn phần tử cho Spinner
        spin.setOnItemSelectedListener(new MyProcessEvent());

        ArrayAdapter<String> adapter2=new ArrayAdapter<String>
                (
                        this,
                        android.R.layout.simple_spinner_item,
                        mYears
                );
        //phải gọi lệnh này để hiển thị danh sách cho Spinner
        adapter2.setDropDownViewResource
                (android.R.layout.simple_list_item_single_choice);
        //Thiết lập adapter cho Spinner
        subSpin.setAdapter(adapter2);
        //thiết lập sự kiện chọn phần tử cho Spinner
        subSpin.setOnItemSelectedListener(new MyProcessEvent());
    }


    public void initChart(){
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

        //setData(12, 50,1);
        mChart.animateY(2500);
        // setting data
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
            setData((mSeekBarX.getProgress() < 12) ? mSeekBarX.getProgress() + 1 : mSeekBarX.getProgress(), 200f, 1);
        }
        else{
            tvX.setText("" + ((mSeekBarX.getProgress()<7)?mSeekBarX.getProgress() +1:mSeekBarX.getProgress()));
            setData((mSeekBarX.getProgress() < 7) ? mSeekBarX.getProgress() + 1 : mSeekBarX.getProgress(), 200f, 2);
        }
        mChart.invalidate();
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        // TODO Auto-generated method stub

    }

    private void setData(int count,float range,int orderby) {

        ArrayList<String> xVals = new ArrayList<String>();
        for (int i = 0; i < count; i++) {
            if(orderby==1) {
                xVals.add((Locale.getDefault().getLanguage().equals("en")) ? mMonths_en[i % 12] : mMonths_vi[i % 12]);
            }
            else{
                xVals.add(mYears[i%7]);
            }
        }

        ArrayList<BarEntry> yVals1 = new ArrayList<BarEntry>();

        for (int i = 0; i < count; i++) {
            float mult = (range + 1);
            float val = (float) (Math.random() * mult);
            yVals1.add(new BarEntry(val, i));
        }

        BarDataSet set1 = new BarDataSet(yVals1, "DataSet2");
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

        RectF bounds = mChart.getBarBounds((BarEntry) e);
        PointF position = mChart.getPosition(e, mChart.getData().getDataSetByIndex(dataSetIndex)
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
                    subSpin.setEnabled(true);
                    subSpin.setClickable(true);
                    mSeekBarX.setMax(12);
                    mSeekBarX.setProgress(12);
                    setData(12,200f,1);
                }
                else{
                    subSpin.setEnabled(false);
                    subSpin.setClickable(false);
                    mSeekBarX.setMax(7);
                    mSeekBarX.setProgress(7);
                    setData(7,200f,2);
                }
            }
            else if(spinner.getId() == R.id.spOrderByYear)
            {

            }


        }
        //Nếu không chọn gì cả
        public void onNothingSelected(AdapterView<?> arg0) {

        }
    }
}
