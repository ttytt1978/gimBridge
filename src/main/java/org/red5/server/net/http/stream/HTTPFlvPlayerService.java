package org.red5.server.net.http.stream;

import static org.red5.server.net.http.message.HTTPHeaders.Names.CONTENT_TYPE;

import java.io.IOException;
import java.util.Set;

import org.apache.mina.core.session.IdleStatus;
import org.red5.conf.ExtConfiguration;
import org.red5.server.api.Red5;
import org.red5.server.api.scope.IScope;
import org.red5.server.api.service.IStreamSecurityService;
import org.red5.server.api.stream.IStreamPlaybackSecurity;
import org.red5.server.api.stream.support.SimplePlayItem;
import org.red5.server.net.http.BaseHTTPService;
import org.red5.server.net.http.HTTPMinaConnection;
import org.red5.server.net.http.IHTTPService;
import org.red5.server.net.http.message.HTTPRequest;
import org.red5.server.net.http.message.HTTPResponse;
import org.red5.server.net.http.message.HTTPResponseStatus;
import org.red5.server.util.ScopeUtils;

/**
 * HTTP Flv Stream Player Service
 * @author pengliren
 *
 */
public class HTTPFlvPlayerService extends BaseHTTPService implements IHTTPService {

	@Override
	public void setHeader(HTTPResponse resp) {

		resp.addHeader("Accept-Ranges", "bytes");
		resp.addHeader(CONTENT_TYPE, "video/x-flv");
		//resp.addHeader(CONTENT_LENGTH, 1000);  
		resp.addHeader("Pragma", "no-cache"); 
		resp.setHeader("Connection", "Keep-Alive");
		resp.setHeader("Cache-Control", "no-cache"); 
	}

	@Override
	public void handleRequest(HTTPRequest req, HTTPResponse resp, IScope scope) throws Exception {

		HTTPMinaConnection conn = (HTTPMinaConnection)Red5.getConnectionLocal();
		if (!REQUEST_GET_METHOD.equalsIgnoreCase(req.getMethod().toString())) {
			sendError(req, resp, HTTPResponseStatus.BAD_REQUEST);
			return;
		}
		
		String path = req.getPath().substring(1);
		String[] segments = path.split("/");
		String streamName;
		if (segments.length < 2) {		
			sendError(req, resp, HTTPResponseStatus.BAD_REQUEST);		
			return;
		}
		streamName = segments[1];		
		
		// play security
		IStreamSecurityService security = (IStreamSecurityService) ScopeUtils.getScopeService(scope, IStreamSecurityService.class);
		if (security != null) {
			Set<IStreamPlaybackSecurity> handlers = security.getStreamPlaybackSecurity();
			for (IStreamPlaybackSecurity handler : handlers) {
				if (!handler.isPlaybackAllowed(scope, streamName, 0, 0, false)) {
					sendError(req, resp, HTTPResponseStatus.BAD_REQUEST);
					return;
				}
			}
		}
		
		HTTPConnectionConsumer consumer = new HTTPConnectionConsumer(conn);		
		
		conn.getHttpSession().getConfig().setReaderIdleTime(0);
		conn.getHttpSession().getConfig().setWriterIdleTime(0);
		conn.getHttpSession().getConfig().setIdleTime(IdleStatus.WRITER_IDLE, ExtConfiguration.HTTP_IDLE);
		
		consumer.getConnection().connect(scope);
		CustomSingleItemSubStream stream = new CustomSingleItemSubStream(scope, consumer);
		SimplePlayItem playItem = SimplePlayItem.build(streamName, -2000, -1);
		stream.setPlayItem(playItem);
		stream.start();
		
		conn.setAttribute("consumer", consumer);
		conn.setAttribute("stream", stream);
		
		setHeader(resp);
		conn.write(resp);
		
		try {
			stream.play();
		} catch (IOException e) {
			log.info("http play faile {}", e.getMessage());
			sendError(req, resp, HTTPResponseStatus.BAD_REQUEST);
			stream.stop();
			return;
		}
		
		if (stream.isFailure()) {
			log.info("stream {} http play faile", streamName);
			sendError(req, resp, HTTPResponseStatus.BAD_REQUEST);
			stream.stop();
			return;
		}
	}
}
