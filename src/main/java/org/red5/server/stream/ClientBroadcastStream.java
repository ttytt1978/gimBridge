/*
 * RED5 Open Source Media Server - https://github.com/Red5/
 * 
 * Copyright 2006-2016 by respective authors (see below). All rights reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.red5.server.stream;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.ref.WeakReference;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.StandardMBean;

import org.apache.commons.lang3.StringUtils;
import org.apache.mina.core.buffer.IoBuffer;
import org.red5.codec.IAudioStreamCodec;
import org.red5.codec.IStreamCodecInfo;
import org.red5.codec.IVideoStreamCodec;
import org.red5.codec.StreamCodecInfo;
import org.red5.io.amf.Output;
import org.red5.server.api.IConnection;
import org.red5.server.api.Red5;
import org.red5.server.api.event.IEvent;
import org.red5.server.api.event.IEventDispatcher;
import org.red5.server.api.event.IEventListener;
import org.red5.server.api.scope.IScope;
import org.red5.server.api.statistics.IClientBroadcastStreamStatistics;
import org.red5.server.api.statistics.support.StatisticsCounter;
import org.red5.server.api.stream.IClientBroadcastStream;
import org.red5.server.api.stream.IStreamAwareScopeHandler;
import org.red5.server.api.stream.IStreamCapableConnection;
import org.red5.server.api.stream.IStreamListener;
import org.red5.server.api.stream.IStreamPacket;
import org.red5.server.api.stream.StreamState;
import org.red5.server.jmx.mxbeans.ClientBroadcastStreamMXBean;
import org.red5.server.messaging.IConsumer;
import org.red5.server.messaging.IFilter;
import org.red5.server.messaging.IMessage;
import org.red5.server.messaging.IMessageComponent;
import org.red5.server.messaging.IMessageOutput;
import org.red5.server.messaging.IPipe;
import org.red5.server.messaging.IPipeConnectionListener;
import org.red5.server.messaging.IProvider;
import org.red5.server.messaging.IPushableConsumer;
import org.red5.server.messaging.OOBControlMessage;
import org.red5.server.messaging.PipeConnectionEvent;
import org.red5.server.net.rtmp.event.AudioData;
import org.red5.server.net.rtmp.event.IRTMPEvent;
import org.red5.server.net.rtmp.event.Invoke;
import org.red5.server.net.rtmp.event.Notify;
import org.red5.server.net.rtmp.event.VideoData;
import org.red5.server.net.rtmp.message.Constants;
import org.red5.server.net.rtmp.message.Header;
import org.red5.server.net.rtmp.status.Status;
import org.red5.server.net.rtmp.status.StatusCodes;
import org.red5.server.stream.message.RTMPMessage;
import org.red5.server.stream.message.StatusMessage;
import org.springframework.jmx.export.annotation.ManagedResource;

import lombok.extern.slf4j.Slf4j;

/**
 * Represents live stream broadcasted from client. As Flash Media Server, Red5 supports recording mode for live streams, that is,
 * broadcasted stream has broadcast mode. It can be either "live" or "record" and latter causes server-side application to record
 * broadcasted stream.
 *	表示从客户端广播的实时流。作为Flash媒体服务器，Red5支持实时流的录制模式，
 *	即广播流具有广播模式。它可以是“活动”或“记录”，后者会导致服务器端应用程序记录广播流。
 * Note that recorded streams are recorded as FLV files.
 *
 * This type of stream uses two different pipes for live streaming and recording.
 * 
 * @author The Red5 Project
 * @author Steven Gong
 * @author Paul Gregoire (mondain@gmail.com)
 * @author Vladimir Hmelyoff (vlhm@splitmedialabs.com)
 */
@Slf4j
@ManagedResource(objectName = "org.red5.server:type=ClientBroadcastStream", description = "ClientBroadcastStream")
public class ClientBroadcastStream extends AbstractClientStream implements IClientBroadcastStream, IFilter, IPushableConsumer, IPipeConnectionListener, IEventDispatcher, IClientBroadcastStreamStatistics, ClientBroadcastStreamMXBean {
 
    /**
     * Whether or not to automatically record the associated stream.
     */
    protected boolean automaticRecording;

    /**
     * Total number of bytes received.
     */
    protected long bytesReceived;

    /**
     * Is there need to check video codec?
     */
    protected boolean checkVideoCodec;

