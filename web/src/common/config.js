const config = {
    appName: 'Night of Chances Registration',
    // Use appVersion defined in gulp env task.
    appVersion: process.env.appVersion,
    isProduction: process.env.NODE_ENV === 'production',
    port: process.env.PORT || 8000,
    // Enable hot reload on remote device. Note it prevents offline testing,
    // because it depends on ip.address(), which doesn't work with disabled wifi.
    // How do we access a website running on localhost from mobile browser?
    // stackoverflow.com/questions/3132105
    remoteHotReload: false,
    sentryUrl: '',
    APIS: {
      default: process.env.REACT_APP_API_URL,
      cloud: process.env.REACT_APP_FIREBASE_FUNTIONS,
    },
  };
  
  export default config;
  