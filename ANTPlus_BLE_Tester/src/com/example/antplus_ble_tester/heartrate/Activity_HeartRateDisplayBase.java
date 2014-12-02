/*
This software is subject to the license described in the License.txt file
included with this software distribution. You may not use this file except in compliance
with this license.

Copyright (c) Dynastream Innovations Inc. 2013
All rights reserved.
 */

package com.example.antplus_ble_tester.heartrate;

import com.dsi.ant.plugins.antplus.pcc.AntPlusHeartRatePcc;
import com.dsi.ant.plugins.antplus.pcc.AntPlusHeartRatePcc.DataState;
import com.dsi.ant.plugins.antplus.pcc.AntPlusHeartRatePcc.ICalculatedRrIntervalReceiver;
import com.dsi.ant.plugins.antplus.pcc.AntPlusHeartRatePcc.IHeartRateDataReceiver;
import com.dsi.ant.plugins.antplus.pcc.AntPlusHeartRatePcc.IPage4AddtDataReceiver;
import com.dsi.ant.plugins.antplus.pcc.AntPlusHeartRatePcc.RrFlag;
import com.dsi.ant.plugins.antplus.pcc.defines.DeviceState;
import com.dsi.ant.plugins.antplus.pcc.defines.EventFlag;
import com.dsi.ant.plugins.antplus.pcc.defines.RequestAccessResult;
import com.dsi.ant.plugins.antplus.pccbase.AntPluginPcc.IDeviceStateChangeReceiver;
import com.dsi.ant.plugins.antplus.pccbase.AntPluginPcc.IPluginAccessResultReceiver;
import com.dsi.ant.plugins.antplus.pccbase.AntPlusLegacyCommonPcc.ICumulativeOperatingTimeReceiver;
import com.dsi.ant.plugins.antplus.pccbase.AntPlusLegacyCommonPcc.IManufacturerAndSerialReceiver;
import com.dsi.ant.plugins.antplus.pccbase.AntPlusLegacyCommonPcc.IVersionAndModelReceiver;
import com.dsi.ant.plugins.antplus.pccbase.PccReleaseHandle;
import com.example.antplus_ble_tester.R;
import com.shinobicontrols.charts.ChartFragment;
import com.shinobicontrols.charts.DataAdapter;
import com.shinobicontrols.charts.DataPoint;
import com.shinobicontrols.charts.DateRange;
import com.shinobicontrols.charts.DateTimeAxis;
import com.shinobicontrols.charts.LineSeries;
import com.shinobicontrols.charts.NumberAxis;
import com.shinobicontrols.charts.NumberRange;
import com.shinobicontrols.charts.SeriesStyle;
import com.shinobicontrols.charts.ShinobiChart;
import com.shinobicontrols.charts.SimpleDataAdapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigDecimal;
import java.util.Date;
import java.util.EnumSet;

/**
 * Base class to connects to Heart Rate Plugin and display all the event data.
 */
public abstract class Activity_HeartRateDisplayBase extends Activity {

    public static final int SAMPLES_TIME_SHOW = 15 * 1000;

    protected abstract void requestAccessToPcc();

    AntPlusHeartRatePcc hrPcc = null;
    protected PccReleaseHandle<AntPlusHeartRatePcc> releaseHandle = null;

    TextView tv_status;

    TextView tv_estTimestamp;

    TextView tv_computedHeartRate;
    TextView tv_heartBeatCounter;
    TextView tv_heartBeatEventTime;

    TextView tv_manufacturerSpecificByte;
    TextView tv_previousHeartBeatEventTime;

    TextView tv_calculatedRrInterval;

    TextView tv_cumulativeOperatingTime;

    TextView tv_manufacturerID;
    TextView tv_serialNumber;

    TextView tv_hardwareVersion;
    TextView tv_softwareVersion;
    TextView tv_modelNumber;

    TextView tv_dataStatus;
    TextView tv_rrFlag;

    ImageView mHeartBump;
    TextView mHearRateValue;
    private int mComputedHEartRate = -1;

