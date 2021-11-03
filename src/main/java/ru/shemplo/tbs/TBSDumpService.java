package ru.shemplo.tbs;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TBSDumpService {
    
    @Setter
    private static volatile TBSDumpService instance;
    
    public static TBSDumpService getInstance () {
        if (instance == null) {
            synchronized (TBSDumpService.class) {
                if (instance == null) {
                    instance = new TBSDumpService ();
                }
            }
        }
        
        return instance;
    }
    
    public void dump (Serializable object, String filename) {
        try (
            final var fos = new FileOutputStream (filename);
            final var oos = new ObjectOutputStream (fos);
        ) { 
            oos.writeObject (object);
        } catch (IOException ioe) {
            log.error ("Failed to dump to `" + filename + "` (" + ioe + ")", ioe);
        }
    }
    
    public <T extends Serializable> T restore (String filename, Class <T> type) {
        try (
            final var fis = new FileInputStream (filename);
            final var ois = new ObjectInputStream (fis);
        ) {
            @SuppressWarnings ("unchecked")
            final var object = (T) ois.readObject ();
            
            return object;
        } catch (IOException | ClassNotFoundException ioe) {
            log.error ("Failed to restore from `" + filename + "` (" + ioe + ")", ioe);
            return null;
        }
    }
    
}
