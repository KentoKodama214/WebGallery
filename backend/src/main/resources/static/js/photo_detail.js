window.onload = () => {
	const focalLength = document.getElementsByName('focalLength')[0].value;
	const fValue = document.getElementsByName('fValue')[0].value;
	const shutterSpeed = document.getElementsByName('shutterSpeed')[0].value;
	const iso = document.getElementsByName('iso')[0].value;
	const setting_tag = document.getElementsByClassName('setting')[0];
	let setting = '';
	
	if(focalLength != '') setting = focalLength + 'mm';
	if(fValue != '') setting = setting + ' F' + fValue;
	if(shutterSpeed != '') setting = setting + ' ' + shutterSpeed + 'sec';
	if(iso != '') setting = setting + ' iso' + iso;
	setting_tag.textContent = setting;
};

function editPhoto() {
	const photo_setting = document.getElementsByClassName('photo_setting')[0];
	photo_setting.submit();
}

async function deletePhoto(obj) {
	if(!window.confirm('写真を削除してもよろしいですか？')) return;
	
	obj.disabled = 'disabled';
	
	const csrf_token = document.getElementsByName('_csrf')[0].value;
	const AccountNo = document.getElementsByName('accountNo')[0];
	const PhotoNo = document.getElementsByName('photoNo')[0];
	const ImageFilePath = document.getElementsByName('imageFilePath')[0];
	const postUrl = document.getElementById('photo_delete_url').value;
	
	const requestData = {
		accountNo: AccountNo.value,
		photoNo: PhotoNo.value,
		imageFilePath: ImageFilePath.src
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

	const response = await fetch(postUrl, otherParam)
		.then((response) => response.json())
  		.catch((err) => console.error(`Fetch problem: ${err.message}`));
  	
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
  	
  	modalOpen();
}

async function addFavorite() {
	const csrf_token = document.getElementsByName('_csrf')[0].value;
	const account_no = document.getElementsByClassName('accountNo')[0].value;
	const photo_no = document.getElementsByClassName('photoNo')[0].value;
	
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
	
	const favorite = document.getElementsByClassName('favorite')[0];
	favorite.style.display = 'block';
	const notFavorite = document.getElementsByClassName('notFavorite')[0];
	notFavorite.style.display = 'none';
}

async function cancelFavorite() {
	const csrf_token = document.getElementsByName('_csrf')[0].value;
	const account_no = document.getElementsByClassName('accountNo')[0].value;
	const photo_no = document.getElementsByClassName('photoNo')[0].value;

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
	
	const favorite = document.getElementsByClassName('favorite')[0];
	favorite.style.display = 'none';
	const notFavorite = document.getElementsByClassName('notFavorite')[0];
	notFavorite.style.display = 'block';
}

function modalOpen() {
	const modal = document.querySelector('.js-modal');
	modal.classList.add('is-active');
	sleepSetTimeout(5000, () => modalClose());
}

function modalClose() {
	const modal = document.querySelector('.js-modal');
	modal.classList.remove('is-active');
	
	let photoListForm = document.getElementById('photo_list');
	photoListForm.submit();
}

function sleepSetTimeout(ms, callback) {
  setTimeout(callback, ms);
}