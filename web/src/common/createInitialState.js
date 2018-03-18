import config from './config';
import configReducer from './Config/reducer';

export default function createInitialState() {
  return {
    config: configReducer(undefined, {})
      .set('appName', config.appName)
      .set('appVersion', config.appVersion)
      .set('sentryUrl', config.sentryUrl),
  };
}
