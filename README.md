# CAR - Licensing

The CAR system itself, comprising its core components, the delivered documentation for them, the associated build scripts and default configuration sets, and the delivered integration point for the Shibboleth v3 IDP are all Copyright Duke University, 2015-2019, and released under the GNU General Public License version 2 (GPL-2).  

The Shibboleth IDP, Apache httpd, Apache Tomcat, OpenLDAP, and other related software mentioned in or built alongside the CAR software, are the intellectual property of their respective licensers, and are made available separately, by them, under their own individual licenses.

# CAR - Overview
Consent Informed Attribute Release

CAR, the Consent-Informed Attribute Release system, acts as a policy decision point (or PDP) for the exchange of personal information between systems.  Given a system holding information about a user (termed a "Resource Holder" or "RH") and a system wishing to receive some or all of that information (termed a "Relying Party" or "RP"), the CAR system can provide the RH with a set of decisions regarding the release of information about the user to the RP.  These decisions can be based on policies articulated by the administrators (on behalf of an organization or institution), policies articulated by the affected users themselves (consent decisions), or a combination of the two.  In the process, CAR can provide end-users with visibility into the information releases taking place (both in the moment and at the user's leisure, through its self-service interface) between RH's and RPs, even if consent is not being employed.  CAR can store users' consent decisions for reuse and to resolve requests for decisions received when the affected user is not present in the workflow (eg., during a back-channel attribute request or in the case of certain OAuth scenarios). 

As a standalone policy decision point, CAR is not uniquely wedded to any particular resource holder implmentation or protocol.  Any system interested in managing the release of personal information in a flexible , consent-enabled fashion can reasonably make use of CAR as a PDP.  One common use for CAR is as a mechanism for managing policies for and applying consent to attribute release in a SAML flow.  Code is available here for integrating a Shibboleth v3 IDP with CAR for this purpose, although integrations with other resource holders (including other SAML identity providers) are expected to become available over time.  Integrations with OIDC OPs and OAuth2 providers are currently in development, and at Duke University, at least one site-specific, identity-focused RESTful web service has been integrated with CAR to allow consent-enabled release decisions to be used in determining what user information to release to specific requestors through the web service.

The current release is considered to be beta-quality code.  As of December, 2019, the code is still under active development, although most commits are feature enhancements or performance enhancements at this point.  We anticipate blessing a release candidate early in 2020, with a first production release to follow shortly after.

# CAR - Software components

In broad overview, the the primary source components you will find here are:

* <b>COPSU</b> - the COnsent Policy Store for Users.  The COPSU acts as a combined policy information, management and decision point for user-articulated (consent) policies.  It is not intended to be used directly, but in combination with the other components of the CAR system.  It exposes a REST-ish interface for manipulation of user policies and for requesting release decisions based on them.

* <b>ARPSI</b> - the Attribute Release Policy Store for Institutions.  The ARPSI is the institutional policy analogue of the COPSU.  It acts as a combined policy information, management adn decision point for institutionally-articulated information release policies.  Like the COPSU, it is not intended to be used directly, but through its reflection via the ICM component.  It exposes REST-ish interfaces for manipulation of institutional policies and for requesting release decisions based on them.

* <b>ICM</b> - the ICM component implements the CARMA -- the CAR MAnager.  It acts as a combined policy information, management and decision point for institutionally-managed meta-policies (policies which describe the circumstances under which institutional policies take precedence over user consent decisions, and vice-versa), and provides the endpoint for Resource Holders to retrieve comprehensive policy decisions.  CARMA meta-policies determine, for specific cases of {user, attribute/value pair, resource holder, and relying party} whether institutional or user policy is controlling.  The CARMA decision interface returns information release decisions based on meta-policy-controlled combinations of decisions rendered by the associated COPSU and ARPSI.  Like the COPSU and ARPSI, the CARMA provides a set of REST-ish web services to manage its meta-policies and request decisions.  In addition, the CARMA provides pass-thru reflections of the COPSU and ARPSI REST-ish APIs.  In normal use, it is expected that all client requests will be directed at these reflections, rather than directly at the COPSU and ARPSI, and that only the ICM component will typically interact directly with the COPSU and ARPSI.

* <b>Informed</b> - the Informed Content service.  CAR takes great care to provide tools making it possible for users to make informed decisions about the release of thier information in different contexts, and in order to facilitate that, CAR takes advantage of a wealth of meta-information about information items and their values, relying parties, and resource holders.  The Informed content service provides REST-ish interfaces of the registration and management of RHs and RPs and for the retrieval of registration information, as well as for the manipulation and management of other "informed content" needed to provide functional UIs for end users.

* <b>CAR</b> - the CAR component provides the primary end-user facing user interfaces into the CAR system, including two primary interfaces:

  * the inline of "intercept" UI, used when a user is present in the webflow during a release decision request and consent or notification are required by the appropriate policies.
  * the self-service interface, used when a user wishes to review and/or update his or her personal information release policies (eg., to revoke an existing consent decision, or review what has previously been consented to)

* <b>CARAdmin</b> - the CAR Admin component provides the primary administrator-facing user interfaces into the CAR system, including interfaces for:

  * registering Resource Holders, along with information about the information items they can provide
  * registering Relying Parties, along with the information items they request and their informed content
  * managing institutional policies and meta-policies for Resource Holders
  * administering the CAR installation as a whole, including delegating administrative rights and managing content

* <b>RegLoader</b> - CAR is not, itself, wedded to any particular information sharing mechanism or system  -- it is a standalone decision rendering service.  Nevertheless, CAR is of particular interest to certain specific communities, among them, the federated Research and Education community.  Recognizing the needs of those communities, CAR provides the regloader component to facilitate capture and re-use of metainformation already available in SAML metadata.  The regloader tool can extract RP registration information directly from a SAML metadata aggregate, along with any informed content and/or attribute request information provided in the metadata, and use it to populate and/or update a CAR registration repository. 

* <b>ShibbolethIdpV3Integration</b> - The IDPv3 Integration component is actually not a component of the CAR system itself, but rather an integration module for the Shibboleth v3 IDP.  In addition to being a comparatively simple example of an integration point for an RH to consume decisions rendered by CAR, it provides for the simple integration of an existing Shibboleth v3 IDP with a CAR instance.  This particular integration module is designed to intercept the IDP flow after the IDP has resolve attributes and before the IDP's own release filter is applied.  The integration point makes the appropriate call out to a configured CAR instance to determine which values of which attributes it has marshalled for the relying party it should release based on the combination of user, institutional, and meta-policies managed by CAR.  It can be deployed universally (in which case all release decisions are processed by the CAR instance) or on a per-SP basis (in which case releases to some SPs may be CAR-controlled and others may not).

# CAR - build options

All the CAR components are written in Java 11, as servlets, and built using Maven.  While most of the code uses no special features unavailable in Java 8, due to incompatibilities and deprecations introduced between Java 8 and Java 11, the code will probably only build and execute properly in a Java 11 (or higher) environment. The ShibbolethIdpV3Integration code is necessarily designed for Java 8, as it is intended to be integrated with the Shibboleth v3 IDP which, itself, is packaged for Java 8.

The CAR components are fully Dockerizable, and in fact, Docker is the preferred deployment mechanism for them in most cases.  They can be run outside Docker containers, of course, but the delivered build and install scripts focus on supporting Dockerized deployments.

All builds start with cloning the CAR project from GitHub:

  * git clone https://github.com/carte018/CAR

The fact that you're reading this document suggests you may have already done this. 

There are four broad strategies available for bulding CAR, depending on your needs.  In increasing order of complexity (and flexibility), they are:

  * A "skunkworks" build which produces, on a single server, both a fully configured CAR instance running across multiple, interconnected Docker containers, and a minimal Dockerized infrastructure (consisting of a Shibboleth IDP and associated LDAP, along with a set of simple relying parties configured as Shibboleth SPs, all integrated with the CAR instance).  The resulting system simulates an organization ("Castle Amber") operating an IDP in a "multiverse" federation.  The build deploys a set of characteristic policies into the CAR instance, and pre-populates some test users in the embedded infrastructure.  It also provides a utility for adding users with "canned" identities to the infrastructure.  It's a good option for those interested in evaluating the software and also for those interested in participating in development (with or without parallel production or pre-production deployments). Jump to [skunkworks](#skunkworks) build instructions.

  * A scripted, dockerized build of just the CAR components together on a single system, for those wishing to build a single-server CAR installation for testing in an existing test of development infrastructure.  This strategy requires no prior knowledge of Docker, but does require that you have deep knowledge of your existing infrastructure and the ability to modify it to integrate with the CAR instance. Jump to [single-sever](#single-server) build instructions.

  * A standalone CAR container build strategy, for building unconfigured Docker containers suitable for configuring and deploying across any number of Docker servers.  This strategy requires some prior knowledge of Docker in order to manage configuration, and assumes that you will be integrating the resulting CAR components with existing infrastructure. Jump to [just-car](#just-car) build instructions.

  * A totally manual, non-dockerized build strategy (essentially, just using Maven to build each of the component WAR files manually), suitable for those requiring absolute control over the build process and/or not prepared to take advantage of the Dockerized builds.  Documentation for fully manual builds is sparse, and support for this strategy is negligible, so it's only recommended for those willing to wade deeply into the internals of the CAR system, but it can offer the greatest flexibility of all the build options. Jump to [manual](#manual) build instructions.

With the exception of the manual build strategy, the only prerequisite for the first four build strategies is installation of the Docker engine and (on platforms where it is packaged separately) the docker-compose utility.  For the manual build strategy, the prerequisites are a compliant Java 11 JDK (the Dockerized builds use Azul Zulu 11, but any comparable Java 11 JDK based on the OpenJDK release should work) and Maven (for building the components).

Details of each of these options appear in sections below.

#### Skunkworks Build <a name="skunkworks"></a>

* The skunkworks build is the simplest of the build strategies and is recommended for those interested in seeing how the CAR system behaves and how it's put together without committing to an external integration(s) and/or any site-specific customizations.  The end product is a self-contained, single-server  demo environment containing:

  * a full CAR installation, complete with UIs and back-end services, backed by a clustered set of three MySQL Docker containers and integrated (for authentication) bilaterally with a local IDP (see below)
  * a Shibboleth IDP and associated LDAP server bound to the fictional "amber.org" domain and integrated with the CAR installation
  * a set of demo relying parties suitable for testing in the demo environment
  * a set of users populated in the IDP/LDAP with attributes suitable for demonstrating various capabilities of the CAR system
  * an interface for building additional sets of demo users with similar attributes for multi-user testing

    The skunkworks build constructs a fictional federation called the Multiverse in which a fictional organization (Castle Amber) operates a CAR instance and an IDP that depends on it, along with a handful of fictional relying parties.  It is intended for deployment on either a local client (a laptop, for example) or a restricted-access server (which may be on-prem or cloud-hosted).  Minimal requirements for the build include:

  * Docker engine (and if packaged separately in your OS environment, docker-compose)
  * Minimally 5 G of disk storage
  * At least 2G of RAM, preferably at least 4G (more may afford better performance)
  * Preferably at least 2 CPU cores (more may afford better performance)

    The skunkworks build does not employ strong authentication to protect CAR API calls, and as such, should not be used to initialize any production deployment.  Assume that any system built with the skunkworks build strategy will be trivially insecure and suitable only for demonstration and testing purposes.

    To begin a skunkworks build, cd into the "docker" subdirectory of the GitHub project and run the build-and-start-car.sh script with the "-k" flag (for "skunkworks"):

  * ./build-and-start-car.sh -k

    You will be prompted to enter a handful of items to configure your skunkworks build, including:

  * <b>FQDN of the server you're deploying to.</b>  This is the FQDN of the machine you are building on, relative to your end users' clients.  If you use "localhost" (as is common), you will only be able to access the skunkworks environment from browsers running on the local machine.  If you use the official FQDN of the machine, you will be able to access the skunkworks environment from anywhere (modulo firewalls, etc.).

  * <b>Database credentials.</b>  You may simply use the defaults here, or if you prefer, select your own credentials for the skunkworks database.

  * <b>Apache server name.</b>  Since the Apache server will be running on the same host as everything else, it's usually optimal to use the default (which should match what you chose for FQDN above).  if you know that you need to use a different name for your Apache server, however, you can enter it here.

  * <b>SP Entity ID.</b>  Usually, you'll want to use the default here, but if you have a strong preference for a different entity ID, you may override the default.

   * <b>Institution name (short and long).</b>  You'll probably want to use the default values ("Amber" and "Amber") here, but if you prefer to change them, you may.

    The build process will then construct and start the requisite Docker containers to build a fully functional federated CAR instance in a box.  At the end of the process, you will have the following containers running:

  * <b>docker\_node1\_1</b>
  * <b>docker\_node2\_1</b>
  * <b>docker\_node3\_1</b>	-	Three-way MySQL/Galera cluster hosting the various CAR databases, pre-configured with the CAR DB schema and well-known schema users and credentials.
  * <b>docker\_copsunode\_1</b>	-	COPSU instance integrated with the local MySQL/Galera cluster
  * <b>docker\_arpsinode\_1</b>	-	ARPSI instance integrated with the local MySQL/Galera cluster
  * <b>docker\_icmnode\_1</b>	-	CARMA instance integrated with the local MySQL/Galera cluster and populated with a public key for the Amber IDP (see below)
  * <b>docker\_informednode\_1</b> -	INFORMED content service instance integrated with the local MySQL/Galera cluster and populated with registration information for the Amber IDP and its federated demo RPs
  * <b>docker\_carnode\_1</b>	    -	user-facing CAR UIs
  * <b>docker\_caradminnode\_1</b> -	CAR admin UIs
  * <b>docker\_idpnode\_1</b>	    -	Shibboleth IDP (based on the TAP Docker container) with the v3 CAR integration endpoint configured in, pointed at the Dockerized CAR instance, and configured to use the coresident LDAP container for authN and as an attribute source.
  * <b>docker\_ldapnode\_1</b>	    -	OpenLDAP container pre-loaded with identities for use in the skunkworks environment

    Note that once the build script finishes, you will need to allow 3-5 minutes for the initialization container:

  * docker\_initnode\_1

    to complete its work setting up the IDP and the LDAP.  Once the docker\_initnode\_1 container terminates, your skunkworks environment will be ready to use.

    For details of the skunkworks configuration, consult the videos by Ken Klingenstein (filmed using a similarly-configured "sliced bread" environment) and the actual policy definitions, etc. generated by the build process.  In overview, the skunkworks environment includes:

  * An IDP for the fictional Castle Amber, configured to make decision requests to the local CAR instance for every attribute release.

  * An LDAP populated with the following users:

     * corwin (password: corwin) - A faculty member and alum of Castle Amber
     * oberon (password: oberon) - A faculty member of Castle Amber
     * benedict (password: benedict) - A part-time staff/part-time faculty member and alum of Castle Amber
     * dworkin (password: dworkin) - An alum of Castle Amber
     * dara (password: dara) - A student of Castle Amber (from Shadow)
     * ugrad (password: ugrad) - An undergraduate student in Castle Amber
     * grad (password: grad) - A graduate student in Castle Amber
     * fgrad (password: fgrad) - Another graduate student, this one asserting FERPA privacy rights
     * faculty (password: faculty) - A generic faculty member in Castle Amber
     * sysadmin (password: sysadmin) - The Castle Amber system administrator (and only user with rights to the CAR admin interface, by default)

  * A group of federated relying parties, available at the URLs:

     * https://<i>fqdn</i>/cadamin/	 -	  the CAR Admin UI (log in as "sysadmin" to access)
     * https://<i>fqdn</i>/car/carma/selfservice - the CAR self-service UI (log in as any valid user)
     * https://<i>fqdn</i>/contentrus/ - A fictional content provider in a contractual relationship with Castle Amber
     * https://<i>fqdn</i>/randsrus/ - A fictional R&S relying party serving researchers at Castle Amber
     * https://<i>fqdn</i>/scholars/ - A fictional Library content provider for researchers at Castle Amber
     * https://<i>fqdn</i>/payroll/ - A fictional payroll system for employees at Castle Amber

  * A set of institutional and meta-policies designed to illustrate various capabilities of the CAR system as different users access the varous relying parties.

#### Scripted dockerized build of just the CAR components together on a single system.  <a name="single-server"></a>

* This strategy is recommended for those wishing to manually integrate a standalone CAR instance with existing infrastructure (an existing Shibboleth IDP, for example, or another already-established Resource Holder) for test or development purposes, where performance and redundancy of the CAR components is not as important as frugality in the deployment.  The end product is a set of connected, running Docker containers providing the core CAR components, together with configuration to support integrating the CAR components with an existing environment.  While this build strategy may be used to build Docker containers for re-deployment to separate servers, the amount of re-configuration necessary to make that feasible makes it ill-advised -- for production or pre-production environments, it's probably best to use the standalone CAR container build strategy outlined above, and follow the Production Deployment Guide (published separately).

    Docker and docker-compose are prerequisites for this build process.  The build process expects to be able to create and manipulate Docker containers.  There should be no other prerequisites, however, as the build process begins by building a Docker container provisioned with the requisite tools to complete the rest of the build process.

    Off the main project directory is a subdirectory named "docker".  To start the dockerized build process, cd into that directory:

  * cd docker

    and run the build-and-start-car.sh script.  If this is your first time running the build in this copy of the tree, you should run the script without any arguments:

  * ./build-and-start-car.sh

    If you've never run the build process in this copy of the tree before, you'll be prompted by the build script to enter a number of configuration options for which (probably nonsensical) defaults will be offered.  If you've run the build process in this tree before, the build process will offer your last answers as defaults.  If you know that you have stored configuration from a prior (successful) run of the build process in this mode and you wish to avoid being prompted to override your prior settings, you may force the build process to be quiet by passing the "-q" flag to it:

  * ./build-and-start-car.sh -q

    Note that in the "quiet" case, you won't have a chance to override any of your prior settings.

    You'll be asked a series of questions, the answers to which will be used to configure the components as they are built and deployed.  The questions (and the implications of your answers) will include:

  * Server FQDN:  The components of the CAR system need to configure URLs relative to the host(s) they're running on.  In this build scenario, all components run on a single host, so this needs to be the FQDN of that host (as it will be entered, for example, in a user's web browser to access the CAR instance).  If you're deploying on a laptop or a desktop workstation for test purposes, for example, this might be "localhost".  Otherwise, use the FQDN of the host you're building on.

  * Credentials for the CAR databases:  For testing purposes in a "localhost" deployment, the defaults may be fine, but feel free to choose your own values as desired.

  * As with the standalone container build process above, you'll be prompted for API credentials, and separately for authentication module parameters.  For "localhost" test builds, using the "null" authentication module may be simplest and may allow for offline testing in the self-contained environment.

  * This build includes an Apache front-end to the CAR component back-ends that implements a Shibboleth SP.  You'll be prompted separately for the hostname to use for internal URL references within the Apache server.  Typically, this will be the same as the FQDN entered above.

  * An entity ID for the SP to front-end authenticated CAR UIs (the admin UI and the self-service UI).  This can be arbitrary, and the computed default will usually suffice.

  * A global information item/attribute to be used as a unique identifier by the COPSU to identify individual users.  This needs to be an identifier your chosen IDP can (and will) provide for all users, and it needs to have the characteristics of a persistent ID.  Likely options might include eduPersonPrincipalName, eduPersonUniqueId, or PersistentId.  In certain specific cases, eduPersonTargetedId or another pairwise identifier directed at the entity ID selected above may suffice, but in mutli-tenant scenarios, a persistent and correlatable identifier will be required.

  * This build strategy assumes you will be using a SAML IDP to authenticate users into the CAR UIs.  Typically, this IDP will also incorporate an integration endpoint for consuming CAR release decisions, but that is not strictly necessary.  In order to federate the SP built in this process with the appropriate IDP for user authentication, you will need to either specify the entity ID of the IDP (for bilateral federation with the IDP) or the URL of a federation discovery service (if you plan to join this SP to a federation).

  * The URL of a metadata provider from which the SP can retrieve metadata for either the federation it belongs to or the IDP it's bilaterally federated with.  This is used to configure a metadata source in the SP.

  * The remaining configuration elements requested in the standalone build process outlined above.

    The end result of this build process will be a collection of Docker containers running from images on the local machine:

  * <b>docker\_copsunode\_1</b> (docker\_copsunode)     -	 A running COPSU implementation
  * <b>docker\_arpsinode\_1</b> (docker\_arpsinode)     -  A running ARPSI implementation
  * <b>docker\_icmnode\_1</b> (docker\_icmnode)	      -  A running ICM implementation
  * <b>docker\_informednode\_1</b> (docker\_informednode) - A running INFORMED component
  * <b>docker\_carnode\_1</b> (docker\_carnode)		- A running instance of the user-facing CAR UIs (and the demo RPs)
  * <b>docker\_caradminnode\_1</b> (docker\_caradminnode) - A running instance of the admin-facing CAR UI
  * <b>docker\_apache-sp\_1</b> (docker\_apache-sp)	- A container running an Apache frontend with URLs protected by an SP federated according to your responses to the questions asked during set-up.
  * <b>docker\_node1\_1</b> (docker\_node1)		- A container running a MySQL server configured in a galera cluster with two other local nodes.
  * <b>docker\_node2\_1</b> (docker\_node2)		- A clone of the MySQL server configured in the same galera cluster
  * <b>docker\_node3\_1</b> (docker\_node3)		- A third member of the galera MySQL cluster

    Along with the running containers will be a number of local Docker data volumes containing configuration and WAR files necessary for the individual component containers to function.  The component containers will be running with the appropriate data volumes mounted as necessary.

    To fully make use of the new CAR instance, you will need to establish integration (including exchanging public keys) with at least one resource holder, configure the resource holder and any associated relying parties in the CAR registration interface, and set up administrative and meta-policies to drive the release decision mechanism that is CAR.  For more details on this, consult the CAR Configuration docuemnt published separately.    

#### Standalone container build strategy. <a name="just-car"></a>

* This strategy is recommended for those wishing to build Docker containers for deployment on other systems.  The result is a collection of minimally-configured Docker containers and associated Docker data volumes prepared to run the core CAR components.  To invoke this build sequence, cd into the "docker" subdirectory of the project and run the:

  * ./build-containers-only.sh

    script.  If this is your first time running this script in this copy of the build tree or if you plan to change basic configuration parameters, you may run the script with no arguments.  If you have previously configured the build (with a prior run of the script) and wish to re-use the same configuration, you may run the script in "quiet" mode with the "-q" flags:

  * ./build-containers-only.sh -q

    If you know the user configuration you will be using for your database, or if you want to set database credentials for the various CAR components at this time, pass in the "-d" flag, and answer the additional questions about database credentials.  This build process does *not* create a database nor set up its schema, but it *does* configure the various CAR components with database credential information if requested:

  * ./build-containers-only.sh -d

    If you wish, you may pass both the "-q" and "-d" flags to invoke the database credential configuration process in quiet mode.

    If you don't run the script in quiet mode, it will ask a series of questions to determine various authentication parameters and set some site-specific information in the various container configurations.  Key items requested include:

  * Database credentials (if the "-d" flag is included), including those for individual users associated with each of the ARPSI, COSPU, ICM, and INFORMED components.

  * API credentials to be used when authenticating to the back-end REST API layer.  Note that the values you choose for these credentials may depend on your choice of authentication mechanisms for the back-ends (see below).

  * Authentication driver(s) to use for each back-end.  The CAR back-end components (ARPSI, COPSU, ICM, INFORMED) support pluggable authentication modules for validating basic-auth credentials passed in with API requests.  Currently, two plugins are provided -- a "null" authentication plugin that accepts any credential as valid and a "krb5" authentication plugin that validates credentials against a Kerberos V environment defined separately.  For testing purposes, the "null" authentication plugin can be useful, since it avoids the need for external infrastructure and avoids the need to specify actual credentials.  The "null" plugin is entirely insecure, however, so for any production use, the Kerberos plugin is recommended. 

  * Kerberos V parameters (KDCs, etc) - if the "krb5" authentication module is selected.

  * Authorized users and administrative users (identified by the credentials associated with the authentication plugin chosen above) for each of the back-end API providers.  Typically, these will be the same across all of the API providers, although that isn't necessary.

  * Short name and long name for your organization, used in certain portions of the UI where organization names are required.  For example, at Duke, we use "Duke University" as the long name and "Duke" as the short name.

  * Organizational icon, for use in constructing banners on UI pages.  At Duke, we use a PNG rendering of our official "Duke" text logo.

    If you desire, you may pre-configure database connection parameters prior to running the build by editing the files named:

  * hibernate.cfg.xml.co

    in each of the subdirectories of the "docker" tree and providing whatever parameters you wish.

    At the end of the build process, you will have a collection of Docker containers to run each of the CAR core components:

  * <b>docker_copsunode</b>  -	    The COPSU container
  * <b>docker_arpsinode</b>  -	    The ARPSI container
  * <b>docker_icmnode</b>    -	    The ICM container
  * <b>docker_informednode</b> -   The INFORMED container
  * <b>docker_carnode</b>    -	    The user-facing UI container
  * <b>docker_caradminnode</b> -   The administrative UI container

    You will also find a collection of Docker data volumes containing actual WAR files:

  * <b>buildnode_arpsi-webapps</b>	-  contains the arpsi.war file
  * <b>buildnode_copsu-webapps</b>	-  contains the copsu.war file
  * <b>buildnode_icm-webapps</b>	-  contains the icm.war file
  * <b>buildnode_informed-webapps</b>	-  contains the informed.war file
  * <b>buildnode_car-webapps</b>	-  contains the car.war file
  * <b>buildnode_caradmin-webapps</b>	-  contains the caradmin.war file

    For ideas on further deployment of the completed Docker containers (including recommendations for database configurations, etc.) see the CAR Production Deployment Guide (published separately).

#### Entirely Manual Build of CAR Components <a name="manual"></a>
* Manual build of just the CAR components.  While building the entire CAR system manually, from scratch, and configuring it manually is entirely feasible, it is not recommended nor is it extensively supported by the developers nor existing documentation.  This option does offer the most flexibility in terms of both configuration and build options, but requires the greatest amount of manual labor and a deep understanding of the CAR system at the code level.  Prerequisites for this option include the installation of a Java 11 JDK (the automated builds outlined below use the Azul Zulu 11 OpenJDK implementation, but any fully compliant Java 11 JDK should work) and a compatible version of Maven.  Each of the core components has a subdirectory off the main project directory:

  * <b>copsu</b>
  * <b>arpsi</b>
  * <b>informed</b>
  * <b>icm</b>
  * <b>caradmin</b>
  * <b>car</b>

    and each subdirectory contains a pom.xml describing the Maven build process for that component.

    You may build individual components manually by cd-ing into their respective subdirectories and running:

  * mvn install

    Note that build order is significant.  Some components depend upon model components from other components -- for example, the "caradmin" component uses model classes from the "copsu" component when manipulating user policy information via the REST-ish interfaces exposed by the ICM.  Components *must* be built (or rebuilt) in the order listed above.  Building each of the earlier components (copsu, arpsi, informed, etc.) will leave in your local Maven repository jar files containing extracted classes required to build later components (caradmin, car, etc.).

    At the end of each build process, you should find a <component>-0.0.1.war archive in the "target" subdirectory that can then be used to deploy the component into your chosen servlet environment.  To date, only Tomcat servlet containers have been fully tested, although other servlet environments should work, provided they are properly configured. 

    Configuration of the individual components, as well as configuration of the required servlet container(s) and SSO-wrapped HTTP services are entirely up to the deployer in the manual build process.  

