package engine;

import sensors.RainSensor;
import sensors.SensorFactory;
import sensors.SensorInterface;
import swansong.*;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Roshan Bharath Das on 06/06/16.
 */
public class EvaluationManager {


    private final Map<String, Result> mCachedResults = new HashMap<String, Result>();


    private final Map<String, SensorInterface> mSensors = new HashMap<String, SensorInterface>();



    public void resolveLocation(Expression expression) {
        if (!Expression.LOCATION_INFER.equals(expression.getLocation())) {
            return;
        }
        String left = null;
        String right = null;
        if (expression instanceof LogicExpression) {
            resolveLocation(((LogicExpression) expression).getLeft());
            left = ((LogicExpression) expression).getLeft().getLocation();
            resolveLocation(((LogicExpression) expression).getRight());
            right = ((LogicExpression) expression).getRight().getLocation();
        } else if (expression instanceof ComparisonExpression) {
            resolveLocation(((ComparisonExpression) expression).getLeft());
            left = ((ComparisonExpression) expression).getLeft().getLocation();
            resolveLocation(((ComparisonExpression) expression).getRight());
            right = ((ComparisonExpression) expression).getRight()
                    .getLocation();
        } else if (expression instanceof MathValueExpression) {
            resolveLocation(((MathValueExpression) expression).getLeft());
            left = ((MathValueExpression) expression).getLeft().getLocation();
            resolveLocation(((MathValueExpression) expression).getRight());
            right = ((MathValueExpression) expression).getRight().getLocation();
        }
        if (left.equals(right)) {
            expression.setInferredLocation(left);
        } else if (left.equals(Expression.LOCATION_INDEPENDENT)) {
            expression.setInferredLocation(right);
        } else if (right.equals(Expression.LOCATION_INDEPENDENT)) {
            expression.setInferredLocation(left);
        } else if (left.equals(Expression.LOCATION_SELF)
                || right.equals(Expression.LOCATION_SELF)) {
            expression.setInferredLocation(Expression.LOCATION_SELF);
        } else {
            expression.setInferredLocation(left);
        }
    }


    public void initialize(String id, Expression expression)
            throws SensorConfigurationException, SensorSetupFailedException {
        // should get the sensors start producing data.
        resolveLocation(expression);
        String location = expression.getLocation();
        if (!location.equals(Expression.LOCATION_SELF)
                && !location.equals(Expression.LOCATION_INDEPENDENT)) {
            //roshan initializeRemote(id, expression, location);
        } else if (expression instanceof LogicExpression) {
            initialize(id + Expression.LEFT_SUFFIX,
                    ((LogicExpression) expression).getLeft());
            initialize(id + Expression.RIGHT_SUFFIX,
                    ((LogicExpression) expression).getRight());
        } else if (expression instanceof ComparisonExpression) {
            initialize(id + Expression.LEFT_SUFFIX,
                    ((ComparisonExpression) expression).getLeft());
            initialize(id + Expression.RIGHT_SUFFIX,
                    ((ComparisonExpression) expression).getRight());
        } else if (expression instanceof MathValueExpression) {
            initialize(id + Expression.LEFT_SUFFIX,
                    ((MathValueExpression) expression).getLeft());
            initialize(id + Expression.RIGHT_SUFFIX,
                    ((MathValueExpression) expression).getRight());
        } else if (expression instanceof SensorValueExpression) {
            if (((SensorValueExpression) expression).getEntity().equals("time")) {
                return;
            }
            // do the real work here, bind to the sensor.
            bindToSensor(id, (SensorValueExpression) expression, false);
        }
    }


