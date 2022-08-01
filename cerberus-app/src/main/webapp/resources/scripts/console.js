var Logger = Logger || {
	enabled : true,
	log : function(msg) {
		if (Logger.enabled) {
			if (window.console) {
				window.console.log(msg);
			}
		}
	}
};

var ConsoleManager = ConsoleManager
		|| {
			IMAGE_SELECTOR : 'img.display',
			NEXT_IMG_SELECTOR : 'img.nxt-img',
			NEXT_IMG_CLASS : 'nxt-img',
			HIGHLIGHT_IMG_CLASS : 'highlight-image',
			IMAGE_GET_ROOT : 'image/',
			IMG_SRC_ATTR : 'src',
			PING : 'ping',
			MOTION : 'motion',
			DEBUG : false,

			isPing : function(data) {
				return data === ConsoleManager.PING;
			},

			isMotion : function(data) {
				// be careful, if 'motion' is anywhere in the filename, this
				// will trigger
				return (data.indexOf(ConsoleManager.MOTION) !== -1);
			},

			highlightImage : function($img) {
				$(ConsoleManager.IMAGE_SELECTOR).removeClass(
						ConsoleManager.HIGHLIGHT_IMG_CLASS);
				$img.addClass(ConsoleManager.HIGHLIGHT_IMG_CLASS);
			},

			setNextImg : function(filename) {
				var debug = ConsoleManager.DEBUG;
				var $images = $(ConsoleManager.IMAGE_SELECTOR);
				var imageCount = $images.length;
				var currentIndex = $images
						.index($(ConsoleManager.NEXT_IMG_SELECTOR));

				if (debug) {
					Logger.log('current index is: ' + currentIndex);
					Logger.log('Found ' + imageCount + ' imgs');
					Logger.log('removing next class');
				}

				var $current = $(ConsoleManager.NEXT_IMG_SELECTOR);
				$current.removeClass(ConsoleManager.NEXT_IMG_CLASS);

				if (debug && $(ConsoleManager.NEXT_IMG_SELECTOR).length > 0) {
					Logger.log('NEXT IMAGE NOT REMOVED!!');
				}

				if (currentIndex + 1 >= imageCount) {
					if (debug) {
						Logger.log('resetting index');
					}
					currentIndex = 0;
				} else {
					currentIndex += 1;
					if (debug) {
						Logger.log('incrementing index to: ' + currentIndex);
					}
				}
				var $nextImage = $images.eq(currentIndex);
				var path = ConsoleManager.IMAGE_GET_ROOT + filename;

				if (debug) {
					Logger.log('adding next class');
					Logger.log('will use path: ' + path);
				}

				$nextImage.addClass(ConsoleManager.NEXT_IMG_CLASS);
				ConsoleManager.highlightImage($nextImage);
				$nextImage.attr(ConsoleManager.IMG_SRC_ATTR, path);
			},

			clearImages : function() {
				$(ConsoleManager.IMAGE_SELECTOR).each(function() {
					$(this).attr('src', '/cerberus/resources/img/default.jpg');
				});
				var $firstImg = $(ConsoleManager.IMAGE_SELECTOR).first();
				ConsoleManager.highlightImage($firstImg);
				$('.' + ConsoleManager.NEXT_IMG_CLASS).removeClass(ConsoleManager.NEXT_IMG_CLASS);
				$firstImg.addClass(ConsoleManager.NEXT_IMG_CLASS);
			}
		};

