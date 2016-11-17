package xbee.api;

public class APIFrame {
	protected byte id;
	protected byte[] data;
	protected String frame = "";

	public APIFrame(byte id, int length){
		this.id = id;
		this.data = new byte[length];
	}
}