    private void bindToSensor(final String id,
                                 final SensorValueExpression expression, boolean discover)
            throws SensorConfigurationException, SensorSetupFailedException {


           //SensorInterface sensor = checkSensorName(expression);
            SensorInterface sensor = SensorFactory.getSensor(expression.getEntity());

            if(sensor!=null) {
                try {
                    sensor.register(id,
                            expression.getValuePath(),
                            expression.getConfiguration(), expression.getHttConfiguration());


                } catch (IOException e) {
                    e.printStackTrace();
                }

                mSensors.put(id, sensor);

            }

        //TODO: generalise it to all sensors
          //  RainSensor rainSensor = new RainSensor();

       // } catch (RemoteException e) {
            //Log.e(TAG, "Registration failed!", e);


      //  }

    }


  /*  private SensorInterface checkSensorName(SensorValueExpression expression){

        String entity = expression.getEntity();
        String entityInCap = entity.substring(0, 1).toUpperCase() + entity.substring(1).toLowerCase();

        System.out.println("Sensor class name :"+ entityInCap);

        try {
            Class SensorClass = Class.forName("sensors."+entityInCap+"Sensor");

            Object sensorObject = SensorClass.newInstance();

            if (sensorObject instanceof SensorInterface){

                return (SensorInterface) sensorObject;

            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }


        return null;
    }
*/


    private void unbindFromSensor(final String id) {

        System.out.println("Unbind from sensor");
        SensorInterface sensor = mSensors.remove(id);
        if (sensor != null) {
           // try {
                sensor.unregister(id);
           // } catch (RemoteException e) {
             /*   Log.d(TAG, "Failed to unregister for id: " + id
                        + ", this should not happen!", e);*/
           // }
        } else {
          /*  Log.e(TAG, "Cannot unregister for id: " + id
                    + ", sensor is null, this should not happen!");*/
        }


    }


    public void stop(String id, Expression expression) {
        // should get the sensors stop producing data.
        String location = expression.getLocation();
        if (!location.equals(Expression.LOCATION_SELF)
                && !location.equals(Expression.LOCATION_INDEPENDENT)) {
            //roshan stopRemote(id, expression);
        }
        if (expression instanceof LogicExpression) {
            stop(id + Expression.LEFT_SUFFIX,
                    ((LogicExpression) expression).getLeft());
            stop(id + Expression.RIGHT_SUFFIX,
                    ((LogicExpression) expression).getRight());
        } else if (expression instanceof ComparisonExpression) {
            stop(id + Expression.LEFT_SUFFIX,
                    ((ComparisonExpression) expression).getLeft());
            stop(id + Expression.RIGHT_SUFFIX,
                    ((ComparisonExpression) expression).getRight());
        } else if (expression instanceof MathValueExpression) {
            stop(id + Expression.LEFT_SUFFIX,
                    ((MathValueExpression) expression).getLeft());
            stop(id + Expression.RIGHT_SUFFIX,
                    ((MathValueExpression) expression).getRight());
        } else if (expression instanceof SensorValueExpression) {
            if (((SensorValueExpression) expression).getEntity().equals("time")) {
                return;
            }
            // do the real work here, unbind from the sensor.
            unbindFromSensor(id);
        }
    }

    public void destroyAll() {
        for (String id : mSensors.keySet()) {
            unbindFromSensor(id);
        }
    }

    public void clearCacheFor(String id) {
        if (mCachedResults.get(id) != null) {
            mCachedResults.get(id).setDeferUntil(0);
        }
        for (String suffix : Expression.RESERVED_SUFFIXES) {
            if (id.endsWith(suffix)) {
                clearCacheFor(id.substring(0, id.length() - suffix.length()));
            }
        }
    }


