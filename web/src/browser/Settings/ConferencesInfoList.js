import React, { Component } from 'react';
import { withStyles } from 'material-ui/styles';
import List, { ListItem, ListItemText, ListItemSecondaryAction } from 'material-ui/List';
import Button from 'material-ui/Button';
import format from 'date-fns/format';


const styles = theme => ({
    root: {
        width: '100%',
        maxWidth: 820,
        margin: 'auto',
        marginTop: '1em',
        backgroundColor: theme.palette.background.paper,
    },
    listItemPrimary: {
        maxWidth: 300,
    },
    listItemSecondary: {
        maxWidth: 150,
    },
    listItemAction: {
        maxWidth: 250,
        width: '100%',
        marginTop: -20,
    }
});

class ConferencesInfoList extends Component {
  render() {
    const { classes, conferences, updateConferenceData, uploadConferenceData } = this.props;

    return (
      <div className={classes.root}>
        <List>
            {conferences.valueSeq().map(conference =>
                <ListItem key={conference.get('id')} divider>
                    <ListItemText
                        className={classes.listItemPrimary}
                        primary={`${conference.get('event_specialization').name} ${format(conference.get('date'), 'DD.MM.YYYY')}`}
                        secondary={`Updated: ${conference.get('lastTimeUpdate') ? format(conference.get('lastTimeUpdate'), 'DD.MM.YYYY HH:mm') : '--'}`}
                    />
                    <ListItemText
                        className={classes.listItemSecondary}
                        primary={' '}
                        secondary={`Attendees: ${conference.get('attendeesCount') || '--'}`}
                    />
                    <ListItemText
                        className={classes.listItemSecondary}
                        primary={' '}
                        secondary={`Activities: ${conference.get('activitiesCount') || '--'}`}
                    />
                    <ListItemSecondaryAction className={classes.listItemAction}>
                        <Button
                            variant="raised"
                            color="primary"
                            onClick={() => updateConferenceData(conference.get('id'))}
                        >Update</Button>

                        <Button
                            style={{ marginLeft: '1em' }}
                            variant="raised"
                            color="primary"
                            disabled={conference.get('uploadTime') === 'UPLOADING'}
                            onClick={() => uploadConferenceData(conference.get('id'))}
                        >
                            {conference.get('uploadTime') ? 'In Troll' : 'Upload'}
                        </Button>
                    </ListItemSecondaryAction>
                </ListItem>
            )}
        </List>
      </div>
    );
  }
}

export default withStyles(styles)(ConferencesInfoList);
