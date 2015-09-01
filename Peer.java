import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;

public class Peer {

	private static String JOIN = "join";
	private static String MID_COORDINATES = "midcoordinates";
	private static String FIND_DISTANCE = "finddistance";
	private static String NEIGHBOR_DISTANCE = "neighbordistance";
	private static String NEW_SPACE = "newspace";
	static int serverPort;
	private static String VIEW = "view";
	private static String EXIT = "exit";
	private static String SEARCH = "search";
	private static String INSERT = "insert";
	private static String PRINT = "print";
	private static String GIVE_POINTS = "givepoints";
	private static String AM_I_NEIGHBOR = "amineighbor";
	private static String NEIGHBOR_FOUND_POINT = "neighborfoundpoint";
	private static String MUTUAL_REMOVE = "mutualremove";
	private static String FIND_DISTANCE_FOR_INSERT = "distanceinsert";
	private static String FIND_DISTANCE_FOR_SEARCH = "searchdistance";
	private static String SEARCH_PRINT_FOUND = "searchprint";

	static class SendingPacket implements java.io.Serializable {
		Point p;
		String keyword;
		String IP;
		String newJoineeIP;
		double distance;
		Point newLowerPoint, newUpperPoint;
		ArrayList<String> newNeighbors = new ArrayList<String>();
		ArrayList<String> newFiles = new ArrayList<String>();
		ArrayList<String> path = new ArrayList<String>();

		SendingPacket(String string, Point myUpperPoint, Point myLowerPoint) {
			this.keyword = string;
			this.newUpperPoint = myUpperPoint;
			this.newLowerPoint = myLowerPoint;
		}

		SendingPacket(Point p, String keyword, String newJoineeIP) {
			this.p = p;
			this.keyword = keyword;
			this.newJoineeIP = newJoineeIP;
		}

		public SendingPacket(String keyword, Point p) {
			this.p = p;
			this.keyword = keyword;
		}

		SendingPacket(String keyword, String IP) {
			this.keyword = keyword;
			this.IP = IP;
			// this.newJoineeIP = destinationIP;
		}

		public SendingPacket(Point newUpperPoint, Point newLowerPoint,
				String keyword, ArrayList<String> newNeighbors,
				ArrayList<String> newFileList) {
			this.newUpperPoint = newUpperPoint;
			this.newLowerPoint = newLowerPoint;
			this.keyword = keyword;
			this.newNeighbors = newNeighbors;
			this.newFiles = newFileList;
		}

		public SendingPacket(double distance2) {
			this.distance = distance2;
		}

		public SendingPacket(String keyWord, ArrayList<String> myNeighbors) {
			this.keyword = keyWord;
			this.newNeighbors.addAll(myNeighbors);
		}

		public SendingPacket(Point destinationPoint, String keyword,
				ArrayList<String> path) {
			this.p = destinationPoint;
			this.keyword = keyword;
			this.path = path;
		}

		public String getNewJoineeIP() {
			return newJoineeIP;
		}

		public String getKeyword() {
			return keyword;
		}

		public Point getPoint() {
			return p;
		}
	}

	static class Point implements java.io.Serializable {
		double x;
		double y;
		String fileName;

		Point(double x2, double y2) {
			this.x = x2;
			this.y = y2;
		}

		public Point(double x2, double y2, String myCommand) {
			this.x = x2;
			this.y = y2;
			this.fileName = myCommand;
		}

		public double getX() {
			return x;
		}

		public double getY() {
			return y;
		}

		public void setX(double x) {
			this.x = x;
		}
	}

	static ArrayList<String> neighbors = new ArrayList<String>();
	static ArrayList<String> fileList = new ArrayList<String>();
	static String IPAddress;
	static int portNumber = 2222;
	static Point myUpperPoint;
	static Point myLowerPoint;
	static String serverIP;

	Peer() {
		myLowerPoint = new Point(0, 0);
		myUpperPoint = new Point(0, 0);
	}

