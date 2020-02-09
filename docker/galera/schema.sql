SET foreign_key_checks=0;
CREATE DATABASE /*!32312 IF NOT EXISTS*/ `arpsi` default character set utf8mb4 default collate utf8mb4_0900_ai_ci /*!40100 DEFAULT CHARACTER SET utf8mb4 */;

USE `arpsi`;


/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `OrgDirectiveOnValues` (
  `DirectiveOnValuesIdentifier` bigint(20) NOT NULL AUTO_INCREMENT,
  `orgReleaseDirective` varchar(255) DEFAULT NULL,
  `orgPolicyBasis` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`DirectiveOnValuesIdentifier`)
) ENGINE=InnoDB AUTO_INCREMENT=706 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `OrgDirectiveOnValues_ValueObject`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `OrgDirectiveOnValues_ValueObject` (
  `OrgDirectiveOnValues_DirectiveOnValuesIdentifier` bigint(20) NOT NULL,
  `valueObjectList_valueKey` bigint(20) NOT NULL,
  UNIQUE KEY `UK_tlbhlrtj3eagilaopmonxh0ow` (`valueObjectList_valueKey`),
  KEY `FK52mdd2f8pgxw46e8e5bhqoxg9` (`OrgDirectiveOnValues_DirectiveOnValuesIdentifier`),
  CONSTRAINT `FK52mdd2f8pgxw46e8e5bhqoxg9` FOREIGN KEY (`OrgDirectiveOnValues_DirectiveOnValuesIdentifier`) REFERENCES `OrgDirectiveOnValues` (`DirectiveOnValuesIdentifier`),
  CONSTRAINT `FKn33b1g9gcnemii62mk20vpdkc` FOREIGN KEY (`valueObjectList_valueKey`) REFERENCES `ValueObject` (`valueKey`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `OrgInfoReleaseStatement`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `OrgInfoReleaseStatement` (
  `OIRKey` bigint(20) NOT NULL AUTO_INCREMENT,
  `infoType` varchar(255) DEFAULT NULL,
  `infoValue` varchar(255) DEFAULT NULL,
  `allOtherValuesConst` varchar(255) DEFAULT NULL,
  `orgReleaseDirective` varchar(255) DEFAULT NULL,
  `orgPolicyBasis` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`OIRKey`)
) ENGINE=InnoDB AUTO_INCREMENT=1033 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `OrgInfoReleaseStatement_OrgDirectiveOnValues`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `OrgInfoReleaseStatement_OrgDirectiveOnValues` (
  `OrgInfoReleaseStatement_OIRKey` bigint(20) NOT NULL,
  `arrayOfOrgDirectiveOnValues_DirectiveOnValuesIdentifier` bigint(20) NOT NULL,
  UNIQUE KEY `UK_cicvqfdcg3guabtio6ofhg399` (`arrayOfOrgDirectiveOnValues_DirectiveOnValuesIdentifier`),
  KEY `FKa7gf1x7nbvxbek8etdx5jloec` (`OrgInfoReleaseStatement_OIRKey`),
  CONSTRAINT `FK1d12ewljipyejlc7wtduiuc6` FOREIGN KEY (`arrayOfOrgDirectiveOnValues_DirectiveOnValuesIdentifier`) REFERENCES `OrgDirectiveOnValues` (`DirectiveOnValuesIdentifier`),
  CONSTRAINT `FKa7gf1x7nbvxbek8etdx5jloec` FOREIGN KEY (`OrgInfoReleaseStatement_OIRKey`) REFERENCES `OrgInfoReleaseStatement` (`OIRKey`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `OrgReturnedPolicy`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `OrgReturnedPolicy` (
  `ReturnedPolicyIdentifier` bigint(20) NOT NULL AUTO_INCREMENT,
  `allOtherInfoType` varchar(255) DEFAULT NULL,
  `allOtherInfoValue` varchar(255) DEFAULT NULL,
  `allOtherValuesConst` varchar(255) DEFAULT NULL,
  `orgReleaseDirective` varchar(255) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `RHType` varchar(255) DEFAULT NULL,
  `RHValue` varchar(255) DEFAULT NULL,
  `createTime` bigint(20) NOT NULL,
  `userType` varchar(255) DEFAULT NULL,
  `userValue` varchar(255) DEFAULT NULL,
  `baseId` varchar(255) DEFAULT NULL,
  `version` varchar(255) DEFAULT NULL,
  `state` int(11) DEFAULT NULL,
  `supersedingBaseId` varchar(255) DEFAULT NULL,
  `supersedingVersion` varchar(255) DEFAULT NULL,
  `priority` bigint(20) NOT NULL,
  `orgPolicyBasis` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`ReturnedPolicyIdentifier`),
  KEY `i_state` (`state`),
  KEY `i_rh` (`RHType`,`RHValue`)
) ENGINE=InnoDB AUTO_INCREMENT=427 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `OrgReturnedPolicy_OrgInfoReleaseStatement`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `OrgReturnedPolicy_OrgInfoReleaseStatement` (
  `OrgReturnedPolicy_ReturnedPolicyIdentifier` bigint(20) NOT NULL,
  `arrayOfInfoReleaseStatement_OIRKey` bigint(20) NOT NULL,
  UNIQUE KEY `UK_ag1onlkoi3f969m9u35a25nxk` (`arrayOfInfoReleaseStatement_OIRKey`),
  KEY `FK7oh3tu0t5mqu8sc4inu5k3gfm` (`OrgReturnedPolicy_ReturnedPolicyIdentifier`),
  CONSTRAINT `FK7oh3tu0t5mqu8sc4inu5k3gfm` FOREIGN KEY (`OrgReturnedPolicy_ReturnedPolicyIdentifier`) REFERENCES `OrgReturnedPolicy` (`ReturnedPolicyIdentifier`),
  CONSTRAINT `FKs4sdnmn868b2im5t90qxe62a` FOREIGN KEY (`arrayOfInfoReleaseStatement_OIRKey`) REFERENCES `OrgInfoReleaseStatement` (`OIRKey`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `OrgReturnedPolicy_RelyingPartyProperty`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `OrgReturnedPolicy_RelyingPartyProperty` (
  `OrgReturnedPolicy_ReturnedPolicyIdentifier` bigint(20) NOT NULL,
  `relyingPartyPropertyArray_relyingPartyIdKey` bigint(20) NOT NULL,
  UNIQUE KEY `UK_r9rxggne22284wicodilgsxcd` (`relyingPartyPropertyArray_relyingPartyIdKey`),
  KEY `FKofl4onl9vwkwyju5c76n70mea` (`OrgReturnedPolicy_ReturnedPolicyIdentifier`),
  CONSTRAINT `FKofl4onl9vwkwyju5c76n70mea` FOREIGN KEY (`OrgReturnedPolicy_ReturnedPolicyIdentifier`) REFERENCES `OrgReturnedPolicy` (`ReturnedPolicyIdentifier`),
  CONSTRAINT `FKr82w132pho0jsr5asmyvbwi0c` FOREIGN KEY (`relyingPartyPropertyArray_relyingPartyIdKey`) REFERENCES `RelyingPartyProperty` (`relyingPartyIdKey`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `OrgReturnedPolicy_UserProperty`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `OrgReturnedPolicy_UserProperty` (
  `OrgReturnedPolicy_ReturnedPolicyIdentifier` bigint(20) NOT NULL,
  `userPropertyArray_userPropertyKey` bigint(20) NOT NULL,
  UNIQUE KEY `UK_p2kypo6g8m5t0iayk3pia3ify` (`userPropertyArray_userPropertyKey`),
  KEY `FKst0s1xq6p9fpxs5v0hndf0vhb` (`OrgReturnedPolicy_ReturnedPolicyIdentifier`),
  CONSTRAINT `FKm3fr154bx6ot7xylonybvjory` FOREIGN KEY (`userPropertyArray_userPropertyKey`) REFERENCES `UserProperty` (`userPropertyKey`),
  CONSTRAINT `FKst0s1xq6p9fpxs5v0hndf0vhb` FOREIGN KEY (`OrgReturnedPolicy_ReturnedPolicyIdentifier`) REFERENCES `OrgReturnedPolicy` (`ReturnedPolicyIdentifier`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `RelyingPartyProperty`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `RelyingPartyProperty` (
  `relyingPartyIdKey` bigint(20) NOT NULL AUTO_INCREMENT,
  `rpPropName` varchar(255) DEFAULT NULL,
  `rpPropValue` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`relyingPartyIdKey`)
) ENGINE=InnoDB AUTO_INCREMENT=436 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `UserProperty`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `UserProperty` (
  `userPropertyKey` bigint(20) NOT NULL AUTO_INCREMENT,
  `userPropName` varchar(255) DEFAULT NULL,
  `userPropValue` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`userPropertyKey`)
) ENGINE=InnoDB AUTO_INCREMENT=427 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ValueObject`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `ValueObject` (
  `valueKey` bigint(20) NOT NULL AUTO_INCREMENT,
  `value` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`valueKey`)
) ENGINE=InnoDB AUTO_INCREMENT=730 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hibernate_sequence`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `hibernate_sequence` (
  `next_val` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

INSERT INTO `hibernate_sequence` VALUES(1000);

--
-- Current Database: `copsu`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `copsu` default character set utf8mb4 default collate utf8mb4_0900_ai_ci /*!40100 DEFAULT CHARACTER SET utf8mb4 */;

USE `copsu`;

--
-- Table structure for table `DirectiveOnValues`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `DirectiveOnValues` (
  `DirectiveIdentifier` bigint(20) NOT NULL,
  `releaseDirective` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`DirectiveIdentifier`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `DirectiveOnValues_ValueObject`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `DirectiveOnValues_ValueObject` (
  `DirectiveOnValues_DirectiveIdentifier` bigint(20) NOT NULL,
  `valueObjectList_valueKey` bigint(20) NOT NULL,
  UNIQUE KEY `UK_cchbjkqkmcwkug2yac8l4rrau` (`valueObjectList_valueKey`),
  KEY `FKide0vfje909ci6qbx3qngi4sw` (`DirectiveOnValues_DirectiveIdentifier`),
  CONSTRAINT `FKide0vfje909ci6qbx3qngi4sw` FOREIGN KEY (`DirectiveOnValues_DirectiveIdentifier`) REFERENCES `DirectiveOnValues` (`DirectiveIdentifier`),
  CONSTRAINT `FKthsyu1nlyrvqtcjp5nnmkddwt` FOREIGN KEY (`valueObjectList_valueKey`) REFERENCES `ValueObject` (`valueKey`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `InfoReleaseStatement`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `InfoReleaseStatement` (
  `InfoIdKey` bigint(20) NOT NULL,
  `allOtherValues` varchar(255) DEFAULT NULL,
  `releaseDirective` varchar(255) DEFAULT NULL,
  `infoType` varchar(255) DEFAULT NULL,
  `infoValue` varchar(255) DEFAULT NULL,
  `persistence` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`InfoIdKey`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `InfoReleaseStatement_DirectiveOnValues`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `InfoReleaseStatement_DirectiveOnValues` (
  `InfoReleaseStatement_InfoIdKey` bigint(20) NOT NULL,
  `arrayOfDirectiveOnValues_DirectiveIdentifier` bigint(20) NOT NULL,
  UNIQUE KEY `UK_rselqpvi40olusxs2ijotkeud` (`arrayOfDirectiveOnValues_DirectiveIdentifier`),
  KEY `FKyt6uwjny3rk7b41kgne73ouh` (`InfoReleaseStatement_InfoIdKey`),
  CONSTRAINT `FKnjnare3d48dblfbgnj2kuyhi0` FOREIGN KEY (`arrayOfDirectiveOnValues_DirectiveIdentifier`) REFERENCES `DirectiveOnValues` (`DirectiveIdentifier`),
  CONSTRAINT `FKyt6uwjny3rk7b41kgne73ouh` FOREIGN KEY (`InfoReleaseStatement_InfoIdKey`) REFERENCES `InfoReleaseStatement` (`InfoIdKey`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ListablePolicyId`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `ListablePolicyId` (
  `ListablePolicyIdentifier` bigint(20) NOT NULL,
  `baseId` varchar(255) DEFAULT NULL,
  `version` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`ListablePolicyIdentifier`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ListableRelyingPartyId`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `ListableRelyingPartyId` (
  `ListableRelyingPartyIdentifier` bigint(20) NOT NULL,
  `RPtype` varchar(255) DEFAULT NULL,
  `RPvalue` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`ListableRelyingPartyIdentifier`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ListableUserId`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `ListableUserId` (
  `ListableUserIdentifier` bigint(20) NOT NULL,
  `userType` varchar(255) DEFAULT NULL,
  `userValue` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`ListableUserIdentifier`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ReturnedChangeOrder`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `ReturnedChangeOrder` (
  `ChangeOrderIdentifier` bigint(20) NOT NULL,
  `allOtherInfoType` varchar(255) DEFAULT NULL,
  `allOtherInfoValue` varchar(255) DEFAULT NULL,
  `allOtherValues` varchar(255) DEFAULT NULL,
  `releaseDirective` varchar(255) DEFAULT NULL,
  `changeOrderType` varchar(255) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `RHType` varchar(255) DEFAULT NULL,
  `RHValue` varchar(255) DEFAULT NULL,
  `whileImAwayDirective` varchar(255) DEFAULT NULL,
  `changeOrderId` varchar(255) DEFAULT NULL,
  `createTime` bigint(20) NOT NULL,
  `userType` varchar(255) DEFAULT NULL,
  `userValue` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`ChangeOrderIdentifier`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ReturnedChangeOrder_InfoReleaseStatement`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `ReturnedChangeOrder_InfoReleaseStatement` (
  `ReturnedChangeOrder_ChangeOrderIdentifier` bigint(20) NOT NULL,
  `arrayOfInfoReleaseStatement_InfoIdKey` bigint(20) NOT NULL,
  UNIQUE KEY `UK_4avt9rpjgu0e8os0iow6434vf` (`arrayOfInfoReleaseStatement_InfoIdKey`),
  KEY `FKgtsit1n8duxu2bi4wmv0xbo5q` (`ReturnedChangeOrder_ChangeOrderIdentifier`),
  CONSTRAINT `FK67vfnduejmvpt09363u25gqg2` FOREIGN KEY (`arrayOfInfoReleaseStatement_InfoIdKey`) REFERENCES `InfoReleaseStatement` (`InfoIdKey`),
  CONSTRAINT `FKgtsit1n8duxu2bi4wmv0xbo5q` FOREIGN KEY (`ReturnedChangeOrder_ChangeOrderIdentifier`) REFERENCES `ReturnedChangeOrder` (`ChangeOrderIdentifier`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ReturnedChangeOrder_ListablePolicyId`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `ReturnedChangeOrder_ListablePolicyId` (
  `ReturnedChangeOrder_ChangeOrderIdentifier` bigint(20) NOT NULL,
  `policyIdArray_ListablePolicyIdentifier` bigint(20) NOT NULL,
  UNIQUE KEY `UK_4s2407mga1318o5mcsiwrj0as` (`policyIdArray_ListablePolicyIdentifier`),
  KEY `FKeayk39tdyatvagkmpxrae77wo` (`ReturnedChangeOrder_ChangeOrderIdentifier`),
  CONSTRAINT `FKeayk39tdyatvagkmpxrae77wo` FOREIGN KEY (`ReturnedChangeOrder_ChangeOrderIdentifier`) REFERENCES `ReturnedChangeOrder` (`ChangeOrderIdentifier`),
  CONSTRAINT `FKj92mdkj5p3vsdo0wp309o8hc6` FOREIGN KEY (`policyIdArray_ListablePolicyIdentifier`) REFERENCES `ListablePolicyId` (`ListablePolicyIdentifier`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ReturnedChangeOrder_ListableRelyingPartyId`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `ReturnedChangeOrder_ListableRelyingPartyId` (
  `ReturnedChangeOrder_ChangeOrderIdentifier` bigint(20) NOT NULL,
  `relyingPartyIdArray_ListableRelyingPartyIdentifier` bigint(20) NOT NULL,
  UNIQUE KEY `UK_8cwser3dwmi18jgg859wtu2kg` (`relyingPartyIdArray_ListableRelyingPartyIdentifier`),
  KEY `FKn0i7yoclso9njx66bd5higke` (`ReturnedChangeOrder_ChangeOrderIdentifier`),
  CONSTRAINT `FK4runlk58akkjxvceh69wp0pf9` FOREIGN KEY (`relyingPartyIdArray_ListableRelyingPartyIdentifier`) REFERENCES `ListableRelyingPartyId` (`ListableRelyingPartyIdentifier`),
  CONSTRAINT `FKn0i7yoclso9njx66bd5higke` FOREIGN KEY (`ReturnedChangeOrder_ChangeOrderIdentifier`) REFERENCES `ReturnedChangeOrder` (`ChangeOrderIdentifier`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ReturnedChangeOrder_ListableUserId`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `ReturnedChangeOrder_ListableUserId` (
  `ReturnedChangeOrder_ChangeOrderIdentifier` bigint(20) NOT NULL,
  `userIdArray_ListableUserIdentifier` bigint(20) NOT NULL,
  UNIQUE KEY `UK_iu9kxvdxgps3h8ryr7sbp3nmj` (`userIdArray_ListableUserIdentifier`),
  KEY `FK2k52rolosphkhh2dgxgmnlbbl` (`ReturnedChangeOrder_ChangeOrderIdentifier`),
  CONSTRAINT `FK2k52rolosphkhh2dgxgmnlbbl` FOREIGN KEY (`ReturnedChangeOrder_ChangeOrderIdentifier`) REFERENCES `ReturnedChangeOrder` (`ChangeOrderIdentifier`),
  CONSTRAINT `FKbcf3lhmy9x2avxy5jhff84mov` FOREIGN KEY (`userIdArray_ListableUserIdentifier`) REFERENCES `ListableUserId` (`ListableUserIdentifier`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ReturnedPolicy`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `ReturnedPolicy` (
  `PolicyIdentifier` bigint(20) NOT NULL,
  `allOtherInfoType` varchar(255) DEFAULT NULL,
  `allOtherInfoValue` varchar(255) DEFAULT NULL,
  `allOtherValues` varchar(255) DEFAULT NULL,
  `releaseDirective` varchar(255) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `RPtype` varchar(255) DEFAULT NULL,
  `RPvalue` varchar(255) DEFAULT NULL,
  `RHType` varchar(255) DEFAULT NULL,
  `RHValue` varchar(255) DEFAULT NULL,
  `userType` varchar(255) DEFAULT NULL,
  `userValue` varchar(255) DEFAULT NULL,
  `whileImAwayDirective` int(11) DEFAULT NULL,
  `changeOrder` varchar(255) DEFAULT NULL,
  `createTime` bigint(20) NOT NULL,
  `creatingUserType` varchar(255) DEFAULT NULL,
  `creatingUserValue` varchar(255) DEFAULT NULL,
  `baseId` varchar(255) DEFAULT NULL,
  `version` varchar(255) DEFAULT NULL,
  `state` int(11) DEFAULT NULL,
  `supersedingId` varchar(255) DEFAULT NULL,
  `supersedingVersion` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`PolicyIdentifier`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ReturnedPolicy_InfoReleaseStatement`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `ReturnedPolicy_InfoReleaseStatement` (
  `ReturnedPolicy_PolicyIdentifier` bigint(20) NOT NULL,
  `arrayOfInfoReleaseStatement_InfoIdKey` bigint(20) NOT NULL,
  UNIQUE KEY `UK_synpvn3mfr7ahehcpf5kj6xc9` (`arrayOfInfoReleaseStatement_InfoIdKey`),
  KEY `FKdovwrd9gciax8tc3piyqpum2t` (`ReturnedPolicy_PolicyIdentifier`),
  CONSTRAINT `FKam6hxe0raa6bhlmtfrru9qsut` FOREIGN KEY (`arrayOfInfoReleaseStatement_InfoIdKey`) REFERENCES `InfoReleaseStatement` (`InfoIdKey`),
  CONSTRAINT `FKdovwrd9gciax8tc3piyqpum2t` FOREIGN KEY (`ReturnedPolicy_PolicyIdentifier`) REFERENCES `ReturnedPolicy` (`PolicyIdentifier`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ValueObject`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `ValueObject` (
  `valueKey` bigint(20) NOT NULL,
  `value` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`valueKey`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hibernate_sequence`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `hibernate_sequence` (
  `next_val` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

INSERT INTO hibernate_sequence VALUES(1000);
--
-- Current Database: `icm`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `icm` default character set utf8mb4 default collate utf8mb4_0900_ai_ci /*!40100 DEFAULT CHARACTER SET utf8mb4 */;

USE `icm`;

--
-- Table structure for table `IcmDirectiveOnValues`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `IcmDirectiveOnValues` (
  `DirectiveOnValuesIdentifier` bigint(20) NOT NULL AUTO_INCREMENT,
  `icmReleaseDirective` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`DirectiveOnValuesIdentifier`)
) ENGINE=InnoDB AUTO_INCREMENT=280 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `IcmDirectiveOnValues_ValueObject`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `IcmDirectiveOnValues_ValueObject` (
  `IcmDirectiveOnValues_DirectiveOnValuesIdentifier` bigint(20) NOT NULL,
  `valueObjectList_valueKey` bigint(20) NOT NULL,
  UNIQUE KEY `UK_leg6t4snl7sp20hk2bfwqmkvu` (`valueObjectList_valueKey`),
  KEY `FKgyhqw6heq5cy168fta38jwp41` (`IcmDirectiveOnValues_DirectiveOnValuesIdentifier`),
  CONSTRAINT `FK6shtsd7os4yw4tguh61miwhwi` FOREIGN KEY (`valueObjectList_valueKey`) REFERENCES `ValueObject` (`valueKey`),
  CONSTRAINT `FKgyhqw6heq5cy168fta38jwp41` FOREIGN KEY (`IcmDirectiveOnValues_DirectiveOnValuesIdentifier`) REFERENCES `IcmDirectiveOnValues` (`DirectiveOnValuesIdentifier`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `IcmInfoReleaseStatement`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `IcmInfoReleaseStatement` (
  `OIRKey` bigint(20) NOT NULL AUTO_INCREMENT,
  `allOtherValuesConst` varchar(255) DEFAULT NULL,
  `icmReleaseDirective` varchar(255) DEFAULT NULL,
  `infoType` varchar(255) DEFAULT NULL,
  `infoValue` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`OIRKey`)
) ENGINE=InnoDB AUTO_INCREMENT=199 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `IcmInfoReleaseStatement_IcmDirectiveOnValues`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `IcmInfoReleaseStatement_IcmDirectiveOnValues` (
  `IcmInfoReleaseStatement_OIRKey` bigint(20) NOT NULL,
  `arrayOfIcmDirectiveOnValues_DirectiveOnValuesIdentifier` bigint(20) NOT NULL,
  UNIQUE KEY `UK_2w6b6nm2f6tludmh64x7mhwqg` (`arrayOfIcmDirectiveOnValues_DirectiveOnValuesIdentifier`),
  KEY `FK6jy573x31kk024qw1n330u4nt` (`IcmInfoReleaseStatement_OIRKey`),
  CONSTRAINT `FK6jy573x31kk024qw1n330u4nt` FOREIGN KEY (`IcmInfoReleaseStatement_OIRKey`) REFERENCES `IcmInfoReleaseStatement` (`OIRKey`),
  CONSTRAINT `FKoqcbekgqfigm7ei18mq4vx71u` FOREIGN KEY (`arrayOfIcmDirectiveOnValues_DirectiveOnValuesIdentifier`) REFERENCES `IcmDirectiveOnValues` (`DirectiveOnValuesIdentifier`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `IcmReturnedPolicy`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `IcmReturnedPolicy` (
  `ReturnedPolicyIdentifier` bigint(20) NOT NULL AUTO_INCREMENT,
  `allOtherInfoType` varchar(255) DEFAULT NULL,
  `allOtherInfoValue` varchar(255) DEFAULT NULL,
  `allOtherValuesConst` varchar(255) DEFAULT NULL,
  `icmReleaseDirective` varchar(255) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `RHType` varchar(255) DEFAULT NULL,
  `RHValue` varchar(255) DEFAULT NULL,
  `createTime` bigint(20) NOT NULL,
  `userType` varchar(255) DEFAULT NULL,
  `userValue` varchar(255) DEFAULT NULL,
  `baseId` varchar(255) DEFAULT NULL,
  `version` varchar(255) DEFAULT NULL,
  `state` int(11) DEFAULT NULL,
  `supersedingId` varchar(255) DEFAULT NULL,
  `supersedingVersion` varchar(255) DEFAULT NULL,
  `priority` bigint(20) NOT NULL,
  PRIMARY KEY (`ReturnedPolicyIdentifier`),
  KEY `i_state` (`state`),
  KEY `i_rh` (`RHType`,`RHValue`)
) ENGINE=InnoDB AUTO_INCREMENT=112 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `IcmReturnedPolicy_IcmInfoReleaseStatement`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `IcmReturnedPolicy_IcmInfoReleaseStatement` (
  `IcmReturnedPolicy_ReturnedPolicyIdentifier` bigint(20) NOT NULL,
  `arrayOfInfoReleaseStatement_OIRKey` bigint(20) NOT NULL,
  UNIQUE KEY `UK_gu28n5sburvm22w6s8uxk2t0d` (`arrayOfInfoReleaseStatement_OIRKey`),
  KEY `FK9luqnxya9vjwdmk61oopcu2s9` (`IcmReturnedPolicy_ReturnedPolicyIdentifier`),
  CONSTRAINT `FK9luqnxya9vjwdmk61oopcu2s9` FOREIGN KEY (`IcmReturnedPolicy_ReturnedPolicyIdentifier`) REFERENCES `IcmReturnedPolicy` (`ReturnedPolicyIdentifier`),
  CONSTRAINT `FKctridk32fvxr3mqt3hlos4l4w` FOREIGN KEY (`arrayOfInfoReleaseStatement_OIRKey`) REFERENCES `IcmInfoReleaseStatement` (`OIRKey`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `IcmReturnedPolicy_RelyingPartyProperty`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `IcmReturnedPolicy_RelyingPartyProperty` (
  `IcmReturnedPolicy_ReturnedPolicyIdentifier` bigint(20) NOT NULL,
  `relyingPartyPropertyArray_relyingPartyIdKey` bigint(20) NOT NULL,
  UNIQUE KEY `UK_1w1ngfveldf4is6whi17csgbo` (`relyingPartyPropertyArray_relyingPartyIdKey`),
  KEY `FKgfij76y7hhrb1m4vqy6bju44j` (`IcmReturnedPolicy_ReturnedPolicyIdentifier`),
  CONSTRAINT `FKgfij76y7hhrb1m4vqy6bju44j` FOREIGN KEY (`IcmReturnedPolicy_ReturnedPolicyIdentifier`) REFERENCES `IcmReturnedPolicy` (`ReturnedPolicyIdentifier`),
  CONSTRAINT `FKq6trbtjf6sv6iu1aerydw4mju` FOREIGN KEY (`relyingPartyPropertyArray_relyingPartyIdKey`) REFERENCES `RelyingPartyProperty` (`relyingPartyIdKey`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `IcmReturnedPolicy_UserProperty`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `IcmReturnedPolicy_UserProperty` (
  `IcmReturnedPolicy_ReturnedPolicyIdentifier` bigint(20) NOT NULL,
  `userPropertyArray_userPropertyKey` bigint(20) NOT NULL,
  UNIQUE KEY `UK_9j3bn8u5fihcqyy7ipbt8nyi2` (`userPropertyArray_userPropertyKey`),
  KEY `FK7xf90xyij4jtk2tivm8vm7vbn` (`IcmReturnedPolicy_ReturnedPolicyIdentifier`),
  CONSTRAINT `FK7xf90xyij4jtk2tivm8vm7vbn` FOREIGN KEY (`IcmReturnedPolicy_ReturnedPolicyIdentifier`) REFERENCES `IcmReturnedPolicy` (`ReturnedPolicyIdentifier`),
  CONSTRAINT `FKt41a6tynbg55v01rbbstfg0pw` FOREIGN KEY (`userPropertyArray_userPropertyKey`) REFERENCES `UserProperty` (`userPropertyKey`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `InfoItemInformedContent`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `InfoItemInformedContent` (
  `RPICIdentifier` bigint(20) NOT NULL AUTO_INCREMENT,
  `displayName` varchar(255) DEFAULT NULL,
  `infoName` varchar(255) DEFAULT NULL,
  `infoValue` varchar(255) DEFAULT NULL,
  `modality` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`RPICIdentifier`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `InfoItemInformedContent_vocabulary`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `InfoItemInformedContent_vocabulary` (
  `InfoItemInformedContent_RPICIdentifier` bigint(20) NOT NULL,
  `vocabulary` varchar(255) DEFAULT NULL,
  KEY `FKnyl13wie6h7qx1geg866cuujq` (`InfoItemInformedContent_RPICIdentifier`),
  CONSTRAINT `FKnyl13wie6h7qx1geg866cuujq` FOREIGN KEY (`InfoItemInformedContent_RPICIdentifier`) REFERENCES `InfoItemInformedContent` (`RPICIdentifier`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `RelyingPartyInformedContent`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `RelyingPartyInformedContent` (
  `RPICIdentifier` bigint(20) NOT NULL AUTO_INCREMENT,
  `accessUrl` varchar(255) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `displayName` varchar(255) DEFAULT NULL,
  `iconUrl` varchar(255) DEFAULT NULL,
  `privacyPolicyUrl` varchar(255) DEFAULT NULL,
  `rpType` varchar(255) DEFAULT NULL,
  `rpValue` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`RPICIdentifier`)
) ENGINE=InnoDB AUTO_INCREMENT=67 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `RelyingPartyInformedContent_displayMode`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `RelyingPartyInformedContent_displayMode` (
  `RelyingPartyInformedContent_RPICIdentifier` bigint(20) NOT NULL,
  `displayMode` varchar(255) DEFAULT NULL,
  `displayMode_KEY` varchar(255) NOT NULL,
  PRIMARY KEY (`RelyingPartyInformedContent_RPICIdentifier`,`displayMode_KEY`),
  CONSTRAINT `FKkqdh92dpa3e5alr15bsgu236m` FOREIGN KEY (`RelyingPartyInformedContent_RPICIdentifier`) REFERENCES `RelyingPartyInformedContent` (`RPICIdentifier`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `RelyingPartyInformedContent_optionalAttributes`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `RelyingPartyInformedContent_optionalAttributes` (
  `RelyingPartyInformedContent_RPICIdentifier` bigint(20) NOT NULL,
  `optionalAttributes` varchar(255) DEFAULT NULL,
  KEY `FK4a6mcncsjsarqqgdlm00709vs` (`RelyingPartyInformedContent_RPICIdentifier`),
  CONSTRAINT `FK4a6mcncsjsarqqgdlm00709vs` FOREIGN KEY (`RelyingPartyInformedContent_RPICIdentifier`) REFERENCES `RelyingPartyInformedContent` (`RPICIdentifier`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `RelyingPartyInformedContent_reasonMap`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `RelyingPartyInformedContent_reasonMap` (
  `RelyingPartyInformedContent_RPICIdentifier` bigint(20) NOT NULL,
  `reasonMap` varchar(255) DEFAULT NULL,
  `reasonMap_KEY` varchar(255) NOT NULL,
  PRIMARY KEY (`RelyingPartyInformedContent_RPICIdentifier`,`reasonMap_KEY`),
  CONSTRAINT `FK47pgmml50jkc4r46gosu20r0v` FOREIGN KEY (`RelyingPartyInformedContent_RPICIdentifier`) REFERENCES `RelyingPartyInformedContent` (`RPICIdentifier`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `RelyingPartyInformedContent_requiredAttributes`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `RelyingPartyInformedContent_requiredAttributes` (
  `RelyingPartyInformedContent_RPICIdentifier` bigint(20) NOT NULL,
  `requiredAttributes` varchar(255) DEFAULT NULL,
  KEY `FKc377kgkeakio9419nu52nkkvr` (`RelyingPartyInformedContent_RPICIdentifier`),
  CONSTRAINT `FKc377kgkeakio9419nu52nkkvr` FOREIGN KEY (`RelyingPartyInformedContent_RPICIdentifier`) REFERENCES `RelyingPartyInformedContent` (`RPICIdentifier`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `RelyingPartyProperty`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `RelyingPartyProperty` (
  `relyingPartyIdKey` bigint(20) NOT NULL AUTO_INCREMENT,
  `rpPropName` varchar(255) DEFAULT NULL,
  `rpPropValue` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`relyingPartyIdKey`)
) ENGINE=InnoDB AUTO_INCREMENT=112 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `UserProperty`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `UserProperty` (
  `userPropertyKey` bigint(20) NOT NULL AUTO_INCREMENT,
  `userPropName` varchar(255) DEFAULT NULL,
  `userPropValue` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`userPropertyKey`)
) ENGINE=InnoDB AUTO_INCREMENT=112 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ValueObject`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `ValueObject` (
  `valueKey` bigint(20) NOT NULL,
  `value` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`valueKey`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hibernate_sequence`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `hibernate_sequence` (
  `next_val` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

INSERT INTO hibernate_sequence VALUES(1000);
--
-- Current Database: `informed`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `informed` default character set utf8mb4 default collate utf8mb4_0900_ai_ci /*!40100 DEFAULT CHARACTER SET utf8mb4 */;

USE `informed`;

--
-- Table structure for table `ActivityStreamEntry`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `ActivityStreamEntry` (
  `aseid` bigint(20) NOT NULL AUTO_INCREMENT,
  `type` varchar(255) DEFAULT NULL,
  `timestamp` bigint(20) DEFAULT NULL,
  `user` varchar(255) DEFAULT NULL,
  `operation` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`aseid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `AdminRoleMapping`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `AdminRoleMapping` (
  `adminRoleId` bigint(20) NOT NULL AUTO_INCREMENT,
  `subject` varchar(255) DEFAULT NULL,
  `roleName` varchar(255) DEFAULT NULL,
  `target` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `created` bigint(20) DEFAULT NULL,
  `archived` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`adminRoleId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `InfoItemIdentifier`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `InfoItemIdentifier` (
  `iiiid` bigint(20) NOT NULL AUTO_INCREMENT,
  `iiid` varchar(255) DEFAULT NULL,
  `iitype` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`iiiid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `InfoItemValueList`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `InfoItemValueList` (
  `ivlid` bigint(20) NOT NULL AUTO_INCREMENT,
  `sourceitemname` varchar(255) DEFAULT NULL,
  `infoitemidentifier_iiiid` bigint(20) DEFAULT NULL,
  `reason_istringid` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`ivlid`),
  KEY `FKs5bej9hpwlvk5h53his5i6hmb` (`infoitemidentifier_iiiid`),
  KEY `FKnbihf7wkaeenm73en4wag0si2` (`reason_istringid`),
  CONSTRAINT `FKnbihf7wkaeenm73en4wag0si2` FOREIGN KEY (`reason_istringid`) REFERENCES `InternationalizedString` (`istringid`),
  CONSTRAINT `FKs5bej9hpwlvk5h53his5i6hmb` FOREIGN KEY (`infoitemidentifier_iiiid`) REFERENCES `InfoItemIdentifier` (`iiiid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `InfoItemValueList_valuelist`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `InfoItemValueList_valuelist` (
  `InfoItemValueList_ivlid` bigint(20) NOT NULL,
  `valuelist` varchar(255) DEFAULT NULL,
  KEY `FKbkq7u4wggbyldm7vrct29dwek` (`InfoItemValueList_ivlid`),
  CONSTRAINT `FKbkq7u4wggbyldm7vrct29dwek` FOREIGN KEY (`InfoItemValueList_ivlid`) REFERENCES `InfoItemValueList` (`ivlid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `InternationalizedString`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `InternationalizedString` (
  `istringid` bigint(20) NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`istringid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `InternationalizedString_LocaleString`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `InternationalizedString_LocaleString` (
  `InternationalizedString_istringid` bigint(20) NOT NULL,
  `locales_localstringid` bigint(20) NOT NULL,
  UNIQUE KEY `UK_6a7h6et8sw9str3ynhqlgm876` (`locales_localstringid`),
  KEY `FKllwy6kvg456vnv2trn9fo4rit` (`InternationalizedString_istringid`),
  CONSTRAINT `FKllwy6kvg456vnv2trn9fo4rit` FOREIGN KEY (`InternationalizedString_istringid`) REFERENCES `InternationalizedString` (`istringid`),
  CONSTRAINT `FKpanpn1k8r9t37lgyxw2lq6os2` FOREIGN KEY (`locales_localstringid`) REFERENCES `LocaleString` (`localstringid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `LocaleString`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `LocaleString` (
  `localstringid` bigint(20) NOT NULL AUTO_INCREMENT,
  `locale` varchar(255) DEFAULT NULL,
  `value` varchar(4000) DEFAULT NULL,
  PRIMARY KEY (`localstringid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `RPIdentifier`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `RPIdentifier` (
  `rpiid` bigint(20) NOT NULL AUTO_INCREMENT,
  `rpid` varchar(255) DEFAULT NULL,
  `rptype` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`rpiid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ReturnedInfoItemMetaInformation`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `ReturnedInfoItemMetaInformation` (
  `riimiid` bigint(20) NOT NULL AUTO_INCREMENT,
  `iimode` int(11) DEFAULT NULL,
  `rhid` varchar(255) DEFAULT NULL,
  `rhtype` varchar(255) DEFAULT NULL,
  `description_istringid` bigint(20) DEFAULT NULL,
  `displayname_istringid` bigint(20) DEFAULT NULL,
  `iiidentifier_iiiid` bigint(20) DEFAULT NULL,
  `presentationtype` varchar(255) DEFAULT NULL,
  `policytype` varchar(255) DEFAULT NULL,
  `sensitivity` bit(1) DEFAULT NULL,
  `asnd` bit(1) DEFAULT NULL,
  `multivalued` bit(1) DEFAULT NULL,
  `version` int(11) DEFAULT NULL,
  `updated` bigint(20) DEFAULT NULL,
  `state` varchar(255) DEFAULT NULL,
  `httpHeader` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`riimiid`),
  KEY `FKboqxryfgn0mcwf6qchf0aaj92` (`description_istringid`),
  KEY `FKm7s9tvqt4ymtmfcomt7117kdk` (`displayname_istringid`),
  KEY `FKpue2lqy0x012g1jvmq0ym6nyf` (`iiidentifier_iiiid`),
  KEY `i_state` (`state`),
  KEY `i_rh` (`rhtype`,`rhid`),
  CONSTRAINT `FKboqxryfgn0mcwf6qchf0aaj92` FOREIGN KEY (`description_istringid`) REFERENCES `InternationalizedString` (`istringid`),
  CONSTRAINT `FKm7s9tvqt4ymtmfcomt7117kdk` FOREIGN KEY (`displayname_istringid`) REFERENCES `InternationalizedString` (`istringid`),
  CONSTRAINT `FKpue2lqy0x012g1jvmq0ym6nyf` FOREIGN KEY (`iiidentifier_iiiid`) REFERENCES `InfoItemIdentifier` (`iiiid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ReturnedInfoTypeList`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `ReturnedInfoTypeList` (
  `typelistID` bigint(20) NOT NULL AUTO_INCREMENT,
  `rhtype` varchar(255) DEFAULT NULL,
  `rhvalue` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`typelistID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ReturnedInfoTypeList_infotypes`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `ReturnedInfoTypeList_infotypes` (
  `ReturnedInfoTypeList_typelistID` bigint(20) NOT NULL,
  `infotypes` varchar(255) DEFAULT NULL,
  KEY `FKqtstd6ra99xu7608r8nv8uw5n` (`ReturnedInfoTypeList_typelistID`),
  CONSTRAINT `FKqtstd6ra99xu7608r8nv8uw5n` FOREIGN KEY (`ReturnedInfoTypeList_typelistID`) REFERENCES `ReturnedInfoTypeList` (`typelistID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ReturnedRHInfoItemList`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `ReturnedRHInfoItemList` (
  `infoitemlistid` bigint(20) NOT NULL AUTO_INCREMENT,
  `rhid` varchar(255) DEFAULT NULL,
  `rhtype` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`infoitemlistid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ReturnedRHInfoItemList_InfoItemIdentifier`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `ReturnedRHInfoItemList_InfoItemIdentifier` (
  `ReturnedRHInfoItemList_infoitemlistid` bigint(20) NOT NULL,
  `infoitemlist_iiiid` bigint(20) NOT NULL,
  UNIQUE KEY `UK_a8y9v3g5bkxsxo3166o5sv455` (`infoitemlist_iiiid`),
  KEY `FKlmflnk1if4o1f0me97y1ijafy` (`ReturnedRHInfoItemList_infoitemlistid`),
  CONSTRAINT `FK9ffftn9nr3ej2v0nauemobrjf` FOREIGN KEY (`infoitemlist_iiiid`) REFERENCES `InfoItemIdentifier` (`iiiid`),
  CONSTRAINT `FKlmflnk1if4o1f0me97y1ijafy` FOREIGN KEY (`ReturnedRHInfoItemList_infoitemlistid`) REFERENCES `ReturnedRHInfoItemList` (`infoitemlistid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ReturnedRHMetaInformation`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `ReturnedRHMetaInformation` (
  `rhmetainfoid` bigint(20) NOT NULL AUTO_INCREMENT,
  `rhid` varchar(255) DEFAULT NULL,
  `rhtype` varchar(255) DEFAULT NULL,
  `description_istringid` bigint(20) DEFAULT NULL,
  `displayname_istringid` bigint(20) DEFAULT NULL,
  `version` int(11) DEFAULT NULL,
  `updated` bigint(20) DEFAULT NULL,
  `state` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`rhmetainfoid`),
  KEY `FKpfohhgxugfduxmkiaof2xk4tw` (`description_istringid`),
  KEY `FKm1rthcv7cws3hs6n8si3aqpuw` (`displayname_istringid`),
  CONSTRAINT `FKm1rthcv7cws3hs6n8si3aqpuw` FOREIGN KEY (`displayname_istringid`) REFERENCES `InternationalizedString` (`istringid`),
  CONSTRAINT `FKpfohhgxugfduxmkiaof2xk4tw` FOREIGN KEY (`description_istringid`) REFERENCES `InternationalizedString` (`istringid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ReturnedRHRPList`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `ReturnedRHRPList` (
  `rplistid` bigint(20) NOT NULL AUTO_INCREMENT,
  `rhid` varchar(255) DEFAULT NULL,
  `rhtype` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`rplistid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ReturnedRHRPList_RPIdentifier`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `ReturnedRHRPList_RPIdentifier` (
  `ReturnedRHRPList_rplistid` bigint(20) NOT NULL,
  `rplist_rpiid` bigint(20) NOT NULL,
  UNIQUE KEY `UK_ekqpxwi4ienhminy87lkcefu5` (`rplist_rpiid`),
  KEY `FKmttley4qyg286ia9ojbmd1k0t` (`ReturnedRHRPList_rplistid`),
  CONSTRAINT `FK2lwxkpvmqmko9kj6nbc9s9yvj` FOREIGN KEY (`rplist_rpiid`) REFERENCES `RPIdentifier` (`rpiid`),
  CONSTRAINT `FKmttley4qyg286ia9ojbmd1k0t` FOREIGN KEY (`ReturnedRHRPList_rplistid`) REFERENCES `ReturnedRHRPList` (`rplistid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ReturnedRPMetaInformation`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `ReturnedRPMetaInformation` (
  `rpmiid` bigint(20) NOT NULL AUTO_INCREMENT,
  `iconurl` text(40000) DEFAULT NULL,
  `privacyurl` varchar(4000) DEFAULT NULL,
  `rhid` varchar(255) DEFAULT NULL,
  `rhtype` varchar(255) DEFAULT NULL,
  `description_istringid` bigint(20) DEFAULT NULL,
  `displayname_istringid` bigint(20) DEFAULT NULL,
  `rpidentifier_rpiid` bigint(20) DEFAULT NULL,
  `version` int(11) DEFAULT NULL,
  `updated` bigint(20) DEFAULT NULL,
  `state` varchar(255) DEFAULT NULL,
  `defaultshowagain` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`rpmiid`),
  KEY `FKtjsoyssb1bl058quduwnjliot` (`description_istringid`),
  KEY `FKl5i6yw2qk239q6j0k6c4q8xnl` (`displayname_istringid`),
  KEY `FK3n55v5hgncryktyrnguxpf13g` (`rpidentifier_rpiid`),
  KEY `i_state` (`state`),
  CONSTRAINT `FK3n55v5hgncryktyrnguxpf13g` FOREIGN KEY (`rpidentifier_rpiid`) REFERENCES `RPIdentifier` (`rpiid`),
  CONSTRAINT `FKl5i6yw2qk239q6j0k6c4q8xnl` FOREIGN KEY (`displayname_istringid`) REFERENCES `InternationalizedString` (`istringid`),
  CONSTRAINT `FKtjsoyssb1bl058quduwnjliot` FOREIGN KEY (`description_istringid`) REFERENCES `InternationalizedString` (`istringid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ReturnedRPMetaInformation_ReturnedRPProperty`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `ReturnedRPMetaInformation_ReturnedRPProperty` (
  `ReturnedRPMetaInformation_rpmiid` bigint(20) NOT NULL,
  `rpproperties_rppropertyid` bigint(20) NOT NULL,
  UNIQUE KEY `UK_845ef50s62hlaskj1mseqw75p` (`rpproperties_rppropertyid`),
  KEY `FK93jg1ebv2uex1qc3hsq0jc363` (`ReturnedRPMetaInformation_rpmiid`),
  CONSTRAINT `FK1lcoyklnipggx8y8g02giw8jl` FOREIGN KEY (`rpproperties_rppropertyid`) REFERENCES `ReturnedRPProperty` (`rppropertyid`),
  CONSTRAINT `FK93jg1ebv2uex1qc3hsq0jc363` FOREIGN KEY (`ReturnedRPMetaInformation_rpmiid`) REFERENCES `ReturnedRPMetaInformation` (`rpmiid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ReturnedRPOptionalInfoItemList`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `ReturnedRPOptionalInfoItemList` (
  `infoitemlistid` bigint(20) NOT NULL AUTO_INCREMENT,
  `rhid` varchar(255) DEFAULT NULL,
  `rhtype` varchar(255) DEFAULT NULL,
  `rpidentifier_rpiid` bigint(20) DEFAULT NULL,
  `version` int(11) DEFAULT NULL,
  `updated` bigint(20) DEFAULT NULL,
  `state` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`infoitemlistid`),
  KEY `FKqidvt2vaw8vngerkwvkq9xhbm` (`rpidentifier_rpiid`),
  CONSTRAINT `FKqidvt2vaw8vngerkwvkq9xhbm` FOREIGN KEY (`rpidentifier_rpiid`) REFERENCES `RPIdentifier` (`rpiid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ReturnedRPOptionalInfoItemList_InfoItemValueList`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `ReturnedRPOptionalInfoItemList_InfoItemValueList` (
  `ReturnedRPOptionalInfoItemList_infoitemlistid` bigint(20) NOT NULL,
  `optionallist_ivlid` bigint(20) NOT NULL,
  UNIQUE KEY `UK_5u6pcy07vly94ow7xjnyomfle` (`optionallist_ivlid`),
  KEY `FKm9bn4hicsxgvr2sx69m2wbh3t` (`ReturnedRPOptionalInfoItemList_infoitemlistid`),
  CONSTRAINT `FKm9bn4hicsxgvr2sx69m2wbh3t` FOREIGN KEY (`ReturnedRPOptionalInfoItemList_infoitemlistid`) REFERENCES `ReturnedRPOptionalInfoItemList` (`infoitemlistid`),
  CONSTRAINT `FKsds19uchblabgcofglhycyrw7` FOREIGN KEY (`optionallist_ivlid`) REFERENCES `InfoItemValueList` (`ivlid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ReturnedRPProperty`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `ReturnedRPProperty` (
  `rppropertyid` bigint(20) NOT NULL AUTO_INCREMENT,
  `rppropertyname` varchar(255) DEFAULT NULL,
  `rppropertyvalue` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`rppropertyid`),
  KEY `i_propname` (`rppropertyname`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ReturnedRPRequiredInfoItemList`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `ReturnedRPRequiredInfoItemList` (
  `infoitemlistid` bigint(20) NOT NULL AUTO_INCREMENT,
  `rhid` varchar(255) DEFAULT NULL,
  `rhtype` varchar(255) DEFAULT NULL,
  `rpidentifier_rpiid` bigint(20) DEFAULT NULL,
  `version` int(11) DEFAULT NULL,
  `updated` bigint(20) DEFAULT NULL,
  `state` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`infoitemlistid`),
  KEY `FK3d4okkjx2odkbtcupqb2ioth3` (`rpidentifier_rpiid`),
  CONSTRAINT `FK3d4okkjx2odkbtcupqb2ioth3` FOREIGN KEY (`rpidentifier_rpiid`) REFERENCES `RPIdentifier` (`rpiid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ReturnedRPRequiredInfoItemList_InfoItemValueList`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `ReturnedRPRequiredInfoItemList_InfoItemValueList` (
  `ReturnedRPRequiredInfoItemList_infoitemlistid` bigint(20) NOT NULL,
  `requiredlist_ivlid` bigint(20) NOT NULL,
  UNIQUE KEY `UK_5jcu63ojlbin758fbvulpldh8` (`requiredlist_ivlid`),
  KEY `FK471pa666j5evcfhxas14tggok` (`ReturnedRPRequiredInfoItemList_infoitemlistid`),
  CONSTRAINT `FK471pa666j5evcfhxas14tggok` FOREIGN KEY (`ReturnedRPRequiredInfoItemList_infoitemlistid`) REFERENCES `ReturnedRPRequiredInfoItemList` (`infoitemlistid`),
  CONSTRAINT `FKpb5l3jv52ohan5itejw74tx3f` FOREIGN KEY (`requiredlist_ivlid`) REFERENCES `InfoItemValueList` (`ivlid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ReturnedUserRPMetaInformation`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `ReturnedUserRPMetaInformation` (
  `rumiid` bigint(20) NOT NULL AUTO_INCREMENT,
  `lastinteracted` bigint(20) NOT NULL,
  `showagain` bit(1) NOT NULL,
  `userid` varchar(255) DEFAULT NULL,
  `usertype` varchar(255) DEFAULT NULL,
  `rpidentifier_rpiid` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`rumiid`),
  KEY `FKltxg62qcp40ppayyocuwjexhf` (`rpidentifier_rpiid`),
  CONSTRAINT `FKltxg62qcp40ppayyocuwjexhf` FOREIGN KEY (`rpidentifier_rpiid`) REFERENCES `RPIdentifier` (`rpiid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ReturnedValueMetaInformation`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `ReturnedValueMetaInformation` (
  `vmiid` bigint(20) NOT NULL AUTO_INCREMENT,
  `displayname` varchar(255) DEFAULT NULL,
  `infoitemname` varchar(255) DEFAULT NULL,
  `infoitemvalue` varchar(255) DEFAULT NULL,
  `version` int(11) DEFAULT NULL,
  `updated` bigint(20) DEFAULT NULL,
  `state` varchar(255) DEFAULT NULL,
  `asnd` bit(1) DEFAULT NULL,
  `rhtype` varchar(255) DEFAULT NULL,
  `rhid` varchar(255) DEFAULT NULL,
  `infoitemtype` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`vmiid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `SupportedIIType`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `SupportedIIType` (
  `sitiid` bigint(20) NOT NULL AUTO_INCREMENT,
  `rhtype` varchar(255) DEFAULT NULL,
  `rhid` varchar(255) DEFAULT NULL,
  `iitype` varchar(255) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`sitiid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `SupportedLanguage`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `SupportedLanguage` (
  `sliid` bigint(20) NOT NULL AUTO_INCREMENT,
  `lang` varchar(255) DEFAULT NULL,
  `displayname` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`sliid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `SupportedRHType`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `SupportedRHType` (
  `srhiid` bigint(20) NOT NULL AUTO_INCREMENT,
  `rhtype` varchar(255) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`srhiid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `SupportedRPType`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `SupportedRPType` (
  `srpiid` bigint(20) NOT NULL AUTO_INCREMENT,
  `rhtype` varchar(255) DEFAULT NULL,
  `rhid` varchar(255) DEFAULT NULL,
  `rptype` varchar(255) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`srpiid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `SupportedUserType`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `SupportedUserType` (
  `sutid` bigint(20) NOT NULL AUTO_INCREMENT,
  `utype` varchar(255) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`sutid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hibernate_sequence`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `hibernate_sequence` (
  `next_val` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO hibernate_sequence VALUES(1000);
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2019-07-17 16:45:53
