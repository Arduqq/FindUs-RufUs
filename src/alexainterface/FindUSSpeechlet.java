package alexainterface;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.LaunchRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SessionEndedRequest;
import com.amazon.speech.speechlet.SessionStartedRequest;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.speechlet.SpeechletV2;
import com.amazon.speech.ui.SsmlOutputSpeech;

import de.aitools.aq.alexa.AlexaService;

public class FindUSSpeechlet implements SpeechletV2 {

	//////////////////////////////////////////////////////////////////////////////
	// LOGGING //
	//////////////////////////////////////////////////////////////////////////////

	private static Logger LOG = LoggerFactory.getLogger(FindUSSpeechlet.class);

	//////////////////////////////////////////////////////////////////////////////
	// CONSTANTS //
	//////////////////////////////////////////////////////////////////////////////

	private static final String INTENT_FetchSpotIntent = "FetchSpotIntent";
	private static final String INTENT_NoteSpotIntent = "NoteSpotIntent";
	private static final String INTENT_ClearAll = "ClearAllIntent";

	private static final String INTENT_Cancel = "AMAZON.CancelIntent";
	private static final String INTENT_Stop = "AMAZON.StopIntent";
	private static final String INTENT_Help = "AMAZON.HelpIntent";
	private static final String INTENT_Fallback = "AMAZON.FallbackIntent";
	private static final String INTENT_NO   = "AMAZON.NoIntent";

	private static final String SLOT_Objects = "object";
	private static final String SLOT_Prepositions = "position";
	private static final String SLOT_Spots = "spot";

	
	private static final String SAVE_PATH = "resources/save.properties";
	private static final Properties props = loadProperties(SAVE_PATH);

	//////////////////////////////////////////////////////////////////////////////
	// HANDLER METHODS //
	//////////////////////////////////////////////////////////////////////////////

	@Override
	public SpeechletResponse onLaunch(final SpeechletRequestEnvelope<LaunchRequest> requestEnvelope) {
		LOG.debug("onLaunch");

		final SpeechletResponse response = new SpeechletResponse();
		final SsmlOutputSpeech output = new SsmlOutputSpeech();
		response.setOutputSpeech(output);

		output.setSsml("<speak>Hi! Are you looking for something? Or should I note where you've put something?</speak>");

		response.setShouldEndSession(false);
		return response;
	}

	@Override
	public void onSessionStarted(final SpeechletRequestEnvelope<SessionStartedRequest> requestEnvelope) {
		LOG.debug("onSessionStarted");

	}

	@Override
	public SpeechletResponse onIntent(final SpeechletRequestEnvelope<IntentRequest> requestEnvelope) {
		LOG.debug("onIntent: " + requestEnvelope.getRequest().getIntent().getName());
		
		final Session session = requestEnvelope.getSession();
		final SpeechletResponse response = new SpeechletResponse();
		final SsmlOutputSpeech output = new SsmlOutputSpeech();
		response.setOutputSpeech(output);
		

		@SuppressWarnings("unchecked")

		final Intent intent = requestEnvelope.getRequest().getIntent();
		switch (intent.getName()) {
		case INTENT_NoteSpotIntent:
			output.setSsml("<speak>" + this.onNoteSpotIntent(session, intent.getSlots()) + "</speak>");
			response.setShouldEndSession(false);
			break;
		case INTENT_FetchSpotIntent:
			output.setSsml("<speak>" + this.onFetchSpotIntent(intent.getSlots()) + "</speak>");
			response.setShouldEndSession(false);
			break;
		case INTENT_ClearAll:
			output.setSsml("<speak>" + this.onClearAllIntent() + "</speak>");
			response.setShouldEndSession(false);
			break;
		case INTENT_Fallback:
		case INTENT_Help:
			output.setSsml("<speak>" 
							+ "Hey there! You can tell me where you've put something, "
							+ "so that if you forget, I can quickly remind you!" 
						    + "</speak>");
			response.setShouldEndSession(false);
			break;
		case INTENT_NO:
			output.setSsml("<speak>" + this.onNoIntent(session) + "</speak>");
			response.setShouldEndSession(false);
			break;
		case INTENT_Cancel:
		case INTENT_Stop:
			output.setSsml("<speak>Close</speak>");
			response.setShouldEndSession(true);
			break;
		default:
			output.setSsml("<speak>Error. Invalid intent. " + intent.getName() + "</speak>");
			response.setShouldEndSession(false);
		}
		return response;
	}

	
	
	
	
	
	
	
	private String onNoIntent(final Session session) {
		String lastObj = getLastObject(session);
		props.remove(lastObj);
		String answer = "Oh, sorry, seems like I mistook. "
						+ "I've just deleted the last entry for you. "
						+ "Would you please say it again?";
		return answer;
	}

