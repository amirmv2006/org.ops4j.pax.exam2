package ir.amv.snippets.pax.exam.karaf;

import java.util.logging.Logger;

public class ClassInOsgi {

    public String print() {
        Logger.getLogger(getClass().getName()).warning("Hi There");
        return "Amir";
    }
}
