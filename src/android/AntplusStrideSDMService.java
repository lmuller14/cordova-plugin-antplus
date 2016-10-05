package cz.zcu.kiv.neuroinformatics.antplus;

import android.content.Context;
import org.apache.cordova.*;

import java.math.BigDecimal;
import java.util.EnumSet;
import org.json.JSONException;
import org.json.JSONObject;

import com.dsi.ant.plugins.antplus.pcc.AntPlusStrideSdmPcc;
import com.dsi.ant.plugins.antplus.pcc.AntPlusStrideSdmPcc.ICalorieDataReceiver;
import com.dsi.ant.plugins.antplus.pcc.AntPlusStrideSdmPcc.IComputationTimestampReceiver;
import com.dsi.ant.plugins.antplus.pcc.AntPlusStrideSdmPcc.IDataLatencyReceiver;
import com.dsi.ant.plugins.antplus.pcc.AntPlusStrideSdmPcc.IDistanceReceiver;
import com.dsi.ant.plugins.antplus.pcc.AntPlusStrideSdmPcc.IInstantaneousCadenceReceiver;
import com.dsi.ant.plugins.antplus.pcc.AntPlusStrideSdmPcc.IInstantaneousSpeedReceiver;
import com.dsi.ant.plugins.antplus.pcc.AntPlusStrideSdmPcc.ISensorStatusReceiver;
import com.dsi.ant.plugins.antplus.pcc.AntPlusStrideSdmPcc.IStrideCountReceiver;
import com.dsi.ant.plugins.antplus.pcc.AntPlusStrideSdmPcc.SensorHealth;
import com.dsi.ant.plugins.antplus.pcc.AntPlusStrideSdmPcc.SensorLocation;
import com.dsi.ant.plugins.antplus.pcc.AntPlusStrideSdmPcc.SensorUseState;
import com.dsi.ant.plugins.antplus.pcc.defines.BatteryStatus;
import com.dsi.ant.plugins.antplus.pcc.defines.DeviceState;
import com.dsi.ant.plugins.antplus.pcc.defines.EventFlag;
import com.dsi.ant.plugins.antplus.pcc.defines.RequestAccessResult;
import com.dsi.ant.plugins.antplus.pccbase.AntPluginPcc;
import com.dsi.ant.plugins.antplus.pccbase.AntPlusCommonPcc.IManufacturerIdentificationReceiver;
import com.dsi.ant.plugins.antplus.pccbase.AntPlusCommonPcc.IManufacturerSpecificDataReceiver;
import com.dsi.ant.plugins.antplus.pccbase.AntPlusCommonPcc.IProductInformationReceiver;

import com.dsi.ant.plugins.antplus.pccbase.PccReleaseHandle;

public class AntplusStrideSDMService {

    private CallbackContext callbackContext;
    private Context context;

    private AntPlusStrideSdmPcc sdmPcc = null;
    private PccReleaseHandle<AntPlusStrideSdmPcc> releaseHandle = null;

    public AntplusStrideSDMService(Context context) {
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
            r.put("antDeviceNumber", sdmPcc.getAntDeviceNumber());
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
            r.put("antDeviceNumber", sdmPcc.getAntDeviceNumber());
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
        releaseHandle = AntPlusStrideSdmPcc.requestAccess(context, antDeviceNumber, 0, mResultReceiver, mDeviceStateChangeReceiver);
    }

    //
    public void unsubscribe(CallbackContext callbackContext) {
        this.callbackContext = callbackContext;
        destroy();
    }

