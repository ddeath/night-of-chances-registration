import promiseMiddleware from 'redux-promise-middleware';
import uuidv4 from 'uuid';

import fetch from './fetch';
import configureStorage from './configureStorage';

// Like redux-thunk but with dependency injection.
const injectMiddleware = (deps) => ({ dispatch, getState }) => (next) => (
  action
) =>
  next(
    typeof action === 'function'
      ? action({ ...deps, dispatch, getState })
      : action
  );

export default function configureMiddleware(
  initialState,
  platformDeps,
  platformMiddleware
) {
  const { storageEngine, storageMiddleware } = configureStorage(
    initialState,
    platformDeps.createStorageEngine
  );

  const middleware = [
    injectMiddleware({
      ...platformDeps,
      fetch,
      getid: () => uuidv4(),
      now: () => Date.now(),
      storageEngine,
    }),
    promiseMiddleware({
      promiseTypeSuffixes: ['START', 'SUCCESS', 'ERROR'],
    }),
    ...platformMiddleware,
  ];

  if (storageMiddleware) {
    middleware.push(storageMiddleware);
  }

  return middleware;
}
