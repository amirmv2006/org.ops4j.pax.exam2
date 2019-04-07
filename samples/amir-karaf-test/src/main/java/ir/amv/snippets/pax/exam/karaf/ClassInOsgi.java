package ir.amv.snippets.pax.exam.karaf;

import java.util.logging.Logger;

public class ClassInOsgi {

    public void print() {
        Logger.getLogger(getClass().getName()).warning("Hi There");
    }
}
