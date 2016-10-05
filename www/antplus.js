/*global cordova, module*/

module.exports = {
    searchDevices: function (deviceType, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "Antplus", "searchDevices", [deviceType]);
    },
    
    stopSearchDevices: function (successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "Antplus", "stopSearchDevices", []);
    },
    
    subscribeHR: function (antDeviceNumber, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "Antplus", "subscribeHR", [antDeviceNumber]);
    },
    
    unsubscribeHR: function (successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "Antplus", "unsubscribeHR", []);
    },
    
    subscribeWGT: function (antDeviceNumber, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "Antplus", "subscribeWGT", [antDeviceNumber]);
    },
    
    unsubscribeWGT: function (successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "Antplus", "unsubscribeWGT", []);
    },
    
    requestBasicWGT: function (successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "Antplus", "requestBasicWGT", []);
    },
    
    requestAdvancedWGT: function (userProfile, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "Antplus", "requestAdvancedWGT",
                [
                    userProfile.age,
                    userProfile.height,
                    userProfile.gender,
                    userProfile.lifetimeAthlete,
                    userProfile.activityLevel
                ]);
    },
    
    subscribeBP: function (antDeviceNumber, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "Antplus", "subscribeBP", [antDeviceNumber]);
    },
    
    unsubscribeBP: function (successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "Antplus", "unsubscribeBP", []);
    },
    
    stopDataMonitorBP: function (successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "Antplus", "stopDataMonitorBP", []);
    },
    
    getAntFsMfgIDBP: function (successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "Antplus", "getAntFsMfgIDBP", []);
    },
    
    requestDownloadMeasurementsBP: function (onlyNew, monitor, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "Antplus", "requestDownloadMeasurementsBP", [onlyNew, monitor]);
    },
    
    requestResetDataAndSetTimeBP: function (doSetTime, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "Antplus", "requestResetDataAndSetTimeBP", [doSetTime]);
    },
    
    subscribeSDM: function (antDeviceNumber, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "Antplus", "subscribeSDM", [antDeviceNumber]);
    },
    
    unsubscribeSDM: function (successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "Antplus", "unsubscribeSDM", []);
    },
    
    subscribeBike: function (antDeviceNumber, circumference, deviceType, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "Antplus", "subscribeBike", [antDeviceNumber, circumference, deviceType]);
    },
    
    unsubscribeBike: function (successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "Antplus", "unsubscribeBike", []);
    }
};
