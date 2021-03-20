package engine;

import interdroid.swancore.swansong.Expression;
import interdroid.swancore.swansong.Result;
import interdroid.swancore.swansong.ValueExpression;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.*;

import static java.lang.Thread.interrupted;

/**
 * Created by Roshan Bharath Das on 06/06/16.
 */
public class EvaluationEngineService /* implements Runnable */ {


    private static EvaluationEngineService instance = null;
    
    //for the optimization sensors
    public static ConcurrentHashMap<String,Worker> activeOptimizationThreads = new ConcurrentHashMap<>();
    
    private EvaluationEngineService(){

        mEvaluationManager = new EvaluationManager();
        // kick off the evaluation thread
        mTerminationThread.start();
        mEvaluationThread.start();

    }


    public static EvaluationEngineService getInstance() {
        if(instance == null) {
            instance = new EvaluationEngineService();
        }
        return instance;
    }



    //private EvaluationCommand mEvaluationCommand;


    // HashMap mParameters;


    EvaluationManager mEvaluationManager;



    PriorityQueue<QueuedExpression> mEvaluationQueue = new PriorityQueue<QueuedExpression>();

    private final HashMap<String, QueuedExpression> mRegisteredExpressions = new HashMap<String, QueuedExpression>() {
        /**
         *
         */
        private static final long serialVersionUID = -658408645837738007L;

        @Override
        public QueuedExpression remove(final Object id) {
            //removeFromDb((String) id);
            return super.remove(id);
        }

        @Override
        public QueuedExpression put(final String key,
                                    final QueuedExpression value) {
            //storeToDb(value);
            return super.put(key, value);
        }

    };

 /*   public synchronized void setNotifyParameter(EvaluationCommand evaluationCommand, HashMap parameters){

        this.mEvaluationCommand = evaluationCommand;
        this.mParameters = parameters;

    } */


    private final List<Worker> threadList = new ArrayList<>();
    private ExecutorService executor = Executors.newFixedThreadPool(1);