    public void subscribeToStrideEvents() {

        sdmPcc.subscribeInstantaneousSpeedEvent(new IInstantaneousSpeedReceiver() {
            @Override
            public void onNewInstantaneousSpeed(final long estTimestamp, final EnumSet<EventFlag> eventFlags,
                    final BigDecimal instantaneousSpeed) {

                JSONObject r = new JSONObject();
                try {
                    r.put("event", "instantaneousSpeedData");
                    r.put("eventFlags", eventFlags);
                    r.put("estTimestamp", estTimestamp);
                    r.put("instantaneousSpeed", instantaneousSpeed);
                } catch (JSONException e) {
                    System.err.println(e.getMessage());
                }

                sendResultOK(r);
            }
        });

        sdmPcc.subscribeInstantaneousCadenceEvent(new IInstantaneousCadenceReceiver() {
            @Override
            public void onNewInstantaneousCadence(
                    final long estTimestamp, final EnumSet<EventFlag> eventFlags,
                    final BigDecimal instantaneousCadence) {

                JSONObject r = new JSONObject();
                try {
                    r.put("event", "instantaneousCadenceData");
                    r.put("eventFlags", eventFlags);
                    r.put("estTimestamp", estTimestamp);
                    r.put("instantaneousCadence", instantaneousCadence);
                } catch (JSONException e) {
                    System.err.println(e.getMessage());
                }

                sendResultOK(r);
            }
        });

        sdmPcc.subscribeDistanceEvent(new IDistanceReceiver() {
            @Override
            public void onNewDistance(final long estTimestamp, final EnumSet<EventFlag> eventFlags,
                    final BigDecimal cumulativeDistance) {
                JSONObject r = new JSONObject();
                try {
                    r.put("event", "cumulativeDistanceData");
                    r.put("eventFlags", eventFlags);
                    r.put("estTimestamp", estTimestamp);
                    r.put("cumulativeDistance", cumulativeDistance);
                } catch (JSONException e) {
                    System.err.println(e.getMessage());
                }

                sendResultOK(r);
            }
        });

        sdmPcc.subscribeStrideCountEvent(new IStrideCountReceiver() {
            @Override
            public void onNewStrideCount(final long estTimestamp, final EnumSet<EventFlag> eventFlags,
                    final long cumulativeStrides) {
                JSONObject r = new JSONObject();
                try {
                    r.put("event", "strideCountData");
                    r.put("eventFlags", eventFlags);
                    r.put("estTimestamp", estTimestamp);
                    r.put("cumulativeStrides", cumulativeStrides);
                } catch (JSONException e) {
                    System.err.println(e.getMessage());
                }

                sendResultOK(r);
            }
        });

        sdmPcc.subscribeComputationTimestampEvent(new IComputationTimestampReceiver() {
            @Override
            public void onNewComputationTimestamp(final long estTimestamp, final EnumSet<EventFlag> eventFlags,
                    final BigDecimal timestampOfLastComputation) {
                JSONObject r = new JSONObject();
                try {
                    r.put("event", "computationTimestampData");
                    r.put("eventFlags", eventFlags);
                    r.put("estTimestamp", estTimestamp);
                    r.put("timestampOfLastComputation", timestampOfLastComputation);
                } catch (JSONException e) {
                    System.err.println(e.getMessage());
                }

                sendResultOK(r);
            }
        });

        sdmPcc.subscribeDataLatencyEvent(new IDataLatencyReceiver() {
            @Override
            public void onNewDataLatency(final long estTimestamp, final EnumSet<EventFlag> eventFlags,
                    final BigDecimal updateLatency) {
                JSONObject r = new JSONObject();
                try {
                    r.put("event", "latencyData");
                    r.put("eventFlags", eventFlags);
                    r.put("estTimestamp", estTimestamp);
                    r.put("updateLatency", updateLatency);

                } catch (JSONException e) {
                    System.err.println(e.getMessage());
                }

                sendResultOK(r);
            }
        });

        sdmPcc.subscribeSensorStatusEvent(new ISensorStatusReceiver() {
            @Override
            public void onNewSensorStatus(final long estTimestamp, EnumSet<EventFlag> eventFlags,
                    final SensorLocation sensorLocation, final BatteryStatus batteryStatus,
                    final SensorHealth sensorHealth, final SensorUseState useState) {
                JSONObject r = new JSONObject();
                try {
                    r.put("event", "sensorStatusData");
                    r.put("eventFlags", eventFlags);
                    r.put("estTimestamp", estTimestamp);
                    r.put("sensorLocation", sensorLocation.toString());
                    r.put("batteryStatus", batteryStatus.toString());
                    r.put("sensorHealth", sensorHealth.toString());
                    r.put("useState", useState.toString());

                } catch (JSONException e) {
                    System.err.println(e.getMessage());
                }

                sendResultOK(r);
            }
        });

        sdmPcc.subscribeCalorieDataEvent(new ICalorieDataReceiver() {
            @Override
            public void onNewCalorieData(final long estTimestamp, final EnumSet<EventFlag> eventFlags,
                    final long cumulativeCalories) {
                JSONObject r = new JSONObject();
                try {
                    r.put("event", "calorieData");
                    r.put("eventFlags", eventFlags);
                    r.put("estTimestamp", estTimestamp);
                    r.put("cumulativeCalories", cumulativeCalories);

                } catch (JSONException e) {
                    System.err.println(e.getMessage());
                }

                sendResultOK(r);
            }
        });

        sdmPcc.subscribeManufacturerIdentificationEvent(new IManufacturerIdentificationReceiver() {
            @Override
            public void onNewManufacturerIdentification(final long estTimestamp, final EnumSet<EventFlag> eventFlags,
                    final int hardwareRevision, final int manufacturerID, final int modelNumber) {
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

        sdmPcc.subscribeProductInformationEvent(new IProductInformationReceiver() {
            @Override
            public void onNewProductInformation(final long estTimestamp, final EnumSet<EventFlag> eventFlags,
                    final int mainSoftwareRevision, final int supplementalSoftwareRevision, final long serialNumber) {
                JSONObject r = new JSONObject();
                try {
                    r.put("event", "productInformationData");
                    r.put("eventFlags", eventFlags);
                    r.put("estTimestamp", estTimestamp);
                    r.put("mainSoftwareRevision", mainSoftwareRevision);
                    if (supplementalSoftwareRevision == -2) {
                        r.put("supplementalSoftwareRevision", "NOT_SUPPORTED");
                    } else if (supplementalSoftwareRevision == 0xFF) {
                        r.put("supplementalSoftwareRevision", "INVALID");
                    } else {
                        r.put("supplementalSoftwareRevision", supplementalSoftwareRevision);
                    }
                    r.put("serialNumber", serialNumber);

                } catch (JSONException e) {
                    System.err.println(e.getMessage());
                }

                sendResultOK(r);
            }
        });

        sdmPcc.subscribeManufacturerSpecificDataEvent(new IManufacturerSpecificDataReceiver() {
            @Override
            public void onNewManufacturerSpecificData(final long estTimestamp,
                    final EnumSet<EventFlag> eventFlags, final byte[] rawDataBytes) {
                JSONObject r = new JSONObject();
                try {
                    r.put("event", "manufacturerSpecificData");
                    r.put("eventFlags", eventFlags);
                    r.put("estTimestamp", estTimestamp);
                    StringBuffer hexString = new StringBuffer();
                    for (int i = 0; i < rawDataBytes.length; i++) {
                        hexString
                                .append("[")
                                .append(String.format("%02X", rawDataBytes[i] & 0xFF))
                                .append("]");
                    }
                    r.put("hexString", hexString.toString());

                } catch (JSONException e) {
                    System.err.println(e.getMessage());
                }

                sendResultOK(r);
            }
        });

    }

    //Receives state changes and shows it on the status display line
    AntPluginPcc.IDeviceStateChangeReceiver mDeviceStateChangeReceiver = new AntPluginPcc.IDeviceStateChangeReceiver() {
        @Override
        public void onDeviceStateChange(final DeviceState newDeviceState) {
            JSONObject r = new JSONObject();
            try {
                r.put("event", "deviceStateChange");
                r.put("name", sdmPcc.getDeviceName());
                r.put("state", newDeviceState);
            } catch (JSONException e) {
                System.err.println(e.getMessage());
            }

            sendResultOK(r);
        }
    };

     AntPluginPcc.IPluginAccessResultReceiver<AntPlusStrideSdmPcc> mResultReceiver = new AntPluginPcc.IPluginAccessResultReceiver<AntPlusStrideSdmPcc>() {
        @Override
        public void onResultReceived(AntPlusStrideSdmPcc result,
                RequestAccessResult resultCode, DeviceState initialDeviceState) {
                    switch (resultCode) {
                        case SUCCESS:
                            sdmPcc = result;
                            subscribeToStrideEvents();
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
