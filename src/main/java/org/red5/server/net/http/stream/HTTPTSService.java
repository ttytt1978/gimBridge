package org.red5.server.net.http.stream;

import static org.red5.server.net.http.message.HTTPHeaders.Names.CONTENT_TYPE;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.mina.core.buffer.IoBuffer;
import org.red5.codec.AudioCodec;
import org.red5.codec.VideoCodec;
import org.red5.io.IStreamableFile;
import org.red5.io.IStreamableFileFactory;
import org.red5.io.IStreamableFileService;
import org.red5.io.ITag;
import org.red5.io.ITagReader;
import org.red5.io.StreamableFileFactory;
import org.red5.io.flv.FLVUtils;
import org.red5.io.ts.FLV2MPEGTSChunkWriter;
import org.red5.server.ScopeContextBean;
import org.red5.server.api.Red5;
import org.red5.server.api.scope.IScope;
import org.red5.server.cache.CacheManager;
import org.red5.server.cache.ObjectCache;
import org.red5.server.net.http.BaseHTTPService;
import org.red5.server.net.http.HTTPMinaConnection;
import org.red5.server.net.http.IHTTPService;
import org.red5.server.net.http.codec.QueryStringDecoder;
import org.red5.server.net.http.message.HTTPRequest;
import org.red5.server.net.http.message.HTTPResponse;
import org.red5.server.net.http.message.HTTPResponseStatus;
import org.red5.server.net.rtmp.event.AudioData;
import org.red5.server.net.rtmp.event.VideoData;
import org.red5.server.stream.IProviderService;

/**
 * HTTP Live Stream Mpegts Service
 * @author pengliren
 *
 */
public class HTTPTSService extends BaseHTTPService implements IHTTPService {

	private static Pattern pattern = Pattern.compile("(\\d+)_(\\d+)_(\\d+)\\.ts$");
	
	private static ObjectCache fileCache;
	
	@Override
	public void handleRequest(HTTPRequest req, HTTPResponse resp, IScope scope) throws Exception {

		String method = req.getMethod().toString();
		if (!REQUEST_GET_METHOD.equalsIgnoreCase(method) && !REQUEST_POST_METHOD.equalsIgnoreCase(method)) {
			// Bad request - return simple error page
			sendError(req, resp, HTTPResponseStatus.BAD_REQUEST);
			return;
		}
		
		QueryStringDecoder queryStringDecoder = new QueryStringDecoder(req.getPath());
		String path = queryStringDecoder.getPath().substring(1);
		String[] segments = path.split("/");
		String app = scope.getName();
		String streamName;
		String tsIndex;
		if (segments.length < 2) {		
			sendError(req, resp, HTTPResponseStatus.BAD_REQUEST);	
			return;
		}
		
		streamName = segments[0];
		tsIndex = segments[1];
								
		Map<String, List<String>> params = queryStringDecoder.getParameters();
		String type = "";
		if(params.get("type") != null && params.get("type").size() > 0) {
			type = params.get("type").get(0);
		}
		if(type.equals("live")) { // live
			playLiveTsStream(scope, app, streamName, tsIndex, req, resp);
		} else if(type.equals("vod")) { // vod
			playVodTsStream(scope, app, streamName, tsIndex, req, resp);
		} else { // no found
			sendError(req, resp, HTTPResponseStatus.NOT_FOUND);	
		}
	}
	
	private void playLiveTsStream(IScope scope, String app, String streamName, String tsIndex, HTTPRequest req, HTTPResponse resp) {
		
		tsIndex = tsIndex.substring(0, tsIndex.lastIndexOf(".ts"));
		int sequenceNumber = Integer.valueOf(tsIndex);
		MpegtsSegmenterService service = MpegtsSegmenterService.getInstance();
		if (service.isAvailable(scope, streamName)) {
			MpegtsSegment segment = service.getSegment(app, streamName, sequenceNumber);
			if (segment != null && segment.isClosed()) {
				IoBuffer data = segment.getBuffer().asReadOnlyBuffer();
				setHeader(resp);
				commitResponse(req, resp, data);
			} else {
				sendError(req, resp, HTTPResponseStatus.NOT_FOUND);
			}
		}
	}
	