    /**
     * Is there need to check audio codec?
     */
    protected boolean checkAudioCodec;

    /**
     * Data is sent by chunks, each of them has size
     */
    protected int chunkSize;

    /**
     * Is this stream still active?
     */
    protected AtomicBoolean closed = new AtomicBoolean(false);

    /**
     * Output endpoint that providers use
     */
    protected transient IMessageOutput connMsgOut;

    /**
     * Stores timestamp of first packet
     */
    protected long firstPacketTime = -1;

    /**
     * Pipe for live streaming
     */
    protected transient IPipe livePipe;

    /**
     * Stream published name
     */
    protected String publishedName;

    /**
     * Streaming parameters
     */
    protected Map<String, String> parameters;

    /**
     * Is there need to send start notification?
     */
    protected boolean sendStartNotification = true;

    /**
     * Stores statistics about subscribers.
     */
    private transient StatisticsCounter subscriberStats = new StatisticsCounter();

    /**
     * Listeners to get notified about received packets.
     */
    protected transient Set<IStreamListener> listeners = new CopyOnWriteArraySet<IStreamListener>();

    /**
     * Recording com.listener
     */
    protected transient WeakReference<IRecordingListener> recordingListener;

    protected long latestTimeStamp = -1;

    /**
     * Whether or not to register with JMX.
     */
    private boolean registerJMX = true;

    /**
     * Check and send notification if necessary
     */
    private void checkSendNotifications(IEvent event) {
        IEventListener source = event.getSource();
        sendStartNotifications(source);
    }

    /**
     * Closes stream, unsubscribes provides, sends stoppage notifications and broadcast close notification.
     */
    public void close() {
        //log.debug("Stream close: {}", publishedName);
        if (closed.compareAndSet(false, true)) {
            if (livePipe != null) {
                livePipe.unsubscribe((IProvider) this);
            }
            // if we have a recording com.listener, inform that this stream is done
            if (recordingListener != null) {
                sendRecordStopNotify();
                notifyRecordingStop();
                // inform the com.listener to finish and close
                recordingListener.get().stop();
            }
            sendPublishStopNotify();
            // can we send the client something to make sure he stops sending data?
            if (connMsgOut != null) {
                connMsgOut.unsubscribe(this);
            }
            notifyBroadcastClose();
            // clear the com.listener after all the notifications have been sent
            if (recordingListener != null) {
                recordingListener.clear();
            }
            // clear listeners
            if (!listeners.isEmpty()) {
                listeners.clear();
            }
            // deregister with jmx
            unregisterJMX();
            setState(StreamState.CLOSED);
        }
    }