    private Result getFromSensor(String id, SensorValueExpression expression,
                                 long now) {

     //   System.out.println("getFromSensor: Sensor id");
        if (mSensors.get(id) == null) {
            //System.out.println("getFromSensor: mSensor id null");
          //  Log.d(TAG, "not yet bound for: " + id + ", " + expression);
            Result result = new Result(new TimestampedValue[]{}, 0);
            // TODO make this a constant (configurable?)
            result.setDeferUntil(System.currentTimeMillis() + 300);
            result.setDeferUntilGuaranteed(false);
            return result;
        }
        //try {
            List<TimestampedValue> values = mSensors.get(id).getValues(id, now,
                    expression.getHistoryLength());


            // TODO if values is empty, should we not just defer until forever?
            // And can values be null at all?
            if (values == null || values.size() == 0) {

                //System.out.println("getFromSensor: values are null");
                Result result = new Result(new TimestampedValue[]{}, 0);
                // TODO make this a constant (configurable?)
                result.setDeferUntil(now + 1000);
                result.setDeferUntilGuaranteed(false);
                //Log.d(TAG, "Deferred until: " + (now + 1000));
                return result;
            }

            TimestampedValue[] reduced = TimestampedValue.applyMode(values,
                    expression.getHistoryReductionMode());

            Result result = new Result(reduced, values.get(values.size() - 1)
                    .getTimestamp());
            if (expression.getHistoryLength() == 0 || reduced == null
                    || reduced.length == 0) {
                // we cannot defer based on values, new values will be retrieved
                // when they arrive
                result.setDeferUntil(Long.MAX_VALUE);
                result.setDeferUntilGuaranteed(false);
                //Log.d(TAG, "Cannot defer based on values");
            } else {
                result.setDeferUntil(values.get(values.size() - 1)
                        .getTimestamp() + expression.getHistoryLength());
                result.setDeferUntilGuaranteed(false);
            }
            return result;
       // } catch (RemoteException e) {
           // Log.e(TAG,
             //       "Got remote exception while retrieving values for expression "
               //             + expression + " with id " + id, e);
       // }
      //  return null;
    }


    public Result evaluate(String id, Expression expression, long now)
            throws SwanException {
        //System.out.println("evaluate method: start");
        if (expression == null) {
            throw new RuntimeException("This should not happen! Please debug");
        }
//        if (mCachedResults.containsKey(id)) {
//            System.out.println("evaluate: mCachedResults contains key");
//            if (mCachedResults.get(id).getDeferUntil() > now) {
//                return mCachedResults.get(id);
//            }
//        }
        Result result = null;
        // if the location is remote, result is null or undefined
        String location = expression.getLocation();
        if (!location.equals(Expression.LOCATION_SELF)
                && !location.equals(Expression.LOCATION_INDEPENDENT)) {
            if (expression instanceof TriStateExpression) {
                if (mCachedResults.containsKey(id)) {
                    return mCachedResults.get(id);
                } else {
                    result = new Result(now, TriState.UNDEFINED);
                }
            } else if (expression instanceof ValueExpression) {
                // we don't have anything cached, so send an empty result.
                result = new Result(new TimestampedValue[]{}, 0);
            }
            result.setDeferUntil(Long.MAX_VALUE);
            result.setDeferUntilGuaranteed(false);
        } else if (expression instanceof LogicExpression) {
            result = applyLogic(id, (LogicExpression) expression, now);
        } else if (expression instanceof ComparisonExpression) {
            result = doCompare(id, (ComparisonExpression) expression, now);
        } else if (expression instanceof ConstantValueExpression) {
            result = ((ConstantValueExpression) expression).getResult();
        } else if (expression instanceof MathValueExpression) {
            result = doMath(id, (MathValueExpression) expression, now);
        } else if (expression instanceof SensorValueExpression) {
            if (((SensorValueExpression) expression).getEntity().equals("time")) {
                throw new RuntimeException(
                        "time can only be used in an ComparisonExpression on the left hand");
            }
            result = getFromSensor(id, (SensorValueExpression) expression, now);

        }
        if (result != null) {
            mCachedResults.put(id, result);
        }
        return result;
    }

