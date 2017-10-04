package com.swandroid.keatest;


import interdroid.swancore.swansong.TimestampedValue;

import java.util.List;

import static com.swandroid.keatest.Compute.sumValues;


/**
 * Created by Roshan Bharath Das on 04/06/2017.
 */
public class WorkerThread implements Runnable {

    private List<TimestampedValue> values;
    int work_offset;
    int work_size;

    ComputationType workType;

    public WorkerThread(List<TimestampedValue> values, int work_offset, int work_size, ComputationType workType){
        this.values=values;
        this.work_offset = work_offset;
        this.work_size = work_size;
        this.workType = workType;
    }

    @Override
    public void run() {
        if(workType==ComputationType.MEAN_PARALLEL){
            do_MEAN_PARALLEL_Work();
        }
        else if(workType==ComputationType.MEAN_ON2_PARALLEL){
            do_MEAN_ON2_PARALLEL_Work();
        }
        else if(workType==ComputationType.MEAN_ON3_PARALLEL){
            do_MEAN_ON3_PARALLEL_Work();
        }


    }


    private void do_MEAN_PARALLEL_Work() {

        int total_work_size = values.size();
        int sum = 0;


        for (int i = work_offset; i < (work_offset + work_size) && i < total_work_size; ++i) {
                sum += Double.valueOf(values.get(i).getValue().toString());
        }

        synchronized ((Object) sumValues){
            sumValues += sum;
        }
    }



    private void do_MEAN_ON2_PARALLEL_Work() {

        int total_work_size = values.size();
        int sum = 0;

        for (TimestampedValue value2 : values) {
            for (int i = work_offset; i < (work_offset + work_size) && i < total_work_size; ++i) {
                sum += Double.valueOf(values.get(i).getValue().toString());
            }
        }


        synchronized ((Object) sumValues){
            sumValues += sum;
        }
    }



    private void do_MEAN_ON3_PARALLEL_Work() {

        int total_work_size = values.size();
        int sum = 0;

        for (TimestampedValue value1 : values) {
            for (TimestampedValue value2 : values) {
                for (int i = work_offset; i < (work_offset + work_size) && i < total_work_size; ++i) {
                    sum += Double.valueOf(values.get(i).getValue().toString());
                }
            }
        }

        synchronized ((Object) sumValues){
            sumValues += sum;
        }
    }



}