    /**
     * Dispatches event
     */
    public void dispatchEvent(IEvent event) {
        if (event instanceof IRTMPEvent && !closed.get()) {
            switch (event.getType()) {
                case STREAM_CONTROL:
                case STREAM_DATA:
                    // create the event
                    IRTMPEvent rtmpEvent;
                    try {
                        rtmpEvent = (IRTMPEvent) event;
                    } catch (ClassCastException e) {
                        log.error("Class cast exception in event dispatch", e);
                        return;
                    }
                    int eventTime = rtmpEvent.getTimestamp();
                    // verify and / or set source type
                    if (rtmpEvent.getSourceType() != Constants.SOURCE_TYPE_LIVE) {
                        rtmpEvent.setSourceType(Constants.SOURCE_TYPE_LIVE);
                    }
                    /*
                    if (log.isTraceEnabled()) {
                        // If this is first packet save its timestamp; expect it is
                        // absolute? no matter: it's never used!
                        if (firstPacketTime == -1) {
                            firstPacketTime = rtmpEvent.getTimestamp();
                            log.trace(String.format("CBS=@%08x: rtmpEvent=%s creation=%s firstPacketTime=%d", System.identityHashCode(this), rtmpEvent.getClass().getSimpleName(), creationTime, firstPacketTime));
                        } else {
                            log.trace(String.format("CBS=@%08x: rtmpEvent=%s creation=%s firstPacketTime=%d timestamp=%d", System.identityHashCode(this), rtmpEvent.getClass().getSimpleName(), creationTime, firstPacketTime, eventTime));
                        }
                    }
                    */
                    //get the buffer only once per call
                    IoBuffer buf = null;
                    if (rtmpEvent instanceof IStreamData && (buf = ((IStreamData<?>) rtmpEvent).getData()) != null) {
                        bytesReceived += buf.limit();
                    }
                    // get stream codec
                    IStreamCodecInfo codecInfo = getCodecInfo();
                    StreamCodecInfo info = null;
                    if (codecInfo instanceof StreamCodecInfo) {
                        info = (StreamCodecInfo) codecInfo;
                    }
                    //log.trace("Stream codec info: {}", info);
                    if (rtmpEvent instanceof AudioData) {
                        //log.trace("Audio: {}", eventTime);
                        IAudioStreamCodec audioStreamCodec = null;
                        if (checkAudioCodec) {
                            // dont try to read codec info from 0 length audio packets
                            if (buf.limit() > 0) {
                                audioStreamCodec = AudioCodecFactory.getAudioCodec(buf);
                                if (info != null) {
                                    info.setAudioCodec(audioStreamCodec);
                                }
                                checkAudioCodec = false;
                            }
                        } else if (codecInfo != null) {
                            audioStreamCodec = codecInfo.getAudioCodec();
                        }
                        if (audioStreamCodec != null) {
                            audioStreamCodec.addData(buf);
                        }
                        if (info != null) {
                            info.setHasAudio(true);
                        }
                    } else if (rtmpEvent instanceof VideoData) {
                        //log.trace("Video: {}", eventTime);
                        IVideoStreamCodec videoStreamCodec = null;
                        if (checkVideoCodec) {
                            videoStreamCodec = VideoCodecFactory.getVideoCodec(buf);
                            if (info != null) {
                                info.setVideoCodec(videoStreamCodec);
                            }
                            checkVideoCodec = false;
                        } else if (codecInfo != null) {
                            videoStreamCodec = codecInfo.getVideoCodec();
                        }
                        if (videoStreamCodec != null) {
                            videoStreamCodec.addData(buf, eventTime);
                        }
                        if (info != null) {
                            info.setHasVideo(true);
                        }
                    } else if (rtmpEvent instanceof Invoke) {
                        //Invoke invokeEvent = (Invoke) rtmpEvent;
                        //log.debug("Invoke action: {}", invokeEvent.getAction());
                        // event / stream listeners will not be notified of invokes
                        return;
                    } else if (rtmpEvent instanceof Notify) {
                        Notify notifyEvent = (Notify) rtmpEvent;
                        String action = notifyEvent.getAction();
                        //if (log.isDebugEnabled()) {
                        //log.debug("Notify action: {}", action);
                        //}
                        if ("onMetaData".equals(action)) {
                            // store the metadata
                            try {
                                //log.debug("Setting metadata");
                                setMetaData(notifyEvent.duplicate());
                            } catch (Exception e) {
                                log.warn("Metadata could not be duplicated for this stream", e);
                            }
                        }
                    }
                    // update last event time
                    if (eventTime > latestTimeStamp) {
                        latestTimeStamp = eventTime;
                    }
                    // notify event listeners
                    checkSendNotifications(event);
                    // note this timestamp is set in event/body but not in the associated header
                    try {
                        // route to live
                        if (livePipe != null) {
                            // create new RTMP message, initialize it and push through pipe
                            RTMPMessage msg = RTMPMessage.build(rtmpEvent, eventTime);
                            livePipe.pushMessage(msg);
                        } else if (log.isDebugEnabled()) {
                            log.debug("Live pipe was null, message was not pushed");
                        }
                    } catch (IOException err) {
                        stop();
                    }
                    // notify listeners about received packet
                    if (rtmpEvent instanceof IStreamPacket) {
                        for (IStreamListener listener : getStreamListeners()) {
                            try {
                                listener.packetReceived(this, (IStreamPacket) rtmpEvent);
                            } catch (Exception e) {
                                log.error("Error while notifying com.listener {}", listener, e);
                                if (listener instanceof RecordingListener) {
                                    sendRecordFailedNotify(e.getMessage());
                                }
                            }
                        }
                    }
                    break;
                default:
                    // ignored event
                    //log.debug("Ignoring event: {}", event.getType());
            }
        } else {
            log.debug("Event was of wrong type or stream is closed ({})", closed);
        }
    } 
    /**
     * Setter for stream published name
     * */
    public void setPublishedName(String name) {
        //log.debug("setPublishedName: {}", name);
        // a publish name of "false" is a special case, used when stopping a stream
        if (StringUtils.isNotEmpty(name) && !"false".equals(name)) {
            this.publishedName = name;
            registerJMX();
        }
    }

