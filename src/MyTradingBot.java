import com.lmax.api.*;
import com.lmax.api.account.LoginCallback;
import com.lmax.api.account.LoginRequest;
import com.lmax.api.account.LoginRequest.ProductType;
import com.lmax.api.orderbook.OrderBookEvent;
import com.lmax.api.orderbook.OrderBookEventListener;
import com.lmax.api.orderbook.OrderBookSubscriptionRequest;


public class MyTradingBot implements LoginCallback, OrderBookEventListener {
    private Session session;

    private final static long SPX500_INSTRUMENT_ID = 100093;
    private final static long EURUSD_INSTRUMENT_ID = 4001;

    private FixedPointNumber lastHourHighValuationBidPrice;
    private FixedPointNumber lastHourLowValuationBidPrice;
    private long lastValuationTimeStamp;

    public static void main(String[] args) {
        MyTradingBot myTradingBot = new MyTradingBot();

        LmaxApi lmaxApi = new LmaxApi("https://testapi.lmaxtrader.com");
        lmaxApi.login(new LoginRequest("apetherapi", "testlmax1", ProductType.CFD_DEMO), myTradingBot);
    }

    public void notify(OrderBookEvent orderBookEvent) {

        // Read the valuationBidPrice and timeStamp
        FixedPointNumber currentValuationBidPrice = orderBookEvent.getValuationBidPrice();
        long currentTimeStamp = orderBookEvent.getTimeStamp();

        // First time this is called both the high and low will be null so set the current bid to both
        if(lastHourLowValuationBidPrice == null && lastHourHighValuationBidPrice == null) {
            lastHourLowValuationBidPrice = currentValuationBidPrice;
            lastHourHighValuationBidPrice = currentValuationBidPrice;
            lastValuationTimeStamp = currentTimeStamp;
        }

        // if an hour has elapsed...
        if(currentTimeStamp > (lastValuationTimeStamp + 3600000)) {

            // if the current is lower than the last low, set current to last low
            if(currentValuationBidPrice.longValue() < lastHourLowValuationBidPrice.longValue()) {
                lastHourLowValuationBidPrice = currentValuationBidPrice;
            }

            // if the current is higher than the last high, set current to last high
            if(currentValuationBidPrice.longValue() > lastHourHighValuationBidPrice.longValue()) {
                lastHourHighValuationBidPrice = currentValuationBidPrice;
            }

            // reset the lastTimeStamp
            lastValuationTimeStamp = currentTimeStamp;

            //System.out.println("TimeStamp: "+currentTimeStamp + ", valuationBidPrice: "+valuationBidPrice);
        }

        //System.out.println("Market data: "+ orderBookEvent);
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
}
