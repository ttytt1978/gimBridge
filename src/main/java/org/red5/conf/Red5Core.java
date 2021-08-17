package org.red5.conf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.red5.cache.impl.NoCacheImpl;
import org.red5.demo.Application;
import org.red5.io.CachingFileKeyFrameMetaCache;
import org.red5.io.flv.impl.FLV;
import org.red5.io.flv.impl.FLVReader;
import org.red5.io.mp3.impl.MP3Reader;
import org.red5.server.ClientRegistry;
import org.red5.server.Context;
import org.red5.server.CoreHandler;
import org.red5.server.MappingStrategy;
import org.red5.server.Server;
import org.red5.server.api.scope.IScopeSecurityHandler;
import org.red5.server.api.service.IStreamableFileService;
import org.red5.server.net.http.HTTPApplicationAdapter;
import org.red5.server.net.http.HTTPMinaIoHandler;
import org.red5.server.net.http.HTTPMinaTransport;
import org.red5.server.net.remoting.RemotingClient;
import org.red5.server.net.remoting.codec.RemotingCodecFactory;
import org.red5.server.net.rtmp.RTMPConnManager;
import org.red5.server.net.rtmp.RTMPHandler;
import org.red5.server.net.rtmp.RTMPMinaConnection;
import org.red5.server.net.rtmp.RTMPMinaIoHandler;
import org.red5.server.net.rtmp.RTMPMinaTransport;
import org.red5.server.net.rtmp.codec.RTMPMinaProtocolDecoder;
import org.red5.server.net.rtmp.codec.RTMPMinaProtocolEncoder;
import org.red5.server.net.rtmp.status.StatusObjectService;
import org.red5.server.net.rtmps.RTMPSMinaIoHandler;
import org.red5.server.net.rtmpt.RTMPTConnection;
import org.red5.server.net.rtmpt.RTMPTHandler;
import org.red5.server.net.rtmpt.RTMPTServlet;
import org.red5.server.net.rtmpt.codec.RTMPTCodecFactory;
import org.red5.server.net.rtsp.RTSPMinaIoHandler;
import org.red5.server.net.rtsp.RTSPMinaTransport;
import org.red5.server.scheduling.JDKSchedulingService;
import org.red5.server.scope.GlobalScope;
import org.red5.server.scope.ScopeResolver;
import org.red5.server.scope.ScopeSecurityHandler;
import org.red5.server.scope.WebScope;
import org.red5.server.service.ContextServiceResolver;
import org.red5.server.service.HandlerServiceResolver;
import org.red5.server.service.IServiceResolver;
import org.red5.server.service.ScopeServiceResolver;
import org.red5.server.service.ServiceInvoker;
import org.red5.server.service.flv.impl.FLVService;
import org.red5.server.service.m4a.impl.M4AService;
import org.red5.server.service.mp3.impl.MP3Service;
import org.red5.server.service.mp4.impl.MP4Service;
import org.red5.server.so.SharedObjectService;
import org.red5.server.stream.ClientBroadcastStream;
import org.red5.server.stream.ConsumerService;
import org.red5.server.stream.PlaylistSubscriberStream;
import org.red5.server.stream.ProviderService;
import org.red5.server.stream.RtmpSampleAccess;
import org.red5.server.stream.SingleItemSubscriberStream;
import org.red5.server.stream.StreamService;
import org.red5.server.stream.StreamableFileFactory;
import org.red5.server.stream.consumer.FileConsumer;
import org.red5.server.stream.timeshift.RecordableBroadcastStream;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.MethodInvokingFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration 
public class Red5Core {
	//-------------------------------------------default-----------------------------
	@Order(1)
	@Bean(name="global.clientRegistry")
	@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public ClientRegistry clientRegistry(){
        return new ClientRegistry();
    }
	@Order(2)
	@Bean(name="global.serviceInvoker")
	@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public ServiceInvoker serviceInvoker(){
		ServiceInvoker inv = new ServiceInvoker();
		Set<IServiceResolver> serviceResolvers = new HashSet<IServiceResolver>();
		serviceResolvers.add(new ScopeServiceResolver());
		serviceResolvers.add(new HandlerServiceResolver());
		serviceResolvers.add(new ContextServiceResolver());
		inv.setServiceResolvers(serviceResolvers);
        return inv;
    } 
	@Order(3)
	@Bean(name="global.mappingStrategy")
	@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public MappingStrategy mappingStrategy(){ 
        return new MappingStrategy();
    }
	@Order(4)
	@Bean(name="global.context",autowire=Autowire.BY_TYPE)
	@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public Context context(){ 
        return new Context();
    }
	@Order(5)
	@Bean(name="global.handler",autowire=Autowire.BY_TYPE)
	@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public CoreHandler coreHandler(){ 
        return new CoreHandler();
    }
	@Order(6)
	@Bean(name="global.scope",initMethod="register")
	@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public GlobalScope globalScope() throws Exception{
		GlobalScope gsope = new GlobalScope(); 
		gsope.setServer(server());
		gsope.setContext(context());
		gsope.setHandler(coreHandler());
		gsope.setPersistenceClass("org.red5.server.persistence.FilePersistence");
		Set<IScopeSecurityHandler> securityHandlers = new HashSet<>();
		ScopeSecurityHandler handler = new ScopeSecurityHandler();
		handler.setConnectionAllowed(false);
		securityHandlers.add(handler);
		gsope.setSecurityHandlers(securityHandlers); 
		return gsope;
    }
	@Order(6)
	@Bean(name="red5.scopeResolver")
	@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public ScopeResolver scopeResolver() throws Exception{
		ScopeResolver resolver = new ScopeResolver();
		resolver.setGlobalScope(globalScope());
        return resolver;
    }
	
	
		
