import java.io.*;
import java.util.*;
import javax.sound.sampled.*;
import java.math.*;
import org.tritonus.share.sampled.TConversionTool;

public class MixingAudioInputStream extends AudioInputStream{
	AudioInputStream[] m_streams = null;
	
	public MixingAudioInputStream(AudioInputStream[] streams) {
		super((AudioInputStream) streams[0], streams[0].getFormat(), AudioSystem.NOT_SPECIFIED);

		m_streams = streams;
	}
	
	public int read(byte[] b, int off, int len) throws IOException{
		int bytesRead = 0;
		byte[] mixedframes = new byte[len];
		
		// initialize mixing frame to 0
		for (int i = 0; i < len; i++){
			mixedframes[i] = 0;
		}
		
		// iterate across streams
		for (int i = 0; i < m_streams.length; i++){
			// read the current stream
			byte[] readframes = new byte[len];
			
			int curBytesRead = m_streams[i].read(readframes,0,len);
			
			// return value still initialized to 0
			if (bytesRead == 0){
				// set it to the first read
				bytesRead = curBytesRead;
			} else if (curBytesRead != bytesRead){
				// if the two read sizes are different for some reason, use the max of the two 
				// so that it keeps reading the buffer that still has data (-1 marks the end)
				bytesRead = Math.max(bytesRead,curBytesRead);
			}
			for (int j = 0; j < len; j += frameSize){
				switch(frameSize){
				case 1:
					mixedframes[j] += readframes[j];
					break;
				case 2:
					int mixedframe16 = TConversionTool.bytesToInt16(mixedframes,j,false);
					int readframe16 = TConversionTool.bytesToInt16(readframes, j, false);
					mixedframe16 += readframe16;
					TConversionTool.intToBytes16(mixedframe16, mixedframes, j, false);
					break;
				case 3:
					int mixedframe24 = TConversionTool.bytesToInt24(mixedframes,j,false);
					int readframe24 = TConversionTool.bytesToInt24(readframes, j, false);
					mixedframe24 += readframe24;
					TConversionTool.intToBytes24(mixedframe24, mixedframes, j, false);
					break;	
				case 4:
					int mixedframe32 = TConversionTool.bytesToInt32(mixedframes,j,false);
					int readframe32 = TConversionTool.bytesToInt32(readframes, j, false);
					mixedframe32 += readframe32;
					TConversionTool.intToBytes32(mixedframe32, mixedframes, j, false);
					break;
				}
			}
		}
		for (int i = 0; i < len; i++){
			b[off+i] = mixedframes[i];
		}
		
		return bytesRead;
	}
	

}