package org.red5.io.ts;

/**
 * FLV TO MPEGTS TS Writer Interface
 * @author pengliren
 *
 */
public interface IFLV2MPEGTSWriter {

	public void nextBlock(long ts, byte[] block);
}
