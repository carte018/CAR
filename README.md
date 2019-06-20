# CAR
Consent Informed Attribute Release

CAR, the Consent Informed Attribute Release system, acts as a policy decision point (or PDP) for the sharing of personal information between systems. Given a system holding information about user (termed a "Resource Holder" or "RH") and a system wishing to receive some or all of that information (termed a "Relying Party" or "RP"), the CAR system can provide the RH with a set of decisions regarding the release of information about the user to the RP. These decisions can be based on policies articulated by administrators (on behalf of an organization or institution), policies articulated by users themselves (consent decisons), or a combination of both. In the process, CAR can provide end-users with visibility into the information releases taking place (both in the moment and at the user's leisure) between RHs and RPs, even if consent is not being employed.

As a standalone policy decision point, CAR is not uniquely wedded to any particular resource holder implementation or protocol.
Any system interested in managing the release of personal information in a flexible, consent-enabled fashion can reasonably make use of CAR as a PDP. One common use for CAR is as a mechanism for managing policies for and applying consent to attribute release in a SAML flow. Code is available here for integrating a Shibboleth v3 IDP with CAR for this purpose, although integrations with other SAML providers are expected to be available in coming months. Integrations with OIDC OPs and OAuth2 providers are in progress, and at Duke University, direct integration with at least one site-specific identity web service is already in production.

The current release is a pre-beta, code-only release intended to give early adopters an early look at the codebase and provide a basis for beginning a wider collaboration around the codebase. Future releases are expected to include Dockerized packaging to facilitate deployment in typical environments and much more robust documentation. The current release is NOT recommended for production use (although it is being actively used in a production pilot deployment at Duke University, where it was originally developed).

README.md files in the individual component trees explain the individual components in somewaht greater detail. In broad overview, the primary policy-handling components you will find here are:

* COPSU - the COnsent Policy Store for Users. The COPSU acts as a combined policy information, management, and decision point for user-articulated policies. It is not intended to be used directly, but in combination with the other two primary P{I,M,D}P components. The COPSU interacts with the rest of the system via REST-ish web services.

* ARPSI - the Attribute Release Policy Store for Institutions. The ARPSI acts as a combined policy information, management, and decision point for institutionally-articulated policies. Like the COPSU, it is not intended to be used directly. The ARPSI also interacts with the rest of the system via REST-ish web services.

* ICM - The ICM component actually implements the CARMA -- the CAR MAnager. It acts as a combined policy information, management, and decision point for institutionally managed meta-policies, and provides the endpoint for Resource Holders to retrieve policy decisions. CARMA meta-policies determine, for specific cases of {user, attribute/value pair, resource holder, and relying party} whether institutional or user/consent policy is controlling. The CARMA decision interface returns information release decisions based on meta-policy controlled combinations of user (COPSU) and institutional (ARPSI) policies. Like the COPSU and ARPSI, the CARMA provides a set of REST-ish web services.

Additional components of the full CAR distribution include:

* Informed - the Informed Content service. CAR strives to provide tools that make it possible for users to make informed consent decisions, and in order to facilitate that, CAR takes advantage of a wealth of meta-information about information items (attributes, etc.) and their values, relying parties, and resource holders. The Informed Content service provides a set of REST-ish web services for the registration of RHs and RPs, and for the retrieval of registration information.

* CAR - the CAR component provides the primary end-user facing user interfaces into the CAR system, including two primary interfaces:

  * the inline or "intercept" UI, used when a user is present in the webflow during a release decision request and consent is required or visibility is to be provided into information release as it happens.
  * the self-service interface, used when a user wishes to review and/or update his or her personal information release policies (eg., to revoke an existing consent decision, or review what has been consented to already)

* CARAdmin - the CAR Admin component provides the primary administrator-facing user interfaces into the CAR system, including interfaces for:

  * registering Resource Holders, along with their available information items
  * registering Relying Parties, their information item requests, and their informed content
  * managing institutional policies and meta-policies for Resource Holders
  * administering the CAR installation as a whole
  * RegLoader - The regloader component provides a tool for importing Relying Party registration information from SAML metadata aggregates. It can extract and register RPs, along with any informed content and/or attribute requests that are contained within the SAML metadata object. It can be used to initially register SAML relying parties from one or more SAML metadata files (eg., a local-sites.xml file from a Shibboleth IDP, or a metadata aggregate from a multilateral federation), as well as to update existing registrations based on information in SAML metadata.

ShibbolethIdpV3Integration - The IDPv3 Integration component is actually not a component of the CAR system itself, but rather an integration module for the Shibboleth v3 IDP. This particular integration module is designed to intercept the Shibboleth IDP flow after the IDP has resolved attribues but before the IDP's own release filter is applied. The integration point makes the appropriate call out to a specified CAR instance to determine which values of which attributes it has marshalled for the relying party it should release based on stored user, institutional, and meta-policies.