package main;

import java.io.IOException;
import java.io.InputStreamReader;

import comm.serial.Comm;

import xbee.api.XbeeAPI;

public class Serial extends Thread {
	
	public void run(){
		Comm port = new Comm();
		port.getComm("COM5", 115200);
		InputStreamReader in = new InputStreamReader(port.getInputStream());
		
		System.out.println("Started");
		XbeeAPI api = new XbeeAPI();
		
		while(true){
			try {
				if (in.ready()){
					while(in.ready()){
						if(api.parse((byte)in.read()))
								System.out.println("Good");
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
