/**
 *  Summer Window Notifications
 *
 *  Copyright 2018 Ryan Haack
 *
 *  Powered by WeatherUnderground
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
definition(
    name: "Summer Window Notifications",
    namespace: "haackr",
    author: "Ryan Haack",
    description: "Notifies you when the temperature outside becomes warmer/cooler than inside so you can close/open your windows. Powered by Weather Underground.",
    category: "Green Living",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")

preferences {
	section("Indoor Sensor") {
        input "tempSensorIn", "capability.temperatureMeasurement", required:true
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {
    compareTemps()
}

def getOutsideTemperature() {
	log.debug "${location.latitude},${location.longitude}"
	def params = [
    	uri: "http://api.wunderground.com",
        path: "/api/3882ac765dbb5837/conditions/q/${location.latitude},${location.longitude}.json"
    ]
    try{
    	httpGet(params) {resp ->
        	//log.debug "${resp.data}"
            log.debug "${resp.data.current_observation.temp_f}"
            return resp.data.current_observation.temp_f
        }
    } catch (e) {
    	log.error "Something went wrong contacting weather undergound: ${e}"
    }
}

def compareTemps() {
	def outsideTemp = getOutsideTemperature()
    def insideTemp = tempSensorIn.currentValue("temperature")
    log.debug "inside: ${insideTemp}, outside: ${outsideTemp}"
    log.debug "last notification: ${state.lastNotification}"
    if(outsideTemp != null && insideTemp != null) {
        if (outsideTemp > insideTemp && state.lastNotification != "close"){
            state.lastNotification = "close"
            log.debug "Close the windows - last notification: ${state.lastNotification}"
            sendPush("Close the windows, it's hot outside!")
        }else if (outsideTemp < insideTemp && state.lastNotification != "open"){
            state.lastNotification = "open"
            log.debug "Open the windows - last notification: ${state.lastNotification}"
            sendPush("Open the windows, it's cooling off!")
        }else {
            log.debug "Do nothing"
        }
    }
    runIn(60*10,compareTemps)
}