    /**
     * Getter for published name
     */
    public String getPublishedName() {
        return publishedName;
    } 
    
    public void setParameters(Map<String, String> params) {
        this.parameters = params;
    }

    
    public Map<String, String> getParameters() {
        return parameters;
    }

    
    public String getSaveFilename() {
        if (recordingListener != null) {
            return recordingListener.get().getFileName();
        }
        return null;
    }

    
    public IClientBroadcastStreamStatistics getStatistics() {
        return this;
    }

    
    public int getTotalSubscribers() {
        return subscriberStats.getTotal();
    }

    /**
     * @return the automaticRecording
     */
    public boolean isAutomaticRecording() {
        return automaticRecording;
    }

    /**
     * @param automaticRecording
     *            the automaticRecording to set
     */
    public void setAutomaticRecording(boolean automaticRecording) {
        this.automaticRecording = automaticRecording;
    }

    /**
     * @param registerJMX
     *            the registerJMX to set
     */
    public void setRegisterJMX(boolean registerJMX) {
        this.registerJMX = registerJMX;
    }

    /**
     * Notifies handler on stream broadcast close
     */
    private void notifyBroadcastClose() {
        final IStreamAwareScopeHandler handler = getStreamAwareHandler();
        if (handler != null) {
            try {
                handler.streamBroadcastClose(this);
            } catch (Throwable t) {
                log.error("Error in notifyBroadcastClose", t);
            }
        }
    }

    /**
     * Notifies handler on stream recording stop
     */
    private void notifyRecordingStop() {
        IStreamAwareScopeHandler handler = getStreamAwareHandler();
        if (handler != null) {
            try {
                handler.streamRecordStop(this);
            } catch (Throwable t) {
                log.error("Error in notifyRecordingStop", t);
            }
        }
    }

    /**
     * Notifies handler on stream broadcast start
     */
    private void notifyBroadcastStart() {
        IStreamAwareScopeHandler handler = getStreamAwareHandler();
        if (handler != null) {
            try {
                handler.streamBroadcastStart(this);
            } catch (Throwable t) {
                log.error("Error in notifyBroadcastStart", t);
            }
        }
        // send metadata for creation and start dates
        IoBuffer buf = IoBuffer.allocate(256);
        buf.setAutoExpand(true);
        Output out = new Output(buf);
        out.writeString("onMetaData");
        Map<Object, Object> params = new HashMap<>();
        Calendar cal = GregorianCalendar.getInstance();
        cal.setTimeInMillis(creationTime);
        params.put("creationdate", ZonedDateTime.ofInstant(cal.toInstant(), ZoneId.of("UTC")).format(DateTimeFormatter.ISO_INSTANT));
        cal.setTimeInMillis(startTime);
        params.put("startdate", ZonedDateTime.ofInstant(cal.toInstant(), ZoneId.of("UTC")).format(DateTimeFormatter.ISO_INSTANT));
        if (log.isDebugEnabled()) {
            log.debug("Params: {}", params);
        }
        out.writeMap(params);
        buf.flip();
        Notify notify = new Notify(buf);
        notify.setAction("onMetaData");
        notify.setHeader(new Header());
        notify.getHeader().setDataType(Notify.TYPE_STREAM_METADATA);
        notify.getHeader().setStreamId(0);
        notify.setTimestamp(0);
        dispatchEvent(notify);
    }

    /**
     * Send OOB control message with chunk size
     */
    private void notifyChunkSize() {
        if (chunkSize > 0 && livePipe != null) {
            OOBControlMessage setChunkSize = new OOBControlMessage();
            setChunkSize.setTarget("ConnectionConsumer");
            setChunkSize.setServiceName("chunkSize");
            if (setChunkSize.getServiceParamMap() == null) {
                setChunkSize.setServiceParamMap(new HashMap<String, Object>());
            }
            setChunkSize.getServiceParamMap().put("chunkSize", chunkSize);
            livePipe.sendOOBControlMessage(getProvider(), setChunkSize);
        }
    }

