package alexainterface;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
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

public class RufUSSpeechlet implements SpeechletV2 {

	//////////////////////////////////////////////////////////////////////////////
	// LOGGING //
	//////////////////////////////////////////////////////////////////////////////

	private static Logger LOG = LoggerFactory.getLogger(RufUSSpeechlet.class);

	//////////////////////////////////////////////////////////////////////////////
	// CONSTANTS //
	//////////////////////////////////////////////////////////////////////////////

	private static final String INTENT_AddTrack = "AddTrackIntent";
	private static final String INTENT_RemoveTrack = "RemoveTrackIntent";
	private static final String INTENT_PlayTrack = "PlayTrackIntent";
	private static final String INTENT_LoopTrack = "LoopTrackIntent";
	private static final String INTENT_RandomMix = "RandomMixIntent";

	private static final String INTENT_Fallback = "AMAZON.FallbackIntent";
	private static final String INTENT_Cancel = "AMAZON.CancelIntent";
	private static final String INTENT_Help = "AMAZON.HelpIntent";
	private static final String INTENT_Stop = "AMAZON.StopIntent";
//	private static final String INTENT_Pause = "AMAZON.PauseIntent";
//	private static final String INTENT_Resume = "AMAZON.ResumeIntent";

	private static final String SLOT_Number = "number";
	private static final String SLOT_Animal = "animal";
	private static final String SLOT_SECONDS = "seconds";

	private static final String OUTPUT_PATH = "https://invictus.cool/storage/";
	private static final String SOUNDLIB_PATH = "https://invictus.cool/storage/sounds/";
	private static final String SERVER_ADD_PATH = "https://invictus.cool:5000/sound/";
	private static final String SERVER_RANDOM_PATH = "https://invictus.cool:5000/";
	private static final String AUDIO_SSML_F = "<audio src=\"";
	private static final String AUDIO_SSML_B = "\"/>";
	// private static final Properties props = loadProperties(SAVE_PATH);
	
	
	private static final String BREAK_ONE_SEC 	= " <break time=\"1s\"/>";
	

	//////////////////////////////////////////////////////////////////////////////
	// HANDLER METHODS //
	//////////////////////////////////////////////////////////////////////////////

	@Override
	public SpeechletResponse onLaunch(final SpeechletRequestEnvelope<LaunchRequest> requestEnvelope) {
		LOG.debug("onLaunch");

		final SpeechletResponse response = new SpeechletResponse();
		final SsmlOutputSpeech output = new SsmlOutputSpeech();
		response.setOutputSpeech(output);

		output.setSsml("<speak> Hi! You can ask me to play animals sounds. Have fun!"
				+  AUDIO_SSML_F + SOUNDLIB_PATH + "ambience.mp3" + AUDIO_SSML_B + " </speak>");

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

		final Intent intent = requestEnvelope.getRequest().getIntent();
		switch (intent.getName()) {
		case INTENT_PlayTrack:
			output.setSsml("<speak>" + this.onPlayTrackIntent(session, intent.getSlots()) + "</speak>");
			response.setShouldEndSession(false);
			break;
		case INTENT_LoopTrack:
			output.setSsml("<speak>" + this.onLoopTrack(session, intent.getSlots()) + "</speak>");
			response.setShouldEndSession(false);
			break;
		case INTENT_RandomMix:
			output.setSsml("<speak>" + this.onRandomMix(session, intent.getSlots()) + "</speak>");
			response.setShouldEndSession(false);
			break;
		case INTENT_AddTrack:
			output.setSsml("<speak>" + this.onAddTrack(session, intent.getSlots()) + "</speak>");
			response.setShouldEndSession(false);
			break;
		case INTENT_RemoveTrack:
			output.setSsml("<speak>" + this.onRemoveTrack(session, intent.getSlots()) + "</speak>");
			response.setShouldEndSession(false);
			break;
		case INTENT_Help:
			output.setSsml("<speak>" + "</speak>");
			response.setShouldEndSession(false);
			break;
		case INTENT_Cancel:
		case INTENT_Stop:
			output.setSsml("<speak>Close</speak>");
			response.setShouldEndSession(true);
			break;
		case INTENT_Fallback:
		default:
			output.setSsml("<speak>Error. Invalid intent. " + intent.getName() + "</speak>");
			response.setShouldEndSession(false);
		}
		return response;
	}

	

	

