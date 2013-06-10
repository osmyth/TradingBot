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

    private FixedPointNumber currentValuationBidPrice;

    public static void main(String[] args) {
        MyTradingBot myTradingBot = new MyTradingBot();

        LmaxApi lmaxApi = new LmaxApi("https://testapi.lmaxtrader.com");
        lmaxApi.login(new LoginRequest("apetherapi", "testlmax1", ProductType.CFD_DEMO), myTradingBot);
    }

    public void notify(OrderBookEvent orderBookEvent) {

        FixedPointNumber valuationBidPrice = orderBookEvent.getValuationBidPrice();
        long timeStamp = orderBookEvent.getTimeStamp();

        System.out.println("TimeStamp: "+timeStamp + ", valuationBidPrice: "+valuationBidPrice);
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
