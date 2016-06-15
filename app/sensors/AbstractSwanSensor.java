package sensors;

/**
 * Created by Roshan Bharath Das on 08/06/16.
 */


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import engine.EvaluationEngineService;
import swansong.TimestampedValue;




abstract class AbstractSwanSensor implements SensorInterface {




    protected AbstractSwanSensor(){

        for (final String valuePath : getValuePaths()) {
            //expressionIdsPerValuePath.put(valuePath, new ArrayList<String>());
            getValues().put(valuePath,
                    Collections.synchronizedList(new ArrayList<TimestampedValue>()));
        }

    }


    public abstract String[] getValuePaths();

    private final Map<String, List<TimestampedValue>> values = new HashMap<String, List<TimestampedValue>>();

    //protected final Map<String, List<String>> expressionIdsPerValuePath = new HashMap<String, List<String>>();
    private final Map<String, String> registeredValuePaths = new HashMap<String, String>();
    //protected final Map<String, HashMap> registeredConfigurations = new HashMap<String, HashMap>();



    public final Map<String, List<TimestampedValue>> getValues() {
        return values;
    }



    protected final void putValueTrimSize(final String valuePath,
                                          final String id, final long now, final Object value /*, final int historySize*/) {


        //System.out.println("putValueTrimSize:"+value);
        try {
            getValues().get(valuePath).add(new TimestampedValue(value, now));
        } catch (OutOfMemoryError e) {
            onDestroySensor();
        }


        if (id != null) {
            notifyDataChangedForId(id);
        }// else {
        //    notifyDataChanged(valuePath);
        // }


    }



    protected final void notifyDataChangedForId(final String... ids) {
        // Intent notifyIntent = new Intent(ACTION_NOTIFY);
        //notifyIntent.putExtra("expressionIds", ids);
        //sendBroadcast(notifyIntent);

        EvaluationEngineService instance = EvaluationEngineService.getInstance();
        instance.doNotify(ids);

    }



    protected static final List<TimestampedValue> getValuesForTimeSpan(
            final List<TimestampedValue> values, final long now,
            final long timespan) {
        List<TimestampedValue> result = new ArrayList<TimestampedValue>();
        if (timespan == 0) {
            if (values != null && values.size() > 0) {
                result.add(values.get(values.size() - 1));    //item in the last position has the latest timestamp
            }
        } else {
            if (values != null) {
                for (int i = values.size() - 1; i >= 0; i--) {
                    if ((now - timespan) <= values.get(i).getTimestamp()) {
                        if (now >= values.get(i).getTimestamp())    //it shouldn't be a future value
                            result.add(values.get(i));
                    } else {    //stop when it reaches too outdated values
                        break;
                    }
                }
            }
        }

        return result;
    }



    @Override
    public void register(String id, String valuePath, HashMap configuration, HashMap httpConfiguration) {

        //registeredConfigurations.put(id, configuration);
        registeredValuePaths.put(id, valuePath);
       /* List<String> ids = expressionIdsPerValuePath.get(valuePath);
        if (ids == null) {
            ids = new ArrayList<String>();
            expressionIdsPerValuePath.put(valuePath, ids);
        }
        ids.add(id);*/


    }


    @Override
    public void unregister(final String id){

        //registeredConfigurations.remove(id);
        String valuePath = registeredValuePaths.remove(id);
        //expressionIdsPerValuePath.get(valuePath).remove(id);

    }



    @Override
    public List<TimestampedValue> getValues(String id, long now, long timespan) {

        List<TimestampedValue> valuesForTimeSpan = null;

        try {
            valuesForTimeSpan = getValuesForTimeSpan(getValues().get(registeredValuePaths.get(id)), now, timespan);
        } catch (OutOfMemoryError e) {

            onDestroySensor();
        }
        return valuesForTimeSpan;


    }



    @Override
    public void onDestroySensor() {

    }

    @Override
    public void onConnected() {

    }

    @Override
    public long getStartUpTime(String id) {
        return 0;
    }

    @Override
    public double getAverageSensingRate() {
        return 0;
    }





}
