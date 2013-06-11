import com.lmax.api.*;
import com.lmax.api.account.LoginCallback;
import com.lmax.api.account.LoginRequest;
import com.lmax.api.account.LoginRequest.ProductType;
import com.lmax.api.orderbook.OrderBookEvent;
import com.lmax.api.orderbook.OrderBookEventListener;
import com.lmax.api.orderbook.OrderBookSubscriptionRequest;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class MyTradingBot implements LoginCallback, OrderBookEventListener {

    private Map<Long, FixedPointNumber> data;

    private final static long SPX500_INSTRUMENT_ID = 100093;
    private final static long EURUSD_INSTRUMENT_ID = 4001;

    public MyTradingBot() {
        data = new HashMap<Long, FixedPointNumber>();
    }

    public Map<Long, FixedPointNumber> getData() {
        return data;
    }

    public void setData(Map<Long, FixedPointNumber> data) {
        this.data = data;
    }

    public void notify(OrderBookEvent orderBookEvent) {

        // Read the currentValuationBidPrice and timeStamp
        FixedPointNumber currentValuationBidPrice = orderBookEvent.getValuationBidPrice();
        long currentTimeStamp = orderBookEvent.getTimeStamp();

        long hourStartTime = currentTimeStamp - 36000000;
        long hourEndTime = currentTimeStamp;

        FixedPointNumber lastHourLowValuationBidPrice = getValuationBidPrice(hourStartTime, hourEndTime, false);
        FixedPointNumber lastHourHighValuationBidPrice = getValuationBidPrice(hourStartTime, hourEndTime, true);

        if (lastHourLowValuationBidPrice != null || lastHourHighValuationBidPrice != null) {

            System.out.println(new Date(currentTimeStamp)+" Price: " + currentValuationBidPrice + " (LOW: " + lastHourLowValuationBidPrice + ") (HIGH: " + lastHourHighValuationBidPrice+")");

            // Check if this is a new low for the last hour
            if (currentValuationBidPrice.longValue() < lastHourLowValuationBidPrice.longValue()) {
                System.out.println("Execute Trade - Found new LOW for Last hour: " + currentValuationBidPrice);
            }

            // Check if this is a new high for the last hour
            if (currentValuationBidPrice.longValue() > lastHourHighValuationBidPrice.longValue()) {
                System.out.println("Execute Trade - Found new HIGH for Last hour: " + currentValuationBidPrice);
            }
        }

        data.put(currentTimeStamp, currentValuationBidPrice);
    }

    public FixedPointNumber getValuationBidPrice(long startTime, long endTime, boolean lastHigh) {
        FixedPointNumber lastHourValuationBidPrice = FixedPointNumber.valueOf(0);

        // iterate over the keySet
        for (Long key : data.keySet()) {
            // if the key (which is the time stamp) is between the startTime and endTime...
            if (key < endTime && key >= startTime) {
                // extract the value for the key
                FixedPointNumber value = data.get(key);

                // if were looking for the last highest bid for the period, then check if the bid is greater than the current value
                // else if were looking for the last lowest bid for the period, then check if the bid is lower than the current value
                if (lastHigh && value.longValue() > lastHourValuationBidPrice.longValue()) {
                    lastHourValuationBidPrice = value;
                } else if (!lastHigh && value.longValue() < lastHourValuationBidPrice.longValue()) {
                    lastHourValuationBidPrice = value;
                }
            }
        }

        // At this point we will either have the highest or lowest bid (depending on lastHigh boolean) over the period.
        return lastHourValuationBidPrice;
    }

    public void onLoginSuccess(Session session) {
        session.registerOrderBookEventListener(this);
        session.subscribe(new OrderBookSubscriptionRequest(EURUSD_INSTRUMENT_ID), new Callback() {
            public void onSuccess() {
                System.out.println("Successful subscription");
            }

            public void onFailure(FailureResponse failureResponse) {
                System.err.printf("Failed to subscribe: %s%n", failureResponse);
            }
        });

        session.start();
    }

    public void onLoginFailure(FailureResponse failureResponse) {
        System.err.printf("Failed to login, reason: %s%n", failureResponse);
    }

    public static void main(String[] args) {
        MyTradingBot myTradingBot = new MyTradingBot();

        LmaxApi lmaxApi = new LmaxApi("https://testapi.lmaxtrader.com");
        lmaxApi.login(new LoginRequest("apetherapi", "testlmax1", ProductType.CFD_DEMO), myTradingBot);
    }
}
