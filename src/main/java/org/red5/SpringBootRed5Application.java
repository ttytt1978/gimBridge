package org.red5;


import com.socket.SocketServer;
import org.mybatis.spring.annotation.MapperScan;
import org.red5.server.util.RootPathUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jmx.JmxAutoConfiguration;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;


@ServletComponentScan
@SpringBootApplication
@ComponentScan(basePackages = {"com.convert","com.service", "com.maincontroller", "com.slavecontroller","com.socket", "com.schedule","org.red5.demo"})
@MapperScan(basePackages = {"com.dao"})
@EnableScheduling
@EnableAutoConfiguration(exclude = {JmxAutoConfiguration.class})
public class SpringBootRed5Application {

    public static void main(String[] args) throws Exception {
    	//设置root目录
    	RootPathUtil.iniRoot();
    	//启动服务器
        ApplicationContext applicationContext = SpringApplication.run(SpringBootRed5Application.class, args);
        applicationContext.getBean(SocketServer.class).start();
        //视频推流
//        PushRTMP.run();
    } 
}
