// Credit goes to:
// • https://stackoverflow.com/questions/1828775/how-to-handle-invalid-ssl-certificates-with-apache-httpclient/1828840#1828840
// • https://stackoverflow.com/questions/19005318/implementing-x509trustmanager-passing-on-part-of-the-verification-to-existing#19005844

import java.net.URL;
import java.security.SecureRandom;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

public class HTTPSTest {

    public static void main(String [] args) throws Exception {
        TrustManagerFactory tmf = TrustManagerFactory
                .getInstance(TrustManagerFactory.getDefaultAlgorithm());

        // Using null here initialises the TMF with the default trust store.
        tmf.init((KeyStore) null);

        // Get hold of the default trust manager
        X509TrustManager x509tm = null;
        for (TrustManager tm : tmf.getTrustManagers()) {

            // Just set to the first and only instance of X509TrustManager
            if (tm instanceof X509TrustManager) {
                x509tm = (X509TrustManager) tm;
                break;
            }
        }

        // configure the SSLContext with the default TrustManager
        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(new KeyManager[0], new TrustManager[] {x509tm}, new SecureRandom());
        SSLContext.setDefault(ctx);

        // Main class takes variable arguments of URLs
        for (String testURL: args) {
            System.out.println("GET " + testURL + "\n");

            // No checks performed on URLs, all expected to be HTTPS
            URL url = new URL(testURL);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();

            // Print out the HTTP response code and message
            System.out.println(conn.getResponseCode() + " " + conn.getResponseMessage());

            // Print out all the headers
            int i = 1;
            while(conn.getHeaderFieldKey(i) != null) {
                System.out.println(conn.getHeaderFieldKey(i) + ": " + conn.getHeaderField(i));
                i++;
            }
            conn.disconnect();

            System.out.println("\n\n");
        }
    }
}
