
package com.rxlogix.evdas.upload.automation.Utils;

public class CommonMethods {

    //Set wait time to standard 5 second
    public static void waitForElement() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //Set wait time to given wait time
    public static void waitForProcessing(Long waitTime) {
        try {
            Thread.sleep(waitTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


}
