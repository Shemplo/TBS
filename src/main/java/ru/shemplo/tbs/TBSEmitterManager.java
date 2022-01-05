package ru.shemplo.tbs;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.shemplo.tbs.entity.Emitter;
import ru.shemplo.tbs.entity.EmittersDump;
import ru.shemplo.tbs.entity.IEmitter;
import ru.shemplo.tbs.entity.IPlanningBond;
import ru.shemplo.tbs.entity.PlanningDump;

@Slf4j
@NoArgsConstructor (access = AccessLevel.PRIVATE)
public class TBSEmitterManager implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private static volatile TBSEmitterManager instance;
    
    public static TBSEmitterManager getInstance () {
        if (instance == null) {
            synchronized (TBSEmitterManager.class) {
                if (instance == null) {
                    instance = new TBSEmitterManager ();
                }
            }
        }
        
        return instance;
    }
    
    public static final File DUMP_FILE = new File ("emitters.bin");
    
    public static void restore () {
        log.info ("Restoring emitters from a binary file...");
        if (DUMP_FILE.exists ()) {
            TBSDumpService.getInstance ().restore (
                TBSPlanner.DUMP_FILE.getName (), 
                PlanningDump.class
            );
        }
    }
    
    @Getter
    private transient ObservableList <IEmitter> emitters = FXCollections.observableArrayList ();
    private transient Map <Long, IEmitter> id2emitter = new ConcurrentHashMap <> ();
    
    public void addEmitter (long id) {
        if (!hasEmitter (id)) {
            synchronized (id2emitter) {   
                if (!hasEmitter (id)) {                    
                    final var bond = new Emitter (id).getProxy ();
                    
                    id2emitter.put (id, bond);
                    emitters.add (bond);
                    sortThis ();
                    
                    dump ();
                }
            }
        }
    }
    
    private void sortThis () {
        emitters.sort (Comparator.<IEmitter, Long> comparing (IEmitter::getId).reversed ());
        
        updateIndices ();
    }
    
    private void updateIndices () {
        for (int i = 0; i < emitters.size (); i++) {
            final var prop = emitters.get (i).getProperty (
                IPlanningBond.INDEX_PROPERTY, () -> 0, false
            );
            
            prop.set (i + 1);
        }
    }
    
    public void dump () {
        TBSDumpService.getInstance ().dump (new EmittersDump (this), DUMP_FILE.getName ());
    }
    
    public boolean hasEmitter (long id) {
        return id2emitter.containsKey (id);
    }
    
    public IEmitter getEmitterById (long id) {
        return id2emitter.get (id);
    }
    
    private void readObject (ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject ();
        instance = this;
        
        emitters = FXCollections.observableArrayList ();
        TBSUtils.doIfNN (_serializeEmitters, emitters -> {
            emitters.forEach (emitter -> {
                this.emitters.add (emitter.getProxy ());
            });
            
            _serializeEmitters.clear ();
        });
        
        id2emitter = new ConcurrentHashMap <> ();
        for (final var emitter : emitters) {
            id2emitter.put (emitter.getId (), emitter);
        }
        
        sortThis ();
    }
    
    private List <IEmitter> _serializeEmitters;
    
    private void writeObject (ObjectOutputStream out) throws IOException, ClassNotFoundException {
        _serializeEmitters = emitters.stream ().map (IEmitter::getRealObject)
                           . collect (Collectors.toList ());
        out.defaultWriteObject ();
    }
    
}
