import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Bootstrap {
	private String rootNodeIP;
	private boolean isPeerPresent = false;
	private int serverPort;

	public int getServerPort() {
		return serverPort;
	}

	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}

	public boolean isPeerPresent() {
		return isPeerPresent;
	}

	public void setPeerPresent(boolean isPeerPresent) {
		this.isPeerPresent = isPeerPresent;
	}
	public String getRootNodeIP() {
		return rootNodeIP;
	}

	public void setRootNodeIP(String rootNodeIP) {
		this.rootNodeIP = rootNodeIP;
	}

	public static void main(String []args) throws IOException{
		Bootstrap myBootstrap = new Bootstrap();
		System.out.println("Enter server port: ");
		String command;
		Scanner in = new Scanner(System.in);
		myBootstrap.setServerPort(new Integer(in.nextLine()).intValue());
		ServerSocket myBootstrapSocket = new ServerSocket(myBootstrap.getServerPort());
		while(true){
			Socket myPeerConnectionSocket = myBootstrapSocket.accept();
			BufferedReader inFromPeer = new BufferedReader(new InputStreamReader(myPeerConnectionSocket.getInputStream()));
			DataOutputStream outFromServer = new DataOutputStream(myPeerConnectionSocket.getOutputStream());
			command = inFromPeer.readLine();
			if("join".equalsIgnoreCase(command)){
				if(myBootstrap.isPeerPresent){
					outFromServer.writeBytes(myBootstrap.getRootNodeIP() + '\n');
				}else{
					myBootstrap.setPeerPresent(true);
					String remoteSocketAddress = myPeerConnectionSocket.getRemoteSocketAddress().toString();
					remoteSocketAddress = remoteSocketAddress.substring(1);
					int index = remoteSocketAddress.indexOf(":");
					remoteSocketAddress = remoteSocketAddress.substring(0, index);
					
					myBootstrap.setRootNodeIP(remoteSocketAddress);
					outFromServer.writeBytes(myBootstrap.getRootNodeIP() + '\n');
				}
			}else{
				System.out.println("Command not recognized.");
			}
		}

	}
}
