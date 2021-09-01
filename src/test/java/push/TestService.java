package push;


import com.bean.*;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.service.*;
import org.apache.james.mime4j.dom.datetime.DateTime;
import org.bytedeco.javacpp.annotation.Raw;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mybatis.spring.annotation.MapperScan;
import org.red5.SpringBootRed5Application;
import org.red5.compatibility.flex.messaging.io.ArrayCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringRunner;
import com.UI.CommonUtil;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SpringBootRed5Application.class)
@ComponentScan({"com.service"})
@MapperScan("com.dao")
public class TestService {
    @Autowired
    UserService userService;

    @Autowired
    PointService pointService;

    @Autowired
    PointDataService pointDataService;

    @Test
    public void test1() throws Exception {
        /*
        User user = new User();
        user.setName("admin");
        user.setPassword("123456");
        System.out.print(user);
        System.out.print(userService.add(user));
        //*/
        //System.out.println(userService.getPasswordByName("administrator"));
        /*
        Point point = new Point();
        point.setPointName("监测点05");
        point.setUnit("港珠澳大桥");
        point.setPort(8001);
        point.setLon(new BigDecimal("104.495530"));
        point.setLat(new BigDecimal("33.680364"));
        point.setStart_x(new BigDecimal("4424507.866"));
        point.setStart_y(new BigDecimal("452299.121"));
        point.setStart_z(new BigDecimal("42.251"));
        point.setCalculation_time(Timestamp.valueOf("2021-07-22 16:51:38.251"));
        point.setStd_x(new BigDecimal("4424507.866"));
        point.setStd_y(new BigDecimal("452299.121"));
        point.setStd_z(new BigDecimal("42.251"));
        point.setLegal_x_threshold(new BigDecimal("1.0"));
        point.setLegal_y_threshold(new BigDecimal("1.0"));
        point.setLegal_z_threshold(new BigDecimal("1.0"));
        point.setStd_x_threshold(new BigDecimal("0.02"));
        point.setStd_y_threshold(new BigDecimal("0.02"));
        point.setStd_z_threshold(new BigDecimal("0.02"));
        point.setEmergency_x_threshold(new BigDecimal("0.02"));
        point.setEmergency_y_threshold(new BigDecimal("0.02"));
        point.setEmergency_z_threshold(new BigDecimal("0.02"));
        point.setTable_name("point_data_05");
        System.out.println(pointService.add(point));
        //*/
        /*
        Point point = pointService.getPointByPointName("监测点01");
        point.setPointName("监测点02");
        point.setUnit("港珠澳大桥");
        point.setPort(8002);
        point.setLon(new BigDecimal("39.9693785"));
        point.setLat(new BigDecimal("116.4414285"));
        point.setStart_x(new BigDecimal("4424507.865"));
        point.setStart_y(new BigDecimal("452299.125"));
        point.setStart_z(new BigDecimal("42.255"));
        point.setCalculation_time(Timestamp.valueOf("2021-07-22 16:51:38.255"));
        point.setStd_x(new BigDecimal("4424507.865"));
        point.setStd_y(new BigDecimal("452299.125"));
        point.setStd_z(new BigDecimal("42.255"));
        point.setLegal_x_threshold(new BigDecimal("1.1"));
        point.setLegal_y_threshold(new BigDecimal("1.1"));
        point.setLegal_z_threshold(new BigDecimal("1.1"));
        point.setStd_x_threshold(new BigDecimal("0.03"));
        point.setStd_y_threshold(new BigDecimal("0.03"));
        point.setStd_z_threshold(new BigDecimal("0.03"));
        point.setEmergency_x_threshold(new BigDecimal("0.03"));
        point.setEmergency_y_threshold(new BigDecimal("0.03"));
        point.setEmergency_z_threshold(new BigDecimal("0.03"));
        point.setTable_name("point_data_02");
        System.out.println(pointService.update(point));
        //*/
        /*/
        PointData pointData = new PointData();
        pointData.setDatetime(Timestamp.valueOf("2021-08-12 11:11:12.124"));
        pointData.setX(BigDecimal.valueOf(4424507.896));
        pointData.setY(BigDecimal.valueOf(452299.151));
        pointData.setZ(BigDecimal.valueOf(42.255));
        pointDataService.add(pointData, "point_data_04");
        //*/
        /*
        PointData pointData = new PointData();
        pointData.setId(1);
        pointData.setStd_x(new BigDecimal("4424507.866"));
        pointData.setStd_y(new BigDecimal("452299.125"));
        pointData.setStd_z(new BigDecimal("42.251"));
        pointData.setBe_legal(1);
        pointData.setBe_std(1);
        pointData.setDiff_x(new BigDecimal("0.01011"));
        pointData.setDiff_y(new BigDecimal("0.01022"));
        pointData.setDiff_z(new BigDecimal("-0.0055"));
        pointData.setEmergency_x(1);
        pointData.setEmergency_y(1);
        pointData.setEmergency_z(1);
        System.out.println(pointDataService.update(pointData, "point_data_01"));
        //*/
        /*
        Timestamp t1 = Timestamp.valueOf("2021-08-16 13:35:09.123");
        Random r = new Random();
        double dr;
        PointData pointData = new PointData();
        for(int i = 0; i < 36000; i++) {
            t1.setTime(t1.getTime() + 1000);
            pointData.setDatetime(t1);
            pointData.setStatus(4);
            dr = (r.nextDouble() - 0.5) * 0.04;
            pointData.setX(BigDecimal.valueOf(4424508.866 + dr));
            dr = (r.nextDouble() - 0.5) * 0.04;
            pointData.setY(BigDecimal.valueOf(452298.121 + dr));
            dr = (r.nextDouble() - 0.5) * 0.04;
            pointData.setZ(BigDecimal.valueOf(48.251 + dr));
            pointDataService.add(pointData, "point_data_02");
        }
        //*/

        //*
        PageInfo<PointDataList> list = pointDataService.getPointDataList(Timestamp.valueOf("2021-08-12 10:11:12.124"), Timestamp.valueOf("2021-08-12 10:26:12.000"), "point_data_03", 2, 50);
        for(PointDataList pointDataList: list.getList()) {
            System.out.print(pointDataList.toString());
        }

        //*/
        /*/
        PageInfo<RawPointDataList> list = pointDataService.getRawPointDataList(Timestamp.valueOf("2021-08-16 14:11:20.124"), Timestamp.valueOf("2021-08-16 14:12:20.000"), "point_data_02", 2, 20);
        for(RawPointDataList rawPointDataList: list.getList()) {
            System.out.print(rawPointDataList.toString());
        }
        //*/
        /*/
        Point point = pointService.getPointByPointName("监测点02");
        pointService.startCalculate(point);
        //*/
        /*
        List<Point> list = pointService.searchNoCalculatePoint();
        System.out.print(list.toString());
        //*/
        /*/
        pointService.calculateStd();
        //*/
//        List<Integer> list = pointService.getWorkPort();
//        for(Integer i: list){
//            System.out.println(i);
//        }
    }