var StreamManager = StreamManager
		|| {
			STREAM_INTERVAL : 2750,
			DEBUG : false,
			IMAGE_INVOKE_SELECTOR : '.image-invoke',
			STREAM_CURRENT_CLASS : 'stream-current',
			STREAM_INVOKE_SELECTOR : '.stream-invoke',
			STREAM_CURRENT_SELECTOR : '.stream-invoke.stream-current',
			timer : null,

			toggle : function(isOn, $target) {
				if (isOn) {
					if (StreamManager.DEBUG) {
						StreamManager.log('Toggling stream to ON');
					}
					StreamManager.handleStartStream($target);
				} else {
					if (StreamManager.DEBUG) {
						StreamManager.log('Toggling stream to OFF');
					}
					StreamManager.handleStopStream($target);
				}
			},

			handleStartStream : function($target) {
				if (StreamManager.isStreaming()) {
					if (StreamManager.DEBUG) {
						StreamManager.log('isStreaming, will stop the stream');
					}
					StreamManager.handleStopStream($target);
					$target.click();
				} else {
					if (StreamManager.DEBUG) {
						StreamManager.log('Will start stream now');
					}

					$target.addClass(StreamManager.STREAM_CURRENT_CLASS);
					$(StreamManager.IMAGE_INVOKE_SELECTOR).attr('disabled',
							'disabled');
					StreamManager.buttonInCurrentRow().removeAttr('disabled');
					StreamManager.startStream();
				}
			},

			handleStopStream : function($target) {
				if (StreamManager.DEBUG) {
					StreamManager.log('Stopping the stream');
				}
				StreamManager.stopStream();
				var $streaming = $(StreamManager.STREAM_INVOKE_SELECTOR);
				if (StreamManager.DEBUG) {
					StreamManager.log('Found checkboxes to disable: '
							+ $streaming.length);
				}
				$streaming.each(function() {
					if ($(this).is(':checked')) {
						$(this).click();
					}
				});
				$(StreamManager.IMAGE_INVOKE_SELECTOR).removeAttr('disabled');
				$(StreamManager.STREAM_CURRENT_SELECTOR).removeClass(
						StreamManager.STREAM_CURRENT_CLASS);
			},

			isStreaming : function() {
				var $streaming = $(StreamManager.STREAM_CURRENT_SELECTOR);
				if (StreamManager.DEBUG) {
					StreamManager.log('Found streaming: ' + $streaming.length);
				}
				return ($streaming.length === 1);
			},

			buttonInCurrentRow : function() {
				return $(StreamManager.STREAM_CURRENT_SELECTOR).parent()
						.parent().find(StreamManager.IMAGE_INVOKE_SELECTOR);
			},

			startStream : function() {
				StreamManager.stopStream();
				StreamManager.timer = window.setInterval(function() {
					var $button = StreamManager.buttonInCurrentRow();
					if (StreamManager.DEBUG) {
						StreamManager.log('clicking: ' + $button.length);
					}
					$button.click();
				}, StreamManager.STREAM_INTERVAL);
			},

			stopStream : function() {
				if (StreamManager.timer !== null) {
					if (StreamManager.DEBUG) {
						StreamManager.log('clearing interval');
					}
					window.clearInterval(StreamManager.timer);
				}
			},

			log : function(msg) {
				Logger.log(msg);
			}
		};

var MotionService = MotionService
		|| {
			DEBUG : false,
			DELIMETER : '|',
			MOTION_INDEX : 0,
			NAME_INDEX : 1,
			ADDR_INDEX : 2,
			ADDR_SELECTOR : 'address-entry',
			NAME_SELECTOR : 'name-entry',
			MOTION_SELECTOR : 'motion-entry',
			LED_TIMEOUT : 6000,

			timerQueue : [],

			handleMotion : function(data) {
				if (MotionService.DEBUG) {
					Logger.log('Handling data: ' + data);
				}
				var motionData = data.split(MotionService.DELIMETER);
				var motionIndicator = motionData[MotionService.MOTION_INDEX];

				var name = motionData[MotionService.NAME_INDEX];
				var addr = motionData[MotionService.ADDR_INDEX];

				if (motionIndicator !== null) {
					var $addresses = $("." + MotionService.ADDR_SELECTOR);
					Logger.log("Found address entries: " + $addresses.length);
					$addresses.each(function() {
						var $this = $(this);
						var $motion = null;
						if ($this.html() === addr) {
							Logger.log("Found");
							$motion = MotionService.queryMotionEntry($this
									.parent().parent());
							MotionService.startIndication($motion);
						} else {
							Logger.log("Not found");
						}
					});
				} else {
					Logger.log("No motion indicator present");
				}
			},

			queryMotionEntry : function($row) {
				return $row.find("." + MotionService.MOTION_SELECTOR);
			},

			startIndication : function($motion) {
				if (MotionService.DEBUG) {
					Logger.log("Starting motion indicator...");
				}
				$motion.css({
					'visibility' : ''
				});
				var timerId = setTimeout(function() {
					MotionService.timerQueue.shift();
					$motion.css({
						'visibility' : 'hidden'
					});
				}, MotionService.LED_TIMEOUT);
				MotionService.timerQueue.push(timerId);
			}
		};

var AudioManager = AudioManager || {
	resource : null,
	beepAudio : null,
	beepLoaded : false,
	enabled : true,
	playing : false,

	loadBeep : function() {
		if (!AudioManager.beepLoaded) {
			AudioManager.beepAudio = new Audio(AudioManager.resource);
			AudioManager.beepAudio.load();
			AudioManager.beepLoaded = true;
		}
	},

	playBeep : function() {
		if (AudioManager.enabled && !AudioManager.playing) {
			AudioManager.loadBeep();
			AudioManager.playing = true;
			AudioManager.beepAudio.play();
			AudioManager.playing = false;
			AudioManager.beepAudio.currentTime = 0;
		}
	}
};