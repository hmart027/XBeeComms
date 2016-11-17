package main;

public class Main {
	
	public static void main(String[] args){
	//	new Serial().start();
		tank.wifi.WiFiInterface tank = new tank.wifi.WiFiInterface();
		System.out.println(tank.getDevices());
	}

}
