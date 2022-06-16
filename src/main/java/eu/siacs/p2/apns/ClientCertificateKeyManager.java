package eu.siacs.p2.apns;

import com.google.common.base.Strings;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.Socket;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import javax.net.ssl.X509KeyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientCertificateKeyManager implements X509KeyManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientCertificateKeyManager.class);

    private final CertificateFactory certificateFactory;

    private final ApnsConfiguration apnsConfiguration;

    ClientCertificateKeyManager(final ApnsConfiguration apnsConfiguration) {
        this.apnsConfiguration = apnsConfiguration;
        try {
            certificateFactory = CertificateFactory.getInstance("X.509");
        } catch (CertificateException e) {
            throw new AssertionError(e);
        }
    }

    @Override
    public String[] getClientAliases(String s, Principal[] principals) {
        return new String[0];
    }

    @Override
    public String chooseClientAlias(String[] strings, Principal[] principals, Socket socket) {
        return "apple";
    }

    @Override
    public String[] getServerAliases(String s, Principal[] principals) {
        return new String[0];
    }

    @Override
    public String chooseServerAlias(String s, Principal[] principals, Socket socket) {
        return null;
    }

    @Override
    public X509Certificate[] getCertificateChain(String alias) {
        final String certificateFile = apnsConfiguration.certificate();
        if (Strings.isNullOrEmpty(certificateFile)) {
            LOGGER.error("No client certificate configured");
            return new X509Certificate[0];
        }
        final File file = new File(certificateFile);
        try {
            final FileInputStream fileInputStream = new FileInputStream(file);
            final Collection<? extends Certificate> certificates =
                    certificateFactory.generateCertificates(fileInputStream);
            final List<X509Certificate> x509Certificates = new ArrayList<>();
            for (Certificate certificate : certificates) {
                if (certificate instanceof X509Certificate) {
                    x509Certificates.add((X509Certificate) certificate);
                }
            }
            return x509Certificates.toArray(new X509Certificate[0]);
        } catch (FileNotFoundException | CertificateException e) {
            LOGGER.error("Unable to load client certificates", e);
            return new X509Certificate[0];
        }
    }

    @Override
    public PrivateKey getPrivateKey(String s) {
        // openssl pkcs8 -topk8 -inform PEM -outform PEM -nocrypt -in
        // /home/daniel/Projects/letsencrypt.sh/certs/gultsch.de/privkey.pem -out privatekey.pem
        final String privateKeyFile = apnsConfiguration.privateKey();
        if (Strings.isNullOrEmpty(privateKeyFile)) {
            LOGGER.error(
                    "Unable to load private key for client certificate authentication. No key"
                            + " configured");
            return null;
        }
        final File file = new File(privateKeyFile);
        try {
            final String key =
                    Files.lines(file.toPath())
                            .filter(l -> !l.startsWith("----"))
                            .collect(Collectors.joining());
            final KeyFactory kf = KeyFactory.getInstance("RSA");
            final byte[] encodedKey = Base64.getDecoder().decode(key);
            final PKCS8EncodedKeySpec keySpecPKCS8 = new PKCS8EncodedKeySpec(encodedKey);
            return kf.generatePrivate(keySpecPKCS8);
        } catch (final Exception e) {
            LOGGER.error("Unable to load private key for client certificate authentication", e);
            return null;
        }
    }
}
