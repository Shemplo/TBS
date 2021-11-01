package ru.shemplo.tbs;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import lombok.extern.slf4j.Slf4j;
import ru.shemplo.tbs.entity.CouponValueMode;
import ru.shemplo.tbs.entity.ITBSProfile;
import ru.shemplo.tbs.entity.TBSProfile;
import ru.shemplo.tbs.xml.Profile;
import ru.tinkoff.invest.openapi.model.rest.Currency;

@Slf4j
public class TBSProfiler {
    
    public static ITBSProfile fetchProfile (String [] args) {
        if (args == null || args.length == 0) {
            return TBSProfile.DEFAULT_RUB;
        }
        
        try {
            final var constant = TBSProfile.valueOf (args [0]);
            if (constant != null) { return constant; }
        } catch (IllegalArgumentException iea) {
            // do nothing - go to next parser
        }
        return parseCustomProfile (args [0]);
    }
    
    private static ITBSProfile parseCustomProfile (String filename) {
        final var file = new File (filename);
        if (!file.exists () || !file.canRead ()) {
            log.error ("Failed to read file with custom profile: {}", file.getAbsolutePath ());
            return null;
        }
        
        try (final var is = new FileInputStream (file)) {
            final var context = JAXBContext.newInstance (Profile.class);
            final var profile = (Profile) context.createUnmarshaller ().unmarshal (is);
            
            final var currencies = Arrays.stream (profile.getCurrencies ().split ("\\s*,\\s*"))
                . map (Currency::fromValue).filter (Objects::nonNull)
                . collect (Collectors.toSet ());
            final var cmodes = Arrays.stream (profile.getCmodes ().split ("\\s*,\\s*"))
                . map (CouponValueMode::fromValue).filter (Objects::nonNull)
                . collect (Collectors.toSet ());
            final var bannede = Arrays.stream (profile.getBannedEmitters ().split ("\\s*,\\s*"))
                . map (v -> {
                    try {
                        return Long.parseLong (v);
                    } catch (NumberFormatException nfe) {
                        return null;
                    }
                }).filter (Objects::nonNull).collect (Collectors.toSet ());
            
            return TBSCustomProfile.builder ()
                .name (profile.getName ())
                .highResponsible (profile.getToken ().isResponsible ())
                .tokenFilename (profile.getToken ().getFilename ())
                .inflation (profile.getGeneral ().getInflation ())
                .maxResults (profile.getGeneral ().getMr ())
                .monthsTillEnd (profile.getParams ().getMte ())
                .couponsPerYear (profile.getParams ().getCpy ())
                .maxDaysToCoupon (profile.getParams ().getMdtc ())
                .nominalValue (profile.getParams ().getNv ())
                .minPercentage (profile.getParams ().getMinp ())
                .maxPrice (profile.getParams ().getMaxpr ())
                .currencies (currencies)
                .couponValuesModes (cmodes)
                .bannedEmitters (bannede)
                .build ();
        } catch (IOException ioe) {
            log.error ("Failed to read file with custom profile caused by some IO error", ioe);
            return null;
        } catch (JAXBException jaxbe) {
            log.error ("Failed to parse custom profile", jaxbe);
            return null;
        }
    }
    
}
