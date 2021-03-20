package engine;

import interdroid.swancore.swansong.*;

/**
 * Created by goose on 06/06/16.
 */

public class QueuedExpression implements Comparable<QueuedExpression> {

    private Expression mExpression;
    private String mId;
    private Result mCurrentResult;
    private boolean mOnTrue;
    private boolean mOnFalse;
    private boolean mOnUndefined;
    private boolean mOnNewValues;



    private long mStartTime;
    private int mEvaluations; // number of evaluations
    private long mTotalEvaluationTime; // total time spent on evaluations so far
    private long mMinEvaluationTime = Long.MAX_VALUE;
    private long mMaxEvaluationTime = Long.MIN_VALUE;
    private long mTotalEvaluationDelay; // total delay
    private long mNumEvaluationsDelay; // number of evaluations with delay

    public QueuedExpression(String id, Expression expression, boolean onTrue,
                            boolean onFalse, boolean onUndefined, boolean onNewValues) {
        mId = id;
        mExpression = expression;
        mStartTime = System.currentTimeMillis();
        mOnTrue = onTrue;
        mOnFalse = onFalse;
        mOnUndefined = onUndefined;
        mOnNewValues = onNewValues;
    }
    
    public QueuedExpression(String id) {
        mId = id;
        mStartTime = System.currentTimeMillis();
        
    }
    
    

    public int compareTo(QueuedExpression another) {
        // mCurrentResult can be null if, for example, a NEARBY expression is registered,
        // but there are no deviced nearby
        if (mCurrentResult == null) {
            return -1;
        }
        return mCurrentResult.compareTo(another.mCurrentResult);
    }

    ;

    public Expression getExpression() {
        return mExpression;
    }

    public String getId() {
        return mId;
    }

    /**
     * update the current result and return whether this update caused a change
     *
     * @param result
     * @return
     */
    public boolean update(Result result) {
        if (mExpression instanceof TriStateExpression && mCurrentResult != null
                && mCurrentResult.getTriState() == result.getTriState()) {
            mCurrentResult = result;
            return false;
        } else if (mExpression instanceof ValueExpression
                && mCurrentResult != null
                && !hasChanged(mCurrentResult.getValues(), result.getValues())) {
            mCurrentResult = result;
            return false;
        }
        mCurrentResult = result;
        return true;
    }

    private boolean hasChanged(TimestampedValue[] a, TimestampedValue[] b) {
        if (a == null || b == null) {
            return true;
        }
        if (a.length != b.length) {
            return true;
        }
        for (int i = 0; i < a.length; i++) {
            if (!a[i].equals(b[i])) {
                return true;
            }
        }
        return false;
    }

    public long getDeferUntil() {
        if (mCurrentResult != null) {
            return mCurrentResult.getDeferUntil();
        } else {
            // we don't have a current result yet, so we can't defer
            return 0;
        }
    }

    /**
     * added as patch by Vladimir to get distributed sensing working (see where it's called)
     */
    public void setDeferUntil(long deferUntil) {
        if (mCurrentResult != null) {
            mCurrentResult.setDeferUntil(deferUntil);
        }
    }

    public boolean isDeferUntilGuaranteed() {
        if (mCurrentResult != null) {
            return mCurrentResult.isDeferUntilGuaranteed();
        } else {
            // we don't have a current result yet, so we can't defer
            return false;
        }
    }

    public String toString() {
        String id = mId;
        if (mId.contains(Expression.SEPARATOR)) {
            id = "<remote> " + mId.split(Expression.SEPARATOR, 2)[1];
        }
        return id;
    }

    public void evaluated(long currentEvalutionTime, long evalDelay) {
        mEvaluations += 1;
        mTotalEvaluationTime += currentEvalutionTime;
        mMinEvaluationTime = Math.min(mMinEvaluationTime, currentEvalutionTime);
        mMaxEvaluationTime = Math.max(mMaxEvaluationTime, currentEvalutionTime);
        if (evalDelay != 0) {
            mTotalEvaluationDelay += evalDelay;
            mNumEvaluationsDelay += 1;
        }
    }

 /*   public Bundle toBundle() {
        Bundle bundle = new Bundle();
        bundle.putLong("start-time", mStartTime);
        bundle.putString("name", mId);
        bundle.putString("result", mCurrentResult == null ? "n.a."
                : mCurrentResult.toString());
        bundle.putDouble(
                "evaluation-rate",
                (mEvaluations / ((System.currentTimeMillis() - mStartTime) / 1000.0)));
        bundle.putLong("min-evaluation-time", mMinEvaluationTime);
        bundle.putLong("max-evaluation-time", mMaxEvaluationTime);
        bundle.putLong("avg-evaluation-time",
                (mTotalEvaluationTime / Math.max(mEvaluations, 1)));
        bundle.putFloat("evaluation-percentage",
                ((mTotalEvaluationTime * 100) / (float) (System
                        .currentTimeMillis() - mStartTime)));
        bundle.putLong("avg-evaluation-delay",
                (mTotalEvaluationDelay / Math.max(mNumEvaluationsDelay, 1)));
        return bundle;
    }

    public Intent getIntent(Result result) {
        if (mExpression instanceof TriStateExpression) {
            switch (result.getTriState()) {
                case TRUE:
                    return mOnTrue;
                case FALSE:
                    return mOnFalse;
                case UNDEFINED:
                    return mOnUndefined;
            }
        }
        return mOnNewValues;
    }

    */

    public boolean getOnTrue() {
        return mOnTrue;
    }

    public boolean getOnFalse() {
        return mOnFalse;
    }

    public boolean getOnUndefined() {
        return mOnUndefined;
    }

    public boolean getOnNewValues() {
        return mOnNewValues;
    }

}
