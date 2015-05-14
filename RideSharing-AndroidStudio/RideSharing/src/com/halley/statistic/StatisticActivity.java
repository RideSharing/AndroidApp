package com.halley.statistic;



import android.annotation.SuppressLint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
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
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
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
import com.halley.app.AppConfig;
import com.halley.app.AppController;
import com.halley.custom_theme.CustomActionBar;
import com.halley.helper.SessionManager;
import com.halley.registerandlogin.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class StatisticActivity extends ActionBarActivity implements OnSeekBarChangeListener,
        OnChartValueSelectedListener {
    private final String STATISTIC_ITINERARY="itinerary";
    private final String STATISTIC_TOTAL_MONEY="total_money";
    int year =Calendar.getInstance().get(Calendar.YEAR);

    private List<String> mMonths;
    private List<String> mYear;

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
    private List<StatisticItem> statisticItems;
    MyAsyncTask mtt;
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
        mtt=new MyAsyncTask();
        mtt.execute();
        swStatistic_role.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if(isChecked) {
                    setData(mMonths.size(), 200f, 1, mChartCustomer);
                    lnDriver.setVisibility(View.GONE);
                    lnCustomer.setVisibility(View.VISIBLE);

                }
                else {
                    setData(mMonths.size(), 200f, 1, mChartDriver);
                    lnDriver.setVisibility(View.VISIBLE);
                    lnCustomer.setVisibility(View.GONE);
                }
                spin.setSelection(0);
                spinbyYear.setSelection(0);
                spinbyType.setSelection(0);
                mSeekBarX.setMax(mMonths.size());
                mSeekBarX.setProgress(mMonths.size());
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

        setData(mMonths.size(), 50,1,mChart);
        mChart.animateY(2500);
        // setting data
        mSeekBarX.setMax(mMonths.size());
        mSeekBarX.setProgress(mMonths.size());
        mSeekBarX.setOnSeekBarChangeListener(this);
        Legend l = mChart.getLegend();
        l.setPosition(LegendPosition.BELOW_CHART_LEFT);
        l.setFormSize(8f);
        l.setXEntrySpace(4f);
        // mChart.setDrawLegend(false);

    }
    private void getStatistic(final String url, final String field_statistic,final String year) {
        // Tag used to cancel the request
        String tag_string_req = "req_get_statistic_customer";
        statisticItems= new ArrayList<StatisticItem>();
        StringRequest strReq = new StringRequest(Request.Method.GET,
                url+"/"+field_statistic+"/"+year,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        // Log.d("Login Response: ", response.toString());
                        try {

                            JSONObject jObj = new JSONObject(response
                                    .substring(response.indexOf("{"),
                                            response.lastIndexOf("}") + 1));
                            boolean error = jObj.getBoolean("error");
                            // Check for error node in json

                            if (!error) {
                                JSONArray statistics=jObj.getJSONArray("stats");
                                for(int i=0;i<statistics.length();i++){
                                    JSONObject statistic = statistics.getJSONObject(i);
                                    StatisticItem statisticItem=new StatisticItem();
                                    statisticItem.setDate(statistic.getString("month"));
                                    if(field_statistic.equals(STATISTIC_ITINERARY)){
                                        statisticItem.setNumber_itinerary(statistic.getString("number"));
                                    }
                                    else if(field_statistic.equals(STATISTIC_TOTAL_MONEY)){
                                        statisticItem.setTotal_money(statistic.getString("total_money"));
                                    }
                                    statisticItems.add(statisticItem);
                                }
                                getValx(statisticItems);


                            } else {
                                // Error in login. Get the error message
                                String message = jObj.getString("message");
                                Toast.makeText(getApplicationContext(), message,
                                        Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            // JSON error
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {

                Toast.makeText(getApplicationContext(),
                        R.string.not_connect,
                        Toast.LENGTH_LONG).show();

            }
        }) {

            @Override
            public Map<String, String> getHeaders() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("Authorization", session.getAPIKey());
                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }
    public void getValx(List<StatisticItem> stats){
        mMonths=new ArrayList<String>();
        mYear=new ArrayList<String>();
        for(int i=0;i<stats.size();i++){
            String[] split_date=stats.get(i).getDate().split("-");
           String month=split_date[1];
            if(!mMonths.contains(month)) mMonths.add(month);
            String year=split_date[0];
            if(!mYear.contains(year)) mYear.add(year);
        }
    }


    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        if(seekBar.getMax()==mMonths.size()) {
            tvX.setText("" + ((mSeekBarX.getProgress()<mMonths.size())?mSeekBarX.getProgress() +1:mSeekBarX.getProgress()));
            setData((mSeekBarX.getProgress() < mMonths.size()) ? mSeekBarX.getProgress() + 1 : mSeekBarX.getProgress(), 200f, 1,(swStatistic_role.isChecked())?mChartCustomer:mChartDriver);
        }
        else{
            tvX.setText("" + ((mSeekBarX.getProgress()<mYear.size())?mSeekBarX.getProgress() +1:mSeekBarX.getProgress()));
            setData((mSeekBarX.getProgress() < mYear.size()) ? mSeekBarX.getProgress() + 1 : mSeekBarX.getProgress(), 200f, 2,(swStatistic_role.isChecked())?mChartCustomer:mChartDriver);
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
                xVals.add(mMonths.get(i%mMonths.size()));
            }
            else{
                xVals.add(mYear.get(i%mYear.size()));
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
                    mSeekBarX.setMax(mMonths.size());
                    mSeekBarX.setProgress(mMonths.size());
                    setData(mMonths.size(),200f,1,(swStatistic_role.isChecked())?mChartCustomer:mChartDriver);
                }
                else{
                    spinbyYear.setEnabled(false);
                    spinbyYear.setClickable(false);
                    mSeekBarX.setMax(mYear.size());
                    mSeekBarX.setProgress(mYear.size());
                    setData(mYear.size(),200f,2,(swStatistic_role.isChecked())?mChartCustomer:mChartDriver);
                }
            }
            else if(spinner.getId() == R.id.spOrderByYear)
            {

            }


        }

        public void onNothingSelected(AdapterView<?> arg0) {

        }
    }
    class MyAsyncTask extends AsyncTask<Void, Void, Void> {
        public MyAsyncTask() {

        }

        @Override
        protected void onPreExecute() {
            String year= String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
            if(!swStatistic_role.isChecked()){
                getStatistic(AppConfig.URL_GET_STATISTIC_CUSTOMER,STATISTIC_ITINERARY,year);

            }
            else{
                getStatistic(AppConfig.URL_GET_STATISTIC_DRIVER,STATISTIC_ITINERARY,year);
            }

            super.onPreExecute();


        }

        @Override
        protected Void doInBackground(Void... arg0) {
            SystemClock.sleep(3000);
            publishProgress();
            return null;
        }

        /**
         * update layout in function
         */
        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            initChart(mChartCustomer);
            initChart(mChartDriver);
            if(!session.isDriver()){
                lnSwitchRole.setVisibility(View.GONE);
                lnDriver.setVisibility(View.GONE);
            }
            else {
                if (swStatistic_role.isChecked()) {
                    lnDriver.setVisibility(View.GONE);
                    lnCustomer.setVisibility(View.VISIBLE);

                } else {
                    lnDriver.setVisibility(View.VISIBLE);
                    lnCustomer.setVisibility(View.GONE);
                }
                initSpinner(spin);
                initSpinner(spinbyYear);
                initSpinner(spinbyType);
            }




        }

        /**
         * after process completed then this function will run
         */
        @Override
        protected void onPostExecute(Void result) {

        }

    }
}
