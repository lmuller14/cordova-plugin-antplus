package cz.zcu.kiv.neuroinformatics.antplus;

import android.content.Context;
import org.apache.cordova.*;

import java.math.BigDecimal;
import java.util.EnumSet;
import org.json.JSONException;
import org.json.JSONObject;

import com.dsi.ant.plugins.antplus.pcc.AntPlusWeightScalePcc;
import com.dsi.ant.plugins.antplus.pcc.defines.AntFsRequestStatus;
import com.dsi.ant.plugins.antplus.pcc.defines.AntFsState;
import com.dsi.ant.plugins.antplus.pcc.defines.DeviceState;
import com.dsi.ant.plugins.antplus.pcc.defines.EventFlag;
import com.dsi.ant.plugins.antplus.pcc.defines.RequestAccessResult;
import com.dsi.ant.plugins.antplus.pccbase.AntPluginPcc;
import com.dsi.ant.plugins.antplus.pccbase.AntPlusCommonPcc;
import com.dsi.ant.plugins.antplus.pccbase.MultiDeviceSearch;
import com.dsi.ant.plugins.antplus.pccbase.PccReleaseHandle;

public class AntplusWeightScaleService {

    private CallbackContext callbackContext;
    private Context context;

    private AntPlusWeightScalePcc wgtPcc = null;
    private PccReleaseHandle<AntPlusWeightScalePcc> releaseHandle = null;
    AntPlusWeightScalePcc.UserProfile userProfile = new AntPlusWeightScalePcc.UserProfile();

