import com.lmax.api.FixedPointNumber;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class MyTradingBotTest {

    private MyTradingBot myTradingBot;

    private static final long START_TIME = 0;
    private static final long END_TIME = 9;

    @Before
    public void beforeTest() {
        myTradingBot = new MyTradingBot();
        Map<Long, FixedPointNumber> data = new HashMap<Long, FixedPointNumber>();


        data.put(START_TIME, FixedPointNumber.valueOf(2));
        data.put(START_TIME + 1, FixedPointNumber.valueOf(3));
        data.put(START_TIME + 2, FixedPointNumber.valueOf(5));
        data.put(START_TIME + 3, FixedPointNumber.valueOf(8));
        data.put(START_TIME + 4, FixedPointNumber.valueOf(4));
        data.put(START_TIME + 5, FixedPointNumber.valueOf(6));
        data.put(START_TIME + 6, FixedPointNumber.valueOf(8));
        data.put(START_TIME + 7, FixedPointNumber.valueOf(9));
        data.put(START_TIME + 8, FixedPointNumber.valueOf(4));
        data.put(START_TIME + 9, FixedPointNumber.valueOf(9));

        myTradingBot.setData(data);
    }

    @Test
    public void testGetValuationBidPriceLow() {
        myTradingBot.getData().put(START_TIME - 1, FixedPointNumber.valueOf(1));
        myTradingBot.getData().put(END_TIME + 1, FixedPointNumber.valueOf(1));

        FixedPointNumber lowBid = myTradingBot.getValuationBidPrice(START_TIME, END_TIME, false);
        Assert.assertEquals(FixedPointNumber.valueOf(2), lowBid);
    }

    @Test
    public void testGetValuationBidPriceHigh() {
        myTradingBot.getData().put(START_TIME - 1, FixedPointNumber.valueOf(10));
        myTradingBot.getData().put(END_TIME + 1, FixedPointNumber.valueOf(10));

        FixedPointNumber highBid = myTradingBot.getValuationBidPrice(START_TIME, END_TIME, true);
        Assert.assertEquals(FixedPointNumber.valueOf(9), highBid);
    }
}
