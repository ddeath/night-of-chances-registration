import { reducer as formReducer } from 'redux-form'
import { combineReducers } from 'redux';
import { routerReducer as routing } from 'react-router-redux';

import app from './App/reducer';
import config from './Config/reducer';
import checkin from './CheckIn/reducer';
import registration from './Registration/reducer';
import activities from './Activities/reducer';

export default function configureReducer(initialState, platformReducers) {
  let reducer = combineReducers({
    ...platformReducers,
    routing,
    app,
    config,
    checkin,
    registration,
    activities,
    form: formReducer,
  });

  return reducer;
}
