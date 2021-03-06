/*
 * Copyright 20xx - 20xx Duke University
    
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License Version 2 as published by
    the Free Software Foundation.
    
    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
    
    You should have received a copy of the GNU General Public License Version 2
    along with this program.  If not, see <https://www.gnu.org/licenses/old-licenses/gpl-2.0.txt>.
 */
package edu.duke.oit.idms.idp.car;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.KeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.util.Date;

import net.shibboleth.utilities.java.support.codec.Base64Support;

import org.opensaml.security.crypto.KeySupport;
import org.opensaml.security.x509.X509Support;
import org.springframework.core.io.Resource;

import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEDecrypter;
import com.nimbusds.jose.JWEEncrypter;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.RSADecrypter;
import com.nimbusds.jose.crypto.RSAEncrypter;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

/**
 * @author shilen
 */
public class JWTUtils {
  
  private static JWSSigner signer;
  
  private static JWEEncrypter encrypter;
  
  private static JWEDecrypter decrypter;
  
  private static JWSVerifier verifier;
    
  private Resource carIdPPrivateKeyResource;
  
  private Resource carCarmaCertificateResource;

  /**
   * 
   */
  public void initialize() {
   
    if (carCarmaCertificateResource == null) {
      throw new RuntimeException("carCarmaCertificateResource is null");
    }
    
    if (carIdPPrivateKeyResource == null) {
      throw new RuntimeException("carIdPPrivateKeyResource is null");
    }

    InputStream is = null;
    try {      
      RSAPrivateKey privateKey = (RSAPrivateKey)KeySupport.decodePrivateKey(carIdPPrivateKeyResource.getFile(), new char[0]);
      signer = new RSASSASigner(privateKey);
      decrypter = new RSADecrypter(privateKey);
    } catch (KeyException e) {
      throw new RuntimeException(e);
    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      if (is != null) {
        try {
          is.close();
        } catch (Exception e) {
          // ignore
        }
      }
    }
    
    is = null;
    try {      
      X509Certificate cert = X509Support.decodeCertificate(carCarmaCertificateResource.getFile());
      encrypter = new RSAEncrypter((RSAPublicKey)cert.getPublicKey());
      verifier = new RSASSAVerifier((RSAPublicKey)cert.getPublicKey());
    } catch (CertificateException e) {
      throw new RuntimeException(e);
    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      if (is != null) {
        try {
          is.close();
        } catch (Exception e) {
          // ignore
        }
      }
    }
  }
  
  /**
   * @param request
   * @param issuer
   * @return string
   */
  public static String signAndEncrypt(String request, String issuer) {
    
    JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
      .expirationTime(new Date(new Date().getTime() + 300 * 1000))
      .issueTime(new Date())
      .issuer(issuer)
      .claim("request", request)
      .build();
    
    SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.RS256), claimsSet);

    try {
      signedJWT.sign(signer);
    } catch (JOSEException e) {
      throw new RuntimeException(e);
    }
    
    JWEObject jweObject = new JWEObject(
        new JWEHeader.Builder(JWEAlgorithm.RSA_OAEP_256, EncryptionMethod.A256GCM)
            .contentType("JWT") // required to signal nested JWT
            .build(),
        new Payload(signedJWT));
    
    try {
      jweObject.encrypt(encrypter);
    } catch (JOSEException e) {
      throw new RuntimeException(e);
    }
  
    String jweString = jweObject.serialize();
       
    return jweString;
  }

  /**
   * @param jweString
   * @return string
   */
  public static String decryptAndVerifySignature(String jweString) {
        
    try {
      JWEObject jweObject = JWEObject.parse(jweString);

      jweObject.decrypt(decrypter);
      
      SignedJWT signedJWT = jweObject.getPayload().toSignedJWT();
      if (!signedJWT.verify(verifier)) {
        throw new RuntimeException("Failed to verify signature");
      }
      
      JWTClaimsSet claimSet = signedJWT.getJWTClaimsSet();

      if (claimSet.getDateClaim("exp").getTime() < System.currentTimeMillis()) {
        throw new RuntimeException("Claim expired at: " + claimSet.getDateClaim("exp").getTime());
      }
      
      if ((claimSet.getDateClaim("iat").getTime() - (300 * 1000)) > System.currentTimeMillis()) {
        throw new RuntimeException("Claimed issue in future: " + claimSet.getDateClaim("iat").getTime());
      }
      
      String base64Response = claimSet.getStringClaim("response");
      String base64Response2 = new String(Base64Support.decode(base64Response), "UTF-8");
      String json = new String(Base64Support.decode(base64Response2), "UTF-8");
      return json;
    } catch (JOSEException e) {
      throw new RuntimeException(e);
    } catch (ParseException e) {
      throw new RuntimeException(e);
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }
  
  /**
   * @param carIdPPrivateKeyResource the carIdPPrivateKeyResource to set
   */
  public void setCarIdPPrivateKeyResource(Resource carIdPPrivateKeyResource) {
    this.carIdPPrivateKeyResource = carIdPPrivateKeyResource;
  }

  
  /**
   * @param carCarmaCertificateResource the carCarmaCertificateResource to set
   */
  public void setCarCarmaCertificateResource(Resource carCarmaCertificateResource) {
    this.carCarmaCertificateResource = carCarmaCertificateResource;
  }
}
