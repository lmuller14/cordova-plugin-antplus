package cz.zcu.kiv.neuroinformatics.antplus;

import android.content.Context;
import org.apache.cordova.*;

import java.math.BigDecimal;
import java.util.EnumSet;
import org.json.JSONException;
import org.json.JSONObject;

import com.dsi.ant.plugins.antplus.pcc.AntPlusHeartRatePcc;
import com.dsi.ant.plugins.antplus.pcc.defines.DeviceState;
import com.dsi.ant.plugins.antplus.pcc.defines.EventFlag;
import com.dsi.ant.plugins.antplus.pcc.defines.RequestAccessResult;
import com.dsi.ant.plugins.antplus.pccbase.AntPluginPcc;
import com.dsi.ant.plugins.antplus.pccbase.AntPlusLegacyCommonPcc;
import com.dsi.ant.plugins.antplus.pccbase.PccReleaseHandle;

public class AntplusHeartRateService {

    private CallbackContext callbackContext;
    private Context context;

    private AntPlusHeartRatePcc hrPcc = null;
    private PccReleaseHandle<AntPlusHeartRatePcc> releaseHandle = null;

    public AntplusHeartRateService(Context context) {
        this.context = context;
    }

    //
    private void destroy() {
        if (releaseHandle != null) {
            releaseHandle.close();
        }
    }

    //
    private void sendResultOK(JSONObject r) {
        try {
            r.put("antDeviceNumber", hrPcc.getAntDeviceNumber());
            r.put("timestamp", System.currentTimeMillis());
        } catch (JSONException e) {
            System.err.println(e.getMessage());
        }        
        PluginResult result = new PluginResult(PluginResult.Status.OK, r);
        result.setKeepCallback(true);
        callbackContext.sendPluginResult(result);
    }

    //
    private void sendResultError(String error, RequestAccessResult resultCode) {
        JSONObject r = new JSONObject();
        try {
            r.put("event", "error");
            r.put("message", error);
            r.put("code", resultCode);
            r.put("antDeviceNumber", hrPcc.getAntDeviceNumber());
        } catch (JSONException e) {
            System.err.println(e.getMessage());
        }
        PluginResult result = new PluginResult(PluginResult.Status.ERROR, r);
        result.setKeepCallback(true);
        callbackContext.sendPluginResult(result);
    }

    //
    public void subscribe(int antDeviceNumber, CallbackContext callbackContext) {
        this.callbackContext = callbackContext;
        releaseHandle = AntPlusHeartRatePcc.requestAccess(context, antDeviceNumber, 0, base_IPluginAccessResultReceiver, base_IDeviceStateChangeReceiver);
    }

    //
    public void unsubscribe(CallbackContext callbackContext) {
        this.callbackContext = callbackContext;
        destroy();
    }

