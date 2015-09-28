package se.inera.axel.shs.broker.validation.certificate;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import org.testng.annotations.Test;

public class PemConverterTest {
    
    @Test
    public void shouldDetectPemHeaders() {
        boolean b = PemConverter.containsCorrectPemHeaders(TestCertificates.PEM_CERTIFICATE_VALID);
        assertThat(b, equalTo(true));
    }

    @Test
    public void shouldNotDetectPemHeadersWhenEmptyPemCertificate() {
        boolean b = PemConverter.containsCorrectPemHeaders(null);
        assertThat(b, equalTo(false));
    }

    @Test
    public void shouldNotDetectPemHeadersWhenHeadersCorrupted() {
        boolean b = PemConverter.containsCorrectPemHeaders(TestCertificates.PEM_CERTIFICATE_WITH_INVAlID_HEADER);
        assertThat(b, equalTo(false));
    }

    @Test(expectedExceptions = CertificateException.class)
    public void shouldThrowExceptionWhenPemCertificateIsNull() throws CertificateException {
        PemConverter.convertPemToX509Certificate(null);
    }

    @Test
    public void shouldBuildCertificate() throws CertificateException {
        X509Certificate certificate = PemConverter.convertPemToX509Certificate(TestCertificates.PEM_CERTIFICATE_VALID);
        assertThat(certificate, notNullValue());
    }

    @Test(expectedExceptions = CertificateException.class)
    public void shouldThrowExceptionWhenInvalidPemCertificate() throws CertificateException {
        PemConverter.convertPemToX509Certificate(TestCertificates.PEM_CERTIFICATE_WITH_INVALID_CERTIFICATE);
    }
}