    /**
     * Out-of-band control message handler
     */
    public void onOOBControlMessage(IMessageComponent source, IPipe pipe, OOBControlMessage oobCtrlMsg) {
        String target = oobCtrlMsg.getTarget();
        if ("ClientBroadcastStream".equals(target)) {
            String serviceName = oobCtrlMsg.getServiceName();
            if ("chunkSize".equals(serviceName)) {
                chunkSize = (Integer) oobCtrlMsg.getServiceParamMap().get("chunkSize");
                notifyChunkSize();
            } else {
                log.debug("Unhandled OOB control message for service: {}", serviceName);
            }
        } else {
            log.debug("Unhandled OOB control message to target: {}", target);
        }
    }

    /**
     * Pipe connection event handler
     */
    @SuppressWarnings("unused")
    public void onPipeConnectionEvent(PipeConnectionEvent event) {
        switch (event.getType()) {
            case PROVIDER_CONNECT_PUSH:
                //log.debug("Provider connect");
                if (event.getProvider() == this && event.getSource() != connMsgOut && (event.getParamMap() == null || !event.getParamMap().containsKey("record"))) {
                    livePipe = (IPipe) event.getSource();
                    //log.debug("Provider: {}", livePipe.getClass().getName());
                    for (IConsumer consumer : livePipe.getConsumers()) {
                        subscriberStats.increment();
                    }
                }
                break;
            case PROVIDER_DISCONNECT:
                //log.debug("Provider disconnect");
                //if (log.isDebugEnabled() && livePipe != null) {
                //log.debug("Provider: {}", livePipe.getClass().getName());
                //}
                if (livePipe == event.getSource()) {
                    livePipe = null;
                }
                break;
            case CONSUMER_CONNECT_PUSH:
                log.debug("Consumer connect");
                IPipe pipe = (IPipe) event.getSource();
                //if (log.isDebugEnabled() && pipe != null) {
                log.debug("Consumer: {}", pipe.getClass().getName());
                //}
                if (livePipe == pipe) {
                    notifyChunkSize();
                }
                subscriberStats.increment();
                break;
            case CONSUMER_DISCONNECT:
                //log.debug("Consumer disconnect: {}", event.getSource().getClass().getName());
                subscriberStats.decrement();
                break;
            default:
        }
    }

    /**
     * Currently not implemented 
     */
    public void pushMessage(IPipe pipe, IMessage message) {}

    /**
     * Save broadcasted stream. 
     */
    public void saveAs(String name, boolean isAppend) throws IOException {
        //log.debug("SaveAs - name: {} append: {}", name, isAppend);
        // 获取连接以检查客户端是否仍在流式处理
        IStreamCapableConnection conn = getConnection();
        if (conn == null) {
            throw new IOException("Stream is no longer connected");
        }
        // 通过此入口点一次一个录音侦听器
        if (recordingListener == null) {
            //paul:重新访问此部分以允许实现自定义irecordingListener
            //IRecordingListener com.listener = (IRecordingListener) ScopeUtils.getScopeService(conn.getScope(), IRecordingListener.class, RecordingListener.class, false);
            //创建录制侦听器
            IRecordingListener listener = new RecordingListener();  
            if (listener.init(conn, name, isAppend)) {
                //获取流的解码器信息（如果存在）
                IStreamCodecInfo codecInfo = getCodecInfo(); 
                if (codecInfo instanceof StreamCodecInfo) {
                    StreamCodecInfo info = (StreamCodecInfo) codecInfo;
                    IVideoStreamCodec videoCodec = info.getVideoCodec(); 
                    if (videoCodec != null) {
                        //检查要发送的解码器配置
                        IoBuffer config = videoCodec.getDecoderConfiguration();
                        if (config != null) { 
                            VideoData videoConf = new VideoData(config.asReadOnlyBuffer());
                            try {
                                listener.getFileConsumer().setVideoDecoderConfiguration(videoConf);
                            } finally {
                                videoConf.release();
                            }
                        }
                    } else {
                        log.debug("Could not initialize stream output, videoCodec is null.");
                    }
                    IAudioStreamCodec audioCodec = info.getAudioCodec(); 
                    if (audioCodec != null) {
                        IoBuffer config = audioCodec.getDecoderConfiguration();
                        if (config != null) { 
                            AudioData audioConf = new AudioData(config.asReadOnlyBuffer());
                            try {
                                listener.getFileConsumer().setAudioDecoderConfiguration(audioConf);
                            } finally {
                                audioConf.release();
                            }
                        }
                    } else {
                        log.debug("No decoder configuration available, audioCodec is null.");
                    }
                }
                // 设为主侦听器
                recordingListener = new WeakReference<IRecordingListener>(listener);
                // 添加为侦听器
                addStreamListener(listener);
                // 启动侦听器线程
                listener.start();
            } else {
                log.warn("Recording com.listener failed to initialize for stream: {}", name);
            }
        } else {
            log.debug("Recording com.listener already exists for stream: {} auto record enabled: {}", name, automaticRecording);
        }
    }

