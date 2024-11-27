package com.rxlogix.util

import grails.util.Holders
import groovy.xml.MarkupBuilder
import org.apache.commons.codec.binary.Base64
import org.apache.commons.codec.digest.Md5Crypt
import org.apache.http.HttpEntity
import org.apache.http.HttpResponse
import org.apache.http.NameValuePair
import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.client.CredentialsProvider
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.protocol.HttpClientContext
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.BasicCookieStore
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.message.BasicNameValuePair
import org.apache.http.protocol.BasicHttpContext
import org.apache.http.protocol.HttpContext
import org.apache.http.util.EntityUtils

class SpotfireUtil {

    static String triggerJob(String spotfireServer,
                             Integer port,
                             String protocol,
                             String xml,
                             String automationUser,
                             String automationPassword, String access_token) {
        String manifestUri = Holders.config.spotfire.manifestUri
        String statusUri = Holders.config.spotfire.statusUri
        String startUrl = Holders.config.spotfire.startUrl
        String authUrl =  Holders.config.spotfire.authUrl

        HttpContext httpContext = new BasicHttpContext()
        org.apache.http.impl.client.BasicCookieStore cookieStore = new BasicCookieStore()
        httpContext.setAttribute(HttpClientContext.COOKIE_STORE, cookieStore)

        CredentialsProvider credentialsProvider = new BasicCredentialsProvider()
        credentialsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials(automationUser, automationPassword))

        CloseableHttpClient httpClient =
                HttpClientBuilder.create().setDefaultCredentialsProvider(credentialsProvider).build()

        HttpGet manifestInvoker = new HttpGet("$protocol://$spotfireServer:$port/$manifestUri")
        HttpResponse response1 = httpClient.execute(manifestInvoker, httpContext)
        try {
            if (access_token) {
                HttpPost jobSender = new HttpPost("$protocol://$spotfireServer:$port/$startUrl")
                jobSender.setHeader('Content-Type', 'application/xml; charset=utf-8')
                jobSender.setHeader('from-pvr', 'true')
                jobSender.setHeader("Authorization", String.format("Bearer %s", access_token))
                StringEntity xmlEntity = new StringEntity(xml)
                jobSender.setEntity(xmlEntity)
                HttpResponse response2 = httpClient.execute(jobSender, httpContext)
                def jobResp = readData(response2)
                return jobResp
            } else {
                throw new Exception('Access Token Invalid')
            }
        } catch (Throwable t) {
            t.printStackTrace()
            return null
        } finally {
            response1.close()
        }
    }
    //fetch token for spotfire using client credentials
    static String getTokenForSpotfire(String protocol, String spotfireServer,Integer port,String authUrl){
        String authorizeUrl = "$protocol://$spotfireServer:$port/$authUrl"
        HttpContext httpContext = new BasicHttpContext()
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider()
        credentialsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials(Holders.config.spotfire.clientId, Holders.config.spotfire.clientSecret))
        CloseableHttpClient httpClient = HttpClientBuilder.create().setDefaultCredentialsProvider(credentialsProvider).build()
        HttpPost jobSender = new HttpPost(authorizeUrl)
        List<NameValuePair> pairs = new ArrayList<NameValuePair>();
        pairs.add(new BasicNameValuePair("grant_type", "client_credentials"));
        pairs.add(new BasicNameValuePair("scope", "api.rest.automation-services-job.execute"));
        jobSender.setEntity(new UrlEncodedFormEntity(pairs));
        HttpResponse response = httpClient.execute(jobSender, httpContext)
        String responseString = readData(response)
        responseString
    }

    def static composeXmlBodyForTask(parameters, spotfireMailEnabled) {
        def writer = new StringWriter()
        def mb = new MarkupBuilder(writer)

        mb.'as:Job'('xmlns:as': 'urn:tibco:spotfire.dxp.automation') {
            'as:Tasks' {
                OpenAnalysisFromLibrary('xmlns': 'urn:tibco:spotfire.dxp.automation.tasks') {
                    'as:Title'(parameters.openTitle)
                    'AnalysisPath'(parameters.AnalysisPath)
                    'ConfigurationBlock'(parameters.ConfigurationBlock)
                }
                SaveAnalysisToLibrary('xmlns': 'urn:tibco:spotfire.dxp.automation.tasks') {
                    'as:Title'(parameters.saveTitle)
                    'LibraryPath'(parameters.LibraryPath)
                    'EmbedData'(parameters.EmbedData)
                    'DeleteExistingBookmarks'(parameters.DeleteExistingBookmarks)
                    'AnalysisDescription'('{jobid}')
                }
                if(spotfireMailEnabled) {
                    SendEmail('xmlns': 'urn:tibco:spotfire.dxp.automation.tasks') {
                        'as:Title'(parameters.emailTitle)
                        'Recipients' {
                            parameters.Recipients.each {
                                'string'(it)
                            }
                        }
                        'Subject'(parameters.Subject)
                        'Message'(parameters.EmailMessage)
                        'Links'()
                        'Attachments'()
                    }
                }
            }
        }
        def xmlString = writer.toString()
        writer.close()
        xmlString
    }

    static String buildAuthToken(String username, String sessionId) {
        Base64.encodeBase64(
                Md5Crypt.md5Crypt("$username . ${sessionId}".getBytes("UTF-8")).
                        getBytes("UTF-8"))
    }

    private static String readData(HttpResponse response) {
        HttpEntity entity = response.getEntity();
        String responseString = EntityUtils.toString(entity, "UTF-8");
        responseString
    }

    static File generateAutomationXml(File folder, String content) {
        def files = folder.listFiles()

        Integer nexNum = 1
        if (files)
            nexNum = files.length + 1

        File file = new File(folder, "job-${nexNum}.xml")
        if(!file.exists()){
            file.write(content)
        } else {
            nexNum = nexNum + 1
            file = new File(folder, "job-${nexNum}.xml")
            file.write(content)
        }
        file
    }
}
