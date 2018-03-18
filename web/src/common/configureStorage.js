import invariant from 'invariant';
import storage from 'redux-storage';
import storageDebounce from 'redux-storage-decorator-debounce';
import { Iterable } from 'immutable';
import { toJSON } from './transit';

const stateToSave = [];

const invariantFeatureState = (state, feature) =>
  invariant(
    Iterable.isIterable(state[feature]),
    `Storage persists only immutable iterables. '${feature}' is something else.`
  );

const storageFilter = (engine) => ({
  ...engine,
  save(state) {
    const saveState = stateToSave.map(([feature, ...featurePath]) => {
      invariantFeatureState(state, feature);
      return {
        feature,
        featurePath,
        value: state[feature].getIn(featurePath),
      };
    });
    return engine.save(toJSON(saveState));
  },
});

const createStorageMiddleware = (storageEngine) => {
  let decoratedEngine = storageFilter(storageEngine);
  decoratedEngine = storageDebounce(decoratedEngine, 300);
  return storage.createMiddleware(decoratedEngine);
};

export const updateStateOnStorageLoad = (reducer) => (state, action) => {
  return reducer(state, action);
};

export default function configureStorage(initialState, createStorageEngine) {
  const storageEngine =
    createStorageEngine &&
    createStorageEngine(`redux-storage:${initialState.config.appName}`);
  const storageMiddleware =
    storageEngine && createStorageMiddleware(storageEngine);

  return {
    STORAGE_SAVE: storage.SAVE,
    storageEngine,
    storageMiddleware,
  };
}
