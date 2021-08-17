package com.socket;

import com.service.PointDataService;
import com.service.PointService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SocketServer {
    @Autowired
    PointService pointService;

    @Autowired
    PointDataService pointDataService;
    public void start(){
        List<Integer> list = pointService.getWorkPortList();
        Server[] servers = new Server[list.size()];
        int index = 0;
        for(Integer i: list){
            String table_name = pointService.getTableNameByPort(i);
            servers[index] = new Server(i, table_name, pointDataService);
            servers[index].start();
            index++;
        }
    }
}