    @Test
    public void test2(){
        /*
        Xtpz xtpz = new Xtpz();
        xtpz.setPzmc("配置1");
        xtpz.setZbpz("双路1");
        xtpz.setDbpz("双路2");
        xtpz.setXtpz("工作3");
        xtpzService.add(xtpz);
        */
        /*
        PageInfo<Xtpz> list = xtpzService.getXtpzList(1, 3);
        for(Xtpz xtpz:list.getList()){
            System.out.println(xtpz.toString());
        }
        //*/
    }

    @Test
    public void test3() throws Exception{
        /*add
        Zbj zbj = new Zbj();
        zbj.setZbjbh("202012130003");
        zbj.setZbjmc("第14直播间");
        zbj.setZblx("1");
        zbj.setKhtldz("127.0.0.5");
        zbj.setKhbfdz("127.0.1.1");
        zbj.setCpjltldz("127.1.1.10");
        zbj.setCpjlbfdz("127.1.1.11");
        zbj.setDltldz("127.1.2.10");
        zbj.setDlbfdz("127.1.2.11");
        zbj.setDsftldz("127.1.3.10");
        zbj.setDsfbfdz("127.1.3.11");
        zbj.setSpwjm("1122.mp4");
        zbj.setZm("abcd");
        zbj.setCjsj(CommonUtil.stringToDate("2021-01-19"));
        zbj.setYhm("0018");
        zbj.setFwqbh("202012140002");
        zbj.setZbfwqbh("202102070007");
        zbj.setZt("3");
        System.out.println(zbjService.add(zbj));
        //*/

        /*upadate
        Zbj zbj = new Zbj();
        zbj.setZbjbh("202102160001");
        zbj.setZbjmc("第7直播间");
        zbj.setZblx("1");
        zbj.setKhtldz("127.0.0.5");
        zbj.setKhbfdz("127.0.1.1");
        zbj.setCpjltldz("127.1.1.10");
        zbj.setCpjlbfdz("127.1.1.11");
        zbj.setDltldz("127.1.2.10");
        zbj.setDlbfdz("127.1.2.11");
        zbj.setDsftldz("127.1.3.10");
        zbj.setDsfbfdz("127.1.3.11");
        zbj.setSpwjm("1122.mp4");
        zbj.setZm("dcba");
        zbj.setCjsj(CommonUtil.stringToDate("2021-01-19"));
        zbj.setYhm("0018");
        zbj.setFwqbh("202012140002");
        zbj.setZbfwqbh("202102070007");
        zbj.setZt("3");
        System.out.println(zbjService.update(zbj));
        //*/

        /*delete
        System.out.println(zbjService.delete("202102160001"));
        //*/

        /*
        System.out.println(zbjService.getZbjByZbjbh("202012130001").toString());
        //*/

        /*
        PageInfo<ZbjList> list = zbjService.getZbjList(null, null, null, null, null, null, null, null,1, 2);
        for(ZbjList zbjList:list.getList()){
            System.out.println(zbjList.toString());
        }
        //*/
    }