    public void subscribeToHrEvents() {
        hrPcc.subscribeHeartRateDataEvent(new AntPlusHeartRatePcc.IHeartRateDataReceiver() {
            @Override
            public void onNewHeartRateData(final long estTimestamp, EnumSet<EventFlag> eventFlags,
                    final int computedHeartRate, final long heartBeatCount,
                    final BigDecimal heartBeatEventTime, final AntPlusHeartRatePcc.DataState dataState) {

                JSONObject r = new JSONObject();
                try {
                    r.put("event", "heartRateData");                    
                    r.put("eventFlags", eventFlags);
                    r.put("heartRate", computedHeartRate);
                    r.put("heartBeatCount", heartBeatCount);
                    r.put("heartBeatEventTime", heartBeatEventTime);
                    r.put("dataState", dataState);
                } catch (JSONException e) {
                    System.err.println(e.getMessage());
                }

                sendResultOK(r);
            }
        });

        hrPcc.subscribePage4AddtDataEvent(
                new AntPlusHeartRatePcc.IPage4AddtDataReceiver() {
                    @Override
                    public void onNewPage4AddtData(long estTimestamp, EnumSet<EventFlag> eventFlags, int manufacturerSpecificByte,
                            BigDecimal previousHeartBeatEventTime) {

                        JSONObject r = new JSONObject();
                        try {
                            r.put("event", "page4AddtData");                            
                            r.put("eventFlags", eventFlags);
                            r.put("estTimestamp", estTimestamp);
                            r.put("manufacturerSpecificByte", manufacturerSpecificByte);
                            r.put("previousHeartBeatEventTime", previousHeartBeatEventTime);
                        } catch (JSONException e) {
                            System.err.println(e.getMessage());
                        }

                        sendResultOK(r);
                    }
                });

        hrPcc.subscribeCumulativeOperatingTimeEvent(new AntPlusLegacyCommonPcc.ICumulativeOperatingTimeReceiver() {
            @Override
            public void onNewCumulativeOperatingTime(final long estTimestamp, final EnumSet<EventFlag> eventFlags, final long cumulativeOperatingTime) {
                JSONObject r = new JSONObject();
                try {
                    r.put("event", "cumulativeOperatingTime");                    
                    r.put("eventFlags", eventFlags);
                    r.put("estTimestamp", estTimestamp);
                    r.put("cumulativeOperatingTime", cumulativeOperatingTime);
                } catch (JSONException e) {
                    System.err.println(e.getMessage());
                }

                sendResultOK(r);
            }
        });

        hrPcc.subscribeManufacturerAndSerialEvent(new AntPlusLegacyCommonPcc.IManufacturerAndSerialReceiver() {
            @Override
            public void onNewManufacturerAndSerial(final long estTimestamp, final EnumSet<EventFlag> eventFlags, final int manufacturerID,
                    final int serialNumber) {
                JSONObject r = new JSONObject();
                try {
                    r.put("event", "manufacturerAndSerial");                    
                    r.put("eventFlags", eventFlags);
                    r.put("estTimestamp", estTimestamp);
                    r.put("manufacturerID", manufacturerID);
                    r.put("serialNumber", serialNumber);
                } catch (JSONException e) {
                    System.err.println(e.getMessage());
                }

                sendResultOK(r);
            }
        });

        hrPcc.subscribeVersionAndModelEvent(new AntPlusLegacyCommonPcc.IVersionAndModelReceiver() {
            @Override
            public void onNewVersionAndModel(final long estTimestamp, final EnumSet<EventFlag> eventFlags, final int hardwareVersion,
                    final int softwareVersion, final int modelNumber) {
                JSONObject r = new JSONObject();
                try {
                    r.put("event", "versionAndModelEvent");                    
                    r.put("eventFlags", eventFlags);
                    r.put("estTimestamp", estTimestamp);
                    r.put("hardwareVersion", hardwareVersion);
                    r.put("softwareVersion", softwareVersion);
                    r.put("modelNumber", modelNumber);
                } catch (JSONException e) {
                    System.err.println(e.getMessage());
                }

                sendResultOK(r);
            }
        });

        hrPcc.subscribeCalculatedRrIntervalEvent(new AntPlusHeartRatePcc.ICalculatedRrIntervalReceiver() {
            @Override
            public void onNewCalculatedRrInterval(final long estTimestamp,
                    EnumSet<EventFlag> eventFlags, final BigDecimal rrInterval, final AntPlusHeartRatePcc.RrFlag flag) {
                JSONObject r = new JSONObject();
                try {
                    r.put("event", "calculatedRrIntervalEvent");                    
                    r.put("eventFlags", eventFlags);
                    r.put("estTimestamp", estTimestamp);
                    r.put("rrInterval", rrInterval);
                    r.put("flag", flag);
                } catch (JSONException e) {
                    System.err.println(e.getMessage());
                }

                sendResultOK(r);
            }
        });
    }

    //Receives state changes and shows it on the status display line
    protected AntPluginPcc.IDeviceStateChangeReceiver base_IDeviceStateChangeReceiver
            = new AntPluginPcc.IDeviceStateChangeReceiver() {
                @Override
                public void onDeviceStateChange(final DeviceState newDeviceState) {
                    JSONObject r = new JSONObject();
                    try {
                        r.put("event", "deviceStateChange");
                        r.put("name", hrPcc.getDeviceName());
                        r.put("state", newDeviceState);
                    } catch (JSONException e) {
                        System.err.println(e.getMessage());
                    }

                    sendResultOK(r);
                }
            };

    protected AntPluginPcc.IPluginAccessResultReceiver<AntPlusHeartRatePcc> base_IPluginAccessResultReceiver
            = new AntPluginPcc.IPluginAccessResultReceiver<AntPlusHeartRatePcc>() {
                //Handle the result, connecting to events on success or reporting failure to user.
                @Override
                public void onResultReceived(AntPlusHeartRatePcc result, RequestAccessResult resultCode,
                        DeviceState initialDeviceState) {
                    switch (resultCode) {
                        case SUCCESS:
                            hrPcc = result;
                            subscribeToHrEvents();
                            JSONObject r = new JSONObject();
                            try {
                                r.put("event", "deviceStateChange");
                                r.put("name", result.getDeviceName());
                                r.put("state", initialDeviceState);
                            } catch (JSONException e) {
                                System.err.println(e.getMessage());
                            }
                            sendResultOK(r);
                            break;
                        case CHANNEL_NOT_AVAILABLE:
                            sendResultError("Channel Not Available", resultCode);
                            break;
                        case ADAPTER_NOT_DETECTED:
                            sendResultError("ANT Adapter Not Available. Built-in ANT hardware or external adapter required.", resultCode);
                            break;
                        case BAD_PARAMS:
                            sendResultError("Bad request parameters.", resultCode);
                            break;
                        case OTHER_FAILURE:
                            sendResultError("RequestAccess failed. See logcat for details.", resultCode);
                            break;
                        case DEPENDENCY_NOT_INSTALLED:
                            sendResultError("Dependency not installed.", resultCode);
                            break;
                        case USER_CANCELLED:
                            sendResultError("User Cancelled.", resultCode);
                            break;
                        case UNRECOGNIZED:
                            sendResultError("Failed: UNRECOGNIZED. PluginLib Upgrade Required?", resultCode);
                            break;
                        default:
                            sendResultError("Unrecognized result: ", resultCode);
                            break;
                    }
                }
            };

}
