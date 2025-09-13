package game;

import java.time.LocalDateTime;

public class CommodityQuote {
    public long totalPrice;
    public int quantity;
    public LocalDateTime validTo = LocalDateTime.MIN;
}
