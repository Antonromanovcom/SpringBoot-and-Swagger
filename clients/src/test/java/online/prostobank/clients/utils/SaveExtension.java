package online.prostobank.clients.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SaveExtension {
    @Test
    public void normalExtension() {
        String name = "file_name.ext";
        assertEquals(".ext", Utils.getFileNameExtension(name));
    }

    @Test
    public void noExtension() {
        String name = "file_name";
        assertEquals("", Utils.getFileNameExtension(name));
    }

    @Test
    public void doubleExtension() {
        String name = "file_name.one.two";
        assertEquals(".two", Utils.getFileNameExtension(name));
    }

    @Test
    public void emptyName() {
        String name = "";
        assertEquals("", Utils.getFileNameExtension(name));
        name = null;
        assertEquals("", Utils.getFileNameExtension(name));
    }
}
