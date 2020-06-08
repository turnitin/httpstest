# HTTPSTest

A simple program to test HTTPS connections

## How to run HTTPSTest

`java -jar httpstest.jar https://5952f2153628c10646609f4829e786da.ithenticate.com https://api.ithenticate.com`

Output:

```text
https://5952f2153628c10646609f4829e786da.ithenticate.com

302 Found
Cache-Control: private, no-cache, no-store, must-revalidate
Pragma: no-cache
Location: http://www.ithenticate.com/
Set-Cookie: ithenticate_session=[redacted]; path=/; secure; HttpOnly
X-Frame-Options: SAMEORIGIN
Content-Length: 311
Content-Type: text/html; charset=utf-8
Date: Mon, 08 Jun 2020 22:43:02 GMT
Connection: keep-alive



https://api.ithenticate.com

302 Found
Cache-Control: private, no-cache, no-store, must-revalidate
Pragma: no-cache
Location: http://www.ithenticate.com/
Set-Cookie: ithenticate_session=[redacted]; path=/; secure; HttpOnly
X-Frame-Options: SAMEORIGIN
Content-Length: 311
Content-Type: text/html; charset=utf-8
Date: Mon, 08 Jun 2020 22:43:03 GMT
Connection: keep-alive



```

## Reproducing errors

Here is how I reproduced the reported errors

### `handshake_failure`

The full error is:

```text
javax.net.ssl.SSLHandshakeException: Received fatal alert: handshake_failure
```

I reproduced this error with JRE 1.6.0_38 which does not support TLS 1.2. This is the error encountered when TLS 1.0 is not supported on the server. For example, I can reproduce this error with `httpstest.jar` on our main endpoint https://api.ithenticate.com

`java -jar httpstest.jar https://api.ithenticate.com`

Output:

```text
GET https://api.ithenticate.com

Exception in thread "main" javax.net.ssl.SSLHandshakeException: Received fatal alert: handshake_failure
	at sun.security.ssl.Alerts.getSSLException(Alerts.java:192)
	at sun.security.ssl.Alerts.getSSLException(Alerts.java:154)
	at sun.security.ssl.SSLSocketImpl.recvAlert(SSLSocketImpl.java:1902)
	at sun.security.ssl.SSLSocketImpl.readRecord(SSLSocketImpl.java:1074)
	at sun.security.ssl.SSLSocketImpl.performInitialHandshake(SSLSocketImpl.java:1320)
	at sun.security.ssl.SSLSocketImpl.startHandshake(SSLSocketImpl.java:1347)
	at sun.security.ssl.SSLSocketImpl.startHandshake(SSLSocketImpl.java:1331)
	at sun.net.www.protocol.https.HttpsClient.afterConnect(HttpsClient.java:440)
	at sun.net.www.protocol.https.AbstractDelegateHttpsURLConnection.connect(AbstractDelegateHttpsURLConnection.java:185)
	at sun.net.www.protocol.http.HttpURLConnection.getInputStream(HttpURLConnection.java:1227)
	at java.net.HttpURLConnection.getResponseCode(HttpURLConnection.java:397)
	at sun.net.www.protocol.https.HttpsURLConnectionImpl.getResponseCode(HttpsURLConnectionImpl.java:338)
	at HTTPSTest.main(HTTPSTest.java:49)
```

### `unable to find valid certification path to requested target`

The full error is:

```text
javax.net.ssl.SSLHandshakeException: sun.security.validator.ValidatorException: PKIX path building failed: sun.security.provider.certpath.SunCertPathBuilderException: unable to find valid certification path to requested target
```

I reproduced this error in any environment that does not contain the UserTrust RSA CA root as a trusted CA root.

`java -jar httpstest.jar https://5952f2153628c10646609f4829e786da.ithenticate.com`

Output:

