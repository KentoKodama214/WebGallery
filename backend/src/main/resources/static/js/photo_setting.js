window.onload = () => {
	const isAddMode = document.getElementById("is_add_mode").value;
	
	if (isAddMode == "false") {
		document.getElementById("image_file").style.display = 'none';
	} else {
		const ImageFile = document.getElementById("image_file");
		ImageFile.setAttribute('required', '');
		ImageFile.value = ''
	}
};

function previewFile() {
	if (!image_file.files[0]) {
		return;
	}

	const maxFileSizeMb = document.getElementById("max_file_size_mb").value;
	const fileLimit = 1024 * 1024 * maxFileSizeMb;
	
	if (image_file.files[0].size > fileLimit) {
		alert("ファイルサイズが" + maxFileSizeMb + "MBを超えています");
		return;
	}

	const reader = new FileReader();
	const image = new Image();

	reader.onload = (function() {
		image.src = reader.result;
		document.getElementById('preview').src = image.src;
		image.onload = function() {
			result = { width: image.naturalWidth, height: image.naturalHeight };
			if (result.width < result.height) {
				document.getElementById("direction_kbn_code").value = "VERTICAL";
			} else if (result.width > result.height) {
				document.getElementById("direction_kbn_code").value = "HORIZONTAL";
			} else {
				document.getElementById("direction_kbn_code").value = "SQUARE";
			}
		}
	});
	reader.readAsDataURL(image_file.files[0]);
}

function validateImageFile(obj) {
	const isAddMode = document.getElementById("is_add_mode").value;
	const error = obj.parentNode.getElementsByClassName('image_file_error')[0];
	
	if (isAddMode == "true" && !obj.files[0]) {
		obj.style.borderColor = 'red';
		error.style.visibility = 'visible';
		error.innerText="ファイルを指定してください";
		return false;
	}
	
	error.style.visibility = 'hidden';
	error.innerText = "";
	
	return true;
}

function blurJapaneseTitle(obj) {
	validateJapaneseTitle(obj);
}

function validateJapaneseTitle(obj) {
	const japanese_title = obj.value;
	const error = obj.parentNode.getElementsByClassName('photoTitle_error')[0];
	
	if(japanese_title.replace('　',' ').trim().length == 0 && japanese_title.length > 0) {
		obj.style.borderColor = 'red';
		error.style.visibility = 'visible';
		error.innerText="スペースのみは入力できません";
		return false;
	}
	
	obj.style.borderColor = '#ddd';
	error.style.visibility = 'hidden';
	error.innerText = "";
	
	return true;
}

function changePhotoAt(obj) {
	validatePhotoAt(obj);
}

function validatePhotoAt(obj) {
	const photo_at = new Date(obj.value);
	const now = new Date(Date.now());
	const error = obj.parentNode.getElementsByClassName('photoAt_error')[0];
	
	if(photo_at > now){
		obj.style.borderColor = 'red';
		error.style.visibility = 'visible';
		error.innerText="過去の日付で入力してください";
		return false;
	}
	
	obj.style.borderColor = '#ddd';
	error.style.visibility = 'hidden';
	error.innerText = "";
	
	return true;
}

function blurFocalLength(obj) {
	validateFocalLength(obj);
}

function validateFocalLength(obj) {
	const focal_length = obj.value;
	const error = obj.parentNode.getElementsByClassName('focalLength_error')[0];
	
	if(focal_length != '' && focal_length <= 0){
		error.style.visibility = 'visible';
		error.innerText="正の数で入力してください";
		return false;
	}
	
	error.style.visibility = 'hidden';
	error.innerText = "";
	
	return true;
}

function blurFValue(obj) {
	validateFValue(obj);
}

function validateFValue(obj) {
	const f_value = obj.value;
	const error = obj.parentNode.getElementsByClassName('fValue_error')[0];

	if(f_value != '' && f_value <= 0){
		error.style.visibility = 'visible';
		error.innerText="正の数で入力してください";
		return false;
	}
	
	error.style.visibility = 'hidden';
	error.innerText = "";
	
	return true;
}

function blurShutterSpeed(obj) {
	validateShutterSpeed(obj);
}

