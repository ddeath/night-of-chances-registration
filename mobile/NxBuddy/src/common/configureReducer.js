import { persistReducer } from 'redux-persist'
import { combineReducers } from 'redux';
import { routerReducer as routing } from 'react-router-redux';
import storage from 'redux-persist/lib/storage';
import immutableTransform from 'redux-persist-transform-immutable';

import app from './app/reducer';
import { LOGOUT } from './app/actions';

const resetStateOnLogout = (reducer, initialState) => (state, action) => {
  // Reset app state on logout, stackoverflow.com/q/35622588/233902.
  if (action.type === LOGOUT) {
    // Preserve state without sensitive data.
    state = {
      app: state.app,
      routing: state.routing // Routing state has to be reused.
    };
  }
  return reducer(state, action);
};

export default function configureReducer(initialState, platformReducers) {
    const config = {
        key: 'root',
        storage,
        transforms: [immutableTransform()],
    };

    let reducer = combineReducers({
        ...platformReducers,
        app,
        routing,
    });

    reducer = persistReducer(config, reducer);

    return reducer;
}
