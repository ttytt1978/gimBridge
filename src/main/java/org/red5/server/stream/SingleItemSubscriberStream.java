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

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.red5.server.api.IConnection;
import org.red5.server.api.IContext;
import org.red5.server.api.Red5;
import org.red5.server.api.scheduling.IScheduledJob;
import org.red5.server.api.scheduling.ISchedulingService;
import org.red5.server.api.scope.IScope;
import org.red5.server.api.stream.IPlayItem;
import org.red5.server.api.stream.ISingleItemSubscriberStream;
import org.red5.server.api.stream.IStreamAwareScopeHandler;
import org.red5.server.api.stream.OperationNotSupportedException;
import org.red5.server.api.stream.StreamState;
import org.red5.server.scheduling.QuartzSchedulingService;

import lombok.extern.slf4j.Slf4j;

/**
 * 订阅服务器的单个播放项流
 * 
 * @author Paul Gregoire (mondain@gmail.com)
 */
@Slf4j
public class SingleItemSubscriberStream extends AbstractClientStream implements ISingleItemSubscriberStream {
	/**
     * 	用于提供通知、保持客户端缓冲区已满、清理的服务, etc...
     */
    protected ISchedulingService schedulingService = QuartzSchedulingService.getInstance();
 
    protected Set<String> jobs = new HashSet<String>(1);  
	/**
     * 	检查VOD流中缓冲区不足的间隔（毫秒）。
     */
    protected int bufferCheckInterval = 0; 
    /**
     * 	待处理邮件数
     *<pre>为VOD流生成netstream.play.insufficientbw</pre>消息。
     * 	欠速触发器
     */
    protected int underrunTrigger = 10; 
    /**
     * Timestamp this stream was created.
     */
    protected long creationTime = System.currentTimeMillis();

    private volatile IPlayItem item;
 
    protected PlayEngine engine; 

    public void play() throws IOException {
        try {
            engine.play(item);
        } catch (StreamNotFoundException e){}
    } 
    
    public void pause(int position) {
        try {
            engine.pause(position);
        } catch (IllegalStateException e) {
            log.debug("pause caught an IllegalStateException");
        }
    }
 
    public void resume(int position) {
        try {
            engine.resume(position);
        } catch (IllegalStateException e) {
            log.debug("resume caught an IllegalStateException");
        }
    }
 
    public void stop() {
        try {
            engine.stop();
        } catch (IllegalStateException e) {
            log.debug("stop caught an IllegalStateException");
        }
    }
 
    public void seek(int position) throws OperationNotSupportedException {
        try {
            engine.seek(position);
        } catch (IllegalStateException e) {
            log.debug("seek caught an IllegalStateException");
        }
    }

    public boolean isPaused() {
        return state.get() == StreamState.PAUSED;
    }
 
    public void receiveVideo(boolean receive) {
        boolean receiveVideo = engine.receiveVideo(receive);
        if (!receiveVideo && receive) {
            //已重新启用视频
            seekToCurrentPlayback();
        }
    }
 
    public void receiveAudio(boolean receive) {
        //检查引擎当前是否接收到音频，返回上一个值
        boolean receiveAudio = engine.receiveAudio(receive);
        if (receiveAudio && !receive) {
            //发送一个空白音频包以重置播放机
            engine.sendBlankAudio(true);
        } else if (!receiveAudio && receive) {
            //do a seek	
            seekToCurrentPlayback();
        }
    }