    public AntplusWeightScaleService(Context context) {
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
            r.put("antDeviceNumber", wgtPcc.getAntDeviceNumber());
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
            r.put("antDeviceNumber", wgtPcc.getAntDeviceNumber());
        } catch (JSONException e) {
            System.err.println(e.getMessage());
        }
        PluginResult result = new PluginResult(PluginResult.Status.ERROR, r);
        result.setKeepCallback(true);
        callbackContext.sendPluginResult(result);
    }

    private void sendResultError(String error, int resultCode) {
        JSONObject r = new JSONObject();
        try {
            r.put("event", "error");
            r.put("message", error);
            r.put("code", resultCode);
            r.put("antDeviceNumber", wgtPcc.getAntDeviceNumber());
        } catch (JSONException e) {
            System.err.println(e.getMessage());
        }
        PluginResult result = new PluginResult(PluginResult.Status.ERROR, r);
        result.setKeepCallback(true);
        callbackContext.sendPluginResult(result);
    }

    private boolean checkRequestResult(AntPlusWeightScalePcc.WeightScaleRequestStatus status) {
        switch (status) {
            case SUCCESS:
                return true;
            case FAIL_ALREADY_BUSY_EXTERNAL:
                sendResultError("Fail: Busy", status.getIntValue());
                break;
            case FAIL_DEVICE_COMMUNICATION_FAILURE:
                sendResultError("Fail: Comm Err", status.getIntValue());
                break;
            case FAIL_DEVICE_TRANSMISSION_LOST:
                sendResultError("Fail: Trans Lost", status.getIntValue());
                break;
            case FAIL_PLUGINS_SERVICE_VERSION:
                sendResultError("Failed: Plugin Service Upgrade Required?", status.getIntValue());
                break;
            case UNRECOGNIZED:
                sendResultError("Failed: UNRECOGNIZED. PluginLib Upgrade Required?", status.getIntValue());
                break;
            default:
                sendResultError("Request failed with unrecognized result.", status.getIntValue());
                break;
        }
        return false;
    }

    //
    public void subscribe(int antDeviceNumber, CallbackContext callbackContext) {
        this.callbackContext = callbackContext;
        releaseHandle = AntPlusWeightScalePcc.requestAccess(context, antDeviceNumber, 0, base_IPluginAccessResultReceiver, base_IDeviceStateChangeReceiver);
    }

    //
    public void unsubscribe(CallbackContext callbackContext) {
        this.callbackContext = callbackContext;
        destroy();
    }

    public void subscribeToWgtEvents() {
        wgtPcc.subscribeManufacturerIdentificationEvent(new AntPlusCommonPcc.IManufacturerIdentificationReceiver() {

            @Override
            public void onNewManufacturerIdentification(final long estTimestamp,
                    final EnumSet<EventFlag> eventFlags, final int hardwareRevision,
                    final int manufacturerID, final int modelNumber) {

                JSONObject r = new JSONObject();
                try {
                    r.put("event", "manufacturerIdentificationData");
                    r.put("eventFlags", eventFlags);
                    r.put("estTimestamp", estTimestamp);
                    r.put("hardwareRevision", hardwareRevision);
                    r.put("manufacturerID", manufacturerID);
                    r.put("modelNumber", modelNumber);
                } catch (JSONException e) {
                    System.err.println(e.getMessage());
                }

                sendResultOK(r);
            }
        });

        wgtPcc.subscribeProductInformationEvent(new AntPlusCommonPcc.IProductInformationReceiver() {
            @Override
            public void onNewProductInformation(final long estTimestamp,
                    final EnumSet<EventFlag> eventFlags, final int mainSoftwareRevision,
                    final int supplementalSoftwareRevision, final long serialNumber) {

                JSONObject r = new JSONObject();
                try {
                    r.put("event", "productInformationData");
                    r.put("eventFlags", eventFlags);
                    r.put("estTimestamp", estTimestamp);
                    r.put("mainSoftwareRevision", mainSoftwareRevision);
                    if (supplementalSoftwareRevision == -2) {
                        // Plugin Service installed does not support supplemental revision
                        r.put("supplementalSoftwareRevision", "?");
                    } else if (supplementalSoftwareRevision == 0xFF) {
                        // Invalid supplemental revision
                        r.put("supplementalSoftwareRevision", "");
                    } else {
                        // Valid supplemental revision
                        r.put("supplementalSoftwareRevision", String.valueOf(supplementalSoftwareRevision));
                    }
                    r.put("serialNumber", serialNumber);
                } catch (JSONException e) {
                    System.err.println(e.getMessage());
                }

                sendResultOK(r);
            }
        });

        wgtPcc.subscribeBodyWeightBroadcastEvent(new AntPlusWeightScalePcc.IBodyWeightBroadcastReceiver() {
            @Override
            public void onNewBodyWeightBroadcast(final long estTimestamp,
                    EnumSet<EventFlag> eventFlags,
                    final AntPlusWeightScalePcc.BodyWeightStatus bodyWeightStatus, final BigDecimal bodyWeight) {

                JSONObject r = new JSONObject();
                try {
                    r.put("event", "bodyWeightBroadcastData");
                    r.put("eventFlags", eventFlags);
                    r.put("estTimestamp", estTimestamp);
                    r.put("bodyWeightStatus", bodyWeightStatus.toString());
                    if (bodyWeightStatus == AntPlusWeightScalePcc.BodyWeightStatus.VALID) {
                        r.put("bodyWeight", bodyWeight.toString());
                    }
                } catch (JSONException e) {
                    System.err.println(e.getMessage());
                }

                sendResultOK(r);
            }
        });
    }

    public void requestBasicMeasurement() {
        wgtPcc.requestBasicMeasurement(new AntPlusWeightScalePcc.IBasicMeasurementFinishedReceiver() {

            @Override
            public void onBasicMeasurementFinished(long estTimestamp,
                    EnumSet<EventFlag> eventFlags, final AntPlusWeightScalePcc.WeightScaleRequestStatus status, final BigDecimal bodyWeight) {

                if (checkRequestResult(status)) {
                    JSONObject r = new JSONObject();
                    try {
                        r.put("event", "basicMeasurementData");
                        r.put("eventFlags", eventFlags);
                        r.put("estTimestamp", estTimestamp);
                        r.put("status", status.toString());
                        r.put("bodyWeight", bodyWeight.toString()); //kg
                    } catch (JSONException e) {
                        System.err.println(e.getMessage());
                    }

                    sendResultOK(r);
                }
            }
        });
    }

    public void requestAdvancedMeasurement(int age, int height, int gender, boolean athlete, int activityLevel) {
        userProfile.age = age;
        userProfile.height = height;
        if (gender == 1) {
            userProfile.gender = AntPlusWeightScalePcc.Gender.MALE;
        } else {
            userProfile.gender = AntPlusWeightScalePcc.Gender.FEMALE;
        }
        userProfile.lifetimeAthlete = athlete;
        userProfile.activityLevel = activityLevel;

        wgtPcc.requestAdvancedMeasurement(new AntPlusWeightScalePcc.IAdvancedMeasurementFinishedReceiver() {
            @Override
            public void onAdvancedMeasurementFinished(long estTimestamp,
                    EnumSet<EventFlag> eventFlags, final AntPlusWeightScalePcc.WeightScaleRequestStatus status,
                    final AntPlusWeightScalePcc.AdvancedMeasurement measurement) {

                if (checkRequestResult(status)) {
                    JSONObject r = new JSONObject();
                    try {
                        r.put("event", "advancedMeasurementData");
                        r.put("eventFlags", eventFlags);
                        r.put("estTimestamp", estTimestamp);
                        r.put("status", status.toString());
                        r.put("bodyWeight", measurement.bodyWeight.floatValue()); //kg

                        r.put("hydrationPercentage", measurement.hydrationPercentage.floatValue()); //%
                        r.put("bodyFatPercentage", measurement.bodyFatPercentage.floatValue()); //%
                        r.put("muscleMass", measurement.muscleMass.floatValue()); //kg
                        r.put("boneMass", measurement.boneMass.floatValue()); //kg
                        r.put("activeMetabolicRate", measurement.activeMetabolicRate.floatValue()); //kcal
                        r.put("basalMetabolicRate", measurement.basalMetabolicRate.floatValue()); //kcal
                    } catch (JSONException e) {
                        System.err.println(e.getMessage());
                    }

                    sendResultOK(r);
                }
            }
        }, userProfile);
    }

    //Receives state changes and shows it on the status display line
    protected AntPluginPcc.IDeviceStateChangeReceiver base_IDeviceStateChangeReceiver
            = new AntPluginPcc.IDeviceStateChangeReceiver() {
                @Override
                public void onDeviceStateChange(final DeviceState newDeviceState) {
                    JSONObject r = new JSONObject();
                    try {
                        r.put("event", "deviceStateChange");
                        r.put("name", wgtPcc.getDeviceName());
                        r.put("state", newDeviceState);
                    } catch (JSONException e) {
                        System.err.println(e.getMessage());
                    }

                    sendResultOK(r);
                }
            };

    protected AntPluginPcc.IPluginAccessResultReceiver<AntPlusWeightScalePcc> base_IPluginAccessResultReceiver
            = new AntPluginPcc.IPluginAccessResultReceiver<AntPlusWeightScalePcc>() {
                //Handle the result, connecting to events on success or reporting failure to user.
                @Override
                public void onResultReceived(AntPlusWeightScalePcc result, RequestAccessResult resultCode,
                        DeviceState initialDeviceState) {
                    switch (resultCode) {
                        case SUCCESS:
                            wgtPcc = result;
                            subscribeToWgtEvents();
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