    /**
     * Sends publish start notifications
     */
    private void sendPublishStartNotify() {
        Status publishStatus = new Status(StatusCodes.NS_PUBLISH_START);
        publishStatus.setClientid(getStreamId());
        publishStatus.setDetails(getPublishedName());

        StatusMessage startMsg = new StatusMessage();
        startMsg.setBody(publishStatus);
        pushMessage(startMsg);
        setState(StreamState.PUBLISHING);
    }

    /**
     * Sends publish stop notifications
     */
    private void sendPublishStopNotify() {
        Status stopStatus = new Status(StatusCodes.NS_UNPUBLISHED_SUCCESS);
        stopStatus.setClientid(getStreamId());
        stopStatus.setDetails(getPublishedName());

        StatusMessage stopMsg = new StatusMessage();
        stopMsg.setBody(stopStatus);
        pushMessage(stopMsg);
        setState(StreamState.STOPPED);
    }

    /**
     * Sends record failed notifications
     */
    private void sendRecordFailedNotify(String reason) {
        Status failedStatus = new Status(StatusCodes.NS_RECORD_FAILED);
        failedStatus.setLevel(Status.ERROR);
        failedStatus.setClientid(getStreamId());
        failedStatus.setDetails(getPublishedName());
        failedStatus.setDesciption(reason);

        StatusMessage failedMsg = new StatusMessage();
        failedMsg.setBody(failedStatus);
        pushMessage(failedMsg);
    }

    /**
     * Sends record start notifications
     */
    private void sendRecordStartNotify() {
        Status recordStatus = new Status(StatusCodes.NS_RECORD_START);
        recordStatus.setClientid(getStreamId());
        recordStatus.setDetails(getPublishedName());

        StatusMessage startMsg = new StatusMessage();
        startMsg.setBody(recordStatus);
        pushMessage(startMsg);
    }

    /**
     * Sends record stop notifications
     */
    private void sendRecordStopNotify() {
        Status stopStatus = new Status(StatusCodes.NS_RECORD_STOP);
        stopStatus.setClientid(getStreamId());
        stopStatus.setDetails(getPublishedName());

        StatusMessage stopMsg = new StatusMessage();
        stopMsg.setBody(stopStatus);
        pushMessage(stopMsg);
    }

    /**
     * Pushes a message out to a consumer. 
     */
    protected void pushMessage(StatusMessage msg) {
        if (connMsgOut != null) {
            try {
                connMsgOut.pushMessage(msg);
            } catch (IOException err) {
                log.error("Error while pushing message: {}", msg, err);
            }
        } else {
            log.warn("Consumer message output is null");
        }
    }

    private void sendStartNotifications(IEventListener source) {
        if (sendStartNotification) {
            // notify handler that stream starts recording/publishing
            sendStartNotification = false;
            if (source instanceof IConnection) {
                IScope scope = ((IConnection) source).getScope();
                if (scope.hasHandler()) {
                    final Object handler = scope.getHandler();
                    if (handler instanceof IStreamAwareScopeHandler) {
                        if (recordingListener != null && recordingListener.get().isRecording()) {
                            // callback for record start
                            ((IStreamAwareScopeHandler) handler).streamRecordStart(this);
                        } else {
                            // delete any previously recorded versions of this now "live" stream per
                            // http://livedocs.adobe.com/flashmediaserver/3.0/hpdocs/help.html?content=00000186.html
                            //                            try {
                            //                                File file = getRecordFile(scope, publishedName);
                            //                                if (file != null && file.exists()) {
                            //                                    if (!file.delete()) {
                            //                                        log.debug("File was not deleted: {}", file.getAbsoluteFile());
                            //                                    }
                            //                                }
                            //                            } catch (Exception e) {
                            //                                log.warn("Exception removing previously recorded file", e);
                            //                            }
                            // callback for publish start
                            ((IStreamAwareScopeHandler) handler).streamPublishStart(this);
                        }
                    }
                }
            }
            // send start notifications
            sendPublishStartNotify();
            if (recordingListener != null && recordingListener.get().isRecording()) {
                sendRecordStartNotify();
            }
            notifyBroadcastStart();
        }
    }

