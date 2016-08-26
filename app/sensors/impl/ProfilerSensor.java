package sensors.impl;

import sensors.base.AbstractSwanSensor;
import sensors.base.SensorPoller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Roshan Bharath Das on 26/08/16.
 */
public class ProfilerSensor extends AbstractSwanSensor {

    //Test case is if x>1 caseScenario 0 is worst and caseScenario 1 is best

    private Map<String, ProfilerPoller> activeThreads = new HashMap<String, ProfilerPoller>();


    public static final String VALUE = "value";


    class ProfilerPoller extends SensorPoller {

        int i=0;

        int[] intArray = new int[4];
        int caseScenario=0;

        ProfilerPoller(String id, String valuePath, HashMap configuration) {
            super(id, valuePath, configuration);
        }


        public void run() {

            intArray[0] =0;
            intArray[1] =1;
            intArray[2] =2;
            intArray[3] =3;

            if(configuration.containsKey("case")){
                caseScenario = Integer.parseInt((String) configuration.get("case"));
            }

            while (!isInterrupted()) {

                long now = System.currentTimeMillis();

                if(caseScenario==0){
                    if(i==0){
                        i=2;
                    }
                    else{
                        i=0;
                    }
                }
                else{
                    if(i==0){
                        i=1;
                    }
                    else{
                        i=0;
                    }

                }

                System.out.println("DELAY="+DELAY+ " I value="+i);

                updateResult(ProfilerSensor.this,i,now);

                try {
                    Thread.sleep(DELAY);
                } catch (InterruptedException e) {
                    break;
                }

            }
        }


    }



    @Override
    public void register(String id, String valuePath, HashMap configuration, HashMap httpConfiguration) {

        super.register(id,valuePath,configuration,httpConfiguration);

        ProfilerPoller profilerPoller = new ProfilerPoller(id, valuePath,
                configuration);
        activeThreads.put(id, profilerPoller);
        profilerPoller.start();

    }

    @Override
    public void unregister(String id) {

        super.unregister(id);
        System.out.println("Unregister sensor called");
        activeThreads.remove(id).interrupt();

    }


    @Override
    public String[] getValuePaths()  {
        return new String[]{ VALUE};
    }

    @Override
    public String getEntity() {
        return "profiler";
    }

    @Override
    public String[] getConfiguration() {
        return new String[] {"delay","case"};
    }


}