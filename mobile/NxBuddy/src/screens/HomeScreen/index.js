import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { StyleSheet, Picker, ScrollView, Text, Image, ImageBackground } from 'react-native';
import { View } from 'react-native-animatable'
import { connect } from 'react-redux';
import { remove as removeDiacritics } from 'diacritics';
import { Map } from 'immutable';

import CustomButton from '../../components/CustomButton';
import bgSrc from '../../images/wallpaper.png';
import logoutSrc from '../../images/logout.png';
import swapSrc from '../../images/swap.png';
import checkedOff from '../../images/checked_off.png';
import checkedOn from '../../images/checked_on.png';
import TouchableView from '../../components/TouchableView';
import CustomTextInput from '../../components/CustomTextInput';
import * as actions from '../../common/app/actions';
import metrics from '../../config/metrics'

/**
 * Just a centered logout button.
 */
export class HomeScreen extends Component {
  static propTypes = {
    logout: PropTypes.func
  }

  state = {
    searchFilter: '',
  }

  componentDidMount() {
    const { fetchActivities, fetchAttendees, fetchPartners, fetchActiveConference } = this.props;

    fetchActiveConference().then(conferenceId => {
      if (conferenceId) {
        fetchActivities(conferenceId);
        fetchAttendees(conferenceId);
        fetchPartners(conferenceId);
      }

      return conferenceId;
    });
  }

  render () {
    const {
      events,
      attendees,
      selectedEventKey,
      isEventsListOpen,
      openPartnersList,
      selectEvent,
      selectPartner,
      checkEventAttendeeAttendance,
      isPartnerListOpen,
      partners,
      selectedPartnerId,
    } = this.props;

    if (!events || !partners) {
      return <View></View>;
    }

    const event = events.get(selectedEventKey);

    let filteredAttendees = new Map();
    let filteredEventAttendees = new Map();
    if (event) {
      filteredAttendees = attendees.filter(attendee => !event.hasIn(['attendees', attendee.get('key')]))
        .filter(attendee => attendee.get('searchName').indexOf(removeDiacritics(this.state.searchFilter.toLowerCase())) !== -1).sort((a, b) => {
        if (a.get('name') < b.get('name')) {
          return -1;
        }

        return 1;
      });

      filteredEventAttendees = event.get('attendees').keySeq().map(key => attendees.get(key).set('key', key))
        .filter(attendee => attendee.get('searchName').indexOf(removeDiacritics(this.state.searchFilter.toLowerCase())) !== -1)
        .sort((a, b) => {
        if (a.get('name') < b.get('name')) {
          return -1;
        }

        return 1;
      });
    }

    return (
      <ImageBackground style={styles.container} source={bgSrc}>
        <View style={styles.headerPanel}>
          {event && !isPartnerListOpen && !isEventsListOpen ?
            <Text style={styles.headerPanelText}>{event.get('name')}</Text>
            : null
          }
          {isPartnerListOpen ?
            <Text style={styles.headerPanelText}>Please select a partner</Text>
            : null
          }
          {isEventsListOpen ?
            <Text style={styles.headerPanelText}>Please select an event</Text>
            : null
          }
        </View>

        {isEventsListOpen ?
          <View style={styles.scrollContainer}>
            {events.filter(event => event.get('partnerId') === selectedPartnerId).size ?
              <ScrollView contentContainerStyle={styles.eventsList}>
                {events.filter(event => event.get('partnerId') === selectedPartnerId).valueSeq().sort((a, b) => {
                  if (a.get('name') < b.get('name')) {
                    return -1;
                  }

                  return 1;
                }).map(event =>
                  <CustomButton
                    key={event.get('key')}
                    onPress={() => selectEvent(event.get('key'))}
                    text={event.get('name')}
                    buttonStyle={styles.eventListItemButton}
                    textStyle={styles.eventListItemButtonText}
                  />
                )}
              </ScrollView>
              :
              <Text>This partner does not have any events</Text>
            }
          </View>
          :
          null
        }

        {isPartnerListOpen ?
          <View style={styles.scrollContainer}>
            <ScrollView contentContainerStyle={styles.eventsList}>
              {partners.valueSeq().sort((a, b) => {
                if (a.get('name') < b.get('name')) {
                  return -1;
                }

                return 1;
              }).map(partner =>
                <CustomButton
                  key={partner.get('id')}
                  onPress={() => selectPartner(partner.get('id'))}
                  text={partner.get('name')}
                  buttonStyle={styles.eventListItemButton}
                  textStyle={styles.eventListItemButtonText}
                />
              )}
            </ScrollView>
          </View>
          : null
        }

        <View style={styles.searchContainer}>
          <CustomTextInput
            name={'searchFilter'}
            ref={(ref) => this.passwordInputRef = ref}
            placeholder={'Search'}
            returnKeyType={'done'}
            withRef={true}
            onChangeText={(value) => this.setState({ searchFilter: value })}
            value={this.state.searchFilter}
            placeholderTextColor={'white'}
            textColor={'white'}
            width={metrics.DEVICE_WIDTH - (metrics.DEVICE_WIDTH * 0.1 * 2)}
          />
          <CustomButton
            text={'X'}
            buttonStyle={styles.clearButton}
            textStyle={styles.clearButtonText}
            onPress={() => this.setState({ searchFilter: '' })}
          />
        </View>
        {!isEventsListOpen && !isPartnerListOpen && event && event.get('attendees') ?
          <View style={styles.scrollContainer}>
            <ScrollView contentContainerStyle={styles.eventsList}>
              {filteredEventAttendees.map(attendee => {
                const eventAttendee = events.getIn([selectedEventKey, 'attendees', attendee.get('key')]);
                const isIn = eventAttendee.get('checkedIn') && !eventAttendee.get('checkedOut');

                return (
                  <CustomButton
                    key={attendee.get('key')}
                    text={attendee.get('name')}
                    buttonStyle={styles.attendeeListItemButton}
                    textStyle={styles.attendeeListItemText}
                    isNew={eventAttendee.get('isNew')}
                    onPress={() => checkEventAttendeeAttendance(attendee.get('key'), selectedEventKey, isIn ? 'out' : 'in', false)}
                    icon={
                      <Image
                        style={styles.attendeeIcon}
                        source={isIn ? checkedOn : checkedOff}
                      />
                    }
                  />
                );
              })}
              {this.state.searchFilter && filteredAttendees.size ?
                filteredAttendees.valueSeq().take(10).map(attendee => {
                  return (
                    <CustomButton
                      key={attendee.get('key')}
                      text={attendee.get('name')}
                      buttonStyle={styles.attendeeListItemButton}
                      textStyle={styles.attendeeListItemText}
                      isNew
                      onPress={() => checkEventAttendeeAttendance(attendee.get('key'), selectedEventKey, 'in', true)}
                      icon={
                        <Image
                          style={styles.attendeeIcon}
                          source={checkedOff}
                        />
                      }
                    />
                  );
                })
                : null
              }
            </ScrollView>
          </View>
          : null
        }
        
        <View style={styles.footerPanel}>
          <TouchableView onPress={openPartnersList} style={styles.footerButtonLeft}>
            <Image style={styles.footerButtonIconLeft} source={swapSrc} />
            <Text style={styles.footerButtonText}>Change Workshop</Text>
          </TouchableView>
          <TouchableView onPress={this.props.logout} style={styles.footerButtonRight}>
            <Image style={styles.footerButtonIconRight} source={logoutSrc} />
            <Text style={styles.footerButtonText}>Logout</Text>
          </TouchableView>
        </View>
      </ImageBackground>
    )
  }
}