	public static String getRootIP() {
		String rootIPAddress = new String();
		try {
			Socket peer = new Socket(serverIP, serverPort);
			DataOutputStream outServer = new DataOutputStream(
					peer.getOutputStream());
			BufferedReader inServer = new BufferedReader(new InputStreamReader(
					peer.getInputStream()));
			outServer.writeBytes("join" + '\n');
			rootIPAddress = inServer.readLine();
			peer.close();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return rootIPAddress;
	}

	static class PeerForward extends Thread {

		static String destinationIPAddress;
		static double destinationX;
		static double destinationY;
		static String keyword;
		static String fileName;
		static ArrayList<String> path = new ArrayList<String>();

		public PeerForward(String keyWord, String IPAddress, double d, double e) {
			PeerForward.keyword = keyWord;
			destinationIPAddress = IPAddress;
			PeerForward.destinationX = d;
			PeerForward.destinationY = e;
		}

		public PeerForward(String keyword, Point p) {
			PeerForward.keyword = keyword;
			PeerForward.destinationX = p.x;
			PeerForward.destinationY = p.y;
			PeerForward.fileName = p.fileName;
		}

		public PeerForward(String keyword2, String newJoineeIP, Point p) {
			PeerForward.keyword = keyword2;
			destinationIPAddress = newJoineeIP;
			PeerForward.destinationX = p.x;
			PeerForward.destinationY = p.y;
			PeerForward.fileName = p.fileName;
		}

		public PeerForward(String keyword2, String newJoineeIP, Point p,
				ArrayList<String> path) {
			PeerForward.keyword = keyword2;
			PeerForward.destinationIPAddress = newJoineeIP;
			PeerForward.destinationX = p.x;
			PeerForward.destinationY = p.y;
			PeerForward.fileName = p.fileName;
			PeerForward.path = path;
		}

		public void run() {
			try {
				String keyWord = new String();
				double[] neighborsDistance = new double[neighbors.size()];
				if (SEARCH.equalsIgnoreCase(keyword)) {
					// get neighbors of current node

					Iterator<String> myIterator = neighbors.iterator();
					int i = 0;
					while (myIterator.hasNext()) {
						String nextNeighbor = myIterator.next();
						// connect to the next neighbor in list
						Socket mySocket = new Socket(nextNeighbor, portNumber);
						ObjectOutputStream out = new ObjectOutputStream(
								mySocket.getOutputStream());
						ObjectInputStream in = new ObjectInputStream(
								mySocket.getInputStream());
						// BufferedReader in = new BufferedReader(new
						// InputStreamReader(mySocket.getInputStream()));
						// create object of point
						Point newJoineePoint = new Point(
								PeerForward.destinationX,
								PeerForward.destinationY, fileName);
						// send packet with request to check if point is in
						// zone, if
						// not, find distance of that neighbor from the point
						SendingPacket myPacket = new SendingPacket(
								newJoineePoint, FIND_DISTANCE_FOR_SEARCH, path);
						// send packet
						out.writeObject(myPacket);
						// get packet with distance of neighbor
						// SendingPacket neighborDistancePacket =
						// (SendingPacket)
						// in.readObject();
						// store distance in array
						SendingPacket so = (SendingPacket) (in.readObject());
						keyWord = so.keyword;
						if (NEIGHBOR_FOUND_POINT.equalsIgnoreCase(so.keyword))
							break;
						neighborsDistance[i] = so.distance;
						i++;
						mySocket.close();
					}
				}

				if (INSERT.equalsIgnoreCase(keyword)) {
					// get neighbors of current node

					Iterator<String> myIterator = neighbors.iterator();
					int i = 0;
					while (myIterator.hasNext()) {
						String nextNeighbor = myIterator.next();
						// connect to the next neighbor in list
						Socket mySocket = new Socket(nextNeighbor, portNumber);
						ObjectOutputStream out = new ObjectOutputStream(
								mySocket.getOutputStream());
						ObjectInputStream in = new ObjectInputStream(
								mySocket.getInputStream());
						// BufferedReader in = new BufferedReader(new
						// InputStreamReader(mySocket.getInputStream()));
						// create object of point
						Point newJoineePoint = new Point(
								PeerForward.destinationX,
								PeerForward.destinationY, fileName);
						// send packet with request to check if point is in
						// zone, if
						// not, find distance of that neighbor from the point
						SendingPacket myPacket = new SendingPacket(
								newJoineePoint, FIND_DISTANCE_FOR_INSERT, "");
						// send packet
						out.writeObject(myPacket);
						// get packet with distance of neighbor
						// SendingPacket neighborDistancePacket =
						// (SendingPacket)
						// in.readObject();
						// store distance in array
						SendingPacket so = (SendingPacket) (in.readObject());
						keyWord = so.keyword;
						if (NEIGHBOR_FOUND_POINT.equalsIgnoreCase(so.keyword))
							break;
						// System.out.println(d);
						neighborsDistance[i] = so.distance;
						i++;
						mySocket.close();
					}
				} else if (JOIN.equalsIgnoreCase(keyword)) {

					// get neighbors of current node
					// double[] neighborsDistance = new
					// double[neighbors.size()];
					Iterator<String> myIterator = neighbors.iterator();
					int i = 0;
					while (myIterator.hasNext()) {
						String nextNeighbor = myIterator.next();
						// connect to the next neighbor in list
						Socket mySocket = new Socket(nextNeighbor, portNumber);
						ObjectOutputStream out = new ObjectOutputStream(
								mySocket.getOutputStream());
						ObjectInputStream in = new ObjectInputStream(
								mySocket.getInputStream());
						// BufferedReader in = new BufferedReader(new
						// InputStreamReader(mySocket.getInputStream()));
						// create object of point
						Point newJoineePoint = new Point(
								PeerForward.destinationX,
								PeerForward.destinationY);
						// send packet with request to check if point is in
						// zone, if
						// not, find distance of that neighbor from the point
						SendingPacket myPacket = new SendingPacket(
								newJoineePoint, FIND_DISTANCE,
								PeerForward.destinationIPAddress);
						// send packet
						out.writeObject(myPacket);
						// get packet with distance of neighbor
						// SendingPacket neighborDistancePacket =
						// (SendingPacket)
						// in.readObject();
						// store distance in array
						SendingPacket so = (SendingPacket) (in.readObject());
						keyWord = so.keyword;

						if (NEIGHBOR_FOUND_POINT.equalsIgnoreCase(so.keyword)) {
							break;
						}

						// System.out.println(d);
						neighborsDistance[i] = so.distance;

						i++;
						mySocket.close();
					}
				}

				if (!(NEIGHBOR_FOUND_POINT.equalsIgnoreCase(keyWord))) {
					int smallestDistanceIndex = 0;
					for (int counter = 1; counter < neighborsDistance.length; counter++) {
						if (neighborsDistance[smallestDistanceIndex] > neighborsDistance[counter]) {
							smallestDistanceIndex = counter;
						}
					}

					Socket mySocket = new Socket(
							neighbors.get(smallestDistanceIndex), portNumber);
					Point destinationPoint = new Point(
							PeerForward.destinationX, PeerForward.destinationY,
							fileName);
					String sendingKeyWord = null;
					if (INSERT.equalsIgnoreCase(keyword))
						sendingKeyWord = new String(INSERT);
					else if (SEARCH.equalsIgnoreCase(keyword))
						sendingKeyWord = new String(SEARCH);
					else
						sendingKeyWord = new String(JOIN);

					SendingPacket joinPacket;
					if (SEARCH.equalsIgnoreCase(keyword)) {
						joinPacket = new SendingPacket(destinationPoint,
								SEARCH, path);

					} else {
						joinPacket = new SendingPacket(destinationPoint,
								sendingKeyWord,
								PeerForward.destinationIPAddress);
					}

					ObjectOutputStream out = new ObjectOutputStream(
							mySocket.getOutputStream());
					out.writeObject(joinPacket);
					mySocket.close();
				}
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	static class PeerSplit extends Thread {

		String newJoineeIP;

		public PeerSplit(String newJoineeIP) {
			this.newJoineeIP = newJoineeIP;
		}

		public void run() {

			ArrayList<String> newNeighbors = new ArrayList<String>();
			// newNeighbors = neighbors;
			Point newLowerPoint, newUpperPoint;
			double newX = 0, newY = 0;
			double midPointX = 0, midPointY = 0;
			// if zone is a square, split vertically
			if ((myUpperPoint.x - myLowerPoint.x) == (myUpperPoint.y - myLowerPoint.y)) {
				midPointX = (myUpperPoint.x + myLowerPoint.x) / 2;
				midPointY = (myUpperPoint.y + myLowerPoint.y) / 2;
				// the right side of square is new zone.

				newX = midPointX;
				newY = myLowerPoint.y;
				newLowerPoint = new Point(newX, myLowerPoint.y);
				newUpperPoint = new Point(myUpperPoint.x, myUpperPoint.y);
				myUpperPoint.x = midPointX;
			} else {// else split horizontally
				midPointY = (myUpperPoint.y + myLowerPoint.y) / 2;
				// newX = myLowerPoint.x;
				newY = midPointY;

				newUpperPoint = new Point(myUpperPoint.x, myUpperPoint.y);
				newLowerPoint = new Point(myLowerPoint.x, newY);
				myUpperPoint.y = midPointY;
			}
			Iterator fileListIterator = fileList.iterator();
			ArrayList<String> newFileList = new ArrayList<String>();
			while (fileListIterator.hasNext()) {
				String currentFile = (String) fileListIterator.next();
				Point p = findHashValue(currentFile);
				boolean isFileInCurrentNode = checkPointInNode(p);
				if (!isFileInCurrentNode) {
					newFileList.add(currentFile);
					fileListIterator.remove();
				}
			}
			try {
				newNeighbors.addAll(neighbors);
				updateNeighbors(newNeighbors);
				neighbors.add(newJoineeIP);
				checkForNeighbors();
				newNeighbors.add(InetAddress.getLocalHost().getHostAddress()
						.toString());

			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Socket mySocket;
			try {
				mySocket = new Socket(this.newJoineeIP, portNumber);
				ObjectOutputStream out = new ObjectOutputStream(
						mySocket.getOutputStream());
				SendingPacket myPacket = new SendingPacket(newUpperPoint,
						newLowerPoint, NEW_SPACE, newNeighbors, newFileList);
				out.writeObject(myPacket);
			} catch (UnknownHostException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	static class PeerListener extends Thread {
		Socket myPeerConnectionSocket;

		PeerListener(Socket myPeerConnectionSocket) {
			this.myPeerConnectionSocket = myPeerConnectionSocket;
		}

		public void run() {
			try {
				ObjectInputStream inFromPeer = new ObjectInputStream(
						myPeerConnectionSocket.getInputStream());
				ObjectOutputStream outFromPeer = new ObjectOutputStream(
						myPeerConnectionSocket.getOutputStream());
				SendingPacket mySendingPacket = (SendingPacket) inFromPeer
						.readObject();
				// reads the keyword in the received object
				// if join then -->
				if (JOIN.equalsIgnoreCase(mySendingPacket.getKeyword())) {
					// checkForNeighbors();
					// updateNeighbors(neighbors);
					// Method to check if the point is in its region
					boolean isPointInPeer = checkPointInNode(mySendingPacket
							.getPoint());
					// If point is in peer, split that peer zone
					if (isPointInPeer) {
						if (JOIN.equalsIgnoreCase(mySendingPacket.keyword)) {
							PeerSplit splitPacket = new PeerSplit(
									mySendingPacket.newJoineeIP);
							splitPacket.start();
						}
					}
					// if not present in zone, forward
					if (!isPointInPeer) {
						// create thread to find closest peer
						PeerForward forwardingThread = new PeerForward(
								mySendingPacket.keyword,
								mySendingPacket.getNewJoineeIP(),
								mySendingPacket.getPoint().getX(),
								mySendingPacket.getPoint().getY());
						forwardingThread.start();
					}
				}

				if (PRINT.equalsIgnoreCase(mySendingPacket.keyword)) {
					Iterator iterator = neighbors.iterator();
					while (iterator.hasNext())
						System.out.println(iterator.toString());
				}

				// reusing constructor, hence different name
				if (SEARCH_PRINT_FOUND
						.equalsIgnoreCase(mySendingPacket.keyword)) {
					System.out.println("File found at "
							+ myPeerConnectionSocket.getRemoteSocketAddress()
									.toString());
					if (null != mySendingPacket.newNeighbors) {
						System.out.println("Path:");
						Iterator ite1 = mySendingPacket.newNeighbors.iterator();
						if (ite1.hasNext()) {
							ite1.next();
							ite1.remove();
						}
						while (ite1.hasNext()) {
							System.out.println(ite1.next().toString());
						}
						System.out.println(InetAddress.getLocalHost()
								.getHostAddress().toString());
					}
				}

				if ((FIND_DISTANCE_FOR_SEARCH
						.equalsIgnoreCase(mySendingPacket.keyword))) {
					System.out.println("in search find listener");
					boolean isPointInPeer = checkPointInNode(mySendingPacket
							.getPoint());
					if (isPointInPeer) {

						Iterator ite = fileList.iterator();
						while (ite.hasNext()) {
							if (ite.next()
									.toString()
									.equalsIgnoreCase(
											mySendingPacket.p.fileName)) {

								Socket mySocket = new Socket(
										mySendingPacket.path.get(0), portNumber);
								ObjectOutputStream out = new ObjectOutputStream(
										mySocket.getOutputStream());
								SendingPacket myPrintPacket = new SendingPacket(
										SEARCH_PRINT_FOUND,
										mySendingPacket.path);
								out.writeObject(myPrintPacket);

								System.out.println("File found at "
										+ InetAddress.getLocalHost()
												.getHostAddress().toString());

							}
						}

						SendingPacket myPacket = new SendingPacket(
								NEIGHBOR_FOUND_POINT, "");
						outFromPeer.writeObject(myPacket);
					} else {
						// IF NOT, WE FIND DISTANCE OF MID POINT TO THE NEW
						// JOINEE'S
						// POINT

						double midPointX = (Peer.myUpperPoint.getX() + Peer.myLowerPoint
								.getX()) / 2;
						double midPointY = (Peer.myUpperPoint.getY() + Peer.myLowerPoint
								.getY()) / 2;

						double distance = Math
								.sqrt(((midPointX - mySendingPacket.p.x) * (midPointX - mySendingPacket.p.x))
										+ ((midPointY - mySendingPacket.p.y) * (midPointY - mySendingPacket.p.y)));

						distance = Math.floor(distance * 100) / 100;

						String s = Double.toString(distance);

						SendingPacket doubleD = new SendingPacket(distance);

						outFromPeer.writeObject(doubleD);
					}
				}
				if (SEARCH.equalsIgnoreCase(mySendingPacket.keyword)) {
					boolean isPointInPeer = checkPointInNode(mySendingPacket
							.getPoint());
					if (isPointInPeer) {

						Iterator ite = fileList.iterator();
						while (ite.hasNext()) {
							if (ite.next()
									.toString()
									.equalsIgnoreCase(
											mySendingPacket.p.fileName)) {
								Socket mySocket = new Socket(
										mySendingPacket.path.get(0), portNumber);
								ObjectOutputStream out = new ObjectOutputStream(
										mySocket.getOutputStream());
								SendingPacket myPrintPacket = new SendingPacket(
										SEARCH_PRINT_FOUND,
										mySendingPacket.path);
								out.writeObject(myPrintPacket);
							}
						}

					} else {
						mySendingPacket.path.add(InetAddress.getLocalHost()
								.getHostAddress().toString());
						PeerForward forwardingThread = new PeerForward(
								mySendingPacket.keyword,
								mySendingPacket.getNewJoineeIP(),
								mySendingPacket.p, mySendingPacket.path);
						forwardingThread.start();
					}
				}

				if (INSERT.equalsIgnoreCase(mySendingPacket.keyword)) {
					boolean isPointInPeer = checkPointInNode(mySendingPacket
							.getPoint());
					if (isPointInPeer) {
						SendingPacket myPacket = new SendingPacket(
								NEIGHBOR_FOUND_POINT, "");
						outFromPeer.writeObject(myPacket);
						fileList.addAll(mySendingPacket.newFiles);
						if (null != mySendingPacket.p
								&& null != mySendingPacket.p.fileName
								&& !(mySendingPacket.p.fileName.isEmpty())) {
							fileList.add(mySendingPacket.p.fileName);
						}
						String newFile = fileList.get(fileList.size() - 1);
						System.out.println(fileList.get(fileList.size() - 1)
								+ " file added at "
								+ InetAddress.getLocalHost().getHostAddress()
										.toString());
					} else {
						PeerForward forwardingThread = new PeerForward(
								mySendingPacket.keyword,
								mySendingPacket.getNewJoineeIP(),
								mySendingPacket.p);
						forwardingThread.start();
					}
				}
				if ((FIND_DISTANCE_FOR_INSERT
						.equalsIgnoreCase(mySendingPacket.keyword))) {
					boolean isPointInPeer = checkPointInNode(mySendingPacket
							.getPoint());
					if (isPointInPeer) {
						SendingPacket myPacket = new SendingPacket(
								NEIGHBOR_FOUND_POINT, "");
						outFromPeer.writeObject(myPacket);
						fileList.addAll(mySendingPacket.newFiles);
						if (null != mySendingPacket.p
								&& null != mySendingPacket.p.fileName
								&& !(mySendingPacket.p.fileName.isEmpty()))
							fileList.add(mySendingPacket.p.fileName);
						System.out.println("file added at "
								+ InetAddress.getLocalHost().getHostAddress()
										.toString());
					} else {
						// IF NOT, WE FIND DISTANCE OF MID POINT TO THE NEW
						// JOINEE'S
						// POINT
						double midPointX = (Peer.myUpperPoint.getX() + Peer.myLowerPoint
								.getX()) / 2;
						double midPointY = (Peer.myUpperPoint.getY() + Peer.myLowerPoint
								.getY()) / 2;
						double distance = Math
								.sqrt(((midPointX - mySendingPacket.p.x) * (midPointX - mySendingPacket.p.x))
										+ ((midPointY - mySendingPacket.p.y) * (midPointY - mySendingPacket.p.y)));

						distance = Math.floor(distance * 100) / 100;
						String s = Double.toString(distance);
						SendingPacket doubleD = new SendingPacket(distance);
						outFromPeer.writeObject(doubleD);
					}
				}

				if (MUTUAL_REMOVE.equalsIgnoreCase(mySendingPacket.keyword)) {
					String remoteSocketAddress = myPeerConnectionSocket
							.getRemoteSocketAddress().toString();
					remoteSocketAddress = remoteSocketAddress.substring(1);
					int index = remoteSocketAddress.indexOf(":");
					remoteSocketAddress = remoteSocketAddress.substring(0,
							index);
					// ArrayList<String> temp = new ArrayList<String>();
					Iterator ite = neighbors.iterator();
					boolean isPresent = false;
					while (ite.hasNext()) {
						if (ite.next().toString()
								.equalsIgnoreCase(remoteSocketAddress)) {
							ite.remove();
						}
					}
				}
				// Peer gets a request to find if point is in the peer, if not,
				// distance of its centre from currentPoint is calculated
				if (FIND_DISTANCE
						.equalsIgnoreCase(mySendingPacket.getKeyword())) {

					if (checkPointInNode(mySendingPacket.getPoint())) {
						PeerSplit splitPacket = new PeerSplit(
								mySendingPacket.newJoineeIP);
						splitPacket.start();
						SendingPacket myPacket = new SendingPacket(
								NEIGHBOR_FOUND_POINT, "");
						outFromPeer.writeObject(myPacket);
					} else {

						// IF NOT, WE FIND DISTANCE OF MID POINT TO THE NEW
						// JOINEE'S
						// POINT

						double midPointX = (Peer.myUpperPoint.getX() + Peer.myLowerPoint
								.getX()) / 2;
						double midPointY = (Peer.myUpperPoint.getY() + Peer.myLowerPoint
								.getY()) / 2;

						double distance = Math
								.sqrt(((midPointX - mySendingPacket.p.x) * (midPointX - mySendingPacket.p.x))
										+ ((midPointY - mySendingPacket.p.y) * (midPointY - mySendingPacket.p.y)));

						distance = Math.floor(distance * 100) / 100;
						String s = Double.toString(distance);
						SendingPacket doubleD = new SendingPacket(distance);
						outFromPeer.writeObject(doubleD);
					}
				}

				if (NEW_SPACE.equalsIgnoreCase(mySendingPacket.keyword)) {
					myUpperPoint.x = mySendingPacket.newUpperPoint.x;
					myLowerPoint.x = mySendingPacket.newLowerPoint.x;
					myUpperPoint.y = mySendingPacket.newUpperPoint.y;
					myLowerPoint.y = mySendingPacket.newLowerPoint.y;
					Iterator ite = mySendingPacket.newNeighbors.iterator();
					while (ite.hasNext()) {
						System.out.println(ite.next().toString());
					}
					neighbors.addAll(mySendingPacket.newNeighbors);
					// checkForNeighbors();
					updateNeighbors(neighbors);
					checkForNeighbors();
					fileList.addAll(mySendingPacket.newFiles);
					display();
					// CODE TO REFRESH NEIGHBORS
				}
				if (VIEW.equalsIgnoreCase(mySendingPacket.keyword)) {
					display();
				}
				if (AM_I_NEIGHBOR.equalsIgnoreCase(mySendingPacket.keyword)) {
					String remoteSocketAddress = myPeerConnectionSocket
							.getRemoteSocketAddress().toString();
					remoteSocketAddress = remoteSocketAddress.substring(1);
					int index = remoteSocketAddress.indexOf(":");
					remoteSocketAddress = remoteSocketAddress.substring(0,
							index);
					// ArrayList<String> temp = new ArrayList<String>();
					Iterator ite = neighbors.iterator();
					boolean isPresent = false;
					while (ite.hasNext()) {
						if (ite.next().toString()
								.equalsIgnoreCase(remoteSocketAddress)) {
							isPresent = true;
						}
					}
					if (!isPresent) {
						neighbors.add(remoteSocketAddress);
					}
				}
				if (GIVE_POINTS.equalsIgnoreCase(mySendingPacket.keyword)) {
					SendingPacket pointPacket = new SendingPacket("",
							myUpperPoint, myLowerPoint);
					outFromPeer.writeObject(pointPacket);
				}
				myPeerConnectionSocket.close();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		private boolean comparePoint(Point fileToSearch, Point point) {
			if (fileToSearch.x == point.x && fileToSearch.y == point.y)
				return true;
			else
				return false;
		}

		private boolean checkForFile(Point p) {
			Iterator ite = fileList.iterator();
			if (!ite.hasNext())
				return false;
			if (ite.hasNext()) {
				if (comparePoint(p, (Point) ite.next()))
					return true;
			}
			return false;
		}

	}

	public static boolean checkPointInNode(Point point) {
		if (point.getX() < Peer.myUpperPoint.getX()
				&& point.getX() > Peer.myLowerPoint.getX()
				&& point.getY() < Peer.myUpperPoint.getY()
				&& point.getY() > Peer.myLowerPoint.getY())
			return true;
		else
			return false;
	}

	static class PeerServerSocket extends Thread {
		int listeningPort;
		ServerSocket myServerSocket;

		public PeerServerSocket(int listeningPort) {
			this.listeningPort = listeningPort;
			try {
				myServerSocket = new ServerSocket(listeningPort);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void run() {
			while (true) {
				try {
					Socket myClient = myServerSocket.accept();
					PeerListener p1 = new PeerListener(myClient);
					p1.start();

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	static public void display() {
		System.out.println("Upper X" + myUpperPoint.x);
		System.out.println("Upper Y" + myUpperPoint.y);
		System.out.println("Lower X" + myLowerPoint.x);
		System.out.println("Lower Y" + myLowerPoint.y);
		System.out.println("NEIGHBORS:");
		Iterator myIterator = neighbors.iterator();
		while (myIterator.hasNext()) {
			System.out.println(myIterator.next());
		}
		System.out.println("END OF NEIGHBORS");
		Iterator ite1 = fileList.iterator();
		System.out.println("FILE LIST: ");
		while (ite1.hasNext()) {
			System.out.println(ite1.next().toString());
		}
		try {
			System.out.println("CURRENT NODE:    "
					+ InetAddress.getLocalHost().toString());
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void updateNeighbors(ArrayList<String> myNeighbors) {
		try {
			Iterator ite = myNeighbors.iterator();
			while (ite.hasNext()) {
				String[] ipaddress = ite.next().toString().split(" ");
				Socket mySocket = new Socket(ipaddress[0], portNumber);
				SendingPacket myPacket = new SendingPacket(AM_I_NEIGHBOR,
						neighbors);
				ObjectOutputStream out = new ObjectOutputStream(
						mySocket.getOutputStream());
				out.writeObject(myPacket);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void checkForNeighbors() {
		try {
			ArrayList<String> tempNeighbors = new ArrayList<String>();
			// tempNeighbors.addAll(neighbors);
			Iterator<String> ite = neighbors.iterator();
			while (ite.hasNext()) {
				String[] ipaddress = ite.next().toString().split(" ");
				Socket mySocket = new Socket(ipaddress[0], portNumber);
				SendingPacket myPacket = new SendingPacket(GIVE_POINTS, "");
				ObjectOutputStream out = new ObjectOutputStream(
						mySocket.getOutputStream());
				out.writeObject(myPacket);
				ObjectInputStream in = new ObjectInputStream(
						mySocket.getInputStream());
				SendingPacket inPacket = (SendingPacket) in.readObject();
				double lowerX = inPacket.newLowerPoint.x;
				double lowerY = inPacket.newLowerPoint.y;
				double upperX = inPacket.newUpperPoint.x;
				double upperY = inPacket.newUpperPoint.y;

				if ((lowerX == myUpperPoint.x && lowerY == myUpperPoint.y)
						|| (upperX == myLowerPoint.x && upperY == myLowerPoint.y)
						|| (lowerX == myLowerPoint.x && upperY == myUpperPoint.y)) {
					myPacket.keyword = MUTUAL_REMOVE;
					out.writeObject(myPacket);
					ite.remove();
				}
			}
			Iterator<String> ite1 = neighbors.iterator();
			while (ite1.hasNext()) {
				String[] ipaddress = ite1.next().toString().split(" ");

				Socket mySocket = new Socket(ipaddress[0], portNumber);
				SendingPacket myPacket = new SendingPacket(GIVE_POINTS, "");
				ObjectOutputStream out = new ObjectOutputStream(
						mySocket.getOutputStream());
				out.writeObject(myPacket);
				ObjectInputStream in = new ObjectInputStream(
						mySocket.getInputStream());
				SendingPacket inPacket = (SendingPacket) in.readObject();
				double lowerX = inPacket.newLowerPoint.x;
				double lowerY = inPacket.newLowerPoint.y;
				double upperX = inPacket.newUpperPoint.x;
				double upperY = inPacket.newUpperPoint.y;

				if (!(lowerX == myLowerPoint.x || lowerX == myUpperPoint.x
						|| upperX == myLowerPoint.x || upperX == myUpperPoint.x
						|| lowerY == myLowerPoint.y || lowerY == myUpperPoint.y
						|| upperY == myLowerPoint.y || upperY == myUpperPoint.y)) {
					myPacket.keyword = MUTUAL_REMOVE;
					out.writeObject(myPacket);

					ite1.remove();
				}
			}

			Iterator<String> ite2 = neighbors.iterator();
			while (ite2.hasNext()) {
				String[] ipaddress = ite2.next().toString().split(" ");
				Socket mySocket = new Socket(ipaddress[0], portNumber);
				SendingPacket myPacket = new SendingPacket(GIVE_POINTS, "");
				ObjectOutputStream out = new ObjectOutputStream(
						mySocket.getOutputStream());
				out.writeObject(myPacket);
				ObjectInputStream in = new ObjectInputStream(
						mySocket.getInputStream());
				SendingPacket inPacket = (SendingPacket) in.readObject();
				double lowerX = inPacket.newLowerPoint.x;
				double lowerY = inPacket.newLowerPoint.y;
				double upperX = inPacket.newUpperPoint.x;
				double upperY = inPacket.newUpperPoint.y;

				if (upperX == myUpperPoint.x) {
					if ((upperY != myLowerPoint.y)
							&& (lowerY != myUpperPoint.y)) {
						myPacket.keyword = MUTUAL_REMOVE;
						out.writeObject(myPacket);
						ite2.remove();
					}
				} else if (lowerX == myLowerPoint.x) {
					if ((upperY != myLowerPoint.y)
							&& (lowerY != myUpperPoint.y)) {
						myPacket.keyword = MUTUAL_REMOVE;
						out.writeObject(myPacket);
						ite2.remove();
					}
				} else if ((upperY == myUpperPoint.y)) {
					if ((upperX != myLowerPoint.x)
							&& (lowerX != myUpperPoint.x)) {
						myPacket.keyword = MUTUAL_REMOVE;
						out.writeObject(myPacket);
						ite2.remove();
					}
				} else if ((lowerY == myLowerPoint.y)) {
					if ((upperX != myLowerPoint.x)
							&& (lowerX != myUpperPoint.x)) {
						myPacket.keyword = MUTUAL_REMOVE;
						out.writeObject(myPacket);
						ite2.remove();
					}
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws IOException,
			ClassNotFoundException {
		Peer myPeer = new Peer();
		String myCommand, rootIP;
		Scanner inn = new Scanner(System.in);
		System.out.println("Enter server IP");
		serverIP = inn.nextLine();
		System.out.println("Enter server port: " + System.currentTimeMillis());
		serverPort = new Integer(inn.next()).intValue();
		System.out.println("Enter peer port: ");
		portNumber = new Integer(inn.next()).intValue();
		PeerServerSocket p1 = new PeerServerSocket(portNumber);
		p1.start();
		while (true) {

			System.out.println("Command: ");
			Scanner in = new Scanner(System.in);
			myCommand = in.nextLine();
			String[] stringArray = myCommand.split(" ");
			if (stringArray.length == 1) {
				if (JOIN.equalsIgnoreCase(myCommand)) {
					rootIP = getRootIP();
					System.out.println(rootIP);
					System.out.println(InetAddress.getLocalHost()
							.getHostAddress().toString());
					if (!rootIP.equalsIgnoreCase(InetAddress.getLocalHost()
							.getHostAddress().toString())) {
						// get random point for the new node
						double x = (((Math.random() * 90) + 10) / 10), y = (float) (((Math
								.random() * 90) + 10) / 10);
//						System.out.println("Random join x" + x);
//						System.out.println("Random join y" + y);
						// make connection to ROOT IP | First IP to join CAN
						Socket peerToPeer = new Socket(rootIP, portNumber);
						ObjectOutputStream outPeer = new ObjectOutputStream(
								peerToPeer.getOutputStream());
						ObjectInputStream inPeer = new ObjectInputStream(
								peerToPeer.getInputStream());
						// Object of destination point
						Point destinationPoint = new Point(x, y);
						// Send the destination point, keyword Join and address
						// of
						// new joinee
						SendingPacket mySendingPacket = new SendingPacket(
								destinationPoint, JOIN, InetAddress
										.getLocalHost().getHostAddress()
										.toString());
						outPeer.writeObject(mySendingPacket);
					} else {
						Peer.myLowerPoint.x = 0;
						Peer.myLowerPoint.y = 0;
						Peer.myUpperPoint.x = 10;
						Peer.myUpperPoint.y = 10;
						display();
					}

					// PeerListener myPeerListener = new PeerListener();
					// myPeerListener.start();
				}
				if (VIEW.equalsIgnoreCase(myCommand)) {
					display();
				}
				if (EXIT.equalsIgnoreCase(myCommand)) {
					System.exit(0);
				}
			} else {

				if (SEARCH.equalsIgnoreCase(stringArray[0])) {
					ArrayList<String> path = new ArrayList<String>();
					path.add(InetAddress.getLocalHost().getHostAddress()
							.toString());
					Point destinationPoint = findHashValue(stringArray[1]);
					SendingPacket mySendingacket = new SendingPacket(
							destinationPoint, SEARCH, path);
					Socket mySocket = new Socket(InetAddress.getLocalHost()
							.getHostAddress().toString(), portNumber);
					ObjectOutputStream out = new ObjectOutputStream(
							mySocket.getOutputStream());
					out.writeObject(mySendingacket);
				}
				if (INSERT.equalsIgnoreCase(stringArray[0])) {
					Point desPoint = findHashValue(stringArray[1]);
					SendingPacket mySendingPacket = new SendingPacket(desPoint,
							INSERT, InetAddress.getLocalHost().getHostAddress()
									.toString());
					Socket mySocket = new Socket(InetAddress.getLocalHost()
							.getHostAddress().toString(), portNumber);
					ObjectOutputStream out = new ObjectOutputStream(
							mySocket.getOutputStream());
					out.writeObject(mySendingPacket);
				}
			}
		}
	}

	private static Point findHashValue(String myCommand) {
		double x = 0, y = 0;
		int sumX = 0, sumY = 0;
		for (int i = 0; i < myCommand.length(); i++) {
			if (i % 2 == 0) {
				sumY += myCommand.charAt(i);
			} else {
				sumX += myCommand.charAt(i);
			}
		}
		x = (sumX % 10) + 0.1;
		System.out.println("hash value x " + x);

		y = (sumY % 10) + 0.1;
		System.out.println("hashvalue y " + y);
		return new Point(x, y, myCommand);
	}
}
