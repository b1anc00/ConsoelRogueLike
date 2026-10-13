package presentation;

import java.util.ArrayList;
import java.util.List;

public class MessageLog {
    private final List<String> messages;
    private final int maxMessages;

    public MessageLog(int maxMessages) {
        this.maxMessages = maxMessages;
        this.messages = new ArrayList<>();
    }

    public void addMessage(String message) {
        messages.add(message);
        if (messages.size() > maxMessages) {
            messages.remove(0);
        }
    }

    public List<String> getMessages() {
        return messages;
    }

    public void clear() {
        messages.clear();
    }
}