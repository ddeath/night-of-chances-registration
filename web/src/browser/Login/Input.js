import React, { Component } from 'react';
import TextField from 'material-ui/TextField';
import { withStyles } from 'material-ui/styles';


const styles = theme => ({
  textField: {
    marginLeft: theme.spacing.unit,
    marginRight: theme.spacing.unit,
    width: 200,
  },
});

class Input extends Component {
  render() {
    const { classes, input, type, label, meta } = this.props;
    return (
      <TextField
        id={input.name}
        label={label}
        className={classes.textField}
        value={input.value}
        onChange={input.onChange}
        margin="normal"
        type={type}
        error={meta.touched && meta.error}
      />
    );
  }
}

export default withStyles(styles)(Input);