	//---------------------------------------common----------------------
	@Order(8)
	@Bean(name="red5.server")
	@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public Server server(){ 
        return Server.getInstance();
    }
	@Order(9)
	@Bean(name="statusObjectService",autowire= Autowire.BY_TYPE) 
	@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public StatusObjectService statusObjectService(){
        return new StatusObjectService();
    }
	@Order(10)
	@Bean(name="minaEncoder") 
	@Scope("prototype")
    public RTMPMinaProtocolEncoder minaEncoder(){
        return new RTMPMinaProtocolEncoder();
    }
	@Order(11)
	@Bean(name="minaDecoder") 
    public RTMPMinaProtocolDecoder minaDecoder(){
        return new RTMPMinaProtocolDecoder();
    }
	@Order(12)
	@Bean(name="rtmptCodecFactory",initMethod = "init",autowire=Autowire.BY_TYPE) 
	@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public RTMPTCodecFactory rtmptCodecFactory(){
		RTMPTCodecFactory factory = new RTMPTCodecFactory(); 
		factory.setBaseTolerance(ExtConfiguration.BASE_TO_LERANCE);
		factory.setDropLiveFuture(ExtConfiguration.DROP_LIVE_FUTURE); 
		return factory;
    }
	@Order(13)
	@Bean(name="remotingCodecFactory",initMethod = "init",autowire= Autowire.BY_TYPE) 
    public RemotingCodecFactory remotingCodecFactory(){
		RemotingCodecFactory factory = new RemotingCodecFactory();  
		return factory;
    }
	@Order(14)
	@Bean(name="streamableFileFactory") 
    public StreamableFileFactory streamableFileFactory(){
		StreamableFileFactory factory = new StreamableFileFactory(); 
		Set<IStreamableFileService> services = new HashSet<>();
		 
		FLVService  flv = new FLVService();
		flv.setGenerateMetadata(true);
		services.add(flv);
		services.add(new MP3Service());
		services.add(new MP4Service());
		services.add(new M4AService()); 
		factory.setServices(services);
		return factory;
    } 
	@Order(15)
	@Bean(name="sharedObjectService") 
    public SharedObjectService sharedObjectService(){
		SharedObjectService factory = new SharedObjectService(); 
		//factory.setMaximumEventsPerUpdate(maxUpdate);
		factory.setPersistenceClassName("org.red5.server.persistence.FilePersistence");
		factory.setScheduler(poolScheduler());
		return factory;
    } 
	@Order(16)
    public ThreadPoolTaskScheduler poolScheduler(){
    	ThreadPoolTaskScheduler factory = new ThreadPoolTaskScheduler(); 
    	factory.setPoolSize(ExtConfiguration.SO_POOL_SIZE);
    	factory.setWaitForTasksToCompleteOnShutdown(false);
    	factory.setDaemon(true);
    	factory.setThreadNamePrefix("SharedObjectScheduler-");
		return factory;
    }
	@Order(17)
    @Bean(name="streamService") 
    public StreamService streamService(){ 
		return new StreamService();
    }
	@Order(18)
    @Bean(name="providerService") 
    public ProviderService providerService(){ 
		return new ProviderService();
    }
	@Order(19)
    @Bean(name="consumerService") 
    public ConsumerService consumerService(){ 
		return new ConsumerService();
    }
	@Order(20)
    @Bean(name="schedulingService") 
    public JDKSchedulingService schedulingService(){ 
		return new JDKSchedulingService();
    }
	@Order(21)
    @Bean(name="remotingClient") 
    public RemotingClient remotingClient(){ 
    	RemotingClient client = new RemotingClient();
    	client.setPoolSize(2);
		return client;
    }
	@Order(22)
    @Bean(name="object.cache") 
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public NoCacheImpl objectCache(){ 
		return NoCacheImpl.getInstance();
    } 
	@Order(23)
    @Bean(name="keyframe.cache") 
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public CachingFileKeyFrameMetaCache keyframeCache(){ 
    	CachingFileKeyFrameMetaCache cache = new CachingFileKeyFrameMetaCache();
    	cache.setMaxCacheEntry(ExtConfiguration.ENTRY_MAX);
		return cache;
    }
	@Order(24)
    @Bean(name="flv.impl") 
    public FLV flvImpl(){ 
    	FLV cache = new FLV();
    	cache.setCache(objectCache());
		return cache;
    }
	@Order(25)
    @Bean(name="flvreader.impl") 
    public FLVReader flvreaderImpl(){ 
    	FLVReader cache = new FLVReader();
    	cache.setKeyFrameCache(keyframeCache());
		return cache;
    }
	@Order(26)
    @Bean(name="mp3reader.impl") 
    public MP3Reader mp3readerImpl(){ 
    	MP3Reader cache = new MP3Reader();
    	cache.setFrameCache(keyframeCache());
		return cache;
    }
	@Order(27)
    @Bean(name="invokingOne")
    public MethodInvokingFactoryBean invoking1(){ 
    	MethodInvokingFactoryBean cache = new MethodInvokingFactoryBean();
    	cache.setStaticMethod("org.red5.io.flv.impl.FLVReader.setBufferType");
		cache.setArguments("auto");
    	return cache;
    }
	@Order(28)
    @Bean(name="invokingTwo") 
    public MethodInvokingFactoryBean invoking2(){ 
    	MethodInvokingFactoryBean cache = new MethodInvokingFactoryBean();
    	cache.setStaticMethod("org.red5.io.flv.impl.FLVReader.setBufferSize");
		cache.setArguments("4096");
    	return cache;
    }
	@Order(29)
    @Bean(name="streamExecutor") 
    public ScheduledThreadPoolExecutor streamExecutor(){ 
    	ScheduledThreadPoolExecutor cache = new ScheduledThreadPoolExecutor(16);
    	cache.setMaximumPoolSize(64);
		return cache;
    } 
	@Order(30)
    @Bean(name="playlistSubscriberStream")
    @Lazy
    @Scope("prototype")
    public PlaylistSubscriberStream playlistSubscriberStream(){ 
    	PlaylistSubscriberStream cache = new PlaylistSubscriberStream();
    	cache.setBufferCheckInterval(ExtConfiguration.INTERVAL);
    	cache.setUnderrunTrigger(ExtConfiguration.TRIGGER); 
		return cache;
    }
	@Order(31)
    @Bean(name="clientBroadcastStream")
    @Lazy
    @Scope("prototype")
    public ClientBroadcastStream clientBroadcastStream(){ 
    	ClientBroadcastStream cache = new ClientBroadcastStream(); 
		return cache;
    } 
	
