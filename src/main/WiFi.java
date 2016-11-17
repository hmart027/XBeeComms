package main;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

import javax.swing.JFrame;

import comm.serial.Comm;

public class WiFi extends Thread{
	
	public int right = 0;
	public int left = 0;
	
	public static Socket sock;
	public static OutputStream out;
	
	@SuppressWarnings("resource")
	public void run(){
		
		new UDPListener().start();
		
		try {

			SocketListener l = new SocketListener();
			l.start();
			Thread.sleep(1000);
			
			DatagramSocket socket = new DatagramSocket();
			final String MULTICAST_GROUP_ID = "192.168.1.255";
//			final String MULTICAST_GROUP_ID = "255.255.255.255";
			final int PORT = 8256;
			byte[] buf = new byte[]{(byte)0xFF,0x20,0x60};
			final InetAddress group = InetAddress.getByName(MULTICAST_GROUP_ID);
			DatagramPacket packet = new DatagramPacket(buf, buf.length, group, PORT);
			for(int i =0; i<5;i++){
				socket.send(packet);
				Thread.sleep(800);
			}
			socket.close();
			
			System.out.println("Sent");
			
//			Thread.sleep(5000);
			
			if(l.replys[0]==null){
				System.out.println("No Reply");
				System.exit(0);
			}
			System.out.println("Trying to connect to: "+ l.replys[0]);
			Socket s = new Socket(l.replys[0], 8256);
			out= s.getOutputStream();
			send();
			System.out.println("Done");
			
			JFrame frame = new JFrame();
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setBounds(100,100,100,100);
			frame.addKeyListener(new Listener());
			frame.setVisible(true);
			
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}
	
	public void send(){
		byte sign = 0;
		if(left>0) sign |=1;
		if(right>0) sign |=2;
		
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
	
	public class Listener implements KeyListener{

		@Override
		public void keyTyped(KeyEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void keyPressed(KeyEvent e) {
			// TODO Auto-generated method stub
			System.out.println();
			switch(e.getKeyChar()){
			case '7':
				if(left<255)left++;break;
			case '4':
				if(left>-255)left--;break;
			case '9':
				if(right<255)right++;break;
			case '6':
				if(right>-255)right--;break;
			}
			
			send();
			
		}

		@Override
		public void keyReleased(KeyEvent e) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	public class SerialListener extends Thread{
		
		public void run(){
			InputStream in = null;
			Comm sock = new Comm();
			sock.getComm("COM6",115200);
			System.out.println("On Com 6");
			in = sock.getInputStream();
			
			while(true){
				try {
					while(in!=null && in.available()>0)
						System.out.println(Integer.toHexString(in.read()));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
		}
	}


	public class UDPListener extends Thread{
		
		@SuppressWarnings("resource")
		public void run(){
			try {
			      int port = 8256;

			      // Create a socket to listen on the port.
			      DatagramSocket dsocket = new DatagramSocket(port);

			      // Create a buffer to read datagrams into. If a
			      // packet is larger than this buffer, the
			      // excess will simply be discarded!
			      byte[] buffer = new byte[2048];

			      // Create a packet to receive data into the buffer
			      DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

			      // Now loop forever, waiting to receive packets and printing them.
			      while (true) {
			        // Wait to receive a datagram
			        dsocket.receive(packet);

			        // Convert the contents to a string, and display them
			        String msg = "";
			        for(int i = 0; i<packet.getLength(); i++)
			        	msg += Integer.toHexString(buffer[i] & 0x0FF) + " ,";
			        System.out.println(packet.getAddress().getHostName() + ": "
			            + msg);

			        // Reset the length of the packet before reusing it.
			        packet.setLength(buffer.length);
			      }
			    } catch (Exception e) {
			      System.err.println(e);
			    }
		}
		
	}

	public class SocketListener extends Thread{
		
		String[] replys= new String[5];
		
		@SuppressWarnings("resource")
		public void run(){
			InputStream in = null;
			try {
				ServerSocket server = new ServerSocket(0x2060);
				System.out.println("Waiting on: 8288");
				Socket sock = server.accept();
				System.out.println("Connected");
				in = sock.getInputStream();
				replys[0] = sock.getInetAddress().getHostAddress()+"";
				System.out.println("From: "+replys[0]);
				sock.close();
				
				long t1 = System.currentTimeMillis();
				
				while(System.currentTimeMillis()-t1 <50000){
					try {
						if(sock.isClosed()){
							sock=server.accept();
							in=sock.getInputStream();
							replys[0] = sock.getInetAddress().getHostAddress()+"";
							System.out.println("From: "+replys[0]);
						}
						while(in.available()>0)
							System.out.println((char)(in.read()));
						Thread.sleep(10);
						sock.close();
					} catch (IOException | InterruptedException e) {
						e.printStackTrace();
					}
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			
					
		}
	}
}