function validateShutterSpeed(obj) {
	const shutter_speed = obj.value;
	const error = obj.parentNode.getElementsByClassName('shutterSpeed_error')[0];
	
	if(shutter_speed != '' && shutter_speed <= 0){
		error.style.visibility = 'visible';
		error.innerText="正の数で入力してください";
		return false;
	}

	error.style.visibility = 'hidden';
	error.innerText = "";
	
	return true;
}

function blurISO(obj) {
	validateISO(obj);
}

function validateISO(obj) {
	const iso = obj.value;
	const error = obj.parentNode.getElementsByClassName('iso_error')[0];
	
	if(iso != '' && iso <= 0){
		error.style.visibility = 'visible';
		error.innerText="正の数で入力してください";
		return false;
	}
	error.style.visibility = 'hidden';
	error.innerText = "";
	
	return true;
}

function blurTag() {
	validateTag();
}

function validateTag() {
	let isCorrect = true;
	const tags = document.getElementsByClassName('tag');
	
	for(const tag of tags) {
		const japanese_tag = tag.getElementsByClassName('tag_japanese')[0];
		const error = tag.getElementsByClassName('tag_error')[0];
		
		if(japanese_tag.value.length == 0) {
			japanese_tag.style.borderColor = 'red';
			error.style.visibility = 'visible';
			error.innerText="タグ（日本語）を入力してください";
			isCorrect = false;
		}
		else if(japanese_tag.value.replace('　',' ').trim().length != japanese_tag.value.length) {
			japanese_tag.style.borderColor = 'red';
			error.style.visibility = 'visible';
			error.innerText="スペースは入力できません";
			
			isCorrect = false;
		}
		else {
			japanese_tag.style.borderColor = '#ddd';
			error.style.visibility = 'hidden';
			error.innerText = "";
		}
	}

	return isCorrect;
}

function addTag(obj) {
	const tags = obj.parentNode;
	const form = tags.parentNode;
	const account_no = form.getElementsByClassName('accountNo')[0].value;
	const photo_no = form.getElementsByClassName('photoNo')[0].value;
	const max_tag_no_tag = form.getElementsByClassName('maxTagNo')[0];
	const max_tag_no = isNaN(parseInt(max_tag_no_tag.value)) ? 0 : parseInt(max_tag_no_tag.value);
		
	const newTag = document.createElement('div');
	newTag.className = 'tag';
	
	const deleteTag = document.createElement('label');
	deleteTag.className='delete_tag';
	deleteTag.innerText = '× 削除';
	deleteTag.setAttribute('onclick','deleteTag(this)');
	newTag.appendChild(deleteTag);
	
	const tagAccountNo = document.createElement('input');
	tagAccountNo.type = 'hidden';
	tagAccountNo.className = 'tag_account_no';
	tagAccountNo.name = 'photoTagRegistRequestList[' + max_tag_no + '].accountNo';
	tagAccountNo.value = account_no;
	newTag.appendChild(tagAccountNo);
	
	const tagPhotoNo = document.createElement('input');
	tagPhotoNo.type = 'hidden';
	tagPhotoNo.className = 'tag_photo_no';
	tagPhotoNo.name = 'photoTagRegistRequestList[' + max_tag_no + '].photoNo';
	tagPhotoNo.value = photo_no;
	newTag.appendChild(tagPhotoNo);
	
	const tagNo = document.createElement('input');
	tagNo.type = 'hidden';
	tagNo.className = 'tag_no';
	tagNo.name = 'photoTagRegistRequestList[' + max_tag_no + '].tagNo';
	newTag.appendChild(tagNo);
	
	const tagJapanese = document.createElement('input');
	tagJapanese.type = 'text';
	tagJapanese.className = 'tag_japanese';
	tagJapanese.name = 'photoTagRegistRequestList[' + max_tag_no + '].tagJapaneseName';
	tagJapanese.placeholder = '日本語';
	tagJapanese.setAttribute('onblur', 'blurTag()');
	newTag.appendChild(tagJapanese);
	
	const tagEnglish = document.createElement('input');
	tagEnglish.type = 'text';
	tagEnglish.className = 'tag_english';
	tagEnglish.name = 'photoTagRegistRequestList[' + max_tag_no + '].tagEnglishName';
	tagEnglish.placeholder = '英語';
	newTag.appendChild(tagEnglish);
	
	const tagError = document.createElement('p');
	tagError.className = 'tag_error';
	newTag.appendChild(tagError);
	
	max_tag_no_tag.value = max_tag_no + 1;
	obj.before(newTag);
}

