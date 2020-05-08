package com.example.communityfavouraider

import android.util.Log
import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.HttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.calendar.CalendarScopes
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.util.*

class GoogleCalendar {
    private val TAG = "GoogleCalendar"

    /** Application name.  */
    private val APPLICATION_NAME = "Google Calendar API Java Quickstart"

    /** Directory to store user credentials for this application.  */
    private val DATA_STORE_DIR = java.io.File(System.getProperty("user.home"),
        ".credentials/calendar-java-quickstart.json")

    /** Global instance of the [FileDataStoreFactory].  */
//    private var DATA_STORE_FACTORY: FileDataStoreFactory? = null

    /** Global instance of the JSON factory.  */
    private val JSON_FACTORY = JacksonFactory.getDefaultInstance()

    /** Global instance of the HTTP transport.  */
    private var HTTP_TRANSPORT: HttpTransport? = null

    /** Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved credentials
     * at ~/.credentials/calendar-java-quickstart.json
     */
    private val SCOPES = Arrays.asList(CalendarScopes.CALENDAR)

    init {
        try {
            HTTP_TRANSPORT = NetHttpTransport()
//            DATA_STORE_FACTORY = FileDataStoreFactory(DATA_STORE_DIR)
        } catch (t: Throwable) {
            t.printStackTrace()
            System.exit(1)
        }
    }

    /**
     * Creates an authorized Credential object.
     * @return an authorized Credential object.
     * *
     * @throws IOException
     */
    @ExperimentalStdlibApi
    @Throws(IOException::class)
    fun authorize(): Credential {
        // Load client secrets.
        val inputString = """{"installed":{"client_id":"270508512965-gqkog78l4jeejg21j2girsv4dd96ak7r.apps.googleusercontent.com","project_id":"quickstart-1588923655661","auth_uri":"https://accounts.google.com/o/oauth2/auth","token_uri":"https://oauth2.googleapis.com/token","auth_provider_x509_cert_url":"https://www.googleapis.com/oauth2/v1/certs","redirect_uris":["urn:ietf:wg:oauth:2.0:oob","http://localhost"]}}"""
        val inputStream: InputStream = ByteArrayInputStream(inputString.encodeToByteArray())
        val reader = InputStreamReader(inputStream)
        val `in` = GoogleCalendar::class.java.getResourceAsStream("credentials.json")
        val clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, reader)

        // Build flow and trigger user authorization request.
        val flow = GoogleAuthorizationCodeFlow.Builder(
            HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
//            .setDataStoreFactory(DATA_STORE_FACTORY!!)
            .setAccessType("offline")
            .build()
        val credential = AuthorizationCodeInstalledApp(
            flow, LocalServerReceiver()
        ).authorize("user")

        Log.i(TAG,"Credentials saved to " + DATA_STORE_DIR.absolutePath)

        return credential
    }

    /**
     * Build and return an authorized Calendar client service.
     * @return an authorized Calendar client service
     * *
     * @throws IOException
     */
    @ExperimentalStdlibApi
    public val calendarService: com.google.api.services.calendar.Calendar
        @Throws(IOException::class)
        get() {
            val credential = authorize()
            return com.google.api.services.calendar.Calendar.Builder(
                HTTP_TRANSPORT!!, JSON_FACTORY, credential).setApplicationName(APPLICATION_NAME).build()
        }
}