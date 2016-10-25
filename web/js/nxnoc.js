$(document).ready(function(){
	$("#password").keyup(function(event){
		if(event.keyCode == 13){
			login();
		}
	});

	$('#login_button').click(login);
});

function initApp(){
	if (typeof(initCheckinSearch) == "function")
	{
		initCheckinSearch();
	}

	if (typeof(initRegistration) == "function")
	{
		initRegistration();
	}
};

function login(){
	var dbRef = new Firebase("https://nxnoc.firebaseio.com/");

	dbRef.authWithPassword({
		"email": $('#email').val(),
		"password": $('#password').val()
	}, function(error, authData) {
		if (error) {
			alert("Login Failed!", error);
		} else {
			$('.login-container').hide('slow');
			$('section.checkin-by-name').removeClass('hidden', 'slow');

			initApp();
		}
	});

	var dbRef = new Firebase("https://nxnoc.firebaseio.com/attendees");
	dbRef.on('value', function(snapshot) {
		checkedIn = 0;
		totalCount = 0;
		snapshot.forEach(function(data){
			totalCount++;
			if (data.val().checked_in !== '') {
				checkedIn++;
			}
		})

		$('.attendees-count').text(checkedIn + '/' + totalCount);
	})

	var dbRef = new Firebase("https://nxnoc.firebaseio.com/attendees");
	dbRef.on('child_changed', function(snapshot) {
		if (snapshot.val().checked_in !== '') {
			checkedIn++;
		} else {
			checked_in--;
		}

		$('.attendees-count').text(checkedIn + '/' + totalCount);
	})

	var dbRef = new Firebase("https://nxnoc.firebaseio.com/events");
	dbRef.on('value', function(snapshot) {
		snapshot.forEach(function(data){
			var totalAttendees = 0;
			var onSiteAttendees = 0;
			var onSiteStandInAttendees = 0;

			var attendees = data.val().attendees;
			var standInAttendees = data.val().standin_attendees

			if (attendees) {
				totalAttendees += Object.keys(attendees).length;
				Object.keys(attendees).forEach(function(key){
					var dbRef2 = new Firebase("https://nxnoc.firebaseio.com/attendees/" + key + "/checked_in");
					dbRef2.once('value', function(snap){
						if (snap.val() !== '') {
							onSiteAttendees++;
						}
					})
				});
			}

			if (onSiteStandInAttendees) {
				totalAttendees += Object.keys(data.val().standin_attendees).length;
				Object.keys(standInAttendees).forEach(function(key){
					var dbRef2 = new Firebase("https://nxnoc.firebaseio.com/attendees/" + key + "/checked_in");
					dbRef2.once('value', function(snap){
						if (snap.val() !== '') {
							onSiteStandInAttendees++;
						}
					})
				});
			}
			
			$('.stats-container').append('<tr id="event-'+data.key+'"><td class="stats-column">'+data.val().name+'</td><td class="stats-column second">'+onSiteAttendees+' + '+ onSiteStandInAttendees +'/'+totalAttendees+'</td></tr>')
		})
	})
}