function deleteTag(obj) {
	obj.parentNode.remove();
}

async function regist(obj) {
	// for Double Submit Protection
	obj.disabled = 'disabled';

	const csrf_token = document.getElementsByName('_csrf')[0].value;
	
	const AccountNo = document.getElementsByName('accountNo')[0];
	const PhotoNo = document.getElementsByName('photoNo')[0];
	const ImageFile = document.getElementsByName('imageFile')[0];
	const ImageFilePath = document.getElementsByName('imageFilePath')[0];
	const DirectionKbnCode = document.getElementsByName('directionKbn')[0];
	const JapaneseTitle = document.getElementsByName('photoJapaneseTitle')[0];
	const EnglishTitle = document.getElementsByName('photoEnglishTitle')[0];
	const PhotoAt = document.getElementsByName('photoAt')[0];
	const FocalLength = document.getElementsByName('focalLength')[0];
	const FValue = document.getElementsByName('fValue')[0];
	const ShutterSpeed = document.getElementsByName('shutterSpeed')[0];
	const Iso = document.getElementsByName('iso')[0];
	const Caption = document.getElementsByName('caption')[0];
	const Tags = document.getElementsByClassName('tag');
	
	if(!(validateImageFile(ImageFile) & validateJapaneseTitle(JapaneseTitle) & validatePhotoAt(PhotoAt)
			& validateFocalLength(FocalLength) & validateFValue(FValue) & validateShutterSpeed(ShutterSpeed) & validateISO(Iso) & validateTag())) {
		obj.disabled = null;
		return;
	}
	
	const isAddMode = document.getElementById("is_add_mode").value;
	const postUrl = document.getElementsByClassName('photo')[0].action;
	
	const formData = new FormData();
	formData.append('accountNo', AccountNo.value);
	formData.append('photoNo', PhotoNo.value);
	formData.append('photoAt', PhotoAt.value);
	formData.append('imageFile', ImageFile.files[0]);
	formData.append('imageFilePath', isAddMode == 'true'  ? '' : ImageFilePath.src);
	formData.append('photoJapaneseTitle', JapaneseTitle.value);
	formData.append('photoEnglishTitle', EnglishTitle.value);
	formData.append('caption', Caption.value);
	formData.append('directionKbn', DirectionKbnCode.value);
	formData.append('focalLength', FocalLength.value);
	formData.append('fValue', FValue.value);
	formData.append('shutterSpeed', ShutterSpeed.value);
	formData.append('iso', Iso.value);
	
	let tag_count = 0;
	for(const tag of Tags) {
		var account_no = tag.getElementsByClassName('tag_account_no')[0].value;
		var photo_no = tag.getElementsByClassName('tag_photo_no')[0].value;
		var tag_no = tag.getElementsByClassName('tag_no')[0].value;
		var japanese_tag = tag.getElementsByClassName('tag_japanese')[0].value;
		var english_tag = tag.getElementsByClassName('tag_english')[0].value;
		
		formData.append('photoTagRegistRequestList[' + tag_count + '].accountNo', account_no);
		formData.append('photoTagRegistRequestList[' + tag_count + '].photoNo', photo_no);
		formData.append('photoTagRegistRequestList[' + tag_count + '].tagNo', tag_no);
		formData.append('photoTagRegistRequestList[' + tag_count + '].tagJapaneseName', japanese_tag);
		formData.append('photoTagRegistRequestList[' + tag_count + '].tagEnglishName', english_tag);
		
		tag_count = ++tag_count;
	}
	
	const otherParam = {
		headers: {
			"X-CSRF-Token": csrf_token,
		},
		method: isAddMode == 'true' ? "POST" : "PUT",
		credentials: "same-origin",
		body: formData
	};
	
	const response = await fetch(postUrl, otherParam)
		.then(response => response.json())
  		.catch((err) => console.error(`Fetch problem: ${err.message}`));
	
	if(response.isSuccess == false) {
		alert(response.message);
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
	obj.disabled = null;
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