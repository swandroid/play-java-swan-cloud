package optimization.algorithm.helperThreads;

import engine.ExpressionManager;
import engine.SwanException;
import engine.ValueExpressionListener;
import interdroid.swancore.swansong.ExpressionFactory;
import interdroid.swancore.swansong.ExpressionParseException;
import interdroid.swancore.swansong.TimestampedValue;
import interdroid.swancore.swansong.ValueExpression;

/**
 * Created by Maria Efthymiadou on Jun, 2019
 */

public class OptimizationThread implements Runnable {
    String id;
    String convertedExpression;

    public OptimizationThread(String token, String convertedExpression) {
        this.id=token;
        this.convertedExpression=convertedExpression;
    }

    @Override
    public void run() {
        try {
            ExpressionManager.registerValueExpression(id, (ValueExpression) ExpressionFactory.parse(convertedExpression), new ValueExpressionListener() {
                @Override
                public void onNewValues(String id, TimestampedValue[] newValues) {
                    if(newValues!=null && newValues.length>0) {
                        System.out.println("New Values " + newValues[newValues.length-1].toString());
                    }
                }
            });
        } catch (SwanException e) {
            e.printStackTrace();
        } catch (ExpressionParseException e) {
            e.printStackTrace();
        }

    }

}