    private Thread mEvaluationThread = new Thread() {
        public void run() {
            while (!interrupted()) {
                //System.out.println("Evaluation thread : while loop");
                QueuedExpression head = mEvaluationQueue.peek();
                if (head == null) {
                    //Log.d(TAG, "Nothing to evaluate!");
                    synchronized (mEvaluationThread) {
                        try {
                            mEvaluationThread.wait();
                        } catch (InterruptedException e) {
                            continue;
                        }
                    }
                } else {
                    //System.out.println("mEvaluationThread : work to do");
                    long deferUntil = head.getDeferUntil();
                    //Log.d(TAG, "Defer until: " + deferUntil);

                    if (deferUntil <= System.currentTimeMillis()) {
                        // Create a worker thread to do the task in parallel
                        Worker worker = new Worker(head);
                        System.out.println("worker head.getId "+head.getId());
                        if(head.getId().contains("optimization")) {
//                            System.out.println("it contains optimization");
                            if(worker!=null)
                                System.out.println(" worker "+worker+ " "+worker.getThreadName());
                            activeOptimizationThreads.put(head.getId(), worker);
                        }
                        
                        
                        executor.submit(worker);

                        // re add the expression to the queue
                        synchronized (mEvaluationThread) {
                            mEvaluationQueue.remove(head);
                            // No need to add again, it will be added in doNotify whenever there are new values
//                            mEvaluationQueue.add(head);
                        }
                    } else {
                        synchronized (mEvaluationThread) {
                            try {
                                long waitTime = Math.max(
                                        1,
                                        head.getDeferUntil()
                                                - System.currentTimeMillis());
                                // Log.d(TAG, "Waiting for " + waitTime +
                                // " ms.");
                                //System.out.println("Putting evaluation thread on wait for " + waitTime);
                                mEvaluationThread.wait(waitTime);
                                // Log.d(TAG, "Done waiting for " + waitTime
                                // + " ms.");
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
    };



    class Worker extends Thread {

        private QueuedExpression head;
        private volatile Result result;
        private String threadName;
        private volatile boolean terminated = false;

        public Worker(QueuedExpression head) {
            this.head = head;

            synchronized (threadList) {
                threadList.add(this);
            }
        }

        @Override
        public void run() {
            terminated = false;
            threadName = Thread.currentThread().getName();
            System.out.println(Thread.currentThread().getName() + " Start");
            
            if(head.getId().contains("optimization"))
                doOptimizationWork();
            else
                doWork();
            
            System.out.println(Thread.currentThread().getName() + " End");
            terminated = true;
        }
    
        void doOptimizationWork() {
            mEvaluationManager.evaluate(head.getId());
        }

        void doWork() {
            try {
                // evaluation delay is the time in ms between when
                // the expression should be evaluated (as indicated
                // by deferuntil) and when it is really evaluated.
                // Normally the evaluation delay is neglectable, but
                // when the load is high, this can become
                // significant.
                long evaluationDelay;
                long deferUntil = head.getDeferUntil();
                if (deferUntil != 0) {
                    evaluationDelay = System.currentTimeMillis()
                            - deferUntil;
                    // code below for debugging purposes
                    if (evaluationDelay > 3600000) {
                        throw new RuntimeException(
                                "Weird evaluation delay: "
                                        + evaluationDelay + ", "
                                        + deferUntil);
                    }
                } else {
                    evaluationDelay = 0;
                }

                long start = System.currentTimeMillis();
                result = mEvaluationManager.evaluate(
                        head.getId(), head.getExpression(),
                        System.currentTimeMillis());

                long end = System.currentTimeMillis();
                long evaluationTime =(end-start);
                System.out.println("Evaluation time: "+evaluationTime);

                // update with statistics: evaluationTime and evaluationDelay
                head.evaluated((end - start), evaluationDelay);

            } catch (SwanException e) {
                e.printStackTrace();
            }
        }

        public Result getResult() {
            return result;
        }

        public QueuedExpression getHead() {
            return head;
        }

        public String getThreadName() {
            return threadName;
        }

        public boolean isTerminated() {
            return terminated;
        }
    }



    private Thread mTerminationThread = new Thread(() -> {
        while(!interrupted()) {
            try {
                Worker thread = null;
                synchronized (threadList) {
                    if (!threadList.isEmpty())
                        thread = threadList.get(0);
                }

                if (thread != null) {
                    if (!thread.isTerminated())
                        continue;

                    Result result = thread.getResult();
    
                    if(result!=null){
                        QueuedExpression head = thread.getHead();
                        if (head.update(result)) {
                            if(!head.getId().contains("optimization")){
                                sendUpdate(head, result);
                            }
                        }
        
                    }

                    synchronized (threadList) {
                        threadList.remove(thread);
                    }

//                    System.out.println("Terminating thread: " + thread.getThreadName());
                    System.out.println("DONE: " + thread.getThreadName());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    });

    public synchronized void doRegister(final String id, final Expression expression,
                            final boolean onTrue, final boolean onFalse,
                            final boolean onUndefined, boolean onNewValues) {
        // handle registration
       // Log.d(TAG, "registring id: " + id + ", expression: " + expression);
        if (mRegisteredExpressions.containsKey(id)) {
            // FAIL!
            //Log.d(TAG, "failed to register, already contains id!");
            return;
        }
        try {
            mEvaluationManager.initialize(id, expression);
        } catch (SensorConfigurationException e) {
            // FAIL!
            e.printStackTrace();
            return;
        } catch (SensorSetupFailedException e) {
            // FAIL!
            e.printStackTrace();
            return;
        }
        synchronized (mEvaluationThread) {
            // add this expression to our registered expression, the queue and
            // notify the evaluation thread
            QueuedExpression queued = new QueuedExpression(id, expression,
                    onTrue, onFalse, onUndefined, onNewValues);
            mRegisteredExpressions.put(id, queued);
            mEvaluationQueue.add(queued);
            mEvaluationThread.notify();
            //LocalBroadcastManager.getInstance(this).sendBroadcast(
              //      getRegisteredExpressions());
        }

    }


    public synchronized void doUnregister(final String id) {
        QueuedExpression expression = mRegisteredExpressions.get(id);
        if (expression == null) {
            // FAIL!
           // Log.d(TAG, "Got spurious unregister for id: " + id);
            return;
        }
       // Log.d(TAG, "unregistering id: " + id + ", expression: " + expression);
        // first stop evaluating
        synchronized (mEvaluationThread) {
            mRegisteredExpressions.remove(id);
            mEvaluationQueue.remove(expression);
            // do we really need to notify the evaluation thread here?
            mEvaluationThread.notify();
            //LocalBroadcastManager.getInstance(this).sendBroadcast(
                    //getRegisteredExpressions());
        }
        // then stop sensing
        mEvaluationManager.stop(id, expression.getExpression());
    }


    public synchronized void doNotify(String[] ids) {

            //System.out.println("doNotify start");
            if (ids == null) {
                return;
            }
            for (String id : ids) {
                String rootId = getRootId(id);
                QueuedExpression queued = mRegisteredExpressions.get(rootId);
                if (queued == null) {
                    // TODO: maybe broadcast a message to inform sensors to stop
                    // producing values for the id
                    //Log.d(TAG, "Got notify, but no expression registered with id: "
                       //     + rootId + " (original id: " + id
                       //     + "), should we kill the sensor?");
                    continue;
                }
                // Log.d(TAG, "Got notification for: " + queued);
                if (queued.getExpression() instanceof ValueExpression
                        || !queued.isDeferUntilGuaranteed()) {
                    // evaluate now!
                    synchronized (mEvaluationThread) {
                        // get it out the queue, update defer until, and put it
                        // back, then notify the evaluation thread.
                        mEvaluationQueue.remove(queued);

                        // the line below will set deferUntil to 0 for the new result that just came
                        // from a remote device, not for the queued, which prevents the evaluation engine
                        // to handle the new result properly in the evaluation thread
//					mEvaluationManager.clearCacheFor(id);

                        // added this as patch; might not work for all cases, as clearCacheFor() does some
                        // extra stuff in addition to setting deferUntil to 0
                        queued.setDeferUntil(0);

                        //System.out.println("doNotify notify evaluation thread");
                        mEvaluationQueue.add(queued);
                        mEvaluationThread.notifyAll();
                    }
                }
            }
    }

    private String getRootId(String id) {
        for (String suffix : Expression.RESERVED_SUFFIXES) {
            if (id.endsWith(suffix)) {
                return getRootId(id.substring(0, id.length() - suffix.length()));
            }
        }
        return id;
    }


    public void sendUpdate(QueuedExpression queued, Result result) {
            // we know it has changed
         //   if (queued.getId().contains(Expression.SEPARATOR)) {
              /*  sendUpdateToRemote(queued.getId().split(Expression.SEPARATOR)[0],
                        queued.getId().split(Expression.SEPARATOR)[1], result); */
           //     return;
            //}
           // Intent update = queued.getIntent(result);
            //if (update == null) {
             //   Log.d(TAG, "State change, but no update intent defined");
              //  return;
            //}

            //System.out.println("sendUpdate: start");
            HashMap update = new HashMap();
            if (queued.getExpression() instanceof ValueExpression) {
                if (result.getValues() == null) {
                    //Log.d(TAG, "Update canceled, no values");
                    return;
                }
                update.put(ExpressionManager.EXTRA_NEW_VALUES, result.getValues());
               // update.putExtra(ExpressionManager.EXTRA_NEW_VALUES,
                        //result.getValues());
            } else {
                //System.out.println("sendUpdate: in Tristate");
                update.put(ExpressionManager.EXTRA_NEW_TRISTATE, result.getTriState().name());
                update.put(ExpressionManager.EXTRA_NEW_TRISTATE_TIMESTAMP,
                        result.getTimestamp());
                //update.putExtra(ExpressionManager.EXTRA_NEW_TRISTATE, result
                       // .getTriState().name());
                //update.putExtra(ExpressionManager.EXTRA_NEW_TRISTATE_TIMESTAMP,
                        //result.getTimestamp());
            }
           /* try {
                String intentType = update
                        .getStringExtra(ExpressionManager.EXTRA_INTENT_TYPE);
                if (intentType == null
                        || intentType
                        .equals(ExpressionManager.INTENT_TYPE_BROADCAST)) {
                    sendBroadcast(update);
                } else if (intentType
                        .equals(ExpressionManager.INTENT_TYPE_ACTIVITY)) {
                    update.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(update);
                } else if (intentType.equals(ExpressionManager.INTENT_TYPE_SERVICE)) {
                    startService(update);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } */
        ExpressionManager.receiveUpdate(queued.getId(), update);

    }
    
    public synchronized void update(String id) {
        synchronized (mEvaluationThread) {
            // add this expression to our registered expression, the queue and
            // notify the evaluation thread
//            QueuedExpression queued = new QueuedExpression(id, expression,
//                    onTrue, onFalse, onUndefined, onNewValues);
            QueuedExpression queued = new QueuedExpression(id);

//            mRegisteredExpressions.put(id, queued);
            mEvaluationQueue.add(queued);
            mEvaluationThread.notify();
            
        }
    }


 /*   @Override
    public void run() {


        while(!interrupted()){

            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // make evaluationCommand mutex

            switch (mEvaluationCommand) {

                case NOTIFY_FOR_EVALUATION:

                    doNotify((String[]) mParameters.get("expressionIds"));

                    System.out.println("NOTIFY");
                    break;

                case REGISTER_EXPRESSION:

                    doRegister((String) mParameters.get("expressionId"), (Expression) mParameters.get("expression"),
                            (boolean) mParameters.get("onTrue"), (boolean) mParameters.get("onFalse"),
                            (boolean) mParameters.get("onUndefined"), (boolean) mParameters.get("onNewValues"));

                    System.out.println("REGISTER");
                    break;

                case UNREGISTER_EXPRESSION:

                    doUnregister((String) mParameters.get("expressionId"));
                    System.out.println("UNREGISTER");
                    break;

                case SEND_UPDATE_TO_APP:

                    System.out.println("UNREGISTER");
                    break;


                default:
                    System.out.println("Do nothing");
                    break;
            }





        }



    } */


}
