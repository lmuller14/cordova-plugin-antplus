package cz.zcu.kiv.neuroinformatics.antplus;

import java.math.BigDecimal;
import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;

public class Antplus extends CordovaPlugin {

    private static final String SEARCH_DEVICES = "searchDevices";
    private static final String STOP_SEARCH_DEVICES = "stopSearchDevices";

    private static final String SUBSCRIBE_HR = "subscribeHR";
    private static final String UNSUBSCRIBE_HR = "unsubscribeHR";

    private static final String SUBSCRIBE_WGT = "subscribeWGT";
    private static final String UNSUBSCRIBE_WGT = "unsubscribeWGT";
    private static final String REQUEST_BASIC_WGT = "requestBasicWGT";
    private static final String REQUEST_ADVANCED_WGT = "requestAdvancedWGT";

    private static final String SUBSCRIBE_BP = "subscribeBP";
    private static final String UNSUBSCRIBE_BP = "unsubscribeBP";
    private static final String STOP_DATA_MONITOR_BP = "stopDataMonitorBP";
    private static final String GET_ANT_FS_MFG_ID_BP = "getAntFsMfgIDBP";
    private static final String REQUEST_DOWNLOAD_MEASUREMENTS_BP = "requestDownloadMeasurementsBP";
    private static final String REQUEST_RESET_DATA_AND_SET_TIME_BP = "requestResetDataAndSetTimeBP";

    private static final String SUBSCRIBE_SDM = "subscribeSDM";
    private static final String UNSUBSCRIBE_SDM = "unsubscribeSDM";

    private static final String SUBSCRIBE_BIKE = "subscribeBike";
    private static final String UNSUBSCRIBE_BIKE = "unsubscribeBike";

    private AntplusMultiDeviceSearch antplusMultiDeviceSearch = null;
    private AntplusHeartRateService antplusHeartRateService = null;
    private AntplusWeightScaleService antplusWeightScaleService = null;
    private AntplusBloodPressureService antplusBloodPressureService = null;
    private AntplusStrideSDMService antplusStrideSDMService = null;
    private AntplusBikeSpeedDistanceService antplusBikeSpeedDistanceService = null;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);

        if (antplusMultiDeviceSearch == null) {
            antplusMultiDeviceSearch = new AntplusMultiDeviceSearch(cordova.getActivity());
        }

        if (antplusHeartRateService == null) {
            antplusHeartRateService = new AntplusHeartRateService(cordova.getActivity().getApplicationContext());
        }

        if (antplusWeightScaleService == null) {
            antplusWeightScaleService = new AntplusWeightScaleService(cordova.getActivity().getApplicationContext());
        }

        if (antplusBloodPressureService == null) {
            antplusBloodPressureService = new AntplusBloodPressureService(cordova.getActivity().getApplicationContext());
        }

        if (antplusStrideSDMService == null) {
            antplusStrideSDMService = new AntplusStrideSDMService(cordova.getActivity().getApplicationContext());
        }
        
        if (antplusBikeSpeedDistanceService == null) {
            antplusBikeSpeedDistanceService = new AntplusBikeSpeedDistanceService(cordova.getActivity().getApplicationContext());
        }
    }

    @Override
    public boolean execute(String action, JSONArray data, final CallbackContext callbackContext) throws JSONException {

        if (action.equals(SEARCH_DEVICES)) {
            final String deviceType = data.getString(0);
            cordova.getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    antplusMultiDeviceSearch.startSearchDevices(callbackContext, deviceType);
                }
            });
            return true;
        } else if (action.equals(STOP_SEARCH_DEVICES)) {
            cordova.getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    antplusMultiDeviceSearch.stopSearchDevices(callbackContext);
                }
            });
            return true;
        } else if (action.equals(SUBSCRIBE_HR)) {
            final int antDeviceNumber = data.getInt(0);
            cordova.getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    antplusHeartRateService.subscribe(antDeviceNumber, callbackContext);
                }
            });
            return true;
        } else if (action.equals(UNSUBSCRIBE_HR)) {
            cordova.getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    antplusHeartRateService.unsubscribe(callbackContext);
                }
            });
            return true;
        } else if (action.equals(SUBSCRIBE_WGT)) {
            final int antDeviceNumber = data.getInt(0);
            cordova.getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    antplusWeightScaleService.subscribe(antDeviceNumber, callbackContext);
                }
            });
            return true;
        } else if (action.equals(UNSUBSCRIBE_WGT)) {
            cordova.getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    antplusWeightScaleService.unsubscribe(callbackContext);
                }
            });
            return true;
        } else if (action.equals(REQUEST_BASIC_WGT)) {
            cordova.getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    antplusWeightScaleService.requestBasicMeasurement();
                }
            });
            return true;
        } else if (action.equals(REQUEST_ADVANCED_WGT)) {
            final int age = data.getInt(0);
            final int height = data.getInt(1);
            final int gender = data.getInt(2);
            final boolean athlete = data.getBoolean(3);
            final int activityLevel = data.getInt(4);
            cordova.getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    antplusWeightScaleService.requestAdvancedMeasurement(age, height, gender, athlete, activityLevel);
                }
            });
            return true;
        } else if (action.equals(SUBSCRIBE_BP)) {
            final int antDeviceNumber = data.getInt(0);
            cordova.getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    antplusBloodPressureService.subscribe(antDeviceNumber, callbackContext);
                }
            });
            return true;
        } else if (action.equals(UNSUBSCRIBE_BP)) {
            cordova.getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    antplusBloodPressureService.unsubscribe(callbackContext);
                }
            });
            return true;
        } else if (action.equals(STOP_DATA_MONITOR_BP)) {
            cordova.getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    antplusBloodPressureService.stopDataMonitor();
                }
            });
            return true;
        } else if (action.equals(GET_ANT_FS_MFG_ID_BP)) {
            cordova.getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    antplusBloodPressureService.getAntFsMfgID();
                }
            });
            return true;
        } else if (action.equals(REQUEST_DOWNLOAD_MEASUREMENTS_BP)) {
            final boolean onlyNew = data.getBoolean(0);
            final boolean monitor = data.getBoolean(1);
            cordova.getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    antplusBloodPressureService.requestDownloadMeasurements(onlyNew, monitor);
                }
            });
            return true;
        } else if (action.equals(REQUEST_RESET_DATA_AND_SET_TIME_BP)) {
            final boolean doSetTime = data.getBoolean(0);
            cordova.getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    antplusBloodPressureService.requestResetDataAndSetTime(doSetTime);
                }
            });
            return true;
        } else if (action.equals(SUBSCRIBE_SDM)) {
            final int antDeviceNumber = data.getInt(0);
            cordova.getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    antplusStrideSDMService.subscribe(antDeviceNumber, callbackContext);
                }
            });
            return true;
        } else if (action.equals(UNSUBSCRIBE_SDM)) {
            cordova.getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    antplusStrideSDMService.unsubscribe(callbackContext);
                }
            });
            return true;
        } else if (action.equals(SUBSCRIBE_BIKE)) {
            final int antDeviceNumber = data.getInt(0);
            final BigDecimal circumference = new BigDecimal(data.getDouble(1));
            final String deviceType = data.getString(2);
            cordova.getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    antplusBikeSpeedDistanceService.subscribe(antDeviceNumber, circumference, deviceType, callbackContext);
                }
            });
            return true;
        } else if (action.equals(UNSUBSCRIBE_BIKE)) {
            cordova.getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    antplusBikeSpeedDistanceService.unsubscribe(callbackContext);
                }
            });
            return true;
        } else {
            return false;
        }
        return false;
    }
}