    private boolean leftFirst(String id, LogicExpression expression, long now) {
        // For a binary logic operation it is important to make a clever
        // decision which of the involved expressions is evaluated first.
        // Depending on the result of this evaluation and the logic operator, it
        // is possible to short circuit EVALUATION or stop SENSING.
        //
        // For instance if the first result is TRUE and the operator is OR,
        // there is no need to evaluate the second expression. This is an
        // EVALUATION optimization, that is, it saves on evaluation time. A good
        // strategy would be to start with the expression that is 'cheapest' to
        // evaluate or has the highest likelihood to cause short circuiting.
        //
        // If we short circuit the current EVALUATION, we compute how long we
        // can defer the next evaluation
        // based on the part that we did evaluate. There is a chance though
        // that by evaluating the other part we find out that we can defer new
        // evaluation even further.
        //
        // For example A OR B, where evaluating A is very cheap and often
        // results in TRUE make A a good choice to start evaluating, because the
        // current evaluation is likely to be cheap and fast. However, it might
        // be the case that B also results in TRUE, but is much more suitable
        // for turning off sensors. B might look whether the maximum over the
        // last hour exceeds a particular limit, while A checks whether the
        // average of the last 10 seconds is above a certain threshold. If we
        // find a recent sensor value that makes B true, we can conclude that B
        // remains true for about an hour, which will make the logic expression
        // true for about an hour. Within this hour no evaluation is needed.

        // TODO improve, take "time" into account, if a sensor has time, we
        // should probably evaluate it first...

        // in case we have a unary operator, it doesn't matter at all.
        if (expression.getOperator() instanceof UnaryLogicOperator) {
            return true;
        }

        // TODO values below should be replaced by real estimates
        float pLeftTrue = 0.5f; // the chance that evaluating the left part
        // results in true
        float pRightTrue = 0.5f; // the chance that evaluating the right part
        // results in true
        float leftEvaluationCost = 100;
        float rightEvaluationCost = 100;
        float leftSenseCost = 200;
        float rightSenseCost = 200;

        float leftFirstCost, rightFirstCost;
        // check which evaluation cost is likely to be cheaper
        switch ((BinaryLogicOperator) expression.getOperator()) {
            case AND:
                leftFirstCost = leftEvaluationCost + pLeftTrue
                        * rightEvaluationCost;
                rightFirstCost = rightEvaluationCost + pRightTrue
                        * leftEvaluationCost;
                return leftFirstCost <= rightFirstCost;
            case OR:
                leftFirstCost = leftEvaluationCost + (1 - pLeftTrue)
                        * rightEvaluationCost;
                rightFirstCost = rightEvaluationCost + (1 - pRightTrue)
                        * leftEvaluationCost;
                return leftFirstCost <= rightFirstCost;
        }
        // check which evaluation is likely to cause the other to sleep/defer
        switch ((BinaryLogicOperator) expression.getOperator()) {
            case AND:

                leftFirstCost = leftSenseCost + (1 - pLeftTrue) * rightSenseCost;

                rightFirstCost = rightEvaluationCost + pRightTrue
                        * leftEvaluationCost;
                return leftFirstCost <= rightFirstCost;
            case OR:
                leftFirstCost = leftEvaluationCost + (1 - pLeftTrue)
                        * rightEvaluationCost;
                rightFirstCost = rightEvaluationCost + (1 - pRightTrue)
                        * leftEvaluationCost;
                return leftFirstCost <= rightFirstCost;
        }

        return true;
    }

    private boolean shortcut(LogicExpression expression, Result first) {
        // Can we short circuit and don't evaluate the last expression?
        // FALSE && ?? -> FALSE
        // TRUE || ?? -> TRUE

        // the only drawback of short circuiting is that we could have
        // FALSE && FALSE
        // TRUE || TRUE
        // where the last result has a higher defer until, than the first. But
        // the leftFirst() method should prevent this to happen.
        if (expression.getOperator() instanceof UnaryLogicOperator) {
            // we can always shortcut unary logic operators (there is no last
            // expression).
            return true;
        }
        if ((first.getTriState() == TriState.FALSE && expression.getOperator()
                .equals(BinaryLogicOperator.AND))
                || ((first.getTriState() == TriState.TRUE) && expression
                .getOperator().equals(BinaryLogicOperator.OR))) {
            return true;
        }
        return false;
    }

