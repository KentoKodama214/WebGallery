function clickPhoto(obj) {
	const photo = obj.closest('.photo');
	photo.setAttribute('name', 'openPhoto');
}

function mouseoverPhoto(obj) {
	const favorite = obj.getElementsByClassName('favorite')[0];
	const not_favorite = obj.getElementsByClassName('notFavorite')[0];
	
	if(favorite != undefined && not_favorite != undefined){
		if(!favorite.className.includes('fade-in')){
			favorite.classList.toggle('fade-in');
			not_favorite.classList.toggle('fade-in');
		}
	}
}

function mouseleavePhoto(obj) {
	const favorite = obj.getElementsByClassName('favorite')[0];
	const not_favorite = obj.getElementsByClassName('notFavorite')[0];
	
	if(typeof favorite != 'undefined'){
		favorite.classList.toggle('fade-in');
		not_favorite.classList.toggle('fade-in');
	}
}

async function addFavorite(obj) {
	const photo = obj.parentNode;
	const photo_detail = photo.getElementsByClassName('photo_detail')[0];

	const csrf_token = document.getElementById('csrf_token').value;
	const account_no = photo_detail.getElementsByClassName('accountNo')[0].value;
	const photo_no = photo_detail.getElementsByClassName('photoNo')[0].value;
	
	const requestData = {
		favoritePhotoAccountNo: account_no,
		favoritePhotoNo: photo_no
	};
	const otherParam = {
		headers: {
			"X-CSRF-Token": csrf_token,
			"Content-Type": "application/json; charset=UTF-8"
		},
		method: "POST",
		credentials: "same-origin",
		body: JSON.stringify(requestData)
	};
	
	const response = await fetch('/api/v1/photos/favorites', otherParam)
		.then(response => response.json())
		.catch(err => (console.log(`Fetch problem: ${err.message}`)));
		
	if(response.isSuccess == false) {
		alert(response.message);
		return;
	}
	if(response.httpStatus != 200) {
		const httpStatus = document.getElementById('http_status');
		httpStatus.value = response.httpStatus;
		const errorCode = document.getElementById('error_code');
		errorCode.value = response.errorCode;
		const errorMessage = document.getElementById('error_message');
		errorMessage.value = response.errorMessage;
		const goBackPageUrl = document.getElementById('go_back_page_url');
		goBackPageUrl.value = response.goBackPageUrl;
		
		let errorForm = document.getElementById('error');
		errorForm.submit();
	}
	
	photo_detail.getElementsByClassName('isFavorite')[0].value = 'true';
	const favorite = photo.getElementsByClassName('favorite')[0];
	favorite.style.display = 'block';
	const notFavorite = photo.getElementsByClassName('notFavorite')[0];
	notFavorite.style.display = 'none';
}

async function cancelFavorite(obj) {
	const photo = obj.parentNode;
	const photo_detail = photo.getElementsByClassName('photo_detail')[0];
	
	const csrf_token = document.getElementById('csrf_token').value;
	const account_no = photo_detail.getElementsByClassName('accountNo')[0].value;
	const photo_no = photo_detail.getElementsByClassName('photoNo')[0].value;
	
	const requestData = {
		favoritePhotoAccountNo: account_no,
		favoritePhotoNo: photo_no
	};
	const otherParam = {
		headers: {
			"X-CSRF-Token": csrf_token,
			"Content-Type": "application/json; charset=UTF-8"
		},
		method: "DELETE",
		credentials: "same-origin",
		body: JSON.stringify(requestData)
	};

	const response = await fetch('/api/v1/photos/favorites', otherParam)
		.then(response => response.json())
		.catch(err => (console.log(`Fetch problem: ${err.message}`)));
	
	if(response.isSuccess == false) {
		alert(response.message);
		return;
	}
	if(response.httpStatus != 200) {
		const httpStatus = document.getElementById('http_status');
		httpStatus.value = response.httpStatus;
		const errorCode = document.getElementById('error_code');
		errorCode.value = response.errorCode;
		const errorMessage = document.getElementById('error_message');
		errorMessage.value = response.errorMessage;
		const goBackPageUrl = document.getElementById('go_back_page_url');
		goBackPageUrl.value = response.goBackPageUrl;
		
		let errorForm = document.getElementById('error');
		errorForm.submit();
	}
	
	photo_detail.getElementsByClassName('isFavorite')[0].value = 'false';
	const favorite = photo.getElementsByClassName('favorite')[0];
	favorite.style.display = 'none';
	const notFavorite = photo.getElementsByClassName('notFavorite')[0];
	notFavorite.style.display = 'block';
}