    /**
     * Creates a play engine based on current services (scheduling service, consumer service, and provider service). This method is useful during unit testing.
     */
    PlayEngine createEngine(ISchedulingService schedulingService, IConsumerService consumerService, IProviderService providerService) {
        engine = new PlayEngine.Builder(this, schedulingService, consumerService, providerService).build();
        return engine;
    }

    
    public void start() { 
        if (engine == null) {
            IScope scope = getScope();
            if (scope != null) {
                IContext ctx = scope.getContext();
//                if (ctx.hasBean(ISchedulingService.BEAN_NAME)) {
//                    schedulingService = (ISchedulingService) ctx.getBean(ISchedulingService.BEAN_NAME);
//                } else {
//                    //try the parent
//                    schedulingService = (ISchedulingService) scope.getParent().getContext().getBean(ISchedulingService.BEAN_NAME);
//                }
                schedulingService = QuartzSchedulingService.getInstance();
                IConsumerService consumerService = null;
                if (ctx.hasBean(IConsumerService.KEY)) {
                    consumerService = (IConsumerService) ctx.getBean(IConsumerService.KEY);
                } else {
                    //try the parent
                    consumerService = (IConsumerService) scope.getParent().getContext().getBean(IConsumerService.KEY);
                }
                IProviderService providerService = null;
                if (ctx.hasBean(IProviderService.BEAN_NAME)) {
                    providerService = (IProviderService) ctx.getBean(IProviderService.BEAN_NAME);
                } else {
                    //try the parent
                    providerService = (IProviderService) scope.getParent().getContext().getBean(IProviderService.BEAN_NAME);
                }
                engine = new PlayEngine.Builder(this, schedulingService, consumerService, providerService).build();
            } else {
                log.info("Scope was null on start");
            }
        }
        
        engine.setBufferCheckInterval(bufferCheckInterval);
         
        engine.setUnderrunTrigger(underrunTrigger);
        
        engine.start();
         
        onChange(StreamState.STARTED);
    }

