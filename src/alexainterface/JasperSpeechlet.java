package alexainterface;

import java.io.IOException;
import java.io.InputStream;
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
import com.amazon.speech.speechlet.SessionEndedRequest;
import com.amazon.speech.speechlet.SessionStartedRequest;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.speechlet.SpeechletV2;
import com.amazon.speech.ui.SsmlOutputSpeech;

import de.aitools.aq.alexa.AlexaService;

public class JasperSpeechlet implements SpeechletV2 {

	//////////////////////////////////////////////////////////////////////////////
	// LOGGING //
	//////////////////////////////////////////////////////////////////////////////

	private static Logger LOG = LoggerFactory.getLogger(JasperSpeechlet.class);

	//////////////////////////////////////////////////////////////////////////////
	// CONSTANTS //
	//////////////////////////////////////////////////////////////////////////////

	private static final String INTENT_FetchSpotIntent = "FetchSpotIntent";
	private static final String INTENT_NoteSpotIntent = "NoteSpotIntent";

	private static final String INTENT_Cancel = "AMAZON.CancelIntent";
	private static final String INTENT_Stop = "AMAZON.StopIntent";
	private static final String INTENT_Help = "AMAZON.HelpIntent";

	private static final String SLOT_Objects = "object";
	private static final String SLOT_Prepositions = "position";
	private static final String SLOT_Spots = "spot";
	
	
	private static final String SAVE_PATH = "../../resources/save.properties";

	//////////////////////////////////////////////////////////////////////////////
	// HANDLER METHODS //
	//////////////////////////////////////////////////////////////////////////////

	@Override
	public SpeechletResponse onLaunch(final SpeechletRequestEnvelope<LaunchRequest> requestEnvelope) {
		LOG.debug("onLaunch");

		final SpeechletResponse response = new SpeechletResponse();
		final SsmlOutputSpeech output = new SsmlOutputSpeech();
		response.setOutputSpeech(output);

		output.setSsml("<speak>Hi! Are you looking for something?</speak>");

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

		final SpeechletResponse response = new SpeechletResponse();
		final SsmlOutputSpeech output = new SsmlOutputSpeech();
		response.setOutputSpeech(output);
		
		final Properties properties = new Properties();
		InputStream inputStream = getClass().getResourceAsStream(SAVE_PATH);
		try {
			properties.load(inputStream);
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Error occured when reading the save file.");
		}

		@SuppressWarnings("unchecked")

		final Intent intent = requestEnvelope.getRequest().getIntent();
		switch (intent.getName()) {
		case INTENT_NoteSpotIntent:
			output.setSsml("<speak>" + this.onNoteSpotIntent(intent.getSlots()) + "</speak>");
			response.setShouldEndSession(false);
			break;
		case INTENT_FetchSpotIntent:
			output.setSsml("<speak>" + this.onFetchSpotIntent(intent.getSlots()) + "</speak>");
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

	
	
	
	private String onNoteSpotIntent(Map<String, Slot> slots) {
		String answer = "";
		String object = null;
		String spot = null;
		String preposition = null;
		
		try {
			object = slots.get(SLOT_Objects).getValue();
			spot = slots.get(SLOT_Spots).getValue();
			preposition = slots.get(SLOT_Prepositions).getValue();
			
			System.out.println("object: " + object);
			System.out.println("place: " + preposition + " " + spot);
		} catch (NullPointerException e) {
			System.out.println("No slot word found!");
		}
		
		// Save to file
		
		
		
		return answer;
	}

	private String onFetchSpotIntent(Map<String, Slot> slots) {
		String answer = "";
		String object = null;
		
		try {
			object = slots.get(SLOT_Objects).getValue();
			
			System.out.println("object: " + object);
		} catch (NullPointerException e) {
			System.out.println("No slot word found!");
		}
		
		return answer;
	}


	//////////////////////////////////////////////////////////////////////////////
	// PROGRAM //
	//////////////////////////////////////////////////////////////////////////////

	public static void main(final String[] args) throws Exception {
		AlexaService.mainV2(JasperSpeechlet.class, args);
	}

	//////////////////////////////////////////////////////////////////////////////
	// UNUSED HANDLER METHODS //
	//////////////////////////////////////////////////////////////////////////////

	@Override
	public void onSessionEnded(final SpeechletRequestEnvelope<SessionEndedRequest> requestEnvelope) {
	}

}
