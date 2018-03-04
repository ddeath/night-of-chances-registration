import React, { Component } from 'react';
import { AppRegistry } from 'react-native';
import { PersistGate } from 'redux-persist/es/integration/react'
import { Provider } from 'react-redux';
import { Text } from 'react-native';

import App from './App';
import configureStore from './src/common/configureStore';

const { persistor, store } = configureStore({
    initialState: {},
    platformDeps: { },
    platformMiddleware: [],
});

class DecoratedApp extends Component {
    render () {
        return (
            <Provider store={store}>
                <PersistGate persistor={persistor} loading={<Text>Loading</Text>}>
                    <App persistor={persistor} />
                </PersistGate>
            </Provider>
        );
    }
};

AppRegistry.registerComponent('NxNoc', () => DecoratedApp);