	//-----------------------------------------------------------core------------------
	@Order(32)
	@Bean(name="rtmpScheduler")
	@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public ThreadPoolTaskScheduler trmpScheduler(){
		ThreadPoolTaskScheduler poolSched = new ThreadPoolTaskScheduler();
		 
		poolSched.setPoolSize(ExtConfiguration.POOL_SIZE);
		poolSched.setDaemon(true);
		poolSched.setWaitForTasksToCompleteOnShutdown(true);
		poolSched.setThreadNamePrefix("RTMPConnectionScheduler-");
		return poolSched;
    }
	@Order(33)
	@Bean(name="messageExecutor")
	@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public ThreadPoolTaskExecutor messageExecutor(){
		ThreadPoolTaskExecutor poolSched = new ThreadPoolTaskExecutor();
		 
		poolSched.setCorePoolSize(ExtConfiguration.CORE_POOL_SIZE);
		poolSched.setMaxPoolSize(ExtConfiguration.MAX_POOL_SIZE);
		poolSched.setQueueCapacity(ExtConfiguration.QUEUE_CAPACITY);
		poolSched.setDaemon(false);
		poolSched.setWaitForTasksToCompleteOnShutdown(true);
		poolSched.setThreadNamePrefix("RTMPConnectionExecutor-");
		return poolSched;
    }
	@Order(34)
	@Bean(name="deadlockGuardScheduler") 
	@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public ThreadPoolTaskScheduler deadlockGuardScheduler(){
		ThreadPoolTaskScheduler poolSched = new ThreadPoolTaskScheduler();
		 
		poolSched.setPoolSize(ExtConfiguration.DEAD_POOL_SIZE);
		poolSched.setDaemon(false);
		poolSched.setWaitForTasksToCompleteOnShutdown(true);
		poolSched.setThreadNamePrefix("DeadlockGuardScheduler-");
		return poolSched;
    }
	@Order(35)
	@Bean(name="rtmpConnManager") 
	@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public RTMPConnManager rtmpConnManager(){ 
		return new RTMPConnManager();
    }
	@Order(36)
	@Bean(name="rtmpHandler") 
	@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public RTMPHandler rtmpHandler(){
		RTMPHandler poolSched = new RTMPHandler(); 
		poolSched.setServer(server()); 
		poolSched.setStatusObjectService(statusObjectService());
		poolSched.setUnvalidatedConnectionAllowed(true);
		return poolSched;
    }
	@Order(37)
	@Bean(name="rtmpMinaIoHandler") 
	@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public RTMPMinaIoHandler rtmpMinaIoHandler(){
		RTMPMinaIoHandler poolSched = new RTMPMinaIoHandler(); 
		poolSched.setHandler(rtmpHandler()); 
		return poolSched;
    }   
	@Order(38)
	@Bean(name="rtmpTransport",initMethod = "start", destroyMethod = "stop") 
    public RTMPMinaTransport rtmpTransport(){
		RTMPMinaTransport poolSched = new RTMPMinaTransport(); 
		poolSched.setIoHandler(rtmpMinaIoHandler());
		List<String> addresses = new ArrayList<String>();
		addresses.add(ExtConfiguration.RTMP_HOST+":"+ExtConfiguration.RTMP_PORT);
		poolSched.setAddresses(addresses);
		poolSched.setIoThreads(ExtConfiguration.IO_THREADS);
		poolSched.setSendBufferSize(ExtConfiguration.BUFFER_SIZE);
		poolSched.setReceiveBufferSize(ExtConfiguration.RECEIVE_BUFFER_SIZE);
		poolSched.setTrafficClass(ExtConfiguration.TRAFFIC_CLASS);
		poolSched.setBacklog(ExtConfiguration.BACK_LOG);
		poolSched.setTcpNoDelay(ExtConfiguration.TCP_NO_DELAY);
		poolSched.setKeepAlive(ExtConfiguration.KEEP_ALIVE);
		poolSched.setThoughputCalcInterval(ExtConfiguration.THOUGH_PUT_CALC_INTERVAL);
		poolSched.setEnableDefaultAcceptor(ExtConfiguration.ENABLED_EFAULT_ACCEPTOR);
		poolSched.setInitialPoolSize(ExtConfiguration.INITIAL_POOL_SIZE);
		poolSched.setMaxPoolSize(ExtConfiguration.MAX_POOL_SIZE);
		poolSched.setMaxProcessorPoolSize(ExtConfiguration.MAX_PROCESSOR_POOL_SIZE);
		poolSched.setExecutorKeepAliveTime(ExtConfiguration.EXECUTOR_KEEP_ALIVE_TIME);
		poolSched.setMinaPollInterval(ExtConfiguration.MINA_POLL_INTERVAL);
		poolSched.setEnableMinaMonitor(ExtConfiguration.ENABLE_MINA_MONITOR);
		poolSched.setEnableMinaLogFilter(ExtConfiguration.ENABLE_MINA_LOG_FILTER);
		return poolSched;
    }
	//http 
	@Order(39)
	@Bean(name="hTTPMinaTransport",initMethod = "start", destroyMethod = "stop") 
    public HTTPMinaTransport hTTPMinaTransport(){
		HTTPMinaTransport poolSched = new HTTPMinaTransport();  
		poolSched.setIoHandler(hTTPMinaIoHandler());
		return poolSched;
    }
	 
