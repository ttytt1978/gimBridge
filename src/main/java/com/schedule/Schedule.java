package com.schedule;

import com.service.PointService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Schedule {
    @Autowired
    PointService pointService;

    @Scheduled(fixedDelay = 10 * 1000)
    public void updateStdValue(){
        pointService.calculateStd();
        System.out.println("Update StdValue...");
    }
}
