package services;


import java.util.Map;

/**
 * Created by goose on 01/06/16.
 */

public class SwanService implements Runnable{

    private String id;
    private String valuePath;
    private Map configuration;

    SwanService(String id, String valuePath,Map configuration){
        this.id =  id;
        this.valuePath = valuePath;
        this.configuration = configuration;

    }



    @Override
    public void run(){

     //   RainSensor rainSensor = new RainSensor(id, valuePath,
      //      configuration);

     //   rainSensor.start();




    }



}