    private ShinobiChart shinobiChart;
    private LineSeries mSeries;
    private DateTimeAxis xAxis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handleReset();
    }

    private void setupChart() {
        // Get the a reference to the ShinobiChart from the ChartFragment
        ChartFragment chartFragment = (ChartFragment) getFragmentManager().findFragmentById(R.id.chart);
        shinobiChart = chartFragment.getShinobiChart();

        shinobiChart.getStyle().setPlotAreaBackgroundColor(getResources().getColor(android.R.color.white));

        // Create the axes, set their titles, and add them to the chart
        xAxis = new DateTimeAxis();
        Date currentDate = new Date();
        xAxis.setDefaultRange(new DateRange(currentDate, new Date(currentDate.getTime() + SAMPLES_TIME_SHOW)));
        xAxis.getStyle().getTickStyle().setMajorTicksShown(false);
        xAxis.getStyle().getTickStyle().setMinorTicksShown(false);
        xAxis.getStyle().getTickStyle().setLabelTextSize(8);
        xAxis.getStyle().getTickStyle().setLabelColor(getResources().getColor(android.R.color.white));
        xAxis.getStyle().setLineColor(getResources().getColor(android.R.color.holo_blue_dark));
        xAxis.getStyle().setLineColor(getResources().getColor(android.R.color.holo_blue_dark));
        xAxis.getStyle().setLineWidth(1f);
        xAxis.enableAnimation(true);

        shinobiChart.setXAxis(xAxis);

        NumberAxis yAxis = new NumberAxis();
        yAxis.setDefaultRange(new NumberRange(new Double(0), new Double(275)));
        yAxis.getStyle().getTickStyle().setLabelTextSize(10);
        yAxis.getStyle().getTickStyle().setLineLength(6);
        yAxis.getStyle().getTickStyle().setLineWidth(0.5f);
        yAxis.setMajorTickFrequency(new Double(50));
        yAxis.getStyle().setLineColor(getResources().getColor(android.R.color.holo_blue_dark));
        yAxis.getStyle().setLineWidth(1f);
        shinobiChart.setYAxis(yAxis);

        mSeries = new LineSeries();
        mSeries.setDataAdapter(new SimpleDataAdapter<Date, Float>());
        shinobiChart.addSeries(mSeries);

        mSeries.getStyle().setAreaLineColor(getResources().getColor(android.R.color.holo_green_dark));
        mSeries.getStyle().setAreaLineWidth(3f);
        mSeries.getStyle().setAreaColor(0x66669900);
        mSeries.getStyle().setFillStyle(SeriesStyle.FillStyle.FLAT);

    }

    /**
     * Resets the PCC connection to request access again and clears any existing display data.
     */
    protected void handleReset() {
        //Release the old access if it exists
        if (releaseHandle != null) {
            releaseHandle.close();
        }

        requestAccessToPcc();
    }

    protected void showDataDisplay(String status) {
        setContentView(R.layout.activity_heart_rate);

        setupChart();

        tv_status = (TextView) findViewById(R.id.textView_Status);

        tv_estTimestamp = (TextView) findViewById(R.id.textView_EstTimestamp);

        tv_computedHeartRate = (TextView) findViewById(R.id.textView_ComputedHeartRate);
        tv_heartBeatCounter = (TextView) findViewById(R.id.textView_HeartBeatCounter);
        tv_heartBeatEventTime = (TextView) findViewById(R.id.textView_HeartBeatEventTime);

        tv_manufacturerSpecificByte = (TextView) findViewById(R.id.textView_ManufacturerSpecificByte);
        tv_previousHeartBeatEventTime = (TextView) findViewById(R.id.textView_PreviousHeartBeatEventTime);

        tv_calculatedRrInterval = (TextView) findViewById(R.id.textView_CalculatedRrInterval);

        tv_cumulativeOperatingTime = (TextView) findViewById(R.id.textView_CumulativeOperatingTime);

        tv_manufacturerID = (TextView) findViewById(R.id.textView_ManufacturerID);
        tv_serialNumber = (TextView) findViewById(R.id.textView_SerialNumber);

        tv_hardwareVersion = (TextView) findViewById(R.id.textView_HardwareVersion);
        tv_softwareVersion = (TextView) findViewById(R.id.textView_SoftwareVersion);
        tv_modelNumber = (TextView) findViewById(R.id.textView_ModelNumber);

        tv_dataStatus = (TextView) findViewById(R.id.textView_DataStatus);
        tv_rrFlag = (TextView) findViewById(R.id.textView_rRFlag);

        // Heart rate bump icon
        mHeartBump = (ImageView) findViewById(R.id.heart_bump);
        mHearRateValue = (TextView) findViewById(R.id.heart_rate_value);

        //Reset the text display
        tv_status.setText(status);

        resetScreen();
    }

    private void resetScreen() {
        mHeartBump.setVisibility(View.GONE);

        tv_estTimestamp.setText("---");

        tv_computedHeartRate.setText("---");
        tv_heartBeatCounter.setText("---");
        tv_heartBeatEventTime.setText("---");

        tv_manufacturerSpecificByte.setText("---");
        tv_previousHeartBeatEventTime.setText("---");

        tv_calculatedRrInterval.setText("---");

        tv_cumulativeOperatingTime.setText("---");

        tv_manufacturerID.setText("---");
        tv_serialNumber.setText("---");

        tv_hardwareVersion.setText("---");
        tv_softwareVersion.setText("---");
        tv_modelNumber.setText("---");
        tv_dataStatus.setText("---");
        tv_rrFlag.setText("---");
    }

    /**
     * Switches the active view to the data display and subscribes to all the data events
     */
    public void subscribeToHrEvents() {

        hrPcc.subscribeHeartRateDataEvent(new IHeartRateDataReceiver() {
            @Override
            public void onNewHeartRateData(final long estTimestamp, EnumSet<EventFlag> eventFlags,
                    final int computedHeartRate, final long heartBeatCount,
                    final BigDecimal heartBeatEventTime, final DataState dataState) {
                // Mark heart rate with asterisk if zero detected
                final String textHeartRate = String.valueOf(computedHeartRate)
                        + ((DataState.ZERO_DETECTED.equals(dataState)) ? "*" : "");

                mComputedHEartRate = computedHeartRate;
                addGraphValue(mComputedHEartRate);

                // Mark heart beat count and heart beat event time with asterisk if initial value
                final String textHeartBeatCount = String.valueOf(heartBeatCount)
                        + ((DataState.INITIAL_VALUE.equals(dataState)) ? "*" : "");
                final String textHeartBeatEventTime = String.valueOf(heartBeatEventTime)
                        + ((DataState.INITIAL_VALUE.equals(dataState)) ? "*" : "");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv_estTimestamp.setText(String.valueOf(estTimestamp));

                        tv_computedHeartRate.setText(textHeartRate);
                        mHearRateValue.setText(textHeartRate);
                        tv_heartBeatCounter.setText(textHeartBeatCount);
                        tv_heartBeatEventTime.setText(textHeartBeatEventTime);

                        tv_dataStatus.setText(dataState.toString());
                    }
                });
            }
        });

        hrPcc.subscribePage4AddtDataEvent(new IPage4AddtDataReceiver() {
            @Override
            public void onNewPage4AddtData(final long estTimestamp, final EnumSet<EventFlag> eventFlags,
                    final int manufacturerSpecificByte,
                    final BigDecimal previousHeartBeatEventTime) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv_estTimestamp.setText(String.valueOf(estTimestamp));

                        tv_manufacturerSpecificByte.setText(String.format("0x%02X", manufacturerSpecificByte));
                        tv_previousHeartBeatEventTime.setText(String.valueOf(previousHeartBeatEventTime));
                    }
                });
            }
        });

        hrPcc.subscribeCumulativeOperatingTimeEvent(new ICumulativeOperatingTimeReceiver() {
            @Override
            public void onNewCumulativeOperatingTime(final long estTimestamp, final EnumSet<EventFlag> eventFlags,
                    final long cumulativeOperatingTime) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv_estTimestamp.setText(String.valueOf(estTimestamp));

                        tv_cumulativeOperatingTime.setText(String.valueOf(cumulativeOperatingTime));
                    }
                });
            }
        });

        hrPcc.subscribeManufacturerAndSerialEvent(new IManufacturerAndSerialReceiver() {
            @Override
            public void onNewManufacturerAndSerial(final long estTimestamp, final EnumSet<EventFlag> eventFlags,
                    final int manufacturerID,
                    final int serialNumber) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv_estTimestamp.setText(String.valueOf(estTimestamp));

                        tv_manufacturerID.setText(String.valueOf(manufacturerID));
                        tv_serialNumber.setText(String.valueOf(serialNumber));
                    }
                });
            }
        });

        hrPcc.subscribeVersionAndModelEvent(new IVersionAndModelReceiver() {
            @Override
            public void onNewVersionAndModel(final long estTimestamp, final EnumSet<EventFlag> eventFlags,
                    final int hardwareVersion,
                    final int softwareVersion, final int modelNumber) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv_estTimestamp.setText(String.valueOf(estTimestamp));

                        tv_hardwareVersion.setText(String.valueOf(hardwareVersion));
                        tv_softwareVersion.setText(String.valueOf(softwareVersion));
                        tv_modelNumber.setText(String.valueOf(modelNumber));
                    }
                });
            }
        });

        hrPcc.subscribeCalculatedRrIntervalEvent(new ICalculatedRrIntervalReceiver() {
            @Override
            public void onNewCalculatedRrInterval(final long estTimestamp,
                    EnumSet<EventFlag> eventFlags, final BigDecimal rrInterval, final RrFlag flag) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv_estTimestamp.setText(String.valueOf(estTimestamp));
                        tv_rrFlag.setText(flag.toString());

                        // Mark RR with asterisk if source is not cached or page 4
                        if (flag.equals(RrFlag.DATA_SOURCE_CACHED)
                                || flag.equals(RrFlag.DATA_SOURCE_PAGE_4)) {
                            tv_calculatedRrInterval.setText(String.valueOf(rrInterval));
                        } else {
                            tv_calculatedRrInterval.setText(String.valueOf(rrInterval) + "*");
                        }
                    }
                });
            }
        });
    }

    private void addGraphValue(final int mComputedHEartRate) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Date now = new Date();
                DataPoint<Date, Float> dataPoint = new DataPoint<Date, Float>(now, (float) mComputedHEartRate);
                ((DataAdapter<Date, Float>) mSeries.getDataAdapter()).add(dataPoint);
                // update the graph view
                setDefaultRangeX(now);
            }
        });
    }

    /**
     * Sets the default displayed window's bounds.
     */
    public void setDefaultRangeX(Date lastAddedDate) {
        /*
         * We have to cover 60 seconds so if:
         *  - we have less then 60 seconds data then we have to extend into the future the windows
         *  - if we have more then 60 seconds we just simply show the last 60 seconds
         */
        Date firstSampleTime = (Date) mSeries.getDataAdapter().get(0).getX();

        if (lastAddedDate.getTime() - firstSampleTime.getTime() >= SAMPLES_TIME_SHOW) {
            Date minimum = new Date(lastAddedDate.getTime() - SAMPLES_TIME_SHOW);
            xAxis.setDefaultRange(new DateRange(minimum, lastAddedDate));
            xAxis.requestCurrentDisplayedRange(minimum, lastAddedDate, true, true);
        } else {

            /*
             * If we just added our fist sample then we set the view to be
             */
            if (mSeries.getDataAdapter().size() == 1) {
                // get the first samples time
                Date max = new Date(firstSampleTime.getTime() + SAMPLES_TIME_SHOW);
                xAxis.setDefaultRange(new DateRange(firstSampleTime, max));
            }

        }
    }


    protected IPluginAccessResultReceiver<AntPlusHeartRatePcc> base_IPluginAccessResultReceiver =
            new IPluginAccessResultReceiver<AntPlusHeartRatePcc>() {
                //Handle the result, connecting to events on success or reporting failure to user.
                @Override
                public void onResultReceived(AntPlusHeartRatePcc result, RequestAccessResult resultCode,
                        DeviceState initialDeviceState) {

                    if (isFinishing()) {
                        return;
                    }

                    showDataDisplay("Connecting...");
                    switch (resultCode) {
                        case SUCCESS:
                            hrPcc = result;
                            tv_status.setText(result.getDeviceName() + ": " + initialDeviceState);
                            subscribeToHrEvents();
                            startHeartRateAnimation();
                            break;
                        case CHANNEL_NOT_AVAILABLE:
                            Toast.makeText(Activity_HeartRateDisplayBase.this, "Channel Not Available",
                                           Toast.LENGTH_SHORT).show();
                            tv_status.setText("Error. Do Menu->Reset.");
                            break;
                        case ADAPTER_NOT_DETECTED:
                            Toast.makeText(Activity_HeartRateDisplayBase.this,
                                           "ANT Adapter Not Available. Built-in ANT hardware or external adapter required.",
                                           Toast.LENGTH_SHORT).show();
                            tv_status.setText("Error. Do Menu->Reset.");
                            break;
                        case BAD_PARAMS:
                            //Note: Since we compose all the params ourself, we should never see this result
                            Toast.makeText(Activity_HeartRateDisplayBase.this, "Bad request parameters.",
                                           Toast.LENGTH_SHORT).show();
                            tv_status.setText("Error. Do Menu->Reset.");
                            break;
                        case OTHER_FAILURE:
                            Toast.makeText(Activity_HeartRateDisplayBase.this,
                                           "RequestAccess failed. See logcat for details.", Toast.LENGTH_SHORT).show();
                            tv_status.setText("Error. Do Menu->Reset.");
                            break;
                        case DEPENDENCY_NOT_INSTALLED:
                            tv_status.setText("Error. Do Menu->Reset.");
                            AlertDialog.Builder adlgBldr = new AlertDialog.Builder(Activity_HeartRateDisplayBase.this);
                            adlgBldr.setTitle("Missing Dependency");
                            adlgBldr.setMessage(
                                    "The required service\n\"" + AntPlusHeartRatePcc.getMissingDependencyName() + "\"\n was not found. You need to install the ANT+ Plugins service or you may need to update your existing version if you already have it. Do you want to launch the Play Store to get it?");
                            adlgBldr.setCancelable(true);
                            adlgBldr.setPositiveButton("Go to Store", new OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent startStore = null;
                                    startStore = new Intent(Intent.ACTION_VIEW, Uri.parse(
                                            "market://details?id=" + AntPlusHeartRatePcc.getMissingDependencyPackageName()));
                                    startStore.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                                    Activity_HeartRateDisplayBase.this.startActivity(startStore);
                                }
                            });
                            adlgBldr.setNegativeButton("Cancel", new OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });

                            final AlertDialog waitDialog = adlgBldr.create();
                            waitDialog.show();
                            break;
                        case USER_CANCELLED:
                            tv_status.setText("Cancelled. Do Menu->Reset.");
                            break;
                        case UNRECOGNIZED:
                            Toast.makeText(Activity_HeartRateDisplayBase.this,
                                           "Failed: UNRECOGNIZED. PluginLib Upgrade Required?",
                                           Toast.LENGTH_SHORT).show();
                            tv_status.setText("Error. Do Menu->Reset.");
                            break;
                        default:
                            Toast.makeText(Activity_HeartRateDisplayBase.this, "Unrecognized result: " + resultCode,
                                           Toast.LENGTH_SHORT).show();
                            tv_status.setText("Error. Do Menu->Reset.");
                            break;
                    }
                }
            };

    /**
     * Starts an animation based on the computed actual heart rate
     */
    private void startHeartRateAnimation() {

        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    if (mComputedHEartRate == -1) {
                        return null;
                    }

                    // The number of milliseconds between two heart bumps
                    int totalInterval = 60 * 1000 / mComputedHEartRate;

                    showRedHeart(View.VISIBLE);
                    Thread.sleep(totalInterval / 6);
                    Thread.sleep(totalInterval / 6);
                    showRedHeart(View.INVISIBLE);
                    Thread.sleep(totalInterval / 6);
                    Thread.sleep(totalInterval / 2);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                startHeartRateAnimation();
            }
        }.execute();

    }

    private void showRedHeart(final int visibilityStatus) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mHeartBump.setVisibility(visibilityStatus);
            }
        });
    }

    //Receives state changes and shows it on the status display line
    protected IDeviceStateChangeReceiver base_IDeviceStateChangeReceiver =
            new IDeviceStateChangeReceiver() {
                @Override
                public void onDeviceStateChange(final DeviceState newDeviceState) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tv_status.setText(hrPcc.getDeviceName() + ": " + newDeviceState);
                        }
                    });


                }
            };

    @Override
    protected void onDestroy() {
        if (releaseHandle != null) {
            releaseHandle.close();
        }
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_heart_rate, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_reset:
                handleReset();
                tv_status.setText("Resetting...");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
