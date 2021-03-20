package optimization;

import controllers.OptimizationController;

import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Maria Efthymiadou on Jun, 2019
 */

public class SensorValuesModifier extends TimerTask {

    boolean increament = true;

    public SensorValuesModifier(){}
    @Override
    public void run() {
            //todo uncomment for static values
//            int position = OptimizationController.optimizationControllerData.listPosition.intValue();
//            if(i.intValue()!=position){
//                OptimizationController.optimizationControllerData.sensorValues = getDynamicValues(position);
        long startTime = System.currentTimeMillis();
        if(OptimizationController.optimizationControllerData.listPosition.intValue()==0){
            OptimizationController.optimizationControllerData.listPosition.set(1);
            increament = true;
            
        }else if(OptimizationController.optimizationControllerData.listPosition.intValue()==1){
            if(increament){
                OptimizationController.optimizationControllerData.listPosition.set(2);
            }else {
                OptimizationController.optimizationControllerData.listPosition.set(0);
                
            }
            
        }else if(OptimizationController.optimizationControllerData.listPosition.intValue()==2){
            if(increament){
                OptimizationController.optimizationControllerData.listPosition.set(3);
            }else{
                OptimizationController.optimizationControllerData.listPosition.set(1);
            }
        }else if(OptimizationController.optimizationControllerData.listPosition.intValue()==3){
            OptimizationController.optimizationControllerData.listPosition.set(2);
            increament=false;
        }
        OptimizationController.optimizationControllerData.sensorValues = getDynamicValues(OptimizationController.optimizationControllerData.listPosition.intValue());
        long endTime = System.currentTimeMillis();
        long seconds = (endTime - startTime);
    }

    private ConcurrentHashMap<String,Double> getDynamicValues(int listPosition){
            ConcurrentHashMap<String,Double> values = new ConcurrentHashMap<>();
            for(String string: OptimizationController.optimizationControllerData.dynamicSensorValues.keySet()){
                values.put(string,OptimizationController.optimizationControllerData.dynamicSensorValues.get(string).get(listPosition));
            }
            for(String string: OptimizationController.optimizationControllerData.dynamicSensorValues.keySet()){
                values.put(string,OptimizationController.optimizationControllerData.dynamicSensorValues.get(string).get(listPosition));
            }
            OptimizationController.optimizationControllerData.sensorValues=values;
            return values;

    }




}
