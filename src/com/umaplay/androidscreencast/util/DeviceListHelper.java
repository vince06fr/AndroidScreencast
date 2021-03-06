package com.umaplay.androidscreencast.util;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import javafx.application.Platform;

import java.io.IOException;

/**
 * Created by user on 10/18/2015.
 */
public class DeviceListHelper {

    public static void RefreshList(final AndroidDebugBridge bridge, final DeviceListListener listListener) {
        listListener.onLoadStart();

        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean triedRestart = false;
                int count = 0;
                while (!bridge.hasInitialDeviceList()) {
                    try {
                        Thread.sleep(100);
                        count++;
                    } catch (InterruptedException e) {
                        // pass
                    }
                    // let's not wait > 10 sec.
                    if (count > 300) {
                        if(triedRestart) {
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    listListener.onLoadTimeout();
                                }
                            });

                            return;
                        }
                        else {
                            count = 0;
                            //try to ginger adb
                            Command.sendToRuntime("adb kill-server");
                            Command.sendToRuntime("adb start-server");
                            Command.sendToRuntime("adb devices");
                        }
                    }
                }

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        listListener.onLoadEnd(bridge);
                    }
                });
            }
        }).start();
    }

    public interface DeviceListListener {
        public void onLoadStart();
        public void onLoadEnd(AndroidDebugBridge bridge);
        public void onLoadTimeout();
    }
}
