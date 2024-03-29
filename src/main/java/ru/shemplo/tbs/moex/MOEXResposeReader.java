package ru.shemplo.tbs.moex;

import java.io.IOException;
import java.net.URL;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import ru.shemplo.tbs.moex.xml.Document;

public class MOEXResposeReader {
    
    public static Document read (URL url) {
        try {
            final var context = JAXBContext.newInstance (Document.class);
            final var value = (Document) context.createUnmarshaller ().unmarshal (url);
            return value;
        } catch (JAXBException jaxbe) {
            jaxbe.printStackTrace ();
            
            try {                
                final var connection = url.openConnection ();
                connection.connect ();
                
                try (final var is = connection.getInputStream ()) {
                    System.out.println ("URL: " + url);
                    System.out.println ("Headers: " + connection.getHeaderFields ()); // SYSOUT
                    System.out.println ("Response: ");
                    System.out.println (new String (is.readAllBytes ()));
                }
            } catch (IOException ioe) {
                ioe.printStackTrace ();
            }
            
            return null;
        }
    }
    
}
