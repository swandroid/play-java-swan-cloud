package sensors.impl; 


import org.json.JSONException; 
import org.json.JSONObject;
import sensors.base.AbstractSwanSensor;
import sensors.base.SensorPoller;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;


public class TreeSensor extends AbstractSwanSensor {

	private Map<String, FogPoller> activeThreads = new HashMap<String, FogPoller>();
	public static final String[] VALUEPATH = {"branch"};
	public static final String[] CONFIGURATION = {"leaves"};
	public static final String ENTITY = "tree";
	public static final int PORT = 7782;



	class FogPoller extends SensorPoller {

		private Map<String, PollThread> activeThreads = new HashMap<String, PollThread>();

		ServerSocket server;
		Socket socket;
		String id;

		ObjectInputStream ois;
		FogPoller(String id, String valuePath, HashMap configuration) {
			super(id, valuePath, configuration);
			try {
				server = new ServerSocket(PORT);

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		public void run() {
			while (!isInterrupted()) {

				try {
					socket = server.accept();
				} catch (IOException e) {
					e.printStackTrace();
				}

				PollThread pollThread = new PollThread(socket, FogPoller.this);
				activeThreads.put(id, pollThread);
				pollThread.start();

			}
			activeThreads.remove(id).interrupt();

		}
	}




	public class PollThread extends Thread {
		protected Socket socket;

		SensorPoller sensorPoller;

		public PollThread(Socket clientSocket, SensorPoller sensorPoller) {
			this.socket = clientSocket;
			this.sensorPoller = sensorPoller;
		}

		public void run() {


			ObjectInputStream ois = null;
			try {
				ois = new ObjectInputStream(socket.getInputStream());

			} catch (IOException e) {
				e.printStackTrace();
			}


			while (!isInterrupted()) {
					try {
						String message = (String) ois.readObject();
						JSONObject json = new JSONObject(message);
						//System.out.println("message:"+message);
						sensorPoller.updateResult(TreeSensor.this, json.get("data"), json.getLong("time"));
					} catch (JSONException e) {
						e.printStackTrace();
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}

			}
			}

	}


	@Override
	public void register(String id, String valuePath, HashMap configuration, HashMap httpConfiguration) {
		super.register(id,valuePath,configuration, httpConfiguration);
		FogPoller fogPoller = new FogPoller(id, valuePath,
			configuration);
		activeThreads.put(id, fogPoller);
		fogPoller.start();
	}

	@Override
	public void unregister(String id) {
		super.unregister(id);
		System.out.println("Unregister sensor called");
		activeThreads.remove(id).interrupt();
	}

	@Override
	public String[] getValuePaths()  {
		return VALUEPATH;
	}

	@Override
	public String getEntity() {
		return ENTITY;
	}

	@Override
	public String[] getConfiguration() {
		return CONFIGURATION;
	}

}