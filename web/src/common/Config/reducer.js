import { Record } from '../transit';

const InitialState = Record(
  {
      appName: '',
      appVersion: '',
      sentryUrl: '',
  },
  'config'
);

export default function configReducer(state = new InitialState(), action) {
  switch (action.type) {

    default:
      return state;
  }
}
