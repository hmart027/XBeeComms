package xbee.api;

public class XbeeAPI {
	
	public static final int HEADER = 0x7E;
	public static enum PARSE_STATE {
		IDLE,HEADER,L1,L2,FRAME;
	}
	
	private PARSE_STATE parseState = PARSE_STATE.IDLE;
	private int length = 0;
	private int fIndex = 0;
	private byte[] frame = null;
	private byte checksum = 0;
	
	public boolean parse(byte d){
		switch (parseState) {
		case IDLE:
			if((d & 0x0FF) == HEADER)
				parseState = PARSE_STATE.HEADER;
			return false;
		case HEADER:
			length = (d & 0x0FF) << 8;
			parseState = PARSE_STATE.L1;
			return false;
		case L1:
			length += (d & 0x0FF);
			frame = new  byte[length];
			parseState = PARSE_STATE.L2;
			return false;
		case L2:
			frame[fIndex++] = d;
			checksum += d;
			if(fIndex == length)
				parseState = PARSE_STATE.FRAME;
			return false;
		case FRAME:
			if( ((d + checksum)&0x0FF) != 0x0FF){
				parseState = PARSE_STATE.IDLE;
				length = 0;
				fIndex = 0;
				frame = null;
				checksum = 0;
				return false;
			}
			break;
		}

		APIFrame f;		
		switch (frame[0]) {
		case (byte)0xB0:
			f = new RXIPv4(this.length-1);
			for(int i = 1; i<this.length; i++)
				f.data[i-1] = frame[i];
			System.out.println("From: "+ ((RXIPv4)f).getSourceIP()+" on port: "+((RXIPv4)f).dPort());
			break;
		}
		
		parseState = PARSE_STATE.IDLE;
		length = 0;
		fIndex = 0;
		frame = null;
		checksum = 0;
		return true;
	}

}
