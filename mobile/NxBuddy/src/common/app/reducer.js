import * as actions from './actions';
import { Map, Record } from 'immutable';
import { remove as removeDiacritics } from 'diacritics';

const InitialState = Record({
  online: false,
  storageLoaded: false,
  user: null,
  events: null,
  attendees: null,
  isEventsListOpen: false,
  isPartnerListOpen: true,
  selectedEventKey: null,
  selectedPartnerId: null,
  partners: null,
}, 'app');

export default function appReducer(state = new InitialState, action) {
    console.log(action.type);
    switch (action.type) {
        case actions.RESET: {
            return new InitialState;
        }

        case actions.OPEN_PARTNERS_LISTS:
            return state.set('isPartnerListOpen', true)
                .set('isEventsListOpen', false);

        case actions.APP_OFFLINE:
            return state.set('online', false);

        case actions.APP_ONLINE:
            return state.set('online', true);
        
        case actions.UPDATE_USER:
            return state.set('user', action.payload);

        case actions.APP_STORAGE_LOAD:
            return state.set('storageLoaded', true);
        
        case actions.FETCH_EVENTS_SUCCESS: {
            const events = action.payload;

            return state.set('events', new Map(Object.keys(events).map(key =>
                [key, new Map({
                    ...events[key],
                    key,
                    attendees: new Map(Object.keys(events[key].attendees || {}).map(attendeeKey =>
                        [attendeeKey,
                            events[key].attendees[attendeeKey] ?
                                new Map({
                                    ...events[key].attendees[attendeeKey],
                                })
                            : new Map({})
                        ]
                    ))
                })]
            )));
        }

        case actions.FETCH_PARTNERS_SUCCESS: {
            const partners = action.payload;

            return state.set('partners', new Map(Object.keys(partners).map(key =>
                [key, new Map({
                    ...partners[key],
                    id: key,
                })]
            )));
        }

        case actions.FETCH_ATTENDEES_SUCCESS: {
            const attendees = action.payload;

            return state.set('attendees', new Map(Object.keys(attendees).map(key =>
                [key, new Map({
                    ...attendees[key],
                    key,
                    searchName: removeDiacritics(attendees[key].name.toLowerCase()),
                })]
            )));
        }

        case actions.OPEN_EVENTS_LISTS: {
            return state.set('isEventsListOpen', true);
        }

        case actions.SELECT_EVENT: {
            return state.set('selectedEventKey', action.payload)
                .set('isEventsListOpen', false);
        }

        case actions.SELECT_PARTNER: {
            return state.set('selectedPartnerId', action.payload)
                .set('isPartnerListOpen', false)
                .set('isEventsListOpen', true);
        }

        case actions.CHECK_EVENT_ATTENDEE_ATTENDANCE_START: {
            const { attendeeKey, eventKey, attendee } = action.meta;

            return state.setIn(['events', eventKey, 'attendees', attendeeKey], attendee);
        }
    }

  return state;
}