    private Result applyLogic(String id, LogicExpression expression, long now)
            throws SwanException {
        boolean leftFirst = leftFirst(id, expression, now);

        Expression firstExpression = leftFirst ? expression.getLeft()
                : expression.getRight();
        Expression lastExpression = !leftFirst ? expression.getLeft()
                : expression.getRight();
        String firstSuffix = leftFirst ? Expression.LEFT_SUFFIX
                : Expression.RIGHT_SUFFIX;
        String lastSuffix = !leftFirst ? Expression.LEFT_SUFFIX
                : Expression.RIGHT_SUFFIX;

        Result first = evaluate(id + firstSuffix, firstExpression, now);

      //  if (shortcut(expression, first)) {
            // apply the sleep and be ready to last
       //     if (first.isDeferUntilGuaranteed()) {
        //        sleepAndBeReady(id + lastSuffix, lastExpression,
        //                first.getDeferUntil());
       //     }
            // put line below in the above if statement if we want to take the
            // risk of evaluating the other part of the expression. This can
            // potentially lead to a sleep and be ready on the current part of
            // the expression.
       //     return first;
     //   }
        Result last = evaluate(id + lastSuffix, lastExpression, now);

       // if (shortcut(expression, last)) {
      //      if (last.isDeferUntilGuaranteed()) {
       //         sleepAndBeReady(id + firstSuffix, firstExpression,
       //                 last.getDeferUntil());
       //     }
      //      return last;
      //  }

        Result result = new Result(now, expression.getOperator().operate(
                first.getTriState(), last.getTriState()));

        result.setDeferUntil(Math.min(first.getDeferUntil(),
                last.getDeferUntil()));
        result.setDeferUntilGuaranteed(first.isDeferUntilGuaranteed()
                && last.isDeferUntilGuaranteed());
        return result;
    }

    private class DeferUntilResult {
        public long deferUntil;
        public boolean guaranteed;

        public DeferUntilResult(long deferUntil, boolean guaranteed) {
            this.deferUntil = deferUntil;
            this.guaranteed = guaranteed;
        }

    }

