from flask import Flask, url_for
from flask_restful import Api, Resource, reqparse
from pydub import AudioSegment
import glob, os
import random

app = Flask(__name__)
api = Api(app)
sound_path = "sounds/"
original_sound = AudioSegment.from_mp3(sound_path + "ambience.mp3")
sound_list = [];

# get every sound file
os.chdir(sound_path)
for file in glob.glob("*.mp3"):
    sound_list.append(str(file))
os.chdir("..")

@app.route('/sound/<animal>')
def api_sound(animal):
	global original_sound
	to_mix_with = AudioSegment.from_mp3(sound_path + animal + ".mp3")
	while (len(to_mix_with)<len(original_sound)):
		to_mix_with += AudioSegment.silent(duration=3000)
		to_mix_with += to_mix_with
	combined = original_sound.overlay(to_mix_with)
	combined.export("output.mp3", format='mp3', bitrate="48k", codec="libmp3lame")
	original_sound = AudioSegment.from_mp3("output.mp3")
	return "File exported", 200

@app.route('/all')
def api_all():
	ambience = AudioSegment.from_mp3(sound_path + "ambience.mp3")
	to_mix_with = AudioSegment.silent(duration=0)
	for sound in sound_list:
		to_mix_with += AudioSegment.from_mp3(sound_path + sound)
	combined = ambience.overlay(to_mix_with)
	combined.export("all.mp3", format='mp3', bitrate="48k", codec="libmp3lame")
	return "File exported", 200

@app.route('/random')
def api_random():
	ambience = AudioSegment.from_mp3(sound_path + "ambience.mp3")[:4000]
	for sound in sound_list:
		current_sound = AudioSegment.silent(duration=random.randint(0,9000))
		while (len(current_sound) < len(ambience)):
				current_sound += AudioSegment.silent(duration=random.randint(0,9000))
				current_sound += AudioSegment.from_mp3(sound_path + sound)
		ambience = ambience.overlay(current_sound)
	ambience.export("random.mp3", format='mp3', bitrate="48k", codec="libmp3lame")
	return "File exported", 200

@app.route('/reset')
def api_reset():
	ambience = AudioSegment.from_mp3(sound_path + "ambience.mp3")
	ambience.export("output.mp3", format='mp3', bitrate="48k", codec="libmp3lame")
	return "File reset", 200




if (__name__ == "__main__"):
	app.run(host = '0.0.0.0', ssl_context=('/etc/letsencrypt/live/invictus.cool-0001/fullchain.pem','/etc/letsencrypt/live/invictus.cool-0001/privkey.pem'), threaded=True)
