package cz.zcu.kiv.neuroinformatics.antplus;

import android.app.Activity;
import org.apache.cordova.*;
import org.json.JSONException;

import com.dsi.ant.plugins.antplus.pcc.MultiDeviceSearch;
import com.dsi.ant.plugins.antplus.pcc.MultiDeviceSearch.RssiSupport;
import com.dsi.ant.plugins.antplus.pcc.defines.DeviceType;
import com.dsi.ant.plugins.antplus.pcc.defines.RequestAccessResult;
import com.dsi.ant.plugins.antplus.pccbase.MultiDeviceSearch.MultiDeviceSearchResult;

import java.util.EnumSet;
import org.json.JSONObject;

public class AntplusMultiDeviceSearch {

    private CallbackContext callbackContext;
    private Activity context;
    public MultiDeviceSearch mSearch;

    public AntplusMultiDeviceSearch(Activity context) {
        this.context = context;
    }

    private void winDeviceFound(final MultiDeviceSearchResult deviceFound) {
        JSONObject r = new JSONObject();
        try {
            r.put("resultID", deviceFound.resultID);
            r.put("describeContents", deviceFound.describeContents());
            r.put("antDeviceNumber", deviceFound.getAntDeviceNumber());
            r.put("antDeviceType", deviceFound.getAntDeviceType());
            r.put("deviceDisplayName", deviceFound.getDeviceDisplayName());
            r.put("isAlreadyConnected", deviceFound.isAlreadyConnected());
            r.put("isPreferredDevice", deviceFound.isPreferredDevice());
            r.put("isUserRecognizedDevice", deviceFound.isUserRecognizedDevice());
        } catch (JSONException e) {
            System.err.println(e.getMessage());
        }

        PluginResult result = new PluginResult(PluginResult.Status.OK, r);
        result.setKeepCallback(true);
        callbackContext.sendPluginResult(result);
    }

    private void sendResultError(String error, RequestAccessResult code) {
        JSONObject r = new JSONObject();
        try {
            r.put("event", "error");
            r.put("message", "MultiDeviceSearch: " + error);
            r.put("code", code);
        } catch (JSONException e) {
            System.err.println(e.getMessage());
        }
        PluginResult result = new PluginResult(PluginResult.Status.ERROR, r);
        result.setKeepCallback(true);
        callbackContext.sendPluginResult(result);
    }

    public void startSearchDevices(CallbackContext callbackContext, String deviceType) {
        this.callbackContext = callbackContext;
        
        @SuppressWarnings("unchecked")
        
        String []deviceTypes = deviceType.split(",");
        EnumSet<DeviceType> devices = EnumSet.of(DeviceType.valueOf(deviceTypes[0]));
        
        if(deviceTypes.length > 1) {
            for(int i = 1; i < deviceTypes.length; i++) {
                devices.add(DeviceType.valueOf(deviceTypes[i]));
            }
        }
        
        //EnumSet<DeviceType> devices = EnumSet.of(DeviceType.HEARTRATE);
        //EnumSet<DeviceType> devices = EnumSet.allOf(DeviceType.class);

        // start the multi-device search
        mSearch = new MultiDeviceSearch(context, devices, mCallback);
    }

    public void stopSearchDevices() {
        mSearch.close();
    }

    public void stopSearchDevices(CallbackContext callbackContext) {
        this.callbackContext = callbackContext;
        mSearch.close();
    }

    /**
     * Callbacks from the multi-device search interface
     */
    private com.dsi.ant.plugins.antplus.pcc.MultiDeviceSearch.SearchCallbacks mCallback = new com.dsi.ant.plugins.antplus.pcc.MultiDeviceSearch.SearchCallbacks() {

        /**
         * Called when a device is found.
         */
        public void onDeviceFound(final MultiDeviceSearchResult deviceFound) {
            AntplusMultiDeviceSearch.this.winDeviceFound(deviceFound);
            //AntplusMultiDeviceSearch.this.stopSearchDevices();
        }

        /**
         * The search has been stopped unexpectedly
         */
        public void onSearchStopped(RequestAccessResult reason) {
            switch (reason) {
                case SUCCESS:
                    sendResultError("Device ready to use.", reason);
                    break;
                case ADAPTER_NOT_DETECTED:
                    sendResultError("The Ant Radio Service has reported that there are not ANT adapters present to provide ANT channels.", reason);
                    break;
                case ALREADY_SUBSCRIBED:
                    sendResultError("This application is already actively granted access from a previous request.", reason);
                    break;
                case BAD_PARAMS:
                    sendResultError("Bad Parameters.", reason);
                    break;
                case OTHER_FAILURE:
                    sendResultError("Indicates there was a critical or unexpected error in the Plugins, check logcat output for details.", reason);
                    break;
                case DEPENDENCY_NOT_INSTALLED:
                    sendResultError("Dependency not installed.", reason);
                    break;
                case USER_CANCELLED:
                    sendResultError("The request was cancelled by user.", reason);
                    break;
                case DEVICE_ALREADY_IN_USE:
                    sendResultError("For plugin device types which do not allow sharing devices this indicates the requested device is already accessed by another application.", reason);
                    break;
                case UNRECOGNIZED:
                    sendResultError("The value sent by the plugin service was unrecognized indicating an upgrade of the PCC may be required to handle the value.", reason);
                    break;
                case CHANNEL_NOT_AVAILABLE:
                    sendResultError("The ANT Radio Service reports that no free channels are available.", reason);
                    break;
                case SEARCH_TIMEOUT:
                    sendResultError("The search did not find the requested device before the timeout period.", reason);
                    break;
                default:
                    sendResultError("Unrecognized result.", reason);
                    break;
            }

        }

        @Override
        public void onSearchStarted(RssiSupport supportsRssi) {

        }
    };

}
