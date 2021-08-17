package org.red5.server.stream.timeshift;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.mina.core.buffer.IoBuffer;
import org.red5.io.ITag;
import org.red5.io.amf.Output;
import org.red5.io.flv.impl.Tag;
import org.red5.server.ScopeContextBean;
import org.red5.server.api.event.IEvent;
import org.red5.server.api.scope.IScope;
import org.red5.server.api.stream.IBroadcastStream;
import org.red5.server.api.stream.IStreamListener;
import org.red5.server.api.stream.IStreamPacket;
import org.red5.server.net.rtmp.event.AudioData;
import org.red5.server.net.rtmp.event.Notify;
import org.red5.server.net.rtmp.event.VideoData;
import org.red5.server.net.rtmp.message.Constants;
import org.red5.server.stream.ClientBroadcastStream;
import org.red5.server.util.SystemTimer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Live Broadcast Stream Record
 * @author pengliren
 *
 */
public class RecordableBroadcastStream extends ClientBroadcastStream implements IStreamListener{

	private Logger log = LoggerFactory.getLogger(RecordableBroadcastStream.class);
	
	private String storePath;
	
	private boolean canRecord = false;
	
	private long durationPerFile = 1000*60*15;//默认15分钟
	
	private long lastRecordTime=-1;
	
	private long lastStreamTime = -1;
	
	private long currentStreamTime = -1;
	
	private RecordFLVWriter writer;
	
	private long lastTimecode = -1;
	
	public RecordableBroadcastStream() {
		
		addStreamListener(this);
	}
	
	public void setStorePath(String storePath) {
		this.storePath = storePath;
	}

	public void setDurationPerFile(long durationPerFile) {
		this.durationPerFile = durationPerFile;
	}

	public long getDurationPerFile() {
		return durationPerFile;
	}

	@Override
	public void setScope(IScope scope) {
		
		super.setScope(scope);
		RecordableBroadcastStream ctxBean = (RecordableBroadcastStream) getScope().getContext().getBean(ScopeContextBean.BROADCASTSTREAM_BEAN); 
		storePath = ctxBean.getStorePath();
		canRecord = ctxBean.isCanRecord();
		if (ctxBean.getDurationPerFile() != 0) {
			durationPerFile = ctxBean.getDurationPerFile() * 1000;
		}
	}
	
	private void startNewWriter() {
		
		closeWriter();
		String path = genRecordPath();
		File recordFile = new File(path);
		try {
			if(!recordFile.getParentFile().exists()) { 
				recordFile.getParentFile().mkdirs();	
				if(File.separatorChar=='/')
					Runtime.getRuntime().exec(String.format("chmod 777 %s", recordFile.getParentFile().getCanonicalPath()));
			}
			
			if(File.separatorChar=='/')			
					Runtime.getRuntime().exec(String.format("chmod 777 %s", recordFile.getCanonicalPath()));
		} catch (IOException e) {			
			log.info("exception {}", e.getMessage());
		}
	    writer = new RecordFLVWriter(recordFile, false);
		
		lastRecordTime = SystemTimer.currentTimeMillis();
		lastStreamTime = currentStreamTime;
		if(this.getCodecInfo()!=null){
			if(this.getCodecInfo().getVideoCodecName()!=null && this.getCodecInfo().getVideoCodecName().equals("AVC")){
				writePacket(0, new VideoData(this.getCodecInfo().getVideoCodec().getDecoderConfiguration()));
			}
			if(this.getCodecInfo().getAudioCodecName()!=null && this.getCodecInfo().getAudioCodecName().equals("AAC")){				
				writePacket(0, new AudioData(this.getCodecInfo().getAudioCodec().getDecoderConfiguration()));
			}
		}
	}
	
	private void writePacket(int timestamp,IStreamPacket packet){
		ITag tag = new Tag();
		tag.setDataType(packet.getDataType());
		tag.setTimestamp(timestamp);
		tag.setBodySize(packet.getData().limit());
		tag.setBody(packet.getData().asReadOnlyBuffer());		
		writer.putTag(tag);	
	}
	
	private void closeWriter() {
		if (writer != null) {
			writer.close();
			writer = null;
		}
	}
	
	public String getStorePath(){
		if(storePath == null){			
			storePath = this.getScope().getContextPath()+"/streams";		
		}
		return storePath;
	}
	
	private String genRecordPath(){
		
		StringBuilder sb = new StringBuilder(getStorePath());
		if(!storePath.endsWith("/") && !storePath.endsWith("\\")) sb.append(File.separatorChar);
		sb.append(this.getPublishedName());
		sb.append(File.separatorChar);
		Date date = new Date();
		sb.append(new SimpleDateFormat("yyyyMMdd").format(date));
	    sb.append(File.separatorChar);
	    sb.append(new SimpleDateFormat("yyyyMMddHHmmss").format(date)).append(".flv");
		
		return sb.toString();
	}
	
	@Override
	public void dispatchEvent(IEvent event) {
		super.dispatchEvent(event);
		long current = SystemTimer.currentTimeMillis();
		if (current - lastTimecode >= 1000) {
			Notify timecodeNotify = new Notify();
			timecodeNotify.setInvokeId(-100);
			timecodeNotify.setTimestamp(this.getCurrentTimestamp());
			IoBuffer timecodeBuff = IoBuffer.allocate(100);
			Output out = new Output(timecodeBuff);
			out.writeString("onTimecode");
			out.writeDate(new Date(current));
			timecodeBuff.flip();
			timecodeNotify.setData(timecodeBuff);
			super.dispatchEvent(timecodeNotify);
			lastTimecode = current;
		}
	}

	@Override
	public void packetReceived(IBroadcastStream stream, IStreamPacket packet) {
		
		if(!canRecord) return;
		if(!isAvailable(packet)) return;
		currentStreamTime = packet.getTimestamp();
		if(lastStreamTime==-1) lastStreamTime = currentStreamTime;
		if (writer == null || ((SystemTimer.currentTimeMillis() - lastRecordTime) > durationPerFile) && isKeyPacket(packet)) startNewWriter();
		writePacket((int) (currentStreamTime - lastStreamTime), packet);
	}
	
	private boolean isAvailable(IStreamPacket packet) {
		if (packet.getDataType() == Constants.TYPE_VIDEO_DATA) {
			if (packet.getData().get(0) == (byte) 0x17 && packet.getData().get(1) == (byte) 0x00)
				return false;
			return true;
		} else if (packet.getDataType() == Constants.TYPE_AUDIO_DATA) {
			if ((packet.getData().get(0) & 0xf0) == 0xa0 && packet.getData().get(1) == (byte) 0x00)
				return false;
			return true;
		} else if (packet.getDataType() == Constants.TYPE_NOTIFY) {
			if (writer == null)
				return false;
			return true;
		}
		return false;
	}
	
	private boolean isKeyPacket(IStreamPacket packet) {
		if (packet.getDataType() == Constants.TYPE_VIDEO_DATA && (packet.getData().get(0) & 0xf0) == 0x10)
			return true;
		return false;
	}

	public boolean isCanRecord() {
		return canRecord;
	}

	public void setCanRecord(boolean canRecord) {
		this.canRecord = canRecord;
		if(!canRecord) {
			closeWriter();
		}
	}
	
}
