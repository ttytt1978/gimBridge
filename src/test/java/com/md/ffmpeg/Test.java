package com.md.ffmpeg;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.metal.MetalSliderUI;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Vector;

/**
 * 对视频播放器组件进行测试
 */
public class Test {
    JFrame jf;
    videoPlayer player;
    public boolean isReleased = false;
    public int shallSec;
    int imgW,imgH;
    JPanel videoPanel;
    public Test(String file){
        if(!(new File(file)).exists()){
            file = getFile();
            if(file == null){
                System.exit(0);
            }
        }
        //每次播放都应该有个历史记录
        //预读取，比较，如果有相同，就不需要再记录了。
        saveHistory(file);
        readHistory();
        jf = new JFrame(getFileName(file));
        jf.setBounds(100,100,1024+100,700+200);
        jf.setLayout(null);
        jp = new JPanel();
        jp.setBounds(0,0,1024+100,700+200);
        jp.setLayout(null);
        jf.add(jp);
        player = new videoPlayer(file);
        imgW = player.getVideoWidth();
        imgH = player.getVideoHeight();
        videoPanel = new JPanel();
        videoPanel.setBackground(Color.BLACK);
        videoPanel.setLayout(null);
        videoPanel.add(player);
        videoPanel.setBounds(0,0,1024,700);
       if(((double)imgW/(double)imgH)>(1024.0/700.0)){
            player.setLocation(0,(720-1024*imgH/imgW)/2);
            player.setVideoWH(1024,1024*imgH/imgW);
        }else{
            player.setLocation((1024-700*imgW/imgH)/2,0);
            player.setVideoWH(700*imgW/imgH,700);
        }
        //player.setStartTime(0,16,0);
        initOther();
        jp.add(videoPanel);
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jf.setVisible(true);
        //Util.DEBUG2 = true;
        //Util.DEBUG3 = true;
        //Util.DEBUG1 = true;
        //Util.DEBUG4 = true;
        //Util.DEBUG5 = true;
    }
    Vector<String> v = new Vector<>();
    private void readHistory() {
        File f = new File("playHistory");
        if(!f.exists()){
            return;
        }
        try {
            BufferedReader bis = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
            String s;
            while((s =bis.readLine())!= null){
                if(!v.contains(s.substring(s.lastIndexOf("\\")+1)))
                    v.add(s.substring(s.lastIndexOf("\\")+1));
            }

            for(int i = 0;i<v.size();i++){
                System.out.println(v.get(i));
            }
            bis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveHistory(String file) {
        File f = new File("playHistory");
        if(!f.exists()){
            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(f,"rw");
            raf.seek(raf.length());
            raf.write((file+"\r\n").getBytes("utf-8"));
            raf.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    JPanel jp;
    JButton start, pause,stop,forward,backward,load;
    JSlider jsl,volAdj;
    JLabel jl;
    boolean jslFlag = false;
    private void initOther() {
        start = new JButton("start");
        start.setBounds(50,700+20,100,20);
        start.addActionListener(new startE());
        jp.add(start);
        pause = new JButton("pause");
        pause.setBounds(170,700+20,100,20);
        pause.addActionListener(new pauseE());
        jp.add(pause);
        stop = new JButton("stop");
        stop.setBounds(290,700+20,100,20);
        stop.addActionListener(new stopE());
        jp.add(stop);
        forward = new JButton("forward");
        forward.setBounds(410,700+20,100,20);
        forward.addActionListener(new forwardE());
        jp.add(forward);
        backward = new JButton("backward");
        backward.setBounds(530,700+20,100,20);
        backward.addActionListener(new backE());
        jp.add(backward);
        jsl = new JSlider(0,(int)((double)player.getVideoTotalLength()/1000000.0),0);
        jsl.setBounds(50,700+50,630,20);
        jsl.addChangeListener(new jslE());
        jslE2 je2 = new jslE2();
        jsl.addMouseListener(je2);
        jsl.addMouseMotionListener(je2);
        jsl.setUI(new MetalSliderUI(){
            @Override
            protected void scrollDueToClickInTrack(int direction){
                int value = slider.getValue();
                if(slider.getOrientation()==JSlider.HORIZONTAL){
                    value = this.valueForXPosition(slider.getMousePosition().x);
                }else{
                    value = this.valueForYPosition(slider.getMousePosition().y);
                }
                slider.setValue(value);
            }
        });
        jp.add(jsl);
        volAdj = new JSlider(0,100,100);
        volAdj.setBounds(700,700+50,200,20);
        volAdj.setUI(new MetalSliderUI(){
            @Override
            protected void scrollDueToClickInTrack(int direction){
                int value = slider.getValue();
                if(slider.getOrientation()==JSlider.HORIZONTAL){
                    value = this.valueForXPosition(slider.getMousePosition().x);
                }else{
                    value = this.valueForYPosition(slider.getMousePosition().y);
                }
                slider.setValue(value);
            }
        });
        volAdj.addChangeListener(new volE());
        jp.add(volAdj);
        load = new JButton("load video");
        load.setBounds(50,700+80,200,20);
        load.addActionListener(new loadE());
        jp.add(load);
        jl = new JLabel();
        displayTime(0);
        new Timer(100, new ActionListener() {
            int oldValue,newValue;
            long msec;
            @Override
            public void actionPerformed(ActionEvent e) {
                msec = player.getCurPlayTime();
                newValue = (int)((double)msec/1000000.0);
                if(newValue != oldValue){
                    oldValue = newValue;
                    displayTime(msec);
                    jslFlag = true;
                    jsl.setValue(newValue);
                }
            }
        }).start();
        jl.setBounds(280,700+80,250,20);
        jp.add(jl);
    }

    public static void main(String[] args){
        String s1 = "myVideo.mp4";
        String s2 = "http://flv2.bn.netease.com/videolib3/1604/28/fVobI0704/SD/fVobI0704-mobile.mp4";
        new Test(s1);
    }
    public static String getFileName(String file){
        return file.substring(file.lastIndexOf(File.separator)+1);
    }
    public void displayTime(long value){
        jl.setText(Util.getTimeString(value)+" / "+player.getTotalString());
    }
    private class startE implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            player.setStartPlay();
        }
    }

    private class stopE implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            player.setStopPlay();
            /**
             * Ref:https://stackoverflow.com/questions/45826802/convert-an-imageicon-to-a-base64-string-and-back-to-an-imageicon-without-saving
             * 参考上面的链接得到一个黑色的图片，用于停止时清除画面。
             */
            BufferedImage image = new BufferedImage(
                    player.getIcon().getIconWidth(),
                    player.getIcon().getIconHeight(),
                    BufferedImage.TYPE_INT_RGB);//这个的话里面的值全0所以是黑色
            player.setIcon(new ImageIcon(image));
        }
    }

