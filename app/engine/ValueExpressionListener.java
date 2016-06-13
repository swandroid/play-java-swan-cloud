package engine;

/**
 * Created by goose on 09/06/16.
 */


import swansong.HistoryReductionMode;
import swansong.TimestampedValue;
import swansong.ValueExpression;

public interface ValueExpressionListener {

    /**
     * This method will be invoked when a {@link ValueExpression} produces new
     * values. Depending on the {@link HistoryReductionMode} the array with new
     * values can have a single value or multiple values.
     *
     * @param id
     * @param newValues
     */
    public void onNewValues(String id, TimestampedValue[] newValues);

}
