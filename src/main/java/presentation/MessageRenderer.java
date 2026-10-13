package presentation;

import com.googlecode.lanterna.graphics.TextGraphics;

public class MessageRenderer {
    private int startX;
    private final int startY;

    public MessageRenderer(int startX, int startY) {
        this.startX = startX;
        this.startY = startY;
    }

    public void setStartX(int startX)
    {
        this.startX = startX;
    }

    public void render(TextGraphics graphics, MessageLog log)
    {
        int y = startY;

        graphics.putString(
                startX,
                y++,
                "=== MESSAGES ==="
        );

        for (String message : log.getMessages())
        {
            if (message.length() > 27)
            {
                message = message.substring(0, 24) + "...";
            }

            graphics.putString(
                    startX,
                    y++,
                    message
            );
        }
    }
}