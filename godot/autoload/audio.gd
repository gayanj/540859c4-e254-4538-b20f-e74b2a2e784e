extends Node
## Music and sound playback. Port of utils/AudioManager.java.

const MUSIC_DIR := "res://assets/music/"
const SOUND_DIR := "res://assets/sounds/"
const GAME_TRACKS := ["background", "background2", "background3", "background4", "background5"]
const SFX_VOICES := 8

var _music := AudioStreamPlayer.new()
var _alert := AudioStreamPlayer.new()
var _sfx: Array[AudioStreamPlayer] = []
var _streams: Dictionary = {}
## The one-shot "particle death" sound is restarted on every kill (original behaviour).
var _particle_death := AudioStreamPlayer.new()


func _ready() -> void:
	process_mode = Node.PROCESS_MODE_ALWAYS
	_music.bus = "Master"
	add_child(_music)
	add_child(_alert)
	add_child(_particle_death)
	for _i in SFX_VOICES:
		var p := AudioStreamPlayer.new()
		add_child(p)
		_sfx.append(p)


func _exit_tree() -> void:
	# Stop everything on shutdown. (Godot's headless/dummy audio driver may still
	# report the active music stream as "in use" at exit; that warning is benign.)
	_music.stop()
	_alert.stop()
	_particle_death.stop()
	for p in _sfx:
		p.stop()
	_music.stream = null
	_alert.stream = null
	_particle_death.stream = null
	_streams.clear()


func _stream(dir: String, name: String, loop: bool) -> AudioStream:
	var path := dir + name + ".ogg"
	if not _streams.has(path):
		var s: AudioStream = load(path)
		if s == null:
			push_warning("Audio: missing stream %s" % path)
			return null
		if s is AudioStreamOggVorbis:
			s.loop = loop
		_streams[path] = s
	return _streams[path]


func play_music(name: String, volume_linear := 1.0) -> void:
	var s := _stream(MUSIC_DIR, name, true)
	if s == null:
		return
	_music.stream = s
	_music.volume_db = linear_to_db(volume_linear)
	_music.play()


func play_random_game_track() -> void:
	play_music(GAME_TRACKS[randi() % GAME_TRACKS.size()])


func stop_music() -> void:
	_music.stop()


func play_sound(name: String, volume_linear := 1.0) -> void:
	var s := _stream(SOUND_DIR, name, false)
	if s == null:
		return
	for p in _sfx:
		if not p.playing:
			p.stream = s
			p.volume_db = linear_to_db(volume_linear)
			p.play()
			return
	# All voices busy: steal the first one.
	_sfx[0].stream = s
	_sfx[0].volume_db = linear_to_db(volume_linear)
	_sfx[0].play()


func play_particle_death() -> void:
	if _particle_death.stream == null:
		_particle_death.stream = _stream(SOUND_DIR, "particle_death", false)
	_particle_death.play()


## Looping low-energy warning (sounds/alert.ogg).
func start_alert(volume_linear := 0.3) -> void:
	if _alert.playing:
		return
	_alert.stream = _stream(SOUND_DIR, "alert", true)
	_alert.volume_db = linear_to_db(volume_linear)
	_alert.play()


func stop_alert() -> void:
	if _alert.playing:
		_alert.stop()