    private DeferUntilResult remainsValidUntil(ValueExpression expression,
                                               long determiningValueTimestamp, long oldestValueTimestamp,
                                               Comparator comparator, TriState triState, boolean left) {
        if (expression instanceof MathValueExpression) {
            // math value is valid as long both of its children are valid
            DeferUntilResult leftResult = remainsValidUntil(
                    ((MathValueExpression) expression).getLeft(),
                    determiningValueTimestamp, oldestValueTimestamp,
                    comparator, triState, left);
            DeferUntilResult rightResult = remainsValidUntil(
                    ((MathValueExpression) expression).getRight(),
                    determiningValueTimestamp, oldestValueTimestamp,
                    comparator, triState, left);
            return new DeferUntilResult(Math.min(leftResult.deferUntil,
                    rightResult.deferUntil), leftResult.guaranteed
                    && rightResult.guaranteed);
        } else if (expression instanceof ConstantValueExpression) {
            return new DeferUntilResult(Long.MAX_VALUE, true);
        } else if (expression instanceof SensorValueExpression) {
            HistoryReductionMode mode = ((SensorValueExpression) expression)
                    .getHistoryReductionMode();
            long historyLength = ((SensorValueExpression) expression)
                    .getHistoryLength();
            if (historyLength == 0) {
                return new DeferUntilResult(Long.MAX_VALUE, false);
            }

            long deferTime = determiningValueTimestamp + historyLength;
            // here we need the big table, see thesis

            // symmetric (left or right doesn't matter)
            if (comparator == Comparator.EQUALS
                    || comparator == Comparator.REGEX_MATCH
                    || comparator == Comparator.STRING_CONTAINS) {
                if (triState == TriState.TRUE) {
                    if (mode == HistoryReductionMode.ANY) {
                        return new DeferUntilResult(deferTime, true);
                    }
                } else if (triState == TriState.FALSE) {
                    if (mode == HistoryReductionMode.ALL) {
                        return new DeferUntilResult(deferTime, true);
                    }
                }
            } else if (comparator == Comparator.NOT_EQUALS) {
                if (triState == TriState.TRUE) {
                    if (mode == HistoryReductionMode.ALL) {
                        return new DeferUntilResult(deferTime, true);
                    }
                } else if (triState == TriState.FALSE) {
                    if (mode == HistoryReductionMode.ANY) {
                        return new DeferUntilResult(deferTime, true);
                    }
                }
            }

            // assymetric (left or right does matter)
            if (left) {
                if (comparator == Comparator.GREATER_THAN
                        || comparator == Comparator.GREATER_THAN_OR_EQUALS) {
                    if (triState == TriState.TRUE) {
                        if (mode == HistoryReductionMode.MAX
                                || mode == HistoryReductionMode.ANY) {
                            return new DeferUntilResult(deferTime, true);
                        }
                    } else if (triState == TriState.FALSE) {
                        if (mode == HistoryReductionMode.MIN
                                || mode == HistoryReductionMode.ALL) {
                            return new DeferUntilResult(deferTime, true);
                        }
                    }
                } else if (comparator == Comparator.LESS_THAN
                        || comparator == Comparator.LESS_THAN_OR_EQUALS) {
                    if (triState == TriState.TRUE) {
                        if (mode == HistoryReductionMode.MIN
                                || mode == HistoryReductionMode.ANY) {
                            return new DeferUntilResult(deferTime, true);
                        }
                    } else if (triState == TriState.FALSE) {
                        if (mode == HistoryReductionMode.MAX
                                || mode == HistoryReductionMode.ALL) {
                            return new DeferUntilResult(deferTime, true);
                        }
                    }
                }
            } else {
                if (comparator == Comparator.GREATER_THAN
                        || comparator == Comparator.GREATER_THAN_OR_EQUALS) {
                    if (triState == TriState.TRUE) {
                        if (mode == HistoryReductionMode.MIN
                                || mode == HistoryReductionMode.ANY) {
                            return new DeferUntilResult(deferTime, true);
                        }
                    } else if (triState == TriState.FALSE) {
                        if (mode == HistoryReductionMode.MAX
                                || mode == HistoryReductionMode.ALL) {
                            return new DeferUntilResult(deferTime, true);
                        }
                    }
                } else if (comparator == Comparator.LESS_THAN
                        || comparator == Comparator.LESS_THAN_OR_EQUALS) {
                    if (triState == TriState.TRUE) {
                        if (mode == HistoryReductionMode.MAX
                                || mode == HistoryReductionMode.ANY) {
                            return new DeferUntilResult(deferTime, true);
                        }
                    } else if (triState == TriState.FALSE) {
                        if (mode == HistoryReductionMode.MIN
                                || mode == HistoryReductionMode.ALL) {
                            return new DeferUntilResult(deferTime, true);
                        }
                    }
                }
            }

            // otherwise we defer based on the oldest timestamp
            return new DeferUntilResult(oldestValueTimestamp + historyLength,
                    false);
        }
        return new DeferUntilResult(0, false); // should not happen!
    }