	@Order(40)
	@Bean(name="hTTPMinaIoHandler") 
	@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public HTTPMinaIoHandler hTTPMinaIoHandler(){
		HTTPMinaIoHandler poolSched = new HTTPMinaIoHandler();  
		return poolSched;
    }
	//rtsp
	@Order(41)
	@Bean(name="rTSPMinaTransport",initMethod = "start", destroyMethod = "stop") 
    public RTSPMinaTransport rTSPMinaTransport() throws IOException{
		RTSPMinaTransport poolSched = new RTSPMinaTransport(); 
		poolSched.setIoHandler(rTSPMinaIoHandler()); 
		return poolSched;
    }
	@Order(42)
	@Bean(name="rTSPMinaIoHandler") 
	@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public RTSPMinaIoHandler rTSPMinaIoHandler(){
		RTSPMinaIoHandler poolSched = new RTSPMinaIoHandler();  
		return poolSched;
    }
	//Forbidden
	@Order(43)
	@Bean(name="forbidden") 
	@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public String forbidden(){
		String forbidden = "*/WEB-INF/*;";
		return forbidden;
    }
	
	//rtmp 
	@Order(44)
	@Bean(name="rtmpMinaConnection") 
	@Scope("prototype")
    public RTMPMinaConnection rtmpMinaConnection(){
		RTMPMinaConnection poolSched = new RTMPMinaConnection(); 
		poolSched.setScheduler(trmpScheduler());
		poolSched.setExecutor(messageExecutor());
		poolSched.setDeadlockGuardScheduler(deadlockGuardScheduler());
		poolSched.setPingInterval(ExtConfiguration.PING_INTERVAL);
		poolSched.setMaxInactivity(ExtConfiguration.MAX_INACTIVITY);
		poolSched.setMaxHandshakeTimeout(ExtConfiguration.MAX_HANDSHAKE_TIMEOUT);
		poolSched.setDefaultServerBandwidth(ExtConfiguration.DEFAULT_SERVER_BANDWIDTH);
		poolSched.setDefaultClientBandwidth(ExtConfiguration.DEFAULT_CLIENT_BANDWIDTH);
		poolSched.setLimitType(ExtConfiguration.LIMIT_TYPE);
		poolSched.setBandwidthDetection(ExtConfiguration.BANDWIDTH_DETECTION);
		poolSched.setMaxHandlingTimeout(ExtConfiguration.MAX_HANDLING_TIMEOUT);
		poolSched.setExecutorQueueSizeToDropAudioPackets(ExtConfiguration.EXECUTOR_QUEUE_SIZE_TO_DROP_AUDIO_PACKETS);
		poolSched.setChannelsInitalCapacity(ExtConfiguration.CHANNELS_INITAL_CAPACITY);
		poolSched.setChannelsConcurrencyLevel(ExtConfiguration.CHANNELS_CONCURRENCY_LEVEL);
		poolSched.setStreamsInitalCapacity(ExtConfiguration.STREAMS_INITAL_CAPACITY);
		poolSched.setStreamsConcurrencyLevel(ExtConfiguration.STREAMS_CONCURRENCY_LEVEL);
		poolSched.setPendingCallsInitalCapacity(ExtConfiguration.PENDING_CALLS_INITAL_CAPACITY);
		poolSched.setPendingCallsConcurrencyLevel(ExtConfiguration.PENDING_CALLS_CONCURRENCY_LEVEL);
		poolSched.setReservedStreamsInitalCapacity(ExtConfiguration.RESERVED_STREAMS_INITAL_CAPACITY);
		poolSched.setReservedStreamsConcurrencyLevel(ExtConfiguration.RESERVED_STREAMS_CONCURREN_CYLEVEL);
		return poolSched;
    }
	@Order(45)
	@Bean(name="rtmptHandler",autowire=Autowire.BY_TYPE)
	@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public RTMPTHandler rtmptHandler(){
		RTMPTHandler poolSched = new RTMPTHandler(); 
		poolSched.setCodecFactory(rtmptCodecFactory());
		return poolSched;
    } 
	@Order(46)
	@Bean(name="rtmptServlet") 
    public RTMPTServlet rtmptServlet(){
		RTMPTServlet poolSched = new RTMPTServlet(); 
		poolSched.setManager(rtmpConnManager());
		poolSched.setHandler(rtmptHandler());
		poolSched.setTargetResponseSize(ExtConfiguration.TARGET_RESPONSE_SIZE);
		return poolSched;
    }  
	@Order(47)
	@Bean(name="rtmptConnection") 
	@Scope("prototype")
    public RTMPTConnection rtmptConnection(){
		RTMPTConnection poolSched = new RTMPTConnection(); 
		poolSched.setScheduler(trmpScheduler());
		poolSched.setExecutor(messageExecutor());
		poolSched.setDeadlockGuardScheduler(deadlockGuardScheduler());
		poolSched.setPingInterval(ExtConfiguration.PING_INTERVAL);
		poolSched.setMaxInactivity(ExtConfiguration.MAX_INACTIVITY_T);
		poolSched.setMaxHandshakeTimeout(ExtConfiguration.MAX_HANDSHAKE_TIMEOUTT);
		poolSched.setMaxInMessagesPerProcess(ExtConfiguration.MAX_IN_MESSAGES_PERPROCESS);
		poolSched.setMaxQueueOfferTime(ExtConfiguration.MAX_QUEUE_OFFER_TIME);
		poolSched.setMaxQueueOfferAttempts(ExtConfiguration.MAX_QUEUE_OFFER_ATTEMPTS);
		poolSched.setMaxHandlingTimeout(ExtConfiguration.MAX_HANDLING_TIMEOUT);
		poolSched.setExecutorQueueSizeToDropAudioPackets(ExtConfiguration.EXECUTOR_QUEUE_SIZE_TO_DROP_AUDIO_PACKETS);
		poolSched.setChannelsInitalCapacity(ExtConfiguration.CHANNELS_INITAL_CAPACITY);
		poolSched.setChannelsConcurrencyLevel(ExtConfiguration.CHANNELS_CONCURRENCY_LEVEL);
		poolSched.setStreamsInitalCapacity(ExtConfiguration.STREAMS_INITAL_CAPACITY);
		poolSched.setStreamsConcurrencyLevel(ExtConfiguration.STREAMS_CONCURRENCY_LEVEL);
		poolSched.setPendingCallsInitalCapacity(ExtConfiguration.PENDING_CALLS_INITAL_CAPACITY);
		poolSched.setPendingCallsConcurrencyLevel(ExtConfiguration.PENDING_CALLS_CONCURRENCY_LEVEL);
		poolSched.setReservedStreamsInitalCapacity(ExtConfiguration.RESERVED_STREAMS_INITAL_CAPACITY);
		poolSched.setReservedStreamsConcurrencyLevel(ExtConfiguration.RESERVED_STREAMS_CONCURREN_CYLEVEL);
		return poolSched;
    }
	//---------------------------------------rtmps-------------------------
	/*@Order(47)
	@Bean(name="rtmpsMinaIoHandler") 
	@Scope("prototype")
    public RTMPSMinaIoHandler rtmpsMinaIoHandler(){
		RTMPSMinaIoHandler poolSched = new RTMPSMinaIoHandler(); 
		poolSched.setHandler(rtmpHandler());
		poolSched.setKeystorePassword(ExtConfiguration.RTMPS_KEYSTOREPASS);
		poolSched.setKeystoreFile(ExtConfiguration.RTMPS_KEYSTOREFILE);
		poolSched.setTruststorePassword(ExtConfiguration.RTMPS_TRUSTSTOREPASS);
		poolSched.setTruststoreFile(ExtConfiguration.RTMPS_TRUSTSTOREFILE);
		String[] cipherSuites = new String[]{"TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256",
				"TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384",
				"TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA",
				"TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384",
				"TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA",
				"TLS_ECDHE_RSA_WITH_RC4_128_SHA",
				"TLS_RSA_WITH_AES_128_CBC_SHA256",
				"TLS_RSA_WITH_AES_128_CBC_SHA",
				"TLS_RSA_WITH_AES_256_CBC_SHA256",
				"TLS_RSA_WITH_AES_256_CBC_SHA",
				"SSL_RSA_WITH_RC4_128_SHA"};
  		poolSched.setCipherSuites(cipherSuites);
  		String[] protocols = new String[]{"TLSv1","TLSv1.1","TLSv1.2"};
   		poolSched.setProtocols(protocols); 
		return poolSched;
    }
	
	@Order(47)
	@Bean(name="rtmpsTransport",initMethod = "start", destroyMethod = "stop") 
    public RTMPMinaTransport rtmpsTransport(){
		RTMPMinaTransport poolSched = new RTMPMinaTransport(); 
		poolSched.setIoHandler(rtmpsMinaIoHandler());
		poolSched.setAddress(ExtConfiguration.RTMPS_HOST+":"+ExtConfiguration.RTMPS_PORT);
		poolSched.setIoThreads(ExtConfiguration.RTMP_IO_THREADS);
		poolSched.setTcpNoDelay(ExtConfiguration.TCP_NO_DELAY); 
		return poolSched;
    }*/
	
