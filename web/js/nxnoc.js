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
}