	private void playVodTsStream(IScope scope, String app, String streamName, String tsIndex, HTTPRequest req, HTTPResponse resp) {
				
		Matcher m = pattern.matcher(tsIndex);
		int start;
		int end;
		if(m.matches()) {
			start = Integer.valueOf(m.group(1));
			end = Integer.valueOf(m.group(2));
		} else {
			sendError(req, resp, HTTPResponseStatus.BAD_REQUEST);
			return;
		}
		HTTPMinaConnection conn = (HTTPMinaConnection)Red5.getConnectionLocal();
		IProviderService providerService = (IProviderService) scope.getContext().getBean(ScopeContextBean.PROVIDERSERVICE_BEAN);
		File file = providerService.getVODProviderFile(scope, streamName);
		IStreamableFileFactory factory = StreamableFileFactory.getInstance();
		IStreamableFileService service = factory.getService(file);
		if (service != null && (StringUtils.endsWithIgnoreCase(streamName, ".flv") 
					|| StringUtils.endsWithIgnoreCase(streamName, ".mp4"))) {
			IoBuffer data = IoBuffer.allocate(4096).setAutoExpand(true);
			FLV2MPEGTSChunkWriter writer;
			boolean audioChecked = false;
			boolean videoChecked = false;
			IoBuffer videoConfig = null;
			IoBuffer audioConfig = null;
			try {
				IStreamableFile streamFile = null;
				ITagReader reader = null;;
				if(getFileCache().get(streamName) == null || (reader = ((ITagReader)getFileCache().get(streamName)).copy()) == null) {
					getFileCache().remove(streamName);
					streamFile = service.getStreamableFile(file);
					reader = streamFile.getReader();
				}
				
				if (start > 0) {				
					ITag tag;
					for (int i = 0; i < 10; i++) {
						if (audioChecked && videoChecked) break;
						tag = reader.readTag();
						if (tag == null) return;
						if (ITag.TYPE_VIDEO == tag.getDataType()) {
							videoChecked = true;
							if (FLVUtils.getVideoCodec(tag.getBody().get(0)) == VideoCodec.AVC.getId() && tag.getBody().get(1) == 0x00) {
								videoConfig = tag.getBody();
							}
						} else if (ITag.TYPE_AUDIO == tag.getDataType()) {
							audioChecked = true;
							if (FLVUtils.getAudioCodec(tag.getBody().get(0)) == AudioCodec.AAC.getId() && tag.getBody().get(1) == 0x00) {
								audioConfig = tag.getBody();
							}
						}
					}
					
					reader.position(start - 4);
				}
				
				writer = new FLV2MPEGTSChunkWriter(videoConfig, audioConfig, false);
				writer.startChunkTS(data);				
				VideoData videoData;
				AudioData audioData;
				while (reader.hasMoreTags()) {
					
					if(conn.isClosing()) return;// if client conn is close we must stop release resources
					if (end != -1 && reader.getBytesRead() + 4 >= end) break;
					ITag tag = reader.readTag();
					if (tag == null) break; // fix tag NPE
					if (tag.getDataType() == 0x09) {
						videoData = new VideoData(tag.getBody());
						videoData.setTimestamp(tag.getTimestamp());				
						writer.writeStreamEvent(videoData);						
					} else if (tag.getDataType() == 0x08) {
						audioData = new AudioData(tag.getBody());
						audioData.setTimestamp(tag.getTimestamp());
						writer.writeStreamEvent(audioData);		
					}
				}
				reader.close();
			} catch (IOException e) {
				log.info("play vod exception {}", e.getMessage());
				sendError(req, resp, HTTPResponseStatus.BAD_REQUEST);		
				return;
			}
			writer.endChunkTS();
			data.flip();
			setHeader(resp);
			commitResponse(req, resp, data);		
		} else {
			sendError(req, resp, HTTPResponseStatus.NOT_FOUND);
		}
	}
	
	public static ObjectCache getFileCache() {
		if (fileCache == null) {
			fileCache = CacheManager.getInstance().getCache("org.red5.server.stream.hls.fileCache");
		}

		return fileCache;
	}

	@Override
	public void setHeader(HTTPResponse resp) {
		
		resp.addHeader("Accept-Ranges", "bytes");
		resp.addHeader(CONTENT_TYPE, "video/MP2T");
		resp.addHeader("Pragma", "no-cache"); 
		resp.setHeader("Cache-Control", "no-cache");
	}

}
