package presentation;

import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;

import java.io.IOException;

public class InputController {
    private final Screen screen;

    public InputController(Screen screen) {
        this.screen = screen;
    }

    public KeyStroke readKey() throws IOException {
        KeyStroke key;
        do {
            key = screen.pollInput();
            if (key == null) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        } while (key == null);
        
        return key;
    }

    public boolean isMoveKey(KeyStroke key) {
        if (key.getKeyType() != KeyType.Character) {
            return false;
        }
        
        char c = Character.toLowerCase(key.getCharacter());
        return c == 'w' || c == 's' || c == 'a' || c == 'd' || c == 'q';
    }

    public int[] getDirection(KeyStroke key) {
        char c = Character.toLowerCase(key.getCharacter());
        
        switch (c) {
            case 'w': return new int[]{0, -1};
            case 's': return new int[]{0, 1};
            case 'a': return new int[]{-1, 0};
            case 'd': return new int[]{1, 0};
            default: return null;
        }
    }

    public boolean isQuit(KeyStroke key) {
        return key.getKeyType() == KeyType.Character && 
               Character.toLowerCase(key.getCharacter()) == 'q';
    }

    public boolean isInventoryKey(KeyStroke key)
    {
        if (key.getKeyType() != KeyType.Character)
        {
            return false;
        }

        char c = Character.toLowerCase(key.getCharacter());

        return c == 'i'
                || c == 'h'
                || c == 'j'
                || c == 'k'
                || c == 'e';
    }

    public char getInventoryType(KeyStroke key) {
        return Character.toLowerCase(key.getCharacter());
    }
}