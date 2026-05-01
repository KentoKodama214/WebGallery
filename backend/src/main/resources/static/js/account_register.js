function blurAccountId(obj) {
	validateAccountId(obj);
}

function validateAccountId(obj) {
	const account_id = obj.value;
	const error = obj.parentNode.getElementsByClassName('account_id_error')[0];
	
	if(account_id.length == 0) {
		obj.style.borderColor = 'red';
		error.style.visibility = 'visible';
		error.innerText="アカウントIDを入力してください";
		return false;
	}
	if(!account_id.match('^([a-zA-Z0-9]{1,})$')){
		obj.style.borderColor = 'red';
		error.style.visibility = 'visible';
		error.innerText="半角英数字で入力してください";
		return false;
	}
	if(account_id.length < 8 || account_id.length > 16) {
		obj.style.borderColor = 'red';
		error.style.visibility = 'visible';
		error.innerText="8〜16文字で入力してください";
		return false;
	}
	
	obj.style.borderColor = '#ddd';
	error.style.visibility = 'hidden';
	error.innerText="";
	
	return true;
}

function blurAccountName(obj) {
	validateAccountName(obj);
}

function validateAccountName(obj) {
	const account_name = obj.value;
	const error = obj.parentNode.getElementsByClassName('account_name_error')[0];
	
	if(account_name.length == 0) {
		obj.style.borderColor = 'red';
		error.style.visibility = 'visible';
		error.innerText="アカウント名を入力してください";
		return false;
	}
	if(account_name.trim().length == 0) {
		obj.style.borderColor = 'red';
		error.style.visibility = 'visible';
		error.innerText="スペースのみは入力できません";
		return false;
	}
	
	obj.style.borderColor = '#ddd';
	error.style.visibility = 'hidden';
	error.innerText="";
	
	return true;
}

function blurPassword(obj) {
	validatePassword(obj);
}

function validatePassword(obj) {
	const password = obj.value;
	const error = obj.parentNode.getElementsByClassName('password_error')[0];
	
	if(password.length == 0) {
		obj.style.borderColor = 'red';
		error.style.visibility = 'visible';
		error.innerText="パスワードを入力してください";
		return false;
	}
	if(!password.match('^([a-zA-Z0-9]{1,})$')){
		obj.style.borderColor = 'red';
		error.style.visibility = 'visible';
		error.innerText="半角英数字で入力してください";
		return false;
	}
	if(password.length < 8) {
		obj.style.borderColor = 'red';
		error.style.visibility = 'visible';
		error.innerText="8文字以上で入力してください";
		return false;
	}
	
	obj.style.borderColor = '#ddd';
	error.style.visibility = 'hidden';
	error.innerText="";
	
	return true;
}

function changeBirthdate(obj) {
	validateBirthdate(obj);
}

function validateBirthdate(obj) {
	const birthdate = new Date(obj.value);
	const now = new Date(Date.now());
	const error = obj.parentNode.getElementsByClassName('birthdate_error')[0];
	
	if(birthdate > now){
		obj.style.borderColor = 'red';
		error.style.visibility = 'visible';
		error.innerText="過去の日付で入力してください";
		return false;
	}
	
	obj.style.borderColor = '#ddd';
	error.style.visibility = 'hidden';
	error.innerText="";
	
	return true;
}

async function regist(obj) {
	// for Double Submit Protection
	obj.disabled = 'disabled';
	
	const csrf_token = document.getElementsByName('_csrf')[0].value;	
	const AccountId = document.getElementById('account_id');
	const AccountName = document.getElementById('account_name');
	const Password = document.getElementById('password');
	const Birthdate = document.getElementById('birthdate');
	
	if(!(validateAccountId(AccountId) & validateAccountName(AccountName) 
			& validatePassword(Password) & validateBirthdate(Birthdate))) {
		obj.disabled = null;
		return;
	}
	
	const requestData = {
		accountId: AccountId.value,
		accountName: AccountName.value,
		password: Password.value,
		birthdate: Birthdate.value,
		sexKbn: document.getElementById('sexKbn').value,
		birthplacePrefectureKbnCode: document.getElementById('birthplacePrefectureKbnCode').value,
		residentPrefectureKbnCode: document.getElementById('residentPrefectureKbnCode').value,
		freeMemo: document.getElementById('free_memo').value
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
	const response = await fetch('/api/v1/accounts', otherParam)
		.then(response => response.json())
  		.catch((err) => console.error(`Fetch problem: ${err.message}`));
	
	if(response.isSuccess == false){
		const error = obj.parentNode.getElementsByClassName('account_id_error')[0];
		error.style.visibility = 'visible';
		alert("すでに使われているアカウントIDです。");
		error.innerText="すでに使われているアカウントIDです";
		obj.disabled = null;
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

function modalOpen() {
	const modal = document.querySelector('.js-modal');
	modal.classList.add('is-active');
	sleepSetTimeout(5000, () => modalClose());
}

function modalClose() {
	const modal = document.querySelector('.js-modal');
	modal.classList.remove('is-active');
	
	let loginForm = document.getElementById('login');
	loginForm.submit();
}

function sleepSetTimeout(ms, callback) {
  setTimeout(callback, ms);
}