package game;

import java.util.ArrayList;

class CategoryCheck {
    public ArrayList<SHORT> checks = new ArrayList<>();
    public short currentIndex;

    public CategoryCheck(ArrayList<SHORT> checks) {
        checks = checks;
        currentIndex = 0;
    }

    public final boolean empty() {
        return checks.isEmpty();
    }

    public final boolean isAtEnd() {
        return currentIndex >= checks.size();
    }

    public final void shuffle() {
        checks = checks.shuffle().ToList();
        currentIndex = 0;
    }
}
