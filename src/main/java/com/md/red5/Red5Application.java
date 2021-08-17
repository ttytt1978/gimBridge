package com.md.red5;

import org.red5.server.adapter.MultiThreadedApplicationAdapter;
import org.red5.server.api.IConnection;
import org.red5.server.api.scope.IScope;
import org.red5.server.api.stream.IBroadcastStream;
import org.red5.server.api.stream.IServerStream;

import java.util.Map;

public class Red5Application extends MultiThreadedApplicationAdapter {

    private IScope appScope;
    private IServerStream serverStream;
    private Map<String, MyStreamListener> map = Red5Session.map;


    /** {@inheritDoc} */
    @Override
    public boolean appStart(IScope app) {
        super.appStart(app);
        log.info("Red5Application appStart应用启动了...");
        System.out.println("Red5Application appStart");
        appScope = app;
//        Mp4Pusher pusher1=new Mp4Pusher("rtmp://127.0.0.1/oflaDemo/stream11","rtmp://127.0.0.1/oflaDemo/stream13");
//        IPusher pusher1=new MyPusher("rtmp://127.0.0.1/oflaDemo/stream12","rtmp://127.0.0.1/oflaDemo/stream13");
////        IPusher pusher1=new MyPusher("d:\\ffmpeg\\b2.mp4","rtmp://127.0.0.1/oflaDemo/stream13");
//        IPusher pusher1=new TwoStreamFullPusher("rtmp://127.0.0.1/oflaDemo/stream12","rtmp://127.0.0.1/oflaDemo/stream11","rtmp://127.0.0.1/oflaDemo/stream13");
//        Red5Session.pusherList.add(pusher1);
//        pusher1.start();
//        IPusher pusher2=new MyPusher("rtmp://127.0.0.1/oflaDemo/stream12","rtmp://127.0.0.1/oflaDemo/stream16");
//        Red5Session.pusherList.add(pusher2);
//        pusher2.start();
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean appConnect(IConnection conn, Object[] params) {
        log.info("Red5Application appConnect新建了一个连接...");
        IScope appScope = conn.getScope();
        log.debug("App connect called for scope: {}", appScope.getName());
        // getting client parameters
        Map<String, Object> properties = conn.getConnectParams();
        if (log.isDebugEnabled()) {
            for (Map.Entry<String, Object> e : properties.entrySet()) {
                log.debug("Connection property: {} = {}", e.getKey(), e.getValue());
            }
        }
        return super.appConnect(conn, params);
    }

    /** {@inheritDoc} */
    @Override
    public void appDisconnect(IConnection conn) {
        log.info("Red5Application appDisconnect断开了一个连接...");
        if (appScope == conn.getScope() && serverStream != null) {
            serverStream.close();
        }
        super.appDisconnect(conn);
    }

    @Override
    public void streamPublishStart(IBroadcastStream stream) {
        String publishedName = stream.getPublishedName();
        log.info("-------------streamPublishStart---------------------"+publishedName);
//        if(publishedName.contains("double"))
//        {
//            super.streamPublishStart(stream);
//            return;
//        }
//        if(!map.keySet().contains(publishedName))
//        {
//            MyStreamListener com.listener=new MyStreamListener(publishedName,this.scope);
//            map.put(publishedName,com.listener);
//            stream.addStreamListener(com.listener);
//        }else {
//            MyStreamListener com.listener=map.get(publishedName);
//            com.listener.reset();
//            stream.addStreamListener(com.listener);
//        }
//        if(publishedName.equals("stream1"))//仅仅转码此通道
//        {
//            PushRTMP.run();
//        }
        super.streamPublishStart(stream);
    }

    @Override
    public void streamBroadcastClose(IBroadcastStream stream) {
        String publishedName = stream.getPublishedName();
        log.info("-------------streamBroadcastClose---------------------"+publishedName);
        super.streamBroadcastClose(stream);
    }


    /** {@inheritDoc} */
    @Override
    public void appStop(IScope app) {
        log.info("Red5Application appStop应用停止了...");
        super.appStop(app);
    }

    }
