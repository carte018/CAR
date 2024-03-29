/*
 * Copyright 2015 - 2019 Duke University
 
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
package edu.internet2.consent.car;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.security.cert.Certificate;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.CertificateFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.springframework.http.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.ehcache.Cache.Entry;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEAlgorithm;
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
import com.nimbusds.jose.crypto.bc.BouncyCastleProviderSingleton;
import com.nimbusds.jose.util.StandardCharset;

import javax.annotation.PostConstruct;

import edu.internet2.consent.arpsi.model.DecisionOnValues;
import edu.internet2.consent.arpsi.model.DecisionsForInfoStatement;
import edu.internet2.consent.icm.model.DecisionRequestObject;
import edu.internet2.consent.icm.model.IcmDecisionOnValues;
import edu.internet2.consent.icm.model.IcmDecisionResponseObject;
import edu.internet2.consent.icm.model.IcmDecisionsForInfoStatement;
import edu.internet2.consent.icm.model.InfoId;
import edu.internet2.consent.icm.model.InfoIdPlusValues;
import edu.internet2.consent.icm.model.PolicySourceEnum;
import edu.internet2.consent.icm.model.RelyingPartyId;
import edu.internet2.consent.icm.model.RelyingPartyProperty;
import edu.internet2.consent.icm.model.ResourceHolderId;
import edu.internet2.consent.icm.model.UserDirectiveAllOtherValues;
import edu.internet2.consent.icm.model.UserDirectiveOnValues;
import edu.internet2.consent.icm.model.UserId;
import edu.internet2.consent.icm.model.UserInfoReleasePolicy;
import edu.internet2.consent.icm.model.UserInfoReleaseStatement;
import edu.internet2.consent.icm.model.UserProperty;
import edu.internet2.consent.icm.model.UserReleaseDirective;
import edu.internet2.consent.icm.model.UserReturnedPolicy;
import edu.internet2.consent.informed.model.InfoItemIdentifier;
import edu.internet2.consent.informed.model.InfoItemValueList;
import edu.internet2.consent.informed.model.RHIdentifier;
import edu.internet2.consent.informed.model.RPIdentifier;
import edu.internet2.consent.informed.model.ReturnedInfoItemMetaInformation;
import edu.internet2.consent.informed.model.ReturnedRHMetaInformation;
import edu.internet2.consent.informed.model.ReturnedRPMetaInformation;
import edu.internet2.consent.informed.model.ReturnedRPOptionalInfoItemList;
import edu.internet2.consent.informed.model.ReturnedRPProperty;
import edu.internet2.consent.informed.model.ReturnedRPRequiredInfoItemList;
import edu.internet2.consent.informed.model.ReturnedUserRPMetaInformation;
import edu.internet2.consent.informed.model.ReturnedValueMetaInformation;
import edu.internet2.consent.informed.model.ScopeMapping;

@Controller
public class MainController {
	
	
	private String generateCSRFToken() {
		String foo = RandomStringUtils.random(32,true,true);
		String bar = Base64.encodeBase64URLSafeString(foo.getBytes());
		return bar;
	}
	
	private String signAndEncryptToRHAsJWT(String payload,String rhid) {
		// Take the input payload and return a base64 string
		// to send as "json" in the result response POST
		// containing a JWT signed with our private key and
		// encrypted in the RH's public key containing:
		//
		// result - a base64 string containing the JSON of the actual response
		// iat - issuance timestamp (as seconds since the epoch)
		// exp - expiration timestamp (as seconds since the epoch -- exp + 5 minutes)
		//
		
		//Security.addProvider(new BouncyCastleProvider());
		//Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);  // remove before add
		Security.insertProviderAt(BouncyCastleProviderSingleton.getInstance(),  1);

		CarConfig config = CarConfig.getInstance();
		
		String myprivfile=config.getProperty("car.privatekeyfile", true);
		String shibcertfile = config.getProperty("car.rhcertfile", true);  // If everything is using the same RH key
		
		// In case there is a unique key for this RH...
		
		int rct = 1;
		String rhent = "begin";
		while (rhent != null) {
			rhent = config.getProperty("car.rhcertentity."+rct, false);
			if (rhent != null && rhent.equals(rhid)) {
				String cf = config.getProperty("car.rhcertfile."+rct,false);
				if (cf != null && ! cf.equalsIgnoreCase("")) {
					shibcertfile = cf;
				}
			}
			rct += 1;
		}
		
		// Now shibcertfile has the appropriate key file name
		// Start by base64ing the payload string (usually a JSON blob)
		String base64 = new String(Base64.encodeBase64(payload.getBytes()));
		
		// Create a JWT from it
		
		JWSSigner signer = null;
		JWEEncrypter encrypter = null;
		
	    JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
	    	      .expirationTime(new Date(new Date().getTime() + 300 * 1000))
	    	      .issueTime(new Date())
	    	      .claim("response", base64)
	    	      .build();
	    SignedJWT sjwt = new SignedJWT(new JWSHeader(JWSAlgorithm.RS256), claimsSet);
		
		try {
			File privinfile = new File(myprivfile);
			byte[] privbytes = new byte[(int) privinfile.length()];
			FileInputStream pfis = new FileInputStream(privinfile);
			pfis.read(privbytes);
			pfis.close();
			
			// privbytes now has the private key in der format
			PKCS8EncodedKeySpec pspec = new PKCS8EncodedKeySpec(privbytes);
			KeyFactory keyf = KeyFactory.getInstance("RSA");
			PrivateKey privkey = keyf.generatePrivate(pspec);

			// privkey now has the PrivateKey object
			
			if (privkey instanceof RSAPrivateKey) {
				// successfully built private key
				signer = new RSASSASigner(privkey);
				sjwt.sign(signer);
				// Now encrypt the signed JWT
                PublicKey pbk;
                FileInputStream in = new FileInputStream(shibcertfile);
                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                Certificate cert = cf.generateCertificate(in);

                pbk = cert.getPublicKey();
                if (pbk instanceof RSAPublicKey) {
                	// Valid RSA key from the cert -- encrypt with it
                    JWEObject jweObject = new JWEObject(
                            new JWEHeader.Builder(JWEAlgorithm.RSA_OAEP_256, EncryptionMethod.A256GCM)
                                .contentType("JWT") // required to signal nested JWT
                                .build(),
                            new Payload(sjwt));
                    encrypter = new RSAEncrypter((RSAPublicKey)pbk);
                    jweObject.encrypt(encrypter);
                    String serialized = jweObject.serialize();
                    // debug
                    //CarUtility.locLog("LOG1001",serialized);
                    return serialized;
                    
                } else {
                	// RSA public key retrieval failed
                	CarUtility.locError("ERR1109");
                	return null;
                }
			} else {
				// RSA private key for us failed retrieval
				CarUtility.locError("ERR1110");
				return null;
			}
		} catch (Exception e) {
			CarUtility.locError("ERR1111",e.getMessage());
			return null;
		} finally {
			Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
		}
	}
	
	private InputRequest cryptoParseInput(HttpServletRequest request) {
		
		// Add the BC provider for the crypto type we need
		//Security.addProvider(new BouncyCastleProvider());
		//Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
		Security.insertProviderAt(BouncyCastleProviderSingleton.getInstance(), 1);
		
		// Retrieve the base64 encoded encrypted, signed input object
		
		String b64input = request.getParameter("json");
				
		// Turn it into a JWE
		JWEObject jwe = null;
		try {
			jwe = JWEObject.parse(b64input);
		} catch (Exception e) {
			// On exception, return null
			CarUtility.locError("ERR1101");
			return null;
		}
		
		// Decrypt the JWE using the appropriate key
		// TODO:  For the moment, there is only one key -- we need to make the input include the unencrypted entityId of the 
		// TODO:  requesting RH so that we can determine which of a possible set of keys to use.
		// TODO:  For now, we assume there's only one key across all the RHs (which in the Duke case is currently correct)
		//
		CarConfig config = CarConfig.getInstance();
		
		String myprivfile = config.getProperty("car.privatekeyfile", true);
		String shibcertfile = config.getProperty("car.rhcertfile", true);
		
		try {
			File privinfile = new File(myprivfile);
			byte[] privbytes = new byte[(int) privinfile.length()];
			FileInputStream pfis = new FileInputStream(privinfile);
			pfis.read(privbytes);
			pfis.close();
			// privbytes now has the private key in der format
			PKCS8EncodedKeySpec pspec = new PKCS8EncodedKeySpec(privbytes);
			KeyFactory keyf = KeyFactory.getInstance("RSA");
			PrivateKey privkey = keyf.generatePrivate(pspec);
			// privkey now has the PrivateKey object
			
			if (privkey instanceof RSAPrivateKey) {
                	jwe.decrypt(new RSADecrypter(privkey));
                	SignedJWT sjwt = jwe.getPayload().toSignedJWT();
                    // Use certificate to validate signature
                    PublicKey pbk;
                    FileInputStream in = new FileInputStream(shibcertfile);
                    CertificateFactory cf = CertificateFactory.getInstance("X.509");
                    Certificate cert = cf.generateCertificate(in);

                    pbk = cert.getPublicKey();
                    if (pbk instanceof RSAPublicKey) {
                            JWSVerifier jwsv = new RSASSAVerifier((RSAPublicKey) pbk);
                            if (sjwt.verify(jwsv)) {
                            	// Everything worked -- the signature verified properly
                            	// Extract what we need and return it if it's not expired
                            	JWTClaimsSet jcs = sjwt.getJWTClaimsSet();
                            	Date exp = jcs.getDateClaim("exp");
                            	if (System.currentTimeMillis() > exp.getTime()) {
                            		// Expired
                            		CarUtility.locLog("ERR1107", String.valueOf(exp),String.valueOf(new Date(System.currentTimeMillis())));
                            		return null;
                            	}
                            	Date iat = jcs.getDateClaim("iat");
                            	if (System.currentTimeMillis() < (iat.getTime() - 300000)) {
                            		// Issued in future > 5 minutes
                            		CarUtility.locLog("ERR1108", String.valueOf(iat), String.valueOf(new Date(System.currentTimeMillis())));;
                            		return null;
                            	}
                            	String b64 = jcs.getStringClaim("request");
                            	//String b64 = new String(jcs.getStringClaim("request").getBytes("UTF-8"),"UTF-8");
                            	Base64 decoder = new Base64(0,new byte [] { '\n'} );
                            	
                            	//DEBUG
                            	//CarUtility.locError("ERR1134",  LogCriticality.error,"Base64 request = " + b64);
                            	
                            	//String json = new String(WrappedBase64Decoder.DecodeBase64(ba),"ISO-8859-1");
                            	String json = new String(decoder.decode(b64.getBytes()),"UTF-8");
                            	//String json = new String(WrappedBase64Decoder.DecodeBase64(b64.getBytes()),"UTF-8");
                            	//DEBUG
                            	//CarUtility.locError("ERR1134", LogCriticality.error,"Validate: Möibius");
                            	//CarUtility.locError("ERR1134", LogCriticality.error,"Decrypted request = " + json);
                            	
                        		//ObjectMapper mapper = new ObjectMapper();
                            	ObjectMapper mapper = OMSingleton.getInstance().getOm();
                        		InputRequest retval = null;
                        		try {
                        			//WrappedInputRequest w = mapper.readValue(json.getBytes("UTF-8"), WrappedInputRequest.class);
                        			WrappedInputRequest w = mapper.readValue(json.getBytes("UTF-8"), WrappedInputRequest.class);
                        			retval = w.getRequest();
                        			return retval;
                        		} catch (Exception e) {
                        			CarUtility.locError("ERR1104", e.getMessage());
                        			return null;
                        		}
                            } else {
                            	// verification failure
                            	CarUtility.locError("ERR1105");
                            	return null;
                            }
                    } else {
                    	// key retrieval failed
                    	CarUtility.locError("ERR1106");
                    	return null;
                    }
			} else {
				// privkey didn't work
				CarUtility.locError("ERR1102");
				return null;
			}
		} catch (JOSEException x) {
			// for now, re-throw for debugging
			throw new RuntimeException(x);
		}
		catch (Exception e) {
			CarUtility.locError("ERR1103",e.getMessage());
			return null;
		}finally {
			Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
		}
	}
	
	private FullInputRequest cryptoParseFullInput(HttpServletRequest request) {
		
		// Add the BC provider for the crypto type we need
		//Security.addProvider(new BouncyCastleProvider());
		//Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
		Security.insertProviderAt(BouncyCastleProviderSingleton.getInstance(), 1);
		
		// Retrieve the base64 encoded encrypted, signed input object
		
		String b64input = request.getParameter("json");
				
		// Turn it into a JWE
		JWEObject jwe = null;
		try {
			jwe = JWEObject.parse(b64input);
		} catch (Exception e) {
			// On exception, return null
			CarUtility.locError("ERR1101");
			return null;
		}
		
		// Decrypt the JWE using the appropriate key
		// TODO:  For the moment, there is only one key -- we need to make the input include the unencrypted entityId of the 
		// TODO:  requesting RH so that we can determine which of a possible set of keys to use.
		// TODO:  For now, we assume there's only one key across all the RHs (which in the Duke case is currently correct)
		//
		CarConfig config = CarConfig.getInstance();
		
		String myprivfile = config.getProperty("car.privatekeyfile", true);
		String shibcertfile = config.getProperty("car.rhcertfile", true);
		
		try {
			File privinfile = new File(myprivfile);
			byte[] privbytes = new byte[(int) privinfile.length()];
			FileInputStream pfis = new FileInputStream(privinfile);
			pfis.read(privbytes);
			pfis.close();
			// privbytes now has the private key in der format
			PKCS8EncodedKeySpec pspec = new PKCS8EncodedKeySpec(privbytes);
			KeyFactory keyf = KeyFactory.getInstance("RSA");
			PrivateKey privkey = keyf.generatePrivate(pspec);
			// privkey now has the PrivateKey object
			
			if (privkey instanceof RSAPrivateKey) {
                	jwe.decrypt(new RSADecrypter(privkey));
                	SignedJWT sjwt = jwe.getPayload().toSignedJWT();
                    // Use certificate to validate signature
                    PublicKey pbk;
                    FileInputStream in = new FileInputStream(shibcertfile);
                    CertificateFactory cf = CertificateFactory.getInstance("X.509");
                    Certificate cert = cf.generateCertificate(in);

                    pbk = cert.getPublicKey();
                    if (pbk instanceof RSAPublicKey) {
                            JWSVerifier jwsv = new RSASSAVerifier((RSAPublicKey) pbk);
                            if (sjwt.verify(jwsv)) {
                            	// Everything worked -- the signature verified properly
                            	// Extract what we need and return it if it's not expired
                            	JWTClaimsSet jcs = sjwt.getJWTClaimsSet();
                            	Date exp = jcs.getDateClaim("exp");
                            	if (System.currentTimeMillis() > exp.getTime()) {
                            		// Expired
                            		CarUtility.locLog("ERR1107", String.valueOf(exp),String.valueOf(new Date(System.currentTimeMillis())));
                            		return null;
                            	}
                            	Date iat = jcs.getDateClaim("iat");
                            	if (System.currentTimeMillis() < (iat.getTime() - 300000)) {
                            		// Issued in future > 5 minutes
                            		CarUtility.locLog("ERR1108", String.valueOf(iat), String.valueOf(new Date(System.currentTimeMillis())));;
                            		return null;
                            	}
                            	String b64 = jcs.getStringClaim("request");
                            	//String b64 = new String(jcs.getStringClaim("request").getBytes("UTF-8"),"UTF-8");
                            	Base64 decoder = new Base64(0,new byte [] { '\n'} );
                            	
                            	//DEBUG
                            	//CarUtility.locError("ERR1134",  LogCriticality.error,"Base64 request = " + b64);
                            	
                            	//String json = new String(WrappedBase64Decoder.DecodeBase64(ba),"ISO-8859-1");
                            	String json = new String(decoder.decode(b64.getBytes()),"UTF-8");
                            	//String json = new String(WrappedBase64Decoder.DecodeBase64(b64.getBytes()),"UTF-8");
                            	
                            	//DEBUG
                            	//CarUtility.locError("ERR1134", LogCriticality.error,"Validate: Möibius");
                            	//CarUtility.locError("ERR1134", LogCriticality.error,"Decrypted request = " + json);
                            	
                        		//ObjectMapper mapper = new ObjectMapper();
                            	ObjectMapper mapper = OMSingleton.getInstance().getOm();
                        		FullInputRequest retval = null;
                        		try {
                        			//WrappedInputRequest w = mapper.readValue(json.getBytes("UTF-8"), WrappedInputRequest.class);
                        			WrappedFullInputRequest w = mapper.readValue(json.getBytes("UTF-8"), WrappedFullInputRequest.class);
                        			retval = w.getRequest();
                        			return retval;
                        		} catch (Exception e) {
                        			CarUtility.locError("ERR1104", e.getMessage());
                        			return null;
                        		}
                            } else {
                            	// verification failure
                            	CarUtility.locError("ERR1105");
                            	return null;
                            }
                    } else {
                    	// key retrieval failed
                    	CarUtility.locError("ERR1106");
                    	return null;
                    }
			} else {
				// privkey didn't work
				CarUtility.locError("ERR1102");
				return null;
			}
		} catch (JOSEException x) {
			// for now, re-throw for debugging
			throw new RuntimeException(x);
		}
		catch (Exception e) {
			CarUtility.locError("ERR1103",e.getMessage());
			return null;
		}finally {
			Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
		}
	}
			
	private InputRequest parseInput(HttpServletRequest request) {
		// Take the input (POST'd as "json" in this context) and return the 
		// parsed out InputRequest object.
		// For now, we simply consider the "json" POST'd as a base64-encoded
		// JSON representation of the InputRequest object.  Later, that may include
		// signature validation and crypto.
		//
		String b64input = request.getParameter("json");

		if (b64input == null) {
			// if there's no input, we return no result
			System.out.println("No input found in posted data");
			System.out.println("entity is: ");
			return null;
		}
		
		// decode
		String decoded = new String(Base64.decodeBase64(b64input.getBytes()));
		
		//ObjectMapper mapper = new ObjectMapper();
		ObjectMapper mapper = OMSingleton.getInstance().getOm();
		InputRequest retval = null;
		try {
			WrappedInputRequest w = mapper.readValue(decoded, WrappedInputRequest.class);
			retval = w.getRequest();
		} catch (Exception e) {
			// For now we don't care why the exception occurs, we simply care that it didn't work
			// TODO: wire this as a loggable error on input
			System.out.println("Mapper failure: " + e.getMessage());
			return null;
		}
		
		return retval;
	}
	
	private FullInputRequest parseFullInput(HttpServletRequest request) {
		// Take the input (POST'd as "json" in this context) and return the 
		// parsed out FullInputRequest object.
		// For now, we simply consider the "json" POST'd as a base64-encoded
		// JSON representation of the FullInputRequest object.  Later, that may include
		// signature validation and crypto.
		//
		String b64input = request.getParameter("json");

		if (b64input == null) {
			// if there's no input, we return no result
			System.out.println("No input found in posted data");
			System.out.println("entity is: ");
			return null;
		}
		
		// decode
		String decoded = new String(Base64.decodeBase64(b64input.getBytes()));
		
		//ObjectMapper mapper = new ObjectMapper();
		ObjectMapper mapper = OMSingleton.getInstance().getOm();
		FullInputRequest retval = null;
		try {
			WrappedFullInputRequest w = mapper.readValue(decoded, WrappedFullInputRequest.class);
			retval = w.getRequest();
		} catch (Exception e) {
			// For now we don't care why the exception occurs, we simply care that it didn't work
			// TODO: wire this as a loggable error on input
			System.out.println("Mapper failure: " + e.getMessage());
			return null;
		}
		
		return retval;
	}
	
	// evictiimicache?rhid=rhid&iiid=iiid
	@RequestMapping(value="/evictiimicache", method=RequestMethod.GET)
	public ModelAndView evictIIMICache(HttpServletRequest request,@RequestHeader HttpHeaders headers) {
		CarConfig config = CarConfig.getInstance();

		if (! CarUtility.isAuthenticated(request, headers , "", config)) {
			return new ModelAndView("errorPage").addObject("message","Failed").addObject("top_heading",CarUtility.getLocalComponent("top_heading")).addObject("institutional_logo_url",CarUtility.getLocalComponent("institutional_logo_url"));
		}
		String rhid = request.getParameter("rhid");
		String iiid = request.getParameter("iiid");
		InfoItemMetaInformationCache cache = InfoItemMetaInformationCache.getInstance();
		cache.evictCachedInfoItemMetaInformation(rhid,iiid);
		return new ModelAndView("errorPage").addObject("message","Success").addObject("top_heading",CarUtility.getLocalComponent("top_heading")).addObject("institutional_logo_url",CarUtility.getLocalComponent("institutional_logo_url"));
	}
	
	// evictrpmicache?rhid=rhid&rpid=rpid
	@RequestMapping(value="/evictrpmicache",method=RequestMethod.GET)
	public ModelAndView evictRPMICache(HttpServletRequest request, @RequestHeader HttpHeaders headers) {
		CarConfig config = CarConfig.getInstance();
		if (! CarUtility.isAuthenticated(request,  headers, "", config)) {
			return new ModelAndView("errorPage").addObject("message","Failed").addObject("top_heading",CarUtility.getLocalComponent("top_heading")).addObject("institutional_logo_url",CarUtility.getLocalComponent("institutional_logo_url"));
		}
		String rhid = request.getParameter("rhid");
		String rpid = request.getParameter("rpid");
		String rptype = request.getParameter("rptype");
		RPMetaInformationCache cache = RPMetaInformationCache.getInstance();
		if (rptype != null)
			cache.evictCachedRPMetaInformation(rhid,rptype,rpid);
		else
			cache.evictCachedRPMetaInformation(rhid, rpid);
		return new ModelAndView("errorPage").addObject("message","Success").addObject("top_heading",CarUtility.getLocalComponent("top_heading")).addObject("institutional_logo_url",CarUtility.getLocalComponent("institutional_logo_url"));
	}
	
	// evictvaluemicache?iiid=iiid&iivalue=iivalue
	@RequestMapping(value="/evictvaluemicache",method=RequestMethod.GET)
	public ModelAndView evictValueMICache(HttpServletRequest request, @RequestHeader HttpHeaders headers) {
		CarConfig config = CarConfig.getInstance();
		if (! CarUtility.isAuthenticated(request,  headers,  "",  config)) {
			return new ModelAndView("errorPage").addObject("message","Failed").addObject("top_heading",CarUtility.getLocalComponent("top_heading")).addObject("institutional_logo_url",CarUtility.getLocalComponent("institutional_logo_url"));
		}
		String iiid = request.getParameter("iiid");
		String iivalue = request.getParameter("iivalue");
		ValueMetaInformationCache cache = ValueMetaInformationCache.getInstance();
		cache.evictCachedValueMetaInformation(iiid, iivalue);
		return new ModelAndView("errorPage").addObject("message","Success").addObject("top_heading",CarUtility.getLocalComponent("top_heading")).addObject("institutional_logo_url",CarUtility.getLocalComponent("institutional_logo_url"));
	}
	
	@RequestMapping(value="/dumpiicache", method=RequestMethod.GET)
	public ModelAndView returnIIMICacheData(HttpServletRequest request) {
		ModelAndView retval = new ModelAndView("errorPage");
		StringBuilder sb = new StringBuilder();

		InfoItemMetaInformationCache iimicache = InfoItemMetaInformationCache.getInstance();
		Iterator<Entry<String,CachedInfoItemMetaInformation>> iter = iimicache.getCache().iterator();
		
		while (iter.hasNext()) {
			Entry<String,CachedInfoItemMetaInformation> e = iter.next();
			CachedInfoItemMetaInformation v = e.getValue();
			sb.append(e.getKey());
			
			if (v != null && v.getData() != null) {
				sb.append("=="+v.getData().getDisplayname());
				sb.append("<br>");
			} else {
				sb.append("==NULL");
				sb.append("<br>");
			}
		}
		retval.addObject("message",sb.toString());
		retval.addObject("top_heading",CarUtility.getLocalComponent("top_heading"));
		retval.addObject("institutional_logo_url",CarUtility.getLocalComponent("institutional_logo_url"));
		return retval;
	}
	@RequestMapping(value="/dumprpmicache", method=RequestMethod.GET)
	public ModelAndView returnRPMICacheData(HttpServletRequest request) {
		ModelAndView retval = new ModelAndView("errorPage");
		StringBuilder sb = new StringBuilder();
		
		RPMetaInformationCache rpmic = RPMetaInformationCache.getInstance();
		Iterator<Entry<String,CachedRPMetaInformation>> iter = rpmic.getCache().iterator();
		
		while (iter.hasNext()) {
			Entry<String, CachedRPMetaInformation> e = iter.next();
			CachedRPMetaInformation v = e.getValue();
			sb.append(e.getKey());
			
			if (v != null && v.getData() != null) {
				sb.append("==" + v.getData().getDisplayname());
				sb.append("<br>");
			} else {
				sb.append("==NULL");
				sb.append("<br>");
			}
		}
		retval.addObject("message",sb.toString());
		retval.addObject("top_heading",CarUtility.getLocalComponent("top_heading"));
		retval.addObject("institutional_logo_url",CarUtility.getLocalComponent("institutional_logo_url"));
		return retval;
	}

	@RequestMapping(value="/cachestatus", method=RequestMethod.GET)
	public ModelAndView returnCacheStatus(HttpServletRequest request) {
		ModelAndView retval = new ModelAndView("errorPage");
		StringBuilder sb = new StringBuilder();
		sb.append("Scrubber status: " + CacheScrubber.getStatus() + "<br>");
		sb.append("RP cache size: " + CacheScrubber.getLastrpsize() + " entries<br>");
		sb.append("II cache size: " + CacheScrubber.getLastiisize() + " entries<br>");
		sb.append("Value cache size: " + CacheScrubber.getLastvalsize() + " entries<br>");
		sb.append("Last cycle length: " + CacheScrubber.getLastrunduration() + " milliseconds<br>");
		sb.append("Max cycle length (since restart): " + CacheScrubber.getMaxduration() + " milliseconds<br>");
		sb.append("Number of cache cycles since restart: " + CacheScrubber.getRuncount());
		
		retval.addObject("message",sb.toString());
		retval.addObject("top_heading",CarUtility.getLocalComponent("top_heading"));
		retval.addObject("institutional_logo_url",CarUtility.getLocalComponent("institutional_logo_url"));
		return retval;
	}
	
	@RequestMapping(value="/reflex", method=RequestMethod.GET) 
	public ModelAndView returnReflex(HttpServletRequest request) {
		ModelAndView retval = new ModelAndView("errorPage");
		retval.addObject("message","GET request not supported for /reflex endpoint");
		retval.addObject("top_heading",CarUtility.getLocalComponent("top_heading"));
		retval.addObject("institutional_logo_url",CarUtility.getLocalComponent("institutional_logo_url"));
		return retval;
	}
	
	@RequestMapping(value="/reflex4",method=RequestMethod.POST)
	public ModelAndView performReflex4(HttpServletRequest request) {
		return handleFullDecisionRequest(request,true); // signing and encryption turned on here
	}
	
	@RequestMapping(value="/reflex3",method=RequestMethod.POST)
	public ModelAndView performReflex3(HttpServletRequest request) {
		return handleFullDecisionRequest(request,false);
	}
	
	@RequestMapping(value="/reflex2", method=RequestMethod.POST)
	public ModelAndView performReflex2(HttpServletRequest request) {
		return handleFilterAndDecide(request,true);  // signing and encryption turned on here
	}
	
	@RequestMapping(value="/reflex", method=RequestMethod.POST)
	public ModelAndView performReflex(HttpServletRequest request) {
		return (handleFilterAndDecide(request, false));  
	}
	
	// No GET.  Only POST.  No GET.
	@RequestMapping(value="/filteranddecide", method=RequestMethod.GET)
	public ModelAndView returnFilterAndDecide(HttpServletRequest request) {
		ModelAndView retval = new ModelAndView("errorPage");
		CarUtility.locLog("ERR0066","/reflex endpoint");
		retval.addObject("message",CarUtility.getLocalError("ERR0066","/reflex endpoint"));
		retval.addObject("intercept_view","1");
		retval.addObject("top_heading",CarUtility.getLocalComponent("top_heading"));
		retval.addObject("institutional_logo_url",CarUtility.getLocalComponent("institutional_logo_url"));
		return retval;
	}
	
	// POST handler is where all the action happens
	// For now, this is monolithic -- may change to separate methods to handle first and second POST but for now, only one
	
	@RequestMapping(value="/filteranddecide", method=RequestMethod.POST, produces="text/html;charset=utf-8")
	public ModelAndView handleFilterAndDecide2(HttpServletRequest request) {
		return handleFilterAndDecide(request, false);  // default behavior for /filteranddecide and /reflex
	}
	
	@RequestMapping(value="/cryptofilteranddecide", method=RequestMethod.POST, produces="text/html;charset=utf-8")
	public ModelAndView cryptoFilterAndDecide(HttpServletRequest request) {
		return handleFilterAndDecide(request, true); // default behavior for /cryptofilteranddecide and /reflex2
	}
	
	public ModelAndView handleFilterAndDecide(HttpServletRequest request, boolean useCrypto) {
		
		long curtime = System.currentTimeMillis();  // for profiling
		
		//RGC Let's try using a single httpClient for each request instead of 
		// generating them on every call.
		HttpClient httpClient = null;
		try {
			httpClient = CarHttpClientFactory.getHttpsClient();
		} catch (Exception e) {
			// Log and create a raw client instead
			CarUtility.locDebug("ERR1136","Falling back to default HttpClient d/t failed client initialization");
			httpClient = HttpClientBuilder.create().build();
		}
		// End let's try
		
		ModelAndView retval = null;  // populate later
		boolean succ = true;  // tourist information -- we mark writeback as successful if writeback isn't needed
		int convo = 0;
		String sconvo = null;
		boolean askUserForDecisions = false;
		
		//
		// Start by forcing the input into UTF-8
		// Should not matter, but...
		//
		
		try {
			request.setCharacterEncoding("UTF-8");
		} catch (Exception e) {
			CarUtility.locError("ERR1134","Failed to set UTF-8 character encoding on input");
		}
		
		CarUtility.locDebug("ERR1166","CAR data inits took " + (System.currentTimeMillis() - curtime) + "ms");
		curtime = System.currentTimeMillis();
		
		// Perform the necessary initialization
		
		CarConfig config = CarUtility.init(request);
		
		CarUtility.locDebug("ERR1166","CAR Config initialization took " + (System.currentTimeMillis() - curtime) + "ms");
		curtime = System.currentTimeMillis();
		
		// Marshal the preferred language for interpolating internationalized strings
		String preflang = CarUtility.prefLang(request);
		
		// prepare a typemap for later use
		HashMap<String,String> typemap = new HashMap<String,String>();
		
		// prepare a map of display names for contained IIs (for use later in oauth_scope processing)
		HashMap<String,String> oadn = new HashMap<String,String>();
		// and a map of descriptions to use in case we need them
		HashMap<String,String> oadescr = new HashMap<String,String>();

		CarUtility.locDebug("ERR1166","HashMap creation took " + (System.currentTimeMillis() - curtime) + "ms");
		curtime = System.currentTimeMillis();
		
		// We have two cases, determined by the presence or absence of data in the Session.
		// Get a session (creating a new one if needed)
		
		HttpSession session = request.getSession(true);
		
		CarUtility.locDebug("ERR1166","Establishing Session took " + (System.currentTimeMillis() - curtime) + "ms");
		curtime = System.currentTimeMillis();
		
		if (session == null) {
			CarUtility.locError("ERR0067");
			retval = new ModelAndView("errorPage");
			retval.addObject("message",CarUtility.getLocalError("ERR0067"));
			retval.addObject("intercept_view","1");
			retval.addObject("top_heading",CarUtility.getLocalComponent("top_heading"));
			retval.addObject("institutional_logo_url",CarUtility.getLocalComponent("institutional_logo_url"));
			return retval;
		} 
		session.setMaxInactiveInterval(600);  // 10 minute window for sessions
		CarUtility.locDebug("ERR1166","Setting max interactive interval took " + (System.currentTimeMillis() - curtime));
		curtime = System.currentTimeMillis();
		
		// Determine if there's an established conversation in the input data
		if (request.getParameter("conversation") != null) {
			convo = Integer.parseInt(request.getParameter("conversation"));
			CarUtility.locDebug("ERR1166","Pulling existing converation number took " + (System.currentTimeMillis() - curtime) + "ms");
			curtime = System.currentTimeMillis();
		} else {
			CarUtility.locDebug("ERR1166","Checking conversation took " + (System.currentTimeMillis() - curtime) + "ms");
			curtime = System.currentTimeMillis();
			
			// Determine what the next available conversation number in the session is
			if (session.getAttribute("maxconv") != null) {
				CarUtility.locDebug("ERR1166", "Pulling maxconv took " + (System.currentTimeMillis() - curtime) + "ms");
				curtime = System.currentTimeMillis();
				
				// bump it and add back
				convo = Integer.parseInt((String) session.getAttribute("maxconv")) + 1;
				CarUtility.locDebug("ERR1166","Bumping maxconv took " + (System.currentTimeMillis() - curtime) + "ms");
				curtime = System.currentTimeMillis();
				
				session.setAttribute("maxconv",String.valueOf(convo));
				CarUtility.locDebug("ERR1166","Writing maxconv to session attribut took " + (System.currentTimeMillis() - curtime) + "ms");
				curtime = System.currentTimeMillis();
			} else {
				// no maxconv value -- start from 0 and set it forward
				convo = 0;
				session.setAttribute("maxconv", String.valueOf(convo));
				CarUtility.locDebug("ERR1166","Setting maxconv to 0 took " + (System.currentTimeMillis() - curtime) + "ms");
				curtime = System.currentTimeMillis();
			}
		}

		// now we have convo set, snatch it as a String
		sconvo = String.valueOf(convo);
		
		CarUtility.locDebug("ERR1166","Setting conversation number took " + (System.currentTimeMillis() - curtime) + "ms");
		curtime = System.currentTimeMillis();
		
		// Session initialization done
		CarUtility.locDebug("ERR1166", "Remaining CAR decide initialization took " + (System.currentTimeMillis() - curtime) +"ms");
		curtime = System.currentTimeMillis();
		
		if (session.getAttribute(sconvo + ":" + "returntourl") == null || session.getAttribute(sconvo + ":" + "icmdecision") == null) {
			// This is the initial case where the input is a POSTd json object from the RH
			// Proceed on that assumption
			// 
			// If we are using crypto, we must first decrypt the input and verify its signature, which is done
			// using a different parser
			InputRequest inputRequest = null;
			if (useCrypto) {
				inputRequest = cryptoParseInput(request);
			} else {
				inputRequest = parseInput(request);
			}
			// input parse
			CarUtility.locDebug("ERR1166","Parsing Input took" + (System.currentTimeMillis() - curtime) + "ms");
			curtime = System.currentTimeMillis();
			
			if (inputRequest == null) {
				// failed input processing
				CarUtility.locLog("ERR0068");
				ModelAndView r = new ModelAndView("errorPage");
				r.addObject("message",CarUtility.getLocalError("ERR0068"));
				r.addObject("intercept_view","1");
				r.addObject("top_heading",CarUtility.getLocalComponent("top_heading"));
				r.addObject("institutional_logo_url",CarUtility.getLocalComponent("institutional_logo_url"));
				return r;
			} //else {
				// RGC debug
				//try {
				//	CarUtility.locError("ERR1134", LogCriticality.error, "Parsed request attributes[1].values[0] is: " + new String(inputRequest.getAttributes().get(1).getValues().get(0).getBytes("UTF-16"),"ISO-8859-1"));
				//	CarUtility.locError("ERR1134", LogCriticality.error,"Parsed Input Request: " + inputRequest.toJson());
				//} catch (Exception x) {
				//	CarUtility.locError("ERR1134",  LogCriticality.error,"Unable to serialize parsed input request!");
				//}
			//}
			// We have input -- early-bind some things to the session so we can track our future state
			// Callback Url
			String returntourl = inputRequest.getHeader().getCallbackUrl();
			session.setAttribute(sconvo + ":" + "returntourl",returntourl);
			// RH identifier
			String rhid = inputRequest.getHeader().getRhId();
			session.setAttribute(sconvo + ":" + "rhid",rhid);
			
			// RP identifier
			String rpid = inputRequest.getHeader().getRelyingPartyId();
			session.setAttribute(sconvo + ":" + "rpid",rpid);
			
			// Compute the user identifier based on attribute value pairings and header
			String userattr = inputRequest.getHeader().getIdentifierName();
			String uservalue = null;
			HashMap<String,List<String>> inputAttrMap = new HashMap<String,List<String>>();
			
			for (AttributeValuelist avl : inputRequest.getAttributes()) {
				// For ease of filtering, we here build a map on attribute name from the input request
				inputAttrMap.put(avl.getAttrname(),avl.getValues());
				if (avl.getAttrname().equals(userattr)) {
					uservalue = avl.getValues().get(0);  // must be only one, else just the first
				}
			}
			if (uservalue == null) {
				CarUtility.locError("ERR0069",LogCriticality.info);
				ModelAndView r = new ModelAndView("errorPage");
				r.addObject("message",CarUtility.getLocalError("ERR0069"));
				r.addObject("intercept_view","1");
				r.addObject("top_heading",CarUtility.getLocalComponent("top_heading"));
				r.addObject("institutional_logo_url",CarUtility.getLocalComponent("institutional_logo_url"));
				return r;
			}
			
			// And store the user value in the session
			session.setAttribute(sconvo + ":" + "username", uservalue);
			session.setAttribute(sconvo + ":" + "usertype", userattr);
			// Now that we have the request information marshalled
			
			CarUtility.locDebug("ERR1166", "Request marshalling took " + (System.currentTimeMillis() - curtime) + "ms");
			curtime = System.currentTimeMillis();

			// Retrieve RP metainformation and parse out the RP attribute information
			
			ReturnedRPMetaInformation rpmetainformation = CarUtility.getRPMetaInformation(rhid, rpid, config, httpClient);
			// Handle unrecognized RP
			boolean unrecRP = false;
			if (rpmetainformation == null || rpmetainformation.getRpproperties() == null) {
				// unrecognized RP
				if ("true".equals(config.getProperty("car.accept_unregistered_rp", false))) {
					rpmetainformation = new ReturnedRPMetaInformation();
					unrecRP = true;
					
					rpmetainformation.setDefaultshowagain("true");
					rpmetainformation.setDescription(null);
					rpmetainformation.setDisplayname(null);
					rpmetainformation.setIconurl(null);
					rpmetainformation.setPrivacyurl(null);
					RHIdentifier rhi = new RHIdentifier();
					rhi.setRhid(rhid);
					rhi.setRhtype("entityId");
					rpmetainformation.setRhidentifier(rhi);
					RPIdentifier rpi = new RPIdentifier();
					rpi.setRpid(rpid);
					rpi.setRptype("entityId");
					rpmetainformation.setRpidentifier(rpi);
					ReturnedRPProperty rpp = new ReturnedRPProperty();
					rpp.setRppropertyname("entityId");
					rpp.setRppropertyvalue(rpid);
					ArrayList<ReturnedRPProperty> arp = new ArrayList<ReturnedRPProperty>();
					arp.add(rpp);
					rpmetainformation.setRpproperties(arp);
					CarUtility.locLog("ERR0806", rpid);
				} else {
					CarUtility.locLog("ERR0806", rpid);
					ModelAndView r = new ModelAndView("errorPage");
					r.addObject("message",CarUtility.getLocalError("ERR0806",rpid));
					r.addObject("page-title","Unrecognized RP");
					r.addObject("intercept_view","1");
					r.addObject("transient","true");
					r.addObject("top_heading",CarUtility.getLocalComponent("top_heading"));
					r.addObject("institutional_logo_url",CarUtility.getLocalComponent("institutional_logo_url"));
					return r;
				}
			}
			ReturnedRPProperty rrrpp = new ReturnedRPProperty();
			
			rrrpp.setRppropertyname("entityId");
			rrrpp.setRppropertyvalue(rpid);
			
			rpmetainformation.getRpproperties().add(rrrpp);  // add the entity ID as entityId property
			// Debug
			//try {
				//ObjectMapper m2 = new ObjectMapper();
				//ObjectMapper m2 = OMSingleton.getInstance().getOm();
				//CarUtility.locError("ERR0083",LogCriticality.debug, m2.writeValueAsString(rpmetainformation));
			//} catch (Exception e) {
				// ignore
			//}
			CarUtility.locDebug("ERR1166","RP property retrieval took " + (System.currentTimeMillis() - curtime) + "ms");
			curtime = System.currentTimeMillis();
			
			// Retrieve the required and optional attribute list information as well
			HashMap<String,String> areason = new HashMap<String,String>();
			
			ReturnedRPRequiredInfoItemList rprequirediilist = CarUtility.getRPRequiredIIList(rhid, rpid, config);
/*			if (rprequirediilist==null) {
				CarUtility.locError("ERR0807",rpid);
				ModelAndView r = new ModelAndView("errorPage");
				r.addObject("message",CarUtility.getLocalError("ERR0807",rpid));
				r.addObject("page-title","Improperly registered RP");
				r.addObject("intercept_view","1");
				return r;
			} */
			ReturnedRPOptionalInfoItemList rpoptionaliilist = CarUtility.getRPOptionalIIList(rhid, rpid, config);
			/* RGC - 11-26-2018 - no longer fatal error to lack any iis for release */
