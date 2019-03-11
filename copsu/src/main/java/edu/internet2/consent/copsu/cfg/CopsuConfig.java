package edu.internet2.consent.copsu.cfg;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Properties;

import org.hibernate.query.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import edu.internet2.consent.copsu.model.AllOtherInfoId;
import edu.internet2.consent.copsu.model.AllOtherInfoReleaseStatement;
import edu.internet2.consent.copsu.model.AllOtherInfoTypeConst;
import edu.internet2.consent.copsu.model.AllOtherInfoValueConst;
import edu.internet2.consent.copsu.model.AllOtherValuesConst;
import edu.internet2.consent.copsu.model.ChangeOrder;
import edu.internet2.consent.copsu.model.ChangeOrderMetaData;
import edu.internet2.consent.copsu.model.ChangeOrderType;
import edu.internet2.consent.copsu.model.DirectiveAllOtherValues;
import edu.internet2.consent.copsu.model.ReleaseDirective;
import edu.internet2.consent.copsu.model.ReturnedChangeOrder;
import edu.internet2.consent.copsu.model.UserId;
import edu.internet2.consent.copsu.model.WhileImAwayDirective;
import edu.internet2.consent.copsu.util.CopsuUtility;
import edu.internet2.consent.exceptions.CopsuConfigurationException;

public class CopsuConfig {

	//Singleton configuration for the COPSU
	private static CopsuConfig config = null;
	private Properties properties = null;
	
	private CopsuConfig() {
		properties = new Properties();
		

		
		InputStream etcfile = null;
		InputStream inputStream  = null;
		try {
			etcfile = new FileInputStream("/etc/car/copsu/copsu.conf");
			properties.load(etcfile);
		} catch (Exception f) {
			ClassLoader cl = CopsuConfig.class.getClassLoader();
			URL url = cl.getResource("copsu.conf");
		
			try {
				inputStream = url.openStream();
				properties.load(inputStream);
			} catch (Exception e) {
				throw new RuntimeException("Failed loading copsu.conf properties",e);
			} finally {
				if (inputStream != null) {
					try {
						inputStream.close();
					} catch (Exception ign) {
						//ignore
					}
				}
			}
		}
		// This is only called the first time the COPSU is activated after a restart -- this is the 
		// proper time to verify that the NewUserChangeOrder exists, and create if it doesn't.
		//
		Session sess = CopsuUtility.getHibernateSession();
		if (sess == null) {
			throw new RuntimeException("Failed to initialize persistence layer");
		}

		Query<ReturnedChangeOrder> nucQuery = sess.createQuery("from ReturnedChangeOrder where changeOrderMetaData.changeOrderId = :changeId",ReturnedChangeOrder.class);
		nucQuery.setParameter("changeId", "NewUserChangeOrder");
		List<ReturnedChangeOrder> lrco = nucQuery.list();
		if (lrco.isEmpty()) {
			// This is where we have to create it
			ReturnedChangeOrder rco = new ReturnedChangeOrder();
			ChangeOrderMetaData como = new ChangeOrderMetaData();
			ChangeOrder co = new ChangeOrder();
			como.setChangeOrderId("NewUserChangeOrder");
			UserId u1 = new UserId();
			u1.setUserType("SYSTEM");
			u1.setUserValue("COPSU");
			como.setCreator(u1);
			como.setCreateTime(System.currentTimeMillis());
			co.setDescription("New User Standing Change Order");
			co.setChangeOrderType(ChangeOrderType.createPolicyFromNewUserConfig);
			co.setWhileImAwayDirective(WhileImAwayDirective.deny);
			AllOtherInfoReleaseStatement alr = new AllOtherInfoReleaseStatement();
			AllOtherInfoId aoi = new AllOtherInfoId();
			aoi.setAllOtherInfoType(AllOtherInfoTypeConst.allOtherInfoType);
			aoi.setAllOtherInfoValue(AllOtherInfoValueConst.allOtherInfoValue);
			alr.setAllOtherInfoId(aoi);
			DirectiveAllOtherValues dav = new DirectiveAllOtherValues();
			dav.setAllOtherValues(AllOtherValuesConst.allOtherValues);
			dav.setReleaseDirective(ReleaseDirective.askMe);
			alr.setDirectiveAllOtherValues(dav);
			co.setAllOtherInfoReleaseStatement(alr);
			rco.setChangeOrderMetaData(como);
			rco.setChangeOrder(co);
			Transaction tx = sess.beginTransaction();
			sess.save(rco);
			tx.commit();
			sess.close();
		} else {
			sess.close();  // close session if already exists.
		}
	}
	
	public static CopsuConfig getInstance() {
		// Get the singleton
		if (config == null) {
			config = new CopsuConfig();
		}
		return config;
	}
	
	// And get a property out of it as needed
	public String getProperty(String property, boolean exceptionIfNotFound) {
		String value = properties.getProperty(property);
		if (value == null && exceptionIfNotFound) {
			throw new CopsuConfigurationException("property:" + property + " not found");
		}
		return value;
	}
}
