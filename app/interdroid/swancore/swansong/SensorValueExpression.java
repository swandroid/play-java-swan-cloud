package interdroid.swancore.swansong;


import java.util.HashMap;

public class SensorValueExpression implements ValueExpression {

	private String mLocation;
	private String mEntity;
	private String mValuePath;
	private HashMap<String,String> mConfig;
	private HistoryReductionMode mMode;
	private long mHistoryLength;
	private HashMap<String,String> mHttpConfig;

	public SensorValueExpression(String location, String entity,
			String valuePath, HashMap<String,String> config, HistoryReductionMode mode,
			long historyLength, HashMap<String,String> httpConfig) {
		mLocation = location;
		mEntity = entity;
		mValuePath = valuePath;
		mConfig = config;
		if (mConfig == null) {
			mConfig = new HashMap<String,String>();
		}
		mMode = mode;
		mHistoryLength = historyLength;
		mHttpConfig = httpConfig;
		if (mHttpConfig == null) {

			mHttpConfig = new HashMap<String,String>();
		}



	}

	@Override
	public HistoryReductionMode getHistoryReductionMode() {
		return mMode;
	}

	public long getHistoryLength() {
		return mHistoryLength;
	}

	@Override
	public String toParseString() {
		String result = mLocation + "@" + mEntity + ":" + mValuePath;
		if (mConfig != null && mConfig.size() > 0) {
			boolean first = true;
			for (String key : mConfig.keySet()) {
				String value = "" + mConfig.get(key);
				if (mConfig.get(key) instanceof String) {
					value = "'" + value + "'";
				}
				result += (first ? "?" : "#") + key + "=" + value;
				first = false;
			}
		}
		if (mHttpConfig != null && mHttpConfig.size() > 0) {
			boolean first = true;
			for (String key : mHttpConfig.keySet()) {
				String value = "" + mHttpConfig.get(key);
				//if (mHttpConfig.get(key) instanceof String) {
				//	value = "'" + value + "'";
				//}

				result += (first ? "$" : "~") + key + "=" + value;
				first = false;
			}
		}
		result += "{" + mMode.toParseString() + "," + mHistoryLength + "}";
		return result;
	}

	public String getEntity() {
		return mEntity;
	}

	@Override
	public void setInferredLocation(String location) {
		throw new RuntimeException(
				"Please don't use this method. For internal use only.");
	}

	public String getValuePath() {
		return mValuePath;
	}

	public String getLocation() {
		return mLocation;
	}

	public HashMap<String,String> getConfiguration() {
		return mConfig;
	}

	public HashMap<String,String> getHttConfiguration(){

		return mHttpConfig;
	}


}