    public void close() {
    	if(engine != null){
    		engine.close();	
    	} 
        onChange(StreamState.CLOSED);
        // clear jobs
        if (schedulingService != null && !jobs.isEmpty()) {
            for (String jobName : jobs) {
                schedulingService.removeScheduledJob(jobName);
            }
            jobs.clear();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void onChange(final StreamState state, final Object... changed) {
        Notifier notifier = null;
        IStreamAwareScopeHandler handler = getStreamAwareHandler();
        switch (state) {
            case SEEK:
                //notifies subscribers on seek
                if (handler != null) {
                    notifier = new Notifier(this, handler) {
                        public void execute(ISchedulingService service) {
                            //make sure those notified have the correct connection
                            Red5.setConnectionLocal(conn);
                            //get item being played
                            IPlayItem item = (IPlayItem) changed[0];
                            //seek position
                            int position = (Integer) changed[1];
                            try {
                                handler.streamPlayItemSeek(stream, item, position);
                            } catch (Throwable t) {
                                log.error("error notify streamPlayItemSeek", t);
                            }
                            // clear thread local reference
                            Red5.setConnectionLocal(null);
                        }
                    };
                }
                break;
            case PAUSED:
                //set the paused state
                this.setState(StreamState.PAUSED);
                //notifies subscribers on pause
                if (handler != null) {
                    notifier = new Notifier(this, handler) {
                        public void execute(ISchedulingService service) {
                            //make sure those notified have the correct connection
                            Red5.setConnectionLocal(conn);
                            //get item being played
                            IPlayItem item = (IPlayItem) changed[0];
                            //playback position
                            int position = (Integer) changed[1];
                            try {
                                handler.streamPlayItemPause(stream, item, position);
                            } catch (Throwable t) {
                                log.error("error notify streamPlayItemPause", t);
                            }
                            // clear thread local reference
                            Red5.setConnectionLocal(null);
                        }
                    };
                }
                break;
            case RESUMED:
                //resume playing
                this.setState(StreamState.PLAYING);
                //notifies subscribers on resume
                if (handler != null) {
                    notifier = new Notifier(this, handler) {
                        public void execute(ISchedulingService service) {
                            //make sure those notified have the correct connection
                            Red5.setConnectionLocal(conn);
                            //get item being played
                            IPlayItem item = (IPlayItem) changed[0];
                            //playback position
                            int position = (Integer) changed[1];
                            try {
                                handler.streamPlayItemResume(stream, item, position);
                            } catch (Throwable t) {
                                log.error("error notify streamPlayItemResume", t);
                            }
                            // clear thread local reference
                            Red5.setConnectionLocal(null);
                        }
                    };
                }
                break;
            case PLAYING:
                //notifies subscribers on play
                if (handler != null) {
                    notifier = new Notifier(this, handler) {
                        public void execute(ISchedulingService service) {
                            //make sure those notified have the correct connection
                            Red5.setConnectionLocal(conn);
                            //get item being played
                            IPlayItem item = (IPlayItem) changed[0];
                            //is it a live broadcast
                            boolean isLive = (Boolean) changed[1];
                            try {
                                handler.streamPlayItemPlay(stream, item, isLive);
                            } catch (Throwable t) {
                                log.error("error notify streamPlayItemPlay", t);
                            }
                            // clear thread local reference
                            Red5.setConnectionLocal(null);
                        }
                    };
                }
                break;
            case CLOSED:
                //notifies subscribers on close
                if (handler != null) {
                    notifier = new Notifier(this, handler) {
                        public void execute(ISchedulingService service) {
                            //make sure those notified have the correct connection
                            Red5.setConnectionLocal(conn);
                            try {
                                handler.streamSubscriberClose(stream);
                            } catch (Throwable t) {
                                log.error("error notify streamSubscriberClose", t);
                            }
                            // clear thread local reference
                            Red5.setConnectionLocal(null);
                        }
                    };
                }
                break;
            case STARTED:
                //notifies subscribers on start
                if (handler != null) {
                    notifier = new Notifier(this, handler) {
                        public void execute(ISchedulingService service) {
                            //make sure those notified have the correct connection
                            Red5.setConnectionLocal(conn);
                            try {
                                handler.streamSubscriberStart(stream);
                            } catch (Throwable t) {
                                log.error("error notify streamSubscriberStart", t);
                            }
                            // clear thread local reference
                            Red5.setConnectionLocal(null);
                        }
                    };
                }
                break;
            case STOPPED:
                //set the stopped state
                this.setState(StreamState.STOPPED);
                //notifies subscribers on stop
                if (handler != null) {
                    notifier = new Notifier(this, handler) {
                        public void execute(ISchedulingService service) {
                            //make sure those notified have the correct connection
                            Red5.setConnectionLocal(conn);
                            //get the item that was stopped
                            IPlayItem item = (IPlayItem) changed[0];
                            try {
                                handler.streamPlayItemStop(stream, item);
                            } catch (Throwable t) {
                                log.error("error notify streamPlaylistItemStop", t);
                            }
                            // clear thread local reference
                            Red5.setConnectionLocal(null);
                        }
                    };
                }
                break;
            case END:
                //notified by the play engine when the current item reaches the end
                break;
            default:
                //there is no "default" handling
        }
        if (notifier != null) {
           // notifier.setConnection(Red5.getConnectionLocal());
            scheduleOnceJob(notifier);
        }
    }

    /**
     * Seek to current position to restart playback with audio and/or video.
     */
    private void seekToCurrentPlayback() {
        if (engine.isPullMode()) {
            try {
                // figure out if this is the correct position to seek to
                final long delta = System.currentTimeMillis() - engine.getPlaybackStart();
                engine.seek((int) delta);
            } catch (OperationNotSupportedException err) {
                // Ignore error, should not happen for pullMode engines
            }
        }
    }
 
    public String scheduleOnceJob(IScheduledJob job) {
        String jobName = schedulingService.addScheduledOnceJob(10, job);
        return jobName;
    }
 
    public String scheduleWithFixedDelay(IScheduledJob job, int interval) {
        String jobName = schedulingService.addScheduledJob(interval, job);
        jobs.add(jobName);
        return jobName;
    }
 
    public void cancelJob(String jobName) {
        schedulingService.removeScheduledJob(jobName);
    }

    /**
     * Handles notifications in a separate thread.
     */
    public class Notifier implements IScheduledJob {

        ISingleItemSubscriberStream stream;

        IStreamAwareScopeHandler handler;

        IConnection conn;

        public Notifier(ISingleItemSubscriberStream stream, IStreamAwareScopeHandler handler) {
            log.trace("Notifier - stream: {} handler: {}", stream, handler);
            this.stream = stream;
            this.handler = handler;
            this.conn = stream.getConnection();
        }

        public void setConnection(IConnection conn) {
            this.conn = conn;
        }

        public void execute(ISchedulingService service) {
        }

    }
    /**
     * Set interval to check for buffer underruns. Set to
     *  <pre> 0 </pre> to disable. 
     * @param bufferCheckInterval  interval in ms
     */
    public void setBufferCheckInterval(int bufferCheckInterval) {
        this.bufferCheckInterval = bufferCheckInterval;
    }

    /**
     * Set maximum number of pending messages at which a
     *  <pre NetStream.Play.InsufficientBW </pre>
     * 
     * message will be generated for VOD streams
     * 
     * @param underrunTrigger
     *            the maximum number of pending messages
     */
    public void setUnderrunTrigger(int underrunTrigger) {
        this.underrunTrigger = underrunTrigger;
    }

    public void setPlayItem(IPlayItem item) {
        this.item = item;
    }
}
