package sensors.impl;

import com.google.common.collect.Lists;
import com.twitter.hbc.ClientBuilder;
import com.twitter.hbc.core.Client;
import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.Hosts;
import com.twitter.hbc.core.endpoint.StatusesFilterEndpoint;
import com.twitter.hbc.core.processor.StringDelimitedProcessor;
import com.twitter.hbc.httpclient.auth.Authentication;
import com.twitter.hbc.httpclient.auth.OAuth1;
import credentials.Twitter;
import sensors.base.AbstractSwanSensor;
import sensors.base.SensorPoller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Roshan Bharath Das on 21/07/16.
 */
public class TwitterSensor extends AbstractSwanSensor{

    private Map<String, TwitterPoller> activeThreads = new HashMap<String, TwitterPoller>();


    private Map<String, String> nameList = new HashMap<String, String>();

    ArrayList<String> arrayList = new ArrayList<String>();

    BlockingQueue<String> queue = new LinkedBlockingQueue<String>(10000);
    StatusesFilterEndpoint endpoint = new StatusesFilterEndpoint();

    Authentication auth = new OAuth1(Twitter.CONSUMER_KEY, Twitter.CONSUMER_SECRET, Twitter.TOKEN, Twitter.TOKEN_SECRET);
    // Authentication auth = new BasicAuth(username, password);

    // Create a new BasicClient. By default gzip is enabled.
    Client client = new ClientBuilder()
            .hosts(Constants.STREAM_HOST)
            .endpoint(endpoint)
            .authentication(auth)
            .processor(new StringDelimitedProcessor(queue))
            .build();




    class TwitterPoller extends SensorPoller {


        TwitterPoller(String id, String valuePath, HashMap configuration) {
            super(id, valuePath, configuration);
        }


        public void run() {
            while (!isInterrupted()) {


                    String msg = null;
                    try {
                        msg = queue.take();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println(msg);



                //System.out.println("Test poller running");

                long now = System.currentTimeMillis();


                System.out.println("DELAY="+DELAY+ " I value="+1);

                updateResult(TwitterSensor.this,1,now);

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

        /*getValues().put(valuePath,
                Collections.synchronizedList(new ArrayList<TimestampedValue>()));*/
        TwitterPoller twitterPoller = new TwitterPoller(id, valuePath,
                configuration);
        activeThreads.put(id, twitterPoller);

        twitterPoller.start();

        if(client!=null) {

            //client.stop();

            arrayList.add((String) configuration.get("name"));
            nameList.put(id, (String) configuration.get("name"));

            endpoint.trackTerms(arrayList);

            client.connect();

        }

    }

    @Override
    public void unregister(String id) {

        super.unregister(id);
        System.out.println("Unregister sensor called");
        activeThreads.remove(id).interrupt();

        if(nameList.containsKey(id)) {
            arrayList.remove(nameList.get(id));
            nameList.remove(id);
        }

        if(nameList.isEmpty()){
            client.stop();
        }


    }


    @Override
    public String[] getValuePaths() {
        return new String[] {"filter"};
    }

    @Override
    public String getEntity() {
        return "twitter";
    }

    @Override
    public String[] getConfiguration() {
        return new String[] {"name","delay"};

    }
}
