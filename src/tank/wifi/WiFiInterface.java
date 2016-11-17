package tank.wifi;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

public class WiFiInterface{

	private java.util.ArrayList<String> devices = null;
	
	public static Socket sock;
	public static OutputStream out;
	
	public int getDevices(){
		devices = new java.util.ArrayList<>();
		SocketListener listener = new SocketListener();
		listener.start();
		try {
			DatagramSocket socket = new DatagramSocket();
			final String MULTICAST_GROUP_ID = "192.168.1.255";
			final int PORT = 8256;
			byte[] buf = new byte[]{(byte)0xFF,0x20,0x60};
			final InetAddress group = InetAddress.getByName(MULTICAST_GROUP_ID);
			DatagramPacket packet = new DatagramPacket(buf, buf.length, group, PORT);
			for(int i =0; i<5;i++){
				socket.send(packet);
				Thread.sleep(800);
			}
			socket.close();	
			Thread.sleep(100);	
			listener.stopListening();
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}		
		if(devices.size()==0){
			System.out.println("No Reply");
		}	
		return devices.size();
	}
	
	public boolean connect(int i){
		try {
			sock = new Socket(devices.get(i), 8256);
			out= sock.getOutputStream();
			return true;
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public boolean disconnect(){
		try {
			sock.close();
			out= null;
			return true;
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public void send(int right, int left){
		byte sign = 0;
		if(left>0) sign |= 1;
		if(right>0) sign |= 2;
		
		System.out.println("sig: "+sign);
		
		try {
			out.write(new byte[]{0,0,sign,(byte) Math.abs(left),(byte) Math.abs(right)});					
		} catch (UnknownHostException x) {
			x.printStackTrace();
		} catch (IOException x) {
			x.printStackTrace();
		}
		
		System.out.println("R: "+ right);
		System.out.println("L: "+ left);
		System.out.println();
	}
		
	public class SocketListener extends Thread{

		private volatile ServerSocket server = null;
		private volatile Socket sock = null;
		private volatile InputStream in = null;
		private volatile boolean keepRunning = true;
		
		public void run(){
			try {
				server = new ServerSocket(0x2060);
				System.out.println("Waiting on: 8288");
				
				while(keepRunning){
					try {
						sock=server.accept();
						in=sock.getInputStream();
						String add = sock.getInetAddress().getHostAddress();
						if(!devices.contains(add))
							devices.add(add);
						System.out.println("From: "+devices.get(0));
						while(in.available()>0)
							System.out.println((char)(in.read()));
						Thread.sleep(10);
						sock.close();
					} catch (java.net.SocketException e){
						if(keepRunning)
							e.printStackTrace();
					} catch (IOException | InterruptedException e) {
						e.printStackTrace();
					}
				}
				if(!server.isClosed())
					server.close();
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	
		public void stopListening(){
			keepRunning = false;
			try {
				server.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
