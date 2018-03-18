import { Record } from '../transit';

import * as actions from './actions';

const InitialState = Record(
  {
    registrationIsSubscribed: false,
  },
  'registration'
);

export default function registrationReducer(state = new InitialState(), action) {
  switch (action.type) {
    case actions.REGISTRATION_SUBSCRIBED: {
      return state.set('registrationIsSubscribed', true);
    }

    default:
      return state;
  }
}