```
GET https://5952f2153628c10646609f4829e786da.ithenticate.com

Exception in thread "main" javax.net.ssl.SSLHandshakeException: sun.security.validator.ValidatorException: PKIX path building failed: sun.security.provider.certpath.SunCertPathBuilderException: unable to find valid certification path to requested target
	at sun.security.ssl.Alerts.getSSLException(Alerts.java:192)
	at sun.security.ssl.SSLSocketImpl.fatal(SSLSocketImpl.java:1836)
	at sun.security.ssl.Handshaker.fatalSE(Handshaker.java:287)
	at sun.security.ssl.Handshaker.fatalSE(Handshaker.java:281)
	at sun.security.ssl.ClientHandshaker.serverCertificate(ClientHandshaker.java:1279)
	at sun.security.ssl.ClientHandshaker.processMessage(ClientHandshaker.java:202)
	at sun.security.ssl.Handshaker.processLoop(Handshaker.java:848)
	at sun.security.ssl.Handshaker.process_record(Handshaker.java:784)
	at sun.security.ssl.SSLSocketImpl.readRecord(SSLSocketImpl.java:1012)
	at sun.security.ssl.SSLSocketImpl.performInitialHandshake(SSLSocketImpl.java:1320)
	at sun.security.ssl.SSLSocketImpl.startHandshake(SSLSocketImpl.java:1347)
	at sun.security.ssl.SSLSocketImpl.startHandshake(SSLSocketImpl.java:1331)
	at sun.net.www.protocol.https.HttpsClient.afterConnect(HttpsClient.java:440)
	at sun.net.www.protocol.https.AbstractDelegateHttpsURLConnection.connect(AbstractDelegateHttpsURLConnection.java:185)
	at sun.net.www.protocol.http.HttpURLConnection.getInputStream(HttpURLConnection.java:1227)
	at java.net.HttpURLConnection.getResponseCode(HttpURLConnection.java:397)
	at sun.net.www.protocol.https.HttpsURLConnectionImpl.getResponseCode(HttpsURLConnectionImpl.java:338)
	at HTTPSTest.main(HTTPSTest.java:49)
Caused by: sun.security.validator.ValidatorException: PKIX path building failed: sun.security.provider.certpath.SunCertPathBuilderException: unable to find valid certification path to requested target
	at sun.security.validator.PKIXValidator.doBuild(PKIXValidator.java:386)
	at sun.security.validator.PKIXValidator.engineValidate(PKIXValidator.java:293)
	at sun.security.validator.Validator.validate(Validator.java:260)
	at sun.security.validator.Validator.validate(Validator.java:236)
	at sun.security.ssl.X509TrustManagerImpl.validate(X509TrustManagerImpl.java:147)
	at sun.security.ssl.X509TrustManagerImpl.checkServerTrusted(X509TrustManagerImpl.java:230)
	at sun.security.ssl.X509TrustManagerImpl.checkServerTrusted(X509TrustManagerImpl.java:270)
	at sun.security.ssl.ClientHandshaker.serverCertificate(ClientHandshaker.java:1258)
	... 13 more
Caused by: sun.security.provider.certpath.SunCertPathBuilderException: unable to find valid certification path to requested target
	at sun.security.provider.certpath.SunCertPathBuilder.engineBuild(SunCertPathBuilder.java:197)
	at java.security.cert.CertPathBuilder.build(CertPathBuilder.java:255)
	at sun.security.validator.PKIXValidator.doBuild(PKIXValidator.java:381)
	... 20 more
```

After importing the UserTrust RSA CA root for our server's TLS certificate, this error goes away.

`keytool -importcert -trustcacerts -keystore $JAVA_HOME/jre/lib/security/cacerts -storepass changeit -noprompt -file ./usertrust_rsa_ca.pem -alias usertrust_rsa_ca.pem`

Output:

```
Certificate was added to keystore
```

`java -jar httpstest.jar https://5952f2153628c10646609f4829e786da.ithenticate.com`

Output:

```
GET https://5952f2153628c10646609f4829e786da.ithenticate.com

302 Found
Cache-Control: private, no-cache, no-store, must-revalidate
Pragma: no-cache
Location: http://www.ithenticate.com/
Set-Cookie: ithenticate_session=684869e3008b50be382615922b77e209c096d745; path=/; secure; HttpOnly
X-Frame-Options: SAMEORIGIN
Content-Length: 311
Content-Type: text/html; charset=utf-8
Date: Mon, 08 Jun 2020 23:02:41 GMT
Connection: keep-alive


```

## Files in repo

### `HTTPSTest.java`

Source code for `httpstest.jar`. Uses the default `TrustManager` and `HostnameVerifier` provided by Java.

### `httpstest.jar`

I built this archive with a Java 1.6.0_38 environment. Takes variable number of URLs as arguments. Prints the HTTP response message and response headers upon successful HTTPS connection.

```
$ javac HTTPSTest.java
$ jar cvfe httpstest.jar HTTPSTest HTTPSTest.class
```

### `usertrust_rsa_ca.pem`

This is the CA Root for our server certificate. This needs to be imported into the trusted keystore.

Here is an example of how to import:

```
$ keytool -importcert -trustcacerts -keystore $JAVA_HOME/jre/lib/security/cacerts -storepass changeit -noprompt -file ./usertrust_rsa_ca.pem -alias usertrust_rsa_ca.pem
```
