package com.md.red5;

import org.apache.mina.core.buffer.IoBuffer;
import org.red5.io.IStreamableFile;
import org.red5.io.ITag;
import org.red5.io.ITagWriter;
import org.red5.io.flv.impl.Tag;
import org.red5.server.api.scope.IScope;
import org.red5.server.api.service.IStreamableFileService;
import org.red5.server.api.stream.IBroadcastStream;
import org.red5.server.api.stream.IStreamListener;
import org.red5.server.api.stream.IStreamPacket;
import org.red5.server.api.stream.IStreamableFileFactory;
import org.red5.server.stream.StreamableFileFactory;
import org.red5.server.util.ScopeUtils;

import java.io.File;
import java.io.IOException;

public class MyStreamListener implements IStreamListener {
    private int startTimeStamp = -1;
    private int lastTimeStamp=0;
    private int previousTimeStamp=0;
    private ITagWriter writer = null;
    private File file;


    MyStreamListener(String publishedName, IScope scope) {
        init(publishedName, scope);
    }


    public void close() {
        try {
            writer.close();
        } catch (Exception e) {
        }

    }

    public void reset()
    {
        previousTimeStamp=0;
    }

    private void init(String publishedName, IScope scope) {

        try {
            file = new File("C:/" + publishedName + "_" + System.currentTimeMillis() + "_a.flv");

            IStreamableFileFactory factory = (IStreamableFileFactory) ScopeUtils
                    .getScopeService(scope, IStreamableFileFactory.class,
                            StreamableFileFactory.class);

            if (!this.file.isFile()) {
                // Maybe the (previously existing) file has been deleted
                this.file.createNewFile();
            } else if (!file.canWrite()) {
                throw new IOException("The file is read-only");
            }

            IStreamableFileService service = factory.getService(this.file);
            IStreamableFile flv = service.getStreamableFile(this.file);
            this.writer = flv.getWriter();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void packetReceived(IBroadcastStream stream, IStreamPacket packet) {
        String publishedName = stream.getPublishedName();
        int timeStamp = packet.getTimestamp();
        System.out.println(timeStamp+" :packetReceived----------"+publishedName);
        IoBuffer data = packet.getData().asReadOnlyBuffer();
        if (data.limit() == 0) {
            return;
        }
        if (startTimeStamp == -1) {
            // That will be not bigger then long value
            startTimeStamp = timeStamp;
            lastTimeStamp=timeStamp;
            previousTimeStamp=0;
        }
        if(timeStamp<lastTimeStamp)
        {
            int gap=timeStamp-previousTimeStamp;
            previousTimeStamp=timeStamp;
            timeStamp=lastTimeStamp+gap;
            lastTimeStamp=timeStamp;
        }else {
            lastTimeStamp=timeStamp;
        }
        timeStamp-=startTimeStamp;
        System.out.println("timeStamp="+timeStamp);
        ITag tag = new Tag();
        tag.setDataType(packet.getDataType());
        tag.setBodySize(data.limit());
        tag.setTimestamp(timeStamp);
        tag.setBody(data);
        try {
            writer.writeTag(tag);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
