package xbee.api;

public class RXIPv4 extends APIFrame {

	public RXIPv4(int length){
		super((byte) 0xB0, length);
		this.frame = "RX IPv4";
	}
	
	public String getSourceIP(){
		return (data[0] & 0x0FF)+"."+(data[1] & 0x0FF)+"."+(data[2] & 0x0FF)+"."+(data[3] & 0x0FF);
	}
	
	public int dPort(){
		return ((data[4] & 0x0FF)<<8) + (data[5] & 0x0FF);
	}
	
	public int sPort(){
		return ((data[6] & 0x0FF)<<8) + (data[7] & 0x0FF);
	}
}
