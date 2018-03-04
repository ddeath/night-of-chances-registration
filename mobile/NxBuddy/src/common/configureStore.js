import { applyMiddleware, createStore, compose } from 'redux';
import { persistStore } from 'redux-persist';

import configureReducer from './configureReducer';
import configureMiddleware from './configureMiddleware';

export default function configureStore(options) {
  const {
    initialState,
    platformDeps = {},
    platformMiddleware = [],
    platformReducers = {},
  } = options;

  const reducer = configureReducer(
    initialState,
    platformReducers
  );

  const middleware = configureMiddleware(
    initialState,
    platformDeps,
    platformMiddleware
  );

  const store = createStore(
      reducer,
      initialState,
      applyMiddleware(...middleware)
    );

  // Enable hot reload where available.
  if (module.hot) {
    const replaceReducer = configureReducer =>
      store.replaceReducer(configureReducer(initialState, platformReducers));

    module.hot.accept('./configureReducer', () => {
      replaceReducer(require('./configureReducer'));
    });
  }

  let persistor = persistStore(store);

  return { persistor, store };
}
