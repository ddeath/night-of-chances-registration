import React, { Component } from 'react';
import Dialog, {
    DialogActions,
    DialogContent,
    DialogContentText,
    DialogTitle,
} from 'material-ui/Dialog';
import Button from 'material-ui/Button';
import { InputLabel } from 'material-ui/Input';
import { FormControl } from 'material-ui/Form';
import Select from 'material-ui/Select';
import format from 'date-fns/format';
import { withStyles } from 'material-ui/styles';
import { connect } from 'react-redux';
import { Field, reduxForm } from 'redux-form';

const styles = theme => ({
    paper: {
        color: '#fff',
        backgroundColor: '#fb8c00',
    },
});

const renderSelector = (field, conferences) => {
    return (
        <FormControl style={{ margin: 'auto', marginTop: '2em' }}>
            <InputLabel htmlFor="active-conference">Active conference:</InputLabel>
            <Select
                native
                value={field.input.value}
                onChange={field.input.onChange}
                inputProps={{
                    id: 'active-conference',
                }}
            >
                <option value={null} />
                {conferences.valueSeq().map((conference, key) =>
                    <option key={key} value={conference.get('id')}>{conference.get('event_specialization').name} ({format(conference.get('date'), 'DD.MM.YYYY')})</option>
                )}
            </Select>
        </FormControl>
    );
};

export class FormDialog extends Component {
    render() {
        const {
            conferences,
            toggleConferenceChangeDialog,
            isOpen,
            setActiveConference,
            classes,
            handleSubmit,
        } = this.props;

        return (
            <Dialog
                open={isOpen}
                aria-labelledby="form-dialog-title"
                classes={classes}
            >
                <DialogTitle id="form-dialog-title">Change active conference</DialogTitle>
                <form onSubmit={handleSubmit(data => setActiveConference(data.conferenceId))}>
                    <DialogContent>
                        <DialogContentText>
                            This action should be done before conference.
                            If you change active conference,
                            all instances of the app in will be forced to reload.
                        </DialogContentText>
                        
                        <div style={{ width: '100%', textAlign: 'center' }}>
                            <Field
                                name="conferenceId"
                                component={(field) => renderSelector(field, conferences)}
                                normalize={value => parseInt(value, 10)}
                            />
                        </div>
                    </DialogContent>
                    <DialogActions>
                        <Button onClick={toggleConferenceChangeDialog} color="primary">
                        Cancel
                        </Button>
                        <Button type="submit" color="primary">
                            Set active conference
                        </Button>
                    </DialogActions>
                </form>
            </Dialog>
        );
    }
}

FormDialog = reduxForm({
    form: 'ChangeActiveConferenceDialogForm'
})(FormDialog);

FormDialog = withStyles(styles)(FormDialog);

export default connect(state => ({
}))(FormDialog);