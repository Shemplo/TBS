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
import ru.shemplo.tbs.entity.IProfile;
import ru.shemplo.tbs.entity.Profile;
import ru.shemplo.tbs.entity.ProfilePreset;
import ru.shemplo.tbs.entity.Range;
import ru.shemplo.tbs.xml.ProfileSchema;
import ru.tinkoff.invest.openapi.model.rest.Currency;

@Slf4j
@Deprecated
public class TBSProfiler {
    
    public static IProfile fetchProfile (String [] args) {
        if (args == null || args.length == 0) {
            return ProfilePreset.DEFAULT_RUB;
        }
        
        try {
            final var constant = ProfilePreset.valueOf (args [0]);
            if (constant != null) { return constant; }
        } catch (IllegalArgumentException iea) {
            // do nothing - go to next parser
        }
        return parseCustomProfile (args [0]);
    }
    
    private static IProfile parseCustomProfile (String filename) {
        final var file = new File (filename);
        if (!file.exists () || !file.canRead ()) {
            log.error ("Failed to read file with custom profile: {}", file.getAbsolutePath ());
            return null;
        }
        
        try (final var is = new FileInputStream (file)) {
            final var context = JAXBContext.newInstance (ProfileSchema.class);
            final var profile = (ProfileSchema) context.createUnmarshaller ().unmarshal (is);
            
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
            
            return Profile.builder ()
                .name (profile.getName ())
                .highResponsible (profile.getToken ().isResponsible ())
                .token (profile.getToken ().getFilename ())
                .inflation (profile.getGeneral ().getInflation ())
                .maxResults (profile.getGeneral ().getMr ())
                .monthsTillEnd (new Range <> (TBSUtils.mapIfNN (profile.getParams ().getMte (), Long::intValue, null), null))
                .couponsPerYear (new Range <> (TBSUtils.mapIfNN (profile.getParams ().getCpy (), Long::intValue, null), null))
                .daysToCoupon (new Range <> (null, TBSUtils.mapIfNN (profile.getParams ().getMdtc (), Long::intValue, null)))
                .nominalValue (new Range <> (profile.getParams ().getNv (), null))
                .percentage (new Range <> (profile.getParams ().getMinp (), null))
                .price (new Range <> (null, profile.getParams ().getMaxpr ()))
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
