package sensors.impl;

import controllers.OptimizationController;
import engine.ExpressionManager;
import optimization.algorithm.UserOptimizationData;
import optimization.hibernateModels.Graph;
import sensors.base.AbstractSwanSensor;
import sensors.base.SensorPoller;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Maria Efthymiadou on 15/01/19.
 */
public class OptimizationSensor extends AbstractSwanSensor {

    public static final String OPTIMIZATION = "optimization";
    private Map<String, OptimizationPoller> activeThreads = new HashMap<String, OptimizationPoller>();

    @Override
    public String[] getValuePaths() {
        return new String[]{OPTIMIZATION};
    }

    @Override
    public String getEntity() {
        return "result";
    }

    @Override
    public String[] getConfiguration() {
        return new String[] {"delay"};
    }

    public OptimizationSensor() {
        super();

    }

    class OptimizationPoller extends SensorPoller {

        protected OptimizationPoller(String id, String valuePath, HashMap configuration) {
            super(id, valuePath, configuration);
        }

        UserOptimizationData userOptimizationData = OptimizationController.optimizationControllerData.activeUsersData.get(this.getID());
        com.vividsolutions.jts.geom.Coordinate userCoordinate;
        public void run() {
            boolean firstRun = true;
            userCoordinate = userOptimizationData.getOrigin().getCoordinate();
            Graph skeleton = userOptimizationData.getUserSkeletonGraph();
            while (!isInterrupted()) {
                if(firstRun){
                    firstRun=false;
                    OptimizationController.optimizationControllerData.userNewData.put(this.getID(),false);
                }else if(OptimizationController.optimizationControllerData.userNewData.get(this.getID()).equals(true)){
                    //location changed
                    if(OptimizationController.optimizationControllerData.activeUsersData.get(this.getID()).getOrigin()
                               .getCoordinate()!=userCoordinate){
                        userCoordinate = OptimizationController.optimizationControllerData.activeUsersData
                                                .get(this.getID()).getOrigin().getCoordinate();
                        if(skeleton.getVertexes().size()!= OptimizationController.optimizationControllerData.activeUsersData
                                                                  .get(this.getID()).getUserSkeletonGraph().getVertexes()
                                                                  .size()){
                            skeleton= OptimizationController.optimizationControllerData.activeUsersData.get(this.getID())
                                             .getUserSkeletonGraph();
                        }
                        OptimizationController.optimizationControllerData.userNewData.put(this.getID(),false);
                        ExpressionManager.notifyForUpdate(OptimizationController.optimizationControllerData.activeUsersData
                                                                  .get(this.getID()).getId());
                     }
                }
            }
        }
    }
    

    @Override
    public void register(String id, String valuePath, HashMap configuration, HashMap httpConfiguration) {
        super.register(id,valuePath,configuration,httpConfiguration);
        OptimizationSensor.OptimizationPoller optimizationPoller = new OptimizationSensor.OptimizationPoller(id, valuePath,
                configuration);
        activeThreads.put(id, optimizationPoller);
        optimizationPoller.start();
    }

    @Override
    public void unregister(String id) {
        super.unregister(id);
        System.out.println("Unregister OptimizationSensor with id "+id);
        activeThreads.get(id).interrupt();
        activeThreads.remove(id);
    }
}

