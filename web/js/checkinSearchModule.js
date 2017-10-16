function initCheckinSearch(){
	attendeesByName = {};
	var dbRef = new Firebase("https://nxnoc.firebaseio.com/attendees");

	dbRef.on('child_added',   createAttendeesIndex);
	dbRef.on('child_removed', removeAttendeesIndex);

	var substringMatcher = function(strs) {
		return function findMatches(q, cb) {
			var matches, substringRegex;

			// an array that will be populated with substring matches
			matches = [];

			// regex used to determine if a string contains the substring `q`
			substrRegex = new RegExp(q, 'i');

			// iterate through the pool of strings and for any string that
			// contains the substring `q`, add it to the `matches` array
			$.each(Object.keys(strs), function(i, str) {
				if (substrRegex.test(str)) {
					matches.push({value: str, code: strs[str]});
				}
			});

			cb(matches);
		};
	};

	$('#search-input').typeahead({
			hint: false,
			highlight: true,
			minLength: 1,
		},
		{
			name: 'attendees',
			source: substringMatcher(attendeesByName),
			display: 'value',
			limit: 100,
			templates : {
                suggestion: Handlebars.compile('<p><strong>{{value}}</strong> <button onClick="checkInAttendee(\'{{code}}\');" data-toggle="modal" type="button" data-target="#check-in-modal" class="btn btn-primary check-in-button">Check-in</button><button data-toggle="modal" type="button" data-target="#check-in-modal" onclick="showAttendeeInfo(\'{{code}}\', true);" type="button" class="btn btn-primary info-button">Info</button></p><span><a href="javascript:;;" onClick="dontClose()" class="mylink">show</a></span>')
          	}
		}
	);

	$('.twitter-typeahead').bind('typeahead:beforeselect', function (event) {
		event.preventDefault();
	});

	$('.twitter-typeahead').bind('typeahead:onBlurred', function (event) {
		event.preventDefault();
	});
};

function createAttendeesIndex(snapshot){
	var attendeeName = snapshot.val().name + ' (' + snapshot.val().email + ')';
	var attendeeKey = snapshot.key();

	if (!(attendeeName in attendeesByName))
	{
		attendeesByName[attendeeName] = attendeeKey;
	}
};

function removeAttendeesIndex(snapshot){
	var attendeeName = snapshot.val().name + '(' + snapshot.val().email + ')';
	var attendeeKey = snapshot.key();

	if (attendeeName in attendeesByName)
	{
		if (Object.keys(attendeesByName[attendeeName]).length == 0)
		{
			delete attendeesByName[attendeeName];
		}
	}
};

function checkInAttendee(attendee_id)
{
	var dbRef = new Firebase("https://nxnoc.firebaseio.com/attendees/" + attendee_id);
	dbRef.once('value', function (snapshot){
		var checked_in = snapshot.child("checked_in").val();
		if (checked_in == "")
		{
			$("#check-in-modal .modal-content").css('background-color', '#33F761');
			dbRef.child("checked_in").set("" + Math.round(new Date().getTime()/1000));
		}
		else
		{
			$("#check-in-modal .modal-content").css('background-color', '#FF7D7D');
		}
	});

	showAttendeeInfo(attendee_id, false);
}

function showAttendeeInfo(attendee_id, check_presence)
{
	$('#events div').remove();
	$("#notes li").remove();

	var dbRef = new Firebase("https://nxnoc.firebaseio.com/attendees/" + attendee_id);
	dbRef.once('value', function (snapshot){
		if (check_presence)
		{
			var checked_in = snapshot.child("checked_in").val();
			if (checked_in == "")
			{
				$("#check-in-modal .modal-content").css('background-color', '#33F761');
			}
			else
			{
				$("#check-in-modal .modal-content").css('background-color', '#FF7D7D');
			}
		}

		new QRCode(document.getElementById("qrcode"), attendee_id);

		$('#attendee-name').text(snapshot.child('name').val());
		$('#ticket-type').text(snapshot.child('ticket_type').val());
		snapshot.child('notes').forEach(function(note){
			$("#notes").append("<li> " + note.val()  + "</li>");
		});
		
		snapshot.child('events').forEach(function(event){
			var dbRef2 = new Firebase("https://nxnoc.firebaseio.com/events/" + event.key());
			dbRef2.once('value', function (snapshot){
				$('#events').append('<div>' + snapshot.child('name').val() + '</div>')
			});
		});

		snapshot.child('standin_events').forEach(function(event){
			var dbRef2 = new Firebase("https://nxnoc.firebaseio.com/events/" + event.key());
			dbRef2.once('value', function (snapshot){
				$('#events').append('<div>NÁHRADNÍK ' + snapshot.child('name').val() + '</div>')
			});
		});
	});
	
}