package game;

public class AuctionThrottleResult {
    public duration delayUntilNext = new duration();
    public boolean throttled;

    public AuctionThrottleResult(Duration delayUntilNext, boolean throttled) {
        delayUntilNext = delayUntilNext;
        throttled = throttled;
    }
}
