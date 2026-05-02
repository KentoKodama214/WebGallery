class PhotoFavorite {
	constructor(account_no, photo_no, csrf_token) {
		this.account_no = account_no;
		this.photo_no = photo_no;
		this.csrf_token = csrf_token;
	}

	async addFavorite() {
		const requestData = {
			favoritePhotoAccountNo: this.account_no,
			favoritePhotoNo: this.photo_no
		};
		const otherParam = {
			headers: {
				"X-CSRF-Token": this.csrf_token,
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
	}

	async cancelFavorite() {
		const requestData = {
			favoritePhotoAccountNo: this.account_no,
			favoritePhotoNo: this.photo_no
		};
		const otherParam = {
			headers: {
				"X-CSRF-Token": this.csrf_token,
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
	}
}

export { PhotoFavorite as default };