    private Result doCompare(String id, ComparisonExpression expression,
                             long now) throws SwanException {
        Result right = evaluate(id + Expression.RIGHT_SUFFIX,
                expression.getRight(), now);


        Result left = evaluate(id + Expression.LEFT_SUFFIX,
                expression.getLeft(), now);

        if (left.getValues().length == 0 || right.getValues().length == 0) {
           // Log.d(TAG, "No data for: " + expression);
            Result result = new Result(now, TriState.UNDEFINED);
            result.setDeferUntil(Long.MAX_VALUE);
            result.setDeferUntilGuaranteed(false);
            return result;
        }

        // in here we should terminate as quickly as possible, but get the
        // highest deferUntil, therefore start from recent to old
        // assume left and right are sorted with most recent one first
        ComparatorResult comparatorResult = new ComparatorResult(now,
                expression.getLeft().getHistoryReductionMode(), expression
                .getRight().getHistoryReductionMode());
        // combination ANY, ANY has a tradeoff. We can terminate evaluation as
        // soon as we find a combination that results in true, BUT if we
        // continue we might find a longer deferUntil
        comparatorResult.startOuterLoop();
        int l = 0, r = 0;
        for (l = 0; l < left.getValues().length; l++) {
            comparatorResult.startInnerLoop();
            for (r = 0; r < right.getValues().length; r++) {
                if (comparatorResult.innerResult(comparePair(
                        expression.getComparator(),
                        left.getValues()[l].getValue(),
                        right.getValues()[r].getValue()))) {
                    break;
                }
            }
            if (comparatorResult.outerResult()) {
                break;
            }
        }
        // if we don't get a break statement, l and r might be off by one (past
        // the last index)
        l = Math.min(left.getValues().length - 1, l);
        r = Math.min(right.getValues().length - 1, r);

        // find out how long this result will remain valid and defer
        // evaluation to that moment
        DeferUntilResult leftDefer = remainsValidUntil(expression.getLeft(),
                left.getValues()[l].getTimestamp(), left.getOldestTimestamp(),
                expression.getComparator(), comparatorResult.getTriState(),
                true);
        DeferUntilResult rightDefer = remainsValidUntil(expression.getRight(),
                right.getValues()[r].getTimestamp(),
                right.getOldestTimestamp(), expression.getComparator(),
                comparatorResult.getTriState(), false);

        comparatorResult.setDeferUntilGuaranteed(leftDefer.guaranteed
                && rightDefer.guaranteed);
        comparatorResult.setDeferUntil(Math.min(leftDefer.deferUntil,
                leftDefer.deferUntil));
        return comparatorResult;
    }


    private static Object promote(Object object) {
        if (object instanceof Integer) {
            return Long.valueOf((Integer) object);
        }
        if (object instanceof Float) {
            return Double.valueOf((Float) object);
        }
        return object;
    }

    public static TriState comparePair(final Comparator comparator,
                                       Object left, Object right) {
        TriState result = TriState.FALSE;
        // promote types
        left = promote(left);
        right = promote(right);

        switch (comparator) {
            case LESS_THAN:
                if (((Comparable) left).compareTo(right) < 0) {
                    result = TriState.TRUE;
                }
                break;
            case LESS_THAN_OR_EQUALS:
                if (((Comparable) left).compareTo(right) <= 0) {
                    result = TriState.TRUE;
                }
                break;
            case GREATER_THAN:
                if (((Comparable) left).compareTo(right) > 0) {
                    result = TriState.TRUE;
                }
                break;
            case GREATER_THAN_OR_EQUALS:
                if (((Comparable) left).compareTo(right) >= 0) {
                    result = TriState.TRUE;
                }
                break;
            case EQUALS:
                if (((Comparable) left).compareTo(right) == 0) {
                    result = TriState.TRUE;
                }
                break;
            case NOT_EQUALS:
                if (((Comparable) left).compareTo(right) != 0) {
                    result = TriState.TRUE;
                }
                break;
            case REGEX_MATCH:
                if (((String) left).matches((String) right)) {
                    result = TriState.TRUE;
                }
                break;
            case STRING_CONTAINS:
                if (((String) left).contains((String) right)) {
                    result = TriState.TRUE;
                }
                break;
            default:
                throw new AssertionError("Unknown comparator '" + comparator
                        + "'. Should not happen");
        }
        return result;
    }


