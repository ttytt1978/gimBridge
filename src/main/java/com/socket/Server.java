package com.socket;

import com.bean.PointData;
import com.service.PointDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class Server extends Thread{

    private PointDataService pointDataService;
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    private boolean started;
    private ServerSocket serverSocket;
    private int port;
    private String table_name;
    private ExecutorService threadPool = Executors.newCachedThreadPool();
    Server(int port, String table_name, PointDataService pointDataService){
        this.port = port;
        this.table_name = table_name;
        this.pointDataService = pointDataService;
    }

    @Override
    public void run(){
        try{
            serverSocket = new ServerSocket(port);
            started = true;
            logger.info("数据接收端口已启动，端口：{}", serverSocket.getLocalPort());
        }catch (IOException e){
            logger.info("端口异常信息", e);
        }
        while(started){
            try {
                Socket socket = serverSocket.accept();
                Runnable runnable = () -> {
                    try{
                        PointData inputData = onMessage(socket);
                        pointDataService.add(inputData, table_name);
                        socket.close();
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                };
                Future future = threadPool.submit(runnable);
                logger.info(future.isDone() + "----------");
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    private static PointData onMessage(Socket socket){
        byte[] bytes = new byte[1024];
        PointData pointData = new PointData();
        int len;
        try{
            InputStream inputStream = socket.getInputStream();
            String rawStr = "";
            while((len = inputStream.read(bytes)) != -1){
                rawStr =  new String(bytes, 0 ,len, StandardCharsets.UTF_8);
            }
            String[] str = rawStr.split(",");
            long week = Long.parseLong(str[1]);
            double sec = Double.parseDouble(str[2]);
            long nanos = (long)(sec * 1e9);
            BigDecimal x = new BigDecimal(str[15]);
            BigDecimal y = new BigDecimal(str[16]);
            BigDecimal z = new BigDecimal(str[17]);
            int status = Integer.parseInt(str[20].substring(0, 1));
            LocalDateTime startDate = LocalDateTime.parse("1980-01-06T00:00:00.000").plusWeeks(week).plusNanos(nanos).plusHours(8).minusSeconds(18);
            Timestamp time = Timestamp.valueOf(startDate);
            pointData.setDatetime(time);
            pointData.setX(x);
            pointData.setY(y);
            pointData.setZ(z);
            pointData.setStatus(status);

        }catch(IOException e){
            e.printStackTrace();
        }
        return pointData;
    }

}
