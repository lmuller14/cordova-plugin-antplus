package cz.zcu.kiv.neuroinformatics.antplus;

import android.content.Context;
import org.apache.cordova.*;

import java.math.BigDecimal;
import java.util.EnumSet;
import org.json.JSONException;
import org.json.JSONObject;

import com.dsi.ant.plugins.antplus.common.AntFsCommon.IAntFsProgressUpdateReceiver;
import com.dsi.ant.plugins.antplus.pcc.AntPlusBloodPressurePcc;
import com.dsi.ant.plugins.antplus.pcc.AntPlusBloodPressurePcc.BloodPressureMeasurement;
import com.dsi.ant.plugins.antplus.pcc.AntPlusBloodPressurePcc.DownloadMeasurementsStatusCode;
import com.dsi.ant.plugins.antplus.pcc.AntPlusBloodPressurePcc.IDownloadMeasurementsStatusReceiver;
import com.dsi.ant.plugins.antplus.pcc.AntPlusBloodPressurePcc.IMeasurementDownloadedReceiver;
import com.dsi.ant.plugins.antplus.pcc.AntPlusBloodPressurePcc.IResetDataAndSetTimeFinishedReceiver;
import com.dsi.ant.plugins.antplus.pcc.AntPlusEnvironmentPcc;
import com.dsi.ant.plugins.antplus.pcc.defines.AntFsRequestStatus;
import com.dsi.ant.plugins.antplus.pcc.defines.AntFsState;
import com.dsi.ant.plugins.antplus.pcc.defines.DeviceState;
import com.dsi.ant.plugins.antplus.pcc.defines.RequestAccessResult;
import com.dsi.ant.plugins.antplus.pccbase.AntPluginPcc.IDeviceStateChangeReceiver;
import com.dsi.ant.plugins.antplus.pccbase.AntPluginPcc.IPluginAccessResultReceiver;
import com.dsi.ant.plugins.antplus.pccbase.MultiDeviceSearch.MultiDeviceSearchResult;
import com.dsi.ant.plugins.antplus.pccbase.PccReleaseHandle;

import com.garmin.fit.BloodPressureMesg;
import com.garmin.fit.DateTime;
import com.garmin.fit.Fit;
import com.garmin.fit.HrType;

public class AntplusBloodPressureService {

    private CallbackContext callbackContext;
    private Context context;

    private AntPlusBloodPressurePcc bpPcc = null;
    private PccReleaseHandle<AntPlusBloodPressurePcc> releaseHandle = null;

    public AntplusBloodPressureService(Context context) {
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
            r.put("antDeviceNumber", bpPcc.getAntDeviceNumber());
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
            r.put("antDeviceNumber", bpPcc.getAntDeviceNumber());
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
            r.put("antDeviceNumber", bpPcc.getAntDeviceNumber());
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
        releaseHandle = AntPlusBloodPressurePcc.requestAccess(context, antDeviceNumber, 0, base_IPluginAccessResultReceiver, base_IDeviceStateChangeReceiver);
    }

    //
    public void unsubscribe(CallbackContext callbackContext) {
        this.callbackContext = callbackContext;
        destroy();
    }
    
    //
    public void stopDataMonitor() {
        if (bpPcc != null) {
            bpPcc.cancelDownloadMeasurementsMonitor();

            JSONObject r = new JSONObject();
            try {
                r.put("event", "stopDataMonitor");
                r.put("result", "OK");
            } catch (JSONException e) {
                System.err.println(e.getMessage());
            }

            sendResultOK(r);
        } else {
            sendResultError("bpPcc is null", 500);
        }
    }
    
    //
    public void getAntFsMfgID() {
        if (bpPcc != null) {
            bpPcc.cancelDownloadMeasurementsMonitor();

            JSONObject r = new JSONObject();
            try {
                r.put("event", "getAntFsMfgID");
                r.put("antFsMfgID", bpPcc.getAntFsManufacturerID());
            } catch (JSONException e) {
                System.err.println(e.getMessage());
            }

            sendResultOK(r);
        } else {
            sendResultError("bpPcc is null", 500);
        }
    }
    
