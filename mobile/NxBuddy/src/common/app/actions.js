import firebase from 'react-native-firebase';
import { Map } from 'immutable';

export const LOGOUT = 'LOGOUT';
export const APP_OFFLINE = 'APP_OFFLINE';
export const APP_ONLINE = 'APP_ONLINE';
export const APP_STORAGE_LOAD = 'APP_STORAGE_LOAD';
export const UPDATE_USER = 'UPDATE_USER';
export const OPEN_EVENTS_LISTS = 'OPEN_EVENTS_LISTS';
export const SELECT_EVENT = 'SELECT_EVENT';
export const SELECT_PARTNER = 'SELECT_PARTNER';
export const OPEN_PARTNERS_LISTS = 'OPEN_PARTNERS_LISTS';
export const RESET = 'RESET';

export const FETCH_EVENTS = 'FETCH_EVENTS';
export const FETCH_EVENTS_START = 'FETCH_EVENTS_START';
export const FETCH_EVENTS_SUCCESS = 'FETCH_EVENTS_SUCCESS';
export const FETCH_EVENTS_ERROR = 'FETCH_EVENTS_ERROR';

export const FETCH_ATTENDEES = 'FETCH_ATTENDEES';
export const FETCH_ATTENDEES_START = 'FETCH_ATTENDEES_START';
export const FETCH_ATTENDEES_SUCCESS = 'FETCH_ATTENDEES_SUCCESS';
export const FETCH_ATTENDEES_ERROR = 'FETCH_ATTENDEES_ERROR';

export const FETCH_PARTNERS = 'FETCH_PARTNERS';
export const FETCH_PARTNERS_START = 'FETCH_PARTNERS_START';
export const FETCH_PARTNERS_SUCCESS = 'FETCH_PARTNERS_SUCCESS';
export const FETCH_PARTNERS_ERROR = 'FETCH_PARTNERS_ERROR';

export const CHECK_EVENT_ATTENDEE_ATTENDANCE = 'CHECK_EVENT_ATTENDEE_ATTENDANCE';
export const CHECK_EVENT_ATTENDEE_ATTENDANCE_START = 'CHECK_EVENT_ATTENDEE_ATTENDANCE_START';
export const CHECK_EVENT_ATTENDEE_ATTENDANCE_SUCCESS = 'CHECK_EVENT_ATTENDEE_ATTENDANCE_SUCCEESS';
export const CHECK_EVENT_ATTENDEE_ATTENDANCE_ERROR = 'CHECK_EVENT_ATTENDEE_ATTENDANCE_ERROR';

export const FETCH_ACTIVE_CONFERENCE_ID = 'FETCH_ACTIVE_CONFERENCE_ID';
export const FETCH_ACTIVE_CONFERENCE_ID_START = 'FETCH_ACTIVE_CONFERENCE_ID_START';
export const FETCH_ACTIVE_CONFERENCE_ID_SUCCESS = 'FETCH_ACTIVE_CONFERENCE_ID_SUCCESS';
export const FETCH_ACTIVE_CONFERENCE_ID_ERROR = 'FETCH_ACTIVE_CONFERENCE_ID_ERROR';


export function updateUser(user) {
    return {
        type: UPDATE_USER,
        payload: user,
    };
}

export function openEventsList() {
    return {
        type: OPEN_EVENTS_LISTS,
    };
}

export function openPartnersList() {
    return {
        type: OPEN_PARTNERS_LISTS,
    };
}

export function fetchActiveConference() {
    return ({ getState }) => {
        const ref = firebase.database().ref('activeConferenceId');
        return {
            type: FETCH_ACTIVE_CONFERENCE_ID,
            payload: {
                promise: ref.once('value').then(response => response.val()),
            },
        };
    };
}

export function fetchActivities(conferenceId) {
    return ({ getState }) => {
        const state = getState();

        if (state.app.get('events')) {
            return { type: 'NO_UPDATE' };
        }

        const ref = firebase.database().ref(`activities/conference_${conferenceId}`);
        return {
            type: FETCH_EVENTS,
            payload: {
                promise: ref.once('value').then(response => response.val()),
            },
        };
    };
}

export function fetchPartners(conferenceId) {
    return ({ getState }) => {
        const state = getState();

        if (state.app.get('partners')) {
            return { type: 'NO_UPDATE' };
        }

        const ref = firebase.database().ref(`companies/conference_${conferenceId}`);
        return {
            type: FETCH_PARTNERS,
            payload: {
                promise: ref.once('value').then(response => response.val()),
            },
        };
    };
}

export function fetchAttendees(conferenceId) {
    return ({ getState }) => {
        const state = getState();

        if (state.app.get('attendees')) {
            return { type: 'NO_UPDATE' };
        }

        const ref = firebase.database().ref(`attendees/conference_${conferenceId}`);
        return {
            type: FETCH_ATTENDEES,
            payload: {
                promise: ref.once('value').then(response => response.val()),
            },
        };
    };
}

export function selectEvent(eventKey){
    return {
        type: SELECT_EVENT,
        payload: eventKey,
    };
}

export function selectPartner(partnerId){
    return {
        type: SELECT_PARTNER,
        payload: partnerId,
    };
}

export function resetApp(){
    return {
        type: RESET,
    };
}

export function checkEventAttendeeAttendance(attendeeKey, eventKey, direction, isNew) {
    return ({ getState }) => {
        const state = getState();

        let attendee = state.app.getIn(['events', eventKey, 'attendees', attendeeKey]);

        if (!attendee) {
            attendee = new Map({ isNew });
        }

        if (direction === 'in') {
            attendee = attendee.set('checkedIn', (new Date()).toISOString());
            attendee = attendee.set('checkedOut', "");
        } else {
            attendee = attendee.set('checkedIn', "");
            attendee = attendee.set('checkedOut', (new Date()).toISOString());
        }

        const ref = firebase.database().ref(`events/${eventKey}/attendees/${attendeeKey}`);
        return {
            type: CHECK_EVENT_ATTENDEE_ATTENDANCE,
            meta: {
                attendeeKey,
                eventKey,
                attendee,
            },
            payload: {
                promise: ref.set(attendee.toJS()),
            },
        };
    };
}
