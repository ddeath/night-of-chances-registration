import React, { Component } from 'react';
import Dialog, {
    DialogActions,
    DialogContent,
    DialogTitle,
} from 'material-ui/Dialog';
import Button from 'material-ui/Button';
import { withStyles } from 'material-ui/styles';
import { connect } from 'react-redux';
import Table, { TableBody, TableCell, TableHead, TableRow } from 'material-ui/Table';

const styles = theme => ({
    timeCell: {
        whiteSpace: 'nowrap',
    }
});

export class AttendeeDetailsDialog extends Component {
    render() {
        const {
            attendee,
            toggleAttendeeDetailsDialog,
            isOpen,
            classes,
            activities,
            companies,
            rooms,
        } = this.props;
        
        if (!activities.size || !companies.size || !rooms.size ) {
            return null;
        }

        return (
            <Dialog
                open={isOpen}
                aria-labelledby="form-dialog-title"
                maxWidth="md"
            >
                <DialogTitle id="form-dialog-title">{attendee.get('name')} {attendee.get('surname')}</DialogTitle>
                <DialogContent>
                    <Table className={classes.table}>
                        <TableHead>
                            <TableRow>
                                <TableCell>Activity name</TableCell>
                                <TableCell>Type</TableCell>
                                <TableCell>Company</TableCell>
                                <TableCell>Room</TableCell>
                                <TableCell>Time</TableCell>
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            {attendee.has('assignments') ?
                                attendee.get('assignments').keySeq().map((key) => {
                                    const activity = activities.get(key);
                                    return (
                                        <TableRow key={key}>
                                            <TableCell>{activity.get('title')}</TableCell>
                                            <TableCell>{activity.get('type')}</TableCell>
                                            <TableCell>{companies.getIn([activities.getIn([key, 'company_id']), 'name'])}</TableCell>
                                            <TableCell>{rooms.getIn([activities.getIn([key, 'room_id']), 'name'])}</TableCell>
                                            <TableCell className={classes.timeCell}>
                                                {activity.get('time_from').substring(0, 5)} - {activity.get('time_to').substring(0, 5)}
                                            </TableCell>
                                        </TableRow>
                                    );
                                })
                                :
                                    <TableRow>
                                        <TableCell colSpan={5} className="text-center">No activities was assigned</TableCell>
                                    </TableRow>
                            }
                        </TableBody>
                    </Table>
                </DialogContent>
                <DialogActions>
                    <Button onClick={() => toggleAttendeeDetailsDialog(null)} color="primary">
                    close
                    </Button>
                </DialogActions>
            </Dialog>
        );
    }
}

AttendeeDetailsDialog = withStyles(styles)(AttendeeDetailsDialog);

export default connect(state => ({
}))(AttendeeDetailsDialog);