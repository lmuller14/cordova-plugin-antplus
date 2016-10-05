package cz.zcu.kiv.neuroinformatics.antplus;

import android.content.Context;
import org.apache.cordova.*;

import java.math.BigDecimal;
import java.util.EnumSet;
import org.json.JSONException;
import org.json.JSONObject;

import com.dsi.ant.plugins.antplus.pcc.AntPlusBikeCadencePcc;
import com.dsi.ant.plugins.antplus.pcc.AntPlusBikeCadencePcc.ICalculatedCadenceReceiver;
import com.dsi.ant.plugins.antplus.pcc.AntPlusBikeSpeedDistancePcc;
import com.dsi.ant.plugins.antplus.pcc.AntPlusBikeSpeedDistancePcc.CalculatedAccumulatedDistanceReceiver;
import com.dsi.ant.plugins.antplus.pcc.AntPlusBikeSpeedDistancePcc.CalculatedSpeedReceiver;
import com.dsi.ant.plugins.antplus.pcc.AntPlusBikeSpeedDistancePcc.IMotionAndSpeedDataReceiver;
import com.dsi.ant.plugins.antplus.pcc.AntPlusBikeSpeedDistancePcc.IRawSpeedAndDistanceDataReceiver;
import com.dsi.ant.plugins.antplus.pcc.defines.BatteryStatus;
import com.dsi.ant.plugins.antplus.pcc.defines.DeviceState;
import com.dsi.ant.plugins.antplus.pcc.defines.DeviceType;
import com.dsi.ant.plugins.antplus.pcc.defines.EventFlag;
import com.dsi.ant.plugins.antplus.pcc.defines.RequestAccessResult;
import com.dsi.ant.plugins.antplus.pccbase.AntPluginPcc.IDeviceStateChangeReceiver;
import com.dsi.ant.plugins.antplus.pccbase.AntPluginPcc.IPluginAccessResultReceiver;
import com.dsi.ant.plugins.antplus.pccbase.AntPlusBikeSpdCadCommonPcc.IBatteryStatusReceiver;
import com.dsi.ant.plugins.antplus.pccbase.AntPlusLegacyCommonPcc.ICumulativeOperatingTimeReceiver;
import com.dsi.ant.plugins.antplus.pccbase.AntPlusLegacyCommonPcc.IManufacturerAndSerialReceiver;
import com.dsi.ant.plugins.antplus.pccbase.AntPlusLegacyCommonPcc.IVersionAndModelReceiver;

import com.dsi.ant.plugins.antplus.pccbase.PccReleaseHandle;

public class AntplusBikeSpeedDistanceService {

    private CallbackContext callbackContext;
    private Context context;

    private AntPlusBikeSpeedDistancePcc bsdPcc = null;
    private PccReleaseHandle<AntPlusBikeSpeedDistancePcc> bsdReleaseHandle = null;

    private AntPlusBikeCadencePcc bcPcc = null;
    private PccReleaseHandle<AntPlusBikeCadencePcc> bcReleaseHandle = null;

    private BigDecimal circumference = new BigDecimal(2070);

    public AntplusBikeSpeedDistanceService(Context context) {
        this.context = context;
    }

    //
    private void destroy() {
        if (bsdReleaseHandle != null) {
            bsdReleaseHandle.close();
        }

        if (bcReleaseHandle != null) {
            bcReleaseHandle.close();
        }
    }

    //
    private void sendResultOK(JSONObject r) {
        try {
            r.put("antDeviceNumber", bsdPcc.getAntDeviceNumber());
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
            r.put("antDeviceNumber", bsdPcc.getAntDeviceNumber());
        } catch (JSONException e) {
            System.err.println(e.getMessage());
        }
        PluginResult result = new PluginResult(PluginResult.Status.ERROR, r);
        result.setKeepCallback(true);
        callbackContext.sendPluginResult(result);
    }

    //
    public void subscribe(int antDeviceNumber, BigDecimal circumference, String deviceType, CallbackContext callbackContext) {
        this.callbackContext = callbackContext;
        this.circumference = circumference;

        boolean isBSC = DeviceType.valueOf(deviceType).equals(DeviceType.BIKE_SPDCAD);

        bsdReleaseHandle = AntPlusBikeSpeedDistancePcc.requestAccess(context, antDeviceNumber, 0, isBSC, mResultReceiver, mDeviceStateChangeReceiver);
    }