	private String onClearAllIntent() {
		// Clear all entries in the saved file
		props.clear();
		return "Okido, I've cleaned all previous entries.";
	}

	// Save said location and item
	private String onNoteSpotIntent(final Session session, Map<String, Slot> slots) {
		String answer = "";
		String object = null;
		String spot = null;
		String preposition = null;
		String secspot = null;
		String secposition = null;
		
		String position = null;
		
		try {
			object = slots.get(SLOT_Objects).getValue();
			spot = slots.get(SLOT_Spots).getValue();
			preposition = slots.get(SLOT_Prepositions).getValue();
			secspot = slots.get("secspot").getValue();
			secposition = slots.get("secposition").getValue();
			
			if (secspot == null || secposition == null) {
				position = preposition + " " + spot;
			} else {
				position = preposition + " " + spot + " " + secposition + " " + secspot;
			}
			System.out.println("object: " + object);
			System.out.println("place: " + position);
		} catch (NullPointerException e) {
			System.out.println("No slot word found!");
		}
		
		// Save to file
		props.setProperty(object, position);
		answer = "I see, you've put the " + object + " " + position;
		// Store properties into property file
		try {
			props.store(new FileOutputStream(SAVE_PATH), null);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		setLastObject(session, object);
		
		
		return answer;
	}

	// Retrieve said item location
	private String onFetchSpotIntent(Map<String, Slot> slots) {
		String answer = "";
		String object = null;
		
		try {
			object = slots.get(SLOT_Objects).getValue();
			
			System.out.println("object: " + object);
		} catch (NullPointerException e) {
			System.out.println("No slot word found!");
		}
		
		// Read saved positions
		String position = props.getProperty(object);
		System.out.println(position);
		if (object.endsWith("s")) {
			answer = "The " + object + " are " + position;
		} else {
			answer = "The " + object + " is " + position;
		}
		
		return answer;
	}
	
	
	
   /**
	 * Loads the properties.
	 */
	private static Properties loadProperties(String path){
		Properties props = new Properties();
		FileInputStream inputStream = null;
		Path configFile = Paths.get(path);
		try {
			inputStream = new FileInputStream(configFile.toString());
			props.load(inputStream);
		} catch (IOException ioe){
			ioe.printStackTrace();
		}
		return props;
	}
	
	
	//////////////////////////////////////////////////////////////////////////////
	// SESSION ATTRIBUTES MANAGER //
	//////////////////////////////////////////////////////////////////////////////
	private void setLastObject(final Session session, String object) {
		session.setAttribute("LastObject", object);
	}
	private String getLastObject(final Session session) {
		return (String) session.getAttribute("LastObject");
	}
	
	

	//////////////////////////////////////////////////////////////////////////////
	// PROGRAM //
	//////////////////////////////////////////////////////////////////////////////

	public static void main(final String[] args) throws Exception {
		AlexaService.mainV2(FindUSSpeechlet.class, args);
	}

	//////////////////////////////////////////////////////////////////////////////
	// UNUSED HANDLER METHODS //
	//////////////////////////////////////////////////////////////////////////////

	@Override
	public void onSessionEnded(final SpeechletRequestEnvelope<SessionEndedRequest> requestEnvelope) {
	}

}
