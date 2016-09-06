function initRegistration(){
	var dbRef = new Firebase("https://nxnoc.firebaseio.com/attendees");

	$('.people-add').click(function(){
		$('.checkin-by-name').addClass('hidden', 'slow');
		$('.registration').removeClass('hidden', 'slow');
	});

	$('.people-search').click(function(){
		$('.checkin-by-name').removeClass('hidden', 'slow');
		$('.registration').addClass('hidden', 'slow');
	});

	$('#register-attendee').click(registerAttendee);
};

function registerAttendee(){
	var attendee = {
		name: $('input[name=name]').val(),
		email: $('input[name=attendee_email]').val(),
		checked_in: "" + Math.round(new Date().getTime()/1000),
		phone: $('input[name=phone]').val(),
		school: $('input[name=school]').val(),
		school_year: $('input[name=school_year]').val(),
		ticket_type: 'ON_SPOT',
	}

	var dbRef = new Firebase("https://nxnoc.firebaseio.com/attendees");
	dbRef.push(attendee, function(error){
		if (error == null)
		{
			$.notify("Účastník pridaný", "success");
			$('input[name=name]').val("");
			$('input[name=attendee_email]').val("");
			$('input[name=phone]').val("");
			$('input[name=school]').val("");
			$('input[name=school_year]').val("");
		}
		else
		{
			$.notify("Nastala chyba pri pridávaní účastníka!" + error, "error");
		}
	})
}
