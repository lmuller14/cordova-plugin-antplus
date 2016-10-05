# ANT+ plugin for Apache Cordova

This plugin enables communication between Android phone and ANT+ devices

Originally created for MoBio app:
* [MoBio](https://github.com/NEUROINFORMATICS-GROUP-FAV-KIV-ZCU/MoBio) 

## Supported Platforms

* Android

# ANT support

In order to use this plugin install the following apps to your phone.
The apps enable the communication with ANT+ OTG modul if you are using it.
* [ANT Radio Service] (https://play.google.com/store/apps/details?id=com.dsi.ant.service.socket)
* [ANT+ Plugins Service] (https://play.google.com/store/apps/details?id=com.dsi.ant.plugins.antplus)   
* [ANT USB Service] (https://play.google.com/store/apps/details?id=com.dsi.ant.usbservice)

# Installing

## Cordova

    $ cordova plugin add cordova-plugin-antplus

# API

## Methods

- [antplus.searchDevices] (#searchDevices)

## searchDevices

Search and discover ANT+ devices.

    antplus.searchDevices(comma_separated_device_definition, success, failure)

### Description

Function `searchDevices` scans for the defined ANT+ devices. Success callback is called every time the a device is discovered.
First parameter is string - comma separated values of device types that the function scans for. 
Possible values are:
- HEARTRATE
- BIKE_SPD
- BIKE_CADENCE
- BIKE_SPDCAD
- STRIDE_SDM
- WEIGHT_SCALE

### success response parameters

- resultID
- describeContents
- antDeviceNumber
- antDeviceType
- deviceDisplayName
- isAlreadyConnected
- isPreferredDevice
- isUserRecognizedDevice

### error response parameters

- event
- message
- code

### Example

    antplus.searchDevices('BIKE_SPD,BIKE_CADENCE,BIKE_SPDCAD', function(device) {
        console.log(JSON.stringify(device));
    }, failure);