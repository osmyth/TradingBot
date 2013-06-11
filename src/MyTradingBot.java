import com.lmax.api.*;
import com.lmax.api.account.LoginCallback;
import com.lmax.api.account.LoginRequest;
import com.lmax.api.account.LoginRequest.ProductType;
import com.lmax.api.orderbook.OrderBookEvent;
import com.lmax.api.orderbook.OrderBookEventListener;
import com.lmax.api.orderbook.OrderBookSubscriptionRequest;

import java.util.HashMap;
import java.util.Map;


public class MyTradingBot implements LoginCallback, OrderBookEventListener {
    private Session session;

    private Map<Long, FixedPointNumber> data;

    private final static long SPX500_INSTRUMENT_ID = 100093;
    private final static long EURUSD_INSTRUMENT_ID = 4001;

    public static void main(String[] args) {
        MyTradingBot myTradingBot = new MyTradingBot();

        LmaxApi lmaxApi = new LmaxApi("https://testapi.lmaxtrader.com");
        lmaxApi.login(new LoginRequest("apetherapi", "testlmax1", ProductType.CFD_DEMO), myTradingBot);
    }

    public MyTradingBot() {
        data = new HashMap<Long, FixedPointNumber>();
    }


    public void notify(OrderBookEvent orderBookEvent) {

        // Read the valuationBidPrice and timeStamp
        FixedPointNumber currentValuationBidPrice = orderBookEvent.getValuationBidPrice();
        long currentTimeStamp = orderBookEvent.getTimeStamp();

        long hourStartTime = currentTimeStamp - 36000000;
        long hourEndTime = currentTimeStamp;

        FixedPointNumber lastHourLowValuationBidPrice = getValuationBidPrice(hourStartTime, hourEndTime, false);
        FixedPointNumber lastHourHighValuationBidPrice = getValuationBidPrice(hourStartTime, hourEndTime, true);

        if (lastHourLowValuationBidPrice != null || lastHourHighValuationBidPrice != null) {

            System.out.println("Current Bid Price: " + currentValuationBidPrice + " Last Hour LOW: " + lastHourLowValuationBidPrice + ", HIGH: " + lastHourHighValuationBidPrice);

            // if the current is lower than the last low, set current to last low
            if (currentValuationBidPrice.longValue() < lastHourLowValuationBidPrice.longValue()) {
                System.out.println("Execute Trade - Found new LOW for Last hour: " + currentValuationBidPrice);
            }

            // if the current is higher than the last high, set current to last high
            if (currentValuationBidPrice.longValue() > lastHourHighValuationBidPrice.longValue()) {
                System.out.println("Execute Trade - Found new HIGH for Last hour: " + currentValuationBidPrice);
            }

        }

        data.put(currentTimeStamp, currentValuationBidPrice);
    }

    public FixedPointNumber getValuationBidPrice(long startTime, long endTime, boolean lastHigh) {

        FixedPointNumber lastHourValuationBidPrice = null;

        for (Long key : data.keySet()) {
            if (key < endTime && key >= startTime) {
                FixedPointNumber value = data.get(key);
                if (lastHourValuationBidPrice == null) {
                    lastHourValuationBidPrice = value;
                }

                if (lastHigh && value.longValue() > lastHourValuationBidPrice.longValue()) {
                    lastHourValuationBidPrice = value;
                } else if (!lastHigh && value.longValue() < lastHourValuationBidPrice.longValue()) {
                    lastHourValuationBidPrice = value;
                }

            }
        }

        return lastHourValuationBidPrice;
    }

    public void onLoginSuccess(Session session) {
        session.registerOrderBookEventListener(this);
        session.subscribe(new OrderBookSubscriptionRequest(SPX500_INSTRUMENT_ID), new Callback() {
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

    public Map<Long, FixedPointNumber> getData() {
        return data;
    }

    public void setData(Map<Long, FixedPointNumber> data) {
        this.data = data;
    }
}
