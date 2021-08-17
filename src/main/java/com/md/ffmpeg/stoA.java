package com.md.ffmpeg;

import org.bytedeco.javacpp.avutil;
import org.bytedeco.javacv.Frame;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;

/**
 * 音频FIFO
 */
public class stoA {
    private final int MAX = 500;
    private ArrayList<myAudioFrame> list = new ArrayList<>();
    public void produce(myAudioFrame f){
        synchronized(list){
            while(list.size()+1>MAX){
                try{
                    if(Util.DEBUG2)
                        System.out.println("音频缓冲区已满，需要等待消费");
                    list.wait();
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
            list.add(f);
            list.notifyAll();
        }
    }
    public myAudioFrame consume(){
        myAudioFrame f;
        synchronized (list){
            while(list.size()<1){
                try{
                    if(Util.DEBUG2)
                        System.out.println("音频缓冲区为空，需要等待生产");
                    list.wait();
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
            if(Util.DEBUG2)
                System.out.println("音频缓冲区当前数量"+list.size());
            f = list.remove(0);
            list.notifyAll();
            return f;
        }
    }
    public int getMax(){
        return MAX;
    }
    public int getCur(){
        return list.size();
    }
    public void clearFIFO(){
        synchronized (list){
            list.clear();
            list.notifyAll();
        }
    }
}

class myAudioFrame{
    public Frame f;
    public long id;
    public long timeStamp;
    Buffer[] buf;
    FloatBuffer leftData,rightData;
    ShortBuffer ILData,IRData;
    ByteBuffer TLData,TRData;
    float vol;
    int sampleFormat;
    byte[] tl,tr;
    byte[] combine;
    public myAudioFrame(Frame f, videoPlayer player, long id, long timeStamp, int sampleFormat){
        this.f = f;
        this.id = id;
        this.timeStamp = timeStamp;
        this.sampleFormat = sampleFormat;
        this.vol = player.getVol();
        processAudio();
    }
    private void processAudio() {
        int k;
        buf = f.samples;
        switch(sampleFormat){
            case avutil.AV_SAMPLE_FMT_FLTP://平面型左右声道分开。
                leftData = (FloatBuffer)buf[0];
                TLData = Util.floatToByteValue(leftData,vol);
                rightData = (FloatBuffer)buf[1];
                TRData = Util.floatToByteValue(leftData,vol);
                tl = TLData.array();
                tr = TRData.array();
                combine = new byte[tl.length+tr.length];
                k = 0;
                for(int i=0;i<tl.length;i=i+2) {//混合两个声道。
                    for (int j = 0; j < 2; j++) {
                        combine[j+4*k] = tl[i + j];
                        combine[j + 2+4*k] = tr[i + j];
                    }
                    k++;
                }
                break;
            case avutil.AV_SAMPLE_FMT_S16://非平面型左右声道在一个buffer中。
                ILData = (ShortBuffer)buf[0];
                TLData = Util.shortToByteValue(ILData,vol);
                tl = TLData.array();
                break;
            case avutil.AV_SAMPLE_FMT_FLT://float非平面型
                leftData = (FloatBuffer)buf[0];
                TLData = Util.floatToByteValue(leftData,vol);
                tl = TLData.array();
                break;
            case avutil.AV_SAMPLE_FMT_S16P://平面型左右声道分开
                ILData = (ShortBuffer)buf[0];
                IRData = (ShortBuffer)buf[1];
                TLData = Util.shortToByteValue(ILData,vol);
                TRData = Util.shortToByteValue(IRData,vol);
                tl = TLData.array();
                tr = TRData.array();
                combine = new byte[tl.length+tr.length];
                k = 0;
                for(int i=0;i<tl.length;i=i+2) {
                    for (int j = 0; j < 2; j++) {
                        combine[j+4*k] = tl[i + j];
                        combine[j + 2+4*k] = tr[i + j];
                    }
                    k++;
                }
                break;
            default:
//                JOptionPane.showMessageDialog(null,"unsupport audio format","unsupport audio format",JOptionPane.ERROR_MESSAGE);
                System.exit(0);
                break;
        }
    }
}