export default connect(state => ({
  events: state.app.get('events'),
  attendees: state.app.get('attendees'),
  isEventsListOpen: state.app.get('isEventsListOpen'),
  isPartnerListOpen: state.app.get('isPartnerListOpen'),
  partners: state.app.get('partners'),
  selectedEventKey: state.app.get('selectedEventKey'),
  selectedPartnerId: state.app.get('selectedPartnerId'),
}), actions)(HomeScreen);

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  searchContainer: {
    height: 52,
    borderWidth: 1,
    borderRadius: 3,
    borderColor: 'rgba(0, 0, 0, 0.3)',
    backgroundColor: 'rgba(1, 1, 1, 0.3)',
    flexDirection: 'row',
    paddingHorizontal: metrics.DEVICE_WIDTH * 0.1
  },
  clearButton: {
    width: metrics.DEVICE_WIDTH * 0.1,
    backgroundColor: 'rgba(0, 0, 0, 0.3)',
  },
  clearButtonText: {
    fontWeight: "bold",
    fontSize: 20,
  },
  scrollContainer: {
    flex: 1,
    marginBottom: 90,
    marginTop: 30,
  },
  eventsList: {
    padding: 10,
  },
  eventListItemButton: {
    backgroundColor: 'rgba(255, 255, 255, 0.2)',
    margin: 2,
  },
  eventListItemButtonText: {
    color: 'white',
    padding: 10,
  },
  attendeeListItemText: {
    color: 'white',
    textAlign: 'left',
  },
  attendeeListItemButton: {
    alignItems: 'center',
    justifyContent: 'flex-start',
  },
  attendeeIcon: {
    marginLeft: 10,
    marginRight: 10,
  },
  button: {
    backgroundColor: '#1976D2',
    margin: 20
  },
  headerPanel: {
    backgroundColor: '#F035E0',
    height: 50,
    alignItems: 'center',
    justifyContent: 'center',
    padding: 10,
  },
  headerPanelText: {
    color: 'white',
    textAlign: 'center',
  },
  buttonText: {
    color: 'white',
    fontWeight: 'bold'
  },
  footerButtonLeft: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    borderColor: 'white',
    borderRightWidth: 1
  },
  footerButtonRight: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  footerButtonIconLeft: {
    flex: 1,
    marginTop: 10,
    width: 40,
  },
  footerButtonIconRight: {
    flex: 1,
    marginTop: 10,
    width: 30,
  },
  footerButtonText: {
    flex: 1,
    color: 'white',
  },
  footerPanel: {
    backgroundColor: '#F035E0',
    height: 70,
    bottom: 0,
    width: '100%',
    flexDirection: 'row',
    alignItems: 'stretch',
    position: 'absolute',
  },
})
