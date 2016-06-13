package engine;

/**
 * Created by goose on 09/06/16.
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import swansong.Expression;
import swansong.TimestampedValue;
import swansong.TriState;
import swansong.TriStateExpression;
import swansong.ValueExpression;

public class ExpressionManager {

    private static final String TAG = "ExpressionManager";


    public static final String ACTION_REGISTER = "interdroid.swan.REGISTER";


    public static final String ACTION_UNREGISTER = "interdroid.swan.UNREGISTER";


    public static final String ACTION_NEW_VALUES = "interdroid.swan.NEW_VALUES";


    public static final String ACTION_NEW_TRISTATE = "interdroid.swan.NEW_TRISTATE";

    public static final String EXTRA_NEW_VALUES = "values";

    public static final String EXTRA_NEW_TRISTATE = "tristate";


    public static final String EXTRA_NEW_TRISTATE_TIMESTAMP = "timestamp";



    private static Map<String, ExpressionListener> sListeners = new HashMap<String, ExpressionListener>();

    static EvaluationEngineService evaluationEngineService = EvaluationEngineService.getInstance();

    private static boolean sReceiverRegistered = false;

    /**
     * Broadcast receiver used in case values have to be forwarded to listeners
     */
  /*  private static BroadcastReceiver sReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String id = intent.getData().getFragment();
            Log.d(TAG, "on receive");
            if (sListeners.containsKey(id)) {
                if (intent.getAction().equals(ACTION_NEW_VALUES)) {
                    // do the conversion from Parcelable[] to
                    // TimestampedValue[], casting doesn't work
                    Parcelable[] parcelables = (Parcelable[]) intent
                            .getParcelableArrayExtra(EXTRA_NEW_VALUES);
                    TimestampedValue[] timestampedValues = new TimestampedValue[parcelables.length];
                    System.arraycopy(parcelables, 0, timestampedValues, 0,
                            parcelables.length);
                    sListeners.get(id).onNewValues(id, timestampedValues);
                } else if (intent.getAction().equals(ACTION_NEW_TRISTATE)) {
                    sListeners
                            .get(id)
                            .onNewState(
                                    id,
                                    intent.getLongExtra(
                                            EXTRA_NEW_TRISTATE_TIMESTAMP, 0),
                                    TriState.valueOf(intent
                                            .getStringExtra(EXTRA_NEW_TRISTATE)));
                }

            } else {
                Log.d(TAG, "got spurious broadcast: " + intent.getDataString());
            }
        }
    }; */



    public static void receiveUpdate(String id, HashMap update){


        System.out.println("receiveUpdate"+id);
        if (sListeners.containsKey(id)) {
            System.out.println("sListener contains the key"+id);
            if (update.containsKey(EXTRA_NEW_VALUES)) {
                // do the conversion from Parcelable[] to
                // TimestampedValue[], casting doesn't work
                TimestampedValue[] timestampedValues = (TimestampedValue[]) update.get(EXTRA_NEW_VALUES);
                sListeners.get(id).onNewValues(id, timestampedValues);

            } else if (update.containsKey(EXTRA_NEW_TRISTATE)) {
                System.out.println("receiveUpdate: in tristate"+ id);
                sListeners
                        .get(id)
                        .onNewState(
                                id,
                                (long) update.get(EXTRA_NEW_TRISTATE_TIMESTAMP),
                                TriState.valueOf((String) update.get(EXTRA_NEW_TRISTATE)
                                        ));
            }

        } else {

            //weird data
        }


    }


   /* public static List<SensorInfo> getSensors(Context context) {
        List<SensorInfo> result = new ArrayList<SensorInfo>();
        Log.d(TAG, "Starting sensor discovery");
        PackageManager pm = context.getPackageManager();
        Intent queryIntent = new Intent("interdroid.swan.sensor.DISCOVER");
        List<ResolveInfo> discoveredSensors = pm.queryIntentActivities(
                queryIntent, PackageManager.GET_META_DATA);
        Log.d(TAG, "Found " + discoveredSensors.size() + " sensors");
        for (ResolveInfo discoveredSensor : discoveredSensors) {
            try {
                Drawable icon = new BitmapDrawable(
                        context.getResources(),
                        BitmapFactory.decodeResource(
                                pm.getResourcesForApplication(discoveredSensor.activityInfo.packageName),
                                discoveredSensor.activityInfo.icon));
//				Log.d(TAG, "\t" + discoveredSensor.activityInfo.name);
                Log.d(TAG, "\t" + discoveredSensor.activityInfo.metaData.getString("entityId"));
                result.add(new SensorInfo(new ComponentName(
                        discoveredSensor.activityInfo.packageName,
                        discoveredSensor.activityInfo.name),
                        discoveredSensor.activityInfo.metaData, icon));
            } catch (Exception e) {
                Log.e(TAG, "Error with discovered sensor: " + discoveredSensor,
                        e);
            }
        }
        return result;
    }
*/

  /*  public static SensorInfo getSensor(Context context, String name)
            throws SwanException {
        for (SensorInfo sensorInfo : getSensors(context)) {
            if (sensorInfo.getEntity().equals(name)) {
                return sensorInfo;
            }
        }
        throw new SwanException("Sensor '" + name + "' not installed.");
    } */


    public static void registerTriStateExpression(/*final Context context,*/
                                                  final String id, final TriStateExpression expression,
                                                  final TriStateExpressionListener listener) throws SwanException {
        if (listener == null) {
            registerExpression(/*context,*/ id, expression, null);
        } else {
            registerExpression(/*context, */id, expression,
                    new ExpressionListener() {

                        @Override
                        public void onNewValues(String id,
                                                TimestampedValue[] newValues) {
                            // ignore, will not happen
                        }

                        @Override
                        public void onNewState(String id, long timestamp,
                                               TriState newState) {
                            listener.onNewState(id, timestamp, newState);
                        }
                    });
        }
    }


    public static void registerValueExpression(/*Context context,*/ String id,
                                               ValueExpression expression, final ValueExpressionListener listener)
            throws SwanException {
        if (listener == null) {
            registerExpression(/*context, */id, expression, null);
        } else {
            registerExpression(/*context,*/ id, expression,
                    new ExpressionListener() {

                        @Override
                        public void onNewValues(String id,
                                                TimestampedValue[] newValues) {
                            listener.onNewValues(id, newValues);
                        }

                        @Override
                        public void onNewState(String id, long timestamp,
                                               TriState newState) {
                            // ignore, will not happen
                        }
                    });
        }
    }

    public static void registerExpression(/*Context context,*/ String id,
                                          Expression expression, ExpressionListener expressionListener)
            throws SwanException {
        if (id == null) {
            throw new SwanException("Invalid id. Null is not allowed as id");
        }
        if (id.contains(Expression.SEPARATOR)) {
            throw new SwanException("Invalid id: '" + id
                    + "' contains reserved separator '" + Expression.SEPARATOR
                    + "'");
        }
        for (String suffix : Expression.RESERVED_SUFFIXES) {
            if (id.endsWith(suffix)) {
                throw new SwanException("Invalid id. Suffix '" + suffix
                        + "' is reserved for internal use.");
            }
        }
        if (sListeners.containsKey(id)) {
            throw new SwanException("Listener already registered for id '" + id
                    + "'");
        } else {
            if (expressionListener != null) {
                //if (sListeners.size() == 0) {
                //    sReceiverRegistered = true;
                //    registerReceiver(/*context*/);
               // }
                sListeners.put(id, expressionListener);
            }
        }
        //Intent newTriState = new Intent(ACTION_NEW_TRISTATE);
        //newTriState.setData(Uri.parse("swan://" + context.getPackageName()
          //      + "#" + id));
        //Intent newValues = new Intent(ACTION_NEW_VALUES);
        //newValues.setData(Uri.parse("swan://" + context.getPackageName() + "#"
          //      + id));
       // registerExpression(/*context, */id, expression, newTriState, newTriState,
      //          newTriState, newValues);

        registerExpression(/*context, */id, expression, false, false,
                false, false);
    }


 /*   public static void registerTriStateExpression( String id,
                                                  TriStateExpression expression, boolean onTrue, boolean onFalse,
                                                  boolean onUndefined) {
        registerExpression(id, expression, onTrue, onFalse,
                onUndefined, false);
    }


    public static void registerValueExpression(String id,
                                               TriStateExpression expression, boolean onNewValues) {
        registerExpression(id, expression, false, false, false,
                onNewValues);
    }
*/
    private static void registerExpression(/*Context context,*/ String id,
                                           Expression expression, boolean onTrue, boolean onFalse,
                                           boolean onUndefined, boolean onNewValues) {
      /*  Intent intent = new Intent(ACTION_REGISTER);
        intent.putExtra("expressionId", id);
        intent.putExtra("expression", expression.toParseString());
        intent.putExtra("onTrue", onTrue);
        intent.putExtra("onFalse", onFalse);
        intent.putExtra("onUndefined", onUndefined);
        intent.putExtra("onNewValues", onNewValues);
        context.sendBroadcast(intent);*/


        evaluationEngineService.doRegister(id,expression,onTrue,onFalse,onUndefined,onNewValues);



    }


    public static void unregisterExpression(/*Context context, */String id) {
        sListeners.remove(id);
       /* if (sListeners.size() == 0 && sReceiverRegistered) {
            sReceiverRegistered = false;
            unregisterReceiver(context);
        }
        Intent intent = new Intent(ACTION_UNREGISTER);
        intent.putExtra("expressionId", id);
        context.sendBroadcast(intent);
        */
        evaluationEngineService.doUnregister(id);

    }

}
