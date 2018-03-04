import promiseMiddleware from 'redux-promise-middleware';

// Deps.
import uuidv4 from 'uuid';

// Like redux-thunk but with dependency injection.
const injectMiddleware = deps => ({ dispatch, getState }) => next => action =>
  next(typeof action === 'function'
    ? action({ ...deps, dispatch, getState })
    : action
  );

export default function configureMiddleware(initialState, platformDeps, platformMiddleware) {
  const middleware = [
    injectMiddleware({
      ...platformDeps,
      getid: () => uuidv4(),
      now: () => Date.now(),
    }),
    promiseMiddleware({
      promiseTypeSuffixes: ['START', 'SUCCESS', 'ERROR']
    }),
    ...platformMiddleware,
  ];

  return middleware;
}