	private String onPlayTrackIntent(Session session, Map<String, Slot> slots) {
		String answer = "";
		String animal = null;
		String number = null;
		
		try {
			animal = slots.get(SLOT_Animal).getValue();
			number = slots.get(SLOT_Number).getValue();
		} catch (NullPointerException e) {
			System.out.println("No slot word found!");
		}
		
		if (animal != null) {
			answer = AUDIO_SSML_F + SOUNDLIB_PATH + animal + ".mp3" + AUDIO_SSML_B;
		}
		return answer;
	}

	private String onLoopTrack(Session session, Map<String, Slot> slots) {
		String answer = "";
		String animal = null;
		String number = null;
		String seconds = null;
		
		try {
			animal = slots.get(SLOT_Animal).getValue();
			number = slots.get(SLOT_Number).getValue();
			seconds = slots.get(SLOT_SECONDS).getValue();
		} catch (NullPointerException e) {
			System.out.println("No slot word found!");
		}
		
		int times = 0, pause = 0;
		if (number == null) {
			times = 5;
			answer += "I'll just loop this track for 5 times for you. ";
		} else {
			times = Integer.parseInt(number);
		}
		pause = Integer.parseInt(seconds);
		
		
		for (int i = 0; i < times; i++) {
			answer += AUDIO_SSML_F + SOUNDLIB_PATH + animal + ".mp3" + AUDIO_SSML_B;
			for (int j = 0; j < pause; j++) {
				answer += BREAK_ONE_SEC;
			}
		}
		
		return answer;
	}

	private String onRandomMix(Session session, Map<String, Slot> slots) {
		String answer = "";
		String url = SERVER_RANDOM_PATH + "random";
		try {
			HTTPRequests.sendGetRequest(url);
		} catch (Exception e) {
			e.printStackTrace();
			answer = "Sorry, there seems to be a problem with the backend server.";
		}
		
		answer = AUDIO_SSML_F + OUTPUT_PATH + "random.mp3" + AUDIO_SSML_B;
		
		return answer;
	}
	
	private String onAddTrack(Session session, Map<String, Slot> slots) {
		String answer = "";
		String animal = null;
		String number = null;
		
		try {
			animal = slots.get(SLOT_Animal).getValue();
//			number = slots.get(SLOT_Number).getValue();
		} catch (NullPointerException e) {
			System.out.println("No slot word found!");
		}
		String url = SERVER_ADD_PATH + animal;
		
		try {
			HTTPRequests.sendGetRequest(url);
		} catch (Exception e) {
			e.printStackTrace();
			answer = "Sorry, there seems to be a problem with the backend server.";
		}
		
		answer = AUDIO_SSML_F + OUTPUT_PATH + "output.mp3" + AUDIO_SSML_B;
		
		return answer;
	}
	
	private String onRemoveTrack(Session session, Map<String, Slot> slots) {
		return "This intent is still in development";
	}



	/**
	 * Loads the properties.
	 */
	private static Properties loadProperties(String path) {
		Properties props = new Properties();
		FileInputStream inputStream = null;
		Path configFile = Paths.get(path);
		try {
			inputStream = new FileInputStream(configFile.toString());
			props.load(inputStream);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		return props;
	}

	//////////////////////////////////////////////////////////////////////////////
	// PROGRAM //
	//////////////////////////////////////////////////////////////////////////////

	public static void main(final String[] args) throws Exception {
		AlexaService.mainV2(RufUSSpeechlet.class, args);
	}

	//////////////////////////////////////////////////////////////////////////////
	// UNUSED HANDLER METHODS //
	//////////////////////////////////////////////////////////////////////////////

	@Override
	public void onSessionEnded(final SpeechletRequestEnvelope<SessionEndedRequest> requestEnvelope) {
	}

}
