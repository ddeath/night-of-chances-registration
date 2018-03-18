
import toastr from 'toastr';
import uuidv4 from 'uuid';

import { setAttendeeAttendance } from '../CheckIn/actions';

export const REGISTER_ATTENDEE = 'REGISTER_ATTENDEE';
export const REGISTER_ATTENDEE_START = 'REGISTER_ATTENDEE_START';
export const REGISTER_ATTENDEE_SUCCESS = 'REGISTER_ATTENDEE_SUCCESS';
export const REGISTER_ATTENDEE_ERROR = 'REGISTER_ATTENDEE_ERROR';

export const REGISTRATION_SUBSCRIBED = 'REGISTRATION_SUBSCRIBED';
export const REGISTRATION_SUBSCRIBED_START = 'REGISTRATION_SUBSCRIBED_START';
export const REGISTRATION_SUBSCRIBED_SUCCESS = 'REGISTRATION_SUBSCRIBED_SUCCESS';
export const REGISTRATION_SUBSCRIBED_ERROR = 'REGISTRATION_SUBSCRIBED_ERROR';

export const NEW_ATTENDEE_REGISTERED = 'NEW_ATTENDEE_REGISTERED';

export function registerAttendee(data, conferenceId) {
  const attendeeData = {
    id: uuidv4(),
    name: data.firstName,
    surname: data.lastName,
    email: data.email,
    phone: data.phone,
    school: data.school,
    students_year: data.schoolYear
  };

  return ({ firebase, dispatch }) => ({
    type: REGISTER_ATTENDEE,
    payload: {
      promise: firebase.database().ref(`/attendance/conference_${conferenceId}/onspot/${attendeeData.id}`)
        .set(attendeeData).then(snapshot => {
            toastr.options.timeOut = 3000;
            toastr.success('Registration was successful');
            dispatch(setAttendeeAttendance(true, attendeeData.id, conferenceId)).then(() => {
                toastr.options.timeOut = 3000;
                toastr.success('Attendee was checked in');
            });
        })
    }
  });
}

export function appendAttendee(key, data) {
  return {
    type: NEW_ATTENDEE_REGISTERED,
    payload: {
      key,
      data,
    }
  }
}

export function subscribeRegistrations(conferenceId) {
    return ({ firebase, getState, dispatch }) => {
        if (getState().registration.registrationIsSubscribed) {
          return { type: 'OPERATION_NOT_NEEDED' };
        }
    
        return {
          type: REGISTRATION_SUBSCRIBED,
          payload: firebase.database().ref(`/attendance/conference_${conferenceId}/onspot`)
              .on('child_added', snapshot => {
                if (snapshot) {
                  dispatch(appendAttendee(snapshot.key, snapshot.val()))
                }
              }),
        }
    };
}