    //
    public void requestDownloadMeasurements(boolean onlyNew, boolean monitor) {
        boolean submitted = bpPcc.requestDownloadMeasurements(onlyNew, monitor,
                new IDownloadMeasurementsStatusReceiver() {
                    @Override
                    public void onDownloadMeasurementsStatus(final DownloadMeasurementsStatusCode statusCode,
                            final AntFsRequestStatus finishedCode) {
                        handleDownloadMeasurementsState(statusCode, finishedCode);
                    }

                    private void handleDownloadMeasurementsState(DownloadMeasurementsStatusCode statusCode, AntFsRequestStatus finishedCode) {
                        JSONObject r = null;
                        switch (statusCode) {
                            case FINISHED:
                                r = new JSONObject();
                                try {
                                    r.put("event", "bPdownloadMeasurementsState");
                                    r.put("desc", "Download finished, handling the results.");
                                    r.put("statusCode", statusCode.getIntValue());
                                } catch (JSONException e) {
                                    System.err.println(e.getMessage());
                                }
                                sendResultOK(r);

                                handleFinishedCode(finishedCode);
                                break;
                            case PROGRESS_MONITORING:
                                r = new JSONObject();
                                try {
                                    r.put("event", "bPdownloadMeasurementsState");
                                    r.put("desc", "Monitoring for new downloads.");
                                    r.put("statusCode", statusCode.getIntValue());
                                } catch (JSONException e) {
                                    System.err.println(e.getMessage());
                                }
                                sendResultOK(r);

                                break;
                            case PROGRESS_SYNCING_WITH_DEVICE:
                                r = new JSONObject();
                                try {
                                    r.put("event", "bPdownloadMeasurementsState");
                                    r.put("desc", "Synchronizing with device.");
                                    r.put("statusCode", statusCode.getIntValue());
                                } catch (JSONException e) {
                                    System.err.println(e.getMessage());
                                }
                                sendResultOK(r);
                                break;
                            case UNRECOGNIZED:
                                sendResultError("Failed: UNRECOGNIZED. PluginLib Upgrade Required?", statusCode.getIntValue());
                                break;
                            default:
                                break;
                        }
                    }

                    private void handleFinishedCode(AntFsRequestStatus finishedCode) {
                        JSONObject r = new JSONObject();
                        try {
                            r.put("event", "newAntFsProgressUpdate");
                            r.put("max", 100);
                            r.put("progress", 100);
                            r.put("messsage", "Finished.");
                        } catch (JSONException e) {
                            System.err.println(e.getMessage());
                        }
                        sendResultOK(r);

                        switch (finishedCode) {
                            case SUCCESS:
                                r = new JSONObject();
                                try {
                                    r.put("event", "bPhandleFinishedCode");
                                    r.put("progress", "DownloadAllHistory finished successfully.");
                                    r.put("statusCode", finishedCode.getIntValue());
                                } catch (JSONException e) {
                                    System.err.println(e.getMessage());
                                }
                                sendResultOK(r);
                                break;
                            case FAIL_ALREADY_BUSY_EXTERNAL:
                                sendResultError("DownloadAllHistory failed, device busy.", finishedCode.getIntValue());
                                break;
                            case FAIL_DEVICE_COMMUNICATION_FAILURE:
                                sendResultError("DownloadAllHistory failed, communication error.", finishedCode.getIntValue());
                                break;
                            case FAIL_AUTHENTICATION_REJECTED:
                                //NOTE: This is thrown when authentication has failed, most likely when user action is required to enable pairing
                                sendResultError("DownloadAllHistory failed, authentication rejected.", finishedCode.getIntValue());
                                break;
                            case FAIL_DEVICE_TRANSMISSION_LOST:
                                sendResultError("DownloadAllHistory failed, transmission lost.", finishedCode.getIntValue());
                                break;
                            case FAIL_PLUGINS_SERVICE_VERSION:
                                sendResultError("Failed: Plugin Service Upgrade Required?", finishedCode.getIntValue());
                                break;
                            case UNRECOGNIZED:
                                sendResultError("Failed: UNRECOGNIZED. PluginLib Upgrade Required?", finishedCode.getIntValue());
                                break;
                            default:
                                break;
                        }
                    }
                },
                new IMeasurementDownloadedReceiver() {
                    @Override
                    public void onMeasurementDownloaded(final BloodPressureMeasurement measurement) {

                        BloodPressureMesg fitBpmMesg = measurement.asBloodPressureFitMesg();
                        JSONObject r = new JSONObject();
                        try {
                            r.put("event", "bPMeasurementDownloaded");

                            //NOTE: All linked data messages must have the SAME timestamp
                            if ((fitBpmMesg.getTimestamp() != null) && (!fitBpmMesg.getTimestamp().getTimestamp().equals(DateTime.INVALID))) {
                                r.put("timeStamp", fitBpmMesg.getTimestamp().toString());
                            } else {
                                r.put("timeStamp", "N/A");
                            }

                            if ((fitBpmMesg.getUserProfileIndex() != null) && (!fitBpmMesg.getUserProfileIndex().equals(Fit.UINT16_INVALID))) {
                                r.put("userProfileIndex", fitBpmMesg.getUserProfileIndex().toString());
                            } else {
                                r.put("userProfileIndex", "N/A");
                            }

                            if ((fitBpmMesg.getSystolicPressure() != null) && (!fitBpmMesg.getSystolicPressure().equals(Fit.UINT16_INVALID))) {
                                r.put("systolicPressure", fitBpmMesg.getSystolicPressure().toString());
                            } else {
                                r.put("systolicPressure", "N/A");
                            }
                            r.put("systolicPressureUnit", "mmHg");

                            if ((fitBpmMesg.getDiastolicPressure() != null) && (!fitBpmMesg.getDiastolicPressure().equals(Fit.UINT16_INVALID))) {
                                r.put("diastolicPressure", fitBpmMesg.getDiastolicPressure().toString());
                            } else {
                                r.put("diastolicPressure", "N/A");
                            }
                            r.put("diastolicPressureUnit", "mmHg");

                            if ((fitBpmMesg.getMeanArterialPressure() != null) && (!fitBpmMesg.getMeanArterialPressure().equals(Fit.UINT16_INVALID))) {
                                r.put("meanArterialPressure", fitBpmMesg.getMeanArterialPressure().toString());
                            } else {
                                r.put("meanArterialPressure", "N/A");
                            }
                            r.put("meanArterialPressureUnit", "mmHg");

                            if ((fitBpmMesg.getHeartRate() != null) && (!fitBpmMesg.getHeartRate().equals(Fit.UINT8_INVALID))) {
                                r.put("heartRate", fitBpmMesg.getHeartRate().toString());
                            } else {
                                r.put("heartRate", "N/A");
                            }
                            r.put("heartRateUnit", "bpm");

                            if ((fitBpmMesg.getMap3SampleMean() != null) && (!fitBpmMesg.getMap3SampleMean().equals(Fit.UINT16_INVALID))) {
                                r.put("map3SampleMean", fitBpmMesg.getMap3SampleMean().toString());
                            } else {
                                r.put("map3SampleMean", "N/A");
                            }
                            r.put("map3SampleMeanUnit", "mmHg");

                            if ((fitBpmMesg.getMapMorningValues() != null) && (!fitBpmMesg.getMapMorningValues().equals(Fit.UINT16_INVALID))) {
                                r.put("mapMorningValues", fitBpmMesg.getMapMorningValues().toString());
                            } else {
                                r.put("mapMorningValues", "N/A");
                            }
                            r.put("mapMorningValuesUnit", "mmHg");

                            if ((fitBpmMesg.getMapEveningValues() != null) && (!fitBpmMesg.getMapEveningValues().equals(Fit.UINT16_INVALID))) {
                                r.put("mapEveningValues", fitBpmMesg.getMapEveningValues().toString());
                            } else {
                                r.put("mapEveningValues", "N/A");
                            }
                            r.put("mapEveningValuesUnit", "mmHg");

                            if ((fitBpmMesg.getHeartRateType() != null) && (!fitBpmMesg.getHeartRateType().equals(HrType.INVALID))) {
                                r.put("heartRateType", fitBpmMesg.getHeartRateType().toString());
                            } else {
                                r.put("heartRateType", "N/A");
                            }

                            if ((fitBpmMesg.getStatus() != null)) {
                                r.put("status", fitBpmMesg.getStatus().toString());
                            } else {
                                r.put("status", "N/A");
                            }

                        } catch (JSONException e) {
                            System.err.println(e.getMessage());
                        }
                        sendResultOK(r);

                    }
                },
                new IAntFsProgressUpdateReceiver() {
                    @Override
                    public void onNewAntFsProgressUpdate(final AntFsState stateCode,
                            final long transferredBytes, final long totalBytes) {
                        JSONObject r = null;
                        switch (stateCode) {
                            //In Link state and requesting to link with the device in order to pass to Auth state
                            case LINK_REQUESTING_LINK:
                                r = new JSONObject();
                                try {
                                    r.put("event", "newAntFsProgressUpdate");
                                    r.put("max", 4);
                                    r.put("progress", 1);
                                    r.put("message", "In Link State: Requesting Link.");
                                } catch (JSONException e) {
                                    System.err.println(e.getMessage());
                                }
                                sendResultOK(r);
                                break;

                            //In Authentication state, processing authentication commands
                            case AUTHENTICATION:
                                r = new JSONObject();
                                try {
                                    r.put("event", "newAntFsProgressUpdate");
                                    r.put("max", 4);
                                    r.put("progress", 2);
                                    r.put("message", "In Authentication State.");
                                } catch (JSONException e) {
                                    System.err.println(e.getMessage());
                                }
                                sendResultOK(r);
                                break;

                            //In Authentication state, currently attempting to pair with the device
                            //NOTE: Feedback SHOULD be given to the user here as pairing typically requires user interaction with the device
                            case AUTHENTICATION_REQUESTING_PAIRING:
                                r = new JSONObject();
                                try {
                                    r.put("event", "newAntFsProgressUpdate");
                                    r.put("max", 4);
                                    r.put("progress", 2);
                                    r.put("message", "In Authentication State: User Pairing Requested.");
                                } catch (JSONException e) {
                                    System.err.println(e.getMessage());
                                }
                                sendResultOK(r);
                                break;

                            //In Transport state, no requests are currently being processed
                            case TRANSPORT_IDLE:
                                r = new JSONObject();
                                try {
                                    r.put("event", "newAntFsProgressUpdate");
                                    r.put("max", 4);
                                    r.put("progress", 3);
                                    r.put("message", "Requesting download (In Transport State: Idle)...");
                                } catch (JSONException e) {
                                    System.err.println(e.getMessage());
                                }
                                sendResultOK(r);
                                break;

                            //In Transport state, files are currently being downloaded
                            case TRANSPORT_DOWNLOADING:
                                r = new JSONObject();
                                try {
                                    r.put("event", "newAntFsProgressUpdate");
                                    r.put("max", 100);
                                    r.put("message", "In Transport State: Downloading.");
                                } catch (JSONException e) {
                                    System.err.println(e.getMessage());
                                }

                                if (transferredBytes >= 0 && totalBytes > 0) {
                                    int progress = (int) (transferredBytes * 100 / totalBytes);
                                    try {
                                        r.put("progress", progress);
                                    } catch (JSONException e) {
                                        System.err.println(e.getMessage());
                                    }
                                    sendResultOK(r);
                                }
                                break;

                            case UNRECOGNIZED:
                                //This flag indicates that an unrecognized value was sent by the service, an upgrade of your PCC may be required to handle this new value.                                
                                sendResultError("Failed: UNRECOGNIZED. PluginLib or Plugin Service Upgrade Required?", stateCode.getIntValue());
                                break;
                            default:
                                System.err.println("AntplusBloodPressureService: Unknown ANT-FS State Code Received: " + stateCode);
                                break;
                        }

                    }
                });

        if (!submitted) {
            sendResultError("Error Downloading Measurements: PCC already busy or dead.", 500);
        }
    }
    
