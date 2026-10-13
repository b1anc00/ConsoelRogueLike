package presentation;

import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.TerminalSize;

import java.io.IOException;

public class GameScreen implements AutoCloseable {
    private final Screen screen;

    public GameScreen() throws IOException
    {
        DefaultTerminalFactory factory =
                new DefaultTerminalFactory();

        factory.setInitialTerminalSize(
                new TerminalSize(120, 30)
        );

        Terminal terminal =
                factory.createTerminal();

        this.screen =
                new TerminalScreen(terminal);

        this.screen.startScreen();
        this.screen.setCursorPosition(null);
    }

    public Screen getScreen() {
        return screen;
    }

    public TextGraphics newTextGraphics() {
        return screen.newTextGraphics();
    }

    public void clear() {
        screen.clear();
    }

    public void refresh() throws IOException {
        screen.refresh();
    }

    @Override
    public void close() throws IOException {
        if (screen != null) {
            screen.stopScreen();
        }
    }
}