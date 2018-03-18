import React, { Component } from 'react';
import Dialog, {
    DialogActions,
    DialogContent,
    DialogContentText,
    DialogTitle,
} from 'material-ui/Dialog';
import Button from 'material-ui/Button';
import { withStyles } from 'material-ui/styles';

const styles = theme => ({
    paper: {
        color: '#fff',
        backgroundColor: '#fb8c00',
    },
});

export class RestartDialog extends Component {
    render() {
        const {
            isOpen,
            closeAction,
            classes
        } = this.props;

        return (
            <Dialog
                open={isOpen}
                aria-labelledby="form-dialog-title"
                classes={classes}
            >
                <DialogTitle id="form-dialog-title">Active conferece was changed</DialogTitle>
                <DialogContent>
                    <DialogContentText>
                        The active conference was changed and refresh of applications is needed.
                        Please refresh this page.
                        If you will continue of using this app, all data will be write to conference,
                        which is not active at this time.
                    </DialogContentText>
                </DialogContent>
                <DialogActions>
                    <Button type="submit" color="primary" onClick={() => closeAction()}>
                        Close
                    </Button>
                </DialogActions>
            </Dialog>
        );
    }
}

export default withStyles(styles)(RestartDialog);