    /**
     * Starts stream, creates pipes, connects
     */
    public void start() {
        //log.info("Stream start: {}", publishedName);
        checkVideoCodec = true;
        checkAudioCodec = true;
        firstPacketTime = -1;
        latestTimeStamp = -1;
        bytesReceived = 0;
        IConsumerService consumerManager = (IConsumerService) getScope().getContext().getBean(IConsumerService.KEY);
        connMsgOut = consumerManager.getConsumerOutput(this);
        if (connMsgOut != null && connMsgOut.subscribe(this, null)) {
            // technically this would be a 'start' time
            startTime = System.currentTimeMillis();
        } else {
            log.warn("Subscribe failed");
        }
        setState(StreamState.STARTED);
    }

    
    public void startPublishing() {
        // We send the start messages before the first packet is received.
        // This is required so FME actually starts publishing.
        sendStartNotifications(Red5.getConnectionLocal());
        // force recording if set
        if (automaticRecording) {
            //log.debug("Starting automatic recording of {}", publishedName);
            try {
                saveAs(publishedName, false);
            } catch (Exception e) {
                log.warn("Start of automatic recording failed", e);
            }
        }
    }

    
    public void stop() {
        //log.info("Stream stop: {}", publishedName);
        setState(StreamState.STOPPED);
        stopRecording();
        close();
    }

    /**
     * Stops any currently active recording.
     */
    public void stopRecording() {
        IRecordingListener listener = null;
        if (recordingListener != null && (listener = recordingListener.get()).isRecording()) {
            sendRecordStopNotify();
            notifyRecordingStop();
            // remove the com.listener
            removeStreamListener(listener);
            // stop the recording com.listener
            listener.stop();
            // clear and null-out the thread local
            recordingListener.clear();
            recordingListener = null;
        }
    }

    /**
     * Get the file we'd be recording to based on scope and given name.
     */
    protected File getRecordFile(IScope scope, String name) {
        return RecordingListener.getRecordFile(scope, name);
    }

    protected void registerJMX() {
        if (registerJMX) {
            // register with jmx
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            try {
                ObjectName oName = new ObjectName(String.format("org.red5.server:type=ClientBroadcastStream,scope=%s,publishedName=%s", getScope().getName(), publishedName));
                mbs.registerMBean(new StandardMBean(this, ClientBroadcastStreamMXBean.class, true), oName);
            } catch (InstanceAlreadyExistsException e) {
                log.debug("Instance already registered", e);
            } catch (Exception e) {
                log.warn("Error on jmx registration", e);
            }
        }
    }

    protected void unregisterJMX() {
        if (registerJMX) {
            if (StringUtils.isNotEmpty(publishedName) && !"false".equals(publishedName)) {
                MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
                try {
                    ObjectName oName = new ObjectName(String.format("org.red5.server:type=ClientBroadcastStream,scope=%s,publishedName=%s", getScope().getName(), publishedName));
                    mbs.unregisterMBean(oName);
                } catch (Exception e) {
                    log.warn("Exception unregistering", e);
                }
            }
        }
    }

    public int getActiveSubscribers() {
        return subscriberStats.getCurrent();
    }
    
    public long getBytesReceived() {
        return bytesReceived;
    }
    
    public int getCurrentTimestamp() {
        return (int) latestTimeStamp;
    } 
    
    public int getMaxSubscribers() {
        return subscriberStats.getMax();
    }
 
    public IProvider getProvider() {
        return this;
    }

    public boolean isRecording() {
        return recordingListener != null && recordingListener.get().isRecording();
    }
    
    public void addStreamListener(IStreamListener listener) {
        listeners.add(listener);
    }
    
    public Collection<IStreamListener> getStreamListeners() {
        return listeners;
    }
    
    public void removeStreamListener(IStreamListener listener) {
        listeners.remove(listener);
    }
}
