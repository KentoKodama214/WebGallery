let page_no;
let directionKbnCode;
let isFavorite;
let tagList;
let sortBy;

window.addEventListener("load", function() {
	page_no = 1;
	updateFiltering();
	
	const showMoteText = document.getElementsByClassName('show_mote_text')[0];
	clickShowMore(showMoteText);
});

function clickSearch() {
	const element = document.getElementById('filter_overlay');
	element.classList.toggle('open');
}

function updateFiltering() {
	const DirectionKbnCode = document.getElementsByName('directionKbn')[0];
	directionKbnCode = DirectionKbnCode.options[DirectionKbnCode.selectedIndex].value;
	let directionText = ''
	if(DirectionKbnCode.options[DirectionKbnCode.selectedIndex].label != "") {
		directionText = '、' + DirectionKbnCode.options[DirectionKbnCode.selectedIndex].label;
	}

	const IsFavorite = document.getElementsByName('favoriteOnly')[0];
	let favoriteText = '';
	if(IsFavorite == undefined){
		isFavorite = null;
	} else {
		isFavorite = IsFavorite.options[IsFavorite.selectedIndex].value;
		if(IsFavorite.options[IsFavorite.selectedIndex].label != ""){
			favoriteText = '、' + IsFavorite.options[IsFavorite.selectedIndex].label;
		}
	}
	const TagList = document.getElementsByName('tagList')[0];
	tagList = '';
	let tagListText = '';
	if(TagList.value != "") {
		tagListText = '、"' + TagList.value + '"';
		tagList = TagList.value;
	}
	
	const SortBy = document.getElementsByName('sortBy')[0];
	sortBy = SortBy.options[SortBy.selectedIndex].value;
	let sortByText = SortBy.options[SortBy.selectedIndex].label;
	
	const filterText = document.getElementsByClassName('filter_text')[0];
	filterText.textContent = sortByText + directionText + favoriteText + tagListText;
}

function filtering() {
	closeFilter();
	page_no = 1;

	const photos = document.getElementById('photos');
	let photoList = document.getElementsByClassName('photo');
	for(let i = photoList.length; i > 0 ; i--){
		photos.removeChild(photoList[i-1]);
	}
	updateFiltering();
	
	let isLast = getPhotoList();
	isLast.then((value) => {
		if(value == false) {
			let showMoteText = document.getElementsByClassName('show_mote_text')[0];
			if(showMoteText == undefined) {
				const showMore = document.createElement('div');
				showMore.className = 'show_more';
				showMoteText = document.createElement('span');
				showMoteText.className = 'show_mote_text';
				showMoteText.setAttribute('onclick', 'clickShowMore(this)');
				showMoteText.textContent = '+もっと見る';
				showMore.appendChild(showMoteText);
				photos.after(showMore);
			}
			
			page_no = page_no + 1;
			showMoteText.disabled = null;
		}
		else {
			let showMoteText = document.getElementsByClassName('show_mote_text')[0];
			if(showMoteText != undefined) {
				showMoteText.parentNode.remove();
			}
		}
	});
}

function closeFilter() {
	const element = document.getElementById('filter_overlay');
	element.classList.toggle('open');
}

function clickShowMore(obj) {
	obj.disabled = 'disabled';
	
	let isLast = getPhotoList();
	isLast.then((value) => {
		if(value == false) {
			page_no = page_no + 1;
			obj.disabled = null;
		} else{
			obj.parentNode.remove();
		}
	});
}

async function getPhotoList() {
	const csrf_token = document.getElementsByName('_csrf')[0].value;
	const photoAccountId = document.getElementById('owner_id').value;
	
	const requestData = {
		directionKbn: directionKbnCode,
		tagList: tagList,
		sortBy: sortBy,
		pageNo: page_no
	};
	if(isFavorite != null && isFavorite !== '') {
		requestData.isFavorite = isFavorite;
	}

	const queryParams = new URLSearchParams(requestData).toString();
	const otherParam = {
		headers: {
			"X-CSRF-Token": csrf_token
		},
		method: "GET",
		credentials: "same-origin"
	};

	const response = await fetch('/api/v1/accounts/' + photoAccountId + '/photos?' + queryParams, otherParam)
		.then(response => response.json())
		.catch(err => (console.log(`Fetch problem: ${err.message}`)));
		
	for(photo of response.photoList){
		createPhoto(photo);
	}
	
	return response.isLast;
}

