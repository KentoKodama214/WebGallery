import PhotoSwipeLightbox from './photoswipe-lightbox.esm.js';
import PhotoSwipe from './photoswipe.esm.js';
import PhotoFavorite from './photo_favorite.js';

window.onload = () => {
	const photos = document.getElementsByClassName('p-gallery__item');

	for (const photo of photos) {
		const image = photo.getElementsByClassName('picture')[0];
		photo.setAttribute('data-pswp-width', image.naturalWidth);
		photo.setAttribute('data-pswp-height', image.naturalHeight);
	}
};

function getCurrentPhotoTag() {
	const pswp__items = document.getElementsByClassName('pswp__item');
	let filePath;

	for (const pswp_item of pswp__items) {
		if (pswp_item.getAttribute('aria-hidden') == 'false') {
			filePath = pswp_item.lastElementChild.lastElementChild.getAttribute('src');
		}
	}
	const imageFilePath = document.getElementsByName('imageFilePath');
	for (const tag of imageFilePath) {
		if (tag.value == filePath) {
			return tag.closest('.photo');
		}
	}
}

function getAccountAndPhotoNo() {
	const photo = getCurrentPhotoTag();
	const photo_detail = photo.getElementsByClassName('photo_detail')[0];
	const account_no = photo_detail.getElementsByClassName('accountNo')[0].value;
	const photo_no = photo_detail.getElementsByClassName('photoNo')[0].value;
	
	return [account_no, photo_no];
}

const options = {
	gallery: '#photos',
	children: '.pswp-gallery__item',
	pswpModule: PhotoSwipe,
	padding: { top: 60, bottom: 90, left: 60, right: 60 }
};

const lightbox = new PhotoSwipeLightbox(options);
lightbox.on('uiRegister', function() {
	const favorite = document.getElementsByClassName('favorite');
	if (favorite.length > 0) {
		lightbox.pswp.ui.registerElement({
			name: 'add-favorite-button',
			order: 9,
			isButton: true,
			html: '',
			onInit: (el) => {
				lightbox.pswp.on('change', () => {
					const openPhoto = getCurrentPhotoTag();
					const favorite = openPhoto.getElementsByClassName('favorite')[0];
					if(favorite.style.display == 'block') {
						el.style.display = 'none';
					}
					else {
						el.style.display = 'block';
					}
				});
			},
			onClick: () => {
				const identifier = getAccountAndPhotoNo();
				const photoFavorite = new PhotoFavorite(identifier[0], identifier[1], document.getElementById('csrf_token').value);
				photoFavorite.addFavorite();
				
				const gallary_favorite = document.getElementsByClassName('pswp__button--add-favorite-button')[0];
				gallary_favorite.style.display = 'none';
				const gallary_notFavorite = document.getElementsByClassName('pswp__button--cancel-favorite-button')[0];
				gallary_notFavorite.style.display = 'block';
				
				const photo = getCurrentPhotoTag();
				const photo_detail = photo.getElementsByClassName('photo_detail')[0];
				photo_detail.getElementsByClassName('isFavorite')[0].value = 'true';
				const favorite = photo.getElementsByClassName('favorite')[0];
				favorite.style.display = 'block';
				const notFavorite = photo.getElementsByClassName('notFavorite')[0];
				notFavorite.style.display = 'none';
			}
		});
	}
	const not_favorite = document.getElementsByClassName('notFavorite');
	if (not_favorite.length > 0) {
		lightbox.pswp.ui.registerElement({
			name: 'cancel-favorite-button',
			order: 9,
			isButton: true,
			html: '',
			onInit: (el) => {
				lightbox.pswp.on('change', () => {
					const openPhoto = getCurrentPhotoTag();
					const favorite = openPhoto.getElementsByClassName('favorite')[0];
					if(favorite.style.display == 'none') {
						el.style.display = 'none';
					}
					else {
						el.style.display = 'block';
					}
				});
			},
			onClick: () => {
				const identifier = getAccountAndPhotoNo();
				const photoFavorite = new PhotoFavorite(identifier[0], identifier[1], document.getElementById('csrf_token').value);
				photoFavorite.cancelFavorite();
				
				const gallary_favorite = document.getElementsByClassName('pswp__button--add-favorite-button')[0];
				gallary_favorite.style.display = 'block';
				const gallary_notFavorite = document.getElementsByClassName('pswp__button--cancel-favorite-button')[0];
				gallary_notFavorite.style.display = 'none';

				const photo = getCurrentPhotoTag();
				const photo_detail = photo.getElementsByClassName('photo_detail')[0];
				photo_detail.getElementsByClassName('isFavorite')[0].value = 'false';
				const favorite = photo.getElementsByClassName('favorite')[0];
				favorite.style.display = 'none';
				const notFavorite = photo.getElementsByClassName('notFavorite')[0];
				notFavorite.style.display = 'block';
			}
		});
	}
	lightbox.pswp.ui.registerElement({
		name: 'custom-caption',
		order: 10,
		isButton: false,
		appendTo: 'root',
		html: 'Caption text',
		onInit: (el) => {
			lightbox.pswp.on('change', () => {
				const currSlideElement = lightbox.pswp.currSlide.data.element;
				let captionHTML = '';
				if (currSlideElement) {
					const hiddenCaption = currSlideElement.querySelector('.hidden-caption-content');
					if (hiddenCaption) {
						// get caption from element with class hidden-caption-content
						captionHTML = hiddenCaption.innerHTML;
					} else {
						// get caption from alt attribute
						captionHTML = currSlideElement.querySelector('img').getAttribute('alt');
					}
				}
				el.innerHTML = captionHTML || '';
			});
		},
		onClick: () => {
			const photo = getCurrentPhotoTag();
			const photo_detail = photo.getElementsByClassName('photo_detail')[0];
			photo_detail.submit();
		}
	});
});
lightbox.init();