    @Test
    public void test4() throws Exception{
        /*add
        Zbfwq zbfwq = new Zbfwq();
        zbfwq.setFwqmc("4号服务器");
        zbfwq.setIp("192.101.100.4");
        zbfwq.setHttpport("8001");
        zbfwq.setRtmpport("8002");
        zbfwq.setHttpsport("8003");
        zbfwq.setKmpport("8004");
        zbfwq.setUdpport("8005");
        zbfwq.setInnerIp("192.168.100.1");
        zbfwq.setInnerHttpPort("8006");
        zbfwq.setInnerHttpsPort("8007");
        zbfwq.setInnerRtmpPort("8008");
        zbfwq.setInnerKmpPort("8009");
        zbfwq.setInnerUdpPort("8010");
        System.out.println(zbfwqService.add(zbfwq));
        //*/

        /*update
        Zbfwq zbfwq = new Zbfwq();
        zbfwq.setFwqbh("202102070007");
        zbfwq.setFwqmc("7号服务器");
        zbfwq.setIp("192.101.100.14");
        zbfwq.setHttpport("8001");
        zbfwq.setRtmpport("8002");
        zbfwq.setHttpsport("8003");
        zbfwq.setKmpport("8004");
        zbfwq.setUdpport("8005");
        zbfwq.setInnerIp("192.168.100.1");
        zbfwq.setInnerHttpPort("8006");
        zbfwq.setInnerHttpsPort("8007");
        zbfwq.setInnerRtmpPort("8008");
        zbfwq.setInnerKmpPort("8009");
        zbfwq.setInnerUdpPort("8010");
        System.out.println(zbfwqService.update(zbfwq));
        //*/

        /*delet
        System.out.println(zbfwqService.delete("202102150001"));
        //*/

        /*getZbfwqByFwqbh
        Zbfwq zbfwq = zbfwqService.getZbfwqByFwqbh("202102070005");
        System.out.println(zbfwq.toString());
        //*/

        /*getZbfwqList
        PageInfo<Zbfwq> list = zbfwqService.getZbfwqList(null, null, 2, 3);
        for(Zbfwq zbfwq :list.getList()){
            System.out.println(zbfwq.toString());
        }
        //*/

    }
    @Test
    public void test5() throws Exception{
        /*add
        Js js = new Js();
        js.setJsmc("管理1");
        js.setZbgl("1");
        js.setZbcx("1");
        js.setDbgl("0");
        js.setDbcx("1");
        js.setFwqpzgl("1");
        js.setFwqpzcx("0");
        js.setZbfwqpzgl("1");
        js.setZbfwqpzcx("0");
        js.setXtgl("1");
        System.out.println(jsService.add(js));
        //*/

        /*update
        Js js = new Js();
        js.setJsbh("005");
        js.setJsmc("管理2");
        js.setZbgl("1");
        js.setZbcx("1");
        js.setDbgl("0");
        js.setDbcx("1");
        js.setFwqpzgl("1");
        js.setFwqpzcx("0");
        js.setZbfwqpzgl("1");
        js.setZbfwqpzcx("0");
        js.setXtgl("1");
        System.out.println(jsService.update(js));
        /*/

        /*delet
        System.out.println(jsService.delete("008"));
        //*/

        /*getJsByJsbh
        System.out.println(jsService.getJsByJsbh("005").toString());
        //*/

        /*getJsList
        PageInfo<Js> list= jsService.getJsList(null, 2, 2);
        for(Js js:list.getList()){
            System.out.println(js.toString());
        }
        //*/

        /*
        PageInfo<JsList> list = jsService.getJsbhAndJsmcList(1,2);
        for(JsList jsList:list.getList()){
            System.out.println(jsList.toString());
        }
        //*/
    }
    @Test
    public void test6() throws Exception{
//        Lmtfwq lmtfwq = new Lmtfwq();
        /*add
        lmtfwq.setFwqmc("6号服务器");
        lmtfwq.setIp("192.168.100.1");
        lmtfwq.setHttpPort("8001");
        lmtfwq.setHttpsPort("8002");
        lmtfwq.setInnerIp("192.168.1001.1");
        lmtfwq.setInnerHttpPort("8003");
        lmtfwq.setInnerHttpsPort("8004");
        lmtfwq.setJbpz("配置3");
        lmtfwq.setZbspcclj("e:\\zb");
        lmtfwq.setDbspcclj("e:\\db");
        lmtfwq.setCjsj(CommonUtil.stringToDate("2021-01-19"));
        lmtfwq.setYhm("0011");
        lmtfwq.setZdslbfs(3);
        lmtfwq.setZt("1");
        System.out.println(lmtfwqService.add(lmtfwq));
        //*/

        /*update
        lmtfwq.setFwqbh("202102160001");
        lmtfwq.setFwqmc("9号服务器");
        lmtfwq.setIp("192.168.100.1");
        lmtfwq.setHttpPort("8001");
        lmtfwq.setHttpsPort("8002");
        lmtfwq.setInnerIp("192.168.1001.1");
        lmtfwq.setInnerHttpPort("8003");
        lmtfwq.setInnerHttpsPort("8004");
        lmtfwq.setJbpz("配置3");
        lmtfwq.setZbspcclj("f:\\zb");
        lmtfwq.setDbspcclj("f:\\db");
        lmtfwq.setCjsj(CommonUtil.stringToDate("2021-01-20"));
        lmtfwq.setYhm("0012");
        lmtfwq.setZdslbfs(5);
        lmtfwq.setZt("1");
        System.out.println(lmtfwqService.update(lmtfwq));
        //*/

        /*delete
        System.out.println(lmtfwqService.delete("202102160001"));
        //*/

        /*getLmtfwqByFwqbh
        System.out.println(lmtfwqService.getLmtfwqByFwqbh("202102150001").toString());
        //*/

        //*
//        PageInfo<Lmtfwq> list = lmtfwqService.getLmtfwqList(null,null, null, null, null, 1, 3);
//        for(Lmtfwq lmtfwqList:list.getList()){
//            System.out.println(lmtfwqList.toString());
//        }
        //*/
    }
}
