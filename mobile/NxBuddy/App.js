import React, { Component } from 'react';
import { connect } from 'react-redux';
import firebase from 'react-native-firebase';
import { purge } from 'redux-persist';

import AuthScreen from './src/screens/AuthScreen';
import HomeScreen from './src/screens/HomeScreen';
import * as actions from './src/common/app/actions';


/**
 * The root component of the application.
 * In this component I am handling the entire application state, but in a real app you should
 * probably use a state management library like Redux or MobX to handle the state (if your app gets bigger).
 */
export class App extends Component {
  state = {
    isLoggedIn: false, // Is the user authenticated?
    isLoading: false,
    loginFailed: false, // Is the user loggingIn/signinUp?
    isAppReady: false, // Has the app completed the login animation?
    unsubscriber: null,
  }

  /**
   * Two login function that waits 1000 ms and then authenticates the user succesfully.
   * In your real app they should be replaced with an API call to you backend.
   */
  _simulateLogin = (email, password) => {
    this.setState({ isLoading: true });
    const cred = firebase.auth().signInWithEmailAndPassword(
        email,
        password
    ).then(
      () => this.setState({ isLoggedIn: true, isLoading: false, loginFailed: false }),
      () => this.setState({ isLoggedIn: false, isLoading: false, loginFailed: true })
    );
  }

  _simulateSignup = (username, password, fullName) => {
    this.setState({ isLoading: true })
    setTimeout(() => this.setState({ isLoggedIn: true, isLoading: false }), 1000)
  }

  componentDidMount() {
    const { updateUser } = this.props;

    this.unsubscriber = firebase.auth().onAuthStateChanged((user) => {
      if (user) {
        updateUser({
          diplayName: user.displayName,
          email: user.email,
          phoneNumber: user.phoneNumber,
          photoUrl: user.photoUrl,
          uid: user.uid,
        });
      } else {
        updateUser(null);
      }
    });
  }

  componentWillUnmount() {
    if (this.unsubscriber) {
      this.unsubscriber();
      console.log('unsubscribe');
    }
  }

  /**
   * Simple routing.
   * If the user is authenticated (isAppReady) show the HomeScreen, otherwise show the AuthScreen
   */
  render () {
    const { user, resetApp } = this.props;

    if (user) {
      return (
        <HomeScreen
          logout={() => {
            firebase.auth().signOut();
            resetApp();
            this.props.persistor.purge();
          }}
        />
      )
    } else {
      return (
        <AuthScreen
          login={this._simulateLogin}
          signup={this._simulateSignup}
          isLoggedIn={this.state.isLoggedIn}
          isLoading={this.state.isLoading}
          loginFailed={this.state.loginFailed}
          onLoginAnimationCompleted={() => this.setState({ isAppReady: true })}
        />
      )
    }
  }
}

export default connect(state => ({
  user: state.app.get('user'),
}), actions)(App);