	//---------------------------------------新增-------------------------
	@Order(48)
	@Bean(name="rtmpSampleAccess") 
	@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public RtmpSampleAccess rtmpSampleAccess() throws Exception{
		RtmpSampleAccess context = new RtmpSampleAccess(); 
		context.setAudioAllowed(true);
		context.setVideoAllowed(true);
        return context;
    }
	@Order(49)
	@Bean(name="broadcastStream") 
	@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public RecordableBroadcastStream recordableBroadcastStream() throws Exception{
		RecordableBroadcastStream context = new RecordableBroadcastStream();
		context.setCanRecord(false);
		context.setStorePath("e:/temp");
		context.setDurationPerFile(60);
        return context;
    }
	@Order(50)
	@Bean(name="singleStream") 
	@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public SingleItemSubscriberStream singleItemSubscriberStream() throws Exception{
		SingleItemSubscriberStream context = new SingleItemSubscriberStream(); 
        return context;
    }
	@Order(51)
	@Bean(name="fileConsumer") 
	@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public FileConsumer fileConsumer(){
		FileConsumer context = new FileConsumer(); 
        return context;
    }	
	
	
	//----------------------------------------demo-------------------------
	@Order(52)
	@Bean(name="web.context.oflaDemo") 
	@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public Context contextOflaDemo() throws Exception{
		Context context = new Context();
		context.setScopeResolver(scopeResolver());
		context.setClientRegistry(clientRegistry());
		context.setServiceInvoker(serviceInvoker());
		context.setMappingStrategy(mappingStrategy());
        return context;
    }
	
	@Order(53)
	@Bean(name="web.handler.oflaDemo") 
	@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public Application handlerOflaDemo() throws Exception{
		Application context = new Application(); 
        return context;
    }
	@Order(54)
	@Bean(name="httpApplicationAdapter") 
	@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public HTTPApplicationAdapter httpApplicationAdapter(){
		HTTPApplicationAdapter context = new HTTPApplicationAdapter(); 
        return context;
    } 
	
	@Order(55)
	@Bean(name="web.scope.oflaDemo") 
    public WebScope scopeOflaDemo() throws Exception{
		WebScope context = new WebScope(); 
		context.setServer(server());
		context.setParent(globalScope()); 
		context.setContext(contextOflaDemo());
		context.setHandler(handlerOflaDemo());
		context.setHttpApplicationAdapter(httpApplicationAdapter()); 
		context.setContextPath("/oflaDemo");
		context.setVirtualHosts("*, localhost, localhost:5080, 127.0.0.1:5080");
        return context;
    }
	 
}
 