    private Result doMath(String id, MathValueExpression expression, long now)
            throws SwanException {
        Result left = evaluate(id + Expression.LEFT_SUFFIX,
                expression.getLeft(), now);
        Result right = evaluate(id + Expression.RIGHT_SUFFIX,
                expression.getRight(), now);
        if (left.getValues().length == 0 || right.getValues().length == 0) {
            Result result = new Result(left.getValues(),
                    left.getOldestTimestamp());
            return result;
        } else if (left.getValues().length == 1
                || right.getValues().length == 1) {
            TimestampedValue[] values = new TimestampedValue[left.getValues().length
                    * right.getValues().length];
            int index = 0;
            for (int i = 0; i < left.getValues().length; i++) {
                for (int j = 0; j < right.getValues().length; j++) {
                    values[index++] = operate(left.getValues()[i],
                            expression.getOperator(), right.getValues()[j]);
                }
            }
            Result result = new Result(values, Math.min(
                    left.getOldestTimestamp(), right.getOldestTimestamp()));
            result.setDeferUntil(Math.min(left.getDeferUntil(),
                    right.getDeferUntil()));
            result.setDeferUntilGuaranteed(false);
            return result;
        } else {
            // TODO: we could relax this statement a bit, and allow for
            // cross-product
            throw new SwanException("Unable to combine two arrays, "
                    + "only one of the operands can be an array: "
                    + expression.getOperator());
        }
    }


    /**
     * Operates on doubles.
     *
     * @param left  the left side value
     * @param right the right side value
     * @return the combined value
     * @throws SwanException if something goes wrong.
     */
    private Double operateDouble(final double left, MathOperator operator,
                                 final double right) throws SwanException {
        Double ret;
        switch (operator) {
            case MINUS:
                ret = left - right;
                break;
            case PLUS:
                ret = left + right;
                break;
            case TIMES:
                ret = left * right;
                break;
            case DIVIDE:
                ret = left / right;
                break;
            case MOD:
                ret = left % right;
            default:
                throw new SwanException("Unknown operator: '" + operator
                        + "' for type Double");
        }
        return ret;
    }


    /**
     * Operates on longs.
     *
     * @param left  the left side value
     * @param right the right side value
     * @return the combined value
     * @throws SwanException if something goes wrong.
     */
    private Long operateLong(final long left, MathOperator operator,
                             final long right) throws SwanException {
        Long ret;
        switch (operator) {
            case MINUS:
                ret = left - right;
                break;
            case PLUS:
                ret = left + right;
                break;
            case TIMES:
                ret = left * right;
                break;
            case DIVIDE:
                ret = left / right;
                break;
            case MOD:
                ret = left % right;
            default:
                throw new SwanException("Unknown operator: '" + operator
                        + "' for type Long");
        }
        return ret;
    }

    /**
     * Operates on string.
     *
     * @param left  the left side value
     * @param right the right side value
     * @return the combined value
     * @throws SwanException if something goes wrong.
     */
    private String operateString(final String left, MathOperator operator,
                                 final String right) throws SwanException {
        String ret;
        switch (operator) {
            case PLUS:
                ret = left + right;
                break;
            default:
                throw new SwanException("Unknown operator: '" + operator
                        + "' for type String");
        }
        return ret;
    }




    private TimestampedValue operate(final TimestampedValue left,
                                     MathOperator operator, final TimestampedValue right)
            throws SwanException {
        if (left.getValue() instanceof Double
                && right.getValue() instanceof Double) {
            return new TimestampedValue(operateDouble((Double) left.getValue(),
                    operator, (Double) right.getValue()), left.getTimestamp());
        } else if (left.getValue() instanceof Long
                && right.getValue() instanceof Long) {
            return new TimestampedValue(operateLong((Long) left.getValue(),
                    operator, (Long) right.getValue()), left.getTimestamp());
        } else if (left.getValue() instanceof String
                && right.getValue() instanceof String) {
            return new TimestampedValue(operateString((String) left.getValue(),
                    operator, (String) right.getValue()), left.getTimestamp());
        }

        throw new SwanException("Trying to operate on incompatible types: "
                + left.getValue().getClass() + " and "
                + right.getValue().getClass());
    }





}