/*			if (rpoptionaliilist == null && rprequirediilist == null) {
				CarUtility.locError("ERR0807",rpid);
				ModelAndView r = new ModelAndView("errorPage");
				r.addObject("message",CarUtility.getLocalError("ERR0807",rpid));
				r.addObject("page-title","Improperly registered RP");
				r.addObject("intercept_view","1");
				return r;
			} */
			if (rprequirediilist == null) {
				rprequirediilist = new ReturnedRPRequiredInfoItemList();
				rprequirediilist.setRequiredlist(new ArrayList<InfoItemValueList>());
				// In the event of an unrecognized RP, use the inputAttrMap keys instead -- RGC
				// This is an approximation.
				if (unrecRP) {
					Set<String> ss = inputAttrMap.keySet();
					CarUtility.locLog("ERR1134","Forging " + ss.size() + " attributes");
					for (String s : ss) {
						InfoItemValueList il = new InfoItemValueList();
						InfoItemIdentifier ii = new InfoItemIdentifier();
						ii.setIiid(s);
						ii.setIitype("attribute"); // force these to be attributes
						il.setInfoitemidentifier(ii);
						ArrayList<String> v = new ArrayList<String>();
						v.add("^.*$");
						il.setValuelist(v);
						il.setSourceitemname(s);
						rprequirediilist.getRequiredlist().add(il);
					}
				}
			}
			if (rpoptionaliilist == null) {
				rpoptionaliilist = new ReturnedRPOptionalInfoItemList();
				rpoptionaliilist.setOptionallist(new ArrayList<InfoItemValueList>());
			}
			
			CarUtility.locDebug("ERR1166","Retrieving attribute lists took " + (System.currentTimeMillis() - curtime) + "ms");
			curtime = System.currentTimeMillis();
			
			// We now need to construct a master attribute/valueset list from the passed-in data and the 
			// required/optional lists that we've obtained.
			// Two considerations are important and non-obvious:
			//    (1) We need to gang values together regardless of whether they are required or optional
			//        -- at this point that isn't relevant
			//    (2) We need to map any source attribute translations along the way, so as to generate 
			//        a desired entry for any attribute that appears in the required or optional list
			//
			
			ArrayList<AttributeValuelist> desiredAttributes = new ArrayList<AttributeValuelist>();
			ArrayList<String> hasvalues = new ArrayList<String>();
			
			
			for (InfoItemValueList iivl : rprequirediilist.getRequiredlist()) {
				InfoItemIdentifier iii = iivl.getInfoitemidentifier();  // release identifier
				String sourceAttribute = iivl.getSourceitemname();
				// Set up the reason string
				if (iivl != null && iivl.getInfoitemidentifier() != null && iivl.getReason() != null) {
					areason.put(iivl.getInfoitemidentifier().getIiid(), CarUtility.localize(iivl.getReason(),preflang));
				} else {
					areason.put(iivl.getInfoitemidentifier().getIiid(),"");  // default to no reason
				}

				/* Deprecated
				if (iivl != null && iivl.getInfoitemidentifier() != null && iivl.getReason() != null && iivl.getReason().getLocales() != null && ! iivl.getReason().getLocales().isEmpty()) {
					areason.put(iivl.getInfoitemidentifier().getIiid(),iivl.getReason().getLocales().get(0).getValue());
				} else {
					areason.put(iivl.getInfoitemidentifier().getIiid(), "");  // default to no reason
				}
				*/
				
				ArrayList<String> mappedValues = new ArrayList<String>();
				if (sourceAttribute != iii.getIiid()) {
					// This is a mapping case -- map the value list from the other attribute
					// TODO:  This is expensive since we don't store a hash -- consider refactoring this later
					// TODO:  I think this may actually be incorrect, as the othervl source probably needs to 
					// iterate over the attributes in input not the required list (or optional list) but for now
					// we'll go with it and verify later -- this may need major rework in a bit.
					
					for (InfoItemValueList othervl : rprequirediilist.getRequiredlist()) {
						if (othervl.getInfoitemidentifier().getIiid().equals(sourceAttribute)) {
							// found it
							// List<String> subset = CarUtility.subsetValueList(othervl.getValuelist(), inputAttrMap.get(othervl.getInfoitemidentifier().getIiid()));
							List<String> subset = CarUtility.subsetValueList(inputAttrMap.get(iivl.getInfoitemidentifier().getIiid()),othervl.getValuelist());
							if (subset != null && ! subset.isEmpty()) {
								mappedValues.addAll(subset);
							}
							break;
						}
					}
					if (mappedValues.isEmpty() && iii.getIitype().equals("oauth_scope")) {
						// mappedValues.add(iii.getIiid() + "_scope");  // placeholder (RGC)
						//
						// Now we make the value map contain the names of the attributes in the scope
						//
						// TODO:  We need to refactor InputRequestHeader to support passing in rhtype as well as rhid and rptype as well as rpid
						// For now we hard-code "entityId"
						RHIdentifier rhi = new RHIdentifier();
						rhi.setRhid(rhid);
						rhi.setRhtype("entityId");
						ScopeMapping sm = CarUtility.getScopeMapping(rhi, iii, config);
						//
						// We use the list of attributes contained as the value *unless*
						// this is a scope without contained attributes, in which case we use 
						// the placeholder value.
						if (sm != null && sm.getInfoitems() != null && ! sm.getInfoitems().isEmpty()) {
							mappedValues.addAll(sm.getInfoitems());
						} else {
							mappedValues.add(iii.getIiid() + "_scope");
						}
						// And populate the oadescr hash
						ReturnedInfoItemMetaInformation riimi = CarUtility.getInfoItemMetaInformation(rhid,"oauth_scope",iii.getIiid(),config,httpClient);
						if (riimi != null && riimi.getDescription() != null) {
							oadescr.put(iii.getIiid(),CarUtility.localize(riimi.getDescription(), preflang));
						} else {
							oadescr.put(iii.getIiid(),iii.getIiid());
						}
					}
				} else {
					// The simple case
					// List<String> subset = CarUtility.subsetValueList(iivl.getValuelist(), inputAttrMap.get(iivl.getInfoitemidentifier().getIiid()));
					
					List<String> subset = CarUtility.subsetValueList(inputAttrMap.get(iivl.getInfoitemidentifier().getIiid()),iivl.getValuelist());
					if (subset != null && ! subset.isEmpty()) {
						mappedValues.addAll(subset);
					} else if (iii.getIitype().equals("oauth_scope")) {
						// mappedValues.add(iii.getIiid()+"_scope"); // placeholder (RGC)
						// Value map contains the list of attributes contained
						// TODO:  Same:  Need InputRequestHeader to contain types as well as IDs
						// for now, entityId hard-coded
						
						RHIdentifier rhi = new RHIdentifier();
						rhi.setRhid(rhid);
						rhi.setRhtype("entityId");
						ScopeMapping sm = CarUtility.getScopeMapping(rhi, iii,config);
						
						if (sm != null && sm.getInfoitems() != null && ! sm.getInfoitems().isEmpty()) {
							mappedValues.addAll(sm.getInfoitems());
						} else {
							mappedValues.add(iii.getIiid() + "_scope");
						}
						// And populate the oadescr hash
						ReturnedInfoItemMetaInformation riimi = CarUtility.getInfoItemMetaInformation(rhid,"oauth_scope",iii.getIiid(),config,httpClient);
						if (riimi != null && riimi.getDescription() != null) {
							oadescr.put(iii.getIiid(),CarUtility.localize(riimi.getDescription(), preflang));
						} else {
							oadescr.put(iii.getIiid(),iii.getIiid());
						}

					}
				}
				
				AttributeValuelist al = new AttributeValuelist();
				al.setAttrname(iii.getIiid());
				al.setValues(mappedValues);
				if ((al.getValues() != null && ! al.getValues().isEmpty()) || iii.getIitype().equals("oauth_scope")) {
					desiredAttributes.add(al);			// only if we have values to release
					typemap.put(iii.getIiid(),iii.getIitype());
					//CarUtility.locError("ERR1134", LogCriticality.error,"Adding: " + iii.getIiid() + " = " + mappedValues + " to desired list with type " + iii.getIitype());
				}
				hasvalues.add(iii.getIiid()); // for tracking
			}
			
			CarUtility.locDebug("ERR1166","Converting required to IVL took " + (System.currentTimeMillis() - curtime) + "ms");
			curtime = System.currentTimeMillis();
			
			// And the somewhat more complicated add of the optional cases
			
			for (InfoItemValueList iivl : rpoptionaliilist.getOptionallist()) {
				InfoItemIdentifier iii = iivl.getInfoitemidentifier();  // release identifier
				String sourceAttribute = iivl.getSourceitemname();
				// Set up the reason string
				if (iivl != null && iivl.getInfoitemidentifier() != null && iivl.getReason() != null) {
					areason.put(iivl.getInfoitemidentifier().getIiid(), CarUtility.localize(iivl.getReason(),preflang));
				} else {
					areason.put(iivl.getInfoitemidentifier().getIiid(),"");  // default to no reason
				}
				
				/* Deprecated
				if (iivl != null && iivl.getInfoitemidentifier() != null && iivl.getReason() != null && iivl.getReason().getLocales() != null && ! iivl.getReason().getLocales().isEmpty()) {
					areason.put(iivl.getInfoitemidentifier().getIiid(),iivl.getReason().getLocales().get(0).getValue());
				} else {
					areason.put(iivl.getInfoitemidentifier().getIiid(), "");  // default to no reason
				}
				*/
				
				ArrayList<String> mappedValues = new ArrayList<String>();
				if (sourceAttribute != iii.getIiid()) {
					// This is a mapping case -- map the value list from the other attribute
					// TODO:  This is expensive since we don't store a hash -- consider refactoring this later
					// TODO:  I think this may actually be incorrect, as the othervl source probably needs to 
					// iterate over the attributes in input not the required list (or optional list) but for now
					// we'll go with it and verify later -- this may need major rework in a bit.
					
					for (InfoItemValueList othervl : rpoptionaliilist.getOptionallist()) {
						if (othervl.getInfoitemidentifier().getIiid().equals(sourceAttribute)) {
							// found it
							// List<String> subset = CarUtility.subsetValueList(othervl.getValuelist(), inputAttrMap.get(othervl.getInfoitemidentifier().getIiid()));
							List<String> subset = CarUtility.subsetValueList(inputAttrMap.get(othervl.getInfoitemidentifier().getIiid()),othervl.getValuelist());
							if (subset != null && ! subset.isEmpty()) {
								mappedValues.addAll(subset);
							} else if (iii.getIitype().equals("oauth_scope")) {
								// mappedValues.add(iii.getIiid()+"_scope"); // placeholder (RGC)
								// Value map contains the list of attributes contained
								// TODO:  Same:  Need InputRequestHeader to contain types as well as IDs
								// for now, entityId hard-coded
								
								RHIdentifier rhi = new RHIdentifier();
								rhi.setRhid(rhid);
								rhi.setRhtype("entityId");
								ScopeMapping sm = CarUtility.getScopeMapping(rhi, iii,config);
								
								if (sm != null && sm.getInfoitems() != null && ! sm.getInfoitems().isEmpty()) {
									mappedValues.addAll(sm.getInfoitems());
								} else {
									mappedValues.add(iii.getIiid() + "_scope");
								}
								// And populate the oadescr hash
								ReturnedInfoItemMetaInformation riimi = CarUtility.getInfoItemMetaInformation(rhid,"oauth_scope",iii.getIiid(),config,httpClient);
								if (riimi != null && riimi.getDescription() != null) {
									oadescr.put(iii.getIiid(),CarUtility.localize(riimi.getDescription(), preflang));
								} else {
									oadescr.put(iii.getIiid(),iii.getIiid());
								}

							}
							break;
						}
					}
				} else {
					// The simple case
					// List<String> subset = CarUtility.subsetValueList(iivl.getValuelist(),inputAttrMap.get(iivl.getInfoitemidentifier().getIiid()));
					List<String> subset = CarUtility.subsetValueList(inputAttrMap.get(iivl.getInfoitemidentifier().getIiid()),iivl.getValuelist());
					if (subset != null && ! subset.isEmpty()) {
						mappedValues.addAll(subset);
					} else if (iii.getIitype().equals("oauth_scope")) {
						// mappedValues.add(iii.getIiid()+"_scope"); // placeholder (RGC)
						// Value map contains the list of attributes contained
						// TODO:  Same:  Need InputRequestHeader to contain types as well as IDs
						// for now, entityId hard-coded
						
						RHIdentifier rhi = new RHIdentifier();
						rhi.setRhid(rhid);
						rhi.setRhtype("entityId");
						ScopeMapping sm = CarUtility.getScopeMapping(rhi, iii,config);
						
						if (sm != null && sm.getInfoitems() != null && ! sm.getInfoitems().isEmpty()) {
							mappedValues.addAll(sm.getInfoitems());
						} else {
							mappedValues.add(iii.getIiid() + "_scope");
						}
						// And populate the oadescr hash
						ReturnedInfoItemMetaInformation riimi = CarUtility.getInfoItemMetaInformation(rhid,"oauth_scope",iii.getIiid(),config,httpClient);
						if (riimi != null && riimi.getDescription() != null) {
							oadescr.put(iii.getIiid(),CarUtility.localize(riimi.getDescription(), preflang));
						} else {
							oadescr.put(iii.getIiid(),iii.getIiid());
						}

					}
				}
				
				// If the attribute is already present, we need to merge valuelists, otherwise, just 
				// add as is.
				if (hasvalues.contains(iii.getIiid())) {
					// hard case -- merge
					// TODO:  Also vastly inefficient -- hash this later
					//
					for (AttributeValuelist orig : desiredAttributes) {
						if (orig.getAttrname().equals(iii.getIiid())) {
							// this is the merge target
							desiredAttributes.remove(orig);  // remove it first
							List<String> preval = orig.getValues();
							preval.addAll(mappedValues);
							orig.setValues((ArrayList<String>)preval);
							if (orig.getValues() != null && ! orig.getValues().isEmpty()) 
								desiredAttributes.add(orig);    // only if values
							break;
						}
					}
				} else {
					// this is the simpler case				
					AttributeValuelist al = new AttributeValuelist();
					al.setAttrname(iii.getIiid());
					al.setValues(mappedValues);
					if ((al.getValues() != null && ! al.getValues().isEmpty()) || iii.getIitype().equals("oauth_scope")) {
						desiredAttributes.add(al);   // only if values
						typemap.put(iii.getIiid(),iii.getIitype());
						// CarUtility.locError("ERR1134", LogCriticality.error,"Simple adding " + iii.getIiid() + " to desired");
					}
				}
			}
			
			try {
				//ObjectMapper m = new ObjectMapper();
				ObjectMapper m = OMSingleton.getInstance().getOm();
				//CarUtility.locError("ERR0078",LogCriticality.debug, m.writeValueAsString(desiredAttributes));
				if ("true".equalsIgnoreCase(config.getProperty("logSensitiveInfo", false)))
					CarUtility.locDebug("ERR0078", new String(m.writeValueAsBytes(desiredAttributes),StandardCharset.UTF_8));
			} catch (Exception ign) {
				// ignore
			}
			
			CarUtility.locDebug("ERR1166","Merging optional attrs took " + (System.currentTimeMillis() - curtime) + "ms");
			curtime = System.currentTimeMillis();
			
			// Now we have the desired attribute/valueset filtered to current values list in desiredAttributes.
			// Values are projected from the proper source attributes, and the proper targets are listed.
			// We need to construct the ICM request from the data we have lying around now.
			// TODO:  This too is inefficient -- whole data flow needs to be redesigned at some point for
			// better efficiency given how it has to play out.
			
			DecisionRequestObject dro = new DecisionRequestObject();
			
			// set userId value
			UserId userid = new UserId();
			userid.setUserType(userattr);
			userid.setUserValue(uservalue);
			dro.setUserId(userid);
			
			// set relying party Id
			// TODO:  Fixed at entityID identifiers for now...make this mutable later
			
			RelyingPartyId relpar = new RelyingPartyId();
			relpar.setRPtype("entityId");
			relpar.setRPvalue(rpid);
			dro.setRelyingPartyId(relpar);
			
			// Ditto the resource holder
			ResourceHolderId reshol = new ResourceHolderId();
			reshol.setRHType("entityId");
			reshol.setRHValue(rhid);
			dro.setResourceHolderId(reshol);
			
			// Build out the list of infoidplusvalues to match the attributevaluelist we have
			
			ArrayList<InfoIdPlusValues> aiipv = new ArrayList<InfoIdPlusValues>();
			for (AttributeValuelist av : desiredAttributes) {
				// for every desired attribute with values...
				InfoIdPlusValues iipv = new InfoIdPlusValues();
				InfoId ii = new InfoId();
				// TODO:  hard-coding type as "attribute" now, but this needs to change later
				// TODO: somewhat better using input typemap to glean original type...
				//ii.setInfoType("attribute");
				if (typemap.containsKey(av.getAttrname())) {
					ii.setInfoType(typemap.get(av.getAttrname()));
					//CarUtility.locError("ERR1134", LogCriticality.error,"(1) " + av.getAttrname() + " is " + typemap.get(av.getAttrname()));
				} else {
					ii.setInfoType("attribute");
					// CarUtility.locError("ERR1134", LogCriticality.error,"(1a) " + av.getAttrname() + " defaulted to attribute");
				}
				ii.setInfoValue(av.getAttrname());
				iipv.setInfoId(ii);
				iipv.setInfoItemValues(av.getValues());
				aiipv.add(iipv);
			}
			
			dro.setArrayOfInfoIdsPlusValues(aiipv);
			
			// Add the user properties for the icm request (which is basically the full set we have)
			//
			ArrayList<UserProperty> aup = new ArrayList<UserProperty>();
			
			// And a hashmap for dereferencing via oauth_scopes
			
			HashMap<String,ArrayList<String>> ohash = new HashMap<String,ArrayList<String>>();
			
			for (String key : inputAttrMap.keySet()) {
				ArrayList<String> ohinter = new ArrayList<String>();
				for (String values : inputAttrMap.get(key)) {
					UserProperty addu = new UserProperty();
					addu.setUserPropName(key);
					addu.setUserPropValue(values);
					aup.add(addu);
					if (CarUtility.isIIEncoded(rhid, key, config)) {
						ReturnedValueMetaInformation rvmi = CarUtility.getValueMetaInformation(key, values, config,httpClient);
						if (rvmi != null && rvmi.getDisplayname() != null && ! rvmi.getDisplayname().equals("")) {
							ohinter.add(rvmi.getDisplayname());
						} else {
							ohinter.add(values);
						}
					} else {
						ohinter.add(values);
					}
				}
				ohash.put(key, ohinter); 
				
				// And populate the oadn and oadescr hashes for display use
				if (typemap.get(key) != null) {
					ReturnedInfoItemMetaInformation riimi = CarUtility.getInfoItemMetaInformation(rhid, typemap.get(key), key, config,httpClient);
					if (riimi != null && riimi.getDisplayname() != null) {
						oadn.put(key, CarUtility.localize(riimi.getDisplayname(),preflang));
					} else {
						oadn.put(key,key);
					}
					if (riimi != null && riimi.getDescription() != null) {
						oadescr.put(key,  CarUtility.localize(riimi.getDescription(),preflang));
					} else {
						oadescr.put(key, key);
					}
				} else {
					ReturnedInfoItemMetaInformation riimi = CarUtility.getInfoItemMetaInformation(rhid, key, config,httpClient);
					if (riimi != null && riimi.getDisplayname() != null) {
						oadn.put(key, CarUtility.localize(riimi.getDisplayname(),preflang));
					} else {
						oadn.put(key, key);
					}
					if (riimi != null && riimi.getDescription() != null) {
						oadescr.put(key,  CarUtility.localize(riimi.getDescription(),preflang));
					} else {
						oadescr.put(key,key);
					}
				}
			}
			
			dro.setArrayofUserProperty(aup);
			
			// And add the rp properties from the metainfo we got
			ArrayList<RelyingPartyProperty> arpp = new ArrayList<RelyingPartyProperty>();
			// Concurrency protection
			for (ReturnedRPProperty rrpp : new ArrayList<ReturnedRPProperty>(rpmetainformation.getRpproperties())) {
				RelyingPartyProperty rpp = new RelyingPartyProperty();
				rpp.setRpPropName(rrpp.getRppropertyname());
				rpp.setRpPropValue(rrpp.getRppropertyvalue());
				arpp.add(rpp);
			}
			
			dro.setArrayOfRelyingPartyProperty(arpp);
			
			CarUtility.locDebug("ERR1166", "Building ICM request took " + (System.currentTimeMillis() - curtime) + "ms");
			curtime = System.currentTimeMillis();
			
			// And convert the request to JSON for passing to the service
			
			//ObjectMapper mapper = new ObjectMapper();
			ObjectMapper mapper = OMSingleton.getInstance().getOm();
			String decisionRequestJson = null;
			try {
				decisionRequestJson = mapper.writeValueAsString(dro);
			} catch (Exception e) {
				ModelAndView r = new ModelAndView("errorPage");
				CarUtility.locError("ERR0081", "#1");
				r.addObject("messsage",CarUtility.getLocalError("ERR0016"));
				r.addObject("intercept_view","1");
				r.addObject("transient","true");
				r.addObject("top_heading",CarUtility.getLocalComponent("top_heading"));
				r.addObject("institutional_logo_url",CarUtility.getLocalComponent("institutional_logo_url"));
				return r;
			}
			//TODO: change back to info instead of error
			// CarUtility.locError("ERR0082",LogCriticality.error, decisionRequestJson);
			// And send it out to get back a response
			
			CarUtility.locDebug("ERR1166", "Serializing ICM request took " + (System.currentTimeMillis() - curtime) + "ms");
			curtime = System.currentTimeMillis();
			
			IcmDecisionResponseObject response = CarUtility.sendDecisionRequest(decisionRequestJson,config);
			//TODO: change back to info instead of error
			if ("true".equalsIgnoreCase(config.getProperty("logSensitiveInfo", false)))
				CarUtility.locLog("ERR0077", decisionRequestJson);
			
			// Check for bad ICM response
			if (response == null) {
				ModelAndView r = new ModelAndView("errorPage");
				CarUtility.locError("ERR0802");
				r.addObject("message",CarUtility.getLocalError("ERR0802"));
				r.addObject("intercept_view","1");
				r.addObject("transient","true");
				r.addObject("top_heading",CarUtility.getLocalComponent("top_heading"));
				r.addObject("institutional_logo_url",CarUtility.getLocalComponent("institutional_logo_url"));
			} else {
				try {
					//ObjectMapper tmapper = new ObjectMapper();
					ObjectMapper tmapper = OMSingleton.getInstance().getOm();
					String m = tmapper.writeValueAsString(response);
					//TODO: log this at debug not error
					if ("true".equalsIgnoreCase(config.getProperty("logSensitiveInfo", false)))
						CarUtility.locDebug("ERR0803", m);
				} catch (Exception e) {
					// ignore -- best effort logging here
				}
			}
			CarUtility.locDebug("ERR1166","ICM response took " + (System.currentTimeMillis() - curtime) + "ms to get back");
			curtime = System.currentTimeMillis();
			
			// We now have an IcmDecisionResponse object.  Save it in the session.
			session.setAttribute(sconvo + ":" + "icmdecision", response);   // save the current decision response as is.
			try {
				if ("true".equalsIgnoreCase(config.getProperty("logSensitiveInfo", false)))
					CarUtility.locLog("ERR0813", response.toJSON());
			} catch(Exception e) {
				// ignore
			}
			// And determine whether to display the dialog or not
			askUserForDecisions = false; // only ask if the criteria are met
			// Get User metainformation
			ReturnedUserRPMetaInformation urpmi = CarUtility.getUserRPMetaInformation(rpid, userattr, uservalue, config);
			//
			// Now, rely on an optional config and an optional per-RP for askUserForDecisions value if urpmi == null here
			boolean defaultShowAgain = false;
			if (config.getProperty("car.default.showAgain", false) == null || config.getProperty("car.default.showAgain",false).equalsIgnoreCase("true")) {
				defaultShowAgain = true;
			}
			if (rpmetainformation.getDefaultshowagain() != null) {
				// if it is set, it is explicit
				if (rpmetainformation.getDefaultshowagain().equalsIgnoreCase("true")) {
					defaultShowAgain = true;
				} else {
					defaultShowAgain = false;
				}
			}
			// if RP default is set to "never", override user setting and always default to car.default.showAgain value (usually false)
			if ((urpmi==null || urpmi.isShowagain()) && (rpmetainformation.getDefaultshowagain() == null || ! rpmetainformation.getDefaultshowagain().equalsIgnoreCase("never"))) {
				if ("true".equalsIgnoreCase(config.getProperty("logSensitiveInfo",false)))
					CarUtility.locDebug("ERR1117","User " + uservalue + " has showAgain = true");
				askUserForDecisions = true;   // they said to show them the page again or they've never made a decision so we show on first attempt
			}
			boolean haschoices = false; // no choices unless we find some
			boolean todisplay = false;  // no display unless something drives it -- RGC 11-21-2018
			// Also show if there's anything marked askMe that's not ASND in the response
			if (response != null && response.getArrayOfInfoDecisionStatement() != null) {
				for (IcmDecisionsForInfoStatement idfis : response.getArrayOfInfoDecisionStatement()) {
					for (IcmDecisionOnValues idov : idfis.getArrayOfDecisionOnValues()) {
						if (idov.getReleaseDecision().equals(UserReleaseDirective.askMe)) {
							ReturnedInfoItemMetaInformation riimi = null;
							if (idfis.getInfoId().getInfoType() != null) {
								riimi = CarUtility.getInfoItemMetaInformation(rhid, idfis.getInfoId().getInfoType(), idfis.getInfoId().getInfoValue(), config,httpClient);
							} else {
								riimi = CarUtility.getInfoItemMetaInformation(rhid,  idfis.getInfoId().getInfoValue(), config, httpClient);
							}
							if (riimi != null && ! riimi.isAsnd()) {
								if ("true".equalsIgnoreCase(config.getProperty("logSensitiveInfo", false)))
									CarUtility.locDebug("ERR1117","askMe decision for " + idfis.getInfoId().getInfoValue());
								askUserForDecisions = true;
								todisplay = true;  // RGC - 11-21-2018
								haschoices = true;
								break;
							}
						} else { // RGC - 11-21-2018
							ReturnedInfoItemMetaInformation riimi = null;
							if (idfis.getInfoId().getInfoType() != null) {
								riimi = CarUtility.getInfoItemMetaInformation(rhid,  idfis.getInfoId().getInfoType(), idfis.getInfoId().getInfoValue(), config,httpClient);
							} else {
								riimi = CarUtility.getInfoItemMetaInformation(rhid,  idfis.getInfoId().getInfoValue(),config,httpClient);
							}
							if (riimi != null && ! riimi.isAsnd()) {
								todisplay = true;  
							}
						}
					}
					if (haschoices) {
						break;
					}
				}
			}
			// RGC - 11-21-2018
			if (askUserForDecisions && ! todisplay) {
				CarUtility.locDebug("ERR1134","Setting askUserForDecisions to false because todisplay is false");
				askUserForDecisions = false;
			}
			
			CarUtility.locDebug("ERR1134","askUserForDecisions is " + askUserForDecisions + " and todisplay is " + todisplay);
			session.setAttribute(sconvo + ":" + "askUserForDecisions", askUserForDecisions);
			
			
			// now we know whether to mint the response page or the pass-thru redirect.
			
			// Here we play a slightly sick game.  This block is executing because we originally were on the 
			// first leg of a request process -- we had no session information stored.
			// If we need to provide a UI for collecting more info, we do so now by returning the 
			// ModelAndView populated with the relevant injections.
			// If we do not need to provide a UI, we simply continue to the next phase of the process. 
			// In the next phase of the process. we check whether the session indicates we asked the user
			// for responses -- if we did, we collect them (since we know they must be present) and process
			// them before continuing.  If we didn't, we simply continue on down the process.
			
			// For debugging purposes, until the real UI is ready, we use a simple form to process
			// things.
			// while we're at it, we need to determine what if any "may" choices (controlling COPSU decisions) the
			// user has *if* we're in askUserForDecisions mode.  If we're not, it's irrelevant (since the UI won't
			// be forged anyway).  Likewise, if we are, we need to determine what if any "nochoice" options there 
			// are and prepare those for injecting to the UI as well.
			// The UI does not expose "recommended" values for "may" and "nochoice" options -- once the user makes a 
			// decision, we assume the user has evaluated the 
			
			if (askUserForDecisions) {
				// Mint and deliver the response form instead of moving on
				// Currently the debug version, but we'll change all that...
				
				// Establish a CSRF context for this conversation
				String csrftoken = generateCSRFToken();
				// And store it in the session early
				session.setAttribute(sconvo + ":" + "csrftoken",csrftoken);
				// We first have to retrieve the relevant decision from the ARPSI in order to get the 
				// consent service recommendations to use.  Mint a DecisionRequest that only affects the 
				// attribute/value pairs that are marked as "askMe" in the current response.
				
				DecisionRequestObject deco = new DecisionRequestObject();
				ArrayList<AttributeValuePair> aav = new ArrayList<AttributeValuePair>();
				// -rgc- ArrayList<String> significant = new ArrayList<String>();
				HashMap<String,ArrayList<String>> significant = new HashMap<String,ArrayList<String>>(); // askMe decisions
				if (response != null && response.getArrayOfInfoDecisionStatement() != null) {
					for (IcmDecisionsForInfoStatement idfis : response.getArrayOfInfoDecisionStatement()) {
						for (IcmDecisionOnValues idov : idfis.getArrayOfDecisionOnValues()) {
							//
							if (idov.getReleaseDecision().equals(UserReleaseDirective.askMe) ) {
								// this is one we care about
								for (String value : idov.getReturnedValuesList()) {
									AttributeValuePair ap = new AttributeValuePair();
									ap.setAttrname(idfis.getInfoId().getInfoValue());
									ap.setAttrvalue(value);
									aav.add(ap);
									// -rgc -if (! significant.contains(idfis.getInfoId().getInfoValue()))
									// -rgc-		significant.add(idfis.getInfoId().getInfoValue());
									if (!significant.containsKey(idfis.getInfoId().getInfoValue())) {
										ArrayList<String> al = new ArrayList<String>();
										significant.put(idfis.getInfoId().getInfoValue(), al);
									} 
									significant.get(idfis.getInfoId().getInfoValue()).add(value);
								}
							}
						}
					}
				}
				UserId u = new UserId();
				u.setUserType(userattr);
				u.setUserValue(uservalue);
				deco.setUserId(u);
				
				ResourceHolderId rhi = new ResourceHolderId();
				rhi.setRHType("entityId");
				rhi.setRHValue(rhid);
				deco.setResourceHolderId(rhi);
				
				RelyingPartyId rpi = new RelyingPartyId();
				rpi.setRPtype("entityId");
				rpi.setRPvalue(rpid);
				deco.setRelyingPartyId(rpi);
				
				ArrayList<InfoIdPlusValues> aiipv2 = new ArrayList<InfoIdPlusValues>();
				for (AttributeValuelist av : desiredAttributes) {
					// for every desired attribute with values...
					InfoIdPlusValues iipv = new InfoIdPlusValues();
					InfoId ii = new InfoId();
					// TODO:  hard-coding type as "attribute" now, but this needs to change later
					// TODO: somewhat better now...
					// ii.setInfoType("attribute");
					if (typemap.containsKey(av.getAttrname())) {
						ii.setInfoType(typemap.get(av.getAttrname()));
						// CarUtility.locError("ERR1134", LogCriticality.error,"(2) " + av.getAttrname() + " is " + typemap.get(av.getAttrname()));

					} else {
						ii.setInfoType("attribute");  // still default
						// CarUtility.locError("ERR1134", LogCriticality.error,"(2a) " + av.getAttrname() + " defaulted to attribute");

					}
					ii.setInfoValue(av.getAttrname());
					iipv.setInfoId(ii);
					iipv.setInfoItemValues(av.getValues());
					aiipv2.add(iipv);
				}
				
				deco.setArrayOfInfoIdsPlusValues(aiipv2);
				
				ArrayList<UserProperty> aup2 = new ArrayList<UserProperty>();
				for (String key : inputAttrMap.keySet()) {
					for (String values : inputAttrMap.get(key)) {
						UserProperty addu = new UserProperty();
						addu.setUserPropName(key);
						addu.setUserPropValue(values);
						aup2.add(addu);
					}
				}
				
				deco.setArrayofUserProperty(aup2);
				
				// And add the rp properties from the metainfo we got
				ArrayList<RelyingPartyProperty> arpp2 = new ArrayList<RelyingPartyProperty>();
				//Concurrency protection
				for (ReturnedRPProperty rrpp : new ArrayList<ReturnedRPProperty>(rpmetainformation.getRpproperties())) {
					RelyingPartyProperty rpp = new RelyingPartyProperty();
					rpp.setRpPropName(rrpp.getRppropertyname());
					rpp.setRpPropValue(rrpp.getRppropertyvalue());
					arpp2.add(rpp);
				}
				
				deco.setArrayOfRelyingPartyProperty(arpp2);
				
				//ObjectMapper mapper2 = new ObjectMapper();
				ObjectMapper mapper2 = OMSingleton.getInstance().getOm();
				String decisionRequestJson2 = null;
				try {
					decisionRequestJson2 = mapper2.writeValueAsString(dro);
				} catch (Exception e) {
					ModelAndView r = new ModelAndView("errorPage");
					CarUtility.locDebug("ERR0081",LogCriticality.debug,"#1");
					r.addObject("messsage",CarUtility.getLocalError("ERR0016"));
					r.addObject("intercept_view","1");
					r.addObject("transient","true");
					r.addObject("top_heading",CarUtility.getLocalComponent("top_heading"));
					r.addObject("institutional_logo_url",CarUtility.getLocalComponent("institutional_logo_url"));
					return r;
				}
				if ("true".equalsIgnoreCase(config.getProperty("logSensitiveInfo", false)))
					CarUtility.locLog("ERR0084",decisionRequestJson2);
				edu.internet2.consent.arpsi.model.DecisionResponseObject arpsiResponse = CarUtility.sendARPSIDecisionRequest(decisionRequestJson2, config);
				boolean hasMustDecisions = false;
				ArrayList<InjectedDecision> injectedDecisions = new ArrayList<InjectedDecision>();
				// -rgc- for (String aid : significant) {
				for (String aid : significant.keySet()) {
					// get info item metainformation
					ReturnedInfoItemMetaInformation riimi = null;
					if (typemap.get(aid) != null) {
						riimi = CarUtility.getInfoItemMetaInformation(rhid, typemap.get(aid), aid, config,httpClient);
					} else {
						riimi =	CarUtility.getInfoItemMetaInformation(rhid,  aid, config,httpClient);
					}
					String attrDisplayName = null;
					
					if (riimi != null && riimi.getDisplayname() != null) {
						String localized = CarUtility.localize(riimi.getDisplayname(), preflang);
						CarUtility.locDebug("ERR0085",aid,localized);
						attrDisplayName = localized;
					} else {
						CarUtility.locDebug("ERR0086",aid);
						attrDisplayName=aid;
					}
					
					/* Deprecated
					if (riimi != null && riimi.getDisplayname() != null && riimi.getDisplayname().getLocales() != null && ! riimi.getDisplayname().getLocales().isEmpty()) {
						CarUtility.locError("ERR0085",LogCriticality.debug,aid,riimi.getDisplayname().getLocales().get(0).getValue());;
						attrDisplayName = riimi.getDisplayname().getLocales().get(0).getValue();
					} else {
						CarUtility.locError("ERR0086",LogCriticality.info,aid);
						attrDisplayName = aid;
					}
					*/
					
					for (DecisionsForInfoStatement ids : arpsiResponse.getArrayOfInfoDecisionStatement()) {
						if (ids.getInfoId().getInfoValue().equals(aid)) {
							// this is a significant one
							for (DecisionOnValues dov : ids.getArrayOfDecisionOnValues()) {
								for (String v : dov.getReturnedValuesList()) {
									// -rgc- ignore non-askMe
									if (significant.get(aid).contains(v)){
									InjectedDecision id = new InjectedDecision();
									id.setAttrName(aid);  
									id.setAttrDisplayName(attrDisplayName);
									id.setAttrValue(v);
									ReturnedValueMetaInformation rvmi = null;
									if (CarUtility.isIIEncoded(rhid, id.getAttrName(), config))
										rvmi = CarUtility.getValueMetaInformation(id.getAttrName(),id.getAttrValue(),config,httpClient);
									
									// debug
									//if (riimi == null) {
									//	CarUtility.locError("ERR1134", LogCriticality.error, "RIIMI null trap - type is " + aid + " of type " + typemap.get(aid));
									//}
									if (riimi != null && riimi.getPresentationtype() != null && riimi.getPresentationtype().equals("ENCODED")) {
										//ReturnedValueMetaInformation rvmi = CarUtility.getValueMetaInformation(id.getAttrName(),id.getAttrValue(),config);
										if (rvmi != null && rvmi.getDisplayname() != null) {
											id.setAttrDisplayValue(rvmi.getDisplayname());
										} else {
											id.setAttrDisplayValue(id.getAttrValue());
										}
									} else {
										id.setAttrDisplayValue(id.getAttrValue());
									}
									id.setRecommendedDirective(dov.getReleaseDecision().toString());
									id.setChosenDirective(dov.getReleaseDecision().toString());
									if (riimi != null)
										id.setSensitivity(riimi.isSensitivity());
									if ((riimi != null && riimi.isAsnd()) || (rvmi != null && rvmi.getAsnd())) {
										id.setAsnd(true);
									} else {
										id.setAsnd(false);
									}
									if (riimi != null)
										id.setPolicytype(riimi.getPolicytype());
									if ((riimi != null && riimi.isAsnd()) || (rvmi != null && rvmi.getAsnd())) {
										// override to PERMIT for Asnd
										id.setChosenDirective("permit");
										id.setRecommendedDirective("permit");
									}
									if (! injectedDecisions.contains(id)) {  // duplicate suppression
										injectedDecisions.add(id);
									}
									// if this is Asnd, it doesn't create the need for hasMust
									if ((riimi == null || ! riimi.isAsnd()) && (rvmi == null || ! rvmi.getAsnd()))
										hasMustDecisions = true;
									}
								}
							}
						}
					}
				}
				
				// Here, we compute a set of "may" decisions in mayDecisions for injection into the UI
				// "may" decisions are values in the response.getArrayOfInfoDecisionStatement()
				// that are presented as either "permit" or "deny" rather than "askMe"
				// NOTE:  ICM handles useAdvice directly, now, so useAdvice never makes it to this level
				
				
				ArrayList<InjectedDecision> mayDecisions = new ArrayList<InjectedDecision>();
				ArrayList<InjectedDecision> nochoiceDecisions = new ArrayList<InjectedDecision>();
				boolean hasMay = false;
				boolean hasNoChoice = false;
				
				if (response != null && response.getArrayOfInfoDecisionStatement() != null) {
					for (IcmDecisionsForInfoStatement idfis : response.getArrayOfInfoDecisionStatement()) {
						for (IcmDecisionOnValues idov : idfis.getArrayOfDecisionOnValues()) {
							String attrDisplayName=null;
							ReturnedInfoItemMetaInformation riimi = null;
							if (idfis.getInfoId().getInfoType() != null) {
								riimi = CarUtility.getInfoItemMetaInformation(rhid, idfis.getInfoId().getInfoType(),idfis.getInfoId().getInfoValue(), config,httpClient);
							} else {
								riimi = CarUtility.getInfoItemMetaInformation(rhid,  idfis.getInfoId().getInfoValue(), config,httpClient);
							}
							if (riimi != null && riimi.getDisplayname() != null) {
								String localized = CarUtility.localize(riimi.getDisplayname(),preflang);
								CarUtility.locDebug("ERR0085",idfis.getInfoId().getInfoValue(),localized);
								attrDisplayName = localized;
							} else {
								CarUtility.locDebug("ERR0086",idfis.getInfoId().getInfoValue());
								attrDisplayName = idfis.getInfoId().getInfoValue();
							}
							
							/* Deprecated
							if (riimi != null && riimi.getDisplayname() != null && riimi.getDisplayname().getLocales() != null && ! riimi.getDisplayname().getLocales().isEmpty()) {
								CarUtility.locError("ERR0085",LogCriticality.debug,idfis.getInfoId().getInfoValue(),riimi.getDisplayname().getLocales().get(0).getValue());;
								attrDisplayName = riimi.getDisplayname().getLocales().get(0).getValue();
							} else {
								CarUtility.locError("ERR0086",LogCriticality.debug,idfis.getInfoId().getInfoValue());
								attrDisplayName = idfis.getInfoId().getInfoValue();
							}
							*/
							
							if (idov.getReleaseDecision().equals(UserReleaseDirective.permit) || idov.getReleaseDecision().equals(UserReleaseDirective.deny)) {
								// this is a may or a nochoice case we care about
								for (String value : idov.getReturnedValuesList()) {
									InjectedDecision ap = new InjectedDecision();
									ap.setAttrName(idfis.getInfoId().getInfoValue());
									ap.setAttrValue(value);
									ReturnedValueMetaInformation rvmi = null;
									if (riimi != null && "ENCODED".equalsIgnoreCase(riimi.getPresentationtype()))
										rvmi = CarUtility.getValueMetaInformation(ap.getAttrName(),ap.getAttrValue(),config,httpClient);
									if (riimi != null && riimi.getPresentationtype() != null && riimi.getPresentationtype().equals("ENCODED")) {
										// ReturnedValueMetaInformation rvmi = CarUtility.getValueMetaInformation(ap.getAttrName(),ap.getAttrValue(),config);
										if (rvmi != null && rvmi.getDisplayname() != null) {
											ap.setAttrDisplayValue(rvmi.getDisplayname());
										} else {
											ap.setAttrDisplayValue(ap.getAttrValue());
										}
									} else {
										ap.setAttrDisplayValue(ap.getAttrValue());
									}
									ap.setAttrDisplayName(attrDisplayName);
									ap.setChosenDirective(idov.getReleaseDecision().toString());
									if (riimi != null)
									ap.setSensitivity(riimi.isSensitivity());
									if (riimi != null && (riimi.isAsnd() || (rvmi != null && rvmi.getAsnd()))) {
										ap.setAsnd(true);
									} else {
										ap.setAsnd(false);
									}
									if (riimi != null)
									ap.setPolicytype(riimi.getPolicytype());
									if (riimi != null && (riimi.isAsnd() || (rvmi != null && rvmi.getAsnd()))) {
										// override chosen directive to PERMIT if Asnd is set on
										ap.setChosenDirective("permit");
										ap.setRecommendedDirective("permit");
									}
									
									if (idov.getAugmentedPolicyId().getPolicySource().equals(PolicySourceEnum.COPSU) && ! mayDecisions.contains(ap)) { // duplicate suppression
										mayDecisions.add(ap);
										// Suppress update of hasMay if this is ASND
										if (riimi == null || (!riimi.isAsnd() && (rvmi == null || ! rvmi.getAsnd())))
											hasMay = true;
									} else if (! nochoiceDecisions.contains(ap) && ! mayDecisions.contains(ap)){ // duplicate suppression
										nochoiceDecisions.add(ap);
										// Suppress update of hasNoChoice if this is ASND
										if (riimi == null || (!riimi.isAsnd() && (rvmi == null || ! rvmi.getAsnd())))
											hasNoChoice = true;
									}
								}
							}
						}
					}
				}
				boolean hasUserChoices = hasMay || hasMustDecisions;
				
				HashMap<String,ArrayList<String>> permitInjected = new HashMap<String,ArrayList<String>>();
				HashMap<String,ArrayList<String>> denyInjected = new HashMap<String,ArrayList<String>>();
				HashMap<String,ArrayList<String>> permitMay = new HashMap<String,ArrayList<String>>();
				HashMap<String,ArrayList<String>> denyMay = new HashMap<String,ArrayList<String>>();
				HashMap<String,ArrayList<String>> permitNo = new HashMap<String,ArrayList<String>>();
				
				HashMap<String,String> sensitivity = new HashMap<String,String>();
				HashMap<String,String> asnd = new HashMap<String,String>();
				
				HashMap<String,String> policytype = new HashMap<String,String>();
				
				HashMap<String,ArrayList<String>> valuesets = new HashMap<String,ArrayList<String>>();
				HashMap<String,ArrayList<String>> displayvaluesets = new HashMap<String,ArrayList<String>>();
				
				for (InjectedDecision id : injectedDecisions) {
					// maintain valuesets and displayvaluesets as well
					if (! valuesets.containsKey(id.getAttrDisplayName())) {
						valuesets.put(id.getAttrDisplayName(), new ArrayList<String>());
						valuesets.get(id.getAttrDisplayName()).add(id.getAttrValue());
						displayvaluesets.put(id.getAttrDisplayName(), new ArrayList<String>());
						displayvaluesets.get(id.getAttrDisplayName()).add(id.getAttrDisplayValue());
					} else {
						valuesets.get(id.getAttrDisplayName()).add(id.getAttrValue());
						displayvaluesets.get(id.getAttrDisplayName()).add(id.getAttrDisplayValue());
					}
					if (! sensitivity.containsKey(id.getAttrDisplayName()) && id.isSensitivity()) {
						sensitivity.put(id.getAttrDisplayName(), "true");
					}
					if (! asnd.containsKey(id.getAttrDisplayName()) && id.isAsnd()) {
						asnd.put(id.getAttrDisplayName(),"true");
					}
					if (! policytype.containsKey(id.getAttrDisplayName()) && id.getPolicytype() != null) {
						policytype.put(id.getAttrDisplayName(),id.getPolicytype());
					}
					if (id.getChosenDirective().equalsIgnoreCase("permit")) {
						if (! permitInjected.containsKey(id.getAttrDisplayName())) {
							ArrayList<String> a = new ArrayList<String>();
							permitInjected.put(id.getAttrDisplayName(),a);
						}
						permitInjected.get(id.getAttrDisplayName()).add(id.getAttrDisplayValue());
					} else {
						if (! denyInjected.containsKey(id.getAttrDisplayName())) {
							ArrayList<String> a = new ArrayList<String>();
							denyInjected.put(id.getAttrDisplayName(), a);
						}
						denyInjected.get(id.getAttrDisplayName()).add(id.getAttrDisplayValue());
					}
				}
				for (InjectedDecision id : nochoiceDecisions) {
					// maintain valuesets and displayvaluesets as well
					if (! valuesets.containsKey(id.getAttrDisplayName())) {
						valuesets.put(id.getAttrDisplayName(), new ArrayList<String>());
						valuesets.get(id.getAttrDisplayName()).add(id.getAttrValue());
						displayvaluesets.put(id.getAttrDisplayName(), new ArrayList<String>());
						displayvaluesets.get(id.getAttrDisplayName()).add(id.getAttrDisplayValue());
					} else {
						valuesets.get(id.getAttrDisplayName()).add(id.getAttrValue());
						displayvaluesets.get(id.getAttrDisplayName()).add(id.getAttrDisplayValue());
					}
					if (! sensitivity.containsKey(id.getAttrDisplayName()) && id.isSensitivity()) {
						sensitivity.put(id.getAttrDisplayName(), "true");
					}
					if (! asnd.containsKey(id.getAttrDisplayName()) && id.isAsnd()) {
						asnd.put(id.getAttrDisplayName(), "true");
					}
					if (! policytype.containsKey(id.getAttrDisplayName()) && id.getPolicytype() != null) {
						policytype.put(id.getAttrDisplayName(),id.getPolicytype());
					}
					if (id.getChosenDirective().equalsIgnoreCase("permit")) {
						if (! permitNo.containsKey(id.getAttrDisplayName())) {
							ArrayList<String> a = new ArrayList<String>();
							permitNo.put(id.getAttrDisplayName(),a);
						}
						permitNo.get(id.getAttrDisplayName()).add(id.getAttrDisplayValue());
					} 
				}
				
				for (InjectedDecision id : mayDecisions) {
					// maintain valuesets and displayvaluesets as well
					if (! valuesets.containsKey(id.getAttrDisplayName())) {
						valuesets.put(id.getAttrDisplayName(), new ArrayList<String>());
						valuesets.get(id.getAttrDisplayName()).add(id.getAttrValue());
						displayvaluesets.put(id.getAttrDisplayName(), new ArrayList<String>());
						displayvaluesets.get(id.getAttrDisplayName()).add(id.getAttrDisplayValue());
					} else {
						valuesets.get(id.getAttrDisplayName()).add(id.getAttrValue());
						displayvaluesets.get(id.getAttrDisplayName()).add(id.getAttrDisplayValue());
					}
					if (! sensitivity.containsKey(id.getAttrDisplayName()) && id.isSensitivity()) {
						sensitivity.put(id.getAttrDisplayName(), "true");
					}
					if (! asnd.containsKey(id.getAttrDisplayName()) && id.isAsnd()) {
						asnd.put(id.getAttrDisplayName(),"true");
					}
					if (! policytype.containsKey(id.getAttrDisplayName()) && id.getPolicytype() != null) {
						policytype.put(id.getAttrDisplayName(),id.getPolicytype());
					}
					if (id.getChosenDirective().equalsIgnoreCase("permit")) {
						if (! permitMay.containsKey(id.getAttrDisplayName())) {
							ArrayList<String> a = new ArrayList<String>();
							permitMay.put(id.getAttrDisplayName(),a);
						}
						permitMay.get(id.getAttrDisplayName()).add(id.getAttrDisplayValue());
					} else {
						if (! denyMay.containsKey(id.getAttrDisplayName())) {
							ArrayList<String> a = new ArrayList<String>();
							denyMay.put(id.getAttrDisplayName(), a);
						}
						denyMay.get(id.getAttrDisplayName()).add(id.getAttrDisplayValue());
					}
				}


				// Marshal the RH info for display purposes
				ArrayList<ReturnedRHMetaInformation> arhmi = CarUtility.getRHMetaInformation(config);
				String rhdisplayname="";
				for (ReturnedRHMetaInformation mi : arhmi) {
					if (mi.getRhidentifier().getRhid().contentEquals(rpmetainformation.getRhidentifier().getRhid())) {
						rhdisplayname = CarUtility.localize(mi.getDisplayname(),preflang);
					}
				}
				
				// RGC - new intercept UI (for now) -- old remains available to revert to
				// ModelAndView debugReturn = new ModelAndView("intercept");
				ModelAndView debugReturn = new ModelAndView("altintercept");
				debugReturn.addObject("use_alternate_intercept",1);
				debugReturn.addObject("isrequesting",CarUtility.getLocalComponent("isrequesting"));
				debugReturn.addObject("transferdetails",CarUtility.getLocalComponent("transferdetails"));
				debugReturn.addObject("isreceiving",CarUtility.getLocalComponent("isreceiving"));
				debugReturn.addObject("inforequired",CarUtility.getLocalComponent("inforequired"));
				debugReturn.addObject("reviewedit",CarUtility.getLocalComponent("reviewandedit"));
				debugReturn.addObject("skip-screen",CarUtility.getLocalComponent("skip-screen"));
				debugReturn.addObject("alt_institutional_logo_url",CarUtility.getLocalComponent("alt_institutional_logo_url"));
				
				// end RGC - new intercept UI
				
				if (! useCrypto) {
					debugReturn.addObject("actionUrl",CarUtility.interceptUrl(config)+"?conversation="+sconvo);
				} else {
					debugReturn.addObject("actionUrl",CarUtility.cryptoInterceptUrl(config)+"?conversation="+sconvo);
				}

				
				Collections.sort(injectedDecisions);
				debugReturn.addObject("injectedDecisions",injectedDecisions);
				Collections.sort(mayDecisions);
				debugReturn.addObject("injectedMayDecisions",mayDecisions);
				Collections.sort(nochoiceDecisions);
				debugReturn.addObject("injectedNochoiceDecisions",nochoiceDecisions);
				//ObjectMapper omapper = new ObjectMapper();
				ObjectMapper omapper = OMSingleton.getInstance().getOm();
				try {
					debugReturn.addObject("sensitivity",omapper.writeValueAsString(sensitivity));
					CarUtility.locDebug("ERR1112",omapper.writeValueAsString(sensitivity));
				} catch (Exception ign) {
					CarUtility.locDebug("ERR1113",sensitivity.toString());
					debugReturn.addObject("sensitivity","");
				}
				try {
					debugReturn.addObject("asnd",omapper.writeValueAsString(asnd));
					CarUtility.locDebug("ERR1114", omapper.writeValueAsString(asnd));
				} catch (Exception ign) {
					CarUtility.locDebug("ERR1115",asnd.toString());
					debugReturn.addObject("asnd","");
				}
				try {
					debugReturn.addObject("policytype",omapper.writeValueAsString(policytype));
					CarUtility.locDebug("ERR1119",omapper.writeValueAsString(policytype));
				} catch (Exception ign) {
					CarUtility.locDebug("ERR1120", policytype.toString());
					debugReturn.addObject("policytype","");
				}
				try {
					debugReturn.addObject("valuesets",omapper.writeValueAsString(valuesets));
					debugReturn.addObject("displayvaluesets",omapper.writeValueAsString(displayvaluesets));
				} catch (Exception ign) {
					debugReturn.addObject("valuesets","");
					debugReturn.addObject("displayvaluesets","");
				}
				try {
					debugReturn.addObject("permitInjected",omapper.writeValueAsString(permitInjected));
				} catch (Exception ign) {
					debugReturn.addObject("permitInjected","");
				}
				try {
					debugReturn.addObject("denyInjected",omapper.writeValueAsString(denyInjected));
				} catch (Exception ign) {
					debugReturn.addObject("denyInjected","");
				}
				try {
					debugReturn.addObject("permitMay",omapper.writeValueAsString(permitMay));
				} catch (Exception ign) {
					debugReturn.addObject("permitMay","");
				}
				try {
					debugReturn.addObject("denyMay",omapper.writeValueAsString(denyMay));
				} catch (Exception ign) {
					debugReturn.addObject("denyMay","");
				}
				try {
					debugReturn.addObject("permitNo",omapper.writeValueAsString(permitNo));
				} catch (Exception ign) {
					debugReturn.addObject("permitNo","");
				}
				try {
					debugReturn.addObject("ohashjs",omapper.writeValueAsString(ohash));
				} catch (Exception ign) {
					debugReturn.addObject("ohashjs","");
				}
				try {
					debugReturn.addObject("oadnjs",omapper.writeValueAsString(oadn));
				} catch (Exception ign) {
					debugReturn.addObject("oadnjs","");
				}
				try {
					debugReturn.addObject("oadescrjs",omapper.writeValueAsString(oadescr));
				} catch (Exception ign) {
					debugReturn.addObject("oadescrjs","");
				}
				
				// Optimize for reuse
				String locrpdisp = rpmetainformation.getRpidentifier().getRpid();
				String locrpdesc = rpmetainformation.getRpidentifier().getRpid();
				if (rpmetainformation.getDisplayname() != null)
					locrpdisp = CarUtility.localize(rpmetainformation.getDisplayname(),preflang);
				if (rpmetainformation.getDescription() != null)
					locrpdesc = CarUtility.localize(rpmetainformation.getDescription(),preflang);
				
				debugReturn.addObject("hasMay",hasMay);
				debugReturn.addObject("hasMustDecisions",hasMustDecisions);
				debugReturn.addObject("hasNoChoice",hasNoChoice);
				debugReturn.addObject("page-title",CarUtility.getLocalComponent("page-title"));
				debugReturn.addObject("title-choice",CarUtility.getLocalComponent("title-choice",locrpdisp));
				debugReturn.addObject("title-nochoice",CarUtility.getLocalComponent("title-nochoice",locrpdisp));
				debugReturn.addObject("intro",CarUtility.getLocalComponent("intro",locrpdisp));
				debugReturn.addObject("hasChoices",haschoices);
				debugReturn.addObject("hasUserChoices",hasUserChoices);
				debugReturn.addObject("rpiconurl",rpmetainformation.getIconurl());
				debugReturn.addObject("rpdisplayname",locrpdisp);
				debugReturn.addObject("rhdisplayname",rhdisplayname);

				debugReturn.addObject("rpdescription",rpmetainformation.getDescription()==null?null:locrpdesc);
				debugReturn.addObject("rpprivacyurl",rpmetainformation.getPrivacyurl());
				debugReturn.addObject("header_nochoice",CarUtility.getLocalComponent("header_nochoice",CarUtility.localize(rpmetainformation.getDisplayname(),preflang)));
				debugReturn.addObject("acknowledge_show_false",CarUtility.getLocalComponent("acknowledge_show_false"));
				debugReturn.addObject("acknowledge_show_true",CarUtility.getLocalComponent("acknowledge_show_true"));
				debugReturn.addObject("continue_button_false",CarUtility.getLocalComponent("continue_button_false"));
				debugReturn.addObject("continue_button_true",CarUtility.getLocalComponent("continue_button_true"));
				debugReturn.addObject("save_prompt",CarUtility.getLocalComponent("save_prompt"));
				
				debugReturn.addObject("header_must", CarUtility.getLocalComponent("header_must",locrpdisp));
				debugReturn.addObject("header_may",CarUtility.getLocalComponent("header_may", locrpdisp));
				debugReturn.addObject("choice_edit",CarUtility.getLocalComponent("choice_edit"));
				debugReturn.addObject("choice_dont_edit",CarUtility.getLocalComponent("choice_dont_edit"));
				debugReturn.addObject("header_must_and_may",CarUtility.getLocalComponent("header_must_and_may",locrpdisp));
				
				debugReturn.addObject("release_what",CarUtility.getLocalComponent("release_what",locrpdisp));
				debugReturn.addObject("save_true_suppress",CarUtility.getLocalComponent("save_true_suppress"));
				debugReturn.addObject("save_true_show_again",CarUtility.getLocalComponent("save_true_show_again"));
				debugReturn.addObject("save_false",CarUtility.getLocalComponent("save_false"));
				debugReturn.addObject("nochoice_nodisplay",CarUtility.getLocalComponent("nochoice_nodisplay"));
				debugReturn.addObject("nochoice_display",CarUtility.getLocalComponent("nochoice_display"));
				debugReturn.addObject("recommends",CarUtility.getLocalComponent("recommends"));
				debugReturn.addObject("show_required",CarUtility.getLocalComponent("show_required",locrpdisp));
				debugReturn.addObject("held-by",CarUtility.getLocalComponent("held-by"));
				debugReturn.addObject("edit-presets",CarUtility.getLocalComponent("edit-presets"));
				debugReturn.addObject("update-settings",CarUtility.getLocalComponent("update-settings"));
				debugReturn.addObject("privacy-policy",CarUtility.getLocalComponent("privacy-policy"));
				debugReturn.addObject("mandatory-header",CarUtility.getLocalComponent("mandatory-header"));
				debugReturn.addObject("dont-show",CarUtility.getLocalComponent("dont-show"));
				debugReturn.addObject("self-service-description",CarUtility.getLocalComponent("self-service-description"));
				debugReturn.addObject("save-continue",CarUtility.getLocalComponent("save-continue"));
				debugReturn.addObject("cancel",CarUtility.getLocalComponent("cancel"));
				
				debugReturn.addObject("top_heading",CarUtility.getLocalComponent("top_heading"));
				debugReturn.addObject("sign_out",CarUtility.getLocalComponent("sign_out"));
				debugReturn.addObject("institutional_logo_url",CarUtility.getLocalComponent("institutional_logo_url"));
				debugReturn.addObject("short_institution",CarUtility.getLocalComponent("short_institution"));
				debugReturn.addObject("preflang",preflang);
				
				debugReturn.addObject("areason",areason);
				
				// And pass in the sconvo value, just in case
				debugReturn.addObject("sconvo",sconvo);
				
				// And pass in the csrftoken, as well
				debugReturn.addObject("csrftoken",csrftoken);
				
				// And suppress the default header in favor of the intercept layout option
				debugReturn.addObject("intercept_view","1");
				
				// provide a blind hashmap for handling form layout
				HashMap<String,Integer> counters = new HashMap<String,Integer>();
				// nullity in Velocity 2.x attaches to empty collections too
				counters.put("make_counters_not_null_for_velocity", Integer.valueOf(1));
				debugReturn.addObject("counters",counters);
				
				// For handling of showagain checkbox
				// Pass in both the current default showagain setting for this RP and this user's showagain setting
				// We set the status of the showagain checkbox accordingly, and then consistently honor 
				// the showagain value on POST back here.
				debugReturn.addObject("defaultShowAgain",defaultShowAgain); // RP default mod system default
				if (urpmi == null) {
					debugReturn.addObject("userShowAgain",defaultShowAgain?"true":"false");  // use default if no user pref
				} else {
					debugReturn.addObject("userShowAgain",urpmi.isShowagain()?"true":"false"); // ternary operators are the devil
				}
				
				// Include the typemap for selection against oauth_scope items
				
				debugReturn.addObject("typemap",typemap);
				
				CarUtility.locDebug("ERR1166", "Marshalling display data for intercept took " + (System.currentTimeMillis() - curtime) + "ms");
				curtime = System.currentTimeMillis();
				
				if (hasMay || hasMustDecisions || hasNoChoice) {  // pass thru if everything turns out to be missing or ASND
					CarUtility.locLog("ERR1134","Displaying intercept");
					return debugReturn;
				} 
				CarUtility.locLog("ERR1134",  LogCriticality.error,"Not Displaying intercept, in AskUser mode");
			}
			CarUtility.locLog("ERR1134",  LogCriticality.error,"Not displaying intercept, no user questions either");
		}
		// if we get here, we're on a return trip by definition.
		
		// Start by getting the sconvo value out of the URL (and error if none is specified)
		if (request.getParameter("conversation") != null) {
			convo = Integer.parseInt((String) request.getParameter("conversation"));
			sconvo = String.valueOf(convo);
		} else if (askUserForDecisions) {
			ModelAndView e = new ModelAndView("errorPage");
			e.addObject("message","Your browser did not properly identify itself.  This usually indicates a bug.");
			e.addObject("intercept_view","1");
			e.addObject("top_heading",CarUtility.getLocalComponent("top_heading"));
			e.addObject("institutional_logo_url",CarUtility.getLocalComponent("institutional_logo_url"));
			return e;  // bail out if there is no conversation specified, since we cannot continue otherwise
		}
		
		// Assuming we now have a conversation detected, check CSRF if appropriate (if there was a roundtrip to the client)
		//
		if (askUserForDecisions) {
			// Check CSRF token
			if (session.getAttribute(sconvo + ":" + "csrftoken") == null || ! session.getAttribute(sconvo+":"+"csrftoken").equals((String) request.getParameter("csrftoken"))) {
				// CSRF failure
				ModelAndView e = new ModelAndView("errorPage");
				e.addObject("message","CSRF failure.  This may occur due to a sesison timeout, due to the use of your browser's back button, or due to purposeful attack.");
				e.addObject("intercept_view","1");
				e.addObject("transient","true");
				e.addObject("top_heading",CarUtility.getLocalComponent("top_heading"));
				e.addObject("institutional_logo_url",CarUtility.getLocalComponent("institutional_logo_url"));
				return e; // bail out if CSRF test fails for any reason
			}
		}
		
		// No CSRF test required if the browser was never consulted, althoug session is still required for continuity
			

		// At this point, we have convo and sconvo set properly
		
		// Start decoding response from the page
		// First, marshall what we have in our session and clear out the session
		IcmDecisionResponseObject originalDecisionResponse = (IcmDecisionResponseObject) session.getAttribute(sconvo + ":" + "icmdecision");
		session.removeAttribute(sconvo + ":" + "icmdecision");
		
		Boolean baskUserForDecisions = (Boolean) session.getAttribute(sconvo + ":" + "askUserForDecisions");
		session.removeAttribute(sconvo + ":" + "askUserForDecisions");
		
		String returntourl = (String) session.getAttribute(sconvo + ":" + "returntourl");
		session.removeAttribute(sconvo + ":" + "returntourl");
		
		String rhid = (String) session.getAttribute(sconvo + ":" + "rhid");
		session.removeAttribute(sconvo + ":" + "rhid");
		
		String rpid = (String) session.getAttribute(sconvo + ":" + "rpid");
		session.removeAttribute(sconvo + ":" + "rpid");
		
		String username = (String) session.getAttribute(sconvo + ":" + "username");
		session.removeAttribute(sconvo + ":" + "username");
		
		String usertype = (String) session.getAttribute(sconvo + ":" + "usertype");
		session.removeAttribute(sconvo + ":" + "usertype");
		
		// Compute the values we need to retrieve from the client side,
		// and retrieve their values based on the information in the hidden form posted back 
		// to us.
		//
		// We walk the original ICM response and produce an ArrayList of AttributeValuePair objects
		// containing the final target decisions, which we then pass to the converter for sending back to
		// the RH.
		
		ArrayList<AttributeValuePair> finalDecisions = new ArrayList<AttributeValuePair>();
		if (originalDecisionResponse != null && originalDecisionResponse.getArrayOfInfoDecisionStatement() != null) {
		for (IcmDecisionsForInfoStatement iids : originalDecisionResponse.getArrayOfInfoDecisionStatement()) {
			for (IcmDecisionOnValues idov : iids.getArrayOfDecisionOnValues()) {
				if (idov.getReleaseDecision().equals(UserReleaseDirective.permit) || idov.getReleaseDecision().equals(UserReleaseDirective.deny)) {
					// the original had a decision already for this attribute.
					// check if it's a may or a nochoice
					if (idov.getAugmentedPolicyId().getPolicySource().equals(PolicySourceEnum.COPSU)) {
						// it was a may decision, so we pull the values back from the posted results from the 
						// client

						for (String v : idov.getReturnedValuesList()) {
							// for every value in this attribute with this decision...
							// These are may values...
							// This is a bit absurd, but...  find the right tag to use for this value
							int ctr = 0;
							boolean foundOrFinished = false;
							boolean usectr = false;
							while (!foundOrFinished) {
								String valuemapname = "valuemap_" + iids.getInfoId().getInfoValue() + "_" + ctr;
								if (request.getParameter(valuemapname) != null && request.getParameter(valuemapname).equals(v)) {
									// this is the one
									foundOrFinished = true;
									usectr = true;
								} else {
									if (request.getParameter(valuemapname) == null) {
										foundOrFinished = true;
										usectr = false;
									} else {
										ctr += 1;
									}
								}
							}
							/* removing debugs here */
							/*if (usectr) {
								CarUtility.locError("ERR0812",LogCriticality.debug,iids.getInfoId().getInfoValue(),String.valueOf(ctr),v,request.getParameter("radio_"+iids.getInfoId().getInfoValue()+"_"+ctr));
							} else {
								CarUtility.locError("ERR0812",LogCriticality.debug,iids.getInfoId().getInfoValue(),"nocounter",v,idov.getReleaseDecision().toString());
							}*/
							
							// At this point, if usectr is false, we didn't get a value, and if it is true, 
							// we use ctr as the counter for the value
							if (!usectr) {
								// this is an odd case that shouldn't happen, but if it does somehow, we simply
								// use the value that was present to begin with.
								AttributeValuePair avp = new AttributeValuePair();
								avp.setAttrname(iids.getInfoId().getInfoValue());
								avp.setAttrvalue(v);
								avp.setCurrentdecision(idov.getReleaseDecision().toString());
								avp.setPolicySource("COPSU");
								finalDecisions.add(avp);
							} else {
								// this case is the common one -- we have a response value to use here
								String radioName = "radio_"+iids.getInfoId().getInfoValue()+"_"+ctr;
								String dec = (String) request.getParameter(radioName);  // should be permit or deny
								AttributeValuePair avp = new AttributeValuePair();
								avp.setAttrname(iids.getInfoId().getInfoValue());
								avp.setAttrvalue(v);
								avp.setCurrentdecision(dec);
								avp.setPolicySource("COPSU");
								finalDecisions.add(avp);
							}
						}
					} else {
						// it was a nochoice decision, so we simply populate what we have
						for (String v : idov.getReturnedValuesList()) {
							// for every value of this attribute with this decision...
							AttributeValuePair avp = new AttributeValuePair();
							avp.setAttrname(iids.getInfoId().getInfoValue());
							avp.setAttrvalue(v);
							avp.setCurrentdecision(idov.getReleaseDecision().toString());
							avp.setPolicySource("ARPSI");
							finalDecisions.add(avp);
						}
					}
				}
				if (idov.getReleaseDecision().equals(UserReleaseDirective.askMe)) {
					// This was a must decision -- mandatory response from the client
					
					for (String v : idov.getReturnedValuesList()) {
						// for every value in this attribute with this decision...
						// These are must values...
						// This is a bit absurd, but...  find the right tag to use for this value
						int ctr = 0;
						boolean foundOrFinished = false;
						boolean usectr = false;
						while (!foundOrFinished) {
							String valuemapname = "valuemap_" + iids.getInfoId().getInfoValue() + "_" + ctr;
							if (request.getParameter(valuemapname) != null && request.getParameter(valuemapname).equals(v)) {
								// this is the one
								foundOrFinished = true;
								usectr = true;
							} else {
								if (request.getParameter(valuemapname) == null) {
									foundOrFinished = true;
									usectr = false;
								} else {
									ctr += 1;
								}
							}
						}
						// Removing debugs here
						/*if (usectr) {
							CarUtility.locError("ERR0812",LogCriticality.debug,iids.getInfoId().getInfoValue(),String.valueOf(ctr),v,request.getParameter("radio_"+iids.getInfoId().getInfoValue()+"_"+ctr));
						} else {
							CarUtility.locError("ERR0812",LogCriticality.debug,iids.getInfoId().getInfoValue(),"nocounter",v,idov.getReleaseDecision().toString());
						}*/
						
						// At this point, if usectr is false, we didn't get a value, and if it is true, 
						// we use ctr as the counter for the value
						if (!usectr) {
							// this is an odd case that shouldn't happen, but if it does somehow, we simply
							// use the value that was present to begin with.
							CarUtility.locDebug("ERR0801");
							AttributeValuePair avp = new AttributeValuePair();
							avp.setAttrname(iids.getInfoId().getInfoValue());
							avp.setAttrvalue(v);
							avp.setCurrentdecision(idov.getReleaseDecision().toString());
							avp.setPolicySource("COPSU");
							finalDecisions.add(avp);
						} else {
							// this case is the common one -- we have a response value to use here
							// CarUtility.locError("ERR1134",  LogCriticality.error,"Handling " + iids.getInfoId().getInfoValue());
							String radioName = "radio_"+iids.getInfoId().getInfoValue()+"_"+ctr;
							String dec = (String) request.getParameter(radioName);  // should be permit or deny
							AttributeValuePair avp = new AttributeValuePair();
							avp.setAttrname(iids.getInfoId().getInfoValue());
							avp.setAttrvalue(v);
							avp.setCurrentdecision(dec);
							avp.setPolicySource("COPSU");
							finalDecisions.add(avp);
						}
					}
				}
			}
		}
		}
		// For now, construct an error message string that depicts what will happen
		StringBuilder rb = new StringBuilder();
		rb.append("The following release decisions will be used:<br><ul>\n");
		for (AttributeValuePair ap : finalDecisions) {
			rb.append("<li>"+ap.getAttrname()+"="+ap.getAttrvalue()+" : " + ap.getCurrentdecision() + "</li>\n");
		}
		rb.append("</ul><br>\n");
		rb.append("The posted values were:<br><ul>\n");
		@SuppressWarnings("unchecked")
		Enumeration<String> attrs = request.getParameterNames();
		while (attrs.hasMoreElements()) {
			String name = attrs.nextElement();
			rb.append("<li>"+name+"="+request.getParameter(name)+"</li>\n");
		}
		rb.append("</ul>");
		if (baskUserForDecisions && (request.getParameter("alltheway") == null || (request.getParameter("alltheway") != null && !request.getParameter("alltheway").equals("1")))) {
				ModelAndView testreturn = new ModelAndView("errorPage");
				//testreturn.addObject("message","Return trip detected?!?");
				testreturn.addObject("message",rb.toString());
				testreturn.addObject("intercept_view","1");
				// Try again...
				if (session != null) 
					session.removeAttribute(sconvo + ":" + "returntourl");
				// recurse
				return handleFilterAndDecide(request, useCrypto);
				//return testreturn;
		} else {
			// This is a push to the other side
			// Construct the response object for the RH
			ArrayList<NameValueDecision> alnvd = new ArrayList<NameValueDecision>();
			for (AttributeValuePair ap : finalDecisions) {
				if (ap.getCurrentdecision().equals("permit")) {
					NameValueDecision nvd = new NameValueDecision();
					nvd.setName(ap.getAttrname());
					nvd.setValue(ap.getAttrvalue());
					nvd.setDecision("permit");
					alnvd.add(nvd);
				} else {
					// In the event that we're asnd, we have to override to permit 
					// despite everything else we've done. 
					//
					// ASND overrides it all
					//
					if (CarUtility.isIIVAsnd(rhid, ap.getAttrname(), ap.getAttrvalue(), config)) {
						NameValueDecision nvd = new NameValueDecision();
						nvd.setName(ap.getAttrname());
						nvd.setValue(ap.getAttrvalue());
						nvd.setDecision("permit");
						alnvd.add(nvd);
						if ("true".equalsIgnoreCase(config.getProperty("logSensitiveInfo", false)))
							CarUtility.locDebug("ERR1134","ASND override of ap.getAttrname() = ap.getAttrvalue() applied");
					}
				}
			}
			DecisionResponse unwrapped = new DecisionResponse();
			DecisionResponseHeader drh = new DecisionResponseHeader();

			try {
				drh.setCarInstanceId("https://"+config.getProperty("car.car.hostname", true)+":"+config.getProperty("car.car.port", true)+"/car");
			} catch (Exception w) {
				drh.setCarInstanceId("https://icm.example.com/car");
			}
			// RGC - 11-21-2018
			if (originalDecisionResponse != null)
			drh.setDecisionId(originalDecisionResponse.getDecisionId());
			unwrapped.setHeader(drh);
			
			unwrapped.setDecisions(alnvd);
			
			ModelAndView resultView = new ModelAndView("reflex");
			
			try {
				// INFO: Here is where we need to add crypto around the decision response 
				//
				
				WrappedDecisionResponse w = new WrappedDecisionResponse();
				w.setDecisionResponse(unwrapped);
				if (! useCrypto) {
					CarUtility.locDebug("LOG1002");
					resultView.addObject("json",new String(Base64.encodeBase64(w.toJson().getBytes())));
				} else {
					CarUtility.locDebug("LOG1003");
					resultView.addObject("json",signAndEncryptToRHAsJWT(new String(Base64.encodeBase64(w.toJson().getBytes())),rhid));
				}
				resultView.addObject("returnUrl",returntourl);
				// debug
				if ("true".equalsIgnoreCase(config.getProperty("logSensitiveInfo", false)))
					CarUtility.locDebug("ERR0808",w.toJson());
			} catch (Exception e) {
				// for now, we don't care about why
				ModelAndView error = new ModelAndView("errorPage");
				error.addObject("message","Failed constructing base64 encoded JSON representation for return: " + e.getMessage());
				error.addObject("intercept_view","1");
				error.addObject("transient","true");
				error.addObject("top_heading",CarUtility.getLocalComponent("top_heading"));
				error.addObject("institutional_logo_url",CarUtility.getLocalComponent("institutional_logo_url"));
				return new ModelAndView("errorPage");
			}
			
			// Immediately before returning the push to the other side, check if updates are required, and
			// if they are, make them.
			// Two updates may be needed:
			//     a merge of these values with user policy from the COPSU via the ICM
			//     an update to the uric endpoint to set "showAgain" for the user/RP pair
			//
			// First, try to perform the policy update if needed
			// Wrap everything in a null try/catch -- this operation is best-effort and non-fatal
			//
			try {
				if (request.getParameter("saveandshow") != null || request.getParameter("saveandhide") != null) {
					// we need to do a merge-save
					// Start by getting the existing COPSU policy
					CarUtility.locDebug("ERR0804");
					UserReturnedPolicy rp = CarUtility.getCOPSUPolicy(username,rhid,rpid,config);
					UserInfoReleasePolicy irp = rp.getUserInfoReleasePolicy();
					// And build a new IRP from the name-value decisions we have for the must and may decisions
					// we started with. 
					// We must avoid processing the nochoice decisions as though they were updates.
					//
					//Start by creating a new policy
					UserInfoReleasePolicy newirp = new UserInfoReleasePolicy();
					// And copying over the relevant metainformation
					newirp.setWhileImAwayDirective(irp.getWhileImAwayDirective());
					newirp.setUserId(irp.getUserId());
					newirp.setRelyingPartyId(irp.getRelyingPartyId());
					newirp.setResourceHolderId(irp.getResourceHolderId());
					newirp.setUserAllOtherInfoReleaseStatement(irp.getUserAllOtherInfoReleaseStatement());
					newirp.setDescription(irp.getDescription());
					
					// Now build the set of actual attribute responses
					ArrayList<UserInfoReleaseStatement> irs = new ArrayList<UserInfoReleaseStatement>();
					
					// and populate it (this is the tricky part)
					// We need to perform some aggregation.  
					HashMap<edu.internet2.consent.icm.model.InfoId,UserInfoReleaseStatement> built = new HashMap<edu.internet2.consent.icm.model.InfoId,UserInfoReleaseStatement>();
					
					for (AttributeValuePair avp : finalDecisions) {
						
						// Special casing for types -- oauth_scope for now
						// carried in os_$name
						// TODO:  Make this less hacky -- this is carried through by 
						// TODO:  insertions in the post-back form for now, but should be 
						// TODO:  refactored to be more intrinsic to the model going forward.
						
						if (request.getParameter("os_"+avp.getAttrname()) != null && request.getParameter("os_"+avp.getAttrname()).equals(avp.getAttrname())) {
							typemap.put(avp.getAttrname(),"oauth_scope");
						}
						
						if (avp.getPolicySource().equals("COPSU")) {
							// this is one we need to mint a decision for
							// check if we already have one
							edu.internet2.consent.icm.model.InfoId ii = new edu.internet2.consent.icm.model.InfoId();
							// TODO:  for now, all info items in this interface are attributes, but... maybe not forever
							// TODO:  figure out how to handle alternative types -- possibly informed content? (yucch)
							// TODO:  possibly prefereble...
							if (typemap.containsKey(avp.getAttrname())) {
								ii.setInfoType(typemap.get(avp.getAttrname()));
								// CarUtility.locError("ERR1134", LogCriticality.error,"(3) " + avp.getAttrname() + " is " + typemap.get(avp.getAttrname()));
							} else {
								ii.setInfoType("attribute");
								// CarUtility.locError("ERR1134", LogCriticality.error,"(3a) " + avp.getAttrname() + " defaulted to attribute");

							}
							
							ii.setInfoValue(avp.getAttrname());
							if (built.containsKey(ii)) {
								// there's already one started -- add to it
								UserInfoReleaseStatement ir = built.get(ii);
								boolean added = false;
								for (UserDirectiveOnValues dov : ir.getArrayOfDirectiveOnValues()) {
									if (dov.getUserReleaseDirective().equals(UserReleaseDirective.valueOf(avp.getCurrentdecision()))) {
										// this is the one we want to add to
										edu.internet2.consent.icm.model.ValueObject vo = new edu.internet2.consent.icm.model.ValueObject();
										vo.setValue(avp.getAttrvalue());
										dov.getValueObjectList().add(vo);
										added = true;
									}
								}
								if (! added) {
									// we need to add a DOV for this
									UserDirectiveOnValues adddov = new UserDirectiveOnValues();
									ArrayList<edu.internet2.consent.icm.model.ValueObject> avo = new ArrayList<edu.internet2.consent.icm.model.ValueObject>();
									edu.internet2.consent.icm.model.ValueObject vo = new edu.internet2.consent.icm.model.ValueObject();
									vo.setValue(avp.getAttrvalue());
									avo.add(vo);
									adddov.setUserReleaseDirective(UserReleaseDirective.valueOf(avp.getCurrentdecision()));
									adddov.setValuesList(avo);
									ir.getArrayOfDirectiveOnValues().add(adddov);
								}
							} else {
								// This is a totally new one
								UserInfoReleaseStatement newirs = new UserInfoReleaseStatement();
								newirs.setPersistence("onChange");  // TODO:  this may need to be variable eventually but we don't currently collect persistence values
								newirs.setInfoId(ii);
								UserDirectiveAllOtherValues daov = new UserDirectiveAllOtherValues();
								daov.setAllOtherValues(edu.internet2.consent.icm.model.AllOtherValuesConst.allOtherValues);
								ReturnedInfoItemMetaInformation riimi = null;
								if (ii.getInfoType() != null) {
									riimi = CarUtility.getInfoItemMetaInformation(rhid, ii.getInfoType(),ii.getInfoValue(), config,httpClient);
								} else {
									riimi = CarUtility.getInfoItemMetaInformation(rhid, ii.getInfoValue(), config,httpClient);
								}
								
								// For PEV, always set the directive on all other values to "askMe"; for PAO, set it to the current setting
								
								if (riimi.getPolicytype().equals("PAO")) {
									daov.setUserReleaseDirective(UserReleaseDirective.valueOf(avp.getCurrentdecision()));
								} else {
									daov.setUserReleaseDirective(UserReleaseDirective.askMe);
								}
								
								newirs.setUserDirectiveAllOtherValues(daov);
								ArrayList<UserDirectiveOnValues> adov = new ArrayList<UserDirectiveOnValues>();
								UserDirectiveOnValues addnewdov = new UserDirectiveOnValues();
								ArrayList<edu.internet2.consent.icm.model.ValueObject> newvo = new ArrayList<edu.internet2.consent.icm.model.ValueObject>();
								edu.internet2.consent.icm.model.ValueObject vo = new edu.internet2.consent.icm.model.ValueObject();
								vo.setValue(avp.getAttrvalue());
								newvo.add(vo);
								addnewdov.setUserReleaseDirective(UserReleaseDirective.valueOf(avp.getCurrentdecision()));
								addnewdov.setValuesList(newvo);
								adov.add(addnewdov);
								newirs.setArrayOfDirectiveOnValues(adov);
								built.put(ii, newirs);
							}
						}
					}
					// and copy from built to the irs
					for (InfoId key : built.keySet()) {
						irs.add(built.get(key));
					}
					// then add the irs to the policy
					newirp.setArrayOfInfoReleaseStatement(irs);
					
					// determine the baseid to update
					String baseid = rp.getPolicyMetaData().getPolicyId().getBaseId();
					
					// and run the update 
					// Since our JDBC driver may be prone to <ahemMySQLahem> random deadlock issues...
					// we set up to retry the policy put up to 5 times before giving up and continuing
					// This is best effort, but if we actually fail the write, we at least ensure we give the user 
					// another shot next time around (as if the user had chosen don't save and show again)
					
					succ = false;
					for (int sc = 0; sc < 5 && !succ; sc++) {
						succ = CarUtility.putCOPSUPolicy(baseid,newirp,config);
						if (!succ)
							CarUtility.locError("ERR0814",String.valueOf(sc));
					}
				}
				// And regardless of that, update showagain for the user accordingly
				// We use usertype and username to do the setting
				if (request.getParameter("saveandshow") != null || request.getParameter("dontsave") != null || ! succ) {
					CarUtility.locDebug("ERR0809","true");
					CarUtility.setShowAgain(usertype,username,rpid,true,config);
				} else {
					CarUtility.locDebug("ERR0809","false");
					CarUtility.setShowAgain(usertype,username,rpid,false,config);
				}
			} catch (Exception ign) {
				// Log and ignore
				CarUtility.locError("ERR0805",CarUtility.exceptionStacktraceToString(ign));
			}
			// and after a best effort at updating, send the response onward
			resultView.addObject("top_heading",CarUtility.getLocalComponent("top_heading"));
			resultView.addObject("sign_out",CarUtility.getLocalComponent("sign_out"));
			resultView.addObject("institutional_logo_url",CarUtility.getLocalComponent("institutional_logo_url"));

			return resultView;
		}
	}
	
	public ModelAndView handleFullDecisionRequest(HttpServletRequest request, boolean useCrypto) {
		
		ModelAndView retval = null;  // populate later
		boolean succ = true;  // tourist information -- we mark writeback as successful if writeback isn't needed
		int convo = 0;
		String sconvo = null;
		boolean askUserForDecisions = false;
		
		//
		// Start by forcing the input into UTF-8
		// Should not matter, but...
		//
		
		try {
			request.setCharacterEncoding("UTF-8");
		} catch (Exception e) {
			CarUtility.locError("ERR1134","Failed to set UTF-8 character encoding on input");
		}
		
		// Perform the necessary initialization
		
		CarConfig config = CarUtility.init(request);
		
		// Marshal the preferred language for interpolating internationalized strings
		String preflang = CarUtility.prefLang(request);
		
		// prepare a typemap for later use
		HashMap<String,String> typemap = new HashMap<String,String>();
		
		// prepare a map of display names for contained IIs (for use later in oauth_scope processing)
		HashMap<String,String> oadn = new HashMap<String,String>();
		// and a map of descriptions to use in case we need them
		HashMap<String,String> oadescr = new HashMap<String,String>();

		// We have two cases, determined by the presence or absence of data in the Session.
		// Get a session (creating a new one if needed)
		
		HttpSession session = request.getSession(true);
		if (session == null) {
			CarUtility.locError("ERR0067");
			retval = new ModelAndView("errorPage");
			retval.addObject("message",CarUtility.getLocalError("ERR0067"));
			retval.addObject("intercept_view","1");
			retval.addObject("transient","true");
			retval.addObject("top_heading",CarUtility.getLocalComponent("top_heading"));
			retval.addObject("institutional_logo_url",CarUtility.getLocalComponent("institutional_logo_url"));
			return retval;
		} 
		session.setMaxInactiveInterval(600);  // 10 minute window for sessions
		
		// Determine if there's an established conversation in the input data
		if (request.getParameter("conversation") != null) {
			convo = Integer.parseInt(request.getParameter("conversation"));
		} else {
			// Determine what the next available conversation number in the session is
			if (session.getAttribute("maxconv") != null) {
				// bump it and add back
				convo = Integer.parseInt((String) session.getAttribute("maxconv")) + 1;
				session.setAttribute("maxconv",String.valueOf(convo));
			} else {
				// no maxconv value -- start from 0 and set it forward
				convo = 0;
				session.setAttribute("maxconv", String.valueOf(convo));
			}
		}

		// now we have convo set, snatch it as a String
		sconvo = String.valueOf(convo);
		
		if (session.getAttribute(sconvo + ":" + "returntourl") == null || session.getAttribute(sconvo + ":" + "icmdecision") == null) {
			// This is the initial case where the input is a POSTd json object from the RH
			// Proceed on that assumption
			// 
			// If we are using crypto, we must first decrypt the input and verify its signature, which is done
			// using a different parser
			FullInputRequest inputRequest = null;
			if (useCrypto) {
				inputRequest = cryptoParseFullInput(request);
			} else {
				inputRequest = parseFullInput(request);
			}
			
			if (inputRequest == null) {
				// failed input processing
				CarUtility.locError("ERR0068");
				ModelAndView r = new ModelAndView("errorPage");
				r.addObject("message",CarUtility.getLocalError("ERR0068"));
				r.addObject("intercept_view","1");
				r.addObject("top_heading",CarUtility.getLocalComponent("top_heading"));
				r.addObject("institutional_logo_url",CarUtility.getLocalComponent("institutional_logo_url"));
				return r;
			} //else {
				// RGC debug
				//try {
				//	CarUtility.locError("ERR1134", LogCriticality.error,"Parsed Input Request: " + inputRequest.toJson());
				//} catch (Exception x) {
				//	CarUtility.locError("ERR1134",  LogCriticality.error,"Unable to serialize parsed input request!");
				//}
			//}
			// We have input -- early-bind some things to the session so we can track our future state
			// Callback Url
			String returntourl = inputRequest.getHeader().getCallbackUrl();
			session.setAttribute(sconvo + ":" + "returntourl",returntourl);
			// RH identifier
			String rhtype = inputRequest.getHeader().getRhType();
			String rhid = inputRequest.getHeader().getRhId();
			session.setAttribute(sconvo + ":" + "rhid",rhid);
			
			// RP identifier
			String rptype = inputRequest.getHeader().getRelyingPartyType();
			String rpid = inputRequest.getHeader().getRelyingPartyId();
			session.setAttribute(sconvo + ":" + "rpid",rpid);
			
			// Compute the user identifier based on attribute value pairings and header
			String usertype = inputRequest.getHeader().getUserIdentifierType();
			String userattr = inputRequest.getHeader().getUserIdentifierName();
			UserId userid = inputRequest.getRequest().getUserId();
			String uservalue = null;
			if (userid != null) {
				uservalue = userid.getUserValue();
			}
			// We will try again after computing the inputAttrMap if necessary
			
			HashMap<String,List<String>> inputAttrMap = new HashMap<String,List<String>>();
			
			// In the full request scenario, instead of an AttributeValuelist array, we 
			// have a full ICM decision request contining a userProperty array and an
			// array of user properties.  Here, we turn the array of user property constructs
			// into the inputAttrMap.
			for (UserProperty up : inputRequest.getRequest().getArrayOfUserProperty()) {
				if (! inputAttrMap.containsKey(up.getUserPropName())) {
					ArrayList<String> ul = new ArrayList<String>();
					ul.add(up.getUserPropValue());
					inputAttrMap.put(up.getUserPropName(),ul);
				} else {
					inputAttrMap.get(up.getUserPropName()).add(up.getUserPropValue());
				}
				if (uservalue == null && up.getUserPropName().equals(userattr)) {
					uservalue = up.getUserPropValue();
				}
			}
			
			if (uservalue == null) {
				CarUtility.locError("ERR0069");
				ModelAndView r = new ModelAndView("errorPage");
				r.addObject("message",CarUtility.getLocalError("ERR0069"));
				r.addObject("intercept_view","1");
				r.addObject("top_heading",CarUtility.getLocalComponent("top_heading"));
				r.addObject("institutional_logo_url",CarUtility.getLocalComponent("institutional_logo_url"));
				return r;
			}
			
			// And store the user value in the session
			session.setAttribute(sconvo + ":" + "username", uservalue);
			session.setAttribute(sconvo + ":" + "usertype", userattr);
			// Now that we have the request information marshalled

			// Retrieve RP metainformation and parse out the RP attribute information
			
			ReturnedRPMetaInformation rpmetainformation = CarUtility.getRPMetaInformation(rhid, rpid, config);
			// Handle unrecognized RP
			boolean unrecRP = false;
			if (rpmetainformation == null || rpmetainformation.getRpproperties() == null) {
				// unrecognized RP
				if ("true".equals(config.getProperty("car.accept_unregistered_rp", false))) {
					rpmetainformation = new ReturnedRPMetaInformation();
					unrecRP = true;
					
					rpmetainformation.setDefaultshowagain("true");
					rpmetainformation.setDescription(null);
					rpmetainformation.setDisplayname(null);
					rpmetainformation.setIconurl(null);
					rpmetainformation.setPrivacyurl(null);
					RHIdentifier rhi = new RHIdentifier();
					rhi.setRhid(rhid);
					rhi.setRhtype(rhtype);
					rpmetainformation.setRhidentifier(rhi);
					RPIdentifier rpi = new RPIdentifier();
					rpi.setRpid(rpid);
					rpi.setRptype(rptype);
					rpmetainformation.setRpidentifier(rpi);
					ReturnedRPProperty rpp = new ReturnedRPProperty();
					rpp.setRppropertyname("entityId");
					rpp.setRppropertyvalue(rpid);
					ArrayList<ReturnedRPProperty> arp = new ArrayList<ReturnedRPProperty>();
					arp.add(rpp);
					rpmetainformation.setRpproperties(arp);
					CarUtility.locLog("ERR0806", rpid);
				} else {
					CarUtility.locError("ERR0806", rpid);
					ModelAndView r = new ModelAndView("errorPage");
					r.addObject("message",CarUtility.getLocalError("ERR0806",rpid));
					r.addObject("page-title","Unrecognized RP");
					r.addObject("intercept_view","1");
					r.addObject("transient","true");
					r.addObject("top_heading",CarUtility.getLocalComponent("top_heading"));
					r.addObject("institutional_logo_url",CarUtility.getLocalComponent("institutional_logo_url"));
					return r;
				}
			}
			ReturnedRPProperty rrrpp = new ReturnedRPProperty();
			String passedRPType = inputRequest.getHeader().getRelyingPartyType();
			if (passedRPType != null && ! passedRPType.equalsIgnoreCase("entityId")) {
				rrrpp.setRppropertyname(passedRPType);
				rrrpp.setRppropertyvalue(rpid);
				rpmetainformation.getRpproperties().add(rrrpp);
			} else {
				rrrpp.setRppropertyname("entityId");  // fake an entityId if there isn't one... 
				rrrpp.setRppropertyvalue(rpid);
			}
			
			rpmetainformation.getRpproperties().add(rrrpp);  // add the entity ID as entityId property
			// Debug
			//try {
				//ObjectMapper m2 = new ObjectMapper();
			//	ObjectMapper m2 = OMSingleton.getInstance().getOm();
			//	CarUtility.locError("ERR0083",LogCriticality.debug, m2.writeValueAsString(rpmetainformation));
			//} catch (Exception e) {
			//	// ignore
			//}
			// Retrieve the required and optional attribute list information as well
			HashMap<String,String> areason = new HashMap<String,String>();
			
			ReturnedRPRequiredInfoItemList irprequirediilist = CarUtility.getRPRequiredIIList(rhid, rpid, config);
			
			ReturnedRPOptionalInfoItemList irpoptionaliilist = CarUtility.getRPOptionalIIList(rhid, rpid, config);

			if (irprequirediilist == null) {
				irprequirediilist = new ReturnedRPRequiredInfoItemList();
				irprequirediilist.setRequiredlist(new ArrayList<InfoItemValueList>());
			}
			if (irpoptionaliilist == null) {
				irpoptionaliilist = new ReturnedRPOptionalInfoItemList();
				irpoptionaliilist.setOptionallist(new ArrayList<InfoItemValueList>());
			}
			// We now need to construct a master attribute/valueset list from the passed-in data and the 
			// required/optional lists that we've obtained.
			// Two considerations are important and non-obvious:
			//    (1) We need to gang values together regardless of whether they are required or optional
			//        -- at this point that isn't relevant
			//    (2) We need to map any source attribute translations along the way, so as to generate 
			//        a desired entry for any attribute that appears in the required or optional list
			//
			
			ArrayList<String> reqiis = new ArrayList<String>();
			
			for (InfoIdPlusValues iipv : inputRequest.getRequest().getArrayOfInfoIdsPlusValues()) {
				if (! reqiis.contains(iipv.getInfoId().getInfoValue())) {
					reqiis.add(iipv.getInfoId().getInfoValue());
				}
			}
			
			// We need to subset the intitial required and optional lists to 
			// only those that are relevant to the input request.  
			
			ReturnedRPRequiredInfoItemList rprequirediilist = new ReturnedRPRequiredInfoItemList();
			rprequirediilist.setRhidentifier(irprequirediilist.getRhidentifier());
			rprequirediilist.setRpidentifier(irprequirediilist.getRpidentifier());
			ArrayList<InfoItemValueList> aliivl = new ArrayList<InfoItemValueList>();
			for (InfoItemValueList iivl : irprequirediilist.getRequiredlist()) {
				if (reqiis.contains(iivl.getInfoitemidentifier().getIiid())) {
					aliivl.add(iivl);
				}
			}
			rprequirediilist.setRequiredlist(aliivl);
			
			ReturnedRPOptionalInfoItemList rpoptionaliilist = new ReturnedRPOptionalInfoItemList();
			rpoptionaliilist.setRhidentifier(irpoptionaliilist.getRhidentifier());
			rpoptionaliilist.setRpidentifier(irpoptionaliilist.getRpidentifier());
			ArrayList<InfoItemValueList> aliivl2 = new ArrayList<InfoItemValueList>();
			for (InfoItemValueList iivl : irpoptionaliilist.getOptionallist()) {
				if (reqiis.contains(iivl.getInfoitemidentifier().getIiid())) {
					aliivl2.add(iivl);
				}
			}
			rpoptionaliilist.setOptionallist(aliivl2);
			
			// Now the required and optional lists are subsetted
			// Use them to generate the desired attribute lists
			
			ArrayList<AttributeValuelist> desiredAttributes = new ArrayList<AttributeValuelist>();
			ArrayList<String> hasvalues = new ArrayList<String>();
			
			
			for (InfoItemValueList iivl : rprequirediilist.getRequiredlist()) {
				
				InfoItemIdentifier iii = iivl.getInfoitemidentifier();  // release identifier
				String sourceAttribute = iivl.getSourceitemname();
				// Set up the reason string
				if (iivl != null && iivl.getInfoitemidentifier() != null && iivl.getReason() != null) {
					areason.put(iivl.getInfoitemidentifier().getIiid(), CarUtility.localize(iivl.getReason(),preflang));
				} else {
					areason.put(iivl.getInfoitemidentifier().getIiid(),"");  // default to no reason
				}
				
				ArrayList<String> mappedValues = new ArrayList<String>();
				if (sourceAttribute != iii.getIiid()) {
					// This is a mapping case -- map the value list from the other attribute
					// TODO:  This is expensive since we don't store a hash -- consider refactoring this later
					// TODO:  I think this may actually be incorrect, as the othervl source probably needs to 
					// iterate over the attributes in input not the required list (or optional list) but for now
					// we'll go with it and verify later -- this may need major rework in a bit.
					
					for (InfoItemValueList othervl : rprequirediilist.getRequiredlist()) {
						if (othervl.getInfoitemidentifier().getIiid().equals(sourceAttribute)) {
							// found it
							// List<String> subset = CarUtility.subsetValueList(othervl.getValuelist(), inputAttrMap.get(othervl.getInfoitemidentifier().getIiid()));
							List<String> subset = CarUtility.subsetValueList(inputAttrMap.get(iivl.getInfoitemidentifier().getIiid()),othervl.getValuelist());
							if (subset != null && ! subset.isEmpty()) {
								mappedValues.addAll(subset);
							}
							break;
						}
					}
					if (mappedValues.isEmpty() && iii.getIitype().equals("oauth_scope")) {
						// mappedValues.add(iii.getIiid() + "_scope");  // placeholder (RGC)
						//
						// Now we make the value map contain the names of the attributes in the scope
						//
						// TODO:  We need to refactor InputRequestHeader to support passing in rhtype as well as rhid and rptype as well as rpid
						// For now we hard-code "entityId"
						RHIdentifier rhi = new RHIdentifier();
						rhi.setRhid(rhid);
						rhi.setRhtype("entityId");
						ScopeMapping sm = CarUtility.getScopeMapping(rhi, iii, config);
						//
						// We use the list of attributes contained as the value *unless*
						// this is a scope without contained attributes, in which case we use 
						// the placeholder value.
						if (sm != null && sm.getInfoitems() != null && ! sm.getInfoitems().isEmpty()) {
							mappedValues.addAll(sm.getInfoitems());
						} else {
							mappedValues.add(iii.getIiid() + "_scope");
						}
						// And populate the oadescr hash
						ReturnedInfoItemMetaInformation riimi = CarUtility.getInfoItemMetaInformation(rhid,"oauth_scope",iii.getIiid(),config);
						if (riimi != null && riimi.getDescription() != null) {
							oadescr.put(iii.getIiid(),CarUtility.localize(riimi.getDescription(), preflang));
						} else {
							oadescr.put(iii.getIiid(),iii.getIiid());
						}
					}
				} else {
					// The simple case
					// List<String> subset = CarUtility.subsetValueList(iivl.getValuelist(), inputAttrMap.get(iivl.getInfoitemidentifier().getIiid()));
					
					List<String> subset = CarUtility.subsetValueList(inputAttrMap.get(iivl.getInfoitemidentifier().getIiid()),iivl.getValuelist());
					if (subset != null && ! subset.isEmpty()) {
						mappedValues.addAll(subset);
					} else if (iii.getIitype().equals("oauth_scope")) {
						// mappedValues.add(iii.getIiid()+"_scope"); // placeholder (RGC)
						// Value map contains the list of attributes contained
						// TODO:  Same:  Need InputRequestHeader to contain types as well as IDs
						// for now, entityId hard-coded
						
						RHIdentifier rhi = new RHIdentifier();
						rhi.setRhid(rhid);
						rhi.setRhtype("entityId");
						ScopeMapping sm = CarUtility.getScopeMapping(rhi, iii,config);
						
						if (sm != null && sm.getInfoitems() != null && ! sm.getInfoitems().isEmpty()) {
							mappedValues.addAll(sm.getInfoitems());
						} else {
							mappedValues.add(iii.getIiid() + "_scope");
						}
						// And populate the oadescr hash
						ReturnedInfoItemMetaInformation riimi = CarUtility.getInfoItemMetaInformation(rhid,"oauth_scope",iii.getIiid(),config);
						if (riimi != null && riimi.getDescription() != null) {
							oadescr.put(iii.getIiid(),CarUtility.localize(riimi.getDescription(), preflang));
						} else {
							oadescr.put(iii.getIiid(),iii.getIiid());
						}

					}
				}
				
				AttributeValuelist al = new AttributeValuelist();
				al.setAttrname(iii.getIiid());
				al.setValues(mappedValues);
				if ((al.getValues() != null && ! al.getValues().isEmpty()) || iii.getIitype().equals("oauth_scope")) {
					desiredAttributes.add(al);			// only if we have values to release
					typemap.put(iii.getIiid(),iii.getIitype());
					// CarUtility.locError("ERR1134", LogCriticality.error,"Adding: " + iii.getIiid() + " = " + mappedValues + " to desired list with type " + iii.getIitype());
				}
				hasvalues.add(iii.getIiid()); // for tracking
			}
			
			// And the somewhat more complicated add of the optional cases
			
			for (InfoItemValueList iivl : rpoptionaliilist.getOptionallist()) {
				InfoItemIdentifier iii = iivl.getInfoitemidentifier();  // release identifier
				String sourceAttribute = iivl.getSourceitemname();
				// Set up the reason string
				if (iivl != null && iivl.getInfoitemidentifier() != null && iivl.getReason() != null) {
					areason.put(iivl.getInfoitemidentifier().getIiid(), CarUtility.localize(iivl.getReason(),preflang));
				} else {
					areason.put(iivl.getInfoitemidentifier().getIiid(),"");  // default to no reason
				}
				
				ArrayList<String> mappedValues = new ArrayList<String>();
				if (sourceAttribute != iii.getIiid()) {
					// This is a mapping case -- map the value list from the other attribute
					// TODO:  This is expensive since we don't store a hash -- consider refactoring this later
					// TODO:  I think this may actually be incorrect, as the othervl source probably needs to 
					// iterate over the attributes in input not the required list (or optional list) but for now
					// we'll go with it and verify later -- this may need major rework in a bit.
					
					for (InfoItemValueList othervl : rpoptionaliilist.getOptionallist()) {
						if (othervl.getInfoitemidentifier().getIiid().equals(sourceAttribute)) {
							// found it
							// List<String> subset = CarUtility.subsetValueList(othervl.getValuelist(), inputAttrMap.get(othervl.getInfoitemidentifier().getIiid()));
							List<String> subset = CarUtility.subsetValueList(inputAttrMap.get(othervl.getInfoitemidentifier().getIiid()),othervl.getValuelist());
							if (subset != null && ! subset.isEmpty()) {
								mappedValues.addAll(subset);
							} else if (iii.getIitype().equals("oauth_scope")) {
								// mappedValues.add(iii.getIiid()+"_scope"); // placeholder (RGC)
								// Value map contains the list of attributes contained
								// TODO:  Same:  Need InputRequestHeader to contain types as well as IDs
								// for now, entityId hard-coded
								
								RHIdentifier rhi = new RHIdentifier();
								rhi.setRhid(rhid);
								rhi.setRhtype("entityId");
								ScopeMapping sm = CarUtility.getScopeMapping(rhi, iii,config);
								
								if (sm != null && sm.getInfoitems() != null && ! sm.getInfoitems().isEmpty()) {
									mappedValues.addAll(sm.getInfoitems());
								} else {
									mappedValues.add(iii.getIiid() + "_scope");
								}
								// And populate the oadescr hash
								ReturnedInfoItemMetaInformation riimi = CarUtility.getInfoItemMetaInformation(rhid,"oauth_scope",iii.getIiid(),config);
								if (riimi != null && riimi.getDescription() != null) {
									oadescr.put(iii.getIiid(),CarUtility.localize(riimi.getDescription(), preflang));
								} else {
									oadescr.put(iii.getIiid(),iii.getIiid());
								}

							}
							break;
						}
					}
				} else {
					// The simple case
					// List<String> subset = CarUtility.subsetValueList(iivl.getValuelist(),inputAttrMap.get(iivl.getInfoitemidentifier().getIiid()));
					List<String> subset = CarUtility.subsetValueList(inputAttrMap.get(iivl.getInfoitemidentifier().getIiid()),iivl.getValuelist());
					if (subset != null && ! subset.isEmpty()) {
						mappedValues.addAll(subset);
					} else if (iii.getIitype().equals("oauth_scope")) {
						// mappedValues.add(iii.getIiid()+"_scope"); // placeholder (RGC)
						// Value map contains the list of attributes contained
						// TODO:  Same:  Need InputRequestHeader to contain types as well as IDs
						// for now, entityId hard-coded
						
						RHIdentifier rhi = new RHIdentifier();
						rhi.setRhid(rhid);
						rhi.setRhtype("entityId");
						ScopeMapping sm = CarUtility.getScopeMapping(rhi, iii,config);
						
						if (sm != null && sm.getInfoitems() != null && ! sm.getInfoitems().isEmpty()) {
							mappedValues.addAll(sm.getInfoitems());
						} else {
							mappedValues.add(iii.getIiid() + "_scope");
						}
						// And populate the oadescr hash
						ReturnedInfoItemMetaInformation riimi = CarUtility.getInfoItemMetaInformation(rhid,"oauth_scope",iii.getIiid(),config);
						if (riimi != null && riimi.getDescription() != null) {
							oadescr.put(iii.getIiid(),CarUtility.localize(riimi.getDescription(), preflang));
						} else {
							oadescr.put(iii.getIiid(),iii.getIiid());
						}

					}
				}
				
				// If the attribute is already present, we need to merge valuelists, otherwise, just 
				// add as is.
				if (hasvalues.contains(iii.getIiid())) {
					// hard case -- merge
					// TODO:  Also vastly inefficient -- hash this later
					//
					for (AttributeValuelist orig : desiredAttributes) {
						if (orig.getAttrname().equals(iii.getIiid())) {
							// this is the merge target
							desiredAttributes.remove(orig);  // remove it first
							List<String> preval = orig.getValues();
							preval.addAll(mappedValues);
							orig.setValues((ArrayList<String>)preval);
							if (orig.getValues() != null && ! orig.getValues().isEmpty()) 
								desiredAttributes.add(orig);    // only if values
							break;
						}
					}
				} else {
					// this is the simpler case				
					AttributeValuelist al = new AttributeValuelist();
					al.setAttrname(iii.getIiid());
					al.setValues(mappedValues);
					if ((al.getValues() != null && ! al.getValues().isEmpty()) || iii.getIitype().equals("oauth_scope")) {
						desiredAttributes.add(al);   // only if values
						typemap.put(iii.getIiid(),iii.getIitype());
						// CarUtility.locError("ERR1134", LogCriticality.error,"Simple adding " + iii.getIiid() + " to desired");
					}
				}
			}
			
			try {
				//ObjectMapper m = new ObjectMapper();
				ObjectMapper m = OMSingleton.getInstance().getOm();
				//CarUtility.locError("ERR0078",LogCriticality.debug, m.writeValueAsString(desiredAttributes));
				if ("true".equalsIgnoreCase(config.getProperty("logSensitiveInfo", false)))
					CarUtility.locDebug("ERR0078", new String(m.writeValueAsBytes(desiredAttributes),StandardCharset.UTF_8));
			} catch (Exception ign) {
				// ignore
			}
			
			// Now we have the desired attribute/valueset filtered to current values list in desiredAttributes.
			// Values are projected from the proper source attributes, and the proper targets are listed.
			// We need to construct the ICM request from the data we have lying around now.
			// TODO:  This too is inefficient -- whole data flow needs to be redesigned at some point for
			// better efficiency given how it has to play out.
			
			DecisionRequestObject dro = new DecisionRequestObject();
			
			// set userId value
			UserId userid2 = new UserId();
			userid2.setUserType(userattr);
			userid2.setUserValue(uservalue);
			dro.setUserId(userid2);
			
			// set relying party Id
			// TODO:  Fixed at entityID identifiers for now...make this mutable later
			
			RelyingPartyId relpar = new RelyingPartyId();
			relpar.setRPtype("entityId");
			relpar.setRPvalue(rpid);
			dro.setRelyingPartyId(relpar);
			
			// Ditto the resource holder
			ResourceHolderId reshol = new ResourceHolderId();
			reshol.setRHType("entityId");
			reshol.setRHValue(rhid);
			dro.setResourceHolderId(reshol);
			
			// Build out the list of infoidplusvalues to match the attributevaluelist we have
			
			ArrayList<InfoIdPlusValues> aiipv = new ArrayList<InfoIdPlusValues>();
			for (AttributeValuelist av : desiredAttributes) {
				// for every desired attribute with values...
				InfoIdPlusValues iipv = new InfoIdPlusValues();
				InfoId ii = new InfoId();
				// TODO:  hard-coding type as "attribute" now, but this needs to change later
				// TODO: somewhat better using input typemap to glean original type...
				//ii.setInfoType("attribute");
				if (typemap.containsKey(av.getAttrname())) {
					ii.setInfoType(typemap.get(av.getAttrname()));
					// CarUtility.locError("ERR1134", LogCriticality.error,"(1) " + av.getAttrname() + " is " + typemap.get(av.getAttrname()));
				} else {
					ii.setInfoType("attribute");
					// CarUtility.locError("ERR1134", LogCriticality.error,"(1a) " + av.getAttrname() + " defaulted to attribute");
				}
				ii.setInfoValue(av.getAttrname());
				iipv.setInfoId(ii);
				iipv.setInfoItemValues(av.getValues());
				aiipv.add(iipv);
			}
			
			dro.setArrayOfInfoIdsPlusValues(aiipv);
			
			// Add the user properties for the icm request (which is basically the full set we have)
			//
			ArrayList<UserProperty> aup = new ArrayList<UserProperty>();
			
			// And a hashmap for dereferencing via oauth_scopes
			
			HashMap<String,ArrayList<String>> ohash = new HashMap<String,ArrayList<String>>();
			
			for (String key : inputAttrMap.keySet()) {
				ArrayList<String> ohinter = new ArrayList<String>();
				for (String values : inputAttrMap.get(key)) {
					UserProperty addu = new UserProperty();
					addu.setUserPropName(key);
					addu.setUserPropValue(values);
					aup.add(addu);
					if (CarUtility.isIIEncoded(rhid,key,config)) {
						ReturnedValueMetaInformation rvmi = CarUtility.getValueMetaInformation(key, values, config);
						if (rvmi != null && rvmi.getDisplayname() != null && ! rvmi.getDisplayname().equals("")) {
							ohinter.add(rvmi.getDisplayname());
						} else {
							ohinter.add(values);
						}
					} else {
						ohinter.add(values);
					}
				}
				ohash.put(key, ohinter); 
				
				// And populate the oadn and oadescr hashes for display use
				if (typemap.get(key) != null) {
					ReturnedInfoItemMetaInformation riimi = CarUtility.getInfoItemMetaInformation(rhid, typemap.get(key), key, config);
					if (riimi != null && riimi.getDisplayname() != null) {
						oadn.put(key, CarUtility.localize(riimi.getDisplayname(),preflang));
					} else {
						oadn.put(key,key);
					}
					if (riimi != null && riimi.getDescription() != null) {
						oadescr.put(key,  CarUtility.localize(riimi.getDescription(),preflang));
					} else {
						oadescr.put(key, key);
					}
				} else {
					ReturnedInfoItemMetaInformation riimi = CarUtility.getInfoItemMetaInformation(rhid, key, config);
					if (riimi != null && riimi.getDisplayname() != null) {
						oadn.put(key, CarUtility.localize(riimi.getDisplayname(),preflang));
					} else {
						oadn.put(key, key);
					}
					if (riimi != null && riimi.getDescription() != null) {
						oadescr.put(key,  CarUtility.localize(riimi.getDescription(),preflang));
					} else {
						oadescr.put(key,key);
					}
				}
			}
			
			dro.setArrayofUserProperty(aup);
			
			// And add the rp properties from the metainfo we got
			ArrayList<RelyingPartyProperty> arpp = new ArrayList<RelyingPartyProperty>();
			// Concurrency protection
			for (ReturnedRPProperty rrpp : new ArrayList<ReturnedRPProperty>(rpmetainformation.getRpproperties())) {
				RelyingPartyProperty rpp = new RelyingPartyProperty();
				rpp.setRpPropName(rrpp.getRppropertyname());
				rpp.setRpPropValue(rrpp.getRppropertyvalue());
				arpp.add(rpp);
			}
			
			dro.setArrayOfRelyingPartyProperty(arpp);
			
			// And convert the request to JSON for passing to the service
			
			//ObjectMapper mapper = new ObjectMapper();
			ObjectMapper mapper = OMSingleton.getInstance().getOm();
			String decisionRequestJson = null;
			try {
				decisionRequestJson = mapper.writeValueAsString(dro);
			} catch (Exception e) {
				ModelAndView r = new ModelAndView("errorPage");
				CarUtility.locError("ERR0081", "#1");
				r.addObject("messsage",CarUtility.getLocalError("ERR0016"));
				r.addObject("intercept_view","1");
				r.addObject("transient","true");
				r.addObject("top_heading",CarUtility.getLocalComponent("top_heading"));
				r.addObject("institutional_logo_url",CarUtility.getLocalComponent("institutional_logo_url"));
				return r;
			}
			//TODO: change back to info instead of error
			// CarUtility.locError("ERR0082",LogCriticality.error, decisionRequestJson);
			// And send it out to get back a response
			
			IcmDecisionResponseObject response = CarUtility.sendDecisionRequest(decisionRequestJson,config);
			//TODO: change back to info instead of error
			if ("true".equalsIgnoreCase(config.getProperty("logSensitiveInfo", false)))
				CarUtility.locDebug("ERR0077", decisionRequestJson);
			
			// Check for bad ICM response
			if (response == null) {
				ModelAndView r = new ModelAndView("errorPage");
				CarUtility.locError("ERR0802");
				r.addObject("message",CarUtility.getLocalError("ERR0802"));
				r.addObject("intercept_view","1");
				r.addObject("transient","true");
				r.addObject("top_heading",CarUtility.getLocalComponent("top_heading"));
				r.addObject("institutional_logo_url",CarUtility.getLocalComponent("institutional_logo_url"));
			} else {
				try {
					//ObjectMapper tmapper = new ObjectMapper();
					ObjectMapper tmapper = OMSingleton.getInstance().getOm();
					String m = tmapper.writeValueAsString(response);
					//TODO: log this at debug not error
					if ("true".equalsIgnoreCase(config.getProperty("logSensitiveInfo", false)))
						CarUtility.locDebug("ERR0803", m);
				} catch (Exception e) {
					// ignore -- best effort logging
				}
			}

			// We now have an IcmDecisionResponse object.  Save it in the session.
			session.setAttribute(sconvo + ":" + "icmdecision", response);   // save the current decision response as is.
			try {
				if ("true".equalsIgnoreCase(config.getProperty("logSensitiveInfo", false)))
					CarUtility.locDebug("ERR0813", response.toJSON());
			} catch(Exception e) {
				// ignore
			}
			// And determine whether to display the dialog or not
			askUserForDecisions = false; // only ask if the criteria are met
			// Get User metainformation
			ReturnedUserRPMetaInformation urpmi = CarUtility.getUserRPMetaInformation(rpid, userattr, uservalue, config);
			//
			// Now, rely on an optional config and an optional per-RP for askUserForDecisions value if urpmi == null here
			boolean defaultShowAgain = false;
			if (config.getProperty("car.default.showAgain", false) == null || config.getProperty("car.default.showAgain",false).equalsIgnoreCase("true")) {
				defaultShowAgain = true;
			}
			if (rpmetainformation.getDefaultshowagain() != null) {
				// if it is set, it is explicit
				if (rpmetainformation.getDefaultshowagain().equalsIgnoreCase("true")) {
					defaultShowAgain = true;
				} else {
					defaultShowAgain = false;
				}
			}
			// if RP default is set to "never", override user setting and always default to car.default.showAgain value (usually false)
			if ((urpmi==null || urpmi.isShowagain()) && (rpmetainformation.getDefaultshowagain() == null || ! rpmetainformation.getDefaultshowagain().equalsIgnoreCase("never"))) {
				if ("true".equalsIgnoreCase(config.getProperty("logSensitiveInfo", false)))
					CarUtility.locDebug("ERR1117","User " + uservalue + " has showAgain = true");
				askUserForDecisions = true;   // they said to show them the page again or they've never made a decision so we show on first attempt
			}
			boolean haschoices = false; // no choices unless we find some
			boolean todisplay = false;  // no display unless something drives it -- RGC 11-21-2018
			// Also show if there's anything marked askMe that's not ASND in the response
			if (response != null && response.getArrayOfInfoDecisionStatement() != null) {
				for (IcmDecisionsForInfoStatement idfis : response.getArrayOfInfoDecisionStatement()) {
					for (IcmDecisionOnValues idov : idfis.getArrayOfDecisionOnValues()) {
						if (idov.getReleaseDecision().equals(UserReleaseDirective.askMe)) {
							ReturnedInfoItemMetaInformation riimi = null;
							if (idfis.getInfoId().getInfoType() != null) {
								riimi = CarUtility.getInfoItemMetaInformation(rhid, idfis.getInfoId().getInfoType(), idfis.getInfoId().getInfoValue(), config);
							} else {
								riimi = CarUtility.getInfoItemMetaInformation(rhid,  idfis.getInfoId().getInfoValue(), config);
							}
							if (riimi != null && ! riimi.isAsnd()) {
								askUserForDecisions = true;
								todisplay = true;  // RGC - 11-21-2018
								haschoices = true;
								break;
							}
						} else { // RGC - 11-21-2018
							ReturnedInfoItemMetaInformation riimi = null;
							if (idfis.getInfoId().getInfoType() != null) {
								riimi = CarUtility.getInfoItemMetaInformation(rhid,  idfis.getInfoId().getInfoType(), idfis.getInfoId().getInfoValue(), config);
							} else {
								riimi = CarUtility.getInfoItemMetaInformation(rhid,  idfis.getInfoId().getInfoValue(),config);
							}
							if (riimi != null && ! riimi.isAsnd()) {
								todisplay = true;  
							}
						}
					}
					if (haschoices) {
						break;
					}
				}
			}
			// RGC - 11-21-2018
			if (askUserForDecisions && ! todisplay) {
				CarUtility.locDebug("ERR1134","Setting askUserForDecisions to false because todisplay is false");
				askUserForDecisions = false;
			}
			
			CarUtility.locDebug("ERR1134","askUserForDecisions is " + askUserForDecisions + " and todisplay is " + todisplay);
			session.setAttribute(sconvo + ":" + "askUserForDecisions", askUserForDecisions);
			
			
			// now we know whether to mint the response page or the pass-thru redirect.
			
			// Here we play a slightly sick game.  This block is executing because we originally were on the 
			// first leg of a request process -- we had no session information stored.
			// If we need to provide a UI for collecting more info, we do so now by returning the 
			// ModelAndView populated with the relevant injections.
			// If we do not need to provide a UI, we simply continue to the next phase of the process. 
			// In the next phase of the process. we check whether the session indicates we asked the user
			// for responses -- if we did, we collect them (since we know they must be present) and process
			// them before continuing.  If we didn't, we simply continue on down the process.
			
			// For debugging purposes, until the real UI is ready, we use a simple form to process
			// things.
			// while we're at it, we need to determine what if any "may" choices (controlling COPSU decisions) the
			// user has *if* we're in askUserForDecisions mode.  If we're not, it's irrelevant (since the UI won't
			// be forged anyway).  Likewise, if we are, we need to determine what if any "nochoice" options there 
			// are and prepare those for injecting to the UI as well.
			// The UI does not expose "recommended" values for "may" and "nochoice" options -- once the user makes a 
			// decision, we assume the user has evaluated the 
			
			if (askUserForDecisions) {
				// Mint and deliver the response form instead of moving on
				// Currently the debug version, but we'll change all that...
				
				// Establish a CSRF context for this conversation
				String csrftoken = generateCSRFToken();
				// And store it in the session early
				session.setAttribute(sconvo + ":" + "csrftoken",csrftoken);
				// We first have to retrieve the relevant decision from the ARPSI in order to get the 
				// consent service recommendations to use.  Mint a DecisionRequest that only affects the 
				// attribute/value pairs that are marked as "askMe" in the current response.
				
				DecisionRequestObject deco = new DecisionRequestObject();
				ArrayList<AttributeValuePair> aav = new ArrayList<AttributeValuePair>();
				// -rgc- ArrayList<String> significant = new ArrayList<String>();
				HashMap<String,ArrayList<String>> significant = new HashMap<String,ArrayList<String>>(); // askMe decisions
				if (response != null && response.getArrayOfInfoDecisionStatement() != null) {
					for (IcmDecisionsForInfoStatement idfis : response.getArrayOfInfoDecisionStatement()) {
						for (IcmDecisionOnValues idov : idfis.getArrayOfDecisionOnValues()) {
							//
							if (idov.getReleaseDecision().equals(UserReleaseDirective.askMe) ) {
								// this is one we care about
								for (String value : idov.getReturnedValuesList()) {
									AttributeValuePair ap = new AttributeValuePair();
									ap.setAttrname(idfis.getInfoId().getInfoValue());
									ap.setAttrvalue(value);
									aav.add(ap);
									// -rgc -if (! significant.contains(idfis.getInfoId().getInfoValue()))
									// -rgc-		significant.add(idfis.getInfoId().getInfoValue());
									if (!significant.containsKey(idfis.getInfoId().getInfoValue())) {
										ArrayList<String> al = new ArrayList<String>();
										significant.put(idfis.getInfoId().getInfoValue(), al);
									} 
									significant.get(idfis.getInfoId().getInfoValue()).add(value);
								}
							}
						}
					}
				}
				UserId u = new UserId();
				u.setUserType(userattr);
				u.setUserValue(uservalue);
				deco.setUserId(u);
				
				ResourceHolderId rhi = new ResourceHolderId();
				rhi.setRHType("entityId");
				rhi.setRHValue(rhid);
				deco.setResourceHolderId(rhi);
				
				RelyingPartyId rpi = new RelyingPartyId();
				rpi.setRPtype("entityId");
				rpi.setRPvalue(rpid);
				deco.setRelyingPartyId(rpi);
				
				ArrayList<InfoIdPlusValues> aiipv2 = new ArrayList<InfoIdPlusValues>();
				for (AttributeValuelist av : desiredAttributes) {
					// for every desired attribute with values...
					InfoIdPlusValues iipv = new InfoIdPlusValues();
					InfoId ii = new InfoId();
					// TODO:  hard-coding type as "attribute" now, but this needs to change later
					// TODO: somewhat better now...
					// ii.setInfoType("attribute");
					if (typemap.containsKey(av.getAttrname())) {
						ii.setInfoType(typemap.get(av.getAttrname()));
						// CarUtility.locError("ERR1134", LogCriticality.error,"(2) " + av.getAttrname() + " is " + typemap.get(av.getAttrname()));

					} else {
						ii.setInfoType("attribute");  // still default
						// CarUtility.locError("ERR1134", LogCriticality.error,"(2a) " + av.getAttrname() + " defaulted to attribute");

					}
					ii.setInfoValue(av.getAttrname());
					iipv.setInfoId(ii);
					iipv.setInfoItemValues(av.getValues());
					aiipv2.add(iipv);
				}
				
				deco.setArrayOfInfoIdsPlusValues(aiipv2);
				
				ArrayList<UserProperty> aup2 = new ArrayList<UserProperty>();
				for (String key : inputAttrMap.keySet()) {
					for (String values : inputAttrMap.get(key)) {
						UserProperty addu = new UserProperty();
						addu.setUserPropName(key);
						addu.setUserPropValue(values);
						aup2.add(addu);
					}
				}
				
				deco.setArrayofUserProperty(aup2);
				
				// And add the rp properties from the metainfo we got
				ArrayList<RelyingPartyProperty> arpp2 = new ArrayList<RelyingPartyProperty>();
				//Concurrency protection
				for (ReturnedRPProperty rrpp : new ArrayList<ReturnedRPProperty>(rpmetainformation.getRpproperties())) {
					RelyingPartyProperty rpp = new RelyingPartyProperty();
					rpp.setRpPropName(rrpp.getRppropertyname());
					rpp.setRpPropValue(rrpp.getRppropertyvalue());
					arpp2.add(rpp);
				}
				
				deco.setArrayOfRelyingPartyProperty(arpp2);
				
				//ObjectMapper mapper2 = new ObjectMapper();
				ObjectMapper mapper2 = OMSingleton.getInstance().getOm();
				String decisionRequestJson2 = null;
				try {
					decisionRequestJson2 = mapper2.writeValueAsString(dro);
				} catch (Exception e) {
					ModelAndView r = new ModelAndView("errorPage");
					CarUtility.locDebug("ERR0081","#1");
					r.addObject("messsage",CarUtility.getLocalError("ERR0016"));
					r.addObject("intercept_view","1");
					r.addObject("transient","true");
					r.addObject("top_heading",CarUtility.getLocalComponent("top_heading"));
					r.addObject("institutional_logo_url",CarUtility.getLocalComponent("institutional_logo_url"));
					return r;
				}
				if ("true".equalsIgnoreCase(config.getProperty("logSensitiveInfo", false)))
					CarUtility.locDebug("ERR0084",decisionRequestJson2);
				edu.internet2.consent.arpsi.model.DecisionResponseObject arpsiResponse = CarUtility.sendARPSIDecisionRequest(decisionRequestJson2, config);
				boolean hasMustDecisions = false;
				ArrayList<InjectedDecision> injectedDecisions = new ArrayList<InjectedDecision>();
				// -rgc- for (String aid : significant) {
				for (String aid : significant.keySet()) {
					// get info item metainformation
					ReturnedInfoItemMetaInformation riimi = null;
					if (typemap.get(aid) != null) {
						riimi = CarUtility.getInfoItemMetaInformation(rhid, typemap.get(aid), aid, config);
					} else {
						riimi =	CarUtility.getInfoItemMetaInformation(rhid,  aid, config);
					}
					String attrDisplayName = null;
					
					if (riimi != null && riimi.getDisplayname() != null) {
						String localized = CarUtility.localize(riimi.getDisplayname(), preflang);
						CarUtility.locDebug("ERR0085",aid,localized);
						attrDisplayName = localized;
					} else {
						CarUtility.locDebug("ERR0086",aid);
						attrDisplayName=aid;
					}
					
					/* Deprecated
					if (riimi != null && riimi.getDisplayname() != null && riimi.getDisplayname().getLocales() != null && ! riimi.getDisplayname().getLocales().isEmpty()) {
						CarUtility.locError("ERR0085",LogCriticality.debug,aid,riimi.getDisplayname().getLocales().get(0).getValue());;
						attrDisplayName = riimi.getDisplayname().getLocales().get(0).getValue();
					} else {
						CarUtility.locError("ERR0086",LogCriticality.info,aid);
						attrDisplayName = aid;
					}
					*/
					
					for (DecisionsForInfoStatement ids : arpsiResponse.getArrayOfInfoDecisionStatement()) {
						if (ids.getInfoId().getInfoValue().equals(aid)) {
							// this is a significant one
							for (DecisionOnValues dov : ids.getArrayOfDecisionOnValues()) {
								for (String v : dov.getReturnedValuesList()) {
									// -rgc- ignore non-askMe
									if (significant.get(aid).contains(v)){
									InjectedDecision id = new InjectedDecision();
									id.setAttrName(aid);  
									id.setAttrDisplayName(attrDisplayName);
									id.setAttrValue(v);
									ReturnedValueMetaInformation rvmi = null;
									if (CarUtility.isIIEncoded(rhid,id.getAttrName(),config))
											rvmi = CarUtility.getValueMetaInformation(id.getAttrName(),id.getAttrValue(),config);
									
									// debug
									//if (riimi == null) {
									//	CarUtility.locError("ERR1134", LogCriticality.error, "RIIMI null trap - type is " + aid + " of type " + typemap.get(aid));
									// }
									if (riimi != null && riimi.getPresentationtype() != null && riimi.getPresentationtype().equals("ENCODED")) {
										//ReturnedValueMetaInformation rvmi = CarUtility.getValueMetaInformation(id.getAttrName(),id.getAttrValue(),config);
										if (rvmi != null && rvmi.getDisplayname() != null) {
											id.setAttrDisplayValue(rvmi.getDisplayname());
										} else {
											id.setAttrDisplayValue(id.getAttrValue());
										}
									} else {
										id.setAttrDisplayValue(id.getAttrValue());
									}
									id.setRecommendedDirective(dov.getReleaseDecision().toString());
									id.setChosenDirective(dov.getReleaseDecision().toString());
									if (riimi != null)
										id.setSensitivity(riimi.isSensitivity());
									if ((riimi != null && riimi.isAsnd()) || (rvmi != null && rvmi.getAsnd())) {
										id.setAsnd(true);
									} else {
										id.setAsnd(false);
									}
									if (riimi != null)
										id.setPolicytype(riimi.getPolicytype());
									if ((riimi != null && riimi.isAsnd()) || (rvmi != null && rvmi.getAsnd())) {
										// override to PERMIT for Asnd
										id.setChosenDirective("permit");
										id.setRecommendedDirective("permit");
									}
									if (! injectedDecisions.contains(id)) {  // duplicate suppression
										injectedDecisions.add(id);
									}
									// if this is Asnd, it doesn't create the need for hasMust
									if ((riimi == null || ! riimi.isAsnd()) && (rvmi == null || ! rvmi.getAsnd()))
										hasMustDecisions = true;
									}
								}
							}
						}
					}
				}
				
				// Here, we compute a set of "may" decisions in mayDecisions for injection into the UI
				// "may" decisions are values in the response.getArrayOfInfoDecisionStatement()
				// that are presented as either "permit" or "deny" rather than "askMe"
				// NOTE:  ICM handles useAdvice directly, now, so useAdvice never makes it to this level
				
				
				ArrayList<InjectedDecision> mayDecisions = new ArrayList<InjectedDecision>();
				ArrayList<InjectedDecision> nochoiceDecisions = new ArrayList<InjectedDecision>();
				boolean hasMay = false;
				boolean hasNoChoice = false;
				
				if (response != null && response.getArrayOfInfoDecisionStatement() != null) {
					for (IcmDecisionsForInfoStatement idfis : response.getArrayOfInfoDecisionStatement()) {
						for (IcmDecisionOnValues idov : idfis.getArrayOfDecisionOnValues()) {
							String attrDisplayName=null;
							ReturnedInfoItemMetaInformation riimi = null;
							if (idfis.getInfoId().getInfoType() != null) {
								riimi = CarUtility.getInfoItemMetaInformation(rhid, idfis.getInfoId().getInfoType(),idfis.getInfoId().getInfoValue(), config);
							} else {
								riimi = CarUtility.getInfoItemMetaInformation(rhid,  idfis.getInfoId().getInfoValue(), config);
							}
							if (riimi != null && riimi.getDisplayname() != null) {
								String localized = CarUtility.localize(riimi.getDisplayname(),preflang);
								CarUtility.locDebug("ERR0085",idfis.getInfoId().getInfoValue(),localized);
								attrDisplayName = localized;
							} else {
								CarUtility.locDebug("ERR0086",idfis.getInfoId().getInfoValue());
								attrDisplayName = idfis.getInfoId().getInfoValue();
							}
							
							/* Deprecated
							if (riimi != null && riimi.getDisplayname() != null && riimi.getDisplayname().getLocales() != null && ! riimi.getDisplayname().getLocales().isEmpty()) {
								CarUtility.locError("ERR0085",LogCriticality.debug,idfis.getInfoId().getInfoValue(),riimi.getDisplayname().getLocales().get(0).getValue());;
								attrDisplayName = riimi.getDisplayname().getLocales().get(0).getValue();
							} else {
								CarUtility.locError("ERR0086",LogCriticality.debug,idfis.getInfoId().getInfoValue());
								attrDisplayName = idfis.getInfoId().getInfoValue();
							}
							*/
							
							if (idov.getReleaseDecision().equals(UserReleaseDirective.permit) || idov.getReleaseDecision().equals(UserReleaseDirective.deny)) {
								// this is a may or a nochoice case we care about
								for (String value : idov.getReturnedValuesList()) {
									InjectedDecision ap = new InjectedDecision();
									ap.setAttrName(idfis.getInfoId().getInfoValue());
									ap.setAttrValue(value);
									ReturnedValueMetaInformation rvmi = null;
									if (riimi != null && "ENCODED".equalsIgnoreCase(riimi.getPresentationtype()))
										rvmi = CarUtility.getValueMetaInformation(ap.getAttrName(),ap.getAttrValue(),config);
									if (riimi != null && riimi.getPresentationtype() != null && riimi.getPresentationtype().equals("ENCODED")) {
										// ReturnedValueMetaInformation rvmi = CarUtility.getValueMetaInformation(ap.getAttrName(),ap.getAttrValue(),config);
										if (rvmi != null && rvmi.getDisplayname() != null) {
											ap.setAttrDisplayValue(rvmi.getDisplayname());
										} else {
											ap.setAttrDisplayValue(ap.getAttrValue());
										}
									} else {
										ap.setAttrDisplayValue(ap.getAttrValue());
									}
									ap.setAttrDisplayName(attrDisplayName);
									ap.setChosenDirective(idov.getReleaseDecision().toString());
									if (riimi != null)
									ap.setSensitivity(riimi.isSensitivity());
									if (riimi != null && (riimi.isAsnd() || (rvmi != null && rvmi.getAsnd()))) {
										ap.setAsnd(true);
									} else {
										ap.setAsnd(false);
									}
									if (riimi != null)
									ap.setPolicytype(riimi.getPolicytype());
									if (riimi != null && (riimi.isAsnd() || (rvmi != null && rvmi.getAsnd()))) {
										// override chosen directive to PERMIT if Asnd is set on
										ap.setChosenDirective("permit");
										ap.setRecommendedDirective("permit");
									}
									
									if (idov.getAugmentedPolicyId().getPolicySource().equals(PolicySourceEnum.COPSU) && ! mayDecisions.contains(ap)) { // duplicate suppression
										mayDecisions.add(ap);
										// Suppress update of hasMay if this is ASND
										if (riimi == null || (!riimi.isAsnd() && (rvmi == null || ! rvmi.getAsnd())))
											hasMay = true;
									} else if (! nochoiceDecisions.contains(ap) && ! mayDecisions.contains(ap)){ // duplicate suppression
										nochoiceDecisions.add(ap);
										// Suppress update of hasNoChoice if this is ASND
										if (riimi == null || (!riimi.isAsnd() && (rvmi == null || ! rvmi.getAsnd())))
											hasNoChoice = true;
									}
								}
							}
						}
					}
				}
				boolean hasUserChoices = hasMay || hasMustDecisions;
				
				HashMap<String,ArrayList<String>> permitInjected = new HashMap<String,ArrayList<String>>();
				HashMap<String,ArrayList<String>> denyInjected = new HashMap<String,ArrayList<String>>();
				HashMap<String,ArrayList<String>> permitMay = new HashMap<String,ArrayList<String>>();
				HashMap<String,ArrayList<String>> denyMay = new HashMap<String,ArrayList<String>>();
				HashMap<String,ArrayList<String>> permitNo = new HashMap<String,ArrayList<String>>();
				
				HashMap<String,String> sensitivity = new HashMap<String,String>();
				HashMap<String,String> asnd = new HashMap<String,String>();
				
				HashMap<String,String> policytype = new HashMap<String,String>();
				
				HashMap<String,ArrayList<String>> valuesets = new HashMap<String,ArrayList<String>>();
				HashMap<String,ArrayList<String>> displayvaluesets = new HashMap<String,ArrayList<String>>();
				
				for (InjectedDecision id : injectedDecisions) {
					// maintain valuesets and displayvaluesets as well
					if (! valuesets.containsKey(id.getAttrDisplayName())) {
						valuesets.put(id.getAttrDisplayName(), new ArrayList<String>());
						valuesets.get(id.getAttrDisplayName()).add(id.getAttrValue());
						displayvaluesets.put(id.getAttrDisplayName(), new ArrayList<String>());
						displayvaluesets.get(id.getAttrDisplayName()).add(id.getAttrDisplayValue());
					} else {
						valuesets.get(id.getAttrDisplayName()).add(id.getAttrValue());
						displayvaluesets.get(id.getAttrDisplayName()).add(id.getAttrDisplayValue());
					}
					if (! sensitivity.containsKey(id.getAttrDisplayName()) && id.isSensitivity()) {
						sensitivity.put(id.getAttrDisplayName(), "true");
					}
					if (! asnd.containsKey(id.getAttrDisplayName()) && id.isAsnd()) {
						asnd.put(id.getAttrDisplayName(),"true");
					}
					if (! policytype.containsKey(id.getAttrDisplayName()) && id.getPolicytype() != null) {
						policytype.put(id.getAttrDisplayName(),id.getPolicytype());
					}
					if (id.getChosenDirective().equalsIgnoreCase("permit")) {
						if (! permitInjected.containsKey(id.getAttrDisplayName())) {
							ArrayList<String> a = new ArrayList<String>();
							permitInjected.put(id.getAttrDisplayName(),a);
						}
						permitInjected.get(id.getAttrDisplayName()).add(id.getAttrDisplayValue());
					} else {
						if (! denyInjected.containsKey(id.getAttrDisplayName())) {
							ArrayList<String> a = new ArrayList<String>();
							denyInjected.put(id.getAttrDisplayName(), a);
						}
						denyInjected.get(id.getAttrDisplayName()).add(id.getAttrDisplayValue());
					}
				}
				for (InjectedDecision id : nochoiceDecisions) {
					// maintain valuesets and displayvaluesets as well
					if (! valuesets.containsKey(id.getAttrDisplayName())) {
						valuesets.put(id.getAttrDisplayName(), new ArrayList<String>());
						valuesets.get(id.getAttrDisplayName()).add(id.getAttrValue());
						displayvaluesets.put(id.getAttrDisplayName(), new ArrayList<String>());
						displayvaluesets.get(id.getAttrDisplayName()).add(id.getAttrDisplayValue());
					} else {
						valuesets.get(id.getAttrDisplayName()).add(id.getAttrValue());
						displayvaluesets.get(id.getAttrDisplayName()).add(id.getAttrDisplayValue());
					}
					if (! sensitivity.containsKey(id.getAttrDisplayName()) && id.isSensitivity()) {
						sensitivity.put(id.getAttrDisplayName(), "true");
					}
					if (! asnd.containsKey(id.getAttrDisplayName()) && id.isAsnd()) {
						asnd.put(id.getAttrDisplayName(), "true");
					}
					if (! policytype.containsKey(id.getAttrDisplayName()) && id.getPolicytype() != null) {
						policytype.put(id.getAttrDisplayName(),id.getPolicytype());
					}
					if (id.getChosenDirective().equalsIgnoreCase("permit")) {
						if (! permitNo.containsKey(id.getAttrDisplayName())) {
							ArrayList<String> a = new ArrayList<String>();
							permitNo.put(id.getAttrDisplayName(),a);
						}
						permitNo.get(id.getAttrDisplayName()).add(id.getAttrDisplayValue());
					} 
				}
				
				for (InjectedDecision id : mayDecisions) {
					// maintain valuesets and displayvaluesets as well
					if (! valuesets.containsKey(id.getAttrDisplayName())) {
						valuesets.put(id.getAttrDisplayName(), new ArrayList<String>());
						valuesets.get(id.getAttrDisplayName()).add(id.getAttrValue());
						displayvaluesets.put(id.getAttrDisplayName(), new ArrayList<String>());
						displayvaluesets.get(id.getAttrDisplayName()).add(id.getAttrDisplayValue());
					} else {
						valuesets.get(id.getAttrDisplayName()).add(id.getAttrValue());
						displayvaluesets.get(id.getAttrDisplayName()).add(id.getAttrDisplayValue());
					}
					if (! sensitivity.containsKey(id.getAttrDisplayName()) && id.isSensitivity()) {
						sensitivity.put(id.getAttrDisplayName(), "true");
					}
					if (! asnd.containsKey(id.getAttrDisplayName()) && id.isAsnd()) {
						asnd.put(id.getAttrDisplayName(),"true");
					}
					if (! policytype.containsKey(id.getAttrDisplayName()) && id.getPolicytype() != null) {
						policytype.put(id.getAttrDisplayName(),id.getPolicytype());
					}
					if (id.getChosenDirective().equalsIgnoreCase("permit")) {
						if (! permitMay.containsKey(id.getAttrDisplayName())) {
							ArrayList<String> a = new ArrayList<String>();
							permitMay.put(id.getAttrDisplayName(),a);
						}
						permitMay.get(id.getAttrDisplayName()).add(id.getAttrDisplayValue());
					} else {
						if (! denyMay.containsKey(id.getAttrDisplayName())) {
							ArrayList<String> a = new ArrayList<String>();
							denyMay.put(id.getAttrDisplayName(), a);
						}
						denyMay.get(id.getAttrDisplayName()).add(id.getAttrDisplayValue());
					}
				}


				// Marshal the RH info for display purposes
				ArrayList<ReturnedRHMetaInformation> arhmi = CarUtility.getRHMetaInformation(config);
				String rhdisplayname="";
				for (ReturnedRHMetaInformation mi : arhmi) {
					if (mi.getRhidentifier().getRhid().contentEquals(rpmetainformation.getRhidentifier().getRhid())) {
						rhdisplayname = CarUtility.localize(mi.getDisplayname(),preflang);
					}
				}
				
				ModelAndView debugReturn = new ModelAndView("intercept");

				
				if (! useCrypto) {
					debugReturn.addObject("actionUrl",CarUtility.interceptUrl(config)+"?conversation="+sconvo);
				} else {
					debugReturn.addObject("actionUrl",CarUtility.cryptoInterceptUrl(config)+"?conversation="+sconvo);
				}

				
				Collections.sort(injectedDecisions);
				debugReturn.addObject("injectedDecisions",injectedDecisions);
				Collections.sort(mayDecisions);
				debugReturn.addObject("injectedMayDecisions",mayDecisions);
				Collections.sort(nochoiceDecisions);
				debugReturn.addObject("injectedNochoiceDecisions",nochoiceDecisions);
				//ObjectMapper omapper = new ObjectMapper();
				ObjectMapper omapper = OMSingleton.getInstance().getOm();
				try {
					debugReturn.addObject("sensitivity",omapper.writeValueAsString(sensitivity));
					CarUtility.locDebug("ERR1112",omapper.writeValueAsString(sensitivity));
				} catch (Exception ign) {
					CarUtility.locDebug("ERR1113",sensitivity.toString());
					debugReturn.addObject("sensitivity","");
				}
				try {
					debugReturn.addObject("asnd",omapper.writeValueAsString(asnd));
					CarUtility.locDebug("ERR1114", omapper.writeValueAsString(asnd));
				} catch (Exception ign) {
					CarUtility.locDebug("ERR1115",asnd.toString());
					debugReturn.addObject("asnd","");
				}
				try {
					debugReturn.addObject("policytype",omapper.writeValueAsString(policytype));
					CarUtility.locDebug("ERR1119",omapper.writeValueAsString(policytype));
				} catch (Exception ign) {
					CarUtility.locDebug("ERR1120", policytype.toString());
					debugReturn.addObject("policytype","");
				}
				try {
					debugReturn.addObject("valuesets",omapper.writeValueAsString(valuesets));
					debugReturn.addObject("displayvaluesets",omapper.writeValueAsString(displayvaluesets));
				} catch (Exception ign) {
					debugReturn.addObject("valuesets","");
					debugReturn.addObject("displayvaluesets","");
				}
				try {
					debugReturn.addObject("permitInjected",omapper.writeValueAsString(permitInjected));
				} catch (Exception ign) {
					debugReturn.addObject("permitInjected","");
				}
				try {
					debugReturn.addObject("denyInjected",omapper.writeValueAsString(denyInjected));
				} catch (Exception ign) {
					debugReturn.addObject("denyInjected","");
				}
				try {
					debugReturn.addObject("permitMay",omapper.writeValueAsString(permitMay));
				} catch (Exception ign) {
					debugReturn.addObject("permitMay","");
				}
				try {
					debugReturn.addObject("denyMay",omapper.writeValueAsString(denyMay));
				} catch (Exception ign) {
					debugReturn.addObject("denyMay","");
				}
				try {
					debugReturn.addObject("permitNo",omapper.writeValueAsString(permitNo));
				} catch (Exception ign) {
					debugReturn.addObject("permitNo","");
				}
				try {
					debugReturn.addObject("ohashjs",omapper.writeValueAsString(ohash));
				} catch (Exception ign) {
					debugReturn.addObject("ohashjs","");
				}
				try {
					debugReturn.addObject("oadnjs",omapper.writeValueAsString(oadn));
				} catch (Exception ign) {
					debugReturn.addObject("oadnjs","");
				}
				try {
					debugReturn.addObject("oadescrjs",omapper.writeValueAsString(oadescr));
				} catch (Exception ign) {
					debugReturn.addObject("oadescrjs","");
				}
				
				// Optimize for reuse
				String locrpdisp = rpmetainformation.getRpidentifier().getRpid();
				String locrpdesc = rpmetainformation.getRpidentifier().getRpid();
				if (rpmetainformation.getDisplayname() != null)
					locrpdisp = CarUtility.localize(rpmetainformation.getDisplayname(),preflang);
				if (rpmetainformation.getDescription() != null)
					locrpdesc = CarUtility.localize(rpmetainformation.getDescription(),preflang);
				
				debugReturn.addObject("hasMay",hasMay);
				debugReturn.addObject("hasMustDecisions",hasMustDecisions);
				debugReturn.addObject("hasNoChoice",hasNoChoice);
				debugReturn.addObject("page-title",CarUtility.getLocalComponent("page-title"));
				debugReturn.addObject("title-choice",CarUtility.getLocalComponent("title-choice",locrpdisp));
				debugReturn.addObject("title-nochoice",CarUtility.getLocalComponent("title-nochoice",locrpdisp));
				debugReturn.addObject("intro",CarUtility.getLocalComponent("intro",locrpdisp));
				debugReturn.addObject("hasChoices",haschoices);
				debugReturn.addObject("hasUserChoices",hasUserChoices);
				debugReturn.addObject("rpiconurl",rpmetainformation.getIconurl());
				debugReturn.addObject("rpdisplayname",locrpdisp);
				debugReturn.addObject("rhdisplayname",rhdisplayname);

				debugReturn.addObject("rpdescription",rpmetainformation.getDescription()==null?null:locrpdesc);
				debugReturn.addObject("rpprivacyurl",rpmetainformation.getPrivacyurl());
				debugReturn.addObject("header_nochoice",CarUtility.getLocalComponent("header_nochoice",CarUtility.localize(rpmetainformation.getDisplayname(),preflang)));
				debugReturn.addObject("acknowledge_show_false",CarUtility.getLocalComponent("acknowledge_show_false"));
				debugReturn.addObject("acknowledge_show_true",CarUtility.getLocalComponent("acknowledge_show_true"));
				debugReturn.addObject("continue_button_false",CarUtility.getLocalComponent("continue_button_false"));
				debugReturn.addObject("continue_button_true",CarUtility.getLocalComponent("continue_button_true"));
				debugReturn.addObject("save_prompt",CarUtility.getLocalComponent("save_prompt"));
				
				debugReturn.addObject("header_must", CarUtility.getLocalComponent("header_must",locrpdisp));
				debugReturn.addObject("header_may",CarUtility.getLocalComponent("header_may", locrpdisp));
				debugReturn.addObject("choice_edit",CarUtility.getLocalComponent("choice_edit"));
				debugReturn.addObject("choice_dont_edit",CarUtility.getLocalComponent("choice_dont_edit"));
				debugReturn.addObject("header_must_and_may",CarUtility.getLocalComponent("header_must_and_may",locrpdisp));
				
				debugReturn.addObject("release_what",CarUtility.getLocalComponent("release_what",locrpdisp));
				debugReturn.addObject("save_true_suppress",CarUtility.getLocalComponent("save_true_suppress"));
				debugReturn.addObject("save_true_show_again",CarUtility.getLocalComponent("save_true_show_again"));
				debugReturn.addObject("save_false",CarUtility.getLocalComponent("save_false"));
				debugReturn.addObject("nochoice_nodisplay",CarUtility.getLocalComponent("nochoice_nodisplay"));
				debugReturn.addObject("nochoice_display",CarUtility.getLocalComponent("nochoice_display"));
				debugReturn.addObject("recommends",CarUtility.getLocalComponent("recommends"));
				debugReturn.addObject("show_required",CarUtility.getLocalComponent("show_required",locrpdisp));
				debugReturn.addObject("held-by",CarUtility.getLocalComponent("held-by"));
				debugReturn.addObject("edit-presets",CarUtility.getLocalComponent("edit-presets"));
				debugReturn.addObject("update-settings",CarUtility.getLocalComponent("update-settings"));
				debugReturn.addObject("privacy-policy",CarUtility.getLocalComponent("privacy-policy"));
				debugReturn.addObject("mandatory-header",CarUtility.getLocalComponent("mandatory-header"));
				debugReturn.addObject("dont-show",CarUtility.getLocalComponent("dont-show"));
				debugReturn.addObject("self-service-description",CarUtility.getLocalComponent("self-service-description"));
				debugReturn.addObject("save-continue",CarUtility.getLocalComponent("save-continue"));
				debugReturn.addObject("cancel",CarUtility.getLocalComponent("cancel"));
				
				debugReturn.addObject("top_heading",CarUtility.getLocalComponent("top_heading"));
				debugReturn.addObject("sign_out",CarUtility.getLocalComponent("sign_out"));
				debugReturn.addObject("institutional_logo_url",CarUtility.getLocalComponent("institutional_logo_url"));
				debugReturn.addObject("short_institution",CarUtility.getLocalComponent("short_institution"));
				debugReturn.addObject("preflang",preflang);
				
				debugReturn.addObject("areason",areason);
				
				// And pass in the sconvo value, just in case
				debugReturn.addObject("sconvo",sconvo);
				
				// And pass in the csrftoken, as well
				debugReturn.addObject("csrftoken",csrftoken);
				
				// And suppress the default header in favor of the intercept layout option
				debugReturn.addObject("intercept_view","1");
				
				// provide a blind hashmap for handling form layout
				HashMap<String,Integer> counters = new HashMap<String,Integer>();
				counters.put("make_counters_not_null_for_velocity",Integer.valueOf(1));
				debugReturn.addObject("counters",counters);
				
				// For handling of showagain checkbox
				// Pass in both the current default showagain setting for this RP and this user's showagain setting
				// We set the status of the showagain checkbox accordingly, and then consistently honor 
				// the showagain value on POST back here.
				debugReturn.addObject("defaultShowAgain",defaultShowAgain); // RP default mod system default
				if (urpmi == null) {
					debugReturn.addObject("userShowAgain",defaultShowAgain?"true":"false");  // use default if no user pref
				} else {
					debugReturn.addObject("userShowAgain",urpmi.isShowagain()?"true":"false"); // ternary operators are the devil
				}
				
				// Include the typemap for selection against oauth_scope items
				
				debugReturn.addObject("typemap",typemap);
				
				if (hasMay || hasMustDecisions || hasNoChoice) {  // pass thru if everything turns out to be missing or ASND
					CarUtility.locDebug("ERR1134","Displaying intercept");
					return debugReturn;
				} 
				CarUtility.locDebug("ERR1134","Not Displaying intercept, in AskUser mode");
			}
			CarUtility.locDebug("ERR1134","Not displaying intercept, no user questions either");
		}
		// if we get here, we're on a return trip by definition.
		
		// Start by getting the sconvo value out of the URL (and error if none is specified)
		if (request.getParameter("conversation") != null) {
			convo = Integer.parseInt((String) request.getParameter("conversation"));
			sconvo = String.valueOf(convo);
		} else if (askUserForDecisions) {
			ModelAndView e = new ModelAndView("errorPage");
			e.addObject("message","Your browser did not properly identify itself.  This usually indicates a bug.");
			e.addObject("intercept_view","1");
			e.addObject("top_heading",CarUtility.getLocalComponent("top_heading"));
			e.addObject("institutional_logo_url",CarUtility.getLocalComponent("institutional_logo_url"));
			return e;  // bail out if there is no conversation specified, since we cannot continue otherwise
		}
		
		// Assuming we now have a conversation detected, check CSRF if appropriate (if there was a roundtrip to the client)
		//
		if (askUserForDecisions) {
			// Check CSRF token
			if (session.getAttribute(sconvo + ":" + "csrftoken") == null || ! session.getAttribute(sconvo+":"+"csrftoken").equals((String) request.getParameter("csrftoken"))) {
				// CSRF failure
				ModelAndView e = new ModelAndView("errorPage");
				e.addObject("message","CSRF failure.  This may occur due to a sesison timeout, due to the use of your browser's back button, or due to purposeful attack.");
				e.addObject("intercept_view","1");
				e.addObject("top_heading",CarUtility.getLocalComponent("top_heading"));
				e.addObject("institutional_logo_url",CarUtility.getLocalComponent("institutional_logo_url"));
				return e; // bail out if CSRF test fails for any reason
			}
		}
		
		// No CSRF test required if the browser was never consulted, althoug session is still required for continuity
			

		// At this point, we have convo and sconvo set properly
		
		// Start decoding response from the page
		// First, marshall what we have in our session and clear out the session
		IcmDecisionResponseObject originalDecisionResponse = (IcmDecisionResponseObject) session.getAttribute(sconvo + ":" + "icmdecision");
		session.removeAttribute(sconvo + ":" + "icmdecision");
		
		Boolean baskUserForDecisions = (Boolean) session.getAttribute(sconvo + ":" + "askUserForDecisions");
		session.removeAttribute(sconvo + ":" + "askUserForDecisions");
		
		String returntourl = (String) session.getAttribute(sconvo + ":" + "returntourl");
		session.removeAttribute(sconvo + ":" + "returntourl");
		
		String rhid = (String) session.getAttribute(sconvo + ":" + "rhid");
		session.removeAttribute(sconvo + ":" + "rhid");
		
		String rpid = (String) session.getAttribute(sconvo + ":" + "rpid");
		session.removeAttribute(sconvo + ":" + "rpid");
		
		String username = (String) session.getAttribute(sconvo + ":" + "username");
		session.removeAttribute(sconvo + ":" + "username");
		
		String usertype = (String) session.getAttribute(sconvo + ":" + "usertype");
		session.removeAttribute(sconvo + ":" + "usertype");
		
		// Compute the values we need to retrieve from the client side,
		// and retrieve their values based on the information in the hidden form posted back 
		// to us.
		//
		// We walk the original ICM response and produce an ArrayList of AttributeValuePair objects
		// containing the final target decisions, which we then pass to the converter for sending back to
		// the RH.
		
		ArrayList<AttributeValuePair> finalDecisions = new ArrayList<AttributeValuePair>();
		if (originalDecisionResponse != null && originalDecisionResponse.getArrayOfInfoDecisionStatement() != null) {
		for (IcmDecisionsForInfoStatement iids : originalDecisionResponse.getArrayOfInfoDecisionStatement()) {
			for (IcmDecisionOnValues idov : iids.getArrayOfDecisionOnValues()) {
				if (idov.getReleaseDecision().equals(UserReleaseDirective.permit) || idov.getReleaseDecision().equals(UserReleaseDirective.deny)) {
					// the original had a decision already for this attribute.
					// check if it's a may or a nochoice
					if (idov.getAugmentedPolicyId().getPolicySource().equals(PolicySourceEnum.COPSU)) {
						// it was a may decision, so we pull the values back from the posted results from the 
						// client

						for (String v : idov.getReturnedValuesList()) {
							// for every value in this attribute with this decision...
							// These are may values...
							// This is a bit absurd, but...  find the right tag to use for this value
							int ctr = 0;
							boolean foundOrFinished = false;
							boolean usectr = false;
							while (!foundOrFinished) {
								String valuemapname = "valuemap_" + iids.getInfoId().getInfoValue() + "_" + ctr;
								if (request.getParameter(valuemapname) != null && request.getParameter(valuemapname).equals(v)) {
									// this is the one
									foundOrFinished = true;
									usectr = true;
								} else {
									if (request.getParameter(valuemapname) == null) {
										foundOrFinished = true;
										usectr = false;
									} else {
										ctr += 1;
									}
								}
							}
							/* Removing debugging here */
							/*if (usectr) {
								CarUtility.locError("ERR0812",LogCriticality.debug,iids.getInfoId().getInfoValue(),String.valueOf(ctr),v,request.getParameter("radio_"+iids.getInfoId().getInfoValue()+"_"+ctr));
							} else {
								CarUtility.locError("ERR0812",LogCriticality.debug,iids.getInfoId().getInfoValue(),"nocounter",v,idov.getReleaseDecision().toString());
							}*/
							
							// At this point, if usectr is false, we didn't get a value, and if it is true, 
							// we use ctr as the counter for the value
							if (!usectr) {
								// this is an odd case that shouldn't happen, but if it does somehow, we simply
								// use the value that was present to begin with.
								AttributeValuePair avp = new AttributeValuePair();
								avp.setAttrname(iids.getInfoId().getInfoValue());
								avp.setAttrvalue(v);
								avp.setCurrentdecision(idov.getReleaseDecision().toString());
								avp.setPolicySource("COPSU");
								finalDecisions.add(avp);
							} else {
								// this case is the common one -- we have a response value to use here
								String radioName = "radio_"+iids.getInfoId().getInfoValue()+"_"+ctr;
								String dec = (String) request.getParameter(radioName);  // should be permit or deny
								AttributeValuePair avp = new AttributeValuePair();
								avp.setAttrname(iids.getInfoId().getInfoValue());
								avp.setAttrvalue(v);
								avp.setCurrentdecision(dec);
								avp.setPolicySource("COPSU");
								finalDecisions.add(avp);
							}
						}
					} else {
						// it was a nochoice decision, so we simply populate what we have
						for (String v : idov.getReturnedValuesList()) {
							// for every value of this attribute with this decision...
							AttributeValuePair avp = new AttributeValuePair();
							avp.setAttrname(iids.getInfoId().getInfoValue());
							avp.setAttrvalue(v);
							avp.setCurrentdecision(idov.getReleaseDecision().toString());
							avp.setPolicySource("ARPSI");
							finalDecisions.add(avp);
						}
					}
				}
				if (idov.getReleaseDecision().equals(UserReleaseDirective.askMe)) {
					// This was a must decision -- mandatory response from the client
					
					for (String v : idov.getReturnedValuesList()) {
						// for every value in this attribute with this decision...
						// These are must values...
						// This is a bit absurd, but...  find the right tag to use for this value
						int ctr = 0;
						boolean foundOrFinished = false;
						boolean usectr = false;
						while (!foundOrFinished) {
							String valuemapname = "valuemap_" + iids.getInfoId().getInfoValue() + "_" + ctr;
							if (request.getParameter(valuemapname) != null && request.getParameter(valuemapname).equals(v)) {
								// this is the one
								foundOrFinished = true;
								usectr = true;
							} else {
								if (request.getParameter(valuemapname) == null) {
									foundOrFinished = true;
									usectr = false;
								} else {
									ctr += 1;
								}
							}
						}
						/* Removing debugging here */
						/*if (usectr) {
							CarUtility.locError("ERR0812",LogCriticality.debug,iids.getInfoId().getInfoValue(),String.valueOf(ctr),v,request.getParameter("radio_"+iids.getInfoId().getInfoValue()+"_"+ctr));
						} else {
							CarUtility.locError("ERR0812",LogCriticality.debug,iids.getInfoId().getInfoValue(),"nocounter",v,idov.getReleaseDecision().toString());
						}*/
						
						// At this point, if usectr is false, we didn't get a value, and if it is true, 
						// we use ctr as the counter for the value
						if (!usectr) {
							// this is an odd case that shouldn't happen, but if it does somehow, we simply
							// use the value that was present to begin with.
							CarUtility.locError("ERR0801");
							// CarUtility.locError("ERR1134", LogCriticality.error,"Handling " + iids.getInfoId().getInfoValue());
							AttributeValuePair avp = new AttributeValuePair();
							avp.setAttrname(iids.getInfoId().getInfoValue());
							avp.setAttrvalue(v);
							avp.setCurrentdecision(idov.getReleaseDecision().toString());
							avp.setPolicySource("COPSU");
							finalDecisions.add(avp);
						} else {
							// this case is the common one -- we have a response value to use here
							// CarUtility.locError("ERR1134",  LogCriticality.error,"Handling " + iids.getInfoId().getInfoValue());
							String radioName = "radio_"+iids.getInfoId().getInfoValue()+"_"+ctr;
							String dec = (String) request.getParameter(radioName);  // should be permit or deny
							AttributeValuePair avp = new AttributeValuePair();
							avp.setAttrname(iids.getInfoId().getInfoValue());
							avp.setAttrvalue(v);
							avp.setCurrentdecision(dec);
							avp.setPolicySource("COPSU");
							finalDecisions.add(avp);
						}
					}
				}
			}
		}
		}
		// For now, construct an error message string that depicts what will happen
		StringBuilder rb = new StringBuilder();
		rb.append("The following release decisions will be used:<br><ul>\n");
		for (AttributeValuePair ap : finalDecisions) {
			rb.append("<li>"+ap.getAttrname()+"="+ap.getAttrvalue()+" : " + ap.getCurrentdecision() + "</li>\n");
		}
		rb.append("</ul><br>\n");
		rb.append("The posted values were:<br><ul>\n");
		@SuppressWarnings("unchecked")
		Enumeration<String> attrs = request.getParameterNames();
		while (attrs.hasMoreElements()) {
			String name = attrs.nextElement();
			rb.append("<li>"+name+"="+request.getParameter(name)+"</li>\n");
		}
		rb.append("</ul>");
		if (baskUserForDecisions && (request.getParameter("alltheway") == null || (request.getParameter("alltheway") != null && !request.getParameter("alltheway").equals("1")))) {
				ModelAndView testreturn = new ModelAndView("errorPage");
				//testreturn.addObject("message","Return trip detected?!?");
				testreturn.addObject("message",rb.toString());
				testreturn.addObject("intercept_view","1");
				// Try again...
				if (session != null) 
					session.removeAttribute(sconvo + ":" + "returntourl");
				// recurse
				return handleFilterAndDecide(request, useCrypto);
				//return testreturn;
		} else {
			// This is a push to the other side
			// Construct the response object for the RH
			ArrayList<NameValueDecision> alnvd = new ArrayList<NameValueDecision>();
			for (AttributeValuePair ap : finalDecisions) {
				if (ap.getCurrentdecision().equals("permit")) {
					NameValueDecision nvd = new NameValueDecision();
					nvd.setName(ap.getAttrname());
					nvd.setValue(ap.getAttrvalue());
					nvd.setDecision("permit");
					alnvd.add(nvd);
				} else {
					// In the event that we're asnd, we have to override to permit 
					// despite everything else we've done. 
					//
					// ASND overrides it all
					//
					if (CarUtility.isIIVAsnd(rhid, ap.getAttrname(), ap.getAttrvalue(), config)) {
						NameValueDecision nvd = new NameValueDecision();
						nvd.setName(ap.getAttrname());
						nvd.setValue(ap.getAttrvalue());
						nvd.setDecision("permit");
						alnvd.add(nvd);
					}
				}
			}
			DecisionResponse unwrapped = new DecisionResponse();
			DecisionResponseHeader drh = new DecisionResponseHeader();

			try {
				drh.setCarInstanceId("https://"+config.getProperty("car.car.hostname", true)+":"+config.getProperty("car.car.port", true)+"/car");
			} catch (Exception w) {
				drh.setCarInstanceId("https://icm.example.com/car");
			}
			// RGC - 11-21-2018
			if (originalDecisionResponse != null)
			drh.setDecisionId(originalDecisionResponse.getDecisionId());
			unwrapped.setHeader(drh);
			
			unwrapped.setDecisions(alnvd);
			
			ModelAndView resultView = new ModelAndView("reflex");
			
			try {
				// INFO: Here is where we need to add crypto around the decision response 
				//
				
				WrappedDecisionResponse w = new WrappedDecisionResponse();
				w.setDecisionResponse(unwrapped);
				if (! useCrypto) {
					CarUtility.locDebug("LOG1002");
					resultView.addObject("json",new String(Base64.encodeBase64(w.toJson().getBytes())));
				} else {
					CarUtility.locDebug("LOG1003");
					resultView.addObject("json",signAndEncryptToRHAsJWT(new String(Base64.encodeBase64(w.toJson().getBytes())),rhid));
				}
				resultView.addObject("returnUrl",returntourl);
				// debug
				if ("true".equalsIgnoreCase(config.getProperty("logSensitiveInfo", false)))
					CarUtility.locDebug("ERR0808",LogCriticality.debug,w.toJson());
			} catch (Exception e) {
				// for now, we don't care about why
				ModelAndView error = new ModelAndView("errorPage");
				error.addObject("message","Failed constructing base64 encoded JSON representation for return: " + e.getMessage());
				error.addObject("intercept_view","1");
				error.addObject("transient","true");
				error.addObject("top_heading",CarUtility.getLocalComponent("top_heading"));
				error.addObject("institutional_logo_url",CarUtility.getLocalComponent("institutional_logo_url"));
				/*return new ModelAndView("errorPage");*/
				return error;
			}
			
			// Immediately before returning the push to the other side, check if updates are required, and
			// if they are, make them.
			// Two updates may be needed:
			//     a merge of these values with user policy from the COPSU via the ICM
			//     an update to the uric endpoint to set "showAgain" for the user/RP pair
			//
			// First, try to perform the policy update if needed
			// Wrap everything in a null try/catch -- this operation is best-effort and non-fatal
			//
			try {
				if (request.getParameter("saveandshow") != null || request.getParameter("saveandhide") != null) {
					// we need to do a merge-save
					// Start by getting the existing COPSU policy
					CarUtility.locDebug("ERR0804");
					UserReturnedPolicy rp = CarUtility.getCOPSUPolicy(username,rhid,rpid,config);
					UserInfoReleasePolicy irp = rp.getUserInfoReleasePolicy();
					// And build a new IRP from the name-value decisions we have for the must and may decisions
					// we started with. 
					// We must avoid processing the nochoice decisions as though they were updates.
					//
					//Start by creating a new policy
					UserInfoReleasePolicy newirp = new UserInfoReleasePolicy();
					// And copying over the relevant metainformation
					newirp.setWhileImAwayDirective(irp.getWhileImAwayDirective());
					newirp.setUserId(irp.getUserId());
					newirp.setRelyingPartyId(irp.getRelyingPartyId());
					newirp.setResourceHolderId(irp.getResourceHolderId());
					newirp.setUserAllOtherInfoReleaseStatement(irp.getUserAllOtherInfoReleaseStatement());
					newirp.setDescription(irp.getDescription());
					
					// Now build the set of actual attribute responses
					ArrayList<UserInfoReleaseStatement> irs = new ArrayList<UserInfoReleaseStatement>();
					
					// and populate it (this is the tricky part)
					// We need to perform some aggregation.  
					HashMap<edu.internet2.consent.icm.model.InfoId,UserInfoReleaseStatement> built = new HashMap<edu.internet2.consent.icm.model.InfoId,UserInfoReleaseStatement>();
					
					for (AttributeValuePair avp : finalDecisions) {
						
						// Special casing for types -- oauth_scope for now
						// carried in os_$name
						// TODO:  Make this less hacky -- this is carried through by 
						// TODO:  insertions in the post-back form for now, but should be 
						// TODO:  refactored to be more intrinsic to the model going forward.
						
						if (request.getParameter("os_"+avp.getAttrname()) != null && request.getParameter("os_"+avp.getAttrname()).equals(avp.getAttrname())) {
							typemap.put(avp.getAttrname(),"oauth_scope");
						}
						
						if (avp.getPolicySource().equals("COPSU")) {
							// this is one we need to mint a decision for
							// check if we already have one
							edu.internet2.consent.icm.model.InfoId ii = new edu.internet2.consent.icm.model.InfoId();
							// TODO:  for now, all info items in this interface are attributes, but... maybe not forever
							// TODO:  figure out how to handle alternative types -- possibly informed content? (yucch)
							// TODO:  possibly prefereble...
							if (typemap.containsKey(avp.getAttrname())) {
								ii.setInfoType(typemap.get(avp.getAttrname()));
								// CarUtility.locError("ERR1134", LogCriticality.error,"(3) " + avp.getAttrname() + " is " + typemap.get(avp.getAttrname()));
							} else {
								ii.setInfoType("attribute");
								// CarUtility.locError("ERR1134", LogCriticality.error,"(3a) " + avp.getAttrname() + " defaulted to attribute");

							}
							
							ii.setInfoValue(avp.getAttrname());
							if (built.containsKey(ii)) {
								// there's already one started -- add to it
								UserInfoReleaseStatement ir = built.get(ii);
								boolean added = false;
								for (UserDirectiveOnValues dov : ir.getArrayOfDirectiveOnValues()) {
									if (dov.getUserReleaseDirective().equals(UserReleaseDirective.valueOf(avp.getCurrentdecision()))) {
										// this is the one we want to add to
										edu.internet2.consent.icm.model.ValueObject vo = new edu.internet2.consent.icm.model.ValueObject();
										vo.setValue(avp.getAttrvalue());
										dov.getValueObjectList().add(vo);
										added = true;
									}
								}
								if (! added) {
									// we need to add a DOV for this
									UserDirectiveOnValues adddov = new UserDirectiveOnValues();
									ArrayList<edu.internet2.consent.icm.model.ValueObject> avo = new ArrayList<edu.internet2.consent.icm.model.ValueObject>();
									edu.internet2.consent.icm.model.ValueObject vo = new edu.internet2.consent.icm.model.ValueObject();
									vo.setValue(avp.getAttrvalue());
									avo.add(vo);
									adddov.setUserReleaseDirective(UserReleaseDirective.valueOf(avp.getCurrentdecision()));
									adddov.setValuesList(avo);
									ir.getArrayOfDirectiveOnValues().add(adddov);
								}
							} else {
								// This is a totally new one
								UserInfoReleaseStatement newirs = new UserInfoReleaseStatement();
								newirs.setPersistence("onChange");  // TODO:  this may need to be variable eventually but we don't currently collect persistence values
								newirs.setInfoId(ii);
								UserDirectiveAllOtherValues daov = new UserDirectiveAllOtherValues();
								daov.setAllOtherValues(edu.internet2.consent.icm.model.AllOtherValuesConst.allOtherValues);
								ReturnedInfoItemMetaInformation riimi = null;
								if (ii.getInfoType() != null) {
									riimi = CarUtility.getInfoItemMetaInformation(rhid, ii.getInfoType(),ii.getInfoValue(), config);
								} else {
									riimi = CarUtility.getInfoItemMetaInformation(rhid, ii.getInfoValue(), config);
								}
								
								// For PEV, always set the directive on all other values to "askMe"; for PAO, set it to the current setting
								
								if (riimi.getPolicytype().equals("PAO")) {
									daov.setUserReleaseDirective(UserReleaseDirective.valueOf(avp.getCurrentdecision()));
								} else {
									daov.setUserReleaseDirective(UserReleaseDirective.askMe);
								}
								
								newirs.setUserDirectiveAllOtherValues(daov);
								ArrayList<UserDirectiveOnValues> adov = new ArrayList<UserDirectiveOnValues>();
								UserDirectiveOnValues addnewdov = new UserDirectiveOnValues();
								ArrayList<edu.internet2.consent.icm.model.ValueObject> newvo = new ArrayList<edu.internet2.consent.icm.model.ValueObject>();
								edu.internet2.consent.icm.model.ValueObject vo = new edu.internet2.consent.icm.model.ValueObject();
								vo.setValue(avp.getAttrvalue());
								newvo.add(vo);
								addnewdov.setUserReleaseDirective(UserReleaseDirective.valueOf(avp.getCurrentdecision()));
								addnewdov.setValuesList(newvo);
								adov.add(addnewdov);
								newirs.setArrayOfDirectiveOnValues(adov);
								built.put(ii, newirs);
							}
						}
					}
					// and copy from built to the irs
					for (InfoId key : built.keySet()) {
						irs.add(built.get(key));
					}
					// then add the irs to the policy
					newirp.setArrayOfInfoReleaseStatement(irs);
					
					// determine the baseid to update
					String baseid = rp.getPolicyMetaData().getPolicyId().getBaseId();
					
					// and run the update 
					// Since our JDBC driver may be prone to <ahemMySQLahem> random deadlock issues...
					// we set up to retry the policy put up to 5 times before giving up and continuing
					// This is best effort, but if we actually fail the write, we at least ensure we give the user 
					// another shot next time around (as if the user had chosen don't save and show again)
					
					succ = false;
					for (int sc = 0; sc < 5 && !succ; sc++) {
						succ = CarUtility.putCOPSUPolicy(baseid,newirp,config);
						if (!succ)
							CarUtility.locError("ERR0814",String.valueOf(sc));
					}
				}
				// And regardless of that, update showagain for the user accordingly
				// We use usertype and username to do the setting
				if (request.getParameter("saveandshow") != null || request.getParameter("dontsave") != null || ! succ) {
					CarUtility.locDebug("ERR0809","true");
					CarUtility.setShowAgain(usertype,username,rpid,true,config);
				} else {
					CarUtility.locDebug("ERR0809","false");
					CarUtility.setShowAgain(usertype,username,rpid,false,config);
				}
			} catch (Exception ign) {
				// Log and ignore
				CarUtility.locError("ERR0805",CarUtility.exceptionStacktraceToString(ign));
			}
			// and after a best effort at updating, send the response onward
			resultView.addObject("top_heading",CarUtility.getLocalComponent("top_heading"));
			resultView.addObject("sign_out",CarUtility.getLocalComponent("sign_out"));
			resultView.addObject("institutional_logo_url",CarUtility.getLocalComponent("institutional_logo_url"));

			return resultView;
		}
	}
}
