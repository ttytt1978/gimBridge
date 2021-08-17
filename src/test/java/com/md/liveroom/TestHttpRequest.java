package com.md.liveroom;

import com.net.IBusinessResponseListener;
import com.net.RequestConfig;
import com.request.AllLiveRoomsRequest;

import java.util.List;
import java.util.Scanner;

public class TestHttpRequest {

    private static String serverUrl = "http://iot.dushuren123.com/red5/rest/";
    public static void main(String[] args) {
        try {
            RequestConfig.setRquestUrlRoot(serverUrl);
            AllLiveRoomsRequest request=new AllLiveRoomsRequest(new IBusinessResponseListener<LiveRoom>() {
                @Override
                public void updateSuccess(List<LiveRoom> newList) {
                        System.out.println(newList.size());
                }

                @Override
                public void updateError(String errorMessage) {
                        System.err.println(errorMessage);
                }
            });
            request.post();
//            Thread.sleep(8*1000);
//            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