    private class pauseE implements ActionListener {
        private boolean toggle;
        public pauseE(){
            toggle = true;
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            if(toggle){
                player.setPausePlay();
                pause.setText("resume");
            }else{
                player.setResumePlay();
                pause.setText("pause");
            }
            toggle = !toggle;
        }
    }

    private class jslE implements ChangeListener {
        @Override
        public void stateChanged(ChangeEvent e) {
            /**
             * 只取进度条拖动或点击后的最后一次值
             * 设置进度，不然容易卡顿。
             */
            if(jslFlag){
                //setValue事件
                jslFlag = false;
                return;
            }
            synchronized (Test.this){
                if(isReleased){
                    isReleased = false;
                    player.setPlayTime(shallSec);
                }else{
                    return;
                }
            }
        }
    }

    private class loadE implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            loadFile();
        }
    }
    private String getFile(){
        if(File.separator.equals("\\"))//判断是windows系统
            try {
                UIManager.setLookAndFeel(com.sun.java.swing.plaf.windows.WindowsLookAndFeel.class.getName());
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        JFileChooser jfc = new JFileChooser();
        jfc.showDialog(null,JFileChooser.APPROVE_SELECTION);
        try {
            UIManager.setLookAndFeel(javax.swing.plaf.metal.MetalLookAndFeel.class.getName());
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        if(jfc.getSelectedFile() == null){
            return null;
        }
        return jfc.getSelectedFile().getAbsolutePath();
    }
    private void loadFile(){
        String file = getFile();
        if(file == null){
            return;
        }
        saveHistory(file);
        player.setStopPlay();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        videoPanel.remove(player);
        jf.setLayout(null);
        jf.setTitle(getFileName(file));
        player = new videoPlayer(file);
        imgW = player.getVideoWidth();
        imgH = player.getVideoHeight();
        if(((double)imgW/(double)imgH)>(1024.0/700.0)){
            player.setLocation(0,(720-1024*imgH/imgW)/2);
            player.setVideoWH(1024,1024*imgH/imgW);
        }else{
            player.setLocation((1024-700*imgW/imgH)/2,0);
            player.setVideoWH(700*imgW/imgH,700);
        }
        jp.setBounds(0,0,1024+100,700+200);
        videoPanel.add(player);
        videoPanel.setBackground(Color.BLACK);
        videoPanel.updateUI();//
        displayTime(0);
        jsl.setMaximum((int)((double)player.getVideoTotalLength()/1000000.0));
        jsl.setValue(0);
        player.updateUI();//加载新的视频后更新封面
    }
    private class forwardE implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            long l = player.getCurPlayTime();
            l+=12000000;//一次快进12s
            player.setPlayTime((int)((double)l/1000000.0));
        }
    }

    private class backE implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            long l = player.getCurPlayTime();
            l-=12000000;//一次快退12s
            if(l < 0){
                l = 0;
            }else if(l>=(player.getVideoTotalLength()-12000000)){
                l = 0;
            }
            player.setPlayTime((int)((double)l/1000000.0));
        }
    }

    private class jslE2 extends MouseAdapter{
        public int sec;
        public jslE2(){
            //设置弹出ToolTip延时时间。
            ToolTipManager.sharedInstance().setInitialDelay(0);
        }
        @Override
        public void mouseReleased(MouseEvent e) {
            synchronized (Test.this){
                isReleased = true;
                shallSec = sec;//这是应该设置的秒数。
            }
        }
        /**
         *  首先,MousePosition.x的范围是0到630
         *  与0到jsl的max成比例关系。
         *  然后我们还要修改按下函数，以jsl.getMousePosition().getX()*jsl.getMaximum()/630
         *  作为新值，不然会有放大误差。
         */
        @Override
        public void mouseMoved(MouseEvent e){
            JSlider jsl = (JSlider)e.getSource();
            //System.out.println("当前秒数是"+jsl.getMousePosition().getX()*(double)jsl.getMaximum()/630.0);
            sec = (int)(jsl.getMousePosition().getX()*jsl.getMaximum()/630);
            jsl.setToolTipText(Util.getTimeString((long)((double)sec*1000000.0)));
        }
    }

    private class volE implements ChangeListener {
        @Override
        public void stateChanged(ChangeEvent e) {
            int i = ((JSlider)e.getSource()).getValue();
            float j = (float)i/100.0f;
            player.setVol(j);
        }
    }
}
