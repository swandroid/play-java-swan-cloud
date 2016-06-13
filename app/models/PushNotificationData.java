package models;

import com.avaje.ebean.Model;

import javax.persistence.Id;

/**
 * Created by goose on 30/05/16.
 */
public class PushNotificationData {


    public String to;

    public Data data;

    public static class Data {
        public Object field;
    }


}
