package ru.shemplo.tbs;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.shemplo.tbs.entity.Bond;
import ru.shemplo.tbs.entity.IProfile;

@Slf4j
@NoArgsConstructor (access = AccessLevel.PRIVATE)
public class TBSBondDetailsManager implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private static volatile TBSBondDetailsManager instance;
    
    public static TBSBondDetailsManager getInstance () {
        if (instance == null) {
            synchronized (TBSBondDetailsManager.class) {
                if (instance == null) {
                    instance = new TBSBondDetailsManager ();
                }
            }
        }
        
        return instance;
    }
    
    private static final File DUMP_FILE = new File ("details.bin");
    
    public void dump (IProfile profile) {
        TBSDumpService.getInstance ().dump (this, TBSBondDetailsManager.DUMP_FILE.getName ());
    }
    
    public static void restore () {
        log.info ("Restoring bonds details from a binary file...");
        TBSDumpService.getInstance ().restore (TBSBondDetailsManager.DUMP_FILE.getName ());
    }
    
    private void readObject (ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject ();
        instance = this;
        
        if (detailed == null) {
            detailed = new ArrayList <> ();
        }
    }
    
    private List <Bond> detailed;
    
    public void addDetailed (IProfile profile, Bond bond) {
        if (detailed == null) {
            detailed = new ArrayList <> ();
        }
        
        detailed.add (bond);
        dump (profile);
    }
    
    public void removeDetailed (IProfile profile, String ticker) {
        detailed.removeIf (bond -> bond.getCode ().equals (ticker));
        dump (profile);
    }
    
    public List <Bond> getDetailed () {
        return detailed == null ? List.of () : Collections.unmodifiableList (detailed);
    }
    
}