    //
    public void requestResetDataAndSetTime(boolean doSetTime) {
        boolean submitted = bpPcc.requestResetDataAndSetTime(doSetTime, new IResetDataAndSetTimeFinishedReceiver() {
            @Override
            public void onNewResetDataAndSetTimeFinished(final AntFsRequestStatus statusCode) {
                //Unrecognized or fail plugins service version indicates the progress dialog would never have started
                if (statusCode == AntFsRequestStatus.UNRECOGNIZED || statusCode == AntFsRequestStatus.FAIL_PLUGINS_SERVICE_VERSION) {
                    sendResultError("Reset Failed. Plugin Lib needs upgrade.", statusCode.getIntValue());
                    return;
                }

                if (statusCode == AntFsRequestStatus.SUCCESS) {
                    JSONObject r = new JSONObject();
                    try {
                        r.put("event", "requestResetDataAndSetTime");
                        r.put("result", "Reset complete.");
                    } catch (JSONException e) {
                        System.err.println(e.getMessage());
                    }
                    sendResultOK(r);
                } else {
                    sendResultError("Reset Failed.", statusCode.getIntValue());
                }

            }
        }, null);

        if (!submitted) {
            sendResultError("Error Resetting Device: PCC likely busy or dead.", 500);
        }
    }

    //Receives state changes and shows it on the status display line
    protected IDeviceStateChangeReceiver base_IDeviceStateChangeReceiver = new IDeviceStateChangeReceiver() {
        @Override
        public void onDeviceStateChange(final DeviceState newDeviceState) {
            JSONObject r = new JSONObject();
            try {
                r.put("event", "deviceStateChange");
                r.put("name", bpPcc.getDeviceName());
                r.put("state", newDeviceState);
            } catch (JSONException e) {
                System.err.println(e.getMessage());
            }

            sendResultOK(r);
        }
    };

    protected IPluginAccessResultReceiver<AntPlusBloodPressurePcc> base_IPluginAccessResultReceiver = new IPluginAccessResultReceiver<AntPlusBloodPressurePcc>() {
        //Handle the result, connecting to events on success or reporting failure to user.
        @Override
        public void onResultReceived(AntPlusBloodPressurePcc result, RequestAccessResult resultCode,
                DeviceState initialDeviceState) {
            switch (resultCode) {
                case SUCCESS:
                    bpPcc = result;
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
