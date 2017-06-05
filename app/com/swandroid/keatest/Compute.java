package com.swandroid.keatest;


import interdroid.swancore.swansong.TimestampedValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * Created by Roshan Bharath Das on 03/06/2017.
 */
public class Compute {

    static volatile double sumValues = 0.0;
    static int N_THREADS = Constants.N_THREADS;

    Random rand = new Random();
    float minX = 0.0f;
    float maxX = 1000.0f;

    static ExecutorService executor;

    public void loopMethodTest(int start, int size, int gap,ComputationType type){

        for (int i=start;i<=size;i=i+gap){
            methodTest(i,type);
        }


    }


    public void methodTest(int size, ComputationType type){

        List<TimestampedValue> sensorValues = generatTimeStampedValues(size);

        long startTime = System.currentTimeMillis();
        computeMethod(type,sensorValues);
        long estimatedTime = System.currentTimeMillis()-startTime;

        System.out.println(sensorValues.size()+"\t"+estimatedTime);
    }



    List<TimestampedValue> generatTimeStampedValues(int totalNumberOfValues){

        List<TimestampedValue> result = new ArrayList<TimestampedValue>();

        for (int i = 0; i < totalNumberOfValues; i++) {

            Object value = rand.nextFloat() * (maxX - minX) + minX;
            long now = System.currentTimeMillis();

            result.add(new TimestampedValue(value, now));

        }

        return result;

    }


    void computeMethod(ComputationType type, List<TimestampedValue> sensorValues){

        switch(type){
            case MEDIAN:
                calculateMedian(sensorValues);
                break;
            case MEAN_ONLOGN:
                calculateMeanNlogN(sensorValues);
                break;
            case MEAN_ON2:
                calculateMeanN2(sensorValues);
                break;
            case MEAN_ON3:
                calculateMeanN3(sensorValues);
                break;
            case MEAN_PARALLEL:
            case MEAN_ON2_PARALLEL:
            case MEAN_ON3_PARALLEL:
                calculateMeanParallel(sensorValues,type);
                break;
            case MEAN:
            default:
                calculateMean(sensorValues);
                break;
        }

    }


    public static final TimestampedValue calculateMedian(
            final List<TimestampedValue> values) {
        long timestamp = values.get(0).getTimestamp();
        Collections.sort(values);
        TimestampedValue result = values.get(values.size() / 2);
        result.mTimestamp = timestamp;
        return result;
    }


    public static TimestampedValue calculateMean(
            final List<TimestampedValue> values) {
        double sumValues = 0.0;

        for (TimestampedValue value : values) {
                sumValues += Double.valueOf(value.getValue().toString());
        }


        return new TimestampedValue(sumValues / values.size(),
                values.get(0).mTimestamp);
    }


    public static TimestampedValue calculateMeanN2(
            final List<TimestampedValue> values) {
        double sumValues = 0.0;

        for (TimestampedValue value2 : values) {
                for (TimestampedValue value : values) {
                    sumValues += Double.valueOf(value.getValue().toString());
                }
        }

        return new TimestampedValue(sumValues / values.size(),
                values.get(0).mTimestamp);
    }



    public static TimestampedValue calculateMeanN3(
            final List<TimestampedValue> values) {
        double sumValues = 0.0;

        for (TimestampedValue value1 : values) {
            for (TimestampedValue value2 : values) {
                for (TimestampedValue value : values) {
                   sumValues += Double.valueOf(value.getValue().toString());
                }
            }
        }

        return new TimestampedValue(sumValues / values.size(),
                values.get(0).mTimestamp);
    }



    public static TimestampedValue calculateMeanNlogN(
            final List<TimestampedValue> values) {
        double sumValues = 0.0;

        int j = values.size();
        for (TimestampedValue value : values) {
            for(int i=j;i>0;i=i/2) {
                sumValues += Double.valueOf(value.getValue().toString());
                //k++;
            }
        }

        return new TimestampedValue(sumValues / values.size(),
                values.get(0).mTimestamp);
    }



    public static TimestampedValue calculateMeanParallel(
            final List<TimestampedValue> values, ComputationType type) {
        double sumValues = 0.0;


        prepareForParallelism(values,type);


        return new TimestampedValue(sumValues / values.size(),
                values.get(0).mTimestamp);
    }



    public static void prepareForParallelism(List<TimestampedValue> values, ComputationType type){

        executor = Executors.newFixedThreadPool(N_THREADS);

        int work_size = values.size() / N_THREADS;


        int work_offset = 0;
        while (work_offset < values.size())  {
            executor.execute(new WorkerThread(values, work_offset, work_size,type));
            //System.out.println("no: of times"+i+++","+work_offset+","+work_size);
            work_offset += work_size;
        }

        executor.shutdown();
        while (!executor.isTerminated()) {
        }


    }





}
