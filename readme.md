# Compilatio : Integration with Sakai 11.x

This project integrates Compilatio plagiarism system with Sakai LMS. The current development is based on Turnitin integration for Sakai 11.x (https://github.com/sakaicontrib/turnitin) and Compilatio integration for Sakai 10.x (https://github.com/fberthome/sakai_compilatio).

Our first rule is not to modify the ContentReview API (item models or service interfaces), so the integration should be much more standard.

This project contains the Compilatio implementation for ContentReview


## Instructions:

- Clone this project : https://github.com/sakaicontrib/compilatio-antiplagiarism
- Compile our contentreview-impl
- Enable the compilatio integration in the content-review module and compile it (Sakai source).
  - Open this file content-review/contentreview-federated/pack/src/webapp/WEB-INF/components.xml
  - Uncomment the line bean="org.sakaiproject.contentreview.service.ContentReviewServiceCompilatio" to enable the Compilatio content review implementation
- Set up the correct sakai properties
- Remembers to set up Quartz jobs : You will have to run jobs manually unless they're set up to auto-run.

## Sakai properties

- assignment.useContentReview=true
- compilatio.secretKey=CLIENT_KEY

#### Optional  (or with default value)
  - compilatio.proxyHost=PROXY_HOST
  - compilatio.proxyPort=PROXY_PORT
  - compilatio.apiURL=http://service.compilatio.net/webservices/CompilatioUserClient.php?
  - compilatio.accept.all.files=false
  - compilatio.acceptable.mime.types=application/msword,application/vnd.openxmlformats-officedocument.wordprocessingml.document,application/excel,application/vnd.ms-excel,application/x-excel,application/x-msexcel,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet,application/mspowerpoint,application/powerpoint,application/vnd.ms-powerpoint,application/x-mspowerpoint,application/vnd.openxmlformats-officedocument.presentationml.presentation,application/mspowerpoint,application/vnd.ms-powerpoint,application/vnd.openxmlformats-officedocument.presentationml.slideshow,application/pdf,application/postscript,application/postscript,text/plain,text/html,text/html,application/wordperfect,application/x-wpwin,application/vnd.oasis.opendocument.text,text/rtf,application/rtf,application/x-rtf,text/richtext
  - compilatio.acceptable.file.extensions=.doc,.docx,.xls,.xls,.xls,.xls,.xlsx,.ppt,.ppt,.ppt,.ppt,.pptx,.pps,.pps,.ppsx,.pdf,.ps,.eps,.txt,.html,.htm,.wpd,.wpd,.odt,.rtf,.rtf,.rtf,.rtf
  - compilatio.acceptable.file.types=ACCEPTABLE_FILE_TYPES
  - compilatio.filename.max.length=200
  - compilatio.networkTimeout=180000

## Quartz Jobs

- Process Content Review Queue : Process the content-review queue, uploads documents to Compilatio and analyze them.
- Process Content Review Reports : Get reports for analyzed documents.

Create a new trigger for this jobs. We suggest you set the job to run at five minute intervals which can be done with the following definition:
```
0 0/5 * * * ?
```

## Assignments set up

Options in the assignment creation screen : 
- Use Compilatio : Basic check to activate the plagiarism system
- Allow students to view report : Let the students get a link to the Compilatio results report.
- Generate originality reports : 
  * "Immediately" : Documents will be uploaded and analyzed in the moment the student uploads them (the next time the job runs after the students upload them). CAUTION : this may cause an incorrect analysis report due to the impossibility to compare with all documents.
  * "On Due Date" : Document will be uploaded and analyzed after the assignment reaches its due date.

## Components.xml set up
* Basic set up
```xml
<bean id="org.sakaiproject.contentreview.service.ContentReviewServiceCompilatio"
		class="org.sakaiproject.contentreview.impl.compilatio.CompilatioReviewServiceImpl"
		init-method="init">
		<property name="dao"
			ref="org.sakaiproject.contentreview.dao.ContentReviewDao" />
		<property name="toolManager" ref="org.sakaiproject.tool.api.ToolManager" />
		<property name="userDirectoryService"
			ref="org.sakaiproject.user.api.UserDirectoryService" />
		
		<property name="serverConfigurationService"
			ref="org.sakaiproject.component.api.ServerConfigurationService" />
		<property name="contentHostingService"
			ref="org.sakaiproject.content.api.ContentHostingService" />
		<property name="assignmentService"
			ref="org.sakaiproject.assignment.api.AssignmentService" />
		<property name="entityManager" ref="org.sakaiproject.entity.api.EntityManager" />
		<property name="compilatioConn"
			ref="org.sakaiproject.contentreview.impl.compilatio.CompilatioAccountConnection" />
		<property name="compilatioContentValidator"
			ref="org.sakaiproject.contentreview.impl.compilatio.CompilatioContentValidator" />
			
		<!-- Uncomment this to delegate into advisors -->
		<!-- <property name="siteAdvisor" ref="org.sakaiproject.contentreview.service.ContentReviewSiteAdvisor" /> -->
	</bean>
```

* Advisors (disabled by default)
```xml
<!-- Uncomment this to allow all sites to use Compilatio regardless of site, type, or property -->
<!-- <bean id="org.sakaiproject.contentreview.service.ContentReviewSiteAdvisor" 
	class="org.sakaiproject.contentreview.impl.adivisors.DefaultSiteAdvisor"> 
</bean> -->
	
<!-- Uncomment this to use a site property to define which sites use c-r -->
<!-- <bean id="org.sakaiproject.contentreview.service.ContentReviewSiteAdvisor" 
	class="org.sakaiproject.contentreview.impl.adivisors.SitePropertyAdvisor"> 
	<property name="siteProperty"><value>useContentReviewService</value></property> 
</bean> -->
	
<!-- uncomment this bean to make c-r available to only sites of the type course -->
<!-- <bean id="org.sakaiproject.contentreview.service.ContentReviewSiteAdvisor" 
	class="org.sakaiproject.contentreview.impl.adivisors.SiteCourseTypeAdvisor"> 
</bean> -->
	
<!--  Uncomment this to use a global property to define if every site uses c-r -->
<!--<bean id="org.sakaiproject.contentreview.service.ContentReviewSiteAdvisor" 
	class="org.sakaiproject.contentreview.impl.advisors.GlobalPropertyAdvisor">
	<property name="sakaiProperty"><value>assignment.useContentReview</value></property>
	<property name="serverConfigurationService"
		ref="org.sakaiproject.component.api.ServerConfigurationService" />
</bean>-->

<!-- uncomment this bean to make c-r available using chained advisors -->
<!--
<bean id="org.sakaiproject.contentreview.service.ContentReviewSiteAdvisor"
	  class="org.sakaiproject.contentreview.impl.advisors.ChainedPropertyAdvisor">
	<property name="advisors">
		<list>
			<bean class="org.sakaiproject.contentreview.impl.advisors.SitePropertyAdvisor">
				<property name="siteProperty">
					<value>useContentReviewService</value>
				</property>
			</bean>
			<bean class="org.sakaiproject.contentreview.impl.advisors.GlobalPropertyAdvisor">
				<property name="sakaiProperty">
					<value>assignment.useContentReview</value>
				</property>
				<property name="serverConfigurationService"
						  ref="org.sakaiproject.component.api.ServerConfigurationService"/>
			</bean>
		</list>
	</property>
</bean> -->
```
## Content Review Service : basic configuration

The original content review service provided by Sakai is prepared to add multiple implementations. Remember to activate the Compilatio one (.../content-review/contentreview-federated/pack/src/webapp/WEB-INF/components.xml) :

```xml
<bean
    id="org.sakaiproject.contentreview.service.ContentReviewService"
    class="org.sakaiproject.contentreview.impl.ContentReviewFederatedServiceImpl"
    init-method="init">
    <property name="providers" ref="contentReviewProviders"/>
    <property name="siteService" ref="org.sakaiproject.site.api.SiteService"/>
    <property name="toolManager" ref="org.sakaiproject.tool.api.ToolManager"/>
    <property name="serverConfigurationService" ref="org.sakaiproject.component.api.ServerConfigurationService" />

</bean>

<util:list id="contentReviewProviders">
    <ref bean="org.sakaiproject.contentreview.service.ContentReviewServiceCompilatio"/>
</util:list>
```