    //
    public void unsubscribe(CallbackContext callbackContext) {
        this.callbackContext = callbackContext;
        destroy();
    }

    public void subscribeBikeEvents() {

        bsdPcc.subscribeCalculatedSpeedEvent(new CalculatedSpeedReceiver(this.circumference) {
            @Override
            public void onNewCalculatedSpeed(final long estTimestamp,
                    final EnumSet<EventFlag> eventFlags, final BigDecimal calculatedSpeed) {

                JSONObject r = new JSONObject();
                try {
                    r.put("event", "calculatedSpeedData");
                    r.put("eventFlags", eventFlags);
                    r.put("estTimestamp", estTimestamp);
                    r.put("calculatedSpeed", calculatedSpeed);
                } catch (JSONException e) {
                    System.err.println(e.getMessage());
                }

                sendResultOK(r);
            }
        });

        bsdPcc.subscribeCalculatedAccumulatedDistanceEvent(new CalculatedAccumulatedDistanceReceiver(this.circumference) {
            @Override
            public void onNewCalculatedAccumulatedDistance(final long estTimestamp,
                    final EnumSet<EventFlag> eventFlags,
                    final BigDecimal calculatedAccumulatedDistance) {

                JSONObject r = new JSONObject();
                try {
                    r.put("event", "calculatedAccumulatedDistanceData");
                    r.put("eventFlags", eventFlags);
                    r.put("estTimestamp", estTimestamp);
                    r.put("calculatedAccumulatedDistance", calculatedAccumulatedDistance);
                } catch (JSONException e) {
                    System.err.println(e.getMessage());
                }

                sendResultOK(r);
            }
        });

        bsdPcc.subscribeRawSpeedAndDistanceDataEvent(new IRawSpeedAndDistanceDataReceiver() {
            @Override
            public void onNewRawSpeedAndDistanceData(final long estTimestamp,
                    final EnumSet<EventFlag> eventFlags,
                    final BigDecimal timestampOfLastEvent, final long cumulativeRevolutions) {
                JSONObject r = new JSONObject();
                try {
                    r.put("event", "cumulativeRevolutionsData");
                    r.put("eventFlags", eventFlags);
                    r.put("estTimestamp", estTimestamp);
                    r.put("timestampOfLastEvent", timestampOfLastEvent);
                    r.put("cumulativeRevolutions", cumulativeRevolutions);
                } catch (JSONException e) {
                    System.err.println(e.getMessage());
                }

                sendResultOK(r);
            }
        });

        if (bsdPcc.isSpeedAndCadenceCombinedSensor()) {

            bcReleaseHandle = AntPlusBikeCadencePcc.requestAccess(context, bsdPcc.getAntDeviceNumber(), 0, true,
                    new IPluginAccessResultReceiver<AntPlusBikeCadencePcc>() {
                        // Handle the result, connecting to events
                        // on success or reporting failure to user.
                        @Override
                        public void onResultReceived(AntPlusBikeCadencePcc result,
                                RequestAccessResult resultCode,
                                DeviceState initialDeviceStateCode) {
                            switch (resultCode) {
                                case SUCCESS:
                                    bcPcc = result;
                                    bcPcc.subscribeCalculatedCadenceEvent(new ICalculatedCadenceReceiver() {
                                        @Override
                                        public void onNewCalculatedCadence(long estTimestamp, EnumSet<EventFlag> eventFlags,
                                                final BigDecimal calculatedCadence) {
                                            JSONObject r = new JSONObject();
                                            try {
                                                r.put("event", "calculatedCadenceData");
                                                r.put("eventFlags", eventFlags);
                                                r.put("estTimestamp", estTimestamp);
                                                r.put("isCombinedSensor", true);
                                                r.put("calculatedCadence", calculatedCadence);
                                            } catch (JSONException e) {
                                                System.err.println(e.getMessage());
                                            }

                                            sendResultOK(r);
                                        }
                                    });
                                    break;
                                case CHANNEL_NOT_AVAILABLE:
                                    sendResultError("Channel Not Available", resultCode);
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
                                default:
                                    sendResultError("Unrecognized result: ", resultCode);
                                    break;
                            }
                        }
                    },
                    new IDeviceStateChangeReceiver() {
                        @Override
                        public void onDeviceStateChange(final DeviceState newDeviceState) {
                            JSONObject r = new JSONObject();
                            try {
                                r.put("event", "deviceStateChange");
                                r.put("name", bcPcc.getDeviceName());
                                r.put("state", newDeviceState);
                            } catch (JSONException e) {
                                System.err.println(e.getMessage());
                            }

                            sendResultOK(r);
                        }
                    });
        } else {

            bsdPcc.subscribeCumulativeOperatingTimeEvent(new ICumulativeOperatingTimeReceiver() {
                @Override
                public void onNewCumulativeOperatingTime(final long estTimestamp,
                        final EnumSet<EventFlag> eventFlags, final long cumulativeOperatingTime) {
                    JSONObject r = new JSONObject();
                    try {
                        r.put("event", "cumulativeOperatingTimeData");
                        r.put("eventFlags", eventFlags);
                        r.put("estTimestamp", estTimestamp);
                        r.put("cumulativeOperatingTime", cumulativeOperatingTime);
                    } catch (JSONException e) {
                        System.err.println(e.getMessage());
                    }

                    sendResultOK(r);
                }
            });

            bsdPcc.subscribeManufacturerAndSerialEvent(new IManufacturerAndSerialReceiver() {
                @Override
                public void onNewManufacturerAndSerial(final long estTimestamp,
                        final EnumSet<EventFlag> eventFlags, final int manufacturerID,
                        final int serialNumber) {
                    JSONObject r = new JSONObject();
                    try {
                        r.put("event", "manufacturerAndSerialData");
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

            bsdPcc.subscribeVersionAndModelEvent(new IVersionAndModelReceiver() {
                @Override
                public void onNewVersionAndModel(final long estTimestamp,
                        final EnumSet<EventFlag> eventFlags, final int hardwareVersion,
                        final int softwareVersion, final int modelNumber) {
                    JSONObject r = new JSONObject();
                    try {
                        r.put("event", "versionAndModelData");
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

            bsdPcc.subscribeBatteryStatusEvent(new IBatteryStatusReceiver() {
                @Override
                public void onNewBatteryStatus(final long estTimestamp, EnumSet<EventFlag> eventFlags,
                        final BigDecimal batteryVoltage, final BatteryStatus batteryStatus) {
                    JSONObject r = new JSONObject();
                    try {
                        r.put("event", "batteryStatusData");
                        r.put("eventFlags", eventFlags);
                        r.put("estTimestamp", estTimestamp);
                        r.put("batteryVoltage", batteryVoltage);
                        r.put("batteryStatus", batteryStatus.toString());

                    } catch (JSONException e) {
                        System.err.println(e.getMessage());
                    }

                    sendResultOK(r);
                }
            });

            bsdPcc.subscribeMotionAndSpeedDataEvent(new IMotionAndSpeedDataReceiver() {
                @Override
                public void onNewMotionAndSpeedData(final long estTimestamp, EnumSet<EventFlag> eventFlags,
                        final boolean isStopped) {
                    JSONObject r = new JSONObject();
                    try {
                        r.put("event", "motionAndSpeedData");
                        r.put("eventFlags", eventFlags);
                        r.put("estTimestamp", estTimestamp);
                        r.put("isStopped", isStopped);

                    } catch (JSONException e) {
                        System.err.println(e.getMessage());
                    }

                    sendResultOK(r);
                }
            });
        }
    }

    //Receives state changes and shows it on the status display line
    IDeviceStateChangeReceiver mDeviceStateChangeReceiver = new IDeviceStateChangeReceiver() {
        @Override
        public void onDeviceStateChange(final DeviceState newDeviceState) {
            JSONObject r = new JSONObject();
            try {
                r.put("event", "deviceStateChange");
                r.put("name", bsdPcc.getDeviceName());
                r.put("state", newDeviceState);
            } catch (JSONException e) {
                System.err.println(e.getMessage());
            }

            sendResultOK(r);
        }
    };

    IPluginAccessResultReceiver<AntPlusBikeSpeedDistancePcc> mResultReceiver = new IPluginAccessResultReceiver<AntPlusBikeSpeedDistancePcc>() {
        @Override
        public void onResultReceived(AntPlusBikeSpeedDistancePcc result,
                RequestAccessResult resultCode, DeviceState initialDeviceState) {
            switch (resultCode) {
                case SUCCESS:
                    bsdPcc = result;
                    subscribeBikeEvents();
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
