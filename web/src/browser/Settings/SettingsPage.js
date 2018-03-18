import React, { Component } from 'react';
import { connect } from 'react-redux';
import Button from 'material-ui/Button';
import Typography from 'material-ui/Typography';
import { withStyles } from 'material-ui/styles';
import format from 'date-fns/format';
import isAfter from 'date-fns/is_after';


import * as actions from '../../common/App/actions';
import ChangeConferenceDialog from './ChangeConferenceDialog';
import ConferencesInfoList from './ConferencesInfoList';


const styles = theme => ({
    formControl: {
        margin: theme.spacing.unit,
        minWidth: 120,
    },
    refreshButton: {
        marginTop: '1em'
    },
});

class SettingsPage extends Component {
  render() {
    const {
        classes,
        conferences,
        updateConferences,
        updateConferenceData,
        activeConferenceId,
        isConferenceChangeDialogOpen,
        toggleConferenceChangeDialog,
        setActiveConference,
        uploadConferenceData,
    } = this.props;

    const sortedConferences = conferences.sort((a, b) => {
        if (isAfter(a.get('date'), b.get('date'))) {
            return -1;
        }

        return 1;
    });

    const activeConference = conferences.get(activeConferenceId);

    return (
      <div className={classes.root}>
        <div>
            {!conferences.size ?
                <Typography>There are no conferences</Typography>
                :
                <div className="col-md-12 text-center">
                    {activeConference ?
                        <h3>{activeConference.get('event_specialization').name} ({format(activeConference.get('date'), 'DD.MM.YYYY')})</h3>
                        :
                        ''
                    }
                    <div>
                        <Button
                            variant="raised"
                            color="primary"
                            onClick={toggleConferenceChangeDialog}
                        >
                            {activeConference ? 'Change active conference' : 'Set active conference'}
                        </Button>
                        
                    </div>
                </div>
            }
        </div>

        <div className="col-md-12">
            {conferences.size ?
                <ConferencesInfoList
                    conferences={sortedConferences}
                    updateConferenceData={updateConferenceData}
                    uploadConferenceData={uploadConferenceData}
                />
                : null
            }
        </div>

        <Button
            className={classes.refreshButton}
            variant="raised"
            color="primary"
            onClick={updateConferences}
        >Refresh</Button>

        <ChangeConferenceDialog
            isOpen={isConferenceChangeDialogOpen}
            conferences={sortedConferences}
            toggleConferenceChangeDialog={toggleConferenceChangeDialog}
            setActiveConference={setActiveConference}
        />
      </div>
    );
  }
}

SettingsPage = withStyles(styles)(SettingsPage);

export default connect(state => ({
  user: state.app.user,
  loadingCount: state.app.loadingCount,
  conferences: state.app.conferences,
  activeConferenceId: state.app.activeConferenceId,
  isConferenceChangeDialogOpen: state.app.isConferenceChangeDialogOpen,
}), actions)(SettingsPage);
