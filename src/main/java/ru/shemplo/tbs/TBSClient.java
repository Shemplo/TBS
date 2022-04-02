package ru.shemplo.tbs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.shemplo.tbs.entity.IProfile;
import ru.tinkoff.piapi.core.InvestApi;

@Slf4j
@NoArgsConstructor (access = AccessLevel.PRIVATE)
public class TBSClient implements AutoCloseable {
    
    private static volatile TBSClient instance;
    
    public static TBSClient getInstance () {
        if (instance == null) {
            synchronized (TBSClient.class) {
                if (instance == null) {
                    instance = new TBSClient ();
                }
            }
        }
        
        return instance;
    }
    
    private volatile InvestApi connection;
    
    private String readToken (IProfile profile) throws IOException {
        log.info ("Reading token from file...");
        final var path = Paths.get (profile.getToken ());
        if (Files.exists (path)) {
            return Files.readString (path);
        } else {
            return profile.getToken ();
        }
    }
    
    public InvestApi getConnection (IProfile profile, TBSLogWrapper log) throws IOException {
        if (connection == null) {
            synchronized (this) {
                if (connection == null) {
                    final var token = readToken (profile);
                    if (token == null) { return null; }
                    
                    log.info ("Connecting to Tinkoff API...");
                    log.info ("{}", profile);
                    //connection = new OkHttpOpenApi (token, !profile.isHighResponsible ());
                    connection = profile.isHighResponsible () 
                               ? InvestApi.create (token) 
                               : InvestApi.createSandbox (token);
                    /*
                    log.info ("Perform indentification in Tinkoff API...");
                    if (connection.isSandboxMode ()) {
                        connection.getSandboxContext ().performRegistration (
                            new SandboxRegisterRequest ()
                        ).join ();
                    }
                    */
                }
            }
        }
        
        return connection;
    }
    
    public void close () throws IOException {
        if (connection != null) {
            log.info ("Closing connection to Tinkoff API...");
            connection = null;
        }
    }
    
}
