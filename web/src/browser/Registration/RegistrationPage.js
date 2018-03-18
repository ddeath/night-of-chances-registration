import React, { Component } from 'react';
import { withStyles } from 'material-ui/styles';
import Button from 'material-ui/Button';
import { Field, reduxForm } from 'redux-form';
import { connect } from 'react-redux';


import Input from '../Login/Input';
import * as actions from '../../common/Registration/actions';

const styles = theme => ({
  button: {
    marginTop: '2em',
  }
});

class RegistrationPage extends Component {
  render() {
    const { classes, handleSubmit, registerAttendee, activeConferenceId } = this.props;

    return (
      <div>
        {activeConferenceId ?
          <form onSubmit={handleSubmit(data => registerAttendee(data, activeConferenceId))}>
            <div className="col-md-12">
              <Field
                name="firstName"
                label="First Name"
                type="text"
                component={Input}
              />
            </div>
            <div className="col-md-12">
              <Field
                name="lastName"
                label="Last Name"
                type="text"
                component={Input}
              />
            </div>
            <div className="col-md-12">
              <Field
                name="email"
                label="Email"
                type="email"
                component={Input}
              />
            </div>
            <div className="col-md-12">
              <Field
                name="phone"
                label="Phone"
                type="text"
                component={Input}
              />
            </div>
            <div className="col-md-12">
              <Field
                name="school"
                label="School"
                type="text"
                component={Input}
              />
            </div>
            <div className="col-md-12">
              <Field
                name="schoolYear"
                label="Year in school"
                type="text"
                component={Input}
              />
            </div>
            <div className="col-md-12">
              <Button type="submit" variant="raised" color="primary" className={classes.button}>
                Add
              </Button>
            </div>
          </form>
          :
          <div>There is no active conference</div>
        }
      </div>
    );
  }
}

RegistrationPage = reduxForm({
  form: 'AttendeeRegistration',
  onSubmitSuccess: (result, dispatch, props) => props.reset(),
})(RegistrationPage);

RegistrationPage = withStyles(styles)(RegistrationPage);

export default connect(state => ({
  activeConferenceId: state.app.activeConferenceId,
}), actions)(RegistrationPage);