function createPhoto(photo) {
	const photoAccountId = document.getElementById('owner_id').value;
	const isAuthenticated = document.getElementById('is_authenticated');
	const photos = document.getElementById('photos');
	
	const newPhoto = document.createElement('div');
	newPhoto.className = 'photo';
	newPhoto.setAttribute('onmouseover', 'mouseoverPhoto(this)');
	newPhoto.setAttribute('onmouseleave', 'mouseleavePhoto(this)');
	
	const formTag = document.createElement('form');
	formTag.className = 'photo_detail';
	formTag.method = 'get';
	formTag.action = '/photo/' + photoAccountId + '/photo_detail';
	if(photo.directionKbn == 'horizontal') {
		formTag.style.width = '100%';
	} else {
		formTag.style.width = '100px';
	}
	
	const pswpTag = document.createElement('div');
	pswpTag.className = 'pswp-gallery__item';
	
	const imageTag = document.createElement('img');
	imageTag.className = 'picture';
	imageTag.name = 'imageFilePath';
	imageTag.src = photo.imageFilePath;
	imageTag.setAttribute('onclick', 'clickPhoto(this)');
	if(photo.directionKbn == 'horizontal') {
		imageTag.style.top = '45px';
		imageTag.style.width = '100%';
	} else {
		imageTag.style.left = '45px';
		imageTag.style.width = '200px';
	}
	
	imageTag.onload = () => {
		const galleryItemTag = document.createElement('a');
		galleryItemTag.className = 'p-gallery__item';
		galleryItemTag.href = photo.imageFilePath;
		galleryItemTag.target = '_blank';
		galleryItemTag.setAttribute('data-pswp-width', imageTag.naturalWidth);
		galleryItemTag.setAttribute('data-pswp-height', imageTag.naturalHeight);
		galleryItemTag.appendChild(imageTag);
		pswpTag.appendChild(galleryItemTag);
	}
	
	const caption = document.createElement('div');
	caption.className = 'hidden-caption-content';
	
	const captionContent = document.createElement('p');
	captionContent.className = 'caption_content';
	captionContent.textContent = photo.caption;
	caption.appendChild(captionContent);

	const showDetail = document.createElement('p');
	showDetail.className = 'show_detail';
	showDetail.innerText = '詳細';
	caption.appendChild(showDetail);
	pswpTag.appendChild(caption);
	formTag.appendChild(pswpTag);

	const imageFilePathTag = document.createElement('input');
	imageFilePathTag.type = 'hidden';
	imageFilePathTag.name = 'imageFilePath';
	imageFilePathTag.value = photo.imageFilePath;
	formTag.appendChild(imageFilePathTag);

	const accountNoTag = document.createElement('input');
	accountNoTag.type = 'hidden';
	accountNoTag.className = 'accountNo';
	accountNoTag.name = 'accountNo';
	accountNoTag.value = photo.accountNo;
	formTag.appendChild(accountNoTag);

	const photoNoTag = document.createElement('input');
	photoNoTag.type = 'hidden';
	photoNoTag.className = 'photoNo';
	photoNoTag.name = 'photoNo';
	photoNoTag.value = photo.photoNo;
	formTag.appendChild(photoNoTag);

	const isFavoriteTag = document.createElement('input');
	isFavoriteTag.type = 'hidden';
	isFavoriteTag.className = 'isFavorite';
	isFavoriteTag.name = 'isFavorite';
	isFavoriteTag.value = photo.isFavorite;
	formTag.appendChild(isFavoriteTag);
	
	newPhoto.appendChild(formTag);
	
	if(isAuthenticated != undefined){
		const favoriteTag = document.createElement('img');
		favoriteTag.className = 'favorite';
		favoriteTag.src = '/image/heart_on.png';
		favoriteTag.setAttribute('onclick', 'cancelFavorite(this)');
		if(photo.isFavorite == true) {
			favoriteTag.style.display = 'block';
		} else {
			favoriteTag.style.display = 'none';
		}
		newPhoto.appendChild(favoriteTag);
		
		const notfavoriteTag = document.createElement('img');
		notfavoriteTag.className = 'notFavorite';
		notfavoriteTag.src = '/image/heart_off.png';
		notfavoriteTag.setAttribute('onclick', 'addFavorite(this)');
		if(photo.isFavorite == true) {
			notfavoriteTag.style.display = 'none';
		} else {
			notfavoriteTag.style.display = 'block';
		}
		newPhoto.appendChild(notfavoriteTag);
	}
	
	photos.appendChild(newPhoto);
}