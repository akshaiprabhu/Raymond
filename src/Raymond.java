import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

/**
 * The class is the implementation of Raymond's Mutual Exclusion algorithm
 * 
 * @author Akshai Prabhu
 *
 */
public class Raymond {

	public static String holder; // holder for each host
	public static RequestServer req_server; // server thread to receive request
											// message
	public static TokenServer tok_server; // server thread to receive token
											// message
	public static CriticalSection cs_req_server; // server thread to receive
													// 'access
													// shared data message'
	public static ReleaseCS rel_tok_server; // server thread to receive 'release
											// shared data message'
	public static Client client; // client thread
	public static boolean token; // token variable
	public Queue<String> queue; // host queue
	public static boolean CS; // variable to set and reset when in and out of
								// critical section respectively
	public static StringBuffer csBuffer; // shared data

	/**
	 * Constructor
	 */
	public Raymond() {
		holder = new String();
		initialize();
		queue = new LinkedList<String>();
		CS = false;
		csBuffer = new StringBuffer();
	}

	/**
	 * To initialize all variable according to the host conditions
	 */
	private void initialize() {
		try {
			if (InetAddress.getLocalHost().getHostAddress().equals("129.21.22.196")) { // glados
				holder = "129.21.22.196";
				token = true; // initially token is with glados
				System.out.println("*Token*");
				System.out.println("Holder: " + holder);
			} else if (InetAddress.getLocalHost().getHostAddress().equals("129.21.37.18")) { // kansas
				holder = "129.21.22.196";
				System.out.println("Holder: " + holder);
				token = false;
			} else if (InetAddress.getLocalHost().getHostAddress().equals("129.21.37.16")) { // newyork
				holder = "129.21.22.196";
				token = false;
				System.out.println("Holder: " + holder);
			} else if (InetAddress.getLocalHost().getHostAddress().equals("129.21.37.15")) { // arizona
				holder = "129.21.37.18";
				token = false;
				System.out.println("Holder: " + holder);
			} else if (InetAddress.getLocalHost().getHostAddress().equals("129.21.37.8")) { // missouri
				holder = "129.21.37.18";
				token = false;
				System.out.println("Holder: " + holder);
			} else if (InetAddress.getLocalHost().getHostAddress().equals("129.21.37.23")) { // california
				holder = "129.21.37.16";
				token = false;
				System.out.println("Holder: " + holder);
			} else if (InetAddress.getLocalHost().getHostAddress().equals("129.21.37.9")) { // nebraska
				holder = "129.21.37.16";
				token = false;
				System.out.println("Holder: " + holder);
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Main method
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		Raymond raymond = new Raymond();
		req_server = raymond.new RequestServer();
		tok_server = raymond.new TokenServer();
		cs_req_server = raymond.new CriticalSection();
		rel_tok_server = raymond.new ReleaseCS();
		client = raymond.new Client();

		tok_server.start();
		req_server.start();
		cs_req_server.start();
		rel_tok_server.start();
		client.start();
	}

	/**
	 * Thread to listen to request messages
	 * 
	 * @author Akshai Prabhu
	 *
	 */
	class RequestServer extends Thread {
		ServerSocket serverSocket;
		Socket server;

		/**
		 * Constructor
		 */
		public RequestServer() {

		}

		/**
		 * Thread run method
		 */
		public void run() {
			while (true) {
				try {
					serverSocket = new ServerSocket(60000);
					server = serverSocket.accept();

					DataInputStream in = new DataInputStream(server.getInputStream());
					String message = in.readUTF();
					System.out.println("Request from: " + message);
					server.close();
					serverSocket.close();
					uponRequest(message);
				} catch (IOException e) {
					e.printStackTrace();
					break;
				}
			}
		}

		/**
		 * Upon receipt of request message
		 * 
		 * @param message
		 */
		private void uponRequest(String message) {
			System.out.println("Before request, Queue: " + queue);
			queue.add(message); // add to queue
			System.out.println("After request, Queue: " + queue);
			if (token) { // if has token
				if (!CS) { // if not using critical section
					String first = queue.remove(); // dequeue
					System.out.println("Before request, Holder: " + holder);
					holder = first; // update holder
					System.out.println("After request, Holder: " + holder);
					client.sendToken(first); // send token to first message in
												// queue
					token = false;
					
					
				}
			} else {
				client.sendRequest(); // send request to holder
			}
		}

	}

	/**
	 * Listens to token messages
	 * 
	 * @author Akshai Prabhu
	 *
	 */
	class TokenServer extends Thread {
		ServerSocket serverSocket;
		Socket server;

		/**
		 * Constructor
		 */
		public TokenServer() {

		}

		/**
		 * Thread run method
		 */
		public void run() {
			while (true) {
				try {
					serverSocket = new ServerSocket(50000);
					server = serverSocket.accept();

					DataInputStream in = new DataInputStream(server.getInputStream());
					System.out.println("Token from: " + in.readUTF());
					server.close();
					serverSocket.close();
					uponToken();
				} catch (IOException e) {
					e.printStackTrace();
					break;
				}
			}
		}

		/**
		 * Upon receipt of token message
		 */
		private void uponToken() {
			System.out.println("Before remove, Queue: " + queue);
			String first = queue.remove();
			System.out.println("After remove, Queue: " + queue);
			token = true;
			System.out.println("*Token*");
			try {
				// if you are the first element in queue
				if (first.equals(InetAddress.getLocalHost().getHostAddress())) {
					holder = InetAddress.getLocalHost().getHostAddress(); // holder
																			// =
																			// self
					enterCS(); // use critical section
				} else {
					System.out.println("Before token, Holder: " + holder);
					holder = first;
					token = false;
					client.sendToken(first);
					System.out.println("After token, Holder: " + holder);
					if (queue.size() != 0) { // send request if you need the
												// token back in future for
												// other requests in queue
						client.sendRequest();
					}
				}
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}

		}
	}

	/**
	 * Listens to release critical section and initiate exit critical section
	 * operation
	 * 
	 * @author Akshai Prabhu
	 *
	 */
	class ReleaseCS extends Thread {
		ServerSocket serverSocket;
		Socket server;

		/**
		 * Constructor
		 */
		public ReleaseCS() {

		}

		/**
		 * Thread run method
		 */
		public void run() {
			while (true) {
				try {
					serverSocket = new ServerSocket(55000);
					server = serverSocket.accept();

					DataInputStream in = new DataInputStream(server.getInputStream());
					String message = in.readUTF();
					if (message.equals("Released shared data")) {
						onExitCS(); // on exit critical section
					}
					server.close();
					serverSocket.close();
				} catch (IOException e) {
					e.printStackTrace();
					break;
				}
			}
		}
	}

	/**
	 * Listens to access critical section message
	 * 
	 * @author Akshai Prabhu
	 *
	 */
	class CriticalSection extends Thread {
		ServerSocket serverSocket;
		Socket server;

		/**
		 * Constructor
		 */
		public CriticalSection() {

		}

		/**
		 * Thread run method
		 */
		public void run() {
			while (true) {
				try {
					serverSocket = new ServerSocket(45000);
					server = serverSocket.accept();

					DataInputStream in = new DataInputStream(server.getInputStream());
					String message = in.readUTF();
					csBuffer.append(message + "\t");
					server.close();
					serverSocket.close();
					// display updated shared data access information
					System.out.println("Using CS: " + message);
					System.out.println("Shared data: " + csBuffer.toString());

					client.sendTokenRelease(message);

				} catch (IOException e) {
					e.printStackTrace();
					break;
				}
			}
		}
	}

	/**
	 * Client thread to send messages
	 * 
	 * @author Akshai
	 *
	 */
	class Client extends Thread {
		Scanner sc;

		public Client() {

		}

		/**
		 * Thread run method
		 */
		public void run() {
			while (true) {
				sc = new Scanner(System.in);
				System.out.println("Press 0 to enter CS");
				String input = sc.nextLine();
				if (input.equals("0")) {
					initiateRequest();
				} else {
					System.out.println("Please enter valid input");
				}
			}
		}

		/**
		 * To initiate critical section access request
		 */
		private void initiateRequest() {
			try {
				// if holder = self
				if (holder.equals(InetAddress.getLocalHost().getHostAddress())) {
					enterCS();
				} else { // if holder != self
					queue.add(InetAddress.getLocalHost().getHostAddress()); // add
																			// self
																			// to
																			// queue
					sendRequest(); // send request to holder
				}
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}

		}

		/**
		 * To send shared data release message
		 * 
		 * @param ip
		 */
		public void sendTokenRelease(String ip) {
			Socket client;
			try {
				client = new Socket(ip, 55000);
				OutputStream outToServer = client.getOutputStream();
				DataOutputStream out = new DataOutputStream(outToServer);
				out.writeUTF("Released shared data");
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		/**
		 * To send request message to holder
		 */
		private void sendRequest() {
			Socket client;
			try {
				client = new Socket(holder, 60000);
				OutputStream outToServer = client.getOutputStream();
				DataOutputStream out = new DataOutputStream(outToServer);
				System.out.println("Sending request to holder: " + holder);
				out.writeUTF(InetAddress.getLocalHost().getHostAddress());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		/**
		 * To send token to first host in queue
		 * 
		 * @param first
		 */
		private void sendToken(String first) {
			Socket client;
			try {
				client = new Socket(first, 50000);
				OutputStream outToServer = client.getOutputStream();
				DataOutputStream out = new DataOutputStream(outToServer);
				System.out.println("Sending token to holder: " + holder);
				out.writeUTF(InetAddress.getLocalHost().getHostAddress());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		/**
		 * To send shared resource access message
		 */
		public void sendCSAccess() {
			Socket client;
			try {
				client = new Socket("129.21.22.196", 45000);
				OutputStream outToServer = client.getOutputStream();
				DataOutputStream out = new DataOutputStream(outToServer);
				out.writeUTF(InetAddress.getLocalHost().getHostAddress());
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

	/**
	 * Tasks to perform while using critical section
	 */
	public void enterCS() {
		System.out.println("Enter Critical Section");
		CS = true;
		try {
			// if shared data host is not self
			if (!InetAddress.getLocalHost().getHostName().equals("glados")) {
				client.sendCSAccess();
			} else {// if shared data host is self
				csBuffer.append(InetAddress.getLocalHost().getHostAddress() + " ");

				System.out.println("Using CS: " + InetAddress.getLocalHost().getHostAddress());
				System.out.println("Shared data: " + csBuffer.toString());

				onExitCS();
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Tasks to perform after exiting critical section
	 */
	public void onExitCS() {
		if (queue.size() != 0) { // if queue not empty
			System.out.println("Before remove Queue: " + queue);
			String first = queue.remove();
			System.out.println("After remove Queue: " + queue);
			client.sendToken(first); // send token to first host in queue
			holder = first;
			System.out.println("Holder: " + holder); // display holder change
			if (queue.size() != 0) {
				client.sendRequest(); // send request to holder if token needed
										// for other processes in queue
			}
		}
		CS = false;
		System.out.println("Exiting Critical Section");
	}

}
